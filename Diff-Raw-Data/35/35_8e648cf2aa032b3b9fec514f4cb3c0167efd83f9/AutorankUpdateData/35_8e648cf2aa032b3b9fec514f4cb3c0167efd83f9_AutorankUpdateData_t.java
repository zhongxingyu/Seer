 package me.armar.plugins.autorank;
 
 import java.util.TimerTask;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.earth2me.essentials.Essentials;
 
 public class AutorankUpdateData extends TimerTask {
 
     private Autorank plugin;
     private Player[] onlinePlayers;
     private Config data;
     private Config config;
     private boolean afkCheck = false;
     private Essentials ess;
 
     public AutorankUpdateData(Autorank plugin) {
 	this.plugin = plugin;
 	this.data = this.plugin.getData();
 	this.config = this.plugin.getConf();
	
 	if (config.get("Essentials AFK integration") != null && (Boolean) config.get("Essentials AFK integration") == true)
 	    afkCheck = true;
 
 	if (afkCheck) {
 	    Plugin x = plugin.getServer().getPluginManager()
 		    .getPlugin("Essentials");
 	    if (x != null & x instanceof Essentials) {
 		ess = (Essentials) x;
 	    } else {
 		plugin.logMessage("Essentials was NOT found! Disabling AFK integration.");
 		afkCheck = false;
 	    }
	}
     }
 
     @Override
     public void run() {
 	onlinePlayers = plugin.getServer().getOnlinePlayers();
 	updateMinutesPlayed();
 	plugin.debugMessage("Updated");
     }
 
     private void updateMinutesPlayed() {
 	int interval = (Integer) config.get("Update interval(minutes)");
 	for (int i = (onlinePlayers.length - 1); i >= 0; i--) {
 	    if (onlinePlayers[i] != null) {
 		String playerName = onlinePlayers[i].getName().toLowerCase();
 		if (!data.exists(playerName)) {
 		    data.set(playerName, 0);
 		}
		if (!(afkCheck && ess.getUser(playerName).isAfk())) {
 		    if (!onlinePlayers[i].hasPermission("autorank.timeexclude")) {
 			data.set(playerName,
 				((Integer) data.get(playerName) + interval));
 		    }
		}
 	    }
 	}
     }
 }
