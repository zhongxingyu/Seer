 package com.cffreedom.utils.db;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cffreedom.beans.DbConn;
 import com.cffreedom.exceptions.DbException;
 import com.cffreedom.exceptions.FileSystemException;
 import com.cffreedom.exceptions.InfrastructureException;
 import com.cffreedom.utils.Convert;
 import com.cffreedom.utils.SystemUtils;
 import com.cffreedom.utils.Utils;
 import com.cffreedom.utils.file.FileUtils;
 import com.cffreedom.utils.security.SecurityManager;
 
 /**
  * Automated layer for accessing DB Connections that should guarantee that
  * the user is not prompted for any information.
  * 
  * Original Class: com.cffreedom.utils.db.ConnectionManager
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  * 
  * Changes:
  * 2013-05-06 	markjacobsen.net 	Created
  * 2013-06-25 	markjacobsen.net 	Added connection pooling to getConnection()
  * 2013-07-15	markjacobsne.net 	Replaced the use of KeyValueFileMgr with a properties file
  * 2013-07-15 	markjacobsen.net 	Added support for commons-dbcp
  * 2013-07-17 	markjacobsen.net 	Added support for the dbconn.properties file being on the classpath
  */
 public class ConnectionManager
 {
 	public static final String PROP_FILE = "dbconn.properties";
 	public static final String DEFAULT_FILE = SystemUtils.getDirConfig() + SystemUtils.getPathSeparator() + PROP_FILE;
 	public static final boolean CREATE_FILE = false;
 	private static final Logger logger = LoggerFactory.getLogger("com.cffreedom.utils.db.ConnectionManager");
 	private HashMap<String, DbConn> conns = new HashMap<String, DbConn>();
 	private Hashtable<String, BasicDataSource> pools = null;
 	private String file = null;
 	private SecurityManager security = new SecurityManager("abasickeyyoushouldnotchange");
 	
 	public ConnectionManager() throws FileSystemException, InfrastructureException
 	{
 		this(ConnectionManager.DEFAULT_FILE);
 	}
 	
 	public ConnectionManager(String file) throws FileSystemException, InfrastructureException
 	{
 		this(file, ConnectionManager.CREATE_FILE);
 	}
 	
 	public ConnectionManager(String file, boolean createPropFileIfNew) throws FileSystemException, InfrastructureException
 	{		
 		this.loadConnectionFile(file, createPropFileIfNew);
 	}
 	
 	/**
 	 * Use to enable commons-dbcp connection pooling. Note that this value
 	 * and connection pooling will only be done via this class if it is 
 	 * unable to get a JNDI connection. In other words, if this class can get
 	 * a JNDI connection it will, and we'll never setup an internal pool
 	 * @param enable True to enable, false to disable
 	 */
 	public void enableConnectionPooling(boolean enable)
 	{
 		if (enable == true){
 			if (this.pools == null){
 				logger.info("Turning on Connection Pooling");
 				this.pools = new Hashtable<String, BasicDataSource>();
 			}
 		}else{
 			logger.info("Turning off Connection Pooling");
 			this.pools = null;
 		}
 	}
 	
 	public void loadConnectionFile(String file) throws FileSystemException, InfrastructureException { this.loadConnectionFile(file, ConnectionManager.CREATE_FILE); }
 	@SuppressWarnings("resource")
 	public void loadConnectionFile(String file, boolean createPropFileIfNew) throws FileSystemException, InfrastructureException
 	{
 		InputStream inputStream = null;
 		Properties props = new Properties();
 		
 		try
 		{
 			this.file = file;
 		
 			if ((this.file != null) && (FileUtils.fileExists(this.file) == false) && (createPropFileIfNew == true))
 			{
 				logger.debug("Attempting to create file: {}", this.file);
 				this.save();
 			}
 			
 			if (FileUtils.fileExists(this.file) == true)
 			{
 				logger.info("Loading from passed in file: {}", this.file);
 				inputStream = new FileInputStream(this.file);
 			}
 			else
 			{
 				logger.info("Attempting to find file on classpath: {}", ConnectionManager.PROP_FILE);
 				inputStream = this.getClass().getClassLoader().getResourceAsStream(ConnectionManager.PROP_FILE);
 			}
 			
 			if (inputStream == null)
 			{
 				throw new InfrastructureException("Invalid connection file or no default file \""+ConnectionManager.PROP_FILE+"\" found on the classpath");
 			}
 			else
 			{
 				logger.debug("Loading property file");
 				
 				props.load(inputStream);
 				inputStream.close();
 				
 				if (props.getProperty("keys") == null)
 				{
 					logger.warn("No \"keys\" property exists so nothing will be read");
 				}
 				else
 				{
 					String[] keys = props.getProperty("keys").split(",");
 					
 					for (String key : keys)
 					{
 						logger.debug(key);
 						String type = props.getProperty(key + ".type");
 						String host = props.getProperty(key + ".host");
 						String db = props.getProperty(key + ".db");
 						String port = props.getProperty(key + ".port");
 						String user = props.getProperty(key + ".user");
 						String password = props.getProperty(key + ".password");
 						String jndi = props.getProperty(key + ".jndi");
 						
						if ((port == null) || (port.trim().length() == 0)) { port = "0"; }
 						
 						DbConn dbconn = new DbConn(DbUtils.getDriver(type),
												DbUtils.getUrl(type, host, db, Convert.toInt(port)), 
 												type,
 												host,
 												db,
 												Convert.toInt(port));
 						
 						if (this.validValue(user) == true) { dbconn.setUser(user); }
 						if (this.validValue(password) == true) { dbconn.setPassword(security.decrypt(password)); }
 						if (this.validValue(jndi) == true) { dbconn.setJndi(jndi); }
 		
 						this.conns.put(key, dbconn);
 					}
 				}
 			}
 		}
 		catch (FileNotFoundException e)
 		{
 			throw new FileSystemException("FileNotFound", e);
 		}
 		catch (IOException e)
 		{
 			throw new FileSystemException("IOException", e);
 		}
 	}
 	
 	private boolean validValue(String val)
 	{
 		if ((val != null) && (val.length() > 0) && (val.equalsIgnoreCase("null") == false)){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	private boolean save()
 	{
 		if (this.file == null)
 		{
 			logger.warn("No file to save to");
 			return false;
 		}
 		else
 		{
 			ArrayList<String> lines = new ArrayList<String>();
 			logger.debug("Saving to file {}", this.getConnectionFile());
 			
 			lines.add("#--------------------------------------------------------------------------------------");
 			lines.add("# While it is not recommended, you can put usernames and passwords into this file.");
 			lines.add("# Passwords do need to be encrypted using the SecurityUtils class.");
 			lines.add("# It is suggested that you use the DbConnManager app in cffreedom-cl-apps to maintain");
 			lines.add("# this file.");
 			lines.add("#--------------------------------------------------------------------------------------");
 			lines.add("");
 			
 			if (this.conns.size() <= 0)
 			{
 				logger.warn("No DbConn objects cached so no actual values will be written");
 				lines.add("# No connections to save");
 			}
 			else
 			{
 				lines.add("keys=" + Convert.toDelimitedString(this.conns.keySet(), ","));
 				lines.add("");
 				
 				for (String entry : this.conns.keySet())
 				{
 					logger.trace(entry);
 					DbConn conn = this.getDbConn(entry);
 					lines.add(entry + ".db=" + this.getPropFileValue(conn.getDb()));
 					lines.add(entry + ".type=" + this.getPropFileValue(conn.getType()));
 					lines.add(entry + ".host=" + this.getPropFileValue(conn.getHost()));
 					lines.add(entry + ".port=" + this.getPropFileValue(Convert.toString(conn.getPort())));
 					lines.add(entry + ".user=" + this.getPropFileValue(conn.getUser()));
 					lines.add(entry + ".password=" + this.getPropFileValue(security.encrypt(conn.getPassword()), true));
 					lines.add(entry + ".jndi=" + this.getPropFileValue(conn.getJndi()));
 					lines.add("");
 				}
 			}
 			
 			return FileUtils.writeLinesToFile(this.getConnectionFile(), lines);
 		}
 	}
 	
 	private String getPropFileValue(String val) { return getPropFileValue(val, false); }
 	private String getPropFileValue(String val, boolean encrypt)
 	{
 		if (val == null){
 			return "";
 		}else{
 			if (encrypt == true){
 				return security.encrypt(val);
 			}else{
 				return val;
 			}
 		}
 	}
 	
 	public void close()
 	{
 		if (this.cacheConnections() == true)
 		{
 			for (String key : this.pools.keySet())
 			{
 				BasicDataSource bds = this.pools.get(key);
 				try
 				{
 					logger.debug("Closing pool: ", key);
 					bds.close();
 					this.pools.remove(key);
 				}
 				catch (SQLException e)
 				{
 					logger.error("Error closing pool: ", e.getMessage());
 				}
 			}
 		}
 	}
 	
 	public String getConnectionFile() { return this.file; }
 	
 	public boolean keyExists(String key)
 	{
 		return this.conns.containsKey(key);
 	}
 	
 	public DbConn getDbConn(String key)
 	{
 		return this.conns.get(key);
 	}
 	
 	public boolean cacheConnections() { if (this.pools != null){ return true; }else{ return false; } }
 		
 	public Connection getConnection(String key, String user, String pass)
 	{
 		Connection conn = null;
 		DbConn dbconn = this.getDbConn(key);
 		
 		if (dbconn != null)
 		{
 			// Set / override username and password if passed in
 			if (user != null) { dbconn.setUser(user); }
 			if (pass != null) { dbconn.setPassword(pass); }
 		}
 		else
 		{
 			logger.warn("A DbConn does not exist for key: {}", key);
 		}
 		
 		// Default to a JNDI connection if one exists
 		if ((dbconn != null) && (dbconn.getJndi() != null) && (dbconn.getJndi().length() > 0))
 		{
 			logger.debug("Getting JNDI connection: {}", key);
 			try
 			{
 				conn = DbUtils.getConnectionJNDI(dbconn.getJndi());
 			}
 			catch (DbException | InfrastructureException e)
 			{
 				logger.warn("Unable to get JNDI connection");
 			}
 		}
 		
 		// Next use connection pooling if configured
 		if ((conn == null) && (this.cacheConnections() == true))
 		{
 			if (this.pools.containsKey(key) == false)
 			{
 				logger.debug("Initializing connection pool: {}", key);				
 				BasicDataSource bds = new BasicDataSource();
 			    bds.setDriverClassName(dbconn.getDriver());
 			    bds.setUrl(dbconn.getUrl());
 			    bds.setUsername(dbconn.getUser());
 			    bds.setPassword(dbconn.getPassword());
 				
 				this.pools.put(key, bds);
 			}
 			
 			try
 			{
 				logger.debug("Getting pooled connection: {}", key);
 				conn = this.pools.get(key).getConnection();
 			}
 			catch (SQLException e)
 			{
 				logger.error("Error getting pooled connection");
 			}
 		}
 		
 		// Then try getting a non-pooled connection
 		if ((conn == null) && (dbconn != null))
 		{
 			logger.debug("Getting non-pooled connection: {}", key);
 			try
 			{
 				conn = DbUtils.getConnection(dbconn.getDriver(), dbconn.getUrl(), dbconn.getUser(), dbconn.getPassword());
 			}
 			catch (DbException | InfrastructureException e)
 			{
 				logger.error("Error getting non-pooled connection");
 			}
 		}
 		
 		// Finally make a last ditch attempt to just get a jndi connection
 		if (conn == null)
 		{
 			logger.warn("Making last ditch attempt to get JNDI connection: {}", key);
 			try
 			{
 				conn = DbUtils.getConnectionJNDI(key);
 			}
 			catch (DbException | InfrastructureException e)
 			{
 				logger.error("Error attempting to get last ditch JNDI connection");
 			}
 		}
 		
 		return conn;
 	}
 	
 	public boolean addConnection(String key, DbConn dbconn)
 	{
 		if (this.conns.containsKey(key) == false)
 		{
 			this.conns.put(key, dbconn);
 			this.save();
 			return true;
 		}
 		else
 		{
 			logger.error("A connection named {} already exists", key);
 			return false;
 		}
 	}
 	
 	public boolean updateConnection(String key, DbConn dbconn)
 	{
 		deleteConnection(key);
 		return addConnection(key, dbconn);
 	}
 	
 	public boolean deleteConnection(String key)
 	{
 		if (this.conns.containsKey(key) == true)
 		{
 			this.conns.remove(key);
 			this.save();
 			return true;
 		}
 		else
 		{
 			logger.error("A connection named {} does not exist");
 			return false;
 		}
 	}
 	
 	public void printKeys()
 	{
 		Utils.output("Keys");
 		Utils.output("======================");
 		if ((this.conns != null) && (this.conns.size() > 0))
 		{
 			for(String key : this.conns.keySet())
 			{
 				Utils.output(key);
 			}
 		}
 	}
 	
 	public void printConnInfo(String key)
 	{
 		DbConn dbconn = getDbConn(key);
 		Utils.output("");
 		Utils.output("Key = " + key);
 		Utils.output("Type = " + dbconn.getType());
 		Utils.output("DB = " + dbconn.getDb());
 		Utils.output("Host = " + dbconn.getHost());
 		Utils.output("Port = " + dbconn.getPort());
 	}
 	
 	public boolean testConnection(String key, String user, String pass)
 	{
 		DbConn dbconn = getDbConn(key);
 		boolean success = DbUtils.testConnection(dbconn, user, pass);
 		if (success == true)
 		{
 			Utils.output("Test SQL succeeded for " + key);
 		}
 		else
 		{
 			Utils.output("ERROR: Running test SQL for " + key);
 		}
 		return success;
 	}
 }
