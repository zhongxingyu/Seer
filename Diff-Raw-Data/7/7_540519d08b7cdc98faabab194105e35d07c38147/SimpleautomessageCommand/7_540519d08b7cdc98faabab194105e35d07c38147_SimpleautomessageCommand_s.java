 package com.carlgo11.simpleautomessage.commands;
 
 import com.carlgo11.simpleautomessage.Main;
 import com.carlgo11.simpleautomessage.language.Lang;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
 
 public class SimpleautomessageCommand implements CommandExecutor {
 
     private Main plugin;
 
     public SimpleautomessageCommand(Main plug) {
         this.plugin = plug;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String senderToSend = plugin.getConfig().getString("sender");
         String sender0 = ChatColor.translateAlternateColorCodes('&', senderToSend);
         String prefixToSend = plugin.getConfig().getString("prefix");
         String prefix = ChatColor.translateAlternateColorCodes('&', prefixToSend);
         String suffixToSend = plugin.getConfig().getString("suffix");
         String suffix = ChatColor.translateAlternateColorCodes('&', suffixToSend);
         String pn = sender.getName();
         if (cmd.getName().equalsIgnoreCase("simpleautomessage")) {
             if (args.length == 0) {
                 if (sender.hasPermission("simpleAutoMessage.simpleautomessage")) {
                     sender.sendMessage(ChatColor.GREEN + "======== " + ChatColor.YELLOW + sender0 + ChatColor.GREEN + " ======== ");
                     sender.sendMessage(ChatColor.GRAY + "-  /" + ChatColor.RED + "SimpleAutoMessage" + ChatColor.YELLOW + Lang.Simplemsg_Main);
                     sender.sendMessage(ChatColor.GRAY + "-  /" + ChatColor.RED + "SimpleAutoMessage Reload" + ChatColor.YELLOW + Lang.Simplemsg_Reload);
                     sender.sendMessage(ChatColor.GRAY + "-  /" + ChatColor.RED + "SimpleAutoMessage Update" + ChatColor.YELLOW + Lang.Simplemsg_Update);
                     sender.sendMessage(ChatColor.GRAY + "-  /" + ChatColor.RED + "SimpleAutoMessage List" + ChatColor.YELLOW + Lang.Simplemsg_List);
                 } else {
                     sender.sendMessage(Lang.BAD_PERMS + "");
                 }
             } else if (args.length == 1) {
                 if (args[0].equalsIgnoreCase("reload")) {
                     if (sender.hasPermission("simpleAutoMessage.simpleautomessage.reload")) {
                         plugin.getServer().getPluginManager().disablePlugin(plugin);
                         plugin.getServer().getPluginManager().enablePlugin(plugin);
                         sender.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.stripColor(sender0) + ChatColor.GREEN + " " + Lang.PL_RELOADED);
                     } else {
                         sender.sendMessage(Lang.BAD_PERMS + "");
                     }
                 } else {
                     if (args[0].equalsIgnoreCase("list")) {
                         if (sender.hasPermission("simpleAutoMessage.simpleautomessage.list")) {
                             sender.sendMessage(ChatColor.GREEN + "======== " + ChatColor.YELLOW + sender0 + ChatColor.GREEN + " ======== ");
                             sender.sendMessage(ChatColor.GRAY + "min-players" + ": " + ChatColor.RESET + plugin.getConfig().getInt("min-players"));
                             sender.sendMessage(ChatColor.GRAY + "time" + ": " + ChatColor.RESET + plugin.getConfig().getInt("time"));
                             sender.sendMessage(ChatColor.GRAY + "prefix" + ": " + ChatColor.RESET + prefix);
                             sender.sendMessage(ChatColor.GRAY + "sender" + ": " + ChatColor.RESET + sender0);
                             sender.sendMessage(ChatColor.GRAY + "suffix" + ": " + ChatColor.RESET + suffix);
                             int err = 0;
                             for (int i = 1; err != 1; i++) {
                                 if (plugin.getConfig().contains("msg" + i)) {
                                     String messageToSend = plugin.getConfig().getString("msg" + i);
                                     String msgToMC = ChatColor.translateAlternateColorCodes('&', messageToSend);
                                     sender.sendMessage(ChatColor.GRAY + "msg" + i + ": '" + ChatColor.RESET + msgToMC + "'");
                                     plugin.onDebug("Found msg" + i);
                                 } else {
                                     err++;
                                     plugin.onDebug("Did not find msg" + i);
                                 }
                             }
                         } else {
                             sender.sendMessage(Lang.BAD_PERMS + "");
                         }
                     } else if(args[0].equalsIgnoreCase("update")){
                             plugin.forceUpdate(Bukkit.getPlayer(pn), sender0);
                         }else{
                         sender.sendMessage(sender0 + " " + Lang.UNKNOWN_CMD);
                         }
                 }
             } else if (args.length > 1) {
                 sender.sendMessage(sender0 + " " + Lang.UNKNOWN_CMD);
             }
         }
         return true;
     }
 }
