 package main;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class command extends JavaPlugin {
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args0) {
 		Server server = getServer();
 		if(command.getName().equalsIgnoreCase("userstatus")){
 			if(sender.hasPermission("userstatus.use") || sender.isOp()){
 				if(args0.length == 1){
 					if(server.getPlayer(args0[0]) instanceof Player){
 						sender.sendMessage(args0[0] + " is " + ChatColor.GREEN + "Online.");
 						
 						if(sender.hasPermission("userstatus.see.IP") || sender.isOp()) 
 							sender.sendMessage("Adress: " + server.getPlayer(args0[0]).getAddress().toString()); 
 						else sender.sendMessage("Adress: " + ChatColor.RED + "You may not see this person's Adress.");
 						
 						if(sender.hasPermission("userstatus.see.GameMode") || sender.isOp()) 
 							sender.sendMessage("Current GameMode: " + server.getPlayer(args0[0]).getPlayer().getGameMode().toString()); 
 						else sender.sendMessage("GameMode: " + ChatColor.RED + "You may not see this person's GameMode.");
 						
 						if(sender.hasPermission("userstatus.see.World") || sender.isOp()) 
 							sender.sendMessage("World: " + server.getPlayer(args0[0]).getPlayer().getWorld().getName()); 
 						else sender.sendMessage("World: " + ChatColor.RED + "You may not see this person's current World.");
 						
 						if(sender.hasPermission("userstatus.see.Position") || sender.isOp())
 							sender.sendMessage("Position: " + String.valueOf(Math.round(server.getPlayer(args0[0]).getLocation().getX())) + ", " + String.valueOf(Math.round(server.getPlayer(args0[0]).getLocation().getY())) + ", " + String.valueOf(Math.round(server.getPlayer(args0[0]).getLocation().getZ()))); 
 						else sender.sendMessage("Position: " + ChatColor.RED + "You may not see this person's Position.");
 						
 						if(sender.hasPermission("userstatus.see.OP") || sender.isOp()) 
 							if(!(server.getPlayer(args0[0]).isOp())) sender.sendMessage("OP: " + ChatColor.RED + "No"); 
 							else sender.sendMessage("OP: " + ChatColor.GREEN + "Yes"); 
 						else sender.sendMessage("OP: " + ChatColor.RED + "You may not see this person's OP status.");
 						
 						if(sender.hasPermission("userstatus.see.Whitelist") || sender.isOp()) 
 							if(!(server.getPlayer(args0[0]).isWhitelisted())) sender.sendMessage("Whitelisted: " + ChatColor.RED + "No"); 
 							else sender.sendMessage("Whitelisted: " + ChatColor.GREEN + "Yes"); 
 						else sender.sendMessage("Whitelisted: " + ChatColor.RED + "You may not see this person's Whitelist status.");
 						
 						if(sender.hasPermission("userstatus.see.PlayedBefore")) 
 							if(!(server.getOfflinePlayer(args0[0]).hasPlayedBefore()))
 								sender.sendMessage(ChatColor.RED + "This user has never played before!");
 							
 						return true;
 					} else {
 						sender.sendMessage(args0[0] + " is " + ChatColor.RED + "Offline.");
 						
 						if(sender.hasPermission("userstatus.see.PlayedBefore")) 
 							if(!(server.getOfflinePlayer(args0[0]).hasPlayedBefore())) 
 								sender.sendMessage(ChatColor.RED + "This user has never played before!");
 						
 						if(sender.hasPermission("userstatus.see.Whitelist")) 
 								if(!(server.getOfflinePlayer(args0[0]).isWhitelisted())) 
 									sender.sendMessage("Whitelist: " + ChatColor.RED + "No"); 
 								else sender.sendMessage("Whitelist: " + ChatColor.GREEN + "Yes");
 						
 						if(sender.hasPermission("userstatus.see.OP")) 
 							if(!(server.getOfflinePlayer(args0[0]).isOp())) 
 								sender.sendMessage("OP: " + ChatColor.RED + "No"); 
 							else sender.sendMessage("OP: " + ChatColor.GREEN + "Yes");
 						
 						return true;
 					}
 				} else {
 					sender.sendMessage("Please provide a username!");
 					sender.sendMessage(ChatColor.RED + "Usage: /userstatus [username]");
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "You don't have permission!");
 				return true;
 			}
 		} return false;
 	}
 }
