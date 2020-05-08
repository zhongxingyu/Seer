 package org.bxmy.shiftclock.shiftduty;
 
 import org.bxmy.shiftclock.Util;
 
 public class Alarm {
 
     private Watch watch;
 
     private int alarmBeforeSeconds;
 
     private int intervalSeconds;
 
     public Alarm(Watch watch, int alarmBeforeSeconds, int intervalSeconds) {
         this.watch = watch;
         this.alarmBeforeSeconds = alarmBeforeSeconds;
         this.intervalSeconds = intervalSeconds;
     }
 
     public String getDate() {
         return Util.formatDate(watch.getDayInSeconds());
     }
 
     public long getBeginSeconds() {
         return watch.getDayInSeconds() - watch.getBeforeSeconds();
     }
 
     public long getEndSeconds() {
         return watch.getDayInSeconds() + watch.getDutyDurationSeconds()
                 + watch.getAfterSeconds();
     }
 
     public long getAlarmBeforeSeconds() {
         return alarmBeforeSeconds;
     }
 
     public long getIntervalSeconds() {
         return intervalSeconds;
     }
 
     public void disable() {
         if (watch.getAlarmStopped() == 0) {
             watch.setAlarmStopped(1);
             if (watch.getAlarmPausedInSeconds() > 0)
                 watch.setAlarmPasuedInSeconds(0);
 
             ShiftDuty.getInstance().updateWatch(watch);
         }
     }
 
     public void pause() {
         if (watch.getAlarmStopped() == 0) {
             watch.setAlarmPasuedInSeconds(Util.now());
             ShiftDuty.getInstance().updateWatch(watch);
         }
     }
 
     public boolean isValidAlarm(long alarmTime) {
         if (watch.getAlarmStopped() != 0)
             return false;
 
         if (watch.getAlarmPausedInSeconds() > 0
                 && alarmTime < watch.getAlarmPausedInSeconds()) {
             return false;
         }
 
         long first = getBeginSeconds() - getAlarmBeforeSeconds();
         if (alarmTime < first)
             return false;
 
         return true;
     }
 
     public long getNextAlarmSeconds() {
         if (watch.getAlarmStopped() != 0)
             return 0;
 
         long now = Util.now();
         if (now >= getBeginSeconds()) {
             if (watch.getAlarmPausedInSeconds() == 0) {
                 return getBeginSeconds();
             } else {
                 return watch.getAlarmPausedInSeconds() + getIntervalSeconds();
             }
         }
 
         long first = getBeginSeconds() - getAlarmBeforeSeconds();
         if (now < first)
             return first;
 
         return first + (now - first + intervalSeconds - 1) / intervalSeconds
                 * intervalSeconds;
     }
 
     public String getWatchTime() {
         return Util.formatDateTimeToNow(getBeginSeconds()) + " 至 "
                 + Util.formatTimeByOther(getBeginSeconds(), getEndSeconds());
     }
 
     public boolean isSame(Alarm old) {
         if (old == null)
             return false;
 
         return this.getDate().equals(old.getDate())
                 && this.getBeginSeconds() == old.getBeginSeconds()
                && this.getIntervalSeconds() == old.getIntervalSeconds();
     }
 }
