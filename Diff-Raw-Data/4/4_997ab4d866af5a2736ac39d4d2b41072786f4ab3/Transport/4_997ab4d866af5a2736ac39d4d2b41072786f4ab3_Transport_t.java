 package iggy.Transport;
 
 import iggy.Regions.Position;
 import iggy.Regions.Regions;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Transport extends JavaPlugin {
 	public static Transport plugin;
 	public final TransportPlayerListener playerListen = new TransportPlayerListener(this);
 	
 	
 	////////////////////////////////// Variables for compass jumping ////////////
 	public Map<Player, Date> lastJump = new HashMap<Player, Date>();
 	
 	
 	
 	////////////////////////////////// Variables for warping ////////////////////
 	public Map<String,Location> cityTeleports = new HashMap<String,Location>();
 	public Map<Location,String> cityActivators = new HashMap<Location,String>();
 	public Map<String,List<String>> playerActivations = new HashMap<String,List<String>>();
 	public String tempCityWarpName = "";
 	public Location tempCityWarpLocation = null;
 	
 	
 	///////////////////////// Variables for flying ////////////////////////////////
 	Map<String, Integer> flightTimeRemaining = new HashMap<String, Integer>();
 	
 	////////////////////////////// File data and IO variables /////////////////////
 	public final Logger logger = Logger.getLogger("Minecraft");
 	PluginDescriptionFile pdFile;
 	String pluginName;
 	String pluginTitle;
 	ChatColor deactivatedWarpColor = ChatColor.DARK_PURPLE; 
 	ChatColor activatedWarpColor = ChatColor.LIGHT_PURPLE;
 	
 	
 	///////////////////////////////////////////////////////////////////////
 	//////////////////////////// Plugins functions ///////////////////////////
 	///////////////////////////////////////////////////////////////
 	
 	
 	/********************************* LOAD CITIES ********************************\
 	|
 	\******************************************************************************/
 	public void loadCities () {
 		cityTeleports.clear();
 		cityActivators.clear();
 		//Load all the city names
 		ConfigurationSection cityconfiguration = this.getConfig().getConfigurationSection("city");
 		
 		Set <String> cities = null;
 		
 		if (cityconfiguration == null) {
 			severe("The list of teleport citys is missing or corrupted: recreating list");
 			severe("If this is the first time runing this plugin do not worry about this error");
 			cities = new HashSet<String>();
 		}
 		else {
 			cities = cityconfiguration.getKeys(false);
 		}
 		
 		if (cities == null) {
 			severe("Failed to read the Configuration File");
 			return;
 		}
 		
 		// iterate through the cities and get their data
 		Iterator<String> it = cities.iterator();
 		while (it.hasNext()) {
 	        String cityName = it.next();
 	        
 	        /// possible version 2 ///
 	        //String warp = this.getConfig().getString("city."+cityName+".warp");
 	        //String activator = this.getConfig().getString("city."+cityName+".activator");
 	        
 	        
 	        // LOAD TELEPORT
 	        
 	        World warpWorld = this.getServer().getWorld(this.getConfig().getString("city."+cityName+".warp.world"));
 	        
 	        if (warpWorld == null) {
 	        	severe("Failed to find the world for "+cityName+"'s warp on the server");
 	        	continue;
 	        }
 	        
 	        Double warpX = this.getConfig().getDouble("city."+cityName+".warp.x");
 	        Double warpY = this.getConfig().getDouble("city."+cityName+".warp.y");
 	        Double warpZ = this.getConfig().getDouble("city."+cityName+".warp.z");
 	        
 	        float warpYaw = Float.parseFloat(this.getConfig().getString("city."+cityName+".warp.yaw"));
 	        float warpPitch = 0;
 	        
 	        //LOAD ACTIVATOR
 	        
 	        World activatorWorld = this.getServer().getWorld(this.getConfig().getString("city."+cityName+".activator.world"));
 	        
 	        if (activatorWorld == null){
 	        	severe("Failed to find the world for "+cityName+"'s activator on the server");
 	        	continue;
 	        }
 	        
 	        Double activatorX = this.getConfig().getDouble("city."+cityName+".activator.x");
 	        Double activatorY = this.getConfig().getDouble("city."+cityName+".activator.y");
 	        Double activatorZ = this.getConfig().getDouble("city."+cityName+".activator.z");
 
 	        // Put both on the lists
 	        cityTeleports.put(cityName, new Location(warpWorld,warpX,warpY,warpZ,warpYaw,warpPitch));
 	        cityActivators.put(new Location(activatorWorld,activatorX,activatorY,activatorZ), cityName);
 	        
 	        
 	    }
 		info("Loaded \033[0;32m" + String.valueOf(cities.size()) + "\033[0m Cities \033[0;35m"+cities.toString() + "\033[0m");
 	}
 	/********************************* SAVE CITIES ********************************\
 	|
 	\******************************************************************************/
 	public void saveCities() {
 		// clear the current listing of cities
 		this.getConfig().set("city","");
 		
 		// add the new cities warp
 		Iterator<Entry<String, Location>> teleportIterator =  cityTeleports.entrySet().iterator();
 		while (teleportIterator.hasNext()) {
 			Entry<String,Location> pairs = teleportIterator.next();
 			
 			String cityName = pairs.getKey();
 			
 			info("Saving warps for :"+cityName);
 			
 			
 			// Set warp
 			this.getConfig().set("city."+cityName+".warp.world", pairs.getValue().getWorld().getName());
 			this.getConfig().set("city."+cityName+".warp.x", pairs.getValue().getX());
 			this.getConfig().set("city."+cityName+".warp.y", pairs.getValue().getY());
 			this.getConfig().set("city."+cityName+".warp.z", pairs.getValue().getZ());
 			this.getConfig().set("city."+cityName+".warp.yaw", pairs.getValue().getYaw());
 	    }
 		
 		// add the new cities activators
 		Iterator<Entry<Location, String>> activatorIterator = this.cityActivators.entrySet().iterator();
 		while (activatorIterator.hasNext()) {
 			Entry<Location, String> pairs = activatorIterator.next();
 			
 			String cityName = pairs.getValue();
 			
 			info("Saving activators for :"+cityName);
 			
 			// Set Activator
 			this.getConfig().set("city."+cityName+".activator.world", pairs.getKey().getWorld().getName());
 			this.getConfig().set("city."+cityName+".activator.x", pairs.getKey().getX());
 			this.getConfig().set("city."+cityName+".activator.y", pairs.getKey().getY());
 			this.getConfig().set("city."+cityName+".activator.z", pairs.getKey().getZ());
 			
 		}
 		
 	}
 	/********************************** ADD CITY **********************************\
 	| This function adds a city to the list of citys, warps, and activators        |
 	\******************************************************************************/
 	public void addCity(String city, Location warp, Location activator) {
 		cityActivators.put(activator, city);
 		cityTeleports.put(city, warp);
 		saveCities();
 		this.saveConfig();
 		info(city+" was created");
 	}
 	/****************************** LOAD ACTIVATIONS ******************************\
 	|
 	\******************************************************************************/
 	public void loadActivations() {
 		playerActivations.clear();
 		
 		Set <String> players = null;
 		
 		ConfigurationSection playersconfiguration = this.getConfig().getConfigurationSection("player");
 		if (playersconfiguration == null) {
 			severe("The list of players and activations is missing or corrupted: recreating list");
 			severe("If this is the first time runing this plugin do not worry about this error");
 			players = new HashSet<String>();
 		}
 		else {
 			players = playersconfiguration.getKeys(false);
 		}
 		
 		
 		
 		
 		if (players == null) {
 			severe("Failed to read the Configuration File");
 		}
 		
 		// iterate through the players
 		Iterator<String> playerIterator = players.iterator();
 		while (playerIterator.hasNext()){
 			String playerName = playerIterator.next();
 			
 			List<String> activations = this.getConfig().getStringList("player."+playerName);
 			
 			if (activations == null) {
 				severe("Failed to read the Configuration File for " + playerName);
 			}
 			
 			playerActivations.put(playerName, activations);
 		}
 		info("Loaded \033[0;32m" + String.valueOf(players.size()) + "\033[0m Players \033[0;35m"+players.toString() + "\033[0m");
 	}
 	/****************************** SAVE ACTIVATIONS ******************************\
 	|
 	\******************************************************************************/
 	public void saveActivations() {
 		this.getConfig().set("player", "");
 		
 		Iterator<Entry<String, List<String>>> it = playerActivations.entrySet().iterator();
 		
 		if (it == null){
 			severe("Failed to save configuration file (playerActivations iterator is null)");
 			return;
 		}
 		
 		while(it.hasNext()) {
 			Entry<String,List<String>> pairs = it.next();
 			
 			String playerName = pairs.getKey();
 			
 			info("Saving player activations for \033[0;32m"+playerName+"\033[0m");
 			
 			this.getConfig().set("player."+playerName,pairs.getValue());
 		}
 	}
 	/******************************* ADD ACTIVATIONS ******************************\
 	| This adds the city to the player's activated cities list. If the player      |
 	| already has the city activated then the function returns false and does      |
 	| nothing, on success it returns true                                           |
 	\******************************************************************************/
 	public Boolean addActivation (String player, String city) {
 		List<String> activations = playerActivations.get(player);
 		
 		if (activations == null) {
 			activations = new ArrayList<String>();
 		}
 		
 		if(activations.contains(city)){
 			return false;
 		}
 		
 		activations.add(city);
 		
 		playerActivations.put(player, activations);
 		
 		saveActivations();
 		this.saveConfig();
 		info(player + " activated " + city);
 		return true;
 	}
 	
 	Plugin regions;
 	Regions regionsapi;
 	/********************************** ON ENABLE *********************************\
 	|
 	\******************************************************************************/
 	@Override
 	public void onEnable() {
 		pdFile = this.getDescription();
 		pluginName = pdFile.getName();
 		pluginTitle = "[\033[0;36m"+pluginName+"\033[0m]";
 		
 		Bukkit.getServer().getPluginManager().registerEvents(playerListen, this);
 		
 		loadCities();
 		loadActivations();
 		
 		
 		regions = Bukkit.getServer().getPluginManager().getPlugin("Regions");
 				
 		if (regions == null){
 			severe("Cannot Find Regions Plugin");
 			return;
 		}
 		
 		
 		regionsapi = (Regions) regions;
 		info ("Loaded Economy");
 		
 		//set up all the block listeners to prevent distruction
 		
 		//economy is required for buying new chunks
 		//dynmap is required for mapfunctions
 		
 		if (!regions.isEnabled()) {
 			getServer().getPluginManager().registerEvents(new OurServerListener(), this);
 			if (!regions.isEnabled()) {
 				info("Waiting for Regions to be enabled");
 			}
 		}
 		else {
 			activateRegions();
 		}
 		
 		startFlyerCounter();
 		
 		info("version " + pdFile.getVersion() +" is enabled");
 	}
 	/********************************* ON DISABLE *********************************\
 	|
 	\******************************************************************************/
 	@Override
 	public void onDisable() {
 		saveCities();
 		saveActivations();
 		this.saveConfig();
 		info("version " + pdFile.getVersion() +" is disabled");
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////// WAIT FOR OTHER PLUGINS ///////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 		// listener class to wait for the other plugins to enable
 		private class OurServerListener implements Listener {
 			// warnings are suppressed becasue this is called using registerEvents when the 
 			@SuppressWarnings("unused")
 			// this function runs whenever a plugin is enabled
 			@EventHandler (priority = EventPriority.MONITOR)
 	        public void onPluginEnable(PluginEnableEvent event) {
 	            Plugin p = event.getPlugin();
 	            String name = p.getDescription().getName();
 	            if(name.equals("Regions")) {
 	            	activateRegions();
 	            }
 	        }
 	    }
 
 		// funtion to finish activating the plugin once the other plugins are enabled
 		public void activateRegions(){
 			//TODO: make these features not enabled if the plugin is not enabeled
 			info ("New warp creation activated");
 		}
   //////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////// COMMANDS ////////////////////////////////// 
 //////////////////////////////////////////////////////////////////////////////	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		/******************************* LIST ALL WARPS *******************************\
 		| This command lists all of the warps that the server has regestered           |
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("warplist")) {
 			String output = "";
 			String differentColor = new String();
 			String defaultColor = new String();
 			if (player == null) {
 				differentColor = "\033[0;35m";
 				defaultColor = "\033[0m";
 			}
 			else {
 				differentColor = deactivatedWarpColor.toString();
 				defaultColor = ChatColor.WHITE.toString();
 			}
 			for (Entry<String, Location> warpPlace : cityTeleports.entrySet()) {
 				if (player == null) {
 					output += differentColor + warpPlace.getKey() + defaultColor + ", ";
 				}
 				else {
					List<String> playerWarps = playerActivations.get(player.getName());
					if (playerWarps == null) playerWarps = new ArrayList<String>();
					if ( playerWarps.contains(warpPlace.getKey()) ) {
 						output += activatedWarpColor + warpPlace.getKey() + defaultColor + ", ";
 					}
 					else {
 						output += differentColor + warpPlace.getKey() + defaultColor + ", ";
 					}
 				}
 			}
 			if (player == null) { info (output); }
 			else { player.sendMessage(output); }
 		}
 		
 		/// past here only players can enter commands ///
 		if (player == null) {
 			info("This command can only be run by a player");
 			return false;
 		}
 		/******************************** LIST MY WARPS *******************************\
 		| This function will list all of the warps that you have activated, but not    |
 		| the ones that you have not activated.                                        |
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("mywarps")) {
 			List<String> activations = playerActivations.get(player.getName());
 			if (activations == null) {
 				player.sendMessage("You have no "+ChatColor.AQUA+"warps" + ChatColor.WHITE);
 				return false;
 			}
 			String warpOutput = "Activated Warps: ";
 			for (String warpName : activations) {
 				warpOutput += activatedWarpColor + warpName + ", ";
 			}
 			player.sendMessage(warpOutput);
 			
 			
 		}
 		/********************************* CREATE WARP ********************************\
 		| 
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("createwarp")){
 			if (player.isOp() || (player.hasPermission("teleport.createwarp"))){
 				if (regions != null && regions.isEnabled()) {
 					ItemStack item = new ItemStack(Material.SIGN);
 					player.getInventory().addItem(item);
 					Position p = new Position(player.getLocation());
 					String name = regionsapi.chunkNames.get(p);
 					if (name == null) {
 						player.sendMessage("You cannot create a warp to the wild");
 						return false;
 					}
 					player.sendMessage("Creating a warp for "+name);
 					tempCityWarpName = name;
 					tempCityWarpLocation = player.getLocation();
 				}
 				else {
 					player.sendMessage("Create Regions broken, regions not enabled. Contact your server admin");
 				}
 				
 			}
 			else {
 				player.sendMessage("Ask an admin to create this warp for you");
 			}
 		}
 		/************************************ WARP ************************************\
 		| The command teleports a player to the location they specified                |
 		\******************************************************************************/
 		if (commandLabel.equalsIgnoreCase("warp")){
 			World currentWorld = player.getWorld();
 			if (!(currentWorld == getServer().getWorld("world") || currentWorld == getServer().getWorld("world_nether"))) {
 				player.sendMessage("You can only warp from the overworld and the nether");
 				return false;
 			}
 			
 			if ( args.length == 1) {
 				List<String> matches = findMatch (args[0]);
 				
 				// if there is only one match: warp there
 				if (matches.size() == 1) {
 					String match = matches.get(0);
 					if (playerActivations.get(player.getName()).contains(match)) {
 						//attempt to teleport the player
 						warp(player,match);
 					} else {
 						player.sendMessage("You have not activated the "+match+"  warp");
 					}
 				}
 				
 				// if there are no matches 
 				else if (matches.size() == 0) {
 					player.sendMessage("Cannot find the " + deactivatedWarpColor + args[0] + ChatColor.WHITE +" warp");
 				}
 				
 				// Otherwise there are many warps
 				else {
 					String warpOutput = "Found Multiple Warps: ";
 					for (String match : matches) {
 						if (playerActivations.get(player.getName()).contains(match)) {
 							warpOutput += activatedWarpColor + match;
 						}
 						else {
 							warpOutput += deactivatedWarpColor + match;
 						}
 						warpOutput += ChatColor.WHITE;
 						warpOutput += ", ";
 					
 					}
 					player.sendMessage(warpOutput);
 				}
 			}
 			else {
 				player.sendMessage("correct usage is /warp <location>");
 			}
 			
 		}
 		
 		
 		if (commandLabel.equalsIgnoreCase("spawn")){
 			player.sendMessage("This function is disabled until writable books get an API");
 			/*World currentWorld = player.getWorld();
 			if (!(currentWorld == getServer().getWorld("world") || currentWorld == getServer().getWorld("world_nether"))) {
 				player.sendMessage("You can only warp from the overworld and the nether");
 				return false;
 			}
 			warp(player,"spawn");*/
 		}
 		/****************************** REFRESH / RELOAD ******************************\
 		| 
 		\*****************************************************************************
 		if (commandLabel.equalsIgnoreCase("refresh")||commandLabel.equalsIgnoreCase("re")) {
 			Location myLocation = player.getLocation();
 			
 			Location otherworld = new Location(getServer().getWorld("shopworld"), 0, 64, 0);
 			player.teleport(otherworld);
 			
 			player.teleport(myLocation);
 		}
 		*/
 		
 		if (commandLabel.equalsIgnoreCase("fly")) {
 			
 			ItemStack boots = player.getInventory().getBoots();
 			
 			String errorMessage = "You are not wearing boots with featherfall";
 			
 			if (boots == null) {
 				player.sendMessage(errorMessage);
 				return false;
 			}
 			
 			if (boots.containsEnchantment(Enchantment.PROTECTION_FALL)) {
 					
 				// durration is the number of minutes the player can fly for
 				int durration = 5;
 				// change the durration depending on what type of boots the player is wearing
 				if (boots.getType() == Material.DIAMOND_BOOTS)      { durration = 30;}
 				else if (boots.getType() == Material.GOLD_BOOTS)    { durration = 20;}
 				else if (boots.getType() == Material.IRON_BOOTS)    { durration = 15;}
 				else if (boots.getType() == Material.LEATHER_BOOTS) { durration = 5;}
 				
 				// Display
 				player.sendMessage("You can now fly for the next " + durration + " minutes");
 				
 				player.setAllowFlight(true);
 				
 				flightTimeRemaining.put(player.getName(), (durration*6) + 1);				
 				// remove the boots from the game
 				player.getInventory().setBoots(new ItemStack(Material.AIR,0));
 				
 			}
 			else {
 				player.sendMessage(errorMessage);
 			}
 		}
 		
 		if (commandLabel.equalsIgnoreCase("forcewarp")) {
 			if (!player.isOp()) {
 				player.sendMessage("Stop trying to cheat");
 				return false;
 			}
 			if (!regions.isEnabled()) {
 				player.sendMessage("The regions plugin is not enabled, it must be enabled to forcewarp");
 				return false;
 			}
 			if (args.length != 1) {
 				player.sendMessage("You need to pick one region to force warp to");
 				return false;
 			}
 			String warpDestination = args[0];
 			if (!regionsapi.chunkOwners.containsKey(warpDestination)) {
 				player.sendMessage("Cannot find the plot "+warpDestination);
 				return false;
 			}
 			Location warpLocation = null;
 			for (Entry<Position, String> testLocation : regionsapi.chunkNames.entrySet()) {
 				if (testLocation.getValue().equals(warpDestination)) {
 					Position position = testLocation.getKey();
 					World world = Bukkit.getServer().getWorld(position._world);
 					warpLocation = new Location (world,position.getMinimumXCorner(), 257, position.getMinimumZCorner());				
 				}
 			}
 			if (warpLocation == null) {
 				player.sendMessage("Cannot find a valid warp point in " + warpDestination);
 				return false;
 			}
 			player.teleport(warpLocation);
 		}
 		return false;
 	}
 	
 	
 	public List<String> findMatch (String cityname) {
 		
 		List<String> matches =  new ArrayList<String>();
 		
 		// 
 		for (Entry<String, Location> warpPlace : cityTeleports.entrySet()) {
 			int maxLength = cityname.length();
 			if (warpPlace.getKey().length() < maxLength) maxLength = warpPlace.getKey().length();
 			if (warpPlace.getKey().substring(0, maxLength).equals(cityname)) {
 				matches.add(warpPlace.getKey());
 			}
 		}
 
 		return matches;
 	}
 	
 	// queue for teleporters
 	public Queue<Player>   teleportingPlayerQueue = new LinkedList<Player>();
 	public Queue<Location> teleportingDestinationQueue = new LinkedList<Location>();
 	// hash table for waiting teleporters
 	public Map<Player,Location> teleportingPlayers = new HashMap<Player,Location>();
 	public Map<Player,Date> lastWarpTime = new HashMap<Player,Date>();
 	//
 	public void warp (Player player, String cityname){
 		Date nowdate = new Date();
 		Date thendate = lastWarpTime.get(player);
 		
 		
 		
 		// prevent rapid jumping or quick jumping
 		long nowtime = nowdate.getTime();
 		long thentime = 0;
 		if (thendate != null) {
 			thentime = thendate.getTime();
 		}
 			
 		if ((nowtime - thentime) < 6000) {
 			int secondsRemaining = (int) (6-(nowtime - thentime)/1000);
 			String s = "";
 			
 			if (secondsRemaining != 1) {
 				s = "s";
 			}
 			
 			player.sendMessage("You must wait " + ChatColor.AQUA + secondsRemaining + ChatColor.WHITE + " second"+s+" to "+ChatColor.AQUA+"teleport" + ChatColor.WHITE);
 			return;
 		}
 		
 		else {
 			player.sendMessage(ChatColor.AQUA + "Now warping to " + activatedWarpColor + cityname + ChatColor.WHITE);
 		}
 		
 		lastWarpTime.put(player, nowdate);
 		
 		
 		Location teleportLocation;
 		if (cityname.equals("spawn")) {
 			teleportLocation = player.getBedSpawnLocation();
 			//player.get
 		}
 		else {
 			teleportLocation = cityTeleports.get(cityname);
 		}
 		
 		teleportingPlayerQueue.offer(player);
 		teleportingDestinationQueue.offer(teleportLocation);
 		teleportingPlayers.put(player, player.getLocation());
 		
 		
 		
 		// need to continue this function later
 		//trick the client to displaying a warp animation by creating a portal under the player
 		player.sendBlockChange(player.getLocation(), Material.PORTAL, (byte) 0);
 		
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			
 			public void run() {
 				Player player = teleportingPlayerQueue.poll();
 				Location location = teleportingDestinationQueue.poll();
 				
 				if (teleportingPlayers.containsKey(player)) {
 					teleportingPlayers.remove(player);
 					
 					//location.getWorld().strikeLightningEffect(location);
 					
 					Block block = player.getWorld().getBlockAt(player.getLocation());
 					
 					player.sendBlockChange(player.getLocation(), block.getType(), block.getData());
 					
 					// Move the player to another world so they have to reload the chunks or 'simulate the world for a bit'
 					Location otherworld = new Location(getServer().getWorld("shopworld"), 0, 64, 0);
 					player.teleport(otherworld);
 					// Teleport the player to the real location
 					player.teleport(location);
 				}
 			}
 		}, 120L);
 	}
 	
 	////////////////////////////////////////////////////////////////////////////
 	///////////////////////////////////////////////////////////////////////////
 	////////////////////////////////////////////////////////////////////////////
 	public void startFlyerCounter() {
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				for (Entry<String, Integer> entry : flightTimeRemaining.entrySet()) {
 					Player player = getServer().getPlayer(entry.getKey());
 					Integer timeLeft = entry.getValue();
 					timeLeft--;
 					if ((timeLeft % 6) == 0 && timeLeft != 0) {
 						Integer minutesLeft = timeLeft/6;
 						player.sendMessage("" + minutesLeft + " minutes remaining in flight");
 					}
 					
 					if (timeLeft <= 5 && timeLeft > 0) {
 						player.sendMessage(""+(timeLeft*10)+" seconds remaining in flight");
 					}
 					
 					if (timeLeft == 0) {
 						player.sendMessage("Flight Time Up");
 						player.setAllowFlight(false);
 						flightTimeRemaining.remove(entry.getKey());
 					}
 					else {
 						player.setAllowFlight(true);
 						flightTimeRemaining.put(entry.getKey(), timeLeft);
 					}
 				}
 			}
 		}, 0L, 200L);
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// DISPLAY HELPERS //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	public void info(String input) {
 		this.logger.info("  "+pluginTitle + " " +input);
 	}
 	public void severe (String input) {
 		this.logger.severe(pluginTitle+" \033[31m"+input+"\033[0m");
 	}
 }
