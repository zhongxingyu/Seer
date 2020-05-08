 package com.github.thebiologist13;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.thebiologist13.listeners.BreakEvent;
 import com.github.thebiologist13.listeners.DamageController;
 import com.github.thebiologist13.listeners.ExpBottleHitEvent;
 import com.github.thebiologist13.listeners.InteractEvent;
 import com.github.thebiologist13.listeners.MobCombustEvent;
 import com.github.thebiologist13.listeners.MobDamageEvent;
 import com.github.thebiologist13.listeners.MobDeathEvent;
 import com.github.thebiologist13.listeners.MobExplodeEvent;
 import com.github.thebiologist13.listeners.MobRegenEvent;
 import com.github.thebiologist13.listeners.PlayerLogoutEvent;
 import com.github.thebiologist13.listeners.PlayerTargetEvent;
 import com.github.thebiologist13.listeners.PotionHitEvent;
 import com.github.thebiologist13.listeners.ProjectileFireEvent;
 import com.github.thebiologist13.listeners.ReloadEvent;
 import com.github.thebiologist13.listeners.SpawnerPowerEvent;
 import com.github.thebiologist13.serialization.SPotionEffect;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 /**
  * CustomSpawners is a plugin for making customizable spawners for Bukkit servers.
  * 
  * Licensed under GNU-GPLv3
  * 
  * @author thebiologist13
  * @version 0.1
  */
 public class CustomSpawners extends JavaPlugin {
 
 	//Selected entity by console.
 	public static int consoleEntity = -1;
 
 	//Selected spawner by console.
 	public static int consoleSpawner = -1;
 
 	//Debug
 	public static boolean debug = false;
 
 	//Default Entity to use.
 	public static SpawnableEntity defaultEntity = null;
 
 	//All of the entity types on the server.
 	public static ConcurrentHashMap<Integer, SpawnableEntity> entities = new ConcurrentHashMap<Integer, SpawnableEntity>();
 
 	//Selected entities for players.
 	public static ConcurrentHashMap<Player, Integer> entitySelection = new ConcurrentHashMap<Player, Integer>();
 
 	//Player selection area Point 1.
 	public static ConcurrentHashMap<Player, Location> selectionPointOne = new ConcurrentHashMap<Player, Location>();
 
 	//Player selection area Point 2.
 	public static ConcurrentHashMap<Player, Location> selectionPointTwo = new ConcurrentHashMap<Player, Location>();
 
 	//Players not using selections.
 	public static ConcurrentHashMap<Player, Boolean> selectMode = new ConcurrentHashMap<Player, Boolean>();
 
 	//All the spawners in the server.
 	public static ConcurrentHashMap<Integer, Spawner> spawners = new ConcurrentHashMap<Integer, Spawner>();
 	
 	//Selected spawners for players.
 	public static ConcurrentHashMap<Player, Integer> spawnerSelection = new ConcurrentHashMap<Player, Integer>();
 	
 	//Transparent Blocks to go through when getting the target location for a spawner.
 	public static HashSet<Byte> transparent = new HashSet<Byte>();
 
 	//Autosave Task ID
 	public int autosaveId;
 	
 	//Logger
 	public Logger log = Logger.getLogger("Minecraft");
 
 	//YAML variable
 	private FileConfiguration config;
 
 	//YAML file variable
 	private File configFile;
 
 	//FileManager
 	private FileManager fileManager = null;
 
 	//LogLevel
 	private byte logLevel;
 
 	//Save interval
 	private long saveInterval;
 	
 	//WorldGuard
 	private WorldGuardPlugin worldGuard = null;
 
 	//Gets an entity
 	public static SpawnableEntity getEntity(int ref) {
 		return getEntity(String.valueOf(ref));
 	}
 
 	//Gets an entity
 	public static SpawnableEntity getEntity(String ref) {
 		
 		if(ref.isEmpty())
 			return null;
 		
 		ref = ref.toLowerCase();
 		
 		if(isInteger(ref)) {
 			int id = Integer.parseInt(ref);
 
 			if(id == -2)
 				return defaultEntity;
 
 			Iterator<Integer> entityItr = entities.keySet().iterator();
 			while(entityItr.hasNext()) {
 				int currentId = entityItr.next();
 
 				if(currentId == id) {
 					return entities.get(id);
 				}
 			}
 
 		} else {
 
 			if(ref.equals("default"))
 				return defaultEntity;
 
 			Iterator<Integer> entityItr = entities.keySet().iterator();
 			while(entityItr.hasNext()) {
 				Integer id = entityItr.next();
 				SpawnableEntity s = entities.get(id);
 				String name = s.getName();
 
 				if(name == null) {
 					return null;
 				}
 
 				if(name.equalsIgnoreCase(ref)) {
 					return s;
 				}
 			}
 		}
 
 		return null;
 	}
 	
 	//Gets the next available ID number in a list
 	public static int getNextID(List<Integer> set) {
 		int returnID = 0;
 		boolean taken = true;
 
 		while(taken) {
 
 			if(set.size() == 0) {
 				return 0;
 			}
 
 			for(Integer i : set) {
 				if(returnID == i) {
 					taken = true;
 					break;
 				} else {
 					taken = false;
 				}
 			}
 
 			if(taken) {
 				returnID++;
 			}
 		}
 
 		return returnID;
 	}
 
 	//Gets a spawner
 	public static Spawner getSpawner(int ref) {
 		return getSpawner(String.valueOf(ref));
 	}
 
 	//Gets a spawner
 	public static Spawner getSpawner(String ref) {
 		
 		if(ref.isEmpty())
 			return null;
 		
 		ref = ref.toLowerCase();
 		
 		if(isInteger(ref)) {
 			int id = Integer.parseInt(ref);
 			Iterator<Integer> spawnerItr = spawners.keySet().iterator();
 
 			while(spawnerItr.hasNext()) {
 				int currentId = spawnerItr.next();
 
 				if(currentId == id) {
 					return spawners.get(id);
 				}
 			}
 		} else {
 			Iterator<Integer> spawnerItr = spawners.keySet().iterator();
 
 			while(spawnerItr.hasNext()) {
 				Integer id = spawnerItr.next();
 				Spawner s = spawners.get(id);
 				String name = s.getName();
 
 				if(name == null) {
 					return null;
 				}
 
 				if(name.equalsIgnoreCase(ref)) {
 					return s;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	//Convenience method for accurately testing if a string can be parsed to an double.
 	public static boolean isDouble(String what) {
 		try {
 			Double.parseDouble(what);
 			return true;
 		} catch(NumberFormatException e) {
 			return false;
 		}
 	}
 	
 	//Convenience method for accurately testing if a string can be parsed to an double.
 	public static boolean isFloat(String what) {
 		try {
 			Float.parseFloat(what);
 			return true;
 		} catch(NumberFormatException e) {
 			return false;
 		}
 	}
 
 	//Convenience method for accurately testing if a string can be parsed to an integer.
 	public static boolean isInteger(String what) {
 		try {
 			Integer.parseInt(what);
 			return true;
 		} catch(NumberFormatException e) {
 			return false;
 		}
 	}
 
 	public boolean allowedEntity(EntityType type) {
 		String name = type.toString();
 		
 		List<?> notAllowed = config.getList("mobs.blacklist");
 		
 		if(notAllowed.contains(name)) {
 			return false;
 		}
 		
 		return true;
 	}
 
 	public Spawner cloneWithNewId(Spawner s) {
 		Spawner s1 = new Spawner(s.getMainEntity(), s.getLoc(), getNextSpawnerId());
 		s1.setData(s.getData());
 		s1.setTypeData(s.getTypeData());
 		return s1;
 	}
 
 	//Converts ticks to MM:SS
 	public String convertTicksToTime(int ticks) {
 		int minutes = 0;
 		float seconds = 0;
 		float floatTick = (float) ticks;
 
 		if(floatTick >= 1200) {
 
 			if((floatTick % 1200) == 0) {
 				minutes = Math.round(floatTick / 1200);
 			} else {
 				seconds = (floatTick % 1200) / 20;
 				minutes = Math.round((floatTick - (floatTick % 1200)) / 1200);
 			}
 
 		} else {
 			seconds = floatTick / 20;
 		}
 
 		String strSec = "";
 		
 		if(seconds < 10) {
 			strSec = "0" + String.valueOf(seconds);
 		} else {
 			strSec = String.valueOf(seconds);
 		}
 		
 		return String.valueOf(minutes) + ":" + strSec;
 	}
 
 	public void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			out.close();
 			in.close();
 		} catch (Exception e) {
 			log.severe("Could not copy config from jar!");
 			e.printStackTrace();
 		}
 	}
 
 	public FileConfiguration getCustomConfig() {
 		if (config == null) {
 			reloadCustomConfig();
 		}
 		return config;
 	}
 	
 	public String getDamageCause(String in) {
 		
 		in.toLowerCase();
 		
 		String type = "";
 		
 		if(in.equals("blockexplosion")) {
 			type = "BLOCK_EXPLOSION";
 		} else if(in.equals("entityexplosion") || in.equals("creeper")) {
 			type = "ENTITY_EXPLOSION";
 		} else if(in.equals("firetick") || in.equals("burning")) {
 			type = "FIRE_TICK";
 		} else if(in.equals("attack") || in.equals("entityattack")) {
 			type = "ENTITY_ATTACK";
 		} else if(in.equals("item") || in.equals("itemdamage")) {
 			type = "ITEM";
 		} else if(in.equals("spawnerfire") || in.equals("spawnerfireticks")) {
 			type = "SPAWNER_FIRE_TICKS";
 		} else {
 			for(DamageCause c : DamageCause.values()) {
 				if(c.toString().equalsIgnoreCase(in)) {
 					type = in;
 					break;
 				}
 			}
 		}
 		
 		return type;
 	}
 
 	public SpawnableEntity getEntityFromSpawner(Entity entity) {
 
 		if(entity == null) {
 			return null;
 		}
 
 		int entityId = entity.getEntityId();
 		Iterator<Spawner> spawnerItr = spawners.values().iterator();
 
 		while(spawnerItr.hasNext()) {
 			Spawner s = spawnerItr.next();
 			Iterator<Integer> mobItr = s.getMobs().keySet().iterator();
 
 			while(mobItr.hasNext()) {
 				Entity currentMob = getEntityFromWorld(mobItr.next(), s.getLoc().getWorld());
 				
 				if(currentMob == null) {
 					continue;
 				}
 
 				if(currentMob.getEntityId() == entityId) {
 					return s.getMobs().get(currentMob);
 				}
 
 			}
 
 		}
 
 		return null;
 
 	}
 	
 	public SpawnableEntity getEntityFromSpawner(int id) {
 		
 		Iterator<Spawner> spawnerItr = spawners.values().iterator();
 
 		while(spawnerItr.hasNext()) {
 			Spawner s = spawnerItr.next();
 			Iterator<Integer> mobItr = s.getMobs().keySet().iterator();
 
 			while(mobItr.hasNext()) {
 				int currentMob = mobItr.next();
 
 				if(currentMob == id) {
 					return s.getMobs().get(currentMob);
 				}
 
 			}
 
 		}
 
 		return null;
 
 	}
 
 	public Entity getEntityFromWorld(int id, World w) {
 		
 		Iterator<Entity> entitiesInWorld = w.getEntities().iterator();
 		while(entitiesInWorld.hasNext()) {
 			Entity e = entitiesInWorld.next();
 
 			if(e.getEntityId() == id) {
 				return e;
 			}
 
 		}
 
 		return null;
 	}
 
 	public FileManager getFileManager() {
 		return fileManager;
 	}
 
 	//Gets a string to represent the name of the entity (String version of ID or name)
 	public String getFriendlyName(SpawnableEntity e) {
 		if(e.getName().isEmpty()) {
 			return String.valueOf(e.getId());
 		} else {
 			return e.getName();
 		}
 	}
 
 	//Gets a string to represent the name of the spawner (String version of ID or name)
 	public String getFriendlyName(Spawner s) {
 		if(s.getName().isEmpty()) {
 			return String.valueOf(s.getId());
 		} else {
 			return s.getName();
 		}
 	}
 
 	//Gets a potion from an alias
 	public PotionEffectType getInputEffect(String effect) {
 		
 		PotionEffectType type = null;
 		effect.toLowerCase();
 		
 		if(effect.equals("damageresistance") || effect.equals("damage_resistance")) {
 			type = PotionEffectType.DAMAGE_RESISTANCE;
 		} else if(effect.equals("instanthealth") || effect.equals("instant_health")) {
 			type = PotionEffectType.HEAL;
 		} else if(effect.equals("instant_damage") || effect.equals("instantdamage")) {
 			type = PotionEffectType.HARM;
 		} else if(effect.equals("haste") || effect.equals("mining_haste") || effect.equals("mininghaste")) {
 			type = PotionEffectType.FAST_DIGGING;
 		} else if(effect.equals("fireresistance")) {
 			type = PotionEffectType.FIRE_RESISTANCE;
 		} else if(effect.equals("strength")) {
 			type = PotionEffectType.INCREASE_DAMAGE;
 		} else if(effect.equals("fatigue") || effect.equals("miningfatigue") || effect.equals("mining_fatigue")) {
 			type = PotionEffectType.SLOW_DIGGING;
 		} else if(effect.equals("slowness")) {
 			type = PotionEffectType.SLOW;
 		} else if(effect.equals("nightvision")) {
 			type = PotionEffectType.NIGHT_VISION;
 		} else if(effect.equals("waterbreathing")) {
 			type = PotionEffectType.WATER_BREATHING;
 		} else {
 			type = PotionEffectType.getByName(effect);
 		}
 		
 		return type;
 		
 	}
 	
 	public ItemStack getItem(String item, int count) {
 		ItemStack stack = getItemStack(item);
 		
 		if(stack == null) {
 			return null;
 		}
 		
 		stack.setAmount(count);
 		
 		return stack;
 	}
 
 	//Gets the proper name of an ItemStack
 	public String getItemName(ItemStack item) {
 		String name = "";
 
 		if(item == null) {
 			return "AIR (0)";
 		}
 
 		if(item.getType() != null) {
 			name += item.getType().toString() + " (" + item.getTypeId() + ")";
 		} else {
 			name += item.getTypeId();
 		}
 
 		if(item.getDurability() != 0) {
 			name += ":" + item.getDurability();
 		}
 
 		return name;
 	}
 
 	//Gets a ItemStack from string with id and damage value
 	public ItemStack getItemStack(String value) {
 		//Format should be either <data value:damage value> or <data value>
 		int id = 0;
 		short damage = 0;
 
 		//Version 0.0.5b - Tweaked this so it would register right
 		int index = value.indexOf(":");
 
 		if(index == -1) {
 			index = value.indexOf("-");
 		}
 
 		if(index == -1) {
 
 			String itemId = value.substring(0, value.length());
 
 			if(!isInteger(itemId)) {
				Material mat = Material.valueOf(itemId);
 				
 				if(mat == null) 
 					return null;
 				
 				id = mat.getId();
 			} else {
 				id = Integer.parseInt(itemId);
 				
 				if(Material.getMaterial(id) == null)
 					return null;
 			}
 
 		} else {
 			String itemId = value.substring(0, index);
 			String itemDamage = value.substring(index + 1, value.length());
 
 			if(!isInteger(itemId)) {
				Material mat = Material.valueOf(itemId);
 				
 				if(mat == null) 
 					return null;
 				
 				id = mat.getId();
 			} else {
 				id = Integer.parseInt(itemId);
 				
 				if(Material.getMaterial(id) == null)
 					return null;
 			}
 			
 			if(!isInteger(itemDamage)) 
 				return null;
 			
 			damage = (short) Integer.parseInt(itemDamage);
 		}
 
 		return new ItemStack(id, 1, damage);
 	}
 
 	//Gets the log level
 	public byte getLogLevel() {
 		return this.logLevel;
 	}
 
 	//Next available entity id
 	public int getNextEntityId() {
 		List<Integer> entityIDs = new ArrayList<Integer>();
 
 		Iterator<Integer> entityItr = entities.keySet().iterator();
 		while(entityItr.hasNext()) {
 			entityIDs.add(entityItr.next());
 		}
 
 		return getNextID(entityIDs);
 	}
 
 	//Next available spawner ID
 	public int getNextSpawnerId() {
 		List<Integer> spawnerIDs = new ArrayList<Integer>();
 
 		Iterator<Integer> spawnerItr = spawners.keySet().iterator();
 		while(spawnerItr.hasNext()) {
 			spawnerIDs.add(spawnerItr.next());
 		}
 
 		return getNextID(spawnerIDs);
 	}
 	
 	//Gets an EntityPotionEffect from format <PotionEffectType>_<level>_<minutes>:<seconds>
 	public SPotionEffect getPotion(String value) {
 		int index1 = value.indexOf("_");
 		int index2 = value.indexOf("_", index1 + 1);
 		int index3 = value.indexOf(":");
 		if(index1 == -1 || index2 == -1 || index3 == -1) {
 			value = "REGENERATION_1_0:0";
 			index1 = value.indexOf("_");
 			index2 = value.indexOf("_", index1 + 1);
 			index3 = value.indexOf(":");
 		}
 
 		PotionEffectType effectType = PotionEffectType.getByName(value.substring(0, index1));
 		int effectLevel = Integer.parseInt(value.substring(index1 + 1, index2));
 		int minutes = Integer.parseInt(value.substring(index2 + 1, index3));
 		int seconds = Integer.parseInt(value.substring(index3 + 1, value.length()));
 		int effectDuration = (minutes * 1200) + (seconds * 20);
 
 		return new SPotionEffect(effectType, effectDuration,  effectLevel);
 	}
 
 	//Gets a spawner from a location
 	public Spawner getSpawnerAt(Location loc) {
 		Iterator<Spawner> spItr = CustomSpawners.spawners.values().iterator();
 
 		while(spItr.hasNext()) {
 			Spawner s = spItr.next();
 
 			if(s.getLoc().equals(loc)) {
 				return s;
 			}
 
 		}
 
 		return null;
 
 	}
 	
 	public Spawner getSpawnerWithEntity(Entity entity) {
 		int entityId = entity.getEntityId();
 		Iterator<Spawner> spawnerItr = spawners.values().iterator();
 
 		while(spawnerItr.hasNext()) {
 			Spawner s = spawnerItr.next();
 
 			Iterator<Integer> mobItr = s.getMobs().keySet().iterator();
 
 			while(mobItr.hasNext()) {
 				Entity currentMob = getEntityFromWorld(mobItr.next(), s.getLoc().getWorld());
 
 				if(currentMob == null) {
 					continue;
 				}
 				
 				if(currentMob.getEntityId() == entityId) {
 					return s;
 				}
 
 			}
 			
 		}
 
 		return null;
 
 	}
 
 	public Spawner getSpawnerWithEntity(int id) {
 		
 		Iterator<Spawner> spawnerItr = spawners.values().iterator();
 
 		while(spawnerItr.hasNext()) {
 			Spawner s = spawnerItr.next();
 			Iterator<Integer> mobItr = s.getMobs().keySet().iterator();
 
 			while(mobItr.hasNext()) {
 				int currentMob = mobItr.next();
 
 				if(currentMob == id) {
 					return s;
 				}
 
 			}
 
 		}
 
 		return null;
 		
 	}
 
 	//Sets up WorldGuard
 	public WorldGuardPlugin getWG() {
 		Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
 
 		if(wg == null || !(wg instanceof WorldGuardPlugin)) 
 			return null;
 
 		return (WorldGuardPlugin) wg;
 	}
 
 	public void onDisable() {
 
 		//Saving Entities
 		fileManager.saveEntities();
 		//Saving spawners
 		fileManager.saveSpawners();
 
 		//Stop Tasks
 		getServer().getScheduler().cancelTasks(this);
 
 		//Disable message
 		log.info("CustomSpawners by thebiologist13 has been disabled!");
 	}
 
 	public void onEnable() {
 
 		//Transparent Blocks
 		transparent.add((byte) 0);
 		transparent.add((byte) 8);
 		transparent.add((byte) 9);
 		transparent.add((byte) 10);
 		transparent.add((byte) 11);
 		
 		//Config
 		config = getCustomConfig();
 
 		//Default Entity
 		defaultEntity = new SpawnableEntity(EntityType.fromName(config.getString("entities.type", "Pig")), -2);
 		defaultEntity.setName("Default");
 
 		//FileManager assignment
 		fileManager = new FileManager(this);
 
 		//Debug
 		debug = config.getBoolean("data.debug", false);
 
 		//LogLevel
 		logLevel = (byte) config.getInt("data.logLevel", 2);
 
 		//Interval
 		saveInterval = (config.getLong("data.interval", 10) * 1200);
 		
 		//Setup WG
 		worldGuard = getWG();
 
 		if(worldGuard == null) {
 
 			if(logLevel > 0) {
 				log.info("[CustomSpawners] Cannot hook into WorldGuard.");
 			}
 
 		} else {
 
 			if(logLevel > 0) {
 				log.info("[CustomSpawners] Hooked into WorldGuard.");
 			}
 
 		}
 
 
 		//Commands
 		SpawnerExecutor se = new SpawnerExecutor(this);
 		CustomSpawnersExecutor cse = new CustomSpawnersExecutor(this);
 		EntitiesExecutor ee = new EntitiesExecutor(this);
 		getCommand("customspawners").setExecutor(cse);
 		getCommand("spawners").setExecutor(se);
 		getCommand("entities").setExecutor(ee);
 
 		//Listeners
 		getServer().getPluginManager().registerEvents(new PlayerLogoutEvent(), this);
 		getServer().getPluginManager().registerEvents(new MobDamageEvent(this), this);
 		getServer().getPluginManager().registerEvents(new MobCombustEvent(), this);
 		getServer().getPluginManager().registerEvents(new PlayerTargetEvent(this), this);
 		getServer().getPluginManager().registerEvents(new MobDeathEvent(this), this);
 		getServer().getPluginManager().registerEvents(new InteractEvent(this), this);
 		getServer().getPluginManager().registerEvents(new ExpBottleHitEvent(this), this);
 		getServer().getPluginManager().registerEvents(new MobExplodeEvent(this), this);
 		getServer().getPluginManager().registerEvents(new MobRegenEvent(this), this);
 		getServer().getPluginManager().registerEvents(new PotionHitEvent(this), this);
 		getServer().getPluginManager().registerEvents(new ProjectileFireEvent(this), this);
 		getServer().getPluginManager().registerEvents(new BreakEvent(this), this);
 		getServer().getPluginManager().registerEvents(new SpawnerPowerEvent(this), this);
 		getServer().getPluginManager().registerEvents(new ReloadEvent(this), this);
 
 		//Load entities from file
 		fileManager.loadEntities();
 
 		//Load spawners from files
 		fileManager.loadSpawners();
 
 		/*
 		 * Spawning Thread
 		 */
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
 			public void run() {
 
 				Iterator<Spawner> spawnerItr = spawners.values().iterator();
 
 				while(spawnerItr.hasNext()) {
 					Spawner s = spawnerItr.next();
 
 					if(!s.getLoc().getChunk().isLoaded()) {
 						continue;
 					}
 
 					s.tick();
 				}
 
 			}
 
 		}, 20, 1);
 
 		/*
 		 * Removal Check Thread
 		 * This thread verifies that all spawned mobs still exist. 
 		 */
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
 			@Override
 			public void run() {
 
 				Iterator<Spawner> sp = spawners.values().iterator();
 				while(sp.hasNext()) {
 					Spawner s = sp.next();
 					Iterator<Integer> spMobs = s.getMobs().keySet().iterator();
 					while(spMobs.hasNext()) {
 
 						int spId = spMobs.next();
 						
 						Entity e = getEntityFromWorld(spId, s.getLoc().getWorld());
 						
 						if(e == null) {
 							s.removeMob(spId);
 							continue;
 						}
 						
 						if(e.getLocation().distance(s.getLoc()) > 192) {
 							s.removeMob(e.getEntityId());
 							e.remove();
 						}
 
 					}
 
 				}
 
 			}
 
 		}, 20, 20);
 
 		/*
 		 * Autosave Thread
 		 * This thread manages autosaving
 		 */
 		if(config.getBoolean("data.autosave") && config.getBoolean("data.saveOnClock")) {
 
 			autosaveId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
 				@Override
 				public void run() {
 
 					fileManager.autosaveAll();
 
 				}
 
 			}, 20, saveInterval);
 		}
 
 		//Enable message
 		log.info("[CustomSpawners] CustomSpawners by thebiologist13 has been enabled!");
 	}
 	
 	//Parses the entity name
 	public String parseEntityName(EntityType type) {
 		String nameOfType = type.getName();
 
 		if(nameOfType == null) {
 			return type.toString();
 		} else {
 			return nameOfType;
 		}
 
 	}
 	
 	//Parses the entity type from it's name
 	public EntityType parseEntityType(String entityType, boolean hasOverride) {
 		EntityType type = null;
 
 		if(entityType.equalsIgnoreCase("irongolem")) {
 
 			type = EntityType.IRON_GOLEM;
 
 		} else if(entityType.equalsIgnoreCase("mooshroom")) {
 
 			type = EntityType.MUSHROOM_COW;
 
 		} else if(entityType.equalsIgnoreCase("zombiepigman")) {
 
 			type = EntityType.PIG_ZOMBIE;
 
 		} else if(entityType.equalsIgnoreCase("magmacube") || entityType.equalsIgnoreCase("fireslime") || entityType.equalsIgnoreCase("firecube")) {
 
 			type = EntityType.MAGMA_CUBE;
 
 		} else if(entityType.equalsIgnoreCase("snowman") || entityType.equalsIgnoreCase("snowgolem")) {
 
 			type = EntityType.SNOWMAN;
 
 		} else if(entityType.equalsIgnoreCase("ocelot") || entityType.equalsIgnoreCase("ozelot")) {
 
 			type = EntityType.OCELOT;
 
 		} else if(entityType.equalsIgnoreCase("arrow")) {
 
 			type = EntityType.ARROW;
 
 		} else if(entityType.equalsIgnoreCase("snowball")) {
 
 			type = EntityType.SNOWBALL;
 
 		} else if(entityType.equalsIgnoreCase("falling_block") || entityType.equalsIgnoreCase("fallingblock") ||
 				entityType.equalsIgnoreCase("sand") || entityType.equalsIgnoreCase("gravel")) {
 
 			type = EntityType.FALLING_BLOCK;
 
 		} else if(entityType.equalsIgnoreCase("tnt") || entityType.equalsIgnoreCase("primed_tnt")
 				|| entityType.equalsIgnoreCase("primed_tnt")) {
 
 			type = EntityType.PRIMED_TNT;
 
 		} else if(entityType.equalsIgnoreCase("firecharge") || entityType.equalsIgnoreCase("smallfireball")
 				|| entityType.equalsIgnoreCase("fire_charge")|| entityType.equalsIgnoreCase("small_fireball")) {
 
 			type = EntityType.SMALL_FIREBALL;
 
 		} else if(entityType.equalsIgnoreCase("fireball") || entityType.equalsIgnoreCase("ghastball")
 				|| entityType.equalsIgnoreCase("fire_ball")|| entityType.equalsIgnoreCase("ghast_ball")) {
 
 			type = EntityType.FIREBALL;
 
 		} else if(entityType.equalsIgnoreCase("potion") || entityType.equalsIgnoreCase("splashpotion")
 				|| entityType.equalsIgnoreCase("splash_potion")) {
 
 			type = EntityType.SPLASH_POTION;
 
 		} else if(entityType.equalsIgnoreCase("experience_bottle") || entityType.equalsIgnoreCase("experiencebottle")
 				|| entityType.equalsIgnoreCase("xpbottle") || entityType.equalsIgnoreCase("xp_bottle")
 				|| entityType.equalsIgnoreCase("expbottle") || entityType.equalsIgnoreCase("exp_bottle")) {
 
 			type = EntityType.THROWN_EXP_BOTTLE;
 
 		} else if(entityType.equalsIgnoreCase("item") || entityType.equalsIgnoreCase("drop")) {
 
 			type = EntityType.DROPPED_ITEM;
 
 		} else if(entityType.equalsIgnoreCase("enderpearl") || entityType.equalsIgnoreCase("ender_pearl")
 				|| entityType.equalsIgnoreCase("enderball") || entityType.equalsIgnoreCase("ender_ball")) {
 
 			type = EntityType.ENDER_PEARL;
 
 		} else if(entityType.equalsIgnoreCase("endercrystal") || entityType.equalsIgnoreCase("ender_crystal")
 				|| entityType.equalsIgnoreCase("enderdragoncrystal") || entityType.equalsIgnoreCase("enderdragon_crystal")) {
 
 			type = EntityType.ENDER_CRYSTAL;
 
 		} else if(entityType.equalsIgnoreCase("egg")) {
 
 			type = EntityType.EGG;
 
 		} else if(entityType.equalsIgnoreCase("wither") || entityType.equalsIgnoreCase("witherboss")
 				|| entityType.equalsIgnoreCase("wither_boss")) {
 
 			type = EntityType.WITHER;
 
 		} else {
 
 			//Try to parse an entity type from input. Null if invalid.
 			type = EntityType.fromName(entityType);
 
 		}
 
 		return type;
 
 	}
 	
 	public void printDebugMessage(String message) {
 		if(debug) {
 			log.info("[CS_DEBUG] " + message);
 		}
 
 	}
 	
 	public void printDebugMessage(String message, Class<?> clazz) {
 		if(debug) {
 			if(clazz != null) {
 				log.info("[CS_DEBUG] " + clazz.getName() + ": " + message);
 			} else {
 				log.info("[CS_DEBUG] " + message);
 			}
 
 		}
 
 	}
 
 	public void printDebugMessage(String[] message) {
 		if(debug) {
 			for(String s : message) {
 				printDebugMessage(s);
 			}
 		}
 	}
 
 	public void printDebugMessage(String[] message, Class<?> clazz) {
 		if(debug) {
 			for(String s : message) {
 				printDebugMessage(s, clazz);
 			}
 		}
 	}
 	
 	public void printDebugTrace(Exception e) {
 		if(debug) {
 			log.severe("[CS_DEBUG] " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	//Config stuff
 	public void reloadCustomConfig() {
 		if (configFile == null) {
 			configFile = new File(getDataFolder(), "config.yml");
 
 			if(!configFile.exists()){
 				configFile.getParentFile().mkdirs();
 				copy(getResource("config.yml"), configFile);
 			}
 
 		}
 
 		config = YamlConfiguration.loadConfiguration(configFile);
 
 		// Look for defaults in the jar
 		InputStream defConfigStream = this.getResource("config.yml");
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			config.options().copyDefaults(true);
 			config.setDefaults(defConfig);
 		}
 
 	}
 
 	//Remove an entity
 	public void removeEntity(SpawnableEntity e) {
 		if(entities.containsValue(e)) {
 			resetEntitySelections(e.getId());
 			entities.remove(e.getId());
 			for(Spawner s : spawners.values()) {
 				s.removeTypeData(e);
 			}
 		}
 	}
 
 	//Removes a spawner from a mob list when it dies
 	public synchronized void removeMob(final Entity e) { //Called when an entity dies. l is the dead entity.
 		int entityId = e.getEntityId();
 
 		Iterator<Spawner> itr = CustomSpawners.spawners.values().iterator();
 
 		while(itr.hasNext()) {
 			Spawner s = itr.next();
 
 			Iterator<Integer> mobs = s.getMobs().keySet().iterator();
 
 			while(mobs.hasNext()) {
 				Entity spawnerMob = getEntityFromWorld(mobs.next(), s.getLoc().getWorld());
 
 				if(spawnerMob == null) {
 					continue;
 				}
 				
 				if(spawnerMob.getEntityId() == entityId) {
 					mobs.remove();
 					if(DamageController.extraHealthEntities.containsKey(spawnerMob)) 
 						DamageController.extraHealthEntities.remove(spawnerMob);
 				}
 
 			}
 
 		}
 
 	}
 
 	//Removes mobs spawned by a certain spawner
 	public synchronized void removeMobs(final Spawner s) { //Called in the removemobs command
 		Iterator<Integer> mobs = s.getMobs().keySet().iterator();
 
 		while(mobs.hasNext()) {
 			Entity spawnerMob = getEntityFromWorld(mobs.next(), s.getLoc().getWorld());
 
 			if(spawnerMob == null) {
 				continue;
 			}
 			
 			if(spawnerMob.getPassenger() != null)
 				spawnerMob.getPassenger().remove();
 
 			if(DamageController.extraHealthEntities.containsKey(spawnerMob)) 
 				DamageController.extraHealthEntities.remove(spawnerMob);
 
 			spawnerMob.remove();
 			mobs.remove();
 
 		}
 
 		s.getMobs().clear();
 
 	}
 
 	//Remove a spawner
 	public void removeSpawner(Spawner s) {
 		if(spawners.containsValue(s)) {
 			resetSpawnerSelections(s.getId());
 			spawners.remove(s.getId());
 		}
 	}
 
 	//Resets selections if a SpawnableEntity has been removed
 	public void resetEntitySelections(int id) {
 		Iterator<Player> pItr = entitySelection.keySet().iterator();
 
 		while(pItr.hasNext()) {
 			Player p = pItr.next();
 
 			if(entitySelection.get(p) == id) {
 				p.sendMessage(ChatColor.RED + "Your selected entity has been removed.");
 				entitySelection.remove(p);
 			}
 		}
 
 	}
 
 	//Resets selections if a spawner is removed
 	public void resetSpawnerSelections(int id) {
 		Iterator<Player> pItr = entitySelection.keySet().iterator();
 
 		while(pItr.hasNext()) {
 			Player p = pItr.next();
 
 			if(spawnerSelection.get(p) == id) {
 				p.sendMessage(ChatColor.RED + "Your selected spawner has been removed.");
 				spawnerSelection.remove(p);
 			}
 		}
 
 	}
 
 	public void saveCustomConfig() {
 		if (config == null || configFile == null) {
 			return;
 		}
 		try {
 			config.save(configFile);
 		} catch (IOException ex) {
 			log.severe("Could not save config to " + configFile.getPath());
 		}
 	}
 
 	public void saveCustomEntityToWorld(SpawnableEntity data, File path) {
 		fileManager.saveEntity(data, path);
 	}
 
 	//This saves a Spawner to the world folder. Kind of "cheating" to make it so custom spawners can be recovered from the world.
 	public void saveCustomSpawnerToWorld(Spawner data) {
 		World w = data.getLoc().getWorld();
 
 		String ch = File.separator;
 		String worldDir = w.getWorldFolder() + ch + "cs_data" + ch;
 		String entityDir = worldDir + ch + "entity";
 		String spawnerDir = worldDir + ch + "spawner";
 
 		String spawnerPath = spawnerDir + ch + data.getId() + ".dat";
 
 		File spawnerFile = new File(spawnerPath);
 
 		File entityFilesDir = new File(entityDir);
 
 		List<Integer> types = data.getTypeData();
 
 		File[] entityFilesList = entityFilesDir.listFiles();
 		ArrayList<String> entityFiles = new ArrayList<String>();
 
 		for(File f : entityFilesList) {
 			entityFiles.add(f.getPath());
 		}
 
 		Iterator<Integer> tItr = types.iterator();
 		while(tItr.hasNext()) {
 			int i = tItr.next();
 
 			printDebugMessage("Checking if entity files exist");
 
 			String fileName = entityDir + ch + i + ".dat";
 
 			printDebugMessage("File to check: " + fileName);
 
 			if(!entityFiles.contains(fileName)) {
 				printDebugMessage("Doesn't contain file. Creating...");
 				saveCustomEntityToWorld(getEntity(String.valueOf(i)), new File(fileName));
 			}
 		}
 
 		printDebugMessage("World Folder: " + spawnerFile.getPath());
 
 		fileManager.saveSpawner(data, spawnerFile);
 	}
 
 	public void sendMessage(CommandSender sender, String message) {
 
 		if(sender == null) 
 			return;
 
 		Player p = null;
 
 		if(sender instanceof Player)
 			p = (Player) sender;
 
 		if(p == null) {
 			message = "[CustomSpawners] " + ChatColor.stripColor(message);
 			log.info(message);
 		} else {
 			p.sendMessage(message);
 		}
 
 	}
 
 	public void sendMessage(CommandSender sender, String[] message) {
 
 		if(sender == null) 
 			return;
 
 		Player p = null;
 
 		if(sender instanceof Player)
 			p = (Player) sender;
 
 		if(p == null) {
 
 			for(String s : message) {
 				s = "[CustomSpawners] " + ChatColor.stripColor(s);
 				log.info(s);
 			}
 
 		} else {
 			p.sendMessage(message);
 		}
 
 	}
 
 }
