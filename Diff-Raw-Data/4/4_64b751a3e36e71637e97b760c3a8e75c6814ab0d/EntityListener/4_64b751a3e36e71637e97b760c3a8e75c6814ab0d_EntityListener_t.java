 package ca.q0r.madvanced.events;
 
 import ca.q0r.madvanced.MAdvanced;
import ca.q0r.madvanced.types.ConfigType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 public class EntityListener implements Listener {
     MAdvanced plugin;
 
     public EntityListener(MAdvanced instance) {
         plugin = instance;
     }
 
     Boolean messageTimeout = true;
 
     @EventHandler
     public void onEntityDamage(EntityDamageEvent event) {
         if (event.isCancelled())
             return;
 
         if (event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             Entity attacker = subEvent.getDamager();
             Entity damaged = subEvent.getEntity();
 
             if (attacker instanceof Player) {
                 Player player = (Player) attacker;
 
                 if (plugin.isAFK.get(player.getName()) == null)
                     return;
 
                if (plugin.isAFK.get(player.getName()) && ConfigType.OPTION_HC_AFK.getBoolean()) {
                     damaged.setLastDamageCause(null);
                     event.setCancelled(true);
                 }
             }
         }
 
         if (event.getEntity() instanceof Player) {
             Player player = (Player) event.getEntity();
 
             if (plugin.isAFK.get(player.getName()) != null)
                 if (plugin.isAFK.get(player.getName()))
                     event.setCancelled(true);
         }
     }
 }
