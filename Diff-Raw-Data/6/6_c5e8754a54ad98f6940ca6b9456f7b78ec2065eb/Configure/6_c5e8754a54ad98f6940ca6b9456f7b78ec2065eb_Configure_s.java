 package com.eroi.migrate;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import com.eroi.migrate.misc.Closer;
 
 public class Configure {
 
 	public static final String PROPERTY_CONNECTION_URL = "connection.url";
 	public static final String PROPERTY_CONNECTION_DRIVER = "connection.driver";
 	public static final String PROPERTY_CONNECTION_USERNAME = "connection.username";
 	public static final String PROPERTY_CONNECTION_PASSWORD = "connection.password";
 	public static final String PROPERTY_CONNECTION_ARGUMENTS = "connection.arguments";
 	public static final String PROPERTY_MIGRATION_PACKAGE_NAME = "migration.package.name";
 	public static final String PROPERTY_MIGRATION_CLASSNAME_PREFIX = "migration.classname.prefix";
 	public static final String PROPERTY_MIGRATION_SEPARATOR = "migration.separator";
 	public static final String PROPERTY_MIGRATION_START_INDEX = "migration.start.index";
 	public static final String PROPERTY_DATABASE_VERSION_TABLE = "database.version.table";
 
 	public static final String DEFAULT_PROPERTIES_FILE = "migrate4j.properties";
 	public static final String DEFAULT_CLASSNAME_PREFIX = "Migration";
 	public static final String DEFAULT_SEPARATOR = "_";
 	public static final Integer DEFAULT_START_INDEX = new Integer(1);
 	public static final String DEFAULT_VERSION_TABLE = "version";
 	
 	public static final String VERSION_FIELD_NAME = "version";
 	
 	/* Configure with variables */
 	private static String url = null;
 	private static String driver = null;
 	private static String username = null;
 	private static String password = null;
 	private static String connectionArguments = null;
 	private static String packageName = null;
 	
 	/* Configure with preconstructed Connection */
 	private static Connection connection = null;
 	private static boolean ownConnection = false;
 	
 	/* Optional configuaration parameters */
 	private static String classprefix = DEFAULT_CLASSNAME_PREFIX;
 	private static String separator = DEFAULT_SEPARATOR;
 	private static Integer startIndex = DEFAULT_START_INDEX;
 	private static String versionTable = DEFAULT_VERSION_TABLE;
 	
 	public static final void configure() {
 		configure(DEFAULT_PROPERTIES_FILE);
 	}
 	
 	public static final void configure(String propertyFileName) {
 		Properties properties = null;
 		
 		if (propertyFileName == null) {
 			propertyFileName = DEFAULT_PROPERTIES_FILE;
 		}
 		
 		try {
 			InputStream in = Configure.class.getClassLoader().getResourceAsStream(propertyFileName);
 			if (in != null) {
 				properties = new Properties();
 				properties.load(in);
 				
 				//These should be null if not found
 				url = properties.getProperty(PROPERTY_CONNECTION_URL);
 				driver = properties.getProperty(PROPERTY_CONNECTION_DRIVER);
 				username = properties.getProperty(PROPERTY_CONNECTION_USERNAME);
 				password = properties.getProperty(PROPERTY_CONNECTION_PASSWORD);
 				packageName = properties.getProperty(PROPERTY_MIGRATION_PACKAGE_NAME);
 				
 				//These should be defaults if not found
 				setConnectionArguments(properties.getProperty(PROPERTY_CONNECTION_ARGUMENTS));
 				setClassprefix(properties.getProperty(PROPERTY_MIGRATION_CLASSNAME_PREFIX));
 				setSeparator(properties.getProperty(PROPERTY_MIGRATION_SEPARATOR));
 				setVersionTable(properties.getProperty(PROPERTY_DATABASE_VERSION_TABLE));
 				
 				Integer startIndex = new Integer(-1);
 				
 				try {
 					int i = Integer.parseInt(properties.getProperty(PROPERTY_MIGRATION_START_INDEX));
 					startIndex = new Integer(i);
 				} catch (Exception ignored) {}
 				
 				setStartIndex(startIndex);
 			}
 			
 		} catch (IOException e) {
 			throw new RuntimeException("Couldn't locate and load property file \"" + propertyFileName + "\"");
 		}
 	}
 	
 	public static void configure(Connection connection, String packageName) {
 		Configure.connection = connection;
 		Configure.packageName = packageName;
 		Configure.ownConnection = false;
 	}
 	
 	public static void configure(String url, 
 								 String driver, 
 								 String username, 
 								 String password, 
 								 String connectionArguments, 
 								 String packageName,
 								 String classprefix,
 								 String separator,
 								 Integer startIndex,
 								 String versionTable) {
 
 		//Required
 		Configure.url = url;
 		Configure.driver = driver;
 		Configure.username = username;
 		Configure.password = password;
 		Configure.packageName = packageName;
 		
 		//These should be defaults if not found
 		setConnectionArguments(connectionArguments);
 		setClassprefix(classprefix);
 		setStartIndex(startIndex);
 		setSeparator(separator);
 		setVersionTable(versionTable);
 				
 	}
 
 	public static void configure(String url, 
 			 String driver, 
 			 String username, 
 			 String password, 
 			 String packageName) {
 		
 		configure(url, 
 				  driver, 
 				  username, 
 				  password, 
 				  null, 
 				  packageName, 
 				  null, 
 				  null, 
 				  null, 
 				  null);
 		
 	}
 	public static Connection getConnection() throws SQLException {
 		checkConnection();
 		
 		return connection;
 	}
 	
 	protected static void close() {
 		Configure.cleanConnection();
 	}
 	
 	private static void setConnectionArguments(String connectionArguments) {
 		if (connectionArguments == null || connectionArguments.trim().length() == 0) {
 			connectionArguments = null;
 		}
 		
 		Configure.connectionArguments = connectionArguments;
 	}
 
 	private static void setClassprefix(String classprefix) {
 		if (classprefix == null || classprefix.trim().length() == 0) {
 			classprefix = DEFAULT_CLASSNAME_PREFIX;
 		} 
 			
 		Configure.classprefix = classprefix;
 	}
 
 	private static void setSeparator(String separator) {
 		if (separator == null || separator.trim().length() ==0) {
 			separator = DEFAULT_SEPARATOR;
 		} 
 		
 		Configure.separator = separator;
 	}
 
 	protected static void setStartIndex(Integer startIndex) {
 		if (startIndex == null || startIndex.compareTo(new Integer(0)) < 0) {
 			startIndex = DEFAULT_START_INDEX;
 		}
 		
 		Configure.startIndex = startIndex;
 	}
 
 	private static void setVersionTable(String versionTable) {
 		if (versionTable == null || versionTable.trim().length() == 0) {
 			versionTable = DEFAULT_VERSION_TABLE;
 		}
 		
 		Configure.versionTable = versionTable;
 	}
 
 	protected static String getUrl() {
 		return url;
 	}
 
 	protected static String getDriver() {
 		return driver;
 	}
 
 	protected static String getUsername() {
 		return username;
 	}
 
 	protected static String getConnectionArguments() {
 		return connectionArguments;
 	}
 
 	protected static String getPackageName() {
 		return packageName;
 	}
 
 	protected boolean isOwnConnection() {
 		return ownConnection;
 	}
 
 	protected static String getClassprefix() {
 		return classprefix;
 	}
 
 	protected static String getSeparator() {
 		return separator;
 	}
 
 	public static Integer getStartIndex() {
 		return startIndex;
 	}
 
 	public static String getVersionTable() {
 		return versionTable;
 	}
 	
 	public static String getBaseClassName() {
 		String baseName = packageName + "." + classprefix + separator;
 		return baseName;
 	}
 	
 	private static void checkConnection() throws SQLException {
 		if (connection == null || connection.isClosed()) {
 			
 			if (driver == null) {
 				throw new RuntimeException("No driver name found!  Make sure you call Configure.configure().");
 			}
 			
 			ownConnection = true;
 			
 			try {
 				Class.forName(driver);
 				connection = DriverManager.getConnection(url, username, password);
 			} catch (ClassNotFoundException e) {
 				throw new RuntimeException(e);
 			}
 			
 		}
 	}
 	
 	private static void cleanConnection() {
 		if (ownConnection) {
 			Closer.close(connection);
 		}
 	}
 	
 }
