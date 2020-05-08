 package edu.brown.cs.systems.cputemp.services;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.BatteryManager;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 import edu.brown.cs.systems.cputemp.ui.MainActivity;
 import edu.brown.cs.systems.cputemp.utils.KernelParameterIO;
 
 public class TemperatureControllerService extends Service {
 
     private static final String TAG = "TemperatureControllerService";
     private boolean enabled;
     private int maxTemperature = DEFAULT_TEMPERATURE;
     private int injectionProb = DEFAULT_INJECTION_PROBABILITY;
     private int taskInterval = 60; /* seconds */
 
    private static final String SYSFS_ENABLE_PATH = "/sys/....";
    private static final String SYSFS_CURRENT_TEMP_PATH = "/sys/....";
    private static final String SYSFS_INJECTION_PROB_PATH = "/sys/....";
 
     private static final int DEFAULT_INJECTION_PROBABILITY = 0;
     private static final int INJECTION_INCREASE_STEP = 2;
     private static final int INJECTION_DECREASE_STEP = 1;
 
     public static final int DEFAULT_TEMPERATURE = 60;
     public static final int MAX_TEMPERATURE = 100;
     public static final int MIN_TEMPERATURE = 0;
 
     private KernelParameterIO kernelIO;
     private PendingIntent sender; // recurring UI update task
 
     public static final String TEMP_CONTROL_ACTION = "edu.brown.cs.systems.cputemp.TEMP_CONTROL";
     public static final String UPDATE_UI_ACTION = "edu.brown.cs.systems.cputemp.UPDATE_UI";
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         kernelIO = KernelParameterIO.getInstance();
         enabled = isIdleInjecting();
         maxTemperature = getMaxTemperature();
         Log.d(TAG, "onCreate()");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         kernelIO.release();
         Log.d(TAG, "onDestroy()");
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.d(TAG, "onStartCommand");
         if (intent.getAction().matches(TEMP_CONTROL_ACTION)) {
             enabled = intent.getBooleanExtra("enabled", false);
             maxTemperature = intent.getIntExtra("maxCpuTemp",
                     DEFAULT_TEMPERATURE);
 
             if (enabled)
                 startTemperatureController(maxTemperature);
             else
                 stopTemperatureController();
         }
 
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     public boolean isIdleInjecting() {
         String ans = kernelIO.readSysFs(SYSFS_ENABLE_PATH);
         return ((ans != null && ans.equals("1")) ? true : false);
     }
 
     private boolean switchIdleInjection(boolean onOff) {
         boolean ret = false;
         if (ret = kernelIO.writeSysFs(SYSFS_ENABLE_PATH, onOff ? "1" : "0"))
             enabled = onOff;
         return ret;
     }
 
     public int getCurrentTemperature() {
         String ans = kernelIO.readSysFs(SYSFS_CURRENT_TEMP_PATH);
         return (ans != null ? Integer.parseInt(ans) : -1);
     }
 
     public int getMaxTemperature() {
         return maxTemperature;
     }
 
     public int getInjectionProbability() {
         String ans = kernelIO.readSysFs(SYSFS_INJECTION_PROB_PATH);
         return (ans != null ? Integer.parseInt(ans) : 0);
     }
 
     private void setInjectionProbability(int injectProb) {
         assert injectionProb >= 0 : injectionProb;
         if (kernelIO.writeSysFs(SYSFS_INJECTION_PROB_PATH,
                 Integer.toString(injectionProb))) {
             this.injectionProb = injectProb;
         } else {
             Toast.makeText(TemperatureControllerService.this,
                     "Can't change injection probability", Toast.LENGTH_SHORT)
                     .show();
         }
     }
 
     @Override
     public IBinder onBind(Intent arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     private void startTemperatureController(int tempThreshold) {
         Log.d(TAG, "startTemperatureController");
 
         switchIdleInjection(enabled);
         maxTemperature = tempThreshold;
 
         Intent intent = new Intent(TemperatureControllerService.this,
                 AlarmReceiver.class);
         sender = PendingIntent.getBroadcast(TemperatureControllerService.this,
                 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
         AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                 taskInterval, sender);
 
         SharedPreferences preferences = PreferenceManager
                 .getDefaultSharedPreferences(TemperatureControllerService.this);
         SharedPreferences.Editor editor = preferences.edit();
 
         // Make sure services won't be running next time we open the application
         editor.putBoolean("enabled", true);
         editor.commit();
 
     }
 
     private void stopTemperatureController() {
         Log.d(TAG, "stopTemperatureController");
         AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         am.cancel(sender);
 
         SharedPreferences preferences = PreferenceManager
                 .getDefaultSharedPreferences(TemperatureControllerService.this);
         SharedPreferences.Editor editor = preferences.edit();
 
         injectionProb = DEFAULT_INJECTION_PROBABILITY;
         // Make sure service won't be running next time we open the application
         editor.putBoolean("enabled", false);
         editor.commit();
     }
 
     class AlarmReceiver extends BroadcastReceiver {
         private int battTemperature = -1;
 
         @Override
         public void onReceive(Context context, Intent intent) {
             int battTemp = getBatteryTemperature();
             int cpuTemp = getCurrentTemperature();
             updateInterface(battTemp, cpuTemp);
             updateControl(battTemp, cpuTemp);
         }
 
         private int getBatteryTemperature() {
             Intent intent = getApplicationContext().registerReceiver(null,
                     new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 
             // Register application when it's installed
             if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                 battTemperature = intent.getIntExtra(
                         BatteryManager.EXTRA_TEMPERATURE, -1);
             }
 
             return battTemperature;
         }
 
         private void updateInterface(int battTemp, int cpuTemp) {
             Intent intent = new Intent(TemperatureControllerService.this,
                     MainActivity.class);
 
             intent.setAction(UPDATE_UI_ACTION);
             intent.putExtra("battTemp", battTemp);
             intent.putExtra("cpuTemp", cpuTemp);
             sendBroadcast(intent);
         }
 
         /*
          * Control injection probability using exponential-backoff-like
          * mechanism
          */
         private void updateControl(int battTemp, int cpuTemp) {
             int prob = injectionProb;
 
             if (cpuTemp >= maxTemperature) {
                 prob = prob > 0 ? prob * INJECTION_INCREASE_STEP : 1;
             } else {
                 prob = prob > 0 ? prob - INJECTION_DECREASE_STEP
                         : DEFAULT_INJECTION_PROBABILITY;
             }
 
             setInjectionProbability(prob);
         }
     }
 }
