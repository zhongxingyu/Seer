 package groupone;
 import java.sql.*;
 
 public class DBConnection {
 
 	/** hold a pointer to the database connection. */
 	private Connection con;
 	private final String DB_DRIVER = "com.mysql.jdbc.Driver";
//	private final String DB_URL = "jdbc:mysql://75.52.121.36:3306/groupone";
	private final String DB_URL = "jdbc:mysql://192.168.1.135:3306/groupone";
 	private final String USER_ID = "sjsu";
 	private final String PASSWORD = "12345";
 
 	public DBConnection() {
 		try {
 			Class.forName(DB_DRIVER);
 			con = DriverManager.getConnection(DB_URL, USER_ID, PASSWORD);
 
 			System.out.println("Connected to DB!");
 
 		} catch (SQLException ex) {
 			System.err.println(ex.getMessage());
 		} catch (ClassNotFoundException ex) {
 			System.err.println(ex.getMessage());
 		}
 
 	}
 
 	/** get the database connection object. */
 	public Connection getDBConnection() {
 		return this.con;
 	}
 	
     /** disconnect from the database. */
 	public void disconnectFromDB() {
 		try {
 			con.close();
 			System.out.println("Disconnected from DB!");
 
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());
 		}
 
 	}
 }
