 package com.github.mineguild.MineguildAdmin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class MGACommandExecutor implements CommandExecutor {
 
 	public MGACommandExecutor(Main plugin) {
 	}
 	@Override
 		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 			if(cmd.getName().equalsIgnoreCase("mga")){
 				if (args.length == 0){
 					return false;
 				}
 				if (!args[0].equalsIgnoreCase("version")){
 					return false;
 				}
 				if (args[0].equalsIgnoreCase("version")) {
 				//Show version to sender and return true
 				sender.sendMessage("MineguildAdmin V0.3");
 				return true;
 				}
 			}
		 if(cmd.getName().equalsIgnoreCase("gm")){
 			 if(args.length == 0){
 				 Player p = (Player) sender;
 				 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 					 p.setGameMode(GameMode.CREATIVE);
 					 return true;
 				 }
 				 else if (p.getGameMode().equals(GameMode.CREATIVE)){
 					 p.setGameMode(GameMode.SURVIVAL);
 					 return true;
 				 }
 			 }
 		 }
 			 if(args.length == 1) {
 				 Player p = Bukkit.getPlayerExact(args[0]);
 				 if(p != null){
 					 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 						 p.setGameMode(GameMode.CREATIVE);
 						 return true;
 					 }
 					 else if (p.getGameMode().equals(GameMode.CREATIVE)){
 						 p.setGameMode(GameMode.SURVIVAL);
 						 return true;
 					 }
 					
 				 
 				 }
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Player is not online!");
 					 return true;
 				 }
 			 }
 		 return false;
 	}
 
 }
 
