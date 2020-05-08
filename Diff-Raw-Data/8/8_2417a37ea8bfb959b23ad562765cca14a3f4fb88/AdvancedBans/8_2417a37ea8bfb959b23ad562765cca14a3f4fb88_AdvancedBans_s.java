 package pl.kyku;
 
 import pl.kyku.ConnectionPool;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 //import com.nijiko.permissions.PermissionHandler;
 //import com.nijikokun.bukkit.Permissions.Permissions;
 
 
 /**
  * Lowienie for Bukkit
  *
  * @author kyku
  */
 
 public class AdvancedBans extends JavaPlugin
 {
 	public final static Logger log = Logger.getLogger("Minecraft");
 	public int usingpermissions = 0;
     private final AdvancedBansPluginPlayerListener playerListener = new AdvancedBansPluginPlayerListener(this);
     private ConnectionPool pool;
     private boolean errorAtLoading = false;
  //   public static PermissionHandler Permissions = null;
  //   public static PermissionManager PEX = null;
     public boolean mysqlEnabled = false;
     
 	//file based banning
 	private FileConfiguration bansConfig = null;
 	private File bansConfigurationFile = null;
 	
 	private static Configuration config;	
 	
 	public void onEnable()
 	{
         PluginManager pm = getServer().getPluginManager();        
         pm.registerEvents(playerListener, this);
         
         PluginDescriptionFile pdfFile = this.getDescription();
         /*
          * disabled to move to bukkitPerm
 		if (AdvancedBans.Permissions == null)
 		{
 			Plugin permPlug = this.getServer().getPluginManager().getPlugin("Permissions");
 			
 		    if ( getServer().getPluginManager().isPluginEnabled("PermissionsEx") ) 
 		    {
 		    	PEX = PermissionsEx.getPermissionManager();
 		    	log.info(pdfFile.getName()+" version "+pdfFile.getVersion()+" is enabled with PermissionEx!");
 			    usingpermissions = 1;
 		    }
 		    else if( usingpermissions == 0 && permPlug != null )
 		    {
 			    Permissions = ( (Permissions) permPlug ).getHandler();
 			    log.info(pdfFile.getName()+" version "+pdfFile.getVersion()+" is enabled with Permissions!");
 			    usingpermissions = 2;
 		    }
 		    else
 		    {
 		    	log.info(pdfFile.getName()+" version "+pdfFile.getVersion()+" is enabled without permissions!");
 		    	usingpermissions = 0;
 		    }
 		}*/
 		
 		this.loadConfig();
 		this.mysqlEnabled = getConfig().getBoolean("MySQL.Use", false); // we do need it from conf. not the static one
 		
 		
 		if( this.mysqlEnabled ){
 			
 			/*
 			//TODO: bukkit has this file build-in since build 1000, remove the dependency for that and later versions
 			 * FIXME: remove in/before version 0.9 if proven that the plugin is stable without it
 			final File file = new File("mysql-connector-java-bin.jar");
 			try {
 				if (!file.exists() || file.length() == 0) {
 					log.info("[AdvancedBans] Downloading file: " + file.getName() + "...");
 					download(new URL("http://download.mc-creative.nl/plugins/advbans/mysql-connector-java-bin.jar"), file);
 				}
 				if (!file.exists() || file.length() == 0)
 					throw new FileNotFoundException(file.getAbsolutePath() + file.getName());
 			} catch (final Exception e) {
 				log.log( Level.SEVERE, "[AdvancedBans] Error while downloading " + file.getName() + ".");
 				log.log( Level.SEVERE, "[AdvancedBans] If you continue to have this issue please report it.");
 				errorAtLoading = true;
 				return;
 			}
 			*/
 			
 			try {
 				String url = "jdbc:mysql://" + getConfig().getString("MySQL.Host") + ":" + getConfig().getString("MySQL.Port") + "/" + getConfig().getString("MySQL.Database");
 				
 				log.info("[AdvancedBans] Connecting to " + getConfig().getString("MySQL.User") + "@" + getConfig().getString("MySQL.Host") + "...");
 				pool = new ConnectionPool(url, getConfig().getString("MySQL.User"), getConfig().getString("MySQL.Password"));
 				final Connection conn = getConnection();
 				conn.close();
 			} catch (final Exception ex) {
 				log.log(Level.SEVERE, "[AdvancedBans] Exception while checking database connection", ex.getMessage() );
 				errorAtLoading	= true;
 				mysqlEnabled	= false;
 				return;
 			}
 			if (!checkTables()) {
 				log.log(Level.SEVERE, "[AdvancedBans] Errors while checking tables. They may or may not exist. Haven't got a clue myself");
 				errorAtLoading	= true;
 				mysqlEnabled	= false;
 				return;
 			}
 		}
 		
 		//FIXME: move to the function checkTables()
 		//check if DB update is needed
 		Float update1Version	= new Float( "0.8" ); //FIXME: This needs to be a static float. In the future we may want to run another upgrade of the DB
 		Float cfgVersion		= new Float ( AdvancedBans.config.getString( "version", pdfFile.getVersion() ) );
 		if ( cfgVersion < update1Version ){
 			
 			if( this.mysqlEnabled ) {
 				log.info( "[AdvancedBans] Old config version found. Starting upgrade." );
 				
 				Connection conn = getConnection();
 				try {
 					conn.createStatement().execute( "ALTER TABLE `"+getConfig().getString("MySQL.table")+"` ADD COLUMN `unbanreason` VARCHAR(128) NULL  AFTER `status` , ADD COLUMN `unbannick` VARCHAR(64) NULL  AFTER `unbanreason`, ADD INDEX `Minecraftname` (`nick` ASC);" );
 					conn.close();
 				} catch (SQLException e) {
 					log.log( Level.SEVERE, "[AdvancedBans] SQL error while upgrading database. Please manually update your tables or contact the maintainer. " + e.getMessage() );
 					errorAtLoading = true;
 				}				
 			}
 			getConfig().set("version", pdfFile.getVersion() );
 			this.saveConfig();//save updated version
 			log.info( "[AdvancedBans] Database updated to version " + pdfFile.getVersion() + "." );
 		}
 		
 		//disable if there are ANY errors
         if (errorAtLoading) {
         	log.log( Level.SEVERE, "[AdvancedBans]" + ChatColor.RED + " Errors detected in mysql activation. Now using file based banning." );
         	getConfig().set("MySQL.Use", false); //mysql's dead don't use it 
         	this.mysqlEnabled = false;
 			//pm.disablePlugin(this);
 			//return;
 		}
         
 		if( errorAtLoading || ! getConfig().getBoolean("MySQL.Use", false) )
 			loadBans();
 		
 	}
 	
 	public void onDisable()
 	{
 		PluginDescriptionFile pdfFile = this.getDescription();
 		if (pool != null)
 			pool.closeConnections();
         log.info(pdfFile.getName()+" version "+pdfFile.getVersion()+" is disabled!");
 	}
 
 	/**
 	 * Replace friendly colour names with acctual colour codes
 	 * @param text The text with friendly colour names
 	 * @return string text with correct colour codes
 	 */
 	public String colorize(String text) // from http://forums.bukkit.org/threads/xx.11955/
 	{
 		text = text.replaceAll("&AQUA;",		ChatColor.AQUA.toString());
 		text = text.replaceAll("&BLACK;",		ChatColor.BLACK.toString());
 		text = text.replaceAll("&BLUE;",		ChatColor.BLUE.toString());
 		text = text.replaceAll("&DARK_AQUA;",	ChatColor.DARK_AQUA.toString());
 		text = text.replaceAll("&DARK_BLUE;",	ChatColor.DARK_BLUE.toString());
 		text = text.replaceAll("&DARK_GRAY;",	ChatColor.DARK_GRAY.toString());
 		text = text.replaceAll("&DARK_GREEN;", 	ChatColor.DARK_GREEN.toString());
 		text = text.replaceAll("&DARK_PURPLE;",	ChatColor.DARK_PURPLE.toString());
 		text = text.replaceAll("&DARK_RED;",	ChatColor.DARK_RED.toString());
 		text = text.replaceAll("&GOLD;",		ChatColor.GOLD.toString());
 		text = text.replaceAll("&GRAY;",		ChatColor.GRAY.toString());
 		text = text.replaceAll("&GREEN;",		ChatColor.GREEN.toString());
 		text = text.replaceAll("&LIGHT_PURPLE;",ChatColor.LIGHT_PURPLE.toString());
 		text = text.replaceAll("&RED;",			ChatColor.RED.toString());
 		text = text.replaceAll("&WHITE;",		ChatColor.WHITE.toString());
 		text = text.replaceAll("&YELLOW;",		ChatColor.YELLOW.toString());
 		return text;
 	}
 	
 	
     private void loadBans() {
 	    if (bansConfigurationFile == null) {
 	    	bansConfigurationFile = new File(getDataFolder(), "bans.yml");
 	    }
 	    bansConfig = YamlConfiguration.loadConfiguration(bansConfigurationFile);
 	 
 	    // Look for defaults in the jar
 	    InputStream defConfigStream = getResource("bans.yml");
 	    if (defConfigStream != null) {
 	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        bansConfig.setDefaults(defConfig);
 	    }
     }
     
     /**
      * Wrapper to load and get the file based bans 
      * @return The bans file object
      */
 	protected FileConfiguration getBansConfig() {
 	    if (bansConfig == null) {
 	        loadBans();
 	    }
 	    
 	    return bansConfig;
 	}
 
 	/**
 	 * Save file based bans to file
 	 */
 	protected void saveBans() {
 		if (bansConfig == null || bansConfig == null) {
 			return;
 		}
 		try {
 			bansConfig.save(bansConfigurationFile);
 		} catch (IOException ex) {
 			log.log(Level.SEVERE,
 					"Could not save bans to " + bansConfigurationFile.getName(), ex);
 		}
 	}
 	
 	protected void loadConfig(){
 		AdvancedBans.config = this.getConfig().getRoot();
 		AdvancedBans.config.options().copyDefaults(true);
 		this.saveConfig();
 	}
 	
 	/**
 	 * Check tables to see if they need to be created
 	 * @return boolean
 	 */
 	private boolean checkTables() {
 		final Connection conn = getConnection();
 		Statement state = null;
 		if (conn == null)
 			return false;
 		try {
 			final DatabaseMetaData dbm = conn.getMetaData();
 			state = conn.createStatement();
 	/*
 		log.log(Level.INFO, "[AdvancedBans] Crating table ab_banlist.");
 		state.execute("CREATE TABLE  `ab_banlist` (`id` INT( 10 ) NOT NULL AUTO_INCREMENT ,`nick` VARCHAR( 32 ) NOT NULL ,`ip` VARCHAR( 35 ) NOT NULL ,`banfrom` INT( 8 ) NOT NULL ,`banto` INT( 8 ) NOT NULL ,`reason` VARCHAR( 128 ) NOT NULL ,PRIMARY KEY (  `id` )) ENGINE = MYISAM ;");
 		if (!dbm.getTables(null, null, "ab_banlist", null).next())
 			
 						
 		CREATE TABLE  `ab_history` (`id` INT( 10 ) NOT NULL AUTO_INCREMENT ,`name` VARCHAR( 32 ) NOT NULL ,`ip` VARCHAR( 35 ) NOT NULL ,PRIMARY KEY (  `id` ));
 	*/
 			if (!dbm.getTables(null, null, getConfig().getString("MySQL.table-history"), null).next())	{
 				log.log(Level.INFO, "[AdvancedBans] Creating table "+getConfig().getString("MySQL.table-history")+".");
 				state.execute("CREATE TABLE  `"+getConfig().getString("MySQL.table-history")+"` (`id` INT( 10 ) NOT NULL AUTO_INCREMENT ,`name` VARCHAR( 32 ) NOT NULL ,`ip` VARCHAR( 35 ) NOT NULL ,PRIMARY KEY (  `id` ));");
 				if (!dbm.getTables(null, null, getConfig().getString("MySQL.table-history"), null).next())
 					return false;
 			}
 			if (!dbm.getTables(null, null, getConfig().getString("MySQL.table"), null).next())	{
 				log.log(Level.INFO, "[AdvancedBans] Creating table "+getConfig().getString("MySQL.table")+".");
 				state.execute("CREATE TABLE  `"+getConfig().getString("MySQL.table")+"` (`id` INT( 10 ) NOT NULL AUTO_INCREMENT ,`nick` VARCHAR( 64 ) NOT NULL ,`adminnick` VARCHAR( 64 ) NOT NULL ,`ip` VARCHAR( 35 ) NOT NULL ,`banfrom` INT( 8 ) NOT NULL ,`banto` INT( 8 ) NOT NULL ,`reason` VARCHAR( 128 ) NOT NULL, `unbannick` VARCHAR(64) NULL, `unbanreason` VARCHAR(64) NULL, `status` INT( 1 ) NOT NULL ,PRIMARY KEY (  `id` ));");
 				if (!dbm.getTables(null, null, getConfig().getString("MySQL.table"), null).next())
 					return false;
 			}		
 			
 			
 			return true;
 		} catch (final SQLException ex) {
 			log.log(Level.SEVERE, "[AdvancedBans] SQL exception while checking tables", ex);
 		} finally {
 			try {
 				if (state != null)
 					state.close();
 				if (conn != null)
 					conn.close();
 			} catch (final SQLException ex) {
 				log.log(Level.SEVERE, "[AdvancedBans] SQL exception on close", ex);
 			}
 		}
 		return false;
 	}
 	
 
 	public boolean onCommand(CommandSender player, Command cmd, String commandLabel, String[] args) {
 		
 		//boolean perm = false;//(sender instanceof ConsoleCommandSender);
 		String name = "*Console*";
 		
 		
 		/*
 		if(player instanceof ConsoleCommandSender){
 			perm = true;
 		} else 
 		*/
 		if(player instanceof Player){
 			name = ((Player) player).getName();
 			//if(player.isOp()||(usingpermissions != 0 && this.checkpermissions((Player)player, "advbans."+cmd.getName().toLowerCase())))perm=true;	
 		} 
 		// perm prefix = advbans 
 
 		
 		
 		if (commandLabel.equalsIgnoreCase("checkbans")) {
 			if ( player.hasPermission("advbans.check") ) {
 				
 				if(args.length >= 1 ){
 				if  ( this.mysqlEnabled ){
 						final Connection conn = getConnection();
 						Statement state = null;
 	
 			    		ResultSet rs;
 			    		ResultSet activeBans;
 			    		String message;
 						if (conn == null){
 							
 							player.sendMessage(ChatColor.BLACK + "[AdvancedBans]" + ChatColor.RED + "[MYSQL] Connection error!");
 							return false;
 						}
 						try {
 							state = conn.createStatement();
 							
 							//TODO: rewrite section to list the last 5 a 6 ( user config? ) bans of \player\. With a tot at top
 							
 							String safenick = args[0].toLowerCase().replaceAll("'", "\"");
 				    		rs = state.executeQuery("SELECT COUNT(id) as count FROM `"+getConfig().getString("MySQL.table")+"` WHERE `nick` = '"+safenick+"'");
 				    		
 				    		if ( rs.next() ) {
 				    			message = "The player "+safenick+" has "+ rs.getInt("count") +" ban(s)";
 					    		activeBans = state.executeQuery("SELECT COUNT(id) as count FROM `"+getConfig().getString("MySQL.table")+"` WHERE `nick` = '"+safenick+"' AND `status` = 1");
 				    			if ( activeBans.next() )
 				    				message += " of which " + activeBans.getInt("count") +" ban(s) are active";
 				    			player.sendMessage( message );
 				    		}
 				    		
 						} catch (final SQLException ex) {
 							log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) while checking player bans", ex );
 						} finally {
 							try {
 								if (state != null)
 									state.close();
 								if (conn != null)
 									conn.close();
 							} catch (final SQLException ex) {
 								log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) when closing the connection", ex);
 							}
 						}
 				} else  player.sendMessage("[AdvancedBans] Currently only supported on MySQL.");
 				} else return false; // we're missing some args
			}//ennd of permission
 		return true;
 		}
 		if (commandLabel.equalsIgnoreCase("kick")) {
 				if ( player.hasPermission( "advbans.kick" ) ) {
 					if(args.length>0){
 					
 					if(this.getServer().getPlayer(args[0]) != null){
 						Player cel = this.getServer().getPlayer(args[0]);
 						
 						String msg = "";
 			            String kickmsg = "";
 			            String powodd = getConfig().getString("Strings.Standard-reason", " ");
 			           
 						
 						if(args.length>1){ // powod
 							powodd = "";
 							for( int a=1; a<args.length;a++)powodd += " "+args[a];
 						}
 							
 						kickmsg = getConfig().getString("Strings.Kick.kickmsg", "Nothing configured. Please check your config or report this bug." );
 						kickmsg = colorize(kickmsg.replaceAll("&REASON;", powodd));
 							
 							
 						msg = getConfig().getString("Strings.Kick.broadcast", "Nothing configured. Please check your config or report this bug." );
 						msg = msg.replaceAll("&ANAME;", name);
 						msg = msg.replaceAll("&TNAME;", cel.getDisplayName());
 						msg = colorize(msg.replaceAll("&REASON;", powodd));
 						
 						
 						//"Admin: "+name+" kicked "+cel.getDisplayName()+". Reason:"+powodd;
 							 
 						log.info(msg);
 						
 						if(getConfig().getBoolean("Settings.Broadcast-kick-msg", true)){
 								//dwa is polish for 2
 							for (Player playerdwa : this.getServer().getOnlinePlayers())
 				        	{
 				          		playerdwa.sendMessage(msg);	
 				           	}
 						}
 						cel.kickPlayer(kickmsg);
 						
 
 					} else {
 						player.sendMessage(colorize(getConfig().getString("Strings.Error.Player-is-offline", "[AdvancedBans] Player is offline")));
 					}
 				} else player.sendMessage("Usage: /kick <nick> [reason]");
 			} else player.sendMessage(colorize(getConfig().getString("Strings.Error.No-access")));
 			return true;
 		}
 		if (commandLabel.equalsIgnoreCase("unbanip")) {
 			if ( player.hasPermission( "advbans.unbanip" ) ) {
 				
 				if(args.length==1){
 					
 					if  ( this.mysqlEnabled ){
 						
 						
 						final Connection conn = getConnection();
 						Statement state = null;
 
 			    		ResultSet rs;
 						if (conn == null){
 							
 							player.sendMessage(ChatColor.RED+"[MYSQL] Connection error!");
 							return false;
 						}
 						try {
 							
 							state = conn.createStatement();
 
 							
 							String safenick = args[0].toLowerCase().replaceAll("'", "\"");
 				    		rs = state.executeQuery("SELECT * FROM `"+getConfig().getString("MySQL.table")+"` WHERE `ip` = '"+safenick+"' and `status` = 1");
 							
 							// BAN NA IP
 							//log.log(Level.FINE, "BUU");
 							boolean activeBan = false;
 				    		while(rs.next()){
 				    			int id = rs.getInt("id");
 				    			PreparedStatement pstmt = conn.prepareStatement("UPDATE "+getConfig().getString("MySQL.table")+" SET status = 2 WHERE id=?");
 				    			
 				    			pstmt.setInt(1,id);
 
 				    			pstmt.executeUpdate();
 				    			pstmt.close();
 				    			
 				    			activeBan=true;
 				    		}
 				    		if(activeBan==true){
 				    		String msg = "";
 							
 								msg = getConfig().getString("Strings.Unban-message", " ");
 								msg = msg.replaceAll("&ANAME;", name);
 								msg = colorize(msg.replaceAll("&TNAME;", args[0]));
 								//msg = colorize(msg.replaceAll("&TIME;", args[1]));
 								log.info(msg);
 								
 							    if(getConfig().getBoolean("Settings.Broadcast-ban-msg", true))
 							    		//dwa means 2
 									for (Player playerdwa : this.getServer().getOnlinePlayers())
 						        	{
 						          		playerdwa.sendMessage(msg);	
 						           	}
 							} else player.sendMessage("This player does not have an active ban");
 							
 					    		
 							
 						} catch (final SQLException ex) {
 							log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) while unbanning player", ex);
 						} finally {
 							try {
 								if (state != null)
 									state.close();
 								if (conn != null)
 									conn.close();
 							} catch (final SQLException ex) {
 								log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) on close", ex);
 							}
 						}
 					
 					
 					} else player.sendMessage("Only supported on MySQL.");
 				} else player.sendMessage(ChatColor.RED+"Usage: /unbanip <IP ADDRESS>");
 			} else player.sendMessage(colorize(getConfig().getString("Strings.Error.No access")));
 			
 			return true;
 		}
 		if (commandLabel.equalsIgnoreCase("unban")) {
 			if ( player.hasPermission( "advbans.unban" ) ) {
 				
 				if(args.length >= 1){
 					
 					if  ( this.mysqlEnabled ){
 						
 						String unbanReason	= "";
 						String unbanNick	= "";
 						
 						if ( args.length >= 1 ){
 							for( int a=1; a<args.length;a++) unbanReason += " "+args[a];
 						}
 						
 						if(player instanceof Player){
 							unbanNick = player.getName().toString();
 						} else {
 							unbanNick = "**Console** or somethin";
 						}
 						
 						
 						final Connection conn = getConnection();
 						Statement state = null;
 
 			    		ResultSet rs;
 						if (conn == null){
 							
 							player.sendMessage(ChatColor.BLACK + "[AdvBans]" + ChatColor.RED + "[MYSQL] Connection error!");
 							return false;
 						}
 						try {
 							
 							state = conn.createStatement();
 
 							
 							String safenick = args[0].toLowerCase().replaceAll("'", "\"");
 				    		rs = state.executeQuery("SELECT id FROM `"+getConfig().getString("MySQL.table")+"` WHERE `nick` = '"+safenick+"' and `status` = 1");
 							
 							// BAN NA IP
 							//log.log(Level.FINE, "BUU");
 							boolean activeBan = false;
 							
 							//moved out of while block, why reset it each time, it's already prepaired 
 			    			PreparedStatement pstmt = conn.prepareStatement("UPDATE "+getConfig().getString("MySQL.table")+" SET status = 2, unbanreason=?, unbannick=? WHERE id=?");
 				    		while(rs.next()){
 				    			int id = rs.getInt("id");
 				    			
 				    			pstmt.setString(1,unbanReason);
 				    			pstmt.setString(2,unbanNick);
 				    			pstmt.setInt(3,id);
 
 				    			pstmt.executeUpdate();
 				    			
 			    				activeBan=true;
 				    		}
 				    		pstmt.close();
 				    		pstmt = null; //throw it away
 				    		
 				    		
 				    		if(activeBan==true){
 				    		String msg = "";
 							
 								msg = getConfig().getString("Strings.Unban-message", " ");
 								msg = msg.replaceAll("&ANAME;", name);
 								msg = colorize(msg.replaceAll("&TNAME;", args[0]));
 								//msg = colorize(msg.replaceAll("&TIME;", args[1]));
 								if ( msg.length() < 2 ) {
 									//we do need some info on console
 									//without this a empty line is displayed on console
 									log.info( ChatColor.BLACK+ "[AdvBans]"+ChatColor.GREEN+" Player "+ args[0] +" has been unbanned by admin " + ChatColor.RED + name );
 								} else 
 									log.info(msg);
 								
 							    if(getConfig().getBoolean("Settings.Broadcast-ban-msg", true))
 									
 									for (Player playerdwa : this.getServer().getOnlinePlayers())
 						        	{
 						          		playerdwa.sendMessage(msg);	
 						           	}
 							} else player.sendMessage("This player does not have an active ban");				    		
 							
 							
 						} catch (final SQLException ex) {
 							log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) while unbanning player", ex);
 						} finally {
 							try {
 								if (state != null)
 									state.close();
 								if (conn != null)
 									conn.close();
 							} catch (final SQLException ex) {
 								log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) on close", ex);
 							}
 						}
 					
 					//start of non mysql unban
 					} else {
 					if ( getBansConfig().getConfigurationSection("bans").getKeys(false).contains(args[0].toLowerCase())) {
 						
 						getBansConfig().set("bans." + args[0].toLowerCase(), null);
 					    saveBans();	
 					   // player.sendMessage("Player "+args[0]+" unbanned");
 						String msg = "";
 						
 						msg = getConfig().getString("Strings.Unban-message", "Nothing configured. Please check your config or report this bug." );
 						msg = msg.replaceAll("&ANAME;", name);
 						msg = colorize(msg.replaceAll("&TNAME;", args[0]));
 						//msg = colorize(msg.replaceAll("&TIME;", args[1]));
 						log.info(msg);
 						
 					    if(getConfig().getBoolean("Settings.Broadcast ban msg", true))
 							
 							for (Player playerdwa : this.getServer().getOnlinePlayers())
 				        	{
 				          		playerdwa.sendMessage(msg);	
 				           	}
 					} else player.sendMessage("This player does not have an active ban");
 					
 					}
 				} else player.sendMessage(ChatColor.RED+"Usage: /unban <nick> [reason]");
 			} else player.sendMessage(colorize(getConfig().getString("Strings.Error.No-access", "&RED;You don't have permission")));
 			
 			return true;
 		}
 		
 		if (commandLabel.equalsIgnoreCase("ban")) {
 			if ( player.hasPermission( "advbans.ban" ) || player.hasPermission( "advbans.tmpban" ) ) {
 				if(args.length>0){
 				String nick = "";
 				boolean jest = false;
 				
 				if(this.getServer().getPlayer(args[0]) != null){
 					
 					Player cel = this.getServer().getPlayer(args[0]);
 					nick = cel.getName();
 					jest = true; //online OR active
 				} else {
 					nick = args[0];
 					jest = false;
 					if(nick.isEmpty()){
 						player.sendMessage("Empty name?..");
 						return true;
 					}
 					
 				}
 					
 					List<String> stringList = new ArrayList<String>();
 					
 					
 					String msg = "";
 		            String kickmsg = "";
 		            String czas = "0"; // ban time in in unix settings
 		            String powodd = getConfig().getString("Strings.Standard-reason", " ");
 		            String time = getConfig().getString("Settings.defaultBanTime", "2880"); // normal non unix time
 		            Boolean noTime = false;
 			           
 					if(args.length>1){ // czas
 						//|| args[1].equalsIgnoreCase( "forever" ) // too much of a pain in the ass, jus type 0 if you want forever
 						if( isParsableToInt(args[1]) ){
 						
 							if(Integer.valueOf(args[1])<=0){
 								time = args[1] = "0";
 							} else {
 								long unixTime = System.currentTimeMillis() / 1000L;
 								czas = String.valueOf((int)unixTime+(Integer.valueOf(args[1])*60));
 								time = args[1]=args[1];
 							}
 						} else {
 							long unixTime = System.currentTimeMillis() / 1000L;
 							czas = String.valueOf((int)unixTime+(Integer.valueOf( time )*60));
 							
 							noTime = true;
 						}
 						
 						//TODO: This should better indicate that it's about tmp VS perm banning rights
 						if ( czas == "0" && ! player.hasPermission( "advbans.ban" ) ){
 							//perm bans
 							player.sendMessage( getConfig().getString("Strings.Ban.No-access", ChatColor.RED + "You don't have permissions" ) );
 							return true;											
 						} else if ( Integer.parseInt( czas ) > 0  && ! player.hasPermission( "advbans.tmpban" ) && ! player.hasPermission( "advbans.ban" )  ) {
 							//tmp banning
 							player.sendMessage( getConfig().getString("Strings.Ban.No-access", ChatColor.RED + "You don't have permissions" ) );
 							return true;
 						}
 						
 						
 						
 
 						kickmsg = getConfig().getString("Strings.Ban.only-time-kickmsg", "Nothing configured. Please check your config or report this bug." );
 						kickmsg = colorize(kickmsg.replaceAll("&TIME;", time ));
 						
 						msg = getConfig().getString("Strings.Ban.only-time-broadcast", "Nothing configured. Please check your config or report this bug." );
 						msg = msg.replaceAll("&ANAME;", name);
 						msg = msg.replaceAll("&TNAME;", nick);
 						msg = colorize(msg.replaceAll("&TIME;", time ));
 						
 						//msg = "Admin: "+name+" has banned "+cel.getDisplayName()+" for "+args[1];
 						 
 					} else // bez powodu/czasu  // for no reason / time
 					{
 
 						msg = getConfig().getString("Strings.Ban.perm-broadcast", "Nothing configured. Please check your config or report this bug." );
 						msg = msg.replaceAll("&ANAME;", name);
 						msg = colorize(msg.replaceAll("&TNAME;", nick));
 						
 						//msg = "Admin: "+name+" has banned "+cel.getDisplayName()+".";
 						kickmsg = colorize(getConfig().getString("Strings.Ban.perm-kickmsg", "Nothing configured. Please check your config or report this bug." ));
 					}
 					 
 					if(args.length>2 || noTime ){ // powod
 						powodd = ""; //reason
 						if ( noTime )
 							for( int a=1; a<args.length;a++)powodd += " "+args[a];
 						else
 							for( int a=2; a<args.length;a++)powodd += " "+args[a];
 						
 						//stringList.add(powod);
 		
 						kickmsg = colorize(getConfig().getString("Strings.Ban.t-and-r-kickmsg", "Nothing configured. Please check your config or report this bug." ));
 						kickmsg = kickmsg.replaceAll("&TIME;", time);
 						kickmsg = kickmsg.replaceAll("&REASON;", powodd);
 						
 						//kickmsg = "Banned for "+args[1]+" Reason:"+powodd;
 						msg = getConfig().getString("Strings.Ban.t-and-r-broadcast", "Nothing configured. Please check your config or report this bug." );
 						msg = msg.replaceAll("&ANAME;", name);
 						msg = msg.replaceAll("&TNAME;", nick);
 						msg = msg.replaceAll("&TIME;", time);
 						
 						msg = colorize(msg.replaceAll("&REASON;", powodd));
 						
 						//msg = "Admin: "+name+" has banned "+cel.getDisplayName()+" for "+args[1]+" Reason:"+powodd;
 						 
 					}
 					
 					//TODO: Add check for active bans. If found ask  if the banner wants to continue, with time and reaso
 					
 					//notify console ppl
 					log.info(msg);
 					
 					if(getConfig().getBoolean("Settings.Broadcast-ban-msg", true))						
 						for (Player playerdwa : this.getServer().getOnlinePlayers())
 			        	{
 			          		playerdwa.sendMessage(msg);	
 			           	}
 					
 					//Is the player on the server RIGHT NOW?
 					if(jest==true){
 
 						Player cel = this.getServer().getPlayer(args[0]);
 						cel.kickPlayer(kickmsg);
 						stringList.add(cel.getAddress().getAddress().getHostAddress());
 					} else {
 						if(getConfig().getBoolean("Settings.Use-IP-history", true) && this.mysqlEnabled ){
 
 					    	Connection conn = getConnection();
 							Statement state = null;
 				    		ResultSet rs;
 				    		try {
 				    			if (conn != null){
 				    			
 				    			state 					= conn.createStatement();
 				    			String safenick 		= nick.toLowerCase().replaceAll("'", "\"");
 				        		rs = state.executeQuery("SELECT * FROM `"+getConfig().getString("MySQL.table-history")+"` WHERE `name` = '"+safenick+"'");
 				    			if(rs.next()){
 				    				
 				    				stringList.add(rs.getString("ip"));
 				    				player.sendMessage("IP: "+rs.getString("ip"));
 				    				
 				    				
 				    			} else stringList.add("Offline");
 			    				
 				    			}
 				    		} catch (final SQLException ex) {
 				    			log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception)", ex);
 				    		} finally {
 				    			try {
 				    				if (state != null)
 				    					state.close();
 				    				if (conn != null)
 				    					conn.close();
 				    			} catch (final SQLException ex) {
 				    				log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) on close", ex);
 				    			}
 				    		
 				    		}
 						} else stringList.add("Offline");
 					}
 					stringList.add(czas);
 					stringList.add(powodd);
 					if  ( this.mysqlEnabled ){
 						
 					String safenick 		= nick.toLowerCase().replaceAll("'", "\"");
 	    			String safeAdminNick	= name.replaceAll("'", "\"");
 					String safereason 		= powodd.replaceAll("'", "\"");
 					
 					final Connection conn = getConnection();
 					Statement state = null;
 					
 					if (conn == null){
 						
 						player.sendMessage(ChatColor.RED+"[MYSQL] Connection error!");
 						return false;
 					}
 					try {
 						
 						state = conn.createStatement();
 						
 						long banfrom = System.currentTimeMillis() / 1000L;
 						
 						state.execute("INSERT INTO `"+getConfig().getString("MySQL.table")+
 						"` (`id`, `nick`, `adminNick` , `ip`, `banfrom`, `banto`, `reason`, `status`) VALUES (NULL, '"+
 								safenick+"', '" + safeAdminNick + "' ,'"+stringList.get(0)+"', '"+banfrom+"', '"+czas+"', '"+safereason+"', '1');");
 						
 						
 					} catch (final SQLException ex) {
 						log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) while banning player", ex);
 					} finally {
 						try {
 							if (state != null)
 								state.close();
 							if (conn != null)
 								conn.close();
 						} catch (final SQLException ex) {
 							log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) on close", ex);
 						}
 					}
 				
 				
 					} else {
 					
 					getBansConfig().set("bans." + nick.toLowerCase(), stringList);
 					saveBans();
 					}
 					
 				//} else {
 					//player.sendMessage(colorize(getConfig().getString("Strings.Error.Player is offline")));
 				//}
 			} else player.sendMessage(ChatColor.RED+"Usage: /ban <nick> [time] [reason]");
 				
 			} else player.sendMessage(colorize(getConfig().getString("Strings.Error.No-access", "No access") ));
 		
 			return true;
 		}
 		if (commandLabel.equalsIgnoreCase("banip")) {
 			if ( player.hasPermission( "advbans.banip" ) ) {
 				if(!getConfig().getBoolean("MySQL.Use", false)){
 					player.sendMessage("Only Supported on mysql");
 					return true;
 				}
 				if(args.length>0){
 				
 				if(args[0].isEmpty()){
 					player.sendMessage("Empty IP ADDRESS?..");
 					return true;
 				}
 					
 				
 					String msg = "";
 		            String kickmsg = "";
 		            String czas = "0";// time
 		            String powodd = getConfig().getString("Strings.Standard-reason", " ");
 		            
 		            String time = getConfig().getString("Settings.defaultBanTime", "2880"); // normal non unix time
 		            Boolean noTime = false;
 			           
 					if(args.length>1){ // czas
 						if( isParsableToInt(args[1]) ){
 							
 							if(Integer.valueOf(args[1])<=0){
 								time = args[1] = "0";
 							} else {
 								long unixTime = System.currentTimeMillis() / 1000L;
 								czas = String.valueOf((int)unixTime+(Integer.valueOf(args[1])*60));
 								time = args[1]=args[1];
 							}
 						} else {
 							long unixTime = System.currentTimeMillis() / 1000L;
 							czas = String.valueOf((int)unixTime+(Integer.valueOf( time )*60));
 							
 							noTime = true;
 						}
 
 						kickmsg = getConfig().getString("Strings.Ban.only-time-kickmsg", "Nothing configured. Please check your config or report this bug." );
 						kickmsg = colorize(kickmsg.replaceAll("&TIME;", time));
 						
 						msg = getConfig().getString("Strings.Ban.only-time-broadcast", "Nothing configured. Please check your config or report this bug." );
 						msg = msg.replaceAll("&ANAME;", name);
 						msg = msg.replaceAll("&TNAME;", args[0]);
 						msg = colorize(msg.replaceAll("&TIME;", time));
 						
 						//msg = "Admin: "+name+" has banned "+cel.getDisplayName()+" for "+args[1];
 						 
 					} else // bez powodu/czasu
 					{
 
 						msg = getConfig().getString("Strings.Ban.perm-broadcast", "Nothing configured. Please check your config or report this bug." );
 						msg = msg.replaceAll("&ANAME;", name);
 						msg = colorize(msg.replaceAll("&TNAME;", args[0]));
 						
 						//msg = "Admin: "+name+" has banned "+cel.getDisplayName()+".";
 						kickmsg = colorize(getConfig().getString("Strings.Ban.perm-kickmsg", "Nothing configured. Please check your config or report this bug." ));
 					}
 					
 					if(args.length>2 || noTime ){ // powod
 						powodd = ""; //reason
 						if ( noTime )
 							for( int a=1; a<args.length;a++)powodd += " "+args[a];
 						else
 							for( int a=2; a<args.length;a++)powodd += " "+args[a];
 						
 						//stringList.add(powod);
 		
 						kickmsg = colorize(getConfig().getString("Strings.Ban.t-and-r-kickmsg", "Nothing configured. Please check your config or report this bug." ));
 						kickmsg = kickmsg.replaceAll("&TIME;", time);
 						kickmsg = kickmsg.replaceAll("&REASON;", powodd);
 						
 						//kickmsg = "Banned for "+args[1]+" Reason:"+powodd;
 						msg = getConfig().getString("Strings.Ban.t-and-r-broadcast", "Nothing configured. Please check your config or report this bug." );
 						msg = msg.replaceAll("&ANAME;", name);
 						msg = msg.replaceAll("&TNAME;", args[0]);
 						msg = msg.replaceAll("&TIME;", time);
 						
 						msg = colorize(msg.replaceAll("&REASON;", powodd));
 						
 						//msg = "Admin: "+name+" has banned "+cel.getDisplayName()+" for "+args[1]+" Reason:"+powodd;
 						 
 					}
 					log.info(msg);
 					
 					if(getConfig().getBoolean("Settings.Broadcast-ban-msg", true))
 						for (Player playerdwa : this.getServer().getOnlinePlayers())
 			        	{
 			          		playerdwa.sendMessage(msg);	
 			           	}
 					
 					String ip = args[0].toLowerCase().replaceAll("'", "\"");
 					String safereason = powodd.replaceAll("'", "\"");
 					
 					final Connection conn = getConnection();
 					Statement state = null;
 					
 					if (conn == null){
 						
 						player.sendMessage(ChatColor.RED+"[MYSQL] Connection error!");
 						return false;
 					}
 					try {
 						
 						state = conn.createStatement();
 						
 						long banfrom = System.currentTimeMillis() / 1000L;
 						String nick = "IP BANNED";
 						if(getConfig().getBoolean("Settings.Use IP history", true)){
 							ResultSet rs;
 							rs = state.executeQuery("SELECT * FROM `"+getConfig().getString("MySQL.table-history")+"` WHERE `ip` = '"+ip+"' LIMIT 1");
 			    			if(rs.next()){
 			    				nick = rs.getString("name");
 			    				
 			    				player.sendMessage("Banned nick: "+nick);
 			    				
 			    				
 			    				
 			    			}
 				    		
 						}
 		    			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `"+getConfig().getString("MySQL.table")+"` (`id`, `nick`, `adminNick` ,`ip`, `banfrom`, `banto`, `reason`, `status`) VALUES (NULL, ?, ?, ?, ?, ?, ?, '1');");
 		    		//	'"+nick+"', '"+ip+"', '"+banfrom+"', '"+czas+"', '"+safereason+"'
 		    			
 		    			pstmt.setString (1,nick);
 		    			pstmt.setString (2,name);
 		    			pstmt.setString (3,ip);
 		    			pstmt.setLong   (4,banfrom);
 		    			pstmt.setString (5,czas);
 		    			pstmt.setString (6,safereason);
 		    			
 
 		    			pstmt.execute();//executeUpdate();
 		    			pstmt.close();
 		    			
 					//	state.execute("INSERT INTO `"+getConfig().getString("MySQL.table")+"` ");
 					} catch (final SQLException ex) {
 						log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) while banning player", ex);
 					} finally {
 						try {
 							if (state != null)
 								state.close();
 							if (conn != null)
 								conn.close();
 						} catch (final SQLException ex) {
 							log.log(Level.SEVERE, "[AdvancedBans] SQL problem (exception) on close", ex);
 						}
 					}
 				
 					
 				//} else {
 					//player.sendMessage(colorize(getConfig().getString("Strings.Error.Player is offline")));
 				//}
 			} else player.sendMessage(ChatColor.RED+"Usage: /banip <IP ADDRESS> [time] [reason]");		
 		} else player.sendMessage(colorize( getConfig().getString("Strings.Error.No-access", "No access") ));
 			return true;
 		} //end of if ipban
 		
 		return false;
 	}
 		
 					
 									
 				
 					
 					
 	
 	public static void download(URL u, File file) throws Exception {
 			/*
 			if (!file.getParentFile().exists())
 				file.getParentFile().mkdir();
 			*/
 			if (file.exists())
 				file.delete();
 			file.createNewFile();
 			final InputStream in = u.openStream();
 			final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
 			final byte[] buffer = new byte[1024];
 			int len;
 			while ((len = in.read(buffer)) >= 0) {
 				out.write(buffer, 0, len);
 			}
 			in.close();
 			out.close();
 	}
 	
 	public boolean isParsableToInt(String i) {
 		try {
 			Integer.parseInt(i);
 			return true;
 		} catch (NumberFormatException nfe) {
 			return false;
 		}
 	}
 	
 	public Connection getConnection() {
 		try {
 			return pool.getConnection();
 		} catch (final SQLException ex) {
 			log.log(Level.SEVERE, "[AdvancedBans] Error while fetching connection", ex);
 			return null;
 		} catch (NullPointerException e) {
 			log.log( Level.SEVERE, "[AVB] Can't get database connection ABORTING!" );
 			getServer().getPluginManager().disablePlugin(this); //kill plugin, we have serius problems
 			return null;
 		}
 	}
 	
 	/*
 	public boolean checkpermissions(Player player, String permission) {
 	/*	
 		if ( usingpermissions == 2 ) {
 			if (Permissions.has(player, permission)) {
 				return true;
 			} else {
 				return false;
 			}
 		} else if ( usingpermissions == 1 ) {	
 			if (PEX.has(player, permission, player.getWorld().getName() ) ) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 	*  -/
 			// we have no perm manager or stuff
 			return true;
 		//}
 	}
 	*/
     
 }
