 package com.ftwinston.Killer;
 
 /*
  * Killer Minecraft
  * a Minecraft Bukkit Mod by 
  * David "Canazza" McQuillan and
  * Andrew Winston "FTWinston" Winston Watkins AKA Winston
  * Created 18/06/2012
  */
 import java.io.File;
 import java.io.RandomAccessFile;
 import java.lang.ref.SoftReference;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import net.minecraft.server.Convertable;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.RegionFile;
 import net.minecraft.server.WorldLoaderServer;
 import net.minecraft.server.WorldType;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.CraftWorld;
 
 @SuppressWarnings("rawtypes")
 public class Killer extends JavaPlugin
 {
 	public void onEnable()
 	{
         instance = this;
         
 		getConfig().addDefault("autoAssign", false);
 		getConfig().addDefault("autoReveal", true);
 		getConfig().addDefault("restartDay", true);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		
 		autoAssignKiller = getConfig().getBoolean("autoAssign");
 		autoReveal = getConfig().getBoolean("autoReveal");
 		restartDayWhenFirstPlayerJoins = getConfig().getBoolean("restartDay");
 		
 		deadPlayers = new Vector<String>();
 		
         getServer().getPluginManager().registerEvents(eventListener, this);
         
 		// create a plinth in the default world. Always done with the same offset, so if the world already has a plinth, it should just get overwritten.
 		plinthPressurePlateLocation = createPlinth(getServer().getWorlds().get(0));
 		
 		
 		//defaultWorldName = getServer().getWorlds().get(0).getName();
         
         /*holdingWorld = getServer().getWorld(holdingWorldName);
         if ( holdingWorld == null )
         {
 	        WorldCreator wc = new WorldCreator(holdingWorldName);
 	        wc.generateStructures(false);
 	        wc.generator(new HoldingWorldGenerator());
 			wc.environment(Environment.THE_END);
 			holdingWorld = CreateWorld(wc, true);
         }*/
         
         seedGen = new Random();
         serverFolder = getServer().getWorldContainer();
         
         try {
         	Field a = net.minecraft.server.RegionFileCache.class.getDeclaredField("a");
         	a.setAccessible(true);
 			regionfiles = (HashMap) a.get(null);
 			rafField = net.minecraft.server.RegionFile.class.getDeclaredField("c");
 			rafField.setAccessible(true);
         	log.info("Successfully bound variable to region file cache.");
         	log.info("File references to unloaded worlds will be cleared!");
 		} catch (Throwable t) {
 			log.warning("Failed to bind to region file cache.");
 			log.warning("Files will stay referenced after being unloaded!");
 			t.printStackTrace();
 		}
 	}
 
 	private static HashMap regionfiles;
 	private static Field rafField;
 	
 	public static Killer instance;
 	static File serverFolder;
 	Random seedGen;
 	private Location plinthPressurePlateLocation;
 	
 	//World holdingWorld;
 	//String defaultWorldName, holdingWorldName = "holding";
 	
 	public void onDisable()
 	{
 		regionfiles = null;
 		rafField = null;
 		serverFolder = null;
 	}
 	
 	private final int absMinPlayers = 2;
 	private EventListener eventListener = new EventListener(this);
 	public boolean autoAssignKiller, autoReveal, restartDayWhenFirstPlayerJoins;
 	public Vector<String> deadPlayers;
 	
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (cmd.getName().equalsIgnoreCase("killer"))
 		{
 			if ( sender instanceof Player )
 			{
 				Player player = (Player)sender;
 				if ( !player.isOp() )
 				{
 					sender.sendMessage("Sorry, you must be an op to use this command.");
 					return true;
 				}
 			}
 			
 			if ( args.length > 0 )
 			{
 				if ( args[0].equalsIgnoreCase("assign") )
 				{
 					if  ( !assignKiller(sender) )
 						return true;
 				}
 				else if ( args[0].equalsIgnoreCase("reveal") )
 				{
 					revealKiller(sender);
 					return true;
 				}
 				else if ( args[0].equalsIgnoreCase("clear") )
 				{
 					clearKiller(sender);					
 					return true;
 				}
 				else if ( args[0].equalsIgnoreCase("restart") )
 				{
 					restartGame();
 					return true;
 				}
 			}
 
 			sender.sendMessage("Invalid command, available parameters are: assign, reveal, clear");
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean assignKiller(CommandSender sender)
 	{
 		Player[] players = getServer().getOnlinePlayers();
 		if ( players.length < absMinPlayers )
 		{
 			if ( sender != null )
 				sender.sendMessage("This game mode really doesn't work with fewer than " + absMinPlayers + " players. Seriously.");
 			return false;
 		}
 		
 		Random random = new Random();
 		int randomIndex = random.nextInt(players.length);
 		
 		for ( int i=0; i<players.length; i++ )
 		{
 			Player player = players[i];
 			if ( i == randomIndex )
 			{
 				killerName = player.getName();
 				player.sendMessage(ChatColor.RED + "You are the killer!");
 			}
 			else
 				player.sendMessage(ChatColor.YELLOW + "You are not the killer.");
 		}
 		
 		String senderName = sender == null ? "" : " by " + sender.getName();
 		getServer().broadcastMessage("A killer has been randomly assigned" + senderName + " - nobody but the killer knows who it is.");
 		return true;
 	}
 	
 	public void revealKiller(CommandSender sender)
 	{
 		if ( hasKillerAssigned() )
 		{
 			String senderName = sender == null ? "automatically" : "by " + sender.getName();
 			getServer().broadcastMessage(ChatColor.RED + "Revealed: " + killerName + " was the killer! " + ChatColor.WHITE + "(revealed " + senderName + ")");
 			
 			killerName = null;
 		}
 		else if ( sender != null )
 			sender.sendMessage("No killer has been assigned, nothing to reveal!");
 	}
 	
 	private void clearKiller(CommandSender sender)
 	{
 		if ( hasKillerAssigned() )
 		{
 			String senderName = sender == null ? "automatically" : "by " + sender.getName();
 			getServer().broadcastMessage(ChatColor.RED + "The killer has been cleared: there is no longer a killer! " + ChatColor.WHITE + "(cleared " + senderName + ")");
 			
 			Player killerPlayer = Bukkit.getServer().getPlayerExact(killerName);
 			if ( killerPlayer != null )
 				killerPlayer.sendMessage(ChatColor.YELLOW + "You are no longer the killer.");
 				
 			killerName = null;
 		}
 		else if ( sender != null )
 			sender.sendMessage("No killer has been assigned, nothing to clear!");
 	}
 
 	private String killerName = null;
 	protected int autoStartProcessID;
 
 	public boolean hasKillerAssigned()
 	{
 		return killerName != null;
 	}
 
 	public void cancelAutoStart()
 	{
 		if ( autoAssignKiller && autoStartProcessID != -1 )
     	{
     		Player[] players = getServer().getOnlinePlayers();
     		if ( players.length > 1 )
     			return; // only do this when the server is empty
     		
     		getServer().getScheduler().cancelTask(autoStartProcessID);
 			autoStartProcessID = -1;
     	}
 	}
 
 	public void playerKilled(String name)
 	{
 		boolean alreadyDead = false;
 		for ( int i=0; i<deadPlayers.size(); i++ )
 			if ( name.equals(deadPlayers.get(i)))
 			{
 				alreadyDead = true;
 				break;
 			}
 		
 		if ( !alreadyDead )
 		{
 			deadPlayers.add(name);
 			
 			// currently, we're banning players instead of setting them into some "observer" mode
 			Player player = Bukkit.getServer().getPlayerExact(name);
 			if (player != null)
 			{
 				player.setBanned(true);
 				player.kickPlayer("You were killed, and are banned until the end of the game");
 			}
 		}
 		
 		if ( !autoReveal )
 			return;
 		
 		int numSurvivors = 0;
 		Player[] players = getServer().getOnlinePlayers();
 		if ( players.length == 0 )
 		{
 			restartGame();
 			return;
 		}
 		
 		for ( int i=0; i<players.length; i++ )
 		{
 			boolean isDead = false;
 			for ( int j=0; j<deadPlayers.size(); j++ )
 				if ( players[i].getName().equals(deadPlayers.get(j)) )
 				{
 					isDead = true;
 					break;
 				}
 		
 			if ( !isDead )
 				numSurvivors ++;
 		}		
 		
 		if ( numSurvivors < 2 )
 			revealKiller(null);
 	}
 	
 	public void restartGame()
 	{	
 		// unban and kick everyone
 		for ( OfflinePlayer player : getServer().getBannedPlayers() )
 			player.setBanned(false);
 	
 		for ( Player player : getServer().getOnlinePlayers() )
 			player.kickPlayer("Server is regenerating the world. Rejoin for another game!");
 				
 		// first of all, ensure the spawn point is loaded
 		//World mainWorld = getServer().getWorlds().get(0);
 		
 		log.info("Clearing out old worlds...");
 		
 		List<World> worlds = getServer().getWorlds();
 				
 		//String[] worldNames = new String[worlds.size()-1];
 		//World.Environment[] worldTypes = new World.Environment[worlds.size()-1];
 
 		//for ( int i=0; i<worldNames.length; i++ )
 		for ( World world : worlds )
 		{
 			//worldNames[i] = worlds.get(i).getName();
 			//worldTypes[i] = worlds.get(i).getEnvironment();
 			
 			//World world = worlds.get(i);
 			forceUnloadWorld(world);
 			log.info("Unloaded " + world.getName());
 			
 			// now we want to try to delete the world folder
 			getServer().getScheduler().scheduleAsyncDelayedTask(this, new WorldDeleter(world.getName().toLowerCase()), 20);
 		}
 		
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run()
 			{
 				log.info("Generating new worlds...");
 				MinecraftServer ms = getMinecraftServer();
 
 				String s = ms.propertyManager.getString("level-name", "world");
 				String s2 = ms.propertyManager.getString("level-type", "DEFAULT");
 				WorldType worldtype = WorldType.getType(s2);
 
 				if (worldtype == null)
 					worldtype = WorldType.NORMAL;
 				
 				try
 				{
 					Method a = ms.getClass().getMethod("a", new Class[] { Convertable.class, String.class, long.class, WorldType.class });
 					a.invoke(ms, new WorldLoaderServer(ms.server.getWorldContainer()), s, seedGen.nextLong(), worldtype);
 				}
 				catch ( NoSuchMethodException ex)
 				{
 					log.warning("No such method: " + ex.getMessage());
 				}
 				catch ( IllegalAccessException ex )
 				{
 					log.warning("Illegal access: " + ex.getMessage());
 				}
 				catch ( IllegalArgumentException ex )
 				{
 					log.warning("Illegal argument: " + ex.getMessage());
 				}
 				catch ( InvocationTargetException ex )
 				{
 					log.warning("Invocation target exception: " + ex.getMessage());
 				}
 			}
 		}, 60);
 		
 		// rearrange the world list so that they're in the right order!
 		
 		// create a plinth in the new default world
 		plinthPressurePlateLocation = createPlinth(getServer().getWorlds().get(0));
 		
 		
 		/*
 		// now want to create new worlds, with the same names and types as we had before
 		// ... hopefully this will keep the default settings (generate structures, etc)
 		for ( int i=0; i<worldNames.length; i++ )
 		{
 			WorldCreator wc = new WorldCreator(worldNames[i]);
 			wc.environment(worldTypes[i]);
 			wc.seed(seedGen.nextLong());
 			//if ( i == 0 )
 				//wc.generator(new HoldingWorldGenerator());
 			CreateWorld(wc, i == 0);
 		}*/
 	}
 	
 	class WorldDeleter implements Runnable
     {
     	String name;
     	public WorldDeleter(String worldName) { name = worldName; }
     	
     	public void run()
     	{
     		try
 			{
     			if ( !delete(new File(serverFolder + File.separator + name)) )
 					log.info("Unable to delete " + name + "'s folder");
 			}
 			catch ( Exception e )
 			{
 				log.info("An error occurred when deleting the " + name + " world: " + e.getMessage());
 			}
     	}
     }
 	/*
 	private World CreateWorld(WorldCreator wc, boolean keepSpawnInMemory)
 	{
 		World world = getServer().createWorld(wc);
 		//getServer().getWorlds().add(world);
 		
 		if (world != null)
 		{
 			final int keepdimension = 7;
 			int spawnx = world.getSpawnLocation().getBlockX() >> 4;
 		    int spawnz = world.getSpawnLocation().getBlockZ() >> 4;
 			for (int x = -keepdimension; x < keepdimension; x++)
 			{
 				for (int z = -keepdimension; z < keepdimension; z++) {
 					world.loadChunk(spawnx + x, spawnz + z);
 				}
 			}
 			
 			world.setKeepSpawnInMemory(keepSpawnInMemory);
 			log.info("World '" + world.getName() + "' has been created successfully!");
 		}
 		else
 			log.info("World creation failed!");
 		
 		return world;
 	}*/
 
 	private static boolean delete(File folder)
 	{
 		if ( !folder.exists() )
 			return true;
 		boolean retVal = true;
 		if (folder.isDirectory())
 			for (File f : folder.listFiles())
 				if (!delete(f))
 				{
 					retVal = false;
 					instance.log.info("Failed to delete file: " + f.getName());
 				}
 		return folder.delete() && retVal;
 	}
 /*
 	public void moveToHoldingWorld(Player player)
 	{
 		Location loc = new Location(holdingWorld, 8, 2, 8);
 		player.teleport(loc);		
 	}
 	*/
 	public void moveToMainWorld(Player player)
 	{
 		World world = getServer().getWorlds().get(0);
		if ( !world.isChunkLoaded(0, 0) )
			world.loadChunk(0,0);
		Location loc = new Location(world, 8, world.getHighestBlockYAt(8, 8), 8);
		player.teleport(loc);
 	}
 	
 	public static boolean clearWorldReference(World world) {
 		String worldname = world.getName();
 		if (regionfiles == null) return false;
 		if (rafField == null) return false;
 		ArrayList<Object> removedKeys = new ArrayList<Object>();
 		try {
 			for (Object o : regionfiles.entrySet()) {
 				Map.Entry e = (Map.Entry) o;
 				File f = (File) e.getKey();
 				if (f.toString().startsWith("." + File.separator + worldname)) {
 					SoftReference ref = (SoftReference) e.getValue();
 					try {
 						RegionFile file = (RegionFile) ref.get();
 						if (file != null) {
 							RandomAccessFile raf = (RandomAccessFile) rafField.get(file);
 							raf.close();
 							removedKeys.add(f);
 						}
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 			}
 		} catch (Exception ex) {
 			instance.log.warning("Exception while removing world reference for '" + worldname + "'!");
 			ex.printStackTrace();
 		}
 		for (Object key : removedKeys) {
 			regionfiles.remove(key);
 		}
 		return true;
 	}
 	
 	private MinecraftServer getMinecraftServer()
 	{
 		try
 		{
 			CraftServer server = (CraftServer)getServer();
 			Field f = server.getClass().getDeclaredField("console");
 			f.setAccessible(true);
 			MinecraftServer console = (MinecraftServer)f.get(server);
 			f.setAccessible(false);
 			return console;}
 		catch ( IllegalAccessException ex )
 		{
 		}
 		catch  ( NoSuchFieldException ex )
 		{
 		}
 		
 		return null;
 	}
 	
 	public void forceUnloadWorld(World world)
 	{
 		for ( Player player : world.getPlayers() )
 			player.kickPlayer("World is being regenerated... and you were in it!");
 		
 		CraftServer server = (CraftServer)getServer();
 		CraftWorld craftWorld = (CraftWorld)world;
 		
 		try
 		{
 			Field f = server.getClass().getDeclaredField("worlds");
 			f.setAccessible(true);
 			@SuppressWarnings("unchecked")
 			Map<String, World> worlds = (Map<String, World>)f.get(server);
 			worlds.remove(world.getName().toLowerCase());
 			f.setAccessible(false);
 		}
 		catch ( IllegalAccessException ex )
 		{
 			log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 		}
 		catch  ( NoSuchFieldException ex )
 		{
 			log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 		}
 		
 		MinecraftServer ms = getMinecraftServer();
 		ms.worlds.remove(ms.worlds.indexOf(craftWorld.getHandle()));
 		
         clearWorldReference(world);
 	}
 	
 	final int plinthHeightAboveSurroundings = 6, spaceBetweenPlinthAndGlowstone = 4;
 	final int plinthSpawnOffsetX = 20, plinthSpawnOffsetZ = 0;
 	public static Location createPlinth(World world)
 	{
 		Location spawnPoint = world.getSpawnLocation();
 		int x = spawnPoint.getBlockX() + plinthSpawnOffsetX;
 		int z = spawnPoint.getBlockZ() + plinthSpawnOffsetZ;
 	
 		int yPeak = Math.max(world.getHighestBlockYAt(x, z), world.getHighestBlockYAt(x, z+1), world.getHighestBlockYAt(x, z-1),
 		world.getHighestBlockYAt(x+1, z), world.getHighestBlockYAt(x+1, z+1), world.getHighestBlockYAt(x+1, z-1),
 		world.getHighestBlockYAt(x-1, z), world.getHighestBlockYAt(x-1, z+1), world.getHighestBlockYAt(x-1, z-1))
 		+ plinthHeightAboveSurroundings;
 		
 		// a 3x3 column from bedrock to the plinth height
 		for ( int y = 0; y < yPeak; y++ )
 			for ( int ix = x - 1; ix < x + 2; ix++ )
 				for ( int iz = z - 1; iz < z + 2; iz++ )
 				{
 					Block b = world.getBlockAt(ix, y, iz);
 					b.setType(Material.BEDROCK);
 				}
 		
 		// with one block sticking up from it
 		int y = yPeak;
 		for ( int ix = x - 1; ix < x + 2; ix++ )
 				for ( int iz = z - 1; iz < z + 2; iz++ )
 				{
 					Block b = world.getBlockAt(ix, y, iz);
 					b.setType(ix == x && iz == z ? Material.BEDROCK : Material.AIR);
 				}
 		
 		// that has a pressure plate on it
 		int y = yPeak + 1;
 		Location pressurePlateLocation = new Location(world, x, y, z);
 		for ( int ix = x - 1; ix < x + 2; ix++ )
 				for ( int iz = z - 1; iz < z + 2; iz++ )
 				{
 					Block b = world.getBlockAt(ix, y, iz);
 					b.setType(ix == x && iz == z ? Material.STONE_PLATE : Material.AIR);
 				}
 				
 		// then a space
 		for ( int y=yPeak + 2; y <= yPeak + spaceBetweenPlinthAndGlowstone; y++ )
 			for ( int ix = x - 1; ix < x + 2; ix++ )
 				for ( int iz = z - 1; iz < z + 2; iz++ )
 				{
 					Block b = world.getBlockAt(ix, y, iz);
 					b.setType(Material.AIR);
 				}
 		
 		// and then a 1x1 pillar of glowstone, up to max height
 		for ( int y=yPeak + spaceBetweenPlinthAndGlowstone + 1; y < world.getMaxHeight(); y++ )
 			for ( int ix = x - 1; ix < x + 2; ix++ )
 				for ( int iz = z - 1; iz < z + 2; iz++ )
 				{
 					Block b = world.getBlockAt(ix, y, iz);
 					b.setType(ix == x && iz == z ? Material.GLOWSTONE : Material.AIR);
 				}
 		
 		return pressurePlateLocation;
 	}
 }
