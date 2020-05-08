 package com.jpmiii.Realmscraft;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 
 
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 
 
 import com.jpmiii.Realmscraft.Realmscraft;
 public class RealmscraftListener  implements Listener {
 	private Realmscraft plugin;
 	
 
     public RealmscraftListener(Realmscraft plugin) {
     	this.plugin = plugin;
 	}
     
 
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void normalLogin(PlayerLoginEvent event) {
 
 
 
     	try {
 			Realmscraft.dbm.loadPlayer(event.getPlayer());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
      	event.getPlayer().setCompassTarget(plugin.portalLoc);
         
     }
     @EventHandler(priority = EventPriority.MONITOR)
     public void portalStep(PlayerInteractEvent event) {
 
     	if (event.getAction() == Action.PHYSICAL && !plugin.getConfig().getString("portalServer").isEmpty()) {
     		//plugin.portalLoc = new  Location(event.getPlayer().getWorld(),0,61, 0) ;
     		if (event.getClickedBlock().getLocation().distance(plugin.portalLoc) <=  5 && !plugin.hotPlayers.containsKey(event.getPlayer().getName())){
    			if(plugin.perms.has(event.getPlayer().getPlayer(), "realmscraft.portal")) {
    				plugin.getServer().getPlayer(event.getPlayer().getName()).updateInventory();
     	            Realmscraft.dbm.savePlayer(event.getPlayer());
     		        plugin.hotPlayers.put(event.getPlayer().getName(), System.currentTimeMillis( ));
     		
 			        ByteArrayOutputStream b = new ByteArrayOutputStream();
 			        DataOutputStream out = new DataOutputStream(b);
 			 
 			        try {
 			            out.writeUTF("Connect");
 			            out.writeUTF(plugin.getConfig().getString("portalServer")); // Target Server
 			        } catch (IOException e) {
 			        // Can never happen
 			        }
 			        event.getPlayer().sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());
     		        this.plugin.getLogger().info(event.getClickedBlock().getLocation().toString());
     		        
     		        Realmscraft.dbm.savePlayer(event.getPlayer());
     		        
     		    }
     		}
     	}
 
     	
  
         
     }
 	
     @EventHandler
     public void PlayerBed(PlayerBedEnterEvent event) {
     	if (plugin.perms.has(event.getPlayer(), "realmscraft.sleep") && !plugin.getConfig().getString("sleepServer").isEmpty()) {
     		String[] msg = {"Would you like to goto", "Dream Land?"};
     		event.getPlayer().sendMessage(msg);
     		
     	}
 
     }
     @EventHandler
     public void PlayerYes(AsyncPlayerChatEvent event) {
     	if (plugin.perms.has(event.getPlayer(), "realmscraft.sleep") && !plugin.getConfig().getString("sleepServer").isEmpty()) {
     		//plugin.getLogger().info(event.getPlayer().getName() + " has permission");
     	
     	    if (event.getPlayer().isSleeping()) {
     	    	//plugin.getLogger().info(event.getPlayer().getName() + " has said" + event.getMessage());
 
     	    	if(event.getMessage().equalsIgnoreCase("yes")) {
     		
     	    		plugin.combatApi.untagPlayer(event.getPlayer().getName());
     	    		ByteArrayOutputStream b = new ByteArrayOutputStream();
     		        DataOutputStream out = new DataOutputStream(b);
     		 
     	    	    try {
     		            out.writeUTF("Connect");
     		            out.writeUTF(plugin.getConfig().getString("sleepServer")); // Target Server
     		        } catch (IOException e) {
     		    // Can never happen
     		        }
     		        event.getPlayer().sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());
     		    
     	    	}
     	    }
     	}
     	
     }
 }
