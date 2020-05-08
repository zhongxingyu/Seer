 package edu.berkeley.cs.cs162.Server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import edu.berkeley.cs.cs162.Synchronization.ReaderWriterLock;
 
 /**
  * DatabaseConnection abstracts a connection to a remote database.
  * 
  * Supported operations include writing game state, loading gameserver state
  * writing and loading client info.
  * 
  * This abstraction should also provide synchronization. The callers of these 
  * methods will assume correctness over multiple threads.
  * 
  * @author xshi
  *
  */
 public class DatabaseConnection {
 	private Connection canonicalConnection;
 	ReaderWriterLock dataLock;
 	
 	public DatabaseConnection(String databasePath) throws SQLException
 	{
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			System.err.println("Could not find sqlite JDBC class. Did you include the correct jar in the build path?");
 		}
 	    canonicalConnection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
         //canonicalConnection.setAutoCommit(false);
         dataLock = new ReaderWriterLock();
         initializeDatabase();
 	}
 
     /**
      * Initializes the database, creating the necessary tables
      */
     public void initializeDatabase(){
         startTransaction();
         try{
             executeWriteQuery("create table if not exists clients (clientId integer primary key autoincrement, name text unique not null, type int not null, passwordHash text not null)");
             executeWriteQuery("create table if not exists games (gameId integer primary key autoincrement, blackPlayer int references clients (clientId) not null, whitePlayer int references clients (clientId) not null, boardSize int not null, blackScore real, whiteScore real, winner int references clients (clientId), moveNum int not null, reason int)");
             executeWriteQuery("create table if not exists moves (moveId integer primary key autoincrement, clientId int references clients (clientId) not null, gameId int references games (gameId) not null, moveType int not null, x int, y int, moveNum int not null)");
             executeWriteQuery("create table if not exists captured_stones (stoneId integer primary key autoincrement, moveId int references moves (moveId), x int, y int)");
             finishTransaction();
         }
         catch(SQLException e){
             e.printStackTrace();
             abortTransaction();
         }
     }
 	
 	/**
 	 * Starts a transaction. It will not be committed or be interrupted until finish transaction is called.
 	 * 
 	 * @return the connection to start the transaction.
 	 */
 	public Connection startTransaction() {
 		dataLock.writeLock();
 		try {
 			canonicalConnection.setAutoCommit(false);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return canonicalConnection;
 	}
 	
 	/**
 	 * Unlocks the write lock, and commits the transaction.
 	 */
 	public void finishTransaction() {
 		try {
 			canonicalConnection.commit();
 			canonicalConnection.setAutoCommit(true);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		finally {
 			dataLock.writeUnlock();
 		}
 	}
 
     /**
      * Aborts the current transaction. Used in the case of an SQLException while writing.
      */
     public void abortTransaction() {
         try {
             canonicalConnection.rollback();
             canonicalConnection.setAutoCommit(true);
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         finally {
             dataLock.writeUnlock();
         }
     }
 
 	/**
 	 * Executes a single read.
      * Remember to call closeReadQuery() on the result when you're done!
      *
 	 * @param query
 	 * @return
 	 * @throws SQLException
 	 */
 	public ResultSet executeReadQuery(String query) {
 		dataLock.readLock();
 		Statement readQuery = null;
 		ResultSet rs = null;
 
         try {
 			readQuery = canonicalConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             rs = readQuery.executeQuery(query);
 		}
         catch (SQLException e) {
 		    e.printStackTrace();
 		}
 
 		return rs;
 	}
 
     /**
      * Used to close a ResultSet and its corresponding Statement after using its data.
      * It also unlocks the readLock, to prevent another thread from writing to the database
      * while the Statement is open.
      *
      * USE THIS -EVERY- TIME YOU EXECUTE A READ QUERY!
      * (ESPECIALLY BEFORE EXECUTING A WRITE QUERY!)
      *
      * @param rs - The currently open ResultSet
      */
     public void closeReadQuery(ResultSet rs){
         try { rs.getStatement().close(); }
         catch (SQLException e) { /* Do nothing... */ }
         dataLock.readUnlock();
     }
 
     public int getPlayerID(String name) throws SQLException {
         ResultSet result = executeReadQuery("select clientId from clients where name='" + name + "'");
         result.next();
         int id = result.getInt("clientId");
         closeReadQuery(result);
         return id;
     }
 
     public int getGameID(int black, int white) throws SQLException {
         ResultSet result = executeReadQuery("select gameId from games where blackPlayer=" + black + ", whitePlayer=" + white);
         result.next();
         int id = result.getInt("gameId");
         closeReadQuery(result);
         return id;
     }
 
     public int getGameID(Game game) throws SQLException {
         int white = getPlayerID(game.getWhitePlayer().getName());
         int black = getPlayerID(game.getBlackPlayer().getName());
         return getGameID(black, white);
     }
 
     public int getMoveNum(int gameID) throws SQLException {
         ResultSet result = executeReadQuery("select moveNum from games where gameId=" + gameID);
         result.next();
         int moveNum = result.getInt("moveNum");
         closeReadQuery(result);
         return moveNum;
     }
 
 	/**
 	 * Executes a single write
 	 * @param query
 	 * @throws SQLException
      * @return true if the write operation was successful, false otherwise.
 	 */
 	public int executeWriteQuery(String query) throws SQLException{
 
 		Statement writeQuery = null;
         int generatedKey = -1;
 
 		try {
 			writeQuery = canonicalConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 		    writeQuery.execute(query);
             ResultSet keys = writeQuery.getGeneratedKeys();
            generatedKey = keys.getInt(0);
             writeQuery.close();
 		}
         catch (SQLException e) {
 		    e.printStackTrace();
             if (writeQuery != null) writeQuery.close();
             throw e; // This needs to be caught upstream so that the transaction can be aborted.
         }
 
        assert generatedKey != -1;
         return generatedKey;
 	}
 
     /**
      * TESTING PURPOSES ONLY. This wipes the database clean; used by the AuthenticationManagerTest.
      */
     public void wipeDatabase(){
         startTransaction();
         try{
             executeWriteQuery("drop table if exists clients");
             executeWriteQuery("drop table if exists games");
             executeWriteQuery("drop table if exists moves");
             executeWriteQuery("drop table if exists captured_stones");
             finishTransaction();
         }
         catch(SQLException e){
             e.printStackTrace();
             abortTransaction();
         }
     }
 }
