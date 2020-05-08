 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.command.skill.Skill;
 
 public class SkillCommand extends BaseCommand {
 
     private static final int SKILLS_PER_PAGE = 8;
 
     public SkillCommand(Heroes plugin) {
         super(plugin);
         name = "Skill";
         description = "Displays a list of your class skills";
         usage = "/skill [page#]";
         minArgs = 0;
         maxArgs = 1;
         identifiers.add("skill");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("You must be a player to use this command.");
             return;
         }
         Player player = (Player) sender;
 
         int page = 0;
         if (args.length != 0) {
             try {
                 page = Integer.parseInt(args[0]) - 1;
             } catch (NumberFormatException e) {
             }
         }
 
         List<BaseCommand> sortCommands = plugin.getCommandManager().getCommands();
         List<Skill> skills = new ArrayList<Skill>();
         HeroClass heroClass = plugin.getHeroManager().getHero(player).getPlayerClass();
 
         // Filter out Skills from the command list.
         for (BaseCommand command : sortCommands) {
             if (command instanceof Skill) {
                 Skill skill = (Skill) command;
                if (heroClass.hasSkill(skill.getName()) && !skills.contains(skill)) {
                     skills.add(skill);
                 }
             }
         }
 
         int numPages = skills.size() / SKILLS_PER_PAGE;
         if (skills.size() % SKILLS_PER_PAGE != 0) {
             numPages++;
         }
 
         if (page >= numPages || page < 0) {
             page = 0;
         }
         sender.sendMessage("§c-----[ " + "§f" + heroClass.getName() + " Skills <" + (page + 1) + "/" + numPages + ">§c ]-----");
         int start = page * SKILLS_PER_PAGE;
         int end = start + SKILLS_PER_PAGE;
         if (end > skills.size()) {
             end = skills.size();
         }
         for (int s = start; s < end; s++) {
             Skill skill = skills.get(s);
             sender.sendMessage("  §a" + skill.getUsage() + " - Lvl " + heroClass.getSkillSettings(skill.getName()).LevelRequirement);
         }
 
         sender.sendMessage("§cFor more info on a particular skill, type '/<skill> ?'");
     }
 
 }
