 package com.tyzoid.jailr.commands;
 
 import com.tyzoid.jailr.JailrPlugin;
 import com.tyzoid.jailr.commands.IssuedCommand;
 import com.tyzoid.jailr.util.Messenger;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.tyzoid.jailr.api.JailAPI;
 
 import java.lang.String;
 
 public class JailrCommand {
     public static boolean issue(IssuedCommand cmd) {
         if (cmd.argExists(0) && cmd.getArgs()[0].equalsIgnoreCase("setjail"))
            setUnjailPoint(cmd);
        else if (cmd.argExists(0) && cmd.getArgs()[0].equalsIgnoreCase("setunjail"))
             setJailPoint(cmd);
         else if (cmd.argExists(0) && cmd.getArgs()[0].equalsIgnoreCase("help"))
             help(cmd);
         else if (cmd.argExists(0) && cmd.getArgs()[0].equalsIgnoreCase("about"))
             about(cmd);
         else
             help(cmd);
         return true;
     }
 
     private static void setUnjailPoint(IssuedCommand cmd) {
         if (cmd.isPlayer()) {
             Player player = (Player) cmd.getSender();
 
             if (player.hasPermission("jailr.setunjail")) {
                 JailAPI.setUnJailPoint(player.getLocation());
                 Messenger.sendMessage(cmd.getSender(), "The unjail point has been set to your location.");
             }
         } else {
             Messenger.sendError(cmd.getSender(), "You must be a player to use that command.");
         }
     }
 
     private static void setJailPoint(IssuedCommand cmd) {
         if (cmd.isPlayer()) {
             Player player = (Player) cmd.getSender();
 
             if (player.hasPermission("jailr.setjail")) {
                 JailAPI.setJailPoint(player.getLocation());
                 Messenger.sendMessage(cmd.getSender(), "The jail point has been set to your location.");
             }
         } else {
             Messenger.sendError(cmd.getSender(), "You must be a player to use that command.");
         }
     }
 
     private static void help(IssuedCommand cmd) {
         String bull = ChatColor.GRAY + "  - ";
 
         Messenger.sendMessage(cmd.getSender(), "Available commands:");
         if (cmd.isPlayer()) {
             Player player = (Player) cmd.getSender();
 
             Messenger.sendMessage(cmd.getSender(), bull + "/jailr help - See this menu", false);
             Messenger.sendMessage(cmd.getSender(), bull + "/jailr about - See information about jailr", false);
             if (player.hasPermission("jailr.jail"))
                 Messenger.sendMessage(cmd.getSender(), bull + "/jailr jail <player> [XhYm] - Jail a player", false);
             if (player.hasPermission("jailr.unjail"))
                 Messenger.sendMessage(cmd.getSender(), bull + "/jailr unjail <player> - Unjail a player", false);
             if (player.hasPermission("jailr.setjail"))
                 Messenger.sendMessage(cmd.getSender(), bull + "/jailr setjail - Sets the jail point.", false);
             if (player.hasPermission("jailr.setunjail"))
                 Messenger.sendMessage(cmd.getSender(), bull + "/jailr setunjail - Sets the jail removal point.", false);
             if (player.hasPermission("jailr.list"))
                 Messenger.sendMessage(cmd.getSender(), bull + "/jailr list - List prisoners", false);
             if (player.hasPermission("jailr.list"))
                 Messenger.sendMessage(cmd.getSender(), bull + "/jailr jailtime <player> - Check the remaining time of a prisoner", false);
         } else {
             Messenger.sendMessage(cmd.getSender(), bull + "/jailr help - See this menu", false);
             Messenger.sendMessage(cmd.getSender(), bull + "/jailr about - See information about jailr", false);
             Messenger.sendMessage(cmd.getSender(), bull + "/jailr jail <player> [XhYm] - Jail a player", false);
             Messenger.sendMessage(cmd.getSender(), bull + "/jailr unjail <player> - Unjail a player", false);
             Messenger.sendMessage(cmd.getSender(), bull + "/jailr list - List prisoners", false);
             Messenger.sendMessage(cmd.getSender(), bull + "/jailr jailtime <player> - Check the remaining time of a prisoner", false);
         }
     }
 
     private static void about(IssuedCommand cmd) {
         Messenger.sendMessage(cmd.getSender(), "jailr v" + JailrPlugin.getPlugin().getDescription().getVersion() + " by goldblattster and Tyzoid");
         Messenger.sendMessage(cmd.getSender(), "jailr is licensed under the BSD 2 clause license. Go to town.");
     }
 }
