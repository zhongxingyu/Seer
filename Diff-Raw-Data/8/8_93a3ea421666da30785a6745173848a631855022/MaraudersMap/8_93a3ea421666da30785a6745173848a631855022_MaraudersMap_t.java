 package com.bukkit.ojrac;
 import java.io.File;
 
 import org.bukkit.Server;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * MaraudersMap for Bukkit
  *
  * @author ojrac
  */
 public class MaraudersMap extends JavaPlugin {
	/** Guessed from the Bukkit source */
	private static final int MS_PER_TICK = 50;
	
	/** The plugin's runnable */
     private PlayerListManager listManager;
     
     public MaraudersMap(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc,
     		File folder, File plugin, ClassLoader cLoader) {
     	super(pluginLoader, instance, desc, folder, plugin, cLoader);
     }
 
     @Override
     public void onEnable() {
     	if (listManager == null) {
     		listManager = new PlayerListManager(this);
     	}
     	
    	int tickDelay = listManager.delayMillis / MS_PER_TICK;
    	getServer().getScheduler().scheduleSyncRepeatingTask(this, listManager, tickDelay, tickDelay);
     }
 
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
     }
 }
 
