 package de.frozenbrain.MessageChanger;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 public class mcPlugin extends JavaPlugin {
 	
 	private final mcPlayerListener playerListener = new mcPlayerListener(this);
 	public String msgKickBanned;
 	public String msgKickWhitelist;
 	public String msgKickFull;
 	public String msgJoin;
 	public String msgKickReason;
 	public String msgKickLeave;
 	public String msgPlayerQuit;
 	
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
 		
 		reloadConfig();
 		
 		PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 	
 	public void onDisable() {
 		System.out.println("MessageChanger disabled.");
 	}
 	
 	public void reloadConfig() {
 		Configuration config = getConfiguration();
 		config.load();
 		msgKickBanned = config.getString("KICK_BANNED", "You are banned from this server!");
 		msgKickWhitelist = config.getString("KICK_WHITELIST", "You are not white-listed on this server!");
 		msgKickFull = config.getString("KICK_FULL", "The server is full!");
		msgJoin = config.getString("PLAYER_JOIN", "&e%pName joined the game.");
 		msgKickReason = config.getString("KICK_KICK_REASON", "Kicked by admin");
		msgKickLeave = config.getString("KICK_KICK_LEAVEMSG", "&e%pName left the game.");
 		msgPlayerQuit = config.getString("PLAYER_QUIT", "%pName left the game.");
 		config.save();
 	}
 	
 }
