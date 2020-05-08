 package com.gris.ege.other;
 
import android.os.Process;

 import java.lang.Thread.UncaughtExceptionHandler;
 
 public class ExceptionHandler implements UncaughtExceptionHandler
 {
     private static final String TAG="ExceptionHandler";
 
 
 
     public static void init()
     {
         Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
     }
 
     @Override
     public void uncaughtException(Thread thread, Throwable e)
     {
         Log.e(TAG, "AndroidRuntime", e);
        Process.killProcess(Process.myPid());
     }
 }
