 package com.github.mpotacon.chatgear;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class muteCommand implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if(commandLabel.equalsIgnoreCase("mute")){
 			if(sender.hasPermission("chatgear.mute")){
 				if(args.length == 1){
 				Player player = Bukkit.getServer().getPlayer(args[0]);
 					if(player.isOnline()){
 						if(player.isOp()){
							sender.sendMessage("You can not mute " + player);
 							return false;
 						} else if(chatListener.mutedPlayers.containsKey(player.getName())){
 							chatListener.mutedPlayers.remove(player.getName());
 						} else {
 						chatListener.mutedPlayers.put(player.getName(), null);
 						return true;
 						}
 					}
					sender.sendMessage("Player not online.");
 					return false;
 				}
				sender.sendMessage("/mute <playername>");
 				return false;
 			}
 			sender.sendMessage(ChatColor.RED + "You do not have permission to mute a player");
 			return false;
 		}
 		return false;
 	}
 
 }
