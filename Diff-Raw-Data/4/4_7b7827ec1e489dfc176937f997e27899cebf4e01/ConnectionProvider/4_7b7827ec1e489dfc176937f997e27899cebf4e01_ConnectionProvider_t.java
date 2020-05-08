 package repoll.server.mappers;
 
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 /*
  * Registry object holding global connection instance.
  */
 public class ConnectionProvider {
     private static final Logger LOG = Logger.getLogger(ConnectionProvider.class);
     private static final String DEFAULT_DB_PATH = "db/main";
 
     private static ConnectionProvider INSTANCE = new ConnectionProvider(DEFAULT_DB_PATH, false);
 
     public static synchronized Connection connection() {
         return INSTANCE.getConnection();
     }
 
     /**
      * @param path - path to Derby database directory. It MUST NOT contain protocol part and any additional parameters.
      * @param create - whether 'create=true' parameter should be added to database URL
      */
     public static synchronized void registerConnection(@NotNull String path, boolean create) {
         INSTANCE = new ConnectionProvider(path, create);
        LOG.debug("Registered connection to database " + path);
     }
 
     private final Connection connection;
 
     protected ConnectionProvider(@NotNull String path, boolean create) {
         try {
            this.connection = DriverManager.getConnection("jdbc:derby:" + path + (create ? ";create=true" : ""));
         } catch (SQLException e) {
             Path absolute = Paths.get(path).toAbsolutePath();
             LOG.error("Failed connection to " + absolute, e);
             throw new AssertionError(String.format("Connection to database '%s' can't be established.", absolute), e);
         }
     }
 
     protected Connection getConnection() {
         return connection;
     }
 }
