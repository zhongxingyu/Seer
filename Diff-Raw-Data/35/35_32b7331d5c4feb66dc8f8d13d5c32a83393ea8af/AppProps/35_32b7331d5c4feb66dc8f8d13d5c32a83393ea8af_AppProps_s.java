 package models;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import play.Logger;
 import play.Play;
 import util.Check;
 import util.ReloadableSingletonFile;
 import util.Utils;
 import util.XStreamHelper;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 
 import exception.QuickException;
 
 
 /**
  * Application properties accessible throught the user interface 
  * 
  * @author Paolo Di Tommaso
  *
  */
 @XStreamAlias("props")
 public class AppProps implements Serializable  {
 
 	static File getConfPath() {
 		final String confProp = "app.properties.file";
 		File result;
 		String propsFileName = Play.configuration.getProperty(confProp,"tserver.properties.xml");
 		
 		if( propsFileName.startsWith( File.separator ) ) {
 			/* when an absolute path is specified just use it */
 			result = new File(propsFileName);
 		}
 		else {
 			/* try to find it on the data folder */
 			result = new File(WORKSPACE_FOLDER,propsFileName);
 			
 			/* if does not exist fallback on the application conf path */
 			if( !result .exists() ) {
 				File conf = new File(Play.applicationPath,"conf");
 				result = new File(conf,propsFileName);
 			}
 		}
 
 		if( result.exists() ) {
 			Logger.info("Detected %s file: %s", confProp, result);
 		}
 		else {
 			Logger.warn("Missing %s file: %s", confProp, result);
 		}
 		
 		return result;
 		
 	}
 	
 	
 	static File getWorkPath( final String propertyName, final String defaultLocation ) { 
 		final String path = Play.configuration.getProperty(propertyName, defaultLocation);
 		
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
 		
		if( !result.exists() && !result.mkdirs()) { 
			throw new QuickException("Unable to create path '%s'", result);
 		}
 		
 		return result;		
 	}
 	
 	/**
 	 * The application main <i>data</i> folder. It contains application 
 	 * properties <code>tserver.properties.xml></code>, tcoffee configuration file
 	 * <code>tserver.conf.xml</code> and application temporary data
 	 */
 	public static final File WORKSPACE_FOLDER;
 
 	/** The path where all application bundles are located */
 	public static final File BUNDLES_FOLDER;
 	
 	public static final File TEMP_PATH;
 	
 	static final Map<String,String> DEF_PROPS;
 
 	/** Reference the server properties file <code>tserver.properties.xml</code> */
 	public static final File SERVER_PROPS_FILE;
 
 	/** Reference the server properties file <code>tserver.conf.xml</code> */
 	//public static final File SERVER_CONF_FILE;
 
 	/** Alignment requests log file */
 	public static final File SERVER_LOG_FILE; 
 	
 	/* singleton instance */
 	final private static ReloadableSingletonFile<AppProps> INSTANCE;
 	
 	static {
 
     	/*
     	 * 1. local data workspace 
     	 * It could be specified by the property 'tserver.workspace.path'
     	 * - or - if it is missing it wiull be used the path {application.path}/data
     	 */
 		String path = Play.configuration.getProperty("tserver.workspace.path");   
 		WORKSPACE_FOLDER = Utils.isNotEmpty(path) 
 				    ? new File(path)
 		 			: new File(Play.applicationPath,"data");
 	
 		if( !WORKSPACE_FOLDER.exists() ) {
			Logger.warn("Application DATA root does not exists: '%s'", WORKSPACE_FOLDER);
 			// try to create it and raise anc exception if it fails 
 			if( !WORKSPACE_FOLDER.mkdirs() ) { 
				throw new QuickException("Unable to create appplication 'data' path: '%s' ", WORKSPACE_FOLDER);
 			}
 		}
 		
 		/*
 		 * 2. define the properties file 
 		 */
 		SERVER_PROPS_FILE = getConfPath();
 
 		/*
 		 * 3. log file name
 		 */
 		SERVER_LOG_FILE = new File(WORKSPACE_FOLDER,"tserver.log");
 		
 		/*
 		 * 4. bundles path 
 		 */
 		BUNDLES_FOLDER = getWorkPath("tserver.bundles.path", "bundles");
 		Logger.info("Using 'bundles' on path: %s", BUNDLES_FOLDER);
 
		/* create upload path */
 		TEMP_PATH = getWorkPath("tserver.temp.path", ".temp");
 		Logger.info("Using 'temp' path: %s", TEMP_PATH);
 		
 		
 		/*
 		 * 5. Define the other default folder that can be overriden at runtime
 		 */
		
 				
 		
 		DEF_PROPS = new HashMap<String,String>();
 		DEF_PROPS.put("requestDaysToLive", "7"); // = The max age (in days) for which the request is stored in the file system
 
 		/*
 		 * 5. create the AppProps singleton
 		 */
 		INSTANCE = new ReloadableSingletonFile<AppProps>(SERVER_PROPS_FILE) {
 			
 			@Override
 			public AppProps readFile(File file) {
 				return file.exists() ? super.readFile(file) : new AppProps();
 			}
 		};
 		
 		/*
 		 * 6. add 'tserver's properties from application.conf
 		 */
 		
 		
 		for( Object obj : Play.configuration.keySet() ) {
 			String key = (String)obj;
 			
 			if( key != null ) {
 				if( key.startsWith("mail.smtp.") || key.startsWith("tserver.")) {
 					/*
 					 * add all "mail.smtp.xxx" and "tserver.xxx" properties  
 					 */
 					addPropertyIfNotAlreadyExists(INSTANCE.get(), key);
 				}  
 			}
 		}
 
 
 	}
 
 	static boolean addPropertyIfNotAlreadyExists( final AppProps props, final String key ) {
 		String value = Play.configuration.getProperty(key);
 		
 		String name = key;
 		// remove "tserver." prefix
 		if( name.startsWith("tserver.")) {
 			name = name.substring("tserver.".length());
 		}
 		
 		// add to properties if not already exists and has a valid value 
 		if( Utils.isNotEmpty(value) && !props.containsKey(name) ) {
 			props.add(name, value);
 			return true;
 		}
 		
 		return false;
 	}
 				
 	
 	@XStreamImplicit(itemFieldName="property")
 	List<Property> properties;
 
 	/** lazy loadable accessor method */ 
 	protected List<Property> properties() {
 		if( properties == null ) {
 			properties = new ArrayList<Property>();
 		}
 		return properties;
 	}
 	
 	/** The default constructor */
 	public AppProps() {
 	}
 	
 	/** The copy constructor */
 	public AppProps( AppProps that ) {
 		this.properties = Utils.copy(that.properties);
 	}
 	
 	/** Factory method to create an istance from its xml representation */
 	static AppProps create(File file) {
 		return XStreamHelper.fromXML(SERVER_PROPS_FILE);
 	}
 	
 	/** Singleton instance accessor */
 	public static AppProps instance() {
 		return INSTANCE.get();
 	} 
 	
 	/**
 	 * Save this properties in the server properties <code>tserver.properties.xml</code>
 	 * 
 	 * @see {@link #SERVER_PROPS_FILE} 
 	 */
 	public void save() {
 		XStreamHelper.toXML(this, SERVER_PROPS_FILE);
 	}
 	
 
 	public String getWebmasterEmail() {
 		return getString("webmasterEmail");
 	}
 	
 	/**
 	 * The application data path. Statically defined cannot be overriden. 
 	 */
 	public String getDataPath() {
 		return Utils.getCanonicalPath(WORKSPACE_FOLDER);
 	}
 
 	public File getDataFolder() {
 		return WORKSPACE_FOLDER;
 	}
 	
 
 	
 	/**
 	 * 
 	 * @return The max age (in secs) for which the request is stored in the file system
 	 */
 	public int getRequestTimeToLive() {
 		Integer secs = getInteger("requestTimeToLive");
 		if( secs != null ) {
 			return secs;
 		}
 
 		/* fallback on 'requestDaysToLive' prop */
 		Integer days = getInteger("requestDaysToLive");
 		if( days != null ) {
 			return days * 24 * 60 * 60;
 		}
 
 		throw new QuickException("Missing request TTL property. Specificy a value for 'requestDaysToLive' or 'requestTimeToLive'");
 	}
 	
 	public String getString(final String key) {
 		return get(key, DEF_PROPS.get(key));
 	}
 
 	/**
 	 * Just a synonim for {@link #getString(String)} method
 	 * 
 	 * @param name the property unique key
 	 * @return the string value for the required property name
 	 */
 	public String getProperty(final String name) { 
 		return get(name, DEF_PROPS.get(name));
 	}
 	
 	/**
 	 * Just a synonim for {@link #put(String, String)}
 	 * @param name the property name 
 	 * @param value the property value 
 	 */
 	public void setProperty( String name, String value ) { 
 		put(name,value);
 	}
 	
 	Integer getInteger(final String key) {
 		String value = get(key, DEF_PROPS.get(key));
 		if( Utils.isEmpty(value) ) { 
 			return null;
 		}
 		
 		return Integer.parseInt(value);
 	}
 	
 	public String get(String key, final String defValue) {
 		Check.notNull(key, "Argument 'key' cannot be null");
 		if( properties == null ) return defValue;
 		
 		for( Property prop : properties ) {
 			if( key.equals(prop.getName())) {
 				return prop.getValue();
 			}
 		}
 		
 		return defValue;
 	}
 
 	public List<String> getNames() {
 
 		List<String> result = new ArrayList<String>();
 		
 		if( properties != null ) 
 		for( Property prop : properties ) {
 			result.add(prop.getName());
 		}
 		return result;
 	}
 	
 	public List<Property> list() {
 		return properties != null ? new ArrayList<Property>(properties) : Collections.<Property>emptyList();
 	}
 	
 	public int indexOf( String key ) {
 		if( properties == null ) return -1;
 
 		int i=0;
 		for( Property prop : properties ) {
 			if( key.equals(prop.getName())) {
 				return i;
 			}
 			i++;
 		}
 		return -1;
 	}
 	
 	public boolean containsKey( String key ) {
 		return indexOf(key) != -1;
 	}
 
 	public void put(String key, String value) {
 		put(new Property(key,value));
 	}
 	
 	public void put( Property property ) {
 		Check.notNull(property, "Argument 'property' cannot be null");
 		
 		int p = indexOf(property.getName());
 		if( p != -1 ) {
 			properties().remove(p);
 			properties().add(p, property);
 		}
 		else {
 			properties().add(property);
 		}
 		
 	}
 
 	public void add(String key, String value) {
 		properties().add(new Property(key, value));
 	}
 	
 	public boolean addIfNotExists(String key, String value) {
 
 		if( !containsKey(key) ) {
 			properties().add(new Property(key, value));
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Put all prorties in the specified instance to this instance. 
 	 * If a property with the same name exists in this instance it will 
 	 * be overriden.  
 	 * 
 	 * @param props the instance to copy from 
 	 */
 	public void putAll( AppProps props ) {
 		if( props == null ) return;
 		
 		for( Property current : props.list() ) {
 			this.put(current.name, current.value);
 		}
 	} 
 
 	/**
 	 * Put all prorties in the specified instance to this instance. 
 	 * 
 	 * @param props the instance to copy from 
 	 */
 	public void putOnlyIfNotExist( AppProps props ) {
 		if( props == null ) return;
 		
 		for( Property current : props.list() ) {
 			if( !containsKey(current.getName()) ) {
 				put(current.name, current.value);
 			}
 		}
 	} 
 	
 }
