 package me.cakenggt.CakesMinerApocalypse;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CakesMinerApocalypse extends JavaPlugin {
 	private Listener playerListener;
 	private Listener moveListener;
 	private Listener blockListener;
 	private CakesMinerApocalypseVaultCreator chunkListener;
 	private Listener nukeListener;
 	private CakesMinerApocalypseBroadcast broadcast;
 	private int size = 10000;
 	private Map<World, Boolean> worldsTable = new HashMap<World, Boolean>();
 	private List<Location> craters;
 	private List<Location> GECKs;
 	private List<Long> craterTimes;
 	private List<Location> radios;
 	private boolean apocalypseDamage = true;
 	private boolean randomSpawn = true;
 	private double shelterChance = 0.001;
 	private double craterChance = 0.001;
 	private int pipboyID = 345;
 	private int chatDistance = 50;
 	private final String configname = "config.yml";
     
     public void onDisable() {
         // TODO: Place any custom disable code here.
         System.out.println(this + " is now disabled!");
     }
     
     public void onEnable() {
     	if(!loadConfig()) {
     		System.out.println(this + " has encountered an error while reading the configuration file," 
     				+ " continuing with defaults");
     	}
 
     	playerListener = new CakesMinerApocalypsePlayerLogin(this);
     	moveListener = new CakesMinerApocalypsePlayerMovement(this);
     	blockListener = new CakesMinerApocalypseBlockListener(this);
     	chunkListener = new CakesMinerApocalypseVaultCreator(this);
     	nukeListener = new CakesMinerApocalypseNuke(this);
     	try {
 			loadCraters();
 			loadGECKs();
 			loadCraterTimes();
 			loadRadios();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	getServer().getPluginManager().registerEvents(playerListener, this);
     	getServer().getPluginManager().registerEvents(moveListener, this);
     	getServer().getPluginManager().registerEvents(blockListener, this);
     	getServer().getPluginManager().registerEvents(chunkListener, this);
     	getServer().getPluginManager().registerEvents(nukeListener, this);
     	this.broadcast = new CakesMinerApocalypseBroadcast(this);
     	try {
 			checkGECKs();
 			checkRadios();
 		} catch (IOException e) {
 			//e.printStackTrace();
 		}
     	final ShapedRecipe gRecipe = new ShapedRecipe(new ItemStack(Material.SPONGE, 1));
         gRecipe.shape("CBC", "BAB", "CBC");
         gRecipe.setIngredient('A', Material.DIAMOND_BLOCK);
         gRecipe.setIngredient('B', Material.IRON_BLOCK);
         gRecipe.setIngredient('C', Material.REDSTONE);
         final FurnaceRecipe fRecipe = new FurnaceRecipe(new ItemStack(Material.GRAVEL, 1), Material.SNOW_BLOCK);
         getServer().addRecipe(gRecipe);
         getServer().addRecipe(fRecipe);
 		if (getConfig().getBoolean("alwaysSneak", true)) {
 			setupRefresh();
 		}
         Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this.broadcast, 20L, 100L);
         System.out.println(this + " is now enabled!");
     }
 
     private void setupRefresh() {
     	Timer refreshTimer = new Timer();
 	     refreshTimer.scheduleAtFixedRate(new TimerTask() {
 	       public void run() {
 	    	   Player[] players = getServer().getOnlinePlayers();
 	       		for (Player p : players) {
 	       			p.setSneaking(false);
 	       			p.setSneaking(true);
 	       		}
 	       	
 	       }
 	     }
 	     , 500L, 5000L);
 	   }
     
 	private boolean loadConfig() {
 	    YamlConfiguration config = new YamlConfiguration();
 	    getDataFolder().mkdirs();
 	    File configfile = new File(getDataFolder(),configname);
 	    try {
 	      config.load(configfile);
 	    } catch (FileNotFoundException e) {
 	      config.set("size", size);
 	      for (World world : getServer().getWorlds()){
 	    	  config.set("worlds." + world.getName(), true);
 	      }
 		config.set("randomSpawn", true);
 		config.set("apocalypseDamage", apocalypseDamage);
 		config.set("shelterChance", shelterChance);
 		config.set("craterChance", craterChance);
 		config.set("pipboyID", pipboyID);
 		config.set("chatDistance", chatDistance);
 		config.set("alwaysSneak", true);    
 		config.set("shelter.lightBlock", "glowstone");
 		//config.set("shelter.lightBlock", "128;1");     // PlasticCraft GlowingPlexiglass
 		config.set("shelter.enabledLootGroups", new String[] { "regular" });
 
 		// Default loot
 		config.set("shelter.loot.regular", new String[] { "pumpkin_seeds", 
 			"bread", "cake", "melon", "mushroom_soup", "cooked_chicken", 
             "cooked_beef", "grilled_pork", "cooked_fish", 
 			"redstone", "356" /* diode item, not block */, "redstone_torch_on", 
             "torch", "iron_fence", "compass", "iron_boots", "iron_chestplate", 
 			"iron_leggings", "melon_seeds", "seeds" });
 
 		// Some examples for reference / inspiration / ease of enabling if mods/plugins installed
 		// These aren't enabled by default (not in enabledLootGroups)
 
     	// http://dev.bukkit.org/server-mods/enchantmore/
 		config.set("shelter.loot.enchantmore", new String[] {
 		   "3xfishing_rod+21@2",              // Fishing Rod + Looting
 		   "4xflint_and_steel;25+5@1",        // Flint & Steel + Respiration
 		   "10xflint_and_steel;60+3@1",       // Flint & Steel + Blast Protection
 		   "1xflint_and_steel;15+17@1",       // Flint & Steel + Smite
 		   "20xiron_helmet+20@1",             // Helmet + Fire Aspect
 		   "1xdiamond_hoe+6@1",               // Hoe + Aqua Affinity
 		   "1xgold_hoe;30+48@1",              // Hoe + Power
 		   "1xiron_hoe+33@1",                 // Hoe + Silk Touch
 		   "1xdiamond_pickaxe+50@1",          // Pickaxe + Flame
 		   "1xiron_pickaxe+16@1",             // Pickaxe + Power
 		   "1xdiamond_pickaxe;1000+33@2",     // Pickaxe + Silk Touch II
 		   "1xdiamond_leggings+2@1",          // Leggings + Feather Falling
 		   "1xshears+18@1",                   // Shears + Bane of Arthropods
 		   "5xshears+21@1+17@1",              // Shears + Looting + Smite
 		   "2xdiamond_spade;1500+48@1",       // Shovel + Power
 		   "1xgold_sword;10+48@1",            // Sword + Power
 		   "1xwood_sword+33@1",               // Sword + Silk Touch
 		   "1xiron_axe+48@1",                 // Axe + Power
 		   "1xleather_boots;10+48@1+49@1",    // Boots + Power + Punch
 		   "10xbow+2@2",                      // Bow + Feather Falling II
 		   "5xleather_chestplate+33@1",       // Chestplate + Silk Touch
 		   "64xarrow" });
 
     	// http://www.minecraftforum.net/topic/182918-11smp-flans-mods-planes-ww2-guns-vehicles-playerapi-moods-mputils-teams/
 		config.set("shelter.loot.plane", new String[] {
 		   "2x23289",  //  metal wing
 		   "2x23267",  //  machine gun
 		   "2x23262",  //  bomb bay
 		   "1x23261",  //  metal tail
 		   "1x23281",  //  metal propeller
 		   "1x23265",  //  metal cockpit
 		   "1x23274",  //  V4 engine
 		   "1x23275",  //  V6 engine
 		   "1x23277",  //  V8 engine
 		   "16x23270", //  bullet
 		   "16x23270", //  bullet
 		   "2x23271",  //  small bomb
 		   "64xcoal",
 		   "2x255" }); //  plane crafting table
 
 		//  http://www.minecraftforum.net/topic/211517-11-balkons-weaponmod-v84-multiplayer/
     	config.set("shelter.loot.balkan", new String[] {
 		   "1x5000",   //  wooden spear
 		   "1x5006",   //  stone halberd
 		   "1x5011",   //  stone battleaxe
 		   "1x5019",   //  gold warhammer
 		   "1x5023",   //  diamond knive
 		   "1x5025",   //  wooden flail
 		   "1x5030",   //  javalin
 		   "1x5033",   //  musket barrel
 		   "5x5037",   //  poisonous dart
 		   "32x5039",  //  fire rod
 		   "1x5040",   //  cannon
 		   "32x5041",  //  cannonball
 		   "32x5043",  //  blunderbuss shot
 		   "1x5046",   //  stock
 		   "1x5045" });//  training dummy
 
 		//  http://www.minecraftforum.net/topic/119361-110-tehkrushs-mods-all-mods-updated-and-plasticcraft-smp/
     	config.set("shelter.loot.plastic", new String[] {
 		   "1x1027",   //  health needle
 		   "1x1027",   //  health needle
 		   "1x1027",   //  health needle
 		   "1x1009",   //  bowl of gelatin
 		   "1x1024",   //  water bottle
 		   "1x1024",   //  water bottle
 		   "1x1025",   //  milk bottle
 		   "1x1028",   //  jello
 		   "16x1013",  //  duct tape
 		   "16x1015",  //  battery
 		   "8x1041",   //  kevlar vest
 		   "8x1042",   //  kevlar pants
 		   "1x1050",   //  plastic shovel
 		   "32x127",   //  C4 plastic explosive
 		   "64x128",   //  plexiglass
 		   "1x1014",   //  plexiglass door
 		   "1x130",    //  microwave oven
 		   "1x1040" });//  night-vision goggles
 
           config.set("shelter.potionLoot", new String[] { "glass_bottle", 
 			"nether_stalk", "glowstone_dust", "redstone", "fermented_spider_eye", "magma_cream",
 			"sugar", "speckled_melon", "spider_eye", "ghast_tear", "blaze_powder", "sulphur" });
 
 	      try {
 	        config.save(configfile);
 	      } catch (IOException e1) {
 	        System.out.println(this + " was unable to create the default config file!");
 	      }
 	    } catch (IOException e) {
 	      e.printStackTrace();
 	      return false;
 	    } catch (InvalidConfigurationException e) {
 	      e.printStackTrace();
 	      return false;
 	    }
 	    this.setSize(config.getInt("size", this.getSize()));
 	    for (World world : getServer().getWorlds()){
 	    	this.setOn(world, config.getBoolean("worlds." + world.getName(), true));
 	    }
 	    this.setRandomSpawn(config.getBoolean("randomSpawn", this.getRandomSpawn()));
 	    this.setApocalypseDamage(config.getBoolean("apocalypseDamage", this.getApocalypseDamage()));
 	    this.setShelterChance(config.getDouble("shelterChance", this.getShelterChance()));
 	    this.setCraterChance(config.getDouble("craterChance", this.getCraterChance()));
 	    this.setPipboyID(config.getInt("pipboyID", this.getPipboyID()));
 	    this.setChatDistance(config.getInt("chatDistance", this.getChatDistance()));
 	    return true;
 	  }
 
 	public void checkGECKs() throws IOException {
 		if (new File("plugins/CakesMinerApocalypse/").mkdirs())
 			System.out.println("GECK file created");
 		File myFile = new File("plugins/CakesMinerApocalypse/GECKs.txt");
 		if (!myFile.exists()){
 			PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/GECKs.txt");
 			System.out.println("GECK file created");
 			outputFile.close();
 		}
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> GECKs = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			GECKs.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		PrintWriter outputFile = new PrintWriter(myFile);
 		if (GECKs == null){
 			outputFile.close();
 			return;
 		}
 		for (Location GECK : GECKs){
 			Block block = GECK.getBlock();
 			if (block.getRelative(BlockFace.NORTH).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.SOUTH).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.EAST).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.WEST).getType() == Material.PISTON_BASE){
 				outputFile.println(block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
 			}
 		}
 		outputFile.close();
 	}
 	
 	public void checkRadios() throws IOException {
 		if (new File("plugins/CakesMinerApocalypse/").mkdirs())
 			System.out.println("radio file created");
 		File myFile = new File("plugins/CakesMinerApocalypse/radios.txt");
 		if (!myFile.exists()){
 			PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/radios.txt");
 			System.out.println("radio file created");
 			outputFile.close();
 		}
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> radios = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			radios.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		PrintWriter outputFile = new PrintWriter(myFile);
 		if (radios == null){
 			outputFile.close();
 			return;
 		}
 		for (Location radio : radios){
 			Block block = radio.getBlock();
 			if (block.isBlockIndirectlyPowered()){
 				outputFile.println(block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
 			}
 		}
 		outputFile.close();
 	}
 	
 	public void setSize(int size) {
 		this.size = size;
 	}
 	
 	public int getSize() {
 		return size;
 	}
 
 	public void setApocalypseDamage(boolean apocalypseDamage) {
 		this.apocalypseDamage = apocalypseDamage;
 	}
 	
 	public boolean getApocalypseDamage() {
 		return apocalypseDamage;
 	}
 
 	public void setOn(World world, boolean isOn){
 		this.worldsTable.put(world, isOn);
 	}
 
 	public Map<World, Boolean> getOn(){
 		return worldsTable;
 	}
 
 	public void setRandomSpawn(boolean randomSpawn){
 		this.randomSpawn = randomSpawn;
 	}
 	
 	public boolean getRandomSpawn() {
 		return randomSpawn;
 	}
 	
 	public void setShelterChance(double shelterChance) {
 		this.shelterChance = shelterChance;
 	}
 	
 	public double getShelterChance() {
 		return shelterChance;
 	}
 	
 	public void setCraterChance(double craterChance) {
 		this.craterChance = craterChance;
 	}
 	
 	public double getCraterChance() {
 		return craterChance;
 	}
 	
 	public void setPipboyID(int id) {
 		this.pipboyID = id;
 	}
 	
 	public int getPipboyID() {
 		return pipboyID;
 	}
 	
 	public void setChatDistance(int distance) {
 		this.chatDistance = distance;
 	}
 	
 	public int getChatDistance() {
 		return chatDistance;
 	}
 	
 	public void loadCraters() throws IOException {
 		File myFile = new File("plugins/CakesMinerApocalypse/craters.txt");
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> craters = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			craters.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		this.craters = craters;
 	}
 	public List<Location> getCraters() {
 		return craters;
 	}
 	public void loadGECKs() throws IOException{
 		File myFile = new File("plugins/CakesMinerApocalypse/GECKs.txt");
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> GECKs = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			GECKs.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		this.GECKs = GECKs;
 	}
 	public List<Location> getGECKs() {
 		return GECKs;
 	}
 	public void loadCraterTimes() throws IOException {
 		File myFile = new File("plugins/CakesMinerApocalypse/craterTimes.txt");
 		Scanner inputFile = new Scanner(myFile);
 		List<Long> craterTimes = new ArrayList<Long>();
 		while (inputFile.hasNextLine()){
 			Long a = Long.valueOf(inputFile.next());
 			craterTimes.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		this.craterTimes = craterTimes;
 	}
 	public List<Long> getCraterTimes() {
 		return craterTimes;
 	}
 	public void loadRadios() throws IOException {
 		File myFile = new File("plugins/CakesMinerApocalypse/radios.txt");
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> radios = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			radios.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		this.radios = radios;
 	}
 	public List<Location> getRadios() {
 		return radios;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 	   	if(cmd.getName().equalsIgnoreCase("radio")){ // If the player typed /radio then do the following...
 	   		Player player = null;
     		if (sender instanceof Player) {
     			player = (Player) sender;
 	    	}
 	   		if (player == null) {
 	   			sender.sendMessage("this command can only be run by a player");
 	   			return false;
 	   		}
 	   		if (player.getItemInHand().getTypeId() != getPipboyID()){
 	   			player.sendMessage("You must be holding a compass to work the radio");
 	   			return true;
 	   		}
     		if (args.length != 1){
 	    		sender.sendMessage(cmd.getUsage());
 	   			return true;
 	   		}
     		if (args[0].equalsIgnoreCase("scan")){
     			try {
 					setFrequency(sender, args[0].toString());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return true;
     		}
     		if (args[0].equalsIgnoreCase("version")){
     			String name = this.toString();
     			player.sendMessage(name);
     			return true;
     		}
     		try {
 				Double.parseDouble(args[0].toString());
 		    }
 			catch (NumberFormatException e) {
 				sender.sendMessage(cmd.getUsage());
 				return true;
 		    }
 	   		String inputString = args[0].toString();
 	   		try {
 	   			setFrequency(sender, inputString);
     		} catch (IOException e) {
 	    		e.printStackTrace();
     		}
     		return true;
 	    }
 	   	if(cmd.getName().equalsIgnoreCase("support")){
 	   		String message = "";
 	   		for (String part : args){
 	   			message = message + " " + part;
 	   		}
 	   		Player[] recipientsArray = sender.getServer().getOnlinePlayers();
 			for (int i = 0; i < recipientsArray.length; i ++){
 				if (recipientsArray[i].isOp()){
 					recipientsArray[i].sendMessage(ChatColor.BLUE + "[Support Chat] " + sender.getName() + ":" + message);
 				}
 			}
 	   		return true;
 	   	}
 
 		if (cmd.getName().equalsIgnoreCase("placeshelter")) {
 			World world = null;
 			if (sender instanceof Player) {
 				if (!((Player)sender).hasPermission("cakesminerapocalypse.placeshelter")) {
 					sender.sendMessage("You do not have permission to place fallout shelters");
 					return true;
 				}
 				world = ((Player)sender).getWorld();
 			}
 
 			// Get location to generate shelter
 			int x = 0, y = 0, z = 0;
 			if (args.length < 3) {
 				if (args.length == 1 && args[0].equalsIgnoreCase("here") && sender instanceof Player) {
 					x = ((Player)sender).getLocation().getBlockX();
 					y = ((Player)sender).getLocation().getBlockY();
 					z = ((Player)sender).getLocation().getBlockZ();
 				} else {
 					sender.sendMessage("Missing required arguments");
 					return false;
 				}
 			} else {
 				try {
 					x = Integer.parseInt(args[0]);
 					y = Integer.parseInt(args[1]);
 					z = Integer.parseInt(args[2]);
 				} catch (NumberFormatException e) {
 					sender.sendMessage("Invalid coordinates");
 					return false;
 				}
 			}
 
 			if (args.length >= 4) {
 				world = Bukkit.getWorld(args[3]);
 			} else if (world == null) {
 				world = Bukkit.getWorlds().get(0);
 			}
 
 			chunkListener.placeShelter(new Location(world, x, y, z));
 			return true;
 		}
 	    	return false;
 	}
 	    
 	   public void setFrequency (CommandSender sender, String inputString) throws IOException{
 		   if (new File("plugins/CakesMinerApocalypse/").mkdirs())
 				System.out.println("Frequencies file created");
 		   File myFile = new File("plugins/CakesMinerApocalypse/frequencies.txt");
 		   if (myFile.exists()){
 			   Scanner inputFileCheck = new Scanner(myFile);
 	           int j = 0;
 	           while (inputFileCheck.hasNext()) {
             	inputFileCheck.nextLine();
             	j++;
 	           }
 	           int size = (j + 1) / 2;
 	           String[] nameArray = new String[size];
 	           String[] circleArray = new String[size];
 	           inputFileCheck.close();
 	           Scanner inputFile = new Scanner(myFile);
 	           for (int i = 0; i < size; i++) {
 	               nameArray[i] = inputFile.nextLine();
 	               circleArray[i] = inputFile.nextLine();
 	           }
 	           boolean isInFile = false;
 	           for (int i = 0; i < size; i++) {
 	               if (nameArray[i].equalsIgnoreCase(sender.getName())){
 	               		circleArray[i] = inputString;
 	               		sender.sendMessage("Frequency " + inputString + " set successfully!");
 	               		isInFile = true;
 	               }
 	           }
 	           inputFile.close();
 	           PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/frequencies.txt");
 	           for (int i = 0; i < size; i++) {
 	               outputFile.println(nameArray[i]);
 	               outputFile.println(circleArray[i]);
 	           }
 	           if (!isInFile){
 	        	   outputFile.println(sender.getName());
 	               outputFile.println(inputString);
 	           }
 	           outputFile.close();
 		}
 		else{
 			PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/frequencies.txt");
 	        outputFile.println(sender.getName());
 	        outputFile.println(inputString);
 	        sender.sendMessage("frequency " + inputString + " set successfully!");
 	        outputFile.close();
 		}
     }
 }
