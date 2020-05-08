 package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroupRoleContext;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
 import gov.nih.nci.security.dao.RoleSearchCriteria;
 import gov.nih.nci.security.dao.SearchCriteria;
 import gov.nih.nci.security.dao.UserSearchCriteria;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 import gov.nih.nci.security.util.ObjectSetUtil;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * @author Padmaja Vedula
  */
 
 public class StudyCalendarAuthorizationManager {
 	public static final String APPLICATION_CONTEXT_NAME = "study_calendar";
 	public static final String BASE_SITE_PG = "BaseSitePG";
     public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
     public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
     public static final String ASSIGNED_PGS = "ASSIGNED_PGS";
     public static final String AVAILABLE_PGS = "AVAILABLE_PGS";
     public static final String ASSIGNED_PES = "ASSIGNED_PES";
     public static final String AVAILABLE_PES = "AVAILABLE_PES";
     public static final String PARTICIPANT_COORDINATOR_GROUP = "PARTICIPANT_COORDINATOR";
     public static final String SITE_COORDINATOR_GROUP = "SITE_COORDINATOR";
     
     private static Log log = LogFactory.getLog(StudyCalendarAuthorizationManager.class);
     
     private UserProvisioningManager userProvisioningManager;
   
     public void assignProtectionElementsToUsers(List<String> userIds, String protectionElementObjectId) throws Exception
 	{
     	boolean protectionElementPresent = false;
     	ProtectionElement pElement = new ProtectionElement();
 			
 		try { 
 			pElement = userProvisioningManager.getProtectionElement(protectionElementObjectId);
 			protectionElementPresent = true;
 		} catch (CSObjectNotFoundException ex){
 			ProtectionElement newProtectionElement = new ProtectionElement();
 			newProtectionElement.setObjectId(protectionElementObjectId);
 			newProtectionElement.setProtectionElementName(protectionElementObjectId);
 			userProvisioningManager.createProtectionElement(newProtectionElement);
 			pElement = userProvisioningManager.getProtectionElement(protectionElementObjectId);
 			//userProvisioningManager.setOwnerForProtectionElement(protectionElementObjectId, userIds.toArray(new String[0]));
 			//userProvisioningManager.assignOwners(protectionElementObjectId, userIds.toArray(new String[0]));
 		}
 		userProvisioningManager.assignOwners(pElement.getProtectionElementId().toString(), userIds.toArray(new String[0]));
 		/*if (protectionElementPresent)
 		{
 			if (log.isDebugEnabled()) {
 				log.debug(" The given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is present in Database");
 			}
 			for (String userId : userIds)
 			{
 				String userName = getUserObject(userId).getLoginName();
 				if (!(userProvisioningManager.checkOwnership((String)userName, protectionElementObjectId)))
 				{
 					if (log.isDebugEnabled()) {
 						log.debug(" Given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is not owned by " + userName);
 					}
 					userProvisioningManager.setOwnerForProtectionElement((String)userName, protectionElementObjectId, userProvisioningManager.getProtectionElement(protectionElementObjectId).getAttribute());
 				} else {
 					if (log.isDebugEnabled()) {
 						log.debug(" Given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is owned by " + userName);
 					}
 				}
 			
 			}
 		}*/
 	}
     	
     public void assignMultipleProtectionElements(String userId, List<String> protectionElementObjectIds) throws Exception
 	{
     	boolean protectionElementPresent = false;
     	String userName = getUserObject(userId).getLoginName();
 		for (String protectionElementObjectId : protectionElementObjectIds)	{
 			try { 
 				userProvisioningManager.getProtectionElement(protectionElementObjectId);
 				protectionElementPresent = true;
 			} catch (CSObjectNotFoundException ex){
 				ProtectionElement newProtectionElement = new ProtectionElement();
 				newProtectionElement.setObjectId(protectionElementObjectId);
 				newProtectionElement.setProtectionElementName(protectionElementObjectId);
 				userProvisioningManager.createProtectionElement(newProtectionElement);
 				//protection element attribute name is set to be the same as protection element object id
 				userProvisioningManager.setOwnerForProtectionElement(userName, protectionElementObjectId, protectionElementObjectId);
 			}
 		
 			if (protectionElementPresent)
 			{
 				if (log.isDebugEnabled()) {
 					log.debug(" The given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is present in Database");
 				}
 				if (!(userProvisioningManager.checkOwnership(userName, protectionElementObjectId)))
 				{
 					if (log.isDebugEnabled()) {
 						log.debug(" Given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is not owned by " + userName);
 					}
 					userProvisioningManager.setOwnerForProtectionElement(userName, protectionElementObjectId, userProvisioningManager.getProtectionElement(protectionElementObjectId).getAttribute());
 				} else {
 					if (log.isDebugEnabled()) 
 						log.debug(" Given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is owned by " + userName);
 				}
 			}
 		}
 	}
     
     //get users of a group, associated with a protection element, and also those not associated
 	public Map getUsers(String groupName, String protectionElementObjectId) throws Exception {
 		HashMap<String, List> usersMap = new HashMap<String, List>();
 		List<User> usersForRequiredGroup = getUsersForGroup(groupName);
         usersMap = (HashMap) getUserListsForProtectionElement(usersForRequiredGroup, protectionElementObjectId);
 				
 		return usersMap;
 	}
     
 	
 	private Map getUserListsForProtectionElement(List<User> users, String protectionElementObjectId) throws Exception {
 		HashMap<String, List> userHashMap = new HashMap<String, List>();
 		List<User> assignedUsers = new ArrayList<User>();
 		List<User> availableUsers = new ArrayList<User>();
 		
 		for (User user : users)
 		{
 			String userName = user.getLoginName();
 			if (userProvisioningManager.checkOwnership(userName, protectionElementObjectId))
 			{
 				assignedUsers.add(user);
 			} else {
 				availableUsers.add(user);
 			}
 		}
 		userHashMap.put(ASSIGNED_USERS, assignedUsers);
 		userHashMap.put(AVAILABLE_USERS, availableUsers);
 		return userHashMap;
 	}
 	
     public User getUserObject(String id) throws Exception {
     	User user = null;
       	user = userProvisioningManager.getUserById(id);
       	return user;
     }
     
     public void createProtectionGroup(String newProtectionGroup, String parentPG) throws Exception {
     	if (parentPG != null) {
     		ProtectionGroup parentGroupSearch = new ProtectionGroup();
     		parentGroupSearch.setProtectionGroupName(parentPG);
             SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(parentGroupSearch);
     		List parentGroupList = userProvisioningManager.getObjects(protectionGroupSearchCriteria);
     		
     		if (parentGroupList.size() > 0) {
     			ProtectionGroup parentProtectionGroup = (ProtectionGroup) parentGroupList.get(0);
     			ProtectionGroup requiredProtectionGroup = new ProtectionGroup();
     			requiredProtectionGroup.setProtectionGroupName(newProtectionGroup);
     			requiredProtectionGroup.setParentProtectionGroup(parentProtectionGroup);
     			userProvisioningManager.createProtectionGroup(requiredProtectionGroup);
     			if (log.isDebugEnabled()) {
 					log.debug("new protection group created " + newProtectionGroup);
 				}
     		}
     	}
     }
     
     /**
      * Method to retrieve all site protection groups
      * 
      */
     
     public List getSites() throws Exception {
     	List<ProtectionGroup> siteList = new ArrayList<ProtectionGroup>() ;
 		ProtectionGroup protectionGroup = new ProtectionGroup();
         SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroup);
 		List<ProtectionGroup> pgList = userProvisioningManager.getObjects(pgSearchCriteria);
 			
 		if (pgList.size() > 0) {
 			for (ProtectionGroup requiredProtectionGroup : pgList) {
 			   if ((requiredProtectionGroup.getParentProtectionGroup()!=null) && (requiredProtectionGroup.getParentProtectionGroup().getProtectionGroupName().equals(BASE_SITE_PG))) {	
 				   siteList.add(requiredProtectionGroup);
 			   }
 			}
 		}
 
 		return siteList;
     }
     
     /**
      * Method to retrieve a site protection group
      * @param name
      * @return null or site Protection Group
      * 
      */
     
     public ProtectionGroup getSite(String name) throws Exception {
     	ProtectionGroup requiredProtectionGroup = null;
     	
 		ProtectionGroup protectionGroupSearch = new ProtectionGroup();
 		protectionGroupSearch.setProtectionGroupName(name);
 	    SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroupSearch);
 		List<ProtectionGroup> protectionGroupList = userProvisioningManager.getObjects(protectionGroupSearchCriteria);
 			
 		if (protectionGroupList.size() > 0) {
 			requiredProtectionGroup = (ProtectionGroup) protectionGroupList.get(0);
 			
 		}
 		return requiredProtectionGroup;
     }
     
     public List getUsersForGroup(String groupName) throws Exception {
 		List<User> usersForRequiredGroup = new ArrayList<User>(); 
 		User user = new User();
         SearchCriteria userSearchCriteria = new UserSearchCriteria(user);
 		List<User> userList = userProvisioningManager.getObjects(userSearchCriteria);
 		if (userList.size() > 0)
 		{
 			
 		   for (User requiredUser : userList) {
 			   try {
 				   Set groups = userProvisioningManager.getGroups(requiredUser.getUserId().toString());
 				   Set<Group> userGroups = groups;
 				   if (userGroups.size() > 0) {	
 					   Group requiredGroup = (Group) userGroups.toArray()[0];
 					   if (groupName.equals(requiredGroup.getGroupName())) {
 						   usersForRequiredGroup.add(requiredUser);
 					   }
 				   }
 			   } catch (CSObjectNotFoundException cse){
 				   throw cse;
 			   }
 		   
 		   }
 		}
 		return usersForRequiredGroup;
     }
     
     /**
      * Method to retrieve users who have the given protection group assigned to them.
      * (can be used for retrieving site coordinators for site protection groups)
      * @param group
      * @param protectionGroupName
      * @return
      * @throws Exception
      */
     public Map getUserPGLists(String group, String protectionGroupName) throws Exception {
     	HashMap<String, List> usersMap = new HashMap<String, List>();
 		List<User> usersForRequiredGroup = getUsersForGroup(group);
         usersMap = (HashMap) getUserListsForProtectionGroup(usersForRequiredGroup, protectionGroupName);
 				
 		return usersMap;
     	
     }
     
     /**
      * 
      * @param users
      * @param protectionGroupName
      * @return
      * @throws Exception
      */
     
     private Map getUserListsForProtectionGroup(List<User> users, String protectionGroupName) throws Exception {
 		HashMap<String, List> userHashMap = new HashMap<String, List>();
 		List<User> assignedUsers = new ArrayList<User>();
 		List<User> availableUsers = new ArrayList<User>();
 		for (User user : users)
 		{
 			boolean isAssigned = false;
 			String userId = user.getUserId().toString();
 			Set<ProtectionGroupRoleContext> pgRoleContext = userProvisioningManager.getProtectionGroupRoleContextForUser(userId);
 			List<ProtectionGroupRoleContext> pgRoleContextList = new ArrayList(pgRoleContext);
 			if (pgRoleContextList.size() != 0) {
 				for (ProtectionGroupRoleContext pgrc : pgRoleContextList) {
 					if (pgrc.getProtectionGroup().getProtectionGroupName().equals(protectionGroupName)) {
 						assignedUsers.add(user);
 						isAssigned = true;
 						break;
 					} 
 				}
 				if (!isAssigned) {
 					availableUsers.add(user);
 				}
 			} else { 
 				availableUsers.add(user);
 			}
 		}
 		userHashMap.put(ASSIGNED_USERS, assignedUsers);
 		userHashMap.put(AVAILABLE_USERS, availableUsers);
 		return userHashMap;
 	}
     
     public void assignProtectionGroupsToUsers(List<String> userIds, ProtectionGroup protectionGroup, String roleName) throws Exception
 	{
     	Role role = new Role();
 		role.setName(roleName);
 		SearchCriteria roleSearchCriteria = new RoleSearchCriteria(role);
 		List roleList = userProvisioningManager.getObjects(roleSearchCriteria);
 		if (roleList.size() > 0) {
 			Role accessRole = (Role) roleList.get(0);
 			String[] roleIds = new String[] {accessRole.getId().toString()};
 
 			for (String userId : userIds)
 			{
 				userProvisioningManager.assignUserRoleToProtectionGroup(userId, roleIds, protectionGroup.getProtectionGroupId().toString());
 			}
 		}
 	}
     
     public void removeProtectionGroupUsers(List<String> userIds, ProtectionGroup protectionGroup) throws Exception
     {
 	
     	for (String userId : userIds)
     	{
     		userProvisioningManager.removeUserFromProtectionGroup(protectionGroup.getProtectionGroupId().toString(), userId);
     	}
     }
     
     public void assignProtectionElementToPGs(List<String> pgIdsList, String protectionElementId) throws Exception {
     	ProtectionElement requiredPE;
      	try { 
 			requiredPE = userProvisioningManager.getProtectionElement(protectionElementId);
 		} catch (CSObjectNotFoundException ex){
 			ProtectionElement newProtectionElement = new ProtectionElement();
 			newProtectionElement.setObjectId(protectionElementId);
 			newProtectionElement.setProtectionElementName(protectionElementId);
 			userProvisioningManager.createProtectionElement(newProtectionElement);
 			requiredPE = userProvisioningManager.getProtectionElement(protectionElementId);
 		}
 		
 		List<ProtectionGroup> assignedPGs = new ArrayList<ProtectionGroup>();
     	List<String> pgIds = new ArrayList<String>();
     	try 
 		{
     		Long peId = userProvisioningManager.getProtectionElement(protectionElementId).getProtectionElementId();
     		Set<ProtectionGroup> protectionGroupsForPE = userProvisioningManager.getProtectionGroups(peId.toString());
 			for (ProtectionGroup protectionGroupForPE : protectionGroupsForPE) {
 				if (protectionGroupForPE.getParentProtectionGroup() != null) {
 					if (protectionGroupForPE.getParentProtectionGroup().getProtectionGroupName().equals(BASE_SITE_PG)) {
 						assignedPGs.add(protectionGroupForPE);
 					}
 				}
 			}
 		} catch (CSObjectNotFoundException  cse) {
 			if (log.isDebugEnabled()) {
 				log.debug("no assigned protectiongroups for this protection element");
 			}
 		}
 		for (ProtectionGroup assignedPG : assignedPGs) {
 			pgIds.add(assignedPG.getProtectionGroupId().toString());
 		}
 		pgIds.addAll(pgIdsList);
     	userProvisioningManager.assignToProtectionGroups(requiredPE.getProtectionElementId().toString(), pgIds.toArray(new String[0]));
     }
 
     public void registerUrl(String url, List<String> protectionGroups) {
         if (log.isDebugEnabled()) log.debug("Attempting to register PE for " + url + " in " + protectionGroups);
 
         ProtectionElement element = getOrCreateProtectionElement(url);
 
         syncProtectionGroups(element, protectionGroups);
     }
 
     private ProtectionElement getOrCreateProtectionElement(String objectId) {
         ProtectionElement element = null;
         try {
             element = userProvisioningManager.getProtectionElement(objectId);
             log.debug("PE for " + objectId + " found");
         } catch (CSObjectNotFoundException e) {
             log.debug("PE for " + objectId + " not found");
             // continue
         }
         if (element == null) {
             element = new ProtectionElement();
             element.setObjectId(objectId);
             element.setProtectionElementName(objectId);
             element.setProtectionElementDescription("Autogenerated PE for " + objectId);
             try {
                 userProvisioningManager.createProtectionElement(element);
             } catch (CSTransactionException e) {
                 throw new StudyCalendarSystemException("Creating PE for " + objectId + " failed", e);
             }
             try {
                 element = userProvisioningManager.getProtectionElement(element.getObjectId());
             } catch (CSObjectNotFoundException e) {
                 throw new StudyCalendarSystemException("Reloading just-created PE for " + element.getObjectId() + " failed", e);
             }
         }
         return element;
     }
 
     private void syncProtectionGroups(ProtectionElement element, List<String> desiredProtectionGroups) {
         Set<ProtectionGroup> existingGroups;
         try {
             existingGroups = userProvisioningManager.getProtectionGroups(element.getProtectionElementId().toString());
         } catch (CSObjectNotFoundException e) {
             throw new StudyCalendarError("Could not find groups for just-created/loaded PE", e);
         }
         // if they're all the same, we don't need to do anything
         if (existingGroups.size() == desiredProtectionGroups.size()) {
             List<String> existingNames = new ArrayList<String>(existingGroups.size());
             for (ProtectionGroup existingGroup : existingGroups) existingNames.add(existingGroup.getProtectionGroupName());
             if (log.isDebugEnabled()) log.debug(element.getObjectId() + " currently in " + desiredProtectionGroups);
             if (existingNames.containsAll(desiredProtectionGroups)) {
                 log.debug("Sync requires no changes");
                 return;
             }
         }
 
         if (log.isDebugEnabled()) log.debug("Setting groups for " + element.getObjectId() + " to " + desiredProtectionGroups);
         // accumulate IDs from names
         // Seriously -- there's no way to look them up by name
         List<ProtectionGroup> allGroups = userProvisioningManager.getProtectionGroups();
         List<String> desiredGroupIds = new ArrayList<String>(desiredProtectionGroups.size());
         for (ProtectionGroup group : allGroups) {
             if (desiredProtectionGroups.contains(group.getProtectionGroupName())) {
                 desiredGroupIds.add(group.getProtectionGroupId().toString());
             }
         }
         // warn about missing groups, if any
         if (desiredGroupIds.size() != desiredProtectionGroups.size()) {
             List<String> missingGroups = new LinkedList<String>(desiredProtectionGroups);
             for (ProtectionGroup group : allGroups) {
                 String name = group.getProtectionGroupName();
                 if (missingGroups.contains(name)) missingGroups.remove(name);
             }
             log.warn("Requested protection groups included one or more that don't exist:  " + missingGroups + ".  These groups were skipped.");
         }
 
         try {
             userProvisioningManager.assignToProtectionGroups(
                 element.getProtectionElementId().toString(), desiredGroupIds.toArray(new String[0]));
         } catch (CSTransactionException e) {
             throw new StudyCalendarSystemException("Assigning PE " + element.getProtectionElementName() + " to groups " + desiredProtectionGroups + " failed", e);
         }
     }
 
     public Map getProtectionGroups(List<ProtectionGroup> allProtectionGroups, String protectionElementObjectId) throws Exception {
     	HashMap<String, List> pgHashMap = new HashMap<String, List>();
 		List<ProtectionGroup> assignedPGs = new ArrayList<ProtectionGroup>();
 		List<ProtectionGroup> availablePGs = new ArrayList<ProtectionGroup>();
 		try 
 		{
 			Long peId = userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementId();
 			Set<ProtectionGroup> protectionGroupsForPE = userProvisioningManager.getProtectionGroups(peId.toString());
 			for (ProtectionGroup protectionGroupForPE : protectionGroupsForPE) {
 				if (protectionGroupForPE.getParentProtectionGroup() != null) {
 					if (protectionGroupForPE.getParentProtectionGroup().getProtectionGroupName().equals(BASE_SITE_PG)) {
 						assignedPGs.add(protectionGroupForPE);
 					}
 				}
 			}
 		} catch (CSObjectNotFoundException  cse) {
 			if (log.isDebugEnabled()) {
 				log.debug("no assigned protectiongroups for this protection element");
 			}
 		}
     	availablePGs = (List) ObjectSetUtil.minus(allProtectionGroups, assignedPGs);
     	pgHashMap.put(ASSIGNED_PGS, assignedPGs);
     	pgHashMap.put(AVAILABLE_PGS, availablePGs);
 		return pgHashMap;
     	
     }
     
    public Map getPEForUserProtectionGroup(String pgName, String userId) throws Exception {
     	HashMap<String, List> peHashMap = new HashMap<String, List>();
 		List<ProtectionElement> assignedPEs = new ArrayList<ProtectionElement>();
 		List<ProtectionElement> availablePEs = new ArrayList<ProtectionElement>();
 		
		Set<ProtectionElement> allAssignedPEsForPGs = userProvisioningManager.getProtectionElements(this.getSite(pgName).getProtectionGroupId().toString());
 		
 		for (ProtectionElement userPE : allAssignedPEsForPGs) {
 			String userName = getUserObject(userId).getLoginName();
 			if (userProvisioningManager.checkOwnership(userName, userPE.getObjectId()))
 			{
 				assignedPEs.add(userPE);
 			}
 		}
 		
 		availablePEs = (List) ObjectSetUtil.minus(allAssignedPEsForPGs, assignedPEs);
     	peHashMap.put(ASSIGNED_PES, assignedPEs);
     	peHashMap.put(AVAILABLE_PES, availablePEs);
 		return peHashMap;
 		
     }
     
     public List getSitePGsForUser(String userName) throws Exception {
     	List<ProtectionGroup> sites = new ArrayList<ProtectionGroup>();
     	User user = userProvisioningManager.getUser(userName);  
     	Set<ProtectionGroupRoleContext> pgRoleContext = userProvisioningManager.getProtectionGroupRoleContextForUser(user.getUserId().toString());
 		   List<ProtectionGroupRoleContext> pgRoleContextList = new ArrayList<ProtectionGroupRoleContext> (pgRoleContext);
 		   if (pgRoleContextList.size() != 0) {
 			   for (ProtectionGroupRoleContext pgrc : pgRoleContextList) {
 					if (pgrc.getProtectionGroup().getParentProtectionGroup().getProtectionGroupName().equals(BASE_SITE_PG)) {
 						sites.add(pgrc.getProtectionGroup());
 					}
 			   }
 		   }
     	return sites;
     }
     
     public void removeProtectionElementFromPGs(List<String> removePGs, String protectionElementObjectId) throws Exception {
     	List<ProtectionGroup> assignedPGs = new ArrayList<ProtectionGroup>();
     	List<String> pgIds = new ArrayList<String>();
     	ProtectionElement requiredPE;
     	try 
 		{
     		Long peId = userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementId();
     		Set<ProtectionGroup> protectionGroupsForPE = userProvisioningManager.getProtectionGroups(peId.toString());
 			for (ProtectionGroup protectionGroupForPE : protectionGroupsForPE) {
 				if (protectionGroupForPE.getParentProtectionGroup() != null) {
 					if (protectionGroupForPE.getParentProtectionGroup().getProtectionGroupName().equals(BASE_SITE_PG)) {
 						assignedPGs.add(protectionGroupForPE);
 					}
 				}
 			}
 		} catch (CSObjectNotFoundException  cse) {
 			if (log.isDebugEnabled()) {
 				log.debug("no assigned protectiongroups for this protection element");
 			}
 		}
 		for (ProtectionGroup assignedPG : assignedPGs) {
 			pgIds.add(assignedPG.getProtectionGroupId().toString());
 		}
 		List<String> newList = (List) ObjectSetUtil.minus(pgIds, removePGs);
 		requiredPE = userProvisioningManager.getProtectionElement(protectionElementObjectId);
     	userProvisioningManager.assignToProtectionGroups(requiredPE.getProtectionElementId().toString(), newList.toArray(new String[0]));
     }
     
     public List checkOwnership(String userName, List<Study> studies) throws Exception {
     	Group requiredGroup = new Group();
     	List<Study> assignedStudies = new ArrayList<Study>(); 
 		User user = new User();
 		user.setLoginName(userName);
         SearchCriteria userSearchCriteria = new UserSearchCriteria(user);
         List<User> userList = userProvisioningManager.getObjects(userSearchCriteria);
         if (userList.size() > 0)
 		{
         	Set groups = userProvisioningManager.getGroups(userList.get(0).getUserId().toString());
 			Set<Group> userGroups = groups;
 			if (userGroups.size() > 0) {	
 				   requiredGroup = (Group) userGroups.toArray()[0];
 				   if (requiredGroup.getGroupName().equals(PARTICIPANT_COORDINATOR_GROUP)) {
 					   for (Study study : studies) {
 						   if (userProvisioningManager.checkOwnership(userName, study.getClass().getName()+"."+study.getId())) {
 							   assignedStudies.add(study);
 						   }
 					   }
 					   
 				   } else if (requiredGroup.getGroupName().equals(SITE_COORDINATOR_GROUP)) {
 					   List<ProtectionGroup> sites = new ArrayList<ProtectionGroup>();
 				   	   Set<ProtectionGroupRoleContext> pgRoleContext = userProvisioningManager.getProtectionGroupRoleContextForUser(userList.get(0).getUserId().toString());
 					   List<ProtectionGroupRoleContext> pgRoleContextList = new ArrayList<ProtectionGroupRoleContext> (pgRoleContext);
 					   if (pgRoleContextList.size() != 0) {
 						   for (ProtectionGroupRoleContext pgrc : pgRoleContextList) {
 								if (pgrc.getProtectionGroup().getParentProtectionGroup().getProtectionGroupName().equals(BASE_SITE_PG)) {
 									sites.add(pgrc.getProtectionGroup());
 								}
 						   }
 					   }
 					   for (Study study : studies) {
 						   for (ProtectionGroup site : sites) {
 							   Set<ProtectionElement> assignedPEs = userProvisioningManager.getProtectionElements(site.getProtectionGroupId().toString());
 							   for (ProtectionElement pe : assignedPEs) {
 								   if (pe.getObjectId().equals(study.getClass().getName()+"."+study.getId())) {
 									   assignedStudies.add(study);
 								   }
 							   }
 						   }
 					   }
 				   } else {
 					   assignedStudies = studies;
 				   }
 			}  
 		}
         
     	return assignedStudies;
     }
 
     
     ////// CONFIGURATION
     
     public void setUserProvisioningManager(UserProvisioningManager userProvisioningManager) {
         this.userProvisioningManager = userProvisioningManager;
     }
     
 }
 
 
