 package com.md_5.district;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.lwc.LWCPlugin;
 
 public class District extends JavaPlugin {
 
     public static final Logger logger = Bukkit.getServer().getLogger();
     @SuppressWarnings("unused")
     private DistrictBlockListener blockListener;
     @SuppressWarnings("unused")
     private DistrictPlayerListener playerListener;
     
     public LWC lwc = null;
 
     public void onEnable() {
         Config.load(this);
         Loader.load(this);
 
         // Find the LWC plugin and get access to it's API
         Plugin lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
         if(lwcPlugin != null) {
             lwc = ((LWCPlugin) lwcPlugin).getLWC();
         }
         
         blockListener = new DistrictBlockListener(this);
         playerListener = new DistrictPlayerListener(this);
         logger.info(String.format("District v%1$s by md_5 enabled", this.getDescription().getVersion()));
     }
 
     public void onDisable() {
         logger.info(String.format("District v%1$s by md_5 disabled", this.getDescription().getVersion()));
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (sender instanceof Player) {
             return onPlayerCommand((Player) sender, command, label, args);
         } else {
             return onConsoleCommand(sender, command, label, args);
         }
     }
     
     public boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
         try {
             if (args.length == 0) {
                 player.sendMessage(ChatColor.GOLD + "District by md_5, the following commands may be used at this time:");
                 player.sendMessage(ChatColor.GOLD + "/district claim <size> <region>");
                 player.sendMessage(ChatColor.GOLD + "/district show <region>");
                 player.sendMessage(ChatColor.GOLD + "/district hide <region>");
                 player.sendMessage(ChatColor.GOLD + "/district remove <region>");
                 player.sendMessage(ChatColor.GOLD + "/district list");
                 player.sendMessage(ChatColor.GOLD + "/district listall [player]");
                 player.sendMessage(ChatColor.GOLD + "/district addmember <region> <player>");
                 player.sendMessage(ChatColor.GOLD + "/district delmember <region> <player>");
                 player.sendMessage(ChatColor.GOLD + "/district listmembers <region>");
                 return true;
             }
             if (args[0].equalsIgnoreCase("claim")) {
                 Commands.claim(player, args, this, 3);
                 return true;
             }
             if (args[0].equalsIgnoreCase("show")) {
                 Commands.show(player, args, getRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("hide")) {
                 Commands.hide(player, args, getRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("remove")) {
                 Commands.remove(player, args, this, getRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("addmember")) {
                 Commands.addMember(player, args, this, getRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("delmember")) {
                 Commands.delMember(player, args, this, getRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("list")) {
                 Commands.list(player.getName(), player);
                 return true;
             }
             if (args[0].equalsIgnoreCase("listall")) {
                 Commands.listAll(player, args);
                 return true;
             }
             if (args[0].equalsIgnoreCase("listmembers")) {
                 Commands.listMembers(player, args, getRegion(player, args));
                 return true;
             }
             player.sendMessage(ChatColor.RED + "District: That is not a valid command");
         } catch(CommandException e) {
             player.sendMessage(ChatColor.RED + "District: " + e.getMessage());
         }
         return true;
 	}
     
     private Region getRegion(Player player, String[] args) {
         if(args.length <= 1) {
             throw new CommandException("You must supply a region name or '-'");
         }
         else if(args[1].trim().equals("-")) {
             ArrayList<Region> regions = Util.getRegions(player.getLocation());
             if(regions == null) {
                 throw new CommandException("Unable to use '-' operator when not in a region");
             }
             if(regions.size() != 1) {
                 throw new CommandException("Unable to use '-' operator when in multiple regions");
             }
             return regions.get(0);
         }
         else {
             Region r = Regions.getRegion(args[1]);
             if (r == null) {
                 throw new CommandException("Region does not exist");
             }
         }
         return Regions.getRegion(args[1]);
     }
 
     public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
         sender.sendMessage(String.format("District v%1$s by md_5", this.getDescription().getVersion()));
         sender.sendMessage("District: No other console functionality is available at this time");
         return true;
     }
 }
