 package me.supermaxman.portimort;
 
 
 import java.util.ArrayList;
 
 import me.supermaxman.portimort.executors.PlayerTpAcceptExecutor;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class PortimortListener implements Listener {
     final Portimort pi;
 
     public PortimortListener(Portimort pi) {
         this.pi = pi;
     }
 
 
 
    @EventHandler
     public void onPlayerLeave(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         
         Utils.checkList(player);
         Portimort.tprequests.remove(player.getName());
         PlayerTpAcceptExecutor.tphere.remove(player.getName());
         for(ArrayList<String> s:Portimort.tprequests.values()) {
         	s.remove(player.getName().toLowerCase());
         }
         
     }
     
 }
