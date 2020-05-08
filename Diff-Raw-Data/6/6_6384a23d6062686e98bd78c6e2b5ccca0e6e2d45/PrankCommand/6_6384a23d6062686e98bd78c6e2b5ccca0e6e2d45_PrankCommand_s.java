 package me.spike.prank;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /***
  * Command handler which takes care of handling user input.
  * @author Andrew
  *
  */
 public class PrankCommand implements CommandExecutor {	
 	private final static PrankCommand instance = new PrankCommand();
 	private final ConfigurationManager config = ConfigurationManager.getConfigurationManager();
 
 	private PrankCommand(){}
 
 	public static PrankCommand getPrankExecutor() {
 		return instance;
 	}
 
 	/**
 	 * Reacts to users commands. The first argument must always
 	 * be "prank", with the subarguments specifying which prank
 	 * is to be applied to which player.
 	 * Commands handled:
 	 * 		- creeper
 	 * 		- toolswitch
 	 */
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
 		// If there is no prank and no player specified, do nothing
 		if (split.length < 2) {
 			return false;
 		}
 		
 		String prank = split[0].toLowerCase();
 		String target = split[1].toLowerCase();
 
 		// Turn on the specified prank for the player
 		if (checkPerm(sender, prank) && checkPrank(prank)) {
 			if (command.getName().equalsIgnoreCase("prank")) {
 				config.turnPrankOn(target, prank);
 				sender.sendMessage(target + " is now being " + prank + " pranked!");
 			} else if (command.getName().equalsIgnoreCase("unprank")) {
 				config.turnPrankOff(target, prank);
 				sender.sendMessage(target + " is no longer being " + prank + " pranked!");
 			}
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Check if the sender has permission to execute the given command.
 	 * @param sender The sender of the command
 	 * @param subnode The name of the permission
 	 * @return If the user has permission, return true
 	 */
 	private boolean checkPerm(CommandSender sender, String subnode) {
 		boolean permission = sender.hasPermission("prank." + subnode);
 		if (!permission) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission to do that (" + subnode + ").");
 		}
 		return permission;
 	}
 
 	private boolean checkPrank(String prank) {
 		if (prank.equalsIgnoreCase("creeper") ||
			prank.equalsIgnoreCase("toolswitch"))
 			return true;

 		return false;
 	}
 }
