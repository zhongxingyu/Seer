 package com.herocraftonline.dev.heroes.skill.skills;
 
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
         super(plugin);
         name = "UnholyRitual";
         description = "Target Zombie or Skeleton is sacrificed, necromancer receives mana";
         usage = "/skill unholyritual [target]";
         minArgs = 0;
         maxArgs = 1;
         identifiers.add("skill unholyritual");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
        if (!(target instanceof Zombie) || !(target instanceof Skeleton) || (Player) target != player) {
             Messaging.send(player, "You need a target!");
             return false;
         }
         target.damage(target.getHealth());
         hero.setMana(hero.getMana() + 20);
         notifyNearbyPlayers(player.getLocation(), useText, player.getName(), name, target == player ? "himself" : getEntityName(target));
         return true;
     }
 
 }
