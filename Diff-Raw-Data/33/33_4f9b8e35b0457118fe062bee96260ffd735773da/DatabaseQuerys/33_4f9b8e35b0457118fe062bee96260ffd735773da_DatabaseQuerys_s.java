 package database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import objects.WillyTask;
 
 import com.sun.org.apache.bcel.internal.Constants;
 
 import core.GlobalConstants;
 
 public class DatabaseQuerys {
 
 
 	/**
 	 * Establishes connection to database TODO make this happen in a separate
 	 * thread -
 	 */
 	public static boolean testConnection(String username, String password) {
 
 		try {
 			Statement stmt;
 			
 
 			// Register the JDBC driver for MySQL.
 			Class.forName(GlobalConstants.DB_MSQLREGISTRY);
 
 			// Define URL of database server for
 			// database named mysql on the localhost
 			// with the default port number 3306.
 			String url = GlobalConstants.DB_URL;
 
 			// Get a connection to the database for a
 			// user named root with a blank password.
 			// This user is the default administrator
 			// having full privileges to do anything.
 			Connection con = DriverManager.getConnection(url,
 					username, password);
 			
 			
 
 			// Display URL and connection information
 			System.out.println("URL: " + url);
 			System.out.println("Connection established successfully: " + con);
 
 			// Do nothing in this test app - just check connection
 
 			// Tidy up
 			System.out.println("Closing connection: " + con);
 			con.close();
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 	/***
 	 * Gets the contents of the specified table. Choose on of the final strings
 	 * in the {@link GlobalConstants} class as the table name
 	 * 
 	 * @param tableName
 	 * @return a results set object
 	 */
 	public synchronized static ObservableList<WillyTask> pullTaskTable() {
 		ResultSet tableContents = null;
 		ObservableList<WillyTask> data = null;
 		try {
 			
 		
 			Statement stmt;
 
 			// Register the JDBC driver for MySQL.
 			Class.forName(GlobalConstants.DB_MSQLREGISTRY);
 
 			// Define URL of database server for
 			// database named mysql on the localhost
 			// with the default port number 3306.
 			String url = GlobalConstants.DB_URL;
 
 			// Get a connection to the database for a
 			// user named root with a blank password.
 			// This user is the default administrator
 			// having full privileges to do anything.
 			Connection con = DriverManager.getConnection(url,
 					GlobalConstants.DB_USERNAME, HandlePasswords.getPassword());
 
 			// Display URL and connection information
 			System.out.println("URL: " + url);
 			System.out.println("Connection established successfully: " + con);
 
 			// try and get the table
 			stmt = con.createStatement();
 			String query = "select * from " + GlobalConstants.TABLE_NAME_TASK + " ;";
 			tableContents = stmt.executeQuery(query);
 			
 			// put the database results object in an observable list
 			data = FXCollections.observableArrayList();
 
 			try {
 				while (tableContents.next()) {
 					// add a new task from the result object
					data.add(new WillyTask(tableContents
							.getString(GlobalConstants.COL_TASKNAME), tableContents
							.getInt(GlobalConstants.COL_NUM_SPENT), tableContents
							.getInt(GlobalConstants.COL_DATE_CREATED)));
 
 				}
 			} catch (SQLException e) {
 
 				e.printStackTrace();
 			}
 
 			// Tidy up
 			System.out.println("Closing connection: " + con);
 			con.close();
 		} catch (Exception e) {
 			return null;
 		}
 		return data;
 	}
 	
 	
 	public synchronized static boolean updateTask(WillyTask task)
 	{
 		try {
 			Statement stmt;
 			
 
 			// Register the JDBC driver for MySQL.
 			Class.forName(GlobalConstants.DB_MSQLREGISTRY);
 
 			// Define URL of database server for
 			// database named mysql on the localhost
 			// with the default port number 3306.
 			String url = GlobalConstants.DB_URL;
 
 			// Get a connection to the database for a
 			// user named root with a blank password.
 			// This user is the default administrator
 			// having full privileges to do anything.
 			Connection con = DriverManager.getConnection(url,
 					GlobalConstants.DB_USERNAME, HandlePasswords.getPassword());
 			
 			
 
 			// Display URL and connection information
 			System.out.println("URL: " + url);
 			System.out.println("Connection established successfully: " + con);
 
 			
 			
 //			This is how the sql should appear
 //			UPDATE taskTable
 //			SET taskName='fury wank', numberOfPomodorosSpent='2'
 //			WHERE taskName='fury wank';
 //			
 			// build the sql update statement
 			String update = "UPDATE "+ GlobalConstants.TABLE_NAME_TASK;
 			String set = " SET taskName = '"+ task.taskNameProperty().get()+"', numberOfPomodorosSpent = "+String.valueOf(task.completedPomsProperty().get());
			String where = " WHERE taskName = "+GlobalConstants.TABLE_NAME_TASK+";";
 			String updateStatement = update+set+where;
 			
 			// send the statement to the db
 			stmt = con.createStatement();
			stmt.executeQuery(updateStatement);
 			
 			// Tidy up
 			System.out.println("Closing connection: " + con);
 			con.close();
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 }
