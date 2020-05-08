 package com.caindonaghey.commandbin.commands;
 
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.caindonaghey.commandbin.Phrases;
 
 public class FlyCommand implements CommandExecutor {
 	
 	public static HashSet<String> flyingPlayers = new HashSet<String>();
 	
 	public boolean onCommand(CommandSender s, Command c, String l, String [] args) {
 		if(l.equalsIgnoreCase("fly")) {
 			if(!(s instanceof Player)) {
 				if(args.length != 1) {
 					System.out.println("[CommandBin] " + Phrases.get("invalid-arguments"));
 					return false;
 				}
 				
 				Player player = Bukkit.getServer().getPlayer(args[0]);
 				if(player == null) {
 					System.out.println("[CommandBin] " + Phrases.get("player-invalid"));
 					return true;
 				}
 				
 				if(!flyingPlayers.contains(player.getName())) {
 					flyingPlayers.add(player.getName());
 					System.out.println("[CommandBin] " + Phrases.get("player-fly"));
 					player.setFlying(true);
 					return true;
 				}
 				
 				flyingPlayers.remove(player.getName());
 				System.out.println("[CommandBin] " + Phrases.get("player-nofly"));
 				player.setFlying(false);
 				return true;
 			}
 			
 			Player player = (Player) s;
 			
 			if(args.length == 0) {
 				if(!player.hasPermission("CommandBin.fly.self")) {
 					player.sendMessage(ChatColor.RED + "[CommandBin] " + Phrases.get("no-permission"));
 					return true;
 				}
 				
 				if(!flyingPlayers.contains(player.getName())) {
 					flyingPlayers.add(player.getName());
 					player.sendMessage(ChatColor.GREEN + "[CommandBin] " + Phrases.get("fly-self"));
 					player.setFlying(true);
 					return true;
 				}
 				flyingPlayers.remove(player.getName());
 				player.sendMessage(ChatColor.GREEN + "[CommandBin] " + Phrases.get("nofly-self"));
 				player.setFlying(false);
 				return true;
 			}
 			
 			if(args.length == 1) {
 				if(!player.hasPermission("CommandBin.fly.others")) {
 					player.sendMessage(ChatColor.RED + "[CommandBin] " + Phrases.get("no-permission"));
 					return true;
 				}
 				Player otherPlayer = Bukkit.getServer().getPlayer(args[0]);
 				if(otherPlayer == null) {
 					player.sendMessage(ChatColor.RED + "[CommandBin] " + Phrases.get("player-invalid"));
 					return true;
 				}
 				
 				if(!flyingPlayers.contains(otherPlayer.getName())) {
 					flyingPlayers.add(otherPlayer.getName());
 					player.sendMessage(ChatColor.GREEN + "[CommandBin] " + Phrases.get("player-fly"));
 					otherPlayer.setFlying(true);
 					return true;
 				}
 				flyingPlayers.remove(otherPlayer.getName());
 				player.sendMessage(ChatColor.GREEN + "[CommandBin] " + Phrases.get("player-nofly"));
 				return true;
 			}
 			
 			player.sendMessage(ChatColor.RED + "[CommandBin] " + Phrases.get("invalid-arguments"));
 			return false;
 		}
 		return true;
 	}
 
 }
