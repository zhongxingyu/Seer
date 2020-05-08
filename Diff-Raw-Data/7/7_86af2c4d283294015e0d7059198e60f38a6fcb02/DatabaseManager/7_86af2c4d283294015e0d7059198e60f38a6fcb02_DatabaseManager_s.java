 package me.fogest.mctrade;
 
 import java.io.File;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 
 import me.fogest.mctrade.MCTrade;
 import me.fogest.mctrade.SQLibrary.*;
 
 public class DatabaseManager {
	private MCTrade plugin;
     public static File dbFolder = new File("plugins/MCTrade");
 
     public static SQLite db = new SQLite(MCTrade.getPlugin().getLogger(), "[MCTrade]", "MCTrade", dbFolder.getPath());
 
     /**
      * Initializes, opens and confirms the tables and database.
      */
    public DatabaseManager()
    {
    	this.plugin = MCTrade.getPlugin();
    }
     
     public void enableDB(){
     	plugin.getLogger().info("Enable DB");
     	plugin.getLogger().info("Initializing");
         db.initialize();
         plugin.getLogger().info("Initializing Done");
         plugin.getLogger().info("Opening DB");
         db.open();
         plugin.getLogger().info("Opening Done");
         plugin.getLogger().info("Confirming Tables");
         confirmTables();
         plugin.getLogger().info("Confirmed!");
     }
 
     /**
      * Closes the database.
      */
     public static void disableDB(){ if(db.checkConnection()) db.close(); }
 
     private static void confirmTables(){
         if(!db.checkTable("MCTrade_users")){
             String queryString = "CREATE TABLE IF NOT EXISTS `users` ("
 			  +"`user_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
 			  +"`username` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`password` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`minecraft_name` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`email` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`user_level` int(11) NOT NULL,"
 			  +"`Current_Open_Trades` int(11) NOT NULL,"
 			  +"`code` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`active` tinyint(4) NOT NULL,"
 			  +"`ip` text COLLATE latin1_general_ci NOT NULL,"
 			  +"PRIMARY KEY (`user_id`))";
 									
             try {
                 db.query(queryString);
                 MCTrade.getPlugin().getLogger().log(Level.INFO, "Successfully created the requests table.");
             } catch (Exception e) {
                 MCTrade.getPlugin().getLogger().log(Level.SEVERE, "Unable to create the requests table.");
                 e.printStackTrace();
             }
         }
         if(!db.checkTable("MCTrade_trades")){
             String queryString = "CREATE TABLE IF NOT EXISTS `trades` ("
 			  +"`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
 			  +"`Minecraft_Username` text NOT NULL,"
 			  +"`Block_ID` int(5) NOT NULL,"
 			  +"`Quantity` int(3) NOT NULL,"
 			  +"`CostPer` int(7) NOT NULL,"
 			  +"`TradeNotes` text NOT NULL,"
 			  +"`AccountName` text NOT NULL,"
 			  +"`IP` text NOT NULL,"
 			  +"`Trade_Status` int(11) NOT NULL COMMENT '1 = Open Trade, 2 = Closed Trade, 3 = Hidden Trade',"
 			  +"PRIMARY KEY (`id`))";
             try {
                 db.query(queryString);
                 MCTrade.getPlugin().getLogger().log(Level.INFO, "Successfully created the trades table.");
             } catch (Exception e) {
                 MCTrade.getPlugin().getLogger().log(Level.SEVERE, "Unable to create the trades table.");
                 e.printStackTrace();
             }
         }
     }
 
     public static int getUserId(String player){
         if(!db.checkConnection()) return 0;
         int userId = 0;
         try {
             PreparedStatement ps = db.getConnection().prepareStatement("SELECT `id` FROM `MCTrade_user` WHERE `mincraft_name` = ?");
             ps.setString(1, player);
             ResultSet rs = ps.executeQuery();
             userId = !rs.isBeforeFirst() ? 0 : rs.getInt("user_id");
             ps.close();
             rs.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return userId;
     }
 
     private static int createUser(String player){
         if(!db.checkConnection()) return 0;
         int userId = 0;
         try {
             PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO `MCTrade_user` (`name`, `banned`) VALUES (?, '0')");
             ps.setString(1, player);
             if(ps.executeUpdate() < 1) return 0;
             ps.close();
             ps = db.getConnection().prepareStatement("SELECT `id` FROM `MCTrade_user` WHERE `name` = ?");
             ps.setString(1, player);
             ResultSet rs = ps.executeQuery();
 
             if(!rs.isBeforeFirst()) return 0;
             userId = rs.getInt(1);
             ps.close();
             rs.close();
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return userId;
     }
     /**
      * Files a request, inserting it into the database.
      * @param player String Name of player
      * @param world String Name of world
      * @param location getLocation() object
      * @param message String help request message
      * @return True if successful.
      */
     public static boolean createTrade(String player, String world, Location location, String message, int userId){
         if(!db.checkConnection() || userId == 0) return false;
         long tstamp = System.currentTimeMillis()/1000;
         try {
             ResultSet rs = db.query("SELECT `banned` FROM MCTrade_user WHERE `id` = '" + userId + "'");
             if(rs.getInt("banned") == 1){
                 rs.close();
                 return false;
             }
             rs.close();
             PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO `MCTrade_request` (`user_id`, `tstamp`, `world`, `x`, `y`, `z`," +
                     " `text`, `status`, `notified_of_completion`) VALUES" +
                     " (?, ?, ?, ?, ?, ?, ?, '0', '0')");
             ps.setInt(1, userId);
             ps.setLong(2, tstamp);
             ps.setString(3, world);
             ps.setInt(4, location.getBlockX());
             ps.setInt(5, location.getBlockY());
             ps.setInt(6, location.getBlockZ());
             ps.setString(7, message);
             if(ps.executeUpdate() < 1) return false;
             ps.close();
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
 
     public static boolean resetDB(){
         db.query("DELETE FROM MCTrade_request");
         db.query("DELETE FROM MCTrade_user");
         return true;
     }
 }
