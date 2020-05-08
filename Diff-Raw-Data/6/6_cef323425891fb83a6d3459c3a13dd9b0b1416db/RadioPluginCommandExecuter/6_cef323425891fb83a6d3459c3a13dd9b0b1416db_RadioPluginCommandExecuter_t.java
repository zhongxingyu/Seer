 package io.github.LilParker.RadioPlugin;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 
 public class RadioPluginCommandExecuter implements CommandExecutor {
 	
 	RadioPlugin plugin;
 	public HashMap<String, Float> playerFreqs = new HashMap<String, Float>();
 	public HashMap<String, String> eKey = new HashMap<String, String>();
 	private String[] alphanumeric = new String[]{"a","b","c","d","e","f","g","h","i","k","j","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
 	
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
 		if (cmd.getName().equalsIgnoreCase("setEKey")){
 			if (sender instanceof Player){
 				if (args.length==1){
 					if(((Player) sender).getItemInHand().getTypeId() == plugin.getConfig().getInt("radioitemid")){
 						if(args[0].equals("clear") == false){
 							eKey.put(sender.getName(), (args[0]));
 							sender.sendMessage("Your encryption key is now " + eKey.get(sender.getName()));
 							return true;
 							}else{
 							eKey.put(sender.getName(), null);
 							sender.sendMessage("Your encription key has been cleared");
 							return true;
 							}
 						}else{
 						sender.sendMessage("You must have a radio in your hand to do this!");
 						return true;
 						}
 					}else{
 					sender.sendMessage("Need 1 argument!");
 					return false;
 					}
 				}else{
 					sender.sendMessage("You must be a player to use this command");
 					return true;
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
 							String enKey = eKey.get(player.getName());
 							if(playerFreq == playerFreqs.get(sender.getName())){
 									if(eKey.get(sender.getName()) != null){
 										if(enKey != null && enKey.equals(eKey.get(sender.getName()))){
											message = plugin.getConfig().getString("radioencryptedcolor") + message;
 											player.sendMessage(message);
 									}else if(enKey != eKey.get(sender.getName())){
 										Random randGen = new Random();
 										String msg = "";
 										for(String messagePart : args){
 											msg = msg + " " + messagePart;
 										}
 										String scrambledMessage = plugin.getConfig().getString("radioencryptedcolor") + "[FREQ: " + playerFreqs.get(sender.getName()) + "] " + sender.getName() + ": ";
 										for(char ch : msg.toCharArray()){
 											if(ch != ' '){
 											int randInt = randGen.nextInt(alphanumeric.length);
 											String str = alphanumeric[randInt];
 											scrambledMessage = scrambledMessage + str;
 											}else{
 											scrambledMessage = scrambledMessage + " ";
 											}
 										}
 										player.sendMessage(scrambledMessage);
 									}
 								}else{
									message = plugin.getConfig().getString("radiocolor") + message;
 									player.sendMessage(message);
 								}
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
