 package com.gris.ege.activity;
 
 import com.gris.ege.R;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.app.Activity;
 import android.content.SharedPreferences;
 
 public class StartTestActivity extends Activity
 {
     private static final String TAG="StartTestActivity";
 
     private String mSelectedLesson;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_start_test);
 
         SharedPreferences aSettings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        mSelectedLesson = aSettings.getString(MainActivity.OPTION_SELECTED_LESSON, "");
 
         Log.v(TAG, "Start testing for lesson \""+mSelectedLesson+"\"");
     }
 }
