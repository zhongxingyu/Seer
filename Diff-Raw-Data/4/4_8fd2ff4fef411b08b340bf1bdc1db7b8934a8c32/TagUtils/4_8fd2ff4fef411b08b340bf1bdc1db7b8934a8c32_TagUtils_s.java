 package com.gmail.jameshealey1994.simplepvptoggle.utils;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.tag.TagAPI;
 
 /**
  * Utility methods that interact with TagAPI.
  *
  * @author JamesHealey94 <jameshealey1994.gmail.com>
  */
 public abstract class TagUtils {
 
     /**
      * The name of the TagAPI plugin.
      */
     public static final String NAME = "TagAPI";
 
     /**
      * The width of a Minecraft chunk.
      */
     public static final int CHUNK_WIDTH = 16;
     
     /**
      * Returns if TagAPI is enabled.
      *
      * @param plugin    plugin with related server and plugin manager
      * @return          if TagAPI is enabled
      */
     public static boolean isEnabled(JavaPlugin plugin) {
         return plugin.getServer().getPluginManager().isPluginEnabled(NAME);
     }
 
     /**
      * If TagAPI is enabled, then refresh player tag.
      *
      * @param player    player with tag to refresh
      * @param plugin    plugin with possible TagAPI
      */
     public static void refreshPlayer(Player player, JavaPlugin plugin) {
         if (isEnabled(plugin)) {
             TagAPI.refreshPlayer(player);
         }
     }
 
     /**
      * If TagAPI is enabled, then refresh player tag for a certain player.
      *
      * @param player    player with tag to refresh
      * @param forWhom   player to update display to
      * @param plugin    plugin with possible TagAPI
      */
     public static void refreshPlayer(Player player, Player forWhom, JavaPlugin plugin) {
         if (isEnabled(plugin)) {
             TagAPI.refreshPlayer(player, forWhom);
         }
     }
 
     /**
      * If TagAPI is enabled, then refresh player tag for a certain players.
      *
      * @param player    player with tag to refresh
      * @param forWhom   players to update display to
      * @param plugin    plugin with possible TagAPI
      */
     public static void refreshPlayer(Player player, Set<Player> forWhom, JavaPlugin plugin) {
         if (isEnabled(plugin)) {
             TagAPI.refreshPlayer(player, forWhom);
         }
     }
 
     /**
      * Reloads all the player tags on the server.
      * Useful if the tags have changed in the configuration
      *
      * Only refreshes nearby players that are visible (and not all player's in
      * that world) to improve security against hacked clients.
      * 
      * @param plugin    plugin with possible TagAPI
      */
     public static void reload(JavaPlugin plugin) {
         if (isEnabled(plugin)) {
             final int maxPlayerViewDistance = plugin.getServer().getViewDistance() * CHUNK_WIDTH;
             
             for (Player player : plugin.getServer().getOnlinePlayers()) {
                 final int maxWorldHeight = player.getWorld().getMaxHeight();
                 final Set<Player> tagsToUpdate = new HashSet();
                 
                 final List<Entity> nearbyEntities = player.getNearbyEntities(maxPlayerViewDistance, maxWorldHeight, maxPlayerViewDistance);
                 for (Entity e : nearbyEntities) {
                     if (e instanceof Player) {
                         final Player p = (Player) e;
                         if (player.canSee(p)) {
                             tagsToUpdate.add(p);
                         }
                     }
                 }
                 
                TagAPI.refreshPlayer(player, tagsToUpdate);
             }
         }
     }
 }
