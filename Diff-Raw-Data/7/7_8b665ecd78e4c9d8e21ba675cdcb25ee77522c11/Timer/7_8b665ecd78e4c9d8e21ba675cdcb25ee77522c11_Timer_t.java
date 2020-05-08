 package fr.aumgn.bukkitutils.util;
 
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.Plugin;
 
 public abstract class Timer implements Runnable {
 
     private static final int TICKS_PER_SECONDS = 20;
 
     private final Plugin plugin;
     private final int majorDelay;
     private final int minorDelay;
     private final String format;
     private final Runnable runnable;
 
     private int remainingTime;
 
     private int taskId;
     private long taskStartTime;
     private int currentDelay;
     private int pauseDelay;
 
     public Timer(Plugin plugin, TimerConfig config, int seconds, Runnable runnable) {
         this.plugin = plugin;
         this.majorDelay = config.getMajorDuration();
         this.minorDelay = config.getMinorDuration();
         this.format = config.getFormat();
 
         this.taskId = -1;
         this.remainingTime = seconds;
         this.runnable = runnable;
 
         this.currentDelay = 0;
     }
 
     private long getCurrentTimeSeconds() {
         return TimeUnit.MILLISECONDS.toSeconds(
                 System.currentTimeMillis());
     }
 
     private void scheduleAndPrintTime(int delay) {
         long minutes = TimeUnit.SECONDS.toMinutes(remainingTime);
         String msg = String.format(format, minutes, remainingTime % 60);
         sendTimeMessage(getCurrentColor() + msg);
         currentDelay = delay;
         taskStartTime = getCurrentTimeSeconds();
         taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(
                 plugin, this, delay * TICKS_PER_SECONDS);
     }
 
     private ChatColor getCurrentColor() {
         if (remainingTime >= majorDelay) {
             return ChatColor.YELLOW;
         } else if (remainingTime >= minorDelay){
             return ChatColor.GOLD;
         } else {
             return ChatColor.RED;
         }
     }
 
     public int getRemainingTime() {
         return remainingTime;
     }
 
     @Override
     public void run() {
         remainingTime -= currentDelay;
         if (remainingTime > majorDelay) {
             scheduleAndPrintTime(majorDelay);
         } else if (remainingTime > minorDelay) {
             scheduleAndPrintTime(minorDelay);
         } else if (remainingTime > 0) {
             scheduleAndPrintTime(1);
         } else {
             runnable.run();
         }
     }
 
     public void cancel() {
         if (taskId != -1) {
             Bukkit.getScheduler().cancelTask(taskId);
             taskId = -1;
         }
     }
 
     public void pause() {
         cancel();
         pauseDelay = (int) (getCurrentTimeSeconds() - taskStartTime);
         remainingTime -= pauseDelay;
     }
 
     public void resume() {
        int delay = currentDelay - pauseDelay;
        if (delay > 0) {
            scheduleAndPrintTime(delay);
        } else {
            runnable.run();
        }
     }
 
     public abstract void sendTimeMessage(String string);
 }
