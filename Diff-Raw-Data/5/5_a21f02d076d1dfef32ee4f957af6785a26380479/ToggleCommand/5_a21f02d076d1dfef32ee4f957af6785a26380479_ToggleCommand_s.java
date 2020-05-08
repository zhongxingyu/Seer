 package com.udonya.signfix.command.sf;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.udonya.signfix.SignFix;
 import com.udonya.signfix.command.AbstractCommand;
 import com.udonya.signfix.command.CmdOwner;
 
 public class ToggleCommand extends AbstractCommand {
 
     public ToggleCommand(String name, SignFix plugin) {
         super(name, plugin);
         owner = CmdOwner.valueOf(true, true);
         setDescription("Toggle Enable Disable this plugin's function");
         setPermission("signfix.toggle");
         setUsage("/sf toggle");
     }
 
     @Override
     public boolean areCompatibleParameters(CommandSender sender, Command command, String s, String[] args) {
         if (args == null) return false;
         if (args.length != 1) return false;
         if (!args[0].equalsIgnoreCase("toggle")) return false;
         return true;
     }
 
     @Override
     public boolean execute(CommandSender sender, String commandLabel, String[] args) {
         if (!(sender instanceof Player)) return true;
         boolean isSuccess;
         if (this.plugin.getDisabled().contains(sender.getName())){
             isSuccess = this.plugin.getDisabled().remove(sender.getName());
            if(isSuccess) sender.sendMessage(clrCmd + "[SignFix] Enabled.");
         }else{
             isSuccess = this.plugin.getDisabled().add(sender.getName());
            if(isSuccess) sender.sendMessage(clrCmd + "[SignFix] Disabled.");
         }
         return isSuccess;
     }
 }
