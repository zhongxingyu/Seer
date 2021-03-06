 package me.chaseoes.tf2.utilities;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Scanner;
 import java.util.logging.Level;
 
 import me.chaseoes.tf2.TF2;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class UpdateChecker {
 
     TF2 plugin;
     private String latestVersion;
 
     public UpdateChecker(TF2 p) {
         plugin = p;
         latestVersion = plugin.getDescription().getVersion();
     }
 
     public boolean needsUpdate() {
         return !latestVersion.equalsIgnoreCase(plugin.getDescription().getVersion());
     }
 
     public void nagPlayer(Player player) {
         player.sendMessage(ChatColor.YELLOW + "[TF2]" + ChatColor.DARK_RED + " Version " + latestVersion + " is available! Please update ASAP.");
         player.sendMessage(ChatColor.RED + "http://dev.bukkit.org/server-mods/team-fortress-2/");
     }
 
     @SuppressWarnings("deprecation")
     public void startTask() {
         plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
             @Override
             public void run() {
                 checkForUpdate();
             }
        }, 0L, 36000L);
     }
 
     public void checkForUpdate() {
         if (plugin.getConfig().getBoolean("update-checking")) {
             try {
                final URL url = new URL("http://plugins.skitscape.com/tf2/latest.php");
                 InputStream i = url.openStream();
                 Scanner scan = new Scanner(i);
                 String ver = scan.nextLine();
                 i.close();
                 latestVersion = ver;
                 return;
             } catch (Exception e) {
                 plugin.getLogger().log(Level.WARNING, "An error was encountered while attempting to check for updates.");
             }
         }
         latestVersion = plugin.getDescription().getVersion();
     }
 
 }
