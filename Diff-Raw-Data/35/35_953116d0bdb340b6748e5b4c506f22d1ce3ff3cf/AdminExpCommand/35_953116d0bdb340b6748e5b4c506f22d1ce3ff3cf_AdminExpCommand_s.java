 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class AdminExpCommand extends BasicCommand {
 
     private final Heroes plugin;
 
     public AdminExpCommand(Heroes plugin) {
         super("AdminExpCommand");
         this.plugin = plugin;
         setDescription("Changes a users exp");
         setUsage("/hero admin exp §9<player> <exp>");
         setArgumentRange(2, 2);
         setIdentifiers(new String[] { "hero admin exp" });
         setPermission("heroes.admin.exp");
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         Player player = plugin.getServer().getPlayer(args[0]);
        Hero hero = plugin.getHeroManager().getHero(player);
         // Check the Player exists.
         if (player == null) {
            Messaging.send(sender, "Failed to find a matching Player for '$1'.", args[0]);
             return false;
         }
         try {
             int expChange = (int) (Integer.parseInt(args[1]) - hero.getExperience());
             hero.gainExp(expChange, ExperienceType.ADMIN, false);
             Messaging.send(sender, "Experience changed.");
             return true;
         } catch (NumberFormatException e) {
             Messaging.send(sender, "Invalid experience value.");
             return false;
         }
 
     }
 }
