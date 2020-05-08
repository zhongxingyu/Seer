 package me.krotn.Rent;
 
 import java.util.logging.Logger;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Rent extends JavaPlugin{
 	
 	private Logger log = Logger.getLogger("Minecraft");
 	private RentLogManager logManager = new RentLogManager(log);
	private final PlayerListener playerListener = new RentPlayerListener(this);
 	private RentDatabaseManager dbman = new RentDatabaseManager();
 	
 	public void onEnable(){
 		logManager.info("Rent enabled");
 		PluginManager pluginManager = getServer().getPluginManager();
 		pluginManager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 		dbman.connect();
 	}
 	
 	public void onDisable(){
 		logManager.info("Rent disabled");
 		dbman.disconnect();
 	}
 	
 	public RentLogManager getLogManager(){
 		return logManager;
 	}
 }
