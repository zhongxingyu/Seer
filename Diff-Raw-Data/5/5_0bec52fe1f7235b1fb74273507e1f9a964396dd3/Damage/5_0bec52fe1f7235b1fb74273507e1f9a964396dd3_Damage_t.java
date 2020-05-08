 package edgruberman.bukkit.obituaries;
 
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Capture volatile information at time of damage.
  */
 class Damage {
 
     final EntityDamageEvent event;
     final BlockState sourceBlock;
     final ItemStack sourceItem;
 
     Damage(final EntityDamageEvent event) {
         this.event = event;
 
         // Capture volatile information
         switch (event.getCause()) {
 
         case CONTACT:
             // Store block state as it could change between damage event and death event
             this.sourceBlock = ((EntityDamageByBlockEvent) event).getDamager().getState();
             this.sourceItem = null;
             break;
 
         case SUFFOCATION:
             // Identify current block at player's top half since player model will drop to floor before death
             final Player victim = (Player) event.getEntity();
             this.sourceBlock = victim.getEyeLocation().getBlock().getState();
             this.sourceItem = null;
             break;
 
         case ENTITY_ATTACK:
             // Death event could be thrown after source item is no longer in damager's hand
             final Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
             if (damager instanceof Player) {
                 final Player damagerPlayer = (Player) damager;
                 this.sourceItem = damagerPlayer.getItemInHand().clone();
             } else {
                 this.sourceItem = null;
             }
             this.sourceBlock = null;
             break;
 
         default:
             this.sourceBlock = null;
             this.sourceItem = null;
 
         }
 
     }
 
}
