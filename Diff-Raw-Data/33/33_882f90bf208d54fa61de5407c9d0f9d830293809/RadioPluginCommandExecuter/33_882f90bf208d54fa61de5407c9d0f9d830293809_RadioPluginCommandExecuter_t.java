 package io.github.LilParker.RadioPlugin;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 
 public class RadioPluginCommandExecuter implements CommandExecutor {
 	
 	RadioPlugin plugin;
 	public HashMap<String, Float> playerFreqs = new HashMap<String, Float>();
 	
 	public RadioPluginCommandExecuter (RadioPlugin actPlugin) {
 		plugin = actPlugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("setfreq")){
 			if(sender instanceof Player){
 				if(args.length == 1){
 					if(((Player) sender).getItemInHand().getTypeId() == plugin.getConfig().getInt("radioitemid")){
						if(testParse(args[0])){
 							playerFreqs.put(sender.getName(), Float.parseFloat(args[0]));
 							sender.sendMessage("Your frequency is now " + playerFreqs.get(sender.getName()));
 							return true;
 						}else{
							sender.sendMessage("Invalid frequency");
							return false;
 						}
 					}else{
 						sender.sendMessage("You must have a radio in hand to tune it");
						return true;
 					}
 				}else{
 					sender.sendMessage("Need 1 argument");
 					return false;
 				}
 				
 			}else{
 				sender.sendMessage("You must be a player");
 				return false;
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("getfreq")){
 			if(sender instanceof Player){
 				if(((Player) sender).getItemInHand().getTypeId() == plugin.getConfig().getInt("radioitemid")){
 					if(playerFreqs.get(sender.getName()) != null){
 						sender.sendMessage("Your current frequency is " + playerFreqs.get(sender.getName()));
						return true;
 					}else{
 						sender.sendMessage("Your radio isn't tuned to a frequency");
 						return true;
 					}
 				}else{
 					sender.sendMessage("You must have a radio in your hand to check it's frequency");
					return true;
 				}
 			}else{
 				sender.sendMessage("You must be a player");
 				return false;
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("radio")){
 			if(sender instanceof Player){
 				if(((Player) sender).getItemInHand().getTypeId() == plugin.getConfig().getInt("radioitemid")){
 				if(playerFreqs.get(sender.getName()) != null){
 					String message = "[FREQ: " + playerFreqs.get(sender.getName()) + "] " + sender.getName() + ": ";
 					for(String messagePart : args){
 						message = message + " " + messagePart;
 					}
 					Player[] playerList = Bukkit.getOnlinePlayers();
 					for(Player player : playerList){
 						if(playerFreqs.get(player.getName()) != null && player.getInventory().contains(plugin.getConfig().getInt("radioitemid"))){
 							float playerFreq = playerFreqs.get(player.getName());
 							if(playerFreq == playerFreqs.get(sender.getName())){
 								player.sendMessage(message);
 							}
 						}
 					}
					return true;
 				}else{
 					sender.sendMessage("Your radio isn't tuned to a frequency");
					return true;
 				}
 			}else{
 				sender.sendMessage("You must have a radio in your hand to use it");
				return true;
 			}
 			}else{
 				sender.sendMessage("You must be a player");
 				return false;
 			}
 		}
 		return false;
 	}
	private boolean testParse (String str) {
		try{
			Float.parseFloat(str);
			return true;
		}catch(NumberFormatException nfe){
			return false;
		}
	}
 }
