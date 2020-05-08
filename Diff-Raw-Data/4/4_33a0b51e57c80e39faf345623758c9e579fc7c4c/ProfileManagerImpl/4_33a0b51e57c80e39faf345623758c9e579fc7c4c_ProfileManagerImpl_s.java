 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
 
 package org.sakaiproject.component.app.profile;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.api.app.profile.Profile;
 import org.sakaiproject.api.app.profile.ProfileManager;
 import org.sakaiproject.api.common.edu.person.SakaiPerson;
 import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
 import org.sakaiproject.authz.api.AuthzGroup;
 import org.sakaiproject.authz.api.GroupNotDefinedException;
 import org.sakaiproject.authz.cover.AuthzGroupService;
 import org.sakaiproject.authz.cover.SecurityService;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.tool.api.Placement;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.user.api.UserNotDefinedException;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 
 /**
  * @author rshastri
  */
 public class ProfileManagerImpl implements ProfileManager
 {
 	private static final Log LOG = LogFactory.getLog(ProfileManagerImpl.class);
 
 	/** Dependency: SakaiPersonManager */
 	private SakaiPersonManager sakaiPersonManager;
 
 	/** Dependency: userDirectoryService */
 	private UserDirectoryService userDirectoryService;
 
 	private static final String ANONYMOUS = "Anonymous";
 
 	public void init()
 	{
 		LOG.debug("init()");; // do nothing (for now)
 	}
 
 	public void destroy()
 	{
 		LOG.debug("destroy()");; // do nothing (for now)
 	}
 
 	/**
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#getProfile()
 	 */
 	public Profile getProfile()
 	{
 		LOG.debug("getProfile()");
 
 		return getProfileById(getCurrentUser(), SessionManager.getCurrentSession().getUserId());
 	}
 
 	/**
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#findProfiles(java.lang.String) Returns userMutable profiles only
 	 */
 	public List findProfiles(String searchString)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("findProfiles(" + searchString + ")");
 		}
 		if (searchString == null || searchString.length() < 1)
 			throw new IllegalArgumentException("Illegal searchString argument passed!");
 
 		List profiles = sakaiPersonManager.findSakaiPerson(searchString);
 		List searchResults = new ArrayList();
 		Profile profile;
 
 		if ((profiles != null) && (profiles.size() > 0))
 		{
 			Iterator profileIterator = profiles.iterator();
 
 			while (profileIterator.hasNext())
 			{
 				profile = new ProfileImpl((SakaiPerson) profileIterator.next());
 
 				// Select the user mutable profile for display on if the public information is viewable.
 				if ((profile != null)
 						&& profile.getSakaiPerson().getTypeUuid().equals(sakaiPersonManager.getUserMutableType().getUuid()))
 				{
 					if ((getCurrentUser().equals(profile.getUserId()) || SecurityService.isSuperUser()))
 					{
 						// allow user to search and view own profile and superuser to view all profiles
 						searchResults.add(profile);
 					}
 					else if ((profile.getHidePublicInfo() != null) && (profile.getHidePublicInfo().booleanValue() != true))
 					{
 						if (profile.getHidePrivateInfo() != null && profile.getHidePrivateInfo().booleanValue() != true)
 						{
 							searchResults.add(profile);
 						}
 						else
 						{
 							searchResults.add(getOnlyPublicProfile(profile));
 						}
 
 					}
 				}
 
 			}
 		}
 
 		return searchResults;
 	}
 
 	/**
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#save(org.sakaiproject.api.app.profile.Profile)
 	 */
 	public void save(Profile profile)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("save(" + profile + ")");
 		}
 		if (profile == null) throw new IllegalArgumentException("Illegal profile argument passed!");
 
 		sakaiPersonManager.save(profile.getSakaiPerson());
 	}
 
 	/**
 	 * @param sakaiPersonManager
 	 */
 	public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("setSakaiPersonManager(SakaiPersonManager " + sakaiPersonManager + ")");
 		}
 
 		this.sakaiPersonManager = sakaiPersonManager;
 	}
 
 	/**
 	 * @param userDirectoryService
 	 *        The userDirectoryService to set.
 	 */
 	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("setUserDirectoryService(userDirectoryService " + userDirectoryService + ")");
 		}
 
 		this.userDirectoryService = userDirectoryService;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#getInstitutionalPhotoByUserId(java.lang.String)
 	 */
 	public byte[] getInstitutionalPhotoByUserId(String uid)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("getInstitutionalPhotoByUserId(String " + uid + ")");
 		}
 		return getInstitutionalPhoto(uid, false);
 
 	}
 
 	public byte[] getInstitutionalPhotoByUserId(String uid, boolean siteMaintainer)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("getInstitutionalPhotoByUserId(String" + uid + ", boolean " + siteMaintainer + ")");
 		}
 		return getInstitutionalPhoto(uid, true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#getUserProfileById(java.lang.String)
 	 */
 	public Profile getUserProfileById(String id)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("getUserProfileById(String" + id + ")");
 		}
 		SakaiPerson sakaiPerson = sakaiPersonManager.getSakaiPerson(getAgentUuidByEnterpriseId(id), sakaiPersonManager
 				.getUserMutableType());
 		if (sakaiPerson == null)
 		{
 			return null;
 		}
 		return new ProfileImpl(sakaiPerson);
 	}
 
 	public String getAgentUuidByEnterpriseId(String uid)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug(" getAgentUuidByEnterpriseId(String " + uid + ")");
 		}
 		if (uid == null || (uid != null && uid.trim().length() < 0))
 		{
 			return null;
 		}
 
 		try
 		{
 			User user = userDirectoryService.getUserByEid(uid);
 			return user.getId();
 		}
 		catch (UserNotDefinedException e)
 		{
 			return null;
 		}
 	}
 
 	/**
 	 * @param userUuid
 	 * @return
 	 */
 	public String getEnterpriseIdByAgentUuid(String userUuid)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug(" getEnterpriseIdByAgentUuid(String " + userUuid + ")");
 		}
 		if (userUuid == null || (userUuid != null && userUuid.trim().length() < 0))
 		{
 			return null;
 		}
 
 		try
 		{
 			User user = userDirectoryService.getUser(userUuid);
 			return user.getEid();
 		}
 		catch (UserNotDefinedException e)
 		{
 			LOG.debug("No user found for Uuid" + userUuid);
 			return null;
 		}
 	}
 
 	public boolean displayCompleteProfile(Profile profile)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("displayCompleteProfile(Profile" + profile + ")");
 		}
 		// complete profile visble to Owner and superUser
 		if (profile == null)
 		{
 			return false;
 		}
 		if ((isCurrentUserProfile(profile) || SecurityService.isSuperUser()))
 		{
 			return true;
 		}
 		else if (profile.getHidePrivateInfo() == null)
 		{
 			return false;
 		}
 		if (profile.getHidePublicInfo() == null)
 		{
 			return false;
 		}
 		if (profile.getHidePrivateInfo().booleanValue() != true && profile.getHidePublicInfo().booleanValue() != true)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#isCurrentUserProfile(org.sakaiproject.api.app.profile.Profile)
 	 */
 	public boolean isCurrentUserProfile(Profile profile)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("isCurrentUserProfile(Profile" + profile + ")");
 		}
 		return ((profile != null) && profile.getUserId().equals(getCurrentUser()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#isDisplayPictureURL(org.sakaiproject.api.app.profile.Profile)
 	 */
 	public boolean isDisplayPictureURL(Profile profile)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("isDisplayPictureURL(Profile" + profile + ")");
 		}
 		return (profile != null
 				&& displayCompleteProfile(profile)
 				&& (profile.isInstitutionalPictureIdPreferred() == null || profile.isInstitutionalPictureIdPreferred()
 						.booleanValue() != true) && profile.getPictureUrl() != null && profile.getPictureUrl().trim().length() > 0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#isDisplayUniversityPhoto(org.sakaiproject.api.app.profile.Profile)
 	 */
 	public boolean isDisplayUniversityPhoto(Profile profile)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("isDisplayUniversityPhoto(Profile" + profile + ")");
 		}
 		return (profile != null && displayCompleteProfile(profile) && profile.isInstitutionalPictureIdPreferred() != null
 				&& profile.isInstitutionalPictureIdPreferred().booleanValue() == true
 				&& getInstitutionalPhotoByUserId(profile.getUserId()) != null && getInstitutionalPhotoByUserId(profile.getUserId()).length > 0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#isDisplayUniversityPhotoUnavailable(org.sakaiproject.api.app.profile.Profile)
 	 */
 	public boolean isDisplayUniversityPhotoUnavailable(Profile profile)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("isDisplayUniversityPhotoUnavailable(Profile" + profile + ")");
 		}
 		return (profile != null && displayCompleteProfile(profile) && profile.isInstitutionalPictureIdPreferred() != null
 				&& profile.isInstitutionalPictureIdPreferred().booleanValue() == true
 				&& getInstitutionalPhotoByUserId(profile.getUserId()) == null && (getInstitutionalPhotoByUserId(profile.getUserId()) == null || getInstitutionalPhotoByUserId(profile
 				.getUserId()).length < 1));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#isDisplayNoPhoto(org.sakaiproject.api.app.profile.Profile)
 	 */
 	public boolean isDisplayNoPhoto(Profile profile)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("isDisplayNoPhoto(Profile" + profile + ")");
 		}
 		return (profile == null || !displayCompleteProfile(profile) || (profile.isInstitutionalPictureIdPreferred() == null || (profile
 				.isInstitutionalPictureIdPreferred().booleanValue() != true && (profile.getPictureUrl() == null || profile
 				.getPictureUrl().trim().length() < 1))));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.api.app.profile.ProfileManager#isShowProfileTool(org.sakaiproject.api.app.profile.Profile)
 	 */
 	public boolean isShowTool()
 	{
 		LOG.debug("isShowTool()");
 		// implement isAnonymous later on.
		if(ServerConfigurationService.getString
				("separateIdEid@org.sakaiproject.user.api.UserDirectoryService").equalsIgnoreCase("true"))
 		{
 			return (getProfile().getUserId() != ANONYMOUS && isSiteMember(getProfile().getSakaiPerson().getAgentUuid()));
 		}
 		return (getProfile().getUserId() != ANONYMOUS && isSiteMember(getProfile().getUserId()));
 	}
 
 	private Profile getOnlyPublicProfile(Profile profile)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("getOnlyPublicProfile(Profile" + profile + ")");
 		}
 		profile.getSakaiPerson().setJpegPhoto(null);
 		profile.setPictureUrl(null);
 		profile.setEmail(null);
 		profile.setHomepage(null);
 		profile.setHomePhone(null);
 		profile.setOtherInformation(null);
 		return profile;
 	}
 
 	/**
 	 * Get the id photo if the profile member is site member and the requestor is either site maintainter or user or superuser.
 	 * 
 	 * @param uid
 	 * @param siteMaintainer
 	 * @return
 	 */
 	private byte[] getInstitutionalPhoto(String uid, boolean siteMaintainer)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("getInstitutionalPhotoByUserId(" + uid + ")");
 		}
 		if (uid == null || uid.length() < 1) throw new IllegalArgumentException("Illegal userId argument passed!");
 
 		SakaiPerson sakaiSystemPerson = sakaiPersonManager.getSakaiPerson(uid, sakaiPersonManager.getSystemMutableType());
 		SakaiPerson sakaiPerson = sakaiPersonManager.getSakaiPerson(uid, sakaiPersonManager.getUserMutableType());
 		Profile profile = null;
 
 		if ((sakaiSystemPerson != null))
 		{
 			Profile systemProfile = new ProfileImpl(sakaiSystemPerson);
 			// Fetch current users institutional photo for either the user or super user
 			if (getCurrentUser().equals(uid) || SecurityService.isSuperUser()
 					|| (siteMaintainer && doesCurrentUserHaveUpdateAccessToSite() && isSiteMember(uid)))
 			{
 				LOG.info("Official Photo fetched for userId " + uid);
 				return systemProfile.getInstitutionalPicture();
 			}
 
 			// if the public information && private information is viewable and user uses to display institutional picture id.
 			if (sakaiPerson != null)
 			{
 				profile = new ProfileImpl(sakaiPerson);
 				if (sakaiPerson != null && (profile.getHidePublicInfo() != null)
 						&& (profile.getHidePublicInfo().booleanValue() == false) && profile.getHidePrivateInfo() != null
 						&& profile.getHidePrivateInfo().booleanValue() == false
 						&& profile.isInstitutionalPictureIdPreferred() != null
 						&& profile.isInstitutionalPictureIdPreferred().booleanValue() == true)
 				{
 					LOG.info("Official Photo fetched for userId " + uid);
 					return systemProfile.getInstitutionalPicture();
 				}
 
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param uid
 	 * @return
 	 */
 	private boolean isSiteMember(String uid)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("isSiteMember(String" + uid + ")");
 		}
 		AuthzGroup realm;
 		try
 		{
 			realm = AuthzGroupService.getAuthzGroup("/site/" + getCurrentSiteId());
 			return realm.getUsers().contains(uid);
 		}
 		catch (GroupNotDefinedException e)
 		{
 			LOG.error("IdUnusedException:", e);
 		}
 		return false;
 	}
 
 	/**
 	 * @return
 	 */
 	private String getCurrentSiteId()
 	{
 		LOG.debug("getCurrentSiteId()");
 		Placement placement = ToolManager.getCurrentPlacement();
 		return placement.getContext();
 	}
 
 	/**
 	 * @return
 	 */
 	private boolean doesCurrentUserHaveUpdateAccessToSite()
 	{
 		LOG.debug("doesCurrentUserHaveUpdateAccessToSite()");
 		try
 		{
 			// If the current site is not my workspace of the user and has update access to the site
 
 			return (SiteService.allowUpdateSite(getCurrentSiteId()) && !SiteService.isUserSite(getCurrentSiteId()));
 		}
 		catch (Exception e)
 		{
 			LOG.error(e.getMessage(), e);
 		}
 
 		return false;
 	}
 
 	/**
 	 * @param uid
 	 * @param sessionManagerUserId
 	 * @return
 	 */
 	private Profile getProfileById(String uid, String sessionManagerUserId)
 	{
 		if (LOG.isDebugEnabled())
 		{
 			LOG.debug("getProfileById(" + uid + "," + sessionManagerUserId + ")");
 		}
 		if (uid == null || uid.length() < 1) throw new IllegalArgumentException("Illegal uid argument passed!");
 		if (sessionManagerUserId == null || sessionManagerUserId.length() < 1)
 			throw new IllegalArgumentException("Illegal sessionManagerUserId argument passed!");
 
 		SakaiPerson sakaiPerson = null;
 
 		if ((uid != null) && (uid.trim().length() > 0))
 		{
 			try
 			{
 				User user = userDirectoryService.getUser(sessionManagerUserId);
 
 				sakaiPerson = sakaiPersonManager.getSakaiPerson(user.getId(), sakaiPersonManager.getUserMutableType());
 
 				if (sakaiPerson == null)
 				{
 					sakaiPerson = sakaiPersonManager.create(user.getId(), uid, sakaiPersonManager.getUserMutableType());
 				}
 			}
 			catch (UserNotDefinedException e)
 			{
 				// TODO: how to handle this use case with UserDirectoryService? name? email? password? Why even do it? -ggolden
 				// User user = userDirectoryService.addUser(sessionManagerUserId, "", sessionManagerUserId, "", "", "", null);
 
 				sakaiPerson = sakaiPersonManager.create(sessionManagerUserId, uid, sakaiPersonManager.getUserMutableType());
 			}
 		}
 		return new ProfileImpl(sakaiPerson);
 	}
 
 	/**
 	 * @return
 	 */
 	private String getCurrentUser()
 	{
 		LOG.debug("getCurrentUser()");
 		return SessionManager.getCurrentSession().getUserEid();
 	}
 }
