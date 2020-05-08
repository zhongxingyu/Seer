 package fi.iki.photon.longminder;
 
 import java.io.Serializable;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 import fi.iki.photon.longminder.entity.LoginData;
 import fi.iki.photon.longminder.entity.User;
 import fi.iki.photon.longminder.entity.dto.UserDTO;
 
 /**
  * Session Bean UserManagerBean for UserManager for managing anything
  * User object related.
  */
 @Stateless
 @LocalBean
 public class UserManagerBean implements UserManager, Serializable {
 	
 	private static final long serialVersionUID = 1L;
 
 	/** Characteds to create random strings. */
 	
 	private static final char[] characters = "abcdef0123456789".toCharArray();
 
 	@PersistenceContext
     private EntityManager em;
 
     /**
      * Default constructor. 
      */
     public UserManagerBean() {
         
     }
 
     /** Simple ping for testing. */
     
     public String hello() {
     	return "Usermanager says Hello!";
     }
 
     /**
      * Given an UserDTO object, create a new User object based on the
      * values in the object and store it in the Persistence Context.
      * 
      * @return fail if User with the same email exists in the database already.
      */
     
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     public boolean create(UserDTO ud) {
     	if (ud == null) return false;
     	
     	User utmp = findTrueUserForEmail(ud.getEmail());
 
     	if (utmp != null) return false;
     	
     	User u = new User();
     	u.initialize(ud);
     	
     	em.persist(u);
 
     	// Flush to database so that the User instance gets its Id value.
     	em.flush();
 
     	// Transfer the id value and the returnPassword to the UserDTO object.
     	u.initializeDTO(ud);
 
     	return true;
     	
     }
 
     /** Given an email, returns an UserDTO instance filled out with
      * values in the corresponding User object.
      */
     
     @Override
     public UserDTO find(String email) {
     	User u = findTrueUserForEmail(email);
     	if (u == null) return null;
     	
 		UserDTO ud = new UserDTO();
 		u.initializeDTO(ud);
 		return ud;
     }
 
     /** Given an email, returns an User instance with this Email. */
     
     public User findTrueUserForEmail(String email) {
 		List<User> users = em.createQuery("SELECT u FROM User u WHERE u.email LIKE ?1", User.class).setParameter(1, email).getResultList();
 		
 		if (users.size() > 0) {
 			return users.get(0);
 		}
 		return null;
     }
 
     /** Returns a list of all users whose emails have been verified to work. */
     
     public List<User> getVerifiedUsers() {
     	TypedQuery<User> result = em.createQuery("SELECT u FROM User u WHERE u.verified = true", User.class);
     	return result.getResultList();
     }
 
     /** Removes an user with the given email.
      * */
     
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     public void remove(String email) {
     	User u = findTrueUserForEmail(email);
     	
     	if (u != null) {
     		em.remove(u);
     	}
     }
  
     /** Returns a list of all users. */
     
     public List<User> findAll() {
     	TypedQuery<User> result = em.createQuery("SELECT u FROM User u", User.class);
     	return result.getResultList();
     }
 
     /** Removes all users from the database. */
     
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     public void removeAll() {
     	for (User u : findAll()) {
     		em.remove(u);
     	}
     }
 
     /** Creates a new temporary login for the User u, adds this LoginData
      * object to the Persistence Context and returns it.
      *
      * @param u
      * @return LoginData containing the generated temporary key.
      */
     
     public LoginData createLogin(User u) {
     	
     	LoginData newLogin = new LoginData();
 
     	boolean doAgain = true;
     	String random = null;
     	while (doAgain) {
         	random = generateRandomString(32);
         	LoginData tmp = em.find(LoginData.class, random);
         	if (tmp == null) { doAgain = false; }
     	}
     	
     	newLogin.setLoginkey(random);
     	newLogin.setIssued(new Date());
 
 		u.setLogins(new ArrayList<LoginData>(1));
 		
     	u.getLogins().add(newLogin);
     	
     	return newLogin;
     }
 
     /** Generates a random string using predefined set of characters. Currently represents a hex value,
      * though uses double the amount of random integers than it could.
      * 
      * @param length
      * @return Random salt string of length length
      */
     
     public static String generateRandomString(int length) {
 		char[] salt = new char[length];
 		SecureRandom random = new SecureRandom();
 		
 		for (int i=0; i<salt.length; i++) {
 			salt[i] = characters[random.nextInt(characters.length)];
 		}
 		
 		return new String(salt);
     	
     }
 
     /** Returns an UserDTO object, whose temporary login key matches the supplied key value.
      */
     
 	@Override
 	public UserDTO findWithLoginKey(String key) {
 		List<User> users = em.createQuery("SELECT u FROM User u JOIN u.logins l WHERE l.loginkey LIKE ?1", User.class).setParameter(1, key).getResultList();
 		
 		if (users.size() > 0) {
 			User u = users.get(0);
 //			u.setVerified(true);
 			UserDTO ud = new UserDTO();
 			u.initializeDTO(ud);
 			return ud;
 		}
 		return null;
 	}
 	
 	/**
 	 * Given a temporary login key, verifies that this user's email is verified.
 	 */
 
 	public boolean verify(String key) {
 		List<User> users = em.createQuery("SELECT u FROM User u JOIN u.logins l WHERE l.loginkey LIKE ?1", User.class).setParameter(1, key).getResultList();
 		
 		if (users.size() > 0) {
 			User u = users.get(0);
 			u.setVerified(true);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Given an user and password, returns true if the User's password matches this.
 	 * 
 	 * @param u User object
 	 * @param password
 	 * @return true if the password matches.
 	 */
 	
 	private boolean verifyPassword(User u, String password) {
 		if (u == null) return false;
 		
 		String pwHash = DigestUtils.sha512Hex(u.getSalt() + password);
 
 		if (pwHash.equals(u.getPassword())) {
 			return true;
 		}
 		
 		return false;
 	}
 
 	/** Given an email and an UserDTO object, updates the matching User 
 	 * with the values from the UserDTO (except id). Updating logic is in 
 	 * User.initialize (currently ignores all values set to null).
 	 * 
 	 * Allows modification of password only if the supplied oldPassword is the same 
 	 * as the current password. 
 	 * 
	 * @return false if passwords don't match.
 	 */
 	
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	@Override
 	public boolean updateUser(String email, UserDTO ud) {
     	if (ud == null || email == null) return false;
 
     	User u = findTrueUserForEmail(email);
 
 		if (u == null) return false;
 
 		System.out.println("Updateuser: " + u.getEmail());
 		
 		if (ud.getPassword1() != null || ( ud.getEmail() != null && ! ud.getEmail().equals(u.getEmail()))) {
 			// Either password or email change requested.
 			if (! verifyPassword(u, ud.getOldPassword())) {
 				return false;
 			}
 		}
 		u.initialize(ud);
 		
 		// Mainly to get returnpassword from u.
 		u.initializeDTO(ud);
 		
 		return true;
 	}
 
    /** Given an UserDTO object containing only a valid Email, fills it with data from
      * the database.
      */
     
 	@Override
	public void fill(UserDTO ud) {
		User u = findTrueUserForEmail(ud.getEmail());
 
 		if (u != null) {
 			u.initializeDTO(ud);
 		}
 	}
 }
