 /**
  * This plugin is used to extend the functionality of SimpleClans
  * to make is easier to use in a PvP setting.  
  * 
  * Written by:
  * Ryan Mendivil <http://nullreff.net>
  * 
  * Tower Management By:
  * Dmitri Amariei <https://github.com/damariei>
  * 
  * Some code and ideas borrowed from:
  * 	Scyntrus <http://www.minecraftforum.net/user/474851-scyntrus/>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License.  This program 
  * is distributed in the hope that it will be useful,  but WITHOUT ANY 
  * WARRANTY.  See the GNU General Public License for more details.
  */
 
 package com.barroncraft.sce;
 
 import java.util.logging.Logger;
 
 import net.sacredlabyrinth.phaed.simpleclans.*;
 import net.sacredlabyrinth.phaed.simpleclans.managers.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class SimpleClansExtensions extends JavaPlugin
 {
     private ClanManager clanManager;
     private WorldGuardPlugin guardManager;
     private ExtensionsCommand commandManager;
 
     private ExtensionsConfig config;
     private Logger log;
 
     public void onEnable()
     {
         log = this.getLogger();
 
         PluginManager manager = getServer().getPluginManager();
 
         Plugin clansPlugin = manager.getPlugin("SimpleClans");
         if (clansPlugin == null)
         {
             log.severe("SimpleClans plugin not found.  SimpleClansExtensions was not enabled.");
             return;
         }
 
         Plugin guardPlugin = manager.getPlugin("WorldGuard");
         if (guardPlugin == null)
         {
             log.severe("WorldGuard plugin not found.  SimpleClansExtensions was not enabled.");
             return;
         }
 
         config = new ExtensionsConfig(this);
         clanManager = ((SimpleClans)clansPlugin).getClanManager();
         guardManager = (WorldGuardPlugin)guardPlugin;
         commandManager = new ExtensionsCommand(this);
         new ExtensionsListener(this, config.getWorld());
 
         log.info("SimpleClanExtensions has been enabled");
     }
 
     public void onDisable()
     {
         log.info("SimpleClanExtensions has been disabled");
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
         if (!(sender instanceof Player))
             return false;
 
         Player player = (Player)sender;
         String commandName = cmd.getName();
         if (commandName.equalsIgnoreCase("sce"))
         {
             if (clanManager == null)
             {
                 player.sendMessage(ChatColor.RED + "SimpleClans plugin not found...");
                 return true;
             }
             else if (args.length == 2 && args[0].equalsIgnoreCase("join"))
             {
                 if (!config.getClanTeams().containsKey(args[1]))
                     player.sendMessage(ChatColor.RED + "The clan " + args[1] + " doesn't exist.");
                 else
                     commandManager.CommandJoin(player, args[1]);
                 return true;
             }
            else if (args.length == 1 && args[0].equalsIgnoreCase("join"))
            {
                commandManager.CommandJoinRandom(player);
                return true;
            }
             else if (args.length == 1 && args[0].equalsIgnoreCase("surrender"))
             {
                 commandManager.CommandSurrender(player);
                 return true;
             }
             else if (args.length == 1 && args[0].equalsIgnoreCase("towers"))
             {
                 commandManager.CommandTowers(player);
                 return true;
             }
         }
 
         return false;
     }
 
     public ClanManager getClanManager() { return clanManager; }
     public WorldGuardPlugin getGuardManager() { return guardManager; }
     public ExtensionsConfig getExtensionsConfig() { return config; }
 }
