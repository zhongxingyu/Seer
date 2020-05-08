 /**
  * 
  */
 package database;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLFeatureNotSupportedException;
 import java.sql.SQLIntegrityConstraintViolationException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.mysql.jdbc.Statement;
 
 /**
  * @author Sascha Meusel
  * 
  */
 public class DatabaseManager {
 
 	private Connection con = null;
 
 	private final PreparedStatement pstAddNewRequest;
 	private final PreparedStatement pstAddNewUser;
 	private final PreparedStatement pstGetAllRequests;
 	private final PreparedStatement pstGetAllUsers;
 	private final PreparedStatement pstGetUserWithEmail;
 	private final PreparedStatement pstGetUserWithId;
 	private final PreparedStatement pstUpdateRequest;
 	private final PreparedStatement pstUpdateUser;
 	private final PreparedStatement pstDeleteRequestWithRequestId;
 	private final PreparedStatement pstDeleteRequestsOfUserWithUserId;
 	private final PreparedStatement pstDeleteUserWithUserId;
 	private final PreparedStatement pstDeleteUserWithEmail;
 	private final PreparedStatement pstGetAllRequestsWithLimitOffset;
 	private final PreparedStatement pstGetRequestWithRequestId;
 	private final PreparedStatement pstGetRequestsWithUserId;
 	private final PreparedStatement pstGetRequestsWithUserIdLimitOffset;
 	private final PreparedStatement pstGetAllUsersWithLimitOffset;
 
 	private final static String addNewRequestString = "INSERT INTO Requests "
 			+ "(UserID, JSONRequest, RequestDate) VALUES(?, ?, ?)";
 
 	private final static String addNewUserString = "INSERT INTO Users "
 			+ "(Email, Passwordhash, Salt, FirstName, LastName, Address, "
 			+ "AdminFlag, Status, RegistrationDate, VerifiedDate)"
 			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
 	private final static String getAllRequestsString = "SELECT id, UserID, "
 			+ "JSONRequest, JSONResponse, PendingFlag, Costs, PaidFlag, "
 			+ "RequestDate, FinishedDate, CPUTime, FailedFlag, "
 			+ "FailDescription FROM Requests";
 
 	private final static String getAllUsersString = "SELECT id, Email, "
 			+ "Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, "
 			+ "Address, RegistrationDate, VerifiedDate, DeleteRequestDate "
 			+ "FROM Users";
 
 	private final static String getUserStringWithEmail = "SELECT id, Email, "
 			+ "Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, "
 			+ "Address, RegistrationDate, VerifiedDate, DeleteRequestDate "
 			+ "FROM Users WHERE Email = ?";
 
 	private final static String getUserStringWithId = "SELECT id, Email, "
 			+ "Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, "
 			+ "Address, RegistrationDate, VerifiedDate, DeleteRequestDate "
 			+ "FROM Users WHERE id = ?";
 
 	private final static String updateRequestString = "UPDATE Requests SET "
 			+ "UserID = ?, JSONRequest = ?, JSONResponse = ?, "
 			+ "PendingFlag = ?, Costs = ?, PaidFlag = ?, RequestDate = ?, "
 			+ "FinishedDate = ?, CPUTime = ?, FailedFlag = ?, "
 			+ "FailDescription = ? WHERE id = ?";
 
 	private final static String updateUserString = "UPDATE Users SET "
 			+ "Email = ?, Passwordhash = ?, Salt = ?, AdminFlag = ?, "
 			+ "Status = ?, FirstName = ?, LastName = ?, Address = ?, "
 			+ "RegistrationDate = ?, VerifiedDate = ?, DeleteRequestDate = ? "
 			+ "WHERE id = ?";
 
 	private final static String deleteRequestWithRequestIdString = "DELETE FROM Requests WHERE id = ?";
 
 	private final static String deleteRequestsOfUserWithUserIdString = "DELETE FROM Requests WHERE UserID = ?";
 
 	private final static String deleteUserWithUserIdString = "DELETE FROM Users WHERE id = ?";
 
 	private final static String deleteUserWithEmailString = "DELETE FROM Users WHERE Email = ?";
 
 	private final static String getAllRequestsWithLimitOffsetString = getAllRequestsString
 			+ " LIMIT ? OFFSET ?";
 
 	private final static String getRequestWithRequestIdString = getAllRequestsString
 			+ " WHERE id = ?";
 
 	private final static String getRequestsWithUserIdString = getAllRequestsString
 			+ " WHERE UserID = ?";
 
 	private final static String getRequestsWithUserIdLimitOffsetString = getRequestsWithUserIdString
 			+ " LIMIT ? OFFSET ?";
 	private final static String getAllUsersWithLimitOffsetString = getAllUsersString
 			+ " LIMIT ? OFFSET ?";
 
 	/**
 	 * Tries to establish a database connection and upholds the connection until
 	 * the garbage collection deletes the object or the connection is
 	 * disconnected. Also creates prepared statements.
 	 * 
 	 * @param url
 	 *            A database driver specific url of the form
 	 *            <i>jdbc:subprotocol:subnamewithoutdatabase</i> where
 	 *            <i>subnamewithoutdatabase</i> is the server address without
 	 *            the database name but with a slash at the end</br> Example:
 	 *            "jdbc:mysql://localhost:3306/"
 	 * @param dbName
 	 *            The database name Example: "tourenplaner"
 	 * @param userName
 	 *            User name of the database user account
 	 * @param password
 	 *            To the user according password
 	 * @throws SQLException
 	 *             Thrown if connection could not be established or if errors
 	 *             occur while creating prepared statements
 	 * @see java.sql.DriverManager.html#getConnection(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 	public DatabaseManager(String url, String dbName, String userName,
 			String password) throws SQLException {
 
 		con = DriverManager.getConnection(url + dbName, userName, password);
 
 		pstAddNewRequest = con.prepareStatement(addNewRequestString,
 				Statement.RETURN_GENERATED_KEYS);
 		pstAddNewUser = con.prepareStatement(addNewUserString,
 				Statement.RETURN_GENERATED_KEYS);
 		pstGetAllRequests = con.prepareStatement(getAllRequestsString);
 		pstGetAllUsers = con.prepareStatement(getAllUsersString);
 		pstGetUserWithEmail = con.prepareStatement(getUserStringWithEmail);
 		pstGetUserWithId = con.prepareStatement(getUserStringWithId);
 		pstUpdateRequest = con.prepareStatement(updateRequestString);
 		pstUpdateUser = con.prepareStatement(updateUserString);
 		pstDeleteRequestWithRequestId = con
 				.prepareStatement(deleteRequestWithRequestIdString);
 		pstDeleteRequestsOfUserWithUserId = con
 				.prepareStatement(deleteRequestsOfUserWithUserIdString);
 		pstDeleteUserWithUserId = con
 				.prepareStatement(deleteUserWithUserIdString);
 		pstDeleteUserWithEmail = con
 				.prepareStatement(deleteUserWithEmailString);
 		pstGetAllRequestsWithLimitOffset = con
 				.prepareStatement(getAllRequestsWithLimitOffsetString);
 		pstGetRequestWithRequestId = con
 				.prepareStatement(getRequestWithRequestIdString);
 		pstGetRequestsWithUserId = con
 				.prepareStatement(getRequestsWithUserIdString);
 		pstGetRequestsWithUserIdLimitOffset = con
 				.prepareStatement(getRequestsWithUserIdLimitOffsetString);
 		pstGetAllUsersWithLimitOffset = con
 				.prepareStatement(getAllUsersWithLimitOffsetString);
 	}
 
 	/**
 	 * Tries to insert a new request dataset into the database. The inserted
 	 * dataset will be pending, have no costs and will be unpaid.</br>SQL
 	 * command: {@value #addNewRequestString}
 	 * 
 	 * @param userID
 	 *            The id of the user, who has sent the request
 	 * @param jsonRequest
 	 *            The request encoded as byte array
 	 * @return Returns the inserted request object only if the id could received
 	 *         from the database and the insert was successful, else an
 	 *         exception will be thrown.
	 * @throws SQLFeatureNotSupportedException
	 *             Thrown if the id could not received.
 	 * @throws SQLException
 	 *             Thrown if the insertion failed.
 	 */
 	public RequestDataset addNewRequest(int userID, byte[] jsonRequest)
			throws SQLFeatureNotSupportedException, SQLException {
 
 		/*
 		  id              INT           NOT NULL AUTO_INCREMENT,
 		  UserID          INT           NOT NULL REFERENCES Users (id),
 		  JSONRequest     LONGBLOB,
 		  JSONResponse    LONGBLOB               DEFAULT NULL,
 		  PendingFlag     BOOL          NOT NULL DEFAULT 1,
 		  Costs           INT           NOT NULL DEFAULT 0,
 		  PaidFlag        BOOL          NOT NULL DEFAULT 0,
 		  RequestDate     DATETIME      NOT NULL,
 		  FinishedDate    DATETIME               DEFAULT NULL,
 		  CPUTime         BIGINT        NOT NULL DEFAULT 0,
 		  FailedFlag      BOOL          NOT NULL DEFAULT 0,
 		  FailDescription TEXT                   DEFAULT NULL,
 		  PRIMARY KEY (ID),
 		  FOREIGN KEY (UserID) REFERENCES Users (id)
 		*/
 		
 		RequestDataset request = null;
 		ResultSet generatedKeyResultSet = null;
 
 		Timestamp stamp = new Timestamp(System.currentTimeMillis());
 
 		pstAddNewRequest.setInt(1, userID);
 		pstAddNewRequest.setBytes(2, jsonRequest);
 		pstAddNewRequest.setTimestamp(3, stamp);
 
 		pstAddNewRequest.executeUpdate();
 
 		request = new RequestDataset(-1, userID, jsonRequest, null, true, 0,
 				false, new Date(stamp.getTime()), null, 0, false, null);
 
 		boolean hasKey = false;
		generatedKeyResultSet = pstAddNewRequest.getGeneratedKeys();
 		if (generatedKeyResultSet.next()) {
 			request.id = generatedKeyResultSet.getInt(1);
 			hasKey = true;
 		}
 		generatedKeyResultSet.close();
 		
 		if (!hasKey) {
 			System.err.println("Current database doesn't support " +
 					"java.sql.Statement.getGeneratedKeys()");
 			throw new SQLFeatureNotSupportedException(
 					"Current database doesn't support " +
 					"java.sql.Statement.getGeneratedKeys()");
 		}
 		
 		return request;
 	}
 
 	/**
 	 * Tries to insert a new user dataset into the database. </br>SQL command:
 	 * {@value #addNewUserString}
 	 * 
 	 * @param email
 	 *            Have to be unique, that means another user must not have the
 	 *            same email. Parameter will be trimmed from this method.
 	 * @param passwordhash
 	 *            Hash of the user password. Parameter will be trimmed from this
 	 *            method.
 	 * @param salt
 	 *            Salt for the user password hash. Parameter will be trimmed
 	 *            from this method.
 	 * @param firstName
 	 *            First name of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param lastName
 	 *            Last Name of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param address
 	 *            Address of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param isAdmin
 	 *            True, if user is an admin, else false.
 	 * @return Returns the inserted user object only if the id could received
 	 *         from the database and the insert was successful. If the insert
 	 *         was not successful because the email already existed, null will
 	 *         be returned.
	 * @throws SQLFeatureNotSupportedException
	 *             Thrown if the id could not received.
 	 * @throws SQLException
 	 *             Thrown if other errors occurred than a duplicate email.
 	 */
 	public UserDataset addNewUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
			boolean isAdmin) 
					throws SQLFeatureNotSupportedException, SQLException {
 
 		return addNewUser(email, passwordhash, salt, firstName, lastName,
 				address, isAdmin, UserStatusEnum.NeedsVerification, false);
 	}
 
 	/**
 	 * Tries to insert a new user dataset into the database, but request should
 	 * have legit admin authentication (will not be checked within this method).
 	 * New user will be verified. </br>SQL command: {@value #addNewUserString}
 	 * 
 	 * @param email
 	 *            Have to be unique, that means another user must not have the
 	 *            same email. Parameter will be trimmed from this method.
 	 * @param passwordhash
 	 *            Hash of the user password. Parameter will be trimmed from this
 	 *            method.
 	 * @param salt
 	 *            Salt for the user password hash. Parameter will be trimmed
 	 *            from this method.
 	 * @param firstName
 	 *            First name of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param lastName
 	 *            Last Name of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param address
 	 *            Address of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param isAdmin
 	 *            True, if user is an admin, else false.
 	 * @return Returns the inserted user object only if the id could received
 	 *         from the database and the insert was successful. If the insert
 	 *         was not successful because the email already existed, null will
 	 *         be returned.
	 * @throws SQLFeatureNotSupportedException
	 *             Thrown if the id could not received.
 	 * @throws SQLException
 	 *             Thrown if other errors occurred than a duplicate email.
 	 */
 	public UserDataset addNewVerifiedUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
			boolean isAdmin) 
					throws SQLFeatureNotSupportedException, SQLException {
 
 		return addNewUser(email, passwordhash, salt, firstName, lastName,
 				address, isAdmin, UserStatusEnum.Verified, true);
 	}
 
 	private UserDataset addNewUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
 			boolean isAdmin, UserStatusEnum status, boolean isVerified)
			throws SQLFeatureNotSupportedException, SQLException {
 
 		/*
 		  id                INT           NOT NULL AUTO_INCREMENT,
   		  Email             VARCHAR(255)  NOT NULL UNIQUE,
   		  Passwordhash      TEXT          NOT NULL,
   		  Salt              TEXT          NOT NULL,
   		  AdminFlag         BOOL          NOT NULL DEFAULT 0,
   		  Status            ENUM ('NeedsVerification', 
                           		  'VerificationFailed', 
                           		  'Verified', 
                                   'Delete') 
                                   NOT NULL DEFAULT 'NeedsVerification',
   		  FirstName         TEXT          NOT NULL,
   		  LastName          TEXT          NOT NULL,
   		  Address           TEXT          NOT NULL,
   		  RegistrationDate  DATETIME      NOT NULL,
   		  VerifiedDate      DATETIME               DEFAULT NULL,
   		  DeleteRequestDate DATETIME               DEFAULT NULL,
   		  PRIMARY KEY (ID)
 		*/
 		
 		UserDataset user = null;
 
 		email = email.trim();
 		passwordhash = passwordhash.trim();
 		salt = salt.trim();
 		firstName = firstName.trim();
 		lastName = lastName.trim();
 		address = address.trim();
 
 		try {
 
 			Timestamp registeredStamp = new Timestamp(
 					System.currentTimeMillis());
 			Timestamp verifiedStamp = null;
 			Date registeredDate = timestampToDate(registeredStamp);
 			Date verifiedDate = null;
 
 			if (isVerified) {
 				verifiedStamp = registeredStamp;
 				verifiedDate = registeredDate;
 			}
 
 			pstAddNewUser.setString(1, email);
 			pstAddNewUser.setString(2, passwordhash);
 			pstAddNewUser.setString(3, salt);
 			pstAddNewUser.setString(4, firstName);
 			pstAddNewUser.setString(5, lastName);
 			pstAddNewUser.setString(6, address);
 			pstAddNewUser.setBoolean(7, isAdmin);
 			pstAddNewUser.setString(8, status.toString());
 			pstAddNewUser.setTimestamp(9, registeredStamp);
 			pstAddNewUser.setTimestamp(10, verifiedStamp);
 
 			pstAddNewUser.executeUpdate();
 
 			user = new UserDataset(-1, email, passwordhash, salt, isAdmin,
 					status, firstName, lastName, address, registeredDate,
 					verifiedDate, null);
 
 			// try {
 			ResultSet generatedKeyResultSet = null;
 			
 			generatedKeyResultSet = pstAddNewUser.getGeneratedKeys();
 			
 			boolean hasKey = false;
 			if (generatedKeyResultSet.next()) {
 				user.id = generatedKeyResultSet.getInt(1);
 				hasKey = true;
 			}
 			generatedKeyResultSet.close();
 			
 			if (!hasKey) {
 				System.err.println("Current database doesn't support " +
 						"java.sql.Statement.getGeneratedKeys()");
 				user = this.getUser(email);
 			}
 			/*
 			 * } catch (SQLFeatureNotSupportedException ex) { if
 			 * (ex.getNextException() != null) { throw ex; } }
 			 */
 		// catch if duplicate key error
 		} catch (SQLIntegrityConstraintViolationException ex) {
 			if (ex.getNextException() == null) {
 				return null;
 			}
 			throw ex;
 		}
 		return user;
 	}
 
 	/**
 	 * Updates the Requests table row with the id given through the parameter
 	 * (<b><code>request.id</code></b>). All values within the given object will
 	 * be written into the database, so all old values within the row will be
 	 * overwritten. <b><code>request.id</code></b> has to be > 0 and must exists
 	 * within the database table. </br>SQL command:
 	 * {@value #updateRequestString}
 	 * 
 	 * @param request
 	 *            The request object to write into the database.
 	 * @throws SQLException
 	 *             Thrown if update fails.
 	 */
 	public void updateRequest(RequestDataset request) throws SQLException {
 
 		pstUpdateRequest.setInt(1, request.userID);
 		pstUpdateRequest.setBytes(2, request.jsonRequest);
 		pstUpdateRequest.setBytes(3, request.jsonResponse);
 		pstUpdateRequest.setBoolean(4, request.isPending);
 		pstUpdateRequest.setInt(5, request.costs);
 		pstUpdateRequest.setBoolean(6, request.isPaid);
 		pstUpdateRequest.setTimestamp(7, dateToTimestamp(request.requestDate));
 		pstUpdateRequest.setTimestamp(8, dateToTimestamp(request.finishedDate));
 		pstUpdateRequest.setLong(9, request.cpuTime);
 		pstUpdateRequest.setBoolean(10, request.hasFailed);
 		pstUpdateRequest.setString(11, request.failDescription);
 		pstUpdateRequest.setInt(12, request.id);
 
 		pstUpdateRequest.executeUpdate();
 	}
 
 	/**
 	 * Updates the Users table row with the id given through the parameter (<b>
 	 * <code>request.id</code></b>). All values within the given object will be
 	 * written into the database, so all old values within the row will be
 	 * overwritten. <b><code>user.id</code></b> has to be > 0 and must exists
 	 * within the database table. </br>SQL command: {@value #updateUserString}
 	 * 
 	 * @param request
 	 *            The user object to write into the database.
 	 * @throws SQLException
 	 *             Thrown if update fails.
 	 */
 	public void updateUser(UserDataset user) throws SQLException {
 		pstUpdateUser.setString(1, user.email);
 		pstUpdateUser.setString(2, user.passwordhash);
 		pstUpdateUser.setString(3, user.salt);
 		pstUpdateUser.setBoolean(4, user.isAdmin);
 		pstUpdateUser.setString(5, user.status.toString());
 		pstUpdateUser.setString(6, user.firstName);
 		pstUpdateUser.setString(7, user.lastName);
 		pstUpdateUser.setString(8, user.address);
 		pstUpdateUser.setTimestamp(9, dateToTimestamp(user.registrationDate));
 		pstUpdateUser.setTimestamp(10, dateToTimestamp(user.verifiedDate));
 		pstUpdateUser.setTimestamp(11, dateToTimestamp(user.deleteRequestDate));
 		pstUpdateUser.setInt(12, user.id);
 
 		pstUpdateUser.executeUpdate();
 	}
 
 	/**
 	 * Deletes the Requests table row with the given request id. </br>SQL
 	 * command: {@value #deleteRequestWithRequestIdString}
 	 * 
 	 * @param id
 	 *            Request id
 	 * @throws SQLException
 	 *             Thrown if delete fails.
 	 */
 	public void deleteRequest(int id) throws SQLException {
 		pstDeleteRequestWithRequestId.setInt(1, id);
 
 		pstDeleteRequestWithRequestId.executeUpdate();
 	}
 
 	/**
 	 * Deletes the Requests table rows with the given user id. </br>SQL command:
 	 * {@value #deleteRequestsOfUserWithUserIdString}
 	 * 
 	 * @param userId
 	 *            User id of the user, whose requests should be deleted.
 	 * @throws SQLException
 	 *             Thrown if delete fails.
 	 */
 	public void deleteRequestsOfUser(int userId) throws SQLException {
 		pstDeleteRequestsOfUserWithUserId.setInt(1, userId);
 
 		pstDeleteRequestsOfUserWithUserId.executeUpdate();
 	}
 
 	/**
 	 * Deletes the Users table row with the given user id. Depending on the
 	 * database configuration(for example strict mode) maybe the according
 	 * requests could be deleted too because of the FOREIGN KEY UserId within
 	 * the Requests table. </br>SQL command:
 	 * {@value #deleteUserWithUserIdString}
 	 * 
 	 * @param userId
 	 *            User id
 	 * @throws SQLException
 	 *             Thrown if delete fails.
 	 */
 	public void deleteUser(int userId) throws SQLException {
 		pstDeleteUserWithUserId.setInt(1, userId);
 
 		pstDeleteUserWithUserId.executeUpdate();
 	}
 
 	/**
 	 * Deletes the Users table row with the given user email. Depending on the
 	 * database configuration(for example strict mode) maybe the according
 	 * requests could be deleted too because of the FOREIGN KEY UserId within
 	 * the Requests table. </br>SQL command: {@value #deleteUserWithEmailString}
 	 * 
 	 * @param email
 	 * @throws SQLException
 	 *             Thrown if delete fails.
 	 */
 	public void deleteUser(String email) throws SQLException {
 		pstDeleteUserWithEmail.setString(1, email);
 
 		pstDeleteUserWithEmail.executeUpdate();
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table. If no requests
 	 * are within the table, an empty list will be returned. </br>SQL command:
 	 * {@value #getAllRequestsString}
 	 * 
 	 * @return A list with all requests. If no requests exists, the list is
 	 *         empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<RequestDataset> getAllRequests() throws SQLException {
 		ResultSet resultSet = pstGetAllRequests.executeQuery();
 		ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new RequestDataset(resultSet.getInt(1),
 					resultSet.getInt(2), resultSet.getBytes(3), resultSet
 							.getBytes(4), resultSet.getBoolean(5), resultSet
 							.getInt(6), resultSet.getBoolean(7),
 					timestampToDate(resultSet.getTimestamp(8)),
 					timestampToDate(resultSet.getTimestamp(9)), resultSet
 							.getLong(10), resultSet.getBoolean(11), resultSet
 							.getString(12)));
 		}
 		
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table with regard to
 	 * the limit and offset constraints. If no requests are found with the given
 	 * constraints or the table is empty, an empty list will be returned.
 	 * </br>SQL command: {@value #getAllRequestsWithLimitOffsetString}
 	 * 
 	 * @param limit
 	 *            How many rows should maximal selected.
 	 * @param offset
 	 *            How many rows should be skimmed.
 	 * @return A list with all selected requests. If no requests selected, the
 	 *         list is empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<RequestDataset> getAllRequests(int limit, int offset)
 			throws SQLException {
 
 		pstGetAllRequestsWithLimitOffset.setInt(1, limit);
 		pstGetAllRequestsWithLimitOffset.setInt(2, offset);
 
 		ResultSet resultSet = pstGetAllRequestsWithLimitOffset.executeQuery();
 		ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new RequestDataset(resultSet.getInt(1),
 					resultSet.getInt(2), resultSet.getBytes(3), resultSet
 							.getBytes(4), resultSet.getBoolean(5), resultSet
 							.getInt(6), resultSet.getBoolean(7),
 					timestampToDate(resultSet.getTimestamp(8)),
 					timestampToDate(resultSet.getTimestamp(9)), resultSet
 							.getLong(10), resultSet.getBoolean(11), resultSet
 							.getString(12)));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a request object of the Requests table with the given request id.
 	 * 
 	 * </br>SQL command: {@value #getRequestWithRequestIdString}
 	 * 
 	 * @param id
 	 *            Request id
 	 * @return A request object if the request with the id exists, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public RequestDataset getRequest(int id) throws SQLException {
 
 		pstGetRequestWithRequestId.setInt(1, id);
 		ResultSet resultSet = pstGetRequestWithRequestId.executeQuery();
 		RequestDataset request = null;
 
 		while (resultSet.next()) {
 			request = new RequestDataset(resultSet.getInt(1),
 					resultSet.getInt(2), resultSet.getBytes(3),
 					resultSet.getBytes(4), resultSet.getBoolean(5),
 					resultSet.getInt(6), resultSet.getBoolean(7),
 					timestampToDate(resultSet.getTimestamp(8)),
 					timestampToDate(resultSet.getTimestamp(9)),
 					resultSet.getLong(10), resultSet.getBoolean(11),
 					resultSet.getString(12));
 		}
 		resultSet.close();
 
 		return request;
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table which have the
 	 * given user id. If no requests are found with the given user id or the
 	 * table is empty, an empty list will be returned. </br>SQL command:
 	 * {@value #getRequestsWithUserIdString}
 	 * 
 	 * @param userId
 	 * @return A list with all selected requests. If no requests selected, the
 	 *         list is empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<RequestDataset> getRequests(int userId) throws SQLException {
 		pstGetRequestsWithUserId.setInt(1, userId);
 
 		ResultSet resultSet = pstGetRequestsWithUserId.executeQuery();
 		ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new RequestDataset(resultSet.getInt(1),
 					resultSet.getInt(2), resultSet.getBytes(3), resultSet
 							.getBytes(4), resultSet.getBoolean(5), resultSet
 							.getInt(6), resultSet.getBoolean(7),
 					timestampToDate(resultSet.getTimestamp(8)),
 					timestampToDate(resultSet.getTimestamp(9)), resultSet
 							.getLong(10), resultSet.getBoolean(11), resultSet
 							.getString(12)));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table which have the
 	 * given user id with regard to the limit and offset constraints. If no
 	 * requests are found with the given user id and given constraints or the
 	 * table is empty, an empty list will be returned. </br>SQL command:
 	 * {@value #getRequestsWithUserIdLimitOffsetString}
 	 * 
 	 * @param userId
 	 *            User id
 	 * @param limit
 	 *            How many rows should maximal selected.
 	 * @param offset
 	 *            How many rows should be skimmed.
 	 * @return A list with all selected requests. If no requests selected, the
 	 *         list is empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<RequestDataset> getRequests(int userId, int limit, int offset)
 			throws SQLException {
 		pstGetRequestsWithUserIdLimitOffset.setInt(1, userId);
 		pstGetRequestsWithUserIdLimitOffset.setInt(2, limit);
 		pstGetRequestsWithUserIdLimitOffset.setInt(3, offset);
 
 		ResultSet resultSet = pstGetRequestsWithUserIdLimitOffset
 				.executeQuery();
 		ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new RequestDataset(resultSet.getInt(1),
 					resultSet.getInt(2), resultSet.getBytes(3), resultSet
 							.getBytes(4), resultSet.getBoolean(5), resultSet
 							.getInt(6), resultSet.getBoolean(7),
 					timestampToDate(resultSet.getTimestamp(8)),
 					timestampToDate(resultSet.getTimestamp(9)), resultSet
 							.getLong(10), resultSet.getBoolean(11), resultSet
 							.getString(12)));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a list with all users within the Users table. If the table is
 	 * empty, an empty list will be returned. </br>SQL command:
 	 * {@value #getAllUsersString}
 	 * 
 	 * @return A list with all selected users. If no users selected, the list
 	 *         is empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<UserDataset> getAllUsers() throws SQLException {
 		ResultSet resultSet = pstGetAllUsers.executeQuery();
 		ArrayList<UserDataset> list = new ArrayList<UserDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new UserDataset(resultSet.getInt(1),
 					resultSet.getString(2), resultSet.getString(3), resultSet
 							.getString(4), resultSet.getBoolean(5),
 					UserStatusEnum.valueOf(resultSet.getString(6)), resultSet
 							.getString(7), resultSet.getString(8), resultSet
 							.getString(9), timestampToDate(resultSet
 							.getTimestamp(10)), timestampToDate(resultSet
 							.getTimestamp(11)), timestampToDate(resultSet
 							.getTimestamp(12))));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a list with all users within the Users table with regard to the
 	 * limit and offset constraints. If no users are found with the given
 	 * constraints or the table is empty, an empty list will be returned.
 	 * </br>SQL command: {@value #getAllUsersWithLimitOffsetString}
 	 * 
 	 * @param limit
 	 *            How many rows should maximal selected.
 	 * @param offset
 	 *            How many rows should be skimmed.
 	 * @return A list with all selected users. If no users selected, the list
 	 *         is empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<UserDataset> getAllUsers(int limit, int offset) throws SQLException {
 		pstGetAllUsersWithLimitOffset.setInt(1, limit);
 		pstGetAllUsersWithLimitOffset.setInt(2, offset);
 
 		ResultSet resultSet = pstGetAllUsersWithLimitOffset.executeQuery();
 		ArrayList<UserDataset> list = new ArrayList<UserDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new UserDataset(resultSet.getInt(1),
 					resultSet.getString(2), resultSet.getString(3), resultSet
 							.getString(4), resultSet.getBoolean(5),
 					UserStatusEnum.valueOf(resultSet.getString(6)), resultSet
 							.getString(7), resultSet.getString(8), resultSet
 							.getString(9), timestampToDate(resultSet
 							.getTimestamp(10)), timestampToDate(resultSet
 							.getTimestamp(11)), timestampToDate(resultSet
 							.getTimestamp(12))));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a user object from the Users table with the given email. </br>SQL
 	 * command: {@value #getUserStringWithEmail}
 	 * 
 	 * @param email
 	 * @return The user object, if the user is found, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public UserDataset getUser(String email) throws SQLException {
 		pstGetUserWithEmail.setString(1, email);
 		ResultSet resultSet = pstGetUserWithEmail.executeQuery();
 		UserDataset user = null;
 
 		while (resultSet.next()) {
 
 			user = new UserDataset(resultSet.getInt(1), resultSet.getString(2),
 					resultSet.getString(3), resultSet.getString(4),
 					resultSet.getBoolean(5), UserStatusEnum.valueOf(resultSet
 							.getString(6)), resultSet.getString(7),
 					resultSet.getString(8), resultSet.getString(9),
 					timestampToDate(resultSet.getTimestamp(10)),
 					timestampToDate(resultSet.getTimestamp(11)),
 					timestampToDate(resultSet.getTimestamp(12)));
 		}
 		resultSet.close();
 
 		return user;
 	}
 
 	/**
 	 * Gets a user object from the Users table with the given user id. </br>SQL
 	 * command: {@value #getUserStringWithId}
 	 * 
 	 * @param id
 	 *            User id
 	 * @returnThe user object, if the user is found, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public UserDataset getUser(int id) throws SQLException {
 		pstGetUserWithId.setInt(1, id);
 		ResultSet resultSet = pstGetUserWithId.executeQuery();
 		UserDataset user = null;
 
 		while (resultSet.next()) {
 
 			user = new UserDataset(resultSet.getInt(1), resultSet.getString(2),
 					resultSet.getString(3), resultSet.getString(4),
 					resultSet.getBoolean(5), UserStatusEnum.valueOf(resultSet
 							.getString(6)), resultSet.getString(7),
 					resultSet.getString(8), resultSet.getString(9),
 					timestampToDate(resultSet.getTimestamp(10)),
 					timestampToDate(resultSet.getTimestamp(11)),
 					timestampToDate(resultSet.getTimestamp(12)));
 		}
 		resultSet.close();
 
 		return user;
 	}
 
 	/**
 	 * Converts a sql timestamp to a java date
 	 * 
 	 * @param stamp
 	 *            The sql timestamp
 	 * @return The generated jave date
 	 */
 	private Date timestampToDate(Timestamp stamp) {
 		if (stamp == null) {
 			return null;
 		}
 		return new Date(stamp.getTime());
 	}
 
 	/**
 	 * Converts a java date to a sql timestamp
 	 * 
 	 * @param date
 	 *            The java date
 	 * @return The generated sql timestamp
 	 */
 	private Timestamp dateToTimestamp(Date date) {
 		if (date == null) {
 			return null;
 		}
 		return new Timestamp(date.getTime());
 	}
 
 	/**
 	 * Yet only for testing purpose
 	 */
 	public void printDatabaseInformation() {
 		try {
 			addNewUser("sascha@tourenplaner", "xyz", "pepper", "sascha",
 					"verratinet", "", true);
 			// addNewRequest(10,
 			// ("EinTestBlob mit falscher UserID").getBytes());
 		} catch (SQLIntegrityConstraintViolationException e) {
 
 			SQLException ex = e;
 
 			do {
 
 				System.out.println("errorcode: " + ex.getErrorCode());
 				System.out.println("sqlstate: " + ex.getSQLState());
 				try {
 					System.out.println("stateType: "
 							+ con.getMetaData().getSQLStateType());
 				} catch (SQLException e1) {
 					System.out.println("someConnectionError:");
 					e1.printStackTrace();
 				}
 				System.out.println(ex.getLocalizedMessage());
 
 				ex = ex.getNextException();
 			} while (ex != null);
 
 			System.out.println("xopen: " + DatabaseMetaData.sqlStateXOpen);
 			System.out.println("sql: " + DatabaseMetaData.sqlStateSQL);
 			System.out.println("sql99: " + DatabaseMetaData.sqlStateSQL99);
 
 		} catch (SQLException ex) {
 			System.out.println("anotherConnectionError:");
 			ex.printStackTrace();
 		}
 
 	}
 
 }
