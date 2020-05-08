 package com.github.manintime.FactoryModPlugin;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.event.Listener;
 import com.github.manintime.*;
 
 public class FactoryManager {
 	List<Listener> listeners;
 	List<Manager> managers;
 	
 	FactoryModPlugin plugin; //The plugin object
 	
	public static FactoryManager facMan;
	
 	public FactoryManager (FactoryModPlugin plugin){
 		this.plugin = plugin;
		FactoryManager.facMan = this;
 		initialiseFactories();
 		
 		
 		
 	}
 	
 	private void initialiseFactories(){
		Listener factoryListener = new FactoryListener(facMan);
 		plugin.getServer().getPluginManager().registerEvents(factoryListener, plugin);
 		
 	}
 }
