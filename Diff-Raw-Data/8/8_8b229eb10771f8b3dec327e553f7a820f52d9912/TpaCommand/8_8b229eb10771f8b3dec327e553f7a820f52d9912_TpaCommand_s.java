 package com.caindonaghey.commandbin.commands;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.caindonaghey.commandbin.Phrases;
 
 public class TpaCommand implements CommandExecutor {
 	
 	public static HashMap<String, String> tpaPlayers = new HashMap<String, String>();
 	
 	public boolean onCommand(CommandSender s, Command c, String l, String [] args) {
 		if(l.equalsIgnoreCase("tpa")) {
 			if(!(s instanceof Player)) {
 				System.out.println(Phrases.get("no-console"));
 				return true;
 			}
 			
 			Player player = (Player) s;
 			
 			if(!player.hasPermission("CommandBin.tpa")) {
 				player.sendMessage(Phrases.get("no-permission"));
 				return true;
 			}
 			
 			if(args.length != 1) {
 				player.sendMessage(Phrases.get("invalid-arguments"));
 				return true;
 			}
 			
 			Player otherPlayer = Bukkit.getServer().getPlayer(args[0]);
 			
 			if(otherPlayer == null) {
 				player.sendMessage(Phrases.get("player-invalid"));
 				return true;
 			}
 			
 			if(!tpaPlayers.containsKey(otherPlayer.getName()) && !(tpaPlayers.get(otherPlayer.getName()) == player.getName())) {
 				tpaPlayers.put(otherPlayer.getName(), player.getName());
 				player.sendMessage(Phrases.get("teleport-request-sent"));
				otherPlayer.sendMessage(player.getName() + " " + Phrases.get("teleport-request-receive"));
 				otherPlayer.sendMessage(Phrases.get("teleport-request-receive-2"));
 				return true;
 			}
 			
 			player.sendMessage(Phrases.get("teleport-request-already"));
 		}
 		return true;
 	}
 
 }
