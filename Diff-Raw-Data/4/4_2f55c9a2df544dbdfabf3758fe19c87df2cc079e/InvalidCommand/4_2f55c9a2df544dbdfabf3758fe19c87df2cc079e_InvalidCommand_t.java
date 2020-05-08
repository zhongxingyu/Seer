 package info.plugmania.origin_core.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import info.plugmania.origin_core.Origin_Core;
 
 public class InvalidCommand extends Command {
 	public InvalidCommand(Origin_Core instance) {
 		super(instance);
 	}
 
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String lbl, String[] args) {
 		sender.sendMessage(ChatColor.RED + lbl + " is not a valid command!");
 		return true;
 	}
 }
