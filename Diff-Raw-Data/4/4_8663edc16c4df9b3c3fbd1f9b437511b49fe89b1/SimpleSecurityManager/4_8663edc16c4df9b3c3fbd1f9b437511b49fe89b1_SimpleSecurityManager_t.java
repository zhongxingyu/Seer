 package org.alt60m.security.dbio.manager;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.alt60m.ministry.model.dbio.Person;
 import org.alt60m.security.dbio.model.*;
 import org.alt60m.staffSite.model.dbio.StaffSiteProfile;
 import org.alt60m.staffSite.profiles.dbio.InvalidAccountNumberException;
 import org.alt60m.staffSite.profiles.dbio.MultipleProfilesFoundException;
 import org.alt60m.staffSite.profiles.dbio.ProfileAlreadyExistsException;
 import org.alt60m.staffSite.profiles.dbio.ProfileManagementException;
 import org.alt60m.staffSite.profiles.dbio.ProfileNotFoundException;
 import org.alt60m.staffSite.profiles.dbio.ProfileManager;
 import org.alt60m.cas.CASUser;
 import org.alt60m.gcx.CommunityAdminInterface;
 import org.alt60m.gcx.CommunityAdminInterfaceException;
 import org.alt60m.gcx.ConnexionBar;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * @stereotype tested
  * @testcase org.alt60m.security.manager.test.TestSimpleSecurityManager
  */
 public class SimpleSecurityManager implements SecurityManager {
 
 	private static Log log = LogFactory.getLog(SimpleSecurityManager.class);
 	
 
 	private String membershipGroup = "Members";
 	private String membershipFullGroupName = "CampusStaff:_" + membershipGroup; 
 	
 	// max logins before locked out
 	private int maxFailedLogins = 5;
 	
 	//TODO: change to get init parameter
 
 	public SimpleSecurityManager() {
 
 	}
 
 
 	public void setMaxFailedLogins(int max) {
 		maxFailedLogins = max;
 	}
 	
 	public int getMaxFailedLogins() {
 		return maxFailedLogins;
 	}
 
 	public boolean authenticate(String username, String password)
 			throws UserNotFoundException, UserLockedOutException,
 			SecurityManagerFailedException {
 
 		boolean authenticated = false;
 		boolean lockedOut = false;
 
 		try {
 
 			User user = getUserObjectByUsername(username);
 
 			authenticated = clear2hash(password.getBytes()).equals(
 					user.getPassword());
 			if (authenticated) {
 				user.hadSuccessfulLogin();
 				user.persist();
 			} else {
 				user.hadFailedLogin();
 				user.persist();
 				lockedOut = (user.getLastFailureCnt() > maxFailedLogins);
 			}
 
 			if (lockedOut)
 				throw new UserLockedOutException("User locked out after "
 						+ maxFailedLogins + "attempts");
 
 			return authenticated;
 //			return true; // DO NOT DEPLOY LIKE THIS!!!
 		} catch (UserLockedOutException uloe) {
 			throw uloe;
 		} catch (UserNotFoundException unfe) {
 			throw unfe;
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(
 					"Couldn't access security database for username '"
 							+ username + "'\n" + e);
 		}
 	}
 
 	// added kb 12/19/2002
 	/**
 	 * updates UserEmail address given the userid and email address. returns the
 	 * User object as a bonus (can obviously be ignored if not needed)
 	 */
 	public User updateUsername(int userid, String username)
 			throws SecurityManagerFailedException, UserAlreadyExistsException {
 		User user = null;
 		User userCheck = null;
 		try {
 			user = getUserObject(userid);
 			if (username != null && !username.equals(user.getUsername())) {
 				userCheck = getUserObjectByUsername(username);
 				if (userCheck.isPKEmpty()) {
 					user.setUsername(username);
 					user.persist();
 				} else {
 					throw new UserAlreadyExistsException();
 				}
 			}
 			return user;
 		} catch (UserAlreadyExistsException e1) {
 			throw e1;
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(
 					"Couldn't access security database for user '" + userid
 							+ "'\n" + e);
 		}
 	}
 
 	public boolean userExists(String userName)
 			throws SecurityManagerFailedException {
 		try {
 			getUserObjectByUsername(userName);
 			return true;
 		} catch (UserNotFoundException unfe) {
 			return false;
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 	public String[] listUsers() throws SecurityManagerFailedException {
 		try {
 			org.alt60m.util.StringVector users = new org.alt60m.util.StringVector();
 
 			// Vector results = ob.doQuery("SELECT user FROM
 			// org.alt60m.security.accounts.User user order by username");
 			// TODO: Might need 1=1 added
 			Vector results = new User().selectList("ORDER BY username");
 
 			for (Iterator iUsers = results.iterator(); iUsers.hasNext();) {
 				User user = (User) iUsers.next();
 				users.add(user.getUsername());
 			}
 			return users.toStringArray();
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 	public String[] listUsers(String likeString)
 			throws SecurityManagerFailedException {
 		try {
 			org.alt60m.util.StringVector users = new org.alt60m.util.StringVector();
 
 			// Vector results = ob.doQuery("SELECT user FROM
 			// org.alt60m.security.accounts.User user where user.username like
 			// \"%"+likeString+"\" order by user.username");
 			Vector results = new User().selectList("username like '%"
 					+ likeString + "' order by username");
 
 			for (Iterator iUsers = results.iterator(); iUsers.hasNext();) {
 				User user = (User) iUsers.next();
 				users.add(user.getUsername());
 			}
 			return users.toStringArray();
 
 		} catch (Exception e) {
 			log.error(e, e);
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 	// Added 1/14/2003 by David Bowdoin
 	public String[] listStaffSiteUsers() throws SecurityManagerFailedException {
 		try {
 			org.alt60m.util.StringVector users = new org.alt60m.util.StringVector();
 			// username, email, password, passwordQuestion, passwordAnswer,
 			// lastFailure, lastFailureCnt, lastLogin, createdOn, emailVerified,
 			// userID
 			// Vector results = ob.doQuery("CALL SQL SELECT * FROM (SELECT 
 			//  SSM.username FROM simplesecuritymanager_user SSM
 			// INNER JOIN staffsite_staffsiteprofile SSP ON SSM.username =
 			// SSP.userName ORDER BY SSM.username) tempTable AS
 			// org.alt60m.persistence.castor.util.SingleField");
 			List results = new User()
 					.selectSQLList("SELECT * FROM (SELECT SSM.* FROM simplesecuritymanager_user SSM INNER JOIN staffsite_staffsiteprofile SSP ON SSM.username = SSP.userName ORDER BY SSM.username) tempTable");
 			for (Iterator iUsers = results.iterator(); iUsers.hasNext();) {
 				User user = (User) iUsers.next();
 				users.add(user.getUsername());
 			}
 			return users.toStringArray();
 		} catch (Exception e) {
 			log.error(e, e);
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 	public User createUser(String username, String password)
 			throws UserAlreadyExistsException, SecurityManagerFailedException {
 		return createUser(username, password, "", "");
 	}
 
 	/*
 	 * public void createUser(String username, String password, String
 	 * passwordQ, String passwordA) throws UserAlreadyExistsException,
 	 * SecurityManagerFailedException { createUser(username, username, password,
 	 * "", ""); }
 	 */
 	// public void createUser(String username, String password, String
 	// passwordQ, String passwordA) throws UserAlreadyExistsException,
 	// SecurityManagerFailedException {
 	// try {
 	// try {
 	// getUserObjectByUsername(username);
 	// throw new UserAlreadyExistsException("User '"+username+"' already
 	// exists.");
 	// } catch (UserNotFoundException e) { }
 	//
 	// User user = new User();
 	//
 	// user.setUsername(username);
 	// user.setEmail(username);
 	// user.setPassword( clear2hash(password.getBytes()) );
 	// user.setPasswordQuestion(passwordQ);
 	// user.setPasswordAnswer(passwordA);
 	//
 	// user.setCreatedOn(new Date());
 	// user.persist();
 	// } catch (UserAlreadyExistsException uaee) {
 	// throw uaee;
 	// } catch (Exception e) {
 	// log.error(e, e);
 	// throw new SecurityManagerFailedException(e);
 	// }
 	// }
 	public User createUser(String username, String password, String passwordQ,
 			String passwordA) throws UserAlreadyExistsException,
 			SecurityManagerFailedException {
 		try {
 			try {
 				getUserObjectByUsername(username);
 				throw new UserAlreadyExistsException("User '" + username
 						+ "' already exists.");
 			} catch (UserNotFoundException e) {
 			}
 
 			User user = new User();
 
 			user.setUsername(username);
 			// user.setEmail(email);
 			user.setPassword(clear2hash(password.getBytes()));
 			user.setPasswordQuestion(passwordQ);
 			user.setPasswordAnswer(passwordA);
 
 			user.setCreatedOn(new Date());
 			user.persist();
 			return user;
 		} catch (UserAlreadyExistsException uaee) {
 			throw uaee;
 		} catch (Exception e) {
 			log.error(e, e);
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 	public void removeUser(String username) throws UserNotFoundException,
 			SecurityManagerFailedException {
 		try {
 			User user = getUserObjectByUsername(username); // (User)
 															// ob.getObject(User.class,
 															// username);
 			user.delete();
 		} catch (UserNotFoundException unfe) {
 			throw unfe;
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 	public User resetPassword(String username, String newPassword)
 			throws UserNotFoundException, SecurityManagerFailedException {
 		try {
 			User user = getUserObjectByUsername(username); // (User)
 															// ob.getObject(User.class,
 															// username);
 			user.setPassword(clear2hash(newPassword.getBytes()));
 			user.hadSuccessfulLogin(); // reset counter...
 			user.persist();
 			return user;
 
 		} catch (UserNotFoundException unfe) {
 			throw unfe;
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 	public String getPasswordQuestion(String username)
 			throws UserNotFoundException, SecurityManagerFailedException {
 		try {
 			User user = getUserObjectByUsername(username); // (User)
 															// ob.getObject(User.class,
 															// username);
 			return user.getPasswordQuestion();
 		} catch (UserNotFoundException unfe) {
 			throw unfe;
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 
 	// added 9 October 2002 RDH for convenience's sake
 	public int getUserID(String username) throws UserNotFoundException,
 			SecurityManagerFailedException {
 		try {
 			User user = getUserObjectByUsername(username);
 			return user.getUserID();
 		} catch (UserNotFoundException unfe) {
 			throw unfe;
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(e);
 		}
 	}
 
 	public void resetPasswordQA(String username, String passwordAnswer,
 			String newPassword) throws UserNotFoundException,
 			UserLockedOutException, NotAuthorizedException,
 			SecurityManagerFailedException {
 		boolean lockedOut = false;
 
 		boolean match;
 		try {
 			User user = getUserObjectByUsername(username);// (User)
 															// ob.getObject(User.class,
 															// username);
  
 
 			String realAnswer = user.getPasswordAnswer();
 			if (realAnswer == null) { // we did something stupid somewhere;
 										// don't punish them. This code is going
 										// away anyway.
 				match = true;
 			} else {
 				match = passwordAnswer.trim().equalsIgnoreCase(
 						realAnswer.trim());
 			}
 			if (match) {
 				user.setPassword(clear2hash(newPassword.getBytes()));
 				user.hadSuccessfulLogin();
 			} else {
 				user.hadFailedLogin();
 				lockedOut = (user.getLastFailureCnt() > maxFailedLogins);
 			}
 			user.persist();
 
 		} catch (UserNotFoundException unfe) {
 			throw unfe;
 		} catch (Exception e) {
 			throw new SecurityManagerFailedException(e);
 		}
 
 		if (lockedOut)
 			throw new UserLockedOutException("User locked out after "
 					+ maxFailedLogins + " failed attempts.");
 		if (!match)
 			throw new NotAuthorizedException(
 					"The answer you provided did not match the information stored in our database. Please double-check your information and try again.");
 	}
 
 	public void changePassword(String username, String oldPassword,
 			String newPassword) throws UserNotFoundException,
 			UserLockedOutException, NotAuthorizedException,
 			SecurityManagerFailedException {
 		boolean lockedOut = false;
 		try {
 			User user = getUserObjectByUsername(username); // (User)
 															// ob.getObject(User.class,
 															// username);
 			if (clear2hash(oldPassword.getBytes()).equals(user.getPassword())) {
 				user.setPassword(clear2hash(newPassword.getBytes()));
 				user.hadSuccessfulLogin();
 			} else {
 				user.hadFailedLogin();
 				lockedOut = (user.getLastFailureCnt() > maxFailedLogins);
 				throw new NotAuthorizedException(
 						"Couldn't authenticate to change password using current password");
 			}
 			user.persist();
 		} catch (UserNotFoundException unfe) {
 			throw unfe;
 		} catch (Exception e) {
 			if (e instanceof NotAuthorizedException)
 				throw (NotAuthorizedException) e;
 			else
 				throw new SecurityManagerFailedException(e);
 		}
 		if (lockedOut)
 			throw new UserLockedOutException("User locked out after "
 					+ maxFailedLogins + "attempts");
 	}
 
 	/**
 	 * @param accountNo
 	 * @return The User with the given accountNo
 	 * @throws UserNotFoundException
 	 * @throws ProfileNotFoundException
 	 * @throws MultipleProfilesFoundException
 	 */
 	public User getUserObjectByAccountNo(String accountNo)
 			throws UserNotFoundException, ProfileNotFoundException,
 			MultipleProfilesFoundException { // , ProfileManagementException
 		log.debug("getUserObjectByAccountNo:" + accountNo);
 
 		StaffSiteProfile ssp = getProfileByAccountNo(accountNo);
 
 		User user = new User();
 		user.setUsername(ssp.getUserName());
 
 		if (!user.select()) {
 			log.debug(" not found!");
 			throw new UserNotFoundException("Username '" + ssp.getUserName()
 					+ "' was not found.");
 		}
 
 		return user;
 	}
 
 	/**
 	 * @param username
 	 * @return
 	 * @throws UserNotFoundException
 	 */
 	public User getUserObjectByUsername(String username)
 			throws UserNotFoundException {
 		log.debug("getUserObjectByUsername: " + username);
 		User user = new User();
 		user.setUsername(username);
 
 		if (!user.select()) {
 			log.debug(" not found!");
 			throw new UserNotFoundException("Username '" + username
 					+ "' was not found.");
 		}
 
 		return user;
 	}
 
 	/**
 	 * @param guid
 	 * @return
 	 * @throws UserNotFoundException
 	 */
 	private User getUserObjectByGUID(String guid) throws UserNotFoundException {
 		User user = new User();
 		user.setGloballyUniqueID(guid);
 
 		if (!user.select()) {
 			throw new UserNotFoundException("User GUID: " + guid
 					+ " was not found.");
 		}
 
 		return user;
 	}
 
 	public User getUserObject(int userID) throws UserNotFoundException,
 			Exception {
 		// Do a query to get the user by userID
 		try {
 			log.debug("getUserObjectByUserID:");
 			User user = new User();
 			user.setUserID(userID);
 
 			if (!user.select()) {
 				log.debug("User with userID \"" + userID
 						+ "\" was not found.");
 				throw new UserNotFoundException("User with userID \"" + userID
 						+ "\" was not found.");
 			}
 			return user;
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	public void markEmailAsVerified(int userID) throws UserNotFoundException,
 			Exception {
 		try {
 			log.debug("Getting ssm user: " + userID + " ... ");
 			User user = getUserObject(userID);
 			log.debug("   Marking email as verified ... ");
 			user.setEmailVerified(true);
 			log.debug("   Email has been successfully marked as verified ... ");
 			user.persist();
 		} catch (UserNotFoundException unfe) {
 			throw unfe;
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	private String clear2hash(byte[] clearText)
 			throws java.security.NoSuchAlgorithmException {
 
 		java.security.MessageDigest md = java.security.MessageDigest
 				.getInstance("MD5");
 		byte[] ba = md.digest(clearText);
 		return new String(org.alt60m.util.Base64.encode(ba));
 
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.alt60m.security.dbio.manager.SecurityManager#checkUser(org.alt60m.security.CAS.CASUser)
 	 * 
 	 * Sorry this is so hard to follow...but I don't think there's any 
 	 * good way to do this.
 	 * Matt Drees 11/1/05
 	 * 
 	 */
 	public User checkUser(CASUser user)
 			throws UserNotFoundException, UserNotVerifiedException, SecurityManagerFailedException, SsmUserAlreadyExistsException {
 		
 		String username = user.getUsername();
 		String guid = user.getGUID();
 		String acctNo = user.getAcctNo();
 		
 		// Get User by GUID
 		User ssmUser = null;
 
 		try {
 			
 			ssmUser = getUserObjectByGUID(guid);
 			if (!username.equals(ssmUser.getUsername())) {
 				changeUsername(ssmUser, username);
 			
 			}	
 		} catch (UserNotFoundException e) {
 			// no existing GUID; i.e., first
 			// SSO login
 			
 			log.info("User " + user.getUsername() + " is logging in for first time");
 
 			// check if user is a member of CampusStaff; if so, create a
 			// staffsite account for them
 			try {
 				Collection<String> groups = null;
 				CommunityAdminInterface cai = null;
 				try {
 					cai = new CommunityAdminInterface("CampusStaff");
 					groups = cai.listContainingGroups(user.getGUID());
 					if (groups != null) {
 						log.debug("Groups:");
 						for (String group : groups) {
 							log.debug(group);
 						}
 					}
 				} catch (CommunityAdminInterfaceException caie) {
 					log.error("Membership query exception", caie);
 				}
 				if (groups == null) {
 					// log
 					if (cai.getError() == null) {
 						log.error("Membership query failed; unknown cause");
 					} else {
 						log.warn("Membership query failed: "
 								+ cai.getError());
 					}
 					// throw new SecurityManagerFailedException("Unable to
 					// perform GCX Community membership query");
 				}
 				if (groups != null && groups.contains(membershipFullGroupName)){
 					ssmUser = createProfile(user, guid);
 				}
 				else
 				{	
 					log.debug("User was not part of " + membershipFullGroupName);
 					ssmUser = addLegacy(user, username, acctNo);
 				}
 			}
 			catch (IOException e1)
 			{
 				throw new SecurityManagerFailedException("Unable to authorize", e1);
 			}
 //			catch (CommunityAdminInterfaceException e1)
 //			{
 //				//TODO: log failure
 //				throw new SecurityManagerFailedException("Unable to authorize", e1);
 //			}
 			
 		}
 		
 		return ssmUser;
 	}
 
 
 	/**
 	 * code for "legacy" users; i.e., had a profile, but not one
 	 * created under SSO. Need to find their ssm/profile and
 	 * store their guid.
 	 * @param user
 	 * @param username
 	 * @param acctNo
 	 * @param ssmUser
 	 * @return
 	 * @throws UserNotVerifiedException
 	 * @throws UserNotFoundException
 	 */
 	User addLegacy(CASUser user, String username, String acctNo) throws SsmUserAlreadyExistsException, UserNotVerifiedException, UserNotFoundException {
 
 		
 		log.info("Completing ssm/profile for legacy user: " + user.getUsername());
 		
 		User ssmUser = null;
 		
 		// check that account is verified;
 		if (acctNo == null || acctNo.equals("")) {
 
 			log.info("User " + user.getUsername() + " is not verified.");
 			throw new UserNotVerifiedException();
 		}
 		try {
 			// check for existing username
 			ssmUser = getUserObjectByUsername(username);
 
 			// check for existing profile
 			ProfileManager.getProfile(username);
 
 			// If existing username, update GUID
 			addGUID(ssmUser, user);
 		} catch (UserNotFoundException e1) { 
 			// from getUserObjectByUsername
 			
 			// Special case; some non-uscm people have (existing) uscm.org
 			// logins.
 			// find existing username by matching account numbers
 
 			try {
 				ssmUser = getUserObjectByAccountNo(acctNo);
 				// If existing username, update GUID
 				changeUsername(ssmUser, username);
 				addGUID(ssmUser, user);
 			} catch (ProfileNotFoundException e2) {
 				throw new UserNotFoundException(
 						"Unable to find user; no profile found", e2);
 			} catch (MultipleProfilesFoundException e2) {
 				throw new UserNotFoundException(
 						"Unable to find user; multiple profiles found", e2);
 			}
 		} catch (ProfileNotFoundException e1) {
 			// from ProfileManager.getProfile().
 			// I.e., ssm user exists, but no matching profile. This occurs
 			// if someone had a CRS account that matches their gcx account,
 			// but their staffsite profile doesn't. The result is their crs
 			// login (not their staffsitelogin) gets associated with their
 			// gcx account. This is ok. I think.
 			try {
 				//try getting profile by account number (like above)
 				StaffSiteProfile ssp = getProfileByAccountNo(acctNo);
 				//if successful, change profile username to match ssm
 				ssp.setUserName(username);
 				ssp.persist();
 				addGUID(ssmUser, user);
 			} catch (ProfileNotFoundException e2) {
 				throw new UserNotFoundException(
 						"Unable to find user; no profile found", e2);
 			} catch (MultipleProfilesFoundException e2) {
 				throw new UserNotFoundException(
 						"Unable to find user; multiple profiles found", e2);
 			}
 			
 		} catch (MultipleProfilesFoundException e1) {
 			throw new UserNotFoundException(
 					"Unable to find user: multiple profiles found", e1);
 		} catch (ProfileManagementException e1) {
 			//don't think these actually happen...
 			throw new UserNotFoundException(
 					"Unable to find user due to ProfileManagementException", e1);
 		}
 		return ssmUser;
 	}
 
 
 	/**
 	 * @param user
 	 * @param guid
 	 * @return
 	 * @throws SecurityManagerFailedException
 	 */
 	User createProfile(CASUser user, String guid) throws SecurityManagerFailedException {
 		log.debug("Creating profile for user: " + user.getUsername());
 		User ssmUser;
 		//create a profile
 		ProfileManager pm = new ProfileManager();
 		StaffSiteProfile ssp = new StaffSiteProfile();
 		ssp.setUserName(user.getUsername());
 		ssp.setFirstName(user.getFirstName());
 		ssp.setLastName(user.getLastName());
 		
 		String actNo = user.getAcctNo();
 		ssp.setAccountNo(actNo);
 		ssp.setIsStaff(actNo != null && !actNo.equals(""));
 		
 		//Need to give them an SSM password.  It won't be used to
 		//log into the staffsite, but they may need it for CRS.
 		//However, they can also create a different CRS account,
 		//but they'll have to use a different username, and they
 		//may not like that.  
 		//Hopefully CRS will go over to SSO soon, and this won't
 		//be necessary
 		
 		//First, check if they have an SSM password; use it, if so.
 		
 		ssmUser = new User();
 		ssmUser.setUsername(user.getUsername());
 		String digestedPassword = ""; 
 		if (ssmUser.select())
 		{
 			digestedPassword = ssmUser.getPassword();
 		}
 		String pw = "vonette";
 		StringBuffer profileID = new StringBuffer();
 		StringBuffer validationErrors = new StringBuffer();
 		try {
 			pm.createProfile(ssp, pw, pw, profileID,
 					validationErrors);
 		}
 		catch (InvalidAccountNumberException e1)
 		{
 			log.warn("Account number is invalid; Ignoring: " + e1);
 			//ignore
 		}
 		catch (ProfileAlreadyExistsException e1)
 		{
 			//username is taken.  So just use existing profile.
 			ssp.clear(); //clear other information; name info could be different
 			ssp.setUserName(user.getUsername());
 			if (!ssp.select())
 			{
 				//part of CampusStaff, but not logged in yet, and
 				//has duplicate profiles... not likely to happen.
 				throw new SecurityManagerFailedException("Unhandled situation", e1);
 			}
 			//else, just use existing profile.
 		}
 		catch (ProfileManagementException e1)
 		{
 			throw new SecurityManagerFailedException("Unspecified error", e1);
 		}
 		ssmUser = new User();
 		ssmUser.setUsername(user.getUsername());
 		ssmUser.select();
 		if (!digestedPassword.equals("")) {
 			// restore original password
 			ssmUser.setPassword(digestedPassword);
 		}
 		ssmUser.setGloballyUniqueID(guid);
 		ssmUser.persist();
 		Person p = new Person();
 		p.setToolName("SSM");
 		p.setFk_ssmUserID(ssmUser.getUserID());
 		if (!p.select()) {
 			p.setFirstName(user.getFirstName());
 			p.setLastName(user.getLastName());
 			p.setAccountNo(user.getAcctNo());
 		}
 		// personally, I think this fk should go away
 		p.setFk_StaffSiteProfileID(Integer.parseInt(ssp.getStaffSiteProfileID()));
 		p.persist();
 		return ssmUser;
 	}
 
 	private void addGUID(User ssmUser, CASUser casUser){
 		log.info("Attempting to add user " +
 				casUser.getUsername() + " to CampusStaff");
 		String guid = casUser.getGUID();
 		
 		//first do a proxy request to GCX; this will fail if they've not logged
 		//into gcx yet, but it will put them in the system
 		
 		//	import Connexions Bar
 		String pgtiou = casUser.getPgtIou();
 
 		boolean success = false;
 		if (pgtiou != null) {
 			String content = ConnexionBar.getBar(pgtiou, guid);
 			if (content != null) {
 				success = true;
 				log.info("successfully retrieved connexion bar");
 			} else {
 				log.warn("first attempt failed; trying again...");
 				content = ConnexionBar.getBar(pgtiou, guid);
 				if (content != null) {
 					log.info("successfully retrieved connexion bar");
 					success = true;
 				} else {
 					log.warn("second attempt failed");
 				}
 			}
 		} 
 		else
 		{
 			log.warn("User has no PgtIou; unable to ensure user's existence in GCX");
 		}
 		if (!success) {
 			log.warn("Attempt to get omnibar failed; " +
 					"attempting to add user to CampusStaff anyway");
 		}
 
 		try {
 			CommunityAdminInterface cai = new CommunityAdminInterface(
 					"CampusStaff");
 
 			if (cai.addToGroup(guid, membershipGroup)) {
 				//add guid to ssm
 				ssmUser.setGloballyUniqueID(guid);
 				ssmUser.persist();
 				log.info("Successfully added " + casUser.getUsername() + 
 						" to CampusStaff");
 			}
 			else
 			{
 				// TODO: better log
 				log.error("User not added to CampusStaff: " + cai.getError());
 			}
 		} catch (IOException e) {
 			log.error("IO Exception adding user to CampusStaff", e);
 			log.info("User not added to CampusStaff");
 		} catch (CommunityAdminInterfaceException e) {
 			log.error("Exception adding user to CampusStaff", e);
 			log.info("User not added to CampusStaff");
 		} catch (Exception e) {
 			log.error("Exception adding user to CampusStaff", e);
 			log.info("User not added to CampusStaff");
 		}
 	}
 
 	/**
 	 * @param ssmUser
 	 * @param username
 	 */
 	private void changeUsername(User ssmUser, String username) throws SsmUserAlreadyExistsException{
 		
 		String oldUsername = ssmUser.getUsername();
 		log.info("Changing username from " + oldUsername + " to " + username);
 			
 		User replacedUser = new User();
 		replacedUser.setUsername(username);
 		if (!oldUsername.equalsIgnoreCase(username) && replacedUser.select()) {
 			//usually, occurs when a new staff person is changing their gcx account
 			//name from their campus email address to staff email address, but a
 			//staffsite profile has already been created for them.  Usually this
 			//does not have anything associated with it (person, etc).
 			Person replacedPerson = new Person();
 			
 			replacedPerson.setFk_ssmUserID(replacedUser.getUserID());
 			if (replacedPerson.select()) {
 				//we balk because this is crazy talk, to delete a valid user on another user's say-so
				log.error("Username "+username+" already exists and is in use by Person record "+replacedPerson.getPersonID()+" ("+replacedPerson.getFirstName()+" "+replacedPerson.getLastName()+").  Unable to change " + oldUsername + ".");
				throw new SsmUserAlreadyExistsException("Username "+username+" already exists and is in use by Person record "+replacedPerson.getPersonID()+" ("+replacedPerson.getFirstName()+" "+replacedPerson.getLastName()+").  Unable to change " + oldUsername + ".");
 								
 			}
 			log.warn("To avoid username collision deleting existing user record: " + replacedUser.getUserID());
 			replacedUser.delete();
 		}
 		
 		// Change in SSM
 		ssmUser.setUsername(username);
 		ssmUser.persist();
 
 		// Change in SSP
 		changeProfileUsername(oldUsername, username);
 		
 	}
 
 	private void changeProfileUsername(String oldUsername, String username) {
 		StaffSiteProfile ssp = new StaffSiteProfile();
 		ssp.setUserName(oldUsername);
 		if (ssp.select()) {
 			StaffSiteProfile replacedProfile = new StaffSiteProfile();
 			replacedProfile.setUserName(username);
 			if (!oldUsername.equalsIgnoreCase(username) && replacedProfile.select()){
 				log.warn("To avoid username collision deleting existing profile record: " + replacedProfile.getStaffSiteProfileID());
 				replacedProfile.delete();
 			}
 			
 			ssp.setUserName(username);
 			ssp.persist();
 		}
 	}
 	
 	/**
 	 * @param accountNo
 	 * @return
 	 * @throws ProfileNotFoundException
 	 * @throws MultipleProfilesFoundException
 	 */
 	private StaffSiteProfile getProfileByAccountNo(String accountNo)
 			throws ProfileNotFoundException, // ProfileManagementException,
 			MultipleProfilesFoundException {
 		Collection profiles = null;
 		try {
 			StaffSiteProfile ssp = new StaffSiteProfile();
 			ssp.setAccountNo(accountNo);
 			profiles = ssp.selectList(); 
 			if (profiles.size() == 0) {
 				throw new ProfileNotFoundException(
 						"Profile with account number '" + accountNo
 								+ "' was not found.");
 			} else if (profiles.size() > 1) {
 				throw new MultipleProfilesFoundException(
 						"Multiple profiles with account number '" + accountNo
 								+ "' were found.");
 			}
 		} catch (ProfileNotFoundException e) {
 			throw e;
 		} catch (MultipleProfilesFoundException e) {
 			throw e;
 		}
 		return (StaffSiteProfile) profiles.iterator().next();
 	}
 
 }
