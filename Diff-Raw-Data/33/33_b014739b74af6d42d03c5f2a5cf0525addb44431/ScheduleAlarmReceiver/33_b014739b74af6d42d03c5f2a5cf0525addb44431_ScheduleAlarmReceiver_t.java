 /**
  * @file   ScheduleAlarmReceiver.java
  * @author josh <yenliangl at gmail dot com>
  * @date   Tue Nov 10 17:47:36 2009
  *
  * A receiver that is usually called when user wants to reshedule
  * an alarm.
  *
  */
 
 package org.startsmall.openalarm;
 
 import android.content.Context;
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.net.Uri;
 import android.text.format.DateUtils;
 import android.util.Log;
 import java.util.Calendar;
 
 public class ScheduleAlarmReceiver extends BroadcastReceiver {
     private static final String TAG = "ScheduleAlarmReceiver";
 
     @Override
     public void onReceive(Context context, Intent intent) {
         Log.d(TAG, "===> ScheduleAlarmReceiver.onReceive()");
 
         // Disable old alarm
         final int alarmId = intent.getIntExtra(Alarms.AlarmColumns._ID, -1);
         final Uri alarmUri = Alarms.getAlarmUri(alarmId);
         final String handlerClassName =
             intent.getStringExtra(Alarms.AlarmColumns.HANDLER);
         Alarms.disableAlarm(context, alarmId, handlerClassName);
 
         // Reschedule this alarm.
         final int hourOfDay = intent.getIntExtra(Alarms.AlarmColumns.HOUR, -1);
        final int minutes = intent.getIntExtra(Alarms.AlarmColumns.MINUTES, -1);
         final int repeatOnDaysCode = intent.getIntExtra(Alarms.AlarmColumns.REPEAT_DAYS, -1);
         final long atTimeInMillis =
             Alarms.calculateAlarmAtTimeInMillis(hourOfDay, minutes, repeatOnDaysCode);
 
         Log.d(TAG, "===> Reschedule alarm " + alarmId + " to " +
               DateUtils.formatDateTime(
                   context,
                   atTimeInMillis,
                   DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_CAP_AMPM|DateUtils.FORMAT_SHOW_WEEKDAY|DateUtils.FORMAT_SHOW_YEAR));
 
         final String label = intent.getStringExtra(Alarms.AlarmColumns.LABEL);
         final String extraData = intent.getStringExtra(Alarms.AlarmColumns.EXTRA);
        Alarms.enableAlarm(context, alarmId, label, atTimeInMillis,
                           repeatOnDaysCode, handlerClassName, extraData);
 
         // Update the new time into database.
         ContentValues newValues = new ContentValues();
         newValues.put(Alarms.AlarmColumns.AT_TIME_IN_MILLIS, atTimeInMillis);
         Alarms.updateAlarm(context, alarmUri, newValues);
     }
 }
