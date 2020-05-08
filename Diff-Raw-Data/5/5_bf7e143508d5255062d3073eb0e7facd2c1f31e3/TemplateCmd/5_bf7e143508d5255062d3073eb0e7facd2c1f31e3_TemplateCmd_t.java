 package com.lebelw.Tickets.commands;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Random;
 
 import com.lebelw.Tickets.TConfig;
 import com.lebelw.Tickets.TDatabase;
 import com.lebelw.Tickets.TLogger;
 import com.lebelw.Tickets.TMoney;
 import com.lebelw.Tickets.TPermissions;
 import com.lebelw.Tickets.TTools;
 import com.lebelw.Tickets.Tickets;
 import com.lebelw.Tickets.extras.DataManager;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 
 /**
  * @description Handles a command.
  * @author Tagette
  */
 public class TemplateCmd implements CommandExecutor {
 
     private final Tickets plugin;
     DataManager dbm = TDatabase.dbm;
     Player target;
     int currentticket, ticketarg, amount;
     TMoney TMoney;
     
     public TemplateCmd(Tickets instance) {
         plugin = instance;
         TMoney = new TMoney(plugin);
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
         					sendMessage(sender,colorizeText("/ticket help for help",ChatColor.YELLOW));
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
             		if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.check", getPlayer(sender).isOp())){
             			sendMessage(sender,colorizeText("/ticket <Name>",ChatColor.YELLOW) +" - See semeone's else ticket amount");
             		}
             		if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.give", getPlayer(sender).isOp())){
             			sendMessage(sender,colorizeText("/ticket give <Name> <Amount>",ChatColor.YELLOW) +" - Give ticket to semeone");
             		}
             		if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.take", getPlayer(sender).isOp())){
             			sendMessage(sender,colorizeText("/ticket take <Name> <Amount>",ChatColor.YELLOW) +" - Take ticket to semeone");
             		}	
             		if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.send", getPlayer(sender).isOp())){
             			sendMessage(sender,colorizeText("/ticket send <Name> <Amount>",ChatColor.YELLOW) +" - Send ticket to semeone");
             		}
             		if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.buy", getPlayer(sender).isOp())){
             			sendMessage(sender,colorizeText("/ticket buy <Amount>",ChatColor.YELLOW) +" - Buy tickets with money.");
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
                 				if (sendername == name){
                 					sendMessage(sender,colorizeText("You can't send ticket(s) to yourself!",ChatColor.RED));
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
         		else if (is(args[0],"buy")){
         			handled = true;
         			if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.buy", getPlayer(sender).isOp())){
         				if (args.length == 1){
         					sendMessage(sender,colorizeText("Cost for 1 ticket:",ChatColor.YELLOW) + colorizeText(TMoney.formatText(TConfig.cost),ChatColor.GREEN));
         					sendMessage(sender,colorizeText("/ticket buy <Amount>",ChatColor.YELLOW));
         					return handled;
         				}
         				if (TMoney.checkIfEconomyPlugin()){
         					if (args[1] != null){
         						if (TTools.isInt(args[1])){
         							String name = ((Player)sender).getName();
         							double amount = Double.parseDouble(args[1]);
         							int tickets = Integer.parseInt(args[1]);
         							if (TMoney.checkIfAccountExists(name)){
         								double price = amount * TConfig.cost;
         								if(TMoney.checkIfEnough(name, price)){
         									if (givePlayerTicket(name,tickets)){
         										TMoney.removeMoney(name, price);
         										sendMessage(sender,colorizeText("You just bought ",ChatColor.GREEN) + tickets + colorizeText(" for ",ChatColor.GREEN) + TMoney.formatText(price));
         									}
         								}else{
         									sendMessage(sender,colorizeText("You don't have enough money! You need ",ChatColor.RED) + colorizeText(TMoney.formatText(price),ChatColor.WHITE));
         								}
         							}else {
         								sendMessage(sender,colorizeText("You don't have any accounts!",ChatColor.RED));
         							}
         						}else{
         							sendMessage(sender,colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
         						}
         					}else{
         						sendMessage(sender,colorizeText("The argument is required!",ChatColor.RED));
         					}
         				}else{
         					sendMessage(sender,colorizeText("A economy system must be loaded!",ChatColor.RED));
         				}
         			}
         		}
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
                     					sendMessage(sender,colorizeText(args[2] +" ticket(s) has been removed from "+ name,ChatColor.GREEN));
 	                					if (target.getName() != null){
 	                						sendMessage(target,colorizeText(ticketarg +" ticket(s) has been removed by "+ ((Player)sender).getName() + ".",ChatColor.RED));
 	                					}
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
         		}else if (is(args[0],"lottery")){
         			handled = true;
         			if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.lottery", getPlayer(sender).isOp())){
         				if (args.length == 1){
         					sendMessage(sender,colorizeText("/ticket lottery <Number>",ChatColor.YELLOW));
         					return handled;
         				}
         				if (args[1] != null && TTools.isInt(args[1])){
         					int lotteryticket = Integer.parseInt(args[1]);
         					if (lotteryticket > TConfig.chance){
         						int numberchance = TConfig.chance - 1;
        						sendMessage(sender,colorizeText("You must choose a number from ",ChatColor.RED) + "0" + colorizeText(" to ",ChatColor.RED) + numberchance);
         						return handled;
         					}
         					String name = ((Player)sender).getName();
         					currentticket = getPlayerTicket(name);
         					
        					amount = currentticket - 1;
         					if (amount < 0){
         						sendMessage(sender,colorizeText("You don't have enough tickets to take a lottery ticket!",ChatColor.RED));
         						return handled;
         					}
         					dbm.update("UPDATE players SET ticket=" + amount + " WHERE name = '" + name + "'");
         					Random generator = new Random();
         					int random = generator.nextInt(TConfig.chance);
         					if (random == lotteryticket){
         						Material item = Material.getMaterial(TConfig.lotteryitem);
         						ItemStack itemstack = new ItemStack(item,1);
         						((Player)sender).getInventory().addItem(itemstack);
         						sendMessage(sender,colorizeText("You just won a " + item.toString() +"!",ChatColor.GREEN));
         					}else{
         						sendMessage(sender,colorizeText("You don't have a winning ticket.",ChatColor.RED));
         					}
         				}
         			}
         		//We check if we want to look at semeone else ticket
         		}else{
         			handled = true;
         			if (isPlayer(sender) && TPermissions.permission(getPlayer(sender), "ticket.check", getPlayer(sender).isOp())){
         				String name = args[0];
         				try {
         					target = plugin.matchSinglePlayer(sender, name);
         					if (target.getName() != null){
             					name = target.getName();
             				}
         				}catch (CommandException error){
         					sendMessage(sender,colorizeText(error.getMessage() + " Type /ticket help for help.",ChatColor.RED));
         					return handled;
         				}
         				int ticket = getPlayerTicket(name);
         				sendMessage(sender,colorizeText(name + " currently have ",ChatColor.GREEN) + ticket + colorizeText(" ticket(s)",ChatColor.GREEN));
         					
         			}else{
         				sendMessage(sender,colorizeText("Type /ticket help for help.",ChatColor.YELLOW));
         				return handled;
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
     	if (checkIfPlayerExists(name)){
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
     	}else {
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
