 package me.limebyte.battlenight.core.commands;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.Other.Waypoint;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class WaypointCommand extends BNCommand {
 
     public WaypointCommand(CommandSender sender, String[] args) {
         super(sender, args);
     }
 
     @Override
     public boolean onPerformed() {
         CommandSender sender = getSender();
         String[] args = getArgs();
 
         if (args.length < 1) {
             sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Please specify a waypoint.");
             sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
             return false;
         } else {
             Waypoint waypoint = null;
 
             for (Waypoint wp : Waypoint.values()) {
                 if (args[0].equalsIgnoreCase(wp.getName())) {
                     waypoint = wp;
                     break;
                 }
             }
 
             if (waypoint != null) {
                 if (args.length == 1) {
                     if (sender instanceof Player) {
                         Player player = (Player) sender;
                         BattleNight.setCoords(waypoint, player.getLocation());
                         BattleNight.tellPlayer(player, ChatColor.GREEN + waypoint.getDisplayName() + " Waypoint set to your current location.");
                         return true;
                     } else {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Please specify a coordinate.");
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Usage: " + getConsoleUsage());
                         return false;
                     }
                 } else if (args.length == 4 && sender instanceof Player) {
                     Player player = (Player) sender;
                     Location loc = parseArgsToLocation(args, player.getWorld());
                     BattleNight.setCoords(waypoint, loc);
                     BattleNight.tellPlayer(player, ChatColor.GREEN + waypoint.getDisplayName() + " Waypoint set to: " +
                             loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + " in this world.");
                     return true;
                 } else if (args.length == 5) {
                     if (Bukkit.getWorld(args[4]) != null) {
                         Location loc = parseArgsToLocation(args, Bukkit.getWorld(args[4]));
                         BattleNight.setCoords(waypoint, loc);
                         sender.sendMessage(BattleNight.BNTag + ChatColor.GREEN + waypoint.getDisplayName() + " Waypoint set to: " +
                                 loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + " in world " + loc.getWorld().getName() + ".");
                         return true;
                     } else {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Can't find world \"" + args[4] + "\".");
                         return false;
                     }
                 } else {
                     sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Incorrect usage.");
                     if (sender instanceof Player) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Usage: " + getUsage());
                     } else {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Usage: " + getConsoleUsage());
                     }
                     return false;
                 }
             } else {
                 sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Invalid waypoint.  Type \"/bn waypoints\" for a list.");
                 return false;
             }
         }
     }
 
     @Override
     public CommandPermission getPermission() {
         return CommandPermission.ADMIN;
     }
 
     @Override
     public String getUsage() {
        return "/bn set [waypoint] <x> <y> <z>\n/bn set [waypoint] <x> <y> <z> <world>";
     }
 
     @Override
     public String getConsoleUsage() {
        return "/bn set [waypoint] [x] [y] [z] [world]";
     }
 
     private Location parseArgsToLocation(String[] args, World world) {
         int x = getInteger(args[1], -30000000, 30000000);
         int y = getInteger(args[2], 0, 256);
         int z = getInteger(args[3], -30000000, 30000000);
 
         return new Location(world, x + 0.5, y, z + 0.5);
     }
 
     private int getInteger(String value, int min, int max) {
         int i = min;
 
         try {
             i = Integer.valueOf(value);
         } catch (final NumberFormatException ex) {
         }
 
         if (i < min) {
             i = min;
         } else if (i > max) {
             i = max;
         }
 
         return i;
     }
 
 }
