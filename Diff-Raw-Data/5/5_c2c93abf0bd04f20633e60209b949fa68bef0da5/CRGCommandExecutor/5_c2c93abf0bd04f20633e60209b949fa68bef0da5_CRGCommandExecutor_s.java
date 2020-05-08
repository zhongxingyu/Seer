 package net.caldonia.bukkit.plugins.RemapGroup;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CRGCommandExecutor implements CommandExecutor {
     private RGPlugin ourPlugin;
 
     public CRGCommandExecutor(RGPlugin ourPlugin) {
         this.ourPlugin = ourPlugin;
     }
 
     @Override
     public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
         if (command.getName().equalsIgnoreCase("crg")) {
             if (strings.length == 0) {
                 commandSender.sendMessage("[CRG] Usage: /crg (reload|list)");
             } else {
                 if (strings[0].equalsIgnoreCase("reload")) {
                    if (!(commandSender instanceof Player) || ((Player) commandSender).getPlayer().hasPermission("caldonia.default_group.reload")) {
                         ourPlugin.loadConfig();
                         commandSender.sendMessage("[CRG] Reloaded configuration.");
                     } else {
                         commandSender.sendMessage("[CRG] Sorry, you do not have permission to do that.");
                     }
                 } else if (strings[0].equalsIgnoreCase("list")) {
                    if (!(commandSender instanceof Player) || ((Player) commandSender).getPlayer().hasPermission("caldonia.default_group.list")) {
                         commandSender.sendMessage("[CRG] Mappings Configured:");
 
                         for (Mapping map : ourPlugin.getGroupMappings().values()) {
                             commandSender.sendMessage("[CRG] " + map.getInfo());
 
                             if (map.getRawAnnounce() != null)
                                 commandSender.sendMessage("[CRG] - A: " + map.getRawAnnounce());
 
                             if (map.getRawDirect() != null)
                                 commandSender.sendMessage("[CRG] - D: " + map.getRawDirect());
                         }
                     } else {
                         commandSender.sendMessage("[CRG] Sorry, you do not have permission to do that.");
                     }
                 } else {
                     commandSender.sendMessage("[CRG] Unknown command, try /crg reload or /crg list.");
                 }
             }
             return true;
         }
         return false;
     }
 }
