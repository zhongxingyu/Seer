 package me.zilo.DTcmd;
 
import java.util.logging.Logger;
 import me.zilo.DTcmd.cmdStore.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
public class DTmain extends JavaPlugin implements Listener
 {
    private Logger log = null;
     private CmdExe cmdExe;
     
     @Override
    public void onEnable(){
        log = getLogger();
         cmdExe = new CmdExe();
        getServer().getPluginManager().registerEvents(this, this);
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
         return cmdExe.CheckCmd(sender, cmd, commandLabel, args);
     }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        //entity.BigCat.name
        String newDeathMsg = event.getDeathMessage();
        newDeathMsg = newDeathMsg.replace("entity.", "");
        newDeathMsg = newDeathMsg.replace(".name", "");
        event.setDeathMessage(newDeathMsg);
    }
 }
