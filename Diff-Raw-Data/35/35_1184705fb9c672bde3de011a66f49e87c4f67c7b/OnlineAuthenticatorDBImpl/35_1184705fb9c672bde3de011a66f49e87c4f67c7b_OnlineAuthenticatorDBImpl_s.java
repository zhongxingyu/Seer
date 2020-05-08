 /*
  * Created on Oct 31, 2006
  */
 package ecologylab.oodss.authentication;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Set;
 
 import ecologylab.oodss.authentication.db.AuthenticationDBStrings;
 import ecologylab.sql.ConnectionWithAutoClose;
 import ecologylab.sql.PreparedStatementWithAutoClose;
 import ecologylab.sql.StatementWithAutoClose;
 
 /**
  * Encapsulates all authentication actions (tracking who is online, etc.), so that Servers don't
  * need to. Requires a backend database of users with passwords (an AuthenticationList).
  * 
  * Database implementation.
  * 
  * @author Zachary O. Toups (zach@ecologylab.net)
  */
 public class OnlineAuthenticatorDBImpl<UwAX extends UserWithAuxData> extends
 		AuthenticationListDBImpl<UwAX> implements OnlineAuthenticator<UwAX>, AuthenticationDBStrings
 {
 	/**
 	 * Creates a new OnlineAuthenticatorDBImpl based on a connection to a MySQL database. Lazily
 	 * instantiates the database connection as needed.
 	 * 
 	 * @param dbLocation
 	 *          URL for database in the form "mysql://...".
 	 * @param username
 	 *          username to connect to database.
 	 * @param password
 	 *          password for database.
 	 */
 	public OnlineAuthenticatorDBImpl(String dbLocation, String username, String password, String db)
 	{
 		super(dbLocation, username, password, db);
 	}
 
 	public OnlineAuthenticatorDBImpl()
 	{
 		super();
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#login(A, java.lang.String)
 	 */
 	public boolean login(UwAX entry, String sessionId)
 	{
 		System.out.println("*****************************************");
 		System.out.println("entry: " + entry.toString());
 
 		boolean loggedInSuccessfully = false;
 
 		// first see if the username exists
 		if (entry != null)
 		{
 			// check password
 			if (super.isValid(entry))
 			{
 				// regardless of whether the user is already online, update their information in the
 				// database.
 				// mark login successful
 				loggedInSuccessfully = true;
 
 				this.performLoginOrLogoutOnDB(entry.getUserKey(), sessionId, true);
 
 				// set the UID from the backing store
 				this.setUID(entry);
 				entry.setSessionId(sessionId);
 			}
 			else
 			{
 				debug("invalid entry");
 			}
 		}
 		else
 		{
 			debug("<null> attempted login.");
 			loggedInSuccessfully = false;
 		}
 
 		return loggedInSuccessfully;
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#lookupUserLevel(A)
 	 */
 	public int lookupUserLevel(UwAX entry)
 	{
 		if (super.isValid(entry))
 		{
 			return super.getAccessLevel(entry);
 		}
 		else
 		{
 			return -1;
 		}
 	}
 
 	/**
 	 * Returns the list of email addresses of users currently logged-in to the system.
 	 * 
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#usersLoggedIn(A)
 	 */
 	public Set<String> usersLoggedIn(UwAX administrator)
 	{
 		if (this.lookupUserLevel(administrator) >= AuthLevels.ADMINISTRATOR)
 		{
 			return this.performLookupOnlineUsersInDB();
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#logout(A, java.lang.String)
 	 */
 	public boolean logout(UwAX entry, String sessionId)
 	{
 		return this.logout(entry, sessionId, false);
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#logout(A, java.lang.String)
 	 */
 	public boolean logout(UwAX entry, String sessionId, boolean useUsername)
 	{
 		try
 		{
 			if (entry.getUid() == this.performLookupUserId(sessionId))
 			{
 				this.performLoginOrLogoutOnDB(entry.getUserKey(), sessionId, false);
 				entry.setSessionId(null);
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#isLoggedIn(java.lang.String)
 	 */
 	public boolean isLoggedIn(String email)
 	{
 		return (this.performIsOnlineDB(email));
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#getSessionId(ecologylab.oodss.authentication.AuthenticationListEntry)
 	 */
 	public String getSessionId(UwAX entry)
 	{
 		return performLookupSessionIdDB(entry.getUserKey());
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#isLoggedIn(ecologylab.oodss.authentication.AuthenticationListEntry)
 	 */
 	public boolean isLoggedIn(UwAX entry)
 	{
 		return performIsOnlineDB(entry.getUserKey());
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#logoutBySessionId(java.lang.String)
 	 */
 	public void logoutBySessionId(String sessionId)
 	{
 		this.performLogoutOnDB(sessionId);
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#sessionValid(java.lang.String)
 	 */
 	public boolean sessionValid(String sessionId)
 	{
 		return this.performLookupUserId(sessionId) != -1;
 	}
 
 	/**
 	 * @param email
 	 * @return true if the user key is online; false if it is offline or does not exist
 	 */
 	private boolean performIsOnlineDB(String userKey)
 	{
 		boolean isOnline = false;
 
 		ConnectionWithAutoClose connection = null;
 		PreparedStatementWithAutoClose stmt = null;
 		ResultSet rs = null;
 
 		try
 		{
 			connection = this.getAutoClosingConnection();
 
 			stmt = connection.prepareStatement(PREPARED_SELECT_USER_BY_USER_KEY);
 			stmt.setString(1, userKey);
 
 			rs = stmt.executeQuery();
 
 			if (rs.next())
 				isOnline = rs.getBoolean(COL_ONLINE);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (connection != null)
 				connection.close();
 		}
 
 		return isOnline;
 	}
 
 	protected synchronized Set<String> performLookupOnlineUsersInDB()
 	{
 		Set<String> onlineUsers = new HashSet<String>();
 
 		ConnectionWithAutoClose connection = null;
 		StatementWithAutoClose<Statement> stmt = null;
 		ResultSet rs = null;
 
 		try
 		{
 			connection = this.getAutoClosingConnection();
 
 			stmt = connection.createStatement();
 			rs = stmt.executeQuery(SELECT_ALL_ONLINE_USERS);
 
 			while (rs.next())
 			{
 				onlineUsers.add(rs.getString(COL_USER_KEY));
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (connection != null)
 				connection.close();
 		}
 
 		return onlineUsers;
 	}
 
 	/**
 	 * @param key
 	 *          the key to use to look up the user; username if useUsername is true or email if it is
 	 *          false.
 	 */
 	private void performLoginOrLogoutOnDB(String key, String sessionId, boolean login)
 	{
 		ConnectionWithAutoClose connection = null;
 		PreparedStatementWithAutoClose stmt = null;
 
 		try
 		{
 			connection = this.getAutoClosingConnection();
 
 			if (login)
 			{
 				stmt = connection.prepareStatement(PREPARED_LOGIN_USER);
 				stmt.setString(1, sessionId);
 				stmt.setString(2, key);
 			}
 			else
 			{
 				stmt = connection.prepareStatement(PREPARED_LOGOUT_USER_BY_USER_KEY);
 				stmt.setString(2, key);
 			}
 
 			stmt.executeUpdate();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (connection != null)
 				connection.close();
 		}
 	}
 
 	/**
 	 * @param email
 	 */
 	private void performLogoutOnDB(String sessionId)
 	{
 		ConnectionWithAutoClose connection = null;
 		PreparedStatementWithAutoClose stmt = null;
 
 		try
 		{
 			connection = this.getAutoClosingConnection();
 
 			stmt = connection.prepareStatement(PREPARED_LOGOUT_USER_BY_SESSION_ID);
 			stmt.setString(1, sessionId);
 			stmt.executeUpdate();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (connection != null)
 				connection.close();
 		}
 	}
 
 	/**
 	 * @param email
 	 * @return the current session id for the given email; null if email is not in system or if user
 	 *         is offline
 	 */
 	private synchronized String performLookupSessionIdDB(String userKey)
 	{
 		String sessionId = null;
 
 		ConnectionWithAutoClose connection = null;
 		PreparedStatementWithAutoClose stmt = null;
 		ResultSet rs = null;
 
 		try
 		{
 			connection = this.getAutoClosingConnection();
 
 			stmt = connection.prepareStatement(PREPARED_SELECT_USER_BY_USER_KEY);
 			stmt.setString(1, userKey);
 			rs = stmt.executeQuery();
 
 			if (rs.next())
 				sessionId = rs.getString(COL_SESSION_ID);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (connection != null)
 				connection.close();
 		}
 
 		return sessionId;
 	}
 
 	/**
 	 * Looks up a unique user identifier from the given session identifier.
 	 * 
 	 * @param sessionId
 	 * @return -1 if the session was not in the database; user id otherwise
 	 */
 	private synchronized long performLookupUserId(String sessionId)
 	{
 		long uid = -1;
 
 		ConnectionWithAutoClose connection = null;
 		PreparedStatementWithAutoClose selectUser = null;
 		ResultSet rs = null;
 
 		try
 		{
 			connection = this.getAutoClosingConnection();
 
 			selectUser = connection.prepareStatement(PREPARED_SELECT_USER_BY_SESSION_ID);
 			selectUser.setString(1, sessionId);
 
 			rs = selectUser.executeQuery();
 
 			if (rs.next())
				uid = rs.getLong(COL_UID);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (connection != null)
 				connection.close();
 		}
 
 		return uid;
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.OnlineAuthenticator#usersLoggedIn()
 	 */
 	public Set<String> usersLoggedIn()
 	{
 		return this.performLookupOnlineUsersInDB();
 	}
 }
