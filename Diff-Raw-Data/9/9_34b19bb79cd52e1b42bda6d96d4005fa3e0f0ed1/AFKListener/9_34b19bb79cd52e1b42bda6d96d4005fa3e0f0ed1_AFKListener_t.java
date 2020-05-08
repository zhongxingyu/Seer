 package de.minestar.SinCity.Listener;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import de.minestar.SinCity.Manager.PlayerManager;
 
 public class AFKListener implements Listener {
 
     private final PlayerManager playerManager;
 
     public AFKListener(PlayerManager playerManager) {
         this.playerManager = playerManager;
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerChat(PlayerChatEvent event) {
         // EVENT IS CANCELLED? => RETURN
         if (event.isCancelled())
             return;
 
        // WE NEED TEXTS!
        if (event.getMessage().length() < 1)
            return;

         // UPDATE EVENT-STATE
         this.playerManager.getPlayer(event.getPlayer()).setLastLocation(event.getPlayer().getLocation());
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
         // EVENT IS CANCELLED? => RETURN
         if (event.isCancelled())
             return;
 
        // WE NEED TEXTS!
        if (event.getMessage().length() < 1)
            return;

         // UPDATE EVENT-STATE
         this.playerManager.getPlayer(event.getPlayer()).setLastLocation(event.getPlayer().getLocation());
     }
 }
