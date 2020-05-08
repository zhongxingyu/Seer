 package com.psyco.tplmc.CustomMessages.commands;
 
 import com.psyco.tplmc.CustomMessages.CustomMessages;
 import com.psyco.tplmc.CustomMessages.MessageTypes;
 import com.psyco.tplmc.CustomMessages.Util;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmFirstjoinCommand extends CommandBase {
 
     private CmFirstjoinCommand() {
         CommandManager.getInstance().registerCommand("firstjoin", this);
     }
 
     static {
         new CmFirstjoinCommand();
     }
 
     @Override
     public void onPlayerCommand(Player player, String label, String[] args) {
         onCommandSenderCommand(player, label, args);
     }
 
     @Override
     public void onCommandSenderCommand(CommandSender sender, String label, String[] args) {
         if(args.length == 0){
             if(!CustomMessages.getConfiguration().permsRequired() || sender.hasPermission("CustomMessages.firstjoin")){
                 sender.sendMessage(ChatColor.GREEN + "Current first join message:");
                sender.sendMessage(CustomMessages.getConfiguration().getGlobalMessage(MessageTypes.FIRSTJOIN) + getGlobalDisabledText(MessageTypes.FIRSTJOIN));
             } else {
                 sender.sendMessage(NO_PERMISSION);
             }
         } else if (sender.hasPermission("CustomMessages.firstjoin")) {
             if (args[0].equalsIgnoreCase("enable")) {
                 if (CustomMessages.getConfiguration().setGlobalMessageEnabled(MessageTypes.FIRSTJOIN, true)) {
                     sender.sendMessage(ChatColor.GREEN + "First join message is now enabled");
                 } else {
                     sender.sendMessage(ChatColor.RED + "First join message was already enabled");
                 }
             } else if (args[0].equalsIgnoreCase("disable")) {
                 if (CustomMessages.getConfiguration().setGlobalMessageEnabled(MessageTypes.FIRSTJOIN, false)) {
                     sender.sendMessage(ChatColor.GREEN + "First join message is now disabled");
                 } else {
                     sender.sendMessage(ChatColor.RED + "First join message was already disabled");
                 }
             } else if (args[0].equalsIgnoreCase("reset")) {
                 CustomMessages.getConfiguration().resetGlobalMessage(MessageTypes.FIRSTJOIN);
                 sender.sendMessage(ChatColor.GREEN + "Reset first join message:");
                sender.sendMessage(CustomMessages.getConfiguration().getGlobalMessage(MessageTypes.FIRSTJOIN) + getGlobalDisabledText(MessageTypes.FIRSTJOIN));
             } else {
                 String spaceString = Util.getSpacedString(args, 0, args.length);
                 spaceString = CustomMessages.getConfiguration().setGlobalMessage(MessageTypes.FIRSTJOIN, spaceString);
                 sender.sendMessage(ChatColor.GREEN + "Set first join message to:");
                 sender.sendMessage(spaceString + getGlobalDisabledText(MessageTypes.FIRSTJOIN));
             }
         } else {
             sender.sendMessage(NO_PERMISSION);
         }
     }
 }
