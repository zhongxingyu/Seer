 package com.voracious.dragons.server;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 public class DBHandler {
 	public static final String dbfile = "game_data.sqlite";
 	private static final String[] tableNames = { "Game", "Player", "Winner",
 			"Turn", "Spectator" };
 	private static Logger logger = Logger.getLogger(DBHandler.class);
 
 	public void init() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
 
 			// Check if the tables are already there or not
 			DatabaseMetaData meta = connection.getMetaData();
 			ResultSet tableResults = meta.getTables(null, null, "%", null);
 
 			ArrayList<String> tables = new ArrayList<String>();
 			while (tableResults.next()) {
 				tables.add(tableResults.getString("TABLE_NAME"));
 			}
 			
 			String[] tablesInDb = {null};
 			tablesInDb = tables.toArray(tablesInDb);
 
 			Arrays.sort(tablesInDb);
 			Arrays.sort(tableNames);
 			
 			if (!tablesInDb.equals(tableNames)) {
 				connection.close();
 				new File(dbfile).delete();
 				connection = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
 				createDatabase(connection);
 			}

		} catch (ClassNotFoundException | SQLException e) {
 			logger.error("Could not load database", e);
 		}
 	}
 
 	private void createDatabase(Connection conn) {
 		Statement query;
 		try {
 			query = conn.createStatement();
 			query.executeUpdate("CREATE TABLE Player (pid VARCHAR(15) PRIMARY KEY NOT NULL, passhash CHAR(60) NOT NULL)");
 			query.executeUpdate("CREATE TABLE Game (gid INTEGER PRIMARY KEY AUTOINCREMENT, pid1 VARCHAR(15) NOT NULL REFERENCES Player(pid), " +
 					            "pid2 VARCHAR(15) NOT NULL REFERENCES Player(pid), inProgress BOOLEAN NOT NULL, gameState VARCHAR(20))");
 			query.executeUpdate("CREATE TABLE Winner (gid INTEGER PRIMARY KEY NOT NULL REFERENCES Game(gid), pid VARCHAR(15) NOT NULL REFERENCES Player(pid))");
 			query.executeUpdate("CREATE TABLE Spectator (gid INTEGER PRIMARY KEY NOT NULL REFERENCES Game(gid), pid VARCHAR(15) NOT NULL REFERENCES Player(pid))");
 			query.executeUpdate("CREATE TABLE Turn (gid INTEGER NOT NULL REFERENCES Game(gid), tnum INTEGER NOT NULL, timeStamp DATETIME NOT NULL DEFAULT CURRENT_TIME," +
 					            "turnString VARCHAR(60) NOT NULL, PRIMARY KEY(gid, tnum))");
 		} catch (SQLException e) {
 			logger.error("Could not create tables", e);
 		}
 	}
 }
