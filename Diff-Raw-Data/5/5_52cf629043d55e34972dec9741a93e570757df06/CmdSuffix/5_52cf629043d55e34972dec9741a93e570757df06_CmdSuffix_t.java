 /**
  * (c) 2013 dmulloy2
  */
 package net.dmulloy2.suffixesplus.commands;
 
 import net.dmulloy2.suffixesplus.SuffixesPlus;
 import net.dmulloy2.suffixesplus.util.Util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 
 /**
  * @author dmulloy2
  */
 
 public class CmdSuffix implements CommandExecutor
 {	
 	public SuffixesPlus plugin;
 	public CmdSuffix(SuffixesPlus plugin)  
 	{
 		this.plugin = plugin;
 	}
 	  
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 1)
 		{
 			if (sender instanceof Player)
 			{
 				String argscheck = args[0].toString().replaceAll("(?i)&([a-f0-9])", "").replaceAll("&", "").replaceAll("\\[", "").replaceAll("\\]", "");
 				if (argscheck.length() <= 10)
 				{
 					String newSuffix = args[0].toString();
 					ConsoleCommandSender ccs = plugin.getServer().getConsoleSender();
 					PluginManager pm = plugin.getServer().getPluginManager();
 					if (pm.isPluginEnabled("GroupManager"))
 					{
 						plugin.getServer().dispatchCommand(ccs, "manuaddv " + sender.getName() + " suffix " + newSuffix + "&r");
 						sender.sendMessage(ChatColor.AQUA + "Your suffix is now '" + newSuffix + "'");
 					}
 					else if (pm.isPluginEnabled("PermissionsEx"))
 					{
 						plugin.getServer().dispatchCommand(ccs, "pex user " + sender.getName() + " suffix \"" + newSuffix + "\"&r");
 						sender.sendMessage(ChatColor.AQUA + "Your suffix is now '" + newSuffix + "'");
 					}
 					else
 					{
 						sender.sendMessage(ChatColor.RED + "Neither GroupManager nor PEX was found.");
 					}
 				}
 				else
 				{
					sender.sendMessage(ChatColor.RED + "Error, your suffix is too long (Max 10 characters)");
 				}
 			}
 			else
 			{
				sender.sendMessage(ChatColor.RED + "Console cannot have a suffix!");
 			}
 		}
 		else if (args.length == 2)
 		{
 			if (sender.hasPermission("sp.others"))
 			{
 				String newSuffix = args[1].toString();
 				Player target = Util.matchPlayer(args[0]);
 				ConsoleCommandSender ccs = plugin.getServer().getConsoleSender();
 				PluginManager pm = plugin.getServer().getPluginManager();
 				if (target != null)
 				{
 					if (pm.isPluginEnabled("GroupManager"))
 					{
 						plugin.getServer().dispatchCommand(ccs, "manuaddv " + target.getName() + " suffix " + newSuffix + "&r");
 						sender.sendMessage(ChatColor.AQUA + target.getName() + "'s suffix is now " + newSuffix);
 						target.sendMessage(ChatColor.AQUA + "Your suffix is now '" + newSuffix + "'");
 					}
 					else if (pm.isPluginEnabled("PermissionsEx"))
 					{
 						plugin.getServer().dispatchCommand(ccs, "pex user " + target.getName() + " suffix \"" + newSuffix + "\"&r");
 						sender.sendMessage(ChatColor.AQUA + target.getName() + "'s suffix is now " + newSuffix);
 						target.sendMessage(ChatColor.AQUA + "Your suffix is now '" + newSuffix + "'");	  
 					}
 					else
 					{
 						sender.sendMessage(ChatColor.RED + "Neither PEX nor GroupManager was found");
 					}
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "Player not found");
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "You do not have permission to preform this command");
 			}
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "Invaild arguments count (/suf [player] <suffix>)");
 		}
 		return true;
 	}
 }
