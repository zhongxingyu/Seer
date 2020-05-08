 package de.damarus.mcdesktopinfo.queries;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 
 import de.damarus.mcdesktopinfo.McDesktopInfo;
 
 public class Tickrate extends Query {
 
     private Poller poller;
 
     protected Tickrate(String query) {
         super(query, true);
 
         poller = new Poller();
 
         if(!isDisabled()) {
             McDesktopInfo.log("Starting TPS counter thread...");
             Bukkit.getScheduler().scheduleSyncRepeatingTask(McDesktopInfo.getPluginInstance(), poller, 0, Poller.INTERVAL);
         }
     }
 
     @Override
     protected String exec(HashMap<String, String> params) {
         // Round to 2 decimal places
         return Math.round(poller.getTps() * 100) / 100.0 + "";
     }
 
     class Poller implements Runnable {
 
         public static final int INTERVAL = 40;
 
         private long lastRun = System.currentTimeMillis() - INTERVAL / 20 * 1000;
         private double tps;
 
         @Override
         public void run() {
             long now = System.currentTimeMillis();
            tps = INTERVAL / ((now - lastRun) / 1000.0);
             lastRun = now;
         }
 
         public double getTps() {
             return tps;
         }
     }
 }
