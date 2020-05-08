 package net.milkycraft;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Commander implements CommandExecutor
 {
 	// Class variables
 	private Banlisting plugin;
 	//private BanConfiguration config;
 	private final Map<String, Integer> page = new HashMap<String, Integer>();
 	private final static String bar = "======================";
 
 	/*public Commander(Banlisting pb, BanConfiguration c)
 	{
 		plugin = pb;
 		config = c;
 	}*/
 	public Commander(Banlisting plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandlabel, String[] args)
 	{
 		if (args.length == 0 && sender.hasPermission("banlisting.bans"))
 		{
			listBanned(sender, 0);
 			try
 			{
 				int pageNum = Integer.parseInt(args[0]);
 				page.put(sender.getName(), pageNum - 1);
				this.listBanned(sender, 0);
 			}
 			catch (NumberFormatException e)
 			{
 				return false;
 			}
 			return true;
 		}
 		else if (sender.hasPermission("banlisting.bans"))
 		{
 			if (args[0].equalsIgnoreCase("help"))
 			{
				this.displayHelp(sender);
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("version"))
 			{
				this.showVersion(sender);
 
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("reload"))
 			{
 				plugin.reloadConfig();
 				sender.sendMessage(ChatColor.AQUA + Banlisting.prefix
 						+ ChatColor.GREEN
 						+ " Config has been sucessfully reloaded!");
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("prev"))
 			{
				this.listBanned(sender, -1);
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("next"))
 			{
				this.listBanned(sender, 1);
 				return true;
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED
 						+ " Syntax error make sure you type the command right");
 				return false;
 			}
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.RED + Banlisting.prefix + " Lack Permission: banlisting.bans");
 			return true;
 		}
 	}
 
	private void displayHelp(CommandSender sender)
 	{
 		sender.sendMessage(ChatColor.BLUE + "=====" + ChatColor.RED
 				+ "BanListing" + ChatColor.BLUE + "=====");
 		if (sender.hasPermission("banlisting.bans"))
 		{
 			sender.sendMessage(ChatColor.GREEN + "/bannedplayers [#]"
 					+ ChatColor.YELLOW + " : Show banned players, alias: "
 					+ ChatColor.AQUA + "/blist");
 			sender.sendMessage(ChatColor.GREEN + "/bannedplayers prev"
 					+ ChatColor.YELLOW + " : Show previous page");
 			sender.sendMessage(ChatColor.GREEN + "/bannedplayers next"
 					+ ChatColor.YELLOW + " : Show next page");
 		}
 	}
 
 	/**
 	 * Shows plugin version and config
 	 * 
 	 * @param Player
 	 *            that sent the message
 	 * @param
 	 */
	private void showVersion(CommandSender sender)
 	{
 		sender.sendMessage(ChatColor.BLUE + bar + "=====");
 		sender.sendMessage(ChatColor.RED + "BanListing v"
 				+ plugin.getDescription().getVersion());
 		sender.sendMessage(ChatColor.GREEN + "Coded by milkywayz");
 		sender.sendMessage(ChatColor.GREEN + "Formally bobbysmithyy");
 		sender.sendMessage(ChatColor.BLUE + "===========" + ChatColor.GRAY
 				+ "Config" + ChatColor.BLUE + "===========");
 		sender.sendMessage(ChatColor.GRAY + "Page entry limit: "
 				+ plugin.getConfig().getInt("limit"));
 		if (plugin.getConfig().getBoolean("limit"))
 		{
 			sender.sendMessage(ChatColor.GRAY + "Logging enabled");
 		}
 	}
 
 	/**
 	 * Lists banned players in a paginated way
 	 * 
 	 * @param player
 	 *            to show list to
 	 * @param page
 	 *            adjustment
 	 */
	private void listBanned(CommandSender player, int pageAdjust)
 	{
 		// Generate array
 		final OfflinePlayer[] array = Bukkit.getBannedPlayers().toArray(
 				new OfflinePlayer[0]);
 		// Add player if they don't exist
 		if (!page.containsKey(player.getName()))
 		{
 			page.put(player.getName(), 0);
 		}
 		else
 		{
 			// They already exist, so adjust if necessary
 			if (pageAdjust != 0)
 			{
 				int adj = page.get(player.getName()).intValue() + pageAdjust;
 				page.put(player.getName(), adj);
 			}
 		}
 		if (array.length <= 0)
 		{
 			// No players in ban list
 			player.sendMessage(ChatColor.YELLOW + Banlisting.prefix
 					+ " No banned players");
 			return;
 		}
 		boolean valid = true;
 		final int limit = plugin.getConfig().getInt("limit");
 		// Caluclate amount of pages
 		int num = array.length / limit;
 		final double rem = (double) array.length % (double) limit;
 		if (rem != 0)
 		{
 			num++;
 		}
 		if (page.get(player.getName()).intValue() < 0)
 		{
 			// They tried to use prev when they're on page 0
 			player.sendMessage(ChatColor.YELLOW + Banlisting.prefix
 					+ " Page does not exist");
 			// reset their current page back to 0
 			page.put(player.getName(), 0);
 			valid = false;
 		}
 		else if ((page.get(player.getName()).intValue()) * limit > array.length)
 		{
 			// They tried to use next at the end of the list
 			player.sendMessage(ChatColor.YELLOW + Banlisting.prefix
 					+ " Page does not exist");
 			// Revert to last page
 			page.put(player.getName(), num - 1);
 			valid = false;
 		}
 		if (valid)
 		{
 			// Header with amount of pages
 			player.sendMessage(ChatColor.BLUE + "===" + ChatColor.RED
 					+ "Banned List" + ChatColor.BLUE + "===" + ChatColor.WHITE
 					+ "Page: " + ((page.get(player.getName()).intValue()) + 1)
 					+ ChatColor.BLUE + " of " + ChatColor.WHITE + num
 					+ ChatColor.BLUE + "===");
 			// list
 			for (int i = ((page.get(player.getName()).intValue()) * limit); i < ((page
 					.get(player.getName()).intValue()) * limit) + limit; i++)
 			{
 				// Don't try to pull something beyond the bounds
 				if (i < array.length)
 				{
 					final StringBuilder sb = new StringBuilder();
 					sb.append(ChatColor.AQUA + array[i].getName());
 					player.sendMessage(sb.toString());
 				}
 				else
 				{
 					break;
 				}
 			}
 		}
 	}
 
 }
