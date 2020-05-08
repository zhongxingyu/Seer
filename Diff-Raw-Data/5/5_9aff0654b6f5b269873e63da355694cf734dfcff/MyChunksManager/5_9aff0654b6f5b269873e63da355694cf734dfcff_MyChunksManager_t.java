 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.betterverse.compass.bukkit.hooks;
 
 import net.betterverse.mychunks.bukkit.MyChunks;
 import net.betterverse.mychunks.bukkit.chunk.OwnedChunk;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 /**
  *
  * @author Julian
  */
 public class MyChunksManager {
     
     private static MyChunks plugin;
     
     public static void setMyChunks(Plugin plugin) {
         if(plugin == null)
             MyChunksManager.plugin = null;
         else
             MyChunksManager.plugin = (MyChunks) plugin;
     }
     
     public static boolean canWarpFrom(Player player) {
         if(plugin == null)
             return true;
         Location location = player.getLocation();
         String world = location.getWorld().getName();
         int x = location.getChunk().getX();
         int z = location.getChunk().getZ();
         OwnedChunk c = plugin.getChunkManager().getOwnedChunk(world, x, z);
         if(c == null)
             return true;
         if(c.isOwner(player.getName()))
             return true;
        if(c.isPermitted(player.getName()))
             return true;
         return false;
     }
     
     public static boolean canWarpTo(Player player, Location location) {
         if(plugin == null)
             return true;
         String world = location.getWorld().getName();
         int x = location.getChunk().getX();
         int z = location.getChunk().getZ();
         OwnedChunk c = plugin.getChunkManager().getOwnedChunk(world, x, z);
         if(c == null)
             return true;
         if(c.isOwner(player.getName()))
             return true;
        if(c.isPermitted(player.getName()))
             return true;
         return false;
     }
 }
