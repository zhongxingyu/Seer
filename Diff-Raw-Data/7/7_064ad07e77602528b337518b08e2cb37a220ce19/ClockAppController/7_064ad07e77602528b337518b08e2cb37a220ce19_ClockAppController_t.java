 package com.babycycle.babyfeeding.ui.controller;
 
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.util.Log;
 import com.babycycle.babyfeeding.R;
 import com.google.inject.Singleton;
 
 import java.lang.ref.WeakReference;
 import java.util.Calendar;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dima
  * Date: 5/2/13
  * Time: 12:54 AM
  * To change this template use File | Settings | File Templates.
  */
 @Singleton
 public class ClockAppController {
 
     private static final int START_NIGHT_HOUR = 22;
     private static final int END_NIGHT_HOUR = 6;
     WeakReference<Context> context;
     boolean foundClockImpl = false;
 
     private Intent alarmClockIntent;
 
     private void checkIfClockAppIsAccessible() {
         PackageManager packageManager = context.get().getPackageManager();
         alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
 
         // Verify clock implementation
         String clockImpls[][] = {
                 {"HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
                 {"Standar Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
                 {"Froyo Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
                 {"Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",  "com.motorola.blur.alarmclock.AlarmClock"},
                 {"Samsung Galaxy Clock", "com.sec.android.app.clockpackage","com.sec.android.app.clockpackage.ClockPackage"}
         };
 
         foundClockImpl = false;
 
         for(int i=0; i<clockImpls.length; i++) {
             String vendor = clockImpls[i][0];
             String packageName = clockImpls[i][1];
             String className = clockImpls[i][2];
             try {
                 ComponentName cn = new ComponentName(packageName, className);
                 ActivityInfo aInfo = packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
                 alarmClockIntent.setComponent(cn);
                 Log.e("ClockAppController", "Found " + vendor + " --> " + packageName + "/" + className);
                 foundClockImpl = true;
             } catch (PackageManager.NameNotFoundException e) {
                 Log.e("ClockAppController", vendor + " does not exists");
             }
         }
 
     }
     public void setContext(Context context) {
         this.context = new WeakReference<Context>(context);
     }
 
     public void openClockAppIfNeed() {
         if(isDayTime()) {
             return;
         }
         checkIfClockAppIsAccessible();
         if (!foundClockImpl) {
             return;
         }
         openConfirmationDialogue();
     }
 
     private boolean isDayTime() {
         Calendar calendar = Calendar.getInstance();
         return calendar.get(Calendar.HOUR_OF_DAY) < START_NIGHT_HOUR || calendar.get(Calendar.HOUR_OF_DAY) > END_NIGHT_HOUR;
     }
 
     private void openConfirmationDialogue() {
         AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
 
         builder.setPositiveButton(R.string.alarm_clock_open_confirm, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 openClockAppIntent();
             }
         });
         builder.setNegativeButton(R.string.alarm_clock_open_cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
 
             }
         });
         builder.setTitle(R.string.alarm_clock_open_title);
         builder.create().show();
     }
 
     private void openClockAppIntent() {
         if (foundClockImpl) {
             PendingIntent pendingIntent = PendingIntent.getActivity(context.get(), 0, alarmClockIntent, 0);
             try {
                 pendingIntent.send();
             } catch (PendingIntent.CanceledException e) {
 
             }
         }
     }
 
 
     public void openClockAppIfPossible() {
         PackageManager packageManager = context.get().getPackageManager();
         Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
 
         // Verify clock implementation
         String clockImpls[][] = {
                 {"HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
                 {"Standar Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
                 {"Froyo Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
                 {"Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",  "com.motorola.blur.alarmclock.AlarmClock"},
                 {"Samsung Galaxy Clock", "com.sec.android.app.clockpackage","com.sec.android.app.clockpackage.ClockPackage"}
         };
 
         foundClockImpl = false;
 
         for(int i=0; i<clockImpls.length; i++) {
             String vendor = clockImpls[i][0];
             String packageName = clockImpls[i][1];
             String className = clockImpls[i][2];
             try {
                 ComponentName cn = new ComponentName(packageName, className);
                 ActivityInfo aInfo = packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
                 alarmClockIntent.setComponent(cn);
                 Log.e("ClockAppController", "Found " + vendor + " --> " + packageName + "/" + className);
                 foundClockImpl = true;
             } catch (PackageManager.NameNotFoundException e) {
                 Log.e("ClockAppController", vendor + " does not exists");
             }
         }
 
         if (foundClockImpl) {
             PendingIntent pendingIntent = PendingIntent.getActivity(context.get(), 0, alarmClockIntent, 0);
             try {
                 pendingIntent.send();
             } catch (PendingIntent.CanceledException e) {
 
             }
         }
     }
 }
