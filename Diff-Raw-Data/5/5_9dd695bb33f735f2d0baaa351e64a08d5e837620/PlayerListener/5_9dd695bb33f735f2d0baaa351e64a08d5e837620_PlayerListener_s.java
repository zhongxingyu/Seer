 package com.norcode.bukkit.livestocklock;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 public class PlayerListener implements Listener {
 
     LivestockLock plugin;
 
     public PlayerListener(LivestockLock plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
         OwnedAnimal oa = plugin.getOwnedAnimal(event.getEntity().getUniqueId());
         if (oa == null) return;
         Player damager = null;
         if (event.getDamager().getType() == EntityType.PLAYER) {
             damager = (Player) event.getDamager();
         } else if (event.getEntity() instanceof Projectile) {
             Projectile p = (Projectile) event.getDamager();
             if (p.getShooter() instanceof Player) {
                 damager = (Player) p.getShooter();
             }
         }
        if (damager != null && !oa.allowAccess(damager)) {
             event.setCancelled(true);
             damager.sendMessage("That animal belongs to " + oa.getOwnerName());
             return;
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Player player = event.getPlayer();
         Entity animal = event.getRightClicked();
         if (plugin.getOwnedAnimals().containsKey(animal.getUniqueId())) {
             // This animal is owned, check for permission.
             OwnedAnimal oa = plugin.getOwnedAnimal(animal.getUniqueId());
            if (!oa.allowAccess(event.getPlayer())) {
                 player.sendMessage("Sorry, that animal belongs to " + oa.getOwnerName());
                 event.setCancelled(true);
             }
         } else if (player.hasMetadata("livestocklock-claim-pending")) {
             String ownerName = player.getMetadata("livestocklock-claim-pending").get(0).asString();
             player.removeMetadata("livestocklock-claim-pending", plugin);
             if (plugin.getAllowedAnimals().containsKey(animal.getType().getTypeId())) {
                 if (player.hasPermission("livestocklock.claim." + animal.getType().getTypeId())) {
                     ClaimableAnimal ca = plugin.getAllowedAnimals().get(animal.getType().getTypeId());
                     event.setCancelled(true);
                     if (ca.takeCost(player)) {
                         OwnedAnimal oa = new OwnedAnimal(plugin, animal.getUniqueId(), ownerName);
                         plugin.getOwnedAnimals().put(animal.getUniqueId(), oa);
                     } else {
                         player.sendMessage("Sorry, you don't have " + ca.getCostDescription());
                     }
                 }
             }
         }
     }
 }
