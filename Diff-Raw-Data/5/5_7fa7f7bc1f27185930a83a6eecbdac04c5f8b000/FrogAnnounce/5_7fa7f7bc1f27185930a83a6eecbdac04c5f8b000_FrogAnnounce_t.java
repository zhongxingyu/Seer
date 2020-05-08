 package me.thelunarfrog.FrogAnnounce;
 
 import java.io.*;
 import java.util.*;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 @SuppressWarnings("deprecation")
 public class FrogAnnounce extends JavaPlugin
 {
 	private static PluginDescriptionFile pdfFile;
 	
 	private static final String DIR = "plugins" + File.separator + "FrogAnnounce" + File.separator;
 	private static final String CONFIG_FILE = "Configuration.yml";
 	private static Configuration Settings = new Configuration(new File(DIR + CONFIG_FILE));
 	
 	private static String Tag;
 	private static int Interval, taskId = -1, counter = 0;
 	private static boolean isScheduling = false, isRandom, permissionsEnabled = false, permission, toGroups;
 	private static List<String> strings, Groups;
 	public static PermissionHandler Permissions;
 	
 	public static FrogAnnounce plugin;
 	
 	public void createNewConfiguration(){
 		Interval = Settings.getInt("Settings.Interval", 5);
 		isRandom = Settings.getBoolean("Settings.Random", false);
 		permission = Settings.getBoolean("Settings.Permission", true);
 		strings = Settings.getStringList("Announcer.Strings", new ArrayList<String>());
 		Tag = colorize(Settings.getString("Announcer.Tag", "&GOLD;[FrogAnnounce]"));
 		toGroups = Settings.getBoolean("Announcer.ToGroups", false);
 		Groups = Settings.getStringList("Announcer.Groups", new ArrayList<String>());
 		Settings.save();
 	}
 	
     @Override
 	public void onEnable()
     {
     	pdfFile = this.getDescription();
     	File fDir = new File(DIR);
 		if (!fDir.exists())
 			fDir.mkdir();
 		try{
 			File fAuths = new File(DIR + CONFIG_FILE);
 			if (!fAuths.exists()){
 				fAuths.createNewFile();
 				createNewConfiguration();
 			}
 		}catch (Exception e){
 			System.out.println(e.getMessage());
 		}
     	load();
     	if(permission)
     		enablePermissions();
     	else
     		System.out.println("[FrogAnnounce] No Permissions system was detected!");
     	System.out.println("[FrogAnnounce] Settings loaded " + strings.size() + " announcements!");
     	isScheduling = scheduleOn(null);
    	System.out.println("[FrogAnnounce] Version " + pdfFile.getVersion() + " by TheLunarFrog has been enabled!" );
 	}
     @Override
 	public void onDisable()
     {
     	scheduleOff(true, null);
    	System.out.println("[FrogAnnounce] Version " + pdfFile.getVersion() + " by TheLunarFrog has been disabled!");
     }
     private void enablePermissions() {
     	Plugin p = getServer().getPluginManager().getPlugin("Permissions");
     	if(p != null) {
     		if(!p.isEnabled())
     			getServer().getPluginManager().enablePlugin(p);
     		Permissions = ((Permissions)p).getHandler();
     		permissionsEnabled = true;
     		System.out.println("[FrogAnnounce] Permissions support was detected!");
     	} else
     		System.out.println("[FrogAnnounce] Permissions system couldn't be found! Defaulting to OP/Non-OP system.");
     }
     private boolean permission(Player player, String line, Boolean op){
     	if(permissionsEnabled) {
     		return Permissions.has(player, line);
     	} else {
     		return op;
     	}
     }
     private void OptOut(boolean OptOut, Player player){
     	if(OptOut = true)
     	{
 			FrogAnnounce.plugin.turnOff(player);
 			plugin.turnOff(player);
 			plugin.OptOut(true, player);
 			debugees.put(player, false);
 			debugees.put(player, Boolean.valueOf(false));
 			getServer().getScheduler().cancelTasks((FrogAnnounce) player);
     	}
     }
     private void turnOff(Player player) {
 		getServer().getScheduler().cancelTasks((FrogAnnounce) player);
 	}
 	private void scheduleOff(boolean Disabling, Player player){
     	if(isScheduling){
     		getServer().getScheduler().cancelTask(taskId);
     		if(player != null) player.sendMessage(ChatColor.RED+"Announcements disabled!");
     		System.out.println("[FrogAnnounce] Announcements disabled!");
 	    	isScheduling = false;
     	}else{
     		if(!Disabling)
     			if(player != null) player.sendMessage(ChatColor.DARK_RED+"No annuoncements running!");
     			System.out.println("[FrogAnnounce] No announcements running!" );
     	}
     }
     private boolean scheduleOn(Player player){
     	if(!isScheduling){
 	    	if(strings.size() > 0){
 	    		taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new printAnnounce(), Interval * 1200, Interval * 1200);
 		    	if(taskId == -1){
 		    		if(player != null) player.sendMessage(ChatColor.DARK_RED+"Announcing failed!");
 		    		System.out.println("[FrogAnnounce] Announcing failed!" );
 		    		return false;
 		    	}else{
 		    		counter = 0;
 		    		if(player != null) player.sendMessage(ChatColor.GREEN+"Scheduled to announce every "+ Interval +" minute(s)!");
 		    		System.out.println("[FrogAnnounce] Scheduled to announce every "+ Interval +" minute(s)!" );
 		    		return true;
 		    	}
 	    	}else{
 	    		if(player != null) player.sendMessage(ChatColor.DARK_RED+"Announcing failed! There are no announcements!");
 	    		System.out.println("[FrogAnnounce] Announcing failed! There are no announcements!" );
 	    		return false;
 	    	}
     	}
 	else{
     		if(player != null) player.sendMessage(ChatColor.DARK_RED+"Announcer is already running.");
     		System.out.println("[FrogAnnounce] Announcer is already running." );
     		return true;
     	}
     }
     private void scheduleRestart(Player player){
     	if(isScheduling){
     		scheduleOff(false, null);
     		load();
     		player.sendMessage(ChatColor.DARK_GREEN + "FrogAnnounce has been successfully reloaded!");
     		player.sendMessage(ChatColor.DARK_GREEN+"Settings loaded "+strings.size()+" announcements!");
     		isScheduling = scheduleOn(player);
     	}else{
     		player.sendMessage(ChatColor.DARK_RED+"No announcements running!");
     	}
     }
     private void setInterval(String[] args, Player player){
     	if(args.length == 2) {
     		try{
 				int interval = Integer.parseInt(args[1], 10);
 				Settings.setProperty("Settings.Interval", interval);
 				Settings.save();
 				player.sendMessage(ChatColor.DARK_GREEN+"Interval changed successfully to "+args[1]+" minutes.");
 				if(isScheduling) player.sendMessage(ChatColor.GOLD+"Restart the schedule to apply changes.");
 			}catch(NumberFormatException err){
 				player.sendMessage(ChatColor.DARK_RED+"Error! Usage: /fa interval 5");
 			}
     	}else{
     		player.sendMessage(ChatColor.DARK_RED+"Error! Usage: /fa interval 5");
     	}
     }
     private void setRandom(String[] args, Player player){
     	if(args.length == 2) {
     		if(args[1].equals("on")){
     			Settings.setProperty("Settings.Random", true);
     			Settings.save();
     			player.sendMessage(ChatColor.DARK_GREEN+"Changed to shuffle-order mode.");
     			if(isScheduling) player.sendMessage(ChatColor.GOLD+"Restart the schedule to apply changes.");
     		}else if(args[1].equals("off")){
     			Settings.setProperty("Settings.Random", false);
     			Settings.save();
     			player.sendMessage(ChatColor.DARK_GREEN+"Changed to consecutive cylcing mode.");
     			if(isScheduling) player.sendMessage(ChatColor.AQUA+"Restart the schedule to apply changes.");
     		}else{
     			player.sendMessage(ChatColor.DARK_RED+"Error! Usage: /fa random off");
     		}
     	}else{
     		player.sendMessage(ChatColor.DARK_RED+"Error! Usage: /fa random off");
     	}
     }
     @Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
     	@SuppressWarnings("unused")
 		String commandName = cmd.getName();
     		Player player = (Player)sender;
     		if(commandLabel.equalsIgnoreCase("fa") || commandLabel.equalsIgnoreCase("frogannounce"))
     		{
     			if(permission(player, "frogannounce.admin", player.isOp()) || permission(player, "frogannounce.*", player.isOp())) {
 	    			try {
 	    				if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase(""))
 	    					auctionHelp(player);
 	    				if(args[0].equalsIgnoreCase("off"))
 	    					scheduleOff(false, player);
 	    				else if(args[0].equalsIgnoreCase("on"))
 	    					isScheduling = scheduleOn(player);
 	    				else if(args[0].equalsIgnoreCase("interval") || args[0].equalsIgnoreCase("int"))
 	    					setInterval(args, player);
 	    				else if(args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("rand"))
 	    					setRandom(args, player);
 	    				else if(args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reload")){
 	    					scheduleRestart(player);
 	    					reloadConfig();
 	    				}
 	    				return true;
 	    			}
 	    			catch(ArrayIndexOutOfBoundsException somTingWong) {
 	    				return false;
 	    			}
     			}
     			if(permission(player, "frogannounce", player.isOp()) || permission(player, "frogannounce.admin", player.isOp())){
     				if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))
     					auctionHelp(player);
     				}
     			if(permission(player, "frogannounce.optout", player.isOp()) || permission(player, "frogannounce.admin", player.isOp())){
     					if(args[1].equalsIgnoreCase("optout") || args[1].equalsIgnoreCase("oo")){
     						OptOut(isEnabled(), player);
     			}
     			}
     			else {
     				player.sendMessage(ChatColor.RED + "You do not have the permission level required to use this command!");
     				return true;
     	    	}
     		}
         	if(permission(player, "frogannounce.ignore", player.isOp()) || permission(player, "frogannounce.admin", player.isOp()))
     		{
         		FrogAnnounce.plugin.turnOff(player);
     			FrogAnnounce.plugin.OptOut(true, player);
     			plugin.turnOff(player);
     			plugin.OptOut(true, player);
     			debugees.put(player, false);
     			debugees.put(player, Boolean.valueOf(false));
     		}
     	return false;
     }
     public void auctionHelp(Player player) {
     	String or = ChatColor.WHITE + "|";
     	String auctionStatusColor = ChatColor.DARK_GREEN.toString();
     	String helpMainColor = ChatColor.GOLD.toString();
     	String helpCommandColor = ChatColor.AQUA.toString();
     	String helpObligatoryColor = ChatColor.DARK_RED.toString();
         player.sendMessage(helpMainColor + " * " + auctionStatusColor + "Help for FrogAnnounce" + helpMainColor + " * ");
         player.sendMessage(helpCommandColor + "/fa <help" + or + helpCommandColor + "?>" + helpMainColor + " - Show this message.");
         player.sendMessage(helpCommandColor + "/fa <on" + or + helpCommandColor + "off>" + helpMainColor + " - Start or stop FrogAnnounce.");
         player.sendMessage(helpCommandColor + "/fa <restart" + or + helpCommandColor + "reload>" + helpMainColor + " - Restart FrogAnnounce.");
         player.sendMessage(helpCommandColor + "/fa <interval" + or + helpCommandColor + "int>" + helpObligatoryColor + " <minutes>" + helpMainColor + " - Set the time between each announcement.");
         player.sendMessage(helpCommandColor + "/fa <random" + or + helpCommandColor + "rand>" + helpObligatoryColor + " <on" + or + helpObligatoryColor + "off>" + helpMainColor + " - Set random or consecutive.");
     }
 	private void load()
 	{
 		Settings.load();
 		Interval = Settings.getInt("Settings.Interval", 5);
 		isRandom = Settings.getBoolean("Settings.Random", false);
 		permission = Settings.getBoolean("Settings.Permission", true);
 		strings = Settings.getStringList("Announcer.Strings", new ArrayList<String>());
 		Tag = colorize(Settings.getString("Announcer.Tag", "&GOLD;[FrogAnnounce]"));
 		toGroups = Settings.getBoolean("Announcer.ToGroups", true);
 		Groups = Settings.getStringList("Announcer.Groups", new ArrayList<String>());
 	}
 	private String colorize(String announce)
 	{
 		announce = announce.replaceAll("&AQUA;",		ChatColor.AQUA.toString());
 		announce = announce.replaceAll("&BLACK;",		ChatColor.BLACK.toString());
 		announce = announce.replaceAll("&BLUE;",		ChatColor.BLUE.toString());
 		announce = announce.replaceAll("&DARK_AQUA;",	ChatColor.DARK_AQUA.toString());
 		announce = announce.replaceAll("&DARK_BLUE;",	ChatColor.DARK_BLUE.toString());
 		announce = announce.replaceAll("&DARK_GRAY;",	ChatColor.DARK_GRAY.toString());
 		announce = announce.replaceAll("&DARK_GREEN;", 	ChatColor.DARK_GREEN.toString());
 		announce = announce.replaceAll("&DARK_PURPLE;",	ChatColor.DARK_PURPLE.toString());
 		announce = announce.replaceAll("&RED;", 		ChatColor.RED.toString());
 		announce = announce.replaceAll("&DARK_RED;",	ChatColor.DARK_RED.toString());
 		announce = announce.replaceAll("&GOLD;",		ChatColor.GOLD.toString());
 		announce = announce.replaceAll("&GRAY;",		ChatColor.GRAY.toString());
 		announce = announce.replaceAll("&GREEN;",		ChatColor.GREEN.toString());
 		announce = announce.replaceAll("&LIGHT_PURPLE;",	ChatColor.LIGHT_PURPLE.toString());		
 		announce = announce.replaceAll("&b;",		ChatColor.AQUA.toString());
 		announce = announce.replaceAll("&0;",		ChatColor.BLACK.toString());
 		announce = announce.replaceAll("&9;",		ChatColor.BLUE.toString());
 		announce = announce.replaceAll("&3;",		ChatColor.DARK_AQUA.toString());
 		announce = announce.replaceAll("&1;",		ChatColor.DARK_BLUE.toString());
 		announce = announce.replaceAll("&8;",		ChatColor.DARK_GRAY.toString());
 		announce = announce.replaceAll("&2;", 		ChatColor.DARK_GREEN.toString());
 		announce = announce.replaceAll("&5;",		ChatColor.DARK_PURPLE.toString());
 		announce = announce.replaceAll("&4;",		ChatColor.DARK_RED.toString());
 		announce = announce.replaceAll("&6;",		ChatColor.GOLD.toString());
 		announce = announce.replaceAll("&7;",		ChatColor.GRAY.toString());
 		announce = announce.replaceAll("&a;",		ChatColor.GREEN.toString());
 		announce = announce.replaceAll("&d;",		ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&c;",		ChatColor.RED.toString());
 		announce = announce.replaceAll("&f;",		ChatColor.WHITE.toString());
 		announce = announce.replaceAll("&e;",		ChatColor.YELLOW.toString());
 		return announce;
 
 	}
     class printAnnounce implements Runnable
     {
         @Override
 		public void run()
         {
         	String announce = "";
         	
         	if(isRandom)
         	{
 	            Random randomise = new Random();
 	            int selection = randomise.nextInt(strings.size());
 	            announce = strings.get(selection);
         	}else{
         		announce = strings.get(counter);
         		counter++;
         		if(counter >= strings.size())
         			counter = 0;
         	}
         	if(permission && toGroups){
         		Player[] players = getServer().getOnlinePlayers();
        			for(Player p: players){
        				for(String group: Groups){
        					if(Permissions.inGroup(p.getWorld().getName(), p.getName(), group)){
        						for (String line : announce.split("&NEW_LINE;"))
        							p.sendMessage(Tag+" "+colorize(line));
        						break;
        					}
         			}
         		}
         	}
         	else{
 					for (String line : announce.split("&NEW_LINE;"))
         			getServer().broadcastMessage(Tag+" "+colorize(line));
         	}
         }
     }
     public boolean isDebugging(Player player)
     {
     	if(!permission(player, "frogchat.admin", player.isOp()) || !permission(player, "frogchat.autoignore", player.isOp())){
         if(debugees.containsKey(player)){
             return ((Boolean)debugees.get(player)).booleanValue();
         }
         else{
             return false;
         }
     	}
 		return false;
     }
     public void setDebugging(Player player, boolean value)
     {
         debugees.put(player, Boolean.valueOf(value));
     }
     private final HashMap<Object, Object> debugees = new HashMap<Object, Object>();
 }
