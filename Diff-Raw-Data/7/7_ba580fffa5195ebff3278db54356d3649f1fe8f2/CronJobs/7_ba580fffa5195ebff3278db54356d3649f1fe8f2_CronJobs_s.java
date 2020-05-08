 /*
  * Copyright (C) 2010 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.util.Calendar;
import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * All of the background jobs that run periodically.
  *
  * @author mcculley
  */
 class CronJobs {
 
     private final Timer timer = new Timer("Sarariman");
     private final Sarariman sarariman;
     private final Directory directory;
     private final EmailDispatcher emailDispatcher;
 
     CronJobs(Sarariman sarariman, Directory directory, EmailDispatcher emailDispatcher) {
         this.sarariman = sarariman;
         this.directory = directory;
         this.emailDispatcher = emailDispatcher;
     }
 
     // Useful time intervals in terms of milliseconds;
     private static final long ONE_SECOND = 1000;
     private static final long ONE_MINUTE = 60 * ONE_SECOND;
     private static final long ONE_HOUR = 60 * ONE_MINUTE;
     private static final long ONE_DAY = 24 * ONE_HOUR;
 
     private void scheduleWeeknightTask() {
         // The weeknight task runs once each night and filters out Saturday and Sunday itself.
         Calendar firstTime = Calendar.getInstance();
         Calendar now = Calendar.getInstance();
         firstTime.set(Calendar.HOUR_OF_DAY, 23);
         firstTime.set(Calendar.MINUTE, 0);
         firstTime.set(Calendar.SECOND, 0);
         if (firstTime.before(now)) {
            firstTime.roll(Calendar.DATE, true);
         }
 
         long period = ONE_DAY;
         timer.scheduleAtFixedRate(new WeeknightTask(sarariman, directory, emailDispatcher), firstTime.getTime(), period);
     }
 
     private void scheduleMorningTask() {
         // The morning task runs once each morning.
         Calendar firstTime = Calendar.getInstance();
         Calendar now = Calendar.getInstance();
         firstTime.set(Calendar.HOUR_OF_DAY, 8);
         firstTime.set(Calendar.MINUTE, 0);
         firstTime.set(Calendar.SECOND, 0);
         if (firstTime.before(now)) {
            firstTime.roll(Calendar.DATE, true);
         }
 
         long period = ONE_DAY;
         timer.scheduleAtFixedRate(new MorningTask(sarariman, directory, emailDispatcher), firstTime.getTime(), period);
     }
 
     private void scheduleDirectoryReload() {
         // Reload the directory once an hour.  The main use case is to discover new employees that were added after the application
         // started.
         timer.schedule(new TimerTask() {
 
             public void run() {
                 directory.reload();
             }
 
         }, ONE_HOUR, ONE_HOUR);
     }
 
     void start() {
         scheduleMorningTask();
         scheduleWeeknightTask();
         scheduleDirectoryReload();
     }
 
     void stop() {
         timer.cancel();
     }
 
 }
