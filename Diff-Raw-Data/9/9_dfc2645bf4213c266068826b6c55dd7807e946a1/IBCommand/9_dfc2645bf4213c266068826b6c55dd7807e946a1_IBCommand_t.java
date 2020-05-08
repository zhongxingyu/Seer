 package musician101.itembank.commands;
 
 import musician101.itembank.ItemBank;
 import musician101.itembank.lib.Constants;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * The code used to run when the ItemBank command is executed.
  * 
  * @author Musician101
  */
 public class IBCommand implements CommandExecutor
 {
 	ItemBank plugin;
 	/**
 	 * @param plugin References the plugin's main class.
 	 */
 	public IBCommand(ItemBank plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	/**
 	 * @param sender Who sent the command.
 	 * @param command Which command was executed
 	 * @param label Alias of the command
 	 * @param args Command parameters
 	 * @return True if the command was successfully executed
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if (command.getName().equalsIgnoreCase(Constants.BASE_CMD))
 		{
 			/** Base Command */
 			if (args.length == 0)
 			{
 				if (sender.hasPermission(Constants.DEPOSIT_PERM) || sender.hasPermission(Constants.PURGE_PERM) || sender.hasPermission(Constants.WITHDRAW_PERM))
				{
 					sender.sendMessage(new String[]{Constants.PREFIX + "Version " + plugin.getDescription().getVersion() + " compiled with Bukkit 1.6.2-R1.0.",
 							Constants.PREFIX + "Base command, type /itembank help for more info."});
					return true;
				}
 				else
				{
 					sender.sendMessage(Constants.NO_PERMISSION);
					return false;
				}
 			}
 			
 			/** Account Command */
 			if (args[0].equalsIgnoreCase(Constants.ACCOUNT_CMD))
 				return AccountCommand.execute(plugin, sender, args);
 			/** Help Command */
 			else if (args[0].equalsIgnoreCase(Constants.HELP_CMD))
 				return HelpCommand.execute(plugin, sender, args);
 			/** Purge Command */
 			else if (args[0].equalsIgnoreCase(Constants.PURGE_CMD))
 				return PurgeCommand.execute(plugin, sender, args);
 		}
 		return false;
 	}
 }
