 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Zombie;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillUnholyRitual extends TargettedSkill {
 
     public SkillUnholyRitual(Heroes plugin) {
         super(plugin, "UnholyRitual");
         setDescription("Target Zombie or Skeleton is sacrificed, necromancer receives mana");
        setUsage("/skill unholyritual");
        setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill unholyritual"});
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
        
        if (!(target instanceof Zombie) || !(target instanceof Skeleton) || (Player) target == player) {
             Messaging.send(player, "You need a target!");
             return false;
         }
         getPlugin().getDamageManager().addSpellTarget((Entity) target);
         target.damage(target.getHealth(), player);
         hero.setMana(hero.getMana() + 20);
         broadcastExecuteText(hero, target);
         return true;
     }
 
 }
