 package org.wyona.yarep.core;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URI;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.apache.log4j.Category;
 
 import org.wyona.commons.io.FileUtil;
 
 /**
  *
  */
 public class RepositoryFactory {
 
     private static Category log = Category.getInstance(RepositoryFactory.class);
 
     public static final String DEFAULT_CONFIGURATION_FILE = "yarep.properties";
     public static String CONFIGURATION_FILE = DEFAULT_CONFIGURATION_FILE;
 
     private Vector repositories;
 
     private URL propertiesURL;
 
     /**
      *
      */
     public RepositoryFactory() throws Exception {
         this(DEFAULT_CONFIGURATION_FILE);
     }
 
     /**
      * TODO: Make CONFIGURATION_FILE loadable from absolute path
      */
     public RepositoryFactory(String configurationFile) throws Exception {
         CONFIGURATION_FILE = configurationFile;
 
         propertiesURL = RepositoryFactory.class.getClassLoader().getResource(CONFIGURATION_FILE);
         if (propertiesURL == null) {
             log.warn("No such resource: " + CONFIGURATION_FILE);
             repositories = new Vector(0);
             return;
         }
 
         Properties props = new Properties();
         log.debug("Properties URL: " + propertiesURL);
         File propsFile = new File(propertiesURL.getFile());
         try {
             props.load(propertiesURL.openStream());
 
 	    String separator = ",";
             String[] tokens = props.getProperty("configurations").split(separator);
             if (tokens.length % 2 != 0) {
                 throw new Exception("Wrong number of config parameters: " + CONFIGURATION_FILE);
             }
 
             repositories = new Vector(tokens.length / 2);
             for (int i = 0;i < tokens.length / 2; i++) {
                 String repoID = tokens[2 * i];
                 String configFilename = tokens[2 * i + 1];
                 log.debug("Property File: " + propsFile.getAbsolutePath());
                 log.debug("PARENT: " + propsFile.getParentFile());
                 log.debug("Filename: " + configFilename);
                 File configFile;
                 if (new File(configFilename).isAbsolute()) {
                     configFile = new File(configFilename);
                 } else {
                     configFile = FileUtil.file(propsFile.getParentFile().getAbsolutePath(), new File(configFilename).toString());
                 }
                 log.debug("Configuration File: " + configFile.getAbsolutePath());
                 Repository rt = new Repository(repoID, configFile);
                 log.debug(rt.toString());
                 repositories.addElement(rt);
             }
 
             // see src/java/org/wyona/meguni/parser/Parser.java
         } catch (Exception e) {
             log.error(e.toString(), e);
             throw e;
         }
     }
 
 
     /**
      * Get properties URL
      */
     public URL getPropertiesURL() {
         return propertiesURL;
     }
 
     /**
      * Get repository IDs
      */
     public String[] getRepositoryIDs() {
         String[] ids = new String[repositories.size()];
         for (int i = 0;i < repositories.size(); i++) {
             ids[i] = ((Repository) repositories.elementAt(i)).getID();
         }
         return ids;
     }
 
     /**
      * List all registered repositories
      */
     public String toString() {
         String s = "Show all repositories listed within " + CONFIGURATION_FILE + " respectively set during runtime:";
         for (int i = 0;i < repositories.size(); i++) {
             s = s + "\n" + (Repository) repositories.elementAt(i);
         }
         return s;
     }
 
     /**
      * Get repository from yarep.properties
      *
      * @param rid Repository ID
      */
     public Repository newRepository(String rid) {
         for (int i = 0;i < repositories.size(); i++) {
             if (((Repository) repositories.elementAt(i)).getID().equals(rid)) return (Repository) repositories.elementAt(i);
         }
        log.warn("No such repository: " + rid + " (" + getPropertiesURL() + ")");
         return null;
     }
 
     /**
      * Get first repository from yarep.properties
      *
      */
     public Repository firstRepository() {
         if (repositories.size() > 0) return (Repository) repositories.elementAt(0);
         log.error("No repositories (set within yarep.properties)!");
         return null;
     }
 
     /**
      * Get repository from specified config, whereas config is being resolved relative to classpath
      */
     public Repository newRepository(String rid, File config) {
         if (exists(rid)) {
             log.warn("Repository ID already exists: " + rid + " Repository will not be added to list of Repository Factory!");
             return null;
         }
 
         if (!config.isAbsolute()) {
             URL configURL = RepositoryFactory.class.getClassLoader().getResource(config.toString());
             try {
                 File configFile = new File(configURL.getFile());
                 log.debug("Config file: " + configFile);
                 repositories.addElement(new Repository(rid, configFile));
                 return new Repository(rid, configFile);
             } catch (Exception e) {
                 log.error(e);
                 return null;
             }
         }
         // TODO: Register rid
         return new Repository(rid, config);
     }
 
     /**
      * Check if repository exists
      *
      * @param rid Repository ID
      */
     public boolean exists(String rid) {
         for (int i = 0;i < repositories.size(); i++) {
             if (((Repository) repositories.elementAt(i)).getID().equals(rid)) return true;
         }
        log.debug("No such repository: " + rid + " (" + getPropertiesURL() + ")");
         return false;
     }
 }
