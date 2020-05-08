 /**
  * Copyright (C) 2012 t7seven7t
  */
 package net.t7seven7t.swornguard.listeners;
 
 import net.t7seven7t.swornguard.SwornGuard;
 import net.t7seven7t.swornguard.types.PlayerData;
 import net.t7seven7t.swornguard.types.Reloadable;
 
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 /**
  * @author t7seven7t
  */
 public class EntityListener implements Listener, Reloadable {
 	private final SwornGuard plugin;
 	private boolean autoClickerDetectorEnabled;
 	private boolean factionBetrayalDetectorEnabled;
 	private boolean combatLogDetectorEnabled;
 	
 	public EntityListener(final SwornGuard plugin) {
 		this.plugin = plugin;
 		this.reload();
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
 		if (!event.isCancelled()) {			
 			if (event.getDamager() instanceof Player) {
 				PlayerData data = plugin.getPlayerDataCache().getData((Player) event.getDamager());
 				
 				// Don't want these players to be able to hurt other players while doing their duties.
 				if (data.isCooldownPatrolling() || data.isInspecting() || data.isPatrolling() || data.isVanished()) {
 					event.setCancelled(true);
 					return;
 				}
 				
 				if (autoClickerDetectorEnabled) {
 					if (plugin.getAutoClickerDetector().isClickingTooFast((Player) event.getDamager())) {
 						event.setCancelled(true);
 						return;
 					}
 				}
 				
 				if (event.getEntity() instanceof Player) {					
 					if (factionBetrayalDetectorEnabled) {
 						plugin.getFactionBetrayaldetector().check((Player) event.getEntity(), (int) event.getDamage(), (Player) event.getDamager());
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDamage(final EntityDamageEvent event) {
 		if (!event.isCancelled()) {
 			if (event.getEntity() instanceof Player) {
 				PlayerData data = plugin.getPlayerDataCache().getData((Player) event.getEntity());
 				
 				// Don't want these players being hurt while doing their duties.
 				if (data.isCooldownPatrolling() || data.isInspecting() || data.isPatrolling() || data.isVanished()) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDamageByEntityMonitor(final EntityDamageByEntityEvent event) {
 		if (! event.isCancelled()) {
 			Entity attacker = event.getDamager();
 			
 			Player att = null;
 			
 			// Figure out the attacker
 			if (attacker instanceof Player) {
 				att = (Player) attacker;
 			} else if (attacker instanceof Projectile) {
 				Projectile proj = (Projectile) attacker;
				if (proj.getShooter() instanceof Player) {
					att = (Player) proj.getShooter();
 				}
 			}
 
 			if (att != null) {
 
 				// Check if target is living entity and thus actually has health
 				if (event.getEntity() instanceof LivingEntity) {
 					
 					// Check that target's health will drop below zero with this event succeeding
 					if (((LivingEntity) event.getEntity()).getHealth() - event.getDamage() < 0) {
 						PlayerData data = plugin.getPlayerDataCache().getData(att); 
 						
 						// If target is player...
 						if (event.getEntity() instanceof Player && System.currentTimeMillis() - data.getLastPlayerKill() > 300L) {
 							data.setLastPlayerKill(System.currentTimeMillis());
 							data.setPlayerKills(data.getPlayerKills() + 1);
 						}
 					
 						// If target is monster...
 						if (event.getEntity() instanceof Monster && System.currentTimeMillis() - data.getLastMonsterKill() > 300L) {
 							data.setLastMonsterKill(System.currentTimeMillis());
 							data.setMonsterKills(data.getMonsterKills() + 1);
 						}
 						
 						// If target is animal...
 						if (event.getEntity() instanceof Animals && System.currentTimeMillis() - data.getLastAnimalKill() > 300L) {
 							data.setLastAnimalKill(System.currentTimeMillis());
 							data.setAnimalKills(data.getAnimalKills() + 1);
 						}
 					}
 				}
 			}
 
 			// Monitor recent damage sources
 			if (event.getEntity() instanceof Player) {
 				if (combatLogDetectorEnabled) {
 					PlayerData data = plugin.getPlayerDataCache().getData(((Player) event.getEntity()).getName());
 					
 					if (event.getDamager() instanceof Monster && plugin.getConfig().getBoolean("combatLogFromMobs")) {
 						data.setLastAttacked(System.currentTimeMillis());
 					} else if (event.getDamager() instanceof Player && plugin.getConfig().getBoolean("combatLogFromPlayers")) {
 						data.setLastAttacked(System.currentTimeMillis());
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void reload() {
 		this.autoClickerDetectorEnabled = plugin.getConfig().getBoolean("autoclickerDetectorEnabled");
 		this.combatLogDetectorEnabled = plugin.getConfig().getBoolean("combatLogDetectorEnabled");
 		this.factionBetrayalDetectorEnabled = plugin.getConfig().getBoolean("factionBetrayalDetectorEnabled");
 	}
 
 }
