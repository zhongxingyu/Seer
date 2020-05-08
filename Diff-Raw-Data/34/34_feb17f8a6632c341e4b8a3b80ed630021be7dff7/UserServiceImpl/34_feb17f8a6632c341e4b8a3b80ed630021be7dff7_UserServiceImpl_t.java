 package com.aavu.server.service.impl;
 
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 import org.acegisecurity.context.SecurityContextHolder;
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.apache.log4j.Logger;
 
 import com.aavu.client.domain.Subscription;
 import com.aavu.client.domain.User;
 import com.aavu.client.exception.DuplicateUserException;
 import com.aavu.client.exception.HippoBusinessException;
 import com.aavu.client.exception.PermissionDeniedException;
 import com.aavu.server.dao.UserDAO;
 import com.aavu.server.domain.ServerSideUser;
 import com.aavu.server.service.UserService;
 import com.aavu.server.web.domain.CreateUserRequestCommand;
 
 public class UserServiceImpl implements UserService {
 
 	private static final Logger log = Logger.getLogger(UserServiceImpl.class);
 
 	private static final long CANCELLED_SUBSCRIPTION_ID = 1;
 
 	private UserDAO userDAO;
 
 	public User getCurrentUser() throws UsernameNotFoundException {
 
 		log.debug("getCurrentUser");
 
 		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
 		String username = "";
 		if (obj instanceof UserDetails) {
 			log.debug("instance of UserDetails");
 			username = ((UserDetails)obj).getUsername();			
 		} else {
 			log.debug("not a UserDetail, it's a "+obj.getClass().getName());
 			username = obj.toString();
 		}
 
 		log.debug("loadUserByUsername "+username);
 
 		try {
 			return userDAO.getUserByUsername(username);	
 		} catch (UsernameNotFoundException e) {
			log.debug(e);
 			throw e;
 		}
 
 
 	}
 
 	public User createUser(CreateUserRequestCommand comm) throws DuplicateUserException {
 		
 
 		return createUser(comm.getUsername(),comm.getPassword(),comm.getEmail());
 
 	}
 
 	private User createUser(String username,String userpass,String email) throws DuplicateUserException{
 		return createUser(username,userpass,email,false);
 	}
 
 	private String hashPassword(String password) {
 		String hashword = null;
 		try {
 			MessageDigest md5 = MessageDigest.getInstance("MD5");
 			md5.update(password.getBytes("UTF-8"));
 			BigInteger hash = new BigInteger(1, md5.digest());
 			hashword = hash.toString(16);
 
 		} catch (NoSuchAlgorithmException nsae) {
 			log.error(nsae);
 		} catch (UnsupportedEncodingException e) {
 			log.error(e);
 		}
 
 		return pad(hashword,32,'0');
 	}
 
 	private String pad(String s, int length, char pad) {
 		StringBuffer buffer = new StringBuffer(s);
 		while (buffer.length() < length) {
 			buffer.insert(0, pad);
 		}
 		return buffer.toString();
 	}
 
 
 	/**
 	 * Return if the command has a unique username
 	 */
 	public boolean isUnique(CreateUserRequestCommand comm) {		
 		try{
 			userDAO.loadUserByUsername(comm.getUsername());			
 			return false; 			
 		}catch (UsernameNotFoundException e) {
 			return true;
 		}		
 	}
 
 
 	public List<User> getAllUsers() {
 		return userDAO.getAllUsers();
 	}
 
 	public void setUserDAO(UserDAO userDAO) {
 		this.userDAO = userDAO;
 	}
 
 	/**
 	 * TODO LOW AOP this security concern
 	 */
 	public void toggleEnabled(Integer id) throws PermissionDeniedException {			
 		if(getCurrentUser().isSupervisor()){
 			User user = userDAO.getUserForId(id);
 			user.setEnabled(!user.isEnabled());
 			userDAO.save(user);
 		}else{
 			throw new PermissionDeniedException("You don't have rights to do that.");
 		}
 	}
 
 	public void delete(Integer id) throws PermissionDeniedException {
 		if(getCurrentUser().isSupervisor()){
 			User user = userDAO.getUserForId(id);
 			userDAO.delete(user);
 		}else{
 			throw new PermissionDeniedException("You don't have rights to do that.");
 		}
 	}
 
 	public void toggleSupervisor(Integer id) throws PermissionDeniedException {
 		if(getCurrentUser().isSupervisor()){
 			System.out.println("ID "+id);
 			User user = userDAO.getUserForId(id);
 			user.setSupervisor(!user.isSupervisor());
 			userDAO.save(user);
 		}else{
 			throw new PermissionDeniedException("You don't have rights to do that.");
 		}
 	}
 
 	/**
 	 * lowercase usernames before creation
 	 * @return 
 	 */
 	public User createUser(String username, String userpass, String email, boolean superV) {
 
 		//hmm a bit odd having the logic catc in the 
 		//
 		log.debug("u: "+username+" p "+userpass);
 		log.debug("pp: "+hashPassword(userpass));
 
 		User user = new User();
 		user.setUsername(username.toLowerCase());
 		user.setPassword(hashPassword(userpass));
 		user.setEmail(email);
 		user.setSupervisor(superV);
 
 		return userDAO.save(user);
 		
 
 	}
 
 	public void changeToSubscriptionAndSave(User user, Subscription subscription, String paypalID) {
 		
 		user.setPaypalID(paypalID);
 		
 		user.setSubscription(subscription);
 		userDAO.save(user);
 		
 	}
 
 
 	/**
 	 * Remove the paypal ID so they can switch to a different paypal account by cancelling,
 	 * then re-signing up.
 	 */	
 	public void subscriptionCancel(String paypalID) {
 		Subscription cancelled = userDAO.getSubscriptionByID(CANCELLED_SUBSCRIPTION_ID);
 		
 		User user = userDAO.getForPaypalID(paypalID);
 		
 		changeToSubscriptionAndSave(user, cancelled,"");
 	}
 
 	/**
 	 * TODO do something.
 	 * NOTE: this req will often be received _before_ subscriptionNewSignup
 	 * 
 	 */
 	public void subscriptionRecordPayment(long hippoID,String paypalID) {
 		
 		User userWithThisPaypal = userDAO.getForPaypalID(paypalID);
 		User userToProcess = userDAO.getUserForId(hippoID);
 		
 		log.info(userToProcess.getId()+" "+userToProcess.getUsername()+" "+paypalID+" PAID!");	
 	}
 
 	/**
 	 * Currently blocking signups for multiple users from 1 paypal account.
 	 * This is because on cancel, we only receive the paypalID and if there are
 	 * multiples, we wouldn't know who to cancel.
 	 */
 	public void subscriptionNewSignup(long hippoID,String paypalID, long subscriptionID,String userEmail) throws HippoBusinessException {
 		
 		User userToProcess = userDAO.getUserForId(hippoID);
 		
 		User otherUserWithThisPaypal = userDAO.getForPaypalID(paypalID);
 		
 		if(otherUserWithThisPaypal != null){
 			throw new HippoBusinessException("Already Have a user for that paypalID");
 		}
 		
 		
 		Subscription subscription = userDAO.getSubscriptionByID(subscriptionID);
 
 		changeToSubscriptionAndSave(userToProcess, subscription,paypalID);
 		
 	}
 
 	public List<Subscription> getAllUpgradeSubscriptions() {
 		return userDAO.getAllUpgradeSubscriptions();
 	}
 
 	/**
 	 * Don't let it go negative.
 	 */
 	public void subtractInvitationFrom(User inviter) {
 		int current = inviter.getInvitations();
 		if(current > 0){
 			inviter.setInvitations(current - 1);
 		}
 		userDAO.save(inviter);
 	}
 	
 
 
 }
