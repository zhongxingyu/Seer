 package com.MoofIT.Minecraft.DeathWish;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class DeathWish extends JavaPlugin {
 	private final DeathWishEntityListener entityListener = new DeathWishEntityListener(this);
 
 	public static Logger log;
 	public PluginManager pm;
 	private FileConfiguration config;
 
 	//Config defaults
 	public boolean alwaysBroadcast = true;
 	public List<String> quietWorlds;
 	public List<String> broadcastWorlds;
 	public String dateFormat = "MM/dd/yyyy";
 	public String timeFormat = "hh:mm a";
 	public boolean versionCheck = true;
 
 	//Logging
 	public boolean printToConsole = true;
 	public boolean logToFile = true;
 	public boolean dailyFile = true;
 	public String logFile = "deathlog.csv";
 
 	//Messages
 	public HashMap<String, List<String>> messages = new HashMap<String, List<String>>();
 
 	//Config versioning
 	private int configVer = 0;
 	private final int configCurrent = 1;
 
 	public void onEnable() {
 		log = Logger.getLogger("Minecraft");
 		pm = getServer().getPluginManager();
 
 		loadConfig();
 		if (versionCheck) versionCheck();
 
 		pm.registerEvents(entityListener, this);
 
 		log.info("DeathWish " + getDescription().getVersion() + " loaded.");
 	}
 
 	public void onDisable() {
 		log.info("[DeathWish] Shutting down.");
 		pm = null;
 	}
 
 	private void loadConfig() {
 		this.reloadConfig();
 		config = this.getConfig();
 
 		configVer = config.getInt("configVer", configVer);
 		if (configVer == 0) {
 			saveDefaultConfig();
 			log.info("[DeathWish] Configuration error or no config file found. Copying default config file from JAR.");
 		}
 		else if (configVer < configCurrent) {
 			log.warning("[DeathWish] Your config file is out of date! Delete your config and reload to see the new options. Proceeding using set options from config file and defaults for new options..." );
 		}
 
 		alwaysBroadcast = config.getBoolean("Core.alwaysBroadcast", alwaysBroadcast);
 		try {
 			quietWorlds = config.getStringList("Core.quietWorlds");
 		} catch (NullPointerException e) {
 			log.warning("[DeathWish] Configuration failure while loading quietWorlds. Using defaults.");
 		}
 		try {
 			broadcastWorlds = config.getStringList("Core.broadcastWorlds");
 		} catch (NullPointerException e) {
 			log.warning("[DeathWish] Configuration failure while loading broadcastWorlds. Using defaults.");
 		}
 		dateFormat = config.getString("Core.dateFormat", dateFormat);
 		timeFormat = config.getString("Core.timeFormat", timeFormat);
 		versionCheck = config.getBoolean("Core.versionCheck", versionCheck);
 
 		printToConsole = config.getBoolean("Logging.printToConsole", printToConsole);
 		logToFile = config.getBoolean("Logging.logToFile", logToFile);
 		dailyFile = config.getBoolean("Logging.dailyFile", dailyFile);
 		logFile = config.getString("Logging.logFile", logFile);
 
 		try {
 			Set<String> typeSet = config.getConfigurationSection("Messages").getKeys(false);
 			for (String type : typeSet) {
 				Set<String> subSet = config.getConfigurationSection("Messages." + type).getKeys(false);
 				for (String key : subSet) {
 					messages.put(key, config.getStringList("Messages." + type + "." + key));
 				}
 			}
 		} catch (NullPointerException e) {
 			log.warning("[DeathWish] Configuration failure while loading deathMessages. Using defaults.");
 		} catch (Exception e) {
 			log.severe("[DeathWish] Configuration failure while loading deathMessages. Shutting down.");
 			pm.disablePlugin(this);
 		}
 	}
 
 	public void versionCheck() {
 		String thisVersion = getDescription().getVersion();
 		URL url = null;
 		try {
 			url = new URL("http://www.moofit.com/minecraft/DeathWish.ver?v=" + thisVersion);
 			BufferedReader in = null;
 			in = new BufferedReader(new InputStreamReader(url.openStream()));
 			String newVersion = "";
 			String line;
 			while ((line = in.readLine()) != null) {
 				newVersion += line;
 			}
 			in.close();
 			if (!newVersion.equals(thisVersion)) {
 				log.warning("[DeathWish] DeathWish is out of date! This version: " + thisVersion + "; latest version: " + newVersion + ".");
 			}
 			else {
 				log.info("[DeathWish] DeathWish is up to date at version " + thisVersion + ".");
 			}
 		}
 		catch (MalformedURLException ex) {
 			log.warning("[DeathWish] Error accessing update URL.");
 		}
 		catch (IOException ex) {
 			log.warning("[DeathWish] Error checking for update.");
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (!(sender instanceof Player)) return false;
 		Player p = (Player)sender;
 		String cmd = command.getName();
 		if (cmd.equalsIgnoreCase("dwreload")) {
 			if (args.length < 1) return false;
 			if (p.hasPermission("deathwish.reload") && args[0].equalsIgnoreCase("reload")) {
 				loadConfig();
 				p.sendMessage(ChatColor.GRAY + "[DeathWish] " + ChatColor.WHITE + "Configuration reloaded from file.");
 			}
 		}
 		return false;
 	}
 }
