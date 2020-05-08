 package com.selfequalsthis.grubsplugin.listeners;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class GrubsPlayerListener extends PlayerListener {
 	
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player p = event.getPlayer();
 		World w = p.getWorld();
 		p.sendMessage("Welcome, " + p.getDisplayName() + "!");
 		p.sendMessage("Current game time is: " + w.getTime());
 		
 		if (w.getPlayers().size() > 0) {
 			String playerListStr = "";
 			boolean useSeparator = false;
 			
 			for (Player player : w.getPlayers()) {
 				if (useSeparator) {
 					playerListStr += ", ";
 				}
 				else {
 					useSeparator = true;
 				}
 				playerListStr += player.getDisplayName();
 			}
 			
 			p.sendMessage("Currently playing: " + playerListStr);
 		}
 		else {
 			p.sendMessage("No other players currently here.");
 		}
 	}
 	
 }
