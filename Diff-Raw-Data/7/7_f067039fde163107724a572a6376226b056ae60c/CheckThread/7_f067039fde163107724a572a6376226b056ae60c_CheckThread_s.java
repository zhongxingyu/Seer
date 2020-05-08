 package de.minestar.diehard.threads;
 
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import de.minestar.diehard.core.DieHardCore;
 import de.minestar.diehard.core.DateTimeHelper;
 import de.minestar.diehard.core.Settings;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class CheckThread implements Runnable {
     private static long nextRestartTime;
     private List<Long> warningTimes;
     private int warningTimesIndex;
 
     public CheckThread(List<Long> restartTimes, List<Long> warningTimes) {
         CheckThread.nextRestartTime = getNextRestartTime(restartTimes);
         this.warningTimes = warningTimes;
         warningTimesIndex = 0;
     }
 
     public CheckThread(int minutesUntilRestart, List<Long> warningTimes) {
         CheckThread.setNextRestart(minutesUntilRestart);
         this.warningTimes = warningTimes;
         warningTimesIndex = 0;
     }
 
     private long getNextRestartTime(List<Long> restartTimes) {
         // read current time but remove everything
         // but hours and minutes for compare
         long nowOnlyTime = DateTimeHelper.getOnlyTime(new Date());
 
         long possibleRestartTime = restartTimes.get(0);
         // search restart times after current time or stick to the first in list
         for (long date : restartTimes) {
             if (date > nowOnlyTime) {
                 possibleRestartTime = date;
                 break;
             }
         }
 
         ConsoleUtils.printInfo(DieHardCore.NAME, "Naechste Restart Zeit: " + DateTimeHelper.convertMillisToTime(possibleRestartTime));
         return possibleRestartTime;
     }
 
     private long getNextWarningTime(List<Long> warnTimes, long minutesLeft) {
         long nextWarnTime;
 
         // find best fitting warning until restart from sorted list
         if (warningTimesIndex < warnTimes.size()) {
             nextWarnTime = TimeUnit.MILLISECONDS.toMinutes(warnTimes.get(warningTimesIndex));
             if (nextWarnTime > minutesLeft) {
                 warningTimesIndex++;
                 return getNextWarningTime(warnTimes, minutesLeft);
             }
         } else {
             nextWarnTime = 0;
         }
         return nextWarnTime;
     }
 
     private static void setNextRestart(int minutesUntilRestart) {
         long nowOnlyTime = DateTimeHelper.getOnlyTime(new Date());
        System.out.println(String.format("restart: %d", CheckThread.nextRestartTime));
        System.out.println(String.format("24h: %d", TimeUnit.HOURS.toMillis(24)));
         if (CheckThread.nextRestartTime >= TimeUnit.HOURS.toMillis(24)) {
             CheckThread.nextRestartTime -= TimeUnit.HOURS.toMillis(24);
         }
        CheckThread.nextRestartTime = nowOnlyTime + TimeUnit.MINUTES.toMillis(minutesUntilRestart);
        
         ConsoleUtils.printInfo(DieHardCore.NAME, "Restart Zeit geaendert auf: " + DateTimeHelper.convertMillisToTime(CheckThread.nextRestartTime));
     }
 
     @Override
     public void run() {
         int lastWarning;
         long nextWarnTime;
         // current time as milliseconds since epoch for compare
         long nowOnlyTime = DateTimeHelper.getOnlyTime(new Date());
 
         long diff = TimeUnit.MILLISECONDS.toMinutes(DateTimeHelper.getTimeDifference(nowOnlyTime, nextRestartTime));
         nextWarnTime = getNextWarningTime(warningTimes, diff);
 
         if (diff > 0) {
             if (diff == nextWarnTime) {
                 // remaining time until restart equals next warning time
                 // --> broadcast message to players
                 MessageThread msg = new MessageThread(nextWarnTime);
                 BukkitScheduler sched = Bukkit.getScheduler();
                 sched.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin(DieHardCore.NAME), msg, 1);
                 // take care to have next warning time at top of the list
                 warningTimes.remove(0);
             }
         } else {
             // initiate server restart
             lastWarning = Settings.getLastWarning();
             StopThread stp = new StopThread();
             BukkitScheduler sched = Bukkit.getScheduler();
             sched.scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin(DieHardCore.NAME), stp, 1, DieHardCore.secondsToTicks(lastWarning));
         }
     }
 }
