 package com.gmail.zariust.mcplugins.othergrowth;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class OtherGrowthConfig {
 
 	public static Verbosity verbosity = Verbosity.NORMAL;
 	public static long taskDelay;
 	public static boolean globalDisableMetrics;
 
 	private final OtherGrowth parent;
 
 	// Track loaded files so we don't get into an infinite loop
 	Set<String> loadedDropFiles = new HashSet<String>();
 	private String configsFile;
 	public static int globalChunkScanRadius = 6;
 	public static Double globalChanceToReplace = 0.5;
 	public static boolean globalScanAsync = true;
 	public static boolean globalScanLoadedChunks = true;
 	public static boolean globalRunOnStartup = true;
 	
 	public OtherGrowthConfig(OtherGrowth instance) {
 		parent = instance;
 		verbosity = Verbosity.HIGH;
 		taskDelay = 200;
 	}
 
 	public void loadFromStartup() {
         loadConfig();
 		Dependencies.init();
 		loadIncludeFile(configsFile);
 		if (globalRunOnStartup) {
 			OtherGrowth.disableOtherGrowth();
 			OtherGrowth.enableOtherGrowth();
 		}
 		
 	}
 	
 	public void load() {
         loadConfig();
 		Dependencies.init();
 		loadIncludeFile(configsFile);
 
         OtherGrowth.disableOtherGrowth();
         OtherGrowth.enableOtherGrowth();
 	}
 	
 	public void loadConfig() {
 		loadedDropFiles.clear();
 		
 		parent.getDataFolder().mkdirs();
 		String filename = "config.yml";
 
 		File global = new File(parent.getDataFolder(), filename);
 		YamlConfiguration globalConfig = YamlConfiguration.loadConfiguration(global);
 
 		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload)
 		// TODO: create the folder if it doesn't exist
 		if (!global.exists()) {
 			writeDefaultConfig(global);
 		}
 		// Load in the values from the configuration file
 		try {
 			globalConfig.load(global);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		verbosity = CommonPlugin.getConfigVerbosity(globalConfig);
 		configsFile = globalConfig.getString("rootconfig", "recipes.yml");
 
 		//safeInsiderecipe = globalConfig.getBoolean("safeinsiderecipe", true);
 		taskDelay = globalConfig.getInt("tick_delay_between_scans", 200);
 		globalChanceToReplace = globalConfig.getDouble("chance_to_replace", 0.5);
 		globalDisableMetrics = globalConfig.getBoolean("disable_metrics", false);
 		globalRunOnStartup = globalConfig.getBoolean("run_on_startup", true);
 		globalScanAsync = globalConfig.getBoolean("scan_asynchronously", true);
 		globalScanLoadedChunks = globalConfig.getBoolean("scan_all_loaded_chunks", true);
 		globalChunkScanRadius = globalConfig.getInt("chunk_scan_radius", 6);
 		
 		if (taskDelay < 5) taskDelay = 5; // a minimum for safety
 		Log.high("Loaded global config ("+global+"), keys found: "+globalConfig.getKeys(false).toString() + " (verbosity="+verbosity+")");
 
 	}
 
 	private void writeDefaultConfig(File global) {
 		try {
 			global.createNewFile();
 			Log.normal("Created an empty file " + parent.getDataFolder() +"/"+global.getName()+", please edit it!");
 
 			PrintWriter stream = null;
 			stream = new PrintWriter(global);
 			//Let's write our goods ;)
 			stream.println("verbosity: normal");
 			stream.println("run_on_startup: true");
 			stream.println("tick_delay_between_scans: 200");
 			stream.println("scan_asynchronously: true");
 			stream.println("scan_all_loaded_chunks: true  # ignores radius setting (if true)");
 			stream.println("chunk_scan_radius: 6");
 			stream.println("material_to_replace: (eg. cobblestone, air or glass)");
 			stream.println("material_to_replace_with: (eg. mossycobblestone, leaves or glowstone)");
 			stream.println("material_needed: (eg. water, grass or 'no-needed-block')");
 			stream.println("chance_to_replace: 0.5");			
 			stream.println("");
 			stream.close();
 			//globalConfig.save(global);
 		} catch (IOException ex){
 			Log.warning("Could not generate "+global.getName()+". Are the file permissions OK?");
 		}
 	}		
 
 	private void loadIncludeFile(String filename) {
 		// Check for infinite include loops
 		if(loadedDropFiles.contains(filename)) {
 			Log.warning("Infinite include loop detected at " + filename);
 			return;
 		} else loadedDropFiles.add(filename);
 
 		Log.high("Loading file: "+filename);
 
 		File yml = new File(parent.getDataFolder(), filename);
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(yml);
 
 		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
 		if (!yml.exists())
 		{
 			try {
 				yml.createNewFile();
 				Log.normal("Created an empty file " + parent.getDataFolder() +"/"+filename+", please edit it!");
 				config.set("recipes", null);
 				Map<String,Object> map = new HashMap<String, Object>();
 				map.put("target", "COBBLESTONE");
 				map.put("replacement", "MOSSY_COBBLESTONE");
 				map.put("needed", "STATIONARY_WATER");
 				map.put("world", "WORLD");
 				map.put("chance", 0.5);
 				List<Object> list = new ArrayList<Object>();
 				list.add(map);
 				config.set("recipes.cobbletomossy", list);
 				config.set("include-files", null);
 				config.set("defaults", null);
 				config.set("aliases", null);
 				config.save(yml);
 			} catch (IOException ex){
 				Log.warning(parent.getDescription().getName() + ": could not generate "+filename+". Are the file permissions OK?");
 			}
 			// Nothing to load in this case, so exit now
 			return;
 		}
 
 		try {
 			config.load(yml);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Load the drops
 		ConfigurationSection node = config.getConfigurationSection("recipes");
 		Set<String> blocks = null;
 
 		if (node != null) {
 			blocks = node.getKeys(false);
 			for(String name : blocks) {
 				loadrecipe(node.getConfigurationSection(name), name);
 			}
 		}
 
 		// Load the include files
 		List<String> includeFiles = config.getStringList("include-files");
 		for(String include : includeFiles) loadIncludeFile(include);
 	}
 
 
 	private void loadrecipe(ConfigurationSection node, String name) {
 		if (node == null) Log.high("No options found for recipe ("+name+")");
 		//		for(String childName : node.getKeys(name)) {
 		//		ConfigurationNode subNode = node.getNode(name+"/"+childName);
 		//Main.logInfo("Parsing recipe ("+name+")", Verbosity.HIGH);
 		//boolean isGroup = dropNode.getKeys().contains("dropgroup");
 		Recipe recipe = Recipe.parseFrom(name, node);
 
 		// loop through worlds and if positive add the recipe to that world
 		if (recipe == null) {
 			Log.warning("recipe failed [null] ("+name+")");
 			return;
 		} 
 		
 		if(recipe.worlds == null) {
 			Log.high("No worlds found for recipe ("+name+"), adding to all.");
 			for (World world : Bukkit.getServer().getWorlds()) {
				if (OtherGrowth.recipes.get(world.getName()) != null) {
					OtherGrowth.recipes.get(world.getName()).add(recipe);
 				} else {
 					Set<Recipe> recipes = new HashSet<Recipe>();
 					recipes.add(recipe);
 					OtherGrowth.recipes.put(world.getName(), recipes);
 				}	
 				Log.high("Adding recipes to world ("+world.getName()+").");
 
 			}
 		} else {
 
 			for (World world : Bukkit.getServer().getWorlds()) {
 				Boolean activeWorld = recipe.worlds.get(world.getName()); 
 				if (activeWorld == null) {
 					//Main.logWarning("Error: world ("+world.getName()+") is null in recipe.");
 					activeWorld = false;
 				}
 				//Log.high("recipe worlds: "+activeWorld);
 				if (activeWorld || recipe.worlds.get(null)) {
					if (OtherGrowth.recipes.get(world.getName()) != null) {
						OtherGrowth.recipes.get(world.getName()).add(recipe);
 					} else {
 						Set<Recipe> recipes = new HashSet<Recipe>();
 						recipes.add(recipe);
 						OtherGrowth.recipes.put(world.getName(), recipes);
 					}
 					Log.high("Adding recipes to world ("+world.getName()+").");
 				}
 			}
 		}
 	}
 
 	public static List<String> getMaybeList(ConfigurationSection node, String key) {
 		if(node == null) return new ArrayList<String>();
 		Object prop = node.get(key);
 		List<String> list;
 		if(prop == null) return new ArrayList<String>();
 		else if(prop instanceof List) list = node.getStringList(key);
 		else list = Collections.singletonList(prop.toString());
 		return list;
 	}
 
 	public static Map<String, Boolean> parseWorldsFrom(ConfigurationSection node, Map<String, Boolean> def) {
 		Log.highest(node.toString());
 		List<String> worlds = getMaybeList(node, "world");
 		List<String> worldsExcept = getMaybeList(node, "worldexcept");
 
 		if(worlds.isEmpty() && worldsExcept.isEmpty()) return def;
 		Map<String, Boolean> result = new HashMap<String,Boolean>();
 		result.put(null, false); 
 		for(String name : worlds) {
 			if(name.equalsIgnoreCase("ALL") || name.equalsIgnoreCase("ANY")) {
 				result.put(null, true);
 				continue;
 			}
 			World world = Bukkit.getServer().getWorld(name);
 			if(world == null && name.startsWith("-")) {
 				world = Bukkit.getServer().getWorld(name.substring(1));
 				if(world == null) {
 					Log.warning("Invalid world " + name + "; skipping...");
 					continue;
 				}
 				result.put(world.getName(), false);
 			} else if (world != null) result.put(world.getName(), true);
 			// wildcard
 		}
 		for(String name : worldsExcept) {
 			World world = Bukkit.getServer().getWorld(name);
 			if(world == null) {
 				Log.warning("Invalid world exception " + name + "; skipping...");
 				continue;
 			}
 			result.put(world.getName(), false);
 		}
 		if(result.isEmpty()) return null;
 		return result;
 	}
 
 
 	public static Verbosity getVerbosity() {
 		return verbosity;
 	}
 
 
 }
 
