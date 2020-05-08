 package net.slipcor.pvparena.modules.powerups;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.player.PlayerVelocityEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.classes.PACheck;
 import net.slipcor.pvparena.core.Debug;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.listeners.EntityListener;
 import net.slipcor.pvparena.loadables.ArenaModuleManager;
 import net.slipcor.pvparena.managers.ArenaManager;
 
 public class PowerupEffect {
 	protected boolean active = false;
 	protected int uses = -1;
 	protected int duration = -1;
 	protected PowerupType type = null;
 	protected String mobtype = null;
 	private double factor = 1.0;
 	private double chance = 1.0;
 	private int diff = 0;
 	private List<String> items = new ArrayList<String>();
 	private static Debug debug = new Debug(17);
 	private PotionEffect potEff = null;
 
 	/**
 	 * create a powerup effect class
 	 * 
 	 * @param eClass
 	 *            the effect class to create
 	 * @param puEffectVals
 	 *            the map of effect values to set/add
 	 */
 	public PowerupEffect(String eClass, HashMap<String, Object> puEffectVals,
 			PotionEffect effect) {
 		debug.i("adding effect " + eClass);
 		this.type = parseClass(eClass);
 		this.potEff = effect;
 
 		debug.i("effect class is " + type.toString());
 		for (Object evName : puEffectVals.keySet()) {
 			if (evName.equals("uses")) {
 				this.uses = (Integer) puEffectVals.get(evName);
 				debug.i("uses :" + String.valueOf(uses));
 			} else if (evName.equals("duration")) {
 				this.duration = (Integer) puEffectVals.get(evName);
 				debug.i("duration: " + String.valueOf(duration));
 			} else if (evName.equals("factor")) {
 				this.factor = (Double) puEffectVals.get(evName);
 				debug.i("factor: " + String.valueOf(factor));
 			} else if (evName.equals("chance")) {
 				this.chance = (Double) puEffectVals.get(evName);
 				debug.i("chance: " + String.valueOf(chance));
 			} else if (evName.equals("diff")) {
 				this.diff = (Integer) puEffectVals.get(evName);
 				debug.i("diff: " + String.valueOf(diff));
 			} else if (evName.equals("items")) {
 				this.items.add((String) puEffectVals.get(evName));
 				debug.i("items: " + items.toString());
 			} else if (evName.equals("type")) {
 				// mob type
 				this.mobtype = (String) puEffectVals.get(evName);
 				debug.i("type: " + type.name());
 			} else {
 				debug.i("undefined effect class value: " + evName);
 			}
 		}
 	}
 
 	/**
 	 * get the PowerupEffect class from name
 	 * 
 	 * @param s
 	 *            the class name
 	 * @return a powerup effect
 	 */
 	public static PowerupType parseClass(String s) {
 		for (PowerupType c : PowerupType.values()) {
 			if (c.name().equalsIgnoreCase(s))
 				return c;
 			if (s.toUpperCase().startsWith("POTION.")) {
 				return PowerupType.POTEFF;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * initiate PowerupEffect
 	 * 
 	 * @param player
 	 *            the player to commit the effect on
 	 */
 	public void init(Player player) {
 		if (uses == 0)
 			return;
 		else if (uses > 0) {
 			active = true;
 			uses--;
 		} else {
 			active = true;
 		}
 
 		debug.i("initiating - " + type.name(), player);
 
 		if (duration == 0) {
 			active = false;
 		}
 		
 		if (type.isActivatedOnPickup()) {
 			commit(player);
 		}
 		
 		if (potEff != null) {
 			player.addPotionEffect(potEff);
 		}
 	}
 
 	/**
 	 * remove PowerupEffect Potion Effect from player
 	 * 
 	 * @param player
 	 *            the player to clear
 	 */
 	public void removeEffect(Player player) {
 		if (potEff != null) {
 			player.addPotionEffect(new PotionEffect(potEff.getType(), 0, 0));
 		}
 	}
 
 	/**
 	 * commit PowerupEffect in combat
 	 * 
 	 * @param attacker
 	 *            the attacking player to access
 	 * @param defender
 	 *            the defending player to access
 	 * @param event
 	 *            the triggering event
 	 * @param isAttacker 
 	 */
 	public void commit(Player attacker, Player defender,
 			EntityDamageByEntityEvent event, boolean isAttacker) {
 		debug.i("committing entitydamagebyentityevent: " + this.type.name(), attacker);
 		if (!isAttacker && this.type == PowerupType.DMG_RECEIVE) {
 			Random r = new Random();
 			Float v = r.nextFloat();
 			debug.i("random r = "+ v, defender);
 			if (v <= chance) {
 				event.setDamage((int) Math.round(event.getDamage() * factor));
 			} // else: chance fail :D
 		} else if (isAttacker && this.type == PowerupType.DMG_CAUSE) {
 			Random r = new Random();
 			Float v = r.nextFloat();
 			debug.i("random r = "+ v, attacker);
 			if (v <= chance) {
 				event.setDamage((int) Math.round(event.getDamage() * factor));
 			} // else: chance fail :D
 		} else if (!isAttacker && this.type == PowerupType.DMG_REFLECT) {
 			if (attacker == null) {
 				return;
 			}
 			Random r = new Random();
 			Float v = r.nextFloat();
 			debug.i("random r = "+ v, attacker);
 			debug.i("random r = "+ v, defender);
 			if (v <= chance) {
 				EntityDamageByEntityEvent reflectEvent = new EntityDamageByEntityEvent(
 						defender, attacker, event.getCause(),
 						Math.round(event.getDamage() * factor));
 				(new EntityListener()).onEntityDamageByEntity(reflectEvent);
 			} // else: chance fail :D
 		} else if (!isAttacker && this.type == PowerupType.IGNITE) {
 			Random r = new Random();
 			Float v = r.nextFloat();
 			debug.i("random r = "+ v, defender);
 			if (v <= chance) {
 				defender.setFireTicks(20);
 			} // else: chance fail :D
 		} else {
 			debug.i("unexpected fight powerup effect: " + this.type.name());
 		}
 	}
 
 	/**
 	 * commit PowerupEffect on player
 	 * 
 	 * @param player
 	 *            the player to commit the effect on
 	 * @return true if the commit succeeded, false otherwise
 	 */
 	public boolean commit(Player player) {
 
 		debug.i("committing " + this.type.name(), player);
 		Random r = new Random();
 		if (r.nextFloat() <= chance) {
 			if (this.type == PowerupType.HEALTH) {
 				if (diff > 0) {
 					double value = player.getHealth() + diff;
 					
 					if (player.getMaxHealth() > value) {
 						value = player.getMaxHealth();
 					}
 					player.setHealth(value);
 				} else {
 					player.setHealth((int) Math.round(player.getHealth()
 							* factor));
 				}
 				return true;
 			} else if (this.type == PowerupType.LIVES) {
 				ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 				int lives = PACheck.handleGetLives(ap.getArena(), ap);
 				if (lives + diff > 0) {
					ap.get().damage(1000d);
 				} else {
 					ArenaTeam team = ap.getArenaTeam();
 					Arena arena = ap.getArena();
 					ArenaModuleManager.announce(
 							arena,
 							Language.parse(MSG.FIGHT_KILLED_BY, player.getName(),
 									arena.parseDeathCause(player,
 											DamageCause.MAGIC, player)), "LOSER");
 					
 					if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
 						arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY,
 								team.colorizePlayer(player) + ChatColor.YELLOW,
 								arena.parseDeathCause(player,
 										DamageCause.MAGIC, player)));
 					}
 					// needed so player does not get found when dead
 					arena.removePlayer(player, arena.getArenaConfig().getString(CFG.TP_LOSE), true, false);
 					ap.getArenaTeam().remove(ap);
 
 					ArenaManager.checkAndCommit(arena, false);
 				}
 
 				return true;
 			} else if (this.type == PowerupType.PORTAL) {
 				potEff = new PotionEffect(PotionEffectType.CONFUSION, duration*20, 2);
 				return true;
 			} else if (this.type == PowerupType.REPAIR) {
 				for (String i : items) {
 					i = i.toUpperCase();
 					ItemStack is = null;
 					if (i.contains("HELM")) {
 						is = player.getInventory().getHelmet();
 					} else if ((i.contains("CHEST")) || (i.contains("PLATE"))) {
 						is = player.getInventory().getChestplate();
 					} else if (i.contains("LEGGINS")) {
 						is = player.getInventory().getLeggings();
 					} else if (i.contains("BOOTS")) {
 						is = player.getInventory().getBoots();
 					} else if (i.contains("SWORD")) {
 						is = player.getItemInHand();
 					}
 					if (is == null)
 						continue;
 
 					if (diff > 0) {
 						if (is.getDurability() + diff > Byte.MAX_VALUE)
 							is.setDurability(Byte.MAX_VALUE);
 						else
 							is.setDurability((short) (is.getDurability() + diff));
 					}
 				}
 				return true;
 			} else if (this.type == PowerupType.SPAWN_MOB) {
 				return true;
 			} else if (this.type == PowerupType.SPRINT) {
 				player.setSprinting(true);
 				potEff = new PotionEffect(PotionEffectType.SPEED, duration*20, 2);
 				return true;
 			}
 		}
 		debug.i("unexpected " + this.type.name());
 		return false;
 	}
 
 	/**
 	 * commit PowerupEffect on health gain
 	 * 
 	 * @param event
 	 *            the triggering event
 	 */
 	public void commit(EntityRegainHealthEvent event) {
 		debug.i("committing entityregainhealthevent " + this.type.name(), ((Player) event.getEntity()));
 		if (this.type == PowerupType.HEAL) {
 			Random r = new Random();
 			if (r.nextFloat() <= chance) {
 				event.setAmount((int) Math.round(event.getAmount() * factor));
 				((Player) event.getEntity()).setSaturation(20);
 				((Player) event.getEntity()).setFoodLevel(20);
 			} // else: chance fail :D
 		} else {
 			debug.i("unexpected fight heal effect: " + this.type.name());
 		}
 	}
 
 	/**
 	 * commit PowerupEffect on velocity event
 	 * 
 	 * @param event
 	 *            the triggering event
 	 */
 	public void commit(PlayerVelocityEvent event) {
 		debug.i("committing velocityevent " + this.type.name(), event.getPlayer());
 		if (this.type == PowerupType.HEAL) {
 			Random r = new Random();
 			if (r.nextFloat() <= chance) {
 				event.setVelocity(event.getVelocity().multiply(factor));
 			} // else: chance fail :D
 		} else {
 			debug.i("unexpected jump effect: " + this.type.name());
 		}
 	}
 
 	/**
 	 * Get the PotionEffect of a PotionEffect class string
 	 * 
 	 * @param eClass
 	 *            the class string to parse
 	 * @return the PotionEffect or null
 	 */
 	public static PotionEffect parsePotionEffect(String eClass) {
 		eClass = eClass.replace("POTION.", "");
 
 		// POTION.BLA:1 <--- duration
 		// POTION.BLA:1:1 <--- amplifyer
 
 		int duration = 1;
 		int amplifyer = 1;
 
 		if (eClass.contains(":")) {
 			String[] s = eClass.split(":");
 
 			eClass = s[0];
 			try {
 				duration = Integer.parseInt(s[1]);
 			} catch (Exception e) {
 				Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.MODULE_POWERUPS_INVALIDPUEFF,
 						eClass));
 			}
 
 			if (s.length > 2) {
 
 				try {
 					amplifyer = Integer.parseInt(s[2]);
 				} catch (Exception e) {
 					Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.MODULE_POWERUPS_INVALIDPUEFF,
 							eClass));
 				}
 			}
 		}
 		
 		for (PotionEffectType pet : PotionEffectType.values()) {
 			if (pet == null) {
 				continue;
 			}
 			debug.i("parsing PET " + pet.toString());
 			if (pet.getName() == null) {
 				continue;
 			}
 			debug.i("parsing PET " + pet.toString());
 			if (pet.getName().equals(eClass)) {
 				return new PotionEffect(pet, duration, amplifyer);
 			}
 		}
 		return null;
 	}
 }
