 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillWolf extends ActiveSkill {
 
     public HashMap<Player, Integer> wolves = new HashMap<Player, Integer>();
 
     public SkillWolf(Heroes plugin) {
         super(plugin);
         name = "Wolf";
         description = "Summons and tames a wolf to your side";
         usage = "/skill wolf";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill wolf");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         if (!wolves.containsKey(player) || wolves.get(player) <= 3) {
             LivingEntity le = player.getWorld().spawnCreature(hero.getPlayer().getLocation(), CreatureType.WOLF);
             Wolf wolf = (Wolf) le;
             wolf.setOwner(player);
             wolf.setTamed(true);
            wolves.put(player, wolves.get(player) + 1);
             return true;
         } else {
             Messaging.send(player, "Sorry, you have too many wolves already", (String[]) null);
             return false;
         }
     }
 }
