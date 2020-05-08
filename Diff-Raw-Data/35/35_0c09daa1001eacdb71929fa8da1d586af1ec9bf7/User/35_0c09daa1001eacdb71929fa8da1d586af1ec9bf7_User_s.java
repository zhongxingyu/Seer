 package org.smartsnip.core;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.smartsnip.persistence.IPersistence;
 import org.smartsnip.security.IHash;
 import org.smartsnip.security.MD5;
 
 // NOTE: TODO: Implement the notification system
 
 /**
  * This is the user of the system. TODO: Write me
  * 
  * The hash code for each user is given by the hash code of the unique user name
  * 
  */
 public class User {
 
 	/** This hash algorithm is used for password hashing */
 	private final static IHash hashAlgorithm = MD5.getInstance();
 
 	/** Final identifier of the username */
 	public final String username;
 
 	/** Real name of the user */
 	private String realName = "";
 
 	/** Users email address */
 	private String email = "";
 
 	/** State of the user */
 	private UserState state = UserState.unvalidated;
 
 	/** Caches favourite snippets */
 	private List<Snippet> favourites = null;
 
 	/** The logger for this class */
 	private static Logger log = Logger.getLogger(User.class);
 
 	/** List of all notifications of the user, or null, if not yet loaded */
 	private transient List<Notification> notifications = null;
 
 	/**
 	 * Determines the status of the user, currently if the user has been
 	 * validated or not
 	 * 
 	 */
 	public enum UserState {
		unvalidated, validated, deleted, moderator, administrator
 	}
 
 	/**
 	 * Constructor for the DB
 	 * 
 	 * Generates a new user with the given parameters. All arguments except
 	 * realName and favorites must not be null or empty, otherwise a new
 	 * {@link IllegalArgumentException} is thrown.
 	 * 
 	 * The password must alreadby be encrypted. Here the password is stored as
 	 * given, in all other cases the password is first encrypted, and then
 	 * stored in the system.
 	 * 
 	 * @param username
 	 *            of the new user
 	 * @param realName
 	 *            the real name of the user. Can be null or empty.
 	 * @param email
 	 *            of the new user
 	 * @param state
 	 * @param favorites
 	 *            Favorited snippets of the user. If null, a new list is created
 	 * 
 	 * @throws IllegalArgumentException
 	 *             Thrown if one of the arguments is null or empty
 	 */
 	User(String username, String realName, String email, UserState state) {
 		if (username == null || username.isEmpty())
 			throw new IllegalArgumentException(
 					"Cannot create user with empty username");
 		if (realName == null) {
 			realName = "";
 		}
 		if (email == null || email.isEmpty())
 			throw new IllegalArgumentException(
 					"Cannot create user with empty email");
 
 		this.username = username.toLowerCase();
 		this.realName = realName;
 		this.email = email;
 		this.state = state;
 	}
 
 	/**
 	 * Generates a new user with the given parameters. Here no more checks are
 	 * done. They are ment to be done in the createNewUser-method The given
 	 * password will be encrypted here.
 	 * 
 	 * @param username
 	 *            of the new user
 	 * @param email
 	 *            of the new user
 	 * @param realName
 	 *            Real name of the user
 	 */
 	private User(String username, String email, String realName) {
 		this.username = username.toLowerCase();
 		this.email = email;
 		this.realName = realName;
 	}
 
 	/**
 	 * Searches for a user in the system and returns the user if found. If not
 	 * user can be found, the result is null. The search for the username is NOT
 	 * case sensitive.
 	 * 
 	 * @param username
 	 *            Name of the user that should be searched
 	 * @return found user with the given username or null if not found
 	 */
 	public synchronized static User getUser(String username) {
 		if (username.length() == 0)
 			return null;
 		username = username.toLowerCase();
 
 		try {
 			return Persistence.instance.getUser(username);
 		} catch (IOException e) {
 			log.info("IOException during getting User \" " + username + "\":"
 					+ e.getMessage(), e);
 			return null;
 		}
 	}
 
 	/**
 	 * Checks if the given username exists in the system. If the username is
 	 * empty, false will be returned
 	 * 
 	 * @param username
 	 *            to be checked
 	 * @return true if existing otherwise false
 	 */
 	public synchronized static boolean exists(String username) {
 		return getUser(username) != null;
 	}
 
 	/**
 	 * Tries to create a new user in the system with the given username and
 	 * email. If the username is already used a new
 	 * {@link IllegalArgumentException} will be thrown. If the email-address is
 	 * invalid a new {@link IllegalArgumentException} will be thrown
 	 * 
 	 * The real name of the new user is set to be the password
 	 * 
 	 * @param username
 	 *            for the new user. Will be lower-cased
 	 * @param password
 	 *            the user's new password
 	 * @param email
 	 *            for the user. Must be valid according to the
 	 *            isValidEmailAddress-Method
 	 * @return the new user
 	 * @throws IllegalArgumentException
 	 *             Thrown if an Argument is invalid. This is the case if one of
 	 *             the strings is empty, the username is already taken or if the
 	 *             email-address is invalid
 	 */
 	public static synchronized User createNewUser(String username,
 			String password, String email) throws IllegalArgumentException {
 		return createNewUser(username, password, email, username);
 	}
 
 	/**
 	 * Tries to create a new user in the system with the given username and
 	 * email. If the username is already used a new
 	 * {@link IllegalArgumentException} will be thrown. If the email-address is
 	 * invalid a new {@link IllegalArgumentException} will be thrown
 	 * 
 	 * The real name of the new user is set to be the password
 	 * 
 	 * @param username
 	 *            for the new user. Will be lower-cased
 	 * @param password
 	 *            the user's new password
 	 * @param email
 	 *            for the user. Must be valid according to the
 	 *            isValidEmailAddress-Method
 	 * @param realname
 	 *            The displayed real name of the user. If null or empty it will
 	 *            be replaced with the username
 	 * @return the new user
 	 * @throws IllegalArgumentException
 	 *             Thrown if an Argument is invalid. This is the case if one of
 	 *             the strings is empty, the username is already taken or if the
 	 *             email-address is invalid
 	 */
 	public static synchronized User createNewUser(String username,
 			String password, String email, String realname)
 			throws IllegalArgumentException {
 		if (username.length() == 0)
 			throw new IllegalArgumentException("Username cannot be empty");
 		if (password.isEmpty())
 			throw new IllegalArgumentException("Password cannot be empty");
 		if (email.length() == 0)
 			throw new IllegalArgumentException("e-mail address cannot be empty");
 		if (!isValidEmailAddress(email))
 			throw new IllegalArgumentException("Illegal email address");
 		// Check for duplicated user entries
 		username = username.toLowerCase();
 		if (exists(username))
 			throw new IllegalArgumentException("Username already taken");
 		if (realname == null || realname.isEmpty()) {
 			realname = username;
 		}
 
 		// All test passed. Create new user
 		User newUser = new User(username, email, realname);
 		addToDB(newUser);
 		newUser.setPassword(password);
 		return newUser;
 	}
 
 	/**
 	 * Removes a user from the system. If the given user cannot be found,
 	 * nothing is done
 	 * 
 	 * @param username
 	 *            name of the user to be deleted
 	 */
 	public synchronized static void deleteUser(String username) {
 		deleteUser(getUser(username));
 	}
 
 	/**
 	 * Sets the state of the user
 	 * 
 	 * @param state
 	 */
 	public synchronized void setState(UserState state) {
		if(this.state == state) return;
 		this.state = state;
		
 		// Create notification for this process
		Notification notification = Notification.createNotification("Your userstate is set to: " state.toString(), getUsername());
		
 		refreshDB();
 	}
 
 	/**
 	 * Removes a user from the system. If the given user is null, nothing
 	 * happens
 	 * 
 	 * @param user
 	 *            that should be deleted.
 	 */
 	public synchronized static void deleteUser(User user) {
 		if (user == null)
 			return;
 
 		try {
 			Persistence.instance.removeUser(user, IPersistence.DB_DEFAULT);
 		} catch (IOException e) {
 			log.warn("IOException during deleteUser(" + user.username + "): "
 					+ e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Internal call to check if the email address is valid
 	 * 
 	 * @param email
 	 *            to be checked
 	 * @return true if valid otherwise false
 	 */
 	private static boolean isValidEmailAddress(String email) {
 		if (email.length() == 0)
 			return false;
 		int atSign = email.indexOf('@');
 		if (atSign < 1)
 			return false;
 		if (atSign >= email.length())
 			return false;
 		return true;
 	}
 
 	/**
 	 * Matches the given password with the user password. Always use this method
 	 * Instant of manual equivalence checking, because the password uses a
 	 * security hash code.
 	 * 
 	 * @param password
 	 *            to be checked
 	 * @return true if the password check is positive, false if a password
 	 *         denial happens
 	 */
 	public boolean checkPassword(String password) {
 		password = hashAlgorithm.hash(password);
 
 		try {
 			return Persistence.instance.verifyPassword(this, password);
 		} catch (IOException e) {
 			log.info("IOException during checking password for user \""
 					+ username + "\": " + e.getMessage(), e);
 			return false;
 		}
 
 	}
 
 	/**
 	 * @return the real name of the user
 	 */
 	public String getRealName() {
 		return realName;
 	}
 
 	/**
 	 * @return the email address of the user
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * Set a new email-address for the user. The email-address must be valid,
 	 * otherwise a new {@link IllegalArgumentException} will be thrown
 	 * 
 	 * @param email
 	 *            the email to set
 	 * @throws IllegalArgumentException
 	 */
 	public void setEmail(String email) throws IllegalArgumentException {
 		if (email.length() == 0)
 			throw new IllegalArgumentException(
 					"Empty email address not allowed");
 		if (!isValidEmailAddress(email))
 			throw new IllegalArgumentException("Illegal email-address");
 
 		if (this.email.equals(email))
 			return;
 		this.email = email;
 		refreshDB();
 	}
 
 	/**
 	 * 
 	 * Sets the real name of the user. If the new name is null or empty, or the
 	 * name does not changes, the method returns without any effect.
 	 * 
 	 * @param name
 	 *            new real name of the user
 	 */
 	public void setRealName(String name) {
 		if (name == null || name.isEmpty())
 			return;
 		if (this.realName.equalsIgnoreCase(name))
 			return;
 		this.realName = name;
 		refreshDB();
 	}
 
 	/**
 	 * @return the username
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	/**
 	 * @return the total count of registered users in the system
 	 */
 	public static int totalCount() {
 		try {
 			return Persistence.instance.getUserCount();
 		} catch (IOException e) {
 			log.warn("IOException during getUserCount(): " + e.getMessage(), e);
 			return -1;
 		}
 	}
 
 	/**
 	 * Sets the user password. Password must not be empty, or a new
 	 * {@link IllegalArgumentException} will be thrown
 	 * 
 	 * @param password
 	 *            the password to set
 	 * @throws IllegalArgumentException
 	 */
 	public synchronized void setPassword(String password)
 			throws IllegalArgumentException {
 		if (password.length() == 0)
 			throw new IllegalArgumentException("Empty password not allowed");
 		password = hashAlgorithm.hash(password);
 
 		try {
 			Persistence.instance.writeLogin(this, password, true,
 					IPersistence.DB_DEFAULT);
 		} catch (IOException e) {
 			log.warn(
 					"IOException during writing password for user \""
 							+ this.getUsername() + "\": " + e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Tries to authorise a user login. If the login succeeds, true is resulted.
 	 * If the username or the password does not exists/match false is returned
 	 * without a reason.
 	 * 
 	 * @param username
 	 *            to be user
 	 * @param password
 	 *            for that user
 	 * @return true if the login is successful, otherwise false (without reason
 	 *         message)
 	 */
 	public static boolean auth(String username, String password) {
 		User user = getUser(username);
 		if (user == null)
 			return false;
 		return user.checkPassword(password);
 	}
 
 	@Override
 	public String toString() {
 		return username;
 	}
 
 	/**
 	 * @return a list of the snippets created by the user
 	 */
 	public List<Snippet> getMySnippets() {
 		try {
 			return Persistence.getInstance().getUserSnippets(this);
 		} catch (IOException e) {
 			log.warn("IOException during getMySnippets(" + this.getUsername()
 					+ "): " + e.getMessage(), e);
 			return null;
 		}
 	}
 
 	/**
 	 * @return a list of the users' favourite snippets
 	 */
 	public synchronized List<Snippet> getFavoriteSnippets() {
 		if (favourites != null)
 			return favourites;
 
 		List<Snippet> result = null;
 		try {
 			result = new ArrayList<Snippet>(Persistence.getInstance()
 					.getFavorited(this));
 		} catch (IOException e) {
 			log.warn("IOException during getting favorites for user \""
 					+ getUsername() + "\": " + e.getMessage(), e);
 		}
 
 		favourites = result;
 		return result;
 
 	}
 
 	/**
 	 * @return a list of the hash codes of the users' favourite snippets
 	 */
 	List<Long> getFavoriteSnippetsHash() {
 		List<Snippet> favorites = getFavoriteSnippets();
 		List<Long> result = new ArrayList<Long>(favorites.size());
 		for (Snippet snippet : favorites) {
 			result.add(snippet.id);
 		}
 		return result;
 
 	}
 
 	/**
 	 * Deletes this user from the database
 	 */
 	public synchronized void delete() {
 		deleteUser(this);
 	}
 
 	/**
 	 * Invokes the refreshing process for the database
 	 */
 	protected void refreshDB() {
 		try {
 			Persistence.instance.writeUser(this, IPersistence.DB_DEFAULT);
 		} catch (IOException e) {
 			log.warn(
 					"IOException writing out user \"" + username + "\": "
 							+ e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Adds a snippet to the user's favourites. If the snippet is null, nothing
 	 * happens. If the snippet as already been added, nothing happens.
 	 * 
 	 * @param snippet
 	 *            to be added
 	 */
 	public void addFavorite(Snippet snippet) {
 		if (snippet == null)
 			return;
 
 		try {
 			Persistence.getInstance().addFavourite(snippet, this,
 					IPersistence.DB_NEW_ONLY);
 		} catch (IOException e) {
 			log.warn(
 					"IOException writing favorite snippet (id="
 							+ snippet.getHashId() + ") for user \"" + username
 							+ "\": " + e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Removes a snippet from the user's favourites. If the snippet is null,
 	 * nothing happens. If the snippet is not in the user's favorite list,
 	 * nothing happens
 	 * 
 	 * @param snippet
 	 */
 	public void removeFavorite(Snippet snippet) {
 		if (snippet == null)
 			return;
 
 		try {
 			Persistence.getInstance().removeFavourite(snippet, this,
 					IPersistence.DB_NEW_ONLY);
 		} catch (IOException e) {
 			log.warn(
 					"IOException writing favorite snippet (id="
 							+ snippet.getHashId() + ") for user \"" + username
 							+ "\": " + e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * @return the current user state
 	 */
 	public UserState getState() {
 		return state;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (!(obj instanceof User))
 			return false;
 
 		User user = (User) obj;
 		return user.username.equals(this.username);
 	}
 
 	@Override
 	public int hashCode() {
 		return username.hashCode();
 	}
 
 	/**
 	 * Writes a user out to the database. If the user is null, nothing happens
 	 * 
 	 * The database is told not to overwrite an existing user.
 	 * 
 	 * @param user
 	 *            to be written out
 	 */
 	protected static synchronized void addToDB(User user) {
 		if (user == null)
 			return;
 
 		try {
 			Persistence.instance.writeUser(user, IPersistence.DB_NEW_ONLY);
 		} catch (IOException e) {
 			log.warn("IOException during adding user \" " + user.getUsername()
 					+ "\" to db: " + e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Trims the username and lowercases him
 	 * 
 	 * @param username
 	 *            to be trimmed and lowercased
 	 * @return the trimmed and lowercased username
 	 */
 	public static String trimUsername(String username) {
 		if (username == null)
 			return null;
 		return username.trim().toLowerCase();
 	}
 
 	/**
 	 * Sets the status of the user to deleted or not deleted
 	 * 
 	 * If the user returns into the world of the living, the state is set to
 	 * invalidated
 	 * 
 	 * @param deleted
 	 *            true if the user is deleted, othwerise false.
 	 */
 	public void setDeleted(boolean deleted) {
 		UserState newState = state;
 		if (deleted) {
 			newState = UserState.deleted;
 		} else {
 			newState = UserState.unvalidated;
 		}
 
 		if (state != newState) {
 			state = newState;
 			refreshDB();
 		}
 	}
 
 	/**
 	 * @return true if the user is deleted, otherwise false
 	 */
 	public boolean isDeleted() {
 		return getState() == UserState.deleted;
 	}
 
 	/**
 	 * Checks if the userstate of this user is moderator or higher. I.e. if the
 	 * user can play the moderator role
 	 * 
 	 * @return true if the user can moderator, otherwise false
 	 */
 	public boolean canModerate() {
 		UserState state = getState();
 		if (state == UserState.administrator)
 			return true;
 		if (state == UserState.moderator)
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * Checks if a given snippet id is one of the favourite snippets
 	 * 
 	 * @param id
 	 *            hash id of the to be checked
 	 * @return true if favourite otherwise false
 	 */
 	public boolean isFavourite(Long id) {
 		return isFavourite(Snippet.getSnippet(id));
 	}
 
 	/**
 	 * Checks if a given snippet is one of the favourite snippets. If the given
 	 * snippet is null, the result is always null.
 	 * 
 	 * @param snippet
 	 *            snippet of the to be checked
 	 * @return true if favourite otherwise false
 	 */
 	public boolean isFavourite(Snippet snippet) {
 		if (snippet == null)
 			return false;
 
 		List<Snippet> favourites = getFavoriteSnippets();
 		for (Snippet check : favourites)
 			if (check.equals(snippet))
 				return true;
 
 		return false;
 	}
 
 	/**
 	 * Gets the rating, the user gave for a snippet or 0, if unrated, or if the
 	 * snippet is not found
 	 * 
 	 * @param hashId
 	 *            Snippet's hash id to fetch the rating status from
 	 * @return the rating of the snippet
 	 */
 	public int getSnippetRating(Long hashId) {
 		// TODO Auto-generated method stub
 		// TODO Not yet implemented in persistence!
 		return -1;
 	}
 
 	/**
 	 * Gets the number of notifications, either of unread-only, if defined, or
 	 * or all notifications
 	 * 
 	 * @param unreadOnly
 	 *            Handle only unread notifications
 	 * @return the number of notifications or of unread notifications if flag is
 	 *         set
 	 */
 	public long getNotificationCount(boolean unreadOnly) {
 		return getNotifications(unreadOnly).size();
 	}
 
 	/**
 	 * Get all notifications of the user
 	 * 
 	 * @return all notifications of the user
 	 */
 	public synchronized List<Notification> getNotifications(boolean unreadOnly) {
 		List<Notification> notifications = getNotificationsFromCache();
 		if (!unreadOnly)
 			return notifications;
 
 		List<Notification> unreadNotifications = new ArrayList<Notification>();
 		for (Notification notification : notifications)
 			if (!notification.isRead())
 				unreadNotifications.add(notification);
 		return unreadNotifications;
 	}
 
 	/**
 	 * Gets the notifications out of the cache, if there or loads the
 	 * notifiactions into it
 	 * 
 	 * @return the notifications
 	 */
 	private List<Notification> getNotificationsFromCache() {
 		if (notifications != null)
 			return notifications;
 
 		try {
			notifications = Persistence.getInstance().getNotifications(this,
					false);
 		} catch (IOException e) {
 			System.err
 					.println("IOException during fetching notifications for user \""
 							+ this.username + "\": " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 		return notifications;
 	}
 
 	/**
 	 * Checks if the user is moderator or higher
 	 * 
 	 * @return true if the user has moderator provileges
 	 */
 	public boolean isModerator() {
 		UserState state = getState();
 		if (state == UserState.administrator)
 			return true;
 		if (state == UserState.moderator)
 			return true;
 		return false;
 	}
 }
