 /**
  * @author dgeorge
  * 
  * $Id: UserManagerImpl.java,v 1.33 2009-05-28 18:47:31 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.32  2008/06/18 17:53:31  pandyas
  * Removed commented out lines needed for authentication
  *
  * Revision 1.31  2008/05/21 19:03:56  pandyas
  * Modified advanced search to prevent SQL injection
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.30  2007/08/07 15:01:25  pandyas
  * replace log statements
  *
  * Revision 1.29  2007/07/31 12:02:21  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.28  2007/04/17 17:32:42  pandyas
  * Added debug for e-mail from LDAP - testing bug for PM e-mail
  *
  * Revision 1.27  2007/03/27 18:39:41  pandyas
  * changed debug statments to clean up output - done testing changes
  *
  * Revision 1.26  2007/03/20 14:11:11  pandyas
  * Added logging to debug QA tier
  *
  * Revision 1.25  2006/12/22 17:03:32  pandyas
  * Reversed commented out ocde for authentication
  *
  * Revision 1.24  2006/09/14 17:39:52  georgeda
  * solved file not found issue - jboss location was wrong
  *
  * Revision 1.22  2006/08/17 18:15:37  pandyas
  * Defect# 410: Externalize properties files - Code changes to get properties
  *
  * Revision 1.21  2006/05/15 13:39:21  georgeda
  * Cleaned up contact info management
  *
  * Revision 1.20  2006/05/08 13:34:27  georgeda
  * Reformat and clean up warnings
  *
  * Revision 1.19  2006/04/20 19:09:26  georgeda
  * Backed out another change
  *
  * Revision 1.18  2006/04/19 15:08:44  georgeda
  * Uncomment login checking
  *
  * Revision 1.17  2006/04/17 19:11:05  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.16  2005/12/06 22:00:04  georgeda
  * Defect #253, only check roles when there is a username
  *
  * Revision 1.15  2005/12/06 14:50:45  georgeda
  * Defect #253, change the lowecase to the login action so that roles match
  *
  * Revision 1.14  2005/12/05 19:35:17  schroedn
  * Defect #253 - Covert username to all lowercase before validating
  *
  * Revision 1.13  2005/11/29 16:13:04  georgeda
  * Check for null password in login
  *
  * Revision 1.12  2005/11/18 21:05:37  georgeda
  * Defect #130, added superuser
  *
  * Revision 1.11  2005/11/08 22:32:44  georgeda
  * LDAP changes
  *
  * Revision 1.10  2005/11/07 13:58:29  georgeda
  * Dynamically update roles
  *
  * Revision 1.9  2005/10/24 13:28:06  georgeda
  * Cleanup changes
  *
  * Revision 1.8  2005/10/17 13:10:16  georgeda
  * Get contact information
  *
  * Revision 1.7  2005/10/13 17:00:06  georgeda
  * Cleanup
  *
  * Revision 1.6  2005/09/30 19:49:58  georgeda
  * Make sure user is in db
  *
  * Revision 1.5  2005/09/22 18:55:49  georgeda
  * Get coordinator from user in properties file
  *
  * Revision 1.4  2005/09/22 15:15:17  georgeda
  * More changes
  *
  * Revision 1.3  2005/09/16 15:52:57  georgeda
  * Changes due to manager re-write
  *
  * 
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.UserManager;
 import gov.nih.nci.camod.util.LDAPUtil;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.security.AuthenticationManager;
 import gov.nih.nci.security.SecurityServiceProvider;
 import gov.nih.nci.security.exceptions.CSException;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Implementation of class used to wrap the CSM implementation
  */
 public class UserManagerImpl extends BaseManager implements UserManager {
 	private AuthenticationManager theAuthenticationMgr = null;
 
 	/**
 	 * Constructor gets reference to authorization manager
 	 */
 	UserManagerImpl() {
 		log.debug("Entering UserManagerImpl");
 
 		try {
             log.debug("Entering main try");
 			theAuthenticationMgr = SecurityServiceProvider
 					.getAuthenticationManager(Constants.UPT_CONTEXT_NAME);
             log.debug("theAuthenticationMgrtoString(): " + theAuthenticationMgr.toString());
 		} catch (CSException ex) {
 			log.error("Error getting authentication managers", ex);
 		} catch (Throwable e) {
 			log.error("Error getting authentication managers", e);
 		}
 
 		log.debug("Exiting UserManagerImpl");
 	}
 
 	/**
 	 * Get a list of roles for a user
 	 * 
 	 * @param inUsername
 	 *            the login name of the user
 	 * 
 	 * @return the list of roles associated with the user
 	 * @throws Exception
 	 */
 	public List getRolesForUser(String inUsername) throws Exception {
 		log.debug("Entering getRolesForUser");
 
 		List<String> theRoles = new ArrayList<String>();
 
 		try {
 			Person thePerson = new Person();
 			thePerson.setUsername(inUsername);
 
 			List thePeople = Search.query(thePerson);
 
 			if (thePeople.size() > 0) {
 				thePerson = (Person) thePeople.get(0);
 
 				Set theRoleSet = thePerson.getRoleCollection();
 				Iterator it = theRoleSet.iterator();
 
 				while (it.hasNext()) {
 					Role theRole = (Role) it.next();
 					theRoles.add(theRole.getName());
 				}
 
 				// Check for superuser priv
 				try {
 					// Get from default bundle
 					Properties camodProperties = new Properties();
 					String camodPropertiesFileName = null;
 
 					camodPropertiesFileName = System
 							.getProperty("gov.nih.nci.camod.camodProperties");
 
 					try {
 
 						FileInputStream in = new FileInputStream(
 								camodPropertiesFileName);
 						camodProperties.load(in);
 
 					} catch (FileNotFoundException e) {
 						log.error("Caught exception finding file for properties: ",	e);
 						e.printStackTrace();
 					} catch (IOException e) {
 						log.error("Caught exception finding file for properties: ",	e);
 						e.printStackTrace();
 					}
 					String theSuperusers = camodProperties
 							.getProperty("superuser.usernames");
 
 					StringTokenizer theTokenizer = new StringTokenizer(
 							theSuperusers, ",");
 
 					while (theTokenizer.hasMoreTokens()) {
 						if (theTokenizer.nextToken().equals(inUsername)) {
 							theRoles.add(Constants.Admin.Roles.SUPER_USER);
 							break;
 						}
 					}
 				} catch (Exception e) {
 					log.error("Cannot get superuser information from bundle", e);
 				}
 			} else {
 				throw new IllegalArgumentException("User: " + inUsername
 						+ " not in caMOD database");
 			}
 		} catch (Exception e) {
 			log.error("Unable to get roles for user (" + inUsername + ": ", e);
 			throw e;
 		}
 
 		log.debug("User: " + inUsername + " and roles: " + theRoles);
 		log.debug("Exiting getRolesForUser");
 
 		return theRoles;
 	}
 
 	/**
 	 * Get a list of users for a particular role
 	 * 
 	 * @param inRoleName
 	 *            is the name of the role
 	 * 
 	 * @return the list of users associated with the role
 	 * @throws Exception
 	 */
 	public List<String> getUsersForRole(String inRoleName) throws Exception {
 		log.debug("Entering getUsersForRole");
 
 		List<String> theUsersForRole = new ArrayList<String>();
 
 		Role theRole = new Role();
 		theRole.setName(inRoleName);
 
 		try {
 			List theRoles = Search.query(theRole);
 
 			if (theRoles.size() > 0) {
 				theRole = (Role) theRoles.get(0);
 
 				// Get the users for the role
 				Set<Party> theUsers = theRole.getPartyCollection();
 				Iterator theIterator = theUsers.iterator();
 
 				// Go through the list of returned Party objects
 				while (theIterator.hasNext()) {
 					Object theObject = theIterator.next();
 
 					// Only add when it's actually a person
 					if (theObject instanceof Person) {
 						Person thePerson = (Person) theObject;
 						theUsersForRole.add(thePerson.getUsername());
 					}
 				}
 			} else {
 				log.warn("Role not found in database: " + inRoleName);
 			}
 		} catch (Exception e) {
 			log.error("Unable to get roles for user: ", e);
 			throw e;
 		}
 
 		log.debug("Role: " + inRoleName + " and users: " + theUsersForRole);
 		log.debug("Exiting getUsersForRole");
 
 		return theUsersForRole;
 	}
 
 	/**
 	 * Get an e-mail address for a user
 	 * 
 	 * @param inUsername
 	 *            is the login name of the user
 	 * 
 	 * @return the list of users associated with the role
 	 */
 	public String getEmailForUser(String inUsername) {
 		log.debug("Entering getEmailForUser");
 		log.debug("Username: " + inUsername);
 
 		String theEmail = "";
 
 		try {
 			theEmail = LDAPUtil.getEmailAddressForUser(inUsername);
 			log.debug("<getEmailForUser> theEmail: " + theEmail.toString());
 		} catch (Exception e) {
 			log.debug("Could not fetch user from LDAP", e);
 		}
 
 		log.debug("Exiting getEmailForUser");
 
 		return theEmail;
 	}
 
 	/**
 	 * Update the roles for the current user
 	 */
 	public void updateCurrentUserRoles(HttpServletRequest inRequest) {
 		String theCurrentUser = (String) inRequest.getSession().getAttribute(
 				Constants.CURRENTUSER);
 		try {
 			List theRoles = getRolesForUser(theCurrentUser);
 			inRequest.getSession().setAttribute(Constants.CURRENTUSERROLES,
 					theRoles);
 		} catch (Exception e) {
 			log.debug("Unable to update user roles for " + theCurrentUser, e);
 		}
 	}
 
 	/**
 	 * Get an e-mail address for the coordinator
 	 * 
 	 * @return the list of users associated with the role
 	 */
 	public String getEmailForCoordinator() {
 		log.debug("Entering getEmailForCoordinator");
 
 		String theEmail = "";
 
 		try {
 			// Get from default bundle
 			Properties camodProperties = new Properties();
 			String camodPropertiesFileName = null;
 
 			camodPropertiesFileName = System
 					.getProperty("gov.nih.nci.camod.camodProperties");
 
 			try {
 
 				FileInputStream in = new FileInputStream(
 						camodPropertiesFileName);
 				camodProperties.load(in);
 
 			} catch (FileNotFoundException e) {
 				log.error("Caught exception finding file for properties: ", e);
 				e.printStackTrace();
 			} catch (IOException e) {
 				log.error("Caught exception finding file for properties: ", e);
 				e.printStackTrace();
 			}
 
 			String theCoordinator = camodProperties
 					.getProperty("coordinator.username");
 			log.debug("theCoordinator: " + theCoordinator.toString());
 
 			theEmail = getEmailForUser(theCoordinator);
 		} catch (Exception e) {
 			log.warn("Unable to get coordinator email: ", e);
 		}
 
 		log.debug("Exiting getEmailForCoordinator");
 
 		return theEmail;
 	}
 
 	/**
 	 * Log in a user and get roles.
 	 * 
 	 * @param inUsername
 	 *            is the login name of the user
 	 * @param inPassword
 	 *            password
 	 * @param inRequest
 	 *            Used to store the roles
 	 * 
 	 * @return the list of users associated with the role
 	 */
 	public boolean login(String inUsername, String inPassword,
 			HttpServletRequest inRequest) {
		boolean loginOk = false;
 		try {
             log.debug("login method inside try");
 			// Work around bug in CSM. Empty passwords pass
 			if (inPassword.trim().length() != 0) {
				loginOk = theAuthenticationMgr.login(inUsername, inPassword);
 				// Does the user exist? Must also be in our database to login
 				List theRoles = getRolesForUser(inUsername);
 				inRequest.getSession().setAttribute(Constants.CURRENTUSERROLES,
 						theRoles);
 			}
 		} catch (Exception e) {
 			log.error("Error logging in user: ", e);
 			loginOk = false;
 		}
 
 		return loginOk;
 	}
 }
