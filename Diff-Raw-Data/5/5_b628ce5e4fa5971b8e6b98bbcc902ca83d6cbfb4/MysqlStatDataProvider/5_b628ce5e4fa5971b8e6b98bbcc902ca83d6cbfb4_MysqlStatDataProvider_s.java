 package me.tehbeard.BeardStat.DataProviders;
 
 import java.sql.*;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStat;
 import me.tehbeard.BeardStat.containers.PlayerStatBlob;
 
 /**
  * Provides backend storage to a mysql database
  * @author James
  *
  */
 public class MysqlStatDataProvider extends IStatDataProvider {
 
 	Connection conn;
 
 	//protected static PreparedStatement prepGetPlayerStat;
 	protected static PreparedStatement prepGetAllPlayerStat;
 
 
 	protected static PreparedStatement prepSetPlayerStat;
 	
 	protected static PreparedStatement keepAlive;
 
 	private static HashMap<String,PlayerStatBlob> writeCache = new HashMap<String,PlayerStatBlob>();
 
 	MysqlStatDataProvider() throws SQLException{
 
 
 		String conStr = String.format("jdbc:mysql://%s/%s",
 				BeardStat.config.getString("stats.database.host"), 
 				BeardStat.config.getString("stats.database.database"));
 
 		BeardStat.printCon("Connecting....");
 		conn = DriverManager.getConnection(conStr,
 				BeardStat.config.getString("stats.database.username"),
 				BeardStat.config.getString("stats.database.password"));
 
 		keepAlive = conn.prepareStatement("SELECT COUNT(*) from `stats`"); 
 		BeardStat.printCon("Checking for table");
 
 	      ResultSet rs = conn.getMetaData().getTables(null, null, "stats", null);
 	      if (!rs.next()) {
 	    	BeardStat.printCon("Stats table not found, creating table");
 	        PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `stats` ("+
  " `player` varchar(32) NOT NULL DEFAULT '-',"+
  " `category` varchar(32) NOT NULL DEFAULT 'stats',"+
  " `stat` varchar(32) NOT NULL DEFAULT '-',"+
  " `value` int(11) NOT NULL DEFAULT '0',"+
  " PRIMARY KEY (`player`,`category`,`stat`)"+
 ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
 	        ps.executeUpdate();
 	        ps.close();
 	        BeardStat.printCon("created table");
 	      }
 	      else
 	      {
 	    	  BeardStat.printCon("Table found");
 	      }
 	      rs.close();
 	      
 	      BeardStat.printDebugCon("Preparing statements");
 		//prepGetPlayerStat = conn.prepareStatement("SELECT * FROM stats WHERE player=?");
 		prepGetAllPlayerStat = conn.prepareStatement("SELECT * FROM stats WHERE player=?");
 		BeardStat.printDebugCon("Player stat statement created");
 
 		prepSetPlayerStat = conn.prepareStatement("INSERT INTO `stats`(`player`,`category`,`stat`,`value`) values (?,?,?,?) ON DUPLICATE KEY UPDATE `value`=?;",Statement.RETURN_GENERATED_KEYS);
 		BeardStat.printDebugCon("Set player stat statement created");
 
 		BeardStat.printCon("Initaised MySQL Data Provider.");
 
 
 
 	}
 	/**
 	 * Create a new instance of this Data Provider
 	 * @return
 	 */
 	public static IStatDataProvider newInstance(){
 		//Attempt to load the SQLite library
 		MysqlStatDataProvider dp = null;
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			BeardStat.printCon("MySQL Library not found!");
 			return null;
 
 		}
 		try {
 			dp = new MysqlStatDataProvider();
 		} catch(SQLException e){
 			BeardStat.printCon("Failed to initaise MySQL Data Provider. Dumping error.");
 			e.printStackTrace();
 			BeardStat.printCon("Shutting down BeardStats");
 			return null;
 		}
 		return dp;
 	}
 
 
 
 	public PlayerStatBlob pullPlayerStatBlob(String player) {
 		return pullPlayerStatBlob(player,true);
 	}
 
 
 	@Override
 	public void  pushPlayerStatBlob(PlayerStatBlob player) {
 
 		//create a copy of the player stat blob to write to the 
 		synchronized(writeCache){
 			PlayerStatBlob copy = null;
 			//grab it if it's still in the cache
 			if(writeCache.containsKey(player.getName())){
 				copy = writeCache.get(player.getName());
 			} else {
 				copy = new PlayerStatBlob(player.getName(),player.getPlayerID());
 			}
 			//copy playerstats that need changing
 			for(PlayerStat ps:player.getStats()){
 				//update or create
 				if(copy.hasStat(ps.getCat(),ps.getName())){
 					copy.getStat(ps.getCat(),ps.getName()).setValue(ps.getValue());
 				}else{
 					if(ps.isArchive()){
 						BeardStat.printDebugCon("Caching stat " + ps.getName() + " as new");
 						PlayerStat nps = new PlayerStat(ps.getCat(),ps.getName(),ps.getValue());
 
 						copy.addStat(nps);
 						ps.clearArchive();
 					}
 				}
 
 			}
 			//push to cache if it doesn't already exist there
 			if(!writeCache.containsKey(player.getName())){
 				writeCache.put(copy.getName(),copy);
 			}
 		}
 	}
 
 	@Override
 	public void flush() {
 		//run SQL in async thread
 		BeardStat.self.getServer().getScheduler().scheduleAsyncDelayedTask(BeardStat.self, new sqlFlusher(pullCacheToThread()));
 
 		//(new sqlFlusher(pullCacheToThread())).run();
 	}
 	@Override
 	public PlayerStatBlob pullPlayerStatBlob(String player, boolean create) {
 		// TODO Auto-generated method stub
 		try {
 			long t1 = (new Date()).getTime();
 			PlayerStatBlob pb = null;
 
 			//try to pull it from the db
 			prepGetAllPlayerStat.setString(1, player);
 			ResultSet rs = prepGetAllPlayerStat.executeQuery();
 			pb = new PlayerStatBlob(player,0);
 			while(rs.next()){
 				//`category`,`stat`,`value`
 				PlayerStat ps = new PlayerStat(rs.getString(2),rs.getString(3),rs.getInt(4));
 				pb.addStat(ps);
 			}
 			rs.close();
 			
 			
 			
 			BeardStat.printDebugCon("time taken to retrieve: "+((new Date()).getTime() - t1) +" Milliseconds");
 			
 			if(pb.getStats().size()==0 && create==false){return null;}
 			
 			return pb;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 	private HashMap<String,PlayerStatBlob> pullCacheToThread(){
 		synchronized(writeCache){
 			HashMap<String,PlayerStatBlob> tmp = writeCache;
 			writeCache = new HashMap<String,PlayerStatBlob>();
 			return tmp;
 
 		}
 
 
 	}
 
 	class sqlFlusher implements Runnable {
 
 		HashMap<String,PlayerStatBlob> toWrite = null;
 		sqlFlusher(HashMap<String,PlayerStatBlob> toWrite){
 			this.toWrite = toWrite;
 		}
 		public void run() {
 			BeardStat.printDebugCon("[Writing to database]");
 
 			// TODO Auto-generated method stub
 			try {
 				int deltaRows;  
 				ResultSet size = keepAlive.executeQuery();
				deltaRows = size.getInt(1);
 				size.close();
 				Long t1 = (new Date()).getTime();
 				int objects = 0;
 				prepSetPlayerStat.clearBatch();
 				for(PlayerStatBlob pb:toWrite.values()){
 					BeardStat.printDebugCon("Packing stats for "+pb.getName());
 					BeardStat.printDebugCon("[");
 					for(PlayerStat ps:pb.getStats()){
 						BeardStat.printDebugCon("stat: " + ps.getCat() + "->"+ ps.getName() + " = " + ps.getValue());
 						prepSetPlayerStat.setString(1, pb.getName());
 						prepSetPlayerStat.setString(2, ps.getCat());
 						prepSetPlayerStat.setString(3, ps.getName());
 						prepSetPlayerStat.setInt(4, ps.getValue());
 						prepSetPlayerStat.setInt(5, ps.getValue());
 						prepSetPlayerStat.addBatch();
 						objects+=1;
 					}
 					BeardStat.printDebugCon("]");
 				}
 
 				prepSetPlayerStat.executeBatch();
 
 				long t2 = (new Date()).getTime();
 				BeardStat.printDebugCon("[Database write Completed]");
 				BeardStat.printDebugCon("Objects written to database: " + objects);
 				BeardStat.printDebugCon("Time taken to write to Database: " + (t2-t1) + "milliseconds");
 				if(objects > 0){
 					BeardStat.printDebugCon("Average time per object: " + (t2-t1)/objects + "milliseconds");
 				}
				size = keepAlive.executeQuery();
				BeardStat.printCon("" +( size.getInt(1) - deltaRows) + " rows added to database in last Update");
				size.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 }
