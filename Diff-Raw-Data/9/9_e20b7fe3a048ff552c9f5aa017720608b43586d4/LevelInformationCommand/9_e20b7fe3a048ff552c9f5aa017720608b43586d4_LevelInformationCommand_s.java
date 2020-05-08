 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class LevelInformationCommand extends BaseCommand {
 
     public LevelInformationCommand(Heroes plugin) {
         super(plugin);
         name = "LevelInformation";
         description = "Player Level information";
         usage = "/hlevel";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("heroes level");
         identifiers.add("hlevel");
         identifiers.add("level");
         identifiers.add("lvl");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             Player p = (Player) sender;
             Hero h = plugin.getHeroManager().getHero(p);
             int exp = h.getExperience();
             Properties prop = this.plugin.getConfigManager().getProperties();
             int level = prop.getLevel(exp);
             int next = prop.levels[level + 1];
 
             sender.sendMessage("§c-----[ " + "§fYour Level Information§c ]-----");
 
             sender.sendMessage("  §aClass : " + h.getPlayerClass().getName());
             sender.sendMessage("  §aLevel : " + level);
             sender.sendMessage("  §aExp : " + exp);
            sender.sendMessage("  §aNext Level : " + level + 1);
             sender.sendMessage("  §aExp to Go: " + (next - exp));
 
             // Possible exp progress bar to come, but this will do for now.
             // String expBar = "";
             // sender.sendMessage("  �aExp Progress: " + );
         }
     }
 }
