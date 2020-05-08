 package com.alexrnl.commons.utils;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 
 /**
  * Class access the application configuration.<br />
  * To retrieve a property, the method {@link #get(String)} should be used.
  * The default configuration file used will be <code>conf/configuration.xml</code>, it may be
  * changed using the {@link #setFile(String)} method. To avoid any warning, use this method before
  * calling any {@link #getInstance()}.
  * The {@link Configuration} is thread-safe as it is based on the {@link Properties} class, itself
  * extending a {@link Hashtable}.
  * @author Alex
  */
 public final class Configuration {
 	/** Logger */
	private static Logger			lg	= Logger.getLogger(Configuration.class.getName());
 	
 	/** Unique instance of the class */
	private static Configuration	singleton = new Configuration();
 	/** The configuration file to load */
	private static String			configurationFile = "conf/configuration.xml";
 	
 	/** The configuration properties */
 	private final Properties		configuration;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Private default constructor, load the configuration properties from the configuration file.
 	 */
 	private Configuration () {
 		super();
 		configuration = new Properties();
 		load();
 	}
 	
 	/**
 	 * Return the unique instance of the singleton.<br />
 	 * Build the properties which will load the configuration.
 	 * @return the instance of the configuration.
 	 */
 	public static Configuration getInstance () {
 		return singleton;
 	}
 	
 	/**
 	 * Set the configuration file to load.
 	 * @param filePath
 	 *        the path to the configuration file to load.
 	 */
 	public static void setFile (final String filePath) {
 		configurationFile = filePath;
 		if (singleton == null) {
 			getInstance();
 		} else {
 			getInstance().load();
 		}
 	}
 	
 	/**
 	 * Load the properties from the configuration file.
 	 */
 	private void load () {
 		// Load the properties
 		try {
 			configuration.clear();
 			configuration.loadFromXML(new FileInputStream(configurationFile));
 			if (lg.isLoggable(Level.INFO)) {
 				lg.info("Properties loaded from file: " + configurationFile);
 			}
 			if (lg.isLoggable(Level.FINE)) {
 				lg.fine("Properties successfully loaded:");
 				for (final Entry<Object, Object> currentProp : configuration.entrySet()) {
 					lg.fine("\t" + currentProp.getKey() + " = " + currentProp.getValue());
 				}
 			}
 		} catch (final IOException e) {
 			lg.warning("Exception while loading configuration (" + ExceptionUtils.display(e) + ")");
 		}
 	}
 	
 	/**
 	 * Get the property matching to the property name specified.
 	 * @param propertyName
 	 *        the name of the property to retrieve.
 	 * @return the value of the property.
 	 */
 	public String get (final String propertyName) {
 		final String propertyValue = configuration.getProperty(propertyName);
 		if (propertyValue != null) {
 			lg.warning("No property with name " + propertyName);
 			return null;
 		}
 		return propertyValue;
 	}
 	
 	/**
 	 * Return the number of properties held by the configuration file.
 	 * @return the number of properties.
 	 */
 	public int size () {
 		return configuration.size();
 	}
 }
