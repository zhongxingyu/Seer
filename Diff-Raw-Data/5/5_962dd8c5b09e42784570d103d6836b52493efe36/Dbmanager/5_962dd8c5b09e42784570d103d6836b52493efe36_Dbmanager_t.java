 package connectivity;
 
 import java.sql.*;
import roosterprogramma.RoosterProgramma;
 
 /**
  *
  * @author UDP
  */
 public class Dbmanager {
     public static final String JDBC_EXCEPTION = "JDBC Exception: ";
     public static final String SQL_EXCEPTION = "SQL Exception: ";
 
     public Connection connection;
 
     /**
      * Open database connection
      */
     public void openConnection() {
         try {
             Class.forName("com.mysql.jdbc.Driver");
 
             String url = "jdbc:mysql://localhost/roosterprogramma";
             String user = "root", pass = "mangos";
 
             /** Open connection */
             connection = DriverManager.getConnection(url, user, pass);
         } catch (ClassNotFoundException e) {
             System.err.println(JDBC_EXCEPTION + e);
            RoosterProgramma.getInstance().shutdown();
         } catch (java.sql.SQLException e) {
             System.err.println(SQL_EXCEPTION + e);
            RoosterProgramma.getInstance().shutdown();
         }
     }
 
     /**
      * Close database connection
      */
     public void closeConnection() {
         try {
             connection.close();
         } catch (Exception e) {
             System.err.println(e.getMessage());
         }
     }
 
     /**
      * Executes a query without result.
      * @param query
      */
     public void executeQuery(String query) {
         try {
             Statement statement = connection.createStatement();
             statement.executeQuery(query);
         } catch (java.sql.SQLException e) {
             System.err.println(SQL_EXCEPTION + e);
         }
     }
 
     /**
      * Executes a query with result.
      * @param query
      * @return 
      */
     public ResultSet doQuery(String query) {
         ResultSet result = null;
         try {
             Statement statement = connection.createStatement();
             result = statement.executeQuery(query);
         } catch (java.sql.SQLException e) {
             System.err.println(SQL_EXCEPTION + e);
         }
         return result;
     }
     
     /**
      * Executes a query with result.
      * @param query 
      * @return
      */
     public ResultSet insertQuery(String query) {
         ResultSet result = null;
         try {
             Statement statement = connection.createStatement();
             statement.executeUpdate(query);
             result = statement.getGeneratedKeys();
         } catch (java.sql.SQLException e) {
             System.err.println(SQL_EXCEPTION + e);
         }
         return result;
     }
 }
