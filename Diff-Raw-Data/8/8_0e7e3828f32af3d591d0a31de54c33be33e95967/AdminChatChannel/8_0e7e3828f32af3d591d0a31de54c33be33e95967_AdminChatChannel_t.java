 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.zilo.DTplugin.Listener;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class AdminChatChannel implements Listener
 {
     public static List<String> playerName = new ArrayList<String>();
    String prefix = ChatColor.YELLOW + "["         + 
                  ChatColor.GREEN  + "Admin Ch"  +
                  ChatColor.YELLOW + "] "        +
                  ChatColor.WHITE;
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerChat(PlayerChatEvent event) 
     {
         String pName = event.getPlayer().getName();
         
         if (playerName.contains(pName))
         {
             event.setCancelled(true);
             
            String send = prefix + pName  + " : " + event.getMessage();
             
             for(Player p:Bukkit.getServer().getOnlinePlayers())
             {
                 if (p.hasPermission("DTplugin.adminChannel"))
                 {
                    p.sendMessage(send);
                 }
             }
         }
     }
 }
