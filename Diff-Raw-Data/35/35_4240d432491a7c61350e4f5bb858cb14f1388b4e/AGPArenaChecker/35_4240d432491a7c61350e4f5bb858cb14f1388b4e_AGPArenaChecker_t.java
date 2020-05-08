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
        if(ArenaGodPlus.getMA())
            return (ArenaGodPlus.am.getArenaWithPlayer(player) != null && ArenaGodPlus.am.getArenaWithPlayer(player).inArena(player));
        else
            return false;
     }
     
     public static boolean isPlayerInPVPArena(Player player)
     {
        if(ArenaGodPlus.getPVP())
            return !PVPArenaAPI.getArenaName(player).equals("");
        else
            return false;
     }
 }
