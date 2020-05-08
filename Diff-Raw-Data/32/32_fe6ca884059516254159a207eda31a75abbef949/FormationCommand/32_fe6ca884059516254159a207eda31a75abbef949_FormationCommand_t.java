 package me.limebyte.endercraftessentials;
 
 import java.util.Set;
 
import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class FormationCommand implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("This command can only be run by a player.");
         } else {
             Player player = (Player) sender;
            if (args != null && args.length >= 1) {
                 try {
                     int radius = Integer.parseInt(args[0]);
 
                     if (radius > 100) {
                         sender.sendMessage("Calm down mate!  Not too big!");
                         return true;
                     }
 
                     Set<Location> locations = WitherFormationFinder.searchArea(player.getLocation(), radius);
                     if (locations.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "No bedrock formations found.");
                     } else {
                         String coords = "";
                         for (Location loc : locations) {
                             coords += "(" + loc.getBlockX() + ", " + loc.getBlockZ() + "), ";
                         }
                        player.sendMessage(ChatColor.GREEN + "Found " + locations.size() + " bedrock formations at:");
                         player.sendMessage(coords.substring(0, coords.length() - 2));
                     }
                 } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid radius.");
                     sender.sendMessage("Usage: " + cmd.getUsage());
                 }
             } else {
                sender.sendMessage(ChatColor.RED + "Please specify a radius.");
                 sender.sendMessage("Usage: " + cmd.getUsage());
             }
         }
         return true;
     }
 
 }
