 package net.virtuallyabstract.minecraft;
 
 import org.bukkit.plugin.java.*;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.server.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.command.*;
 import org.bukkit.scheduler.*;
 import com.nijikokun.register.payment.*;
 import com.nijikokun.register.payment.Method.MethodAccount;
 import org.bukkit.plugin.Plugin;
 import java.util.logging.*;
 import java.util.concurrent.*;
 import java.util.*;
 import java.io.*;
 
 public class DungeonBuilder extends JavaPlugin
 {
 	private static Logger myLogger;
 	public static String dungeonRoot = "plugins/dungeons";
 	public static boolean proximityCheck = true, enableSuperperms = true;
 	public static boolean dontSaveBlocks = false;
 	public static Event.Priority respawnPriority = Event.Priority.Normal;
 	public Server server;
 
 	public DungeonManager dungeonManager;
 	public ConcurrentHashMap<String, ArrayList<Dungeon>> dungeonMap;
 	public ConcurrentHashMap<String, Dungeon> inDungeons;
 	public ConcurrentHashMap<String, DungeonParty> inParty, pendingInvites;
 	public ConcurrentHashMap<String, Long> idleTimer;
 	private ConcurrentHashMap<String, LocationWrapper> activeSavePoints;
 	private HashMap<String, CommandBuilder> commandBuilders;
 	private HashMap<String, DungeonMarker> activeMarkers;
 	private MyServerListener economyListener;
 	private DBPlayerListener playerListener;
 	public BukkitScheduler scheduler = null;
 	private static final Long idleTimeout = 60000L;
 
 	static
 	{
 		myLogger = Logger.getLogger("minecraft");	
 		loadConfig();
 	}
 
 	private static void loadConfig()
 	{
 		File configFile = new File("plugins/dungeonbuilder.cfg");
 		if(!configFile.exists())
 		{
 			File oldConfig = new File("dungeonbuilder.cfg");
 			if(oldConfig.exists())
 			{
 				oldConfig.renameTo(configFile);
 			}
 			else
 			{
 				createDefaultConfig();
 				return;
 			}
 		}
 
 		if(!configFile.exists())
 			return;
 
 		BufferedReader br = null;
 		try
 		{
 			br = new BufferedReader(new FileReader(configFile));
 			String line = null;
 			while((line = br.readLine()) != null)
 			{
 				line = line.trim();
 				if(line.startsWith("rootFolder="))
 				{
 					String temp = line.substring(11);	
 					if(temp.endsWith("/"))
 						temp = temp.substring(0, temp.length()-2);
 
 					File test = new File(temp);
 					if(test.isDirectory())
 					{
 						myLogger.log(Level.INFO, "DungeonBuilder - Setting dungeon root folder to: " + temp);
 						dungeonRoot = temp;
 					}
 				}
 				if(line.startsWith("disableProximityChecks="))
 				{
 					String temp = line.substring(23);
 					if(temp.toLowerCase().equals("true"))
 						proximityCheck = false;
 				}
 				if(line.startsWith("enableSuperperms="))
 				{
 					String temp = line.substring(17);
 					System.out.println("superperms: " + temp);
 					if(temp.toLowerCase().equals("false"))
 						enableSuperperms = false;
 				}
 				if(line.startsWith("setRespawnPriority="))
 				{
 					String temp = line.substring(19);
 					try
 					{
 						respawnPriority = Event.Priority.valueOf(temp);
 					}
 					catch(Exception e)
 					{
 						System.out.println("Invalid respawn priority: " + temp);
 						respawnPriority = Event.Priority.Normal;
 					}
 				}
 				if(line.startsWith("dontSaveBlocks="))
 				{
 					String temp = line.substring(15);
 					if(temp.toLowerCase().equals("true"))
 						dontSaveBlocks = true;
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			myLogger.log(Level.WARNING, "DungeonBuilder - Error reading configuration file");
 			e.printStackTrace();
 		}
 		finally
 		{
 			try
 			{
 				if(br != null)
 				{
 					br.close();
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static void createDefaultConfig()
 	{
 		PrintWriter pw = null;
 		try
 		{
 			pw = new PrintWriter(new FileWriter("plugins/dungeonbuilder.cfg"));
 
 			File dungeonDir = new File("dungeons");
 			if(dungeonDir.isDirectory())
 			{
 				pw.print("rootFolder=dungeons\n");
 				dungeonRoot = "dungeons";
 			}
 			else
 				pw.print("rootFolder=" + dungeonRoot + "\n");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			try
 			{
 				if(pw != null)
 					pw.close();
 			}
 			catch(Exception e2)
 			{
 				e2.printStackTrace();
 			}
 		}
 
 	}
 
 	@Override public void onEnable()
 	{
 		myLogger.log(Level.INFO, "DungeonBuilder (v.0.8.2) Enabled");
 
 		server = this.getServer();
 
 		dungeonMap = new ConcurrentHashMap<String, ArrayList<Dungeon>>();
 		loadDungeons(server);
 		dungeonManager = new DungeonManager(server, dungeonMap);
 		inDungeons = new ConcurrentHashMap<String, Dungeon>();
 		inParty = new ConcurrentHashMap<String, DungeonParty>();
 		idleTimer = new ConcurrentHashMap<String, Long>();
 		pendingInvites = new ConcurrentHashMap<String, DungeonParty>();
 		activeSavePoints = new ConcurrentHashMap<String, LocationWrapper>();
 		activeMarkers = new HashMap<String, DungeonMarker>();
 		commandBuilders = new HashMap<String, CommandBuilder>();
 
 		PluginManager pm = server.getPluginManager();
 		scheduler = server.getScheduler();
 
 		playerListener = new DBPlayerListener(this);	
 		MyBlockListener blistener = new MyBlockListener();
 		MyEntityListener elistener = new MyEntityListener();
 		economyListener = new MyServerListener();
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, economyListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, economyListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, respawnPriority, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blistener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blistener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.CREATURE_SPAWN, elistener, Event.Priority.Normal, this);
 	}
 
 	@Override public void onDisable()
 	{
 		myLogger.log(Level.INFO, "DungeonBuilder - Teleporting players out of dungeons");
 		for(String key : inDungeons.keySet())
 		{
 			Player player = server.getPlayer(key);
 			if(player == null)
 				continue;
 
 			Location l = player.getCompassTarget();
 			World world = player.getWorld();
 			world.loadChunk(l.getBlockX(), l.getBlockZ());
 			player.teleport(l);
 			
 			inDungeons.remove(player.getName());
 		}
 
 		for(String key : dungeonMap.keySet())
 		{
 			for(Dungeon d : dungeonMap.get(key))
 			{
 				d.killMonsters();
 				d.clearEntities();
 			}
 		}
 
 		myLogger.log(Level.INFO, "DungeonBuilder Disabled");
 	}
 
 	@Override public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String [] args)
 	{
 		if(!(sender instanceof Player))
 		{
 			sender.sendMessage("This command is only available to players");	
 			return true;
 		}
 
 		Player player = (Player)sender;
 		String playername = player.getName();
 
 		if(label.equals("db"))
 		{
 			if(!commandBuilders.containsKey(playername))
 				commandBuilders.put(playername, new CommandBuilder());
 
 			CommandBuilder cb = commandBuilders.get(playername);
 			cb.reset();
 
 			for(String arg : args)
 			{
 				cb.update(player, arg, false);
 			}
 
 			if(!cb.isDone())
 			{
 				cb.nextPrompt(player);
 				return true;
 			}
 			else
 			{
 				label = cb.getCommand();
 				args = cb.getArgs();
 			}
 		}
 
 		if((label.equals("createemptydungeon") || label.equals("createdungeon")) && checkPermission(player, "dungeonbuilder.dungeons.create"))
 		{
 			if(args.length < 4)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			boolean hollow = false;
 			if(label.equals("createemptydungeon"))
 				hollow = true;
 
 			String name = args[0];
 			try
 			{
 				int width = Integer.parseInt(args[1]);
 				int depth = Integer.parseInt(args[2]);
 				int height = Integer.parseInt(args[3]);
 
 				Dungeon d = null;
 				if(args.length == 5)
 				{
 					String material = args[4].toUpperCase();
 					if(material.equals("NONE"))
 					{
 						d = createDungeon(name, playername, width, depth, height, -1, hollow);
 					}
 					else
 					{
 						Material m = Material.valueOf(material);
 						if(m != null)
 							d = createDungeon(name, playername, width, depth, height, m.getId(), hollow);
 						else
 						{
 							sender.sendMessage("Invalid material type");
 							return true;
 						}
 					}
 				}
 				else
 					d = createDungeon(name, playername, width, depth, height, hollow);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage("Dungeon creation failed - " + e.getMessage());
 				return false;
 			}
 		}
 
 		if(label.equals("createworld") && checkPermission(player, "dungeonbuilder.worlds.create"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			WorldCreator wc = new WorldCreator(args[0]);
 			wc.environment(World.Environment.valueOf(args[1]));
 			server.createWorld(wc);
 
 			sender.sendMessage("World created.");
 		}
 
 		if(label.equals("teleporttoworld") && checkPermission(player, "dungeonbuilder.worlds.teleport"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			World w = server.getWorld(args[0]);
 			if(w == null)
 			{
 				sender.sendMessage("The world '" + args[0] + "' does not exist");
 				return true;
 			}
 
 			player.teleport(w.getSpawnLocation());
 			inDungeons.remove(playername);
 		}
 
 		if(label.equals("teleporttodungeon") && checkPermission(player, "dungeonbuilder.dungeons.teleport"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			try
 			{
 
 				teleportToDungeon(args[0], playername);
 
 				if(inDungeons.containsKey(playername) && !inDungeons.get(playername).getName().equals(args[0]))
 					inDungeons.remove(playername);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage(e.getMessage());
 				return true;
 			}
 		}
 
 		if(label.equals("teleporttodungeoncenter") && checkPermission(player, "dungeonbuilder.dungeons.teleport"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			try
 			{
 				teleportToDungeonCenter(args[0], playername);
 
 				if(inDungeons.containsKey(playername) && !inDungeons.get(playername).getName().equals(args[0]))
 					inDungeons.remove(playername);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage(e.getMessage());
 				return true;
 			}
 		}
 
 		if(label.equals("teleportoutsidedungeon") && checkPermission(player, "dungeonbuilder.dungeons.teleport"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			try
 			{
 				teleportOutsideDungeon(args[0], playername);
 				inDungeons.remove(playername);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage(e.getMessage());
 				return true;
 			}
 		}
 
 		if(label.equals("listdungeons") && checkPermission(player, "dungeonbuilder.dungeons.query"))
 		{
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			StringBuffer list = new StringBuffer("");
 			for(Dungeon d : new TreeSet<Dungeon>(dungeonMap.get(playername)))
 			{
 				if(list.length() > 0)
 					list.append(", ");
 				list.append(d.getName());
 			}
 
 			sender.sendMessage(list.toString());
 		}
 
 		if(label.equals("savedungeon") && checkPermission(player, "dungeonbuilder.dungeons.save"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d != null)
 			{
 				try
 				{
 					d.saveDungeon();
 					sender.sendMessage("Dungeon saved");
 				}
 				catch(Exception e)
 				{
 					sender.sendMessage("Failed to save dungeon: " + e.getMessage());
 					e.printStackTrace();
 				}
 
 				return true;
 			}
 
 			sender.sendMessage("The dungeon '" + name + "' does not exist for that player");
 		}
 
 		if(label.equals("loaddungeon") && checkPermission(player, "dungeonbuilder.dungeons.load"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d != null)
 			{
 				d.loadDungeon();
 				sender.sendMessage("Dungeon loaded");
 				return true;
 			}
 
 			sender.sendMessage("The dungeon '" + name + "' does not exist for that player");
 		}
 
 		if(label.equals("setdungeonstart") && checkPermission(player, "dungeonbuilder.dungeons.setstart"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("The dungeon '" + name + "' does not exist for that player");
 				return true;
 			}
 
 			if(!d.containsLocation(player.getLocation()))
 			{
 				sender.sendMessage("You must be inside the dungeon to set the starting location");
 				return true;
 			}
 
 			dungeonManager.removeDungeon(d);
 			d.setStartingLocation(player.getLocation());
 			dungeonManager.addDungeon(d);
 			sender.sendMessage("Starting location updated for dungeon '" + name + "'");
 		}
 
 		if(label.equals("setdungeonexit") && checkPermission(player, "dungeonbuilder.dungeons.setexit"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("The dungeon '" + name + "' does not exist for that player");
 				return true;
 			}
 
 			if(!d.containsLocation(player.getLocation()))
 			{
 				sender.sendMessage("You must be inside the dungeon to set the exit location");
 				return true;
 			}
 
 			dungeonManager.removeDungeon(d);
 			d.setExitLocation(player.getLocation());
 			dungeonManager.addDungeon(d);
 			sender.sendMessage("Exit location updated for dungeon '" + name + "'");
 		}
 
 		if(label.equals("setdungeonexitdestination") && checkPermission(player, "dungeonbuilder.dungeons.setexit"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("The dungeon '" + name + "' does not exist for that player");
 				return true;
 			}
 
 			if(isPlayerInADungeon(player))
 			{
 				sender.sendMessage("You cannot set the exit destination from inside a dungeon");
 				sender.sendMessage("Trying to chain dungeons together?  I recommend setting the entry teleporter for the second dungeon in the first");
 				return true;
 			}
 
 			dungeonManager.removeDungeon(d);
 			try
 			{
 				d.setExitDestination(player.getLocation());
 				dungeonManager.addDungeon(d);
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				sender.sendMessage("An error occurred while trying to save the changes to the dungeon");
 			}
 			sender.sendMessage("Exit destination location updated for dungeon '" + name + "'");
 		}
 
 		if(label.equals("deletedungeon") && checkPermission(player, "dungeonbuilder.dungeons.delete"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + player + "' does not currently own any dungeons");
 				return true;
 			}
 
 			boolean clear = false;
 			if(args.length == 2)
 			{
 				try
 				{
 					clear = Boolean.parseBoolean(args[1].toLowerCase());
 				}
 				catch(Exception e)
 				{
 					sender.sendMessage("Invalid second argument");
 					return false;
 				}
 			}
 
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("The dungeon '" + name + "' does not exist for player '" + playername + "'");
 				return true;
 			}
 
 			String key1 = WorldUtils.createLocationKey(d.getStartingLocation());
 			String key2 = WorldUtils.createLocationKey(player.getLocation());
 			if(!key1.equals(key2))
 			{
 				sender.sendMessage("You must be standing on the starting location to delete the dungeon");
 				return true;
 			}
 
 			if(!deleteDungeon(name, player, clear))
 				sender.sendMessage("Failed to delete dungeon '" + name + "' for player '" + playername + "'");
 			else
 			{
 				for(String key : inDungeons.keySet())
 				{
 					if(inDungeons.get(key).equals(d))
 						inDungeons.remove(key);
 				}
 				sender.sendMessage("Dungeon deleted.");
 			}
 		}
 
 		if(label.equals("cleardungeon") && checkPermission(player, "dungeonbuilder.dungeons.clear"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Failed to locate dungeon '" + name + "'");
 				return true;
 			}
 			d.clearDungeon();
 		}
 
 		if(label.equals("cleartorches") && checkPermission(player, "dungeonbuilder.dungeons.clear"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Failed to locate dungeon '" + name + "'");
 				return true;
 			}
 			d.clearTorches();
 		}
 
 		if(label.equals("clearliquids") && checkPermission(player, "dungeonbuilder.dungeons.clear"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Failed to locate dungeon '" + name + "'");
 				return true;
 			}
 			d.clearLiquids();
 		}
 
 		if(label.equals("publishdungeon") && checkPermission(player, "dungeonbuilder.dungeons.publish"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Failed to locate dungeon '" + name + "'");
 				return true;
 			}
 
 			if(d.isPublished())
 			{
 				sender.sendMessage("The dungeon '" + name + "' is already published");
 				return true;
 			}
 
 			if(d.containsLocation(player.getLocation()))
 			{
 				sender.sendMessage("You cannot publish a dungeon from inside itself");
 				return true;
 			}
 
 			if(d != null)
 			{
 				if(d.getStartingLocation() == null)
 				{
 					sender.sendMessage("The starting location must be set before you can publish the dungeon");
 					return true;
 				}
 				if(d.getExitLocation() == null)
 				{
 					sender.sendMessage("The exit location must be set before you can publish the dungeon");
 					return true;
 				}
 
 				try
 				{
 					d.publish(player.getLocation());
 				}
 				catch(Exception e)
 				{
 					sender.sendMessage("Failed to publish dungeon - " + e.getMessage());
 					return true;
 				}
 				dungeonManager.addDungeon(d, true);
 				sender.sendMessage("The dungeon '" + name + "' is now available to normal users");
 			}
 			else
 				sender.sendMessage("The dungeon '" + name + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("unpublishdungeon") && checkPermission(player, "dungeonbuilder.dungeons.unpublish"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String name = args[0];
 			if(!dungeonMap.containsKey(playername))
 			{
 				sender.sendMessage("The player '" + playername + "' does not currently own any dungeons");
 				return true;
 			}
 
 			Dungeon d = lookupDungeon(name, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Failed to locate dungeon '" + name + "'");
 				return true;
 			}
 
 			if(!d.isPublished())
 			{
 				sender.sendMessage("The dungeon '" + name + "' is already unpublished");
 				return true;
 			}
 
 			try
 			{
 				for(String key : inDungeons.keySet())
 				{
 					if(inDungeons.get(key).equals(d))
 					{
 						Player p = server.getPlayer(key);
 						if(p != null)
 						{
 							p.teleport(d.getExitDestination());		
 							inDungeons.remove(key);
 						}
 					}
 				}
 				d.unpublish();
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage("The dungeon was unpublished but an error occurred - " + e.getMessage());
 			}
 			dungeonManager.removeDungeon(d);
 			sender.sendMessage("The dungeon '" + name + "' is no longer available to normal users");
 		}
 
 		if(label.equals("teleporttospawn") && checkPermission(player, "dungeonbuilder.worlds.teleport"))
 		{
 			Location l = player.getCompassTarget();
 			World world = player.getWorld();
 			world.loadChunk(l.getBlockX(), l.getBlockZ());
 			player.teleport(l);
 			inDungeons.remove(playername);
 		}
 
 		if(label.equals("addmonsterspawn") && checkPermission(player, "dungeonbuilder.dungeons.addmonster"))
 		{
 			if(args.length < 3)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String type = args[1].toUpperCase();
 			String dungeon = args[2];
 			int count = 1;
 
 			try
 			{
 				if(args.length == 4)
 					count = Integer.parseInt(args[3]);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage("Invalid argument: " + args[3]);
 				return true;
 			}
 
 			try
 			{
 				CreatureType.valueOf(type);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage("Invalid creature type: " + type);
 			}
 
 			Dungeon d = lookupDungeon(dungeon, playername);
 			if(d != null)
 			{
 				//if(!d.containsLocation(player.getLocation()))
 				//{
 				//	sender.sendMessage("You cannot set a monster spawn from outside the dungeon");
 				//	return true;
 				//}
 
 				dungeonManager.removeDungeon(d);
 				if(d.addMonster(player.getLocation(), alias, type, count))
 					sender.sendMessage("Monster spawnpoint added");
 				else
 					sender.sendMessage("Monster spawnpoint already exists for that alias");
 				dungeonManager.addDungeon(d);
 			}
 			else
 				sender.sendMessage("The dungeon '" + dungeon + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("removemonsterspawn") && checkPermission(player, "dungeonbuilder.dungeons.removemonster"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String dungeon = args[1];
 
 			Dungeon d = lookupDungeon(dungeon, playername);
 			if(d != null)
 			{
 				dungeonManager.removeDungeon(d);
 				if(d.removeMonster(alias))
 					sender.sendMessage("Monster spawnpoint removed");
 				else
 					sender.sendMessage("There is no monster spawnpoint with that alias");
 				dungeonManager.addDungeon(d);
 			}
 			else
 				sender.sendMessage("The dungeon '" + dungeon + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("listmonsterspawns") && checkPermission(player, "dungeonbuilder.dungeons.query"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String dungeon = args[0];
 
 			Dungeon d = lookupDungeon(dungeon, playername);
 			if(d != null)
 				sender.sendMessage(d.listMonsters());
 			else
 				sender.sendMessage("The dungeon '" + dungeon + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("spawnmonsters") && checkPermission(player, "dungeonbuilder.dungeons.spawnmonsters"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String dungeon = args[0];
 
 			Dungeon d = lookupDungeon(dungeon, playername);
 			if(d != null)
 			{
 				try
 				{
 					d.spawnMonsters();
 					sender.sendMessage("Monsters spawned");
 				}
 				catch(Exception e)
 				{
 					sender.sendMessage("Failed to spawn monsters - " + e.getMessage());
 				}
 			}
 			else
 				sender.sendMessage("The dungeon '" + dungeon + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("killmonsters") && checkPermission(player, "dungeonbuilder.dungeons.killmonsters"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String dungeon = args[0];
 
 			Dungeon d = lookupDungeon(dungeon, playername);
 			if(d != null)
 			{
 				d.killMonsters();
 				sender.sendMessage("Monsters killed");
 			}
 			else
 				sender.sendMessage("The dungeon '" + dungeon + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("viewdungeonparameters") && checkPermission(player, "dungeonbuilder.dungeons.query"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String dungeon = args[0];
 			Dungeon d = lookupDungeon(dungeon, playername);
 			if(d != null)
 			{
 				sender.sendMessage("Dungeon: " + dungeon);
 				sender.sendMessage("Width: " + d.getWidth());
 				sender.sendMessage("Length: " + d.getDepth());
 				sender.sendMessage("Height: " + d.getHeight());
 			}
 			else
 				sender.sendMessage("The dungeon '" + dungeon + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("setdungeonreward") && checkPermission(player, "dungeonbuilder.dungeons.setreward"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String dungeon = args[0];
 			Dungeon d = lookupDungeon(dungeon, playername);
 			if(d != null)
 			{
 				Double amount = 0.0;
 				try
 				{
 					amount = Double.parseDouble(args[1]);
 				}
 				catch(Exception e)
 				{
 					sender.sendMessage("Invalid format for reward amount.  Expecting a double amount");
 					return false;
 				}
 
 				d.setDungeonReward(amount);
 				sender.sendMessage("Dungeon reward for '" + dungeon + "' set to '" + amount + "'");
 			}
 			else
 				sender.sendMessage("The dungeon '" + dungeon + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("showdungeonreward") && checkPermission(player, "dungeonbuilder.dungeons.query"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String dungeon = args[0];
 			Dungeon d = lookupDungeon(dungeon, playername);
 			if(d != null)
 			{
 				double amount = d.getDungeonReward();
 				sender.sendMessage("The current reward for '" + dungeon + "' is " + amount + " credits");
 			}
 			else
 				sender.sendMessage("The dungeon '" + dungeon + "' does not exist for player '" + playername + "'");
 		}
 
 		if(label.equals("playerisnotindungeon") && checkPermission(player, "dungeonbuilder.dungeons.admin"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			playername = args[0];
 			inDungeons.remove(playername);
 		}
 
 		if(label.equals("setfirstdungeonmarker") && checkPermission(player, "dungeonbuilder.dungeons.create"))
 		{
 			if(!activeMarkers.containsKey(playername))
 				activeMarkers.put(playername, new DungeonMarker());
 
 			DungeonMarker dm = activeMarkers.get(playername);
 			dm.setFirstMarker(player.getLocation());
 			sender.sendMessage("First marker set");
 		}
 
 		if(label.equals("setseconddungeonmarker") && checkPermission(player, "dungeonbuilder.dungeons.create"))
 		{
 			if(!activeMarkers.containsKey(playername))
 				activeMarkers.put(playername, new DungeonMarker());
 
 			DungeonMarker dm = activeMarkers.get(playername);
 			dm.setSecondMarker(player.getLocation());
 			sender.sendMessage("Second marker set");
 		}
 
 		if(label.equals("createdungeonfrommarkers") && checkPermission(player, "dungeonbuilder.dungeons.create"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			if(!activeMarkers.containsKey(playername))
 			{
 				sender.sendMessage("Markers must be set before making a dungeon from them");
 				return true;
 			}
 
 			DungeonMarker dm = activeMarkers.get(playername);
 			Location l1 = dm.getFirstMarker();
 			Location l2 = dm.getSecondMarker();
 
 			if(l1 == null)
 			{
 				sender.sendMessage("You are missing the first marker");
 				return true;
 			}
 
 			if(l2 == null)
 			{
 				sender.sendMessage("You are missing the second marker");
 				return true;
 			}
 
 			int depth = (int)Math.abs(l1.getX() - l2.getX());
 			int width = (int)Math.abs(l1.getZ() - l2.getZ());
 			int height = (int)Math.abs(l1.getY() - l2.getY());
 
 			height--;
 
 			double minY = l1.getY();
 			if(l2.getY() < minY)
 				minY = l2.getY();
 
 			int depthDirection = -1;
 			if(l1.getX() < l2.getX())
 				depthDirection = 1;
 
 			int widthDirection = -1;
 			if(l1.getZ() < l2.getZ())
 				widthDirection = 1;
 
 			Location center = l1.clone();
 			center.setX(center.getX() + (depthDirection * (depth / 2)));
 			center.setZ(center.getZ() + (widthDirection * (width / 2)));
 			center.setY(minY + 1.0);
 
 			try
 			{
 				Dungeon d = null;
 				if(args.length == 2)
 				{
 					if(args[1].toUpperCase().equals("NONE"))
 						d = createDungeon(alias, playername, center, width, depth, height, -1, false);
 					else
 					{
 						Material m = Material.valueOf(args[1].toUpperCase());
 						if(m != null)
 							d = createDungeon(alias, playername, center, width, depth, height, m.getId(), false);
 						else
 						{
 							sender.sendMessage("Invalid material type");
 							return true;
 						}
 					}
 				}
 				else
 					d = createDungeon(alias, playername, center, width, depth, height, 7, false);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage("Dungeon creation failed - " + e.getMessage());
 				return true;
 			}
 		}
 
 		if(label.equals("undodungeoncreation") && checkPermission(player, "dungeonbuilder.dungeons.create"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];	
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Could not find the dungeon '" + alias + "'");
 				return true;
 			}
 
 			if(d.canUndo())
 			{
 				try
 				{
 					deleteDungeon(alias, player, false);
 					d.undoDungeon();
 				}
 				catch(Exception e)
 				{
 					sender.sendMessage("Failed to undo dungeon creation: " + e.getMessage());
 					e.printStackTrace();
 				}
 			}
 			else
 			{
 				sender.sendMessage("No longer able to undo dungeon creation");
 				return true;
 			}
 		}
 
 		if(label.equals("adddefaultdungeonpermission") && checkPermission(player, "dungeonbuilder.dungeons.admin"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String node = args[1];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			d.addDefaultPermission(node);
 			sender.sendMessage("Permission added");
 		}
 
 		if(label.equals("removedefaultdungeonpermission") && checkPermission(player, "dungeonbuilder.dungeons.admin"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String node = args[1];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			d.removeDefaultPermission(node);
 			sender.sendMessage("Permission removed");
 		}
 
 		if(label.equals("cleardefaultdungeonpermissions") && checkPermission(player, "dungeonbuilder.dungeons.admin"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			d.clearDefaultPermissions();
 			sender.sendMessage("Permissions cleared");
 		}
 
 		if(label.equals("listdefaultdungeonpermissions") && checkPermission(player, "dungeonbuilder.dungeons.query"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			for(String node : d.listDefaultPermissions())
 			{
 				sender.sendMessage(node);
 			}
 		}
 
 		if(label.equals("leavedungeon"))
 		{
 			removePlayerFromDungeon(player, true);
 			//if(inParty.containsKey(playername))
 			//{
 			//	DungeonParty party = inParty.get(playername);
 			//	if(!party.getLeader().getName().equals(playername))
 			//	{
 			//		sender.sendMessage("Only the party leader can leave the dungeon");
 			//		return true;
 			//	}
 
 			//	for(Player p : party.listMembers())
 			//	{
 			//		removePlayerFromDungeon(p, true);
 			//	}
 			//}
 		}
 
 		if(label.equals("setpartysize") && checkPermission(player, "dungeonbuilder.dungeons.admin"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String size = args[1];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			try
 			{
 				Integer sizeInt = Integer.parseInt(size);
 				if(sizeInt <= 0)
 					throw new Exception("Invalid");
 
 				d.setPartySize(sizeInt);
 				sender.sendMessage("Party size set to: " + sizeInt);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage("Invalid party size parameter: " + size);
 				return true;
 			}
 		}
 
 		if(label.equals("toggleautoload") && checkPermission(player, "dungeonbuilder.dungeons.admin"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String enabled = args[1];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			try
 			{
 				Boolean temp = Boolean.parseBoolean(enabled);
 				d.toggleAutoload(temp);
 
 				sender.sendMessage("Autoloading for dungeon '" + alias + "' enabled: " + temp);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage("Invalid parameter: " + enabled);
 				return false;
 			}
 		}
 
 		if(label.equals("addmonstertrigger") && checkPermission(player, "dungeonbuilder.dungeons.trigger"))
 		{
 			if(args.length < 3)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String dungeonAlias = args[1];
 			String monsterAlias = args[2];
 
 			Dungeon d = lookupDungeon(dungeonAlias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + dungeonAlias + "'");
 				return true;
 			}
 
 			//if(!d.containsLocation(player.getLocation()))
 			//{
 			//	sender.sendMessage("You cannot set a monster trigger from outside the dungeon");
 			//	return true;
 			//}
 
 			Integer blockCount = 1;
 			if(args.length == 4)
 			{
 				try
 				{
 					blockCount = Integer.parseInt(args[3]);
 				}
 				catch(Exception e)
 				{
 					sender.sendMessage("Invalid argument: " + args[3]);
 					return true;
 				}
 			}
 
 			dungeonManager.removeDungeon(d);	
 			LocationWrapper lw = d.addMonsterTrigger(player, alias, monsterAlias, blockCount);
 			if(blockCount > 1)
 			{
 				playerListener.recordingLocations.put(playername, lw);
 			}
 			dungeonManager.addDungeon(d);
 
 			if(blockCount == 1)
 				sender.sendMessage("Trigger added");
 		}
 
 		if(label.equals("removemonstertrigger") && checkPermission(player, "dungeonbuilder.dungeons.trigger"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String dungeonAlias = args[1];
 
 			Dungeon d = lookupDungeon(dungeonAlias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + dungeonAlias + "'");
 				return true;
 			}
 
 			dungeonManager.removeDungeon(d);	
 			boolean retVal = d.removeMonsterTrigger(alias);
 			dungeonManager.addDungeon(d);
 
 			if(retVal)
 				sender.sendMessage("Monster trigger removed");
 			else
 				sender.sendMessage("Failed to remove trigger");
 		}
 
 		if(label.equals("listmonstertriggers") && checkPermission(player, "dungeonbuilder.dungeons.query"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			StringBuffer result = new StringBuffer();
 			for(LocationWrapper lw : d.listMonsterTriggers())
 			{
 				if(result.length() != 0)
 					result.append(", ");
 				result.append(lw.getAlias());
 			}
 
 			sender.sendMessage(result.toString());
 		}
 
 		if(label.equals("resettriggers") && checkPermission(player, "dungeonbuilder.dungeons.trigger"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			for(LocationWrapper trigger : d.listMonsterTriggers())
 				trigger.setActive(true);
 
 			for(LocationWrapper trigger : d.listScriptTriggers())
 				trigger.setActive(true);
 
 			sender.sendMessage("Triggers reactivated");
 		}
 
 		if(label.equals("addsavepoint") && checkPermission(player, "dungeonbuilder.dungeons.savepoint"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String dungeonalias = args[1];
 
 			Dungeon d = lookupDungeon(dungeonalias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + dungeonalias + "'");
 				return true;
 			}
 
 			Integer blockCount = 1;
 			if(args.length == 3)
 			{
 				try
 				{
 					blockCount = Integer.parseInt(args[2]);
 				}
 				catch(Exception e)
 				{
 					sender.sendMessage("Invalid argument: " + args[2]);
 					return false;
 				}
 			}
 
 			dungeonManager.removeDungeon(d);
 			LocationWrapper lw = d.addSavePoint(player, alias, blockCount);
 			if(blockCount > 1)
 			{
 				playerListener.recordingLocations.put(playername, lw);
 			}
 			dungeonManager.addDungeon(d);
 
 			if(blockCount == 1)
 				sender.sendMessage("Savepoint added");
 		}
 
 		if(label.equals("removesavepoint") && checkPermission(player, "dungeonbuilder.dungeons.savepoint"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String dungeonalias = args[1];
 
 			Dungeon d = lookupDungeon(dungeonalias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + dungeonalias + "'");
 				return true;
 			}
 
 			dungeonManager.removeDungeon(d);
 			boolean found = d.removeSavePoint(alias);
 			dungeonManager.addDungeon(d);
 
 			if(found)
 				sender.sendMessage("Save point removed");
 			else
 				sender.sendMessage("No save points found under that alias");
 		}
 
 		if(label.equals("listsavepoints") && checkPermission(player, "dungeonbuilder.dungeons.query"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			StringBuffer retVal = new StringBuffer("");
 			for(LocationWrapper lw : d.listSavePoints())
 			{
 				if(retVal.length() != 0)
 					retVal.append(", ");
 				retVal.append(lw.getAlias());
 			}
 
 			sender.sendMessage(retVal.toString());
 		}
 
 		if(label.equals("continuedungeon"))
 		{
 			if(!inDungeons.containsKey(playername))
 			{
 				sender.sendMessage("You are not currently running a dungeon");
 				return true;
 			}
 
 			LocationWrapper lw = getSavePoint(playername);
 			if(lw != null)
 			{
 				player.teleport(lw.getTargetLocation());
 				return true;
 			}
 
 			Dungeon d = inDungeons.get(playername);
 			player.teleport(d.getStartingLocation());
 			return true;
 		}
 
 		if(label.equals("addscripttrigger") && checkPermission(player, "dungeonbuilder.dungeons.script"))
 		{
 			if(args.length < 3)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String dungeonalias = args[1];
 			String method = args[2];
 
 			Dungeon d = lookupDungeon(dungeonalias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + dungeonalias + "'");
 				return true;
 			}
 
 			Integer blockCount = 1;
 			if(args.length == 4)
 			{
 				try
 				{
					blockCount = Integer.parseInt(args[3]);
 				}
 				catch(Exception e)
 				{
					sender.sendMessage("Invalid argument: " + args[3]);
 					return false;
 				}
 			}
 
 			dungeonManager.removeDungeon(d);
 			LocationWrapper lw = d.addScriptTrigger(player, alias, method, blockCount);
 			if(blockCount > 1)
 			{
 				playerListener.recordingLocations.put(playername, lw);
 			}
 			dungeonManager.addDungeon(d);
 
 			if(blockCount == 1)
 				sender.sendMessage("Script trigger added");
 		}
 
 		if(label.equals("removescripttrigger") && checkPermission(player, "dungeonbuilder.dungeons.script"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String dungeonalias = args[1];
 
 			Dungeon d = lookupDungeon(dungeonalias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + dungeonalias + "'");
 				return true;
 			}
 
 			dungeonManager.removeDungeon(d);
 			boolean found = d.removeScriptTrigger(alias);
 			dungeonManager.addDungeon(d);
 
 			if(found)
 				sender.sendMessage("Script trigger removed");
 			else
 				sender.sendMessage("No script triggers found under that alias");
 		}
 
 		if(label.equals("listscripttriggers") && checkPermission(player, "dungeonbuilder.dungeons.query"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			StringBuffer retVal = new StringBuffer("");
 			for(LocationWrapper lw : d.listScriptTriggers())
 			{
 				if(retVal.length() != 0)
 					retVal.append(", ");
 				retVal.append(lw.getAlias() + "(" + lw.getMetaData() + ")");
 			}
 
 			sender.sendMessage(retVal.toString());
 		}
 
 		if(label.equals("createdungeontemplate") && checkPermission(player, "dungeonbuilder.dungeons.export"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 			String templateName = alias;
 
 			if(args.length == 2)
 				templateName = args[1];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			File templateFolder = new File(dungeonRoot + "/templates");
 			if(!templateFolder.isDirectory())
 			{
 				if(!templateFolder.mkdirs())
 				{
 					sender.sendMessage("Failed to create template folder");
 					return true;
 				}
 			}
 
 			File dungeonFile = new File(dungeonRoot + "/templates/" + templateName);
 			try
 			{
 				d.saveToFile(dungeonFile, true);
 				sender.sendMessage("Template saved");
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				sender.sendMessage("Failed to create dungeon template - " + e.getMessage());
 			}
 		}
 
 		if(label.equals("createdungeonfromtemplate") && checkPermission(player, "dungeonbuilder.dungeons.import"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String templateName = args[0];
 			String alias = args[1];
 
 			File dungeonFile = new File(dungeonRoot + "/templates/" + templateName);
 			if(!dungeonFile.exists())
 			{
 				sender.sendMessage("A template by the name '" + templateName + "' does not exist");
 				return true;
 			}
 			
 			try
 			{
 				Dungeon d = new Dungeon(alias, playername, player.getLocation(), dungeonFile, this);
 
 				if(dungeonMap.containsKey(playername))
 				{
 					dungeonMap.get(playername).add(d);
 				}
 				else
 				{
 					ArrayList<Dungeon> dungeons = new ArrayList<Dungeon>();
 					dungeons.add(d);
 					dungeonMap.put(playername, dungeons);
 				}
 
 				sender.sendMessage("Dungeon created");
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				sender.sendMessage("Failed to create dungeon - " + e.getMessage());
 			}
 		}
 
 		if(label.equals("listtemplates") && checkPermission(player, "dungeonbuilder.dungeons.import"))
 		{
 			File templateDir = new File(dungeonRoot + "/templates");
 			if(!templateDir.isDirectory())
 			{
 				sender.sendMessage("No templates available");
 				return true;
 			}
 
 			StringBuffer retVal = new StringBuffer();
 			for(String file : templateDir.list())
 			{
 				if(retVal.length() > 0)
 					retVal.append(",");
 				retVal.append(file);
 			}
 
 			sender.sendMessage(retVal.toString());
 		}
 
 		if(label.equals("listparty") && checkPermission(player, "dungeonbuilder.party.query"))
 		{
 			if(inParty.containsKey(playername))
 			{
 				DungeonParty dp = inParty.get(playername);
 				StringBuffer partyList = new StringBuffer();
 				for(String partyMember : dp.listMembers())
 				{
 					if(partyList.length() > 0)
 						partyList.append(", ");
 					partyList.append(partyMember);
 				}
 
 				sender.sendMessage(partyList.toString());
 			}
 			else
 			{
 				sender.sendMessage("You are not currently in a party");
 			}
 
 			return true;
 		}
 
 		if(label.equals("addpartymember") && checkPermission(player, "dungeonbuilder.party.create"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String memberName = args[0];
 
 			DungeonParty party = null;
 			if(inParty.containsKey(playername))
 				party = inParty.get(playername);
 			else
 				party = new DungeonParty(playername, server);
 
 			if(!party.getLeader().equals(playername))
 			{
 				sender.sendMessage("Only the leader can add party members");
 				return true;
 			}
 
 			Player member = server.getPlayer(memberName);
 			if(member == null)
 			{
 				sender.sendMessage("Unknown player - " + memberName);
 				return true;
 			}
 
 			pendingInvites.put(memberName, party);
 			inParty.put(playername, party);
 
 			sender.sendMessage("Invite sent");
 			member.sendMessage(playername + " has invited you to join their party, type /acceptInvite to join");
 		}
 
 		if(label.equals("disbandparty") && checkPermission(player, "dungeonbuilder.party.create"))
 		{
 			if(inDungeons.containsKey(playername))
 			{
 				sender.sendMessage("Please leave the dungeon before disbanding the party");
 				return true;
 			}
 
 			if(!inParty.containsKey(playername))
 			{
 				sender.sendMessage("You are not currently in a party");
 				return true;
 			}
 
 			DungeonParty party = inParty.get(playername);	
 			if(!party.getLeader().equals(playername))
 			{
 				sender.sendMessage("Only the leader can disband the party");
 				return true;
 			}
 
 			for(String membername : party.listMembers())
 			{
 				inParty.remove(membername);
 				if(inDungeons.containsKey(membername))
 				{
 					inDungeons.get(membername).removeParty(party);
 					Player member = server.getPlayer(membername);
 					if(member != null)
 						removePlayerFromDungeon(member, true);
 				}
 			}
 
 			for(ArrayList<Dungeon> dungeons : dungeonMap.values())
 			{
 				for(Dungeon d : dungeons)
 					d.removeParty(party);
 			}
 
 			sender.sendMessage("Party disbanded");
 		}
 
 		if(label.equals("kickpartymember") && checkPermission(player, "dungeonbuilder.party.kick"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			if(inDungeons.containsKey(playername))
 			{
 				sender.sendMessage("Please leave the dungeon before removing party members");
 				return true;
 			}
 
 			if(args[0].equals(playername))
 			{
 				sender.sendMessage("You cannot kick yourself from the party, try /disbandParty instead");
 				return true;
 			}
 
 			if(inParty.containsKey(playername))
 			{
 				DungeonParty dp = inParty.get(playername);
 
 				if(!dp.getLeader().equals(playername))
 				{
 					sender.sendMessage("Only the leader can kick members from the party");
 					return true;
 				}
 
 				Player targetPlayer = null;
 				for(String partyMember : dp.listMembers())
 				{
 					if(partyMember.equals(args[0]))
 					{
 						targetPlayer = server.getPlayer(partyMember);
 					}
 				}
 
 				if(targetPlayer == null)
 					sender.sendMessage("Unable to find player '" + args[0] + "' in party");
 				else
 				{
 					dp.removeMember(targetPlayer.getName());
 					inParty.remove(targetPlayer.getName());
 					if(inDungeons.containsKey(targetPlayer.getName()))
 						removePlayerFromDungeon(targetPlayer, true);
 					sender.sendMessage("Player '" + targetPlayer.getName() + "' kicked.");
 				}
 			}
 			else
 			{
 				sender.sendMessage("You are not currently in a party");
 			}
 
 			return true;
 		}
 
 		if(label.equals("leaveparty"))
 		{
 			if(!inParty.containsKey(playername))
 			{
 				sender.sendMessage("You are not currently in a party");
 				return true;
 			}
 
 			if(inDungeons.containsKey(playername))
 			{
 				sender.sendMessage("Please leave the dungeon before leaving the party");
 				return true;
 			}
 
 			DungeonParty dp = inParty.get(playername);
 			if(dp.getLeader().equals(playername))
 			{
 				sender.sendMessage("The leader cannot leave the party.  Try /disbandParty instead");
 				return true;
 			}
 
 			dp.removeMember(playername);
 			inParty.remove(playername);
 
 			return true;
 		}
 
 		if(label.equals("acceptinvite"))
 		{
 			if(inParty.containsKey(playername))
 			{
 				player.sendMessage("You are already in a party, type /leaveParty to leave");
 				return true;
 			}
 
 			if(!pendingInvites.containsKey(playername))
 			{
 				player.sendMessage("You have no pending party invites");
 				return true;
 			}
 
 			DungeonParty party = pendingInvites.get(playername);
 			party.addMember(playername);
 			inParty.put(playername, party);
 
 			pendingInvites.remove(playername);
 
 			for(String pname : party.listMembers())
 			{
 				if(pname.equals(playername))
 					continue;
 
 				Player p = server.getPlayer(pname);
 				if(p != null)
 					p.sendMessage("Party member \"" + playername + "\" has joined");
 			}
 
 			player.sendMessage("You have joined the party.  Type /listParty to view fellow members or /leaveParty to leave");
 			//sender.sendMessage("Player added to party, you can view current party members with /listParty");
 		}
 
 		if(label.equals("startdungeon") && checkPermission(player, "dungeonbuilder.dungeons.start"))
 		{
 			if(args.length < 1)
 			{
 				player.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String dungeonName = args[0];
 			String ownerName = null;
 			if(args.length == 2)
 				ownerName = args[1];
 
 			Dungeon targetDungeon = null;
 			if(ownerName != null)
 			{
 				for(Dungeon d : dungeonMap.get(ownerName))
 				{
 					if(d.getName().equals(dungeonName))
 					{
 						targetDungeon = d;
 						break;
 					}
 				}
 			}
 			else
 			{
 				maploop: for(String key : dungeonMap.keySet())
 				{
 					for(Dungeon d : dungeonMap.get(key))
 					{
 						if(d.getName().equals(dungeonName))
 						{
 							targetDungeon = d;
 							break maploop;
 						}
 					}
 				}
 			}
 
 			if(targetDungeon == null)
 			{
 				player.sendMessage("Unable to locate dungeon \"" + dungeonName + "\"");
 				return true;
 			}
 
 			if(!targetDungeon.isPublished())
 			{
 				player.sendMessage("That dungeon has not been published for use.");
 				return true;
 			}
 
 			DungeonParty dp = null;
 			if(!inParty.containsKey(playername))
 			{
 				if(targetDungeon.getPartySize() == 1)
 				{
 					dp = new DungeonParty(playername, server);
 					inParty.put(playername, dp);
 				}
 				else
 				{
 					player.sendMessage("You need to be in a party of size " + targetDungeon.getPartySize() + " to start this dungeon.");
 					return true;
 				}
 			}
 			else
 			{
 				dp = inParty.get(playername);
 				if(targetDungeon.getPartySize() != dp.getSize())
 				{
 					player.sendMessage("You need to be in a party of size " + targetDungeon.getPartySize() + " to start this dungeon.");
 					return true;
 				}
 			}
 			
 
 			Dungeon.PartyStatus status = targetDungeon.addParty(dp);
 			switch(status)
 			{
 				case READY:
 					startDungeon(targetDungeon, dp, targetDungeon.getStartingLocation());	
 					break;
 				case INQUEUE:
 					player.sendMessage("You are now queued for the dungeon and will be notified when it becomes available.");
 					if(targetDungeon.getPartySize() == 1 && dp.getSize() == 1)
 						inParty.remove(playername);
 					break;
 			}
 
 		}
 
 		if(label.equals("setexpreward") && checkPermission(player, "dungeonbuilder.dungeons.expreward"))
 		{
 			if(args.length < 2)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			try
 			{
 				int exp = Integer.parseInt(args[1]);
 				d.setExpReward(exp);
 				sender.sendMessage("Exp reward for dungeon '" + alias + "' set to " + exp);
 			}
 			catch(Exception e)
 			{
 				sender.sendMessage("Invalid exp amount: " + args[1]);
 				return false;
 			}
 		}
 
 		if(label.equals("showexpreward") && checkPermission(player, "dungeonbuilder.dungeons.expreward"))
 		{
 			if(args.length < 1)
 			{
 				sender.sendMessage("Invalid number of arguments");
 				return false;
 			}
 
 			String alias = args[0];
 
 			Dungeon d = lookupDungeon(alias, playername);
 			if(d == null)
 			{
 				sender.sendMessage("Unable to find dungeon by name '" + alias + "'");
 				return true;
 			}
 
 			int exp = d.getExpReward();
 			if(exp > 0)
 				sender.sendMessage("Current exp reward set to " + exp + " for dungeon '" + alias + "'");
 			else
 				sender.sendMessage("There is currently no exp reward for dungeon '" + alias + "'");
 		}
 
 		return true;
 	}
 
 	private Dungeon lookupDungeon(String name, String player)
 	{
 		if(!dungeonMap.containsKey(player))
 			return null;
 
 		for(Dungeon d : dungeonMap.get(player))
 		{
 			if(d.getName().equals(name))
 			{
 				return d;
 			}
 		}
 
 		return null;
 	}
 
 	private class MyEntityListener extends EntityListener
 	{
 		public void onCreatureSpawn(CreatureSpawnEvent event)
 		{
 			Location loc = event.getLocation();
 
 			for(String key : dungeonMap.keySet())
 			{
 				for(Dungeon d : dungeonMap.get(key))
 				{
 					if(d.containsLocation(loc) && !d.spawnsAllowed())
 					{
 						event.setCancelled(true);
 						return;
 					}
 				}
 			}
 		}
 	}
 
 	private class MyServerListener extends ServerListener
 	{
 		public Methods m;
 
 		public MyServerListener()
 		{
 			m = new Methods();
 		}
 
 		public Method getMethod()
 		{
 			if(isMethodEnabled())
 				return m.getMethod();
 			else
 				return null;
 		}
 
 		public boolean isMethodEnabled()
 		{
 			return m.hasMethod();
 		}
 
 		@Override public void onPluginDisable(PluginDisableEvent event)
 		{
 			if(!m.hasMethod())
 				return;
 
 			m.checkDisabled(event.getPlugin());
 		}
 
 		@Override public void onPluginEnable(PluginEnableEvent event)
 		{
 			if(m.hasMethod())
 				return;
 
 			m.setMethod(server.getPluginManager());
 		}
 	}
 
 	private class MyBlockListener extends BlockListener
 	{
 		@Override public void onBlockBreak(BlockBreakEvent event)
 		{
 			Player p = event.getPlayer();
 			String name = p.getName();
 			if(!inDungeons.containsKey(name))
 				return;
 
 			Dungeon d = inDungeons.get(name);
 			if(d.getOwner().equals(name))
 				return;
 
 			Block b = event.getBlock();
 			if(d.hasDefaultPermission("dungeonbuilder.blocks.breaktype." + b.getType().toString()))
 				return;
 
 			if(d.hasDefaultPermission("*") || d.hasDefaultPermission("dungeonbuilder.blocks.breaktype.*"))
 				return;
 
 			String node = "dungeonbuilder.blocks.breakin." + d.getOwner() + "." + d.getName();
 			boolean allowed = checkPermission(p, node, false);
 			if(!allowed)
 			{
 				event.setCancelled(true);
 				return;
 			}
 
 			node = "dungeonbuilder.blocks.breaktype." + b.getType().toString();
 			allowed = checkPermission(p, node, false);
 			event.setCancelled(!allowed);
 		}
 
 		@Override public void onBlockPlace(BlockPlaceEvent event)
 		{
 			Player p = event.getPlayer();
 			String name = p.getName();
 			if(!inDungeons.containsKey(name))
 				return;
 
 			Dungeon d = inDungeons.get(name);
 			if(d.getOwner().equals(name))
 				return;
 
 			Block b = event.getBlock();
 			if(d.hasDefaultPermission("dungeonbuilder.blocks.placetype." + b.getType().toString()))
 				return;
 
 			if(d.hasDefaultPermission("*") || d.hasDefaultPermission("dungeonbuilder.blocks.placetype.*"))
 				return;
 
 			String node = "dungeonbuilder.blocks.placein." + d.getOwner() + "." + d.getName();
 			boolean allowed = checkPermission(p, node, false);
 			if(!allowed)
 			{
 				event.setCancelled(true);
 				return;
 			}
 
 			node = "dungeonbuilder.blocks.placetype." + b.getType().toString();
 			allowed = checkPermission(p, node, false);
 			event.setCancelled(!allowed);
 		}
 	}
 
 	public boolean isPlayerIdle(String playername)
 	{
 		if(!idleTimer.containsKey(playername))
 			return false;
 
 		Long time = idleTimer.get(playername);
 		Long difference = System.currentTimeMillis() - time;
 		if(difference >= idleTimeout)
 			return true;
 		
 		return false;
 	}
 
 	public void startDungeon(Dungeon d, DungeonParty party, Location loc)
 	{
 		d.killMonsters();
 		for(LocationWrapper mt1 : d.listMonsterTriggers())
 			mt1.setActive(true);
 		for(LocationWrapper st1 : d.listScriptTriggers())
 			st1.setActive(true);
 
 		for(String dpname : party.listMembers())
 		{
 			//I think what happens here is if the player disconnects and reconnects the server creates a new player object...
 			//lets always make sure we have the latest one for certain actions
 			Player ptemp = server.getPlayer(dpname);
 			addPlayerToDungeon(dpname, d);
 			clearSavePoint(dpname);
 			ScriptManager.runDungeonStartScript(d, server, ptemp);
 
 			if(!isPlayerStillInDungeon(ptemp))
 				ptemp.teleport(loc);
 		}
 		d.setActiveParty(party);
 	}
 
 	private Dungeon createDungeon(String name, String player, int width, int depth, int height, boolean hollow)
 		throws Exception
 	{
 		return createDungeon(name, player, width, depth, height, 7, hollow);
 	}
 
 	private Dungeon createDungeon(String name, String player, int width, int depth, int height, int blockType, boolean hollow)
 		throws Exception
 	{
 		Player p = server.getPlayer(player);
 		if(p == null)
 			throw new Exception("Unable to locate player - " + player);
 
 		return createDungeon(name, player, p.getLocation(), width, depth, height, blockType, hollow);
 	}
 
 	private Dungeon createDungeon(String name, String player, Location center, int width, int depth, int height, int blockType, boolean hollow)
 		throws Exception
 	{
 		Player p = server.getPlayer(player);
 		if(p == null)
 			throw new Exception("Unable to locate player - " + player);
 
 		ArrayList<Dungeon> dungeons;
 		if(!dungeonMap.containsKey(player))
 		{
 			dungeons = new ArrayList<Dungeon>();
 			dungeonMap.put(player, dungeons);
 		}
 		else
 		{
 			dungeons = dungeonMap.get(player);
 			Dungeon d = lookupDungeon(name, player);
 			if(d != null)
 				throw new Exception("A dungeon already exists with that name for that player");
 		}
 
 		try
 		{
 			ArrayList<BlockInfo> origBlocks = WorldUtils.createRoom(center, width, depth, height, blockType, hollow);
 			Dungeon d = new Dungeon(name, player, center, width, depth, height, this);
 			d.setOriginalBlocks(origBlocks);
 			dungeons.add(d);
 
 			return d;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	private boolean teleportToDungeon(String name, String player)
 		throws Exception
 	{
 		Player p = server.getPlayer(player);
 		if(p == null)
 			throw new Exception("Unable to locate player - " + player);
 
 		if(!dungeonMap.containsKey(player))
 			throw new Exception("The player '" + player + "' does not currently have any dungeons");
 
 		Dungeon d = lookupDungeon(name, player);
 		if(d != null)
 		{
 			Location loc = d.getStartingLocation();
 			d.killMonsters();
 
 			World world = loc.getWorld();
 			world.loadChunk(loc.getBlockX(), loc.getBlockZ());
 			p.teleport(loc);
 			return true;
 		}
 
 		throw new Exception("Unable to locate dungeon '" + name + "'");
 	}
 
 	private boolean teleportOutsideDungeon(String name, String player)
 		throws Exception
 	{
 		Player p = server.getPlayer(player);
 		if(p == null)
 			throw new Exception("Unable to locate player - " + player);
 
 		if(!dungeonMap.containsKey(player))
 			throw new Exception("The player '" + player + "' does not currently have any dungeons");
 
 		Dungeon d = lookupDungeon(name, player);
 		if(d != null)
 		{
 			Location loc = d.getCenterLocation().clone();
 			loc.setY(loc.getY() + d.getHeight());
 
 			Block b = loc.getBlock();
 			if(b.getType() != Material.AIR)
 				throw new Exception("Unable to find appropriate teleport point outside dungeon");
 			Location loc2 = loc.clone();
 			loc2.setY(loc2.getY() + 1);
 			b = loc2.getBlock();
 			if(b.getType() != Material.AIR)
 				throw new Exception("Unable to find appropriate teleport point outside dungeon");
 
 			World world = loc.getWorld();
 			world.loadChunk(loc.getBlockX(), loc.getBlockZ());
 			p.teleport(loc);
 			return true;
 		}
 
 		throw new Exception("Unable to locate dungeon '" + name + "'");
 	}
 
 	private boolean teleportToDungeonCenter(String name, String player)
 		throws Exception
 	{
 		Player p = server.getPlayer(player);
 		if(p == null)
 			throw new Exception("Unable to locate player - " + player);
 
 		if(!dungeonMap.containsKey(player))
 			throw new Exception("The player '" + player + "' does not currently have any dungeons");
 
 		Dungeon d = lookupDungeon(name, player);
 		if(d != null)
 		{
 			Location loc = d.getCenterLocation();
 			d.killMonsters();
 
 			World world = loc.getWorld();
 			world.loadChunk(loc.getBlockX(), loc.getBlockZ());
 			p.teleport(loc);
 			return true;
 		}
 
 		throw new Exception("Unable to locate dungeon '" + name + "'");
 	}
 
 	private boolean deleteDungeon(String name, Player player, boolean clear)
 	{
 		Dungeon d = lookupDungeon(name, player.getName());
 		if(d == null)
 			return false;
 
 		dungeonManager.removeDungeon(d);
 
 		if(!d.deleteFromServer(clear))
 			return false;
 
 		dungeonMap.get(player.getName()).remove(d);
 
 		return true;
 	}
 
 	private void loadDungeons(Server s)
 	{
 		File dungeonDir = new File(dungeonRoot);
 		if(!dungeonDir.exists())
 			return;
 
 		for(String playername : dungeonDir.list())
 		{
 			if(playername.equals("templates"))
 				continue;
 
 			File dungeonFiles = new File(dungeonRoot + "/" + playername);
 			if(!dungeonFiles.isDirectory())
 				continue;
 
 			ArrayList<Dungeon> dungeons = new ArrayList<Dungeon>();
 			for(String dungeonName : dungeonFiles.list())
 			{
 				if(dungeonName.endsWith("previous"))
 					continue;
 				if(dungeonName.endsWith(".groovy"))
 					continue;
 				if(dungeonName.endsWith(".js"))
 					continue;
 				if(dungeonName.endsWith(".perms"))
 					continue;
 
 				try
 				{
 					myLogger.log(Level.INFO, "DungeonBuilder - Loading dungeon - " + playername + ":" + dungeonName);	
 					Dungeon d = new Dungeon(dungeonName, playername, this);
 					dungeons.add(d);
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 
 			dungeonMap.put(playername, dungeons);
 		}
 	}
 
 	public void rewardPlayer(Player p, Dungeon d)
 	{
 		if(!economyListener.isMethodEnabled())
 		{
 			myLogger.log(Level.INFO, "DungeonBuilder - No economy plugin detected.  Skipping player rewards");
 			return;
 		}
 
 		double reward = d.getDungeonReward();
 		if(reward == 0.0)
 			return;
 
 		Method m = economyListener.getMethod();
 		if(m == null)
 		{
 			myLogger.log(Level.WARNING, "DungeonBuilder - Strange, the server listener returned a null Method object");
 			return;
 		}
 
 		MethodAccount ma = m.getAccount(p.getName());
 		if(ma.add(reward))
 			myLogger.log(Level.INFO, "DungeonBuilder - Adding " + reward + " credits to " + p.getName() + "'s account");
 		else
 			myLogger.log(Level.INFO, "DungeonBuilder - Could not access account for player: '" + p.getName() + "'");
 	}
 
 	private boolean checkPermission(Player p, String node)
 	{
 		return checkPermission(p, node, true);
 	}
 
 	private boolean checkPermission(Player p, String node, boolean allowOp)
 	{
 		if(enableSuperperms)
 			return p.hasPermission(node);
 		else if(allowOp)
 			return p.isOp();
 		else
 			return false;
 	}
 
 	public boolean isPlayerStillInDungeon(Player p)
 	{
 		String name = p.getName();
 		if(!inDungeons.containsKey(name))
 			return false;
 
 		Dungeon d = inDungeons.get(name);
 		Location playerLoc = p.getLocation();
 
 		return d.containsLocation(playerLoc);
 	}
 
 	private boolean isPlayerInADungeon(Player p)
 	{
 		String playername = p.getName();
 
 		if(inDungeons.containsKey(playername))
 		{
 			if(isPlayerStillInDungeon(p))
 				return true;
 		}
 
 		Location loc = p.getLocation();
 		for(String key : dungeonMap.keySet())
 		{
 			for(Dungeon d : dungeonMap.get(key))	
 			{
 				if(d.containsLocation(loc))
 				{
 					inDungeons.put(playername, d);
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public void addPlayerToDungeon(String playername, Dungeon d)
 	{
 		inDungeons.put(playername, d);
 	}
 
 	public void removePlayerFromDungeon(final Player player, boolean teleport)
 	{
 		if(!isPlayerInADungeon(player))
 			return;
 
 		String playername = player.getName();
 
 		clearSavePoint(playername);
 		
 		if(inDungeons.containsKey(playername))
 		{
 			final Dungeon d = inDungeons.get(playername);
 			if(inParty.containsKey(playername))
 			{
 				DungeonParty party = inParty.get(playername);
 				d.checkActiveParty(playername);  //Clear the active party status for the dungeon if no party members are still in it
 				if(party.getSize() == 1)
 					inParty.remove(playername);
 			}
 			inDungeons.remove(playername);
 			if(teleport)
 			{
 				scheduler.scheduleSyncDelayedTask(this, new Runnable() { @Override public void run() {
 					player.teleport(d.getExitDestination());	
 				}});
 			}
 			if(d.getActiveParty() == null)
 			{
 				if(d.getAutoload())
 					d.loadDungeon();
 				d.killMonsters();
 				ScriptManager.runDungeonExitScript(d, server, player);
 
 				for(LocationWrapper mt1 : d.listMonsterTriggers())
 					mt1.setActive(true);
 				for(LocationWrapper st1 : d.listScriptTriggers())
 					st1.setActive(true);
 			}
 		}
 		//else //In the chance we dont know what dungeon they were in lets go ahead and manually search through all of them
 		//{
 		//	for(ArrayList<Dungeon> dungeonList : dungeonMap.values())
 		//	{
 		//		for(final Dungeon d : dungeonList)
 		//		{
 		//			if(d.removePlayer(player))
 		//			{
 		//				inParty.remove(player.getName());
 		//				if(teleport)
 		//				{
 		//					scheduler.scheduleSyncDelayedTask(this, new Runnable() { @Override public void run() {
 		//						player.teleport(d.getExitDestination());
 		//					}});
 		//				}
 
 		//				if(d.getActiveParty() == null)
 		//				{
 		//					if(d.getAutoload())
 		//						d.loadDungeon();
 		//					d.killMonsters();
 		//					ScriptManager.runDungeonExitScript(d, server, player);
 		//					for(LocationWrapper mt1 : d.listMonsterTriggers())
 		//						mt1.setActive(true);
 		//					for(LocationWrapper st1 : d.listScriptTriggers())
 		//						st1.setActive(true);
 		//				}
 		//			}
 		//		}
 		//	}
 		//}
 	}
 
 	public LocationWrapper getSavePoint(String playername)
 	{
 		if(activeSavePoints.containsKey(playername))
 			return activeSavePoints.get(playername);
 
 		return null;
 	}
 
 	public void clearSavePoint(String playername)
 	{
 		if(activeSavePoints.containsKey(playername))
 			activeSavePoints.remove(playername);
 	}
 	
 	public boolean setSavePoint(String playername, LocationWrapper savePoint)
 	{
 		if(!inDungeons.containsKey(playername))
 			return false;
 
 		activeSavePoints.put(playername, savePoint);
 		return true;
 	}
 
 	private class DungeonMarker
 	{
 		private Location l1, l2;
 
 		public DungeonMarker()
 		{
 			l1 = null;
 			l2 = null;
 		}
 
 		public void setFirstMarker(Location l)
 		{
 			l1 = l;
 		}
 
 		public void setSecondMarker(Location l)
 		{
 			l2 = l;
 		}
 
 		public Location getFirstMarker()
 		{
 			return l1;
 		}
 
 		public Location getSecondMarker()
 		{
 			return l2;
 		}
 	}
 }
