 package play.modules.multijpa;
 
 import play.Play;
 
 import java.util.Properties;
 
 public class DataSourceConfiguration {
 
     private static String prefix = "db";
 
     private String name;
     private Properties properties;
 
     public DataSourceConfiguration(String dataSourceName, Properties properties) {
         initialize(dataSourceName, properties);
     }
 
     public DataSourceConfiguration(String dataSourceName) {
         initialize(dataSourceName, Play.configuration);
     }
 
     private void initialize(String dataSourceName, Properties properties) {
         this.name = dataSourceName;
         this.properties = properties;
     }
 
     private String get(String key) {
         return getOrElse(key, null);
     }
 
     public String getOrElse(String key, String defaultValue) {
         String value = properties.getProperty(prefix + "." + name + "." + key);
 
         if (value != null) {
             return value;
         } else {
             return properties.getProperty(prefix + "." + key, defaultValue);
         }
     }
 
     public String getDB() {
        String defaultValue = properties.getProperty(prefix);
        String specificValue = properties.getProperty(prefix + "." + name);
        return specificValue != null ? specificValue : defaultValue;
     }
 
     public String getUrl() {
         return get("url");
     }
 
     public String getDriver() {
         return get("driver");
     }
 
     public String getUser() {
         return get("user");
     }
 
     public String getPass() {
         return get("pass");
     }
 }
