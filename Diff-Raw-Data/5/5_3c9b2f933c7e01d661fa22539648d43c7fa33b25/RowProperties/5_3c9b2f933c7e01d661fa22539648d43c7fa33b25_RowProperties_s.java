 package org.makumba.parade.init;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 /**
  * This class handles the extraction of information from the rows.properties file, and creates it if it doesn't exist
  * (along with an example string demonstrating its syntax). The rows.properties file is read when a new instance is
  * created and every time the row information is requested through the getRowDefinitions() method.
  * 
  * @author Manuel Gay
  * 
  */
 public class RowProperties {
 
     private Properties state;
 
     private static String DEFAULT_ROWSFILE = "rows.properties";
 
     public Map<String, Map<String, String>> rowDefinitions = new HashMap<String, Map<String, String>>();
 
     static Logger logger = Logger.getLogger(RowProperties.class.getName());
 
     public RowProperties() {
         readRowDefinitions();
     }
 
     /**
      * Gets the row definition
      * 
      * @return a Map containing a set of Maps which themselves contain the row definitions. The key is the rowname.
      */
     public Map<String, Map<String, String>> getRowDefinitions() {
 
         // we read the definitions again, in case the file changed
         readRowDefinitions();
 
         return this.rowDefinitions;
     }
 
     /**
      * Adds a row definition to the parsed entries
      * 
      * @param name
      *            the name of the row
      * @param path
      *            the path of the row
      * @param webapp
      *            the relative path to the webapp folder
      * @param description
      *            the description of the row
      * @param user
      *            the name of the primary user of this row
      */
     public void addRowDefinition(String name, String path, String webapp, String description, String user) {
         Map<String, String> row = new HashMap<String, String>();
         row.put("name", name);
         row.put("path", path);
         row.put("webapp", webapp);
         row.put("desc", description);
         row.put("user", user);
 
         rowDefinitions.put(name, row);
 
     }
 
     public void setRowDefinitions(Map<String, Map<String, String>> rowStoreProperties) {
         this.rowDefinitions = rowStoreProperties;
     }
 
     /**
      * Reads the row definitions from the rows.properties file
      */
     public void readRowDefinitions() {
 
         state = new Properties();
         try {
             java.io.File rowProperties = new java.io.File(ParadeProperties.getParadeBase() + java.io.File.separator
                     + "webapp/WEB-INF/classes/" + DEFAULT_ROWSFILE);
             state.load(new FileInputStream(rowProperties));
 
         } catch (Exception e) {
             // if there's no row definition file, we create one
             logger.warn("No rows.properties file found, attempting to generate one");
             state.setProperty("", ParadeProperties.getParadeBase());
             try {
                 state.store(new FileOutputStream(new java.io.File(ParadeProperties.getClassesPath()
                         + java.io.File.separator + "rows.properties")), "rows\n" + "# example:\n"
                         + "# <name_appl>=<path, e.g. ..\\iplabWeb>\n" + "# rowdata.<name_appl>.obs=<space for notes>\n"
                         + "# rowdata.<name_appl>.webapp=<relative path to the context, e.g. 'public_html'>\n");
 
             } catch (FileNotFoundException e1) {
                 e1.printStackTrace();
             } catch (IOException e1) {
                 e1.printStackTrace();
             }
         }
 
         for (Enumeration e = state.keys(); e.hasMoreElements();) {
             String rowName = (String) e.nextElement();
             if (!rowName.startsWith("rowdata.")) {
 
                 // we check if the row path is valid, if not, we ignore it
                 String rowPath = state.getProperty(rowName);
 
                 java.io.File f = new java.io.File(rowPath);
                 if (f.exists()) {
                     extractRowDefinitions(rowName);
                 } else {
                     logger.error("Error in rows.properties: could not access the path " + rowPath + " for row "
                             + rowName + ". Please check if it is correct.");
                 }
             }
         }
     }
 
     /**
      * Extracts the row definition for one row
      * 
      * @param name
      *            the name of the row in the rows.properties file
      */
     private void extractRowDefinitions(String name) {
 
         String propName = "rowdata." + name + ".";
         String obs = "", webapp = "", user = "";
         for (Enumeration e = state.keys(); e.hasMoreElements();) {
             String key = (String) e.nextElement();
 
             if (key.startsWith(propName + "obs"))
                 obs = state.getProperty(propName + "obs");
             if (key.startsWith(propName + "webapp"))
                 webapp = state.getProperty(propName + "webapp");
             if (key.startsWith(propName + "user"))
                 user = state.getProperty(propName + "user");
         }
 
         // if we don't find anything, add as default the ParaDe row
         if (name.equals("")) {
             this.addRowDefinition("(root)", state.getProperty(name), ParadeProperties.getProperty("webapp.path"),
                     "ParaDe webapp", "parade");
         } else {
             this.addRowDefinition(name, state.getProperty(name), webapp, obs, user);
         }
     }
 
 }
