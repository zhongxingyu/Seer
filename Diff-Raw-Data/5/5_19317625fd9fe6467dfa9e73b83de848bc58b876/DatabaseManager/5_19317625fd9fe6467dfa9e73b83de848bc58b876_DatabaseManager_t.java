 package com.jmeyer.bukkit.jlevel;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 
 /**
  * Handle calls to SQLite Database
  * @author JMEYER
  * 
  * Special Thanks:
  * - tkelly: suggesting SQLite, offering IRC assistance, providing sample source
  * - thegleek: suggesting SQLite, offering IRC assistance
  */
 public class DatabaseManager {
 	
 	public static final Logger LOG = Logger.getLogger("Minecraft");
 	public static final String ROOT_DIRECTORY = "JLevel-Data";
 	public static final String PLAYER_DB_DIRECTORY = "jdbc:sqlite:" + ROOT_DIRECTORY + File.separator + "Players" + File.separator;
 	public static final String SKILL_DB_DIRECTORY = "jdbc:sqlite:" + ROOT_DIRECTORY + File.separator + "Skills" + File.separator;
 	
 	// TODO: remove this. doesn't allow for name change.
 	/*
 	private final static String WARP_TABLE = "CREATE TABLE `warpTable` (" + "`id` INTEGER PRIMARY KEY," + "`name` varchar(32) NOT NULL DEFAULT 'warp',"
 		+ "`creator` varchar(32) NOT NULL DEFAULT 'Player'," + "`world` tinyint NOT NULL DEFAULT '0'," + "`x` DOUBLE NOT NULL DEFAULT '0',"
 		+ "`y` tinyint NOT NULL DEFAULT '0'," + "`z` DOUBLE NOT NULL DEFAULT '0'," + "`yaw` smallint NOT NULL DEFAULT '0'," + "`pitch` smallint NOT NULL DEFAULT '0'," + "`publicAll` boolean NOT NULL DEFAULT '1',"
 		+ "`permissions` varchar(150) NOT NULL DEFAULT ''," + "`welcomeMessage` varchar(100) NOT NULL DEFAULT ''" +");";
 	*/
 	
 	// TODO: make ignore null fields
 	// TODO: implement clearCurrent, which removes original tables if true
 	// TODO: implement clearCurrent with createSkillDatabaseIfNotExists
 	// Updates, or creates skill if not already created
 	public static void updateSkill(String skill, String[] itemRules, String[] expRules, String[] expTable, boolean clearCurrent) {
 		createSkillDatabaseIfNotExists(skill);
 		
 		// TODO: remove test values and actually parse
 		Connection conn = null;
 		Statement st = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection(skillDatabasePath(skill));
 			st = conn.createStatement();
 			
 			// TODO: make addRow() and addItemRulesRow(Skill, _, _);
			String itemRulesUpdate = "INSERT INTO `itemRules` (`itemId`,`level`) VALUES(0,1);";
 			st.executeUpdate(itemRulesUpdate);
 
 			String expRulesUpdate = "INSERT INTO `expRules` (`action`,`receiver`,`receiverState`,`exp`) VALUES('blockbreak', '1', '0', 3);";
 			st.executeUpdate(expRulesUpdate);
 		
 			String expLevelsUpdate = "INSERT INTO `expLevels` (`level`, `expNeeded`) VALUES(1, 83);";
 			st.executeUpdate(expLevelsUpdate);
 
 		} catch (SQLException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Create Table Exception", e);
 		} catch (ClassNotFoundException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 		} finally {
 			try {
 				if (conn != null)
 					conn.close();
 				if (st != null)
 					st.close();
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Could not create the table (on close)");
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	public static void createDirectoriesIfNotExists() {
 		File rootDirectory = new File("JLevel-Data");
 		File playerDirectory = new File("JLevel-Data/Players");
 		File skillDirectory = new File("JLevel-Data/Skills");
 		
 		if (!rootDirectory.exists()) {
 			rootDirectory.mkdir();
 		}
 		
 		if (!playerDirectory.exists()) {
 			playerDirectory.mkdir();
 		}
 		
 		if (!skillDirectory.exists()) {
 			skillDirectory.mkdir();
 		}
 	}
 	
 	public static void createPlayerDatabaseIfNotExists(Player player) {
 		if (!playerTableExists(player)) {
 			Connection conn = null;
 			Statement st = null;
 			try {
 				Class.forName("org.sqlite.JDBC");
 				conn = DriverManager.getConnection(playerDatabasePath(player));
 				st = conn.createStatement();
 				
 				String update = "CREATE TABLE `" + player.getName() + "` (" +
 					"`id` INTEGER PRIMARY KEY," +
 					"`skillName` varchar(32)," +
 					"`skillLevel` INTEGER," +
 					"`levelExp` INTEGER," +
 					"`nextLevelExp` INTEGER," +
 					"`totalExp` INTEGER" + ");";
 				
 				st.executeUpdate(update);
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Create Table Exception", e);
 			} catch (ClassNotFoundException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 			} finally {
 				try {
 					if (conn != null)
 						conn.close();
 					if (st != null)
 						st.close();
 				} catch (SQLException e) {
 					LOG.log(Level.SEVERE, "[JLEVEL]: Could not create the table (on close)");
 				}
 			}
 		}
 	}
 	
 	public static void createSkillDatabaseIfNotExists(String skill) { // , String[] itemRules, String[] expRules, String[] expTable) {
 		Connection conn = null;
 		Statement st = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection(skillDatabasePath(skill));
 			st = conn.createStatement();
 			
 			if (!itemRulesTableExistsForSkill(skill)) {
 				String itemRulesUpdate = "CREATE TABLE `itemRules` (" +
 					"`id` INTEGER PRIMARY KEY," +
 					"`itemId` INTEGER," +
					"`level` INTEGER" + ");";
 				st.executeUpdate(itemRulesUpdate);
 			}
 			
 			if (!expRulesTableExistsForSkill(skill)) {
 				String expRulesUpdate = "CREATE TABLE `expRules` (" +
 					"`id` INTEGER PRIMARY KEY," +
 					"`action` varchar(32)," +
 					"`receiver` varchar(32)," + 
 					"`receiverState` varchar(32)," +
 					"`exp` INTEGER" + ");";
 				st.executeUpdate(expRulesUpdate);
 			}
 			
 			if (!expLevelsTableExistsForSkill(skill)) {
 				String expLevelsUpdate = "CREATE TABLE `expLevels` (" +
 					"`id` INTEGER PRIMARY KEY," +
 					"`level` INTEGER," +
 					"`expNeeded` INTEGER" + ");";
 				st.executeUpdate(expLevelsUpdate);
 			}			
 			
 		} catch (SQLException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Create Table Exception", e);
 		} catch (ClassNotFoundException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 		} finally {
 			try {
 				if (conn != null)
 					conn.close();
 				if (st != null)
 					st.close();
 			} catch (SQLException e) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Could not create the table (on close)");
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	public static boolean playerTableExists(Player player) {
 		return tableExists(playerDatabasePath(player), player.getName());
 	}
 	
 	public static boolean itemRulesTableExistsForSkill(String skill) {
 		return tableExists(skillDatabasePath(skill), "itemRules");
 	}
 	
 	public static boolean expRulesTableExistsForSkill(String skill) {
 		return tableExists(skillDatabasePath(skill), "expRules");
 	}
 	
 	public static boolean expLevelsTableExistsForSkill(String skill) {
 		return tableExists(skillDatabasePath(skill), "expLevels");
 	}
 	
 	public static boolean tableExists(String connectionPath, String tableName) {
 		Connection conn = null;
 		ResultSet rs = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection(connectionPath);
 			DatabaseMetaData dbm = conn.getMetaData();
 			rs = dbm.getTables(null, null, tableName, null);
 			if (!rs.next())
 				return false;
 			return true;
 		} catch (SQLException ex) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Table Check Exception", ex);
 			return false;
 		} catch (ClassNotFoundException e) {
 			LOG.log(Level.SEVERE, "[JLEVEL]: Error loading org.sqlite.JDBC");
 			return false;
 		} finally {
 			try {
 				if (rs != null)
 					rs.close();
 				if (conn != null)
 					conn.close();
 			} catch (SQLException ex) {
 				LOG.log(Level.SEVERE, "[JLEVEL]: Table Check SQL Exception (on closing)");
 			}
 		}
 	}
 	
 	
 	
 	
 	private static String playerDatabasePath(Player player) {
 		return PLAYER_DB_DIRECTORY + player.getName() + ".db";
 	}
 	
 	private static String skillDatabasePath(String skill) {
 		return SKILL_DB_DIRECTORY + skill + ".db";
 	}
 	
 }
