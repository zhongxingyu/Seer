 package de.hswt.hrm.common.database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import static com.google.common.base.Strings.*;
 
 import de.hswt.hrm.common.Config;
 import de.hswt.hrm.common.Config.Keys;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 
 /**
  * Class that is used to get a database connection. This may be changed to injection later on.
  */
 public final class DatabaseFactory {
 	
 	private DatabaseFactory() { }
 	
     /**
      * Returns a connection object for the database.
      * 
      * @return Connection object for the database.
      * @throws DatabaseException If connection could not be created.
      */
     public static Connection getConnection() throws DatabaseException {
         // load mariadb driver
         try {
             Class.forName("org.mariadb.jdbc.Driver");
         }
         catch (ClassNotFoundException e) {
             throw new DatabaseException("Database driver not found.", e);
         }
 
         Config cfg = Config.getInstance();
         final String host = cfg.getProperty(Keys.DB_HOST, "jdbc:mysql://localhost");
         final String username = cfg.getProperty(Keys.DB_USER, "root");
         final String password = cfg.getProperty(Keys.DB_PASSWORD, "70b145pl4ch7");
         final String database = cfg.getProperty(Keys.DB_NAME, "hrm");
         
         // Build connection String
         String conStr = host;
         if (!isNullOrEmpty(database)) {
             conStr += conStr.endsWith("/") ? database : "/" + database; 
         }
         
         try {
            return DriverManager.getConnection(conStr, username, password);
         }
         catch (SQLException e) {
             // TODO maybe add specific information about the error
             throw new DatabaseException(e);
         }
 
     }
 }
