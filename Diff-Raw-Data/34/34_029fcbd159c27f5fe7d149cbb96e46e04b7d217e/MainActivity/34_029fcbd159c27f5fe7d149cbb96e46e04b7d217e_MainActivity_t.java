 package com.example.mylogger;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.util.Log;
 import android.view.Menu;
 
 public class MainActivity extends Activity {
  private static final String TAG = "LoggerActivity";
 
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Log.d(TAG, "THIS IS MY LOGGING LINE --- IT’S GREAT FOR DEBUGGING");
  }
 
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
 
  @Override
  protected void onPause() {
    super.onPause();
    Log.d(TAG, "I got onPause!!!");
  }
 }
