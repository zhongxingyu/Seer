 package fr.ethilvan.bukkit.dimensions.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
 
 import fr.ethilvan.bukkit.api.EthilVan;
 import fr.ethilvan.bukkit.api.dimensions.Dimension;
 import fr.ethilvan.bukkit.api.dimensions.Dimensions;
 import fr.ethilvan.bukkit.api.event.dimensions.DimensionEnterEvent;
 import fr.ethilvan.bukkit.api.event.dimensions.DimensionLeaveEvent;
 import fr.ethilvan.bukkit.api.event.dimensions.DimensionRespawnEvent;
 
 public class EventAdapter implements Listener {
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
         Player player = event.getPlayer();
 
         Dimensions dimensions = EthilVan.getDimensions();
         Dimension from = dimensions.get(event.getFrom());
         Dimension to = dimensions.get(player.getWorld());
         if (from != to) {
             DimensionLeaveEvent leaveEvent =
                     new DimensionLeaveEvent(from, player);
             Bukkit.getPluginManager().callEvent(leaveEvent);
             DimensionEnterEvent enterEvent =
                     new DimensionEnterEvent(to, player, from);
             Bukkit.getPluginManager().callEvent(enterEvent);
 
             if (enterEvent.getChangeGameMode()) {
                 player.setGameMode(to.getConfig().getGameMode());
             }
             if (enterEvent.getChangeInventories()) {
                 EthilVan.getInventories().replace(player,
                         inventoryName(player, from),
                         inventoryName(player, to));
             }
            if (from.getConfig().hasFlag("reset-potion")) {
                for (PotionEffect potion : player.getActivePotionEffects()) {
                    player.removePotionEffect(potion.getType());
                }
            }
         }
     }
 
     private String inventoryName(Player player, Dimension dimension) {
         return "dimensions." + dimension.getName() + "." + player.getName();
     }
 
     @EventHandler
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         Dimension dim = EthilVan.getDimensions().get(event.getPlayer());
         DimensionRespawnEvent respawnEvent =
                 new DimensionRespawnEvent(dim, event);
         Bukkit.getPluginManager().callEvent(respawnEvent);
     }
 }
