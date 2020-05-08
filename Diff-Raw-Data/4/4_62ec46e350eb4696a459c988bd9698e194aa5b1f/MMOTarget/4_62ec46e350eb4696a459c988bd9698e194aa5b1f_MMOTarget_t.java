 /*
  * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
  *
  * mmoMinecraft is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mmo.Target;
 
 import java.util.BitSet;
 import java.util.HashMap;
 import mmo.Core.gui.GenericLivingEntity;
 import mmo.Core.MMO;
 import mmo.Core.MMOPlugin;
 import org.bukkit.entity.*;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.util.config.Configuration;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.gui.Container;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class MMOTarget extends MMOPlugin {
 
 	private int updateTask;
 	protected static final HashMap<Player, LivingEntity> targets = new HashMap<Player, LivingEntity>();
 	protected static final HashMap<Player, GenericLivingEntity> bars = new HashMap<Player, GenericLivingEntity>();
 	protected static final HashMap<Player, Container> containers = new HashMap<Player, Container>();
 	/**
 	 * Config values
 	 */
	String config_ui_align = "TOP_CENTER";
	int config_ui_left = 0;
 	int config_ui_top = 3;
 	static int config_ui_maxwidth = 160;
 	static int config_max_range = 15;
 
 	@Override
 	public BitSet mmoSupport(BitSet support) {
 		support.set(MMO_PLAYER);
 		return support;
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 
 		mmoTargetPlayerListener tpl = new mmoTargetPlayerListener();
 		pm.registerEvent(Type.PLAYER_INTERACT_ENTITY, tpl, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_MOVE, tpl, Priority.Monitor, this);
 
 		mmoTargetEntityListener tel = new mmoTargetEntityListener();
 		pm.registerEvent(Type.ENTITY_DEATH, tel, Priority.Monitor, this);
 		pm.registerEvent(Type.ENTITY_DAMAGE, tel, Priority.Monitor, this);
 		pm.registerEvent(Type.ENTITY_EXPLODE, tel, Priority.Monitor, this);
 		pm.registerEvent(Type.PROJECTILE_HIT, tel, Priority.Monitor, this); // craftbukkit 1000
 
 		updateTask = server.getScheduler().scheduleSyncRepeatingTask(this,
 				  new Runnable() {
 
 					  @Override
 					  public void run() {
 						  MMOTarget.updateAll();
 					  }
 				  }, 20, 20);
 	}
 
 	@Override
 	public void loadConfiguration(Configuration cfg) {
 		config_ui_align = cfg.getString("ui.default.align", config_ui_align);
 		config_ui_left = cfg.getInt("ui.default.left", config_ui_left);
 		config_ui_top = cfg.getInt("ui.default.top", config_ui_top);
 		config_ui_maxwidth = cfg.getInt("ui.default.width", config_ui_maxwidth);
 		config_max_range = cfg.getInt("max_range", config_max_range);
 	}
 
 	@Override
 	public void onDisable() {
 		server.getScheduler().cancelTask(updateTask);
 		targets.clear();
 		super.onDisable();
 	}
 
 	@Override
 	public void onSpoutCraftPlayer(SpoutPlayer player) {
 		Container container = getContainer(player, config_ui_align, config_ui_left, config_ui_top).setAuto(false);
 		containers.put(player, container);
 	}
 
 //	@SuppressWarnings("unchecked")
 	public static void updateAll() {
 		for (Player player : ((HashMap<Player, LivingEntity>) targets.clone()).keySet()) {
 			update(player);
 		}
 	}
 
 	public static void update(Player player) {
 		LivingEntity target = targets.get(player);
 		if (target != null) {
 			int health = MMO.getHealth(target);
 			if (!target.isDead()
 					  && health > 0
 					  && player.getWorld() == target.getWorld()
 					  && player.getLocation().distance(target.getLocation()) <= config_max_range) {
 				if (MMOPlugin.hasSpout && SpoutManager.getPlayer(player).isSpoutCraftEnabled()) {
 					GenericLivingEntity bar = bars.get(player);
 					if (bar == null) {
 						bars.put(player, bar = new GenericLivingEntity());
 						Container container = containers.get(player);
 						container.addChild(bar);
 					}
 					bar.setEntity(target);
 					if (target instanceof Player && targets.containsKey((Player) target)) {
 						bar.setTargets(targets.get((Player) target));
 					} else if (target instanceof Creature && ((Creature) target).getTarget() != null && !((Creature) target).getTarget().isDead()) {
 						bar.setTargets(((Creature) target).getTarget());
 					} else {
 						bar.setTargets();
 					}
 				}
 			} else {
 				targets.remove(player);
 				if (MMOPlugin.hasSpout && SpoutManager.getPlayer(player).isSpoutCraftEnabled()) {
 					GenericLivingEntity bar = bars.remove(player);
 					if (bar != null) {
 						bar.getContainer().removeChild(bar);
 					}
 				}
 			}
 		}
 	}
 
 	private static class mmoTargetPlayerListener extends PlayerListener {
 
 		@Override
 		public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 			Player player = event.getPlayer();
 			Entity target = event.getRightClicked();
 			if (target instanceof LivingEntity) {
 				targets.put(player, (LivingEntity) target);
 				update(player);
 			}
 		}
 
 		@Override
 		public void onPlayerMove(PlayerMoveEvent event) {
 			Player player = event.getPlayer();
 			update(player);
 		}
 	}
 
 	private class mmoTargetEntityListener extends EntityListener {
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public void onEntityDeath(EntityDeathEvent event) {
 			Entity target = event.getEntity();
 			if (target instanceof LivingEntity && targets.containsValue((LivingEntity) target)) {
 				for (Player player : ((HashMap<Player, LivingEntity>) targets.clone()).keySet()) {
 					if (target.equals(targets.get(player))) {
 						update(player);
 					}
 				}
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public void onEntityExplode(EntityExplodeEvent event) {
 			if (event.isCancelled()) {
 				return;
 			}
 			Entity target = event.getEntity();
 			if (target instanceof LivingEntity && targets.containsValue((LivingEntity) target)) {
 				for (Player player : ((HashMap<Player, LivingEntity>) targets.clone()).keySet()) {
 					if (target.equals(targets.get(player))) {
 						update(player);
 					}
 				}
 			}
 		}
 
 		@Override
 		public void onEntityDamage(EntityDamageEvent event) {
 			if (event.isCancelled()) {
 				return;
 			}
 			LivingEntity attacker = null, defender = null;
 			if (event.getEntity() instanceof LivingEntity) {
 				defender = (LivingEntity) event.getEntity();
 			} else if (event.getEntity() instanceof Tameable) {
 				Tameable pet = (Tameable) event.getEntity();
 				if (pet.isTamed() && pet.getOwner() instanceof Player) {
 					defender = (Player) pet.getOwner();
 				}
 			}
 			if (event.getCause() == DamageCause.ENTITY_ATTACK) {
 				EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
 				if (e.getDamager() instanceof LivingEntity) {
 					attacker = (LivingEntity) e.getDamager();
 				} else if (e.getDamager() instanceof Tameable) {
 					Tameable pet = (Tameable) e.getDamager();
 					if (pet.isTamed() && pet.getOwner() instanceof Player) {
 						defender = (Player) pet.getOwner();
 					}
 				}
 			} else if (event.getCause() == DamageCause.PROJECTILE) {
 				EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
 				Projectile arrow = (Projectile) e.getDamager();
 				if (arrow.getShooter() instanceof LivingEntity) {
 					attacker = arrow.getShooter();
 				}
 			}
 			if (defender instanceof Player && attacker != null && !targets.containsKey((Player) defender)) {
 				targets.put((Player) defender, attacker);
 				update((Player) defender);
 			}
 			if (attacker instanceof Player && defender != null) {
 				targets.put((Player) attacker, defender);
 				update((Player) attacker);
 			}
 		}
 	}
 }
