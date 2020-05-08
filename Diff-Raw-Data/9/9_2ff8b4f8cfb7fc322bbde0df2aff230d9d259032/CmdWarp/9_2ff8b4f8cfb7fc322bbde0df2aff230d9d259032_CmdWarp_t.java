 package com.bukkit.JamesArchuleta.Warp;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmdWarp extends Command {
 	
 
 	protected CmdWarp(String name) {
 		super(name);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public Boolean run(CommandSender sender,
 			org.bukkit.command.Command command, String commandLabel,
 			String[] args, Server server) {
 
 		
 		
 		if (!hasPermissions((Player) sender)) {
 			((Player) sender)
 					.sendMessage("You do not have permission to use this command.");
 			return true;
 		}
 		
 		
 
 		if (args.length == 1 && hasPermissions((Player) sender)) {
 
 			Player player = (Player) sender;
 			
 			
 			Location l = Locations.getLocation(args[0], server);
 
 			
 			if (l != null) {
 				
				player.teleport(l);
 				player.sendMessage("Warped to " + args[0]);
 				System.out.println(player.getName() + " warped to " + args[0]);
 			} else {
 				player.sendMessage("Warp not found: " + args[0]);
 				System.out.println(player.getName() + " warp failed. Tried "
 						+ args[0]);
 			}
 			return true;
 		}
 		return false;
 
 	}
 	
 	
 
 }
