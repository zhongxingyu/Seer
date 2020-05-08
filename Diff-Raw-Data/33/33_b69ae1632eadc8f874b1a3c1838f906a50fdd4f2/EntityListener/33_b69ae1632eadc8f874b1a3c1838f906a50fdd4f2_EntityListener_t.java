 package com.minecarts.verrier.lockdown.listener;
 
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Entity;
 import com.minecarts.verrier.lockdown.*;
 
/*
 import org.bukkit.event.painting.PaintingCreateEvent;
 import org.bukkit.event.painting.PaintingRemoveEvent;
 import org.bukkit.event.painting.PaintingRemoveEvent.RemoveCause;
 import org.bukkit.event.painting.PaintingRemoveByEntityEvent;
*/
 import org.bukkit.event.entity.*;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 public class EntityListener extends org.bukkit.event.entity.EntityListener {
     private Lockdown plugin;
     public EntityListener(Lockdown instance)
     {
         plugin = instance;
     }
 //PVP
    @Override
     public void onEntityDamage(EntityDamageEvent event){
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         
         //PVP entity protection
         if(event instanceof EntityDamageByEntityEvent){
             EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
             Entity attacker = e.getDamager();
             Entity defender = e.getEntity();
             if(attacker instanceof Player && defender instanceof Player){
                 e.setCancelled(true);
                 plugin.informPlayer((Player) attacker);
             }
         } else
         //PVP arrow protection
         if(event instanceof EntityDamageByProjectileEvent){
             EntityDamageByProjectileEvent e = (EntityDamageByProjectileEvent) event;
             Entity attacker = e.getDamager();
             Entity defender = e.getEntity();
             if(attacker instanceof Player && defender instanceof Player){
                 e.setCancelled(true);
                 plugin.informPlayer((Player) attacker);
             }
         } else {
         // Something else.. do we need to block anything?
         }
         
     }
 //Explosions
     public void onEntityExplode(EntityExplodeEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
     }
 //Painting events
/*
     public void onPaintingCreate(PaintingCreateEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
         plugin.informPlayer(event.getPlayer());
     }
     public void onPaintingRemove(PaintingRemoveEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
         
         //If it was removed by a player, let that player know
         if(event.getCause() == RemoveCause.ENTITY){
             PaintingRemoveByEntityEvent prbe = ((PaintingRemoveByEntityEvent) event);
             if(prbe.getRemover() instanceof Player){
                 plugin.informPlayer(((Player) prbe.getRemover()));
             }
         }
     }
*/
 }
