 package com.hlidskialf.android.alarmclock;
 
 
 import android.os.Bundle;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.BroadcastReceiver;
 import android.view.View;
 import android.view.Window;
 import android.widget.LinearLayout;
 import android.widget.TextSwitcher;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.os.PowerManager;
 import android.os.BatteryManager;
 import android.os.Handler;
 import java.lang.Runnable;
 
 import android.view.animation.AnimationUtils;
 
 
 public class BigClock extends Activity  implements View.OnClickListener, ViewSwitcher.ViewFactory
 {
   private TextSwitcher mSwitcher;
   private Calendar mCal;
   private Handler mHandler;
   private Runnable mCallback;
   private LayoutInflater mInflater;
 
   private boolean mDoWakeLock;
   private PowerManager mPower;
   private BatteryManager mBattery;
   private BroadcastReceiver mBatteryReceiver;
   private PowerManager.WakeLock mWakeLock;
 
 
   @Override
   protected void onCreate(Bundle icicle) {
     super.onCreate(icicle);
     getWindow().requestFeature(Window.FEATURE_NO_TITLE);
 
 
     mInflater = getLayoutInflater();
 
     setContentView(R.layout.bigclock);
 
     mSwitcher = (TextSwitcher)findViewById(R.id.bigclock_time);
     mSwitcher.setFactory(this);
     mSwitcher.setInAnimation( AnimationUtils.loadAnimation(this, android.R.anim.fade_in) );
     mSwitcher.setOutAnimation( AnimationUtils.loadAnimation(this, android.R.anim.fade_out) );
     //mSwitcher.setInAnimation( AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left) );
     //mSwitcher.setOutAnimation( AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right) );
     mSwitcher.setOnClickListener(this);
 
 
     mDoWakeLock = getSharedPreferences(AlarmClock.PREFERENCES, 0).getBoolean("bigclock_wake_lock", false);
     if (mDoWakeLock) {
         mPower = (PowerManager)getSystemService(Context.POWER_SERVICE);
         mWakeLock = mPower.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, BigClock.class.getName());
     }
 
     mCal = Calendar.getInstance();
     mHandler = new Handler();
     mBatteryReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
            int plugged = intent.getIntExtra("plugged", 0);
            if (mWakeLock == null) return;
            if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged ==  BatteryManager.BATTERY_PLUGGED_USB) 
            {
                if (!mWakeLock.isHeld()) {
                   mWakeLock.acquire();
                   android.util.Log.v("BigClock","wakelock/acquire");
                }
            } else {
               if (mWakeLock.isHeld()) {
                  mWakeLock.release();
                  android.util.Log.v("BigClock","wakelock/release-unplug");
               }
            }
         }
     }; 
   }
   @Override 
   protected void onDestroy() {
     if (mWakeLock != null && mWakeLock.isHeld()) {
         mWakeLock.release();
         android.util.Log.v("BigClock","wakelock/release-destroy");
     }
     super.onDestroy();
   }
   @Override
   public void onResume() {
     super.onResume();
 
     if (mDoWakeLock && mBatteryReceiver != null) {
       registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
     }
 
     mCallback = new Runnable() { 
       public void run() {
         BigClock.this.updateClock();
         mHandler.postDelayed(mCallback, 1000);
       }
     };
     mHandler.postDelayed(mCallback, 0);
   }
   @Override
   public void onPause() {
     if (mWakeLock != null && mWakeLock.isHeld()) {
         mWakeLock.release();
         android.util.Log.v("BigClock","wakelock/release-pause");
     }
     if (mDoWakeLock && mBatteryReceiver != null) {
         unregisterReceiver(mBatteryReceiver);
     }
     if (mHandler != null && mCallback != null) {
       mHandler.removeCallbacks(mCallback);
       mCallback = null;
     }
     super.onPause();
   }
   public void updateClock()
   {
     mCal.setTime(new Date());
     String time = Alarms.formatTimeWithSeconds(this, mCal);
     mSwitcher.setText(time == null ? "null" : time);
   }
 
   public View makeView() {
     return mInflater.inflate(R.layout.bigclock_text, null);
   }
 
   public void onClick(View v) { finish(); }
   public boolean onKeyDown(int keyCode, android.view.KeyEvent event) { finish(); return true; }
 
 }
