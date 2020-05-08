 /**
  * 
  */
 package database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 
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
 	
 	private final String addNewRequestString = "INSERT INTO Requests" +
 			" (UserID, JSONRequest, RequestDate) VALUES(?)";
 	
 	private final String addNewUserString = "INSERT INTO Users" +
 			" (Email, Passwordhash, Salt, FirstName, LastName, Address," +
 			" AdminFlag, RegistrationDate) VALUES(?)";
 	
 	private final String getAllRequestsString = "SELECT id, UserID," +
 			" JSONRequest, JSONResponse, PendingFlag, Costs, PaidFlag," +
 			" RequestDate, FinishedDate, CPUTime, FailedFlag," +
 			" FailedDescription FROM Requests";
 	
 	private final String getAllUsersString = "SELECT id, Email, Passwordhash," +
 			" Salt, AdminFlag, Status, FirstName, LastName, Adress," +
 			" RegistrationDate, VerifiedDate, DeleteRequestDate FROM Users";
 	
 	/**
 	 * 
 	 * @param url Example: "jdbc:mysql://localhost:3306/"
 	 * @param dbName Example: "tourenplaner"
 	 * @param userName User name of the database user account
 	 * @param password To the user according password
 	 * @throws SQLException
 	 */
 	public DatabaseManager(String url, String dbName, String userName, 
 			String password) throws SQLException {
 		
 		//TODO use config package
		url = "jdbc:mysql://localhost:3306/";
		dbName = "tourenplaner";
		userName = "tnpuser";
		password = "H4milt0n";
 		
 		con = DriverManager.getConnection(url + dbName, userName, password);
 		
 		pstAddNewRequest = con.prepareStatement(addNewRequestString);
 		pstAddNewUser = con.prepareStatement(addNewUserString);
 		pstGetAllRequests = con.prepareStatement(getAllRequestsString);
 		pstGetAllUsers = con.prepareStatement(getAllUsersString);
 		
 	}
 	
 	
 	public void addNewRequest(int userID, byte[] jsonRequest) 
 			throws SQLException  {
 		pstAddNewRequest.setInt(1, userID);
 		pstAddNewRequest.setBytes(2, jsonRequest);
 		pstAddNewRequest.setTimestamp(3, 
 				new Timestamp(System.currentTimeMillis()));
 		
 		pstAddNewRequest.executeUpdate();
 		
 	}
 	
 	public void addNewUser(String email, String passwordhash, String salt, 
 			String firstName, String lastName, String address, boolean isAdmin) 
 			throws SQLException  {
 		pstAddNewUser.setString(1, email);
 		pstAddNewUser.setString(2, passwordhash);
 		pstAddNewUser.setString(3, salt);
 		pstAddNewUser.setString(4, firstName);
 		pstAddNewUser.setString(5, lastName);
 		pstAddNewUser.setString(6, address);
 		pstAddNewUser.setBoolean(7, isAdmin);
 		pstAddNewUser.setTimestamp(8, 
 				new Timestamp(System.currentTimeMillis()));
 		
 		pstAddNewUser.executeUpdate();
 	}
 	
 	public RequestsDBRow[] getAllRequests() throws SQLException {
 		ResultSet resultSet = pstGetAllRequests.executeQuery();
 		ArrayList<RequestsDBRow> list = new ArrayList<RequestsDBRow>();
 		
 		while(resultSet.next()) {
 			list.add(new RequestsDBRow(
 					resultSet.getInt(1), 
 					resultSet.getInt(2), 
 					resultSet.getBytes(3), 
 					resultSet.getBytes(4),
 					resultSet.getBoolean(5), 
 					resultSet.getInt(6), 
 					resultSet.getBoolean(7), 
 					new Date(resultSet.getTimestamp(8).getTime()), 
 					new Date(resultSet.getTimestamp(9).getTime()), 
 					resultSet.getLong(10), 
 					resultSet.getBoolean(11), 
 					resultSet.getString(12)
 					));
 		}
 		
 		return list.toArray(new RequestsDBRow[0]);
 	}
 	
 	
 	public UsersDBRow[] getAllUsers() throws SQLException {
 		ResultSet resultSet = pstGetAllUsers.executeQuery();
 		ArrayList<UsersDBRow> list = new ArrayList<UsersDBRow>();
 		
 		while(resultSet.next()) {
 			list.add(new UsersDBRow(
 					resultSet.getInt(1), 
 					resultSet.getString(2), 
 					resultSet.getString(3), 
 					resultSet.getString(4),
 					resultSet.getBoolean(5), 
 					UserStatusEnum.valueOf(resultSet.getString(6)), 
 					resultSet.getString(7), 
 					resultSet.getString(8), 
 					resultSet.getString(9), 
 					new Date(resultSet.getTimestamp(10).getTime()), 
 					new Date(resultSet.getTimestamp(11).getTime()), 
 					new Date(resultSet.getTimestamp(12).getTime())
 					));
 		}
 		
 		return list.toArray(new UsersDBRow[0]);
 	}
 
 }
