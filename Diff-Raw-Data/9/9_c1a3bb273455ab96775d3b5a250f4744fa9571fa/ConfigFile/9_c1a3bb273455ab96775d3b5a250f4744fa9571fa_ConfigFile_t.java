 package model;
 
 import java.io.IOException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Properties;
 import java.util.Scanner;
 
 import controller.TimeKeeper;
 import model.Logging;
 
 public class ConfigFile
 {
 	private static String configFileFullName; // = new String( System.getenv( "TASK_SERVER_CONFIG_DIR" ) + File.separator + "config.txt") ;   
 	private Properties properties;
 	private Logging logg; 
 	private String configLogID= "Configuration";
 	private TimeKeeper timekeeper;
 	
 	public ConfigFile()
 	{
 		properties = new Properties();
 		logg = Logging.getInstance();
 	}
 	
 	public boolean setConfigFile(File fileObj)
 	{
 		configFileFullName = new String(fileObj.getAbsolutePath());
 		if (!fileObj.exists()) 
 			return false;
 		System.out.println("config file " + fileObj.getAbsolutePath() + " exists.");
 		return true;
 	}
 	
 	public void loadProperties() throws Exception
 	{
 		try
 		{			
 			FileInputStream in = new FileInputStream( ConfigFile.configFileFullName);
 			properties.load( in );
 			logg.LoggingFile(configLogID, "Properties loaded from " + configFileFullName);
 			in.close();			
 		} catch ( Exception e )
 		{
 			System.out.println( "Exception in ConfigFile loadProperties: " + e.getMessage() );
 			throw e;
 		}
		timekeeper = TimeKeeper.getInstance();
 		timekeeper.setConfigurationTimeInMinutes(getConfigurationTime());
 		timekeeper.setRunTimeInMinutes(getRunTime());
 		timekeeper.setMaximumTimeInMinutes();
 	}
 
 	public Properties getProperties()
 	{
 		return properties;
 	}
 	
 	private double getConfigurationTime() throws Exception
 	{
 		String str = properties.getProperty("configTime");
 		double d = 0.0;
 		Scanner scnr = new Scanner(str);
 		if (scnr.hasNextDouble()) {
 			d = scnr.nextDouble();
 		} 
 		else
 			System.out.println("No double in getRunTime");
 
 		return d;
 	}
 	
 	private double getRunTime() 
 	{
 		String str = properties.getProperty("runTime");
 		double d = 0.0;
 		Scanner scnr = new Scanner(str);
 		if (scnr.hasNextDouble()) {
 			d = scnr.nextDouble();
 		}
 		else 
 			System.out.println("No double in getRunTime");
 		
 		return  d;
 	}
 }
 
