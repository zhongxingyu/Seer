 package net.fififox.dailycalendaralarm;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.util.Log;
 
 /**
  * An service which periodically checks for events in the calendar and rings before the first one each day.
  * @author Joan Rieu
  */
 public class Service extends IntentService {
 
     /**
      * The request code used to schedule event searches {@link PendingIntent}s in the {@link AlarmManager}.
      */
     private static final int SEARCH_RQ = 0;
 
     /**
      * The request code used to schedule alarms {@link PendingIntent}s in the {@link AlarmManager}.
      */
     private static final int RUN_RQ = 1;
 
     /**
      * The request code used to launch the {@link Notification} and the {@link AlarmActivity}.
      */
     private static final int ALERT_RQ = 2;
 
     /**
      * The tag under which the service status is logged.
      */
     private static final String LOG_TAG = "DCA";
 
     /**
      * The amount of milliseconds in a day.
      */
     private static final long MILLIS_IN_ONE_DAY = 24 * 60 * 60 * 1000;
 
     private static final long SEARCH_INTERVAL = AlarmManager.INTERVAL_HOUR;
 
     /**
      * The preferences shared with the {@link SetupActivity}.
      */
     private Settings mSettings;
 
     /**
      * Describes a calendar event with helper methods for alarm times.
      */
     private static class Alarm {
 
         /**
          * The name of the calendar event.
          */
         private final String mEventTitle;
 
         /**
          * The date and time that the calendar event will begin at.
          */
         private final long mEventTime;
 
         /**
          * Fills in a calendar event's data and logs it.
          * @param eventTitle The name of the event.
          * @param eventTime The date and time of the event.
          */
         public Alarm(String eventTitle, long eventTime) {
             mEventTitle = eventTitle;
             mEventTime = eventTime;
             log();
         }
 
         /**
          * Gets the name of the calendar event.
          * @return The name of the event.
          */
         public String getEventTitle() {
             return mEventTitle;
         }
 
         /**
          * Gets the date and time that the calendar event will begin at.
          * @return The date and time of the calendar event's beginning.
          */
         public long getEventTime() {
             return mEventTime;
         }
 
         /**
          * Gets the date and time that the alarm for this event should ring at.
          * @param offset The time to subtract from the event's start time.
          * @return The date and time the alarm should ring at.
          */
         public long getAlarmTime(long offset) {
             return mEventTime - offset;
         }
 
         /**
          * Logs this event's title, date and time.
          */
         private void log() {
             DateFormat format = SimpleDateFormat.getDateTimeInstance();
             String date = format.format(new Date(getEventTime()));
             Log.v(LOG_TAG, "Event `" + getEventTitle() + "' at " + date + ".");
         }
 
     }
 
     /**
      * Creates and names the worker thread.
      */
     public Service() {
         super("DailyCalendarAlarm");
     }
 
     /**
      * Analyzes received {@link Intent}s from {@link SetupActivity} and calls the appropriate functions.
      * @param intent The received {@link Intent}.
      */
     @Override
     protected void onHandleIntent(Intent intent) {
 
         mSettings = new Settings(this);
 
         if (intent.getAction() == Intent.ACTION_CONFIGURATION_CHANGED) {
             updateSearchSchedule();
         } else if (intent.getAction() == Intent.ACTION_RUN) {
             runAlarm();
             updateNextAlarm();
         } else if (intent.getAction() == Intent.ACTION_SEARCH) {
             updateNextAlarm();
         } else {
             Log.w(LOG_TAG, "Service received unknown intent with action " + intent.getAction() + ".");
         }
 
         mSettings = null;
 
     }
 
     /**
      * Defines or cancels a repeating event search {@link Intent}, depending on {@link Settings#isEnabled()}.
      */
     private void updateSearchSchedule() {
 
         Log.v(LOG_TAG, "Updating search schedule...");
 
         AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
         Intent searchIntent = new Intent(this, Service.class);
         searchIntent.setAction(Intent.ACTION_SEARCH);
         PendingIntent pendingSearch = PendingIntent.getService(this, SEARCH_RQ, searchIntent, 0);
 
         if (mSettings.isEnabled()) {
 
             Log.v(LOG_TAG, "Search ON.");
 
             alarmManager.setInexactRepeating(
                     AlarmManager.RTC_WAKEUP,
                     0,
                     SEARCH_INTERVAL,
                     pendingSearch
             );
 
         } else {
 
             Log.v(LOG_TAG, "Search OFF.");
 
             alarmManager.cancel(pendingSearch);
             updateNextAlarm();
 
         }
     }
 
     /**
      * Sets up an alarm if a calendar event is found matching the app's requirements.
      */
     private void updateNextAlarm() {
 
         Log.v(LOG_TAG, "Updating next alarm...");
 
         AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
         Intent runIntent = new Intent(this, Service.class);
         runIntent.setAction(Intent.ACTION_RUN);
         PendingIntent pendingRun = PendingIntent.getService(this, RUN_RQ, runIntent, 0);
 
         if (mSettings.isEnabled()) {
 
             Alarm alarm = getNextAlarm();
 
             if (alarm != null) {
 
                 Log.v(LOG_TAG, "Alarm ON " + mSettings.getOffsetInMinutes() + " minutes before.");
 
                 long time = alarm.getAlarmTime(mSettings.getOffsetInMillis());
                 alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingRun);
 
                 mSettings.setNextAlarmTitle(alarm.getEventTitle());
                 mSettings.setNextAlarmTime(time);
 
             } else {
 
                 Log.v(LOG_TAG, "Alarm OFF (no event).");
 
                 alarmManager.cancel(pendingRun);
                 mSettings.clearNextAlarm();
 
             }
 
         } else {
 
             Log.v(LOG_TAG, "Alarm OFF (disabled).");
 
             alarmManager.cancel(pendingRun);
             mSettings.clearNextAlarm();
 
         }
 
     }
 
     /**
      * Searches for events in the calendar matching all of the following criteria:
      * <ul>
      *     <li>the event is the first of its day (either today or tomorrow);</li>
      *     <li>the event doesn't last all day;</li>
      *     <li>the event's alarm hasn't already rung.</li>
      * </ul>
      * @return
      */
     private Alarm getNextAlarm() {
 
         Alarm alarm;
 
         long now = System.currentTimeMillis();
         long midnightUTC = (now / MILLIS_IN_ONE_DAY) * MILLIS_IN_ONE_DAY;
         long today = midnightUTC - Calendar.getInstance().getTimeZone().getOffset(midnightUTC);
         long tomorrow = today + MILLIS_IN_ONE_DAY;
 
         String[] projection = new String[] { Events.TITLE, Events.DTSTART };
         String selection = Events.DTSTART + " >= ?";
         selection += " AND " + Events.DTSTART + " < ?";
         selection += " AND " + Events.ALL_DAY + " < 1";
         String[] selectionArgs = new String[] { Long.toString(today), Long.toString(tomorrow) };
         String sortOrder = Events.DTSTART;
 
         Cursor cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                 projection, selection, selectionArgs, sortOrder);
 
         if (cursor.moveToFirst()) {
             Log.v(LOG_TAG, "Found an event today.");
             alarm = new Alarm(cursor.getString(0), cursor.getLong(1));
         } else {
             Log.v(LOG_TAG, "No event in calendar for today, checking tomorrow...");
             alarm = null;
         }
 
         cursor.close();
 
        if (alarm == null
                || (alarm.getEventTime() > now
                        && alarm.getAlarmTime(mSettings.getOffsetInMillis()) <= mSettings.getLastAlarmTime())) {
 
             if (alarm != null) {
                 Log.v(LOG_TAG, "But the alarm already rang, checking tomorrow...");
             }
 
             selectionArgs[0] = selectionArgs[1];
             selectionArgs[1] = Long.toString(tomorrow + MILLIS_IN_ONE_DAY);
 
             cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                     projection, selection, selectionArgs, sortOrder);
 
             if (cursor.moveToFirst()) {
                 Log.v(LOG_TAG, "Found an event tomorrow.");
                 alarm = new Alarm(cursor.getString(0), cursor.getLong(1));
             } else {
                 Log.v(LOG_TAG, "No event found tomorrow.");
                 alarm = null;
             }
 
             cursor.close();
 
             if (alarm != null
                    && alarm.getAlarmTime(mSettings.getOffsetInMillis()) <= mSettings.getLastAlarmTime()) {
                 Log.v(LOG_TAG, "But the alarm already rang.");
                 alarm = null;
             }
 
         }
 
         return alarm;
 
     }
 
     /**
      * Rings the alarm.
      * It uses a Notification with:
      * <ul>
      *     <li>the event's title;</li>
      *     <li>the event's date and time;</li>
      *     <li>the default alarm ringtone;</li>
      *     <li>a custom light pattern;</li>
      *     <li>the default notification vibration pattern;</li>
      *     <li>the highest priority.</li>
      * </ul>
      */
     @SuppressWarnings("deprecation")
     private void runAlarm() {
 
         Log.v(LOG_TAG, "ALARM!");
 
         Alarm alarm = new Alarm(
                 mSettings.getNextAlarmTitle(),
                 mSettings.getNextAlarmTime() + mSettings.getOffsetInMillis()
         );
 
         mSettings.setLastAlarmTime(System.currentTimeMillis());
         mSettings.clearNextAlarm();
 
         Intent intent = new Intent(this, AlarmActivity.class);
         intent.putExtra("title", alarm.getEventTitle());
         intent.putExtra("time", alarm.getEventTime());
         intent.putExtra("id", ALERT_RQ);
 
         PendingIntent pending = PendingIntent.getActivity(
                 this,
                 ALERT_RQ,
                 intent,
                 PendingIntent.FLAG_CANCEL_CURRENT
         );
 
         Notification.Builder builder = new Notification.Builder(this)
                 .setSmallIcon(R.drawable.ic_launcher)
                 .setContentTitle(alarm.getEventTitle())
                 .setContentText(getText(R.string.app_name))
                 .setWhen(alarm.getEventTime())
                 .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                         AudioManager.STREAM_ALARM)
                 .setLights(0xFFFFFFFF, 200, 200)
                 .setDefaults(Notification.DEFAULT_VIBRATE)
                 .setAutoCancel(true)
                 .setFullScreenIntent(pending, true);
 
         Notification notification = builder.getNotification();
         notification.flags |= Notification.FLAG_INSISTENT;
 
         NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notificationManager.notify(ALERT_RQ, notification);
 
     }
 
 }
