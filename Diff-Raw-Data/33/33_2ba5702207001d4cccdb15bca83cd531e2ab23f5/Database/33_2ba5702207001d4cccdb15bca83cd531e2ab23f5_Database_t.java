 package main;
import java.io.*;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.SQLException;
import java.util.Properties;
 
 /**
  * This singleton class handles the database connection.
  * 
  * @author Michael Feist
  */
 public class Database {
 	
 	// Static Instance
 	private static Database _instance = null;
 	
 	// Static Variables
 	public static final String m_url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
 	public static final String m_driverName = "oracle.jdbc.driver.OracleDriver";
 
	public static String m_userName = "mdfeist";
	public static String m_password = "tiger291";
 	
 	// Instance Variables
 	private Connection _connection;
 	
	/**
	 * Creates a connection to the database. Also grabs
	 * the user name and password for the database from
	 * the config.properties file.
	 */
 	private Database() 
 	{
 		_connection = null;
 		
		Properties prop = new Properties();
		 
    	try {
    		 //load a properties file
    		InputStream is = getClass().getResourceAsStream("/config.properties");
    		
    		if (is != null)
    		{
	    		prop.load(is);
	    		
	    		// get the properties value
	    		m_userName = prop.getProperty("dbuser");
	    		m_password = prop.getProperty("dbpassword");
    		}
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
		
 		try {
 			Class<?> drvClass = Class.forName(m_driverName);
 			DriverManager.registerDriver((Driver) drvClass.newInstance());
 		} catch (Exception e) {
 			System.err.print("ClassNotFoundException: ");
 			System.err.println(e.getMessage());
 		}
 		
 		try {
 			_connection = DriverManager.getConnection(m_url, m_userName, m_password);
 		} catch (SQLException ex) {
 			System.err.println("SQLException: " + ex.getMessage());
 		}
 	}
 
 	public static Database getInstance() {
 		if (_instance == null)
 			_instance = new Database();
 
 		return _instance;
 	}
 	
 	public Connection getConnection()
 	{
 		return _connection;
 	}
 	
 	public void closeConnection()
 	{
 		// Close Connection
 		try {
 			_connection.close();
 		} catch (SQLException e) {
 			System.err.println("SQLException: " + e.getMessage());
 		}
 	}
 }
