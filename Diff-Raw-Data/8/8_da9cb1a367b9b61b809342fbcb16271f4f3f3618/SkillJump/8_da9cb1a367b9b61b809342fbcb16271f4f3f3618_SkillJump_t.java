 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillJump extends ActiveSkill {
 
     // TODO: Register this command in Heroes
     public SkillJump(Heroes plugin) {
         super(plugin);
         name = "Jump";
         description = "Skill - Jump";
         usage = "/skill jump";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill jump");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         float pitch = player.getEyeLocation().getPitch();
         if (pitch > 0) {
             pitch = -pitch;
         }
         float multiplier = (90f + pitch) / 40f;
         Vector v = player.getVelocity().setY(1).add(player.getLocation().getDirection().setY(0).normalize().multiply(multiplier));
         player.setVelocity(v);
        player.setFallDistance(-5f);
         return true;
     }
 }
