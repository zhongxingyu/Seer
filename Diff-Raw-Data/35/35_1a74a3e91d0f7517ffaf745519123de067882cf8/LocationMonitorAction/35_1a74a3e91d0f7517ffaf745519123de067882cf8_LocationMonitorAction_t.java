 package ru.spb.cupchinolabs.androidlocator;
 
 import android.app.Activity;
import android.content.Context;
 import android.content.Intent;
import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ToggleButton;
 
 public class LocationMonitorAction extends Activity {
 
     private static final String TAG = "LocationMonitorAction";
    private static final String IS_ON = "IS_ON";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.location_monitor_control);

        ((ToggleButton) findViewById(R.id.togglebutton))
                .setChecked(getPreferences(Context.MODE_PRIVATE).getBoolean(IS_ON, false));

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

        getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putBoolean(IS_ON, ((ToggleButton) findViewById(R.id.togglebutton)).isChecked())
                .commit();

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
