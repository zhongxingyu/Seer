 package com.me.tft_02.dynamictextures;
 
 import java.util.Arrays;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener {
     DynamicTextures plugin;
 
     public PlayerListener(final DynamicTextures instance) {
         plugin = instance;
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerJoin(PlayerJoinEvent event) {
         loadTexturePack(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
         loadTexturePack(event.getPlayer());
     }
 
     private void loadTexturePack(Player player) {
         if (!player.hasPermission("dynamictextures.change_texturepack")) {
             return;
         }
 
         String url;
 
         String world = player.getWorld().getName().toLowerCase();
         url = plugin.getConfig().getString("Worlds." + world);
 
         String[] texturePermissions = plugin.getConfig().getConfigurationSection("Permissions").getKeys(false).toArray(new String[0]);
 
         for (String name : Arrays.asList(texturePermissions)) {
             if (player.hasPermission("dynamictextures." + name)) {
                String permission_url = plugin.getConfig().getString("Permissions." + name);
                if ((permission_url.contains("http://") || permission_url.contains("https://")) && permission_url.contains(".zip")) {
                    url = plugin.getConfig().getString("Permissions." + name);
                }
             }
         }
 
         if ((url != null) && (url.contains("http://") || url.contains("https://")) && url.contains(".zip")) {
             player.setTexturePack(url);
         }
     }
 }
