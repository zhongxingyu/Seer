 package me.zilo.DTcmd;
 
 import me.zilo.DTcmd.cmdStore.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
public class DTmain extends JavaPlugin
 {
     private CmdExe cmdExe;
     
     @Override
    public void onEnable()
    {
         cmdExe = new CmdExe();
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
         return cmdExe.CheckCmd(sender, cmd, commandLabel, args);
     }
 }
