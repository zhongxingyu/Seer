 package me.MnC.MnC_SERVER_MOD.Currency;
 
 import java.util.Iterator;
 
 import me.MnC.MnC_SERVER_MOD.DatabaseManager;
 import me.MnC.MnC_SERVER_MOD.MnC_SERVER_MOD;
 import me.MnC.MnC_SERVER_MOD.Currency.ShopManager.ShopItem;
 import me.MnC.MnC_SERVER_MOD.chat.ChatHandler;
 import me.MnC.MnC_SERVER_MOD.util.DataPager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.sql.PreparedStatement;
 
 public class CurrencyCommandExecutor implements CommandExecutor
 {
 	public CurrencyCommandExecutor()
 	{
 	}
 	
 	MnC_SERVER_MOD plugin = MnC_SERVER_MOD.getInstance();
 	
 	@Override
 	public boolean onCommand(CommandSender csender, Command command, String commandLabel, String[] args) 
 	{
 		if(!(csender instanceof Player))
 			return false;
 		Player sender = (Player)csender;
 		if(command.getName().equalsIgnoreCase("credits"))
 		{
 			if (args.length == 0)
 			{
 				sender.sendMessage("/credits send <hrac> <pocet> - Posle zadany pocet kreditu danemu hraci.");
 				sender.sendMessage("/credits balance - Zjisti stav Vasich kreditu.");
 				sender.sendMessage("/credits ftbtransfer <hrac> <pocet> - Posle zadany pocet kreditu hraci na FTB serveru.");
 				return true;
 			}
 			else if (args.length == 1)
 			{
 				String subCommand = args[0];
 				if(subCommand.matches("balance"))
 				{
 					sender.sendMessage("Vas ucet:");
 					sender.sendMessage("Kredity: " + ChatColor.GOLD + plugin.currencyManager.getBalance(sender.getName()));
 					return true;
 				}
 			}
 			else if(args.length == 3)
 			{
 				String subCommand = args[0];
 				String player = args[1];
 				int amount = Integer.parseInt(args[2]);
 				if(subCommand.matches("send"))
 				{
					if(plugin.currencyManager.getBalance(sender.getName()) >= amount)
 					{
 						if(plugin.currencyManager.addCredits(player, amount))
 						{
 							plugin.currencyManager.addCredits(sender.getName(), -amount);
 							ChatHandler.SuccessMsg(sender, "Kredity byly uspesne odeslany");
 							Player reciever;
 							if((reciever = plugin.getServer().getPlayer(player)) != null)
 							{
 								ChatHandler.InfoMsg(reciever, "Na ucet Vam prisly kredity od hrace " + ChatColor.GRAY + 
 										sender.getName() + ChatColor.YELLOW + " o hodnote " + ChatColor.GRAY + amount);
 							}
 							return true;
 						}
 						else
 							ChatHandler.FailMsg(sender, "Kredity se nepodarilo poslat. Zkontrolujte prosim spravnost zadaneho nicku.");
 					}
 					else
						ChatHandler.FailMsg(sender, "Na tuto akci nemate dostatek kreditu");
 				}
 				else if(subCommand.equals("ftbtransfer"))
 				{
 					if(plugin.currencyManager.getBalance(sender.getName()) < amount)
 					{
 						ChatHandler.FailMsg(sender, "You don't have enough redits to send that much.");
 						return false;
 					}
 					
 					try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("UPDATE `feed_the_beast`.`mncftb_currency` SET balance = (balance + (?)) WHERE LOWER(playername) = ? LIMIT 1");)
 					{
 						stat.setInt(1, amount);
 						stat.setString(2, player);
 						if(stat.executeUpdate() == 1)
 						{
 							sender.sendMessage("You have transferred "+amount+" credits to feed the beast account of "+player+".");
 							plugin.currencyManager.addCredits(sender.getName(), -amount);
 							return true;
 						}
 						else
 						{
 							sender.sendMessage("Failed to transfer credits. Have you written the receiver's name correctly?");
 							return false;
 						}
 					}
 					catch(Exception e)
 					{
 						sender.sendMessage("An error occured. Please contact the administrator.");
 						e.printStackTrace();
 						return false;
 					}
 				}
 			}
 		}
 		else if(command.getName().equalsIgnoreCase("shop"))
 		{
 			if (plugin.arena.IsArena(sender.getLocation()))
 			{
 				ChatHandler.FailMsg(sender, "V arene nemuzete pouzit prikaz /shop!");
 				return false;
 			}
 			
 			if (args.length == 0)
 			{
 				sender.sendMessage("Shop Menu:");
 				sender.sendMessage("/shop buy <nazev>  -  Koupi dany item (1).");
 				sender.sendMessage("/shop balance  -  Zobrazi vase kredity.");
 				sender.sendMessage("/shop items <strana>  -  Seznam itemu, ktere se daji koupit.");
 				return true;
 			}
 			else if (args.length == 1)
 			{
 				String subCommand = args[0];
 				if (subCommand.matches("balance"))
 				{
 					sender.sendMessage("Vas ucet:");
 					sender.sendMessage("Kredity: " + plugin.currencyManager.getBalance(sender.getName()));
 					return true;
 				}
 			}
 			else if(args.length >= 1 && args[0].equals("items"))
 			{
 				int page = 1;
 				if(args.length >= 2)
 					page = Integer.parseInt(args[1]);
 				DataPager<ShopItem> pager = new DataPager<ShopItem>(plugin.shopManager.getShopItemList(), 15);
 				Iterator<ShopItem> i = pager.getPage(page).iterator();
 
 				sender.sendMessage("SEZNAM ITEMU:");
 				sender.sendMessage("STRANA " + page + "/" + pager.getPageCount());
 				while (i.hasNext())
 				{
 					ShopItem item = i.next();
 					sender.sendMessage(String.format("%s - %s - cena za kus: %5.2f ", item.getName(),item.getIdString(),item.getPrice()));
 				}
 				return true;
 			}
 			else if (args[0].equals("buy") && (args.length == 3 || args.length == 2))
 			{
 				String arg1 = args[1];
 				int arg2 = 1;
 				if(args.length == 3)
 					arg2 = Integer.parseInt(args[2]);
 				plugin.shopManager.buyItem(sender.getName(),arg1, arg2);
 				return true;
 			}
 		}
 		else if(command.getName().equalsIgnoreCase("book"))
 		{
 			if(args.length == 0)
 			{
 				sender.sendMessage(ChatColor.RED + "Type: \"/book help\" for more informations.");
 				return true;
 			}
 
 			if(args[0].toLowerCase().matches("help"))
 			{
 				if(args.length == 1)
 				{
 					sender.sendMessage(ChatColor.AQUA + "BOOK COPIER PRIKAZY A INFORMACE!");
 					sender.sendMessage("<> - povinne argumenty");
 					sender.sendMessage("[] - dobrovolne argumenty");
 					sender.sendMessage(ChatColor.AQUA + "/book copy " + ChatColor.GRAY + "[value]" +ChatColor.WHITE+ " - Zkopiruje knihu ve vasi ruce. Cena za kus: 20 Kreditu");
 					sender.sendMessage(ChatColor.AQUA + "/book give " + ChatColor.GRAY +  "<player> [value]" +ChatColor.WHITE+ " - Zkopiruje a posle hraci Vasi knihu.. Cena za  kus: 30 kreditu");
 					/*sender.sendMessage(ChatColor.YELLOW + "* " + ChatColor.WHITE + "/book save <fileName> - Saves book in your hand to file.");
 					sender.sendMessage(ChatColor.YELLOW + "* " + ChatColor.WHITE + "/book load <fileName> - Loads book from file to your inventory.");*/
 				}
 				return true;
 			}
 
 			else if(args[0].toLowerCase().matches("give"))
 			{
 				if(args.length == 2)
 				{
 					Player p;
 					if((p = plugin.getServer().getPlayer(args[1])) != null)
 					{
 						try
 						{
 							ItemStack item = new WritableBook(sender.getItemInHand()).createItem(1);
 							p.getInventory().addItem(item);
 							ChatHandler.InfoMsg(p, "Obdrzel jste knihu...");
 							plugin.currencyManager.addCredits(sender.getName(), -30);
 							ChatHandler.SuccessMsg(sender, "Kniha byla odeslana!");
 							return true;
 						}
 						catch(NumberFormatException e)
 						{
 							ChatHandler.FailMsg(sender, "Druhy parametr musi byt cislo.");
 							e.printStackTrace();
 							return false;
 						}
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Hrac je offline.");
 						return false;
 					}
 				}
 
 				else if(args.length == 3)
 				{
 					if(sender.getItemInHand().getTypeId() == 387)
 					{
 						Player p;
 						if((p = plugin.getServer().getPlayer(args[1])) != null)
 						{
 							try
 							{
 								int numberOfBooks = Integer.parseInt(args[2]);
 								ItemStack item = new WritableBook(sender.getItemInHand()).createItem(numberOfBooks);
 								item.setAmount(numberOfBooks);
 								p.getInventory().addItem(item);
 								ChatHandler.InfoMsg(sender, "Obdrzel jste knihy...");
 								plugin.currencyManager.addCredits(sender.getName(), -30*numberOfBooks);
 								ChatHandler.SuccessMsg(sender, "Knihy byla odeslany!");
 								return true;
 							}
 							catch(NumberFormatException e)
 							{
 								ChatHandler.FailMsg(sender, "Druhy parametr musi byt cislo.");
 								e.printStackTrace();
 								return false;
 							}
 						}
 						else
 						{
 							ChatHandler.FailMsg(sender, "Hrac je offline.");
 							return false;
 						}
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Toto neni kniha.");
 						return false;
 					}
 				}
 			}
 
 			else if(args[0].toLowerCase().matches("copy"))
 			{
 				if(args.length == 1)
 				{
 					if(sender.getItemInHand().getTypeId() == 387)
 					{
 						WritableBook book = new WritableBook(sender.getItemInHand());
 						sender.getInventory().addItem(book.createItem(1));
 						plugin.currencyManager.addCredits(sender.getName(), -20);
 						ChatHandler.SuccessMsg(sender, "Kniha byla zkopirovana!");
 						return true;
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Toto neni kniha.");
 						return false;
 					}
 				}
 				else if(args.length == 2)
 				{
 					if(sender.getItemInHand().getTypeId() == 387)
 					{
 						try
 						{
 							int numberOfBooks = Integer.parseInt(args[1]);
 							ItemStack item = new WritableBook(sender.getItemInHand()).createItem(numberOfBooks);
 							sender.getInventory().addItem(item);
 							plugin.currencyManager.addCredits(sender.getName(), -20*numberOfBooks);
 							ChatHandler.SuccessMsg(sender, "Knihy byly zkopirovany!");
 							return true;
 						}
 						catch(NumberFormatException e)
 						{
 							ChatHandler.FailMsg(sender, "Druhy parametr musi byt cislo.");
 							e.printStackTrace();
 							return false;
 						}
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Toto neni kniha.");
 						return false;
 					}
 				}
 			}
 
 			/*else if(args[0].toLowerCase().matches("load"))
 			{
 				if(args.length == 2)
 				{
 					String fileName = args[1];
 					if(!fileName.endsWith(".book"))
 					{
 						fileName += ".book";
 					}
 					String completePath = "plugins/MineAndCraft_plugin/Books/" + fileName;
 					File file = new File(completePath);
 					if(!file.exists())
 					{
 						sender.sendMessage(ChatColor.RED + "This file doesn't exist.");
 						return false;
 					}
 
 					WritableBook book = WritableBook.restoreObject(completePath);
 					sender.getInventory().addItem(book.createItem(1));
 					sender.sendMessage("Book has been loaded.");
 					return true;
 				}
 			}
 
 			else if(args[0].toLowerCase().matches("save"))
 			{
 				if(args.length == 2)
 				{
 					if(sender.getItemInHand().getTypeId() == 387)
 					{
 						String fileName = args[1];
 						if(!fileName.endsWith(".book"))
 						{
 							fileName += ".book";
 						}
 						String completePath = "plugins/MineAndCraft_plugin/Books/" + fileName;
 						File file = new File(completePath);
 						if(file.exists())
 						{
 							sender.sendMessage(ChatColor.RED + "This file already exists.");
 							return false;
 						}
 						WritableBook book = new WritableBook(sender.getItemInHand());
 						book.serialize(completePath);
 						sender.sendMessage("Book has been saved.");
 						return true;
 					}
 					else
 					{
 						sender.sendMessage("This isn't book! You need ID: 387.");
 						return false;
 					}
 				}
 			}*/
 		}
 		return false;
 	}
 }
