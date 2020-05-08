 package com.meggawatts.MeggaChat;
 
import java.util.logging.Logger;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener {
 
    public static final Logger log = Logger.getLogger("Minecraft");

     @EventHandler
     public void PlayerLogin(PlayerJoinEvent event) {
         if (event.getPlayer().hasPermission("meggachat.fly")) {
             event.getPlayer().setAllowFlight(true);
            log.info(event.getPlayer().getName() + " is on the list, allowing flight.");
         }
     }
 }
