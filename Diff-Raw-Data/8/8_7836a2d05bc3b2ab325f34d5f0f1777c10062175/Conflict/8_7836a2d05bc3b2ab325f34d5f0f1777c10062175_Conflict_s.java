 package Lihad.Conflict;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import com.nuclearw.firstlastseen.FirstLastSeen;
 
 import Lihad.Conflict.Command.CommandHandler;
 import Lihad.Conflict.Information.BeyondInfo;
 import Lihad.Conflict.Listeners.BeyondBlockListener;
 import Lihad.Conflict.Listeners.BeyondEntityListener;
 import Lihad.Conflict.Listeners.BeyondPlayerListener;
 import Lihad.Conflict.Listeners.BeyondPluginListener;
 import Lihad.Conflict.Listeners.BeyondSafeModeListener;
 import Lihad.Conflict.Util.BeyondUtil;
 
 public class Conflict extends JavaPlugin {
 	/** Name of the plugin, used in output messages */
 	protected static String name = "Conflict";
 	/** Header used for console and player output messages */
 	protected static String header = "[" + name + "] ";
 
 	public static YamlConfiguration information;
 
 	public static PermissionHandler handler;
 	public static PermissionManager ex;
 	private static Logger log = Logger.getLogger("Minecraft");
 
     //public static City Abatton = new City();
     //public static City Oceian = new City();
     //public static City Savania = new City();
     
 	public static List<String> ABATTON_PLAYERS = new LinkedList<String>();
 	public static Location ABATTON_LOCATION;
 	public static Location ABATTON_LOCATION_SPAWN;
 	public static Location ABATTON_LOCATION_DRIFTER;
 	public static List<String> ABATTON_GENERALS = new LinkedList<String>();
 	public static List<String> ABATTON_TRADES = new LinkedList<String>();
 	public static List<String> ABATTON_TRADES_TEMP = new LinkedList<String>();
 	public static List<String> ABATTON_PERKS = new LinkedList<String>();
 	public static int ABATTON_WORTH;
 	public static int ABATTON_PROTECTION;
 	public static List<String> OCEIAN_PLAYERS = new LinkedList<String>();
 	public static Location OCEIAN_LOCATION;
 	public static Location OCEIAN_LOCATION_SPAWN;
 	public static Location OCEIAN_LOCATION_DRIFTER;
 	public static List<String> OCEIAN_GENERALS = new LinkedList<String>();
 	public static List<String> OCEIAN_TRADES = new LinkedList<String>();
 	public static List<String> OCEIAN_TRADES_TEMP = new LinkedList<String>();
 	public static List<String> OCEIAN_PERKS = new LinkedList<String>();
 	public static int OCEIAN_WORTH;
 	public static int OCEIAN_PROTECTION;
 	public static List<String> SAVANIA_PLAYERS = new LinkedList<String>();
 	public static Location SAVANIA_LOCATION;
 	public static Location SAVANIA_LOCATION_SPAWN;
 	public static Location SAVANIA_LOCATION_DRIFTER;
 	public static List<String> SAVANIA_GENERALS = new LinkedList<String>();
 	public static List<String> SAVANIA_TRADES = new LinkedList<String>();
 	public static List<String> SAVANIA_TRADES_TEMP = new LinkedList<String>();
 	public static List<String> SAVANIA_PERKS = new LinkedList<String>();
 	public static int SAVANIA_WORTH;
 	public static int SAVANIA_PROTECTION;
 	public static Location TRADE_BLACKSMITH;
 	public static Location TRADE_POTIONS;
 	public static Location TRADE_ENCHANTMENTS;
 	public static Location TRADE_RICHPORTAL;
 	public static Location TRADE_MYSTPORTAL;
     
     public static class Node {
         public String name;
         public Location location;
         public Node(String n, Location l) { name = n; location = l; }
     };
     
     public static List<Node> nodes = new LinkedList<Node>();
 
 	public static List<String> UNASSIGNED_PLAYERS = new LinkedList<String>(); 
 	public static Map<String, String> PLAYER_SET_SELECT = new HashMap<String, String>();
 
 	public static Map<String, Integer> TRADE_BLACKSMITH_PLAYER_USES = new HashMap<String, Integer>();
 	public static Map<String, Integer> TRADE_POTIONS_PLAYER_USES = new HashMap<String, Integer>();
 	public static Map<String, Integer> TRADE_ENCHANTMENTS_PLAYER_USES = new HashMap<String, Integer>();
 
 	public static Map<String, Integer> TRADE_BLACKSMITH_CAP_COUNTER = new HashMap<String, Integer>();
 	public static Map<String, Integer> TRADE_POTIONS_CAP_COUNTER = new HashMap<String, Integer>();
 	public static Map<String, Integer> TRADE_ENCHANTMENTS_CAP_COUNTER = new HashMap<String, Integer>();
 	public static Map<String, Integer> TRADE_RICHPORTAL_CAP_COUNTER = new HashMap<String, Integer>();
 	public static Map<String, Integer> TRADE_MYSTPORTAL_CAP_COUNTER = new HashMap<String, Integer>();
 
     public static War war = null;
 
 	public static int TASK_ID_EVENT;
 
 	public static boolean IS_EVENT_RUNNING = false;
 
 	public static CommandExecutor cmd;
 	public static BeyondInfo info;
     public static Random random = new Random();
     
     public enum CityEnum {
         None,
         Contested,
         Abatton,
         Oceian,
         Savania
     };
     
     public static CityEnum GetPlayerCity(String playerName) {
         if (ABATTON_PLAYERS.contains(playerName)) return CityEnum.Abatton;
         if (OCEIAN_PLAYERS.contains(playerName)) return CityEnum.Oceian;
         if (SAVANIA_PLAYERS.contains(playerName)) return CityEnum.Savania;
         return CityEnum.None;
     }
     
     public static boolean PlayerCanUseTrade(String playerName, String trade) {
         CityEnum c = GetPlayerCity(playerName);
         if (c == CityEnum.Abatton && ABATTON_TRADES.contains(trade)) return true;
         if (c == CityEnum.Oceian  && OCEIAN_TRADES.contains(trade))  return true;
         if (c == CityEnum.Savania && SAVANIA_TRADES.contains(trade)) return true;
         return false;
     }
 
 	private final BeyondPluginListener pluginListener = new BeyondPluginListener(this);
 	private final BeyondBlockListener blockListener = new BeyondBlockListener(this);
 	private final BeyondPlayerListener playerListener = new BeyondPlayerListener(this);
 	private final BeyondEntityListener entityListener = new BeyondEntityListener(this);
 	private final BeyondSafeModeListener safeListener = new BeyondSafeModeListener(this);
 
 	public static File infoFile = new File("plugins/Conflict/information.yml");
 
 
 	@Override
 	public void onDisable() {
         saveInfoFile();
 	}
 	@Override
 	public void onEnable() {
 		TRADE_ENCHANTMENTS_CAP_COUNTER.put("Abatton", 0);
 		TRADE_ENCHANTMENTS_CAP_COUNTER.put("Oceian", 0);
 		TRADE_ENCHANTMENTS_CAP_COUNTER.put("Savania", 0);
 		TRADE_POTIONS_CAP_COUNTER.put("Abatton", 0);
 		TRADE_POTIONS_CAP_COUNTER.put("Oceian", 0);
 		TRADE_POTIONS_CAP_COUNTER.put("Savania", 0);
 		TRADE_BLACKSMITH_CAP_COUNTER.put("Abatton", 0);
 		TRADE_BLACKSMITH_CAP_COUNTER.put("Oceian", 0);
 		TRADE_BLACKSMITH_CAP_COUNTER.put("Savania", 0);
 		TRADE_RICHPORTAL_CAP_COUNTER.put("Abatton", 0);
 		TRADE_RICHPORTAL_CAP_COUNTER.put("Oceian", 0);
 		TRADE_RICHPORTAL_CAP_COUNTER.put("Savania", 0);
 		TRADE_MYSTPORTAL_CAP_COUNTER.put("Abatton", 0);
 		TRADE_MYSTPORTAL_CAP_COUNTER.put("Oceian", 0);
 		TRADE_MYSTPORTAL_CAP_COUNTER.put("Savania", 0);
 		//InfoManager
 		information = new YamlConfiguration();
 		info = new BeyondInfo(this);
         loadInfoFile();
 		//TimerManager
 		if(ABATTON_LOCATION == null || OCEIAN_LOCATION == null || SAVANIA_LOCATION == null){
 			severe("Unable to find all Capital Locations.  Booted in SAFE MODE for Timers");
 		}else if(TRADE_BLACKSMITH == null || TRADE_POTIONS == null || TRADE_ENCHANTMENTS == null
 				|| TRADE_RICHPORTAL == null || TRADE_MYSTPORTAL == null){
 			severe("Unable to find all Trade Locations.  Booted in SAFE MODE for Timers");
 		}else if(ABATTON_LOCATION_DRIFTER == null || OCEIAN_LOCATION_DRIFTER == null || SAVANIA_LOCATION_DRIFTER == null){
 			severe("Unable to find all Drifter Locations.  Booted in SAFE MODE for Timers");
 		}else{
 			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 				public void run() {
 					TRADE_BLACKSMITH_PLAYER_USES.clear();
 					TRADE_POTIONS_PLAYER_USES.clear();
 					TRADE_ENCHANTMENTS_PLAYER_USES.clear();
 				}
 			},0L, 432000L);
 			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 				public void run() {
                     // Maintenance timer - runs every 5 mins
                     if(war != null){
                         war.postWarAutoList();
                     }
                    PurgeInactivePlayers();
                     saveInfoFile();
 				}
 			},0L, 5500L);
 			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 				public void run() {
                     // War timer - runs every second
                     if(war == null){
                         if(War.warShouldStart()){
 							ABATTON_TRADES.clear();
 							OCEIAN_TRADES.clear();
 							SAVANIA_TRADES.clear();
 							war = new War();
 							getServer().broadcastMessage(ChatColor.RED+"Mount your Pigs! Strap on your Diamond Armor!  It Has BEGUN!!!");
 						}
 					}
                     else{
                         war.executeWarTick();
 
                         if(war.warShouldEnd()) {
                             war.endWar();  // This should make the peace protesters happy.
                             war = null;
                         }
 					}
 				}
 			},0L, 20L);
 		}
 		//CommandManager
 		cmd = new CommandHandler(this);
 		getCommand("abatton").setExecutor(cmd);
 		getCommand("oceian").setExecutor(cmd);
 		getCommand("savania").setExecutor(cmd);
 		getCommand("point").setExecutor(cmd);
 		getCommand("purchase").setExecutor(cmd);
 		getCommand("spawn").setExecutor(cmd);
 		getCommand("setcityspawn").setExecutor(cmd);
 		getCommand("rarity").setExecutor(cmd);
 		getCommand("post").setExecutor(cmd);
 		getCommand("look").setExecutor(cmd);
 		getCommand("gear").setExecutor(cmd);
 		getCommand("cc").setExecutor(cmd);
 		getCommand("cca").setExecutor(cmd);
 		getCommand("nulls").setExecutor(cmd);
 		getCommand("protectcity").setExecutor(cmd);
 		getCommand("myst").setExecutor(cmd);
 		getCommand("cwho").setExecutor(cmd);
 
 
 		//PermsManager
 		setupPermissions();
 		setupPermissionsEx();
 
 		//PluginManager
 		if(ABATTON_LOCATION == null || OCEIAN_LOCATION == null || SAVANIA_LOCATION == null){
 			severe("Unable to find all Capital Locations.  Booted in SAFE MODE for Listeners");
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(safeListener, this);
 		}else if(TRADE_BLACKSMITH == null || TRADE_POTIONS == null || TRADE_ENCHANTMENTS == null
 				|| TRADE_RICHPORTAL == null || TRADE_MYSTPORTAL == null){
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(safeListener, this);
 			severe("Unable to find all Trade Locations.  Booted in SAFE MODE for Listeners");
 		}else if(ABATTON_LOCATION_DRIFTER == null || OCEIAN_LOCATION_DRIFTER == null || SAVANIA_LOCATION_DRIFTER == null){
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(safeListener, this);
 			severe("Unable to find all Drifter Locations.  Booted in SAFE MODE for Listeners");
 		}else{
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(blockListener, this);
 			pm.registerEvents(pluginListener, this);
 			pm.registerEvents(playerListener, this);
 			pm.registerEvents(entityListener, this);
 			pm.registerEvents(safeListener, this);
 		}
 	}
 	public void setupPermissions() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
 		if (permissionsPlugin != null) {
 			info("Succesfully connected to Permissions!");
 			handler = ((Permissions) permissionsPlugin).getHandler();
 
 		} else {
 			handler = null;
 			warning("Disconnected from Permissions...what could possibly go wrong?");
 		}
 	}
 	public void setupPermissionsEx() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("PermissionsEx");
 
 		if (permissionsPlugin != null) {
 			info("Succesfully connected to PermissionsEx!");
 			ex = PermissionsEx.getPermissionManager();
 
 		} else {
 			ex = null;
 			warning("Disconnected from PermissionsEx...what could possibly go wrong?");
 		}
 	}
 	public void notification(String message){
 		getServer().broadcastMessage(ChatColor.LIGHT_PURPLE.toString() +"[Religion Notification] " + ChatColor.AQUA.toString() + message);
 	}
 	/**
 	 * Logs an informative message to the console, prefaced with this plugin's header
 	 * @param message: String
 	 */
 	public static void info(String message)
 	{ 
 		log.info(header + ChatColor.WHITE + message);
 	}
 
 	/**
 	 * Logs a severe error message to the console, prefaced with this plugin's header
 	 * Used to log severe problems that have prevented normal execution of the plugin
 	 * @param message: String
 	 */
 	public static void severe(String message)
 	{
 		log.severe(header + ChatColor.RED + message);
 	}
 
 	/**
 	 * Logs a warning message to the console, prefaced with this plugin's header
 	 * Used to log problems that could interfere with the plugin's ability to meet admin expectations
 	 * @param message: String
 	 */
 	public static void warning(String message)
 	{
 		log.warning(header + ChatColor.YELLOW + message);
 	}
 
 	/**
 	 * Logs a message to the console, prefaced with this plugin's header
 	 * @param level: Logging level under which to send the message
 	 * @param message: String
 	 */
 	public static void log(java.util.logging.Level level, String message)
 	{
 		log.log(level, header + message);
 	}
 	public static void saveInfoFile() {
 		try {
             BeyondInfo.writer();
             information.save(infoFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
     
     public static void loadInfoFile() {
         try {
             information.load(infoFile);
         } catch (java.io.FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InvalidConfigurationException e) {
             e.printStackTrace();
         }
         BeyondInfo.loader();
     }
 
     public static String theerror = "error not set";
     
     private static boolean purgeHasRunAlready = false;
     static void PurgeInactivePlayers() {
         // Only need to do this once per server restart.
         if (purgeHasRunAlready) return;
 
         try {
             long now = java.lang.System.currentTimeMillis();
         
             for (java.util.Iterator<String> it = ABATTON_PLAYERS.iterator(); it.hasNext();) {
                 long lastseen = 0;
                 lastseen = FirstLastSeen.getLastSeenLong(it.next());
 
                 long days = (now - lastseen) / 1000 / 86400;
                 // Purge anyone who hasn't logged in the last four weeks
                 if (days > 28) {
                     it.remove();
                 }            
             }
             for (java.util.Iterator<String> it = OCEIAN_PLAYERS.iterator(); it.hasNext();) {
                 long lastseen = 0;
                 lastseen = FirstLastSeen.getLastSeenLong(it.next());
 
                 long days = (now - lastseen) / 1000 / 86400;
                 // Purge anyone who hasn't logged in the last four weeks
                 if (days > 28) {
                     it.remove();
                 }            
             }
             for (java.util.Iterator<String> it = SAVANIA_PLAYERS.iterator(); it.hasNext();) {
                 long lastseen = 0;
                 lastseen = FirstLastSeen.getLastSeenLong(it.next());
 
                 long days = (now - lastseen) / 1000 / 86400;
                 // Purge anyone who hasn't logged in the last four weeks
                 if (days > 28) {
                     it.remove();
                 }            
             }
         }
         catch (java.lang.NoClassDefFoundError e) {
             severe("Cannot find FirstLastSeen!  Automatic town member purge is disabled.");
         }
         purgeHasRunAlready = true;
     }
 }
