 package org.Ter13.VeryStickyPistons;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 
 public class VeryStickyPistons extends org.bukkit.plugin.java.JavaPlugin {
 
 	
 	public Logger log;
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void onEnable() {
 		// TODO Auto-generated method stub
 		log = Logger.getLogger("Minecraft");
 		PluginManager pm = this.getServer().getPluginManager();
		StickyPistonListener spl = new StickyPistonListener(this);
 		pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND, spl, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT, spl, Event.Priority.Normal, this);
 		log.info("[VSP] Pistons are now very sticky.");
 	}
 
 }
