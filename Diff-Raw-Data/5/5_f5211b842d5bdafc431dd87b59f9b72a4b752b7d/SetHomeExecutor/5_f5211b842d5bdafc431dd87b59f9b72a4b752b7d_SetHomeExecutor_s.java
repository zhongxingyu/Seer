 package net.mdcreator.tpplus.home;
 
 import net.mdcreator.tpplus.TPPlus;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SetHomeExecutor implements CommandExecutor{
 
     TPPlus plugin;
     private String title = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "TP+" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
 
     public SetHomeExecutor(TPPlus plugin){
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         Player send;
         if(!(sender instanceof Player)){
             sender.sendMessage(title + ChatColor.RED + "Player context is required!");
             return true;
         }
         send = (Player) sender;
         if(args.length==0){
            send.performCommand("/home set");
             return true;
         } else if(args.length==1&&args[0].equalsIgnoreCase("bed")){
            send.performCommand("/home set bed");
             return true;
         }
         return false;
     }
 }
