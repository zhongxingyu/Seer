 package me.tehbeard.BeardAch.dataSource;
 
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.bukkit.Bukkit;
 
 import me.tehbeard.BeardAch.BeardAch;
 
 
 public class SqlDataSource extends AbstractDataSource{
 
 	private HashMap<String,HashSet<String>> writeCache = new HashMap<String,HashSet<String>>();
 	Connection conn;
 
 
 	//protected static PreparedStatement prepGetPlayerStat;
 	protected static PreparedStatement prepGetAllPlayerAch;
 	protected static PreparedStatement prepAddPlayerAch;
 
 
 
 	public SqlDataSource() {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 
 			System.out.println(BeardAch.config);
 			String conUrl = String.format("jdbc:mysql://%s/%s",
 					BeardAch.config.getString("ach.database.host"), 
 					BeardAch.config.getString("ach.database.database"));
 			BeardAch.printCon("Configuring....");
 			Properties conStr = new Properties();
 			conStr.put("user",BeardAch.config.getString("ach.database.username",""));
 			conStr.put("password",BeardAch.config.getString("ach.database.password",""));
 			conStr.put("autoReconnect","true");
			conStr.put("maxReconnects","6");
 			BeardAch.printCon("Connecting....");
 			conn = DriverManager.getConnection(conUrl,conStr);
 			
 			
 			BeardAch.printCon("Checking for table");
 
 			ResultSet rs = conn.getMetaData().getTables(null, null, "achievements", null);
 			if (!rs.next()) {
 				BeardAch.printCon("Achievements table not found, creating table");
 				PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `achievements` (`player` varchar(32) NOT NULL, `achievement` varchar(32) NOT NULL,  UNIQUE KEY `player` (`player`,`achievement`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
 				ps.executeUpdate();
 				ps.close();
 				BeardAch.printCon("created table");
 			}
 			else
 			{
 				BeardAch.printCon("Table found");
 			}
 			rs.close();
 
 			BeardAch.printDebugCon("Preparing statements");
 			//prepGetPlayerStat = conn.prepareStatement("SELECT * FROM stats WHERE player=?");
 			prepGetAllPlayerAch = conn.prepareStatement("SELECT `achievement` FROM `achievements` WHERE player=?");
 
 
 			prepAddPlayerAch = conn.prepareStatement("INSERT INTO `achievements` values (?,?)",Statement.RETURN_GENERATED_KEYS);
 
 			Bukkit.getScheduler().scheduleSyncRepeatingTask(BeardAch.self, new Runnable(){
 
 				public void run() {
 					Runnable r = new SqlFlusher(writeCache);
 					writeCache = new HashMap<String,HashSet<String>>();
 					Bukkit.getScheduler().scheduleAsyncDelayedTask(BeardAch.self, r);
 				}
 
 			}, 2400L, 2400L);
 			BeardAch.printCon("Initaised MySQL Data Provider.");
 		} catch (ClassNotFoundException e) {
 			BeardAch.printCon("MySQL Library not found!");
 
 
 		}catch (SQLException e){
 			BeardAch.printCon("Something went derp.");
 			e.printStackTrace();
 		}
 
 
 
 	}
 
 
 
 
 	public HashSet<String> getPlayersAchievements(String player) {
 
 		try {
 			prepGetAllPlayerAch.setString(1, player);
 			ResultSet rs = prepGetAllPlayerAch.executeQuery();
 			HashSet<String> c = new HashSet<String>();
 			while(rs.next()){
 				c.add(rs.getString(1));
 			}
 			rs.close();
 			if(writeCache.containsKey(player)){
 				c.addAll(writeCache.get(player));
 			}
 			return c;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 	public void setPlayersAchievements(String player,
 			String achievements) {
 
 
 		if(!writeCache.containsKey(player)){
 			writeCache.put(player, new HashSet<String>());
 		}
 		writeCache.get(player).add(achievements);
 
 	}
 
 
 	class SqlFlusher implements Runnable {
 
 		private HashMap<String, HashSet<String>> write;
 		SqlFlusher(HashMap<String,HashSet<String>> toWrite){
 			write = toWrite;
 		}
 
 		public void run() {
 			BeardAch.printCon("Flushing to database");
 			try {
 				for( Entry<String, HashSet<String>> es :write.entrySet()){
 
 					prepAddPlayerAch.setString(1, es.getKey());
 
 					for(String val : es.getValue()){
 						prepAddPlayerAch.setString(2,val);
 						prepAddPlayerAch.addBatch();
 					}
 				}
 				prepAddPlayerAch.executeBatch();
 				prepAddPlayerAch.clearBatch();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public void flush() {
 		(new SqlFlusher(writeCache)).run();
 		writeCache = new HashMap<String,HashSet<String>>();
 
 	}
 
 
 	public void clearAchievements(String player) {
 		// TODO Auto-generated method stub
 
 	}
 }
