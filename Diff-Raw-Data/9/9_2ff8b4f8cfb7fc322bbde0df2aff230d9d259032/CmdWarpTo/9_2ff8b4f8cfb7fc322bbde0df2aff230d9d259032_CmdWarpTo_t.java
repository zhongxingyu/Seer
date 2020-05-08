 package com.bukkit.JamesArchuleta.Warp;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmdWarpTo extends Command {
 	
 
 	protected CmdWarpTo(String name) {
 		super(name);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public Boolean run(CommandSender sender, org.bukkit.command.Command command, String commandLabel,
 			String[] args, Server server) {
 
 		
 		if( !hasPermissions((Player)sender)){
 			((Player)sender).sendMessage("You do not have permission to use this command.");
 			return true;
 		}
 		
 		
 		if (args.length == 2  && hasPermissions((Player)sender)){
 		
 			Player player = (Player) sender;
 			Player victim = server.matchPlayer(args[0]).get(0);
 			if (victim == null){
 				player.sendMessage(args[0]+ " not found ");
 			}
 			
 			Location l = Locations.getLocation(args[1],server);
 	    	
 	    	if (l != null){
	    		victim.teleport(l);
 	    		victim.sendMessage("Warped by " + player.getDisplayName());
 	    		player.sendMessage(victim.getName()+ " warped to " + args[1]);
 	    		System.out.println(victim.getName() + " warped to " + args[1] + " By " + player.getName());
 	    	}
 	    	else{
 	    		player.sendMessage("Warp not found: " + args[0]);
 	    		System.out.println(player.getName() + " warp failed. Tried " + player.getName() + " to" + args[1]);
 	    	}
 	    	return true;
 	   
 		}
 		return false;
 		
 	}
 	
 	
 
 }
