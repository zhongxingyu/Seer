 package com.MoofIT.Minecraft.DeathWish;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class DeathWishEntityListener implements Listener {
 	private DeathWish plugin;
 	private static HashMap<String, EntityDamageEvent> dyingPlayers = new HashMap<String, EntityDamageEvent>();
 
 	public DeathWishEntityListener(DeathWish instance) {
 		this.plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (event.isCancelled()) return;
 		if (!(event.getEntity() instanceof Player))return;
 
 		Player player = (Player)event.getEntity();
 		if (player.getHealth() - event.getDamage() <= 0) {
 			dyingPlayers.put(player.getName(), event);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDeath(EntityDeathEvent event)
 	{
 		if (!(event.getEntity() instanceof Player)) return;
 		Player player = (Player)event.getEntity();
 		if (!player.hasPermission("deathwish.display")) return;
 		String playerName = player.getName();
 
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new YouLose(plugin,player,dyingPlayers.get(playerName),event));
 		dyingPlayers.remove(playerName);
 	}
 }
