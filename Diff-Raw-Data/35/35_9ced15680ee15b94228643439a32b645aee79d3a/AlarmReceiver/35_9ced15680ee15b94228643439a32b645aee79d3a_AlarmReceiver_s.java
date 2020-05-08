 package com.tzwm.deadalarm;
 
 import android.app.Activity;
 import android.app.Application;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.Toast;
 
 /**
  * Created by tzwm on 10/4/13.
  */
 public class AlarmReceiver extends BroadcastReceiver {
     static final int WAKEUP_NORMAL = 0;
     static final int SOUND_REPRODUCE = 1;
     static final int PUNCH_THE_BALL = 2;
 
     static int wakeupWay = 0;
 
     private Context mContext;
 
     @Override
     public void onReceive(Context context, Intent intent) {
         mContext = context;
 
         Toast.makeText(context, "Time's Up!", Toast.LENGTH_LONG).show();
 
         switch (wakeupWay) {
             case WAKEUP_NORMAL:
                 wakeUpNormal();
                 break;
             case SOUND_REPRODUCE:
                 soundReproduce();
                 break;
             case PUNCH_THE_BALL:
                 punchTheBall();
                 break;
             default:
                 break;
         }
 
     }
 
     private void wakeUpNormal() {
         MediaController mediaController = new MediaController(mContext);
         mediaController.playNotification();
     }
 
     private void soundReproduce() {
         MediaController mediaController = new MediaController(mContext);
         mediaController.startPlaying();
     }
 
     private void punchTheBall() {
         Intent intent = new Intent(mContext, BeeActivity.class);
         mContext.startActivity(intent);
     }
 }
