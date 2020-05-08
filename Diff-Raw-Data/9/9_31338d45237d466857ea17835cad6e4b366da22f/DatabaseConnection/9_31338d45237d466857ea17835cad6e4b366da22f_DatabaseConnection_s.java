 package arcade.database;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.FileHandler;
 import java.util.logging.Logger;
 
 
 /**
  * Establishes connection to the database
  * @author Natalia Carvalho
  */
 public class DatabaseConnection {
 
     private static final String URL = "jdbc:postgresql://cgi.cs.duke.edu/nrc10";
     private static final String USER = "nrc10";
     private static final String PASSWORD = "aUsg5xj2f";
     private Connection myConnection;
     private PreparedStatement myPreparedStatement; 
     private ResultSet myResultSet;
     private FileHandler myDatabaseErrorLog;
     private Logger myLogger;
 
 
     /**
      * Constructor for DatabaseConnection that establishes connection to database
      */
     public DatabaseConnection() {
         establishConnectionToDatabase();
         try {
             myDatabaseErrorLog = new FileHandler(System.getProperty("user.dir") + 
                    "/src/arcade/resources/ErrorLog.log");
             myLogger = Logger.getLogger(Table.class.getName());
             myLogger.addHandler(myDatabaseErrorLog);
         }
         catch (IOException e) {
             System.err.println("File not found");
         } 
     }
     
     private void establishConnectionToDatabase() {
         try {
             Class.forName("org.postgresql.Driver");
         }
         catch (ClassNotFoundException e) {
             logError("Class not found for Database in DatabaseConnection.java @Line 46");
         }
 
         myConnection = null;
         try {
             myConnection = DriverManager.getConnection(URL, USER, PASSWORD);
         }
         catch (SQLException e) {
             logError("SQL Error connection to database in DatabaseConnection.java@Line 54");
         }
         myResultSet = null;
         myPreparedStatement = null;
     }
     
     /**
      * Closes Connection, ResultSet, and PreparedStatements once done with database
      */
     public void closeConnection() {
         try {
             if (myPreparedStatement != null) {
                 myPreparedStatement.close();
             }
             if (myResultSet != null) {
                 myResultSet.close();
             }
             if (myConnection != null) {
                 myConnection.close();
             }
         }
         catch (SQLException e) {
             logError("Error closing connection in DatabaseConnection.java @ Line 68");
         }
     }
        
     /**
      * Returns established connection
      */
     public Connection getConnection() {
         return myConnection;
     }
     
     /**
      * Returns established ResultSet
      */
     public ResultSet getResultSet() {
         return myResultSet;
     }
     
     /**
      * Returns established prepared statement
      */
     public PreparedStatement getPreparedStatement() {
         return myPreparedStatement;
     }
     /**
      * Logs an error in the logger file
      * @param errorToBeLogged is error that's logged
      */
     public void logError(String errorToBeLogged) {
         myLogger.info(errorToBeLogged);
     }
 
 }
