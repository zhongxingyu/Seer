 package com.bukkitfiller.CobbleCraft;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CobbleCraft extends JavaPlugin {
 	
 	public static String FILEDIRECTORY;
 	protected Logger log = Logger.getLogger("Minecraft");
	private PluginDescriptionFile desc = getDescription();
 	private CobbleCraftBlockListener blockListener = new CobbleCraftBlockListener(this);
 	private CobbleCraftPlayerListener playerListener = new CobbleCraftPlayerListener(this);
 	private CobbleCraftCommandExecutor commandExecutor = new CobbleCraftCommandExecutor(this);
 	CobbleCraftFileHandler fileHandler = new CobbleCraftFileHandler(this);
 	LevelValues lvlValues = new LevelValues(this);
 
 	public void onEnable() {
		FILEDIRECTORY = this.getDataFolder().toString();
 		PluginManager pm = getServer().getPluginManager();
 		CobbleCraftFileHandler.writeDir(FILEDIRECTORY);
 		
 		getCommand("mining").setExecutor(commandExecutor);
 		getCommand("digging").setExecutor(commandExecutor);
 		getCommand("fishing").setExecutor(commandExecutor);
 		
 		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this.playerListener, Priority.Normal, this);
 		
		consoleInfo(desc.getVersion() + " was enabled.");
 	}
 
 	public void onDisable() {
 		
 	}
 	
 	public void consoleInfo(String string) {
 		log.info(desc.getName() + ": " + string);
 	}
 
 	public void consoleWarning(String string) {
 		log.warning(desc.getName() + ": " + string);
 	}
 	
 }
