 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
import org.bukkit.Location;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 
 /**
  * When a player respawns.
  * @author UndeadScythes
  */
 public class PlayerRespawn extends ListenerWrapper implements Listener {
     @EventHandler
    public void onEvent(final PlayerRespawnEvent event) {
         final SaveablePlayer player = UDSPlugin.getOnlinePlayers().get(event.getPlayer().getName());
        Location location = null;
        Region home;
        if((home = UDSPlugin.getHomes().get(player.getName() + "home")) != null) {
            location = home.getWarp();
        }
        if(location == null) {
            event.setRespawnLocation(UDSPlugin.getData().getSpawn());
        } else {
            event.setRespawnLocation(Warp.findSafePlace(location));
         }
         if(player.isDuelling()) {
             player.endChallenge();
             player.sendMessage(Color.MESSAGE + "The duel is over.");
             player.loadItems();
         }
         if(player.hasPermission(Perm.BACK_ON_DEATH)) {
             player.sendMessage(Color.MESSAGE + "Use /back to return to where you died.");
         }
     }
 }
