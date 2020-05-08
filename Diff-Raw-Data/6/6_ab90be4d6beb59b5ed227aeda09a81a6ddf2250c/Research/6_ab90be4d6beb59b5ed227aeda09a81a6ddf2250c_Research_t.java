 package net.croxis.plugins.research;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Research extends JavaPlugin {
 	static TechManager techManager;
 	public static boolean debug = false;
 	
 	public static Logger logger;
 	
 	private FileConfiguration techConfig = null;
 	//private File techConfigFile = new File(getDataFolder(), "tech.yml");
 	private File techConfigFile = null;
 	private RBlockListener blockListener = new RBlockListener();
 	private RPlayerListener playerListener = new RPlayerListener();
 	
 	public static HashSet<Integer> validIds = new HashSet<Integer>();
 	
     public void onDisable() {
         // TODO: Place any custom disable code here.
         System.out.println(this + " is now disabled!");
     }
     
     public static void logDebug(String message){
     	if(debug)
     		logger.log(Level.INFO, "[Research - Debug] " + message);
     }
     
     public static void logInfo(String message){
     	logger.log(Level.INFO, "[Research] " + message);
     }
     
     public void logWarning(String message){
     	logger.log(Level.WARNING, "[Research] " + message);
     }
 
 	public void onEnable() {
     	logger = Logger.getLogger(JavaPlugin.class.getName());
     	loadPlugin();
         logInfo("Mounting player database.");
         setupDatabase();
         logInfo("Database mounted. Setup complete.");
         
         // This should be it. Permission setups should happen onPlayerJoin
         
         this.getServer().getPluginManager().registerEvents(playerListener, this);
         this.getServer().getPluginManager().registerEvents(blockListener, this);
         this.getServer().getPluginManager().registerEvents(blockListener, this);
         this.getServer().getPluginManager().registerEvents(new RInventoryListener(), this);
         
         System.out.println(this + " is now enabled!");
     }
 	
 	public void reloadPlugin(){
 		unloadPlugin();
 		loadPlugin();
 		
 		for (Player player : getServer().getOnlinePlayers()){
 			if(player.hasPermission("research")){
 				TechManager.initPlayer(player);
 				Tech t = TechManager.getCurrentResearch(player);
 				if(t == null){
 					t = new Tech();
 					t.name = "None";
 				}
 				if(player.hasPermission("research.logininfo"))
 					player.sendMessage("You currently know " + TechManager.getResearched(player).size() + " technologies" +
 	        		" and are currently researching " + t.name + ".");
 			}
 		}
 	}
 	
 	public void unloadPlugin(){
 		techManager = null;
 		validIds = new HashSet<Integer>();
 		
 	}
 	
 	public void loadPlugin(){
 		techManager = new TechManager(this);
     	// Set up default systems
     	debug = this.getConfig().getBoolean("debug", false);
     	TechManager.permissions = new HashSet<String>(this.getConfig().getStringList("default.permissions"));
     	TechManager.canPlace = new HashSet<Integer>( this.getConfig().getIntegerList("default.canPlace"));
     	TechManager.canBreak = new HashSet<Integer>( this.getConfig().getIntegerList("default.canBreak"));
     	TechManager.canCraft = new HashSet<Integer>( this.getConfig().getIntegerList("default.canCraft"));
     	TechManager.cantUse = new HashSet<Integer>( this.getConfig().getIntegerList("default.cantUse"));
     	
     	Research.logDebug("==Defaults==");
     	Research.logDebug("Can place: " + TechManager.canPlace.toString());
     	
     	ConfigurationSection ranges = getConfig().getConfigurationSection("ranges");
     	Set<String> keys = ranges.getKeys(false);   
     	for (String key : keys){
     		List<Integer> item = getConfig().getIntegerList("ranges." + key);
     		for(int i = item.get(0); i < item.get(1); i++){
     			validIds.add(i);
     		}
     	}
 
     	
     	getConfig().options().copyDefaults(true);
         saveConfig();
         logInfo("Loaded default permissions. Now loading techs.");        
     	logDebug("Valid Ids: " + validIds.toString());
         // Load tech config
        reloadTechConfig();
        //getTechConfig().options().copyDefaults(true);
        //saveTechConfig();
         
         Set<String> techNames = techConfig.getKeys(false);
         int i = 0;
         for (String techName : techNames){
         	Tech tech = new Tech();
         	tech.name = techName;
         	if(!techConfig.contains(techName + ".cost"))
         		continue;
         	logDebug("Loading " + tech.name + " with recorded cost " + Integer.toString(techConfig.getInt(techName + ".cost")));
         	tech.cost = techConfig.getInt(techName + ".cost");        	
         	if(techConfig.contains(techName + ".permissions"))
         		tech.permissions = new HashSet<String>( techConfig.getStringList(techName + ".permissions"));        	
         	if(techConfig.contains(techName + ".description"))
         		tech.description = techConfig.getString(techName + ".description");
         	if(techConfig.contains(techName + ".prereqs"))
         		tech.preReqs = new HashSet<String>( techConfig.getStringList(techName + ".prereqs"));        	
         	if(techConfig.contains(techName + ".canPlace"))
         		tech.canPlace = new HashSet<Integer>( techConfig.getIntegerList(techName + ".canPlace"));        	
         	if(techConfig.contains(techName + ".canBreak"))
         		tech.canBreak = new HashSet<Integer>( techConfig.getIntegerList(techName + ".canBreak"));        	
     		if(techConfig.contains(techName + ".canCraft"))
         		tech.canCraft = new HashSet<Integer>( techConfig.getIntegerList(techName + ".canCraft"));
     		if(techConfig.contains(techName + ".canUse"))
         		tech.canUse = new HashSet<Integer>( techConfig.getIntegerList(techName + ".canUse"));
         	
         	TechManager.techs.put(techName, tech);
         	i++;
         }        
         logInfo(Integer.toString(i) + " techs loaded. Now linking tree.");
         
         for (Tech tech : TechManager.techs.values()){
         	// Check if it is a starting tech
         	//if(tech.preReqs.isEmpty()){
         	//	tech.parents.add(techManager.techs.get("root"));
         	//	techManager.techs.get("root").children.add(tech);
         	//} else {
         		for(String parentName : tech.preReqs){
         			if(TechManager.techs.containsKey(parentName)){
         				tech.parents.add(TechManager.techs.get(parentName));
         				TechManager.techs.get(parentName).children.add(tech);
         			} else {
         				this.logWarning("Could not link " + tech.name + " with " + parentName + ". One of them is malformed!");
         			}
         		}
         	//}
         }
         logInfo("Tech tree linking complete.");
 	}
     
     public void reloadTechConfig() {
     	if (techConfigFile == null) {
     	    techConfigFile = new File(getDataFolder(), "tech.yml");
     	    }
         techConfig = YamlConfiguration.loadConfiguration(techConfigFile);
      
         // Look for defaults in the jar
         InputStream defConfigStream = getResource("tech.yml");
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             techConfig.setDefaults(defConfig);
         }
     }
     
     public FileConfiguration getTechConfig() {
         if (techConfig == null) {
             reloadTechConfig();
         }
         return techConfig;
     }
     
     public void saveTechConfig() {
         try {
             techConfig.save(techConfigFile);
         } catch (IOException ex) {
             logger.log(Level.SEVERE, "Could not save config to " + techConfigFile, ex);
         }
     }
     
     private void setupDatabase() {
         try {
             getDatabase().find(SQLPlayer.class).findRowCount();
         } catch (PersistenceException ex) {
             System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
             installDDL();
         }
     }
     
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(SQLPlayer.class);
         return list;
     }
     
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
     	if(args.length == 0)
     		return false;
     	if(args[0].equalsIgnoreCase("player") && args.length == 2){
     		RPlayer rplayer = TechManager.players.get(getServer().getPlayer(args[1]));
     		sender.sendMessage("[Research] Debug info for " + args[1]);
     		sender.sendMessage("CantPlace: " + rplayer.cantPlace.toString());
     		sender.sendMessage("CantBreak: " + rplayer.cantBreak.toString());
     		sender.sendMessage("CantCraft: " + rplayer.cantCraft.toString());
     		sender.sendMessage("CantUse: " + rplayer.cantUse.toString());
     	} else if (args[0].equalsIgnoreCase("reload")){
     		sender.sendMessage("Initiating reload");
     		reloadPlugin();
     	} else if (args[0].equalsIgnoreCase("debug")){
     		debug = !debug;
     		sender.sendMessage("Research debug toggled");
     	}
     	return true;
     }
 }
