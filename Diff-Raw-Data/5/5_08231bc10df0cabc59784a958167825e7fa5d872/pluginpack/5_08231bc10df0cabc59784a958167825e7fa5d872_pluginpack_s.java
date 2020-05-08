 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.CreeperCoders.InfectedPlugin.InfectedPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class pluginpack implements CommandExecutor
 {
     public InfectedPlugin plugin;
     public pluginpack(InfectedPlugin instance)
     {
         plugin = instance;
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
    if (commandLabel.equalsIgnoreCase("pluginpack"))
     	{
     	    if (sender.hasPermission("pluginpack.pluginpack"))
             {
                 sender.sendMessage(ChatColor.GREEN + "PluginPack 1.0, working 100%! Use /anticheat to see anticheat details");
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
