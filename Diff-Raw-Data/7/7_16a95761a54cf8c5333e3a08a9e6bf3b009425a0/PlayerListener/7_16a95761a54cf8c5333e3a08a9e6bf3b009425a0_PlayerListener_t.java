 package net.mayateck.OldEdenCore;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class PlayerListener implements Listener{
 	private OldEdenCore plugin;
 	public PlayerListener(OldEdenCore plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         this.plugin = plugin;
     }
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent evt){
 		Player player = evt.getPlayer();
 		String pname = player.getName();
 		evt.setJoinMessage(AlertsHandler.alertSys+ChatColor.BLUE+pname+ChatColor.GRAY+" connected. Sync Completed.");
 		plugin.getLogger().info("Player "+pname+" connected.");
 		if (plugin.getConfig().contains("players."+pname)){
 			player.sendMessage(OldEdenCore.serverHead+"Welcome back, "+pname+".");
 			plugin.getLogger().info("PlayerData found.");
 		} else {
 			player.sendMessage(OldEdenCore.serverHead+"Welcome to Old Eden!");
 			plugin.getLogger().info("PlayerData not found. Creating...");
 			player.sendMessage(OldEdenCore.serverHead+"Please wait while first-time setup is run.");
			plugin.getConfig().set("players."+pname+".regionID", "none");
			plugin.getConfig().set("players."+pname+".funds", 0);
 			plugin.saveConfig();
 			player.sendMessage(OldEdenCore.serverHead+"Complete! Welcome "+pname+"!");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent evt){
 		Player player = evt.getPlayer();
 		String pname = player.getName();
 		evt.setQuitMessage(AlertsHandler.alertSys+ChatColor.BLUE+pname+ChatColor.GRAY+" disconnected.");
 	}
 }
