 package me.cmastudios.plugins.WarhubModChat.commands;
 
 import me.cmastudios.plugins.WarhubModChat.WarhubModChat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class DeafCommand implements CommandExecutor {
 	public static WarhubModChat plugin;
     public DeafCommand(WarhubModChat instance) {
         plugin = instance;
     }
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String arg2,
 			String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if (args.length < 1) {
 			player.sendMessage(ChatColor.YELLOW + "Deafened players:");
 			String plrs = "";
 			for (String plr : plugin.ignores) {
 				plrs += plr + ", ";
 			}
 			player.sendMessage(ChatColor.YELLOW + plrs);
 			player.sendMessage(ChatColor.YELLOW
 					+ "Use /deaf <player> to deafen someone.");
 			return true;
 		} else if (args.length == 1) {
 			// One argument, mute the player
 			String todeafenstring;
 			Player todeafen = Bukkit.getServer().getPlayer(args[0]);
 			if (todeafen != null) {
 				todeafenstring = todeafen.getName();
 			} else {
 				sender.sendMessage(ChatColor.RED + args[0] + " is not online.");
 			    return true;	
 			}
 			if (todeafen == player) {
 				if (plugin.ignores.contains(todeafenstring)) {
					plugin.ignores.remove(todeafen);
 					todeafen.sendMessage(ChatColor.YELLOW
 							+ "You have been undeafened.");
 				} else {
 					plugin.ignores.add(todeafenstring);
 					todeafen.sendMessage(ChatColor.YELLOW
 							+ "You have been deafened.");
 				}
 			} else if (player.hasPermission("warhub.moderator")) {
 				if (plugin.ignores.contains(todeafenstring)) {
					plugin.ignores.remove(todeafen);
 					player.sendMessage(ChatColor.YELLOW
 							+ todeafen.getName() + " has been undeafened.");
 					todeafen.sendMessage(ChatColor.YELLOW
 							+ "You have been undeafened.");
 				} else {
 					plugin.ignores.add(todeafenstring);
 					player.sendMessage(ChatColor.YELLOW
 							+ todeafen.getName() + " has been deafened.");
 					todeafen.sendMessage(ChatColor.YELLOW
 							+ "You have been deafened.");
 				}
 
 			} else {
 				player.sendMessage(ChatColor.RED
 						+ "You do not have permissions to deafen others.");
 			}
 			return true;
 		}
 		return false;
 	
 	}
 
 }
