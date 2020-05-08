 package com.webkonsept.bukkit.memorywarning;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 public class MemoryWarning extends JavaPlugin {
 	public static Logger log = Logger.getLogger("Minecraft");
 	public MemPoller poller;
	PluginDescriptionFile pdfFile = this.getDescription();
 	MemoryWarningPlayerListener playerListener = new MemoryWarningPlayerListener(this);
 	
 	// settings
 	boolean verbose = false;
 	Double warnPercentage = 95.0;
 	Double panicPercentage = 97.0;
 	Integer minutesBetweenPolls = 5;
 	Boolean pollingMessage = false;
 	List<String> killPlugins = new ArrayList<String>();
 	
 
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		poller = null;
 		out("Disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		out("Coming on line...");
 		
 		loadConfig();
 		
 		poller = new MemPoller(this);
 		poller.setServer(getServer());
 		poller.setWarnPercentage(warnPercentage);
 		poller.setPanicPercentage(panicPercentage);
 		poller.setPollingMessage(pollingMessage);
 		poller.setSlayPlugins(killPlugins);
 		
 		Integer interval = minutesBetweenPolls * 1200; // On a good day it's 20 ticks per second, 1200 ticks per minute.
 		
 		double memMax = Runtime.getRuntime().maxMemory() / 1048576;
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this,poller,200,interval);
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN,playerListener,Priority.Normal,this);
 
 		if (pollingMessage){
 			out("I ill poll every "+interval+" server ticks, or rougly "+minutesBetweenPolls+" minutes apart, depending on load.");
 			out("NOTE:  I'm going to spam your console every time I poll memory usage!");
 		}
 		babble("You've given Java a memory allowance of "+memMax+"MB I'll warn if the server uses more than "+warnPercentage+"%");
 		babble("If memory usage goes over "+panicPercentage+"%, I'll panic and do irrational stuff, like kicking players and disabling plugins.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (!isEnabled()) return false;
 		Player player = null;
 		if (sender instanceof Player){
 			player = (Player) sender;
 			if (! player.isOp()){
 				player.sendMessage(ChatColor.RED+"Ops only, sorry!");
 				return true;
 			}
 		}
 		if (args.length == 0){
 			String message = poller.getSummary();
 			sender.sendMessage(ChatColor.LIGHT_PURPLE+"[MemoryWarning] "+message);
 			if (player != null){
 				out( player.getName()+" got summary: "+message );
 			}
 			return true;
 		}
 		else if (args[0].equalsIgnoreCase("setlevel")){
 			if (args.length != 3){
 				return commandUsage(sender,"Wrong number of arguments!");
 			}
 			Double value = 0D;
 			try {
 				value = Double.parseDouble(args[2]);
 			}
 			catch (NumberFormatException e){
 				return commandUsage(sender,"Sorry, but '"+args[2]+"' is not a value I can work with.");
 			}
 			if (value > 99.9 || value < 60){
 				return commandUsage(sender,"If you want to be silly, please be silly in the config file.  Value out of range: "+args[2]);
 			}
 			
 			if (args[1].equalsIgnoreCase("warn")){
 				poller.setWarnPercentage(value);
 				sender.sendMessage(ChatColor.LIGHT_PURPLE+"Warn percentage set to "+value+"%");
 				return true;
 			}
 			else if (args[1].equalsIgnoreCase("panic")){
 				poller.setPanicPercentage(value);
 				sender.sendMessage(ChatColor.LIGHT_PURPLE+"Panic percentage set to "+value+"%");
 				return true;
 			}
 			else {
 				return commandUsage(sender,"Can't set level for"+args[1]);
 			}
 		}
 		else if (args[0].equalsIgnoreCase("poll")){
 			if (args.length != 1){
 				return commandUsage(sender,"Poll "+args[1]+"?  What?");
 			}
 			sender.sendMessage(ChatColor.LIGHT_PURPLE+"Forcing MemoryWarning polling and associated actions...");
 			poller.run();
 			sender.sendMessage(ChatColor.LIGHT_PURPLE+"Forced polling complete");
 		}
 		else {
 			return commandUsage(sender,"Syntax does not compute.");
 		}
 		
 		return true; // Silly Java... The code can never get here >.<
 	}
 	
 	private boolean commandUsage(CommandSender sender,String problem){
 		String[] lines = {
 			"MemoryWarning command usage:",
 			"/memory [poll|setlevel level value]",
 			"With no argument, current status is returned.",
 			"setlevel knows the levels 'warning' and 'panic', ranges 60 to 99.9, decimals accepted.",
 			"poll will force a polling right now, with the relevant actions involved."
 		};
 		for (String line : lines){
 			sender.sendMessage(ChatColor.DARK_PURPLE+line);
 		}
 		sender.sendMessage(ChatColor.LIGHT_PURPLE+"Problem: "+problem);
 		return true;
 	}
 	public void loadConfig() {
 		
 		File configFile = new File(getDataFolder().toString()+"/settings.yml");
 		Configuration config = new Configuration(configFile);
 		
 		config.load();
 		verbose = config.getBoolean("log.verbose", verbose);
 		warnPercentage = config.getDouble("level.warn", warnPercentage);
 		panicPercentage = config.getDouble("level.panic", panicPercentage);
 		minutesBetweenPolls = config.getInt("minutesBetweenPolls",minutesBetweenPolls);
 		pollingMessage = config.getBoolean("log.polling", pollingMessage);
 		killPlugins = config.getStringList("killPlugins",killPlugins);
 		
 		if (!configFile.exists()){
 			this.getDataFolder().mkdirs();
 			try {
 				configFile.createNewFile();
 				killPlugins.add("Please edit your");
 				killPlugins.add("settings file");
 				killPlugins.add("for MemoryWarning");
 				config.setProperty("killPlugins",killPlugins);
 				out("There was no config file, so I made one in "+getDataFolder().toString()+"/"+configFile.getName());
 			} catch (IOException e) {
 				e.printStackTrace();
 				this.crap("IOError while creating config file: "+e.getMessage());
 			}
 		}
 		config.save();
 	}
 	public void out(String message) {
 		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
 	}
 	public void crap(String message){
 		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
 	}
 	public void babble(String message){
 		if (!this.verbose){ return; }
 		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " VERBOSE] " + message);
 	}
 }
