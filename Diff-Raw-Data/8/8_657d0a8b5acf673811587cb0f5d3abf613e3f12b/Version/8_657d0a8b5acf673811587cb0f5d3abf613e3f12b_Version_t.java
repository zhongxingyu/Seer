 /*
  * Sep 13, 2003
  */
 package com.thinkparity.codebase;
 
 import com.thinkparity.codebase.config.Config;
 import com.thinkparity.codebase.config.ConfigFactory;
 
 /**
 * The version class is used to obtain name\version info from the codebase
 * library.
 * 
  * @author raykroeker@gmail.com
 * @version 1.2.2.4
  */
 public abstract class Version {
 
 	/**
 	 * Handle to the version config file
 	 * @see Version#configLock
 	 */
 	private static final Config config;
 
 	/**
 	 * Synchronization lock for config.
 	 * @see Version#config
 	 */
 	private static final Object configLock;
 
 	static {
 		config = ConfigFactory.newInstance(Version.class);
 		configLock = new Object();
 	}
 
 	/**
 	 * Obtain the build id of the codebase.
 	 * 
 	 * @return The build id.
 	 */
 	public static String getBuildId() { return config.getProperty("buildId"); }
 
 	/**
 	 * Obtain the name of the codebase.
 	 * 
 	 * @return The name.
 	 */
 	public static String getName() { return config.getProperty("name"); }
 
 	/**
 	 * Obtain the version of the codebase.
 	 * 
 	 * @return The version
 	 */
 	public static String getVersion() {return config.getProperty("version"); }
 }
