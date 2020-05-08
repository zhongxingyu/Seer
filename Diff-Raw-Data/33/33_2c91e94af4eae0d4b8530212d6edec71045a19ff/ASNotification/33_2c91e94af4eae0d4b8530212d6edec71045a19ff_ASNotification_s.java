 package com.turt2live.antishare;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 public class ASNotification {
 
	public static void sendNotification(NotificationType type, AntiShare plugin, Player player, String variable){
 		if(!plugin.getConfig().getBoolean("notifications.send")){
 			return;
 		}
 		if(player.hasPermission("AntiShare.silent")){
 			return;
 		}
 		String message = "";
 		switch (type){
 		// ILLEGAL actions
 		case ILLEGAL_BLOCK_PLACE:
 			if(variable.equalsIgnoreCase("BEDROCK")){
 				break;
 			}
			if(plugin.storage.isBlocked(Material.getMaterial(variable), BlockedType.INTERACT, player.getWorld())){
 				break;
 			}
 			if(plugin.getConfig().getBoolean("notifications.illegal.block_place")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to place " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_BLOCK_BREAK:
 			if(variable.equalsIgnoreCase("BEDROCK")){
 				break;
 			}
			if(plugin.storage.isBlocked(Material.getMaterial(variable), BlockedType.INTERACT, player.getWorld())){
 				break;
 			}
 			if(plugin.getConfig().getBoolean("notifications.illegal.block_break")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to break " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_CREATIVE_BLOCK_BREAK:
 			if(variable.equalsIgnoreCase("BEDROCK")){
 				break;
 			}
			if(plugin.storage.isBlocked(Material.getMaterial(variable), BlockedType.INTERACT, player.getWorld())){
 				break;
 			}
 			if(plugin.getConfig().getBoolean("notifications.illegal.creative_block_break")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to break the creative block " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_WORLD_CHANGE:
 			if(plugin.getConfig().getBoolean("notifications.illegal.world_transfer")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to go to world " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_COMMAND:
 			if(plugin.getConfig().getBoolean("notifications.illegal.command")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to send the command " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_DEATH:
 			if(plugin.getConfig().getBoolean("notifications.illegal.death")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to die in " + ChatColor.DARK_RED + variable + ChatColor.AQUA + " mode!";
 			}
 			break;
 		case ILLEGAL_DROP_ITEM:
 			if(plugin.getConfig().getBoolean("notifications.illegal.drop_item")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to drop the item " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_INTERACTION:
 			if(plugin.getConfig().getBoolean("notifications.illegal.interact")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to interact with " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_PLAYER_PVP:
 			if(plugin.getConfig().getBoolean("notifications.illegal.pvp")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to hit " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_MOB_PVP:
 			if(plugin.getConfig().getBoolean("notifications.illegal.mob-pvp")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to hit a " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_EGG:
 			if(plugin.getConfig().getBoolean("notifications.illegal.egg")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to use a " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case ILLEGAL_BEDROCK:
 			if(plugin.getConfig().getBoolean("notifications.illegal.bedrock_attempt")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " tried to use " + ChatColor.DARK_RED + variable + ChatColor.AQUA + "!";
 			}
 			break;
 
 		// LEGAL actions
 		case LEGAL_BLOCK_PLACE:
 			if(variable.equalsIgnoreCase("BEDROCK")){
 				break;
 			}
			if(plugin.storage.isBlocked(Material.getMaterial(variable), BlockedType.INTERACT, player.getWorld())){
 				break;
 			}
 			if(plugin.getConfig().getBoolean("notifications.legal.block_place")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " placed " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_BLOCK_BREAK:
 			if(variable.equalsIgnoreCase("BEDROCK")){
 				break;
 			}
			if(plugin.storage.isBlocked(Material.getMaterial(variable), BlockedType.INTERACT, player.getWorld())){
 				break;
 			}
 			if(plugin.getConfig().getBoolean("notifications.legal.block_break")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " broke " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_CREATIVE_BLOCK_BREAK:
 			if(variable.equalsIgnoreCase("BEDROCK")){
 				break;
 			}
			if(plugin.storage.isBlocked(Material.getMaterial(variable), BlockedType.INTERACT, player.getWorld())){
 				break;
 			}
 			if(plugin.getConfig().getBoolean("notifications.legal.creative_block_break")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " broke the creative block " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_WORLD_CHANGE:
 			if(plugin.getConfig().getBoolean("notifications.legal.world_transfer")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " went to world " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_COMMAND:
 			if(plugin.getConfig().getBoolean("notifications.legal.command")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " sent the command " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_DEATH:
 			if(plugin.getConfig().getBoolean("notifications.legal.death")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " died in " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + " mode!";
 			}
 			break;
 		case LEGAL_DROP_ITEM:
 			if(plugin.getConfig().getBoolean("notifications.legal.drop_item")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " dropped the item " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_INTERACTION:
 			if(plugin.getConfig().getBoolean("notifications.legal.interact")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " interacted with " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_PLAYER_PVP:
 			if(plugin.getConfig().getBoolean("notifications.legal.pvp")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " hit " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_MOB_PVP:
 			if(plugin.getConfig().getBoolean("notifications.legal.mob-pvp")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " hit a " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_EGG:
 			if(plugin.getConfig().getBoolean("notifications.legal.egg")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " used a " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		case LEGAL_BEDROCK:
 			if(plugin.getConfig().getBoolean("notifications.legal.bedrock_attempt")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " used " + ChatColor.DARK_GREEN + variable + ChatColor.AQUA + "!";
 			}
 			break;
 
 		// GENERAL actions
 		case GAMEMODE_CHANGE:
 			if(plugin.getConfig().getBoolean("notifications.general.gamemode_change")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.AQUA + player.getName() + " changed to gamemode " + ChatColor.BLUE + variable + ChatColor.AQUA + "!";
 			}
 			break;
 		}
 		if(message.length() > 0){
 			for(Player p : Bukkit.getServer().getOnlinePlayers()){
 				if(p.hasPermission("AntiShare.notify")){
					ASUtils.sendToPlayer(player, message);
 				}
 			}
 			ASUtils.sendToPlayer(Bukkit.getConsoleSender(), message);
 		}
 	}
 
 	public static void sendNotification(NotificationType type, Player player, String variable){
		sendNotification(type, (AntiShare) Bukkit.getServer().getPluginManager().getPlugin("AntiShare"), player, variable);
 	}
 
 }
