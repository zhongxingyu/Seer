 package com.herocraftonline.dev.heroes.skill.skills;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillReplenish extends ActiveSkill {
 
     public SkillReplenish(Heroes plugin) {
         super(plugin);
         name = "Replenish";
         description = "Brings your mana back to full";
         usage = "/skill replenish";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill replenish");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         hero.setMana(100);
         Messaging.send(hero.getPlayer(), "Your mana has been fully replenished!");
         if (hero.isVerbose()) {
             Messaging.send(hero.getPlayer(), Messaging.createManaBar(100));
         }
         notifyNearbyPlayers(hero.getPlayer().getLocation(), useText, hero.getPlayer().getName(), name);
        return false;
     }
 
 }
