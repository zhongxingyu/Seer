 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.Collection;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.hero.Hero;
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
         setIdentifiers("hero who");
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         Player searchedPlayer = plugin.getServer().getPlayer(args[0]);
         HeroClass searchedClass = plugin.getClassManager().getClass(args[0]);
         if (searchedPlayer != null) {
             Hero hero = plugin.getHeroManager().getHero(searchedPlayer);
             int level = Properties.getLevel(hero.getExperience());
             HeroClass sClass = hero.getSecondClass();
             String secondClassName = sClass != null ? " | " + sClass.getName() : "";
             String secondLevelInfo = sClass != null ? (" | " + hero.getLevel(sClass)) : "";
             
             sender.sendMessage("§c-----[ " + "§f" + searchedPlayer.getName() + "§c ]-----");
             sender.sendMessage("  §aClass : " + hero.getHeroClass().getName() + secondClassName);
             sender.sendMessage("  §aLevel : " + level + secondLevelInfo);
         } else if (searchedClass != null) {
             Collection<Hero> heroes = plugin.getHeroManager().getHeroes();
             sender.sendMessage("§c-----[ " + "§f" + searchedClass.getName() + "§c ]-----");
             for (Hero hero : heroes) {
                 if (hero == null || !hero.getPlayer().isOnline()) {
                     continue;
                 }
                if (searchedClass.equals(hero.getHeroClass()) || searchedClass.equals(hero.getSecondClass())) {
                    int level = Properties.getLevel(hero.getLevel(searchedClass));
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
