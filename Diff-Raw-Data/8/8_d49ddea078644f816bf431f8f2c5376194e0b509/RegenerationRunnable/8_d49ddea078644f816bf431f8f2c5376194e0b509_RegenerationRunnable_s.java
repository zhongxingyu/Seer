 package net.betterverse.unclaimed.util;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class RegenerationRunnable extends BukkitRunnable {
     
     // This is all the chunks that have been regenerated since the last startup.  Key is the X coordinate, Value is Z.
     static Map<Integer, Integer> regenerated = new HashMap<Integer, Integer>();
     JavaPlugin plugin;
     List<String> worlds;
     
     public RegenerationRunnable(JavaPlugin plugin, List<String> worlds) {
         this.worlds = worlds;
     }
 
     @Override
     public void run() {
         int offset = 0;
         for (String w : worlds) {
             final World world = Bukkit.getWorld(w);
             if (world == null) continue;
             for (final Chunk c : world.getLoadedChunks()) {
                 ProtectionInfo info = CheckProtection.isProtected(c);
                 if (!info.isProtected()) {
                     Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                         public void run() {
                             world.regenerateChunk(c.getX(), c.getZ());
                         }
                     }, offset);
                     regenerated.put(c.getX(), c.getZ());
                     offset++;
                 }
             }
         }
     }
     
     public static void clearRegeneratedChunks() {
         regenerated.clear();
     }
 
 }
