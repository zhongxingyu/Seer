 package utilities;
 
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Connection;
 
import edu.gatech.sqltutor.Utils;

 
 public class JDBC_PostgreSQL_Connection extends JDBC_Abstract_Connection {
 	protected static final String DB_CONNECTION_STRING = "jdbc:postgresql://localhost/";
 	protected static final String DB_DRIVER = "org.postgresql.Driver";
 	
 	/*
 	 * 
 	 */
 	protected Connection getConnection(String databaseName) {
 		Connection connection = null;
 		try {
 			Class.forName(DB_DRIVER);
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.getMessage());
 		}
 		try {
 			connection = DriverManager.getConnection(
                             DB_CONNECTION_STRING + databaseName, DB_MANAGER_USERNAME ,DB_PASSWORD);
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 		return connection;
 	}
 	
 	/*
 	 * 
 	 */
 	protected Connection getConnection(String databaseName, String databaseUsername) {
 		Connection connection = null;
 		try {
 			Class.forName(DB_DRIVER);
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.getMessage());
 		}
 		try {
 			connection = DriverManager.getConnection(
                             DB_CONNECTION_STRING + databaseName, databaseUsername, DB_PASSWORD);
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 		return connection;
 	}
 } 
