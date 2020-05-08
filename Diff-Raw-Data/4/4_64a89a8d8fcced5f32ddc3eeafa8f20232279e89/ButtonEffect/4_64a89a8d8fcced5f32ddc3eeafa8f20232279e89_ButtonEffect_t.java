 package com.source.tripwithme.main_ui;
 
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Button;
 
 public class ButtonEffect {
 
     private static final int TIMES_SWITCH = 6;
     private static final long SLEEP_BETWEEN_MILLIS = 300;
 
     public static void glowOnUIThread(final Button messageButton) {
         final Drawable backgroundDrawable = messageButton.getBackground();
         new AsyncTask<Void, Boolean, Void>() {
 
            @SuppressWarnings("deprecation")
             @Override
             protected void onProgressUpdate(Boolean... values) {
                 if (values[0]) {
                     messageButton.setBackgroundColor(Color.YELLOW);
                 } else {
                    messageButton.setBackgroundDrawable(backgroundDrawable);
                 }
             }
 
             @Override
             protected Void doInBackground(Void... params) {
                 try {
 
                     for (int i = 0; i < TIMES_SWITCH; i++) {
                         publishProgress(true);
                         Thread.sleep(SLEEP_BETWEEN_MILLIS);
                         publishProgress(false);
                         Thread.sleep(SLEEP_BETWEEN_MILLIS);
                     }
                 } catch (InterruptedException e) {
                     Log.e("ButtonEffect", "interuped", e);
                 }
                 return null;
             }
         }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
     }
 }
