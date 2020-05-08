 /**
 * Copyright (C) 2013 dalang
  */
 
 package tk.dalang.gaminder;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import tk.dalang.gaminder.analytics.MyExceptionParser;
 import android.app.Application;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.ExceptionReporter;
 import com.google.analytics.tracking.android.GAServiceManager;
 
 /**
  * GameReminder
  * <p>
  * app
  * </p>
  * 
  * @author <a href="http://weibo.com/iDalang">dalang</a>
  * @version Apr, 11 2013
  */
 public class MyApplication extends Application {
 
     @Override
     public void onCreate() {
         super.onCreate();
         EasyTracker.getInstance().setContext(this);
         EasyTracker.getTracker().setExceptionParser(new MyExceptionParser(getApplicationContext()));
         UncaughtExceptionHandler handler = new ExceptionReporter(EasyTracker.getTracker(),
                 GAServiceManager.getInstance(), Thread.getDefaultUncaughtExceptionHandler());
         Thread.setDefaultUncaughtExceptionHandler(handler);
         // CrashHandler.getInstance().init(this);
     }
 
 }
