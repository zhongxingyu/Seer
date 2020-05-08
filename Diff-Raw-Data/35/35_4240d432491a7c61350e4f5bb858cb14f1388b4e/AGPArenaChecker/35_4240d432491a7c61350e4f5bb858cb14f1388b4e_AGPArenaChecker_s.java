 package com.ACStache.ArenaGodPlus;
 
 import net.slipcor.pvparena.api.PVPArenaAPI;
 
 import org.bukkit.entity.Player;
 
 public class AGPArenaChecker
 {
     /**
      * Checks to see if a player is in a MobArena arena
      * @param player the player being checked
      * @return true or false
      */
     public static boolean isPlayerInMAArena(Player player)
     {
        return (ArenaGodPlus.am.getArenaWithPlayer(player) != null && ArenaGodPlus.am.getArenaWithPlayer(player).inArena(player));
     }
     
     public static boolean isPlayerInPVPArena(Player player)
     {
        return !PVPArenaAPI.getArenaName(player).equals("");
     }
 }
