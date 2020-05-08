 package com.dutymator.service;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import com.dutymator.*;
 
 import java.util.Date;
 
 /**
  * @author Zsolt Takacs <takacs.zsolt@ustream.tv>
  */
 public class RedirectService extends Service
 {
     @Override
     public void onStart(Intent intent, int startId)
     {
         super.onStart(intent, startId);
 
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int calendarId = Integer.parseInt(settings.getString(Preferences.CALENDAR_ID, "-1"));
 
         CalendarReader reader = new CalendarReader();
         ContactsReader contactsReader = new ContactsReader();
 
         try {
             Event activeEvent = reader.getActiveEventFromCalendar(
                 this, calendarId, new Date(settings.getLong(Preferences.ALL_DAY_FROM, 0)), new Date(settings.getLong(Preferences.ALL_DAY_TO, 0))
             );
 
             if (activeEvent != null) {
                 String number = contactsReader.getNumber(this, activeEvent.title);
 
                 if (!activeEvent.title.equals(Redirecter.lastRedirectedContact)) {
                     if (number != null) {
                         Intent callIntent = new Intent(Intent.ACTION_CALL);
                         callIntent.setData(Uri.fromParts("tel", "**21*\\" + number + "\\#", ""));
                         callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         if (!settings.getBoolean(Preferences.DRY_RUN, false)) {
                             startActivity(callIntent);
                         }
 
                         Redirecter.lastRedirectedContact = activeEvent.title;
 
                         String message = "Redirected to " + activeEvent.title + " (" + number + ")";
 
                         Logger.log(this, Log.INFO, message);
                         Notifier.notifyRedirect(getApplicationContext(), message);
                     } else {
                         Logger.log(this, Log.INFO, "No number for name " + activeEvent.title);
                     }
                 } else {
                     Logger.log(this, Log.VERBOSE, "Already redirected to " + activeEvent.title + " (" + number + ")");
                 }
             } else {
                 Logger.log(this, Log.VERBOSE, "No active event found!");
             }
 
             Redirecter.scheduleBySettings(this);
         } catch (IntermittentException e) {
             Logger.log(this, Log.INFO, "Intermittent error: " + e.toString() + " stacktrace: " + e.getStackTrace().toString());
             Redirecter.scheduleInSeconds(this, 30);
         }
     }
 
     @Override
     public IBinder onBind(Intent intent)
     {
         return null;
     }
 }
