 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.Set;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class WhoCommand extends BasicCommand {
     private final Heroes plugin;
 
     public WhoCommand(Heroes plugin) {
         super("Who");
         this.plugin = plugin;
         setDescription("Checks the players level and other information");
         setUsage("/hero who §9<player|class>");
         setArgumentRange(1, 1);
         setIdentifiers(new String[] { "hero who" });
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         Player searchedPlayer = plugin.getServer().getPlayer(args[0]);
         HeroClass searchedClass = plugin.getClassManager().getClass(args[0]);
         if (searchedPlayer != null) {
             Properties prop = this.plugin.getConfigManager().getProperties();
             Hero hero = plugin.getHeroManager().getHero(searchedPlayer);
             int level = prop.getLevel(hero.getExperience());
 
             sender.sendMessage("§c-----[ " + "§f" + searchedPlayer.getName() + "§c ]-----");
             sender.sendMessage("  §aClass : " + hero.getHeroClass().getName());
             sender.sendMessage("  §aLevel : " + level);
         } else if (searchedClass != null) {
             Properties prop = this.plugin.getConfigManager().getProperties();
             Set<Hero> heroes = plugin.getHeroManager().getHeroes();
             sender.sendMessage("§c-----[ " + "§f" + searchedClass.getName() + "§c ]-----");
             for (Hero hero : heroes) {
                 if (hero == null) {
                     continue;
                 }
                 if (hero.getHeroClass().equals(searchedClass)) {
                     int level = prop.getLevel(hero.getExperience());
                     sender.sendMessage("  §aName : " + hero.getPlayer().getName() + "  §aLevel : " + level);
                 }
             }
         } else {
             Messaging.send(sender, "Player not online!");
             return false;
         }
 
         return true;
     }
 
 }
