 package org.nationsatwar.kitty;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.nationsatwar.kitty.Events.DamageEvents;
 import org.nationsatwar.kitty.Events.TargetEvents;
 import org.nationsatwar.kitty.Utility.CommandParser;
 import org.nationsatwar.kitty.Utility.ConfigHandler;
 
 /**
  * The iSpy parent class.
  * <p>
  * Custom scripting plugin for Minecraft
  * 
  * @author Aculem
  */
 public final class Kitty extends JavaPlugin {
 	
	public final CommandParser commandParser = new CommandParser(this);
 	public final SumoManager sumoManager = new SumoManager(this);
 	
 	private static final Logger log = Logger.getLogger("Minecraft");
 
 	/**
 	 * Initializes the plugin on server startup.
 	 */
 	public void onEnable() {
 		
 		// Creates all the default Sumos packaged with the plugin
 		ConfigHandler.createDefaultSumoFiles();
 		
     	// Register Events
 		getServer().getPluginManager().registerEvents(new DamageEvents(this), this);
 		getServer().getPluginManager().registerEvents(new TargetEvents(this), this);
 		
 		// Set Command Executor
     	getCommand("kitty").setExecutor(commandParser);
     	
     	log("Kitty has been enabled.");
 	}
 
 	/**
 	 * Handles the plugin on server stop.
 	 */
 	public void onDisable() {
 		
 		log("Kitty has been disabled.");
 	}
 
 	/**
 	 * Plugin logger handler. Useful for debugging.
 	 * 
 	 * @param logMessage  Message to send to the console.
 	 */
 	public static void log(String logMessage) {
 		
 		log.info("Kitty: " + logMessage);
 	}
 }
