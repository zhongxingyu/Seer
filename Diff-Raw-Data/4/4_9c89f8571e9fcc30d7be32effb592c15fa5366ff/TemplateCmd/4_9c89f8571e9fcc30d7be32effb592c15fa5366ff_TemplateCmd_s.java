 package com.lebelw.Tickets.commands;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import com.lebelw.Tickets.TDatabase;
 import com.lebelw.Tickets.TLogger;
 import com.lebelw.Tickets.TPermissions;
 import com.lebelw.Tickets.TTools;
 import com.lebelw.Tickets.Tickets;
 import com.lebelw.Tickets.extras.DataManager;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * @description Handles a command.
  * @author Tagette
  */
 public class TemplateCmd implements CommandExecutor {
 
     private final Tickets plugin;
     DataManager dbm = TDatabase.dbm;
     Player target;
     int currentticket, ticketarg, amount;
     
     public TemplateCmd(Tickets instance) {
         plugin = instance;
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         boolean handled = false;
         if (is(label, "ticket")) {
         	if (args == null || args.length == 0) {
         		handled = true;
         		if (isPlayer(sender)){
         			String name = getName(sender);
         			try{
         				ResultSet result = dbm.query("SELECT ticket FROM players WHERE name='" + name + "'");
         				if (result != null && result.next()){
         					sendMessage(sender,colorizeText("You have ",ChatColor.GREEN) + result.getInt("ticket") + colorizeText(" ticket(s).",ChatColor.GREEN));
         				}
         				else
         					sendMessage(sender,colorizeText("You have 0 ticket",ChatColor.RED));
         			
 	        		} catch (SQLException se) {
 						TLogger.error(se.getMessage());
 	                }
         		
         		
         		}
         	}
         	else {
         		//Is the first argument give?
         		if (is(args[0],"help")){
         			handled = true;
             		sendMessage(sender, "You are using " + colorizeText(Tickets.name, ChatColor.GREEN)
                             + " version " + colorizeText(Tickets.version, ChatColor.GREEN) + ".");
             		sendMessage(sender, "Commands:");
             		if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.give", getPlayer(sender).isOp())){
             			sendMessage(sender,colorizeText("/ticket give <Name> <Amount>",ChatColor.YELLOW) +" - Give ticket to semeone");
             		}
             		if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.take", getPlayer(sender).isOp())){
             			sendMessage(sender,colorizeText("/ticket take <Name> <Amount>",ChatColor.YELLOW) +" - Take ticket to semeone");
             		}	
         		}
         		else if (is(args[0],"send")){
         			handled = true;
         			if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.send", getPlayer(sender).isOp())){
         				if (args.length == 1 || args.length == 2){
         					sendMessage(sender,colorizeText("/ticket send <Name> <Amount>",ChatColor.YELLOW) +" - Send ticket to semeone");
         					return handled;
         				}
         				if (!TTools.isInt(args[1])){
         					if (TTools.isInt(args[2])){
         						String sendername = ((Player)sender).getName();
         						String name = args[1];
                 				try {
                 					target = plugin.matchSinglePlayer(sender, name);
                 					if (target.getName() != null){
                     					name = target.getName();
                     				}
                 				}catch (CommandException error){
                 					sendMessage(sender,colorizeText(error.getMessage(),ChatColor.RED));
                 					return handled;
                 				}
                 				ticketarg = Integer.parseInt(args[2]);
                 				if (checkIfPlayerExists(sendername)){
                 			    		currentticket = getPlayerTicket(sendername);
                 						amount = currentticket - ticketarg;
                 						if (amount < 0){
                 							sendMessage(sender,colorizeText("You online have ",ChatColor.RED) + currentticket + colorizeText(" ticket(s)! You can't send ",ChatColor.RED) + ticketarg + colorizeText(" ticket(s)!",ChatColor.RED));
                 							return handled;
                 						}
                 						if (givePlayerTicket(name,ticketarg)){
                 							dbm.update("UPDATE players SET ticket=" + amount + " WHERE name = '" + name + "'");
                 							sendMessage(sender,colorizeText(args[2] +" ticket(s) has been given to "+ name,ChatColor.GREEN));
                         					if (target.getName() != null){
                         						sendMessage(target,colorizeText("You received "+ ticketarg +" ticket(s) from "+ ((Player)sender).getName() + ".",ChatColor.GREEN));
                         					}
                 						}
                 						
                 						
                 			    }else{
                 			    	sendMessage(sender,colorizeText("You can't send ticket(s) because you don't have any!",ChatColor.RED));	
                 				}
                 				
         					}else{
                 				sendMessage(sender,colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
                 			}
         					
         				}else {
         					sendMessage(sender,colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
         				}
         					
         			}else{
         				sendMessage(sender,colorizeText("Permission denied.",ChatColor.RED));
     				} 
         		}
         		else if(is(args[0],"give")){
         			handled = true;
         			//We check the guy permission
         			if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.give", getPlayer(sender).isOp())){
             			//We check if we received a string for the first parameter (Player)
             			if (args[1] != null && !TTools.isInt(args[1])){
             				//We check if we received a int for the second parameter (Amount)
             				if (args[2] != null && TTools.isInt(args[2])){
             					
                     				String name = args[1];
                     				try {
                     					target = plugin.matchSinglePlayer(sender, name);
                     					if (target.getName() != null){
                         					name = target.getName();
                         				}
                     				}catch (CommandException error){
                     					sendMessage(sender,colorizeText(error.getMessage(),ChatColor.RED));
                     					return handled;
                     				}
                     				ticketarg = Integer.parseInt(args[2]);
                     				if (givePlayerTicket(name,ticketarg)){
                     					sendMessage(sender,colorizeText(args[2] +" ticket(s) has been given to "+ name,ChatColor.GREEN));
                     					if (target.getName() != null){
                     						sendMessage(target,colorizeText("You received "+ ticketarg +" ticket(s) from "+ ((Player)sender).getName() + ".",ChatColor.GREEN));
                     					}
                     				}else{
                     					sendMessage(sender,colorizeText("A error occured",ChatColor.GREEN));
                     				}
             				}
             				else{
                 				sendMessage(sender,colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
                 			}
             			}
             			else{
             				sendMessage(sender,colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
             			}
             			
             		}
         			else{
         				sendMessage(sender,colorizeText("Permission denied.",ChatColor.RED));
         			}        			
         		}
         		//Is the first argument take?
         		else if(is(args[0],"take")){
         			handled = true;
         			if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.take", getPlayer(sender).isOp())){
             			
             			//We check if we received a string for the first parameter (Player)
             			if (args[1] != null && !TTools.isInt(args[1])){
             				//We check if we received a int for the second parameter (Amount)
             				if (args[2] != null && TTools.isInt(args[2])){
             					
                     				String name = args[1];
                     				try {
                     					target = plugin.matchSinglePlayer(sender, name);
                     					if (target.getName() != null){
                         					name = target.getName();
                         				}
                     				}catch (CommandException error){
                     					sendMessage(sender,colorizeText(error.getMessage(),ChatColor.RED));
                     					return handled;
                     				}
                     				
                     				ticketarg = Integer.parseInt(args[2]);
                     				if (checkIfPlayerExists(name)){
                     		    		currentticket = getPlayerTicket(name);
                     					amount = currentticket - ticketarg;
                     					if (amount < 0){
                     						sendMessage(sender,colorizeText("You can't remove ",ChatColor.RED) + ticketarg + colorizeText(" ticket(s)! This player only have ",ChatColor.RED) + currentticket + colorizeText(" ticket(s)!",ChatColor.RED));
                     						return handled;
                     					}
                     					dbm.update("UPDATE players SET ticket=" + amount + " WHERE name = '" + name + "'");
                     		    	}else{
                     		    		sendMessage(sender,colorizeText("You can't remove tickets to " + name + " because he doesn't have any!",ChatColor.RED));
                     		    		
                     		    	}
             				}
             				else{
                 				sendMessage(sender,colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
                 			}
             			}
             			else{
             				sendMessage(sender,colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
             			}
             			
             		}
         			else{
         				sendMessage(sender,colorizeText("Permission denied.",ChatColor.RED));
         			}        
         		}
         	}
         	
         }
         return handled;
     }
 
     // Simplifies and shortens the if statements for commands.
     private boolean is(String entered, String label) {
         return entered.equalsIgnoreCase(label);
     }
 
     // Checks if the current user is actually a player.
     private boolean isPlayer(CommandSender sender) {
         return sender != null && sender instanceof Player;
     }
 
     // Checks if the current user is actually a player and sends a message to that player.
     private boolean sendMessage(CommandSender sender, String message) {
         boolean sent = false;
         if (isPlayer(sender)) {
             Player player = (Player) sender;
             player.sendMessage(message);
             sent = true;
         }
         return sent;
     }
 
     private boolean sendLog(CommandSender sender, String message) {
         boolean sent = false;
         if (!isPlayer(sender)) {
             TLogger.info(message);
             sent = true;
         }
         return sent;
     }
 
     // Checks if the current user is actually a player and returns the name of that player.
     private String getName(CommandSender sender) {
         String name = "";
         if (isPlayer(sender)) {
             Player player = (Player) sender;
             name = player.getName();
         }
         return name;
     }
 
     // Gets the player if the current user is actually a player.
     private Player getPlayer(CommandSender sender) {
         Player player = null;
         if (isPlayer(sender)) {
             player = (Player) sender;
         }
         return player;
     }
 
     private String colorizeText(String text, ChatColor color) {
         return color + text + ChatColor.WHITE;
     }
     /*
      * Checks if a player account exists
      * 
      * @param name    The full name of the player.
      */
     private boolean checkIfPlayerExists(String name)
     {
     	ResultSet result = dbm.query("SELECT id FROM players WHERE name = '" + name + "'");
 		try {
 			if (result != null  && result.next()){
 				return true;
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			TLogger.warning(e.getMessage());
 			return false;
 		}
 		return false;
     	
     }
     /*
      * Get the amount of tickets a player have
      * 
      * @param name    The full name of the player.
      */
     private int getPlayerTicket(String name){
     	ResultSet result = dbm.query("SELECT ticket FROM players WHERE name = '" + name + "'");
 		try {
 			if (result != null  && result.next()){
 				return result.getInt("Ticket");
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			TLogger.warning(e.getMessage());
 			return 0;
 		}
 		return 0;
     }
     /*
      * Create a player ticket account
      * 
      * @param name    The full name of the player.
      */
     private boolean createPlayerTicketAccount(String name){
     	if (!checkIfPlayerExists(name)){
     		if(dbm.insert("INSERT INTO players(name) VALUES('" + name + "')")){
     			return true;
     		}else{
     			return false;
     		}
     	}else{
     		return false;
     	}
     }
     private boolean givePlayerTicket(String name, Integer amount){
     	if (checkIfPlayerExists(name)){
     		currentticket = getPlayerTicket(name);
 			amount = currentticket + amount;
 			return dbm.update("UPDATE players SET ticket=" + amount + " WHERE name = '" + name + "'");
     	}else{
     		if (createPlayerTicketAccount(name)){
     			return dbm.update("UPDATE players SET ticket=" + amount + " WHERE name = '" + name + "'");
     		}else{
     			return false;
     		}
     	}
     }
 }
