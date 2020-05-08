 package db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 /**
  * @author Amilie
  */
 public class Database {
     //JDBC driver name
    private static final String DRIVER = "org.postgresql.Driver";
     //database connection string
    private static final String DB_URL = "jdbc:mysql://localhost:8084/MovieList/";
     //database username
     private static final String DB_USER = "root";
     //database password
     private static final String DB_PASS = "root";
 
     //load the JDBC driver
     static {
         try {
             Class.forName(DRIVER);
         }
         catch (ClassNotFoundException exc) { }
     }
 
     //open a connection to the database
     public static Connection open() throws SQLException {
         return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
     }
 
 }
