 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 package org.sakaiproject.sdata.services.prfc;
 
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
 import org.sakaiproject.sdata.tool.json.JSONServiceHandler;
 import org.sakaiproject.user.api.PreferencesEdit;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserAlreadyDefinedException;
 import org.sakaiproject.user.api.UserEdit;
 import org.sakaiproject.user.api.UserLockedException;
 import org.sakaiproject.user.api.UserNotDefinedException;
 import org.sakaiproject.user.api.UserPermissionException;
 import org.sakaiproject.user.cover.UserDirectoryService;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.util.StringUtil;
 import org.sakaiproject.time.cover.TimeService;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.user.cover.PreferencesService;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.event.cover.NotificationService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InUseException;
 import org.sakaiproject.exception.PermissionException;
 
 
 /**
  * Handler for Preferences, in JSON form
  * 
  */
 public class PreferencesHandler extends JSONServiceHandler
 {
 	private static final Log log = LogFactory.getLog(PreferencesHandler.class);
 	
 	private static final long serialVersionUID = 1L;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.json.JSONServiceServlet#getServiceDefinitionFactory()
 	 */
 	@Override
 	protected ServiceDefinitionFactory getServiceDefinitionFactory()
 			throws ServletException
 	{
 		return new PreferencesServiceDefinitionFactory();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.ServiceServlet#getServiceDefinitionFactory(javax.servlet.ServletConfig)
 	 */
 	@Override
 	protected ServiceDefinitionFactory getServiceDefinitionFactory(
 			Map<String, String> config) throws ServletException
 	{
 		return new PreferencesServiceDefinitionFactory();
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException 
 	{
 		String saveMode = request.getParameter("savemode");
 		if(saveMode != null)
 		{
 			if(saveMode.equals("savedetail"))
 			{
 				try
 				{
 					saveDetail(request);
 				} 
 				catch (PreferencesPwException e)
 				{
 					log.error(e.getMessage(), e);
 					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 				}
 			}
 			else if(saveMode.equals("savenoti"))
 			{
 				saveNotificationPref(request);
 			}
 		}
 	}
 	
 	private void saveDetail(HttpServletRequest request) throws PreferencesPwException
 	{
 		UserEdit user = null;
 		Session currentSession = SessionManager.getCurrentSession();
 
 		User currentUser = null;
 		
 		try
 		{
 			currentUser = UserDirectoryService.getUser(currentSession.getUserId());
 			user = UserDirectoryService.editUser(currentUser.getId());
 			
 			String oldPw = request.getParameter("currentpw");
 			String newPw = request.getParameter("newpw");
 			String retypePw = request.getParameter("retypepw");
 			String selectedZone = request.getParameter("selected_zone");
 			String selectedLanguage = request.getParameter("seleted_language");
 			log.error(selectedZone);
 			log.error(selectedLanguage);
 			
 			try
 			{
 				if(user.checkPassword(oldPw) && !StringUtil.different(newPw, retypePw))
 				{
 					user.setPassword(newPw);
 				}
 				else if((oldPw != null && !oldPw.trim().equals(""))
 					|| (newPw != null && !newPw.trim().equals(""))
 					|| (retypePw != null && !retypePw.trim().equals("")))
 				{
 					// release the UserEdit object
 					UserDirectoryService.cancelEdit(user);
 					throw new PreferencesPwException("Invalid password saving.");
 				}
 				UserDirectoryService.commitEdit(user);
 			} 
 			catch (UserAlreadyDefinedException uade)
 			{
 				log.error(uade.getMessage(), uade);
 				
 			}
 			
 			// close out the UserEdit object in case we didn't make any changes
 			// to it
 			if (user.isActiveEdit()) {
 				UserDirectoryService.cancelEdit(user);
 			}
 
 			PreferencesEdit prefEdit = null;
 			try
 			{
 				prefEdit = (PreferencesEdit) PreferencesService.edit(user.getId());
 				ResourcePropertiesEdit props = prefEdit.getPropertiesEdit(TimeService.APPLICATION_ID);
 				if(props.getProperty(TimeService.TIMEZONE_KEY) != null && 
 						!((String)props.getProperty(TimeService.TIMEZONE_KEY)).equals(selectedZone))
 				{
 					props.removeProperty(TimeService.TIMEZONE_KEY);
 					props.addProperty(TimeService.TIMEZONE_KEY, selectedZone);
 					PreferencesService.commit(prefEdit);				
 				}
 				else if (props.getProperty(TimeService.TIMEZONE_KEY) == null)
 				{
 					props.addProperty(TimeService.TIMEZONE_KEY, selectedZone);
 					PreferencesService.commit(prefEdit);
 				}
 				props = (ResourcePropertiesEdit) prefEdit.getPropertiesEdit(ResourceLoader.APPLICATION_ID);
 				if(props.getProperty(ResourceLoader.LOCALE_KEY) != null && 
 						!((String)props.getProperty(ResourceLoader.LOCALE_KEY)).equals(selectedLanguage))
 				{
 					props.removeProperty(ResourceLoader.LOCALE_KEY);
 					props.addProperty(ResourceLoader.LOCALE_KEY, selectedLanguage);
 					Locale[] locale = Locale.getAvailableLocales();
 					Locale toSet = null;
 					for (int i = 0; i < locale.length; i++){
 						Locale current = locale[i];
 						if ((current.getLanguage() + "_" + current.getCountry()).equals(selectedLanguage)){
 							toSet = current;
 						}
 					}
 					currentSession.setAttribute(
 							"sakai.locale." + currentUser.getId(), toSet);
 					PreferencesService.commit(prefEdit);				
 				}
 				else if (props.getProperty(ResourceLoader.LOCALE_KEY) == null)
 				{
 					props.addProperty(ResourceLoader.LOCALE_KEY, selectedLanguage);
 					Locale[] locale = Locale.getAvailableLocales();
 					Locale toSet = null;
 					for (int i = 0; i < locale.length; i++){
 						Locale current = locale[i];
 						if ((current.getLanguage() + "_" + current.getCountry()).equals(selectedLanguage)){
 							toSet = current;
 						}
 					}
 					currentSession.setAttribute(
 							"sakai.locale." + currentUser.getId(), toSet);
 					PreferencesService.commit(prefEdit);				
 				}
 				
 				// close out the edit in case it wasn't committed above
 				if (prefEdit.isActiveEdit()) {
 					PreferencesService.cancel(prefEdit);
 				}
 			}
 			catch (IdUnusedException iue)
 			{
 				try
 				{
 					prefEdit = PreferencesService.add(user.getId());
 					ResourcePropertiesEdit props = prefEdit.getPropertiesEdit(TimeService.APPLICATION_ID);
 					props.addProperty(TimeService.TIMEZONE_KEY, selectedZone);
 					props.addProperty(ResourceLoader.LOCALE_KEY, selectedLanguage);
 					PreferencesService.commit(prefEdit);
 				}
 				catch (PermissionException pe1)
 				{
 					log.error(pe1.getMessage(), pe1);
 				}
 				catch (IdUsedException ie1)
 				{
 					log.error(ie1.getMessage(), ie1);
 				}
 			}
 			catch (PermissionException pe)
 			{
 				log.error(pe.getMessage(), pe);
 			} 
 			catch (InUseException ie)
 			{
 				log.error(ie.getMessage(), ie);
 			}
 		}
 		catch (UserNotDefinedException unde)
 		{
 			log.error(unde.getMessage(), unde);
 		} 
 		catch (UserPermissionException upe)
 		{
 			log.error(upe.getMessage(), upe);
 		} 
 		catch (UserLockedException ule)
 		{
 			log.error(ule.getMessage(), ule);
 		}
 		
 	}
 	
 	private class PreferencesPwException extends Exception
 	{
 		public PreferencesPwException(String message) 
 		{
 			super(message);
 	  }
 	}
 	
 	private void saveNotificationPref(HttpServletRequest request) 
 	{
 		String anncNotif = request.getParameter("annc_notif");
 		String mailArchiveNotif = request.getParameter("mailarchive_notif");
 		String resourcesNotif = request.getParameter("resources_notif");
 		String syllabusNotif = request.getParameter("syllabus_notif");
 
 		Session currentSession = SessionManager.getCurrentSession();
 		String currUserId = currentSession.getUserId();
 		
 		PreferencesEdit prefEdit;
 
 		try 
 		{
 			prefEdit = (PreferencesEdit) PreferencesService.edit(currUserId);
 			updateAllPreferences(prefEdit, anncNotif, mailArchiveNotif, 
 					resourcesNotif, syllabusNotif);
 		} 
 		catch (IdUnusedException ide) 
 		{
 			// no preferences exist yet for this user, so we need to create them 
 			// before continuing
 			try
 			{
 				prefEdit = PreferencesService.add(currUserId);
 				updateAllPreferences(prefEdit, anncNotif, mailArchiveNotif, 
 						resourcesNotif, syllabusNotif);
 			}
 			catch (PermissionException pe)
 			{
 				log.error(pe.getMessage(), pe);
 			}
 			catch (IdUsedException ie)
 			{
 				log.error(ie.getMessage(), ie);
 			}
 			
 		} 
 		catch (InUseException iue) 
 		{
 			log.error(iue.getMessage(), iue);
 		}
 		catch (PermissionException pe) 
 		{
 			log.error(pe.getMessage(), pe);
 		}
 		
 
 	}
 	
 	private void updateAllPreferences(PreferencesEdit prefEdit, String anncNotif, String mailArchiveNotif, String resourcesNotif, String syllabusNotif)
 	{
 		// note: we are hard-coding the APPLICATION_IDs here to limit dependencies
 		if (anncNotif != null && anncNotif.trim().length() > 0) 
 		{
 			updateNotificationPreference("sakai:announcement", prefEdit, anncNotif);
 		}
 
 		if (mailArchiveNotif != null && mailArchiveNotif.trim().length() > 0) 
 		{
 			updateNotificationPreference("sakai:mailarchive", prefEdit, mailArchiveNotif);
 		}
 
 		if (resourcesNotif != null && resourcesNotif.trim().length() > 0) 
 		{
 			updateNotificationPreference(ContentHostingService.APPLICATION_ID, prefEdit, resourcesNotif);
 		}
 
 		if (syllabusNotif != null && syllabusNotif.trim().length() > 0) 
 		{
 			updateNotificationPreference("sakai:syllabus", prefEdit, syllabusNotif);
 		}
 
 		PreferencesService.commit(prefEdit);
 	}
 
 	private void updateNotificationPreference(String type, PreferencesEdit edit, String updatedValue) 
 	{
 		ResourcePropertiesEdit props = edit.getPropertiesEdit(NotificationService.PREFS_TYPE + type);
 		props.addProperty(Integer.toString(NotificationService.NOTI_OPTIONAL), updatedValue);
 	}
 }
