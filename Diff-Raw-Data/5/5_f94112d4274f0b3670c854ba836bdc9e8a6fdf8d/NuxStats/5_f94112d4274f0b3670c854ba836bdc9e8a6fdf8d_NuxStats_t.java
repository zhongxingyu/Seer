 package net.n4th4.bukkit.nuxstats;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NuxStats extends JavaPlugin {
     private final NSPlayerListener playerListener = new NSPlayerListener(this);
    public Logger                  log;
     public int                     playersCount   = 0;
 
     public NuxStats() {
         try {
             new File("plugins/NuxStats/").mkdirs();
             new File("plugins/NuxStats/playersNumber.txt").createNewFile();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public void onEnable() {
        log = this.getServer().getLogger();

         playersCount = getServer().getOnlinePlayers().length;
         writePlayersNumber();
 
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
     }
 
     public void onDisable() {
         playersCount = 0;
         writePlayersNumber();
     }
 
     public void writePlayersNumber() {
         try {
             OutputStreamWriter osw = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("plugins/NuxStats/playersNumber.txt")), "8859_1");
             osw.write(String.valueOf(playersCount));
             osw.close();
         } catch (FileNotFoundException e) {
             log.severe("[NuxStats] File not found : plugins/NuxStats/playersNumber.txt");
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
