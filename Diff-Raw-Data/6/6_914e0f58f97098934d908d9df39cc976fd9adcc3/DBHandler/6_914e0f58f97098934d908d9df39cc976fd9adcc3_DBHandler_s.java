 package com.voracious.dragons.server;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.PreparedStatement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 public class DBHandler {
 	public static final String dbfile = "game_data.sqlite";
 	private static final String[] tableNames = { "Game", "Player", "Winner", "Turn", "Spectator" };
 	private static Logger logger = Logger.getLogger(DBHandler.class);
 	private Connection conn;
 
 	private PreparedStatement checkHash;
 	private PreparedStatement registerUser;
 	private PreparedStatement numGames,numWins,aveTurn,numTuples,times;
 	private PreparedStatement storeTurn,storeSpect,storeWinner,storeGame;
 	
 	public void init() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
 
 			// Check if the tables are already there or not
 			DatabaseMetaData meta = conn.getMetaData();
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
 				conn.close();
 				new File(dbfile).delete();
 				conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
 				createDatabase();
 			}
 			
 			prepareStatements();
 		} catch (ClassNotFoundException e) {
 			logger.error("Could not load database", e);
 		} catch (SQLException e) {
 			logger.error("Could not load database", e);
 		}
 	}
 	
 	private void prepareStatements(){
 		try {
 			
 			checkHash = conn.prepareStatement("SELECT passhash" +
 					                          "FROM Player" +
 					                          "WHERE pid = ?");
 			
 			registerUser = conn.prepareStatement("INSERT INTO Player VALUES(?, ?)");
 			
 			numGames = conn.prepareStatement(
 					"SELECT count(gid) AS answer" +
 					"FROM Game" +
 					"WHERE (pid1=? OR pid2=?) AND inProgress=?" +
 					"GROUP BY gid;");
 			
 			numWins=conn.prepareStatement(
 					"SELECT count(gid) AS answer" +
 					"FROM Winner" +
 					"WHERE pid=?" +
 					"GROUP BY gid;");
 			
 			aveTurn=conn.prepareStatement(
 					"SELECT count(*) AS answer" +
 					"FROM Turn" +
 					"WHERE pid=?" +
 					"GROUP BY gid, tnum;");
 			numTuples=conn.prepareStatement(
 					"SELECT count(*) AS answer" +
 					"FROM Turn" +
 					"WHERE pid=?" +
 					"GROUP BY gid,tnum;");
 			times=conn.prepareStatement(
 					"SELECT gid AS GID, tNUM as TNUM, timeStamp AS TIMESTAMP" +
 					"FROM Turn" +
 					"WHERE pid=?" +
 					"ORDER BY gid ASC,tNum ASC;");
 			//possible to sort the time stamps also, so each games is in order from top to bottom
 			//g0 t0
 			//g0 t1
 			//g1 t0
 			//g2 t0
 			//g2 t1
 			//g2 t2
 			
 			storeTurn=conn.prepareStatement(
 					"INSERT INTO Turn VALUES(?,?,?,?,?);");
 			
 			storeSpect=conn.prepareStatement(
 					"INSERT INTO Spectator VALUES(?,?);");
 			storeWinner=conn.prepareStatement(
 					"INSERT INTO Winner VALUES(?,?);");
 			storeGame=conn.prepareStatement(
 					"INSERT INTO Game (pid1,pid2,gameState) VALUES(?,?,?)");
 			//assuming a game inserted will be inprogress
 		} catch (SQLException e) {
 			logger.error("Error preparing statements", e);
 		}
 	}
 
 	private void createDatabase() {
 		Statement query;
 		try {
 			query = conn.createStatement();
 			query.executeUpdate("CREATE TABLE Player (pid VARCHAR(15) PRIMARY KEY NOT NULL," +
 					            "\n                     passhash CHAR(60) NOT NULL)");
 			
 			query.executeUpdate("CREATE TABLE Game (gid INTEGER PRIMARY KEY AUTOINCREMENT," +
 					            "\n                   pid1 VARCHAR(15) NOT NULL REFERENCES Player(pid), " +
 					            "\n                   pid2 VARCHAR(15) NOT NULL REFERENCES Player(pid)," +
 					            "\n                   inProgress BOOLEAN NOT NULL," +
 					            "\n                   gameState VARCHAR(20))");
 			
 			query.executeUpdate("CREATE TABLE Winner (gid INTEGER PRIMARY KEY NOT NULL REFERENCES Game(gid)," +
 					            "\n                     pid VARCHAR(15) NOT NULL REFERENCES Player(pid))");
 			
 			query.executeUpdate("CREATE TABLE Spectator (gid INTEGER PRIMARY KEY NOT NULL REFERENCES Game(gid)," +
 					            "\n                        pid VARCHAR(15) NOT NULL REFERENCES Player(pid))");
 			
 			query.executeUpdate("CREATE TABLE Turn (gid INTEGER NOT NULL REFERENCES Game(gid)," +
 					            "\n                   tnum INTEGER NOT NULL," +
 					            "\n                   timeStamp DATETIME NOT NULL DEFAULT CURRENT_TIME," +
 					            "\n                   pid VARCHAR(15) NOT NULL REFERENCES Player(pid)," +
 					            "\n                   turnString VARCHAR(60) NOT NULL," +
 					            "\n                   PRIMARY KEY(gid, pid, tnum))");
 		} catch (SQLException e) {
 			logger.error("Could not create tables", e);
 		}
 	}
 	
 	public void insertGame(String PID1,String PID2,String GAMESTATE){
 		try {
 			storeGame.setString(1, PID1);
 			storeGame.setString(2, PID2);
 			storeGame.setString(3, GAMESTATE);
 			storeGame.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("Could not add to the game table",e);
 		}
 	}
 	
 	public void insertWinner(int GID,String PID){
 		try {
 			storeWinner.setInt(1, GID);
 			storeWinner.setString(2, PID);
 			storeWinner.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("Coud not add to the winner table",e);
 		}
 		
 		
 	}
 	
 	public void insertSpectator(int GID,String PID){
 		try {
 			storeSpect.setInt(1, GID);
 			storeSpect.setString(2, PID);
 			storeSpect.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("Could not add to teh specatator table",e);
 		}
 		
 		
 	}
 	
 	public void insertTurn(int GID,int TNUM,Timestamp TIME,String PID,String TURNSTRING){
 		try{
 			storeTurn.setInt(1,GID);
 			storeTurn.setInt(2, TNUM);
 			storeTurn.setTimestamp(3, TIME);
 			storeTurn.setString(4, PID);
 			storeTurn.setString(5, TURNSTRING);
 			storeTurn.executeUpdate();
 		}
 		catch(SQLException e){
 			logger.error("Coul not add to the turn table", e);
 		}
 	}
 	
 	public String getPasswordHash(String pid){
 		try {
 			checkHash.setString(1, pid);
 			ResultSet rs = checkHash.executeQuery();
 			return rs.getString("passhash");
 		} catch (SQLException e) {
 			logger.error("Could not check hash", e);
 			return null;
 		}
 	}
 	
 	public void registerUser(String uid, String passhash){
 		try {
 			registerUser.setString(1, uid);
 			registerUser.setString(2, passhash);
 			registerUser.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("Could not register user", e);
 		}
 	}
 
 	public int numGames(String PID, boolean inPlay){
 		//the boolean is to know if the games being counted are over or still occurring
 		try{
 		numGames.setString(1, PID);
 		numGames.setString(2, PID);
 		numGames.setString(3,inPlay+"");//does "true" == true+"" ?
 		ResultSet ret=numGames.executeQuery();
 		return ret.getInt("answer");
 		}
 		catch(SQLException e){
 			logger.error("Could not count the number of games",e);
 			return -1;
 		}
 	}
 	
 	public int countWins(String PID){
 		try{
 		numWins.setString(1,PID);
 		ResultSet ret=numWins.executeQuery();
 		return ret.getInt("answer");
 		}
 		catch(SQLException e){
 			logger.error("Could not count the number of wins",e);
 			return -1;
 		}
 	}
 	
 	public double aveTurns(String PID,int totalNumGames) {
 		//the totalNumGames = done + current games, so it needs the other qureies to be done first
 		
 		try {
 			aveTurn.setString(1, PID);
 			ResultSet ret=aveTurn.executeQuery();
 			int numTurns=ret.getInt("answer");
 			return numTurns/totalNumGames;
 		} catch (SQLException e) {
 			logger.error("Could not count the ave number of turns", e);
 			return -1.0;
 		}
 	}
 	
 	public long aveTime(String PID){
 		//the group by is only there to have an arrogate query
 		try {
 			numTuples.setString(1, PID);
 			ResultSet tuples=numTuples.executeQuery();
 			int numberTuples=tuples.getInt("answer");
 			
 			times.setString(1, PID);
 			ResultSet timeRes=times.executeQuery();
 			
 			long sum=0;
 			//calculates the total time
 			Timestamp temp=new Timestamp(0),current;
 			while(timeRes.next()){
 				int turnCounter=timeRes.getInt("TNUM");//readin's turn num
 
 				if(turnCounter==0){
 					//set that reading's timestamp as the temp
 					temp=timeRes.getTimestamp("TIMESTAMP");
 				}
 				else {
 					current=timeRes.getTimestamp("TIMESTAMP");
 					sum+=current.getTime()-temp.getTime();
 					temp=current;
 
 				}
 			}
 			
 			sum/=numberTuples;
 			return sum;
 		} catch (SQLException e) {
 			logger.error("Could not count the ave time between turns", e);
 			return -1;
 		}
 	}
 }
