 package fi.helsinki.cs.okkopa.shared;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Component;
 
 /**
  * Manages and reads XML files.
  */
 @Component
 public class Settings extends Properties {
 
     private static final Logger LOGGER = Logger.getLogger(Settings.class.getName());
 
     public Settings() throws IOException {
         String envVar = "OKKOPA_SETTINGS_FILEPATH";
         String filePath = System.getenv(envVar);
         Properties props;
         if (filePath == null) {
             LOGGER.warn(envVar + " environment variable not set, trying to find settings.xml in program folder.");
             filePath = "settings.xml";
             try {
                 props = readSettingXML(filePath);
             } catch(IOException ex) {
                throw new IOException("Settings file could not be found using environment variable: "+envVar+". Value of "+envVar+" was set to: "+filePath+
                                      ". Make sure it points to your xml settings file.");
             }
         } else {
             try {
                 props = readSettingXML(filePath);
             } catch(IOException ex) {
                throw new IOException("Settings file could not be found. Environment variable: "+envVar+" was not set."+
                                      "Make sure it points to your xml settings file.");
             }
         }
         this.putAll(props);
     }
 
     /**
      *
      * @param fileName to where xml-file is.
      * @throws IOException
      */
     public Settings(String fileName) throws IOException {
         Properties props = readSettingXML(fileName);
         this.putAll(props);
     }
 
     private Properties readSettingXML(String fileName) throws IOException {
         Properties currentProps = new Properties();
 
         try {
             InputStream currentStream = getClass().getResourceAsStream("/" + fileName);
             currentProps.loadFromXML(currentStream);
             currentStream.close();
             return currentProps;
         } catch (Exception e) {
             FileInputStream fis = new FileInputStream(fileName);
             currentProps.loadFromXML(fis);
             fis.close();
             return currentProps;
         }
     }
 }
