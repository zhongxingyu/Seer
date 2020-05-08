 /**
  * This file is part of Atomic Tagging.
  * 
  * Atomic Tagging is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * Atomic Tagging is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Atomic Tagging. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.atomictagging.core.accessors;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 
 import org.apache.commons.configuration.CombinedConfiguration;
 import org.atomictagging.core.configuration.Configuration;
 
 /**
  * A class holding the connection to the MySQL server
  * 
  * @author Stephan Mann
  */
 public class DB {
 	/**
 	 * Connection to the MySQL server.
 	 */
 	public static Connection	CONN;
 
 
 	private DB() {
 		// Utility class
 	}
 
 
 	/**
 	 * Initializes the connection to the DB by retrieving all required data from the configuration and trying to
 	 * connect.
 	 * 
 	 * @throws Exception
 	 *             If either loading of the driver or connecting to the DB failed for whatever reason
 	 */
 	public static void init() throws Exception {
 		Class.forName( "com.mysql.jdbc.Driver" );
 
 		CombinedConfiguration conf = Configuration.get();
 
 		String type = conf.getString( "database.type" );
 		String host = conf.getString( "database.host" );
 		String db = conf.getString( "database.db" );
 		String user = conf.getString( "database.user" );
 		String pass = conf.getString( "database.pass" );
 
		if ( type == null || host == null || db == null || user == null ) {
			throw new Exception(
					"Failed to load database configuration. Please specify valid values for database type, host, name and user." );
		}

 		String connectString = "jdbc:" + type + "://" + host + "/" + db;
 		CONN = DriverManager.getConnection( connectString, user, pass );
 	}
 }
