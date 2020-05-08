 package net.loadingchunks.plugins.PushEnder;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PushEnderCommandExecutor implements CommandExecutor {
 	
 	private PushEnder plugin;
 	private Pushover messageSender;
 	private HashMap<String, Long> delayQueue = new HashMap<String, Long>();
 	
 	public PushEnderCommandExecutor(PushEnder plugin, Pushover messageSender) {
 		this.plugin = plugin;
 		this.messageSender = messageSender;
 	}
 
 	@Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(command.getName().equalsIgnoreCase("pushender")) {
 			if(args.length == 1 && args[0].equalsIgnoreCase("reload") && (sender.isOp() || sender.hasPermission("pushender.reload"))) {
 				plugin.reloadConfig();
 				messageSender.ReloadTokens(plugin);
 				sender.sendMessage(ChatColor.GREEN + "Reloaded Config!");
 				return true;
 			}
 		}
 		
 		if(command.getName().equalsIgnoreCase("callstaff")) {
 			if(sender.hasPermission("pushender.callstaff") && sender instanceof Player) {
 				if(delayQueue.containsKey(sender.getName()) && delayQueue.get(sender.getName()) > System.currentTimeMillis()) {
 					sender.sendMessage(ChatColor.RED + "You may only use this once every " + plugin.getConfig().getInt("pushover.cooldowns.callstaff") + " seconds.");
 					return true;
 				}
 				
 				if(args.length > 0) {
					messageSender.SendMessages(ChatColor.stripColor(((Player)sender).getDisplayName()) + " needs help!", ChatColor.stripColor(StringUtils.join(args)), PushType.CALL_STAFF);
 					
 					for(Player p : plugin.getServer().getOnlinePlayers()) {
 						if(p.hasPermission("pushender.notify")) {
							p.sendMessage(ChatColor.YELLOW + "CALLSTAFF ALERT: " + ChatColor.stripColor(((Player)sender).getDisplayName()) + ": " + StringUtils.join(args));
 						}
 					}
 					sender.sendMessage(ChatColor.GREEN + "Thank you, if a member of staff is able to come online they will be along to assist you shortly.");
 					
 					delayQueue.put(sender.getName(), System.currentTimeMillis() + (1000L * plugin.getConfig().getInt("pushover.cooldowns.callstaff")));
 					
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED + "Please enter a message to send to staff.");
 					return false;
 				}
 			} else {
 				if(!(sender instanceof Player))
 					sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
 				else if(!sender.hasPermission("pushender.callstaff"))
 					sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
 			}
 		}
 		return false;
 	}
 
 }
