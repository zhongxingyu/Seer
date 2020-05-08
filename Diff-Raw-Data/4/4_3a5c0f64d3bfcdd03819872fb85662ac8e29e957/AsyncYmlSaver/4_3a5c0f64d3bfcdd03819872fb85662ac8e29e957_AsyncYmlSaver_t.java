 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.xhawk87.Coinage.moneybags;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.scheduler.BukkitTask;
 
 /**
  *
  * @author XHawk87
  */
 public class AsyncYmlSaver implements Runnable {
 
     private Plugin plugin;
     private FileConfiguration data;
     private String dataCache;
     private File file;
     private BukkitTask task;
     private boolean invalid;
 
     public AsyncYmlSaver(Plugin plugin, FileConfiguration data, File file) {
         this.plugin = plugin;
         this.data = data;
         this.file = file;
     }
 
    public synchronized void save() {
         invalid = true;
         if (task == null) {
             dataCache = data.saveToString();
             invalid = false;
             task = plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this);
         }
     }
 
     @Override
     public void run() {
         try (FileWriter writer = new FileWriter(file)) {
             writer.write(dataCache);
         } catch (IOException ex) {
             plugin.getLogger().log(Level.SEVERE, "Cannot save " + file.getPath() + ":\n" + dataCache, ex);
         }
 
        dataCache = null;
         task = null;
         if (invalid) {
             save();
         }
     }
 
     public FileConfiguration getData() {
         return data;
     }
 }
