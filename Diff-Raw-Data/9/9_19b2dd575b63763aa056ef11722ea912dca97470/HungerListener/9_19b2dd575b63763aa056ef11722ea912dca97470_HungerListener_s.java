 package com.TeamNovus.Supernaturals.Listeners.Custom;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.TeamNovus.Supernaturals.SNPlayers;
 import com.TeamNovus.Supernaturals.Player.SNPlayer;
 
 public class HungerListener implements Listener {
 
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onFoodLevelChange(FoodLevelChangeEvent event) {
 		if(event.isCancelled())
 			return;
 		
 		if(event.getEntity() instanceof Player) {
 			SNPlayer player = SNPlayers.i.get((Player) event.getEntity());
												
			int newLevel = (player.getFoodLevel() + (event.getFoodLevel() - ((Player) event.getEntity()).getFoodLevel()));
						
 			event.setFoodLevel((int) Math.ceil(player.getFoodLevel() * 20 / player.getMaxFoodLevel()));
		
			player.setFoodLevel(newLevel);	
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		SNPlayer player = SNPlayers.i.get(event.getPlayer());
 
 		player.setFoodLevel(player.getMaxFoodLevel());
 		player.updateFoodLevel();
 	}
 		
 }
