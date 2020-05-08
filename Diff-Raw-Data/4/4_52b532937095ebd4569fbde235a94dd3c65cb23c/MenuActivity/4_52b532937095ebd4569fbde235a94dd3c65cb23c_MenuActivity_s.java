 package com.fluxxy.awesomeness;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class MenuActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

         setContentView(R.layout.activity_menu);

	Log.d(TAG, "Hey");
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu, menu);
         return true;
     }
     
 }
