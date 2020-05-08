 package net.slipcor.pvparena.modules.powerups;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerVelocityEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.classes.PABlock;
 import net.slipcor.pvparena.classes.PABlockLocation;
 import net.slipcor.pvparena.classes.PALocation;
 import net.slipcor.pvparena.classes.PASpawn;
 import net.slipcor.pvparena.commands.AbstractArenaCommand;
 import net.slipcor.pvparena.core.Debug;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.managers.SpawnManager;
 import net.slipcor.pvparena.loadables.ArenaModule;
 import net.slipcor.pvparena.loadables.ArenaRegion;
 import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
 
 public class PowerupManager extends ArenaModule implements Listener  {
 
 	
 	protected Powerups usesPowerups = null;
 
 	private int powerupDiff = 0;
 	private int powerupDiffI = 0;
 
 	protected int SPAWN_ID = -1;
 	
 	private boolean setup = false;
 
 	public PowerupManager() {
 		super("PowerUps");
 		debug = new Debug(402);
 	}
 	
 	@Override
 	public String version() {
		return "v1.1.0.355";
 	}
 
 	/**
 	 * calculate a powerup and commit it
 	 */
 	protected void calcPowerupSpawn() {
 		debug.i("powerups?");
 		if (usesPowerups == null)
 			return;
 
 		if (usesPowerups.puTotal.size() <= 0)
 			return;
 
 		debug.i("totals are filled");
 		Random r = new Random();
 		int i = r.nextInt(usesPowerups.puTotal.size());
 
 		for (Powerup p : usesPowerups.puTotal) {
 			if (--i > 0)
 				continue;
 			commitPowerupItemSpawn(p.item);
 			arena.broadcast(Language.parse(MSG.MODULE_POWERUPS_SERVER, p.name));
 			return;
 		}
 
 	}
 	
 	@Override
 	public boolean checkCommand(String s) {
 		return s.equals("!pu") || s.startsWith("powerup");
 	}
 	
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		// !pu time 6
 		// !pu death 4
 		
 		if (!PVPArena.hasAdminPerms(sender)
 				&& !(PVPArena.hasCreatePerms(sender, arena))) {
 			arena.msg(
 					sender,
 					Language.parse(MSG.ERROR_NOPERM,
 							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
 			return;
 		}
 
 		if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 2,3 })) {
 			return;
 		}
 		
 		if (args[0].equals("!pu") || args[0].startsWith("powerup")) {
 			if (args.length == 2) {
 				if (args[1].equals("off")) {
 					arena.getArenaConfig().set(CFG.MODULES_POWERUPS_USAGE, args[1]);
 					arena.getArenaConfig().save();
 					arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_POWERUPS_USAGE.getNode(), args[1]));
 					return;
 				} else if (args[1].equals("dropspawn")) {
 					boolean b = arena.getArenaConfig().getBoolean(CFG.MODULES_POWERUPS_DROPSPAWN);
 					arena.getArenaConfig().set(CFG.MODULES_POWERUPS_DROPSPAWN, !b);
 					arena.getArenaConfig().save();
 					arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_POWERUPS_DROPSPAWN.getNode(), String.valueOf(!b)));
 				}
 				arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "off | dropspawn"));
 				return;
 			}
 			int i = 0;
 			try {
 				i = Integer.parseInt(args[2]);
 			} catch (Exception e) {
 				arena.msg(sender,
 						Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
 				return;
 			}
 			if (args[1].equals("time") || args[1].equals("death")) {
 				arena.getArenaConfig().set(CFG.MODULES_POWERUPS_USAGE, args[1]+":"+i);
 				arena.getArenaConfig().save();
 				arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_POWERUPS_USAGE.getNode(), args[1]+":"+i));
 				return;
 			}
 			
 			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "time | death"));
 			return;
 		}
 	}
 
 	@Override
 	public boolean commitEnd(ArenaTeam arg1) {
 		if (usesPowerups != null) {
 			if (arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE).startsWith("death")) {
 				debug.i("calculating powerup trigger death");
 				powerupDiffI = ++powerupDiffI % powerupDiff;
 				if (powerupDiffI == 0) {
 					calcPowerupSpawn();
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * commit the powerup item spawn
 	 * 
 	 * @param item
 	 *            the material to spawn
 	 */
 	protected void commitPowerupItemSpawn(Material item) {
 		debug.i("dropping item?");
 		if (arena.getArenaConfig().getBoolean(CFG.MODULES_POWERUPS_DROPSPAWN)) {
 			dropItemOnSpawn(item);
 		} else {
 			Set<ArenaRegion> ars = arena.getRegionsByType(RegionType.BATTLE);
 			for (ArenaRegion ar : ars) {
 				
 				PABlockLocation min = ar.getShape().getMinimumLocation();
 				PABlockLocation max = ar.getShape().getMaximumLocation();
 				
 				Random r = new Random();
 
 				int x = r.nextInt(max.getX() - min.getX());
 				int z = r.nextInt(max.getZ() - min.getZ());
 				
 				World w = Bukkit.getWorld(min.getWorldName());
 				
 				mark(w.dropItem(w.getHighestBlockAt(min.getX() + x, min.getZ() + z).getRelative(BlockFace.UP).getLocation(), new ItemStack(item,1)));
 				
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void configParse(YamlConfiguration config) {
 		if (!setup) {
 			Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
 			setup = true;
 		}
 		HashMap<String, Object> powerups = new HashMap<String, Object>();
 		if (config.getConfigurationSection("powerups") != null) {
 			HashMap<String, Object> map = (HashMap<String, Object>) config
 					.getConfigurationSection("powerups").getValues(false);
 			HashMap<String, Object> map2 = new HashMap<String, Object>();
 			HashMap<String, Object> map3 = new HashMap<String, Object>();
 			debug.i("parsing powerups");
 			for (String key : map.keySet()) {
 				// key e.g. "OneUp"
 				map2 = (HashMap<String, Object>) config
 						.getConfigurationSection("powerups." + key).getValues(
 								false);
 				HashMap<String, Object> temp_map = new HashMap<String, Object>();
 				for (String kkey : map2.keySet()) {
 					// kkey e.g. "dmg_receive"
 					if (kkey.equals("item")) {
 						temp_map.put(kkey, String.valueOf(map2.get(kkey)));
 						debug.i(key + " => " + kkey + " => "
 								+ String.valueOf(map2.get(kkey)));
 					} else {
 						debug.i(key + " => " + kkey + " => "
 								+ parseList(map3.values()));
 						map3 = (HashMap<String, Object>) config
 								.getConfigurationSection(
 										"powerups." + key + "." + kkey)
 								.getValues(false);
 						temp_map.put(kkey, map3);
 					}
 				}
 				powerups.put(key, temp_map);
 			}
 		}
 
 		if (powerups.size() < 1) {
 			return;
 		}
 
 		String pu = arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE, "off");
 		
 		String[] ss = pu.split(":");
 		if (pu.startsWith("death")) {
 			powerupDiff = Integer.parseInt(ss[1]);
 			usesPowerups = new Powerups(powerups);
 		} else if (pu.startsWith("time")) {
 			powerupDiff = Integer.parseInt(ss[1]);
 			usesPowerups = new Powerups(powerups);
 		} else {
 			PVPArena.instance.getLogger().warning("error activating powerup module");
 		}
 
 		config.options().copyDefaults(true);
 	}
 	
 	@Override
 	public void displayInfo(CommandSender player) {
 		player.sendMessage("usage: "
 				+ StringParser.colorVar(usesPowerups != null)
 				+ "("
 				+ StringParser.colorVar(arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE))
 				+ ")");
 	}
 
 	/**
 	 * drop an item at a powerup spawn point
 	 * 
 	 * @param item
 	 *            the item to drop
 	 */
 	protected void dropItemOnSpawn(Material item) {
 		debug.i("calculating item spawn location");
 		Set<PALocation> locs = SpawnManager.getSpawnsContaining(arena, "powerup");
 		if (locs.size() < 1) {
 			PVPArena.instance.getLogger().warning("No valid powerup spawns found!");
 			return;
 		}
 		int pos = (new Random()).nextInt(locs.size());
 		for (PALocation loc : locs) {
 			if (--pos > 0) {
 				continue;
 			}
 			Location aim = loc.toLocation().add(0, 1, 0);
 			debug.i("dropping item on spawn: " + aim.toString());
 			mark(Bukkit.getWorld(arena.getWorld()).dropItem(aim, new ItemStack(item, 1)));
 			break;
 		}
 
 	}
 
 	@Override
 	public boolean hasSpawn(String s) {
 		return s.toLowerCase().startsWith("powerup");
 	}
 	
 	private final String POWERUPSTRING = ChatColor.RED+"Power\nUp";
 	
 	private void mark(Item drop) {
 		ItemMeta meta = drop.getItemStack().getItemMeta();
 		
 		meta.setDisplayName(POWERUPSTRING);
 		drop.getItemStack().setItemMeta(meta);
 	}
 	
 	private boolean isPowerup(final ItemStack item) {
 		if (!item.hasItemMeta()) {
 			return false;
 		}
 		return item.getItemMeta().getDisplayName().equals(POWERUPSTRING);
 	}
 
 	@Override
 	public void onEntityDamageByEntity(Player attacker,
 			Player defender, EntityDamageByEntityEvent event) {
 		if (usesPowerups != null) {
 			debug.i("committing powerup triggers", attacker);
 			debug.i("committing powerup triggers", defender);
 			Powerup p = usesPowerups.puActive.get(attacker);
 			if ((p != null) && (p.canBeTriggered())) {
 				
 				p.commit(attacker, defender, event, true);
 			}
 			p = usesPowerups.puActive.get(defender);
 			if ((p != null) && (p.canBeTriggered())) {
 				p.commit(attacker, defender, event, false);
 			}
 		}
 
 	}
 
 	@Override
 	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
 		if (usesPowerups != null) {
 			debug.i("regaining health");
 			Powerup p = usesPowerups.puActive.get((Player) event.getEntity());
 			if (p != null) {
 				if (p.canBeTriggered()) {
 					if (p.isEffectActive(PowerupType.HEAL)) {
 						event.setCancelled(true);
 						p.commit(event);
 					}
 				}
 			}
 
 		}
 	}
 
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		Player player = event.getPlayer();
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 		if (!arena.equals(ap.getArena())) {
 			return;
 		}
 		if (usesPowerups != null && isPowerup(event.getItem().getItemStack())) {
 			debug.i("onPlayerPickupItem: fighting player", player);
 			debug.i("item: " + event.getItem().getItemStack().getType(), player);
 			Iterator<Powerup> pi = usesPowerups.puTotal.iterator();
 			while (pi.hasNext()) {
 				Powerup p = pi.next();
 				debug.i("is it " + p.item + "?", player);
 				if (event.getItem().getItemStack().getType().equals(p.item)) {
 					debug.i("yes!", player);
 					Powerup newP = new Powerup(p);
 					if (usesPowerups.puActive.containsKey(player)) {
 						usesPowerups.puActive.get(player).deactivate(player);
 						usesPowerups.puActive.remove(player);
 					}
 					usesPowerups.puActive.put(player, newP);
 					arena.broadcast(Language.parse(MSG.MODULE_POWERUPS_PLAYER,
 							player.getName(), newP.name));
 					event.setCancelled(true);
 					event.getItem().remove();
 					if (newP.canBeTriggered())
 						newP.activate(player); // activate for the first time
 
 					return;
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void onPlayerVelocity(PlayerVelocityEvent event) {
 		debug.i("inPlayerVelocity: fighting player", event.getPlayer());
 		if (usesPowerups != null) {
 			Powerup p = usesPowerups.puActive.get(event.getPlayer());
 			if (p != null) {
 				if (p.canBeTriggered()) {
 					if (p.isEffectActive(PowerupType.JUMP)) {
 						p.commit(event);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * turn a collection of objects into a comma separated string
 	 * 
 	 * @param values
 	 *            the collection
 	 * @return the comma separated string
 	 */
 	protected String parseList(Collection<Object> values) {
 		String s = "";
 		for (Object o : values) {
 			if (!s.equals("")) {
 				s += ",";
 			}
 			try {
 				s += String.valueOf(o);
 				debug.i("a");
 			} catch (Exception e) {
 				debug.i("b");
 				s += o.toString();
 			}
 		}
 		return s;
 	}
 	
 	@EventHandler
 	public void parseMove(PlayerMoveEvent event) {
 
 		// debug.i("onPlayerMove: fighting player!");
 		if (usesPowerups != null) {
 			//debug.i("parsing move");
 			Powerup p = usesPowerups.puActive.get(event.getPlayer());
 			if (p != null) {
 				if (p.canBeTriggered()) {
 					if (p.isEffectActive(PowerupType.FREEZE)) {
 						debug.i("freeze in effect, cancelling!", event.getPlayer());
 						event.setCancelled(true);
 					}
 					if (p.isEffectActive(PowerupType.SPRINT)) {
 						debug.i("sprint in effect, sprinting!", event.getPlayer());
 						event.getPlayer().setSprinting(true);
 					}
 					if (p.isEffectActive(PowerupType.SLIP)) {
 						//
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * powerup tick, tick each arena that uses powerups
 	 */
 	protected void powerupTick() {
 		if (usesPowerups != null)
 			usesPowerups.tick();
 	}
 
 	@Override
 	public void reset(boolean force) {
 		if (SPAWN_ID > -1)
 			Bukkit.getScheduler().cancelTask(SPAWN_ID);
 		SPAWN_ID = -1;
 		if (usesPowerups != null) {
 			usesPowerups.puActive.clear();
 		}
 	}
 
 	@Override
 	public void parseStart() {
 		if (usesPowerups != null) {
 			String pu = arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE);
 			String[] ss = pu.split(":");
 			if (pu.startsWith("time")) {
 				// arena.powerupTrigger = "time";
 				powerupDiff = Integer.parseInt(ss[1]);
 			} else {
 				return;
 			}
 
 			debug.i("using powerups : "
 					+ arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE) + " : "
 					+ powerupDiff);
 			if (powerupDiff > 0) {
 				debug.i("powerup time trigger!");
 				powerupDiff = powerupDiff * 20; // calculate ticks to seconds
 				// initiate autosave timer
 				SPAWN_ID = Bukkit
 						.getServer()
 						.getScheduler()
 						.scheduleSyncRepeatingTask(PVPArena.instance,
 								new PowerupRunnable(this), powerupDiff,
 								powerupDiff);
 			}
 		}
 	}
 }
