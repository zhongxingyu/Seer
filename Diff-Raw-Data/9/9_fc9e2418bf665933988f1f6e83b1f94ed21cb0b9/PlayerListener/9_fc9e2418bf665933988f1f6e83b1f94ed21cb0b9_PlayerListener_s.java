 package com.mcprohosting.plugins.mindcrack.kotl.listeners;
 
 import com.mcprohosting.plugins.mindcrack.kotl.KotL;
 import com.mcprohosting.plugins.mindcrack.kotl.database.DatabaseManager;
 import com.mcprohosting.plugins.mindcrack.kotl.utitilies.Helpers;
 import com.mcprohosting.plugins.mindcrack.kotl.utitilies.SpawnHandler;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class PlayerListener implements Listener {
 	@EventHandler
 	public void onHungerChange(FoodLevelChangeEvent event) {
 		event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onDamage(EntityDamageEvent event) {
 		if (event.getEntityType().equals(EntityType.PLAYER)) {
 			Player player = (Player) event.getEntity();
 
			if (player.getLocation().getY() < 85) {
				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onDeath(PlayerDeathEvent event) {
 		event.getDrops().clear();
		if (!event.getEntity().hasPermission("kotl.mindcrack")) {
 			Helpers.redirectToServer("1-kotl-lobby", event.getEntity());
 		}
 	}
 
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent event) {
 		SpawnHandler.randomlySpawnPlayer(event.getPlayer());
 	}
 
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event) {
 		if (!DatabaseManager.containsPlayer(event.getPlayer().getName())) {
 			DatabaseManager.addPlayer(event.getPlayer().getName());
 		}
 
 		SpawnHandler.randomlySpawnPlayer(event.getPlayer());
 
 		KotL.getLeaderboard().addPlayer(event.getPlayer());
 	}
 
 	@EventHandler
 	public void onQuit(PlayerQuitEvent event) {
 		KotL.getLeaderboard().removePlayer(event.getPlayer().getName());
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (!event.getPlayer().hasPermission("kotl.changeblocks")) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if (!event.getPlayer().hasPermission("kotl.changeblocks")) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if (event.getTo().getY() < 0) {
 			SpawnHandler.randomlySpawnPlayer(event.getPlayer());
 		}
 		if (event.getTo().getBlockY() == KotL.getLadder().getCenterY() && event.getFrom().getBlockY() < event.getTo().getBlockY()) {
 			event.getPlayer().sendMessage("You are half way to the top of the ladder!");
 		}
 	}
 }
