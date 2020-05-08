 package com.herocraftonline.dev.heroes.skill.skills;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 public class SkillIronBones extends PassiveSkill {
 
     public SkillIronBones(Heroes plugin) {
         super(plugin, "IronBones");
         setDescription("You hit the ground with a thunderous roar!");
         setArgumentRange(0, 0);
         setEffectTypes(EffectType.PHYSICAL, EffectType.BENEFICIAL);
         setTypes(SkillType.PHYSICAL);
 
         registerEvent(Event.Type.ENTITY_DAMAGE, new SkillDamageListener(), Event.Priority.Highest);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("damage", 0.10);
         node.setProperty("radius", 10);
         return node;
     }
 
     public class SkillDamageListener extends EntityListener {
 
         public void onEntityDamage(EntityDamageEvent event) {
             Heroes.debug.startTask("HeroesSkillListener");
             if (event.getCause() != DamageCause.FALL || !(event.getEntity() instanceof Player)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             Player player = (Player) event.getEntity();
             Hero hero = plugin.getHeroManager().getHero(player);
 
             if (!hero.hasEffect(getName())) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             double damage = event.getDamage() * getSetting(hero.getHeroClass(), "damage", 0.10);
             int radius = getSetting(hero.getHeroClass(), "radius", 10);
 
             for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) entity;
                    if (!damageCheck(player, target)) {
                        continue;
                    }
                    target.damage((int) damage, player);
                 }
             }
             Heroes.debug.stopTask("HeroesSkillListener");
         }
     }
 }
