 package com.hotmail.shinyclef.shinychannels;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import java.util.HashSet;
 
 /**
  * User: Peter
  * Date: 14/07/13
  * Time: 5:23 AM
  */
 
 public class EventListener implements Listener
 {
     private ShinyChannels plugin;
 
     public EventListener(ShinyChannels plugin)
     {
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     public void eventCommandPreprocess(PlayerCommandPreprocessEvent e)
     {
         final String message = e.getMessage().trim();
        if (message.startsWith("/mb"))
         {
             e.setCancelled(true);
 
             String sentence;
             if (e.getMessage().length() < 5)
             {
                 sentence = null;
             }
             else
             {
                 sentence = e.getMessage().substring(4);
             }
 
             StaffChat.modChat(e.getPlayer(), sentence);
         }
     }
 }
