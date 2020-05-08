 package me.duper51.BattlefieldPlugin;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BFMain extends JavaPlugin {
 	private final BFPlayerListener plistener = new BFPlayerListener();
 	Logger log = Logger.getLogger("Minecraft");
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		log.info("[BFPlugin] Battlefield Plugin is disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		// TODO Auto-generated method stub
 		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_LOGIN, plistener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, plistener, Priority.Normal, this);
 		getCommand("bf").setExecutor(new BFCMDEXE());
 	}
 
 }
