 package fr.ybo.ybotv.android;
 
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.os.StrictMode;
 import fr.ybo.ybotv.android.database.YboTvDatabase;
 
 public class YboTvApplication extends Application {
 
 
     public static final String TAG = "YboTv";
 
     private YboTvDatabase database;
 
     @Override
     public void onCreate() {
         super.onCreate();
         database = new YboTvDatabase(this);
         /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                 .detectAll()
                 .penaltyLog()
                 .penaltyDeath()
                 .build());
 
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                 .detectAll()
                 .penaltyLog()
                 .penaltyDeath()
                 .build());*/
     }
 
     public YboTvDatabase getDatabase() {
         return database;
     }
 
     private String versionInPref = null;
 
     public String getVersionInPref() {
         if (versionInPref == null) {
             SharedPreferences prefs = getSharedPreferences(TAG, 0);
             versionInPref = prefs.getString("ybo-tv.version", "0");
         }
         return versionInPref;
     }
 
     public void updateVersionInPref(String currentVersion) {
         SharedPreferences prefs = getSharedPreferences(TAG, 0);
         SharedPreferences.Editor editor = prefs.edit();
         editor.putString("ybo-tv.version", currentVersion);
         editor.commit();
         versionInPref = currentVersion;
     }
 
 }
