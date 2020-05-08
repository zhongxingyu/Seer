 package com.gmail.zariust.LightVote;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.bukkit.Bukkit;
 
 public class PermanentTime {
    private static Timer timer = null;
     public static final int nightstart = 14000;
 
     public static void setReset() {
         timer = new Timer();
         timer.schedule(new timeReset(), 15000, 15000);
     }
 
     static class timeReset extends TimerTask {
     	@Override
         public void run(){
             long currenttime = Bukkit.getServer().getWorlds().get(0).getTime();
     		boolean isNight = !Utils.isDay(currenttime);
     		currenttime = currenttime - (currenttime % 24000); // one day lasts 24000
             currenttime += LightVote.config.permaOffset;
             if (isNight)
                 currenttime += nightstart;
             Bukkit.getServer().getWorlds().get(0).setTime(currenttime);
     	}
     }
 
 }
