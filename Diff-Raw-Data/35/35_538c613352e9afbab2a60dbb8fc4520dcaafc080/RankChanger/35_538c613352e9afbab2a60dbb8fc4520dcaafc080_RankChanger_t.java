 package me.armar.plugins.autorank;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class RankChanger implements Listener {
 
     private Autorank plugin;
     private Config config;
     private Config data;
     private VaultHandler vault;
 
     public RankChanger(Autorank plugin) {
 	this.plugin = plugin;
 	config = plugin.getConf();
 	data = plugin.getData();
 	vault = plugin.getVault();
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void OnPlayerJoin(PlayerJoinEvent e) {
 	CheckRank(e.getPlayer());
     }
 
     public void CheckRank(Player player) {
 	
	String playerName = player.getName().toLowerCase();
	
	if (!data.exists(playerName)) {
	    data.set(playerName, 0);
	}
	
 	if((Boolean)config.get("Enabled") == null){
 	    plugin.logMessage("Section 'Enabled' was not found in the config, please check that you are not using a pre-1.0 config");
 	    return;
 	}
 	
 	if(!(Boolean)config.get("Enabled")){
 	    plugin.logMessage("Plugin disabled from config. Please set 'enabled' to true after setting up the config.");
 	    return;
 	}
 	
 	String world = player.getWorld().getName();
 
 	String currentRank = vault.getRank(player, world);
	if(currentRank == null){currentRank = "Default";}
 
 	plugin.debugMessage("Player " + playerName + " logged on in world "
 		+ world + " and has rank " + currentRank);
 
 	int entry = 1;
 	boolean found = false;
 	while (config.get(entry + ".from") != null && !found) {
 	    
		if(plugin.getDebug()){
			plugin.debugMessage("Testing entry " + entry);
			plugin.debugMessage("From: " + (String) config.get(entry + ".from"));
			plugin.debugMessage("To: " + (String) config.get(entry + ".to"));
			plugin.debugMessage("World: " + (String) config.get(entry + ".world"));
			}
 	
	    if (currentRank.equalsIgnoreCase((String) config.get(entry + ".from"))
 		    && (config.get(entry + ".world") == null)
		    || world.equalsIgnoreCase((String) config.get(entry + ".world"))) {
 		found = true;
 		plugin.debugMessage("Confirmed entry " + entry);
 	    }else{
 		entry++;
 	    }
 	}
 	
 	if (found != true)
 	    return;
 
 	Integer timePlayed = (Integer) data.get(playerName);
 	
 	plugin.debugMessage("Time played: " + timePlayed);
 	plugin.debugMessage("Time needed: " + (Integer) config.get(entry
 		+ ".required minutes played"));
 	
 	if (timePlayed == null
 		|| timePlayed < (Integer) config.get(entry
 			+ ".required minutes played"))
 	    return;
 
 	vault.setRank(playerName, currentRank,
 		(String) config.get(entry + ".to"), world);
 
 	String message = (String) config.get(entry + ".message");
 	if(message != null)
 	player.sendMessage(message.replaceAll("(&([a-f0-9]))", "\u00A7$2"));
 	
 	
 	String commandEntry = (String)config.get(entry + ".commands");
 	String[] commands = null;
 	if (commandEntry != null)
 	    {commands = commandEntry.split(";");}
 	
 	if (commands != null) {
 	    for (String cmd : commands) {
 		Server server = Bukkit.getServer();
 		if (cmd != null) {
 		    cmd = cmd.replace("&p", playerName);
 		    server.dispatchCommand(server.getConsoleSender(), cmd);
 		}
 	    }
 	}
 	
 	plugin.debugMessage("Sent message: " + message);
 
     }
 
 }
