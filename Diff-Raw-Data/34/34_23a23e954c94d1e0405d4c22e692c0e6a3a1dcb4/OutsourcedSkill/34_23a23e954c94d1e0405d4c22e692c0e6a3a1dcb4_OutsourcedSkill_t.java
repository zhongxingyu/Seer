 package com.herocraftonline.dev.heroes.command.skill;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ClassChangeEvent;
 import com.herocraftonline.dev.heroes.api.HeroLoadEvent;
 import com.herocraftonline.dev.heroes.api.LevelEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.SkillSettings;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class OutsourcedSkill extends Skill {
 
     protected String[] permissions;
 
     public OutsourcedSkill(Heroes plugin, String name, String[] permissions, String usage) {
         super(plugin);
         this.name = name;
         this.permissions = permissions;
         this.usage = usage;
         this.minArgs = 0;
         this.maxArgs = 0;
         registerEvent(Type.CUSTOM_EVENT, new SkillCustomListener(), Priority.Normal);
     }
 
     public void tryLearningSkill(Player player) {
         if (Heroes.Permissions == null) {
             return;
         }
 
         Hero hero = plugin.getHeroManager().getHero(player);
         HeroClass heroClass = hero.getPlayerClass();

         String world = player.getWorld().getName();
         String playerName = player.getName();
        if (heroClass.hasSkill(name)) {
            SkillSettings settings = heroClass.getSkillSettings(name);
            if (settings != null && meetsLevelRequirement(hero, settings.LevelRequirement)) {
                for (String permission : permissions) {
                    if (!Heroes.Permissions.has(player, permission)) {
                        Heroes.Permissions.addUserPermission(world, playerName, permission);
                    }
                 }
            } else {
                for (String permission : permissions) {
                    if (Heroes.Permissions.has(player, permission)) {
                        Heroes.Permissions.removeUserPermission(world, playerName, permission);
                    }
                 }
             }
            Heroes.Permissions.save(world);
         }
     }
 
     public class SkillCustomListener extends CustomEventListener {
 
         @Override
         public void onCustomEvent(Event event) {
             if (event instanceof ClassChangeEvent) {
                 tryLearningSkill(((ClassChangeEvent) event).getPlayer());
             } else if (event instanceof LevelEvent) {
                 tryLearningSkill(((LevelEvent) event).getPlayer());
             } else if (event instanceof HeroLoadEvent) {
                 tryLearningSkill(((HeroLoadEvent) event).getHero().getPlayer());
             }
         }
 
     }
 
     @Override
    public void execute(CommandSender sender, String[] args) {}
 
 }
