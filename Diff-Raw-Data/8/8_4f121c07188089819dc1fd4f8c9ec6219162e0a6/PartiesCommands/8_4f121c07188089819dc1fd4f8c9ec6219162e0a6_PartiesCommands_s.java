 package com.CC.Commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.CC.Parties.Create;
 import com.CC.Parties.*;
 
 public class PartiesCommands {
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) { 
 		if(sender instanceof Player) {
 			Player player = (Player)sender;
 			if (cmd.getName().equalsIgnoreCase("party")) {
				if (args.length == 0){
 				if (args[0].equalsIgnoreCase("help")) {
 					Help.help(player);
 				} else if (args[0].equalsIgnoreCase("create")){
 					Create.create(player);
 				} else if (args[0].equalsIgnoreCase("join")){
 					Join.join(player);
 				} else if (args[0].equalsIgnoreCase("leave")){
 					Leave.leave(player);
 				} else if (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("status")) {
 					Status.status(player);
 				} else if (args[0].equalsIgnoreCase("start")){
					if (args.length == 1 && args[1].equalsIgnoreCase("red")){
 					StartRed.start(player);
					}else if (args.length == 1 && args[1].equalsIgnoreCase("blue")){
 						StartBlue.start(player);
 						
 					}
 				}
 
 			
 			
 			
 			// instance of check because NOT all senders are players. Simply typecasting to player would break on console cmd.
 		}else{
 			
 		}
 		
 		
 		
 
 			}
 		}
 		return false;
 	}
 	
 }
 
