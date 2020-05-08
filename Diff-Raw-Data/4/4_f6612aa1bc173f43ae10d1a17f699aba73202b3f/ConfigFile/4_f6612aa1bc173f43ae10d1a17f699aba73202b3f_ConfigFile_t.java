 package model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Properties;
 import java.util.Scanner;
 
 public class ConfigFile {
 
   private static String configFileFullName; // = new String( System.getenv(
   // "TASK_SERVER_CONFIG_DIR" ) +
   // File.separator +
   // "config.txt") ;
   private Properties properties;
   private Logging logg;
   private String configLogID = "Configuration";
 
   public ConfigFile() {
     properties = new Properties();
     logg = Logging.getInstance();
   }
 
   public boolean setConfigFile(File fileObj) {
     configFileFullName = fileObj.getAbsolutePath();
     if (!fileObj.exists()) {
       return false;
     }
     System.out.println("config file " + fileObj.getAbsolutePath() + " exists.");
     return true;
   }
 
   public void loadProperties() throws Exception {
    try
    {
      FileInputStream in = new FileInputStream(ConfigFile.configFileFullName);
       properties.load(in);
       logg.globalLogging(configLogID, "Properties loaded from " + configFileFullName);
     } catch (Exception e) {
       System.out.println("Exception in ConfigFile loadProperties: " + e.getMessage());
       throw e;
     }
   }
 
   public Properties getProperties() {
     return properties;
   }
 
   public long getConfigurationTime() {
     String str = properties.getProperty("configTime");
     long configTime = 0;
     Scanner scnr = new Scanner(str);
     if (scnr.hasNextLong()) {
       configTime = scnr.nextLong();
     } else {
       logg.globalLogging(configLogID, "no configuration time");
     }
     return configTime;
   }
 
   public String getServerIP() {
     String str = properties.getProperty("serverIP");
     String refIP = null;
     Scanner scnr = new Scanner(str);
     if (scnr.hasNext()) {
       refIP = scnr.next();
       System.out.println(refIP);
     } else {
       System.out.println("No ServerIP specified in the config file");
       logg.globalLogging(configLogID, "no ServerIP specified");
     }
     return refIP;
   }
 
   public String getPortTeam1() {
     String str = properties.getProperty("portTeam1");
     String refPort = null;
     Scanner scnr = new Scanner(str);
     if (scnr.hasNext()) {
       refPort = scnr.next();
       System.out.println(refPort);
     } else {
       System.out.println("No Port specified in the config file");
       logg.globalLogging(configLogID, "no Port specified");
     }
 
     return refPort;
   }
 
   public String getPortTeam2() {
     String str = properties.getProperty("portTeam2");
     String refPort = null;
     Scanner scnr = new Scanner(str);
     if (scnr.hasNext()) {
       refPort = scnr.next();
       System.out.println(refPort);
     } else {
       System.out.println("No Port specified in the config file");
       logg.globalLogging(configLogID, "no Port specified");
     }
 
     return refPort;
   }
 
   public long getRunTime() {
     String str = properties.getProperty("runTime");
     long runTime = 0;
     Scanner scnr = new Scanner(str);
     if (scnr.hasNextLong()) {
       runTime = scnr.nextLong();
     } else {
       logg.globalLogging(configLogID, "no configuration time");
     }
     return runTime;
   }
 
   public String getPropertyByName(String key) {
     return properties.getProperty(key);
   }
 }
