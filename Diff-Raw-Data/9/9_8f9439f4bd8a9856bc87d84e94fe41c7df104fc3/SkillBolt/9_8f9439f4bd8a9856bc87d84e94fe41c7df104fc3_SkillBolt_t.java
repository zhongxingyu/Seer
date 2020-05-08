 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillBolt extends TargettedSkill {
 
     public SkillBolt(Heroes plugin) {
         super(plugin);
         name = "Bolt";
         description = "Calls a bolt of thunder down on the target";
         usage = "/skill bolt [target]";
         minArgs = 0;
         maxArgs = 1;
         identifiers.add("skill bolt");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
 
         if (target == player) {
             Messaging.send(player, "You need a target.");
             return false;
         }
 
         EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, 0);
         plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
         if (damageEntityEvent.isCancelled()) {
             return false;
         }
 
         int radius = getSetting(hero.getHeroClass(), "radius", 10);
         List<Entity> entityList = target.getNearbyEntities(radius, radius, radius);
         for (Entity n : entityList) {
             if (n instanceof LivingEntity) {
                 if (n != player) {
                     // Throw a dummy damage event to make it obey PvP restricting plugins
                     EntityDamageEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.ENTITY_ATTACK, 0);
                     plugin.getServer().getPluginManager().callEvent(event);
                     if (!event.isCancelled()) {
                         target.getWorld().strikeLightning(n.getLocation());
                     }
                 }
             }
         }
         target.getWorld().strikeLightning(target.getLocation());
 
         notifyNearbyPlayers(player.getLocation(), useText, player.getName(), name, target == player ? "himself" : getEntityName(target));
        return true;
     }
 }
