 package com.davelabs.wakemehome;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.widget.NumberPicker;
 
 public class SettingsActivity extends Activity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings);
        NumberPicker np = (NumberPicker) findViewById(R.id.settingsRadiusWheel);
        np.setMaxValue(10);
        np.setMinValue(1);
     }
 }
