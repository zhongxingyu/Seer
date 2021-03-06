 package com.github.zathrus_writer.commandsex.commands;
 
 import static com.github.zathrus_writer.commandsex.Language._;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.zathrus_writer.commandsex.Vault;
 import com.github.zathrus_writer.commandsex.helpers.Commands;
 import com.github.zathrus_writer.commandsex.helpers.LogHelper;
 
 public class Command_cex_setrank {
 	
 	/***
 	 * SETRANK - sets a players rank
 	 * @param sender
 	 * @param args
 	 * @return
 	 */
 	
 	public static Boolean run(CommandSender sender, String alias, String[] args) {
 		
 		// Check for Vault/Permissions
 		if(Vault.permsEnabled() != true) {
 			 LogHelper.logSevere(_("permissionsNotFound", ""));
 			 LogHelper.showWarning("permissionsNotFound", sender);
 		}
 		
 		// Check they have specified a player
		if(args.length==0) {
 			Commands.showCommandHelpAndUsage(sender, "cex_setrank", alias);
 		}
 		
 		// Command variables
 		Player player = Bukkit.getServer().getPlayerExact(args[0]);
 		String group = args[1];
 		
 		// Set group
 		Vault.perms.playerAddGroup(player, group);
 		
 		// Notify sender and player
 		LogHelper.showInfo("[" + player + "setrankToSender#####[" + group, sender);
 		LogHelper.showInfo("setrankToPlayer#####[" + group, player);
 		
 		return true;
 	}
 }
