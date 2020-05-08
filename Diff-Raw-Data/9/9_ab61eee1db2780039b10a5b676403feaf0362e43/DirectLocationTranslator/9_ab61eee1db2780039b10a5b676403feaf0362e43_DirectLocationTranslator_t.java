 /*
  */
 package com.github.omwah.SDFEconomy;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 
 /**
  * Returns the location of a player exactly.
  */
 public class DirectLocationTranslator implements LocationTranslator {
     private Server server;
     
     public DirectLocationTranslator(Server server) {
         this.server = server;
     }
 
 
     public String getLocationName(String playerName) {
         OfflinePlayer offlinePlayer = server.getOfflinePlayer(playerName);
         Player onlinePlayer = offlinePlayer.getPlayer();
         if (onlinePlayer != null) {
             return onlinePlayer.getLocation().getWorld().getName();
        } else if (offlinePlayer.hasPlayedBefore()) {
            // Make sure the offline player has played before or the call to
            // getBedSpawnLocation will cause a null pointer inside of bukkit
             Location bedLocation = offlinePlayer.getBedSpawnLocation();
             if (bedLocation != null) {
                 return bedLocation.getWorld().getName();
             }
         }
        return null;
     }
 }
