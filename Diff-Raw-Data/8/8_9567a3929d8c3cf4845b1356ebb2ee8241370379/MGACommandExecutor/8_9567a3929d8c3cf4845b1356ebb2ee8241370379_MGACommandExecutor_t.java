 package com.github.mineguild.MineguildAdmin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public class MGACommandExecutor implements CommandExecutor {
 	public MGACommandExecutor(Main plugin) {
 	}
 	@SuppressWarnings({ "unused"})
 	@Override
 	//Command interpreter
 		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 //Introducing /mga command
 		if(cmd.getName().equalsIgnoreCase("mga")){
 				//If the args are length 0 or the args[0] isnt equal "version" it will return false
 				if (args.length == 0 || !args[0].equalsIgnoreCase("version")){
 					return false;
 				}
 				if (args[0].equalsIgnoreCase("version")) {
 				//Show version to sender and return true if the value of args[0] is equal to "version"
 				sender.sendMessage("MineguildAdmin V0.3");
 				return true;
 				}
 		}
 //Introducing /heal command
 		 if(cmd.getName().equalsIgnoreCase("heal")){
 			 //If there are no args, simply act with the command sender
 			 if(args.length == 0){
 				 //Only do this if the sender is instanceof player
 				 if(sender instanceof Player){
 				 Player p = (Player) sender;
 				 //set max Health and message
 					 p.setHealth(20);
 					 p.sendMessage(ChatColor.RED+"You feel restored");
 				 }
 				 //If the sender is not instanceof player send message with console use back to the sender
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Please use /heal <player> on console!");
 					 return true;
 				 } 
 			 }
 	     //If the args have the length 1 continue
 			 else {
 			 Player p = Bukkit.getPlayerExact(args[0]);
 			 //If the above defined player isnt null continue
 			 if(p != null){
 				 //set max Health and message
 				 String pname = p.getName();
 				 p.setHealth(20);
 				 p.sendMessage(ChatColor.RED+"You feel restored");
 				 sender.sendMessage(ChatColor.RED+"You just healed " + pname);
 				 return true;
 			 }
 			 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 			 else {
 				 sender.sendMessage(ChatColor.RED+"Player is not online!");
 				 return true;
 			 }
 			 }
 		 }
 //Introducing /feed command
 			 if(cmd.getName().equalsIgnoreCase("feed")){
 				 //If there are no args, simply act with the command sender
 				 if(args.length == 0){
 					 //Only do this if the sender is instanceof player
 					 if(sender instanceof Player){
 					 Player p = (Player) sender;
 					 //set max hunger level and message
 						 p.setFoodLevel(20);
 						 p.sendMessage(ChatColor.RED+"You feeded yourself");
 					 }
 					 //If the sender is not instanceof player send message with console use back to the sender
 					 else {
 						 sender.sendMessage(ChatColor.RED+"Please use /feed <player> on console!");
 						 return true;
 					 } 
 				 }
 			 
 		     //If the args have the length 1 continue
 				 else {
 				 Player p = Bukkit.getPlayerExact(args[0]);
 				 //If the above defined player isnt null continue
 				 if(p != null){
 					 //set max hunger level and message both
 					 String pname = p.getName();
 					 p.setFoodLevel(20);
 					 p.sendMessage(ChatColor.RED+"You were feeded.");
 					 sender.sendMessage(ChatColor.RED+"You just feeded " + pname);
 					 return true;
 				 
 				 }
 				 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 					 else {
 						 sender.sendMessage(ChatColor.RED+"Player is not online!");
 						 return true;
 					 }
 				 }
 			 }
 //Introducing /check command
 			 if(cmd.getName().equalsIgnoreCase("check")){
 				 //If there are no args, simply act with the command sender
 				 if(args.length == 0){
 					 //Only do this if the sender is instanceof player
 					 if(sender instanceof Player){
 					 Player p = (Player) sender;
 					 //check health then /2 and print
 						 double health = p.getHealth();
 						 health = health / 2.0;
 						 double hunger = p.getFoodLevel();
 						 hunger = hunger / 2.0;
 						 p.sendMessage(ChatColor.RED+"Your Health is " + health);
 						 p.sendMessage(ChatColor.RED+"Your Hunger is " + hunger);
 						 return true;
 						 }
 					 //If the sender is not instanceof player send message with console use back to the sender
 					 else {
 						 sender.sendMessage(ChatColor.RED+"Please use /check <player> on console!");
 						 return true;
 					 }
 				 }
 			     //If the args have the length 1 continue
 					 if(args.length == 1) {
 					 Player p = Bukkit.getPlayerExact(args[0]);
 					 //If the above defined player isnt null continue
 					 if(p != null){
 						 //set max Health and message
 						 double health = p.getHealth();
 						 String string1 = p.getName();
 						 double hunger = p.getFoodLevel();
 						 hunger = hunger / 2.0;
 						 health = health / 2.0;
 						 sender.sendMessage(ChatColor.RED + string1 +"'s Health is " + health);
 						 sender.sendMessage(ChatColor.RED + string1 +"'s Hunger is " + hunger);
 						 return true;
 					 
 					 }
 					 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 					 else {
 						 sender.sendMessage(ChatColor.RED+"Player is not online!");
 						 return true;
 					 }
 				 }
 			 }
 //Introducing /gm command
 		 if(cmd.getName().equalsIgnoreCase("gm")){
 			 //If there are no args, simply act with the command sender
 			 if(args.length == 0){
 				 //Only do this if the sender is instanceof player
 				 if(sender instanceof Player){
 				 Player p = (Player) sender;
 				 //If the gamemode is survival it will switch to creative and vice versa
 				 //Also returning true, for the correctness
 					 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 						 p.setGameMode(GameMode.CREATIVE);
 						 p.sendMessage(ChatColor.GOLD + "You are now in creative mode!");
 						 return true;
 					 }
 					 else if (p.getGameMode().equals(GameMode.CREATIVE)){
 						 p.setGameMode(GameMode.SURVIVAL);
 						 p.sendMessage(ChatColor.GOLD + "You are now in survival mode!");
 						 return true;
 					 }
 				 }
 				 //If the sender is not instanceof player send message with console use back to the sender
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Please use /gm <player> <gamemode> on console!");
 					 return true;
 				 }
 			 }
 		     //If the args have the length 1 continue
 		      if(args.length == 1) {
 		    	 if(args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("c") || args[0].equals("1") || args[0].equals("0")){
 		    		 
 			    		 if(sender instanceof Player){
 			    			 
 			    			 Player p = (Player) sender;
 			    			 
 				    		 if (args[0].equalsIgnoreCase("c") || args[0].equals(1)){
 								 p.setGameMode(GameMode.CREATIVE);
 								 sender.sendMessage(ChatColor.GOLD + "You are now in creative mode!");
 								 return true;
 							 }
 							 if(args[0].equalsIgnoreCase("s") || args[0].equals(0)){
 								 p.setGameMode(GameMode.SURVIVAL);
 								 sender.sendMessage(ChatColor.GOLD + "You are now in survival mode!");
 								 return true;
 							 }
 			    		 }
 			    		 else{
 			    			 sender.sendMessage(ChatColor.RED+"Please use /gm <player> <gamemode> on console!");
 							 return true;
 			    		 }
 		    	 }
 		    	 else{
		    		 if(sender instanceof Player){
 						 //If the above defined player isnt null continue
						 Player p = Bukkit.getPlayerExact(args[0]);
 						 if(p != null){
							 String pname = p.getDisplayName();
 							 //If the gamemode is survival it will switch to creative and vice versa
 							 //Also returning true, for the correctness
 							 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 								 p.setGameMode(GameMode.CREATIVE);
 								 sender.sendMessage(ChatColor.GOLD + pname + " is now in creative mode!");
 								 p.sendMessage(ChatColor.GOLD + "You are now in creative mode!");
 								 return true;
 							 }
 							 if (p.getGameMode().equals(GameMode.CREATIVE)){
 								 p.setGameMode(GameMode.SURVIVAL);
 								 sender.sendMessage(ChatColor.GOLD + pname + " is now in survival mode!");
 								 p.sendMessage(ChatColor.GOLD + "You are now in survival mode!");
 								 return true;
 							 }
 						 }
 					 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 						 else{
 							 sender.sendMessage(ChatColor.RED+"Player is not online!");
 							 return true;
 						 }
 		    		 }
 		    		 else{
 		    			 sender.sendMessage(ChatColor.RED+"Please use /gm <player> <gamemode> on console!");
 						 return true; 
 		    		 }
 		    	 }
 			 }
 		     else if (args.length == 2) {
 		    	 if(sender instanceof Player){
 			    	 Player p = Bukkit.getPlayerExact(args[0]);
 			    	 String pname = p.getDisplayName();
 			    	 if(p != null){
 						 if (args[1].equalsIgnoreCase("c") || args[1].equals("1")){
 							 p.setGameMode(GameMode.CREATIVE);
 							 sender.sendMessage(ChatColor.GOLD + pname + " is now in creative mode!");
 							 return true;
 						 }
 						 if(args[1].equalsIgnoreCase("s") || args[1].equals("0")){
 							 p.setGameMode(GameMode.SURVIVAL);
 							 sender.sendMessage(ChatColor.GOLD + pname + " is now in survival mode!");
 							 return true;
 						 }
 			    	 }
 			    	 else{
 						 sender.sendMessage(ChatColor.RED+"Player is not online!");
 						 return true;
 					 }
 		    	 }
 		    	 else{
 		    		 sender.sendMessage(ChatColor.RED+"Please use /gm <player> <gamemode> on console!");
 					 return true;
 		    	 }
 		     }
 		 }
 		 if(cmd.getName().equalsIgnoreCase("spawn")){
 			 if(args.length == 0){
 				 if(sender instanceof Player){
 					 Player p = (Player) sender;
 					 Location l = p.getWorld().getSpawnLocation();
 					 p.teleport(l);
 					 sender.sendMessage(ChatColor.GREEN + "You were teleported to the spawn!");
 					 return true;
 				 }
 				 else{
 					 sender.sendMessage(ChatColor.RED + "You cant use this command from console yet!");
 					 return true;
 				 }
 			 }
 			 else{
 				 return false;
 			 }
 		 }
 		 if(cmd.getName().equalsIgnoreCase("setspawn")){
 			 if(sender instanceof Player){
 				 Player p = (Player) sender;
 				 int x = p.getLocation().getBlockX();
 				 int y = p.getLocation().getBlockY();
 				 int z = p.getLocation().getBlockZ();
 				 p.getWorld().setSpawnLocation(x, y, z);
 				 sender.sendMessage(ChatColor.GREEN + "The spawn point was set to you location");
 				 return true;
 			 }
 			 else{
 				 sender.sendMessage(ChatColor.RED + "You only can use this command as player!");
 				 return true;
 			 }
 		 }
 	return false;
 	}
 }
 
 
