 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.entity.*;
 
 /**
  * Description.
  * @author UndeadScythes
  */
 public class EntityDeath extends ListenerWrapper implements Listener {
     @EventHandler
     public void onEvent(EntityDeathEvent event) {
         final Entity victim = event.getEntity();
         final EntityDamageEvent damageEvent = victim.getLastDamageCause();
         if(damageEvent instanceof EntityDamageByEntityEvent) {
             final Entity killer = getAbsoluteEntity(((EntityDamageByEntityEvent) damageEvent).getDamager());
             if(killer instanceof Player) {
                 if(killer instanceof Player) {
                     payReward(UDSPlugin.getPlayers().get(((Player)killer).getName()), victim);
                 }
             } else if(killer instanceof Tameable) {
                 final Tameable pet = (Tameable) killer;
                 if(pet.isTamed()) {
                     payReward(UDSPlugin.getPlayers().get(((Player)pet.getOwner()).getName()), victim);
                 }
             }
         }
     }
 
     private void payReward(final SaveablePlayer player, final Entity victim) {
         if(player.getGameMode() == GameMode.SURVIVAL && !player.hasGodMode()) {
             final Random generator = new Random();
            final int reward = (int)(Config.MOB_REWARDS.get(victim.getClass().getName().substring(35).toLowerCase()) * generator.nextDouble());
             player.credit(reward);
            player.sendMessage(Color.MESSAGE + "You picked up " + reward + " " + (reward == 1 ? Config.CURRENCY : Config.CURRENCIES) + ".");
         }
     }
 }
