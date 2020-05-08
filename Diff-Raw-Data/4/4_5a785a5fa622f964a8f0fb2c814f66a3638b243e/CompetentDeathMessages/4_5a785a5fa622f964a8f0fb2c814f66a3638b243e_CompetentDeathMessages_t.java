 package com.republicasmp.cdm;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CompetentDeathMessages extends JavaPlugin implements Listener {
 	private HashMap<String, String> lastDamagerMap;
 	private HashMap<String, Long> lastTimeDamagedByPlayerMap;
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("fakekill")) {
 			if (sender instanceof Player) {
 				Bukkit.broadcastMessage(MessageWrapper.getMessage(args[0], sender.getName(), ((Player) sender).getItemInHand().getType()));
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player victim = event.getEntity();
 		String victimName = victim.getDisplayName();
 		EntityDamageEvent lastDamageEvent = victim.getLastDamageCause();
 		if (lastDamageEvent instanceof EntityDamageByBlockEvent) {
 			EntityDamageByBlockEvent blockDamageEvent = (EntityDamageByBlockEvent) lastDamageEvent;
 			Material blockType = blockDamageEvent.getDamager().getType();
 			event.setDeathMessage(MessageWrapper.getMessage(victimName, blockType));
 		} else if (lastDamageEvent instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) lastDamageEvent;
 			Entity damager = entityDamageEvent.getDamager();
 			if (damager instanceof Player) {
 				Player killer = (Player) damager;
 				String killerName = killer.getDisplayName();
 				Material heldItemType = killer.getItemInHand().getType();
 				event.setDeathMessage(MessageWrapper.getMessage(victimName, killerName, heldItemType));
 			} else {
 				event.setDeathMessage(MessageWrapper.getMessage(victimName, damager.getType()));
 			}
 		} else {
 			DamageCause cause = lastDamageEvent.getCause();
			if (System.currentTimeMillis() - lastTimeDamagedByPlayerMap.get(victimName) < 5000) {
 				event.setDeathMessage(MessageWrapper.getMessage(victimName, lastDamagerMap.get(victimName), cause));
 			} else {
 				event.setDeathMessage(MessageWrapper.getMessage(victimName, cause));
 			}
 		}
 	}
 	
	@EventHandler
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		Entity victim = event.getEntity();
 		if (victim instanceof Player) {
 			Entity victor = event.getDamager();
 			if (victor instanceof Projectile) {
 				victor = ((Projectile) victor).getShooter();
 			}
 			if (victor instanceof Player) {
 				lastDamagerMap.put(((Player) victim).getDisplayName(), ((Player) victor).getDisplayName());
 				lastTimeDamagedByPlayerMap.put(((Player) victim).getDisplayName(), System.currentTimeMillis());
 			}
 		}
 	}
 
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		lastDamagerMap = new HashMap<String, String>();
 		lastTimeDamagedByPlayerMap = new HashMap<String, Long>();
 	}
 }
