 package com.mistphizzle.donationpoints.plugin;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.mistphizzle.donationpoints.sql.Database;
 import com.mistphizzle.donationpoints.sql.MySQLConnection;
 import com.mistphizzle.donationpoints.sql.SQLite;
 import com.mysql.jdbc.DatabaseMetaData;
 
 public final class DBConnection {
 
 	public static Database sql;
 
 	public static String engine;
 	public static String sqlite_db;
 	public static String host;
 	public static int port;
 	public static String db;
 	public static String user;
 	public static String pass;
 	public static String playerTable;
 	public static String transactionTable;
 	public static String frameTable;
 
 	public static void init() {
 		if (engine.equalsIgnoreCase("mysql")) {
 			sql = new MySQLConnection(DonationPoints.log, "[DonationPoints] Establishing Database Connection...", host, port, user, pass, db);
 			((MySQLConnection) sql).open();
 			DonationPoints.log.info("[DonationPoints] Etablishing Database Connection...");
 
 			
 			if (sql.tableExists("points_players")) {
 				DonationPoints.log.info("Renaming points_players to " + playerTable + ".");
 				sql.modifyQuery("RENAME TABLE points_players TO " + playerTable);
 			}
 			
 			DonationPoints.log.info("Making sure all data is correct.");
 			sql.modifyQuery("UPDATE " + playerTable + " SET player = lower(player)");
 			
 			if (!sql.tableExists(playerTable)) {
 				DonationPoints.log.info("Creating " + playerTable + " table.");
 				String query = "CREATE TABLE IF NOT EXISTS `" + playerTable + "` ("
 						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
 						+ "`player` TEXT(32),"
 						+ "`balance` double,"
 						+ " PRIMARY KEY (id));";
 				sql.modifyQuery(query);
 			}
 
 			if (!sql.tableExists(transactionTable)) {
 				DonationPoints.log.info("Creating " + transactionTable + " table");
 				String query = "CREATE TABLE `" + transactionTable + "` ("
 						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
 						+ "`player` TEXT(32),"
 						+ "`package` TEXT(255),"
 						+ "`price` double,"
 						+ "`date` VARCHAR(32),"
 						+ "`activated` VARCHAR(32),"
 						+ "`expires` VARCHAR(32),"
 						+ "`expiredate` VARCHAR(32),"
 						+ "`expired` VARCHAR(32),"
 						+ "`server` VARCHAR(255),"
 						+ " PRIMARY KEY (id));";
 				sql.modifyQuery(query);
 			}
 			if (!sql.tableExists(frameTable)) {
 				DonationPoints.log.info("Creating " + frameTable + " table");
 				String query = "CREATE TABLE `" + frameTable + "` ("
 						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
 						+ "`x` double,"
 						+ "`y` double,"
 						+ "`z` double,"
 						+ "`world` TEXT(255),"
 						+ "`package` TEXT(255),"
 						+ "`server` VARCHAR(255),"
 						+ " PRIMARY KEY (id));";
 				sql.modifyQuery(query);
 			}
 			java.sql.DatabaseMetaData md;
 			try {
 				md = sql.getConnection().getMetaData();
 				ResultSet rs1 = md.getColumns(null, null, transactionTable, "server");
 				ResultSet rs2 = md.getColumns(null, null, frameTable, "server");
 				if (!rs1.next()) {
					sql.modifyQuery("ALTER TABLE " + transactionTable + " ADD server STRING(255)");
 				}
 				if (!rs2.next()) {
					sql.modifyQuery("ALTER TABLE " + frameTable + " ADD server STRING(255)");
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			/*
 			 * Everything below this line is for the sqlite connections.
 			 * Will only work if the player has "sqlite" for the engine in their config. 
 			 * sqlite can be in any case.
 			 * Generates the tables points_players, points_transactions, and points_cumulative and logs them into the console.
 			 */
 		} else if (engine.equalsIgnoreCase("sqlite")) {
 			sql = new SQLite(DonationPoints.log, "[DonationPoints] Establishing SQLite Connection.", sqlite_db, DonationPoints.getInstance().getDataFolder().getAbsolutePath());
 			((SQLite) sql).open();
 			
 			if (sql.tableExists("points_players")) {
 				DonationPoints.log.info("Renaming points_players to " + playerTable);
 				sql.modifyQuery("RENAME TABLE points_players TO " + playerTable);
 			}
 
 			if (!sql.tableExists(playerTable)) {
 				DonationPoints.log.info("Creating " + playerTable + " table.");
 				String query = "CREATE TABLE `" + playerTable + "` ("
 						+ "`player` TEXT(32),"
 						+ "`balance` DOUBLE(255));";
 				sql.modifyQuery(query);
 			}
 
 			if (!sql.tableExists(transactionTable)) {
 				DonationPoints.log.info("Creating " + transactionTable + " table.");
 				String query = "CREATE TABLE `" + transactionTable + "` ("
 						+ "`player` TEXT(32),"
 						+ "`package` TEXT(255),"
 						+ "`price` DOUBLE(255),"
 						+ "`date` STRING(32),"
 						+ "`activated` STRING(32),"
 						+ "`expires` STRING(32),"
 						+ "`expiredate` STRING(32),"
 						+ "`server` STRING(255),"
 						+ "`expired` STRING(32));";
 				
 				sql.modifyQuery(query);
 			}
 			
 			if (!sql.tableExists(frameTable)) {
 				DonationPoints.log.info("Creating " + frameTable + " table");
 				String query = "CREATE TABLE `" + frameTable + "` ("
 						+ "`x` double,"
 						+ "`y` double,"
 						+ "`z` double,"
 						+ "`world` TEXT(255),"
 						+ "`server` VARCHAR(255),"
 						+ "`package` TEXT(255));";
 				sql.modifyQuery(query);
 			}
 			java.sql.DatabaseMetaData md;
 			try {
 				md = sql.getConnection().getMetaData();
 				ResultSet rs1 = md.getColumns(null, null, transactionTable, "server");
 				ResultSet rs2 = md.getColumns(null, null, frameTable, "server");
 				if (!rs1.next()) {
 					sql.modifyQuery("ALTER TABLE " + transactionTable + " ADD server STRING(255)");
 				}
 				if (!rs2.next()) {
 					sql.modifyQuery("ALTER TABLE " + frameTable + " ADD server STRING(255)");
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} else {
 			DonationPoints.log.info("[DonationPoints] Unknown value for SQL Engine, expected sqlite or mysql.");
 		}
 	}
 
 }
