 package me.ryanclancy000.flight;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class FlightCommands {
 
     String pre = ChatColor.YELLOW + "[Flight] ";
     ChatColor yellow = ChatColor.YELLOW;
     ChatColor green = ChatColor.GREEN;
     ChatColor white = ChatColor.WHITE;
     ChatColor red = ChatColor.RED;
     public Flight plugin;
     public Util u;
 
     public FlightCommands(Flight instance) {
         this.plugin = instance;
     }
 
     // Help Command
     public void helpCommand(CommandSender sender, String[] args) {
 
         if (args.length > 1) {
             sender.sendMessage(pre + red + "Too many arguments!");
             return;
         }
 
         sender.sendMessage(pre + green + "Help Menu!");
         sender.sendMessage(yellow + "/flight " + green + "list - " + yellow + "List players who have flight enabled.");
         sender.sendMessage(yellow + "/flight " + green + "toggle [player] - " + yellow + "Toggles flight.");
         sender.sendMessage(yellow + "/flight " + green + "check [player] - " + yellow + "Checks flight status.");
         sender.sendMessage(yellow + "/flight " + green + "on [player] - " + yellow + "Enable flight.");
         sender.sendMessage(yellow + "/flight " + green + "off [player] - " + yellow + "Disabled flight.");
 
     }
 
     // Toggle Command
     public void toggleCommand(CommandSender sender, String[] args) {
 
         if (args.length == 1) {
 
             if (!(sender instanceof Player)) {
                 sender.sendMessage(pre + red + "Console can't fly!");
                 return;
             }
 
             Player player = (Player) sender;
 
             if (u.isCreative(player)) {
                 player.sendMessage(pre + red + "Cannot change flight while in creative!");
                 return;
             }
 
             if (player.getAllowFlight()) {
                 u.disableFly(player);
                 player.sendMessage(pre + red + "Flight disabled!");
                 return;
             } else {
                 u.enableFly(player);
                 player.sendMessage(pre + green + "Flight enabled!");
                 return;
             }
 
         }
 
         if (args.length == 2 && sender.hasPermission("flight.toggle.other")) {
 
             Player target = Bukkit.getServer().getPlayer(args[1]);
 
             try {
 
                 if (u.isCreative(target)) {
                     sender.sendMessage(pre + red + "Can't edit flight for " + target.getName() + ", they are in creative mode!");
                     return;
                 }
 
                 if (target.getAllowFlight()) {
                     u.disableFly(target);
 
                     if (target.getName().equalsIgnoreCase(sender.getName())) {
                         sender.sendMessage(pre + red + "Flight disabled!");
                         return;
                     }
 
                     sender.sendMessage(pre + red + "Flight disabled for " + target.getName());
                     target.sendMessage(pre + red + "Flight disabled by " + sender.getName());
                     return;
                 } else {
                     u.enableFly(target);
 
                     if (target.getName().equalsIgnoreCase(sender.getName())) {
                         sender.sendMessage(pre + green + "Flight enabled!");
                         return;
                     }
 
                     sender.sendMessage(pre + green + "Flight enabled for " + target.getName());
                     target.sendMessage(pre + green + "Flight enabled by " + sender.getName());
                     return;
                 }
 
             } catch (Exception e) {
                 sender.sendMessage(pre + red + "Player not online!");
                 return;
             }
 
         }
 
         if (args.length > 2) {
             sender.sendMessage(pre + red + "Too many arguments!");
         }
 
         sender.sendMessage(red + "You do not have permission to do that...");
 
     }
 
     public void flyOn(CommandSender sender, String[] args) {
 
         if (args.length == 1) {
 
             if (!(sender instanceof Player)) {
                 sender.sendMessage(pre + red + "Console can't fly!");
                 return;
             }
 
             Player player = (Player) sender;
 
             if (u.isCreative(player)) {
                 player.sendMessage(pre + red + "Cannot change flight while in creative!");
                 return;
             }
 
             if (u.flyModeEnabled(player)) {
                 player.sendMessage(pre + red + "Flight already on!");
                 return;
             }
 
             u.enableFly(player);
             sender.sendMessage(pre + green + "Flight enabled!");
             return;
 
         }
 
         if (args.length == 2 && sender.hasPermission("flight.on.other")) {
 
             Player target = Bukkit.getServer().getPlayer(args[1]);
 
             try {
                 if (u.isCreative(target)) {
                     sender.sendMessage(pre + red + "Can't edit flight for " + target.getName() + ", they are in creative mode!");
                     return;
                 }
 
                 if (u.flyModeEnabled(target)) {
                     sender.sendMessage(pre + red + "Flight already on for " + target.getName());
                     return;
                 }
 
                 u.enableFly(target);
 
                 if (target.getName().equalsIgnoreCase(sender.getName())) {
                     sender.sendMessage(pre + green + "Flight enabled!");
                     return;
                 }
 
                 sender.sendMessage(pre + green + "Flight enabled for " + target.getName());
                 target.sendMessage(pre + green + "Flight enabled by " + sender.getName());
                 return;
 
             } catch (Exception e) {
                 sender.sendMessage(pre + red + "Player not online!");
                 return;
             }
         }
 
         if (args.length > 2) {
             sender.sendMessage(pre + red + "Too many arguments!");
         }
 
         sender.sendMessage(red + "You do not have permission to do that...");
 
     }
 
     public void flyOff(CommandSender sender, String[] args) {
 
         if (args.length == 1) {
 
             if (!(sender instanceof Player)) {
                 sender.sendMessage(pre + red + "Console can't fly!");
                 return;
             }
 
             Player player = (Player) sender;
 
             if (u.isCreative(player)) {
                 player.sendMessage(pre + red + "Cannot change flight while in creative!");
                 return;
             }
 
             if (!u.flyModeEnabled(player)) {
                 player.sendMessage(pre + red + "Flight already off!");
                 return;
             }
 
             u.disableFly(player);
             sender.sendMessage(pre + red + "Flight disabled!");
             return;
 
         }
 
         if (args.length == 2 && sender.hasPermission("flight.off.other")) {
 
             Player target = Bukkit.getServer().getPlayer(args[1]);
 
             try {
 
                 if (u.isCreative(target)) {
                     sender.sendMessage(pre + red + "Can't edit flight for " + target.getName() + ", they are in creative mode!");
                     return;
                 }
 
                 if (!u.flyModeEnabled(target)) {
                     sender.sendMessage(pre + red + "Flight already off for " + target.getName());
                     return;
                 }
 
                 u.disableFly(target);
 
                 if (target.getName().equalsIgnoreCase(sender.getName())) {
                     sender.sendMessage(pre + red + "Flight disabled!");
                     return;
                 }
 
                 sender.sendMessage(pre + red + "Flight disabled for " + target.getName());
                 target.sendMessage(pre + red + "Flight disabled by " + sender.getName());
                 return;
 
             } catch (Exception e) {
                 sender.sendMessage(pre + red + "Player not online!");
                 return;
             }
         }
 
         if (args.length > 2) {
             sender.sendMessage(pre + red + "Too many arguments!");
         }
 
         sender.sendMessage(red + "You do not have permission to do that...");
 
     }
 
     public void checkCommand(CommandSender sender, String[] args) {
 
         if (!(sender instanceof Player)) {
             sender.sendMessage(pre + red + "Console can't fly!");
             return;
         }
 
         Player player = (Player) sender;
 
         if (args.length == 1) {
 
             if (u.isCreative(player)) {
                 sender.sendMessage(pre + red + "You are in creative, of course mode is enabled!");
                 return;
             }
 
             if (u.flyModeEnabled(player)) {
                 sender.sendMessage(pre + green + "Your flight is enabled!");
                 return;
             }
 
             sender.sendMessage(pre + red + "Your flight is disabled!");
             return;
 
         }
 
         if (args.length == 2 && sender.hasPermission("flight.check.other")) {
 
             Player target = Bukkit.getServer().getPlayer(args[1]);
 
             try {
 
                 if (u.isCreative(target)) {
                     sender.sendMessage(pre + red + target.getName() + " is in creative, of course flight is enabled!");
                     return;
                 }
 
                 if (u.flyModeEnabled(target)) {
                     sender.sendMessage(pre + green + target.getName() + " has flight enabled!");
                     return;
                 } else {
                     sender.sendMessage(pre + red + target.getName() + " has flight disabled!");
                     return;
                 }
 
             } catch (Exception e) {
                 sender.sendMessage(pre + red + "Player not online!");
                 return;
             }
 
         }
 
         if (args.length > 2) {
             sender.sendMessage(pre + red + "Too many arguments!");
         }
 
         sender.sendMessage(red + "You do not have permission to do that...");
 
     }
 
     public void listCommand(CommandSender sender, String[] args) {
 
         StringBuilder s = new StringBuilder();
 
         if (args.length != 1) {
             sender.sendMessage(pre + red + "Too many arguments!");
             return;
         }
 
         for (Player p : Bukkit.getOnlinePlayers()) {
            
            if (!u.flyModeEnabled(p)) {
                return;
            }
 
             if (s.length() > 0) {
                 s.append(white + ", ");
             }
 
             if (p.getAllowFlight()) {
                 s.append(green + p.getName());
             }
         }
         sender.sendMessage(pre + green + "Flight Mode Enabled: " + s);
 
     }
 }
