 package au.com.addstar.birthdaygift;
 /*
 * BirthdayGift
 * Copyright (C) 2013 add5tar <copyright at addstar dot com dot au>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 import java.io.IOException;
 import java.nio.file.*;
 import static java.nio.file.StandardCopyOption.*;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 public class Database {
 	private BirthdayGift plugin;
 	public String DBFilename;
 	public Connection Conn;
 	public boolean IsConnected = false;
 	
 	public Database(BirthdayGift instance, String filename) {
 		plugin = instance;
 		DBFilename = filename;
 		OpenDatabase();
 	}
 
 	public boolean OpenDatabase() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Conn = DriverManager.getConnection("jdbc:sqlite:plugins/" + plugin.getName() + "/" + DBFilename);
 			IsConnected = true;
 			
 			if (TableExists(Conn, "birthdaygift")) {
 				// Check/update existing table
 				if (!ColumnExists(Conn, "birthdaygift", "lastAnnouncedDate")) {
 					plugin.Warn("Old table format detected!");
 					
 					// Backup existing database
 					plugin.Log("Creating backup of existing database...");
 					Path src = Paths.get("plugins/" + plugin.getName() + "/" + DBFilename);
 					Path dst = Paths.get("plugins/" + plugin.getName() + "/backup.db");
 					
 					try {
 						Files.copy(src, dst, new CopyOption[] { REPLACE_EXISTING });
 					} catch (IOException e) {
 						plugin.Warn("Unable to create database backup! Refusing to continue!!");
 						Conn.close();
 						IsConnected = false;
 						e.printStackTrace();
 						return false;
 					}
 					
 					// Make changes to database
 					plugin.Log("Updating database table...");
 					int result = ExecuteUpdate("ALTER TABLE birthdaygift ADD COLUMN `lastAnnouncedDate` DATE");
 					plugin.Debug("SQL Result: " + result);
 				}
 			} else {
 				// Create new table schema
 				plugin.Log("Database table does not exist, creating one.");
 				ExecuteUpdate("CREATE TABLE IF NOT EXISTS birthdaygift (" +
 						"`player` varchar(250) NOT NULL PRIMARY KEY, " +
 						"`birthdayDate` DATE," +
 						"`lastGiftDate` DATE," +
 						"`lastAnnouncedDate` DATE)");
 			}
 			IsConnected = true;
 			return false;
 		} catch (SQLException e) {
 			plugin.Warn("Unable to open database!");
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public ResultSet ExecuteQuery(String query) {
 		Statement st;
 		
 		if (!IsConnected) { return null; }
 		
 		try {
 			st = Conn.createStatement();
 			plugin.Debug("SQL Query: " + query);
 			return st.executeQuery(query);
 		} catch (SQLException e) {
 			plugin.Warn("Query execution failed!");
 			plugin.Log("SQL: " + query);
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public ResultSet PreparedQuery(String query, String[] params) {
 		PreparedStatement ps;
 		
 		if (!IsConnected) { return null; }
 		
 		try {
 			ps = Conn.prepareStatement(query);
 			// Construct PreparedStatement by adding all supplied params to the query
 			plugin.Debug("SQL Query: " + query);
 			for (int x=0; x < params.length; x++) {
 				plugin.Debug("Param " + (x+1) + ": "+ params[x]);
 				ps.setString(x+1, params[x]);
 			}
 			return ps.executeQuery();
 		} catch (SQLException e) {
 			plugin.Warn("Prepared query execution failed!");
 			plugin.Log("SQL: " + query);
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public int ExecuteUpdate(String query) {
 		Statement st;
 		
 		if (!IsConnected) { return -1; }
 		
 		try {
 			st = Conn.createStatement();
 			plugin.Debug("SQL Update: " + query);
 			return st.executeUpdate(query);
 		} catch (SQLException e) {
 			plugin.Warn("Query execution failed!");
 			plugin.Log("SQL: " + query);
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	public int PreparedUpdate(String query, String[] params) {
 		PreparedStatement ps;
 		
 		if (!IsConnected) { return -1; }
 		
 		try {
 			ps = Conn.prepareStatement(query);
 			// Construct PreparedStatement by adding all supplied params to the query
 			plugin.Debug("SQL Update: " + query);
 			for (int x=0; x < params.length; x++) {
 				plugin.Debug("Param " + (x+1) + ": "+ params[x]);
 				ps.setString(x+1, params[x]);
 			}
 			return ps.executeUpdate();
 		} catch (SQLException e) {
 			plugin.Warn("Prepared query execution failed!");
 			plugin.Log("SQL: " + query);
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	public boolean CloseDatabase() {
 		try {
 			Conn.close();
 		} catch (SQLException e) {
 			plugin.Warn("Close database failed!");
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	public boolean TableExists(Connection conn, String tname) {
 		DatabaseMetaData md;
 		ResultSet rs;
 		
 		try {
 			md = conn.getMetaData();
 		} catch (SQLException e) {
 			// This shouldn't really happen
 			plugin.Warn("Unable to read DatabaseMetaData from DB connection!");
 			e.printStackTrace();
 			return false;
 		}
 
 		try {
 			plugin.Debug("Getting list of database tables");
 			rs = md.getTables(null, null, tname, null);
 		} catch (SQLException e) {
 			// This shouldn't really happen
 			plugin.Warn("Unable to getTables from DatabaseMetaData!");
 			e.printStackTrace();
 			return false;
 		}
 		
 		try {
 			if (rs.next()) {
 				// Table exists 
 				return true;
 			}
 		} catch (SQLException e) {
 			// This shouldn't really happen
 			plugin.Warn("Unable to iterate table resultSet!");
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean ColumnExists(Connection conn, String tname, String cname) {
 		DatabaseMetaData md;
 		ResultSet rs;
 		
 		try {
 			md = conn.getMetaData();
 		} catch (SQLException e) {
 			// This shouldn't really happen
 			plugin.Warn("Unable to read DatabaseMetaData from DB connection!");
 			e.printStackTrace();
 			return false;
 		}
 
 		try {
 			plugin.Debug("Getting list of table columns");
 			rs = md.getColumns(null, null, tname, cname);
 		} catch (SQLException e) {
 			// This shouldn't really happen
 			plugin.Warn("Unable to getColumns from DatabaseMetaData!");
 			e.printStackTrace();
 			return false;
 		}
 		
 		try {
 			if (rs.next()) {
 				// Table exists 
 				return true;
 			}
 		} catch (SQLException e) {
 			// This shouldn't really happen
 			plugin.Warn("Unable to iterate column resultSet!");
 			e.printStackTrace();
 		}
 		return false;
 	}
 }
