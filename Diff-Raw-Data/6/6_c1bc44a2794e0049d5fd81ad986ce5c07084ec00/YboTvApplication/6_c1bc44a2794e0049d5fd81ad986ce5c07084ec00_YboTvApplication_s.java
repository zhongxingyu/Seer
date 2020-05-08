 package fr.ybo.ybotv.android;
 
 
 import android.app.Application;
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
                 .build());*/
     }
 
     public YboTvDatabase getDatabase() {
         return database;
     }
 
 }
