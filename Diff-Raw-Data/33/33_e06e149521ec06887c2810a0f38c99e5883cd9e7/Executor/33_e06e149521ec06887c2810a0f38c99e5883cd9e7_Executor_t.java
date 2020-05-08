 package me.rosswalker.serverstats;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Executor implements CommandExecutor {
 
 	private ServerStats plugin;
 	
 	
 	public Executor(ServerStats plugin) {
 		this.plugin = plugin;
 	}
 
 	/*
 	 * @name onCommand()
 	 * @description Handles commands for ServerStats.
 	 * @param1 CommandSender
 	 * @param2 Command
 	 * @param3 String
 	 * @param4 String[]
 	 * 
 	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
 	 */
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
	   Player player = null;
 		if((sender instanceof Player)){
		    player = (Player) sender;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("ss")){
 			if(args.length == 0) {
				if((sender.getName() == "CONSOLE") || player.hasPermission("ServerStats.check")){
				    sender.sendMessage(sender.getName());
 					int totalPlayers = (plugin.getServer().getOfflinePlayers().length 
 							+ plugin.getServer().getOnlinePlayers().length);
 					int onlinePlayers = plugin.getServer().getOnlinePlayers().length;
 					int maxSlots = plugin.getServer().getMaxPlayers();
 					String serverName = plugin.getServer().getName();
 					String serverIP = plugin.getServer().getIp();
 					int serverPort = plugin.getServer().getPort();
 					int serverOPs = plugin.getServer().getOperators().size();
 					String bukkitVersion = plugin.getServer().getBukkitVersion();
 				
 					sender.sendMessage(ChatColor.BLUE + "----------ServerStats----------");
 					sender.sendMessage(ChatColor.GRAY + "  There are " + ChatColor.DARK_GRAY + onlinePlayers + ChatColor.GRAY + " out of " + ChatColor.DARK_GRAY + maxSlots + ChatColor.GRAY + " players online.");
 					sender.sendMessage(ChatColor.DARK_GRAY + "  " + serverName + ChatColor.GRAY + " is running on IP " + ChatColor.DARK_GRAY + serverIP + ChatColor.GRAY + " with port " + ChatColor.DARK_GRAY + serverPort + ChatColor.GRAY + ".");
 					sender.sendMessage(ChatColor.DARK_GRAY + "  " + serverName + ChatColor.GRAY + " has Bukkit version " + ChatColor.DARK_GRAY + bukkitVersion + ChatColor.GRAY + ".");
 					sender.sendMessage(ChatColor.GRAY + "  There are currently " + ChatColor.DARK_GRAY + serverOPs + ChatColor.GRAY + " players with 'OP' status.");
 					sender.sendMessage(ChatColor.GRAY + "  A total of " + ChatColor.DARK_GRAY + totalPlayers + ChatColor.GRAY + " players have joined " + ChatColor.DARK_GRAY + serverName);
 					sender.sendMessage(ChatColor.GRAY + "  Maximum Memory: " + ChatColor.DARK_GRAY + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + ChatColor.GRAY + " MB");
 					sender.sendMessage(ChatColor.GRAY + "  Total Memory: " + ChatColor.DARK_GRAY + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + ChatColor.GRAY + " MB");
 					sender.sendMessage(ChatColor.GRAY + "  Free Memory: " + ChatColor.DARK_GRAY + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + ChatColor.GRAY + " MB");
 					sender.sendMessage(ChatColor.BLUE + "--------------------------------");
 					return true;
 				} else {
 					//PERMFAIL
 				}
 				
 			} else if(args.length == 1){
				if(!(sender.getName() == "CONSOLE") && player.hasPermission("ServerStats.player") ){
 					boolean op = player.isOp();
 					String name = player.getName();
 					
 					sender.sendMessage(ChatColor.BLUE + "----------ServerStats----------");
 					sender.sendMessage(ChatColor.GRAY + "  Hello, " + ChatColor.DARK_BLUE + name + ChatColor.GRAY + "!");
 					if(op){
 						sender.sendMessage(ChatColor.GRAY + "  You have OP rights!");
 					} else {
 						sender.sendMessage(ChatColor.GRAY + "  You have do not have OP rights!");
 					}
 					sender.sendMessage(ChatColor.GRAY + "  You are playing in gamemode '" + ChatColor.DARK_GRAY + player.getGameMode() + ChatColor.GRAY + ".'");
 					sender.sendMessage(ChatColor.GRAY + "  You are playing on IP " + ChatColor.DARK_GRAY + player.getAddress() + ChatColor.GRAY + ".");
 					sender.sendMessage(ChatColor.GRAY + "  You have " + ChatColor.DARK_GRAY + player.getHealth() + ChatColor.GRAY + " out of " + ChatColor.DARK_GRAY + player.getMaxHealth() + ChatColor.GRAY + " health.");
 					sender.sendMessage(ChatColor.BLUE + "--------------------------------");
 					
 					return true;
 				} else {
 					//PERMFAIL
 				}
 			} else if(args.length == 2){
			    if(!(sender.getName() == "CONSOLE") && player.hasPermission("ServerStats.player.other") ){
 					Player otherplayer = player.getServer().getPlayer(args[1]);
					
 					if(otherplayer == null) {
					    
					    OfflinePlayer otherPlayer = null;
					    try {
					        otherPlayer = player.getServer().getPlayer(args[1]);
					    } catch (NullPointerException ex){
					        //DOESNOTEXIST
					        return true;
					    }
					    
 						sender.sendMessage(ChatColor.BLUE + "----------ServerStats----------");
 						sender.sendMessage(ChatColor.GRAY + "  " + ChatColor.DARK_BLUE + otherPlayer.getName() + "'s stats:");
 						sender.sendMessage(ChatColor.GRAY + "  OP: " + ChatColor.DARK_GRAY + otherPlayer.isOp());
 						sender.sendMessage(ChatColor.GRAY + "  Online: " + ChatColor.DARK_GRAY + "False");
 						sender.sendMessage(ChatColor.BLUE + "--------------------------------");
 						
 						return true;
 					} else {
 						sender.sendMessage(ChatColor.BLUE + "----------ServerStats----------");
 						sender.sendMessage(ChatColor.GRAY + "  " + ChatColor.DARK_BLUE + otherplayer.getName() + "'s stats:");
 						sender.sendMessage(ChatColor.GRAY + "  OP: " + ChatColor.DARK_GRAY + otherplayer.isOp());
 						sender.sendMessage(ChatColor.GRAY + "  Online: " + ChatColor.DARK_GRAY + otherplayer.isOnline());
 						sender.sendMessage(ChatColor.GRAY + "  Gamemode: " + ChatColor.DARK_GRAY + otherplayer.getGameMode() + ChatColor.GRAY);
 						sender.sendMessage(ChatColor.GRAY + "  IP: " + ChatColor.DARK_GRAY + otherplayer.getAddress());
 						sender.sendMessage(ChatColor.GRAY + "  Health: " + ChatColor.DARK_GRAY + otherplayer.getHealth());
 						sender.sendMessage(ChatColor.GRAY + "  Location: " + ChatColor.DARK_GRAY + "(" + otherplayer.getWorld().getName() + ", " + Math.round(otherplayer.getLocation().getX()) + ", " + Math.round(otherplayer.getLocation().getY()) + ", " + Math.round(otherplayer.getLocation().getZ()) + ")");
 						sender.sendMessage(ChatColor.BLUE + "--------------------------------");
 						return true;
 					}
 					
 				} else {
 					//PERMFAIL
				    return true;
 				}
 			} else {
 				//ARGFAIL
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	
 }
