 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.core;
 
 import sorcer.util.HostUtil;
 import sorcer.service.ConfigurationException;
 import sorcer.util.GenericUtil;
 import sorcer.util.ParentFirstProperties;
 import sorcer.util.StringUtils;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.security.AccessControlException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static sorcer.core.SorcerConstants.*;
 
 public class SorcerEnv {
 
     // This is copied from Context
     final static String DATA_NODE_TYPE = "dnt";
     final static Logger logger = Logger.getLogger(SorcerEnv.class.getName());
     public static boolean debug = false;
     /**
      * Default name 'provider.properties' for a file defining provider
      * properties.
      */
     public static String PROVIDER_PROPERTIES_FILENAME = "provider.properties";
     protected static SorcerEnv sorcerEnv;
     /**
      * General database prefix for the SORCER database schema.
      */
     private static String dbPrefix = "SOC";
     private static String sepChar = ".";
     /**
      * Default name 'data.formats' for a file defining service dataContext node
      * types.
      */
     private static String CONTEXT_DATA_FORMATS = "data.formats";
     /**
      * Default name 'servid.per' for a file storing a service registration ID.
      */
     private static String serviceIdFilename = "servid.per";
 
 	static String SCRATCH_DIR_ORIG;
 
     /**
      * Port for a code sever (webster)
      */
     private static int port = 0;
     /*
      * location of the SORCER environment properties file loaded by this
      * environment
      */
     protected String loadedEnvFile;
     /**
      * Stores the description of where from the sorcer env was loaded
      */
     protected String envFrom;
     private Properties properties = new ParentFirstProperties(System.getProperties());
     /**
      * Indicates if Booter is used in this environment.
      */
     private boolean bootable = false;
 
 	static int subDirCounter = 0;
 
     private LookupLocators lookupLocators = new LookupLocators();
 
     public SorcerEnv(Map<String, String> props) {
         this();
         properties.putAll(props);
     }
 
     protected SorcerEnv() {
     }
 
     static {
         sorcerEnv = new SorcerEnv();
         sorcerEnv.loadBasicEnvironment();
         sorcerEnv.overrideFromEnvironment(System.getenv());
     }
 
     /**
      * Checks which port to use for a SORCER class server.
      *
      * @return a port number
      */
     public static int getWebsterPort() {
         if (port != 0)
             return port;
 
         String wp = System.getenv("SORCER_WEBSTER_PORT");
         if (wp != null && wp.length() > 0) {
             return new Integer(wp);
         }
 
         wp = System.getProperty(P_WEBSTER_PORT);
         if (wp != null && wp.length() > 0) {
             return new Integer(wp);
         }
 
         wp = getProperty(P_WEBSTER_PORT);
         if (wp != null && wp.length() > 0) {
             return new Integer(wp);
         }
 
         try {
             port = getAnonymousPort();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return port;
     }
 
     /**
      * Returns the start port to use for a SORCER code server.
      *
      * @return a port number
      */
     public static int getWebsterStartPort() {
         String hp = System.getenv("SORCER_WEBSTER_START_PORT");
         if (hp != null && hp.length() > 0) {
             return new Integer(hp);
         }
 
         hp = System.getProperty(P_WEBSTER_START_PORT);
         if (hp != null && hp.length() > 0) {
             return new Integer(hp);
         }
 
         hp = getProperty(P_WEBSTER_START_PORT);
         if (hp != null && hp.length() > 0) {
             return new Integer(hp);
 
         }
 
         return 0;
     }
 
     /**
      * Returns the end port to use for a SORCER code server.
      *
      * @return a port number
      */
     public static int getWebsterEndPort() {
         String hp = System.getenv("SORCER_WEBSTER_END_PORT");
 
         if (hp != null && hp.length() > 0) {
             return new Integer(hp);
         }
 
         hp = System.getProperty(P_WEBSTER_END_PORT);
         if (hp != null && hp.length() > 0) {
             return new Integer(hp);
         }
 
         hp = getProperty(P_WEBSTER_END_PORT);
         if (hp != null && hp.length() > 0) {
             return new Integer(hp);
         }
 
         return 0;
     }
 
     /**
      * Returns a registered service ID received previously from a LUS and
      * persisted in a file.
      *
      * @return service ID filename
      */
     public static String getServiceIdFilename() {
         return serviceIdFilename;
     }
 
     /**
      * Return
      * <code>true<code> if multicast for lookup discovery is enabled, otherwise <code>false<code>.
      *
      * @return true if multicast is enabled, default is true.
      */
     public static boolean isMulticastEnabled() {
         return getProperty(MULTICAST_ENABLED, "true").equals("true");
     }
 
     /**
      * Does a ServiceDiscoveryManager use a lookup cache?
      *
      * @return true if ServiceDiscoveryManager use a lookup cache.
      */
     public static boolean isLookupCacheEnabled() {
         return getProperty(LOOKUP_CACHE_ENABLED, "false").equals("true");
     }
 
     /**
      * Returns required wait duration for a ServiceDiscoveryManager.
      *
      * @return wait duration
      */
     public static Long getLookupWaitTime() {
         return Long.parseLong(getProperty(LOOKUP_WAIT, "1000"));
     }
 
     /**
      * Returns a number of min lookup matched for a ServiceDiscoveryManager.
      *
      * @return number of min lookup matches
      */
     public static int getLookupMinMatches() {
         return Integer.parseInt(getProperty(LOOKUP_MIN_MATCHES, "1"));
     }
 
     /**
      * Returns a number of max lookup matched for a ServiceDiscoveryManager.
      *
      * @return number of max lookup matches
      */
     public static int getLookupMaxMatches() {
         return Integer.parseInt(getProperty(LOOKUP_MAX_MATCHES, "999"));
     }
 
     /**
      * Returns a URL for the SORCER class server.
      *
      * @return the current URL for the SORCER class server.
      */
     public static String getWebsterUrl() {
         return "http://" + getWebsterInterface() + ':' + getWebsterPort();
     }
 
     /**
      * Returns a URL for the SORCER class server.
      *
      * @return the current URL for the SORCER class server.
      */
     public static URL getWebsterUrlURL() {
         try {
             return new URL("http", getWebsterInterface(), getWebsterPort(), "");
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static String getDatabaseStorerUrl() {
         //return "sos://" + DatabaseStorer.class.getName() + '/' + getActualDatabaseStorerName();
         return "sos://DatabaseStorer/" + getActualDatabaseStorerName();
     }
 
     public static String getDataspaceStorerUrl() {
         return "sos://DataspaceStorer/" + getActualDatabaseStorerName();
         //return "sos://" + DataspaceStorer.class.getName() + '/' + getActualSpacerName();
     }
 
     /**
      * Returns a URL for the SORCER data server.
      *
      * @return the current URL for the SORCER data server.
      */
     public static String getDataServerUrl() {
         return "http://" + getDataServerInterface() + ':' + getDataServerPort();
     }
 
     /**
      * Returns the hostname of a data server.
      *
      * @return a data server name.
      */
     public static String getDataServerInterface() {
         String hn = System.getenv("DATA_SERVER_INTERFACE");
 
         if (hn != null && hn.length() > 0) {
             logger.finer("data server hostname as the system environment value: "
                     + hn);
             return hn;
         }
 
         hn = System.getProperty(DATA_SERVER_INTERFACE);
         if (hn != null && hn.length() > 0) {
             logger.finer("data server hostname as 'data.server.interface' system property value: "
                     + hn);
             return hn;
         }
 
         hn = getProperty(DATA_SERVER_INTERFACE);
         if (hn != null && hn.length() > 0) {
             logger.finer("data server hostname as 'data.server.interface' provider property value: "
                     + hn);
             return hn;
         }
 
         try {
             hn = getHostName();
             logger.finer("data.server.interface hostname as the local host value: "
                     + hn);
         } catch (UnknownHostException e) {
             logger.severe("Cannot determine the data.server.interface hostname.");
         }
 
         return hn;
     }
 
     /**
      * Returns the port of a provider data server.
      *
      * @return a data server port.
      */
     public static int getDataServerPort() {
         String wp = System.getenv("DATA_SERVER_PORT");
         if (wp != null && wp.length() > 0) {
             // logger.finer("data server port as 'DATA_SERVER_PORT': " + wp);
             return new Integer(wp);
         }
 
         wp = System.getProperty(DATA_SERVER_PORT);
         if (wp != null && wp.length() > 0) {
             // logger.finer("data server port as System 'data.server.port': "
             // + wp);
             return new Integer(wp);
         }
 
         wp = getProperty(DATA_SERVER_PORT);
         if (wp != null && wp.length() > 0) {
             logger.info("data server port as Sorcer 'data.server.port': "
                             + wp);
             return new Integer(wp);
         }
 
         // logger.severe("Cannot determine the 'data.server.port'.");
         throw new RuntimeException("Cannot determine the 'data.server.port'.");
     }
 
     /**
      * Specify a URL for the SORCER application server; default is
      * http://127.0.0.1:8080/
      *
      * @return the current URL for the SORCER application server.
      */
     public static String getPortalUrl() {
         return sorcerEnv.getProperty("http://" + P_PORTAL_HOST) + ':'
                 + sorcerEnv.getProperty(P_PORTAL_PORT);
     }
 
     /**
      * Gets the service locators for unicast discovery.
      *
      * @return and array of strings as locator URLs
      */
     public static String[] getLookupLocators() {
         LookupLocators lookupLocators = sorcerEnv.lookupLocators;
         if (!lookupLocators.isInitialized()) {
             String locs = getProperty(P_LOCATORS);
             lookupLocators.setStaticUrls((locs != null && locs.length() != 0) ? toArray(locs)
                     : new String[]{});
         }
         return lookupLocators.getLookupLocators();
     }
 
     /**
      * Returns the Jini Lookup Service groups for this environment.
      *
      * @return an array of group names
      */
     public static String[] getLookupGroups() {
         String[] ALL_GROUPS = null; // Jini ALL_GROUPS
         String groups = getProperty(P_GROUPS);
         if (groups == null || groups.length() == 0)
             return ALL_GROUPS;
         return toArray(groups);
     }
 
     public static String getRioVersion() {
         return sorcerEnv.getProperty(S_VERSION_RIO, RIO_VERSION);
     }
 
     /***
     /**
      * Gets an exertion space group name to use with this environment.
      *
      * @return a of space group name
      */
     public static String getSpaceGroup() {
         return getProperty(P_SPACE_GROUP, getLookupGroups()[0]);
     }
 
     /**
      * Returns an exertion space name to use with this environment.
      *
      * @return a not suffixed space name
      */
 
     public static String getSpaceName() {
         return getProperty(P_SPACE_NAME, "Exert Space");
     }
 
     /**
      * Returns an the actual space name, eventually suffixed, to use with this environment.
      *
      * @return a space actual name
      */
     public static String getActualSpaceName() {
         return getActualName(getSpaceName());
     }
 
     /**
      * Returns whether this cache store should be in a database or local file.
      *
      * @return true if cached to file
      */
     public static boolean getPersisterType() {
         if ((getProperty(S_PERSISTER_IS_DB_TYPE)).equals("true"))
             return true;
         else
             return false;
     }
 
     /**
      * Checks which host to use for RMI.
      *
      * @return a name of ExertMonitor provider
      */
     public static String getExertMonitorName() {
         return getProperty(EXERT_MONITOR_NAME, "Exert Monitor");
     }
 
     public static String getDatabaseStorerName() {
         return getProperty(DATABASE_STORER_NAME, "Database Storage");
     }
 
     public static String getActualDatabaseStorerName() {
         return getActualName(getDatabaseStorerName());
     }
 
     public static String getDataspaceStorerName() {
         return getProperty(DATASPACE_STORER_NAME, "Dataspace Storage");
     }
 
     public static String getActualDataspaceStorerName() {
         return getActualName(getDataspaceStorerName());
     }
 
     public static String getSpacerName() {
         return sorcerEnv.getProperty(SPACER_NAME, "Spacer");
     }
 
     public static String getActualSpacerName() {
         return getActualName(getSpacerName());
     }
 
     /**
      * Checks which host to use for RMI.
      *
      * @return a hostname
      */
     public static String getRmiHost() {
         return sorcerEnv.getProperty(S_RMI_HOST);
     }
 
     /**
      * Checks which port to use for RMI.
      *
      * @return a port number
      */
     public static String getRmiPort() {
         return sorcerEnv.getProperty(S_RMI_HOST, "1099");
     }
 
     /**
      * Specifies a host to be used for the SORCER SORCER application server. A
      * default host name is localhost.
      *
      * @return a hostname
      */
     public static String getPortalHost() {
         return sorcerEnv.getProperty(P_PORTAL_HOST);
     }
 
     /**
      * Specifies a port to be used for the SORCER application server. A default
      * port is 8080.
      *
      * @return a port number
      */
     public static String getPortalPort() {
         return sorcerEnv.getProperty(P_PORTAL_PORT);
     }
 
     /**
      * Checks whether a certain boolean property is set.
      *
      * @param property
      * @return true if property is set
      */
     public static boolean isOn(String property) {
         return sorcerEnv.getProperty(property, "false").equals("true");
     }
 
     /**
      * Should we use the Oracle DB for the Persister service provider?
      *
      * @return true if we should
      */
     public static boolean isDbOracle() {
         return sorcerEnv.getProperty(S_IS_DB_ORACLE, "false").equals("true");
     }
 
     /**
      * Return true if a modified name is used.
      *
      * @return true if name is suffixed
      */
     public static boolean nameSuffixed() {
         return sorcerEnv.getProperty(S_IS_NAME_SUFFIXED, "false").equals("true");
     }
 
     /**
      * Gets the value of a certain property.
      *
      * @param property
      * @return the string value of that property
      */
     public static String getProperty(String property) {
         return getEnvProperties().getProperty(property);
     }
 
     /**
      * Sets the value of a certain property.
      *
      * @param key
      * @param value
      */
     public static void setProperty(String key, String value) {
         getEnvProperties().setProperty(key, value);
     }
 
     /**
      * Gets the value for a certain property or the default value if property is
      * not set.
      *
      * @param property
      * @param defaultValue
      * @return the string value of that property
      */
     public static String getProperty(String property, String defaultValue) {
         return getEnvProperties().getProperty(property, defaultValue);
     }
 
     /**
      * All database table names start with this schema prefix.
      *
      * @return
      */
     public static String getDBPrefix() {
         return dbPrefix;
     }
 
     public static void setDBPrefix(String prefix) {
         dbPrefix = prefix;
     }
 
     public static String getSepChar() {
         return sepChar;
     }
 
     public static void setSepChar(String character) {
         sepChar = character;
     }
 
     /**
      * @param tableBaseName
      * @return
      */
     public static String seqIdPath(String tableBaseName) {
         return dbPrefix + "_" + tableBaseName + "." + tableBaseName + "_Seq_Id";
     }
 
     /**
      * @param tableBaseName
      * @return
      */
     public static String seqName(String tableBaseName) {
         return dbPrefix + "_" + tableBaseName + "_seq";
     }
 
     /**
      * @param tableBaseName
      * @return
      */
     public static String seqIdName(String tableBaseName) {
         return tableBaseName + "_Seq_Id";
     }
 
     /**
      * @param tableBaseName
      * @return
      */
     public static String tableName(String tableBaseName) {
         return dbPrefix + "_" + tableBaseName;
     }
 
     /**
      * Uses getRMIHost and getRMIPort to return the RMI registry URL.
      */
     public static String getRmiUrl() {
         return "rmi://" + SorcerEnv.getRmiHost() + ":" + SorcerEnv.getRmiPort() + "/";
     }
 
     /**
      * Returns the name of the JNDI dataContext factory.
      *
      * @return a fully qualified name of class of dataContext factory
      */
     public static String getContextFactory() {
         return "com.sun.jndi.rmi.registry.RegistryContextFactory";
     }
 
     /**
      * Returns the JNDI dataContext provider URL string.
      *
      * @return URL string of the JNDI dataContext provider
      */
     public static String getContextProviderUrl() {
         return getRmiUrl();
     }
 
     /**
      * Returns the properties. Implementers can use this method instead of the
      * access methods to cache the environment and optimize performance.
      *
      * @return the instance of Properties
      */
     public static Properties getEnvProperties() {
         return sorcerEnv.properties;
     }
 
     public void setEnvProperties(Properties properties) {
         this.properties = properties;
     }
 
     /**
      * Returns the properties. Implementers can use this method instead of the
      * access methods to cache the environment and optimize performance. Name of
      * properties are defined in sorcer.core.SorceConstants.java
      *
      * @return the props
      */
     public static Properties getProperties() {
         return getEnvProperties();
     }
 
     /**
      * Updates this environment properties from the provider properties
      * <code>properties</code> and makes them available as the global SORCER
      * environment properties.
      *
      * @param properties the additional properties used to update the
      *                   <code>Sorcer<code>
      *                   properties
      */
     public static void updateFromProperties(Properties properties) {
 
         try {
             String val = null;
 
             val = properties.getProperty(SORCER_HOME);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(SORCER_HOME, val);
 
             val = properties.getProperty(S_JOBBER_NAME);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(S_JOBBER_NAME, val);
 
             val = properties.getProperty(S_CATALOGER_NAME);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(S_CATALOGER_NAME, val);
 
             val = properties.getProperty(S_COMMANDER_NAME);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(S_COMMANDER_NAME, val);
 
             val = properties.getProperty(SORCER_HOME);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(SORCER_HOME, val);
 
             val = properties.getProperty(S_RMI_HOST);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(S_RMI_HOST, val);
             val = properties.getProperty(S_RMI_PORT);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(S_RMI_PORT, val);
 
             val = properties.getProperty(P_WEBSTER_INTERFACE);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(P_WEBSTER_INTERFACE, val);
             val = properties.getProperty(P_WEBSTER_PORT);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(P_WEBSTER_PORT, val);
 
             val = properties.getProperty(P_PORTAL_HOST);
             if (val != null && val.length() != 0)
                 getEnvProperties().put("P_PORTAL_HOST", val);
             val = properties.getProperty(P_PORTAL_PORT);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(P_PORTAL_PORT, val);
 
             // provider data
             val = properties.getProperty(DATA_SERVER_INTERFACE);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(DATA_SERVER_INTERFACE, val);
             val = properties.getProperty(DATA_SERVER_PORT);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(DATA_SERVER_PORT, val);
             val = properties.getProperty(P_DATA_DIR);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(P_DATA_DIR, val);
 
             val = properties.getProperty(P_SCRATCH_DIR);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(P_SCRATCH_DIR, val);
 
             val = properties.getProperty(LOOKUP_WAIT);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(LOOKUP_WAIT, val);
 
             val = properties.getProperty(LOOKUP_CACHE_ENABLED);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(LOOKUP_CACHE_ENABLED, val);
 
             val = properties.getProperty(P_SERVICE_ID_PERSISTENT);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(P_SERVICE_ID_PERSISTENT, val);
 
             val = properties.getProperty(P_SPACE_GROUP);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(P_SPACE_GROUP, val);
 
             val = properties.getProperty(P_SPACE_NAME);
             if (val != null && val.length() != 0)
                 getEnvProperties().put(P_SPACE_NAME, val);
 
         } catch (AccessControlException ae) {
             ae.printStackTrace();
         }
     }
 
     /**
      * Returns the provider's data directory.
      *
      * @return a provider data directory
      */
     public static File getDataDir() {
         return getDocRootDir();
     }
 
     /**
      * Returns a directory for provider's HTTP document root directory.
      *
      * @return a HTTP document root directory
      */
     public static File getDocRootDir() {
         return new File(System.getProperty(DOC_ROOT_DIR));
     }
 
     public static File getDataFile(String filename) {
         return new File(getDataDir() + File.separator + filename);
     }
 
     /**
      * Returns a directory for providers's scratch files
      *
      * @return a scratch directory
      */
     static public File getUserHomeDir() {
         return new File(System.getProperty("user.home"));
     }
 
     /**
      * Returns a directory for providers's scratch files
      *
      * @return a scratch directory
      */
     static public File getScratchDir() {
         return getNewScratchDir();
     }
 
     public static synchronized String getUniqueId() {
         // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
         SimpleDateFormat sdf = new SimpleDateFormat("dd-HHmmss");
         long time = System.currentTimeMillis();
 
         String uid = UUID.randomUUID().toString();
         // return sdf.format(time) + "-" + Long.toHexString(time);
         return sdf.format(time) + "-" + uid;
     }
 
     /**
      * Returns a directory for providers's new scratch files
      *
      * @return a scratch directory
      */
     static public File getNewScratchDir() {
         return getNewScratchDir("");
     }
 
     static public File getNewScratchDir(String scratchDirNamePrefix) {
         logger.info("scratch_dir = " + System.getProperty(SCRATCH_DIR));
         logger.info("dataDir = " + getDataDir());
         String dirName = getDataDir() + File.separator
                 + System.getProperty(SCRATCH_DIR) + File.separator
                 + getUniqueId();
         File tempdir = new File(dirName);
         File scratchDir = null;
         if (scratchDirNamePrefix == null || scratchDirNamePrefix.length() == 0) {
             scratchDir = tempdir;
         } else {
             scratchDir = new File(tempdir.getParentFile(), scratchDirNamePrefix
                     + tempdir.getName());
         }
 		
 		// check to see number of dirs in parent directory (32000 is problem in 
 		// linux)
 		File parentDir = scratchDir.getParentFile();
 		String[] subDirs = parentDir.list(new FilenameFilter() {
 				public boolean accept(File dir, String name) {
 				 return new File(dir, name).isDirectory();
 			}												
 		});
 		logger.info("subDirs = " + subDirs);
 		if (subDirs != null) {
 			logger.info("number of subdirectories = " + subDirs.length);
 			if (subDirs.length > 10000) {
 				logger.info("number of subdirectories is > 10,000; setting sys prop...");
 				System.setProperty(SCRATCH_DIR, SCRATCH_DIR_ORIG + subDirCounter++);
 				logger.info("DONE setting sys prop.");
 			}
 		}
 		//logger.info("scratchDir = " + scratchDir);
 		boolean madeDirs = scratchDir.mkdirs();
 		//logger.info("madeDirs = " + madeDirs);
 		//logger.info("can read? " + scratchDir.canRead());
 
         return scratchDir;
     }
 
     public static String getAbsoluteScrachFilename(String filename) {
         return getNewScratchDir() + File.separator + filename;
     }
 
     /**
      * Returns the URL of a scratch file at the provider HTTP data server.
      *
      * @param scratchFile
      * @return the URL of a scratch file
      * @throws MalformedURLException
      */
     public static URL getScratchURL(File scratchFile)
             throws MalformedURLException {
         String dataUrl = getDataServerUrl();
         String path = scratchFile.getAbsolutePath();
 
         //String scratchDir = System.getProperty(SCRATCH_DIR);
 
         //File scratchDirFile = new File(scratchDir);
         //scratchDir = scratchDirFile.getPath();
 
         //int index = path.indexOf(scratchDir);
 
         logger.info("dataUrl = " + dataUrl);
         logger.info("scratchFile = " + scratchFile.getAbsolutePath());
         //logger.info("scratchDir = " + scratchDir);
 
         logger.info("DOC_ROOT_DIR = " + System.getProperty(DOC_ROOT_DIR));
         logger.info("substring = "
                 + path.substring(System.getProperty(DOC_ROOT_DIR).length() + 1));
         //if (index < 0) {
         //    throw new MalformedURLException("Scratch file: " + path
         //            + " is not in: " + scratchDir);
         //}
         String url = dataUrl + File.separator
                 + path.substring(System.getProperty(DOC_ROOT_DIR).length() + 1);
         url = url.replaceAll("\\\\+", "/");
         logger.info("url = " + url);
 
         return new URL(url);
     }
 
     /**
      * Returns the URL of a dataFile at the provider HTTP data server.
      *
      * @param dataFile
      * @return the URL of a data file
      * @throws MalformedURLException
      */
     public static URL getDataURL(File dataFile) throws MalformedURLException {
         String dataUrl = getDataServerUrl();
         String path = dataFile.getAbsolutePath();
         String docDir = System.getProperty(DOC_ROOT_DIR);
         int index = path.indexOf(docDir);
         if (index < 0) {
             throw new MalformedURLException("Data file: " + path
                     + " is not in: " + docDir);
         }
         return new URL(dataUrl + File.separator
                 + path.substring(System.getProperty(DOC_ROOT_DIR).length() + 1));
     }
 
     /**
      * Get an anonymous port.
      *
      * @return An anonymous port created by invoking {@link #getPortAvailable()}.
      *         Once this method is called the return value is set statically for
      *         future reference
      * @throws IOException If there are problems getting the anonymous port
      */
     public static int getAnonymousPort() throws IOException {
         if (port == 0)
             port = getPortAvailable();
         return port;
     }
 
     private static void storeEnvironment(String filename) {
         getEnvProperties().setProperty(P_WEBSTER_PORT, "" + port);
         try {
             getEnvProperties().setProperty(P_WEBSTER_INTERFACE, getHostName());
         } catch (UnknownHostException e) {
             e.printStackTrace();
             return;
         }
         try {
             if (filename != null)
                 getEnvProperties().store(new FileOutputStream(filename),
                         "SORCER auto-generated environment properties");
             else
                 getEnvProperties().store(new FileOutputStream(getEnvironment().loadedEnvFile),
                         "SORCER auto-generated environment properties");
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Get an anonymous port
      *
      * @return An available port created by instantiating a
      *         <code>java.net.ServerSocket</code> with a port of 0
      * @throws IOException If an available port cannot be obtained
      */
     public static int getPortAvailable() throws java.io.IOException {
         java.net.ServerSocket socket = new java.net.ServerSocket(0);
         int port = socket.getLocalPort();
         socket.close();
         return port;
     }
 
     public static void updateCodebase() {
         String codebase = System.getProperty("java.rmi.server.codebase");
         if (codebase == null)
             return;
         String pattern = "${localhost}";
         if (codebase.indexOf(pattern) >= 0) {
             try {
                 String val = codebase.replace(pattern, getHostAddress());
                 System.setProperty("java.rmi.server.codebase", val);
             } catch (UnknownHostException e1) {
                 e1.printStackTrace();
             }
         }
     }
 
     public static void setCodeBase(String[] jars) {
         String url = getWebsterUrl();
         String codebase = "";
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < jars.length - 1; i++) {
             sb.append(url).append("/").append(jars[i]).append(" ");
         }
         sb.append(url).append("/").append(jars[jars.length - 1]);
         codebase = sb.toString();
         System.setProperty("java.rmi.server.codebase", codebase);
         if (logger.isLoggable(Level.FINE))
            logger.fine("Setting codbase 'java.rmi.server.codebase': "
                     + codebase);
     }
 
     /**
      * Convert a comma, space, and '|' delimited String to array of Strings
      *
      * @param arg The String to convert
      * @return An array of Strings
      */
     public static String[] toArray(String arg) {
         StringTokenizer token = new StringTokenizer(arg, " ," + APS);
         String[] array = new String[token.countTokens()];
         int i = 0;
         while (token.hasMoreTokens()) {
             array[i] = token.nextToken();
             i++;
         }
         return (array);
     }
 
     public static String getNameSuffix() {
         String suffix = getProperty(S_NAME_SUFFIX);
         if (suffix == null)
             suffix = getDefaultNameSuffix(3);
         return suffix;
     }
 
     public static String getDefaultNameSuffix(int suffixLength) {
         return System.getProperty("user.name").substring(0, suffixLength)
                 .toUpperCase();
     }
 
     public static String getSuffixedName(String name) {
         String suffix = getProperty(S_NAME_SUFFIX,
                 getDefaultNameSuffix(3));
         return name + "-" + suffix;
     }
 
     public static String getActualName(String name) {
 		if (nameSuffixed()) {
 			String suffix = sorcerEnv.properties.getProperty(S_NAME_SUFFIX,
 					getDefaultNameSuffix(3));
 			if (name.indexOf(suffix) > 0)
 				return name;
 			else
 				return name + "-" + suffix;
 		}
         return name;
     }
 
     public static String getSuffixedName(String name, int suffixLength) {
         return name + "-" + getDefaultNameSuffix(suffixLength);
     }
 
     /**
      * Load dataContext node (value) types from default 'node.types'. SORCER node
      * types specify application types of data nodes in SORCER service contexts.
      * It is an analog of MIME types in SORCER. Each type has a format
      * 'cnt/application/format/modifiers'.
      */
     public static void loadContextNodeTypes(Hashtable<?, ?> map) {
         if (map != null && !map.isEmpty()) {
             String idName = null, cntName = null;
             String[] tokens;
             for (Enumeration<?> e = map.keys(); e.hasMoreElements(); ) {
                 idName = (String) e.nextElement();
                 tokens = toArray(idName);
                 cntName = ("".equals(tokens[1])) ? tokens[0] : tokens[1];
                 getEnvProperties().put(cntName,
                         DATA_NODE_TYPE + APS + map.get(idName));
             }
         }
     }
 
     public static SorcerEnv getEnvironment() {
         return sorcerEnv;
     }
 
     /**
      * Returns the home directory of the Sorcer environment.
      *
      * @return a path of the home directory
      */
     public static File getHomeDir() {
         if (sorcerEnv.getSorcerHome() != null)
             return new File(sorcerEnv.getSorcerHome());
         else
             return null;
     }
 
 
     public static File getExtDir() {
         return sorcerEnv.getSorcerExtDir();
     }
 
     public static String getRepoDir() {
         return getEnvProperties().getProperty(S_SORCER_REPO);
     }
 
     public static String getLibPath() {
         return new File(getHomeDir(), "lib").getPath();
     }
 
     /**
      * @return Set Sorcer Local jar repo location
      */
     public Properties setRepoDir(Properties props) {
         String repo = props.getProperty(S_SORCER_REPO);
         if (repo != null) repo = repo.trim();
 
         if (repo != null && !repo.isEmpty()) {
             try {
                 File repoDir = new File(repo);
                 if (repoDir.exists() && repoDir.isDirectory())
                     props.put(S_SORCER_REPO, repo);
             } catch (Throwable t) {
                 logger.throwing(
                         SorcerEnv.class.getName(),
                         "The given Sorcer Jar Repo Location: " + repo + " does not exist or is not a directory!",
                         t);
             }
         } else {
 
             // Fall back to default location in user's home/.m2
             try {
                 File repoDir = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
                 if (repoDir.exists() && repoDir.isDirectory())
                     props.put(S_SORCER_REPO, repoDir.getAbsolutePath());
                 else {
                     logger.info("Creaing missing Repo Dir default location: " + repoDir.getAbsolutePath());
                     if (!repoDir.mkdirs())
                         logger.severe("Problem creating Repo Dir default location: " + repoDir.getAbsolutePath());
                     props.put(S_SORCER_REPO, repoDir.getAbsolutePath());
                 }
                 logger.fine("Setting Repo Dir default location: " + repoDir.getAbsolutePath());
             } catch (Throwable t) {
                 logger.throwing(
                         SorcerEnv.class.getName(),
                         "The given Sorcer Jar Repo Location: " + repo + " does not exist or is not a directory!",
                         t);
             }
         }
         return props;
     }
 
     public static Properties loadProperties(InputStream inputStream)
             throws ConfigurationException {
         Properties properties = new Properties();
         try {
             properties.load(inputStream);
         } catch (IOException e) {
             throw new ConfigurationException(e);
         }
         sorcerEnv.reconcileProperties(properties);
         return properties;
     }
 
     public static URL getCodebase(URL root, String jar) {
         try {
             return new URL(root, jar);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     /**
      * Helper method adding root to each of the jars, and joining the result with spaces
      *
      * @param root
      * @param jars
      * @return
      */
     public static String getCodebase(URL root, String[] jars) {
         Collection<String> cb = new ArrayList<String>(jars.length);
         for (String jar : jars) {
             cb.add(getCodebase(root, jar).toExternalForm());
         }
         return StringUtils.join(cb, CODEBASE_SEPARATOR);
     }
 
     /**
      * Appends properties from the parameter properties <code>properties</code>
      * and makes them available as the global SORCER environment properties.
      *
      * @param properties the additional properties used to update the
      *                   <code>Sorcer<code>
      *                   properties
      */
     public static void appendProperties(Properties properties) {
         getEnvProperties().putAll(properties);
     }
 
     /**
      * Expands properties embedded in a string with ${some.property.name}. Also
      * treats ${/} as ${file.separator}.
      */
     public static String expandStringProperties(String value,
                                                 boolean isSystemProperty, Properties props) throws ConfigurationException {
         int p = value.indexOf("${", 0);
         if (p == -1) {
             return value;
         }
         int max = value.length();
         StringBuffer sb = new StringBuffer(max);
         int i = 0; /* Index of last character we copied */
         while (true) {
             if (p > i) {
                 /* Copy in anything before the special stuff */
                 sb.append(value.substring(i, p));
                 i = p;
             }
             int pe = value.indexOf('}', p + 2);
             if (pe == -1) {
                 /* No matching '}' found, just add in as normal text */
                 sb.append(value.substring(p, max));
                 break;
             }
             String prop = value.substring(p + 2, pe);
             if (prop.equals("/")) {
                 sb.append(File.separatorChar);
             } else {
                 try {
                     String val = null;
                     if (isSystemProperty) {
                         val = prop.length() == 0 ? null : System
                                 .getProperty(prop);
                         if (val == null) {
                             // try System env
                             val = prop.length() == 0 ? null : System
                                     .getenv(prop);
                         }
                     } else {
                         val = prop.length() == 0 ? null : props
                                 .getProperty(prop);
                     }
                     if (val != null) {
                         sb.append(val);
                     } else {
                         return null;
                     }
                 } catch (SecurityException e) {
                     throw new ConfigurationException(e);
                 }
             }
             i = pe + 1;
             p = value.indexOf("${", i);
             if (p == -1) {
 				/* No more to expand -- copy in any extra. */
                 if (i < max) {
                     sb.append(value.substring(i, max));
                 }
                 break;
             }
         }
         return sb.toString();
     }
 
     /**
      * Return the local host address using
      * <code>java.net.InetAddress.getLocalHost().getHostAddress()</code>
      *
      * @return The local host address
      * @throws java.net.UnknownHostException if no IP address for the local host could be found.
      */
     public static String getHostAddress() throws java.net.UnknownHostException {
         return getLocalHost().getHostAddress();
     }
 
     /**
      * Return the local host address for a passed in host using
      * {@link java.net.InetAddress#getByName(String)}
      *
      * @param name The name of the host to return
      * @return The local host address
      * @throws java.net.UnknownHostException if no IP address for the host name could be found.
      */
     public static String getHostAddress(String name)
             throws java.net.UnknownHostException {
         return java.net.InetAddress.getByName(name).getHostAddress();
     }
 
     /**
      * Return the local host name
      * <code>java.net.InetAddress.getLocalHost().getHostName()</code>
      *
      * @return The local host name
      * @throws java.net.UnknownHostException if no hostname for the local host could be found.
      */
     public static String getHostName() throws java.net.UnknownHostException {
         return getLocalHost().getCanonicalHostName();
         // return java.net.InetAddress.getLocalHost().getHostName();
     }
 
     public static InetAddress getLocalHost() throws UnknownHostException {
         return HostUtil.getInetAddress();
     }
 
     public static String[] getWebsterRoots() {
         String websterRootStr = System.getProperty(WEBSTER_ROOTS, getRepoDir());
         return websterRootStr.split(";");
     }
 
     /**
      * Return the local host address based on the value of a system property.
      * using {@link java.net.InetAddress#getByName(String)}. If the system
      * property is not resolvable, return the default host address obtained from
      * {@link java.net.InetAddress#getLocalHost()}
      *
      * @param property The property name to use
      * @return The local host address
      * @throws java.net.UnknownHostException if no IP address for the host name could be found.
      */
     public static String getHostAddressFromProperty(String property)
             throws java.net.UnknownHostException {
         String host = getHostAddress();
         String value = System.getProperty(property);
         if (value != null) {
             host = java.net.InetAddress.getByName(value).getHostAddress();
         }
         return (host);
     }
 
     /**
      * <p>
      * Return <code>true</code> if the SORCER sorcer.provider.boot.Booter
      * is used, otherwise <code>false</code>,
      * </p>
      *
      * @return the bootable
      */
     public static boolean isBootable() {
         return sorcerEnv.bootable;
     }
 
     /**
      * <p>
      * Assigns <code>true</code> by the sorcer.boot.provider.Booter when
      * used.
      * </p>
      *
      * @param bootable the bootable to set
      */
     public static void setBootable(boolean bootable) {
         sorcerEnv.bootable = bootable;
     }
 
     public static String getSorcerVersion() {
         return getProperty(S_VERSION_SORCER, SORCER_VERSION);
     }
 
     public static URL getCodebaseRoot() {
         try {
             // TODO: allow to override hostAddress from sorcer.env
             return getCodebaseRoot(getHostAddress(), sorcerEnv.getWebsterPortProperty());
         } catch (UnknownHostException e) {
             throw new IllegalStateException("Could not obtain local address", e);
         }
     }
 
     /**
      * Prepare codebase root URL - URL with only hostname and port
      *
      * @param address
      * @param port
      * @return
      */
     public static URL getCodebaseRoot(String address, int port) {
         try {
             return new URL("http", address, port, "/");
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("Could not prepare codebase root URL", e);
         }
     }
 
     public static String getWebsterInterface() {
         return sorcerEnv.getProperty(P_WEBSTER_INTERFACE);
     }
 
     /**
      * Collects all the properties from sorcer.env, related properties from a
      * provider properties file, provider Jini configuration file, and JVM
      * system properties.
      */
 
     public void setHomeDir() {
         String hd = System.getenv("SORCER_HOME");
 
         if (hd != null && hd.length() > 0) {
             try {
                 hd = new File(hd).getCanonicalPath();
                 this.setSorcerHome(hd);
                 System.setProperty(SORCER_HOME, hd);
                 return;
             } catch (IOException io) {
             }
         }
         hd = System.getProperty(SORCER_HOME);
         if (hd != null && hd.length() > 0) {
             try {
                 hd = new File(hd).getCanonicalPath();
                 this.setSorcerHome(hd);
                 return;
             } catch (IOException io) {
             }
         }
         throw new IllegalArgumentException(hd
                 + " is not a valid 'sorcer.home' directory");
     }
 
     /**
      * Loads the environment properties from the default filename (sorcer.env)
      * or as given by the system property <code>sorcer.env.file</code> and
      * service context types from the default filename (node.types) or the
      * system property <code>sorcer.formats.file</code>.
      */
     private void loadBasicEnvironment() {
         // Try and load from path given in system properties
         String cntFile = null;
         String envFile = null;
         Properties sorcerEnvProps = null;
 
         setHomeDir();
         try {
             envFile = System.getProperty(S_KEY_SORCER_ENV);
             envFrom = "(default)";
             String cdtFrom = "(default)";
 
             if (envFile != null) {
                 load(envFile, "(system property)");
             } else {
                 envFile = S_ENV_FIENAME;
                 String envPath = getSorcerHome() + "/configs/" + envFile;
                 System.setProperty(S_KEY_SORCER_ENV, envPath);
                 sorcerEnvProps = loadProperties(envFile);
                 update(sorcerEnvProps);
                 properties.putAll(sorcerEnvProps);
                 envFrom = "(Sorcer resource)";
             }
             logger.fine("SORCER env properties:\n"
                     + GenericUtil.getPropertiesString(properties));
 
             cntFile = System.getProperty("sorcer.formats.file");
 
             updateCodebase();
             logger.finer("Sorcer codebase: "
                     + System.getProperty("java.rmi.server.codebase"));
 
             if (cntFile != null) {
                 loadDataNodeTypes(cntFile);
                 cdtFrom = "(system property)";
             } else {
                 cntFile = CONTEXT_DATA_FORMATS;
                 loadDataNodeTypes(cntFile);
                 envFrom = "(Sorcer resource)";
             }
             logger.finer("Sorcer loaded " + envFrom + " properties " + envFile);
 
             logger.finer("Data formats loaded " + cdtFrom + " from: "
                     + CONTEXT_DATA_FORMATS);
 
             logger.finer("* Sorcer provider accessor:"
                     + properties.getProperty(S_SERVICE_ACCESSOR_PROVIDER_NAME));
             // Repo directory - setting
             properties = setRepoDir(properties);
             prepareWebsterInterface(properties);
         } catch (Throwable t) {
             logger.throwing(
                     SorcerEnv.class.getName(),
                     "Unable to find/load SORCER environment configuration files",
                     t);
         }
     }
 
     private void load(String envFile, String sourceDesc) throws IOException, ConfigurationException {
         properties.load(new FileInputStream(envFile));
         envFrom = sourceDesc;
         update(properties);
         loadedEnvFile = envFile;
     }
 
     // STATIC
 
     /**
      * Tries to load properties from a <code>filename</code>, first in a local
      * directory. If there is no file in the local directory then load the file
      * from the classpath at sorcer/util/sorcer.env.
      *
      * @throws IOException
      * @throws ConfigurationException
      */
     public Properties loadProperties(String filename)
             throws ConfigurationException {
         Properties props = new Properties();
         try {
             // Try in user home directory first
             properties.load((new FileInputStream(new File(filename))));
             logger.fine("loaded properties from: " + filename);
 
         } catch (Exception e) {
             try {
                 // try to look for sorcer.env in SORCER_HOME/configs
                 String home = System.getProperty(SORCER_HOME, System.getenv(E_SORCER_HOME));
                 File file = new File(home + "/configs/" + filename);
                 props.load((new FileInputStream(file)));
                 logger.fine("loaded properties from: " + file);
             } catch (Exception ee) {
                 try {
                     // No file give, try as resource sorcer/util/sorcer.env
                     InputStream stream = SorcerEnv.class.getResourceAsStream(filename);
                     if (stream != null)
                         props.load(stream);
                     else
                         logger.severe("could not load properties as Sorcer resource file>"
                                 + filename + "<");
                 } catch (Throwable t2) {
                     throw new ConfigurationException(e);
                 }
             }
         }
         reconcileProperties(props);
         return props;
     }
 
     private void reconcileProperties(Properties props)
             throws ConfigurationException {
 
         update(props);
 
         // set the document root for HTTP server either for provider or
         // requestor
 
         String rootDir, dataDir;
         rootDir = props.getProperty(P_DATA_ROOT_DIR);
         dataDir = props.getProperty(P_DATA_DIR);
         if (rootDir != null && dataDir != null) {
             System.setProperty(DOC_ROOT_DIR, rootDir + File.separator + dataDir);
         } else {
             rootDir = props.getProperty(R_DATA_ROOT_DIR);
             dataDir = props.getProperty(R_DATA_DIR);
             if (rootDir != null && dataDir != null) {
                 System.setProperty(DOC_ROOT_DIR, rootDir + File.separator
                         + dataDir);
             }
         }
         dataDir = props.getProperty(P_SCRATCH_DIR);
         if (dataDir != null) {
             System.setProperty(SCRATCH_DIR, dataDir);
         } else {
             dataDir = props.getProperty(R_SCRATCH_DIR);
             if (dataDir != null) {
                 System.setProperty(SCRATCH_DIR, dataDir);
             }
         }
 
         String httpInterface = null, httpPort = null;
         httpInterface = props.getProperty(P_DATA_SERVER_INTERFACE);
         httpPort = props.getProperty(P_DATA_SERVER_PORT);
         if (httpInterface != null) {
             System.setProperty(DATA_SERVER_INTERFACE, httpInterface);
             System.setProperty(DATA_SERVER_PORT, httpPort);
         } else {
             httpInterface = props.getProperty(R_DATA_SERVER_INTERFACE);
             httpPort = props.getProperty(R_DATA_SERVER_PORT);
             if (httpInterface != null) {
                 System.setProperty(DATA_SERVER_INTERFACE, httpInterface);
                 System.setProperty(DATA_SERVER_PORT, httpPort);
             }
         }
 		
 		SCRATCH_DIR_ORIG = System.getProperty(SCRATCH_DIR);
 
     }
 
     /**
      * Overwrites defined properties in sorcer.env (sorcer.home,
      * provider.webster.interface, provider.webster.port) with those defined as
      * JVM system properties.
      *
      * @param props
      * @throws ConfigurationException
      */
     private void update(Properties props)
             throws ConfigurationException {
         Enumeration<?> e = props.propertyNames();
         String key, value, evalue = null;
         String pattern = "${" + "localhost" + "}";
 //		String userDirPattern = "${user.home}";
         // first substitute for this localhost
         while (e.hasMoreElements()) {
             key = (String) e.nextElement();
             value = props.getProperty(key);
             if (value.equals(pattern)) {
                 try {
                     value = getHostAddress();
                 } catch (UnknownHostException ex) {
                     ex.printStackTrace();
                 }
                 props.put(key, value);
             }
 	/*		if (value.equals(userDirPattern)) {
 				value = System.getProperty("user.home");
 				properties.put(key, value);
 			}*/
         }
         // now substitute other entries accordingly
         e = props.propertyNames();
         while (e.hasMoreElements()) {
             key = (String) e.nextElement();
             value = props.getProperty(key);
             evalue = expandStringProperties(value, true, props);
             // try SORCER env properties
             if (evalue == null)
                 evalue = expandStringProperties(value, false, props);
             if (evalue != null)
                 props.put(key, evalue);
             if (value.equals(pattern)) {
                 try {
                     evalue = getHostAddress();
                 } catch (UnknownHostException e1) {
                     e1.printStackTrace();
                 }
                 props.put(key, evalue);
             }
         }
     }
 
     /**
      * Returns the hostname of a SORCER class server.
      *
      * @return a webster host name.
      */
     public String prepareWebsterInterface(Properties props) {
         String hn = System.getenv("SORCER_WEBSTER_INTERFACE");
 
         if (hn != null && hn.length() > 0) {
             return hn;
         }
 
         hn = System.getProperty(P_WEBSTER_INTERFACE);
         if (hn != null && hn.length() > 0) {
             return hn;
         }
 
         hn = props.getProperty(P_WEBSTER_INTERFACE);
         if (hn != null && hn.length() > 0) {
             return hn;
         }
 
         try {
             hn = getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             logger.severe("Cannot determine the webster hostname.");
         }
 
         return hn;
     }
 
     /**
      * Loads data node (value) types from the SORCER data store or file. Data
      * node types specify application types of data nodes in service contexts.
      * It is analogous to MIME types in SORCER. Each type has a format
      * 'cnt/application/format/modifiers' or in the association format
      * 'cnt|application|format|modifiers' when used with {@link sorcer.service.Context}.
      *
      * @param filename name of file containing service dataContext node type definitions.
      */
     private void loadDataNodeTypes(String filename) {
         try {
             // Try in local directory first
             properties.load((new FileInputStream(new File(filename))));
 
         } catch (Throwable t1) {
             try {
                 // try to look for sorcer.env in SORCER_HOME/configs
                 String home = System.getProperty(SORCER_HOME, System.getenv(E_SORCER_HOME));
                 File file = new File(home + "/configs/" + filename);
                 properties.load((new FileInputStream(file)));
                 logger.fine("loaded data nodes from: " + file);
             } catch (Exception e) {
                 try {
                     // Can not access "filename" give try as resource
                     // sorcer/util/data.formats
                     InputStream stream = SorcerEnv.class.getResourceAsStream(filename);
                     if (stream != null)
                         properties.load(stream);
                     else
                         logger.severe("could not load data node types from: "
                                 + filename);
                 } catch (Throwable t2) {
                     logger.severe("could not load data node types: \n"
                             + t2.getMessage());
                     logger.throwing(SorcerEnv.class.getName(), "loadDataNodeTypes", t2);
                 }
             }
 
         }
     }
 
     /**
      * Returns the hostname of a provider data server.
      *
      * @return a data server name.
      */
     public String getProviderDataServerInterface() {
         return System.getProperty(P_DATA_SERVER_INTERFACE);
     }
 
     /**
      * Returns the port of a provider data server.
      *
      * @return a data server port.
      */
     public String getProviderDataServerPort() {
         return System.getProperty(P_DATA_SERVER_PORT);
     }
 
     /**
      * Returns the provider's data root directory.
      *
      * @return a provider data root directory
      */
     public File getDataRootDir() {
         return new File(getProperty(P_DATA_ROOT_DIR));
     }
 
     private void overrideFromEnvironment(Map<String, String> env) {
         String portStr = env.get(E_WEBSTER_PORT);
         if (portStr != null && !portStr.isEmpty()) {
             setWebsterPortProperty(Integer.parseInt(portStr));
         }
         String sorcerHome = env.get(SORCER_HOME);
         if (sorcerHome != null)
             setSorcerHome(sorcerHome);
     }
 
     public boolean isWebsterInternal() {
         return Boolean.parseBoolean(properties.getProperty(SORCER_WEBSTER_INTERNAL, "false"));
     }
 
     public void setWebsterInternal(boolean internal) {
         properties.setProperty(SORCER_WEBSTER_INTERNAL, Boolean.toString(internal));
     }
 
     public String getRequestorWebsterCodebase() {
         return properties.getProperty(R_CODEBASE);
     }
 
     public void setRequestorWebsterCodebase(String codebase) {
         properties.setProperty(R_CODEBASE, codebase);
     }
 
     public int getWebsterPortProperty() {
         return Integer.parseInt(properties.getProperty(P_WEBSTER_PORT, "0"));
     }
 
     public void setWebsterPortProperty(int port) {
         properties.setProperty(P_WEBSTER_PORT, Integer.toString(port));
     }
 
     public String getWebsterRootsString() {
         return properties.getProperty(WEBSTER_ROOTS);
     }
 
     public void setWebsterRootsString(String roots) {
         properties.setProperty(WEBSTER_ROOTS, roots);
     }
 
     public String getSorcerHome() {
         return properties.getProperty(SORCER_HOME);
     }
 
     public void setSorcerHome(String sorcerHome) {
         properties.setProperty(SORCER_HOME, sorcerHome);
     }
 
     /**
      * copy all entries from provided map to SorcerEnv properties
      */
     public void  load(Map<String, String> props){
         properties.putAll(props);
     }
 
     public static SorcerEnv load(String fileName){
         SorcerEnv result = new SorcerEnv();
 
         try {
             result.load(fileName,"user file");
         } catch (IOException e) {
             throw new IllegalArgumentException("Could not load config from "+fileName);
         } catch (ConfigurationException e) {
             throw new IllegalArgumentException("Could not load config from "+fileName);
         }
         sorcerEnv.overrideFromEnvironment(System.getenv());
         return result;
     }
 
     public static void setSorcerEnv(SorcerEnv sorcerEnv){
         SorcerEnv.sorcerEnv = sorcerEnv;
     }
 
     public String getSorcerExt() {
         String sorcerExt = System.getenv(E_SORCER_EXT);
         if (sorcerExt == null || "".equals(sorcerExt)) {
             sorcerExt = getSorcerHome();
         }
         return sorcerExt;
     }
 
     public File getSorcerExtDir() {
         return new File(getSorcerExt());
     }
 
     public LookupLocators getLookupLocatorsHolder(){
         return lookupLocators;
     }
 
     private static List<String> websterRoots = Arrays.asList(
             SorcerEnv.getRepoDir(),
             SorcerEnv.getLibPath(),
             new File(SorcerEnv.getHomeDir(), "deploy").getPath()
     );
 
     static {
         String scratch = System.getProperty(SCRATCH_DIR);
         if (scratch != null) {
             List<String> oldWr = websterRoots;
             websterRoots = new ArrayList<String>(oldWr.size() + 1);
             websterRoots.addAll(oldWr);
             websterRoots.add(scratch);
         }
     }
 
     public static String[] getWebsterRoots(String[] additional) {
         String[] result = new String[websterRoots.size() + additional.length];
         addAll(websterRoots, result);
         System.arraycopy(additional, 0, result, websterRoots.size(), additional.length);
         return result;
     }
     private static <T> void addAll(List<T> src, T[] target) {
         int i = 0;
         for (T o : src) {
             target[i] = o;
             ++i;
         }
     }
 }
