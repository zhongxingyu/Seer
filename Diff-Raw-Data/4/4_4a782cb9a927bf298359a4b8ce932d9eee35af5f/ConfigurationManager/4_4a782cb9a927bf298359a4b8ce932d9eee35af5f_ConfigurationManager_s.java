 package edu.ncsu.csc573.project.common;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 public class ConfigurationManager {
 
     private static final double DEFAULT_THRESHOLD_MATCH_VALUE = 0.80;
 	private static final String DEFAULT_HOST_INTERFACE = "127.0.0.1";
 	private static final String CONFIGURATION_PROPERTIES = "configuration.properties";
     public static final int DEFAULT_SERVER_PORT = 9000;
     public static final int DEFAULT_TIME_OUT = 2;
     public static final long DEFAULT_CLI_TIME_OUT = 60000;
     public static final int DEFAULT_BACK_LOG_COUNT = 10;
     public static final int DEFAULT_FILE_TRANSFER_PORT = 9000;
    private static final String DEFAULT_DOWNLOAD_DIRECTORY = System.getProperty("user.home") + "/publish";
    private static final String DEFAULT_PUBLISH_DIRECTORY = System.getProperty("user.home") + "/downloads";
     private int serverPort;
     private int timeOut;
     private long cliTimeOut;
     private int backLogCount;
     private String trigramDataBase;
     private int fileTrasferPort;
     private File downloadDirectory;
     private File publishDirectory;
 	private double thresholdValue = 0.0;
     private static Properties config = null;
     private static ConfigurationManager confManager = null;
     private static Logger logger;
 
     private ConfigurationManager() {
     }
 
     public static ConfigurationManager getInstance() {
         logger = Logger.getLogger(ConfigurationManager.class);
 
         if (confManager == null) {
             config = new Properties();
             try {
                 config.load(ClassLoader.getSystemResourceAsStream(CONFIGURATION_PROPERTIES));
             } catch (IOException e) {
                 logger.error("Unable to load configuration file", e);
                 logger.info("Using default values");
             }
             confManager = new ConfigurationManager();
             File pubDir = new File(DEFAULT_PUBLISH_DIRECTORY);
             pubDir.mkdirs();
             File downDir = new File(DEFAULT_DOWNLOAD_DIRECTORY);
             downDir.mkdirs();
             try {
                 confManager.setDownloadDirectory(downDir);
                 confManager.setPublishDirectory(pubDir);
             } catch (Exception e) {
                 logger.error("Unable to set default values", e);
             }
         }
 
         return confManager;
     }
 
     private long getAsLong(String parameter, long default_value) {
         String timeOutPropertyVal = config.getProperty(parameter).trim();
         if (timeOutPropertyVal == null || timeOutPropertyVal.trim().equals("")) {
             logger.error("Unable to fetch " + parameter
                     + "for configuration file");
             logger.info("Using default value " + default_value + "for "
                     + parameter);
             return default_value;
         }
         try {
             return Long.parseLong(timeOutPropertyVal);
         } catch (Exception e) {
             logger.error("The given parameter " + parameter
                     + "is not a valid integer/long");
             logger.error("Please configure it as an integer value in configurations.xml file");
             return default_value;
         }
     }
 
     private int getAsInt(String parameter, int default_value) {
         String timeOutPropertyVal = config.getProperty(parameter).trim();
         if (timeOutPropertyVal == null || timeOutPropertyVal.trim().equals("")) {
             logger.error("Unable to fetch " + parameter
                     + "for configuration file");
             logger.info("Using default value " + default_value + "for "
                     + parameter);
             return default_value;
         }
         try {
             return Integer.parseInt(timeOutPropertyVal);
         } catch (Exception e) {
             logger.error("The given parameter " + parameter
                     + "is not a valid integer/long");
             logger.error("Please configure it as an integer value in configurations.xml file");
             return default_value;
         }
     }
 
     private String getAsString(String parameter, String default_value) {
         String timeOutPropertyVal = config.getProperty(parameter).trim();
         if (timeOutPropertyVal == null || timeOutPropertyVal.trim().equals("")) {
             logger.error("Unable to fetch " + parameter
                     + "for configuration file");
             logger.info("Using default value " + default_value + "for "
                     + parameter);
             return default_value;
         }
         return timeOutPropertyVal;
     }
 
     private double getAsDouble(String parameter, double default_value) {
     	String timeOutPropertyVal = config.getProperty(parameter).trim();
         if (timeOutPropertyVal == null || timeOutPropertyVal.trim().equals("")) {
             logger.error("Unable to fetch " + parameter
                     + "for configuration file");
             logger.info("Using default value " + default_value + "for "
                     + parameter);
             return default_value;
         }
         try {
             return Double.parseDouble(timeOutPropertyVal);
         } catch (Exception e) {
             logger.error("The given parameter " + parameter
                     + "is not a valid integer/long");
             logger.error("Please configure it as an integer value in configurations.xml file");
             return default_value;
         }
 	}
     
     private void setAsString(String paramater, String value) throws Exception {
         config.setProperty(paramater, value);
         URL url = ClassLoader.getSystemResource(CONFIGURATION_PROPERTIES);
         String configFileName = url.getFile();
         if (configFileName != null && configFileName.equalsIgnoreCase("")) {
             logger.error("Configuration file is missing (configuration.properties)");
             throw new Exception("Configuration file does not exist");
         }
         File configFile = new File(configFileName);
         FileWriter configWriter = new FileWriter(configFile);
         config.store(configWriter, null);
         if (configWriter != null) {
             configWriter.close();
         }
     }
 
     
     /*
     private void setAsLong(String parameter, long value) throws Exception {
     setAsString(parameter, String.valueOf(value));
     }
     
     private void setAsInt(String parameter, int value) throws Exception {
     setAsString(parameter, String.valueOf(value));
     }
      */
     public int getServerPort() {
         if (serverPort == 0) {
             serverPort = getAsInt("SERVER_LISTENING_PORT", DEFAULT_SERVER_PORT);
         }
         return serverPort;
     }
 
     public int getTimeOut() {
         if (timeOut == 0) {
             timeOut = getAsInt("CLIENT_SOCKET_TIMEOUT", DEFAULT_TIME_OUT);
         }
         return timeOut;
     }
 
     public long getCLITimeOut() {
         if (cliTimeOut == 0) {
             cliTimeOut = getAsLong("CLI_TIMEOUT", DEFAULT_CLI_TIME_OUT);
         }
         return cliTimeOut;
     }
 
     public int getBackLogCount() {
         if (backLogCount == 0) {
             backLogCount = getAsInt("BACK_LOG_COUNT", 10);
         }
         return backLogCount;
     }
 
     // get all the trigrams
     public String getTrigramDatabase() {
         if (trigramDataBase == null) {
             trigramDataBase = getAsString("TRIGRAM_DATABASE_FILE",
                     "trigram.txt");
         }
         return trigramDataBase;
     }
 
     public int getFileTransferPort() {
         if (fileTrasferPort == 0) {
             fileTrasferPort = getAsInt("FILE_TRANSFER_PORT",
                     DEFAULT_FILE_TRANSFER_PORT);
         }
         return fileTrasferPort;
     }
 
     public File getDownloadDirectory() {
         if (downloadDirectory == null) {
             downloadDirectory = new File(getAsString("DOWNLOAD_DIRECTORY",
                     DEFAULT_DOWNLOAD_DIRECTORY));
         }
         return downloadDirectory;
     }
 
     public File getPublishDirectory() {
         if (publishDirectory == null) {
             publishDirectory = new File(getAsString("PUBLISH_DIRECTORY",
                     DEFAULT_PUBLISH_DIRECTORY));
         }
         return publishDirectory;
     }
 
     public void setPublishDirectory(File value) throws Exception {
         setAsString("PUBLISH_DIRECTORY", value.getAbsolutePath());
         publishDirectory = value;
     }
 
     public void setDownloadDirectory(File value) throws Exception {
         setAsString("DOWNLOAD_DIRECTORY", value.getAbsolutePath());
         downloadDirectory = value;
     }
     
     public String getHostInterface() {
     	return getAsString("HOST_INTERFACE", DEFAULT_HOST_INTERFACE);
     }
     
     public double getThresholdValue() {
     	if(thresholdValue == 0.0) {
     		thresholdValue =  getAsDouble("MATCH_THRESHOLD", DEFAULT_THRESHOLD_MATCH_VALUE);
     	}
     	return thresholdValue;
     }
 
 	
 }
