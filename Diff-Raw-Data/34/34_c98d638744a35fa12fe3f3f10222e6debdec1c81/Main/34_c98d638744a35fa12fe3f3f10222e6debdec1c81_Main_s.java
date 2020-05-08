 package me.blha303.giveeverydamnachievement;
 
 import org.bukkit.Achievement;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        Player p = null;
        try
        {
            p = getServer().getPlayer(args[0]);
        } 
        catch (Exception e)
        {
            return false;
        }
        if (p != null)
        {
            for (Achievement ach : Achievement.values())
            {
                p.awardAchievement(ach);
            }
            return true;
        }
        return false;
     }
 }
