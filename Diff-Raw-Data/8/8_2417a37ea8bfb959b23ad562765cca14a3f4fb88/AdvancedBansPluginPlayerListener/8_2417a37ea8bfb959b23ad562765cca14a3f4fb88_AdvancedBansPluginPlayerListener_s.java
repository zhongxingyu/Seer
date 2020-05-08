 package pl.kyku;
 
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 public class AdvancedBansPluginPlayerListener implements Listener
 {
     public static AdvancedBans plugin;
         
     public AdvancedBansPluginPlayerListener(AdvancedBans instance)
     {
         plugin = instance;
     }
     
     public ArrayList<Player> pokazac = new ArrayList<Player>();  // show
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerJoin(PlayerJoinEvent event){
     	
     	Player pl = event.getPlayer();
     	String safenick = pl.getDisplayName().toLowerCase().replaceAll("'", "\"");
 		
     	String ip = pl.getAddress().getAddress().getHostAddress();
     	if(pokazac.contains(pl)){
     		while (pokazac.remove(pl));
     	
 			pl.sendMessage(plugin.colorize(plugin.getConfig().getString("Strings.Join.Welcome-msg", "&GREEN;You have been unbanned. Welcome back!")));
     	} else if( plugin.mysqlEnabled ){
     		Connection conn = plugin.getConnection();
     		Statement state = null;
     		ResultSet rs;
     		try {
     			if (conn == null)
     				return;
     		state = conn.createStatement();
     		if(plugin.getConfig().getBoolean("Settings.Use-IP-history", true)){      		
     			
         		rs = state.executeQuery("SELECT * FROM `"+plugin.getConfig().getString("MySQL.table-history")+"` WHERE `name` = '"+safenick+"'");
     			if(!rs.next()){
 	    		//	int id = rs.getInt("id");
 	    			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `"+plugin.getConfig().getString("MySQL.table-history")+"` (`id`, `name`, `ip`) VALUES (NULL, ?, ?);", Statement.RETURN_GENERATED_KEYS );
 	    			
 	    			pstmt.setString(1,safenick);
 	    			pstmt.setString(2,ip);
 
 	    			pstmt.execute();//executeUpdate();
 	    			
 	    			/* This was custom code for MCC only.
 	    			 * TODO: remove if not going to be used
 	    		 	int plyNth = pstmt.getGeneratedKeys().getInt(1);
 	    			if ( plyNth == 3000 ) {
 	    				pl.sendMessage( "Congratulations! You are our " + ChatColor.DARK_RED +  plyNth + "th player!" );
 	    			} else{
 	    				pl.sendMessage( "Welcome new player! You are our " +  plyNth + "th player!" );
 	    			}
 	    			*/
 	    			
 	    			pstmt.close();
 	    			
     				AdvancedBans.log.log(Level.INFO,safenick+" - first join IP: "+ip);
     				
 
     			}
     			   	//
     			/*
     			 * state.executeQuery("");
     			 				    			int id = rs.getInt("id");
 				    			PreparedStatement pstmt = conn.prepareStatement("UPDATE "+config.getString("Advanced Bans.MySQL.table")+" SET status = 2 WHERE id=?");
 				    			
 				    			pstmt.setInt(1,id);
 
 				    			pstmt.executeUpdate();
 				    			pstmt.close();
 				    			
     			 */
         	}
     		
     		rs = state.executeQuery("SELECT * FROM `"+plugin.getConfig().getString("MySQL.table")+"` WHERE `ip` = '"+ip+"' And `status` = 1 ORDER BY  `banfrom` DESC");
 			
 				// BAN NA IP
 				
 			
     		while(rs.next()){
     			int czasdo = rs.getInt("banto");
     			long teraz = System.currentTimeMillis() / 1000L;
 				
     			if(czasdo == 0 || czasdo > teraz){
     				
     				pl.kickPlayer(plugin.colorize(plugin.getConfig().getString("Strings.Join.Banned-IP", "&RED;Your IP has been banned.&WHITE; Do not change nickname!")));
                 	//pl.sendMessage("masz bana [ip]");
                 	break;
     			}
     				
     				
     			
     			//AdvancedBans.log.log(Level.INFO, "BANEED: "+rs.getString(1)+", "+rs.getString(4));
     		}
     		
     		
 			
     		} catch (final SQLException ex) {
     			AdvancedBans.log.log(Level.SEVERE, "[AdvancedBans] SQL exception", ex);
     		} finally {
     			try {
     				if (state != null)
     					state.close();
     				if (conn != null)
     					conn.close();
     			} catch (final SQLException ex) {
     				AdvancedBans.log.log(Level.SEVERE, "[AdvancedBans] SQL exception on close", ex);
     			}
     		}
     		
     	} else if(plugin.getConfig().getBoolean("Settings.Ban-nickname-and-IP", true)){//:P
     		
     		for (String key : plugin.getBansConfig().getConfigurationSection("bans").getKeys( false ) ) {
                 List<String> stringList = plugin.getBansConfig().getStringList("bans." + key);
                 if (stringList != null) {
                     for (String fullAddress : stringList) {
                         if (fullAddress.equalsIgnoreCase(pl.getAddress().getAddress().getHostAddress())) {
                            //ban ip
                         	pl.kickPlayer(plugin.colorize(plugin.getConfig().getString("Strings.Join.Banned-IP", "&RED;Your IP has been banned.&WHITE; Do not change nickname!")));
                         	//pl.sendMessage("masz bana [ip]");
                         	break;
                         }
                     }
                 }
             }
     		
     	
     	}
     	
     	
     	
     }
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerLogin(PlayerLoginEvent event){
 
     	Player pl = event.getPlayer();
     	if ( plugin.mysqlEnabled ){
 
         	Connection conn = plugin.getConnection();
     		Statement state = null;
     		ResultSet rs;
     		try {
     			if (conn == null)
     				return;
     		state = conn.createStatement();
     		String safenick = pl.getDisplayName().toLowerCase().replaceAll("'", "\"");
     		
     		
     		
     		rs = state.executeQuery("SELECT * FROM `"+plugin.getConfig().getString("MySQL.table")+"` WHERE `nick` = '"+safenick+"' and `status` = 1 ORDER BY  `banfrom` DESC");
 			
 				// BAN NA IP
 				
 			
     		while(rs.next()){
     			int czasdo = rs.getInt("banto");
     			long teraz = System.currentTimeMillis() / 1000L;
 				
     			if(czasdo == 0){
     				String msg = "";
     				String powod = rs.getString("reason");
         			msg = plugin.getConfig().getString("Strings.Join.Banned-reason", "&RED;You have been banned from this server. Reason:&REASON;.");
 					msg = msg.replaceAll("&REASON;", powod);
 					msg = plugin.colorize(msg);
 					
         			event.disallow(Result.KICK_OTHER, msg);	
         			break;
     			}else if(czasdo > teraz){
     			
     				String msg = "";
     				String powod = rs.getString("reason");
     				long zostalo = (czasdo-teraz)/60+1;
     				
 					msg = plugin.getConfig().getString("Strings.Join.Banned-reason-left", "&RED;Banned! Reason:&REASON; (Left: &LEFT;min.)");
         			msg = msg.replaceAll("&REASON;", powod);
         			
         			msg = msg.replaceAll("&LEFT;", String.valueOf(zostalo));
 					
 					msg = plugin.colorize(msg);
 					
 					event.disallow(Result.KICK_OTHER, msg);
 					break;
     				
     			} else {
     				//state.executeQuery("UPDATE `ab_banlist` SET `status` = '0' WHERE  `id` = '"+rs.getInt("id")+"';");
     				int id = rs.getInt("id");
 	    			PreparedStatement pstmt = conn.prepareStatement("UPDATE "+plugin.getConfig().getString("MySQL.table")+" SET status = 0 WHERE id=?");
 	    			
 	    			pstmt.setInt(1,id);
 
 	    			pstmt.executeUpdate();
 	    			pstmt.close();
 	    			
 
     				if(plugin.getConfig().getBoolean("Settings.Welcome-message", true))
 				    	pokazac.add(pl);
     				
     				break;
     				
     			}
     			
     			//AdvancedBans.log.log(Level.INFO, "BANEED: "+rs.getString(1)+", "+rs.getString(4));
     		}
     		
     		
 			
     		} catch (final SQLException ex) {
     			AdvancedBans.log.log(Level.SEVERE, "[AdvancedBans] SQL exception", ex);
     		} finally {
     			try {
     				if (state != null)
     					state.close();
     				if (conn != null)
     					conn.close();
     			} catch (final SQLException ex) {
     				AdvancedBans.log.log(Level.SEVERE, "[AdvancedBans] SQL exception on close", ex);
     			}
     		}
     		
     		//end of mysql banning, start of file
     	} else {
     	ConfigurationSection onlyBans = plugin.getBansConfig().getConfigurationSection("bans");
     	if ( onlyBans.getKeys(false).contains( pl.getName().toLowerCase() )  ) {
             //ban nick
         	List<String> slist = plugin.getBansConfig().getStringList("bans."+pl.getName().toLowerCase() );
         	if(slist.size()==3){
         		String powod = slist.get(2); //reason
         		
 
         		long unixTime = System.currentTimeMillis() / 1000L;
         		long zostalo = Long.valueOf(slist.get(1));  //remaining time
         		if(zostalo == 0){
         			//player banned for ever, don display time left
         			
         			String msg = "";
         			msg = plugin.getConfig().getString("Strings.Join.Banned-reason", "&RED;You have been banned from this server. Reason:&REASON;.");
 					msg = msg.replaceAll("&REASON;", powod);
 					msg = plugin.colorize(msg);
 					
         			event.disallow(Result.KICK_OTHER, msg);
         			
         		} else {
 					zostalo = (zostalo-unixTime)/60+1;
 					if(zostalo>0)
 					{
 
 						String msg = "";
 	        		
 						msg = plugin.getConfig().getString("Strings.Join.Banned-reason-left", "&RED;Banned! Reason:&REASON; (Left: &LEFT;min.)");
 	        			msg = msg.replaceAll("&REASON;", powod);
 	        			
 	        			msg = msg.replaceAll("&LEFT;", String.valueOf(zostalo));
 						
 						msg = plugin.colorize(msg);
 						
 						event.disallow(Result.KICK_OTHER, msg);
 					} else {
 						 //if (getBansConfig().getKeys("bans").contains(pl.getna.toLowerCase())) {
 					    if(plugin.getConfig().getBoolean("Settings.Welcome-message", true))
 					    	pokazac.add(pl);
 						plugin.getBansConfig().set("bans." + pl.getName().toLowerCase(), null);
 					    plugin.saveBans();
 					   
 					}
         		}
         	
         	}
         }/* else {
         	//stuff here would run if there wasn't a FILE ban
         
         }*/
     	}
     }
     
     
 }
