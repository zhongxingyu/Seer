 package com.alta189.minemail;
 
 import java.io.File;
 import com.iConomy.*;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.alta189.minemail.addons.AddonManager;
 import com.alta189.minemail.command.CommandHandler;
 import com.alta189.minemail.config.ConfigCore;
 import com.alta189.minemail.listeners.MineMailPlayerListener;
 import com.alta189.minemail.listeners.ServerMonitor;
 import com.alta189.sqllitelib.sqlCore;
 
 public class MineMail extends JavaPlugin {
 	
 	//Declare all the basic objects\\
 	public final Logger log = Logger.getLogger("Minecraft");
 	public String version = "1.0";
 	public final File pFolder = new File("plugins" + File.separator + "MineMail");
 	public final String logPrefix = "[MineMail] ";
 	
 	//Declare all of the Handlers\\
 	public sqlCore dbManage;
 	public AddonManager addons = new AddonManager(this);
 	public CommandHandler command = new CommandHandler(this);
 	public MailServer mmServer = new MailServer(this);
 	public ConfigCore config = new ConfigCore(this);
 	
 	//Declare all of the Listeners\\
 	public MineMailPlayerListener pListener = new MineMailPlayerListener(this);
 	public ServerMonitor sMonitor = new ServerMonitor(this);
 	
 	//Declare any other variables\\
 	public Boolean ScheduledWipe = false;
 	public int DelayWipeTime = 60; //Time in seconds to delay the wipe
 	
 	//Declare the iConomy plugin\\
 	public iConomy iConomy = null;
 	
 	@Override
 	public void onDisable() {
 		
 		this.log.info(this.logPrefix + "v " + version + "is disabled");
 		
 	}
 	
 	@Override
 	public void onEnable() {
 		// pdfFile \\
 		PluginDescriptionFile pdfFile = getDescription();
 		this.version =  pdfFile.getVersion();
 		this.log.info(this.logPrefix + "v " + version + " is initializing");
 		
 		//Create folders and database\\
 		createPluginFolder();
 		
 		this.config.initialize();
 		
		this.addons.initialize();
		
 		this.dbManage = new sqlCore(this.log, this.logPrefix, "mail", pFolder.getPath());
 		
 		this.dbManage.initialize();
 		
 		if (!this.dbManage.checkTable("mails")) {
 			String query1 = "CREATE  TABLE mails ( 'id' INTEGER PRIMARY KEY,  'sender' VARCHAR(80) NOT NULL ,  'receiver' VARCHAR(80) NOT NULL ,  'message' TEXT NOT NULL ,  'read' INT NOT NULL DEFAULT 0);";
 			dbManage.createTable(query1);
 		}
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		//Register Events\\
 		pm.registerEvent(Event.Type.PLAYER_JOIN, this.pListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.pListener, Event.Priority.Normal, this);
 		getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, this.sMonitor, Priority.Monitor, this);
         getServer().getPluginManager().registerEvent(Type.PLUGIN_DISABLE, this.sMonitor, Priority.Monitor, this);
        
        this.log.info(this.logPrefix + "v " + version + " is initialized");
 	}
 	
 	public void onDisable(PluginDisableEvent event) {
         
     }
 
 	//Command Executer\\
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = (Player) sender;
 		if (commandLabel.equalsIgnoreCase("mail")) {
 			 if (args.length >= 1) {
 				 	if (args[0].equalsIgnoreCase("read") && player != null) {
 				 		this.command.read(player, cmd, commandLabel, args);
 				 	} else if (args[0].equalsIgnoreCase("write") && player != null) {
 				 		if (args.length >= 3) {
 				 			this.command.write(player, cmd, commandLabel, args);
 				 		} else {
 				 			player.sendMessage("<help>/mm write <player name> <message>"); 
 				 		}
 				 	} else if (args[0].equalsIgnoreCase("help") && player != null) {
 				 		this.command.help(player, cmd, commandLabel, args);
 				 	} else if (args[0].equalsIgnoreCase("admin") && player != null) {
 				 		this.command.admin(player, cmd, commandLabel, args);
 				 	} else if (args[0].equalsIgnoreCase("wipe") && player != null) {
 				 		this.command.wipe(player, cmd, commandLabel, args);
 				 	} else if (args[0].equalsIgnoreCase("reload") && player != null) {
 				 		this.command.reload(player, cmd, commandLabel, args);
 				 	}
 			 }
 		}
 		return false;
 	}
 	
 	public Boolean isAdmin(Player player, String type) { //Handles Permissions\OP access to commands
 		if (type.contains("/")) {
 			for (String subType : type.split("/")) {
 				if (player.isOp() || this.addons.PermManager.hasPermissions(player, "admin") || this.addons.PermManager.hasPermissions(player, subType)) {
 					return true;
 				}
 			}
 		} else {
 			if (player.isOp() || this.addons.PermManager.hasPermissions(player, "admin") || this.addons.PermManager.hasPermissions(player, type)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public Boolean isFree(Player player) {
 		if (this.config.settingsFile.OPfree && player.isOp()) {
 			return true;
 		}
 		if (this.addons.PermManager.hasPermissions(player, "free")) {
 			return true;
 		}
 		return false;
 	}
 	
 	public void createPluginFolder() { //This will create the plugin folder so that we can store stuff in it!
 		if (!this.pFolder.exists()) {
 			pFolder.mkdir();
 		}
 	}
 
 	public Player getPlayer(String playername) { //This will get the player from his/her name
 		Player player = null;
 		
 		for (Player checkPlayer : this.getServer().getOnlinePlayers()) { 
 			//for each player in online player do this 
 			if (checkPlayer.getName().equalsIgnoreCase(playername)) {
 				player = checkPlayer;
 			}
 		}
 		
 		return player;
 	}
 	
 	public void notifyReceiver(String playername) { //This is an easy way to notify the player when he gets a message
 		Player receiver = this.getPlayer(playername);
 		if (receiver != null) {
 			addons.msgFormat.formatAndSend("MineMail - You got a message", receiver);
 		}
 	}
 }
