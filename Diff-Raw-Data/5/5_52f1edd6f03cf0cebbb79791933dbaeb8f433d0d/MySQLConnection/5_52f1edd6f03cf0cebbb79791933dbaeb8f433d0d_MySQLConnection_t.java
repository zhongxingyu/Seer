 package Util;
 
 import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * MySQL connector.
  * Singleton providing database connection.
  * 
  * @author Niklas Hansen
  * @version 0.1
  */
 public class MySQLConnection {
 	
 	private Connection conn;
 	
 	// Singleton instance
 	private static MySQLConnection Instance;
 	
 	/**
 	 * Constructor for object of class MySQLConnection.
 	 * Initializes the database-connection.
 	 */
 	protected MySQLConnection () {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.conn = DriverManager.getConnection
 				("jdbc:mysql://mysql.itu.dk/rent_a_car", "rac", "vand22kanon");
 		} catch (SQLException e) {
 			Logger.write("Couldn't open database connection: " + e.getMessage());
 		} catch (ClassNotFoundException e) {
 			Logger.write("Couldn't load database driver: " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Method for getting the MySQLConnection instance. If an instance 
 	 * isn't created yet, an instance will be created and returned.
 	 * 
 	 * @return The MySQLConnection instance
 	 */
 	static public MySQLConnection getInstance() {
 		if (Instance == null) {
 			Instance = new MySQLConnection();
 		}
 		
 		return Instance;
 	}
 	
 	/**
 	 * Send a query to the database. The ResultSet containing the result 
 	 * of the query will be returned after execution of the query.
 	 * 
 	 * @param query The SQL query string.
 	 * @return true on success; false on failure
 	 * @see fetch()
 	 */
 	public ResultSet query (String query) {
 		try {
 			Statement stmt = conn.createStatement();
			if (query.substring(0, query.indexOf(' ')).toLowerCase().equals("select"))
				return stmt.executeQuery(query);
			stmt.execute(query, Statement.RETURN_GENERATED_KEYS);
			return stmt.getGeneratedKeys();
 		} catch (SQLException e) {
 			Logger.write("SQL query failed: " + e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Close the database connection, and remove the MySQLConnection instance.
 	 */
 	public void close () {
 		try {
 			if (conn != null) {
 				conn.close();
 			}
 		} catch (SQLException e) {
 			Logger.write("Couldn't close database connection: " + e.getMessage());
 		}
 		
 		Instance = null;
 	}
 	
 	/**
 	 * Destructor.
 	 * Destructor for object of class MySQLConnection. Will only be called 
 	 * by the garbage-collector, and therefore isn't garuanteed to be called 
 	 * at all.
 	 */
 	protected void finalize () {
 		close();
 	}
 }
