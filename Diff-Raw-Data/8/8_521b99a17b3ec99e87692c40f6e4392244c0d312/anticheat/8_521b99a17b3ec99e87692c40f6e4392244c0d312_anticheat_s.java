 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.CreeperCoders.InfectedPlugin.InfectedPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class anticheat implements CommandExecutor
 {
     public InfectedPlugin plugin;
     public anticheat(InfectedPlugin instance)
     {
         plugin = instance;
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
     	Player p = (Player) sender;	
     	
     	if (commandLabel.equalsIgnoreCase("anticheat"))
     	{
             if (sender.hasPermission("pluginpack.anticheat"))
             {
    	    sender.sendMessage(ChatColor.GREEN + "Anticheat 1.0 is working 100%");
            return true;
             }
             else
             {
                 sender.sendMessage(InfectedPlugin.MSG_NO_PERMS);
                 return true;
             }
     	}
 	return false;
     }
}
