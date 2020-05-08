 package com.meggawatts.MeggaChat;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener {
 
     @EventHandler
     public void PlayerLogin(PlayerJoinEvent event) {
         if (event.getPlayer().hasPermission("meggachat.fly")) {
             event.getPlayer().setAllowFlight(true);
         }
     }
 }
