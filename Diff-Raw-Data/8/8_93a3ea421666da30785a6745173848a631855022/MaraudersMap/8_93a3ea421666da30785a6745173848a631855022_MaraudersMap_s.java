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
     	
    	getServer().getScheduler().scheduleSyncRepeatingTask(this, listManager, listManager.delayMillis, listManager.delayMillis);
     }
 
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
     }
 }
 
