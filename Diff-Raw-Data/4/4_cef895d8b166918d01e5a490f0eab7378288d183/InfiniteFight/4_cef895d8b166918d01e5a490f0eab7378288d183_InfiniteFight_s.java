 package fi.dy.esav.InfiniteFight;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class InfiniteFight extends JavaPlugin {
 
 	Logger log = Logger.getLogger("Minecraft");
 
 	InfiniteFightEntityListener entityListener = new InfiniteFightEntityListener();
 	
 	static boolean enabled = false;
 	
 	public void onEnable() {
 
 		log.info(this.getDescription().getName() + " version "
 				+ this.getDescription().getVersion() + " started.");
 
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(entityListener, this);
 //		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener,
 //				Event.Priority.Normal, this);
 //		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
 //				Event.Priority.Normal, this);
 
 	}
 
 	public void onDisable() {
 
 		log.info(this.getDescription().getName() + " version "
 				+ this.getDescription().getVersion() + " stopped.");
 
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(args.length == 1) {
 			if(args[0] == "enable") {
 				if(enabled) {
 					sender.sendMessage("InfiniteFight already enabled");
 				} else {
 					sender.sendMessage("Enabled InfiniteFight");
 					enabled = true;
 				}
 			} else if(args[0] == "disable") {
 				if(!enabled) {
 					sender.sendMessage("InfiniteFight already disabled");
 				} else {
 					sender.sendMessage("Disabled InfiniteFight");
 					enabled = false;
 				}
 			} else if(args[0] == "status") {
 				if(enabled) {
 					sender.sendMessage("InfiniteFight is enabled");
 				} else {
 					sender.sendMessage("InfiniteFight is disabled");
 				}
 			}
 		}
 		return false;
 	}
 	
 }
