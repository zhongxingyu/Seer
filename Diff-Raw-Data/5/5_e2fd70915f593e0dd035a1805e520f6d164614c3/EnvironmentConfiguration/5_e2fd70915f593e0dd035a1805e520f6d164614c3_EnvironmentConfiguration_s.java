 package com.gentics.cr.configuration;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.jcs.engine.control.CompositeCacheManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.gentics.cr.util.CRUtil;
 
 
 
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class EnvironmentConfiguration {
 	
 	private static final String LOGGER_FILE_PATH = "${com.gentics.portalnode.confpath}/nodelog.properties";
 	private static final String CACHE_FILE_PATH = "${com.gentics.portalnode.confpath}/cache.ccf";
 	
 	private static final String USE_PORTAL_CACHE_KEY = "com.gentics.cr.useportalcaches";
 	
 	private static Logger log = Logger.getLogger(EnvironmentConfiguration.class);
 	
 	/**
 	 * Load Environment Properties
 	 * 		- load logger properties for log4j
	 * 		- load chache properties for JCS
 	 */
 	public static void loadEnvironmentProperties()
 	{
 		loadLoggerPropperties();
 		loadCacheProperties();
 	}
 	
 	/**
 	 * Load Property file for Log4J
 	 */
 	public static void loadLoggerPropperties() {
 		Properties logprops = new Properties();
 		try {
 			String confpath = CRUtil.resolveSystemProperties(LOGGER_FILE_PATH);
 			logprops.load(new FileInputStream(confpath));
 			PropertyConfigurator.configure(logprops);
 		} catch (IOException e) {
 			log.error("Could not find nodelog.properties.");
 		}catch (NullPointerException e) {
 			log.error("Could not find nodelog.properties.");
 		}
 	}
 	
 	/**
	 * Load Property file for JCS chache
 	 */
 	public static void loadCacheProperties()
 	{
 		try {
 			//LOAD CACHE CONFIGURATION
 			String confpath = CRUtil.resolveSystemProperties(CACHE_FILE_PATH);
 			Properties cache_props = new Properties();
 			cache_props.load(new FileInputStream(confpath));
 			if(cache_props.containsKey(USE_PORTAL_CACHE_KEY) && Boolean.parseBoolean(cache_props.getProperty(USE_PORTAL_CACHE_KEY)))
 			{
 				log.debug("Will not initialize ContentConnector Cache - Using the cache configured by portalnode instead.");
 			}
 			else
 			{
 				CompositeCacheManager cManager = CompositeCacheManager.getUnconfiguredInstance();
 				cManager.configure(cache_props);
 			}
 		} catch(NullPointerException e){
 			log.error("Could not load cache configuration. Perhaps you are missing the file cache.ccf in "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/")+"!");
 		} catch (FileNotFoundException e) {
 			log.error("Could not load cache configuration. Perhaps you are missing the file cache.ccf in "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/")+"!");
 		} catch (IOException e) {
 			log.error("Could not load cache configuration. Perhaps you are missing the file cache.ccf in "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/")+"!");
 		}
 	}
 }
