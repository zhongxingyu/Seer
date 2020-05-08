 package me.davejavu.pce.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import me.davejavu.pce.PalCommand;
 import me.davejavu.pce.PalCraftEssentials;
 
 public class tp extends PalCommand {
 	Plugin plugin;
 	public tp(){}
 	public tp(PalCraftEssentials plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String cmdLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("tp")) {
 			if (sender instanceof Player) {
 				if (permissionCheck(sender, "PalCraftEssentials.command.tp")) {
 					if (args.length == 1) {
 						Player p2 = getPlayer(args[0]);
 						if (p2 == null) {
 							sendMessage(sender, ChatColor.RED + "'" + args[0] + "' is not online!");
 							return true;
 						}
						if (!getConfig(p2).getFC().getBoolean("tp")) {
 							sendMessage(sender, ChatColor.RED + "'" + p2.getDisplayName() + "' has teleportation disabled.");
 							return true;
 						}
 						if (vanish.isVanished(p2)) {
 							sendMessage(sender, ChatColor.RED + "'" + args[0] + "' is not online!");
 							return true;
 						}
 						((Player) sender).teleport(p2);
 						
 						sendMessage(sender, ChatColor.GOLD + "Teleported to " + ChatColor.WHITE + p2.getDisplayName());
 						return true;
 					} else if (args.length == 2) {
 						Player p2 = getPlayer(args[0]);
 						if (p2 == null) {
 							sendMessage(sender, ChatColor.RED + "'" + args[0] + "' is not online!");
 							return true;
 						}
 						Player p3 = getPlayer(args[0]);
 						if (p3 == null) {
 							sendMessage(sender, ChatColor.RED + "'" + args[0] + "' is not online!");
 							return true;
 						}
						if (!getConfig(p2).getFC().getBoolean("tp")) {
 							sendMessage(sender, ChatColor.RED + "'" + p2.getDisplayName() + "' has teleportation disabled.");
 							return true;
 						}
						if (!getConfig(p3).getFC().getBoolean("tp")) {
 							sendMessage(sender, ChatColor.RED + "'" + p3.getDisplayName() + "' has teleportation disabled.");
 							return true;
 						}
 						p2.teleport(p3);
 						p2.sendMessage(ChatColor.GOLD + "Teleported to " + ChatColor.WHITE + p3.getDisplayName());
 						sendMessage(sender, ChatColor.GOLD + "Teleported " + ChatColor.WHITE + p2.getDisplayName() + " to " + p3.getDisplayName());
 					} else {
 						sendMessage(sender, ChatColor.RED + "Usage: /tp <player> [player2]");
 						return true;
 					}
 				} else {
 					noPermission(cmd.getName(), sender);
 					return true;
 				}
 			} else {
 				sendMessage(sender, ChatColor.RED + "Only players in game can use this command!");
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
