 package com.lebelw.Tickets.commands;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Random;
 
 import com.lebelw.Tickets.TBusiness;
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
     int currentticket , ticketarg, amount;
     String businessname,businessowner;
     TMoney TMoney;
     
     public TemplateCmd(Tickets instance) {
         plugin = instance;
         TMoney = new TMoney(plugin);
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         boolean handled = false;
         try{
         	// /ticket command
         	if (is(label, "ticket")) {
         		//No arguments? We show the tickets the guy have!
 	        	if (args == null || args.length == 0) {
 	        		handled = true;
 	        		if (isPlayer(sender)){
 	        			String name = plugin.getName(sender);
 	        				ResultSet result = plugin.getPlayerTicket(name);
 	        				if (result != null){
 	        					listPlayerTicket(sender,result);
 	        				}else
 	        					sendMessage(sender,plugin.colorizeText("You got no tickets!",ChatColor.YELLOW));
 	        				sendMessage(sender,plugin.colorizeText("/ticket help for help",ChatColor.YELLOW));
 	        		}
 	        	}
 	        	else {
 	        		//Is the first argument give?
 	        		if (is(args[0],"help")){
 	        			handled = true;
 	            		sendMessage(sender, "You are using " + plugin.colorizeText(Tickets.name, ChatColor.GREEN)
 	                            + " version " + plugin.colorizeText(Tickets.version, ChatColor.GREEN) + ".");
 	            		sendMessage(sender, "Commands:");
 	            		if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.check", plugin.getPlayer(sender).isOp())){
 	            			sendMessage(sender,plugin.colorizeText("/ticket <Name>",ChatColor.YELLOW) +" - See semeone's else ticket amount");
 	            		}
 	            		if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.give", plugin.getPlayer(sender).isOp())){
 	            			sendMessage(sender,plugin.colorizeText("/ticket give <Name> <Amount> <Business>",ChatColor.YELLOW) +" - Give ticket to semeone");
 	            		}
 	            		if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.take", plugin.getPlayer(sender).isOp())){
 	            			sendMessage(sender,plugin.colorizeText("/ticket take <Name> <Amount> <Business>",ChatColor.YELLOW) +" - Take ticket to semeone");
 	            		}	
 	            		if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.send", plugin.getPlayer(sender).isOp())){
 	            			sendMessage(sender,plugin.colorizeText("/ticket send <Name> <Amount> <Business>",ChatColor.YELLOW) +" - Send ticket to semeone");
 	            		}
 	            		if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.buy", plugin.getPlayer(sender).isOp())){
 	            			sendMessage(sender,plugin.colorizeText("/ticket buy <Amount>",ChatColor.YELLOW) +" - Buy tickets with money.");
 	            		}
 	            		if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.lottery", plugin.getPlayer(sender).isOp())){
 	            			sendMessage(sender,plugin.colorizeText("/ticket lottery <Number>",ChatColor.YELLOW) + "- Win a item with ticket lottery!");
 	            		}
 	            		
 	        		}
 	        		else if (is(args[0],"send")){
 	        			handled = true;
 	        			if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.send", plugin.getPlayer(sender).isOp())){
 	        				//We check if we have all the arguments
 	        				if (args.length == 1 || args.length == 2 || args.length == 3){
 	        					sendMessage(sender,plugin.colorizeText("/ticket send <Name> <Amount> <Business>",ChatColor.YELLOW) +" - Send ticket to semeone");
 	        					return handled;
 	        				}
 	        				//We do type check args[1] contains the receiver name args[2] Contains the amount of tickets args[3] contains the business name
 	        				if (!TTools.isInt(args[1])){
 	        					if (TTools.isInt(args[2])){
 	        						if (!TTools.isInt(args[3])){
 	        							//We put it in friendly variables
 	        							String businessname = args[3];
 	        							String sendername = ((Player)sender).getName();
 		        						String name = args[1];
 		        						//We try to find the received online
 		                				try {
 		                					target = plugin.matchSinglePlayer(sender, name);
 		                					if (target.getName() != null){
 		                    					name = target.getName();
 		                    				}
 		                				}catch (CommandException error){
 		                					sendMessage(sender,plugin.colorizeText(error.getMessage(),ChatColor.RED));
 		                					return handled;
 		                				}
 		                				//We check if we don't try to send ticket to ourself
 		                				if (sendername == name){
 		                					sendMessage(sender,plugin.colorizeText("You can't send ticket(s) to yourself!",ChatColor.RED));
 		                					return handled;
 		                				}
 		                				//We parse the ticket amount in integer
 		                				ticketarg = Integer.parseInt(args[2]);
 		                				//We do the stuff
 		                				plugin.checkIfPlayerExists(sendername,sender);
 		                				plugin.removePlayerTicket(sendername,ticketarg,businessname,sender);
 		                				plugin.givePlayerTicket(name,ticketarg,businessname);
 		                				//Send the success message
 		                				sendMessage(sender,plugin.colorizeText(args[2] +" ticket(s) has been given to "+ name,ChatColor.GREEN));
 		                				if (target.getName() != null){
 		                        			sendMessage(target,plugin.colorizeText("You received "+ ticketarg +" ticket(s) from "+ ((Player)sender).getName() + ".",ChatColor.GREEN));
 		                        		}
 		                			
 	        						}else
 	        							sendMessage(sender,plugin.colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
 	        					}else
 	                				sendMessage(sender,plugin.colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
 	        				}else
 	        					sendMessage(sender,plugin.colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));			
 	        			}else
 	        				sendMessage(sender,plugin.colorizeText("Permission denied.",ChatColor.RED));
 	        		}
 	        		else if(is(args[0],"give")){
 	        			handled = true;
 	        			//We check the guy permission
 	        			if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.give", plugin.getPlayer(sender).isOp())){
 	        				//We check if we have all the arguments
 	        				if (args.length == 1 || args.length == 2 || args.length == 3){
 	        					sendMessage(sender,plugin.colorizeText("/ticket give <Name> <Amount> <Business>",ChatColor.YELLOW) +" - Give ticket to semeone");
 	        					return handled;
 	        				}
 	            			//We check if we received a string for the first parameter (Player)
 	            			if (!TTools.isInt(args[1])){
 	            				//We check if we received a int for the second parameter (Amount)
 	            				if (TTools.isInt(args[2])){
 	            					//We check if we received a string for the third parameter (Business)
 	            					if (!TTools.isInt(args[1])){
 	            						//We set friendly variables
 	            						String name = args[1];
 	                    				try {
 	                    					target = plugin.matchSinglePlayer(sender, name);
 	                    					if (target.getName() != null){
 	                        					name = target.getName();
 	                        				}
 	                    				}catch (CommandException error){
 	                    					sendMessage(sender,plugin.colorizeText(error.getMessage(),ChatColor.RED));
 	                    					return handled;
 	                    				}
 	                    				ticketarg = Integer.parseInt(args[2]);
 	                    				String businessname = args[3];
 	                    				plugin.givePlayerTicket(name,ticketarg,businessname);
 	                    				sendMessage(sender,plugin.colorizeText(args[2] +" ticket(s) has been given to "+ name,ChatColor.GREEN));
 	                    				if (target.getName() != null){
 	                    					sendMessage(target,plugin.colorizeText("You received "+ ticketarg +" ticket(s) from "+ ((Player)sender).getName() + ".",ChatColor.GREEN));
 	                    				}
 	            					}
 	                    				
 	            				}
 	            				else{
 	                				sendMessage(sender,plugin.colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
 	                			}
 	            			}
 	            			else{
 	            				sendMessage(sender,plugin.colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
 	            			}
 	            			
 	            		}
 	        			else{
 	        				sendMessage(sender,plugin.colorizeText("Permission denied.",ChatColor.RED));
 	        			}        			
 	        		}
 	        		//Is the first argument take?
 	        		/*else if (is(args[0],"buy")){
 	        			handled = true;
 	        			if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.buy", plugin.getPlayer(sender).isOp())){
 	        				if (args.length == 1){
 	        					sendMessage(sender,plugin.colorizeText("Cost for 1 ticket:",ChatColor.YELLOW) + plugin.colorizeText(TMoney.formatText(TConfig.cost),ChatColor.GREEN));
 	        					sendMessage(sender,plugin.colorizeText("/ticket buy <Amount>",ChatColor.YELLOW));
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
 	        									if (plugin.givePlayerTicket(name,tickets)){
 	        										TMoney.removeMoney(name, price);
 	        										sendMessage(sender,plugin.colorizeText("You just bought ",ChatColor.GREEN) + tickets + plugin.colorizeText(" for ",ChatColor.GREEN) + TMoney.formatText(price));
 	        									}
 	        								}else{
 	        									sendMessage(sender,plugin.colorizeText("You don't have enough money! You need ",ChatColor.RED) + plugin.colorizeText(TMoney.formatText(price),ChatColor.WHITE));
 	        								}
 	        							}else {
 	        								sendMessage(sender,plugin.colorizeText("You don't have any accounts!",ChatColor.RED));
 	        							}
 	        						}else{
 	        							sendMessage(sender,plugin.colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
 	        						}
 	        					}else{
 	        						sendMessage(sender,plugin.colorizeText("The argument is required!",ChatColor.RED));
 	        					}
 	        				}else{
 	        					sendMessage(sender,plugin.colorizeText("A economy system must be loaded!",ChatColor.RED));
 	        				}
 	        			}
 	        		}*/
 	        		else if(is(args[0],"take")){
 	        			handled = true;
 	        			if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.take", plugin.getPlayer(sender).isOp())){
 	            			//We check if we have all the arguments
 	        				if (args.length == 1 || args.length == 2 || args.length == 3){
 	        					sendMessage(sender,plugin.colorizeText("/ticket take <Name> <Amount> <Business>",ChatColor.YELLOW) +" - Take ticket to semeone");
 	        					return handled;
 	        				}
 	            			//We check if we received a string for the first parameter (Player)
 	            			if (args[1] != null && !TTools.isInt(args[1])){
 	            				//We check if we received a int for the second parameter (Amount)
 	            				if (args[2] != null && TTools.isInt(args[2])){
 	            					//We check if we received a string for the third parameter (Business)
 	            					if (args[3] != null && !TTools.isInt(args[3])){
 	            						//We give friendly names for the arguments
 	            						String name = args[1];
 	            						//We test to see if that user is online
 	                    				try {
 	                    					target = plugin.matchSinglePlayer(sender, name);
 	                    					if (target.getName() != null){
 	                        					name = target.getName();
 	                        				}
 	                    				}catch (CommandException error){
 	                    					sendMessage(sender,plugin.colorizeText(error.getMessage(),ChatColor.RED));
 	                    					return handled;
 	                    				}
 	                    				
 	                    				ticketarg = Integer.parseInt(args[2]);
 	                    				String businessname = args[3];
 	                    				//We check if this business exists
 	                    					if (plugin.checkIfPlayerExists(name)){
 		                    					plugin.removePlayerTicket(name,ticketarg,businessname,sender);		                    					
 		                    					sendMessage(sender,plugin.colorizeText(args[2] +" ticket(s) has been removed from "+ name,ChatColor.GREEN));
 			                					if (target.getName() != null){
 			                						sendMessage(target,plugin.colorizeText(ticketarg +" ticket(s) has been removed by "+ ((Player)sender).getName() + ".",ChatColor.RED));
 			                					}
 	                    				}
 		                    				
 	            					}
 		                    				
 	            				}
 	            				else{
 	                				sendMessage(sender,plugin.colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
 	                			}
 	            			}
 	            			else{
 	            				sendMessage(sender,plugin.colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
 	            			}
 	            			
 	            		}
 	        			else{
 	        				sendMessage(sender,plugin.colorizeText("Permission denied.",ChatColor.RED));
 	        			}
 	        		}else if (is(args[0],"lottery")){
 	        			handled = true;
 	        			if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.lottery", plugin.getPlayer(sender).isOp())){
 	        				if (args.length == 1 || args.length == 2){
 	        					sendMessage(sender,plugin.colorizeText("/ticket lottery <Number> <Business>",ChatColor.YELLOW) + "- Play Lottery and win a item!");
 	        					return handled;
 	        				}
 	        				//Do we have a number for the lottery?
 	        				if (TTools.isInt(args[1])){
 	        					//Do we have the business name?
 	        					if (!TTools.isInt(args[2])){
 	        						//we set the arguments to friendly variables
 	        						int lotteryticket = Integer.parseInt(args[1]);
 	        						String businessname = args[2];
 	        						//We check if this business is a lottery one
 	        						TBusiness.isLotteryBusiness(businessname);
 	        						//We grab the lottery chance for this business
 	        						int businesschance = TBusiness.getBusinessLotteryChance(businessname);
 	        						//Did we select a number higher than the chance?
 		        					if (lotteryticket > businesschance){
 		        						sendMessage(sender,plugin.colorizeText("You must choose a number from ",ChatColor.RED) + "0" + plugin.colorizeText(" to ",ChatColor.RED) + businesschance);
 		        						return handled;
 		        					}
 		        					//We grab the player name and remove 1 ticket
 		        					String name = ((Player)sender).getName();
 		        					plugin.removePlayerTicket(name,TBusiness.getBusinessLotteryCost(businessname),businessname);
 		        					//Starts the generator
 		        					Random generator = new Random();
 		        					//We generate a number
 		        					int random = generator.nextInt(businesschance + 1);
 		        					//Did the guy win?
 		        					if (random == lotteryticket){
 		        						//We grab the item and we give it to the winner
 		        						int businesslotteryitem = TBusiness.getBusinessLotteryItem(businessname);
 		        						Material item = Material.getMaterial(businesslotteryitem);
 		        						ItemStack itemstack = new ItemStack(item,1);
 		        						((Player)sender).getInventory().addItem(itemstack);
 		        						sendMessage(sender,plugin.colorizeText("You just won a " + item.toString() +"!",ChatColor.GREEN));
 		        					}else{
 		        						sendMessage(sender,plugin.colorizeText("You do not have a winning ticket.",ChatColor.RED));
 		        					}
 	        					}
 	            				else
 	                				sendMessage(sender,plugin.colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
 	            			}else
 	            				sendMessage(sender,plugin.colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
 	        					
 	        			}
 	        		//We check if we want to look at semeone else ticket
 	        		}else{
 	        			handled = true;
 	        			if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.check", plugin.getPlayer(sender).isOp())){
 	        				String name = args[0];
 	        					target = plugin.matchSinglePlayer(sender, name);
 	        					if (target.getName() != null){
 	            					name = target.getName();
 	            				}
 	        				sendMessage(sender,plugin.colorizeText(name + " currently have:",ChatColor.GREEN));
 	        				ResultSet result = plugin.getPlayerTicket(name);
 	        				if (result != null){
 	        					listPlayerTicket(sender,result);
 	        				}else
 	        					sendMessage(sender,plugin.colorizeText("You got no tickets!",ChatColor.YELLOW));
 	        				
 	        					
 	        			}else{
 	        				sendMessage(sender,plugin.colorizeText("Type /ticket help for help.",ChatColor.YELLOW));
 	        				return handled;
 	        			}
 	        			
 	        		}
 	        	}
 	        // /business command
 	        }else if(is(label,"business")){
	        	if (args == null || args.length == 0){
	        		sendMessage(sender,plugin.colorizeText("Type /business help for help.",ChatColor.YELLOW));
	        		return true;
	        	}
 	        	if (is(args[0],"help")){
 	        		handled = true;
 	        		if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.business.add", plugin.getPlayer(sender).isOp())){
             			sendMessage(sender,plugin.colorizeText("/business add <Name> <Owner> (Lottery) (Lottery Chance) (Lottery Item)",ChatColor.YELLOW) + "- Create a business. (Optional)");
             		}
 	        	}
 	        	//Is the subcommand add?
 	        	if (is(args[0],"add")){
 	        		handled = true;
 	        		int lottery = 0,lotterychance = 0,lotteryitem = 0;
         			if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.business.add", plugin.getPlayer(sender).isOp())){
         				if (args.length == 1 || args.length == 2 || args.length == 3){
         					sendMessage(sender,plugin.colorizeText("/business add <Name> <Owner> (Lottery) (Lottery Chance) (Lottery Item)",ChatColor.YELLOW) + "- Create a business. (Optional)");
         					return handled;
         				}
         				//We check if we put the Lottery activated argument
         				if (args.length == 4){
         					if (TTools.isInt(args[3]))
         						lottery = Integer.parseInt(args[3]);
         				}
         				if (args.length == 5){
         					if (TTools.isInt(args[4]))
         						lotterychance = Integer.parseInt(args[4]);
         				}
         				if (args.length == 6){
         					if (TTools.isInt(args[5]))
         						lotteryitem = Integer.parseInt(args[5]);
         				}
         				if (!TTools.isInt(args[1])){
         					if (TTools.isInt(args[2])){
         						//set friendly variables
         						businessname = args[1];
         						businessowner = args[2];
         						try {
                 					target = plugin.matchSinglePlayer(sender, businessowner);
                 					if (target.getName() != null){
                     					businessowner = target.getName();
                     				}
                 				}catch (CommandException error){
                 					sendMessage(sender,plugin.colorizeText(error.getMessage(),ChatColor.RED));
                 					return handled;
                 				}
 	        					TBusiness.addBusiness(businessname,businessowner,lottery,lotterychance,lotteryitem);
         						sendMessage(sender,plugin.colorizeText("Business "+ businessname + " added!",ChatColor.RED));
         					}else
 	            				sendMessage(sender,plugin.colorizeText("String received for the second parameter. Expecting integer.",ChatColor.RED));
         						
         				}else
             				sendMessage(sender,plugin.colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
         			}else
         				sendMessage(sender,plugin.colorizeText("Permission denied.", ChatColor.RED));
 	        	}
         		else if(is(args[0],"delete")){
         			handled = true;
         			if (args.length == 1){
     	        		sendMessage(sender,plugin.colorizeText("/business delete <Name>",ChatColor.YELLOW) + "- Delete a business");
     					return handled;
     	        	}
     	        	//We check if we received text
     	        	if(TTools.isInt(args[1])){
     	        		sendMessage(sender,plugin.colorizeText("Integer received for the first parameter. Expecting string.",ChatColor.RED));
     	        		return handled;
     	        	}
     	        	//We put it in a friendly name
     	        	String businessname = args[1];
         			if (isPlayer(sender) && TPermissions.permission(plugin.getPlayer(sender), "ticket.business.delete", plugin.getPlayer(sender).isOp())){
         	        	//We try to delete the business
         	        	TBusiness.deleteBusiness(businessname);
         	        	sendMessage(sender,plugin.colorizeText("Business deleted!",ChatColor.GREEN));
         			//If that user is not a admin we check if he is the owner
         			}else{
         				if (TBusiness.isBusinessOwner(((Player)sender).getName(),businessname)){
         					//We try to delete the business
             	        	TBusiness.deleteBusiness(businessname);
             	        	sendMessage(sender,plugin.colorizeText("Business deleted!",ChatColor.GREEN));
         				}
         			}
         		}else
 	        		sendMessage(sender,plugin.colorizeText("Type /business help for help.",ChatColor.YELLOW));
 	        }
         }catch(CommandException e){
         	sendMessage(sender,plugin.colorizeText(e.getMessage() + " Type /ticket help for help.",ChatColor.RED));
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
     private void listPlayerTicket(CommandSender sender, ResultSet result){
     	String lottery = "";
 		try {
 			while(result.next()){
 				if (result.getBoolean("lottery")){
 					lottery = "(Lottery)";
 				}
 				sendMessage(sender,plugin.colorizeText(result.getString("name") + " "+ lottery +" : ",ChatColor.YELLOW) + result.getInt("tickets"));
 				lottery = "";
 			}
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 		}
     }
 }
