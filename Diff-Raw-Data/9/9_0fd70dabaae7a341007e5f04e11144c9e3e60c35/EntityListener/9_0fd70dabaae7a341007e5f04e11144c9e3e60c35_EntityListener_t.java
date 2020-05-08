 package com.me.tft_02.assassin.Listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import com.me.tft_02.assassin.Assassin;
 import com.me.tft_02.assassin.util.PlayerData;
 
 public class EntityListener implements Listener {
 
     Assassin plugin;
 
     public EntityListener(Assassin instance) {
         plugin = instance;
     }
 
     private PlayerData data = new PlayerData(plugin);
 
     /**
      * Monitor EntityDamageByEntity events.
      * 
      * @param event The event to monitor
      */
    @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 //        if (event instanceof FakeEntityDamageByEntityEvent)
 //            return;
 
         if (event.getDamage() <= 0) return;
 
         Entity attacker = event.getDamager();
         Entity defender = event.getEntity();
 
         if (attacker.hasMetadata("NPC") || defender.hasMetadata("NPC")) return; // Check if either players is are Citizens NPCs
 
         if (attacker instanceof Projectile) {
             attacker = ((Projectile) attacker).getShooter();
         } else if (attacker instanceof Tameable) {
             AnimalTamer animalTamer = ((Tameable) attacker).getOwner();
 
             if (animalTamer instanceof Entity) {
                 attacker = (Entity) animalTamer;
             }
         }
 
         if (defender instanceof Player) {
             Player defendingPlayer = (Player) defender;
 
             if (!defendingPlayer.isOnline()) {
                 return;
             }
 
             if (attacker instanceof Player) {
                 if (data.bothNeutral(defendingPlayer, (Player) attacker) && Assassin.getInstance().getConfig().getBoolean("Assassin.prevent_neutral_pvp")) {
                    if (!event.isCancelled()) {
                        ((Player) attacker).sendMessage(ChatColor.DARK_RED + "You are not an Assassin.");
                        event.setCancelled(true);
                    }
                     return;
                 } else {
                     if (event.isCancelled() && Assassin.getInstance().getConfig().getBoolean("Assassin.override_pvp_prevention")) {
                         event.setCancelled(false);
                     }
                     if (Assassin.getInstance().getConfig().getBoolean("Assassin.particle_effects")) {
                         defendingPlayer.getWorld().playEffect(defendingPlayer.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
                     }
                 }
             }
         }
     }
 }
