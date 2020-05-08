 package com.app.settleexpenses;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class SplashScreen extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
               setContentView(R.layout.splash);
               Thread splashThread = new Thread() {
                  @Override
                  public void run() {
                     try {
                       int waited = 0;
                       while (waited < 5000) {
                          sleep(100);
                          waited += 100;
                       }
                     } catch (InterruptedException e) {
                        // do nothing
                     } finally {
                        finish();
                        Intent i = new Intent();
                        i.setClassName("com.app.settleexpenses",
                                       "com.app.settleexpenses.SettleExpenses");
                        startActivity(i);
                     }
                  }
               };
               splashThread.start();
 
     }
 }
