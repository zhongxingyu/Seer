 package ru.openitr.exinformer;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.*;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Created by
  * User: Oleg Balditsyn
  * Date: 24.04.13
  * Time: 9:41
  * Сервис запрашивает информацию с сервера cbr.ru и помещает её в БД.
  */
 public class InfoRefreshService extends Service {
 
     AlarmManager alarms;
     PendingIntent alarmIntent;
     protected Calendar nextExecuteTime = Calendar.getInstance();
     protected long nextExecuteTimeInMills;
     private boolean autoupdate = false;
     private boolean lastInfo = false;
     private boolean onlySetAlarm;
     private DailyInfoStub dailyInfo;
     @Override
     public void onCreate() {
         super.onCreate();
         dailyInfo = new DailyInfoStub();
         nextExecuteTimeInMills = 0;
         if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Service created");
         Context context = getApplicationContext();
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
         autoupdate = sharedPreferences.getBoolean("PREF_AUTO_UPDATE", false);
         int hourOfRefresh = sharedPreferences.getInt("PREF_UPDITE_TIME.hour", 0);
         int minuteOfRefresh = sharedPreferences.getInt("PREF_UPDITE_TIME.minute", 0);
         if (autoupdate) {
             alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
             String ALARM_ACTION;
             ALARM_ACTION = InfoRefreshReciever.ACTION_REFRESH_INFO_ALARM;
             Intent intentToFire = new Intent(ALARM_ACTION);
             alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
             nextExecuteTime.set(Calendar.HOUR_OF_DAY, hourOfRefresh);
             nextExecuteTime.set(Calendar.MINUTE, minuteOfRefresh);
 //            nextExecuteTime.roll(Calendar.DAY_OF_YEAR, true);
             nextExecuteTimeInMills = nextExecuteTime.getTimeInMillis();
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Calendar onDate = Calendar.getInstance();
         Long dateFromExtraParam = intent.getLongExtra(main.PARAM_DATE,0);
         onDate.setTimeInMillis(dateFromExtraParam);
         onlySetAlarm = intent.getBooleanExtra(main.PARAM_ONLY_SET_ALARM, false);
         if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Service onStartCommand execute refresh task.");
         new refreshCurrencyTask().execute(onDate);
         return Service.START_NOT_STICKY;
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Destroy service.");
     }
 
     private class refreshCurrencyTask extends AsyncTask<Calendar, Integer, Integer> {
         private static final int OK = 20;
         private static final int STATUS_NETWORK_DISABLE = 30;
         private static final int STATUS_NOT_RESPOND = 40;
         private static final int STATUS_NO_DATA = 50;
         private static final int STATUS_NOT_FRESH_DATA = 60;
         static final String CURRENCY_URI = "content://ru.openitr.exinformer.currency/currencys";
         boolean startFromNulldate = false;
         @Override
         protected Integer doInBackground(Calendar... params) {
 
             if (onlySetAlarm){
                 if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Only need update alarmSet");
                 return OK;
             }
             Calendar onDate = Calendar.getInstance();
             if (params[0].getTimeInMillis() == 0 ){
                 startFromNulldate = true;
                 try {
                     onDate = dailyInfo.getLatestDate();
                     lastInfo = true;
                     if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: onDate = 0. getLastDate return: " + onDate.getTime().toLocaleString());
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             else {
                 onDate = params[0];
             }
             boolean infoNeedUpdate = new CurrencyDbAdapter(getBaseContext()).isNeedUpdate(onDate);
             if (infoNeedUpdate ){
                 if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Data need to update. newDate = " + onDate.getTime().toString() + " onDate = " + onDate.getTime().toString() + " infoNeedUpdate = "+infoNeedUpdate);
                 publishProgress();
                 return getCursOnDate(onDate);
             }
             if (ExtraCalendar.isToday(onDate)) return STATUS_NOT_FRESH_DATA;
             return OK;
         }
 
         @Override
         protected void onProgressUpdate(Integer... progress) {
             super.onProgressUpdate(progress);
             Intent intent = new Intent(main.INFO_REFRESH_INTENT);
             intent.putExtra(main.PARAM_STATUS, main.STATUS_BEGIN_REFRESH);
             sendBroadcast(intent);
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             int alarmType = AlarmManager.RTC;
             super.onPostExecute(result);
             Intent resIntent = new Intent(main.INFO_REFRESH_INTENT);
             Intent widgetUpdateIntent = new Intent(CurrencyWidget.CURRENCY_WIDGET_UPDATE);
             switch (result) {
                 case STATUS_NOT_RESPOND:
                     resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_NOT_RESPOND);
                     nextExecuteTimeInMills = System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                     break;
                 case STATUS_NETWORK_DISABLE:
                     resIntent.putExtra(main.PARAM_STATUS, main.FINS_STATUS_NETWORK_DISABLE);
                     nextExecuteTimeInMills = System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR;
                     break;
                 case STATUS_NO_DATA:
                     resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_NO_DATA);
                     nextExecuteTimeInMills = System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                     break;
                 case STATUS_NOT_FRESH_DATA:
                     resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_OK);
                     if ((System.currentTimeMillis() - nextExecuteTime.getTimeInMillis()) < (AlarmManager.INTERVAL_HOUR*4))
                         nextExecuteTimeInMills = System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR;
                    break;
                 default:
                     resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_OK);
                     if (nextExecuteTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
                         nextExecuteTime.roll(Calendar.DAY_OF_YEAR,true);
                     nextExecuteTimeInMills = nextExecuteTime.getTimeInMillis();
             }
             if (autoupdate) {
                 //nextExecuteTimeInMills = System.currentTimeMillis()+1000*60*10;
                 alarms.set(alarmType, nextExecuteTimeInMills, alarmIntent);
                 if (main.DEBUG) {
                     LogSystem.logInFile(main.LOG_TAG, "Service: (onPostExecute) Result of service: " + result);
                     LogSystem.logInFile(main.LOG_TAG, "Alarm is set to " + new Date(nextExecuteTimeInMills).toLocaleString());
                 }
             }
             sendBroadcast(resIntent);
             if (lastInfo){
                 if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: lastInfo = " + lastInfo);
                 widgetUpdateIntent.putExtra("CURS_TIME",Calendar.getInstance().getTimeInMillis());
                 sendBroadcast(widgetUpdateIntent);
             }
 
 
             stopSelf();
         }
 
         private int getCursOnDate(Calendar onDate) {
             ContentResolver cr = getContentResolver();
 
             int res = OK;
             if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Info need to update.");
             if (!internetAvailable()) {
                 return STATUS_NETWORK_DISABLE;
             }
             if (ExtraCalendar.isToday(onDate) && startFromNulldate) res = STATUS_NOT_FRESH_DATA;
             try {
                 ArrayList <Icurrency> infoStub = dailyInfo.getCursOnDate(onDate);
                 if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Start update base.");
                 for (Icurrency icurrencyRecord : infoStub) {
                     ContentValues _cv = icurrencyRecord.toContentValues();
                     if (cr.update(Uri.parse(CURRENCY_URI + "/" + icurrencyRecord.getVchCode()),_cv,null,null) == 0) {
                         cr.insert(Uri.parse(CURRENCY_URI), _cv);
                     }
                 }
                 if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Stop update base.");
             } catch (IOException e) {
                 e.printStackTrace();
                 res = STATUS_NOT_RESPOND;
 
             } catch (ArrayIndexOutOfBoundsException e) {
                 e.printStackTrace();
                 res = STATUS_NO_DATA;
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             return res;
         }
 
     }
 
     private boolean internetAvailable() {
         ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
         if (cm == null) {
             return false;
         }
         NetworkInfo[] netInfo = cm.getAllNetworkInfo();
         if (netInfo == null) {
             return false;
         }
         for (NetworkInfo ni : netInfo)
         {
             if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                 if (ni.isConnected()) {
                     if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "test: wifi connection found");
                     return true;
                 }
             if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                 if (ni.isConnected()) {
                     if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "test: mobile connection found");
                     return true;
                 }
         }
         if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "test: Network connection not found");
         return false;
     }
 
     public void setAlarm(long executeTime){
         AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         String ALARM_ACTION;
         ALARM_ACTION = InfoRefreshReciever.ACTION_REFRESH_INFO_ALARM;
         Intent intentToFire = new Intent(ALARM_ACTION);
         PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
 
 
     }
 
 }
