 package org.Ter13.VeryStickyPistons;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 
 public class VeryStickyPistons extends org.bukkit.plugin.java.JavaPlugin {
 
 	
 	public Logger log;
	public StickyPistonListener spl;
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
		spl.isEnabled = false;
		log.info("[VSP] Pistons are no longer very sticky.");
 	}
 
 	@Override
 	public void onEnable() {
 		// TODO Auto-generated method stub
 		log = Logger.getLogger("Minecraft");
 		PluginManager pm = this.getServer().getPluginManager();
		spl = new StickyPistonListener(this);
		spl.isEnabled = true;
 		pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND, spl, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT, spl, Event.Priority.Normal, this);
 		log.info("[VSP] Pistons are now very sticky.");
 	}
 
 }
