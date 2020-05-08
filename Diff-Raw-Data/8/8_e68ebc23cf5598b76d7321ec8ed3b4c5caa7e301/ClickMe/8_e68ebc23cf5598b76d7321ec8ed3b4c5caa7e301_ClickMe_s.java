 package net.digiex.clickme;
 
 import java.util.logging.Logger;
 
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ClickMe extends JavaPlugin {
 
     private Logger log;
    private Listener clickmelistener = new cmListener();
 
     @Override
     public void onDisable() {
         log.info("is now disabled.");
     }
 
     @Override
     public void onEnable() {
         log = getLogger();
        getServer().getPluginManager().registerEvents(clickmelistener, this);
         log.info("is now enabled!");
     }
 
     public boolean has(Player player) {
         return player.hasPermission("clickme.player");
     }
 
     public boolean hasConsole(Player player) {
         return player.hasPermission("clickme.console");
     }
 
     private class cmListener implements Listener {
 
         @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
         public void onPlayerInteract(PlayerInteractEvent event) {
             if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)
                     || ((event.getAction() == Action.LEFT_CLICK_BLOCK))) {
                 BlockState state = event.getClickedBlock().getState();
                 Player player = event.getPlayer();
                 if ((state instanceof Sign)) {
                     Sign sign = (Sign) state;
                     if (sign.getLines()[0].equalsIgnoreCase("[ClickMe]")) {
                         if (has(event.getPlayer())) {
                             if (sign.getLines()[1].equalsIgnoreCase("console")) {
                                 if (hasConsole(player)) {
                                     getServer()
                                             .dispatchCommand(
                                             getServer().getConsoleSender(),
                                             sign.getLines()[2].toString()
                                             + sign.getLines()[3]
                                             .toString());
                                 } else {
                                     player.sendMessage("You don't have the required permissions for this...");
                                 }
                             } else {
                                 getServer().dispatchCommand(
                                         event.getPlayer(),
                                         sign.getLines()[1].toString()
                                         + sign.getLines()[2].toString()
                                         + sign.getLines()[3].toString());
                             }
                         } else {
                             player.sendMessage("You don't have the required permissions for this...");
                         }
                     }
 
                 }
             }
         }
     }
}
