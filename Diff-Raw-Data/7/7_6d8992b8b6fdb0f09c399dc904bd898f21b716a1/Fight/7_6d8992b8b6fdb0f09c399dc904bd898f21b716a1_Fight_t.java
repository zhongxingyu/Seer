 package me.ashconnell.Fight;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.Location;
 import org.bukkit.plugin.Plugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.iConomy.*;
 import com.iConomy.system.Holdings;
 
 public class Fight extends JavaPlugin {
 	
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public static PermissionHandler permissionHandler;
 	public iConomy iConomy = null;
 	
 	private final FightSignListener signListener = new FightSignListener(this);
 	private final FightReadyListener readyListener = new FightReadyListener(this);
 	private final FightRespawnListener respawnListener = new FightRespawnListener(this);
 	private final FightDeathListener deathListener = new FightDeathListener(this);
 	private final FightDropListener dropListener = new FightDropListener(this);
 	private final FightServerListener serverListener = new FightServerListener(this);
 	
     public final Map<String, String> fightUsersTeam = new HashMap<String, String>();
     public final Map<String, String> fightUsersClass = new HashMap<String, String>();
     public final Map<String, String> fightClasses = new HashMap<String, String>();
     public final Map<String, Sign> fightSigns = new HashMap<String, Sign>();
     public final Map<String, String> fightUsersRespawn = new HashMap<String, String>();
     
     int redTeam = 0;
     int blueTeam = 0;
     
     boolean redTeamIronClicked = false;
     boolean blueTeamIronClicked = false;    
     
     boolean fightInProgress = false;
     
     int rewardAmount;
     String rewardItems;
     
 	public void onEnable() {
 		
 		setupPermissions();
 		
 		// Event Registration
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, signListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, readyListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_RESPAWN, respawnListener, Event.Priority.Highest, this);
 		pm.registerEvent(Event.Type.ENTITY_DEATH, deathListener, Event.Priority.Highest, this);
 		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, dropListener, Event.Priority.Highest, this);
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Event.Priority.Monitor, this);
 		
 		
		log.info("[Fight] Plugin Started. (version 1.1.2)");
 		
 		// Create Config if Non-Existant
 		new File("plugins/Fight").mkdir();
 		File configFile = new File("plugins/Fight/config.yml");
 		if(!configFile.exists()){
 		    try { configFile.createNewFile(); } catch(Exception e){ log.info("[Fight] Error when creating config file."); }
 		}
 		
 		// Load Classes From Config
 		Configuration config = new Configuration(configFile);
 		config.load();
 		List<String> classes;
 		// Set up default config file if it does not exist
 		if(config.getKeys("classes") == null){
 			config.setProperty("classes.Ranger.items", "261,262:64,298,299,300,301");
 			config.setProperty("classes.Swordsman.items", "276,306,307,308,309");
 			config.setProperty("classes.Tank.items", "272,310,311,312,313");
 			config.setProperty("classes.Pyro.items", "259,46:2,298,299,300,301");
			config.setProperty("rewards.amount", 0);
			config.setProperty("rewards.items", "none");
 			config.save();
 		}
 		classes = config.getKeys("classes");
 		log.info("[Fight] Loaded " + classes.size() + " Classes.");
 		
 		// Load Classes
 		for(int i=0; i < classes.size(); i++){
 			String className = classes.get(i);
 			fightClasses.put(className, config.getString("classes." + className + ".items", null));
 		}
 		
 		// Load Rewards
 		rewardAmount = config.getInt("rewards.amount", 0);
 		rewardItems = config.getString("rewards.items", "none");
 	}
 
 	public void onDisable() {
 		log.info("[Fight] Plugin Stopped.");
 		cleanSigns();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		String[] fightCmd = args;
 		
 		if(commandLabel.equalsIgnoreCase("Fight")){
 			Player player = (Player) sender;
 			
 			// Command: /Fight
 			if(args.length < 1 && this.isSetup() && !fightInProgress && hasPermissions(player, "user")){
 				
 				// Check For Empty Inventory				
 				if(emptyInventory(player) == true){
 
 					// Add New Users To Map
 					if(!this.fightUsersTeam.containsKey(player.getName())){
 						this.fightUsersTeam.put(player.getName(), "none");
 					}
 					
 					// Pick Team and Teleport To Lounge
 					if(fightUsersTeam.get(player.getName()) == "none"){
 						if(blueTeam > redTeam){
 							fightUsersTeam.put(player.getName(), "red");
 							tellPlayer(player, "Welcome! You are on team " + ChatColor.RED + "<Red>");
 							redTeam++;
 							player.teleport(getCoords("redlounge"));
 						}
 						else {
 							fightUsersTeam.put(player.getName(), "blue");
 							blueTeam++;
 							tellPlayer(player, "Welcome! You are on team " + ChatColor.BLUE + "<Blue>");
 							player.teleport(getCoords("bluelounge"));
 						}
 					}
 					
 					// If Already In Team, Teleport To Lounge
 					else {
 						String team = fightUsersTeam.get(player.getName());
 						player.teleport(getCoords(team + "lounge"));
 					}	
 				}
 				
 				// Errors
 				else {
 					if(fightUsersTeam.containsKey(player.getName())){
 						tellPlayer(player, "You have already joined a team!");
 					} else {
 						tellPlayer(player, "You must have an empty inventory to join a Fight!");
 					}
 				}
 			}
 			// Errors
 			else if(args.length < 1){
 				if(!this.isSetup()){
 					tellPlayer(player, "All Waypoints must be set up first.");
 				}
 				if(fightInProgress){
 					tellPlayer(player, "A Fight is already in progress");
 				}
 			}
 			
 			// Command: /Fight <argument>
 			if(args.length == 1){
 				
 				// Command: /Fight RedLounge
 				if(fightCmd[0].equalsIgnoreCase("redlounge") && hasPermissions(player, "admin")){
 					setCoords(player, "redlounge");
 					tellPlayer(player, "Red Lounge Set.");
 				}
 				
 				// Command: /Fight RedSpawn
 				else if(fightCmd[0].equalsIgnoreCase("redspawn") && hasPermissions(player, "admin")){
 					setCoords(player, "redspawn");
 					tellPlayer(player, "Red Spawn Set.");
 				}
 				
 				// Command: /Fight BlueLounge
 				else if(fightCmd[0].equalsIgnoreCase("bluelounge") && hasPermissions(player, "admin")){
 					setCoords(player, "bluelounge");
 					tellPlayer(player, "Blue Lounge Set.");
 				}
 				
 				// Command: /Fight BlueSpawn
 				else if(fightCmd[0].equalsIgnoreCase("bluespawn") && hasPermissions(player, "admin")){
 					setCoords(player, "bluespawn");
 					tellPlayer(player, "Blue Spawn Set.");
 				}
 				
 				// Command: /Fight Spectator
 				else if(fightCmd[0].equalsIgnoreCase("spectator") && hasPermissions(player, "admin")){
 					setCoords(player, "spectator");
 					tellPlayer(player, "Spectator Area Set.");
 				}
 				
 				// Command: /Fight Exit
 				else if(fightCmd[0].equalsIgnoreCase("exit") && hasPermissions(player, "admin")){
 					setCoords(player, "exit");
 					tellPlayer(player, "Exit Area Set.");
 				}
 				
 				// Command: /Fight Watch
 				else if(fightCmd[0].equalsIgnoreCase("watch") && this.isSetup() && hasPermissions(player, "user")){
 					
 					// Teleport To Spectator Area
 					player.teleport(getCoords("spectator"));
 					tellPlayer(player, "Welcome to the spectator's area!");
 					if(fightUsersTeam.containsKey(player.getName())){
 						if(fightUsersTeam.get(player.getName()) == "red"){
 							redTeam = redTeam - 1;
 							log.info("red:" + redTeam + " blue:" + blueTeam);
 						}
 						if(fightUsersTeam.get(player.getName()) == "blue"){
 							blueTeam = blueTeam - 1;
 							log.info("red:" + redTeam + " blue:" + blueTeam);
 						}
 						fightUsersTeam.remove(player.getName());
 						fightUsersClass.remove(player.getName());
 						cleanSigns(player.getName());
 						player.getInventory().clear();
 						clearArmorSlots(player);
 					}
 				}
 				
 				// Command: /Fight Leave
 				else if(fightCmd[0].equalsIgnoreCase("leave") && hasPermissions(player, "user")){
 					if(fightUsersTeam.containsKey(player.getName())){
 						if(fightUsersTeam.get(player.getName()) == "red"){
 							redTeam = redTeam - 1;
 						}
 						if(fightUsersTeam.get(player.getName()) == "blue"){
 							blueTeam = blueTeam - 1;
 						}
 						tellPlayer(player, "You have left the fight.");
 						fightUsersTeam.remove(player.getName());
 						fightUsersClass.remove(player.getName());
 						cleanSigns(player.getName());
 						player.getInventory().clear();
 						clearArmorSlots(player);
 						player.teleport(getCoords("exit"));
 					}
 					else {
 						tellPlayer(player, "You are not in a team.");
 					}
 				}
 				
 				// Invalid Command
 				else {
 					tellPlayer(player, "Invalid Command. (503)");
 				}
 			}
 			
 			// Command: /Fight <argument> <argument> cont...
 			if(args.length > 1){
 				tellPlayer(player, "Invalid Command. (504)");
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	// Set Coords (to config.yml)
 	public void setCoords(Player player, String place) {
 		Location location = player.getLocation();
 		File configFile = new File("plugins/Fight/config.yml");
 		Configuration config = new Configuration(configFile);
 		config.load();
 		config.setProperty("coords." + place + ".world", location.getWorld().getName());
 		config.setProperty("coords." + place + ".x", location.getX());
 		config.setProperty("coords." + place + ".y", location.getY());
 		config.setProperty("coords." + place + ".z", location.getZ());
 		config.setProperty("coords." + place + ".yaw", location.getYaw());
 		config.setProperty("coords." + place + ".pitch", location.getPitch());
 		config.save();
 	}
 	
 	// Get Coords (from config.yml)
 	public Location getCoords(String place){
 		File configFile = new File("plugins/Fight/config.yml");
 		Configuration config = new Configuration(configFile);
 		config.load();
 		Double x = config.getDouble("coords." + place + ".x", 0);
 		Double y = config.getDouble("coords." + place + ".y", 0);
 		Double z = config.getDouble("coords." + place + ".z", 0);
 		Float yaw = new Float(config.getString("coords." + place + ".yaw"));
 		Float pitch = new Float(config.getString("coords." + place + ".pitch"));
 		World world = Bukkit.getServer().getWorld(config.getString("coords." + place + ".world"));
 		return new Location(world, x, y, z, yaw, pitch);
 	}
 	
 	// Check if all Waypoints have been set.
 	public Boolean isSetup(){
 		File configFile = new File("plugins/Fight/config.yml");
 		Configuration config = new Configuration(configFile);
 		config.load();
 		if(config.getKeys("coords") == null){
 			return false;
 		}
 		else{
 			List<String> list = config.getKeys("coords");
 			if(list.size() == 6){
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 	}
 	
 	// Give Player Class Items
 	public void giveItems(Player player){
 		String playerClass = fightUsersClass.get(player.getName());
 		String rawItems = fightClasses.get(playerClass);
 		String[] items;
 		items = rawItems.split(",");
 		for(int i=0; i < items.length; i++){
 			String item = items[i];
 			String[] itemDetail = item.split(":");
 			if(itemDetail.length == 2){
 				int x = Integer.parseInt(itemDetail[0]);
 				int y = Integer.parseInt(itemDetail[1]);
 				ItemStack stack = new ItemStack (x, y);
 				player.getInventory().setItem(i, stack);
 			}
 			else{
 				int x = Integer.parseInt(itemDetail[0]);
 				ItemStack stack = new ItemStack (x, 1);
 				player.getInventory().setItem(i, stack);
 			}
 		}
 	}
 	
 	// Permissions Support
 	private void setupPermissions() {
 	      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
 	      if (Fight.permissionHandler == null) {
 	          if (permissionsPlugin != null) {
 	              Fight.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 	          } else {
 	              log.info("Permission system not detected, defaulting to OP");
 	          }
 	      }
 	  }
 	
 	// Return True If Player Has Permissions
 	private boolean hasPermissions(Player player, String type){
 		if(type == "admin"){
 			if(Fight.permissionHandler.has(player, "fight.admin")){
 				return true;
 			}
 			else { return false; }
 		}
 		else if(type == "user"){
 			if(Fight.permissionHandler.has(player, "fight.user")){
 				return true;
 			}
 			else { return false; }
 		}
 		else {
 			return false;
 		}
 	}
 	
 	// Clean Up All Signs People Have Used For Classes
 	public void cleanSigns(){
 		Set<String> set = fightSigns.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			Sign sign = fightSigns.get(o.toString());
 			sign.setLine(2, "");
 			sign.setLine(3, "");
 			sign.update();
 		}
 	}
 	
 	// Clean Up Signs Specific Player Has Used For Classes
 	public void cleanSigns(String player){
 		Set<String> set = fightSigns.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			Sign sign = fightSigns.get(o.toString());
 			if(sign.getLine(2) == player){
 				sign.setLine(2, "");
 				sign.update();
 			}
 			if(sign.getLine(3) == player){
 				sign.setLine(3, "");
 				sign.update();
 			}
 		}
 	}
 	
 	// Check If Team Has All Chosen A Class
 	public boolean teamReady(String color){
 		int members = 0;
 		int membersReady = 0;
 		Set<String> set = fightUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			if(fightUsersTeam.get(o.toString()) == color){
 				members++;
 				if(fightUsersClass.containsKey(o.toString())){
 					membersReady++;
 				}
 			}
 		}		
 		if(members == membersReady && members > 0){
 			if(color == "red"){
 				return true;
 			}
 			if(color =="blue"){
 				return true;
 			}
 		}
 		else{
 			return false;
 		}
 		return false;
 	}
 	
 	// Tell All Fight Players
 	public void tellEveryone(String msg){
 		Set<String> set = fightUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			Player z = getServer().getPlayer(o.toString());
 			z.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + msg);
 		}
 	}
 	
 	
 	// Tell Fight Team Mates
 	public void tellTeam(String color, String msg){
 		Set<String> set = fightUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			if(fightUsersTeam.get(o.toString()) == color){
 				Player z = getServer().getPlayer(o.toString());
 				z.sendMessage(ChatColor.YELLOW + "[Fight] " + msg);
 			}
 		}
 	}
 	
 	// Tell Fight User 
 	public void tellPlayer(Player player, String msg){
 		player.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + msg);
 	}
 	
 	// Teleport All Fight Players To Spawn
 	public void teleportAllToSpawn(){
 		Set<String> set = fightUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			if(fightUsersTeam.get(o.toString()) == "red"){
 				Player z = getServer().getPlayer(o.toString());
 				z.teleport(getCoords("redspawn"));
 			}
 			if(fightUsersTeam.get(o.toString()) == "blue"){
 				Player z = getServer().getPlayer(o.toString());
 				z.teleport(getCoords("bluespawn"));
 			}
 		}
 	}
 	
 	// Clear All Armor Slots
 	public void clearArmorSlots(Player player){
 		player.getInventory().setHelmet(null);
 		player.getInventory().setBoots(null);
 		player.getInventory().setChestplate(null);
 		player.getInventory().setLeggings(null);
 	}
 	
 	// Checks to see if player has no items and armor
 	public boolean emptyInventory(Player player){
 		ItemStack[] invContents = player.getInventory().getContents();
 		ItemStack[] armContents = player.getInventory().getArmorContents();
 		int invNullCounter = 0;
 		int armNullCounter = 0;
 		for(int i=0; i < invContents.length; i++){
 			if(invContents[i]==null){
 				invNullCounter++;
 			}
 		}
 		for(int i=0; i < armContents.length; i++){
 			if(armContents[i].getType()==Material.AIR){
 				armNullCounter++;
 			}
 		}
 		if(invNullCounter == invContents.length && armNullCounter == armContents.length){
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	// Give player iConomy and Item rewards
 	public void giveRewards(Player player) {
 		if (rewardAmount <= 0){
 			return;
 		}
 		if (iConomy != null) {
 			Holdings balance = com.iConomy.iConomy.getAccount(player.getName()).getHoldings();
 			balance.add(rewardAmount);
 			tellPlayer(player, "You have been awarded " + com.iConomy.iConomy.format(rewardAmount));
 		}
 		if(rewardItems != "none"){
 			String[] items;
 			items = rewardItems.split(",");
 			for(int i=0; i < items.length; i++){
 				String item = items[i];
 				String[] itemDetail = item.split(":");
 				if(itemDetail.length == 2){
 					int x = Integer.parseInt(itemDetail[0]);
 					int y = Integer.parseInt(itemDetail[1]);
 					ItemStack stack = new ItemStack (x, y);
 					player.getInventory().setItem(i, stack);
 				}
 				else{
 					int x = Integer.parseInt(itemDetail[0]);
 					ItemStack stack = new ItemStack (x, 1);
 					player.getInventory().setItem(i, stack);
 				}
 			}
 		}
 	}
 }
