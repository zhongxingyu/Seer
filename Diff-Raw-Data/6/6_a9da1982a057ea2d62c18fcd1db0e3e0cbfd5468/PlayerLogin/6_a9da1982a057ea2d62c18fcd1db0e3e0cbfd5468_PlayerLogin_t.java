 package com.undeadscythes.udsplugin.eventhandlers;
 
 import org.bukkit.*;
import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 /**
  *
  * @author Dave
  */
 public class PlayerLogin implements Listener {
    @EventHandler
     public void onEvent(final PlayerLoginEvent event) {
        if(Bukkit.hasWhitelist() && !event.getPlayer().isWhitelisted()) {
             event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "The server is currently whitelisted while we make major improvements. Thank you for your patience.");
         }
     }
 }
