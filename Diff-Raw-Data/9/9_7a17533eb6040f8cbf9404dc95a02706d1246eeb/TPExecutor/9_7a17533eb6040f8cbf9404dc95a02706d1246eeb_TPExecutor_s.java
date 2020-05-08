 package net.mdcreator.tpplus.tp;
 
 import net.mdcreator.tpplus.TPPlus;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class TPExecutor implements CommandExecutor{
 
     private TPPlus plugin;
     private String title = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "TP+" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
     private TpManager tpManager;
 
     public TPExecutor (TPPlus plugin){
         this.plugin = plugin;
         tpManager = plugin.tpManager;
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         Player send;
         if(!(sender instanceof Player)){
             sender.sendMessage(title + ChatColor.RED + "Player context is required!");
             return true;
         }
         send = (Player) sender;
         if(args.length==1){
             if(args[0].equals("help")){
                 return false;
             }
             Player target = plugin.getServer().getPlayer(args[0]);
             if(target==null){
                 send.sendMessage(title + ChatColor.RED + "That player does not exist!");
                 return true;
             }
             if(send.isOp()){
                 Location loc = send.getLocation();
                 send.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 1);
                 send.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
                 send.getWorld().playEffect(loc, Effect.STEP_SOUND, 35);
                target.teleport(send);
                 loc = target.getLocation();
                 send.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 1);
                 send.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
                 send.getWorld().playEffect(loc, Effect.STEP_SOUND, 35);
                 send.sendMessage(title + "Tp done.");
                 target.sendMessage(title + send.getName() + " has tped to you.");
             } else{
                 tpManager.pairs.add(new TpPair(send, target));
                 target.sendMessage(title + send.getName() + " requests to tp to you.");
                 send.sendMessage(title + "Request sent.");
                 send.getWorld().playEffect(send.getLocation().add(0, 1, 0), Effect.SMOKE, 1);
                 target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.SMOKE, 1);
             }
             return true;
         } else if(args.length==2){
             if(args[0].equals("accept")){
                 Player target = plugin.getServer().getPlayer(args[1]);
                 if(target==null){
                     send.sendMessage(title + ChatColor.RED + "That player does not exist!");
                     return true;
                 } else if(tpManager.get(target, send)==null){
                     send.sendMessage(title + ChatColor.RED + "That player did not send a request to you!");
                     return true;
                 }
                 tpManager.pairs.remove(tpManager.get(target, send));
                 target.sendMessage(title + "Request accepted.");
                 send.sendMessage(title + "Accept sent.");
                 Location loc = send.getLocation();
                 send.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 1);
                 send.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
                 send.getWorld().playEffect(loc, Effect.STEP_SOUND, 35);
                 target.teleport(send);
                 loc = target.getLocation();
                 send.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 1);
                 send.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
                 send.getWorld().playEffect(loc, Effect.STEP_SOUND, 35);
                 return true;
             } else{
                 return false;
             }
         }
         return false;
     }
 }
