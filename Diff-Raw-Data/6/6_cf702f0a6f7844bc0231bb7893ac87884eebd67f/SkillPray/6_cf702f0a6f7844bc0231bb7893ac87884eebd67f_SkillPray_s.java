 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillPray extends ActiveSkill{
 
     public SkillPray(Heroes plugin) {
         super(plugin);
         name = "Pray";
         description = "Heals you for a certain amount of health";
         usage = "/skill pray";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill pray");
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("heal-amount", 4);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         int healamount = getSetting(hero.getHeroClass(), "heal-amount", 4);
         hero.getPlayer().setHealth(hero.getPlayer().getHealth() + healamount);
        return false;
     }
 
 }
