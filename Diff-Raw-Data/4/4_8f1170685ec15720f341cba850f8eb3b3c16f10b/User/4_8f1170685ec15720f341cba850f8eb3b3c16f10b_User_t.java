 package edu.gatech.cs2340.thc.model;
 
 import java.io.Serializable;
 
 /**
  * This is the User class. User's have can register their credentials with the
  * application. Users can add lost/found items and search through a list of
  * items.
  * 
  * @author Trigger Happy Coders
  * @version 1.0
  */
 @SuppressWarnings("serial")
 public class User implements Serializable {
 
 	private String name;
 	private String password;
 	private String email;
 	private boolean isLocked;
 	private boolean isAdmin;
 
 	/**
 	 * This is the User constructor, defines what it means to be a user who can
 	 * login into the application. Admin inherits from the this class and
 	 * therefore, has all of the same parameters. The only difference is that
 	 * the boolean "isAdmin" is true, whereas for User the boolean "isAdmin" is
 	 * false.
 	 * 
 	 * @param name
 	 *            - String user's name
 	 * @param password
 	 *            - String user's passphrase
 	 * @param email
 	 *            - String user's email address
 	 * @param isLocked
 	 *            - boolean tells the system whether or not that user's account
 	 *            is locked (and can only be unlocked by Administrator)
 	 * @param isAdmin
	 *            - boolean tells the system whether or not the user has
	 *            elevated privileges (special permissions to add/delete other
 	 *            user's etc.)
 	 */
 	public User(String name, String password, String email, boolean isLocked,
 			boolean isAdmin) {
 		this.name = name;
 		this.password = password;
 		this.email = email;
 		this.isLocked = isLocked;
 		this.isAdmin = isAdmin;
 	}
 
 	/**
 	 * A setter for the user's name
 	 * 
 	 * @param n
 	 *            - String setting the user's name to argument (n)
 	 */
 	public void setName(String n) {
 		name = n;
 	}
 
 	/**
 	 * A setter for user's password
 	 * 
 	 * @param p
 	 *            - String setting the user's password to argument (p)
 	 */
 	public void setPassword(String p) {
 		password = p;
 	}
 
 	/**
 	 * A setter for user's email
 	 * 
 	 * @param e
 	 *            - String sets the user's email to argument (e)
 	 */
 	public void setEmail(String e) {
 		email = e;
 	}
 
 	/**
 	 * A getter for user's name
 	 * 
 	 * @return - the user's name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * A getter for user's password
 	 * 
 	 * @return - user's password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * A getter for user's email
 	 * 
 	 * @return - user's email
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * Sets user's account to "locked" status
 	 */
 	public void lockedUser() {
 		isLocked = true;
 	}
 
 	/**
 	 * Sets user's account to "unLocked" status
 	 */
 	public void unLockedUser() {
 		isLocked = false;
 	}
 
 	/**
 	 * A getter that returns user's unlocked/locked status
 	 * 
 	 * @return - whether or not the user is locked out
 	 */
 	public boolean getLockedStatus() {
 		return isLocked;
 	}
 
 	/**
 	 * A getter that gets the user's elevated status
 	 * 
 	 * @return - whether or not the user is an admin
 	 */
 	public boolean getIsAdmin() {
 		return isAdmin;
 	}
 
 	/**
 	 * A setter that changes admin status to true
 	 */
 	public void setToAdmin() {
 		isAdmin = true;
 	}
 
 	/**
 	 * A setter that changes admin status to false
 	 */
 	public void revokeAdmin() {
 		isAdmin = false;
 	}
 
 }
