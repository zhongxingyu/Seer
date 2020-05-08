 package no.ebakke.studycaster.backend;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class BackendConfiguration {
   public static final String JDBC_URL_PROPERTY    = "JDBC_CONNECTION_STRING";
   public static final String STORAGE_DIR_PROPERTY = "PARAM1";
 
   private String databaseURL;
   private String storageDirPath;
   private BackendException propertyError;
 
   public BackendConfiguration() {
     this(null, null);
   }
 
   public BackendConfiguration(String databaseURL, String storageDirPath) {
     InputStream in =
         Backend.class.getResourceAsStream("/development.properties");
     Properties develProp = new Properties();
     if (in != null) {
       try {
         develProp.load(in);
       } catch (IOException e) {
         propertyError = new BackendException(e);
         return;
       }
     }
     this.databaseURL    = databaseURL;
     this.storageDirPath = storageDirPath;
     if (this.databaseURL == null)
       this.databaseURL    = getProperty(develProp, JDBC_URL_PROPERTY);
     if (this.storageDirPath == null)
       this.storageDirPath = getProperty(develProp, STORAGE_DIR_PROPERTY);
   }
 
   private static String getProperty(Properties develProp, String key) {
     String ret = develProp.getProperty(key);
     if (ret == null)
       ret = System.getProperty(key);
    if (ret != null && ret.isEmpty())
      ret = null;
     return ret;
   }
 
   public String getDatabaseURL() throws BackendException {
     if (propertyError != null)
       throw propertyError;
     if (databaseURL == null) {
       throw new BackendException("Database URL property " +
           JDBC_URL_PROPERTY + " not defined");
     }
     return databaseURL;
   }
 
   public String getStorageDirPath() throws BackendException {
     if (propertyError != null)
       throw propertyError;
     if (storageDirPath == null) {
       throw new BackendException("Storage directory property " +
           STORAGE_DIR_PROPERTY + " not defined");
     }
     return storageDirPath;
   }
 }
