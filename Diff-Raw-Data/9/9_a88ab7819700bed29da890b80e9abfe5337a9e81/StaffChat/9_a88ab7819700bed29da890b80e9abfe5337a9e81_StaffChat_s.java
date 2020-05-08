 package com.hotmail.shinyclef.shinychannels;
 
 import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Peter
  * Date: 14/07/13
  * Time: 4:17 AM
  */
 
 public class StaffChat
 {
     private static ShinyChannels plugin;
     private static ShinyBaseAPI shinyBaseAPI;
     private static Server server;
     private static List<String> staffChatGuests;
 
     public static void initializeStaffChat(ShinyChannels thePlugin, ShinyBaseAPI theShinyBaseAPI)
     {
        plugin = thePlugin; //test
         shinyBaseAPI = theShinyBaseAPI;
         server = plugin.getServer();
         staffChatGuests = new ArrayList<String>();
     }
 
     //this method is being called from the commandPreProcess event, not CmdExecutor
     public static void modChat(CommandSender sender, String sentence)
     {
         //args for sentence
         if (sentence == null)
         {
             sender.sendMessage("/mb [message]");
             return;
         }
 
         //check perm
         if (!sender.hasPermission("rolyd.mod"))
         {
             sender.sendMessage(ChatColor.RED + "Sorry, you do not have permission to do that.");
             return;
         }
 
         //broadcast
         server.broadcast(ChatColor.RED + "[MB] " + sender.getName() + ": "
                 + ChatColor.GREEN + sentence, "rolyd.mod");
     }
 
     public static boolean staffChat(CommandSender sender, String[] args)
     {
         //args length
         if (args.length < 1)
         {
             return false;
         }
 
         //check perm
         if (!sender.hasPermission("rolyd.exp") && !staffChatGuests.contains(sender.getName().toLowerCase()))
         {
             sender.sendMessage(ChatColor.RED + "Sorry, you do not have permission to do that.");
             return true;
         }
 
         //make sentence
         String sentence = ShinyChannels.makeSentence(args);
 
         //guest qty
         String guestQty = " ";
         if (staffChatGuests.size() > 0)
         {
             guestQty = "(+" + staffChatGuests.size() + ") ";
         }
 
         //perms broadcast
         server.broadcast(ChatColor.RED + "[Staff]" + guestQty + sender.getName() + ": "
                 + ChatColor.AQUA + sentence, "rolyd.exp");
 
         //guest broadcast
         for (String playerName : staffChatGuests)
         {
             if (server.getOfflinePlayer(playerName).isOnline())
             {
                 server.getPlayer(playerName).sendMessage(ChatColor.RED + "[Staff] " + sender.getName()
                         + ": " + ChatColor.AQUA + sentence);
             }
         }
 
         return true;
     }
 
     public static boolean staffChatAdd(CommandSender sender, String[] args)
     {
         //check perm
         if (!sender.hasPermission("rolyd.mod"))
         {
             sender.sendMessage(ChatColor.RED + "Sorry, you do not have permission to do that.");
             return true;
         }
 
         //args length
         if (args.length < 1)
         {
             return false;
         }
 
         //has played check
         String playerName = args[0].toLowerCase();
         if (!shinyBaseAPI.isExistingPlayer(playerName))
         {
             sender.sendMessage(ChatColor.RED + "That player does not exist.");
             return true;
         }
 
         //if the player is already in the list
         if (staffChatGuests.contains(playerName))
         {
             sender.sendMessage(ChatColor.RED + "That player is already in the staff channel.");
             return true;
         }
 
         //add to list and inform staff
         staffChatGuests.add(playerName);
         server.broadcast(ChatColor.YELLOW + playerName + ChatColor.BLUE
                 + " has been added to the staff channel.", "rolyd.mod");
 
         //notify guests
         for (String listName : staffChatGuests)
         {
             if (server.getOfflinePlayer(listName).isOnline() && listName != playerName)
             {
                 server.getPlayer(listName).sendMessage(ChatColor.YELLOW + playerName + ChatColor.BLUE
                         + " has been added to the staff channel.");
             }
         }
 
         //notify added player if online
         if (server.getOfflinePlayer(args[0]).isOnline())
         {
             plugin.getServer().getPlayer(playerName).sendMessage(ChatColor.BLUE
                     + "You have been added to the staff channel. Type " + ChatColor.YELLOW
                     + "/sb [message]" + ChatColor.BLUE + " to post a message.");
         }
 
         return true;
     }
 
     public static boolean staffChatRemove(CommandSender sender, String[] args)
     {
         //check perm
         if (!sender.hasPermission("rolyd.mod"))
         {
             sender.sendMessage(ChatColor.RED + "Sorry, you do not have permission to do that.");
             return true;
         }
 
         //args length
         if (args.length < 1)
         {
             return false;
         }
 
         //has played check
         String playerName = args[0].toLowerCase();
         if (!shinyBaseAPI.isExistingPlayer(playerName))
         {
             sender.sendMessage(ChatColor.RED + "That player does not exist.");
             return true;
         }
 
         //if the player is not in the list
         if (!staffChatGuests.contains(playerName))
         {
             sender.sendMessage(ChatColor.RED + "That player is not in the staff channel.");
             return true;
         }
 
         //remove from list and inform staff
         staffChatGuests.remove(playerName);
         server.broadcast(ChatColor.YELLOW + playerName + ChatColor.BLUE
                 + " has been removed from the staff channel.", "rolyd.mod");
 
         //notify guests
         for (String name : staffChatGuests)
         {
             if (server.getOfflinePlayer(name).isOnline())
             {
                 server.getPlayer(name).sendMessage(ChatColor.YELLOW + playerName + ChatColor.BLUE
                         + " has been removed from the staff channel.");
             }
         }
 
         //notify removed player if online
         if (server.getOfflinePlayer(args[0]).isOnline())
         {
             plugin.getServer().getPlayer(playerName).sendMessage(ChatColor.BLUE
                     + "You have been removed from the staff channel.");
         }
 
         return true;
     }
 
     public static boolean staffList(CommandSender sender, String[] args)
     {
         //check perm
         if (!sender.hasPermission("rolyd.mod"))
         {
             sender.sendMessage(ChatColor.RED + "Sorry, you do not have permission to do that.");
             return true;
         }
 
         if (args.length > 0)
         {
             sender.sendMessage(ChatColor.RED + "/sblist does not require any parameters (just type /sblist).");
             return true;
         }
 
         //return a list of players to command sender
         sender.sendMessage(ChatColor.BLUE + staffChatGuests.toString());
         return true;
     }
 }
