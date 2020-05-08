 package com.TeamNovus.Supernaturals.Commands;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import com.TeamNovus.Supernaturals.Permission;
 import com.TeamNovus.Supernaturals.Supernaturals;
 
 public class BaseCommandExecutor implements CommandExecutor {
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(args.length == 0) {
			sender.sendMessage(CommandManager.getDark() + "______________[ " + CommandManager.getLight() + Supernaturals.getPlugin().getName() + CommandManager.getDark() + " ]______________");
 			sender.sendMessage(CommandManager.getDark() + "Description: " + CommandManager.getLight() + Supernaturals.getPlugin().getDescription().getDescription());
 			sender.sendMessage(CommandManager.getDark() + "Author: " + CommandManager.getLight() + Supernaturals.getPlugin().getDescription().getAuthors().get(0));
 			sender.sendMessage(CommandManager.getDark() + "Version: " + CommandManager.getLight() + Supernaturals.getPlugin().getDescription().getVersion());
 			sender.sendMessage(CommandManager.getDark() + "Website: " + CommandManager.getLight() + Supernaturals.getPlugin().getDescription().getWebsite());
 			return true;
 		}
 		
 		if(CommandManager.getCommand(args[0]) == null) {
 			sender.sendMessage(CommandManager.getError() + "The specified command was not found!");
 			return true;
 		}
 		
 		BaseCommand command = CommandManager.getCommand(args[0]);
 		Object[] commandArgs = ArrayUtils.remove(args, 0);
 		
 		if(sender instanceof Player && !(command.player())) {
 			sender.sendMessage(CommandManager.getError() + "This command cannot be ran as a player!");
 			return true;
 		}
 		
 		if(sender instanceof ConsoleCommandSender && !(command.console())) {
 			sender.sendMessage(CommandManager.getError() + "This command cannot be ran from the console!");
 			return true;
 		}
 		
 		if(command.permission() != null && !(command.permission().equals(Permission.NONE)) && !(Permission.has(command.permission(), sender))) {
 			sender.sendMessage(CommandManager.getError() + "You do not have permission for this command!");
 			return true;
 		}
 		
 		if((commandArgs.length < command.min()) || (commandArgs.length > command.max() && command.max() != -1)) {
 			sender.sendMessage(CommandManager.getError() + "Usage: /" + commandLabel + " " + command.aliases()[0] + " " + command.usage());
 			return true;
 		}
 		
 		CommandManager.execute(command, sender, cmd, commandLabel, commandArgs);
 		return true;
 	}
 	
 }
