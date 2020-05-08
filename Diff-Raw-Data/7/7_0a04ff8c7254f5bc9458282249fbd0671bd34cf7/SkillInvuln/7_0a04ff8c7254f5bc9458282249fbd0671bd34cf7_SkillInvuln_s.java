 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import java.util.Map;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillInvuln extends ActiveSkill {
 
     public SkillInvuln(Heroes plugin) {
         super(plugin);
         name = "Invuln";
         description = "Skill - Invuln";
         usage = "/skill invuln";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill invuln");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         hero.getEffects().put(name, System.currentTimeMillis() + 10000.0);
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             Entity defender = event.getEntity();
             if (defender instanceof Player) {
                 Player player = (Player) defender;
                 Map<String, Double> effects = plugin.getHeroManager().getHero(player).getEffects();
                if (effects.containsKey(name) && effects.get(name) < System.currentTimeMillis()) {
                     event.setCancelled(true);
                 }
             }
         }
 
     }
 }
