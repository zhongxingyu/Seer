 package com.psyco.tplmc.CustomMessages.commands;
 
 import com.psyco.tplmc.CustomMessages.CustomMessages;
 import com.psyco.tplmc.CustomMessages.MessageTypes;
 import com.psyco.tplmc.CustomMessages.Util;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmQuitCommand extends CommandBase {
 
     private CmQuitCommand() {
         CommandManager.getInstance().registerCommand("quit", this);
     }
 
     static {
         new CmQuitCommand();
     }
 
     @Override
     public void onPlayerCommand(Player player, String label, String[] args) {
         if (args.length == 0) {
             if (!CustomMessages.getConfiguration().permsRequired() || player.hasPermission("CustomMessages.quit")) {
                 player.sendMessage(ChatColor.GREEN + "Current quit message:");
                 player.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(player, MessageTypes.QUIT)) + getPlayerDisabledText(player, MessageTypes.QUIT));
             } else {
                 player.sendMessage(NO_PERMISSION);
             }
         } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
             if (args[0].equalsIgnoreCase("enable")) {
                 if (player.hasPermission("CustomMessages.quit")) {
                     if (CustomMessages.getConfiguration().setPlayerMessageEnabled(player, MessageTypes.QUIT, true)) {
                         player.sendMessage(ChatColor.GREEN + "Your quit message is now enabled");
                     } else {
                         player.sendMessage(ChatColor.RED + "Your quit message is already enabled");
                     }
                 } else {
                     player.sendMessage(NO_PERMISSION);
                 }
             } else if (args[0].equalsIgnoreCase("disable")) {
                 if (player.hasPermission("CustomMessages.quit")) {
                     if (CustomMessages.getConfiguration().setPlayerMessageEnabled(player, MessageTypes.QUIT, false)) {
                         player.sendMessage(ChatColor.GREEN + "Your quit message is now disabled");
                     } else {
                         player.sendMessage(ChatColor.RED + "Your quit message is already disabled");
                     }
                 }
             } else if (args[0].equalsIgnoreCase("reset")) {
                 if (player.hasPermission("CustomMessages.quit")) {
                     CustomMessages.getConfiguration().resetPlayerMessage(player, MessageTypes.QUIT);
                     player.sendMessage(ChatColor.GREEN + "Reset your quit message:");
                     player.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(player, MessageTypes.QUIT)) + getPlayerDisabledText(player, MessageTypes.QUIT));
                 } else {
                     player.sendMessage(NO_PERMISSION);
                 }
             } else {
                 if (target != null) {
                     if (player.hasPermission("CustomMessages.quit.other")) {
                         player.sendMessage(ChatColor.GREEN + target.getName() + "'s current quit message:");
                         player.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(target, MessageTypes.QUIT)) + getPlayerDisabledText(target, MessageTypes.QUIT));
                     } else {
                         player.sendMessage(NO_PERMISSION);
                     }
                 } else if (CustomMessages.getVaultCompat().isGroup(args[0])) {
                     if (player.hasPermission("CustomMessages.quit.group")) {
                         player.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s current quit message:");
                         player.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getGroupMessage(args[0], MessageTypes.QUIT)) + getGroupDisabledText(args[0], MessageTypes.QUIT));
                     } else {
                         player.sendMessage(NO_PERMISSION);
                     }
                 } else {
                     if (player.hasPermission("CustomMessages.quit")) {
                         player.sendMessage(ChatColor.GREEN + "Set your quit message to:");
                         player.sendMessage(CustomMessages.getConfiguration().setPlayerMessage(player, MessageTypes.QUIT, args[0]) + getPlayerDisabledText(player, MessageTypes.QUIT));
                     } else {
                         player.sendMessage(NO_PERMISSION);
                     }
                 }
             }
         } else {
            Player target = Bukkit.getPlayer(args[0]);
             if (target != null) {
                 if (target.getName().equals(player.getName())) {
                     if (player.hasPermission("CustomMessages.quit")) {
                         if (args[0].equalsIgnoreCase("enable")) {
                             if (CustomMessages.getConfiguration().setPlayerMessageEnabled(player, MessageTypes.QUIT, true)) {
                                 player.sendMessage(ChatColor.GREEN + "Your quit message is now enabled");
                             } else {
                                 player.sendMessage(ChatColor.RED + "Your quit message is already enabled");
                             }
                         } else if (args[0].equalsIgnoreCase("disable")) {
                             if (CustomMessages.getConfiguration().setPlayerMessageEnabled(player, MessageTypes.QUIT, false)) {
                                 player.sendMessage(ChatColor.GREEN + "Your quit message is now disabled");
                             } else {
                                 player.sendMessage(ChatColor.GREEN + "Your quit message is already disabled");
                             }
                         } else if (args[0].equalsIgnoreCase("reset")) {
                             CustomMessages.getConfiguration().resetPlayerMessage(player, MessageTypes.QUIT);
                             player.sendMessage(ChatColor.GREEN + "Reset your quit message:");
                             player.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(player, MessageTypes.QUIT)) + getPlayerDisabledText(player, MessageTypes.QUIT));
                         } else {
                             String messageString = Util.getSpacedString(args, 1, args.length);
                             messageString = CustomMessages.getConfiguration().setPlayerMessage(player, MessageTypes.QUIT, messageString);
                             player.sendMessage(ChatColor.GREEN + "Set your quit message to:");
                             player.sendMessage(messageString + getPlayerDisabledText(player, MessageTypes.QUIT));
                         }
                     } else {
                         player.sendMessage(NO_PERMISSION);
                     }
 
                 } else {
                     if (player.hasPermission("CustomMessages.quit.other")) {
                         if (args[1].equalsIgnoreCase("enable")) {
                             if (CustomMessages.getConfiguration().setPlayerMessageEnabled(target, MessageTypes.QUIT, true)) {
                                 player.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message is now enabled");
                                 target.sendMessage(ChatColor.GREEN + "Your quit message was enabled by " + player.getName());
                             } else {
                                 player.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message was already enabled");
                             }
                         } else if (args[1].equalsIgnoreCase("disable")) {
                             if (CustomMessages.getConfiguration().setPlayerMessageEnabled(target, MessageTypes.QUIT, false)) {
                                 player.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message is now disabled");
                                 target.sendMessage(ChatColor.GREEN + "Your quit message was disabled by " + player.getName());
                             } else {
                                 player.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message was already disabled");
                             }
                         } else if (args[1].equalsIgnoreCase("reset")) {
                             CustomMessages.getConfiguration().resetPlayerMessage(target, MessageTypes.QUIT);
                             player.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s quit message:");
                             target.sendMessage(ChatColor.GREEN + player.getName() + " reset your quit message:");
                             player.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(target, MessageTypes.QUIT)) + getPlayerDisabledText(target, MessageTypes.QUIT));
                             target.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(target, MessageTypes.QUIT)) + getPlayerDisabledText(target, MessageTypes.QUIT));
                         } else {
                             String messageString = Util.getSpacedString(args, 1, args.length);
                             messageString = CustomMessages.getConfiguration().setPlayerMessage(target, MessageTypes.QUIT, messageString);
                             player.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s quit message to:");
                             player.sendMessage(messageString + getPlayerDisabledText(target, MessageTypes.QUIT));
                             target.sendMessage(ChatColor.GREEN + player.getName() + " set your quit message to:");
                             target.sendMessage(messageString + getPlayerDisabledText(target, MessageTypes.QUIT));
                         }
                     } else {
                         player.sendMessage(NO_PERMISSION);
                     }
                 }
             } else if (CustomMessages.getVaultCompat().isGroup(args[0])) {
                 if (player.hasPermission("CustomMessages.quit.group")) {
                     if (args[1].equalsIgnoreCase("enable")) {
                         if (CustomMessages.getConfiguration().setGroupMessageEnabled(args[0], MessageTypes.QUIT, true)) {
                             player.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message was enabled");
                         } else {
                             player.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message is already enabled");
                         }
                     } else if (args[1].equalsIgnoreCase("disable")) {
                         if (CustomMessages.getConfiguration().setGroupMessageEnabled(args[0], MessageTypes.QUIT, false)) {
                             player.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message was disabled");
                         } else {
                             player.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message is already disabled");
                         }
                     } else if(args[1].equalsIgnoreCase("reset")){
                         CustomMessages.getConfiguration().resetGroupMessage(args[0], MessageTypes.QUIT);
                         player.sendMessage(ChatColor.GREEN + "Reset group " + args[0] + "'s quit message:");
                         player.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getGroupMessage(args[0], MessageTypes.QUIT)) + getGroupDisabledText(args[0], MessageTypes.QUIT));
                     } else {
                         String messageString = Util.getSpacedString(args, 1, args.length);
                         messageString = CustomMessages.getConfiguration().setGroupMessage(args[0], MessageTypes.QUIT, messageString);
                         player.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message is now:");
                         player.sendMessage(messageString + getGroupDisabledText(args[0], MessageTypes.QUIT));
                     }
                 } else {
                     player.sendMessage(NO_PERMISSION);
                 }
             } else {
                 if (player.hasPermission("CustomMessages.quit")) {
                     String messageString = Util.getSpacedString(args, 0, args.length);
                     messageString = CustomMessages.getConfiguration().setPlayerMessage(player, MessageTypes.QUIT, messageString);
                     player.sendMessage(ChatColor.GREEN + "Set your quit message to:");
                     player.sendMessage(messageString);
                 } else {
                     player.sendMessage(NO_PERMISSION);
                 }
             }
         }
     }
 
     @Override
     public void onCommandSenderCommand(CommandSender sender, String label, String[] args) {
         if (args.length == 0) {
             sender.sendMessage(ChatColor.GREEN + "/" + label + " quit <player|group> [message|enable|disable|reset]");
         } else if (args.length == 1) {
             Player target = Bukkit.getPlayer(args[0]);
             if (target != null) {
                 if (sender.hasPermission("CustomMessages.quit.other")) {
                     sender.sendMessage(ChatColor.GREEN + target.getName() + "'s current quit message:");
                     sender.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(target, MessageTypes.QUIT)) + getPlayerDisabledText(target, MessageTypes.QUIT));
                 } else {
                     sender.sendMessage(NO_PERMISSION);
                 }
             } else if (CustomMessages.getVaultCompat().isGroup(args[0])) {
                 if (sender.hasPermission("CustomMessages.quit.group")) {
                     sender.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s current quit message:");
                     sender.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getGroupMessage(args[0], MessageTypes.QUIT)) + getGroupDisabledText(args[0], MessageTypes.QUIT));
                 } else {
                     sender.sendMessage(NO_PERMISSION);
                 }
             } else {
                 sender.sendMessage(ChatColor.GREEN + "Could not find a player or group called '" + args[0] + "'");
             }
         } else {
             Player target = Bukkit.getPlayer(args[0]);
             if (target != null) {
                 if (sender.hasPermission("CustomMessages.quit.other")) {
                     if (args[1].equalsIgnoreCase("enable")) {
                         if (CustomMessages.getConfiguration().setPlayerMessageEnabled(target, MessageTypes.QUIT, true)) {
                             sender.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message was enabled");
                             target.sendMessage(ChatColor.GREEN + sender.getName() + " enabled your quit message");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message is already enabled");
                         }
                     } else if (args[1].equalsIgnoreCase("disable")) {
                         if (CustomMessages.getConfiguration().setPlayerMessageEnabled(target, MessageTypes.QUIT, false)) {
                             sender.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message was disabled");
                             target.sendMessage(ChatColor.GREEN + sender.getName() + " disabled your quit message");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message is already disabled");
                         }
                     } else if(args[1].equalsIgnoreCase("reset")){
                         CustomMessages.getConfiguration().resetPlayerMessage(target, MessageTypes.QUIT);
                         sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s quit message:");
                         sender.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(target, MessageTypes.QUIT)) + getPlayerDisabledText(target, MessageTypes.QUIT));
                         target.sendMessage(ChatColor.GREEN + sender.getName() + " reset your quit message:");
                         target.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getPlayerMessage(target, MessageTypes.QUIT)) + getPlayerDisabledText(target, MessageTypes.QUIT));
                     } else {
                         String messageString = Util.getSpacedString(args, 1, args.length);
                         messageString = CustomMessages.getConfiguration().setPlayerMessage(target, MessageTypes.QUIT, messageString);
                         sender.sendMessage(ChatColor.GREEN + target.getName() + "'s quit message is now:");
                         sender.sendMessage(messageString + getPlayerDisabledText(target, MessageTypes.QUIT));
                         target.sendMessage(ChatColor.GREEN + sender.getName() + " set your quit message to:");
                         target.sendMessage(messageString + getPlayerDisabledText(target, MessageTypes.QUIT));
                     }
                 } else {
                     sender.sendMessage(NO_PERMISSION);
                 }
             } else if (CustomMessages.getVaultCompat().isGroup(args[0])) {
                 if (sender.hasPermission("CustomMessages.quit.group")) {
                     if (args[1].equalsIgnoreCase("enable")) {
                         if (CustomMessages.getConfiguration().setGroupMessageEnabled(args[0], MessageTypes.QUIT, true)) {
                             sender.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message was enabled");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message is already enabled");
                         }
                     } else if (args[1].equalsIgnoreCase("disable")) {
                         if (CustomMessages.getConfiguration().setGroupMessageEnabled(args[0], MessageTypes.QUIT, false)) {
                             sender.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message was disabled");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message is already disabled");
                         }
                     } else if (args[1].equalsIgnoreCase("reset")) {
                         CustomMessages.getConfiguration().resetGroupMessage(args[0], MessageTypes.QUIT);
                         sender.sendMessage(ChatColor.GREEN + "Reset group " + args[0] + "'s quit message:");
                         sender.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getGroupMessage(args[0], MessageTypes.QUIT)) + getGroupDisabledText(args[0], MessageTypes.QUIT));
                     } else {
                         String messageString = Util.getSpacedString(args, 1, args.length);
                         messageString = CustomMessages.getConfiguration().setGroupMessage(args[0], MessageTypes.QUIT, messageString);
                         sender.sendMessage(ChatColor.GREEN + "Group " + args[0] + "'s quit message is now:");
                         sender.sendMessage(messageString + getGroupDisabledText(args[0], MessageTypes.QUIT));
                     }
                 } else {
                     sender.sendMessage(NO_PERMISSION);
                 }
             } else {
                 sender.sendMessage(ChatColor.GREEN + "Could not find a player or group called '" + args[0] + "'");
             }
         }
     }
 }
