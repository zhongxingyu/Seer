 /*
  * Created On: May 25, 2006 10:40:09 PM
  * $Id$
  */
 package com.thinkparity;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import com.thinkparity.Constants.Directories;
 import com.thinkparity.Constants.FileNames;
 import com.thinkparity.Constants.PropertyNames;
 import com.thinkparity.Constants.Sundry;
 
 /**
  * The thinkParity client application entry point.
  * 
  * @author raymond@thinkparity.com
  * @version $Revision$
  */
 public class ThinkParity {
 
     /**
      * The thinkParity client application entry point.
      * 
      * @param args
      *            The command line arguments.
      */
     public static void main(String[] args) {
         try { new ThinkParity(); }
         catch(final Throwable t) {
             t.printStackTrace(System.err);
             System.exit(1);
         }
     }
 
     /**
      * Look for the file; and if it is not found; report an error and exit.
      *
      * @param file
      *      A file.
      */
     static void checkFileExists(final File file) {
         if(!file.exists()) {
             System.err.println("File " + file.getName() + " was not found.");
             System.exit(1);
         }
     }
 
     /**
      * Look for the file; and if it is not found; report an error and exit.
      * 
      * @param parent
      *            The parent abstract pathname
      * @param child
      *            The child pathname string
      * @see File#File(File, String)
      */
     static void checkFileExists(final File parent, final String child) {
         final File file = new File(parent, child);
         if(!file.exists()) {
             System.err.println("File " + parent.getName() + "/" + child + " was not found.");
             System.exit(1);
         }
     }
 
     /**
      * Look for a key in the set of properties and if it is not found; report
      * an error and exit.
      *
      * @param properties
      *      The properties to check.
      * @param key
      *      The property key to look for.
      */
     static void checkProperty(final Properties properties, final String key) {
         final String value = properties.getProperty(key, null);
         if(null == value) {
             System.err.println("Property " + key + " was not found.");
             System.exit(1);
         }
     }
 
     /**
      * Look for the key in the system properties and if it is not found;
      * report an error and exit.
      *
      * @param key
      *      A property key.
      */
     static void checkSystemProperty(final String key) {
         checkProperty(System.getProperties(), key);
     }
 
     /** thinkParity image. */
     private Image image;
 
     /** thinkParity properties. */
     private Properties properties;
 
     /** Create ThinkParity. */
     private ThinkParity() throws IOException {
         super();
         checkSystemProperty(PropertyNames.ParityInstall);
         loadProperties();
         executeImage();
         saveProperties();
     }
 
     /**
      * Create a new image; mount it and execute it.
      *
      */
     private void executeImage() {
         this.image = new Image(properties);
         this.image.mount();
         this.image.execute();
     }
 
     /**
      * Load thinkParity.properties.
      *
      */
     private void loadProperties() throws IOException {
         final File propertiesFile = new File(
                 Directories.ParityInstall, FileNames.ThinkParityProperties);
         checkFileExists(propertiesFile);
         this.properties = new Properties();
         PropertiesUtil.load(properties, propertiesFile);
     }
 
     /**
      * Save thinkParity.properties.
      *
      */
     private void saveProperties() throws IOException {
         properties.setProperty(PropertyNames.ParityImageName, image.getName());
        PropertiesUtil.store(properties, new File(Directories.ParityInstall, FileNames.ThinkParityProperties), Sundry.ThinkParityHeader);
     }
 }
