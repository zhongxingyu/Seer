 package com.gris.ege.activity;
 
 import com.gris.ege.R;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.app.Activity;
 import android.content.SharedPreferences;
 
 public class ViewResultsActivity extends Activity
 {
     private static final String TAG="ViewResultsActivity";
 
     private String mSelectedLesson;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_view_results);
 
         SharedPreferences aSettings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        mSelectedLesson = aSettings.getString(MainActivity.OPTION_SELECTED_LESSON, "");
 
         Log.v(TAG, "View results for lesson \""+mSelectedLesson+"\"");
     }
 }
