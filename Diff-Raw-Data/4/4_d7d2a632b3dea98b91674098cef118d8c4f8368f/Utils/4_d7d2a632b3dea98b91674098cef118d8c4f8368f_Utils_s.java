 package org.clubrockisen.dao;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.clubrockisen.common.Configuration;
 import org.clubrockisen.common.ConfigurationKey;
 import org.clubrockisen.common.error.SQLConfigurationError;
 
 /**
  * Utility methods for DAOs (connection, statement generation, etc).
  * @author Alex
  */
 public final class Utils {
 	/** Logger */
 	private static Logger					lg	= Logger.getLogger(Utils.class.getName());
 	
 	/** Access to the configuration */
 	private static final Configuration		CONFIG	= Configuration.getInstance();
 	/** Access to the key structure of the configuration */
 	private static final ConfigurationKey	KEYS	= ConfigurationKey.KEY;
 	
 	/**
 	 * Constructor #1.<br />
 	 */
 	private Utils () {
 		super();
 	}
 	
 	/**
 	 * Class holding the database configuration information.<br />
 	 * @author Alex
 	 */
 	public static class DBConnectionInfo {
 		/** The URL of the connection */
 		private final String url;
 		/** The user name to use */
 		private final String username;
 		/** The password associated to the user name */
 		private final String password;
 		
 		/**
 		 * Constructor #1.<br />
 		 * @param url
 		 *        the URL of the database.
 		 * @param username
 		 *        the user name to use.
 		 * @param password
 		 *        the password associated to the user name.
 		 */
 		public DBConnectionInfo (final String url, final String username, final String password) {
 			super();
 			this.url = url;
 			this.username = username;
 			this.password = password;
 		}
 		
 		/**
 		 * Return the URL.
 		 * @return the URL.
 		 */
 		public String getUrl () {
 			return url;
 		}
 		
 		/**
 		 * Return the attribute user name.
 		 * @return the attribute user name.
 		 */
 		public String getUsername () {
 			return username;
 		}
 		
 		/**
 		 * Return the attribute password.
 		 * @return the attribute password.
 		 */
 		public String getPassword () {
 			return password;
 		}
 		
 	}
 	
 	/**
 	 * Returns the connection information in the configuration file.
 	 * @return the connection information.
 	 */
 	public static DBConnectionInfo getConnectionInfo () {
 		return new DBConnectionInfo(CONFIG.get(KEYS.db().url()),
 				CONFIG.get(KEYS.db().username()),
 				CONFIG.get(KEYS.db().password()));
 	}
 	
 	/**
 	 * Return the connection to the database with the information in the configuration file.
 	 * @return the connection to the database.
 	 */
 	public static Connection getConnection () {
 		final Connection connection;
 		try {
 			final DBConnectionInfo dbInfos = Utils.getConnectionInfo();
 			connection = DriverManager.getConnection(dbInfos.getUrl(), dbInfos.getUsername(), dbInfos.getPassword());
 			if (connection.isValid(0)) {
 				if (lg.isLoggable(Level.INFO)) {
 					lg.info("Successfully connected to database " + dbInfos.getUrl());
 				}
 			} else {
				throw new SQLException("Database connection not validated.");
 			}
 		} catch (final SQLException e) {
 			lg.severe("Failed to create the connection to the database " + e.getMessage());
 			throw new SQLConfigurationError("Failed to connect to database: "
 					+ e.getMessage(), e);
 		}
 		return connection;
 	}
 	
 	/**
 	 * Close a database connection.
 	 * @param connection
 	 *        the connection to close.
 	 */
 	public static void close (final Connection connection) {
 		try {
 			connection.close();
 		} catch (final SQLException e) {
 			lg.warning("Error while closing database connection (" + e.getMessage() + ")");
 		}
 	}
 }
