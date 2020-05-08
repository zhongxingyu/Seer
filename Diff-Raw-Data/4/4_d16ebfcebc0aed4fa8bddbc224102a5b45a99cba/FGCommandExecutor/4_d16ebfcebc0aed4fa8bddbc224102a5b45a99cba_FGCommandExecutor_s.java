 package me.mango.firegods;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class FGCommandExecutor implements CommandExecutor {
 	
 	private FireGods plugin;
 
 	public FGCommandExecutor(FireGods plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		String commandName = cmd.getName().toLowerCase();
 		if (commandName.equalsIgnoreCase("fire")) {
 			if (((plugin.permission.has(sender, "firegods.use")) || (sender.isOp())) && (sender instanceof Player)) {
 				if (!plugin.firePlayers.contains((Player) sender)) {
 					plugin.firePlayers.add((Player) sender);
 					sender.sendMessage(ChatColor.GREEN + "You have enabled fire god mode!");
 					return true;
 				} else {
 					if (plugin.firePlayers.contains((Player) sender)) {
 						plugin.firePlayers.remove((Player) sender);
 						sender.sendMessage(ChatColor.DARK_RED + "You have disabled fire god mode.");
 						return true;
 					} else {
 						sender.sendMessage("Unknown error!");
 					}
 				}
 			} else {
				sender.sendMessage(ChatColor.RED + "You can't use that command!");
 				return true;
 			}
 		}
 		return false;
 	}
 }
