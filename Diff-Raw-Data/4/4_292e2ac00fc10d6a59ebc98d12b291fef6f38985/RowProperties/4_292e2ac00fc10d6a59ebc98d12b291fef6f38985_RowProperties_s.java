 package org.makumba.parade.init;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 public class RowProperties {
 
     private Properties state;
     
     private static String DEFAULT_ROWSFILE = "/rows.properties";
 
     public Map rowDefinitions = new HashMap();
     
     static Logger logger = Logger.getLogger(ParadeProperties.class.getName());
 
     public RowProperties() {
         
         /* for development purposes
 
         this.addRowDefinition("(root)", ".", "webapp", "ParaDe webapp");
         this.addRowDefinition("test2-k", "../parade", "", "the old parade row");
         this.addRowDefinition("manu-k", "E:\\bundle\\sources\\karamba", "public_html", "manu messing it all up again");
         */
         readRowDefinitions();
     }
 
     
     /* Get Row definitions */
     public Map getRowDefinitions() {
         return this.rowDefinitions;
 
     }
 
     /* Add a row definition */
     public void addRowDefinition(String name, String path, String webapp, String description) {
         Map row = new HashMap();
         row.put("name", name);
         row.put("path", path);
         row.put("webapp", webapp);
         row.put("desc", description);
 
         rowDefinitions.put(name, row);
 
     }
 
     /* Delete row definition */
     public void delRowDefinition() {
 
     }
 
     public void setRowDefinitions(Map rowStoreProperties) {
         this.rowDefinitions = rowStoreProperties;
     }
 
     /* reads row definition from properties file */
     public void readRowDefinitions() {
         state = new Properties();
         try {
             state.load(RowProperties.class.getResourceAsStream(DEFAULT_ROWSFILE));
             
         } catch (Exception e) {
             // if there's no row definition file, we create one
             logger.warn("No rows.properties file found, attempting to generate one");
             state.setProperty("", ParadeProperties.getParadeBase());
             try {
                state.store(new FileOutputStream(new java.io.File(ParadeProperties.getParadeBase() + java.io.File.separator + "webapp" + java.io.File.separator + "WEB-INF" + java.io.File.separator + "classes" + java.io.File.separator + "rows.properties")), 
                         "rows\n"
                         + "# example:\n"
                         + "# <name_appl>=<path, e.g. ..\\iplabWeb>\n"
                         + "# rowdata.<name_appl>.obs=<space for notes>\n"
                         + "# rowdata.<name_appl>.webapp=<relative path to the context, e.g. 'public_html'>\n");
             } catch (FileNotFoundException e1) {
                 // TODO Auto-generated catch block
                 e1.printStackTrace();
             } catch (IOException e1) {
                 // TODO Auto-generated catch block
                 e1.printStackTrace();
             }
     }
         
         for(Enumeration e= state.keys(); e.hasMoreElements();) {
             String key = (String) e.nextElement();
             if (!key.startsWith("rowdata.")) {
                 extractRowDefinitions(key);
                 
             }    
         }
     }
 
     /* Extracts the definition of one row */
     private void extractRowDefinitions(String name) {
         
         String propName = "rowdata." + name + ".";
         String obs="", webapp="";
         for (Enumeration e = state.keys(); e.hasMoreElements();) {
             String key = (String) e.nextElement();
             
             if(key.startsWith(propName + "obs"))
                 obs = state.getProperty(propName + "obs");
             if(key.startsWith(propName + "webapp"))
                 webapp = state.getProperty(propName + "webapp");
         }
         
         // if we don't find anything, add as default the ParaDe row
         if(name.equals("")) {
             this.addRowDefinition("(root)", state.getProperty(name), ParadeProperties.getProperty("webapp.path"), "ParaDe webapp");
         } else {
             this.addRowDefinition(name, state.getProperty(name), webapp, obs);
         }
         
     }
 
 }
