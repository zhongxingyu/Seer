 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillPort extends ActiveSkill {
 
     public SkillPort(Heroes plugin) {
         super(plugin);
         setName("Port");
         setDescription("Teleports you and your nearby party to the set location!");
         setUsage("/skill port <location>");
         setMinArgs(1);
         setMaxArgs(1);
         getIdentifiers().add("skill port");
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("item-cost", 331);
         return node;
     }
     
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         if (getSetting(hero.getHeroClass(), args[0].toLowerCase(), null) != null) {
             String[] splitArg = getSetting(hero.getHeroClass(), args[0].toLowerCase(), null).split(":");
             int levelRequirement = Integer.parseInt(splitArg[3]);
             
             if(hero.getLevel() < levelRequirement) {
                 Messaging.send(player, "Sorry, you need to be level $1 to use that!", levelRequirement);
                 return false;
             }
             
            if(player.getInventory().contains(Material.matchMaterial(getSetting(hero.getHeroClass(), "itemcost", null)))){
                player.getInventory().remove(Material.matchMaterial(getSetting(hero.getHeroClass(), "itemcost", null)));
             }else {
                 Messaging.send(player, "Sorry, you need to have $1 to use that!", Material.matchMaterial(getSetting(hero.getHeroClass(), "itemcost", null)));
                 return false;   
             }
             
             List<Entity> surrounding = player.getNearbyEntities(10, 10, 10);
             for(Entity n : surrounding) {
                 if(n instanceof Player  ) {
                     Player playerN = (Player)n;
                     if(plugin.getHeroManager().getHero(playerN).getParty() == hero.getParty()){
                         playerN.teleport(new Location(hero.getPlayer().getWorld(), Double.parseDouble(splitArg[0]), Double.parseDouble(splitArg[1]), Double.parseDouble(splitArg[2])));
                     }
                 }
             }
             player.teleport(new Location(hero.getPlayer().getWorld(), Double.parseDouble(splitArg[0]), Double.parseDouble(splitArg[1]), Double.parseDouble(splitArg[2])));
             notifyNearbyPlayers(player.getLocation(), getUseText(), player.getName(), getName());
             return true;
         } else {
             return false;
         }
     }
 }
