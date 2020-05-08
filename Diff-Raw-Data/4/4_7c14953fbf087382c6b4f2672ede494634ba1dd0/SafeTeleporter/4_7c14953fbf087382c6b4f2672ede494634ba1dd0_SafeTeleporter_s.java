 package me.limebyte.battlenight.core.util;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.battle.Waypoint;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.kitteh.tag.TagAPI;
 
 public class SafeTeleporter {
 
     private static Queue<String> playerQueue = new LinkedList<String>();
     private static Queue<Waypoint> waypointQueue = new LinkedList<Waypoint>();
     private static int taskID = 0;
 
     public static void queue(Player player, Waypoint waypoint) {
         playerQueue.add(player.getName());
         waypointQueue.add(waypoint);
     }
 
     public static void tp(Player player, Waypoint waypoint) {
         safeTP(player, waypoint);
     }
 
     public static void startTeleporting() {
         taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(BattleNight.instance, new Runnable() {
             public void run() {
                 if (playerQueue.isEmpty()) {
                     stopTeleporting();
                 } else {
                     safeTP(Bukkit.getPlayerExact(playerQueue.poll()), waypointQueue.poll());
                 }
             }
         }, 0L, 10L);
     }
 
     private static void stopTeleporting() {
         Bukkit.getServer().getScheduler().cancelTask(taskID);
         taskID = 0;
     }
 
     private static void safeTP(final Player player, Waypoint waypoint) {
         final Location loc = waypoint.getLocation();
 
         BattleNight.BattleTelePass.put(player.getName(), "yes");
         player.teleport(loc, TeleportCause.PLUGIN);
         player.setAllowFlight(true);
         player.setFlying(true);
 
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BattleNight.instance, new Runnable() {
             public void run() {
                 player.teleport(loc, TeleportCause.PLUGIN);
                 BattleNight.BattleTelePass.remove(player.getName());
                 player.setFlying(false);
                player.setAllowFlight(false);
                 try {
                     TagAPI.refreshPlayer(player);
                 } catch (Exception e) {
                 }
             }
         }, 10L);
     }
 }
