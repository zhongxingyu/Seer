 package net.serubin.serubans.commands;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import net.serubin.serubans.SeruBans;
 import net.serubin.serubans.util.HashMaps;
 import net.serubin.serubans.util.HelpMessages;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class SeruBansCommand implements CommandExecutor {
 
     private SeruBans plugin;
 
     public SeruBansCommand(SeruBans plugin) {
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         if (commandLabel.equalsIgnoreCase("serubans")) {
             if (SeruBans.hasPermission(sender, SeruBans.HELPPERM)
                     || SeruBans.hasPermission(sender, SeruBans.DEBUGPERM)) {
                 if (args.length == 0) {
                     sender.sendMessage(ChatColor.GREEN + "Serubans "
                             + ChatColor.YELLOW + " version "
                             + SeruBans.getVersion());
                     sender.sendMessage(ChatColor.YELLOW + "For help with: ");
                    sender.sendMessage(ChatColor.GREEN + "    banning "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans ban");
                    sender.sendMessage(ChatColor.GREEN + "    tempbanning "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans tempban");
                     sender.sendMessage(ChatColor.GREEN + "    kicking "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans kick");
                     sender.sendMessage(ChatColor.GREEN + "    warning "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans warn");
                     sender.sendMessage(ChatColor.GREEN + "    unbaning "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans unban");
                     sender.sendMessage(ChatColor.GREEN + "    checking bans "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans checkban");
                     sender.sendMessage(ChatColor.GREEN + "    updating bans "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans update");
                     sender.sendMessage(ChatColor.GREEN + "    searching "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans search");
                     sender.sendMessage(ChatColor.GREEN + "    debug "
                             + ChatColor.YELLOW + "type " + ChatColor.GREEN
                             + "/serubans debug");
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("ban")) {
                     // Ban help
                     HelpMessages.banHelp(sender);
                     HelpMessages.banOptions(sender);
 
                 } else if (args[0].equalsIgnoreCase("tempban")) {
                     // Tempban help
                     HelpMessages.tempBanHelp(sender);
                     HelpMessages.banOptions(sender);
 
                 } else if (args[0].equalsIgnoreCase("kick")) {
                     // Kick help
                     HelpMessages.kickHelp(sender);
                     HelpMessages.banOptions(sender);
                 } else if (args[0].equalsIgnoreCase("warn")) {
                     // Warn help
                     HelpMessages.warnHelp(sender);
                     HelpMessages.banOptions(sender);
                 } else if (args[0].equalsIgnoreCase("unban")) {
                     // Unban help
                     HelpMessages.unbanHelp(sender);
                     HelpMessages.banOptions(sender);
                 }
 
                 else if (args[0].equalsIgnoreCase("checkban")) {
                     // Checkban help
                     HelpMessages.checkbanHelp(sender);
 
                 } else if (args[0].equalsIgnoreCase("update")) {
                     // Update help
                     HelpMessages.updateHelp(sender);
                 } else if (args[0].equalsIgnoreCase("search")) {
                     // Search help
                     HelpMessages.searchHelp(sender);
                 } else if (args[0].equalsIgnoreCase("debug")) {
                     // Debug help
                     HelpMessages.debugHelp(sender);
                 } else {
                     if (SeruBans.hasPermission(sender, SeruBans.DEBUGPERM)) {
                         if (args[0].startsWith("-")) {
                             if (args[0].contains("a")
                                     && !args[0].contains("api")) {
                                 sender.sendMessage("Players: "
                                         + HashMaps.getFullPlayerList());
                                 sender.sendMessage("Banned Players: "
                                         + HashMaps.getFullBannedPlayers());
                                 sender.sendMessage("TempBan: "
                                         + HashMaps.getFullTempBannedTime());
                                 sender.sendMessage("Ids: "
                                         + HashMaps.getFullIds());
                                 return true;
                             }
                             if (args[0].contains("p")
                                     && !args[0].contains("api")) {
                                 sender.sendMessage("Players: "
                                         + HashMaps.getFullPlayerList());
                             }
                             if (args[0].contains("i")
                                     && !args[0].contains("api")) {
                                 sender.sendMessage("Ids: "
                                         + HashMaps.getFullIds());
                             }
                             if (args[0].contains("b")) {
                                 sender.sendMessage("Banned Players: "
                                         + HashMaps.getFullBannedPlayers());
                             }
                             if (args[0].contains("t")) {
                                 sender.sendMessage("TempBan: "
                                         + HashMaps.getFullTempBannedTime());
                             }
                             if (args[0].contains("w")) {
                                 sender.sendMessage("Warns: "
                                         + HashMaps.getFullWarnList());
                             }
                             if (args[0].contains("e")) {
                                 plugin.log
                                         .info(sender.getName()
                                                 + " has exected the export command. Now attempting to export bans to vanilla bans file. Once this has completed unbanning a player in serubans may not unban them. Make sure to remove their name from the vanilla bans file!");
                                 List<String> ban = HashMaps.getBannedForFile();
                                 Iterator<String> iterator = ban.iterator();
                                 try {
                                     BufferedWriter banlist = new BufferedWriter(
                                             new FileWriter(
                                                     "banned-players.txt", true));
 
                                     while (iterator.hasNext()) {
                                         String player = iterator.next();
                                         banlist.write(player);
                                         banlist.newLine();
                                     }
                                     banlist.close();
                                     sender.sendMessage(ChatColor.GREEN
                                             + "Bans were successfully exported!");
                                 } catch (IOException e) {
                                     plugin.log
                                             .severe("File Could not be writen!");
                                     sender.sendMessage(ChatColor.RED
                                             + "File Could not be writen!");
                                 }
 
                             }
                             return true;
                         } else {
                             sender.sendMessage(ChatColor.RED
                                     + "Help/Debug argument was not found.");
                             return true;
                         }
                     }
                 }
             }
             return true;
         }
         return false;
     }
 }
