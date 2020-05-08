 package me.Kruithne.UniqueInventories;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class UniqueInventories extends JavaPlugin {
 	
 	public void onEnable()
 	{
 		this.getServer().getPluginManager().registerEvents(
 			new PlayerListener(
				new InventoryHandler(),
				this.getServer()
				
 			),
 			this
 		);
 	}
 
 }
