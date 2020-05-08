 package net.robinjam.bukkit.eternalwolf;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 /**
  *
  * @author robinjam
  */
 public class EntityListener extends org.bukkit.event.entity.EntityListener {
 
     private EternalWolf plugin;
 
     public EntityListener(EternalWolf instance) {
         plugin = instance;
     }
 
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         // If the entity that was damaged is a wolf
         if (event.getEntity() instanceof Wolf) {
             Wolf wolf = (Wolf) event.getEntity();
 
             // ...and the wolf has an owner
             if (wolf.isTamed()) {
 
                 // If the wolf was damaged by another entity
                 if (event instanceof EntityDamageByEntityEvent) {
                     EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
 
                     // If the wolf was damaged by its owner using a bone
                     if (damageEvent.getDamager() instanceof Player && ((Player)damageEvent.getDamager()).getName().equals(EternalWolf.getWolfOwnerName(wolf))) {
                         Player owner = (Player) wolf.getOwner();
 
                         // Check if the player has permission to release their own wolves
                         if (owner.getItemInHand().getType() == Material.BONE && plugin.playerHasPermission(owner, "eternalwolf.release_own_wolves", true)) {
                             // Release the wolf
                             wolf.setOwner(null);
                             wolf.setSitting(false);
                             owner.sendMessage(ChatColor.RED + "You have released your wolf!");
                         }
 
                         event.setCancelled(true);
                     }
 
                     // If the player has permission to release other peoples' wolves
                     else if(damageEvent.getDamager() instanceof Player) {
                         Player attacker = (Player) damageEvent.getDamager();
                         if (plugin.playerHasPermission(attacker, "eternalwolf.release_other_wolves", attacker.isOp()) && attacker.getItemInHand().getType() == Material.BONE) {
                             attacker.sendMessage(ChatColor.RED + "You have released " + EternalWolf.getWolfOwnerName(wolf) + "'s wolf!");
                             if (wolf.getOwner() instanceof Player && ((Player)wolf.getOwner()).isOnline())
                                 ((Player)wolf.getOwner()).sendMessage(ChatColor.RED + attacker.getDisplayName() + " has released your wolf!");
                             
                             wolf.setOwner(null);
                             wolf.setSitting(false);
                             event.setCancelled(true);
                         }
                     }
                 }
 
                 // If the wolf's owner is offline or they have permission to have invincible wolves, cancel the event
                 if (wolf.getOwner() instanceof Player) {
                     Player owner = (Player) wolf.getOwner();
                     if (!owner.isOnline() || plugin.playerHasPermission(owner, "eternalwolf.invincible_wolves", true)) {
                         event.setCancelled(true);
                     }
                 } else {
                     event.setCancelled(true);
                 }
             }
         }
 
         // The following is a bugfix to prevent tamed wolves becoming agressive
         // if their owner attacks himself with an arrow
 
         if (event.getEntity() instanceof Player && event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
 
            if (damageEvent.getDamager().equals(damageEvent.getEntity()))
                 event.setCancelled(true);
         }
     }
     
 }
