 package me.MrForknSpoon.TourHelper;
 
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 
 public class TourHelper extends JavaPlugin{
 	
 	Logger log;
 	Configuration config;
 	HashMap<String, Integer> lastLine = new HashMap<String, Integer>();
 	
 	public void onEnable(){
 		
 		saveDefaultConfig();
 		log = getLogger();
 		loadConfig();
 		log.info("TourHelper has been enabled.");
 	}
 
 	public void onDisable(){
 		
 		log.info("TourHelper has been disabled.");
 	}
 	
 	public void loadConfig(){
 		
 		reloadConfig();
 		config = getConfig();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args){
 		
 		if (cmdLabel.equalsIgnoreCase("")){
 			if (sender instanceof Player){
 				Player player = (Player) sender;
 				if (args.length == 0){
 					if (player.hasPermission("tourhelper.send")){
 						int lineNum = (lastLine.containsKey(player.getName())) ? lastLine.get(player.getName()) + 1 : 1;
 						if (lineNum > 0){
 							if (config.contains(player.getName().toLowerCase())){
 								if (config.contains(player.getName().toLowerCase() + "." + lineNum)){
 									if (config.getString(player.getName().toLowerCase() + "." + lineNum) != null && config.getString(player.getName().toLowerCase() + "." + lineNum).length() > 0){
 										player.chat(config.getString(player.getName().toLowerCase() + "." + lineNum));
 										lastLine.put(player.getName().toLowerCase(), lineNum);
 									}else{
 										player.sendMessage(ChatColor.RED + "Line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + " is not valid.");
 									}
 								}else{
 									player.sendMessage(ChatColor.RED + "There is no line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + ".");
 								}
 							}else if (config.contains("default")){
 								if (config.contains("default." + lineNum)){
 									if (config.getString("default." + lineNum) != null && config.getString("default." + lineNum).length() > 0){
 										player.chat(config.getString("default." + lineNum));
 										lastLine.put(player.getName().toLowerCase(), lineNum);
 									}else{
 										player.sendMessage(ChatColor.RED + "Default line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + " is not valid.");
 									}
 								}else{
 									player.sendMessage(ChatColor.RED + "There is no default line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + ".");
 								}
 							}else{
 								player.sendMessage(ChatColor.RED + "There are no default " + ChatColor.WHITE + "or" + ChatColor.RED + " personal lines in the config. Use '" + ChatColor.WHITE + "/tourhelper upload <url>" + ChatColor.RED + "'.");
 							}
 						}else{
 							player.sendMessage(ChatColor.RED + "Line number " + ChatColor.WHITE + lineNum + ChatColor.RED + " is invalid. Use a number greater than 0.");
 						}
 					}else{
 						player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 					}
 				}else if (args.length == 1){
 					if (player.hasPermission("tourhelper.send")){
 						try{
 							int lineNum = Integer.parseInt(args[0]);
 							if (lineNum > 0){
 								if (config.contains(player.getName().toLowerCase())){
 									if (config.contains(player.getName().toLowerCase() + "." + lineNum)){
 										if (config.getString(player.getName().toLowerCase() + "." + lineNum) != null && config.getString(player.getName().toLowerCase() + "." + lineNum).length() > 0){
 											player.chat(config.getString(player.getName().toLowerCase() + "." + lineNum));
 											lastLine.put(player.getName().toLowerCase(), lineNum);
 										}else{
 											player.sendMessage(ChatColor.RED + "Line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + " is not valid.");
 										}
 									}else{
 										player.sendMessage(ChatColor.RED + "There is no line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + ".");
 									}
 								}else if (config.contains("default")){
 									if (config.contains("default." + lineNum)){
 										if (config.getString("default." + lineNum) != null && config.getString("default." + lineNum).length() > 0){
 											player.chat(config.getString("default." + lineNum));
 											lastLine.put(player.getName().toLowerCase(), lineNum);
 										}else{
 											player.sendMessage(ChatColor.RED + "Default line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + " is not valid.");
 										}
 									}else{
 										player.sendMessage(ChatColor.RED + "There is no default line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + ".");
 									}
 								}else{
 									player.sendMessage(ChatColor.RED + "There are no default " + ChatColor.WHITE + "or" + ChatColor.RED + " personal lines in the config. Use '" + ChatColor.WHITE + "/tourhelper upload <url>" + ChatColor.RED + "'.");
 								}
 							}else{
 								player.sendMessage(ChatColor.RED + "Line number " + ChatColor.WHITE + lineNum + ChatColor.RED + " is invalid. Use a number greater than 0.");
 							}
 						}catch (NumberFormatException nfe){
 							player.sendMessage(ChatColor.RED + "Line " + ChatColor.WHITE + args[0] + ChatColor.RED + " is invalid. Use a number greater than 0.");
 						}
 					}else{
 						player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 					}
 				}else{
 					if (player.hasPermission("tourhelper.send")) player.sendMessage(ChatColor.RED + "Invalid Command. Use '" + ChatColor.WHITE + "/ [line]" + ChatColor.RED + "'.");
 					else player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 				}
 			}else{
 				sender.sendMessage("This command can only be used ingame.");
 			}
 			return true;
 		}else if (cmdLabel.equalsIgnoreCase("tourhelper")){
 			if (sender instanceof Player){
 				Player player = (Player) sender;
 				if (args.length == 0){
					if (player.hasPermission("tourhelper.upload") || player.hasPermission("tourhelper.remove.other")) player.sendMessage(ChatColor.RED + "Invalid Command. Use '" + ChatColor.WHITE + "/" + cmdLabel + " upload <url>" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " remove [player]" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " edit <line> [new line]" + ChatColor.RED + "'.");
 					else player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 				}else if (args.length == 1){
 					if (args[0].equalsIgnoreCase("remove")){
 						if (player.hasPermission("tourhelper.upload")){
 							if (config.contains(player.getName().toLowerCase())){
 								config.set(player.getName().toLowerCase(), null);
 								saveConfig();
 								if (lastLine.containsKey(player.getName().toLowerCase())) lastLine.remove(player.getName().toLowerCase());
 								player.sendMessage(ChatColor.GREEN + "Your personal lines have been " + ChatColor.WHITE + "removed" + ChatColor.GREEN + " from the config.");
 							}else{
 								player.sendMessage(ChatColor.RED + "You do not have any personal lines to " + ChatColor.WHITE + "remove" + ChatColor.RED + " from the config.");
 							}
 						}else{
 							player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 						}
 					}else{
						if (player.hasPermission("tourhelper.upload") || player.hasPermission("tourhelper.remove.other")) player.sendMessage(ChatColor.RED + "Invalid Command. Use '" + ChatColor.WHITE + "/" + cmdLabel + " upload <url>" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " remove [player]" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " edit <line> [new line]" + ChatColor.RED + "'.");
 						else player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 					}
 				}else if (args.length == 2){
 					if (args[0].equalsIgnoreCase("upload")){
 						if (player.hasPermission("tourhelper.upload")){
 							@SuppressWarnings("unused")
 							BukkitTask task = new URLReaderTask(this, player.getName(), args[1]).runTaskLaterAsynchronously(this, 0);
 						}else{
 							player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 						}
 					}else if (args[0].equalsIgnoreCase("remove")){
 						if (player.hasPermission("tourhelper.remove.other")){
 							if (config.contains(args[1].toLowerCase())){
 								config.set(args[1].toLowerCase(), null);
 								saveConfig();
 								if (lastLine.containsKey(player.getName().toLowerCase())) lastLine.remove(player.getName().toLowerCase());
 								player.sendMessage(ChatColor.WHITE + args[1] + "'s" + ChatColor.GREEN + " personal lines have been " + ChatColor.WHITE + "removed" + ChatColor.GREEN + " from the config.");
 							}else{
 								player.sendMessage(ChatColor.WHITE + args[1] + ChatColor.RED + " does not have any personal lines to " + ChatColor.WHITE + "remove" + ChatColor.RED + " from the config.");
 							}
 						}else{
 							player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 						}
 					}else if (args[0].equalsIgnoreCase("view")){
 						if (player.hasPermission("tourhelper.upload")){
 							try{
								int lineNum = Integer.parseInt(args[0]);
 								if (lineNum > 0){
 									if (config.contains(player.getName().toLowerCase())){
 										if (config.contains(player.getName().toLowerCase() + "." + lineNum)){
 											if (config.getString(player.getName().toLowerCase() + "." + lineNum) != null && config.getString(player.getName().toLowerCase() + "." + lineNum).length() > 0){
												player.sendMessage(ChatColor.GREEN + "Line " + ChatColor.WHITE + "#" + lineNum + ChatColor.GREEN + ": " + config.getString(player.getName().toLowerCase() + "." + lineNum));
 											}else{
 												player.sendMessage(ChatColor.RED + "Line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + " is not valid.");
 											}
 										}else{
 											player.sendMessage(ChatColor.RED + "There is no line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + ".");
 										}
 									}else if (config.contains("default")){
 										if (config.contains("default." + lineNum)){
 											if (config.getString("default." + lineNum) != null && config.getString("default." + lineNum).length() > 0){
												player.sendMessage(ChatColor.GREEN + "Default line " + ChatColor.WHITE + "#" + lineNum + ChatColor.GREEN + ": " + config.getString("default." + lineNum));
 											}else{
 												player.sendMessage(ChatColor.RED + "Default line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + " is not valid.");
 											}
 										}else{
 											player.sendMessage(ChatColor.RED + "There is no default line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + ".");
 										}
 									}else{
 										player.sendMessage(ChatColor.RED + "There are no default " + ChatColor.WHITE + "or" + ChatColor.RED + " personal lines in the config. Use '" + ChatColor.WHITE + "/tourhelper upload <url>" + ChatColor.RED + "'.");
 									}
 								}else{
 									player.sendMessage(ChatColor.RED + "Line number " + ChatColor.WHITE + lineNum + ChatColor.RED + " is invalid. Use a number greater than 0.");
 								}
 							}catch (NumberFormatException nfe){
 								player.sendMessage(ChatColor.RED + "Line " + ChatColor.WHITE + args[0] + ChatColor.RED + " is invalid. Use a number greater than 0.");
 							}
 						}else{
 							player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 						}
 					}else if (args[0].equalsIgnoreCase("edit")){
 						if (player.hasPermission("tourhelper.upload")){
 							try{
 								int lineNum = Integer.parseInt(args[1]);
 								if (lineNum > 0){
 									if (config.contains(player.getName().toLowerCase())){
 										if (config.contains(player.getName().toLowerCase() + "." + lineNum)){
 											config.set(player.getName().toLowerCase() + "." + lineNum, null);
 											saveConfig();
 											player.sendMessage(ChatColor.GREEN + "Line " + ChatColor.WHITE + "#" + lineNum + ChatColor.GREEN + " has been removed.");
 										}else{
 											player.sendMessage(ChatColor.RED + "There is no line " + ChatColor.WHITE + "#" + lineNum + ChatColor.RED + ".");
 										}
 									}else{
 										player.sendMessage(ChatColor.RED + "There are no personal lines in the config. Use '" + ChatColor.WHITE + "/tourhelper upload <url>" + ChatColor.RED + "'.");
 									}
 								}else{
 									player.sendMessage(ChatColor.RED + "Line number " + ChatColor.WHITE + lineNum + ChatColor.RED + " is invalid. Use a number greater than 0.");
 								}
 							}catch (NumberFormatException nfe){
 								player.sendMessage(ChatColor.RED + "Line " + ChatColor.WHITE + args[0] + ChatColor.RED + " is invalid. Use a number greater than 0.");
 							}
 						}else{
 							player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 						}
 					}else{
						if (player.hasPermission("tourhelper.upload") || player.hasPermission("tourhelper.remove.other")) player.sendMessage(ChatColor.RED + "Invalid Command. Use '" + ChatColor.WHITE + "/" + cmdLabel + " upload <url>" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " remove [player]" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " edit <line> [new line]" + ChatColor.RED + "'.");
 						else player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 					}
 				}else if (args.length >= 3){
 					if (args[0].equalsIgnoreCase("edit")){
 						if (player.hasPermission("tourhelper.upload")){
 							try{
 								int lineNum = Integer.parseInt(args[1]);
 								if (lineNum > 0){
 									String args2 = "";
 									for (String arg : args) args2 = args2 + " " + arg;
 									args2 = args2.substring(7).trim();
 									if (config.contains(player.getName().toLowerCase())){
 										if (config.contains(player.getName().toLowerCase() + "." + lineNum)){
 											config.set(player.getName().toLowerCase() + "." + lineNum, args2);
 											player.sendMessage(ChatColor.GREEN + "Line " + ChatColor.WHITE + "#" + lineNum + ChatColor.GREEN + " has been overwritten.");
 										}else{
 											config.set(player.getName() + "." + lineNum, args2);
 											player.sendMessage(ChatColor.GREEN + "Line " + ChatColor.WHITE + "#" + lineNum + ChatColor.GREEN + " has been set.");
 										}
 									}else{
 										config.createSection(player.getName().toLowerCase());
 										config.set(player.getName().toLowerCase() + "." + lineNum, args2);
 										player.sendMessage(ChatColor.GREEN + "Line " + ChatColor.WHITE + "#" + lineNum + ChatColor.GREEN + " has been set.");
 									}
 									saveConfig();
 								}else{
 									player.sendMessage(ChatColor.RED + "Line number " + ChatColor.WHITE + lineNum + ChatColor.RED + " is invalid. Use a number greater than 0.");
 								}
 							}catch (NumberFormatException nfe){
 								player.sendMessage(ChatColor.RED + "Line " + ChatColor.WHITE + args[0] + ChatColor.RED + " is invalid. Use a number greater than 0.");
 							}
 						}else{
 							player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 						}
 					}else{
						if (player.hasPermission("tourhelper.upload") || player.hasPermission("tourhelper.remove.other")) player.sendMessage(ChatColor.RED + "Invalid Command. Use '" + ChatColor.WHITE + "/" + cmdLabel + " upload <url>" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " remove [player]" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " edit <line> [new line]" + ChatColor.RED + "'.");
 						else player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 					}
 				}else{
					if (player.hasPermission("tourhelper.upload") || player.hasPermission("tourhelper.remove.other")) player.sendMessage(ChatColor.RED + "Invalid Command. Use '" + ChatColor.WHITE + "/" + cmdLabel + " upload <url>" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " remove [player]" + ChatColor.RED + "' or '" + ChatColor.WHITE + "/" + cmdLabel + " edit <line> [new line]" + ChatColor.RED + "'.");
 					else player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + "permission" + ChatColor.RED + " to do this.");
 				}
 			}else{
 				sender.sendMessage("This command can only be used ingame.");
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	public void configUpdaterTask(String playerName, List<String> list){
 		
 		@SuppressWarnings("unused")
 		BukkitTask task = new ConfigUpdaterTask(this, playerName, list).runTaskLater(this, 0);
 	}
 	
 	public void notifyPlayerOfBadURLTask(String playerName){
 		
 		@SuppressWarnings("unused")
 		BukkitTask task = new NotifyPlayerOfBadURLTask(this, playerName).runTaskLater(this, 0);
 	}
 	
 	public void updateConfig(String playerName, List<String> list){
 		
 		if (config.contains(playerName.toLowerCase())) config.set(playerName.toLowerCase(), null);
 		if (lastLine.containsKey(playerName.toLowerCase())) lastLine.remove(playerName.toLowerCase());
 		ConfigurationSection cs = config.createSection(playerName.toLowerCase());
 		for (int i = 0 ; i < list.size() ; i++) cs.set("" + (i + 1), list.get(i));
 		saveConfig();
 		if (getServer().getPlayerExact(playerName) != null) getServer().getPlayerExact(playerName).sendMessage(ChatColor.GREEN + "Your personal lines have been " + ChatColor.WHITE + "uploaded" + ChatColor.GREEN + " successfully.");
 	}
 	
 	public void notifyPlayerOfBadURL(String playerName){
 		
 		if (getServer().getPlayerExact(playerName) != null) getServer().getPlayerExact(playerName).sendMessage(ChatColor.RED + "The URL you entered did not respond.");
 	}
 }
