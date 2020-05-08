 package com.mcprohosting.plugins.marcwar.listeners;
 
 import com.mcprohosting.plugins.marcwar.utilities.TeamHandler;
 import org.bukkit.Location;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener {
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onJoin(PlayerJoinEvent event) {
 		Location location = TeamHandler.assignTeam(event.getPlayer().getName());
 		if (!(location.getBlockY() == 0)) {
 			event.getPlayer().teleport(location);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onDeath(PlayerDeathEvent event) {
		TeamHandler.getPlayerTeam(event.getEntity().getName()).removPlayer(event.getEntity().getName());
 		//UtilityMethods.redirectToServer("1-marcwars-lobby", event.getEntity());
 	}
 
 }
