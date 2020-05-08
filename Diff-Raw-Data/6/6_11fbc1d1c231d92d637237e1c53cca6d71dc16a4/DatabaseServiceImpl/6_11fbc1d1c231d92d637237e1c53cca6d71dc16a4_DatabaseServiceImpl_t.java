 package org.openremote.controller.service.impl;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.hsqldb.Server;
 import org.openremote.controller.Constants;
 import org.openremote.controller.ControllerConfiguration;
 import org.openremote.controller.service.DatabaseService;
 import org.openremote.controller.utils.AlgorithmUtil;
 
 /**
  * Database service for creating connection, do sql statements, 
  *  get number of rows, closing the connection(s) and more...
  * 
  * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
  */
 
 public class DatabaseServiceImpl implements DatabaseService 
 {  
    private final static Logger logger = Logger.getLogger(Constants.SERVICE_LOG_CATEGORY);
 
    private final static String CONFIGURATION_NAME_1 = "authentication";
    private final static String CONFIGURATION_NAME_2 = "composer_username";
    private final static String CONFIGURATION_NAME_3 = "ca_path";
    private final static String CONFIGURATION_NAME_4 = "pin_check";
    private final static String CONFIGURATION_NAME_5 = "composer_password";
    private final static String CONFIGURATION_NAME_6 = "session_timestamp";
    private final static String CONFIGURATION_NAME_7 = "salt";
 
    private final static String CONFIGURATION_TYPE_1 = "boolean";
    private final static String CONFIGURATION_TYPE_2 = "string";
    private final static String CONFIGURATION_TYPE_3 = "string";
    private final static String CONFIGURATION_TYPE_4 = "boolean";
    private final static String CONFIGURATION_TYPE_5 = "string";
    private final static String CONFIGURATION_TYPE_6 = "string";
    private final static String CONFIGURATION_TYPE_7 = "string";
 
    private final static String CONFIGURATION_VALUE_1 = "true";
    private final static String CONFIGURATION_VALUE_4 = "true";
    private final static String CONFIGURATION_VALUE_7 = AlgorithmUtil.getSalt();
    
    private final static String CONFIGURATION_INFORMATION_1 = "When checked the authentication is activated, meaning devices must be accepted before they can use the OpenRemote Controller.<br/>If not checked there is no authentication and SSL security is not active.";    
    private final static String CONFIGURATION_INFORMATION_2 = "The username of the administrator.";
    private final static String CONFIGURATION_INFORMATION_3 = "The CA path is the directory path where the CA (Certificate authority) files are located. For example the key store files.";
    private final static String CONFIGURATION_INFORMATION_4 = "When checked you are forced to enter the pin in the user management given by the device.<br/>If not checked, it is optional to use the pin.";
    private final static String CONFIGURATION_INFORMATION_5 = "The password of the administrator.";
    private final static String CONFIGURATION_INFORMATION_6 = "The session timestamp";
    private final static String CONFIGURATION_INFORMATION_7 = "This is a random salt used for hashing the password.";
    
    private ControllerConfiguration configuration;
    private static Connection connection;
    private Server hsqlServer;
    Statement statement = null;
    ResultSet resultSet = null;
    
    /**
     * Initialize database path from XML configuration file 
     * 
     * @return true is success else false
     */
    private boolean initDatabase()
    {
       boolean returnValue = true;
       
       hsqlServer = new Server();
 
       // HSQLDB prints out a lot of informations when
       // starting and closing, which we don't need now.
       // Normally you should point the setLogWriter
       // to some Writer object that could store the logs.      
       hsqlServer.setLogWriter(null);
       hsqlServer.setErrWriter(null);
       hsqlServer.setSilent(true);
       hsqlServer.setTrace(false);
       
       hsqlServer.setPort(10001);
       
       // init database path
       configuration = ControllerConfiguration.readXML();
       
       // Set logger to error level
       Logger databaseLogger = Logger.getLogger("hsqldb.db");
       databaseLogger.setLevel(Level.ERROR);
       
       if(configuration != null)
       {
          // The actual database will be named 'xdb' and its
          // settings and data will be stored in files
          // testdb.properties and testdb.script
          hsqlServer.setDatabaseName(0, "openremote");
          hsqlServer.setDatabasePath(0, "file:" + configuration.getResourcePath() + "/database/openremote");
                   
          // Start the database!
          hsqlServer.start();
       }
       else
       {
          returnValue = false;
          logger.error("Controller configuration is null");
       }
       
       if(hsqlServer.getState() != 1) {
          logger.error("hsqlServer not started, probably bind exception");
          returnValue = false;
       }   
       
       return returnValue;
    }
    
    /**
     * Setup the database connection via DriverManager, using the database path
     * 
     * @return true if success else false
     */
    private boolean setupConnection()
    {
       boolean returnValue = true;
       try {
          Class.forName("org.hsqldb.jdbcDriver");
          connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:10001/openremote", "SA", "");
        } catch (SQLException e) {
          returnValue = false;
          logger.error("SQL Exception: " + e.getMessage());
       } catch (ClassNotFoundException e) {
          returnValue = false;
          logger.error("SQL Exception Class not Found: " + e.getMessage());
       }
       return returnValue;
    }
    
    /**
     * Create the tables in the database
     */
    private void createTables()
    {
       try
       {
          connection.prepareStatement("SET SCHEMA PUBLIC").execute();
          connection.prepareStatement("SET DATABASE DEFAULT INITIAL SCHEMA PUBLIC").execute();
          
          connection.prepareStatement("CREATE TABLE PUBLIC.client "+
                      "(client_id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0, INCREMENT BY 1) NOT NULL, "+
                      "client_serial VARCHAR(25) NOT NULL, "+
                      "client_pincode VARCHAR(4) NOT NULL, "+
                      "client_device_name VARCHAR(150), "+
                      "client_email VARCHAR(250), "+
                      "client_alias VARCHAR(200), "+
                      "client_active BOOLEAN DEFAULT FALSE NOT NULL, "+
                      "client_creation_timestamp TIMESTAMP DEFAULT NOW,  "+
                      "client_modification_timestamp TIMESTAMP DEFAULT NOW,  "+
                      "client_group_id BIGINT DEFAULT NULL, "+
                      "client_role VARCHAR(200), "+
                      "client_dn VARCHAR(250), "+
                      "PRIMARY KEY (client_id), "+
                      "UNIQUE (client_alias, client_group_id, client_dn))")
                      .execute();
          
          connection.prepareStatement("CREATE TABLE PUBLIC.client_group "+
                "(group_id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0, INCREMENT BY 1) NOT NULL, "+
                "group_name VARCHAR(250) NOT NULL, "+
                "PRIMARY KEY (group_id))")
                .execute();
          
          connection.prepareStatement("CREATE TABLE PUBLIC.configuration "+
                "(configuration_id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0, INCREMENT BY 1) NOT NULL, "+
                "configuration_name VARCHAR(100) NOT NULL, "+
                "configuration_value VARCHAR(250) NOT NULL, "+
                "configuration_type VARCHAR(100) NOT NULL, "+     
                "configuration_information VARCHAR(255) NOT NULL, "+
                "configuration_disabled BOOLEAN DEFAULT FALSE NOT NULL, "+
                "PRIMARY KEY (configuration_id), "+
                "UNIQUE (configuration_name))")
                .execute();
       } catch (SQLException e) {
          // ignore exceptions, because table creations can be done multiple times
          logger.error("SQL exception table creation: " + e.getMessage());
       }
    }
    
    /**
     * Fill the database with content necessary for later
     */
    private void fillDatabase()
    {
       try {
          connection.prepareStatement("INSERT INTO PUBLIC.configuration " +
                "(configuration_id, configuration_name, configuration_value, configuration_type, configuration_information) VALUES " +
                "(null, '" + CONFIGURATION_NAME_1 + "', '" + CONFIGURATION_VALUE_1 + "', '" + CONFIGURATION_TYPE_1 + "', '" + CONFIGURATION_INFORMATION_1 + "')," +
                "(null, '" + CONFIGURATION_NAME_2 + "', '', '" + CONFIGURATION_TYPE_2 + "', '" + CONFIGURATION_INFORMATION_2 + "'), " +
                "(null, '" + CONFIGURATION_NAME_3 + "', '', '" + CONFIGURATION_TYPE_3 + "', '" + CONFIGURATION_INFORMATION_3 + "'), " +
                "(null, '" + CONFIGURATION_NAME_4 + "', '" + CONFIGURATION_VALUE_4 + "', '" + CONFIGURATION_TYPE_4 + "', '" + CONFIGURATION_INFORMATION_4 + "'), " +
                "(null, '" + CONFIGURATION_NAME_5 + "', '', '" + CONFIGURATION_TYPE_5 + "', '" + CONFIGURATION_INFORMATION_5 + "')," + 
                "(null, '" + CONFIGURATION_NAME_6 + "', '', '" + CONFIGURATION_TYPE_6 + "', '" + CONFIGURATION_INFORMATION_6 + "')," +
                "(null, '" + CONFIGURATION_NAME_7 + "', '" + CONFIGURATION_VALUE_7 + "', '" + CONFIGURATION_TYPE_7 + "', '" + CONFIGURATION_INFORMATION_7 + "')")
                .execute(); 
       } catch (SQLException e) {
          // ignore exceptions, because database filling can be done multiple times
          logger.error("SQL exception table filling: " + e.getMessage());
       }
    }
    
    /**
     * Create database connection, with the right settings so it is possible to scroll trough the result set
     *  
     * @return true if success else false
     */
    private boolean createStatement()
    {
       boolean returnValue = true;
       try {
          statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                           ResultSet.CONCUR_UPDATABLE);
       } catch (SQLException e) {
          returnValue = false;
          logger.error("SQL Exception: " + e.getMessage());
       }
       return returnValue;
    }
 
    /**
     * Database initialization
     * 
     * @return true is success else false
     */
    @Override
    public boolean databaseInit()
    { 
       boolean returnValue = true;
       
       returnValue = this.initDatabase();
       returnValue = this.setupConnection();
       
       this.createTables();
       this.fillDatabase();
       
       returnValue = this.createStatement();
       
       return returnValue;
    }
 
    /**
     * Execute SQL query into the database
     * 
     * @return ResultSet with the result of the query
     */
    @Override
    public ResultSet doSQL(String sql)
    {
       try 
       {
          if(statement != null)
          {
             resultSet = statement.executeQuery(sql);
          }
       } catch (SQLException e) {
          logger.error("SQL Exception: " + e.getMessage());
       }
       return resultSet;
    }
    
    /**
     * Execute SQL query via prepare statement to prevent SQL Injection
     * 
     * @param prepareStatement Prepare statement
     * @return ResultSet with the result of the query
     */
    @Override
    public ResultSet doSQL(PreparedStatement preparedStatement)
    {
       try {
          if(preparedStatement != null)
          {
             resultSet = preparedStatement.executeQuery();
          }
       } catch (SQLException e) {
          logger.error("SQL Exception: " + e.getMessage());
       }
       return resultSet;
    }
 
    /**
     * Create a PreparedStatement from a query
     * @return PreparedStatement
     * @throws SQLException 
     */
    @Override
    public PreparedStatement createPrepareStatement(String query) throws SQLException
    {
       PreparedStatement preparedStatement = null;
  
       preparedStatement = connection.prepareStatement(query, 
             ResultSet.TYPE_SCROLL_INSENSITIVE, 
             ResultSet.CONCUR_UPDATABLE);
 
       return preparedStatement;
    }
    
    /**
     * Do a insert, update or delete SQL query into the database
     * 
     * @return 0 or -1 is unsuccessfully, 1 (or higher) is successfully 
     */
    @Override
    public int doUpdateSQL(String sql)
    {
       int result = -1;
       try 
       {
          if(statement != null)
          {
             result = statement.executeUpdate(sql);
          }
       } catch (SQLException e) {
          logger.error("SQL Exception: " + e.getMessage());
       }
       return result;
    }     
    
    /**
     * Do a insert, update or delete SQL query via prepare statement to prevent SQL Injection
     * 
     * @return 0 or -1 is unsuccessfully, 1 (or higher) is successfully 
     */
    @Override
    public int doUpdateSQL(PreparedStatement preparedStatement)
    {
       int result = -1;
       try 
       {
          if(preparedStatement != null)
          {
             result = preparedStatement.executeUpdate();
          }
       } catch (SQLException e) {
          logger.error("SQL Exception: " + e.getMessage());
       }
       return result;
    }      
    
    /**
     * Get the number of rows of the result set (after a doSQL)
     * 
     * @see doSQL(String sql)
     * @return the number of the rows
     */
    @Override
    public int getNumRows()
    {
       int numRows = 0;
       try 
       {
          if(resultSet != null)
          {
             resultSet.last();
             numRows = resultSet.getRow();
             resultSet.beforeFirst();
          }
       } 
       catch (NullPointerException e) 
       {
          logger.error(e.getMessage());
       }
       catch (SQLException e) {
          logger.error("SQLException: "  + e.getMessage());
       }
       return numRows;
    }
 
    /**
     * Get the last inserted ID (IDENTITY) of the last database query
     * 
     * @return the last inserted id
     */
    @Override
    public int getInsertID()
    {
       int newID = -1;
 
       try 
       {
          if (resultSet != null && resultSet.next()) 
          {
             newID = resultSet.getInt(1);   
          }
       } catch (SQLException e) {
          logger.error("SQL Exception: " + e.getMessage());
       } 
       return newID;
    }
 
    /**
     * Free the database result set, so it closes the result set and frees the resources
     */
    @Override
    public void free() {
       try {
          if(resultSet != null)
          {
             resultSet.close();
          }
       } catch (SQLException e) {
          logger.error("SQL Exception: " + e.getMessage());
       }
    }
 
    /**
     * Close the database and shutdown the HSQLDB server, only necessary at stopping the application
     */
    public void close() 
    {
       try {         
          if(statement != null)
          {
             statement.close();
          }
          
          if(connection != null)
          {
             connection.close();
          }
          
          // Stop hsqldb server
          if(hsqlServer != null)
          {
             hsqlServer.shutdown();
          }
       } catch (SQLException e) {
          logger.error("SQL Exception: " + e.getMessage());
       }
    }
    
    /**
     * Sets the configuration.
     * 
     * @param configuration the new configuration
     */   
    public void setConfiguration(ControllerConfiguration configuration)
    {
       this.configuration = configuration;
    }   
 }
