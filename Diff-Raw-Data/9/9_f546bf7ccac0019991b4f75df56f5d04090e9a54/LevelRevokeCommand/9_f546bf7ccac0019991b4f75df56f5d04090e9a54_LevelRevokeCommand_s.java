 package de.craftlancer.skilllevels.commands;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.craftlancer.skilllevels.LevelLanguage;
 import de.craftlancer.skilllevels.LevelSystem;
 import de.craftlancer.skilllevels.SkillLevels;
 import de.craftlancer.skilllevels.Utils;
 
 public class LevelRevokeCommand extends LevelSubCommand
 {
     
     public LevelRevokeCommand(String permission, SkillLevels plugin)
     {
         super(permission, plugin);
     }
     
     @Override
     protected void execute(CommandSender sender, Command cmd, String label, String[] args)
     {
         if (!sender.hasPermission(getPermission()) && sender instanceof Player)
             sender.sendMessage(LevelLanguage.COMMAND_PERMISSION);
        else if (!(sender instanceof Player) && args.length < 5)
             sender.sendMessage(LevelLanguage.COMMAND_ARGUMENTS);
         else if (Bukkit.getPlayerExact(args[1]) == null)
             sender.sendMessage(LevelLanguage.COMMAND_PLAYER_NOT_EXIST);
         else if (!plugin.getLevelSystems().containsKey(args[2]))
             sender.sendMessage(LevelLanguage.COMMAND_SYSTEM_NOT_EXIST);
         else if (!Utils.arrayContains(new String[] { "xp", "points", "level" }, args[3]))
             sender.sendMessage(LevelLanguage.COMMAND_ADDSETRESET_ARGERROR);
         else if (!args[4].matches("-?[0-9]+"))
             sender.sendMessage(LevelLanguage.COMMAND_NOT_A_NUMBER);
         else
         {
             LevelSystem ls = plugin.getLevelSystems().get(args[2]);
             
             if (args[3].equalsIgnoreCase("xp"))
                 ls.revokeExp(Integer.parseInt(args[4]), args[1]);
             else if (args[3].equalsIgnoreCase("points"))
                 ls.addUsedPoints(Integer.parseInt(args[4]), args[1]);
             else if (args[3].equalsIgnoreCase("level"))
                 ls.revokeLevel(Integer.parseInt(args[4]), args[1]);
         }
     }
     
     @Override
     protected List<String> onTabComplete(String[] args)
     {
         switch (args.length)
         {
             case 3:
                 return Utils.getMatches(args[2], plugin.getLevelSystems().keySet());
             case 4:
                 return Utils.getMatches(args[3], new String[] { "xp", "points", "level" });
             default:
                 return null;
         }
     }
     
 }
