 package com.herocraftonline.dev.heroes;
 
 import java.util.HashMap;
 import java.util.Set;
 
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.api.ExperienceGainEvent;
 import com.herocraftonline.dev.heroes.api.LevelEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class HEntityListener extends EntityListener {
 
     private final Heroes plugin;
     private HashMap<Entity, Player> kills = new HashMap<Entity, Player>();
 
     public HEntityListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public void onEntityDeath(EntityDeathEvent event) {
         Entity defender = event.getEntity();
         Player attacker = kills.get(defender);
         kills.remove(defender);
         if (attacker != null) {
             // Get the Hero representing the player
             Hero hero = plugin.getHeroManager().getHero(attacker);
             // Get the player's class definition
             HeroClass playerClass = hero.getPlayerClass();
             // Get the sources of experience for the player's class
             Set<ExperienceType> expSources = playerClass.getExperienceSources();
             // If the player gains experience from killing
             if (expSources.contains(ExperienceType.KILLING)) {
                 if (defender instanceof LivingEntity) {
                     int addedExp = 0;
                     // If the dying entity is a Player
                     if (defender instanceof Player) {
                         addedExp = plugin.getConfigManager().getProperties().playerKillingExp;
                         
                         // Incur 5% experience loss to dying player
                         Hero heroDefender = plugin.getHeroManager().getHero((Player) defender);
                         heroDefender.setExperience((int) (heroDefender.getExperience() * 0.95));
                     } else {
                         // Get the dying entity's CreatureType
                         CreatureType type = null;
                         try {
                             Class<?>[] interfaces = defender.getClass().getInterfaces();
                             for (Class<?> c : interfaces) {
                                 if (LivingEntity.class.isAssignableFrom(c)) {
                                     type = CreatureType.fromName(c.getSimpleName());
                                     break;
                                 }
                             }
                         } catch (IllegalArgumentException e) {
                         }
                         if (type != null) {
                             addedExp = plugin.getConfigManager().getProperties().creatureKillingExp.get(type);
                         }
                     }
                     // Fire the experience gain event
                     int exp = hero.getExperience();
                    int currentLevel = plugin.getConfigManager().getProperties().getLevel(exp + addedExp);
                     int newLevel = plugin.getConfigManager().getProperties().getLevel(exp + addedExp);
                     ExperienceGainEvent expEvent;
                     if (newLevel == currentLevel) {
                         expEvent = new ExperienceGainEvent(attacker, addedExp);
                     } else {
                         expEvent = new LevelEvent(attacker, addedExp, newLevel, currentLevel);
                     }
                     plugin.getServer().getPluginManager().callEvent(expEvent);
                     if (expEvent.isCancelled()) {
                         return;
                     }
                     addedExp = expEvent.getExp();
                     
                     // Only perform an experience update if we're actually
                     // adding or subtracting from their experience.
                     if (addedExp != 0) {
                         hero.setExperience(exp + addedExp);
                         plugin.getMessager().send(attacker, "$1: Gained $2 Exp", playerClass.getName(), String.valueOf(addedExp));
                         if (newLevel != currentLevel) {
                             plugin.getMessager().send(attacker, "You leveled up! (Lvl $1 $2)", String.valueOf(newLevel), playerClass.getName());
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         Entity defender = event.getEntity();
         if (defender instanceof LivingEntity) {
             if (((LivingEntity) defender).getHealth() - event.getDamage() <= 0) {
                 // Grab the Attacker regardless of the Event.
                 Entity attacker = null;
                 if (event instanceof EntityDamageByProjectileEvent) {
                     EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
                     attacker = subEvent.getDamager();
                 } else if (event instanceof EntityDamageByEntityEvent) {
                     EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                     attacker = subEvent.getDamager();
                 }
                 // Check if the Attacker is in the Defenders Party.
                 if (attacker instanceof Player && defender instanceof Player) {
                     HeroParty party = plugin.getHeroManager().getHero((Player) attacker).getParty();
                     if (party != null && party.getMembers().contains(defender)) {
                         event.setCancelled(true);
                         return;
                     }
                 }
                 // If it's a legitimate attack then we add it to the Kills list.
                 if (attacker != null && attacker instanceof Player) {
                     kills.put(defender, (Player) attacker);
                 } else {
                     kills.remove(defender);
                 }
             }
         }
     }
 }
