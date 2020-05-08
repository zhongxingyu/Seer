 package com.github.Mephilis7.PlayerManager;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import com.github.Mephilis7.PlayerManager.VAR;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*TODO:
  * - add more information for /pman
  * - add a way to log more information about players
  * - add a /fullban to ban all IP's ever logged by a player
  * - add a hooking into Vault
  */
 public class PMan_main extends JavaPlugin {
 	
 	private PMan_IPLogger ip = new PMan_IPLogger();
 	ChatColor green = ChatColor.GREEN;
 	ChatColor darkgreen = ChatColor.DARK_GREEN;
 	ChatColor gold = ChatColor.GOLD;
 	ChatColor aqua = ChatColor.AQUA;
 	ChatColor white = ChatColor.WHITE;
 	
 	public void onDisable() {
 		VAR.log.info(VAR.logHeader + "Shutdown.");
 	}
 	public void onEnable() {
 		checkConfig();
 		try {
 			VAR.config.load(VAR.f_config);
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (InvalidConfigurationException e1) {
 			e1.printStackTrace();
 		}
 		
 		if (!VAR.config.getBoolean("enable")){
 			Bukkit.getPluginManager().disablePlugin(this);
 		}
 		//checking whether the config "punishment" is kick, ban or none. If not, regenerate the config.yml
 		if (!VAR.config.getString("punishment").equalsIgnoreCase("kick") && !VAR.config.getString("punishment").equalsIgnoreCase("ban") && !VAR.config.getString("punishment").equalsIgnoreCase("none")){
 			newConfig();
 		}
 		getServer().getPluginManager().registerEvents(this.ip, this);
 		VAR.log.info(VAR.logHeader + "Ready to manage your players!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		Player playerShowInfo = null;
 		
 		if ((cmd.getName().equalsIgnoreCase("pman"))){
 			if (sender.hasPermission("pman.manage") || (sender.isOp())){
 			
 				if (args.length == 0){
 					sender.sendMessage(gold + "------------------" + green + " PlayerManager " + gold + "-----------------");
 					sender.sendMessage(gold + "/pman"+white+" - "+darkgreen+"Shows this message.");
 					sender.sendMessage(gold + "/pman info <player|ip>"+white+" - "+darkgreen+"Show information about a player.");
 					sender.sendMessage(gold + "/pman list"+white+" - "+darkgreen+"Show all players and their gamemode.");
 					sender.sendMessage(gold + "/pman reload"+white+" - "+darkgreen+"Reload the config.yml");
 					return true;
 					
 				}
 				if (args.length == 1){
 					//reloading the config.yml
 					if (args[0].equalsIgnoreCase("reload")){
 						checkConfig();
 						try {
 							VAR.config.load(VAR.f_config);
 						} catch (FileNotFoundException e1) {
 							e1.printStackTrace();
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						} catch (InvalidConfigurationException e1) {
 							e1.printStackTrace();
 						}
 					
 						sender.sendMessage(VAR.Header +ChatColor.GREEN+ "config.yml reloaded!");
 						return true;
 					}
 					//listing all online players with their gamemode
 					if (args[0].equalsIgnoreCase("list")){
 						sender.sendMessage(gold + "------------------" + green + " PlayerManager " + gold + "-----------------");
 						for (Player on: getServer().getOnlinePlayers()){
 							sender.sendMessage(ChatColor.GRAY+on.getName()+white+" - "+ChatColor.DARK_AQUA+ChatColor.BOLD+on.getGameMode());
 						}
 						return true;
 					}
 					//show information about a player; check whether the player is online.
 					if (args[0].equalsIgnoreCase("info")){
 						for (Player infoPlayer: getServer().getOnlinePlayers()){
 							if (infoPlayer.getName().equalsIgnoreCase(args[0])){
 								playerShowInfo = infoPlayer;
 							}
 						}
 						if (playerShowInfo == null){
 							for (Player infoPlayer: getServer().getOnlinePlayers()){
 								String[] infoIp = infoPlayer.getAddress().toString().split(":");
 								if (infoIp[0].equalsIgnoreCase(args[0]) || infoIp[0].equalsIgnoreCase("/" + args[0])){
 									playerShowInfo = infoPlayer;
 								}
 							}
 							if (playerShowInfo == null){
 								sender.sendMessage(VAR.Header + "Could not find specified player, is he offline?");
 								return true;
 							}
 						}
 						if (playerShowInfo != null){
 							sender.sendMessage(gold + "------------------" + green + " PlayerManager " + gold + "-----------------");
 							String[] Order = VAR.config.getString("order").split(";");
 							int i=0;
 						
 							//showing information about the specified player as specified in the config.yml
 							while (i < Order.length){
 								if (Order[i].equalsIgnoreCase("Name"))
 									sender.sendMessage(darkgreen + "Name: " + aqua + playerShowInfo.getName());
 								if (Order[i].equalsIgnoreCase("IP"))
 									sender.sendMessage(darkgreen + "IP Address: " + aqua + playerShowInfo.getAddress());
 								if (Order[i].equalsIgnoreCase("World"))
 									sender.sendMessage(darkgreen + "World: " + aqua + playerShowInfo.getWorld().getName());
 								if (Order[i].equalsIgnoreCase("Health"))
 									sender.sendMessage(darkgreen + "Health: " + aqua + playerShowInfo.getHealth());
 								if (Order[i].equalsIgnoreCase("Food"))
 									sender.sendMessage(darkgreen + "Food: " + aqua + playerShowInfo.getFoodLevel());
 								if (Order[i].equalsIgnoreCase("Xp"))
 									sender.sendMessage(darkgreen + "Exp level: " + aqua + playerShowInfo.getLevel());
 								if (Order[i].equalsIgnoreCase("GameMode"))
 									sender.sendMessage(darkgreen + "GameMode: " + aqua + playerShowInfo.getGameMode());
 								if (Order[i].equalsIgnoreCase("Position"))
 									sender.sendMessage(darkgreen + "Position:  " + aqua +"X: "+playerShowInfo.getLocation().getBlockX() + "  Z: " +playerShowInfo.getLocation().getBlockZ()+ "  Y: " +playerShowInfo.getLocation().getBlockY());
 								if (Order[i].equalsIgnoreCase("Distance")){
 									if (sender instanceof Player){
 										int x = abs(playerShowInfo.getLocation().getBlockX());
 										int loc = abs(((Player) sender).getPlayer().getLocation().getBlockX());
 										x = abs(x-loc);
 										int y = abs(playerShowInfo.getLocation().getBlockY());
 										loc = abs(((Player) sender).getPlayer().getLocation().getBlockY());
 										y = abs(y-loc);
 										int z = abs(playerShowInfo.getLocation().getBlockZ());
 										loc = abs(((Player) sender).getPlayer().getLocation().getBlockZ());
 										z = abs(z-loc);
 										x = x+y+z;
 										sender.sendMessage(darkgreen + "Distance: " + aqua + x);
 									}
 								}
 								i++;
 							}
 							sender.sendMessage(gold + "--------------------------------------------------");
 							return true;
 						}
 					}
				}else{sender.sendMessage(VAR.Header + ChatColor.RED +"Too many arguments! Type /pman for help.");}
 			} else {
 				denied(sender);
 				return true;
 			}
 		}return true;
 		
 	}
 	public void checkConfig(){
 		new File(VAR.directory).mkdir();
 		VAR.config = new YamlConfiguration();
 		if (!VAR.f_config.exists()){
 			newConfig();
 		} else {
 			try {
 				VAR.config.load(VAR.f_config);
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			} catch (InvalidConfigurationException e1) {
 				e1.printStackTrace();
 			}
 			if (!VAR.config.isSet("enable")){
 				newConfig();
 			} else if (!VAR.config.isSet("customJQ")){
 				newConfig();
 			} else if (!VAR.config.isSet("joinMsg")){
 				newConfig();
 			} else if (!VAR.config.isSet("joinMsgOther")){
 				newConfig();
 			} else if (!VAR.config.isSet("quitMsg")){
 				newConfig();
 			} else if (!VAR.config.isSet("order")){
 				newConfig();
 			} else if (!VAR.config.isSet("LogIP")){
 				newConfig();
 			} else if (!VAR.config.isSet("supportReiMinimap")){
 				newConfig();
 			} else if (!VAR.config.isSet("enableBotBlock")){
 				newConfig();
 			} else if (!VAR.config.isSet("logDuplicatedIps")){
 				newConfig();
 			} else if (!VAR.config.isSet("punishment")){
 				newConfig();
 			}
 		}
 	}
 	private void denied(CommandSender sender){
 		sender.sendMessage(VAR.Header + ChatColor.RED + "You don't have permission to use that command.");
 	}
 	public void newConfig(){
 		//generate a fresh config.yml
 		try {
 			VAR.f_config.createNewFile();
 			FileWriter fstream = new FileWriter(VAR.f_config);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("# Here are all variables you may need for the messages:\n");
 			out.write("# &0 - Black          &6 - Gold          &b - Aqua\n");
 			out.write("# &1 - Dark Blue      &7 - Gray          &c - Red\n");
 			out.write("# &2 - Dark Green     &8 - Dark Gray     &d - Light Purple\n");
 			out.write("# &3 - Dark Aqua      &9 - Blue          &e - Yellow\n");
 			out.write("# &4 - Dark Red       &a - Green         &f - White\n");
 			out.write("# &5 - Dark Purple\n");
 			out.write("# %NAME  - %IP  - %WORLD  - %GAMEMODE\n\n\n");
 			
 			
 			out.write("# Enable the plugin?\n");
 			out.write("enable: true\n\n");
 			
 			out.write("# Do you want custom join/quit messages?\n");
 			out.write("customJQ: true\n");
 			out.write("# Set your join message here.\n");
 			out.write("joinMsg: '&aHello %NAME!'\n");
 			out.write("# This is the message other players will see.\n");
 			out.write("joinMsgOther: '&bPlayer &e%NAME &b(&c%IP&b) has connected.'\n");
 			out.write("# The quit message when somebody leaves your server.\n");
 			out.write("quitMsg: '&e%NAME has left the game.'\n\n");
 			
 			out.write("# Define the order of the information shown on /pinfo here. Separate the words with ;\n");
 			out.write("# Name: The player's name\n");
 			out.write("# IP: The player's IP address\n");
 			out.write("# World: The world the player is in\n");
 			out.write("# Health: The player's health\n");
 			out.write("# Food: The player's food level\n");
 			out.write("# Xp: The player's Xp level\n");
 			out.write("# GameMode: The player's gamemode\n");
 			out.write("# Position: The player's position\n");
 			out.write("# Distance: The distance from the command executor to the player\n");
 			out.write("# Example: Name;IP;World;Xp\n");
 			out.write("order: Name;IP;World;Xp\n\n");
 			
 			out.write("# Should every player's IP address be logged?\n");
 			out.write("LogIP: true\n");
 			out.write("# Should BotBlocking be enabled?\n");
 			out.write("enableBotBlock: true\n");
 			out.write("# Should two players with the same IP be logged separately?\n");
 			out.write("logDuplicatedIps: true\n");
 			out.write("# What should I do if I find two players with\n");
 			out.write("# the same IP? (Normally one of them is a bot then)\n");
 			out.write("# Accepted are kick/ban/none.\n");
 			out.write("punishment: kick\n\n");
 			
 			out.write("# Should Rei's Minimap be supported?\n");
 			out.write("supportReiMinimap: false\n\n");
 			
 			out.close();
 			VAR.log.warning(VAR.logHeader + "Default config.yml generated.");
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	public static int abs(int a){
 		if (a < 0)
 			a = a*(-1);
 		return a;
 	}
 }
 
