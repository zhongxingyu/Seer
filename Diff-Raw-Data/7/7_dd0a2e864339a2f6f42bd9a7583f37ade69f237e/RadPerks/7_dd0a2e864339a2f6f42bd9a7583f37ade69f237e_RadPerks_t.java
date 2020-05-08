package me.draosrt.radperks;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RadPerks extends JavaPlugin {
 
 	protected RadPerksLogHandler log;
 	public final RadPerksPlayerListener playerListener = new RadPerksPlayerListener(this);
 	
 	@Override
 	public void onEnable() {
 		this.log = new RadPerksLogHandler (this);
 		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Normal, this);
 		
 		this.log.info("Enabled");
 		
 	}
 
 	@Override
 	public void onDisable() {
 		this.log.info("Disabled");
 		
 		
 	}
 
 	
 	
 }
