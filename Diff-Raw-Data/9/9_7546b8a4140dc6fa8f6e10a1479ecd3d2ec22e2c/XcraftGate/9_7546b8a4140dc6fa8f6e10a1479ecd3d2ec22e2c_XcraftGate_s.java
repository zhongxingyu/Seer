 package de.xcraft.engelier.XcraftGate;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.xcraft.engelier.XcraftGate.Commands.*;
 import de.xcraft.engelier.XcraftGate.Generator.Generator;
 
 public class XcraftGate extends JavaPlugin {
 	private final ListenerServer pluginListener = new ListenerServer(this);
 	private final ListenerPlayer playerListener = new ListenerPlayer(this);
 	private final ListenerCreature creatureListener = new ListenerCreature(this);
 	private final ListenerEntity entityListener = new ListenerEntity(this);
 	private final ListenerWeather weatherListener = new ListenerWeather(this);
 	private final ListenerWorld worldListener = new ListenerWorld(this);
 	private final InventoryManager inventoryManager = new InventoryManager(this);
 
 	private PluginManager pm = null;
 	
 	private SetWorld worlds = new SetWorld(this);
 	private SetGate gates = new SetGate(this);
 	
 	public Map<String, Location> justTeleported = new HashMap<String, Location>();
 	public Map<String, Location> justTeleportedFrom = new HashMap<String, Location>();
 
 	public YamlConfiguration config = null;
 
 	public final Logger log = Logger.getLogger("Minecraft");
 	public final Properties serverconfig = new Properties(); 
 
 	class RunCreatureLimit implements Runnable {
 		public void run() {
 			for (DataWorld thisWorld: worlds) {
 				thisWorld.checkCreatureLimit();
 			}
 		}
 	}
 	
 	class RunTimeFrozen implements Runnable {
 		public void run() {
 			for (DataWorld thisWorld: worlds) {
 				if (thisWorld.isTimeFrozen()) {
 					thisWorld.resetFrozenTime();
 				}
 			}
 		}
 	}
 	
 	class RunCheckWorldInactive implements Runnable {
 		@Override
 		public void run() {
 			for (World thisWorld : getServer().getWorlds()) {
 				if (worlds.get(thisWorld).checkInactive() && !thisWorld.getName().equalsIgnoreCase(serverconfig.getProperty("level-name"))) {
 					log.info(getNameBrackets() + "World '" + thisWorld.getName() + "' inactive. Unloading.");
 					
 					worlds.get(thisWorld).unload();
 				}
 			}						
 		}		
 	}
 	
 	class RunLoadAllWorlds implements Runnable {
 		@Override
 		public void run() {
 			for (DataWorld thisWorld : worlds) {
 				if (!thisWorld.isLoaded() && (config.getBoolean("dynworld.enabled", false) == false || thisWorld.isSticky())) {
 					thisWorld.load();
 				}
 			}
 		}		
 	}
 	
 	public void saveAll() {
 		playerListener.savePlayers();
 		inventoryManager.save();
 		gates.save();
 		worlds.save();		
 	}
 	
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		saveAll();
 	}
 
 	public void onEnable() {
 		playerListener.loadPlayers();
 		inventoryManager.load();
 		
 		pm = new PluginManager(this);
 				
 		pm.registerEvents(creatureListener);
 		pm.registerEvents(entityListener);
 		pm.registerEvents(playerListener);
 		pm.registerEvents(pluginListener);
 		pm.registerEvents(weatherListener);
 		pm.registerEvents(worldListener);
 
 		
 		
 		File serverconfigFile = new File("server.properties");
 		if (!serverconfigFile.exists()) {
 			log.severe(getNameBrackets() + "unable to load server.properties.");
 		} else {
 			try {
 				serverconfig.load(new FileInputStream(serverconfigFile));
 			} catch (Exception ex) {
 				log.severe(getNameBrackets() + "error loading " + serverconfigFile);
 				ex.printStackTrace();
 			}
 		}
 
 		config = getConfig(getConfigFile("config.yml"));
 		try {
 			setConfigDefaults();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		worlds.load();
 		gates.load();
 
		for(World thisWorld : getServer().getWorlds()) {
			worlds.onWorldLoad(thisWorld);
		}
		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new RunCreatureLimit(), 600, 600);
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new RunTimeFrozen(), 200, 200);
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				saveAll();
 			}
 		}, 12000, 12000);
 		
 		if (config.getBoolean("dynworld.enabled", false)) {
 			getServer().getScheduler().scheduleSyncRepeatingTask(this, new RunCheckWorldInactive(), config.getInt("dynworld.checkInterval", 60) * 20, config.getInt("dynworld.checkInterval", 60) * 20);
 		}
 		
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new RunLoadAllWorlds());
 		getServer().getScheduler().scheduleSyncDelayedTask(this, pm);
 		
 		try {
 			getCommand("gate").setExecutor(new CommandHandlerGate(this));
 			getCommand("gworld").setExecutor(new CommandHandlerWorld(this));
 		} catch (Exception ex) {
 			log.warning(getNameBrackets() + "getCommand().setExecutor() failed! Seems I got enabled by another plugin. Nag the bukkit team about this!");
 		}
 	}
 	
 	@Override
 	public YamlConfiguration getConfig() {
 		return config;
 	}
 	
 	public YamlConfiguration getConfig(String fileName) {
 		return getConfig(getConfigFile(fileName));
 	}
 	
 	public YamlConfiguration getConfig(File file) {
 		YamlConfiguration ret = new YamlConfiguration();
 		try {
 			ret.load(file);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 		return ret;
 	}
 	
 	public File getConfigFile(String fileName) {
 		File configFile = new File(getDataFolder(), fileName);
 
 		if (!configFile.exists()) {
 			try {
 				getDataFolder().mkdir();
 				getDataFolder().setWritable(true);
 				getDataFolder().setExecutable(true);
 				
 				configFile.createNewFile();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 		
 		return configFile;
 	}
 	
 	private void setConfigDefaults() throws IOException {
 		config.addDefault("dynworld.enabled", true);
 		config.addDefault("dynworld.checkInterval", 60);
 		config.addDefault("dynworld.maxInactiveTime", 300);
 		
 		config.addDefault("invsep.enabled", true);
 		config.addDefault("invsep.exp", true);
 		config.addDefault("invsep.health", true);
 		config.addDefault("invsep.food", true);
 		
 		config.addDefault("fixes.chunkRefreshOnTeleport", false);
 		
 		config.addDefault("biomes.desert.chanceCactus", 1);
 		config.addDefault("biomes.desert.chanceDeadShrub", 2);
 		config.addDefault("biomes.forest.chanceLakeWater", 1);
 		config.addDefault("biomes.forest.chanceTreeNormal", 32);
 		config.addDefault("biomes.forest.chanceTreeBig", 2);
 		config.addDefault("biomes.forest.chanceTreeBirch", 32);
 		config.addDefault("biomes.forest.chanceTreeRedwood", 16);
 		config.addDefault("biomes.forest.chanceTreeTallRedwood", 2);
 		config.addDefault("biomes.forest.chanceFlowerYellow", 4);
 		config.addDefault("biomes.forest.chanceFlowerRedRose", 4);
 		config.addDefault("biomes.forest.chanceGrassTall", 50);
 		config.addDefault("biomes.plains.chanceTreeNormal", 1);
 		config.addDefault("biomes.plains.chanceFlowerYellow", 10);
 		config.addDefault("biomes.plains.chanceFlowerRedRose", 10);
 		config.addDefault("biomes.plains.chanceGrassTall", 150);
 		config.addDefault("biomes.rainforest.chanceLakeWater", 3);
 		config.addDefault("biomes.rainforest.chanceTreeNormal", 28);
 		config.addDefault("biomes.rainforest.chanceTreeBig", 2);
 		config.addDefault("biomes.rainforest.chanceTreeBirch", 28);
 		config.addDefault("biomes.rainforest.chanceTreeRedwood", 32);
 		config.addDefault("biomes.rainforest.chanceTreeTallRedwood", 2);
 		config.addDefault("biomes.rainforest.chanceFlowerYellow", 5);
 		config.addDefault("biomes.rainforest.chanceFlowerRedRose", 5);
 		config.addDefault("biomes.rainforest.chanceGrassFern", 30);
 		config.addDefault("biomes.rainforest.chanceGrassTall", 70);
 		config.addDefault("biomes.savanna.chanceTreeNormal", 1);
 		config.addDefault("biomes.seasonalforest.chanceLakeWater", 2);
 		config.addDefault("biomes.seasonalforest.chanceTreeNormal", 32);
 		config.addDefault("biomes.seasonalforest.chanceTreeBig", 2);
 		config.addDefault("biomes.seasonalforest.chanceTreeBirch", 32);
 		config.addDefault("biomes.seasonalforest.chanceTreeRedwood", 28);
 		config.addDefault("biomes.seasonalforest.chanceTreeTallRedwood", 2);
 		config.addDefault("biomes.seasonalforest.chanceFlowerYellow", 4);
 		config.addDefault("biomes.seasonalforest.chanceFlowerRedRose", 4);
 		config.addDefault("biomes.seasonalforest.chanceGrassTall", 70);
 		config.addDefault("biomes.shrubland.chanceLakeLava", 1);
 		config.addDefault("biomes.shrubland.chanceTreeNormal", 3);
 		config.addDefault("biomes.shrubland.chanceGrassShrub", 5);
 		config.addDefault("biomes.swampland.chanceSugarCane", 75);
 		config.addDefault("biomes.swampland.chanceLakeWater", 10);
 		config.addDefault("biomes.taiga.chanceTreeRedwood", 4);
 		config.addDefault("biomes.taiga.chanceGrassTall", 2);
 		config.addDefault("biomes.tundra.chanceLakeWater", 1);
 		
 		System.out.println("Saving default config.");
 		config.options().copyDefaults();
 		config.save(getConfigFile("config.yml"));
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,	String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("gate")) {
 			getCommand("gate").setExecutor(new CommandHandlerGate(this));
 			getCommand("gate").execute(sender, commandLabel, args);
 			return true;
 		} else if (cmd.getName().equalsIgnoreCase("gworld")) {
 			getCommand("gworld").setExecutor(new CommandHandlerWorld(this));						
 			getCommand("gworld").execute(sender, commandLabel, args);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public String getNameBrackets() {
 		return "[" + this.getDescription().getFullName() + "] ";
 	}
 	
 	public SetWorld getWorlds() {
 		return worlds;
 	}
 	
 	public SetGate getGates() {
 		return gates;
 	}
 	
 	public PluginManager getPluginManager() {
 		return pm;
 	}
 	
 	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
 		for (Generator thisGen : Generator.values()) {
 			if (thisGen.toString().equalsIgnoreCase(id)) {
 				return thisGen.getChunkGenerator(this);
 			}
 		}
 
 		return null;
 	}
 }
