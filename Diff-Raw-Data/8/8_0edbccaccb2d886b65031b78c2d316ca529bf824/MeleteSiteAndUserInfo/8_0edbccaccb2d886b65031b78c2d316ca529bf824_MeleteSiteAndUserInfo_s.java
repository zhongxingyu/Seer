 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  * 
  *********************************************************************************/
 
 
 package org.etudes.tool.melete;
 
 import java.io.File;
 import java.util.Map;
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.api.app.melete.ModuleService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.cover.UserDirectoryService;
 
 /**
  * @author Foothill college
  *
  * 5/10/05 - Mallika - added getuploadsize to add max upload size to sessionMap
  * Rashmi - 5/26/05 revised processnavigate() and added refresh of sessionmap
  * Rashmi - 06/13/05 - add commondirlocation() and revised remote location function for the right url
  * Rashmi - 06/23/05 - add editorarchivelocation() to get the full path for applet jar for Mac installs
  * Mallika - 3/7/06 - Removing instr_id from path
  * Rashmi - 09/25/06 - add location for browselinklocation for sferyx editor
  **/
 public class MeleteSiteAndUserInfo {
 
 	/*
 	 * course site properties
 	 */
 	//Names of state attributes corresponding to properties of a site - From siteAction.java
 	private final static String PROP_SITE_TERM = "term";
 
 	/*******************************************************************************
 	* Dependencies
 	*******************************************************************************/
 	/** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(MeleteSiteAndUserInfo.class);
 
     /** Dependency: The Melete Security service. */
     protected MeleteSecurityService meleteSecurityService;
 
     protected ModuleService moduleService;
 
 	private String course_id;
 
 	/**
 	 * constructor added by rashmi
 	 */
 	public MeleteSiteAndUserInfo() {
 	}
 
 	/**
 	 * gets the current user
 	 * @return Returns the user currently logged in to the system
 	 */
 	public User getCurrentUser(){
 	    return UserDirectoryService.getCurrentUser();
 	}
 
 	/**
 	 * gets the current site infromation
 	 * @return Returns the site object
 	 */
 	public Site getCurrenSite()throws Exception{
 		try {
 	    return SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
 		}catch (Exception e) {
 			throw new Exception(e);
 		}
 	}
 
 
 	/**
 	 * gets the assigned term to the course
 	 * @return Returns the course term as Month Year
 	 */
 	public String getCourseTerm() throws Exception{
 		try {
 			return getSiteProperties().getProperty(PROP_SITE_TERM);
 		} catch (Exception e) {
 			throw new Exception(e);
 		}
 	}
 
 	/**
 	 * gets the title of the course
 	 * @return Returns the course title
 	 */
 	public String getCourseTitle()throws Exception{
 		try {
 			return SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getTitle();
 			// return ToolManager.getCurrentPlacement().getTitle();
 		} catch (Exception e) {
 			logger.error(e.toString());
 			throw new Exception(e);
 		}
 	}
 
 	/**
 	 * gets the description of the course
 	 * @return Returns the course description
 	 */
 	public String getCourseDescription()throws Exception{
 		try {
 			 return SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getDescription();
 		//	ToolManager.getCurrentPlacement().g
 		} catch (Exception e) {
 			throw new Exception(e);
 		}
 	}
 
 	/**
 	 * Check if the current user has permission as author.
 	 * @return true if the current user has permission to perform this action, false if not.
 	 */
 	public boolean isUserAuthor()throws Exception{
 
 		try {
 			return meleteSecurityService.allowAuthor();
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	/**
 	 * Check if the current user has permission as student.
 	 * @return true if the current user has permission to perform this action, false if not.
 	 */
 	public boolean isUserStudent()throws Exception{
 		try {
 			return meleteSecurityService.allowStudent();
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	public boolean isSuperUser(){
 	  return meleteSecurityService.isSuperUser(getCurrentUser().getId());
 	}
 
 	/**
 	 * gets the current site id
 	 * @return the current site id
 	 */
 	public String getCurrentSiteId(){
 	    return ToolManager.getCurrentPlacement().getContext();
 	}
 
 	/**
 	 * gets the window name encoded
 	 * @return
 	 */
 	public String getWinEncodeName(){
 		String element = null;
 		String pid = ToolManager.getCurrentPlacement().getId();
 		if (pid != null){
 			element = escapeJavascript("Main" + pid);
 		}
 
 		return element;
 	}
 	
 	public boolean checkAuthorization()
 	{
 		try
 	    {
		  if (isUserStudent())
 		  {
			return false;
 		  }
 		  else
 		  {
			return true;
 		  }
 	    }
 	    catch (Exception e) {
 			logger.error(e.toString());
 	    }
 	    return false;
 	}
 
 	public void populateMeleteSession()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		try {
 			sessionMap.put("courseId", getCurrentSiteId());
 			sessionMap.put("userId", getCurrentUser().getId());
 			setCourse_id( getCurrentSiteId());
 			sessionMap.put("firstName", getCurrentUser().getFirstName());
 			sessionMap.put("lastName", getCurrentUser().getLastName());
 			sessionMap.put("institute", "Foothill College");
             sessionMap.put("maxSize", String.valueOf(getMaxUploadSize()));
 
             logger.debug("Is Author is "+ isUserAuthor());
 
 
 			if (isUserAuthor()){
 				sessionMap.put("role", "INSTRUCTOR");
 
 			}
 			else if (isUserStudent()){
 				sessionMap.put("role", "STUDENT");
 			}
 		}catch (Exception e) {
 				logger.error(e.toString());
 		}
 	}
 
 
 	/**
 	 * Navigates to related landing page based on user is either student or author
 	 * @return the name the page to naviagate
 	 */
 	public String processNavigate(){
 		if (logger.isDebugEnabled()) logger.debug("process navigate is called");
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		try{
 		populateMeleteSession();
 
 		int migrateResult=-1;
 		String beginMigrate = ServerConfigurationService.getString("melete.migrate","false");
 
 	    moduleService.checkInstallation();
 
 		if ((isSuperUser()&& beginMigrate.equals("false")) || ((!isSuperUser()) && isUserAuthor()) || ((!isSuperUser()) && isUserStudent()))
 		{
 				migrateResult = moduleService.getMigrateStatus();
 				try
 				{
 				  if (migrateResult == moduleService.MIGRATE_COMPLETE)
 				  {
 					if (isUserAuthor()) return "list_auth_modules";
 					if (isUserStudent()) return "list_modules_student";
 				  }
 				  if (migrateResult == moduleService.MIGRATE_INCOMPLETE)
 				  {
 					String errMsg = bundle.getString("migration_process_incomplete");
 					context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"migration_process_incomplete",errMsg));
 					return "error_migration";
 				  }
 				}
 				catch (Exception ex)
 				{
 				   logger.error("Some other error in getMigrateStatus");
 				   String errMsg = bundle.getString("migration_process_incomplete");
 				   context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"migration_process_incomplete",errMsg));
 				   return "error_migration";
 				}
 		}
 		else
 		{
 			if (!isSuperUser() && !isUserAuthor() && !isUserStudent())
 			{
 				String errMsg = bundle.getString("access_denied");
 				context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"access_denied",errMsg));
 				return "error_migration";
 			}
 		}
 
 		migrateResult=-1;
 		//Only invoke program for admins
 		if (isSuperUser())
 		{
 
 		  if (beginMigrate.equals("true"))
 		  {
 			logger.info("User is admin, invoking migrateMeleteDocs");
 
 		    try
 		    {
 		      migrateResult = moduleService.migrateMeleteDocs(context.getExternalContext().getInitParameter("meleteDocsDir"));
 		      if (migrateResult == moduleService.MIGRATE_IN_PROCESS)
 		      {
 		    	  String errMsg = bundle.getString("migration_in_process");
 		    	  context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"migration_in_process",errMsg));
 		    	  return "error_migration";
 		      }
 		      if (migrateResult == moduleService.MIGRATE_FAILED)
 		      {
 		    	  String errMsg = bundle.getString("migration_process_fail");
 		    	  context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"migration_process_fail",errMsg));
 		    	  return "error_migration";
 		      }
 //		      if (migrateResult == moduleService.MIGRATE_COMPLETE)
 //			  {
 //				  String successMsg = bundle.getString("migration_process_success");
 //				  context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_INFO,"migration_process_success",successMsg));
 //				  return "list_auth_modules";
 //			  }
 		      if (migrateResult == moduleService.MIGRATE_COMPLETE)
 		    	  return "list_auth_modules";
 		    }
 		    catch (Exception ex)
 		    {
 				String errMsg = bundle.getString("migration_process_fail");
 				context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"migration_process_fail",errMsg));
 				return "error_migration";
 		    }
 		  }
 		}
 
 		} catch (Exception e) {
 			String errMsg = bundle.getString("migration_process_fail");
 			context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"migration_process_fail",errMsg));
 			logger.warn(e.toString());
 		}
 		return "error_migration";
 	}
 
 
 	/**
 	 * gets the season and year of the term
 	 * @return season and year
 	 * @throws Exception
 	 */
 	public String[] getTermSeasonYear() throws Exception {
 		String strterm = getCourseTerm();
 		if (strterm != null && strterm.trim().length() > 0)
 			return strterm.split(" ");
 
 		return null;
 	}
 
 	/**
 	 * to get site properties like term(PROP_SITE_TERM), contactemail etc
 	 * @return
 	 */
 	private ResourceProperties getSiteProperties()throws Exception{
 		ResourceProperties siteProperties;
 		try {
 			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
 			siteProperties = site.getProperties();
 
 			return siteProperties;
 
 		} catch (IdUnusedException e) {
 			throw new Exception(e);
 		}
 	}
 
 	/*
 	 * server location
 	 */
 
 	/**
 	* Return a string based on value that is safe to place into a javascript / html identifier:
 	* anything not alphanumeric change to 'x'. If the first character is not alphabetic, a
 	* letter 'i' is prepended.
 	* @param value The string to escape.
 	* @return value fully escaped using javascript / html identifier rules.
 	*/
 	protected String escapeJavascript(String value){
 		if (value == null || value == "") return "";
 		try
 		{
 			StringBuffer buf = new StringBuffer();
 
 			// prepend 'i' if first character is not a letter
 			if(! java.lang.Character.isLetter(value.charAt(0)))
 			{
 				buf.append("i");
 			}
 
 			// change non-alphanumeric characters to 'x'
 			for (int i = 0; i < value.length(); i++)
 			{
 				char c = value.charAt(i);
 				if (! java.lang.Character.isLetterOrDigit(c))
 				{
 					buf.append("x");
 				}
 				else
 				{
 					buf.append(c);
 				}
 			}
 
 			String rv = buf.toString();
 			return rv;
 		}
 		catch (Exception e)
 		{
 			return value;
 		}
 
 	}	// escapeJavascript
 
 	/*
 	 * server location
 	 */
 	public String getRemoteBrowseLocation()
 	{
 		//String remoteurl = serverConfigurationService.getServerUrl() + "/etudes-melete-tool/melete/remotefilebrowser.jsf";
 		String remoteurl = ServerConfigurationService.getServerUrl() +"/portal/tool/" +ToolManager.getCurrentPlacement().getId() + "/remotefilebrowser#";
 		return remoteurl;
 	}
 
 	public String getRemoteLinkBrowseLocation()
 	{
 		//String remoteurl = serverConfigurationService.getServerUrl() + "/etudes-melete-tool/melete/remotefilebrowser.jsf";
 		String remoteurl = ServerConfigurationService.getServerUrl() +"/portal/tool/" +ToolManager.getCurrentPlacement().getId() + "/remotelinkbrowser#";
 		return remoteurl;
 	}
 
 	public String getMeleteDocsLocation()
 	{
 		String remoteurl = "/access/meleteDocs/content/private/meleteDocs/"+getCurrentSiteId()+"/uploads/";
 	//	String remoteurl = ServerConfigurationService.getServerUrl() + "/access/meleteDocs/content/private/meleteDocs/"+getCurrentSiteId()+"/uploads/";
 
 		return remoteurl;
 	}
 
 	public String getAbsoluteTranslationLocation()
 	{
 		String remoteurl = ServerConfigurationService.getServerUrl();
 		return remoteurl;
 	}
 
 	public String getTranslationFile()
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String translationfile = ServerConfigurationService.getServerUrl() + bundle.getString("translationfile");
 			return translationfile;
 	}
 
 
 	public String getMeleteDocsSaveLocation()
 	{
 		String remoteurl = ServerConfigurationService.getServerUrl() + "/etudes-melete-tool/melete/save.jsf";
 		return remoteurl;
 	}
 
 	public String getCommonDirLocation()
 	{
 		String remoteurl = ServerConfigurationService.getServerUrl() +"/portal/tool/" +ToolManager.getCurrentPlacement().getId() + "/commonfilebrowser#";
 		return remoteurl;
 	}
 
 	public String getEditorArchiveLocation()
 	{
 		String remoteurl = ServerConfigurationService.getServerUrl() + "/etudes-melete-tool/HTMLEditorAppletEnterprise.jar";
 		return remoteurl;
 	}
 
 	public int getMaxUploadSize()
 	{
 		int maxSize = ServerConfigurationService.getInt("content.upload.max", 2);
 		logger.debug("MAX UPLOAD SIZE IS "+maxSize);
 		return maxSize;
 	}
 	/*******************************************************************************
 	* setter methods
 	*******************************************************************************/
 
 
 
 	/**
 	 * @param meleteSecurityService The meleteSecurityService to set.
 	 */
 	public void setMeleteSecurityService(
 			MeleteSecurityService meleteSecurityService) {
 		this.meleteSecurityService = meleteSecurityService;
 	}
 
 	/**
 	 * @param moduleService The moduleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	}
 
 	/**
 	 * @return Returns the course_id.
 	 */
 	public String getCourse_id() {
 		return course_id;
 	}
 	/**
 	 * @param course_id The course_id to set.
 	 */
 	public void setCourse_id(String course_id) {
 		this.course_id = course_id;
 	}
 }
