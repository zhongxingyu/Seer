 /**
  * 
  */
 package database;
 
 import com.mysql.jdbc.Statement;
 import config.ConfigManager;
 
 import java.sql.*;
 import java.util.*;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Sascha Meusel
  * 
  */
 public class DatabaseManager {
     
     private static Logger log = Logger.getLogger("database");
 
 	private Connection con = null;
     
     private final String url;
     private final String userName;
     private final String password;
 
     private final int maxTries = initMaxTries();
 
     private static HashMap<SqlStatementEnum, SqlStatementString> sqlStatementStringMap = createSqlStatementStringMap();
     private HashMap<SqlStatementEnum, PreparedStatement> preparedStatementMap;
     
 
     /*
        INSERT statements
      */
 	private final static String strAddNewRequest = "INSERT INTO Requests "
 			+ "(UserID, Algorithm, JSONRequest, RequestDate) VALUES(?, ?, ?, ?)";
 
 	private final static String strAddNewUser = "INSERT INTO Users "
 			+ "(Email, Passwordhash, Salt, FirstName, LastName, Address, "
 			+ "AdminFlag, Status, RegistrationDate, VerifiedDate)"
 			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
 
     /*
        SELECT COUNT(*) statements
      */
     private final static String strCountAllRequests = "SELECT COUNT(*) FROM Requests";
 
     private final static String strCountRequestsWithUserId = "SELECT COUNT(*) FROM Requests WHERE UserID = ?";
 
     private final static String strCountAllUsers = "SELECT COUNT(*) FROM Users";
 
 
     /*
        SELECT statements for table Requests
      */
     
 	private final static String strGetAllRequests = "SELECT id, UserID, "
             + "Algorithm, JSONRequest, JSONResponse, PendingFlag, Costs, PaidFlag, "
             + "RequestDate, FinishedDate, CPUTime, FailedFlag, "
             + "FailDescription FROM Requests";
 
     private final static String strGetAllRequestsWithLimitOffset = strGetAllRequests
             + " LIMIT ? OFFSET ?";
 
     private final static String strGetRequestsWithUserIdLimitOffset = strGetAllRequests
             + " WHERE UserID = ? LIMIT ? OFFSET ?";
 
 
     private final static String strGetAllRequestsNoJson = "SELECT id, UserID, "
             + "Algorithm, PendingFlag, Costs, PaidFlag, "
             + "RequestDate, FinishedDate, CPUTime, FailedFlag, "
             + "FailDescription FROM Requests";
 
     private final static String strGetAllRequestsNoJsonWithLimitOffset = strGetAllRequestsNoJson
             + " LIMIT ? OFFSET ?";
 
     private final static String strGetRequestsNoJsonWithUserIdLimitOffset = strGetAllRequestsNoJson
             + " WHERE UserID = ? LIMIT ? OFFSET ?";
 
 
     // single result
     private final static String strGetRequestWithRequestId = strGetAllRequests
             + " WHERE id = ?";
 
 
     /*
        SELECT statements for table Users
      */
 	private final static String strGetAllUsers = "SELECT id, Email, "
 			+ "Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, "
 			+ "Address, RegistrationDate, VerifiedDate, DeleteRequestDate "
 			+ "FROM Users";
 
     private final static String strGetAllUsersWithLimitOffset = strGetAllUsers
             + " LIMIT ? OFFSET ?";
 
     // single result
 	private final static String strGetUserWithEmail = strGetAllUsers + " WHERE Email = ?";
 
     // single result
 	private final static String strGetUserWithId = strGetAllUsers + " WHERE id = ?";
 
 
 
     /*
        UPDATE statements
      */
 	private final static String strUpdateRequest = "UPDATE Requests SET "
 			+ "UserID = ?, Algorithm = ?, JSONRequest = ?, JSONResponse = ?, "
 			+ "PendingFlag = ?, Costs = ?, PaidFlag = ?, RequestDate = ?, "
 			+ "FinishedDate = ?, CPUTime = ?, FailedFlag = ?, "
 			+ "FailDescription = ? WHERE id = ?";
 
 	private final static String strUpdateRequestWithComputeResult = "UPDATE Requests SET JSONResponse = ?, "
 			+ "PendingFlag = ?, Costs = ?, "
 			+ "FinishedDate = ?, CPUTime = ?, FailedFlag = ?, "
 			+ "FailDescription = ? WHERE id = ?";
 
 	private final static String strUpdateUser = "UPDATE Users SET "
 			+ "Email = ?, Passwordhash = ?, Salt = ?, AdminFlag = ?, "
 			+ "Status = ?, FirstName = ?, LastName = ?, Address = ?, "
 			+ "RegistrationDate = ?, VerifiedDate = ?, DeleteRequestDate = ? "
 			+ "WHERE id = ?";
 
 
     /*
        DELETE statements
      */
 	private final static String strDeleteRequestWithRequestId = "DELETE FROM Requests WHERE id = ?";
 
 	private final static String strDeleteRequestsOfUserWithUserId = "DELETE FROM Requests WHERE UserID = ?";
 
 	private final static String strDeleteUserWithUserId = "DELETE FROM Users WHERE id = ?";
 
 	private final static String strDeleteUserWithEmail = "DELETE FROM Users WHERE Email = ?";
 
 
 
 	/**
 	 * Tries to establish a database connection and upholds the connection until
 	 * the garbage collection deletes the object or the connection is
 	 * disconnected. Also creates prepared statements.
 	 * 
 	 * @param url
 	 *            A database driver specific url of the form
 	 *            <i>jdbc:subprotocol:subname</i> where <i>subname</i> is the server address with
      *            the database name (for example &quot;tourenplaner&quot;)
      *            and at the end some connection properties if needed
      *            (for example &quot;?autoReconnect=true&quot;)
      *            </br> Example:
 	 *            "jdbc:mysql://localhost:3306/tourenplaner?autoReconnect=true"
 	 * @param userName
 	 *            User name of the database user account
 	 * @param password
 	 *            To the user according password
 	 * @throws SQLException
 	 *             Thrown if connection could not be established or if errors
 	 *             occur while creating prepared statements
 	 * @see java.sql.DriverManager#getConnection(java.lang.String,java.lang.String, java.lang.String)
 	 */
 	public DatabaseManager(String url, String userName,
 			String password) throws SQLException {
         this.url = url;
         this.userName = userName;
         this.password = password;
         
 		this.init();
 	}
 
 
     private enum SqlStatementEnum {
         AddNewRequest,
         AddNewUser,
 
         GetAllUsers,
         GetUserWithEmail,
         GetUserWithId,
         GetRequestWithRequestId,
 
         UpdateRequest,
         UpdateRequestWithComputeResult,
         UpdateUser,
 
         DeleteRequestWithRequestId,
         DeleteRequestsOfUserWithUserId,
         DeleteUserWithUserId,
         DeleteUserWithEmail,
 
         GetAllRequestsWithLimitOffset,
         GetRequestsWithUserIdLimitOffset,
         GetAllUsersWithLimitOffset,
 
         GetAllRequestsNoJsonWithLimitOffset,
         GetRequestsNoJsonWithUserIdLimitOffset,
 
         CountAllRequests,
         CountRequestsWithUserId,
         CountAllUsers
     }
 
 
     private static int initMaxTries() {
         int maxTries = ConfigManager.getInstance().getEntryInt("maxdbtries", 2);
         if (maxTries > 0) {
             return maxTries;
         }
         return 2;
     }
 
     private static HashMap<SqlStatementEnum, SqlStatementString> createSqlStatementStringMap() {
         HashMap<SqlStatementEnum, SqlStatementString> sqlStatementMap = new HashMap<SqlStatementEnum, SqlStatementString>(21);
 
         // INSERT statements
 
         sqlStatementMap.put(SqlStatementEnum.AddNewRequest,
                 new SqlStatementString(strAddNewRequest,
                         Statement.RETURN_GENERATED_KEYS));
 
         sqlStatementMap.put(SqlStatementEnum.AddNewUser,
                 new SqlStatementString(strAddNewUser,
                         Statement.RETURN_GENERATED_KEYS));
 
         // SELECT statements without limit and without offset
 
         sqlStatementMap.put(SqlStatementEnum.GetAllUsers,
                 new SqlStatementString(strGetAllUsers));
 
         sqlStatementMap.put(SqlStatementEnum.GetUserWithEmail,
                 new SqlStatementString(strGetUserWithEmail));
 
         sqlStatementMap.put(SqlStatementEnum.GetUserWithId,
                 new SqlStatementString(strGetUserWithId));
 
         sqlStatementMap.put(SqlStatementEnum.GetRequestWithRequestId,
                 new SqlStatementString(strGetRequestWithRequestId));
 
         // UPDATE statements
 
         sqlStatementMap.put(SqlStatementEnum.UpdateRequest,
                 new SqlStatementString(strUpdateRequest));
 
         sqlStatementMap.put(SqlStatementEnum.UpdateRequestWithComputeResult,
                 new SqlStatementString(strUpdateRequestWithComputeResult));
 
         sqlStatementMap.put(SqlStatementEnum.UpdateUser,
                 new SqlStatementString(strUpdateUser));
 
         // DELETE statements
 
         sqlStatementMap.put(SqlStatementEnum.DeleteRequestWithRequestId,
                 new SqlStatementString(strDeleteRequestWithRequestId));
 
         sqlStatementMap.put(SqlStatementEnum.DeleteRequestsOfUserWithUserId,
                 new SqlStatementString(strDeleteRequestsOfUserWithUserId));
 
         sqlStatementMap.put(SqlStatementEnum.DeleteUserWithUserId,
                 new SqlStatementString(strDeleteUserWithUserId));
 
         sqlStatementMap.put(SqlStatementEnum.DeleteUserWithEmail,
                 new SqlStatementString(strDeleteUserWithEmail));
 
         // SELECT statements with limit and offset
 
         sqlStatementMap.put(SqlStatementEnum.GetAllRequestsWithLimitOffset,
                 new SqlStatementString(strGetAllRequestsWithLimitOffset));
 
         sqlStatementMap.put(SqlStatementEnum.GetRequestsWithUserIdLimitOffset,
                 new SqlStatementString(strGetRequestsWithUserIdLimitOffset));
 
         sqlStatementMap.put(SqlStatementEnum.GetAllUsersWithLimitOffset,
                 new SqlStatementString(strGetAllUsersWithLimitOffset));
 
         sqlStatementMap.put(SqlStatementEnum.GetAllRequestsNoJsonWithLimitOffset,
                 new SqlStatementString(strGetAllRequestsNoJsonWithLimitOffset));
 
         sqlStatementMap.put(SqlStatementEnum.GetRequestsNoJsonWithUserIdLimitOffset,
                 new SqlStatementString(strGetRequestsNoJsonWithUserIdLimitOffset));
 
         // statements for COUNTING rows
 
         sqlStatementMap.put(SqlStatementEnum.CountAllRequests,
                 new SqlStatementString(strCountAllRequests));
 
         sqlStatementMap.put(SqlStatementEnum.CountRequestsWithUserId,
                 new SqlStatementString(strCountRequestsWithUserId));
 
         sqlStatementMap.put(SqlStatementEnum.CountAllUsers,
                 new SqlStatementString(strCountAllUsers));
 
         return sqlStatementMap;
     }
 
     private static HashMap<SqlStatementEnum, PreparedStatement> createPreparedStatementMap(Connection con)
             throws SQLException {
         HashMap<SqlStatementEnum, PreparedStatement> pstMap
                 = new HashMap<SqlStatementEnum, PreparedStatement>(sqlStatementStringMap.size());
 
         Iterator<SqlStatementEnum> iterator = sqlStatementStringMap.keySet().iterator();
 
         SqlStatementEnum key;
         while (iterator.hasNext()) {
             key = iterator.next();
             pstMap.put(key, sqlStatementStringMap.get(key).prepareStatement(con));
         }
         
         return pstMap;
     }
 
     private void init() throws SQLException {
         con = DriverManager.getConnection(url, userName, password);
         this.preparedStatementMap = createPreparedStatementMap(con);
     }
 
 	/**
 	 * Tries to insert a new request dataset into the database. The inserted
 	 * dataset will be pending, have no costs and will be unpaid.</br>SQL
 	 * command: {@value #strAddNewRequest}
 	 * 
 	 * @param userID
 	 *            The id of the user, who has sent the request
 	 * @param algorithm
 	 *            The algorithm name
 	 * @param jsonRequest
 	 *            The request encoded as byte array
 	 * @return Returns the inserted request object only if the id could received
 	 *         from the database and the insert was successful, else an
 	 *         exception will be thrown.
 	 * @throws SQLFeatureNotSupportedException
 	 *             Thrown if the id could not received or another function is
 	 *             not supported by driver.
 	 * @throws SQLException
 	 *             Thrown if the insertion failed.
 	 */
 	public RequestDataset addNewRequest(int userID, String algorithm,
 			byte[] jsonRequest) throws SQLException {
 
 		/*
 		 * id INT NOT NULL AUTO_INCREMENT, UserID INT NOT NULL REFERENCES Users
 		 * (id), Algorithm VARCHAR(255) NOT NULL, JSONRequest LONGBLOB,
 		 * JSONResponse LONGBLOB DEFAULT NULL, PendingFlag BOOL NOT NULL DEFAULT
 		 * 1, Costs INT NOT NULL DEFAULT 0, PaidFlag BOOL NOT NULL DEFAULT 0,
 		 * RequestDate DATETIME NOT NULL, FinishedDate DATETIME DEFAULT NULL,
 		 * CPUTime BIGINT NOT NULL DEFAULT 0, FailedFlag BOOL NOT NULL DEFAULT
 		 * 0, FailDescription TEXT DEFAULT NULL, PRIMARY KEY (ID), FOREIGN KEY
 		 * (UserID) REFERENCES Users (id)
 		 */
 
         PreparedStatement pstAddNewRequest = preparedStatementMap.get(SqlStatementEnum.AddNewRequest);
         
 		RequestDataset request = null;
 		ResultSet generatedKeyResultSet;
 
         boolean hasKey = false;
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 Timestamp stamp = new Timestamp(System.currentTimeMillis());
 
                 pstAddNewRequest.setInt(1, userID);
                 pstAddNewRequest.setString(2, algorithm);
                 pstAddNewRequest.setBytes(3, jsonRequest);
                 pstAddNewRequest.setTimestamp(4, stamp);
 
                 pstAddNewRequest.executeUpdate();
                 // if no exception occurred, request is added, so do not try again
                 tryAgain = -1;
 
                 request = new RequestDataset(-1, userID, algorithm, jsonRequest, null,
                         true, 0, false, new Date(stamp.getTime()), null, 0, false, null);
 
                 hasKey = false;
                 generatedKeyResultSet = pstAddNewRequest.getGeneratedKeys();
                 if (generatedKeyResultSet.next()) {
                     request.requestID = generatedKeyResultSet.getInt(1);
                     hasKey = true;
                 }
                 generatedKeyResultSet.close();
 
                 tryAgain = 0;
                 log.fine("Database query successful");
 
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
 
         if (!hasKey) {
             log.severe("Current database doesn't support java.sql.Statement.getGeneratedKeys()");
             throw new SQLFeatureNotSupportedException(
                     "Current database doesn't support "
                             + "java.sql.Statement.getGeneratedKeys()");
         }
 
 		return request;
 	}
 
 	/**
 	 * Tries to insert a new user dataset into the database. </br>SQL command:
 	 * {@value #strAddNewUser}
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
      *            Last name of the user. Parameter will be trimmed from this
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
 	 *             Thrown if the id could not received or another function is
 	 *             not supported by driver.
 	 * @throws SQLException
 	 *             Thrown if other errors occurred than a duplicate email.
 	 */
 	public UserDataset addNewUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
 			boolean isAdmin) throws SQLException {
 
 		return addNewUser(email, passwordhash, salt, firstName, lastName,
 				address, isAdmin, UserStatusEnum.needs_verification, false);
 	}
 
 	/**
 	 * Tries to insert a new user dataset into the database, but request should
 	 * have legit admin authentication (will not be checked within this method).
 	 * New user will be verified. </br>SQL command: {@value #strAddNewUser}
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
 	 *             Thrown if the id could not received or another function is
 	 *             not supported by driver.
 	 * @throws SQLException
 	 *             Thrown if other errors occurred than a duplicate email.
 	 */
 	public UserDataset addNewVerifiedUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
 			boolean isAdmin) throws SQLException {
 
 		return addNewUser(email, passwordhash, salt, firstName, lastName,
 				address, isAdmin, UserStatusEnum.verified, true);
 	}
 
     /**
      *
      * @param email email
      * @param passwordhash password hash
      * @param salt salt
      * @param firstName first name
      * @param lastName last name
      * @param address address
      * @param isAdmin isAdmin flag
      * @param status status
      * @param isVerified isVerified flag
      * @return user object
      * @throws SQLFeatureNotSupportedException
      *             Thrown if the id could not received or another function is
      *             not supported by driver.
      * @throws SQLException
      *            Thrown if other errors occurred than a duplicate email.
      *
      */
 	private UserDataset addNewUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
 			boolean isAdmin, UserStatusEnum status, boolean isVerified)
 			throws SQLException {
 
 		/*
 		 * id INT NOT NULL AUTO_INCREMENT, Email VARCHAR(255) NOT NULL UNIQUE,
 		 * Passwordhash TEXT NOT NULL, Salt TEXT NOT NULL, AdminFlag BOOL NOT
 		 * NULL DEFAULT 0,Status ENUM ('needs_verification','verified')
          * NOT NULL DEFAULT 'needs_verification', FirstName TEXT NOT NULL, LastName TEXT NOT NULL,
 		 * Address TEXT NOT NULL, RegistrationDate DATETIME NOT NULL,
 		 * VerifiedDate DATETIME DEFAULT NULL, DeleteRequestDate DATETIME
 		 * DEFAULT NULL, PRIMARY KEY (ID)
 		 */
 
         PreparedStatement pstAddNewUser = preparedStatementMap.get(SqlStatementEnum.AddNewUser);
 
 		UserDataset user = null;
 
 		email = email.trim();
 		passwordhash = passwordhash.trim();
 		salt = salt.trim();
 		firstName = firstName.trim();
 		lastName = lastName.trim();
 		address = address.trim();
 
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
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
 
                 try {
 
                     pstAddNewUser.executeUpdate();
                     // if no exception occurred, user is added, so do not try again
                     tryAgain = -1;
 
                     // catch if duplicate key error
                 } catch (SQLIntegrityConstraintViolationException ex) {
                     if (ex.getNextException() == null) {
                         return null;
                     }
                     throw ex;
                 }
 
 
                 user = new UserDataset(-1, email, passwordhash, salt, isAdmin,
                         status, firstName, lastName, address, registeredDate,
                         verifiedDate, null);
 
                 ResultSet generatedKeyResultSet;
 
                 generatedKeyResultSet = pstAddNewUser.getGeneratedKeys();
 
                 boolean hasKey = false;
                 if (generatedKeyResultSet.next()) {
                     user.userid = generatedKeyResultSet.getInt(1);
                     hasKey = true;
                 }
                 generatedKeyResultSet.close();
 
                 if (!hasKey) {
                     log.severe("Current database doesn't support java.sql.Statement.getGeneratedKeys()");
                     user = this.getUser(email);
                 }
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
 		
 		return user;
 	}
 
 	/**
 	 * Updates the Requests table row with the id given through the parameter
 	 * (<b><code>request.id</code></b>). All values within the given object will
 	 * be written into the database, so all old values within the row will be
 	 * overwritten. <b><code>request.id</code></b> has to be > 0 and must exists
 	 * within the database table. </br>SQL command:
 	 * {@value #strUpdateRequest}
 	 * 
 	 * @param request
 	 *            The request object to write into the database.
 	 * @throws SQLException
 	 *             Thrown if update fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int updateRequest(RequestDataset request) throws SQLException {
 
         PreparedStatement pstUpdateRequest = preparedStatementMap.get(SqlStatementEnum.UpdateRequest);
 
         int tryAgain = maxTries;
         int rowsAffected;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
                 boolean hasFailed = false;
                 boolean isPending = false;
 
                 if (request.status == RequestStatusEnum.pending) {
                     isPending = true;
                 }
                 if (request.status == RequestStatusEnum.failed) {
                     hasFailed = true;
                 }
 
 
                 pstUpdateRequest.setInt(1, request.userID);
                 pstUpdateRequest.setString(2, request.algorithm);
                 pstUpdateRequest.setBytes(3, request.jsonRequest);
                 pstUpdateRequest.setBytes(4, request.jsonResponse);
                 pstUpdateRequest.setBoolean(5, isPending);
                 pstUpdateRequest.setInt(6, request.cost);
                 pstUpdateRequest.setBoolean(7, request.isPaid);
                 pstUpdateRequest.setTimestamp(8, dateToTimestamp(request.requestDate));
                 pstUpdateRequest.setTimestamp(9, dateToTimestamp(request.finishedDate));
                 pstUpdateRequest.setLong(10, request.duration);
                 pstUpdateRequest.setBoolean(11, hasFailed);
                 pstUpdateRequest.setString(12, request.failDescription);
                 pstUpdateRequest.setInt(13, request.requestID);
 
                 rowsAffected = pstUpdateRequest.executeUpdate();
 
                 log.fine("Database query successful");
                 return rowsAffected;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
         return 0;
 	}
 
 	/**
 	 * Updates the Requests table row with the id given through the parameter
 	 * (<b><code>requestID</code></b>). The given parameters will overwrite the
 	 * old values in the database row. FinishedDate will be set to the current
 	 * timestamp. <b><code>requestID</code></b> has to be > 0 and must exists
 	 * within the database table. </br>SQL command:
 	 * {@value #strUpdateRequestWithComputeResult}
 	 * 
 	 * @param requestID requestID
 	 * @param jsonResponse JSON response object
 	 * @param isPending isPending flag
 	 * @param costs cost of request
 	 * @param cpuTime cpuTime
 	 * @param hasFailed hasFailed flag
 	 * @param failDescription failure description
 	 * @throws SQLException
 	 *             Thrown if update fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int updateRequestWithComputeResult(int requestID,
 			byte[] jsonResponse, boolean isPending, int costs, long cpuTime,
 			boolean hasFailed, String failDescription) throws SQLException {
 
         PreparedStatement pstUpdateRequestWithComputeResult
                 = preparedStatementMap.get(SqlStatementEnum.UpdateRequestWithComputeResult);
 
         int rowsAffected;
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 Timestamp stamp = new Timestamp(System.currentTimeMillis());
 
                 pstUpdateRequestWithComputeResult.setBytes(1, jsonResponse);
                 pstUpdateRequestWithComputeResult.setBoolean(2, isPending);
                 pstUpdateRequestWithComputeResult.setInt(3, costs);
                 pstUpdateRequestWithComputeResult.setTimestamp(4, stamp);
                 pstUpdateRequestWithComputeResult.setLong(5, cpuTime);
                 pstUpdateRequestWithComputeResult.setBoolean(6, hasFailed);
                 pstUpdateRequestWithComputeResult.setString(7, failDescription);
                 pstUpdateRequestWithComputeResult.setInt(8, requestID);
 
                 rowsAffected = pstUpdateRequestWithComputeResult.executeUpdate();
 
                 log.fine("Database query successful");
                 return rowsAffected;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
         return 0;
 	}
 
     /**
 	 * Updates the Users table row with the id given through the parameter (<b>
 	 * <code>request.id</code></b>). All values within the given object will be
 	 * written into the database, so all old values within the row will be
 	 * overwritten. <b><code>user.userid</code></b> has to be > 0 and must exists
 	 * within the database table. </br>SQL command: {@value #strUpdateUser}
 	 * 
 	 * @param user
 	 *            The user object to write into the database.
 	 * @throws SQLException
 	 *             Thrown if update fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int  updateUser(UserDataset user) throws SQLException {
 
         PreparedStatement pstUpdateUser = preparedStatementMap.get(SqlStatementEnum.UpdateUser);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstUpdateUser.setString(1, user.email);
                 pstUpdateUser.setString(2, user.passwordhash);
                 pstUpdateUser.setString(3, user.salt);
                 pstUpdateUser.setBoolean(4, user.admin);
                 pstUpdateUser.setString(5, user.status.toString());
                 pstUpdateUser.setString(6, user.firstName);
                 pstUpdateUser.setString(7, user.lastName);
                 pstUpdateUser.setString(8, user.address);
                 pstUpdateUser.setTimestamp(9, dateToTimestamp(user.registrationDate));
                 pstUpdateUser.setTimestamp(10, dateToTimestamp(user.verifiedDate));
                 pstUpdateUser.setTimestamp(11, dateToTimestamp(user.deleteRequestDate));
                 pstUpdateUser.setInt(12, user.userid);
 
                 return pstUpdateUser.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return 0;
 	}
 
 	/**
 	 * Deletes the Requests table row with the given request id. </br>SQL
 	 * command: {@value #strDeleteRequestWithRequestId}
 	 * 
 	 * @param id
 	 *            Request id
 	 * @throws SQLException
 	 *             Thrown if delete fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int deleteRequest(int id) throws SQLException {
 
         PreparedStatement pstDeleteRequestWithRequestId
                 = preparedStatementMap.get(SqlStatementEnum.DeleteRequestWithRequestId);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstDeleteRequestWithRequestId.setInt(1, id);
 
                 return pstDeleteRequestWithRequestId.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return 0;
 	}
 
 	/**
 	 * Deletes the Requests table rows with the given user id. </br>SQL command:
 	 * {@value #strDeleteRequestsOfUserWithUserId}
 	 * 
 	 * @param userId
 	 *            User id of the user, whose requests should be deleted.
 	 * @throws SQLException
 	 *             Thrown if delete fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int deleteRequestsOfUser(int userId) throws SQLException {
 
         PreparedStatement pstDeleteRequestsOfUserWithUserId
                 = preparedStatementMap.get(SqlStatementEnum.DeleteRequestsOfUserWithUserId);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstDeleteRequestsOfUserWithUserId.setInt(1, userId);
 
                 return pstDeleteRequestsOfUserWithUserId.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return 0;
 	}
 
 	/**
 	 * Deletes the Users table row with the given user id. Depending on the
 	 * database configuration(for example strict mode) maybe the according
 	 * requests could be deleted too because of the FOREIGN KEY UserId within
 	 * the Requests table. </br>SQL command:
 	 * {@value #strDeleteUserWithUserId}
 	 * 
 	 * @param userId
 	 *            User id
 	 * @throws SQLException
 	 *             Thrown if delete fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int deleteUser(int userId) throws SQLException {
 
         PreparedStatement pstDeleteUserWithUserId
                 = preparedStatementMap.get(SqlStatementEnum.DeleteUserWithUserId);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstDeleteUserWithUserId.setInt(1, userId);
 
                 return pstDeleteUserWithUserId.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return 0;
 	}
 
 	/**
 	 * Deletes the Users table row with the given user email. Depending on the
 	 * database configuration(for example strict mode) maybe the according
 	 * requests could be deleted too because of the FOREIGN KEY UserId within
 	 * the Requests table. </br>SQL command: {@value #strDeleteUserWithEmail}
 	 * 
 	 * @param email The email of the user
 	 * @throws SQLException
 	 *             Thrown if delete fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int deleteUser(String email) throws SQLException {
 
         PreparedStatement pstDeleteUserWithEmail
                 = preparedStatementMap.get(SqlStatementEnum.DeleteUserWithEmail);
 
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstDeleteUserWithEmail.setString(1, email);
 
                 return pstDeleteUserWithEmail.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return 0;
 
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table with regard to
 	 * the limit and offset constraints. If no requests are found with the given
 	 * constraints or the table is empty, an empty list will be returned.
 	 * </br>SQL command: {@value #strGetAllRequestsWithLimitOffset}
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
 
         PreparedStatement pstGetAllRequestsWithLimitOffset
                 = preparedStatementMap.get(SqlStatementEnum.GetAllRequestsWithLimitOffset);
 
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstGetAllRequestsWithLimitOffset.setInt(1, limit);
                 pstGetAllRequestsWithLimitOffset.setInt(2, offset);
 
                 ResultSet resultSet = pstGetAllRequestsWithLimitOffset.executeQuery();
                 ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new RequestDataset(resultSet.getInt(1), resultSet
                             .getInt(2), resultSet.getString(3), resultSet.getBytes(4),
                             resultSet.getBytes(5), resultSet.getBoolean(6), resultSet
                             .getInt(7), resultSet.getBoolean(8),
                             timestampToDate(resultSet.getTimestamp(9)),
                             timestampToDate(resultSet.getTimestamp(10)), resultSet
                             .getLong(11), resultSet.getBoolean(12), resultSet
                             .getString(13)));
                 }
                 resultSet.close();
 
                 return list;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return null;
 
     }
 
 
     /**
      * Gets a list with all requests within the Requests table with regard to
      * the limit and offset constraints. If no requests are found with the given
      * constraints or the table is empty, an empty list will be returned.
      * </br>SQL command: {@value #strGetAllRequestsWithLimitOffset}
      *
      * @param limit
      *            How many rows should maximal selected.
      * @param offset
      *            How many rows should be skimmed.
      * @param sendJson Should be false, if you do not want to send JSON objects, else it should be true.
      * @return A list with all selected requests. If no requests selected, the
      *         list is empty, but not null.
      * @throws SQLException
      *             Thrown if select fails.
      */
     public List<RequestDataset> getAllRequests(int limit, int offset, boolean sendJson)
             throws SQLException {
 
         if (sendJson) {
             return getAllRequests(limit, offset);
         }
 
         PreparedStatement pstGetAllRequests
                 = preparedStatementMap.get(SqlStatementEnum.GetAllRequestsNoJsonWithLimitOffset);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstGetAllRequests.setInt(1, limit);
                 pstGetAllRequests.setInt(2, offset);
 
                 ResultSet resultSet = pstGetAllRequests.executeQuery();
                 ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new RequestDataset(
                             resultSet.getInt(1),
                             resultSet.getInt(2),
                             resultSet.getString(3),
                             null,
                             null,
                             resultSet.getBoolean(4),
                             resultSet.getInt(5),
                             resultSet.getBoolean(6),
                             timestampToDate(resultSet.getTimestamp(7)),
                             timestampToDate(resultSet.getTimestamp(8)),
                             resultSet.getLong(9),
                             resultSet.getBoolean(10),
                             resultSet.getString(11)));
                 }
                 resultSet.close();
 
                 return list;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return null;
     }
 
 	/**
 	 * Gets a request object of the Requests table with the given request id.
 	 * 
 	 * </br>SQL command: {@value #strGetRequestWithRequestId}
 	 * 
 	 * @param id
 	 *            Request id
 	 * @return A request object if the request with the id exists, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public RequestDataset getRequest(int id) throws SQLException {
 
         PreparedStatement pstGetRequestWithRequestId
                 = preparedStatementMap.get(SqlStatementEnum.GetRequestWithRequestId);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstGetRequestWithRequestId.setInt(1, id);
                 ResultSet resultSet = pstGetRequestWithRequestId.executeQuery();
                 RequestDataset request = null;
 
                 while (resultSet.next()) {
                     request = new RequestDataset(resultSet.getInt(1),
                             resultSet.getInt(2), resultSet.getString(3),
                             resultSet.getBytes(4), resultSet.getBytes(5),
                             resultSet.getBoolean(6), resultSet.getInt(7),
                             resultSet.getBoolean(8),
                             timestampToDate(resultSet.getTimestamp(9)),
                             timestampToDate(resultSet.getTimestamp(10)),
                             resultSet.getLong(11), resultSet.getBoolean(12),
                             resultSet.getString(13));
                 }
                 resultSet.close();
 
                 return request;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return null;
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table which have the
 	 * given user id with regard to the limit and offset constraints. If no
 	 * requests are found with the given user id and given constraints or the
 	 * table is empty, an empty list will be returned. </br>SQL command:
 	 * {@value #strGetRequestsWithUserIdLimitOffset}
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
 
         PreparedStatement pstGetRequestsWithUserIdLimitOffset
                 = preparedStatementMap.get(SqlStatementEnum.GetRequestsWithUserIdLimitOffset);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstGetRequestsWithUserIdLimitOffset.setInt(1, userId);
                 pstGetRequestsWithUserIdLimitOffset.setInt(2, limit);
                 pstGetRequestsWithUserIdLimitOffset.setInt(3, offset);
 
                 ResultSet resultSet = pstGetRequestsWithUserIdLimitOffset
                         .executeQuery();
                 ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new RequestDataset(resultSet.getInt(1), resultSet
                             .getInt(2), resultSet.getString(3), resultSet.getBytes(4),
                             resultSet.getBytes(5), resultSet.getBoolean(6), resultSet
                             .getInt(7), resultSet.getBoolean(8),
                             timestampToDate(resultSet.getTimestamp(9)),
                             timestampToDate(resultSet.getTimestamp(10)), resultSet
                             .getLong(11), resultSet.getBoolean(12), resultSet
                             .getString(13)));
                 }
                 resultSet.close();
 
                 return list;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return null;
 	}
 
 
     /**
      * Gets a list with all requests within the Requests table which have the
      * given user id with regard to the limit and offset constraints. If no
      * requests are found with the given user id and given constraints or the
      * table is empty, an empty list will be returned. </br>SQL command:
      * {@value #strGetRequestsWithUserIdLimitOffset}
      *
      * @param userId
      *            User id
      * @param limit
      *            How many rows should maximal selected.
      * @param offset
      *            How many rows should be skimmed.
      * @param sendJson Should be false, if you do not want to send JSON objects, else it should be true.
      * @return A list with all selected requests. If no requests selected, the
      *         list is empty, but not null.
      * @throws SQLException
      *             Thrown if select fails.
      */
     public List<RequestDataset> getRequests(int userId, int limit, int offset, boolean sendJson)
             throws SQLException {
 
         if (sendJson) {
             return getRequests(userId, limit, offset);
         }
 
         PreparedStatement pstGetRequests
                 = preparedStatementMap.get(SqlStatementEnum.GetRequestsNoJsonWithUserIdLimitOffset);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstGetRequests.setInt(1, userId);
                 pstGetRequests.setInt(2, limit);
                 pstGetRequests.setInt(3, offset);
 
                 ResultSet resultSet = pstGetRequests
                         .executeQuery();
                 ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new RequestDataset(
                             resultSet.getInt(1),
                             resultSet.getInt(2),
                             resultSet.getString(3),
                             null,
                             null,
                             resultSet.getBoolean(4),
                             resultSet.getInt(5),
                             resultSet.getBoolean(6),
                             timestampToDate(resultSet.getTimestamp(7)),
                             timestampToDate(resultSet.getTimestamp(8)),
                             resultSet.getLong(9),
                             resultSet.getBoolean(10),
                             resultSet.getString(11)));
                 }
                 resultSet.close();
 
                 return list;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
         return null;
     }
 
 
 	/**
 	 * Gets a list with all users within the Users table. If the table is empty,
 	 * an empty list will be returned. </br>SQL command:
 	 * {@value #strGetAllUsers}
 	 * 
 	 * @return A list with all selected users. If no users selected, the list is
 	 *         empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<UserDataset> getAllUsers() throws SQLException {
 
         PreparedStatement pstGetAllUsers = preparedStatementMap.get(SqlStatementEnum.GetAllUsers);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 ResultSet resultSet = pstGetAllUsers.executeQuery();
                 ArrayList<UserDataset> list = new ArrayList<UserDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new UserDataset(resultSet.getInt(1), resultSet
                             .getString(2), resultSet.getString(3), resultSet
                             .getString(4), resultSet.getBoolean(5), UserStatusEnum
                             .valueOf(resultSet.getString(6)), resultSet.getString(7),
                             resultSet.getString(8), resultSet.getString(9),
                             timestampToDate(resultSet.getTimestamp(10)),
                             timestampToDate(resultSet.getTimestamp(11)),
                             timestampToDate(resultSet.getTimestamp(12))));
                 }
                 resultSet.close();
 
                 return list;
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
         return null;
 	}
 
 	/**
 	 * Gets a list with all users within the Users table with regard to the
 	 * limit and offset constraints. If no users are found with the given
 	 * constraints or the table is empty, an empty list will be returned.
 	 * </br>SQL command: {@value #strGetAllUsersWithLimitOffset}
 	 * 
 	 * @param limit
 	 *            How many rows should maximal selected.
 	 * @param offset
 	 *            How many rows should be skimmed.
 	 * @return A list with all selected users. If no users selected, the list is
 	 *         empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<UserDataset> getAllUsers(int limit, int offset)
 			throws SQLException {
 
         PreparedStatement pstGetAllUsersWithLimitOffset
                 = preparedStatementMap.get(SqlStatementEnum.GetAllUsersWithLimitOffset);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstGetAllUsersWithLimitOffset.setInt(1, limit);
                 pstGetAllUsersWithLimitOffset.setInt(2, offset);
 
                 ResultSet resultSet = pstGetAllUsersWithLimitOffset.executeQuery();
                 ArrayList<UserDataset> list = new ArrayList<UserDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new UserDataset(resultSet.getInt(1), resultSet
                             .getString(2), resultSet.getString(3), resultSet
                             .getString(4), resultSet.getBoolean(5), UserStatusEnum
                             .valueOf(resultSet.getString(6)), resultSet.getString(7),
                             resultSet.getString(8), resultSet.getString(9),
                             timestampToDate(resultSet.getTimestamp(10)),
                             timestampToDate(resultSet.getTimestamp(11)),
                             timestampToDate(resultSet.getTimestamp(12))));
                 }
                 resultSet.close();
 
                 return list;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return null;
 	}
 
 	/**
 	 * Gets a user object from the Users table with the given email. </br>SQL
 	 * command: {@value #strGetUserWithEmail}
 	 * 
 	 * @param email The email of the user
 	 * @return The user object, if the user is found, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public UserDataset getUser(String email) throws SQLException {
 
         PreparedStatement pstGetUserWithEmail = preparedStatementMap.get(SqlStatementEnum.GetUserWithEmail);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 UserDataset user = null;
 
                 pstGetUserWithEmail.setString(1, email);
                 ResultSet resultSet = pstGetUserWithEmail.executeQuery();
 
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
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return null;
 	}
 
 	/**
 	 * Gets a user object from the Users table with the given user id. </br>SQL
 	 * command: {@value #strGetUserWithId}
 	 * 
 	 * @param id
 	 *            User id
 	 * @return The user object, if the user is found, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public UserDataset getUser(int id) throws SQLException {
 
         PreparedStatement pstGetUserWithId = preparedStatementMap.get(SqlStatementEnum.GetUserWithId);
 
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 UserDataset user = null;
 
                 pstGetUserWithId.setInt(1, id);
                 ResultSet resultSet = pstGetUserWithId.executeQuery();
 
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
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return null;
 	}
 
 
     /**
      * Returns the number of data sets within the Requests table
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfRequests() throws SQLException {
 
         PreparedStatement pstCountAllRequests = preparedStatementMap.get(SqlStatementEnum.CountAllRequests);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 ResultSet resultSet = pstCountAllRequests.executeQuery();
 
                 if (resultSet.next()) {
                     int count = resultSet.getInt(1);
                     resultSet.close();
                     return count;
                 }
 
                 log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
                 resultSet.close();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
         return 0;
     }
 
     /**
      * Returns the number of data sets with a certain UserID within the Requests table    
      * @param userId The UserID from the user for which you want to get the number of data sets
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfRequestsWithUserId(int userId) throws SQLException {
 
         PreparedStatement pstCountRequestsWithUserId
                 = preparedStatementMap.get(SqlStatementEnum.CountRequestsWithUserId);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstCountRequestsWithUserId.setInt(1, userId);
                 ResultSet resultSet = pstCountRequestsWithUserId.executeQuery();
 
                 if (resultSet.next()) {
                     int count = resultSet.getInt(1);
                     resultSet.close();
                     return count;
                 }
 
                 log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
                 resultSet.close();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return 0;
     }
 
     /**
      * Returns the number of data sets within the Users table
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfUsers() throws SQLException {
 
         PreparedStatement pstCountAllUsers
                 = preparedStatementMap.get(SqlStatementEnum.CountAllUsers);
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 ResultSet resultSet = pstCountAllUsers.executeQuery();
 
                 if (resultSet.next()) {
                     int count = resultSet.getInt(1);
                     resultSet.close();
                     return count;
                 }
 
                 log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
                 resultSet.close();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             }
 
         }
 
         return 0;
     }
 
 
     /**
      * Closes the database connection. Exceptions will be caught. 
      */
     public void close() {
         for (SqlStatementEnum sqlStatementEnum : preparedStatementMap.keySet()) {
             try {
                 preparedStatementMap.get(sqlStatementEnum).close();
             } catch (SQLException ignored) {
             }
         }
         try {
             con.close();
         } catch (SQLException ignored) {
         }
     }
 
     /**
      *
      * @param tryAgain the old tryAgain value, specifies how many tries are left before this exception
      * @param exception Needs to be an Exception, not SQLException, because null pointer can be thrown
      * @return the new tryAgain value, specifies how many tries are left after this exception
      * @throws SQLException Thrown if could not be reestablished with allowed number of tries
      */
     private int processTryAgainExceptionHandling(int tryAgain, Exception exception) throws SQLException {
         if (tryAgain > 0) {
             log.log(Level.WARNING, "Database exception occurred after " + (maxTries - tryAgain) + ". attempt, " +
                     "thread will now reconnect database and send again the sql statement " + 
                    "\n --- (Exception: " + exception.getCause().toString() + " --- " +
                    exception.getMessage() + ")");
             
             this.close();
             try {
                 init();
             } catch (SQLException e) {
                 tryAgain--;
                 return processTryAgainExceptionHandling(tryAgain, e);
             }
         } else {
             log.log(Level.SEVERE, "Database exception occurred after " + (maxTries - tryAgain) + ". attempt, " +
                     "thread will now give up executing the statement", exception);
             if (exception instanceof SQLException) {
                 throw (SQLException) exception;
             } else {
                throw new SQLException("Exception within database method: " + exception.toString());
             }
             
         }
         return tryAgain;
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
 
 }
