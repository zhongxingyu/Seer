 package com.hawkfalcon.deathswap.Game;
 
 import org.bukkit.entity.Player;
 
 import com.hawkfalcon.deathswap.DeathSwap;
 
 public class Leave {
 
     public DeathSwap plugin;
 
     public Leave(DeathSwap ds) {
         this.plugin = ds;
     }
 
     public void leave(Player player) {
         String name = player.getName();
         if (plugin.lobby.contains(name)) {
            plugin.utility.message("You left the game!", player);
             plugin.utility.broadcastLobby(name + " left the game!");
             plugin.lobby.remove(name);
             plugin.utility.teleport(player, 1);
         }
         if (plugin.game.contains(name)) {
             plugin.utility.restorePlayer(player);
             plugin.utility.teleport(player, 1);
         }
     }
 }
