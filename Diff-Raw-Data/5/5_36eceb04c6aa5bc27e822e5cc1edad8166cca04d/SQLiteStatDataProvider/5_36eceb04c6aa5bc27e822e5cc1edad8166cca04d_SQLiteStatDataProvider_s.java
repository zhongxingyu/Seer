 package me.tehbeard.BeardStat.DataProviders;
 
 import java.sql.*;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 
 
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStat;
 import me.tehbeard.BeardStat.containers.PlayerStatBlob;
 import me.tehbeard.BeardStat.containers.StaticPlayerStat;
 import me.tehbeard.BeardStat.scoreboards.Scoreboard;
 import me.tehbeard.BeardStat.scoreboards.ScoreboardEntry;
 
 /**
  * Provides backend storage to a mysql database
  * @author James
  *
  */
 public class SQLiteStatDataProvider implements IStatDataProvider {
 
     protected Connection conn;
 
     private String filename;
     private String table;
 
     //protected static PreparedStatement prepGetPlayerStat;
     protected static PreparedStatement prepGetAllPlayerStat;
     protected static PreparedStatement prepSetPlayerStat;
 
     protected static PreparedStatement keepAlive;
 
     private HashMap<String,HashSet<PlayerStat>> writeCache = new HashMap<String,HashSet<PlayerStat>>();
 
     public SQLiteStatDataProvider(String filename,String table) throws SQLException{
 
         this.filename = filename;
         this.table = table;
         try {
             Class.forName("org.sqlite.JDBC");
 
             createConnection();
 
             checkAndMakeTable();
             prepareStatements();
             if(conn == null){
                 throw new SQLException("Failed to start");
             }
         } catch (ClassNotFoundException e) {
            BeardStat.printCon("MySQL Library not found!");
         }
 
 
 
     }
 
     /**
      * Connection to the database.
      * @throws SQLException
      */
     private void createConnection() {
         String conUrl = String.format("jdbc:sqlite:%s",filename);
 
         BeardStat.printCon("Connecting....");
 
         try {
             conn = DriverManager.getConnection(conUrl);
             //conn.setAutoCommit(false);
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
             conn = null;
         }
 
     }
 
 
 
     protected void checkAndMakeTable(){
         BeardStat.printCon("Checking for table");
 
         try{
             ResultSet rs = conn.getMetaData().getTables(null, null, table, null);
             if (!rs.next()) {
                 BeardStat.printCon("Stats table not found, creating table");
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + table + "` ("+
                         " `player` varchar(32) NOT NULL DEFAULT '-',"+
                         " `category` varchar(32) NOT NULL DEFAULT 'stats',"+
                         " `stat` varchar(32) NOT NULL DEFAULT '-',"+
                         " `value` int(11) NOT NULL DEFAULT '0',"+
                         " PRIMARY KEY (`player`,`category`,`stat`)"+
                         ");");
 
                 ps.executeUpdate();
                 ps.close();
                 BeardStat.printCon("created table");
             }
             else
             {
                 BeardStat.printCon("Table found");
             }
             rs.close();
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }		
     }
 
     protected void prepareStatements(){
         try{
             BeardStat.printDebugCon("Preparing statements");
 
             keepAlive = conn.prepareStatement("SELECT COUNT(*) from `" + table + "`");
             prepGetAllPlayerStat = conn.prepareStatement("SELECT * FROM " + table + " WHERE player=?");
             BeardStat.printDebugCon("Player stat statement created");
 
             prepSetPlayerStat = conn.prepareStatement("INSERT OR REPLACE INTO `" + table + "`" +
                     "(`player`,`category`,`stat`,`value`) " +
                     "values (?,?,?,?); ");
 
             BeardStat.printDebugCon("Set player stat statement created");
            BeardStat.printCon("Initaised MySQL Data Provider.");
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }
     }
 
 
 
     public PlayerStatBlob pullPlayerStatBlob(String player) {
         return pullPlayerStatBlob(player,true);
     }
 
     public PlayerStatBlob pullPlayerStatBlob(String player, boolean create) {
         try {
 
             long t1 = (new Date()).getTime();
             PlayerStatBlob pb = null;
 
             //try to pull it from the db
             prepGetAllPlayerStat.setString(1, player);
             ResultSet rs = prepGetAllPlayerStat.executeQuery();
             pb = new PlayerStatBlob(player,"");
             while(rs.next()){
                 //`category`,`stat`,`value`
                 PlayerStat ps = new StaticPlayerStat(rs.getString(2),rs.getString(3),rs.getInt(4));
                 pb.addStat(ps);
             }
             rs.close();
 
             BeardStat.printDebugCon("time taken to retrieve: "+((new Date()).getTime() - t1) +" Milliseconds");
             if(pb.getStats().size()==0 && create==false){return null;}
 
             return pb;
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }
         return null;
     }
 
     public void pushPlayerStatBlob(PlayerStatBlob player) {
 
         synchronized (writeCache) {
 
 
             HashSet<PlayerStat> copy = writeCache.containsKey(player.getName()) ? writeCache.get(player.getName()) : new HashSet<PlayerStat>();
 
             for(PlayerStat ps : player.getStats()){
                 if(ps.isArchive()){
 
                     PlayerStat ns = new  StaticPlayerStat(ps.getCat(),ps.getName(),ps.getValue());
                     copy.add(ns);
                 }
             }
 
             if(!writeCache.containsKey(player.getName())){
                 writeCache.put(player.getName(), copy);
             }
         }
 
     }
 
     public void flush() {
 
         new Thread(new Runnable() {
 
             public void run() {
                 synchronized (writeCache) {
 
 
 
                     long t = System.currentTimeMillis();
                     BeardStat.printDebugCon("Saving to database");
                     for(Entry<String, HashSet<PlayerStat>> entry : writeCache.entrySet()){
                         try {
                             HashSet<PlayerStat> pb = entry.getValue();
 
                             BeardStat.printDebugCon(entry.getKey() + " " + entry.getValue() +  " [" + pb.size() + "]");
                             prepSetPlayerStat.clearBatch();
                             for(PlayerStat ps : pb){
 
                                 prepSetPlayerStat.setString(1, entry.getKey());
 
                                 prepSetPlayerStat.setString(2, ps.getCat());
                                 prepSetPlayerStat.setString(3, ps.getName());
                                 prepSetPlayerStat.setInt(4, ps.getValue());
 
 
                                 prepSetPlayerStat.addBatch();
                             }
                             prepSetPlayerStat.executeBatch();
 
                         } catch (SQLException e) {
                         }
                         BeardStat.printDebugCon("Clearing write cache");
                         BeardStat.printDebugCon("Time taken to write: " +((System.currentTimeMillis() - t)/1000L));
                         writeCache.clear();
                     }
                 }
 
             }
         }).start();
     }
 
 	public List<Scoreboard> getScoreboards() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void registerScoreboard(Scoreboard scoreboard) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void addScore(Scoreboard scoreboard, ScoreboardEntry entry) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 }
