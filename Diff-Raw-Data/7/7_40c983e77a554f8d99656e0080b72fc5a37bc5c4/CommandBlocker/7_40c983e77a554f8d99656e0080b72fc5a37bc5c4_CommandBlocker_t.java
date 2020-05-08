 package me.limebyte.battlenight.core.Listeners;
 
 import java.util.List;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.command.Command;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class CommandBlocker implements Listener {
 
     // Get Main Class
     public static BattleNight plugin;
     public CommandBlocker(BattleNight instance) {
         plugin = instance;
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         if (event.isCancelled()) return;
         if (!plugin.BattleUsersTeam.containsKey(event.getPlayer().getName())) return;
         if (!plugin.config.getBoolean("Commands.Block")) return;
         
         List<String> whitelist = plugin.config.getStringList("Commands.Whitelist");
         whitelist.add("bn");
         
         String[] cmdArg = event.getMessage().split(" ");
        String cmdString = cmdArg[0].trim().substring(1).toLowerCase();
         
         try {
            Command command = plugin.getServer().getPluginCommand(cmdString);
             
             // Check if the command is listed
             if (whitelist.contains(command.getLabel().toLowerCase())) return;
                 
             // Check if there are any aliases listed
             if (!command.getAliases().isEmpty()) {
                 for (String alias : command.getAliases()) {
                     if (whitelist.contains(alias.toLowerCase())) return;
                 }
             }
         } catch (NullPointerException e) {
             // Check if the command is listed
            if (whitelist.contains(cmdString)) return;
         }
     
         // Its not listed so block it
         event.setCancelled(true);
         plugin.tellPlayer(event.getPlayer(), "You are not permitted to perform this command while in a Battle.");
         return;
     }
     
 }
