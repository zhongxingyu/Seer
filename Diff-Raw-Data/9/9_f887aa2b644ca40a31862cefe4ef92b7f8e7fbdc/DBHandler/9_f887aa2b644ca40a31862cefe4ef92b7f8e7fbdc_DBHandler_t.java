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
 	private PreparedStatement numGames,numWins,aveTurn,numTuples,times,latestTurn;
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
 			//TODO: There are extra tables that happen to come after ours in the sort get rid of those
 			
 			Arrays.sort(tablesInDb);
 			Arrays.sort(tableNames);
 
 			boolean equal = true;
 			for(int i=0; i<tableNames.length; i++){
 				if(tablesInDb[i] == null){
 					equal = false;
 					break;
 				}
 				
 				if(!tablesInDb[i].equals(tableNames[i])){
 					equal = false;
 				}
 			}
 			
 			if (!equal) {
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
 			checkHash = conn.prepareStatement("SELECT passhash " +
 					                          "FROM Player " +
 					                          "WHERE pid = ?");
 			
 			registerUser = conn.prepareStatement("INSERT INTO Player VALUES(?, ?)");
 			
 			numGames = conn.prepareStatement(
 					"SELECT sum(answer) AS numGames" +
 					"FROM 	(SELECT count(pid1) AS answer" +
 					"		FROM Game" +
 					"		WHERE pid1=? AND inProgress=?" +
 					"		GROUP By pid1" +
 					"		UNION" +
 					"		SELECT count(pid2) AS answer" +
 					"		FROM Game" +
 					"		WHERE pid2=? AND inProgress=?" +
 					"		GROUP By pid2);");
 			
 			numWins=conn.prepareStatement(
 					"SELECT count(gid) AS answer " +
 					"FROM Winner " +
 					"WHERE pid=? " +
 					"GROUP BY pid;");
 			
 			aveTurn=conn.prepareStatement(
 					"SELECT count(*) AS answer " +
 					"FROM Turn " +
 					"WHERE pid=? " +
 					"GROUP BY gid, tnum;");
 			numTuples=conn.prepareStatement(
 					"SELECT count(*) AS answer " +
 					"FROM Turn " +
 					"WHERE pid=? " +
 					"GROUP BY gid,tnum;");
 			times=conn.prepareStatement(
 					"SELECT gid AS GID, tNUM as TNUM, timeStamp AS TIMESTAMP " +
 					"FROM Turn " +
 					"WHERE pid=? " +
 					"ORDER BY gid ASC,tNum ASC; ");
 			latestTurn=conn.prepareStatement(
 					"SELECT pid as id, MAX(tnum) AS answer " +
 					"FROM Turn" +
 					"WHERE gid=?" +
 					"GROUP BY pid;");
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
 			logger.error("Could not add to the winner table",e);
 		}
 		
 		
 	}
 	
 	public void insertSpectator(int GID,String PID){
 		try {
 			storeSpect.setInt(1, GID);
 			storeSpect.setString(2, PID);
 			storeSpect.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("Could not add to the specatator table",e);
 		}
 		
 		
 	}
 	
 	public boolean insertTurn(int GID,int TNUM,String PID,String TURNSTRING){
 		try{
 			storeTurn.setInt(1,GID);
 			storeTurn.setInt(2, TNUM);
 			
 			latestTurn.setInt(1, GID);
 			ResultSet ret=latestTurn.executeQuery();
 			if(!ret.next())
 				return false;
 			String p1id = ret.getString("id");
 			int p1turnNum = ret.getInt("answer");			
 			if(!ret.next())
 				return false;
 			String p2id = ret.getString("id");
 			int p2turnNum = ret.getInt("answer");
 			if(PID==p1id && p1turnNum > p2turnNum)
 				return false;
 			if(PID==p2id && p2turnNum > p1turnNum)
 				return false;
 			storeTurn.setString(4, PID);
 			storeTurn.setString(5, TURNSTRING);
 			storeTurn.executeUpdate();
 			return true;
 		}
 		catch(SQLException e){
 			logger.error("Could not add to the turn table", e);
 			return false;
 		}
 	}
 	
 	public String getPasswordHash(String pid){
 		try {
 			checkHash.setString(1, pid);
 			ResultSet rs = checkHash.executeQuery();
 			if(!rs.next()){
 				return null;
 			}
 
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
 
 	public int numGames(String PID, int inPlay){
 		//the int is to know if the games being counted are over or still occurring
 		//0 false
 		//1 true
 		try{
 		numGames.setString(1, PID);
 		numGames.setString(3, PID);
 		numGames.setLong(2, inPlay);
 		numGames.setLong(4, inPlay);
 		ResultSet ret=numGames.executeQuery();
 		return ret.getInt("numGames");
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
 		if(totalNumGames==0)
 			return 0.0;
 		
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
 			
 			if(numberTuples==0)
 				return 0;
 			
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
