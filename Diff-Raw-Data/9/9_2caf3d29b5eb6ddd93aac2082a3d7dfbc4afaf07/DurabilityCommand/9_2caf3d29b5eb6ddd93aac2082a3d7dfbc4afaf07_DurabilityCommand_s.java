 package com.github.thebiologist13;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class DurabilityCommand implements CommandExecutor {
 
 	//Plugin variable
 	private NeverBreak plugin;
 	
 	//Assigning plugin variable
 	public DurabilityCommand(NeverBreak plugin) {
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		//Player variable
 		Player p = null; 
 		
 		//If a player, not the console
 		if(arg0 instanceof Player) {
 			p = (Player) arg0;
 		}
 		
 		//If the command sender is a player, the player has the permission, and the command is "setdurability"
 		if(p != null && p.hasPermission("neverbreak.setdurability") && arg1.getName().equalsIgnoreCase("setdurability")) {
 			//List of the items allowed to set durability for from configuration
 			List<?> items = plugin.getCustomConfig().getList("items");
 			//Boolean value for if they are holding a valid item. i.e. one specified in configuration
 			boolean holdingValidItem = false;
 			
 			//Finds if they are holding a valid item
 			for(Object o : items) {
 				if(o instanceof Integer) {
 					int handItemId = p.getItemInHand().getTypeId();
 					if(handItemId == (Integer) o) {
 						holdingValidItem = true;
 						break;
 					}
 				} else {
 					continue;
 				}
 			}
 			
 			if(holdingValidItem) {
 				//Makes sure the value can be parsed to a short
 				if(Integer.parseInt(arg3[0]) <= -32767 || Integer.parseInt(arg3[0]) > 32767) {
 					p.sendMessage(ChatColor.RED + "A tool cannot have a durablity less than or equal to -32,767 or greater than 32,767.");
 				//Setting the new durability.
 				} else {
 					p.sendMessage(ChatColor.GREEN + "The durability of your tool has been set to " + ChatColor.GOLD + arg3[0] + ChatColor.GREEN + "!");
 					p.getItemInHand().setDurability(Short.valueOf(arg3[0]));
 				}
 			} else {
 				p.sendMessage(ChatColor.RED + "You are not holding an item that this server is configured to allow durability setting for.");
 			}
 			return true;
 		}
 		
 		if(p == null && arg1.getName().equalsIgnoreCase("setdurability")) {
			plugin.log.info(ChatColor.RED + "The " + arg1.getName() + " cannot be used from the console.");
 			return true;
 		}
 		
 		return false;
 	}
 
 }
