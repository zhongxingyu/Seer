 package uk.ac.ebi.arrayexpress.app;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Properties;
 
 public class ApplicationPreferences extends ApplicationComponent
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private String propertiesFileName;
     private Properties properties;
 
     public ApplicationPreferences( Application app, String fileName )
     {
         super("Preferences");
 
         propertiesFileName = fileName;
         properties = new Properties();
     }
 
     public void initialize()
     {
         load();
     }
 
     public void terminate()
     {
         properties = null;
     }
 
     public String getString( String key )
     {
         return properties.getProperty(key);
     }
 
     public Long getLong( String key )
     {
         Long value = null;
         String strVal = null;
         try {
             strVal = properties.getProperty(key);
            if (null != strVal && !strVal.trim().equals("")) {
                 value = Long.valueOf(strVal);
            }
         } catch ( NumberFormatException x ) {
             logger.error("Value [{}] of preference [{}] is expected to be a number", strVal, key);
         } catch ( Throwable x ) {
             logger.error("Caught an exception while converting value [" + properties.getProperty(key) + "] of preference [" + key + "] to Long:", x);
         }
 
         return value;
     }
 
     public boolean getBoolean( String key )
     {
         String value = properties.getProperty(key);
         return (null != value && value.toLowerCase().equals("true"));
     }
 
     private void load()
     {
         try {
             properties.load(
                     Application.getInstance().getResource(
                             "/WEB-INF/classes/" + propertiesFileName + ".properties"
                     ).openStream()
             );
         } catch ( Throwable e ) {
             logger.error("Caught an exception:", e);
         }
     }
 }
