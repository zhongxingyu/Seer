 package com.ftwinston.KillerMinecraft;
 
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import com.ftwinston.KillerMinecraft.Game.GameState;
 
 
 public class Settings
 {
 	public static final int absMaxGames = 8;
 	public static int numGames;
 	
 	public static String
 	stagingWorldName,
 	killerWorldNamePrefix,
	defaultGameMode = "Killer", // remove this
 	defaultWorldGenerator = "Default World"; // remove this
 	
 	public static boolean
 	nothingButKiller,
 	allowTeleportToStagingArea,
 	filterChat,
 	filterScoreboard,
 	allowPlayerLimits,
 	allowSpectators,
 	allowLateJoiners,
 	reportStats;
 	
 	public static Location spawnCoordMin, spawnCoordMax, protectionMin, protectionMax;
 	
 	public static final int minPlayerLimit = 2, maxPlayerLimit = 200;
 		
 	public static Material teleportModeItem = Material.WATCH, followModeItem = Material.ARROW;
 	
 	public static void setup(KillerMinecraft plugin)
 	{
 		plugin.saveDefaultConfig();
 		FileConfiguration config = plugin.getConfig();
 		
 		stagingWorldName = config.getString("stagingWorldName", "staging");
 		killerWorldNamePrefix = config.getString("killerWorldNamePrefix", "killer");
 
 		spawnCoordMin = readLocation(config, "stagingSpawn.from", null, -8, 64, -8);
 		spawnCoordMax = readLocation(config, "stagingSpawn.to", null, 8, 64, 8);
 		sortMinMax(spawnCoordMin, spawnCoordMax);
 		
 		protectionMin = readLocation(config, "protected.from", null, -8, 64, -8);
 		protectionMax = readLocation(config, "protected.to", null, 8, 64, 8);
 		sortMinMax(spawnCoordMin, spawnCoordMax);
 		
 		nothingButKiller = config.getBoolean("nothingButKiller", false);
 		allowTeleportToStagingArea = config.getBoolean("allowTeleportToStagingArea", true);
 		filterChat = config.getBoolean("filterChat", true);
 		filterScoreboard = config.getBoolean("filterScoreboard", true);
 		allowSpectators = config.getBoolean("allowSpectators", true);
 		reportStats = config.getBoolean("reportStats", true);
 		allowLateJoiners = config.getBoolean("allowLateJoiners", true);
 		allowPlayerLimits = config.getBoolean("allowPlayerLimits", true);
 		
 		//startingItems = readMaterialList(config, "startingItems", new ArrayList<Integer>(), Material.STONE_PICKAXE);
 	}
 
 	private static void sortMinMax(Location min, Location max)
 	{
 		if ( max.getX() < min.getX() )
 		{
 			double tmp = max.getX();
 			max.setX(min.getX());
 			min.setX(tmp);
 		}
 		if ( max.getY() < min.getY() )
 		{
 			double tmp = max.getY();
 			max.setY(min.getY());
 			min.setY(tmp);
 		}
 		if ( max.getZ() < min.getZ() )
 		{
 			double tmp = max.getZ();
 			max.setZ(min.getZ());
 			min.setZ(tmp);
 		}
 	}
 	
 	private static void setStagingWorld(World world)
 	{
 		spawnCoordMin.setWorld(world);
 		spawnCoordMax.setWorld(world);
 		protectionMin.setWorld(world);
 		protectionMax.setWorld(world);
 	}
 	
 	public static boolean setupGames(KillerMinecraft plugin)
 	{
 		if ( plugin.games != null )
 			return false;
 		
 		setStagingWorld(plugin.stagingWorld);
 		
 		List<?> config = plugin.getConfig().getList("games");
 		
 		if ( config == null )
 		{
 			plugin.log.warning("Killer cannot start: Configuration contains no game information");
 			return false;
 		}
 		
 		numGames = config.size();
 		if ( numGames == 0 )
 		{
 			plugin.log.warning("Killer cannot start: Configuration game section contains no information");
 			return false;
 		}
 		
 		if ( numGames > absMaxGames )
 		{
 			plugin.log.warning("Killer only supports up to " + absMaxGames + " games, but " + numGames + " were specified. Ignoring the excess...");
 			numGames = absMaxGames;
 		}
 		
 		plugin.games = new Game[numGames];
 		
 		for ( int iGame = 0; iGame < numGames; iGame++ )
 		{
 			LinkedHashMap<String, Object> gameConfig = resolveConfigSection(config.get(iGame));
 			
 			if ( gameConfig == null )
 			{
 				plugin.log.warning("Killer cannot start: Invalid configuration for game #" + (iGame+1));
 				return false;
 			}
 
 			Game g;
 			plugin.games[iGame] = g = new Game(plugin, iGame);
 			setupGame(plugin, g, gameConfig);
 		}
 		
 		// do a quick duplicate game name check
 		for ( int iGame = 0; iGame < numGames; iGame++ )
 			for ( int iOther = iGame+1; iOther < numGames; iOther++ )
 				if ( plugin.games[iGame].getName().equals(plugin.games[iOther].getName()) )
 					plugin.log.warning("Two games (" + (iGame+1) + " & " + (iOther+1) + ") found with the same name: " + plugin.games[iGame].getName());
 		
 		return true;
 	}
 
 	private static void setupGame(KillerMinecraft plugin, Game game, LinkedHashMap<String, Object> config)
 	{
 		String name = getString(config, "name", "Unnamed game");
 		game.setName(name);
 		
 		// set the game mode
 		LinkedHashMap<String, Object> section = resolveConfigSection(config.get("mode"));
		String modeName = getString(section, "plugin", defaultGameMode);
 		
 		GameModePlugin modePlugin = GameMode.getByName(modeName);
 		if ( modePlugin == null )
 			modePlugin = GameMode.get(0);
 		game.setGameMode(modePlugin);
 		
 		// set the game mode's options, if any specified
 		setupOptionsFromConfig(game.getGameMode(), section);
 		
 		
 		// set the world option
 		section = resolveConfigSection(config.get("world"));
 		String worldName = getString(section, "name", defaultWorldGenerator);
 		
 		WorldGeneratorPlugin worldPlugin = WorldGenerator.getByName(worldName);
 		if ( worldPlugin == null )
 			worldPlugin = WorldGenerator.get(0);
 		game.setWorldGenerator(worldPlugin);
 		
 		//  set the world option's options, if any specified
 		setupOptionsFromConfig(game.getWorldGenerator(), section);
 		
 		
 		// now the game buttons
 		section = resolveConfigSection(config.get("buttons"));
 		if ( section == null )
 		{
 			plugin.log.warning("Can't find \"buttons\" section for " + game.getName());
 			return;
 		}
 				
 		Location joinButton = readLocation(section, "join", plugin.stagingWorld);
 		Location configButton = readLocation(section, "config", plugin.stagingWorld);
 		Location startButton = readLocation(section, "start", plugin.stagingWorld);
 		game.initButtons(joinButton, configButton, startButton);
 		
 		// and its signs
 		section = resolveConfigSection(config.get("signs"));
 		if ( section == null )
 		{
 			plugin.log.warning("Can't find \"signs\" section for " + game.getName());
 			return;
 		}
 		
 		Location statusSign = readLocation(section, "status", plugin.stagingWorld);
 		Location joinSign = readLocation(section, "join", plugin.stagingWorld);
 		Location configSign = readLocation(section, "config", plugin.stagingWorld);
 		Location startSign = readLocation(section, "start", plugin.stagingWorld);
 		game.initSigns(statusSign, joinSign, configSign, startSign);
 		
 		// the frames containing detailed info
 		section = resolveConfigSection(config.get("infoframes"));
 		if ( section == null )
 		{
 			plugin.log.warning("Can't find \"infoframes\" section for " + game.getName());
 			return;
 		}
 		
 		Location modeFrame = readLocation(section, "mode", plugin.stagingWorld);
 		Location miscFrame = readLocation(section, "misc", plugin.stagingWorld);
 		game.initFrames(modeFrame, miscFrame);
 		
 		// and lastly, the progress indicator 
 		section = resolveConfigSection(config.get("progressbar"));
 		if ( section != null )
 		{
 			Location start = readLocation(section, "start", plugin.stagingWorld);
 			String dir = getString(section, "dir", "+z");
 			int length = readInt(section, "length", 18);
 			int breadth = readInt(section, "breadth", 1);
 			int depth = readInt(section, "depth", 1);
 			game.initProgressBar(start, dir, length, breadth, depth);
 		}
 		
 		game.setGameState(GameState.stagingWorldSetup);
 	}
 
 	private static void setupOptionsFromConfig(KillerModule module, LinkedHashMap<String, Object> configSection)
 	{
 		LinkedHashMap<String, Object> options = resolveConfigSection(configSection.get("options"));
 		if ( options != null )
 		{
 			Iterator<Map.Entry<String, Object>> it = options.entrySet().iterator();
 			while ( it.hasNext() )
 			{
 				Map.Entry<String, Object> kvp = it.next();
 				String optionName = kvp.getKey();
 				
 				Option option = module.findOption(optionName);
 				if ( option == null )
 				{
 					KillerMinecraft.instance.log.warning(module.getName() + " has unrecognised option specified in config: " + optionName);
 					continue;
 				}
 				
 				String val = kvp.getValue().toString();
 				if ( val == null )
 				{
 					KillerMinecraft.instance.log.warning(module.getName() + " option \"" + optionName + "\" has no value");
 					continue;
 				}
 				option.setEnabled(Boolean.parseBoolean(val));
 			}
 		}
 	}
 	
 	static LinkedHashMap<String, Object> resolveConfigSection(Object config)
 	{
 		if ( config == null || !(config instanceof LinkedHashMap<?, ?>) )
 			return null;
 		
 		try
 		{
 			@SuppressWarnings("unchecked")
 			LinkedHashMap<String, Object> section = (LinkedHashMap<String, Object>)config;
 			return section;
 		}
 		catch ( ClassCastException ex )
 		{
 			return null;
 		}
 	}
 	
 	private static String getString(LinkedHashMap<String, Object> config, String key, String defaultVal)
 	{
 		Object o = config.get(key);
 		if ( o == null )
 			return defaultVal;
 
 		try
 		{
 			return (String)o;
 		}
 		catch ( ClassCastException ex )
 		{
 			KillerMinecraft.instance.log.warning("'" + key + "' is not a string, but it's supposed to be!");
 			return defaultVal;
 		}
 	}
 
 	private static int readInt(LinkedHashMap<String, Object> config, String key, int defaultVal)
 	{
 		Object o = config.get(key);
 		if ( o == null )
 			return defaultVal;
 
 		try
 		{
 			return (Integer)o;
 		}
 		catch ( ClassCastException ex )
 		{
 			KillerMinecraft.instance.log.warning("'" + key + "' is not an int, but it's supposed to be!");
 			return defaultVal;
 		}
 	}
 	
 	private static Location doReadLocation(String str, World world)
 	{
 		String[] parts = str.split(",", 3);
 		if ( parts.length != 3 )
 		{
 			KillerMinecraft.instance.log.warning("Invalid location in config: " + str);
 			return null;
 		}
 		
 		try
 		{
 			int x, y, z;
 			x = Integer.parseInt(parts[0].trim());
 			y = Integer.parseInt(parts[1].trim());
 			z = Integer.parseInt(parts[2].trim());
 			return new Location(world, x, y, z);
 		}
 		catch ( NumberFormatException ex )
 		{
 			KillerMinecraft.instance.log.warning("Invalid location in config: " + str);
 			return null;
 		}
 	}
 	
 	private static Location readLocation(FileConfiguration config, String name, World world, int defX, int defY, int defZ)
 	{
 		Location loc = readLocation(config, name, world);
 		return loc == null ? new Location(world, defX, defY, defZ) : loc;
 	}
 	
 	private static Location readLocation(FileConfiguration config, String name, World world)
 	{
 		String str = config.getString(name);
 		if ( str == null )
 			return null;
 		
 		return doReadLocation(str, world);
 	}
 
 	private static Location readLocation(LinkedHashMap<String, Object> config, String name, World world)
 	{
 		String str = getString(config, name, null);
 		if ( str == null )
 			return null;
 		
 		return doReadLocation(str, world);
 	}
 	
 	/*
 	private static Material[] readMaterialList(FileConfiguration config, String keyName, List<Integer> defaultValues, Material defaultOnError)
 	{
 		config.addDefault(keyName, defaultValues);
 	
 		List<Integer> itemIDs = config.getIntegerList(keyName); 
 		Material[] retVal = new Material[itemIDs.size()];
 		for ( int i=0; i<retVal.length; i++ )
 		{
 			Material mat = Material.getMaterial(itemIDs.get(i));
 			if ( mat == null )
 			{
 				mat = defaultOnError;
 				plugin.log.warning("Item ID " + itemIDs.get(i) + " not recognized in " + keyName + ".");
 			} 
 			retVal[i] = mat;
 		}
 		
 		return retVal;
 	}*/
 }
