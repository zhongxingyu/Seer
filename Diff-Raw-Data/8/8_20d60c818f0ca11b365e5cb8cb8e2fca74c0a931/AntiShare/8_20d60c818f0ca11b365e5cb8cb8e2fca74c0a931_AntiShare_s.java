 package com.turt2live.antishare;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.convert.Convert;
 import com.turt2live.antishare.inventory.InventoryManager;
 import com.turt2live.antishare.metrics.Metrics;
 import com.turt2live.antishare.metrics.TrackerList;
 import com.turt2live.antishare.money.MoneyManager;
 import com.turt2live.antishare.notification.Alert;
 import com.turt2live.antishare.notification.Messages;
 import com.turt2live.antishare.notification.Messenger;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.permissions.Permissions;
 import com.turt2live.antishare.regions.ASRegion;
 import com.turt2live.antishare.regions.RegionFactory;
 import com.turt2live.antishare.regions.RegionManager;
 import com.turt2live.antishare.signs.SignManager;
 import com.turt2live.antishare.storage.BlockManager;
 import com.turt2live.antishare.storage.ItemMap;
 import com.turt2live.antishare.storage.PerWorldConfig;
 import com.turt2live.antishare.storage.SQL;
 
 /**
  * AntiShare
  * 
  * @author turt2live
  */
 public class AntiShare extends PluginWrapper {
 
 	/* Changes since 3.2.2
 	 * 
 	 * - Added negation to block lists
 	 * - Added options to drop blocks when a player breaks a block of the opposite Game Mode
 	 * - Bug Fix: Actually remove blocks when broken from the registry
 	 * - Fixed "X is not Y (found Z)" messages spamming (toggle option)
 	 * - Auto-Update blocks in the registry
 	 * - Cleanup registry blocks by removing AIR
 	 * - Fixed concurrent exceptions
 	 * - Added Metrics
 	 * - Added World Split warnings
 	 * - Fixed world splits and regions not getting along
 	 * - Fixed /gm working in world splits
 	 * - Added per-region configurations
 	 * - Added a way to block/allow specific signs
 	 * - Removed /gm command
 	 * - Added block ID support (eg: wool:1) to block and track lists
 	 * - Fixed logging in as a different Game Mode from what you logged off as with the no swap permission giving you the wrong inventory
 	 * - Fixed logging off before a server restarts/AS reloaded giving you a 'null' or no inventory
 	 */
 
 	/**
 	 * Represents a Log Entry Type
 	 * 
 	 * @author turt2live
 	 */
 	public static enum LogType{
 		/**
 		 * Startup Message
 		 */
 		STARTUP,
 		/**
 		 * Shutdown Message
 		 */
 		SHUTDOWN,
 		/**
 		 * General/Info message
 		 */
 		INFO,
 		/**
 		 * Invalid block message
 		 */
 		BLOCK,
 		/**
 		 * Bypasses all "silent" checks
 		 */
 		BYPASS;
 	}
 
 	/**
 	 * AntiShare tool material
 	 */
 	public static final Material ANTISHARE_TOOL = Material.BLAZE_ROD;
 	private static AntiShare instance;
 	private boolean useSQL = false;
 	private boolean sqlRetry = false;
 	private Messenger messenger;
 	private Permissions permissions;
 	private ItemMap itemMap;
 	private ASListener listener;
 	private Alert alerts;
 	private Messages messages;
 	private RegionManager regions;
 	private RegionFactory factory;
 	private BlockManager blocks;
 	private InventoryManager inventories;
 	private SQL sql;
 	private Metrics metrics;
 	private TrackerList trackers;
 	private SignManager signs;
 	private MoneyManager tender;
 
 	/**
 	 * Gets the active AntiShare instance
 	 * 
 	 * @return the instance
 	 */
 	public static AntiShare getInstance(){
 		return instance;
 	}
 
 	@Override
 	public void onEnable(){
 		instance = this;
 
 		// Convert
 		Convert.start();
 		Convert.convertConfig313to320b(); // Fixes configuration file
 
 		// We need to initiate an SQL connection now
 		sql = new SQL();
 		if(getConfig().getBoolean("enabled-features.sql")){
 			// Setup properties
 			String hostname = getConfig().getString("settings.sql.host");
 			String username = getConfig().getString("settings.sql.username");
 			String password = getConfig().getString("settings.sql.password");
 			String database = getConfig().getString("settings.sql.database");
 
 			// Try connection
 			boolean connected = sql.connect(hostname, username, password, database);
 			if(connected){
 				sql.setup();
 				useSQL = true;
 			}
 		}
 
 		// Continue conversion
 		Convert.convert313to320b(); // Changes data (inventories and regions)
 		Convert.end();
 
 		// Check configuration
 		getConfig().loadDefaults(getResource("resources/config.yml"));
 		if(!getConfig().fileExists() || !getConfig().checkDefaults()){
 			getConfig().saveDefaults();
 		}
 		getConfig().load();
 
 		// Migrate world configurations
 		PerWorldConfig.migrate();
 
 		// Setup (order is important!)
 		try{
 			metrics = new Metrics(this);
 		}catch(IOException e1){
 			e1.printStackTrace();
 		}
 		messenger = new Messenger();
 		signs = new SignManager();
 		trackers = new TrackerList();
 		tender = new MoneyManager();
 		permissions = new Permissions();
 		itemMap = new ItemMap();
 		listener = new ASListener();
 		alerts = new Alert();
 		messages = new Messages();
 		regions = new RegionManager();
 		factory = new RegionFactory();
 		blocks = new BlockManager();
 		inventories = new InventoryManager();
 
 		// Statistics
 		UpdateChecker.start();
 		UsageStatistics.send(); // Handles config internally, also handles Metrics
 
 		// Start listeners
 		getServer().getPluginManager().registerEvents(permissions, this);
 		getServer().getPluginManager().registerEvents(listener, this);
 
 		// Command handlers
 		getCommand("antishare").setExecutor(new CommandHandler());
 
 		// Check players
 		for(Player player : Bukkit.getOnlinePlayers()){
 			ASRegion playerRegion = regions.getRegion(player.getLocation());
 			if(playerRegion != null){
 				playerRegion.alertSilentEntry(player);
 			}
 		}
 
 		// Enabled
 		log("Enabled!", Level.INFO, LogType.STARTUP);
 
 		// Start periodic save
 		Integer saveInterval = 0;
 		String strSaveInterval = getConfig().getString("settings.auto-save-interval");
 		try{
 			saveInterval = Integer.parseInt(strSaveInterval);
 		}catch(Exception e){
 			saveInterval = 30;
 			messenger.log("Save interval " + strSaveInterval + " is not an integer. Defaulting to " + saveInterval, Level.WARNING, LogType.BYPASS);
 		}
 		int interval = 20 * 60 * saveInterval;
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 			@Override
 			public void run(){
 				messenger.log("Reloading AntiShare...", Level.INFO, LogType.BYPASS);
 				reload();
 				messenger.log("Reloaded", Level.INFO, LogType.BYPASS);
 			}
 		}, interval, interval);
 
 	}
 
 	@Override
 	public void onDisable(){
 		// Save
 		regions.save();
 		blocks.save();
 		inventories.save();
 		tender.save();
 		metrics.flush();
 		sql.disconnect();
 
 		// Disable
 		getServer().getScheduler().cancelTasks(this);
 		log("Disabled!", Level.INFO, LogType.SHUTDOWN);
 
 		// Prepare as though it's a reload
 		permissions = null;
 		messenger = null;
 		itemMap = null;
 		listener = null;
 		alerts = null;
 		messages = null;
 		instance = null;
 		regions = null;
 		factory = null;
 		blocks = null;
 		inventories = null;
 		sql = null;
 		metrics = null;
 		trackers = null;
 		signs = null;
 		tender = null;
 	}
 
 	/**
 	 * Reloads AntiShare
 	 */
 	public void reload(){
 		reloadConfig();
 		// Permissions has no reload
 		messenger.reload();
 		itemMap.reload();
 		signs.reload();
 		listener.reload();
 		alerts.reload();
 		messages.reload();
 		regions.reload();
 		// Region Factory has no reload
 		blocks.reload();
 		inventories.reload();
 		// SQL has no reload
 		// Metrics has no reload
 		// Tracker List has no reload
 		tender.reload();
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param world the world
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, World world){
 		if(permissions.has(player, allowPermission, world)){
 			return false;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_CREATIVE) && player.getGameMode() == GameMode.CREATIVE){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_SURVIVAL) && player.getGameMode() == GameMode.SURVIVAL){
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Gets a message
 	 * 
 	 * @param path the path to the message
 	 * @return the message
 	 */
 	public String getMessage(String path){
 		return messages.getMessage(path);
 	}
 
 	/**
 	 * Gets the messages handler in AntiShare
 	 * 
 	 * @return the messages handler
 	 */
 	public Messages getMessages(){
 		return messages;
 	}
 
 	/**
 	 * Gets the permissions handler for AntiShare
 	 * 
 	 * @return the permissions
 	 */
 	public Permissions getPermissions(){
 		return permissions;
 	}
 
 	/**
 	 * Gets the messenger for AntiShare
 	 * 
 	 * @return the messenger
 	 */
 	public Messenger getMessenger(){
 		return messenger;
 	}
 
 	/**
 	 * Gets the Item Map for AntiShare
 	 * 
 	 * @return the item map
 	 */
 	public ItemMap getItemMap(){
 		return itemMap;
 	}
 
 	/**
 	 * Gets the Alert instance for AntiShare
 	 * 
 	 * @return the alerts system
 	 */
 	public Alert getAlerts(){
 		return alerts;
 	}
 
 	/**
 	 * Gets the listener being used by AntiShare
 	 * 
 	 * @return the listener
 	 */
 	public ASListener getListener(){
 		return listener;
 	}
 
 	/**
 	 * Gets the region manager being used by AntiShare
 	 * 
 	 * @return the region manager
 	 */
 	public RegionManager getRegionManager(){
 		return regions;
 	}
 
 	/**
 	 * Gets the region factory being used by AntiShare
 	 * 
 	 * @return the region factory
 	 */
 	public RegionFactory getRegionFactory(){
 		return factory;
 	}
 
 	/**
 	 * Gets the block manager being used by AntiShare
 	 * 
 	 * @return the block manager
 	 */
 	public BlockManager getBlockManager(){
 		return blocks;
 	}
 
 	/**
 	 * Gets the inventory manager being used by AntiShare
 	 * 
 	 * @return the inventory manager
 	 */
 	public InventoryManager getInventoryManager(){
 		return inventories;
 	}
 
 	/**
 	 * Gets the metrics being used by AntiShare
 	 * 
 	 * @return the metrics
 	 */
 	public Metrics getMetrics(){
 		return metrics;
 	}
 
 	/**
 	 * Gets the tracker list being used by AntiShare
 	 * 
 	 * @return the trackers
 	 */
 	public TrackerList getTrackers(){
 		return trackers;
 	}
 
 	/**
 	 * Gets the sign manager being used by AntiShare
 	 * 
 	 * @return the sign manager
 	 */
 	public SignManager getSignManager(){
 		return signs;
 	}
 
 	/**
 	 * Gets the money manager (rewards/fines) being used by AntiShare
 	 * 
 	 * @return the money manager
 	 */
 	public MoneyManager getMoneyManager(){
 		return tender;
 	}
 
 	/**
 	 * Gets the SQL manager for AntiShare
 	 * 
 	 * @return the SQL manager
 	 */
 	public SQL getSQL(){
 		return sql;
 	}
 
 	/**
 	 * Determines if AntiShare should use SQL or not
 	 * 
 	 * @return true if SQL should be used
 	 */
 	public boolean useSQL(){
 		if(getConfig().getBoolean("enabled-features.sql") && !useSQL && !sqlRetry){
 			startSQL();
 			sqlRetry = true;
 		}
 		return useSQL && getConfig().getBoolean("enabled-features.sql") && sql.isConnected();
 	}
 
 	/**
 	 * Force starts the SQL connection
 	 * 
 	 * @return true if connected
 	 */
 	public boolean startSQL(){
 		sql = new SQL();
 
 		// Setup properties
 		String hostname = getConfig().getString("settings.sql.host");
 		String username = getConfig().getString("settings.sql.username");
 		String password = getConfig().getString("settings.sql.password");
 		String database = getConfig().getString("settings.sql.database");
 
 		// Try connection
 		boolean connected = sql.connect(hostname, username, password, database);
 		if(connected){
 			sql.setup();
 			useSQL = true;
 			return true;
 		}
 
 		// Failed connection
 		return false;
 	}
 
 	/**
 	 * Determines if AntiShare is outdated
 	 * 
 	 * @return true if outdated
 	 */
 	public static boolean isOutdated(){
 		double current = Double.valueOf(getVersion().split("-")[0].replaceFirst("\\.", ""));
 		;
 		double release = Double.valueOf(getNewVersion());
 		return release > current;
 	}
 
 	/**
 	 * Gets the active version of AntiShare
 	 * 
 	 * @return the active version of AntiShare
 	 */
 	public static String getVersion(){
 		return instance.getDescription().getVersion();
 	}
 
 	/**
 	 * Gets the public release version of AntiShare
 	 * 
 	 * @return the public release version of AntiShare
 	 */
 	// Borrowed from Vault, thanks Sleaker!
 	// https://github.com/MilkBowl/Vault/blob/master/src/net/milkbowl/vault/Vault.java#L520
 	public static String getNewVersion(){
 		String pluginUrlString = "http://dev.bukkit.org/server-mods/antishare/files.rss";
 		try{
 			URL url = new URL(pluginUrlString);
 			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
 			doc.getDocumentElement().normalize();
 			NodeList nodes = doc.getElementsByTagName("item");
 			Node firstNode = nodes.item(0);
 			if(firstNode.getNodeType() == 1){
 				Element firstElement = (Element) firstNode;
 				NodeList firstElementTagName = firstElement.getElementsByTagName("title");
 				Element firstNameElement = (Element) firstElementTagName.item(0);
 				NodeList firstNodes = firstNameElement.getChildNodes();
 				return firstNodes.item(0).getNodeValue().replace("v", "").replaceFirst("\\.", "").trim();
 			}
 		}catch(Exception localException){} // Do not handle
 		return getVersion();
 	}
 
 	/**
 	 * Logs a message
 	 * 
 	 * @param message the message
 	 * @param level the log level
 	 * @param type the log entry type
 	 */
 	public static void log(String message, Level level, LogType type){
 		instance.messenger.log(message, level, type);
 	}
 }
