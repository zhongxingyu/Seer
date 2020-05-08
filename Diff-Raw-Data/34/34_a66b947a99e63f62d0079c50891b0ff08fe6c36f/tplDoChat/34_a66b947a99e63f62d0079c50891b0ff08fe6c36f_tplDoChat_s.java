 package de.minestar.theterminal.http.pages;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.bukkit.gemo.BukkitHTTP.HTTPEvent;
 import com.bukkit.gemo.BukkitHTTP.Page;
 
 import de.minestar.theterminal.core.Core;
 
 public class tplDoChat extends TemplatePage {
 
     public static void execTemplate(Page page, HTTPEvent event) {
         // GET TIMESTAMP FROM PARAMETERS (GET-METHOD)
         if (event.postParameter != null) {
             if (event.postParameter.containsKey("user") && event.postParameter.containsKey("text")) {
                 String playerName = event.postParameter.get("user");
                 String message = event.postParameter.get("text");
                 Core.getInstance().getChatManager().addWebChat(playerName, message);
                 sendMessageToAll(ChatColor.GRAY + "[WEB] " + playerName + ChatColor.WHITE + ": " + message);
             }
         }
     }
 
     private static void sendMessageToAll(String message) {
         Player[] players = Bukkit.getOnlinePlayers();
         for (Player player : players) {
             if (player.isOnline()) {
                 player.sendMessage(message);
             }
         }
     }
 }
