 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 
 public class SkillDrainsoul extends TargettedSkill {
 
     public SkillDrainsoul(Heroes plugin) {
         super(plugin);
         name = "Drainsoul";
         description = "Absorb health from target";
         minArgs = 0;
         maxArgs = 1;
         identifiers.add("skill drainsoul");
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("absorb-amount", 4);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
 
         if (target instanceof Player) {
             Player targetPlayer = (Player) target;
             if (targetPlayer.getName().equalsIgnoreCase(player.getName())) {
                 return false;
             }
         }
 
         // Throw a dummy damage event to make it obey PvP restricting plugins
         EntityDamageEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.ENTITY_ATTACK, 0);
         plugin.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
             return false;
         }
 
         int absorbamount = getSetting(hero.getHeroClass(), "absorb-amount", 4);
 
         if ((hero.getPlayer().getHealth() + absorbamount) > 10) {
             absorbamount = (10 - hero.getPlayer().getHealth());
         }
 
         player.setHealth(player.getHealth() + absorbamount);
         target.damage(absorbamount);
 
         notifyNearbyPlayers(hero.getPlayer().getLocation(), useText, hero.getPlayer().getName(), name, getEntityName(target));
         return true;
     }
 
 }
