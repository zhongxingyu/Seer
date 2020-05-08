 /*
  * Created on Mar 30, 2006
  */
 package ecologylab.oodss.authentication;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.naming.NamingException;
 
 import ecologylab.generic.Debug;
 import ecologylab.oodss.authentication.db.AuthenticationDBStrings;
 import ecologylab.oodss.exceptions.SaveFailedException;
 import ecologylab.sql.ConnectionWithAutoClose;
 import ecologylab.sql.PreparedStatementWithAutoClose;
 
 /**
  * Abstracts access to a database as a list of AuthenticationEntry's. Raw passwords are never
  * serialized using this object, only one-way hashes of them (see
  * {@link ecologylab.oodss.authentication.User AuthenticationListEntry}).
  * 
  * Instances of this should be used by a server to determine valid usernames and passwords.
  * 
  * Most methods in this class are synchronized, so that they cannot be interleaved on multiple
  * threads. This should prevent consistency errors.
  * 
  * This authentication list assumes that authentication list entry's username field refers to an
  * email address.
  * 
  * @author Zachary O. Toups (zach@ecologylab.net)
  */
 public class AuthenticationListDBImpl<U extends User> extends Debug implements
 		AuthenticationList<U>, AuthenticationDBStrings
 {
 	/** Database connection string; combines URL, username, and password. */
 	private String	dBConnectString	= null;
 
 	/**
 	 * Creates a new AuthenticationList based on a connection to a MySQL database. Lazily instantiates
 	 * the database connection as needed.
 	 * 
 	 * @param dbLocation
 	 *          URL for database in the form "mysql://...".
 	 * @param username
 	 *          username to connect to database.
 	 * @param password
 	 *          password for database.
 	 */
 	public AuthenticationListDBImpl(String dbLocation, String username, String password, String db)
 	{
 		super();
 
 		dBConnectString = "jdbc:"
 				+ dbLocation
 				+ "/"
 				+ db
 				+ "?user="
 				+ username
 				+ "&password="
 				+ password;
 	}
 
 	public AuthenticationListDBImpl()
 	{
 		super();
 	}
 
 	protected ConnectionWithAutoClose getAutoClosingConnection() throws SQLException
 	{
 		Connection conn = null;
 
 		try
 		{
 			conn = this.getConnection();
 		}
 		catch (NamingException e)
 		{
 			e.printStackTrace();
 		}
 
 		if (conn != null)
 			return new ConnectionWithAutoClose(conn);
 
 		return null;
 	}
 
 	protected Connection getConnection() throws SQLException, NamingException
 	{
 		try
 		{
 			Class.forName("org.postgresql.Driver").newInstance();
 		}
 		catch (InstantiationException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e)
 		{
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 
 		System.out.println("connect string: " + dBConnectString);
 		return DriverManager.getConnection(dBConnectString);
 	}
 
 	/**
 	 * Adds the given entry to this.
 	 * 
 	 * @throws SaveFailedException
 	 */
 	public synchronized boolean addUser(U user) throws SaveFailedException
 	{
 		if (!this.contains(user))
 		{
 			// TODO this is pretty kludgy; we could probably split out a new subclass to handle this
 			user.setUid(this.performInsertUser(user.getUserKey(), user.getPassword(),
 					(user instanceof UserWithAuxData ? ((UserWithAuxData) user).getAuxUserData() : null)));
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Inserts the information for a new user into the database. Does not check to see if the user
 	 * exists; this should be done prior to calling this method.
 	 * 
 	 * Returns the auto-increment UID for the user; this should be attached to the
 	 * AuthenticationListEntry that triggered the call to addUser.
 	 * 
 	 * @param userKey
 	 *          the String with which to look up the user
 	 * @param password
 	 * @param name
 	 * @return the auto-increment UID for the user. -1 if there was an error.
 	 * @throws SaveFailedException
 	 */
 	protected synchronized long performInsertUser(String userKey, String password, String auxUserData)
 			throws SaveFailedException
 	{
 		long userId = -1;
 
 		ConnectionWithAutoClose connection = null;
 		PreparedStatementWithAutoClose stmt = null;
 
 		try
 		{
 			connection = this.getAutoClosingConnection();
 
 			stmt = connection.prepareStatement(PREPARED_INSERT_USER, Statement.RETURN_GENERATED_KEYS);
 			stmt.setString(1, userKey);
 			stmt.setString(2, password);
 			stmt.setString(3, auxUserData);
 
 			stmt.executeUpdate();
 
 			ResultSet autoGenKeys = stmt.getGeneratedKeys();
 
 			if (autoGenKeys.next())
 				userId = autoGenKeys.getLong(1);
 
 			debug("new user_id generated: " + userId);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 
 			throw new SaveFailedException(e);
 		}
 		finally
 		{
 			if (connection != null)
 				connection.close();
 		}
 
 		return userId;
 	}
 
 	/**
 	 * Looks up the given user in the database and constructs a UserWithEmail object from the data.
 	 * Does not populate the password field of the UserWithEmail object.
 	 * 
 	 * @param userKey
 	 * @return
 	 */
 	protected synchronized UserWithAuxData retrieveUserFromDB(String userKey)
 	{
 		UserWithAuxData foundUser = null;
 
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
 			{
 				foundUser = new UserWithAuxData(rs.getString(COL_USER_KEY), null, rs
 						.getString(COL_AUX_USER_DATA));
 				foundUser.setLevel(rs.getInt(COL_LEVEL));
				foundUser.setUid(rs.getLong(DB_COL_USER_ID));
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
 
 		return foundUser;
 	}
 
 	/**
 	 * Cloning AuthenticationLists is not allowed, because it is a security violation.
 	 * 
 	 * This method just throws an UnsupportedOperationException.
 	 */
 	@Override
 	public final Object clone() throws UnsupportedOperationException
 	{
 		throw new UnsupportedOperationException(
 				"Cannot clone an AuthenticationList, for security reasons.");
 	}
 
 	/**
 	 * Checks to see if this contains the username given in entry; returns true if it does.
 	 * 
 	 * @param entry
 	 * @return
 	 */
 	public synchronized boolean contains(U entry)
 	{
 		return this.retrieveUserFromDB(entry.getUserKey()) != null;
 	}
 
 	/**
 	 * Retrieves the access level for the given entry.
 	 * 
 	 * @param entry
 	 * @return
 	 */
 	public synchronized int getAccessLevel(U entry)
 	{
 		UserWithAuxData foundUser = this.retrieveUserFromDB(entry.getUserKey());
 
 		if (foundUser != null)
 			return foundUser.getLevel();
 
 		return -1;
 	}
 
 	/**
 	 * Checks entry against the entries contained in this. Verifies that the username exists, and the
 	 * password matches; returns true if both are true.
 	 * 
 	 * @param entry
 	 * @return
 	 */
 	public synchronized boolean isValid(U entry)
 	{
 		UserWithAuxData foundUser = this.retrieveUserFromDB(entry.getUserKey());
 
 		return ((foundUser != null) && (entry.getPassword() != null) && entry
 				.compareHashedPassword(this.retrievePassword(entry.getUserKey())));
 	}
 
 	/**
 	 * Looks up the hashed password in the database and returns it.
 	 * 
 	 * @param email
 	 * @return
 	 */
 	private String retrievePassword(String userKey)
 	{
 		String password = null;
 
 		ConnectionWithAutoClose connection = null;
 		PreparedStatementWithAutoClose selectUserStmt = null;
 		ResultSet rs = null;
 
 		try
 		{
 			connection = this.getAutoClosingConnection();
 
 			selectUserStmt = connection.prepareStatement(PREPARED_SELECT_USER_BY_USER_KEY);
 			selectUserStmt.setString(1, userKey);
 
 			rs = selectUserStmt.executeQuery();
			if (rs.next())
				password = rs.getString(COL_PASSWORD);
			else
				password = null;
 		}
 		catch (SQLException e1)
 		{
 			e1.printStackTrace();
 
 			return null;
 		}
 		finally
 		{
 			if (connection != null)
 				connection.close();
 		}
 
 		return password;
 	}
 
 	/**
 	 * Attempts to remove the given object; this will succeed if and only if the following are true:
 	 * 
 	 * 1.) the Object is of type AuthenticationListEntry 2.) this list contains the
 	 * AuthenticationListEntry 3.) the AuthenticationListEntry's username and password both match the
 	 * one in this list
 	 * 
 	 * @param user
 	 *          the User to attempt to remove.
 	 * @throws SaveFailedException
 	 */
 	public synchronized boolean removeUser(U user) throws SaveFailedException
 	{
 		if (this.isValid(user))
 		{
 			String deleteUser = PREPARED_DELETE_USER_BY_USER_KEY;
 
 			ConnectionWithAutoClose connection = null;
 			PreparedStatementWithAutoClose stmt = null;
 
 			try
 			{
 				connection = this.getAutoClosingConnection();
 
 				stmt = connection.prepareStatement(deleteUser);
 				stmt.setString(1, user.getUserKey());
 
 				stmt.executeUpdate();
 			}
 			catch (SQLException e)
 			{
 				e.printStackTrace();
 				throw new SaveFailedException("SQLException occurred when attempting to remove user.", e);
 			}
 			finally
 			{
 				if (connection != null)
 					connection.close();
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Returns a String indicating the number of entries in the AuthenticationList.
 	 */
 	@Override
 	public String toString()
 	{
 		return "DBAuthenticationList";
 	}
 
 	/**
 	 * @see ecologylab.oodss.authentication.AuthenticationList#setUID(ecologylab.oodss.authentication.User)
 	 */
 	public void setUID(U entry)
 	{
 		UserWithAuxData foundUser = this.retrieveUserFromDB(entry.getUserKey());
 		entry.setUid(foundUser.getUid());
 	}
 
 	/**
 	 * This method does nothing, as all of the add / remove methods are automatically written to the
 	 * backing store (database) when they are called.
 	 */
 	public void save() throws SaveFailedException
 	{
 	}
 }
