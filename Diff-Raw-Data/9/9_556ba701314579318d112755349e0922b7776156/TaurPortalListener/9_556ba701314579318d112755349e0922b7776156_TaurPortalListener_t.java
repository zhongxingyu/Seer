 package me.embarker.tauryuu.taurportal;
 
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class TaurPortalListener extends PlayerListener {
 	
     public void onPlayerPortal(PlayerPortalEvent event) {
     	event.setCancelled(true);
     	return;
     }
     
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         Player player = event.getPlayer();
         World targetWorld = player.getWorld();
         if(targetWorld.getEnvironment() == Environment.THE_END) {
         	event.setCancelled(true);
    		Location spawn = player.getWorld().getSpawnLocation();
        	spawn.setX(spawn.getBlockX() + 0.5);
        	spawn.setY(spawn.getBlockY());
        	spawn.setZ(spawn.getBlockZ() + 0.5);
                 player.teleport(spawn);
         }
 }
     
 }
