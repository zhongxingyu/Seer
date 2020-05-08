 package edu.unca.jderbysh.MinedGames;
 
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.google.common.base.Joiner;
 
 /*
  * This is a sample CommandExectuor
  */
 public class MinedGamesCommandExecutor implements CommandExecutor {
     private final MinedGames plugin;
 
     /*
      * This command executor needs to know about its plugin from which it came from
      */
     public MinedGamesCommandExecutor(MinedGames plugin) {
         this.plugin = plugin;
     }
 
     /*
      * On command set the sample message
      */
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equals("message") && sender.hasPermission("message")) {
             this.plugin.getConfig().set("message", Joiner.on(' ').join(args));
             return true;
         } else if (command.getName().equals("score")){
         	((Player)sender).sendMessage("" + plugin.scores.get((Player)sender));
             return true;
         } else if (command.getName().equals("join")) {
         	//plugin.addPlayer((Player)sender);  //doesn't work!
         	
         	plugin.getLogger().info("Trying to join " + (Player)sender + " to a mined game.");
         	if(!plugin.gameRunning && plugin.gameInit) {
 	        	if(plugin.scores.containsKey((Player)sender)) {
 	        		plugin.getLogger().info(((Player)sender).getDisplayName() + " has already joined!");
 	        		((Player)sender).sendMessage("You are already in the game.");
 	        	}
 	        	else {
 	        		((Player)sender).sendMessage("You have joined a Mined Game!");
 	        		plugin.scores.put((Player)sender, 0);
 	        	}
         	}
         	else {
         		plugin.getLogger().info("The Mined Game has not started just yet.");
         	}
 	        return true;
         } else if (command.getName().equals("start")){
        	plugin.getLogger().info("Trying to start a Mined game");
        	if(plugin.gameInit) {
        		plugin.getLogger().info("Starting a Mined Game");
        		plugin.gameRunning = true;
        	}
         	return true;
     	} else if (command.getName().equals("lobby")) {
     		plugin.gameInit = true;
     		plugin.getLogger().info("The next Mined Game will start soon.");
     		plugin.getServer().broadcastMessage(((Player)sender).getDisplayName() + "has created a new Mined Game. \nType /join to go to the lobby!");
     		return true;
     	} else {
         	return false;
         }
     }
 
 }
