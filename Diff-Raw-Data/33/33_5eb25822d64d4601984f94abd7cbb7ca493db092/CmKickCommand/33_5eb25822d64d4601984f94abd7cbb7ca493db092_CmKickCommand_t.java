 package com.psyco.tplmc.CustomMessages.commands;
 
 import com.psyco.tplmc.CustomMessages.CustomMessages;
 import com.psyco.tplmc.CustomMessages.MessageTypes;
 import com.psyco.tplmc.CustomMessages.Util;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmKickCommand extends CommandBase {
 
     private CmKickCommand() {
         CommandManager.getInstance().registerCommand("kick", this);
     }
 
     static {
         new CmKickCommand();
     }
 
     @Override
     public void onPlayerCommand(Player player, String label, String[] args) {
         onCommandSenderCommand(player, label, args);
     }
 
     @Override
     public void onCommandSenderCommand(CommandSender sender, String label, String[] args) {
         if(args.length == 0){
             if(!CustomMessages.getConfiguration().permsRequired() || sender.hasPermission("CustomMessages.kick")){
                 sender.sendMessage(ChatColor.GREEN + "Current kick message:");
                sender.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getGlobalMessage(MessageTypes.KICK) + getGlobalDisabledText(MessageTypes.KICK)));
             } else {
                 sender.sendMessage(NO_PERMISSION);
             }
         } else if (sender.hasPermission("CustomMessages.kick")) {
             if (args[0].equalsIgnoreCase("enable")) {
                 if (CustomMessages.getConfiguration().setGlobalMessageEnabled(MessageTypes.KICK, true)) {
                     sender.sendMessage(ChatColor.GREEN + "Kick message is now enabled");
                 } else {
                     sender.sendMessage(ChatColor.RED + "Kick message was already enabled");
                 }
             } else if (args[0].equalsIgnoreCase("disable")) {
                 if (CustomMessages.getConfiguration().setGlobalMessageEnabled(MessageTypes.KICK, false)) {
                     sender.sendMessage(ChatColor.GREEN + "Kick message is now disabled");
                 } else {
                     sender.sendMessage(ChatColor.RED + "Kick message was already disabled");
                 }
             } else if (args[0].equalsIgnoreCase("reset")) {
                 CustomMessages.getConfiguration().resetGlobalMessage(MessageTypes.KICK);
                 sender.sendMessage(ChatColor.GREEN + "Reset kick message:");
                sender.sendMessage(Util.translateColor(CustomMessages.getConfiguration().getGlobalMessage(MessageTypes.KICK)) + getGlobalDisabledText(MessageTypes.KICK));
             } else {
                 String spaceString = Util.getSpacedString(args, 0, args.length);
                 spaceString = CustomMessages.getConfiguration().setGlobalMessage(MessageTypes.KICK, spaceString);
                 sender.sendMessage(ChatColor.GREEN + "Set kick message to:");
                 sender.sendMessage(spaceString + getGlobalDisabledText(MessageTypes.KICK));
             }
         } else {
             sender.sendMessage(NO_PERMISSION);
         }
     }
 }
