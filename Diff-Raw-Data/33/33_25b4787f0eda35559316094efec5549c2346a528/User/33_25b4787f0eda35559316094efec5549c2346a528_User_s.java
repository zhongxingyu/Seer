 package models.obdmedb;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.db.jpa.Blob;
 import play.db.jpa.Model;
 import edu.unl.csce.obdme.encryption.EncryptionUtils;
 
 @Entity
 @Table(name="userbase")
 @SuppressWarnings("serial")
 public class User extends Model {
 	
 	@Email
 	@Required
 	@Column(name="email", unique=true)
 	public String email;
 	
 	@Column(name="passwordhash", length=64)
 	@Required
 	public String passwordhash;
 	
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name="regdate")
 	public Date regdate;
 
 	@Column(name="firstname", length=255)
 	public String firstname;
 	
 	@Column(name="lastname", length=255)
 	public String lastname;
 	
 	@Column(name="sendemail", nullable=false, columnDefinition="boolean default false")
 	public Boolean sendemail;
 	
 	@Column(name="sendsms", nullable=false, columnDefinition="boolean default false")
 	public Boolean sendsms;
 	
	@Column(name="avatar512")
 	public Blob avatar512;
 
	@Column(name="avatar256")
 	public Blob avatar256;
 	
	@Column(name="avatar128")
 	public Blob avatar128;
 	
	@Column(name="avatar64")
 	public Blob avatar64;
 	
	@Column(name="avatar32")
 	public Blob avatar32;
 
 	public static User createUserFromClearTextCredentials(String email, String clearTextPassword) {
 		String encryptedPassword = EncryptionUtils.encryptPassword(clearTextPassword);
 		return createUserFromEncryptedCredentials(email, encryptedPassword);
 	}
 	
 	public static User createUserFromEncryptedCredentials(String email, String encryptedPassword) {
 		User user = new User();
 		user.email = email;
 		user.passwordhash = encryptedPassword;
 		user.regdate = new Date();
 		user.sendemail = false;
 		user.sendsms = false;
 		return user.validateAndSave() ? user : null;		
 	}
 	
 	public static User findByEmail(String email) {
 		return find("email", email).first();
 	}
 	
 	public static User findUserById(Long id) {
 		return findById(id);
 	}
 	
 	public static boolean isUserRegistered(String email) {
 		return findByEmail(email) != null;
 	}
 	
 	public static boolean isValidCredentialsClearText(String email, String clearTextPassword) {
 		return validateUserCredentialsClearText(email, clearTextPassword) != null;
 	}
 	
 	public static boolean isValidCredentialsEncrypted(String email, String encryptedPassword) {
 		return validateUserCredentialsEncrypted(email, encryptedPassword) != null;
 	}
 	
 	public static User validateUserCredentialsClearText(String email, String clearTextPassword) {
 		String encryptedPassword = EncryptionUtils.encryptPassword(clearTextPassword);
 		return validateUserCredentialsEncrypted(email, encryptedPassword);
 	}
 	
 	public static User validateUserCredentialsEncrypted(String email, String encryptedPassword) {
 		return find("email like ? and passwordhash like ?", email, encryptedPassword).first();
 	}
 	
 	public static void changeUserPassword(String email, String clearTextCurrentPassword, String clearTextNewPassword) {
 		String encryptedPassword = EncryptionUtils.encryptPassword(clearTextCurrentPassword);
 		User userFound = validateUserCredentialsEncrypted(email, encryptedPassword);
 		if (userFound != null) {
 			userFound.setPasswordhash(EncryptionUtils.encryptPassword(clearTextNewPassword));
 			userFound.save();
 		}
 	}
 	
 	public String getEmail() {
 		return email.toLowerCase();
 	}
 
 	public void setEmail(String email) {
 		this.email = email.toLowerCase().trim();
 	}
 
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname.trim();
 	}
 
 	public String getLastname() {
 		return lastname;
 	}
 
 	public void setLastname(String lastname) {
 		this.lastname = lastname.trim();
 	}
 
 	public Boolean isSendemail() {
 		return sendemail;
 	}
 
 	public void setSendemail(Boolean sendemail) {
 		this.sendemail = sendemail;
 	}
 
 	public Boolean isSendsms() {
 		return sendsms;
 	}
 
 	public void setSendsms(Boolean sendsms) {
 		this.sendsms = sendsms;
 	}
 
 	public String getPasswordhash() {
 		return passwordhash;
 	}
 
 	public void setPasswordhash(String passwordhash) {
 		this.passwordhash = passwordhash;
 	}
 
 	public Blob getAvatar512() {
 		return avatar512;
 	}
 
 	public void setAvatar512(Blob avatar512) {
 		this.avatar512 = avatar512;
 	}
 
 	public Blob getAvatar256() {
 		return avatar256;
 	}
 
 	public void setAvatar256(Blob avatar256) {
 		this.avatar256 = avatar256;
 	}
 
 	public Blob getAvatar128() {
 		return avatar128;
 	}
 
 	public void setAvatar128(Blob avatar128) {
 		this.avatar128 = avatar128;
 	}
 
 	public Blob getAvatar64() {
 		return avatar64;
 	}
 
 	public void setAvatar64(Blob avatar64) {
 		this.avatar64 = avatar64;
 	}
 
 	public Blob getAvatar32() {
 		return avatar32;
 	}
 
 	public void setAvatar32(Blob avatar32) {
 		this.avatar32 = avatar32;
 	}
 }
