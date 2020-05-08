 package net.erbros.HoldGuest;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 class HoldGuestCommandExecutor implements CommandExecutor {
     private Misc misc;
     
     HoldGuestCommandExecutor(HoldGuest plugin) {
         this.misc = plugin.misc;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label,
             String[] args) {
 
         if(args.length > 0) {
             if (args[0].equalsIgnoreCase("center")) {
                 if(sender instanceof Player) {
                     if(!sender.hasPermission("holdguest.setcenter"))
                         return false;
                     
                     misc.setCenter( (Player) sender);
                     sender.sendMessage( misc.customMessage( "holdguestheader" ) );
                     sender.sendMessage( misc.customMessage( "centerset" ) );
                     return true;
                 } else {
                     sender.sendMessage( misc.customMessage( "holdguestheader" ) );
                     sender.sendMessage( misc.customMessage( "noconsole" ) );
                     return true;
                 }
             } else if (args[0].equalsIgnoreCase("radius")) {
                 if(sender instanceof Player) {
                     if(!sender.hasPermission("holdguest.setradius"))
                         return false;
                 }
                 // No radius arg?
                 if(args.length == 1) {
                     sender.sendMessage( misc.customMessage( "holdguestheader" ) );
                     sender.sendMessage( misc.customMessage( "radiushelp" ) );
                     return true;
                 }
                 // Is it a double?
                 double radius = 0;
                 
                 try {
                     radius = Double.parseDouble(args[1]);
                 } catch (NumberFormatException e) {
                     sender.sendMessage( misc.customMessage( "holdguestheader" ) );
                     sender.sendMessage( misc.customMessage( "radiushelp" ) );
                     return true;
                 }
                 sender.sendMessage( misc.customMessage( "holdguestheader" ) );
                 sender.sendMessage( misc.customMessage( "radiusset" ) );
                 misc.setRadius(radius);
                 return true;
             } else if (args[0].equalsIgnoreCase("reload")) {
                 if(sender instanceof Player) {
                     if(!sender.hasPermission("holdguest.reload"))
                         return false;
                 }
                 
                 sender.sendMessage( misc.customMessage( "holdguestheader" ) );
                 sender.sendMessage( misc.customMessage( "reloaded" ) );
                 
             }
             return true;
         }
 
         if(sender instanceof Player) {
             sender.sendMessage( misc.customMessage( "holdguestheader" ) );
             sender.sendMessage( misc.customMessage( "centerhelp" ) );
             sender.sendMessage( misc.customMessage( "radiushelp" ) );
             return true;
         } else {
             sender.sendMessage( misc.customMessage( "holdguestheader" ) );
             sender.sendMessage( misc.customMessage( "radiushelp" ) );
             return true;
         }
     }
 }
