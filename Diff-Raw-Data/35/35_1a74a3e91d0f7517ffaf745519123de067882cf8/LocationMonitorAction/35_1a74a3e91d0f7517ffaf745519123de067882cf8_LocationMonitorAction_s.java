 package ru.spb.cupchinolabs.androidlocator;
 
 import android.app.Activity;
 import android.content.Intent;
import android.os.*;
 import android.util.Log;
 import android.view.View;
 import android.widget.ToggleButton;
 
 public class LocationMonitorAction extends Activity {
 
     private static final String TAG = "LocationMonitorAction";

    //TODO FIX BUG - handle navigation on back button, app shouldn't be destroyed
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.location_monitor_control);
         Log.d(TAG, "onCreate");
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         Log.d(TAG, "onStop");
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         Log.d(TAG, "onDestroy");
     }
 
     public void onToggleClicked(View view) {
         boolean on = ((ToggleButton) view).isChecked();
         if (on) {
             startService(new Intent(LocationMonitorService.class.getName()));
         } else {
             stopService(new Intent(LocationMonitorService.class.getName()));
         }
         Log.d(TAG, "onToggleClicked:" + on);
     }
 
 }
