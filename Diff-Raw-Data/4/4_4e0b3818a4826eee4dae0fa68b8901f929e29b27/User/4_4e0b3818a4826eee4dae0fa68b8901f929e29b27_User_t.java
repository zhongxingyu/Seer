 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 /**
  * Class representing a user in the system.
  */
 @Entity
 @Table(name = "person")
 public class User extends Model {
 
 	@Id
 	@Formats.NonEmpty
 	public Long person_id;
 
 	@Constraints.Required(message="Required")
 	public String username;
 
 	@Constraints.Required(message="Required")
 	@Constraints.Email
 	public String email;
 
 	@Constraints.Required(message="Required")
 	public String surname;
 
 	@Constraints.Required(message="Required")
 	public String name;
 
 	@Constraints.Required
 	@Enumerated(EnumType.ORDINAL)
 	public Role role = Role.Applicant;
 
 	@Constraints.MinLength(6)
 	@Constraints.Required(message="Required")
 	public String password;
 
 	@Constraints.MinLength(6)
 	@Constraints.Required(message="Required")
 	public transient String confirmPassword;
 	
 	@Constraints.Required(message="Required")
 	public String ssn;
 
	@OneToMany(cascade=CascadeType.ALL)
 	public List<Competence> competenceList;
 	
 	public static Model.Finder<String, User> find = new Model.Finder(String.class, User.class);
 
 	public User() {
 	}
 
 	/**
 	 * Retrieve User by email.
 	 * *
 	 * @param email - Email of user.
 	 * @return User.
 	 */
     public static User findByEmail(String email) {
         return find.where().eq("email", email).findUnique();
     }
 
     /**
      * Retrieve User by username.
      * 
      * @param username - User name of User.
      * @return User.
      */
     public static User findByUsername(String username) {
         return find.where().eq("username", username).findUnique();
     }
     
     /**
      * Retrieve User by username and password pair. Used to authenticates a User.
 	 *
      * @param username - Username of User.
      * @param password - Password of User.
      * @return User.
      */
     public static User authenticate(String username, String password) {
         return find.where()
             .eq("username", username)
             .eq("password", encrypt(password))
             .findUnique();
     }
 
     /**
      * Return a list of all Users.
      * 
      * @return List of all Users.
      */
 	public static List<User> getAll() {
 		return find.all();
 	}
 
 	/**
 	 * Convert user to String representation.
 	 */
 	@Override
 	public String toString() {
 		return username;
 	}
 
 	/**
 	 * Check if email address is available for registration.
 	 * If email address is not available for registration,
 	 * it is already in use with in the system.
 	 * 
 	 * @param email - email address to check.
 	 * @return false if found, else true.
 	 */
 	static public boolean emailAvailable(String email) {
 		if (findByEmail(email) != null)
 			return false;
 		
 		return true;
 	}
 
 	/**
 	 * Check if user name is available for registration.
 	 * If username is not available for registration,
 	 * it is already in use with in the system.
 	 * 
 	 * @param username - user name to check.
 	 * @return false if found, else true.
 	 */
 	static public boolean usernameAvailable(String username) {
 		if (findByUsername(username) != null)
 			return false;
 		
 		return true;
 	}
 	
 	/**
 	 * Digest string to md5 sum.
 	 * 
 	 * FIXME: replace with http://stackoverflow.com/questions/12250656/play-framework-cryptoaes-secret-key
 	 * 
 	 * @param string - String to digest.
 	 * @return returns digested string or null if any problems wore encountered.
 	 */
 	static public String encrypt(String string) {
 		try {
 			MessageDigest md5 = MessageDigest.getInstance("MD5");
 			byte[] bytesOfMessage = string.getBytes("UTF-8");
 			byte[] digest = md5.digest(bytesOfMessage);
 			
 			digest  = Base64.encode(digest);
 			
 			return new String(digest);
 			
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public void save() {
 		password = encrypt(password);		
 		super.save();
 	}
 	
 	/**
 	 * Validate User registration form.
 	 * 
 	 * @return null if validation is successful, else error message is returned.
 	 */
 	public String validate() {
 		if (!emailAvailable(email))
 			return "Email already registered";
 		
 		if (!usernameAvailable(username))
 			return "Username already registered";
 		
 		if (!password.equals(confirmPassword))
 			return "Your password and confirmation password don't match";
 
 		return null;
 	}
 }
