 
 package com.zanoccio.axirassa.domain;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.xml.crypto.dsig.DigestMethod;
 
 import com.zanoccio.axirassa.util.RandomStringGenerator;
 import com.zanoccio.axirassa.webapp.exceptions.ExceptionInActionError;
 
 public class User {
 
 	private Long id;
 	private String name;
 	private String username;
 	private String password;
 	private String salt;
 
 	private Date signupdate;
 	private String email;
 	private Boolean confirmed;
 	private Boolean active;
 
 	private Set accessEvents = new HashSet();
 
 
 	public User() {
 		confirmed = false;
 		active = true;
 	}
 
 
	public static void main(String[] args) {
		User u = new User();

		u.setPassword("password123");

		System.out.println("Password: " + u.getPassword());
	}


 	//
 	// Logons
 	//
 	public Set getAccessEvents() {
 		return accessEvents;
 	}
 
 
 	public void setAccessEvents(Set logons) {
 		this.accessEvents = logons;
 	}
 
 
 	//
 	// Name
 	//
 	public String getName() {
 		return name;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	//
 	// Salt
 	//
 	public void setSalt(String salt) {
 		this.salt = salt;
 	}
 
 
 	public String getSalt() {
 		return salt;
 	}
 
 
 	/**
 	 * @return a 16-byte random string
 	 */
 	private String createSalt() {
 		return RandomStringGenerator.getInstance().randomString(16);
 	}
 
 
 	//
 	// Password
 	//
 	public String getPassword() {
 		return password;
 	}
 
 
 	public void setPassword(String password) {
 		this.salt = createSalt();
 		try {
 			MessageDigest msgdigest = MessageDigest.getInstance(DigestMethod.SHA256);
 			msgdigest.update(salt.getBytes());
 			msgdigest.update(password.getBytes());
 
 			this.password = new String(msgdigest.digest());
 		} catch (NoSuchAlgorithmException e) {
 			throw new ExceptionInActionError("When attempting to register user.", e);
 		}
 	}
 
 
 	//
 	// Username
 	//
 	public String getUsername() {
 		return username;
 	}
 
 
 	public void setUsername(String username) {
 		if (this.name == null)
 			this.name = username;
 
 		this.username = username;
 	}
 
 
 	//
 	// SignUpDate
 	//
 	public Date getSignUpDate() {
 		return signupdate;
 	}
 
 
 	public void setSignUpDate(Date signupdate) {
 		this.signupdate = signupdate;
 	}
 
 
 	//
 	// Email
 	//
 	public String getEmail() {
 		return email;
 	}
 
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 
 	//
 	// Confirmed
 	//
 	public Boolean isConfirmed() {
 		return confirmed;
 	}
 
 
 	public void setConfirmed(Boolean confirmed) {
 		this.confirmed = confirmed;
 	}
 
 
 	//
 	// Active
 	//
 	public Boolean isActive() {
 		return active;
 	}
 
 
 	public void setActive(Boolean active) {
 		this.active = active;
 	}
 
 
 	//
 	// ID
 	//
 	public void setID(Long id) {
 		this.id = id;
 	}
 
 
 	public Long getID() {
 		return id;
 	}
 
 }
