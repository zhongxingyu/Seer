 package models;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.RollingFileAppender;
 
 import play.Logger;
 import play.Play;
 import play.libs.Crypto;
 import play.libs.IO;
 import play.libs.Time;
 import play.mvc.Router;
 import util.Check;
 import util.ReloadableSingletonFile;
 import util.Utils;
 import exception.QuickException;
 
 
 /**
  * Application properties accessible throught the user interface 
  * 
  * @author Paolo Di Tommaso
  *
  */
 
 public class AppProps implements Serializable  {
 
 	Properties properties;
 
 	List<String> changed = new ArrayList<String>();
 	List<String> removed = new ArrayList<String>();
 	
 	public String contextPath;
 	
 	
 	static File getWorkPath( final String propertyName, final String defValue ) { 
 		final String path = Play.configuration.getProperty(propertyName, defValue);
 		
 		/* when an absolute file name is specified just use it */
 		if( path.startsWith(File.separator) ) { 
 			return new File(path);
 		}
 		
 		/* try to find it out on the data folder */
 		File result = new File(WORKSPACE_FOLDER, path);
 		if( result.exists() ) { 
 			return result;
 		}
  
 		/* otherwise fallback on the application root */
 		result = new File(Play.applicationPath,path);
 		
 		if( !result.exists() ) { 
 			Logger.info("Creating path '%s'",result);
 			if( !result.mkdirs() ) { 
 				throw new QuickException("Unable to create path '%s'", result);
 			}
 		}
 		
 		return result;		
 	}
 	
 	/**
 	 * The application main <i>data</i> folder. 
 	 */
 	public static final File WORKSPACE_FOLDER;
 
 	/** 
 	 * The path where application bundles are located 
 	 */
 	public static final File BUNDLES_FOLDER;
 	
 	/**
 	 * Where temporaries files are stored 
 	 */
 	public static final File TEMP_PATH;
 	
 	/** 
 	 * Location of the file containing server specific configuration
 	 */
 	public static final File SERVER_PROPS_FILE;
 
 	/**
 	 * Location of server log file 
 	 */
 	public static final File SERVER_APPLOG_FILE;
 	
 	public static final File VALIDATION_LOG_FILE;
 
 	
 	
 	/* singleton instance */
 	final private static ReloadableSingletonFile<AppProps> INSTANCE;
 	
 	static {
 		
     	/*
     	 * 1. local data workspace 
     	 * It could be specified by the property 'settings.workspace.path'
     	 * - or - if it is missing it wiull be used the path {application.path}/data
     	 */
 		String sPath = Play.configuration.getProperty("settings.workspace.path");   
 		WORKSPACE_FOLDER = Utils.isNotEmpty(sPath) 
 				    ? new File(sPath)
 		 			: new File(Play.applicationPath,"data");
 	
 		if( !WORKSPACE_FOLDER.exists() ) {
 			Logger.warn("Creating Workspace folder: '%s'", WORKSPACE_FOLDER);
 			// try to create it and raise anc exception if it fails 
 			if( !WORKSPACE_FOLDER.mkdirs() ) { 
 				throw new QuickException("Unable to create workspace folder: '%s' ", WORKSPACE_FOLDER);
 			}
 		}
 		else { 
 			Logger.info("Using Workspace folder: '%s'", WORKSPACE_FOLDER);
 		}
 		
 		/*
 		 * 2. define the properties file 
 		 */
 		final String propsFileName = Play.configuration.getProperty("settings.properties.file", "tserver.properties");
 		
 		/* when an absolute file name is specified just use it */
 		SERVER_PROPS_FILE = propsFileName.startsWith(File.separator)  
 				? new File(propsFileName)
 				: new File(WORKSPACE_FOLDER, propsFileName);
 
 		Logger.info("Using Application properties file: '%s'",SERVER_PROPS_FILE );
 		
 		/*
 		 * 3. define application log file 
 		 */
 		SERVER_APPLOG_FILE = getApplicationLogFile();
 		Logger.info("Using Application log file: '%s'", SERVER_APPLOG_FILE);
 		
 		
 		/*
 		 * 4. bundles path 
 		 */
 		BUNDLES_FOLDER = getWorkPath("settings.bundles.path", "bundles");
 		Logger.info("Using Bundles path: %s", BUNDLES_FOLDER);
 
 		/* create temporary path */
 		TEMP_PATH = getWorkPath("settings.temp.path", ".temp");
 		Logger.info("Using Temp path: %s", TEMP_PATH);
 		
 		/*
 		 * 5. validation log file name
 		 */
 		final String validationLogFile = Play.configuration.getProperty("settings.validation.log.file");
 		if( Utils.isNotEmpty(validationLogFile)) { 
 			VALIDATION_LOG_FILE = validationLogFile.startsWith(File.separator)  
 				? new File(validationLogFile)
 				: new File(WORKSPACE_FOLDER, validationLogFile);
 		}
 		else { 
 			VALIDATION_LOG_FILE = null;
 		}
 		Logger.info("Using Validation log file: %s", VALIDATION_LOG_FILE!=null ? VALIDATION_LOG_FILE : "(none)");
 		
 		
 		/*
 		 * 6. create the AppProps singleton
 		 */
 		INSTANCE = new ReloadableSingletonFile<AppProps>(SERVER_PROPS_FILE) {
 			
 			@Override
 			public AppProps readFile(File file) {
 				AppProps result = new AppProps();
 				if( file.exists() ) { 
 					result.load(file);
 				}
 				return result;
 			}
 		};
 
 
 	}
 
 	/*
 	 * Strategy: 
 	 * 1) look for an Log4j FileAppender 
 	 * 2) if it is found extract the appender file name 
 	 * 3) otherwise define a new file appender using the properties 
 	 * in the qpplication.conf
 	 */
 	private static File getApplicationLogFile() {
 
 		File logFile = null;
 		FileAppender appender = null;
 		
 		String logFileName = Play.configuration.getProperty("settings.log.file");
 		String pattern = Play.configuration.getProperty("settings.log.pattern", "%d{ISO8601} %-5p ~ %m%n");
 		String maxFileSize = Play.configuration.getProperty("settings.log.maxFileSize", "10MB");
 		String maxBackupIndex = Play.configuration.getProperty("settings.log.maxBackupIndex", "10");
 		
 		if( Play.mode.equals( Play.Mode.PROD ) ) { 
 			/*
 			 * try to discorver a FileAppender
 			 */
 			Enumeration iterator = Logger.log4j.getRootLogger().getAllAppenders();
 			while( iterator.hasMoreElements()  ) { 
 				Appender item = (Appender) iterator.nextElement();
 				if( item instanceof FileAppender ) { 
 					appender = (FileAppender)item;
 					break;
 				}
 			}
 		}
 
 		/* 
 		 * if a log4j file appender exists use to find out the log file name 
 		 */
 		if( appender != null ) { 
 			logFileName = appender.getFile();
 			logFile = new File(logFileName);
 			
 		}
 		else { 
 			/* 
 			 * otherwise use the specified file name in the properties or a new default one 
 			 */
 			if( Utils.isEmpty(logFileName ) ) { 
 				logFileName = "application.log";
 			}
 
 			/* if the specified file is a relative path, it will relative to application
 			 * workspace path */
 			logFile = logFileName.startsWith( File.separator )
 						? new File(logFileName)
 						: new File(WORKSPACE_FOLDER, logFileName);
 			
 
 			/* define a new log file appender */
 			RollingFileAppender rolling = new RollingFileAppender();
 			rolling.setFile(logFile.getAbsolutePath());
 			rolling.setLayout(new PatternLayout(pattern));
 			rolling.setAppend(true);
 			rolling.setMaxFileSize(maxFileSize);
 			rolling.setMaxBackupIndex(Integer.parseInt(maxBackupIndex));
 			rolling.activateOptions();
 			Logger.log4j.addAppender(rolling);
 		}
 
 		return logFile;
 	}
 
 	
 	/** 
 	 * This constructur will create an instance of current Play configuration object,
 	 * containg oly the properties prefixed with the current application id
 	 * 
 	 */
 	public AppProps() {
 		this.properties = new Properties();
 		
     	String id = Play.id;
     	Logger.info("Intercepting conf properties for '%s'", id);
 
     	Properties playConf = IO.readUtf8Properties( Play.confs.iterator().next().inputstream() );
        Pattern pattern = Pattern.compile("^%"+id+"\\.(.*)$");
 
         for (Object key : playConf.keySet()) {
             Matcher matcher = pattern.matcher(key.toString());
             if (matcher.matches()) {
             	String name = matcher.group(1);
             	String value = playConf.getProperty(key.toString());
             	Logger.debug("Retaining propery: %s=%s", name, value);
             	properties.put(name, value);
             }
         }
 	
 	}
 	
 	/**
 	 * Load the 
 	 * @param file
 	 */
 	public void load( File file ) { 
 		Logger.info("Loading server properties from file: '%s'", file);
 		Properties prop = new Properties();
 		try {
 			prop.load( new FileInputStream(file) );
 		} 
 		catch (IOException e) {
 			Logger.error("Unable to read properties file: '%s'", file);
 			return;
 		}
 		
 		for( Entry<Object, Object> entry : prop.entrySet() ) { 
 			Object key = entry.getKey();
 			Object val = entry.getValue();
 			Logger.debug("Loading property: %s=%s", key, val);
 			this.put(
 						key != null ? key.toString() : null, 
 						val != null ? val.toString() : null);
 		}
 	}
 	
 	/** 
 	 * The copy constructor 
 	 */
 	public AppProps( AppProps that ) {
 		this.properties = new Properties();
 		this.properties.putAll(that.properties);
 		this.changed = new ArrayList<String>(that.changed);
 		this.contextPath = that.contextPath;
 	}
 	
 	/** Singleton instance accessor */
 	public static AppProps instance() {
 		return INSTANCE.get();
 	} 
 	
 	/**
 	 * Save this properties in the server properties file
 	 * 
 	 * @see {@link #SERVER_PROPS_FILE} 
 	 */
 	public void save() {
 		try {
 			/* 
 			 * note save only the properties which name is in the 'changed' list
 			 */
 			Properties copy = new Properties();
 			for( String key : changed ) { 
 				copy.put(key, properties.get(key));
 			}
 			copy.store( new FileOutputStream(SERVER_PROPS_FILE), null);
 		} 
 		catch (IOException e) {
 			throw new QuickException(e, "Unable to save properties to file: '%s'", SERVER_PROPS_FILE);
 		}
 	}
 	
 
 	public String getWebmasterEmail() {
 		return getString("settings.webmaster");
 	}
 	
 	/**
 	 * The application data path. Statically defined cannot be overriden. 
 	 */
 	public String getDataPath() {
 		return WORKSPACE_FOLDER.getAbsolutePath();
 	}
 
 	public File getDataFolder() {
 		return WORKSPACE_FOLDER;
 	}
 
 	
 	/**
 	 * 
 	 * @return The max age (in secs) for which the request is stored in the file system
 	 */
 	public int getDataCacheDuration() {
 		int defValue = 10 * 24 * 60 * 60; // <-- by default 10 days
 		String duration = getString("data.cache.duration");
 		if( Utils.isEmpty(duration) ) { 
 			return defValue;
 		}
 		
 		try { 
 			return Time.parseDuration(duration);
 		}
 		catch( Exception e ) { 
 			Logger.warn(e, "Unable to parse duration string: '%s'", duration);
 			return defValue;
 		}
 	}
 	
 	public String getString(final String key) {
 		return getString(key, null);
 	}
 
 	/**
 	 * 
 	 * @see Time#parseDuration(String)
 	 * 
 	 * @param key
 	 * @return Parse a duration property and return the duration expressed as number of seconds 
 	 * 
 	 */
 	public Integer getDuration( final String key ) { 
 		return getDuration(key,null);
 	}
 	
 	public Integer getDuration( final String key, Integer defValue ) { 
 		String value = getString(key, null);
 		if( Utils.isEmpty(value) ) { 
 			return defValue;
 		}
 
 		try { 
 			return Time.parseDuration(value);
 		}
 		catch( IllegalArgumentException e ) { 
 			Logger.warn("Invalid duration value: '%s' for property: '%s'", value, key);
 			return defValue;
 		}
 	}
 	
 	/**
 	 * Just a synonim for {@link #put(String, String)}
 	 * @param name the property name 
 	 * @param value the property value 
 	 */
 	public void setProperty( String name, String value ) { 
 		put(name,value);
 	}
 	
 	public Integer getInteger(final String key) {
 		String value = getString(key, null);
 		if( Utils.isEmpty(value) ) { 
 			return null;
 		}
 		
 		return Integer.parseInt(value);
 	}
 	
 	public Integer getInteger( String key, int defValue ) {
 		String value = getString(key, null);
 		if( Utils.isEmpty(value) ) { 
 			return defValue;
 		}
 		
 		return Utils.parseInteger(value, defValue);
 	}
 
 	public Long getLong( String key, long defValue ) {
 		String value = getString(key, null);
 		if( Utils.isEmpty(value) ) { 
 			return defValue;
 		}
 		
 		return Utils.parseLong(value, defValue);
 	}
 	
 	public Long getLong( String key) {
 		String value = getString(key, null);
 		if( Utils.isEmpty(value) ) { 
 			return null;
 		}
 		
 		return Utils.parseLong(value, null);
 	}	
 	
 	public String getString(String key, final String defValue) {
 		Check.notNull(key, "Argument 'key' cannot be null");
 		if( properties == null ) return defValue;
 		
 		/*
 		 * when a value is wrapped by three brackets it is assumed to be encrypted, 
 		 * so it will be decrypted before to the returned 
 		 */
 		String value = properties.getProperty(key);
 		if( value != null && value.startsWith("{{{") && value.endsWith("}}}") ) { 
 			value = value.substring(3, value.length()-3);
 			value = Crypto.decryptAES(value);
 		}
 		return value != null ? value : defValue;
 	}
 
 	public List<String> getNames() {
 		return new ArrayList( properties.keySet() );
 	}
 	
 	public boolean remove( String key ) { 
 		Object val = properties.remove(key);
 		Play.configuration.remove(key);
 		
 		if( !removed.contains(key) ) { 
 			removed.add(key);
 		}
 
 		if( changed.contains(key) ) { 
 			changed.remove(key);
 		}
 	
 		return val != null;
 	}
 	
 	
 	public boolean containsKey( String key ) {
 		return properties.containsKey(key);
 	}
 
 	public void put(String key, String value) {
 		properties.put(key, value);
 		Play.configuration.put(key, value);
 		if( !changed.contains(key) ) { 
 			changed.add(key);
 		}
 	}
 
 	/*
 	 * TODO 
 	 * Take in consideration the 'http.path' conf param 
 	 * http://www.playframework.org/documentation/1.2.4/configuration#http.path
 	 */
 	public String getContextPath() { 
 		if( contextPath != null ) { 
 			return contextPath;
 		}
 		
 		String path = Router.reverse("Main.index").toString();
 
 		/* normalize the discovered context path */
 		if( path != null && path.equals("/") ) { 
 			Logger.info("Using ROOT Context path");
 			contextPath = "";
 			return contextPath;
 		}
 		
 		if( Utils.isEmpty(path) || !path.startsWith("/") ) {
 			Logger.warn("Invalid context path: '%s'", path);
 			return "";
 		}
 		
 		int p = path.substring(1).indexOf("/");
 		if( p != -1 ) { 
 			path = path.substring(0,p+1);
 			Logger.info("Detected application Context Path: '%s'", path);
 			contextPath = path;
 		}
 		else { 
 			contextPath = "";
 		}
 		return contextPath;
 	}
 
 
 	/**
 	 * @return the name of the hosting web server e.g. 'tcoffee.crg.cat'. 
 	 * It can be used to specified a non-stardard http port (e.g. localhost:9000)
 	 */
 	public String getHostName() {
 		return getString("settings.hostname");
 	}
 	
 }
