 package com.turt2live.antishare;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.ASRegion;
 
 /**
  * GameMode Command
  * 
  * @author turt2live
  */
 public class GameModeCommand {
 
 	public static void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		if((event.getMessage().toLowerCase().startsWith("/gm")
 				|| event.getMessage().toLowerCase().startsWith("/gamemode"))
 				&& AntiShare.instance.getConfig().getBoolean("enabled-features.gamemode-command")){
 			// Setup variables
 			GameMode to = null;
 			Player target = null;
 			CommandSender sender = event.getPlayer();
 			String message = (event.getMessage().toLowerCase().startsWith("/gm")) ? event.getMessage().toLowerCase().replace("/gm", "") : (event.getMessage().toLowerCase().startsWith("/gamemode")) ? event.getMessage().toLowerCase().replace("/gamemode", "") : event.getMessage().toLowerCase();
 			String[] args = message.trim().split(" ");
 			AntiShare plugin = AntiShare.instance;
 			boolean skip = false;
 
 			// Console check
 			if(!(sender instanceof Player)){
 				if(args.length < 2){
 					target = Bukkit.getPlayer(args[0]);
 					if(target != null){
 						to = target.getGameMode().equals(GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE;
 					}else{
 						ASUtils.sendToPlayer(sender, ChatColor.RED + "Player not found! Sorry :(");
 					}
 				}
 			}
 
 			// Find target and gamemode
 			if(args.length < 1){
 				target = (Player) sender;
 				to = target.getGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE;
 			}else if(args.length == 1 && sender instanceof Player){
 				target = (Player) sender;
 				to = ASUtils.getGameMode(args[0]);
 				if(to == null){ // for /gm turt2live
 					target = Bukkit.getPlayer(args[0]);
 					if(target == null){
 						// Reset
 						target = (Player) sender;
 					}else{
 						to = target.getGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE;
 					}
 				}
 			}else if(args.length == 1 && !(sender instanceof Player)){
 				target = Bukkit.getPlayer(args[0]);
 				if(target != null){
 					to = target.getGameMode().equals(GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE;
 				}
 			}else if(args.length == 2){
 				target = Bukkit.getPlayer(args[1]);
 				to = ASUtils.getGameMode(args[0]);
 			}
 
 			// Check gamemode and target for validity
 			if(target == null || to == null){
 				if(target == null){
 					ASUtils.sendToPlayer(sender, ChatColor.RED + "Player not found! Sorry :(");
 				}else{
 					ASUtils.sendToPlayer(sender, ChatColor.RED + "Gamemode not known! Sorry :(");
 				}
 				return;
 			}
 
 			// Check permissions
 			if(!plugin.getPermissions().has(sender, "AntiShare.gamemode")){
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "You cannot do that!");
 				return;
 			}
 			if(sender instanceof Player && !target.getName().equals(sender.getName())){
 				if(!plugin.getPermissions().has(sender, "AntiShare.gamemode.others")){
 					ASUtils.sendToPlayer(sender, ChatColor.RED + "You cannot do that!");
 					return;
 				}
 			}
 
 			// Region Check
 			if(!plugin.getPermissions().has(target, PermissionNodes.REGION_ROAM)){
 				ASRegion region = plugin.getRegionManager().getRegion(target.getLocation());
 				if(region != null){
 					ASUtils.sendToPlayer(sender, ChatColor.RED + "Your target is in a region and therefore cannot change Game Mode");
 					skip = true;
 				}
 			}
 
 			// Split check
 			GameMode split = plugin.getListener().getConfig(target.getWorld()).getSideOfSplit(target);
 			if(split != null){
				if(split != to){
					ASUtils.sendToPlayer(sender, ChatColor.RED + "Your target is in a World Split and therefore cannot change Game Mode");
					skip = true;
				}
 			}
 
 			// Only do stuff if we are told not to skip it
 			if(!skip){
 				// Do everything and alert people
 				target.setGameMode(to);
 				ASUtils.sendToPlayer(target, "You were changed to " + to.toString().toLowerCase());
 				if(!target.getName().equalsIgnoreCase(sender.getName())){
 					ASUtils.sendToPlayer(sender, "You changed " + target.getName() + "'s Game Mode to " + to.toString().toLowerCase());
 				}
 
 				// Alert server/admins
 				message = ChatColor.YELLOW + sender.getName() + ChatColor.WHITE + " changed " + ChatColor.YELLOW + (target.getName().equalsIgnoreCase(sender.getName()) ? "themselves" : target.getName()) + ChatColor.WHITE + " to " + ChatColor.YELLOW + to.name();
 				plugin.getAlerts().alert(message, sender, "no message", AlertType.GENERAL, AlertTrigger.GENERAL);
 			}
 
 			// Stop the "Command not found" message
 			event.setCancelled(true);
 		}
 	}
 
 }
