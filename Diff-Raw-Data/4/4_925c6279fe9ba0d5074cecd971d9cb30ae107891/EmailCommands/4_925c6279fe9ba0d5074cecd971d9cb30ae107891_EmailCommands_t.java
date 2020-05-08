 /*
     This file is part of Email.
 
     Email is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Email is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Email.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.mike724.email;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class EmailCommands implements CommandExecutor {
 	
 	private Email plugin;
 
 	public EmailCommands(Email plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("email")) {
 			if(args.length == 0) {
 				sender.sendMessage(ChatColor.AQUA+"Use '/email help' for help");
 				return true;
 			}
 			boolean isPlayer   = sender instanceof Player;
 			String msgUseHelp  = ChatColor.RED+"Invalid command usage or no permission, use '/email help' for help.";
 			String opt = args[0];
 			if(opt.equalsIgnoreCase("help")) {
 				sender.sendMessage(ChatColor.GREEN+"~~~ Start Email help ~~~");
 				sender.sendMessage(ChatColor.AQUA+"To view plugin information: ");
 				sender.sendMessage(ChatColor.YELLOW+"/email info");
 				if(sender.hasPermission("Email.set")) {
 					sender.sendMessage(ChatColor.AQUA+"To set your email (players only): ");
 					sender.sendMessage(ChatColor.YELLOW+"/email set youremail@website.com");
 				}
 				if(sender.hasPermission("Email.set.others")) {
 					sender.sendMessage(ChatColor.AQUA+"To set a player's email: ");
 					sender.sendMessage(ChatColor.YELLOW+"/email set <player name> youremail@website.com");
 				}
 				if(sender.hasPermission("Email.remove")) {
 					sender.sendMessage(ChatColor.AQUA+"To remove your email (players only): ");
 					sender.sendMessage(ChatColor.YELLOW+"/email remove");
 				}
 				if(sender.hasPermission("Email.remove.others")) {
 					sender.sendMessage(ChatColor.AQUA+"To remove a player's email: ");
 					sender.sendMessage(ChatColor.YELLOW+"/email remove <player name>");
 				}
 				if(sender.hasPermission("Email.view")) {
 					sender.sendMessage(ChatColor.AQUA+"To view your email (players only): ");
 					sender.sendMessage(ChatColor.YELLOW+"/email view");
 				}
 				if(sender.hasPermission("Email.view.others")) {
 					sender.sendMessage(ChatColor.AQUA+"To view a player's email: ");
 					sender.sendMessage(ChatColor.YELLOW+"/email view <player name>");
 				}
 				if(sender.hasPermission("Email.send")) {
 					sender.sendMessage(ChatColor.AQUA+"To send an email to a specific player: ");
 					sender.sendMessage(ChatColor.YELLOW+"/email send <player name>");
 					sender.sendMessage(ChatColor.YELLOW+"(must be holding a written book)");
 				}
 				if(sender.hasPermission("Email.send.all")) {
 					sender.sendMessage(ChatColor.AQUA+"To send an email to a ALL players: ");
 					sender.sendMessage(ChatColor.YELLOW+"/email send");
 					sender.sendMessage(ChatColor.YELLOW+"(must be holding a written book)");
 				}
 				if(sender.hasPermission("Email.export")) {
 					sender.sendMessage(ChatColor.AQUA+"To export emails to a text file: ");
 					sender.sendMessage(ChatColor.YELLOW+"/email export <type, either 1 or 2>");
 					sender.sendMessage(ChatColor.YELLOW+"Type 1 will output names and emails, type 2 will only export emails.");
 				}
 				sender.sendMessage(ChatColor.GREEN+"~~~ End Email help ~~~");
 				return true;
 			} else if(opt.equalsIgnoreCase("set")) {
 				if(args.length == 2 && isPlayer && sender.hasPermission("Email.set")) {
 					boolean result = plugin.emails.setPlayerEmail(sender.getName(), args[1]);
 					sender.sendMessage((result) ? ChatColor.GREEN+"Email set" : ChatColor.RED+"Invalid email");
 					return true;
 				} else if(args.length == 3 && sender.hasPermission("Email.set.others")) {
 					boolean result = plugin.emails.setPlayerEmail(args[1], args[2]);
 					sender.sendMessage((result) ? ChatColor.GREEN+"Email set" : ChatColor.RED+"Invalid email");
 					return true;
 				} else {
 					sender.sendMessage(msgUseHelp);
 					return true;
 				}
 			} else if(opt.equalsIgnoreCase("remove")) {
 				if(args.length == 1 && isPlayer && sender.hasPermission("Email.remove")) {
 					plugin.emails.removePlayerEmail(sender.getName());
 					sender.sendMessage(ChatColor.GREEN+"Email removed");
 					return true;
 				} else if(args.length == 2 && sender.hasPermission("Email.remove.others")) {
 					plugin.emails.removePlayerEmail(args[1]);
 					sender.sendMessage(ChatColor.GREEN+"Email removed");
 					return true;
 				} else {
 					sender.sendMessage(msgUseHelp);
 					return true;
 				}
 			} else if(opt.equalsIgnoreCase("view")) {
 				if(args.length == 1 && isPlayer && sender.hasPermission("Email.view")) {
 					String email = plugin.emails.getPlayerEmail(sender.getName());
 					if(email != null) {
 						sender.sendMessage(ChatColor.GREEN+"The email is: "+ChatColor.YELLOW+email);
 					} else {
 						sender.sendMessage(ChatColor.RED+"You don't have an email set");
 					}
 					return true;
 				} else if(args.length == 2 && sender.hasPermission("Email.view.others")) {
 					String email = plugin.emails.getPlayerEmail(args[1]);
 					if(email != null) {
 						sender.sendMessage(ChatColor.GREEN+"The email is: "+ChatColor.YELLOW+email);
 					} else {
 						sender.sendMessage(ChatColor.RED+"That player does not have an email set");
 					}
 					return true;
 				} else {
 					sender.sendMessage(msgUseHelp);
 					return true;
 				}
 			} else if(opt.equalsIgnoreCase("send")) {
 				if(plugin.mailman == null) {
 					sender.sendMessage(ChatColor.RED+"Email sending is disabled on this server!");
 					return true;
 				}
 				if(!isPlayer) {
 					sender.sendMessage(ChatColor.RED+"Sorry, only players can do that.");
 					return true;
 				}
				if(!(args.length == 1 || args.length == 2)) {
 					sender.sendMessage(msgUseHelp);
 					return true;
 				}
 				boolean allPlayers = args.length == 1;
 				if((allPlayers && sender.hasPermission("Email.send.all")) || (!allPlayers && sender.hasPermission("Email.send"))) {
 					//Get itemstack in hand, quit if it's not a written book
 					Player p = (Player)sender;
 					ItemStack hand = p.getItemInHand();
 					if(hand.getType() != Material.WRITTEN_BOOK) {
 						sender.sendMessage(ChatColor.RED+"You must be holding a written book to do that!");
 						return true;
 					}
 					
 					//Set appropriate recipient string
 					//If all players, then toEmail will be a CSV string (ex. email1@example.com,email2@example.com)
 					String toEmail = "";
 					if(allPlayers) {
 						String[] emailArray = plugin.emails.getAllPlayerEmails();
 						for(String email : emailArray) {
 							toEmail += ","+email;
 						}
 						toEmail = toEmail.substring(1);
 					} else {
 						toEmail = plugin.emails.getPlayerEmail(args[1]);
 					}
 					
 					//Can't have that!
 					if(toEmail.isEmpty()) {
 						sender.sendMessage(ChatColor.RED+"That player has not set their email!");
 						return true;
 					}
 					
 					//Get the book's metadata
 					BookMeta data = (BookMeta)hand.getItemMeta();
 					
 					//The email's subject
 					String emailSubject = data.getTitle();
 					
 					//The email's body
 					String emailContent = "";
 					for(String page : data.getPages()) {
 						emailContent += " "+page;
 					}
 					
 					//Remove the extra space
 					emailContent = emailContent.substring(1);
 					
 					//Send the email! :)
 					Bukkit.getScheduler().runTaskAsynchronously(plugin, new EmailTask(plugin.mailman, toEmail, emailSubject, emailContent));
                    sender.sendMessage(ChatColor.GREEN+"Email is being sent! It should be received soon.");
 				} else {
 					sender.sendMessage(msgUseHelp);
 					return true;
 				}
 			} else if(opt.equalsIgnoreCase("export")) {
 				if(args.length == 2 && sender.hasPermission("Email.export")) {
 					if(args[1].equalsIgnoreCase("1")) {
 						plugin.emails.export(1);
 						sender.sendMessage(ChatColor.GREEN+"Emails and names exported");
 						return true;
 					} else if(args[1].equalsIgnoreCase("2")) {
 						plugin.emails.export(2);
 						sender.sendMessage(ChatColor.GREEN+"Emails exported");
 						return true;
 					} else {
 						sender.sendMessage(ChatColor.RED+"Incorrect type, must be 1 or 2.");
 					}
 				} else {
 					sender.sendMessage(msgUseHelp);
 					return true;
 				}
 			} else if(opt.equalsIgnoreCase("info")) {
 				PluginDescriptionFile pdf = plugin.getDescription();
 				String name = ChatColor.YELLOW+pdf.getName()+ChatColor.AQUA;
 				String version = ChatColor.YELLOW+pdf.getVersion()+ChatColor.AQUA;
 				String author = ChatColor.YELLOW+pdf.getAuthors().get(0)+ChatColor.AQUA;
 				sender.sendMessage(name+" version "+version+" by "+author+" is "+ChatColor.GREEN+"running.");
 				return true;
 			}
 			sender.sendMessage(msgUseHelp);
 			return true;
 		}
 		return false;
 	}
 
 }
