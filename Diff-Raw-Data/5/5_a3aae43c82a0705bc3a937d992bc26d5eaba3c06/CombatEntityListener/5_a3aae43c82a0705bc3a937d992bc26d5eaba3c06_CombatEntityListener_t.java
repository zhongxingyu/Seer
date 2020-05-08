 package net.milkbowl.combatevents.listeners;
 
 import java.util.Iterator;
 
 import net.milkbowl.combatevents.Camper;
 import net.milkbowl.combatevents.CombatEventsCore;
 import net.milkbowl.combatevents.CombatPlayer;
 import net.milkbowl.combatevents.CombatReason;
 import net.milkbowl.combatevents.Config;
 import net.milkbowl.combatevents.KillType;
 import net.milkbowl.combatevents.LeaveCombatReason;
 import net.milkbowl.combatevents.Utility;
 import net.milkbowl.combatevents.events.EntityKilledByEntityEvent;
 import net.milkbowl.combatevents.events.PlayerEnterCombatEvent;
 import net.milkbowl.combatevents.events.PlayerLeaveCombatEvent;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTargetEvent;
 
 public class CombatEntityListener extends EntityListener {
 
 	private CombatEventsCore plugin;
 	public CombatEntityListener(CombatEventsCore plugin) {
 		this.plugin = plugin;
 	}
 
 	public void OnEntityTarget(EntityTargetEvent event) {
 		if (event.isCancelled() || !(event.getTarget() instanceof Player) || !(event.getEntity() instanceof Creature) || !Config.isTargetTriggersCombat() )
 			return;
 
 		Player player = (Player) event.getTarget();
 		Creature cEntity = (Creature) event.getEntity();
 		//If this target is a Player and within the proper distance fire our EnterCombatEvent
 		if (Utility.getDistance(player.getLocation(), cEntity.getLocation()) <= Config.getTargetTriggerRange()) {
 			plugin.getServer().getPluginManager().callEvent(new PlayerEnterCombatEvent(player, CombatReason.TARGETED_BY_MOB));
 			plugin.enterCombat(player, new CombatPlayer(player, CombatReason.TARGETED_BY_MOB, cEntity, plugin), cEntity, CombatReason.TARGETED_BY_MOB);
 		}
 	}
 
 	public void onEntityDamage(EntityDamageEvent event) {
 		//Disregard this event?
 		if (event.isCancelled() || !isValidEntity(event.getEntity()))
 			return;
 
 
 		//Convert the entity.
 		LivingEntity cEntity = (LivingEntity) event.getEntity();
 
 		//Don't even think about trying to pop into combat against a dead entity.
 		if (cEntity.getHealth() <= 0)
 			return;
 
 		//Reasons to pop us into combat
 		CombatReason reason = null;
 		Player player = null;
 		Entity rEntity = null;
 		Player pvpPlayer = null;
 		if (event instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
 			if (subEvent.getDamager() instanceof Tameable && Config.isPetTriggersCombat()) {
 				player = getOwner((Tameable) subEvent.getDamager());
 				if (player != null) {
 					reason = CombatReason.PET_ATTACKED;
 					rEntity = subEvent.getEntity();
 				}
 			} else if (subEvent.getEntity() instanceof Tameable && Config.isPetTriggersCombat()) {
 				player = getOwner((Tameable) subEvent.getEntity());
 				if (player != null) {
 					reason = CombatReason.PET_TOOK_DAMAGE;
 					rEntity = subEvent.getDamager();
 				}
 			} else if (subEvent.getEntity() instanceof Player) {
 				player = (Player) subEvent.getEntity();
 				rEntity = subEvent.getDamager();
 				if (subEvent.getDamager() instanceof Player) {
 					pvpPlayer = (Player) subEvent.getDamager();
 					reason = CombatReason.DAMAGED_BY_PLAYER;
 				} else 
 					reason = CombatReason.DAMAGED_BY_MOB;
 			} else if (subEvent.getDamager() instanceof Player && !(subEvent.getEntity() instanceof Player)) {
 				player = (Player) subEvent.getDamager();
 				reason = CombatReason.ATTACKED_MOB;
 				rEntity = subEvent.getEntity();
 			} 
 		} else if (event instanceof EntityDamageByProjectileEvent) {
 			EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
 			if (subEvent.getDamager() instanceof Tameable && Config.isPetTriggersCombat()) {
 				player = getOwner((Tameable) subEvent.getDamager());
 				if (player != null) {
 					reason = CombatReason.PET_ATTACKED;
 					rEntity = subEvent.getEntity();
 				}
 			} else if (subEvent.getEntity() instanceof Tameable && Config.isPetTriggersCombat()) {
 				player = getOwner((Tameable) subEvent.getEntity());
 				if (player != null) {
 					reason = CombatReason.PET_TOOK_DAMAGE;
 					rEntity = subEvent.getDamager();
 				}
 			} else if (subEvent.getEntity() instanceof Player) {
 				player = (Player) subEvent.getEntity();
 				rEntity = subEvent.getDamager();
 				if (subEvent.getDamager() instanceof Player) {
 					pvpPlayer = (Player) subEvent.getDamager();
 					reason = CombatReason.DAMAGED_BY_PLAYER;
 				} else
 					reason = CombatReason.DAMAGED_BY_MOB;
 			}  else if (subEvent.getDamager() instanceof Player && !(subEvent.getEntity() instanceof Player)) {
 				player = (Player) subEvent.getDamager();
 				reason = CombatReason.ATTACKED_MOB;
 				rEntity = subEvent.getEntity();
 			}
 		}
 		
 		if (reason != null && player != null) {
 			CombatPlayer cPlayer = new CombatPlayer(player, reason, rEntity, plugin);
 			plugin.enterCombat(player, cPlayer, rEntity, reason);
 
 			//If this is a PvP event lets tag the other player as in-combat or update their times
 			if (reason.equals(CombatReason.DAMAGED_BY_PLAYER)) {
 				plugin.enterCombat(pvpPlayer, new CombatPlayer(pvpPlayer, CombatReason.ATTACKED_PLAYER, rEntity, plugin), player, CombatReason.ATTACKED_PLAYER);
 			}
 		}
 	}
 
 	public void onEntityDeath (EntityDeathEvent event) {
 		//Reasons to disregard this event
 		if ( !isValidEntity(event.getEntity()) )
 			return;
 
 		LivingEntity cEntity = (LivingEntity) event.getEntity();
 
 
 		/**
 		 * Removes the dying entity from the KillMap if they are in it
 		 * and fires the appropriate event.
 		 * 
 		 * This new Event WILL override the EntityKilled events drops if
 		 * the drops are changed in the sub-event
 		 * 
 		 */
 		LivingEntity attacker = null;
 		KillType killType = null;
 		if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
 			attacker = (LivingEntity) ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
 			killType = KillType.NORMAL;
 		} else if (event.getEntity().getLastDamageCause() instanceof EntityDamageByProjectileEvent) {
 			attacker = (LivingEntity) ((EntityDamageByProjectileEvent) event.getEntity().getLastDamageCause()).getDamager();
 			killType = KillType.PROJECTILE;
 		}
 		if ( attacker != null ) {
 			//Lets check if this player is camping and adjust the Kill Reason appropriately
 			if (attacker instanceof Player && !(event.getEntity() instanceof Player) && Config.isAntiCamp()) {
 				Player p = (Player) attacker;
 				Camper campPlayer;
 				if (plugin.getCampMap().containsKey(p.getName())) {
 					campPlayer = plugin.getCampMap().get(p.getName());
 					plugin.getServer().getScheduler().cancelTask(campPlayer.getCampTask());
 					if (Utility.getDistance(p.getLocation(), campPlayer.getSpawner()) > Config.getCampRange() * 2) {
 						plugin.getCampMap().remove(p);
 					} else {
 						campPlayer.addKill();
 						campPlayer.setCampTask(plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new CampRemover(p), Config.getCampTime() * 20));
 						if (campPlayer.getKills() > Config.getCampKills()) 
 							killType = KillType.CAMPING;
 						if (campPlayer.getKills() >= Config.getCampKills())
 							p.sendMessage(Config.getCampMessage());
 					}
 				} else {
 					//If this player is near a spawner lets add them to the camp detector
 					Location spawnLoc = Utility.findSpawner(p.getLocation());
 					if (spawnLoc != null) {
 						campPlayer = new Camper(spawnLoc);
 						campPlayer.setCampTask(plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new CampRemover(p), Config.getCampTime() * 20));
 					}
 				}
 			}
 			EntityKilledByEntityEvent kEvent = new EntityKilledByEntityEvent(attacker, cEntity, event.getDrops(), killType);
 			plugin.getServer().getPluginManager().callEvent(kEvent);
 		} else {
 			return;
 		}
 
 		/**
 		 * If this is a player, lets run our checks and remove them from combat with any other
 		 * players.
 		 * 
 		 */
 		if (cEntity instanceof Player) {
 			Player player = (Player) cEntity;
 			//If this player is in the camp map, lets remove them
 			plugin.getCampMap().remove(player);
 
 			//Check 
 			Iterator<Entity> iter = plugin.getCombatPlayer(player).getReasonMap().keySet().iterator();
 			while (iter.hasNext()) {
 				Entity entity = iter.next();
 				if (entity instanceof Player) {
 					Player p = (Player) entity;
 					CombatReason lastReason = plugin.getCombatPlayer(p).removeReason(player);
 					if (plugin.getCombatPlayer(p).getReasonMap().isEmpty()) {
 						//If the mapping is empty lets leave combat.
 						if (throwPlayerLeaveCombatEvent(p, LeaveCombatReason.TARGET_DIED, new CombatReason[] {lastReason}))
 							plugin.getServer().getScheduler().cancelTask(plugin.getCombatTask(p));
 
 						plugin.leaveCombat(p);
 					}
 					break;
 				}
 			}
 			/**
 			 * remove the Killed player from combat and remove their combat reasons.
 			 * 
 			 */
 			if (throwPlayerLeaveCombatEvent(player, LeaveCombatReason.DEATH, plugin.getCombatPlayer(player).getReasons())) {
 				plugin.getCombatPlayer(player).clearReasons();
 				//Cancel the task and remove the player from the combat map and send our leave combat message
 				plugin.getServer().getScheduler().cancelTask(plugin.getCombatTask(player));
 			}
 			plugin.leaveCombat(player);	
 		} 
 		/**
 		 * if a player didn't die lets run a check and remove the entity from any other player
 		 * combat maps.
 		 */
 		else {
 			for ( Player p : plugin.getServer().getOnlinePlayers() ) {
 				if (plugin.getCombatPlayer(p) == null)
 					continue;
 
 				CombatReason[] lastReasons = null;
 				if (plugin.getCombatPlayer(p).getReasons().length == 1)
 					lastReasons =  plugin.getCombatPlayer(p).getReasons();
 
 				Iterator<Entity> iter = plugin.getCombatPlayer(p).getReasonMap().keySet().iterator();
 				while (iter.hasNext()) {
 					Entity entity = iter.next();
 					if (entity.equals(cEntity)) {
 						iter.remove();
 						break;
 					}
 				}
 				/**
 				 *  After we removed the entity, lets kick the player out of combat if
 				 *  they have no more reasons to be in combat
 				 */
 				if (plugin.getCombatPlayer(p).getReasonMap().isEmpty()) {
 					//If the mapping is empty lets leave combat.
 					if (throwPlayerLeaveCombatEvent(p, LeaveCombatReason.TARGET_DIED, lastReasons))
 						plugin.getServer().getScheduler().cancelTask(plugin.getCombatTask(p));
 
 					plugin.leaveCombat(p);
 				}
 			}
 		}
 	}
 
 	private boolean throwPlayerLeaveCombatEvent(Player player, LeaveCombatReason leaveReason, CombatReason[] reasons) {
 		PlayerLeaveCombatEvent event = new PlayerLeaveCombatEvent(player, leaveReason, reasons);
 		plugin.getServer().getPluginManager().callEvent(event);
 		//Return if the event was successful
 		return !event.isCancelled();
 	}
 
 	private Player getOwner (Tameable tEntity) {
 		if (tEntity.isTamed())
 			if (tEntity.getOwner() instanceof Player) 
 				return (Player) tEntity.getOwner();
 
 		return null;
 	}
 
 	private boolean isValidEntity (Entity thisEntity) {
 		if ( !(thisEntity instanceof LivingEntity) )
 			return false;
 
 		return true;
 	}
 
 	public class CampRemover implements Runnable {
 
 		Player player;
 		CampRemover(Player player) {
 			this.player = player;
 		}
 
 		@Override
 		public void run() {
 			plugin.getCampMap().remove(player);
 		}
 	}
 }
