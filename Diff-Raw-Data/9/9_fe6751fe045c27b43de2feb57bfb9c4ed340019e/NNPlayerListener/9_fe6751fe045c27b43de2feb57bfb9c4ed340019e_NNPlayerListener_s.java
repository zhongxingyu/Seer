 package com.bukkit.N4th4.NuxNoobs;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class NNPlayerListener extends PlayerListener {
     public final NuxNoobs plugin;
 
     public NNPlayerListener(NuxNoobs instance) {
         plugin = instance;
     }
 
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
        if (plugin.permissions.inGroup(player.getWorld().getName(), player.getName(), plugin.group)) {
             for (int i = 0; i < plugin.noobMessage.size(); i++) {
                 player.sendMessage(plugin.noobMessage.get(i));
             }
             plugin.getServer().broadcastMessage(ChatColor.AQUA + plugin.welcomeMessage.replace("%nick%", player.getName()));
         }
     }
 }
