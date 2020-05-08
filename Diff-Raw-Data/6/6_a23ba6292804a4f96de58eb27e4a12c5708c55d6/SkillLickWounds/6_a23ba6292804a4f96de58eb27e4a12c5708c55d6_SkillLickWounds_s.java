 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillLickWounds extends ActiveSkill {
 
     public SkillLickWounds(Heroes plugin) {
         super(plugin);
         name = "LickWounds";
         description = "Heals all your wolves which are around you";
         usage = "/skill lickwounds";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill lickwounds");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         String playerName = player.getName();
 
         List<Entity> entityList = player.getNearbyEntities(10, 10, 10);
         for (Entity n : entityList) {
             if (n instanceof Wolf) {
                 Wolf nWolf = (Wolf) n;
                 if (nWolf.getOwner() == player) {
                    nWolf.setHealth(nWolf.getHealth() + 30);
                 }
             }
         }
         notifyNearbyPlayers(player.getLocation(), useText, playerName, name);
         return true;
     }
 
 }
