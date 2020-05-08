 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.persistence.HeroEffects;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 
 public class SkillSafefallOther extends TargettedSkill {
 
     public SkillSafefallOther(Heroes plugin) {
         super(plugin);
         name = "SafefallOther";
         description = "Skill - Safefall";
        usage = "/skill safefallother <target>";
         minArgs = 0;
        maxArgs = 1;
         identifiers.add("skill safefallother");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("duration", 5000);
         return node;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || event.getCause() != DamageCause.FALL) {
                 return;
             }
 
             Entity defender = event.getEntity();
             if (defender instanceof Player) {
                 Player player = (Player) defender;
                 HeroEffects effects = plugin.getHeroManager().getHero(player).getEffects();
                 if (effects.hasEffect(name)) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         String playerName = player.getName();
         if (target instanceof Player) {
             Hero newHero = plugin.getHeroManager().getHero((Player) target);
             double duration = getSetting(hero.getHeroClass(), "duration", 5000);
             newHero.getEffects().putEffect(name, duration);
            notifyNearbyPlayers(player.getLocation(), useText, player.getName(), name, target == player ? "himself" : getEntityName(target));
             return true;
         } else {
             return false;
         }
 
     }
 }
