 package BookCopier;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class Commands 
 {
 	public static void setPlugin(BookCopier bc)
 	{
 		plugin = bc;
 	}
 	public static void commandCopy(Player commandSender, String args[])
 	{
 		if(plugin.config.getOPsOnly())
 		{
 			if(!commandSender.isOp())
 				return;
 		}
 		if(args.length == 0)
 		{
 			commandSender.sendMessage(ChatColor.RED + "Type: \"/bc help\" for more informations.");
 		}
 		
 		if(args[0].toLowerCase().matches("help"))
 		{
 			if(args.length == 1)
 			{
 				commandSender.sendMessage(ChatColor.AQUA + "BOOK COPIER COMMANDS AND INFO!");
 				commandSender.sendMessage("<> - required arguments");
 				commandSender.sendMessage("[] - optional arguments");
 				commandSender.sendMessage(ChatColor.YELLOW + "* " + ChatColor.WHITE + "/bc copy [value] - Copies book in your hand.(No value means 1*)");
 				commandSender.sendMessage(ChatColor.YELLOW + "* " + ChatColor.WHITE + "/bc give <player> [value] - Adds book in your hand to target player's inventory.");
 			}
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
 						ItemStack item = new MyBook(commandSender.getItemInHand()).createItem();
 						p.getInventory().addItem(item);
 						p.sendMessage("You recieve some book...");
 						commandSender.sendMessage("Book has been added to target player's inventory!");
 					}
 					catch(NumberFormatException e)
 					{
 						commandSender.sendMessage(ChatColor.RED + "Second argument must be number (Integer)");
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 					commandSender.sendMessage(ChatColor.RED + "Target player is offline");
 				}
 			}
 			
 			else if(args.length == 3)
 			{
 				if(commandSender.getItemInHand().getTypeId() == 387)
 				{
 					Player p;
 					if((p = plugin.getServer().getPlayer(args[1])) != null)
 					{
 						try
 						{
 							int numberOfBooks = Integer.parseInt(args[2]);
 							ItemStack item = new MyBook(commandSender.getItemInHand()).createItem();
 							item.setAmount(numberOfBooks);
 							p.getInventory().addItem(item);
 							p.sendMessage("You recieved some books...");
 							commandSender.sendMessage("Books has been added to target player's inventory!");
 						}
 						catch(NumberFormatException e)
 						{
 							commandSender.sendMessage(ChatColor.RED + "Second argument must be number (Integer)");
 							e.printStackTrace();
 						}
 					}
 					else
 					{
 						commandSender.sendMessage(ChatColor.RED + "Target player is offline");
 					}
 				}
 				else
 				{
 						commandSender.sendMessage("This isn't book! You need ID: 387.");
 				}
 			}
 		}
 		
 		else if(args[0].toLowerCase().matches("copy"))
 		{
 			if(args.length == 1)
 			{
 				if(commandSender.getItemInHand().getTypeId() == 387)
 				{
 					MyBook book = new MyBook(commandSender.getItemInHand());
 					commandSender.getInventory().addItem(book.createItem());
 					commandSender.sendMessage("Book has been copied!");
 				}
 				else
 				{
 					commandSender.sendMessage("This isn't book! You need ID: 387.");
 				}
 			}
 			else if(args.length == 2)
 			{
 				if(commandSender.getItemInHand().getTypeId() == 387)
 				{
 					try
 					{
 						int numberOfBooks = Integer.parseInt(args[1]);
 						ItemStack item = new MyBook(commandSender.getItemInHand()).createItem();
 						item.setAmount(numberOfBooks);
 						commandSender.getInventory().addItem(item);
 						commandSender.sendMessage("Books has been copied!");
 					}
 					catch(NumberFormatException e)
 					{
 						commandSender.sendMessage(ChatColor.RED + "Second argument must be number (Integer)");
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 					commandSender.sendMessage("This isn't book! You need ID: 387.");
 				}
 			}
 		}
 		
 		else if(args[0].toLowerCase().matches("load"))
 		{
 			if(args.length == 2)
 			{
 				String fileName = args[1];
 				if(!fileName.endsWith(".book"))
 				{
 					fileName += ".book";
 				}
 				String completePath = "plugins/BookCopier/" + fileName;
 				File file = new File(completePath);
 				if(!file.exists())
 				{
 					commandSender.sendMessage(ChatColor.RED + "This file doesn't exist.");
 					return;
 				}
 				if(!BookCreator.checkFormat(completePath))
 				{
 					commandSender.sendMessage(ChatColor.RED + "Bad format of target file!");
 					return;
 				}
 				ItemStack item = BookCreator.loadBook(completePath);
 				commandSender.getInventory().addItem(item);
 				commandSender.sendMessage("Book has been loaded.");
 			}
 		}
 		
 		else if(args[0].toLowerCase().matches("save"))
 		{
 			if(args.length == 2)
 			{
 				if(commandSender.getItemInHand().getTypeId() == 387)
 				{
 					String fileName = args[1];
 					if(!fileName.endsWith(".book"))
 					{
 						fileName += ".book";
 					}
 					String completePath = "plugins/BookCopier/" + fileName;
 					File file = new File(completePath);
 					if(file.exists())
 					{
 						commandSender.sendMessage(ChatColor.RED + "This file already exists.");
 						return;
 					}
 					MyBook book = new MyBook(commandSender.getItemInHand());
 					BookCreator.saveBook(book.getTitle(), book.getAuthor(), book.getPages(), completePath);
 					commandSender.sendMessage("Book has been saved.");
 				}
 				else
 				{
 					commandSender.sendMessage("This isn't book! You need ID: 387.");
 				}
 			}
 		}
 	}
 	private static BookCopier plugin;
 }
