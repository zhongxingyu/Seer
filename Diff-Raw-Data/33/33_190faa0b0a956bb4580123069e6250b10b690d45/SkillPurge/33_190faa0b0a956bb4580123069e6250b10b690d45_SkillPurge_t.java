 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillPurge extends TargettedSkill {
 
     public SkillPurge(Heroes plugin) {
         super(plugin, "Purge");
         setDescription("You purge effects near the targets location");
         setUsage("/skill purge");
         setArgumentRange(0, 0);
         setIdentifiers("skill purge");
         setTypes(SkillType.SILENCABLE);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("max-removals", -1);
         node.setProperty(Setting.RADIUS.node(), 10);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (target instanceof Player && (hero.getParty() == null || !hero.getParty().isPartyMember((Player) target))) {
             if (!damageCheck(player, target)) {
                 Messaging.send(player, "Invalid target!");
                 return false;
             }
         }

         int radius = getSetting(hero.getHeroClass(), Setting.RADIUS.node(), 10);
         int removalsLeft = getSetting(hero.getHeroClass(), "max-removals", -1);
         int maxRemovals = removalsLeft;
         for (Entity e : target.getNearbyEntities(radius, radius, radius)) {
             if (removalsLeft == 0)
                 break;
             if (e instanceof Creature)
                 removalsLeft = purge((Creature) e, removalsLeft, hero);
             if (e instanceof Player) {
                 removalsLeft = purge(plugin.getHeroManager().getHero((Player) e), removalsLeft, hero);
             }
         }
 
         if (maxRemovals != removalsLeft) {
             broadcastExecuteText(hero);
             return true;
         } else {
             Messaging.send(player, "No valid targets in range.");
             return false;
         }
     }  

     private int purge(Creature creature, int removalsLeft, Hero hero) {
        //Return immediately if this creature has no effects
        if (plugin.getHeroManager().getCreatureEffects(creature) == null)
            return removalsLeft;
        
         boolean removeHarmful = false;
         if (hero.getSummons().contains(creature))
             removeHarmful = true;
         
         for (Effect effect : plugin.getHeroManager().getCreatureEffects(creature)) {
             if (removalsLeft == 0) {
                 break;
             } else if (effect.isType(EffectType.HARMFUL) && effect.isType(EffectType.DISPELLABLE) && removeHarmful) {
                 plugin.getHeroManager().removeCreatureEffect(creature, effect);
                 removalsLeft--;
             } else if (effect.isType(EffectType.BENEFICIAL) && effect.isType(EffectType.DISPELLABLE) && !removeHarmful) {
                 plugin.getHeroManager().removeCreatureEffect(creature, effect);
                 removalsLeft--;
             }
         }
         return removalsLeft;
     }

     private int purge(Hero tHero, int removalsLeft, Hero hero) {
         boolean removeHarmful = false;
         if (tHero.equals(hero) || (hero.hasParty() && hero.getParty().isPartyMember(tHero))) {
             removeHarmful = true;
         }
         for (Effect effect : tHero.getEffects()) {
             if (removalsLeft == 0) {
                 break;
             } else if (effect.isType(EffectType.HARMFUL) && effect.isType(EffectType.DISPELLABLE) && removeHarmful) {
                 hero.removeEffect(effect);
                 removalsLeft--;
             } else if (effect.isType(EffectType.BENEFICIAL) && effect.isType(EffectType.DISPELLABLE) && !removeHarmful) {
                 hero.removeEffect(effect);
                 removalsLeft--;
             }
         }
         return removalsLeft;
     }
 }
