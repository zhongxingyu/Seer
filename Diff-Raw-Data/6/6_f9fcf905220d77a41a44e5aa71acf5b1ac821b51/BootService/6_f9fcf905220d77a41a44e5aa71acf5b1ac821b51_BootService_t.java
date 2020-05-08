 package fr.ybo.ybotv.android.service;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.exception.YboTvException;
 import fr.ybo.ybotv.android.modele.Channel;
 import fr.ybo.ybotv.android.modele.Programme;
 import fr.ybo.ybotv.android.receiver.AlertReceiver;
 import fr.ybo.ybotv.android.util.PreferencesUtil;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class BootService extends Service {
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         ((YboTvApplication) getApplication()).setRecurringAlarm();
 
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
 
         Set<String> idsInNotifs = PreferencesUtil.getStringSets(prefs, "ybo-tv.programme.alert.ids");
         Log.d(YboTvApplication.TAG, "idsInNotids : " + idsInNotifs);
 
         for (String idProgramme : idsInNotifs) {
             if (prefs.getBoolean("ybo-tv.programme.alert." + idProgramme, false))  {
                 Programme programme = getProgrammeById(idProgramme);
                if (programme != null) {
                    Channel channel = getChannelById(programme.getChannel());
                    createNotif(programme, channel);
                }
             }
         }
         stopSelf();
         return START_STICKY;
     }
 
     private Programme getProgrammeById(String id) {
         Programme programmeSelect = new Programme();
         programmeSelect.setId(id);
         return ((YboTvApplication) getApplication()).getDatabase().selectSingle(programmeSelect);
     }
 
     private Channel getChannelById(String id) {
         Channel channelSelect = new Channel();
         channelSelect.setId(id);
         return ((YboTvApplication) getApplication()).getDatabase().selectSingle(channelSelect);
     }
 
     private void createNotif(Programme programme, Channel channel) {
 
         Log.d(YboTvApplication.TAG, "Create notif for " + programme.getStart());
 
         long timeToNotif;
         try {
             // The notification is 3 minutes before programme start.
             timeToNotif = new SimpleDateFormat("yyyyMMddHHmmss").parse(programme.getStart()).getTime() - (3 * 60 * 1000);
         } catch (ParseException e) {
             throw new YboTvException(e);
         }
 
 
         Intent alert = new Intent(this, AlertReceiver.class);
         alert.putExtra("programme", (Parcelable) programme);
         alert.putExtra("channel", (Parcelable) channel);
         int notificationId = Integer.parseInt(programme.getStart().substring(8));
 
         PendingIntent pendingAlert = PendingIntent.getBroadcast(this, notificationId, alert, PendingIntent.FLAG_CANCEL_CURRENT);
         AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         alarms.set(AlarmManager.RTC_WAKEUP, timeToNotif, pendingAlert);
     }
 }
