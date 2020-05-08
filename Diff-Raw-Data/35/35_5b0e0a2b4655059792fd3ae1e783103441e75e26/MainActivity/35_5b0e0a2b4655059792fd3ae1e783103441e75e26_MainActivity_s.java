 package com.allen.mapple;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 
 public class MainActivity extends Activity {
 
     public final static String BASE_URL = "http://192.168.100.103:3000";
 
     private static final String TAG = "com.allen.mapple.MainActivity";
 
     private User user;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         SharedPreferences preferences = getPreferences(MODE_PRIVATE);
         if(!preferences.contains("auth_token") || !preferences.contains("email")) {
             Intent intent = new Intent(this, SignupActivity.class);
            startActivityForResult(intent, 0);
         } else {
             user = new User(preferences.getString("email", ""), preferences.getString("auth_token", ""));
             Log.d(TAG, "loaded user: " + user.toString());
             setupActivity();
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED) {
            finish();
         } else {
            user = data.getParcelableExtra("user");
            setupActivity();
         }
     }
 
     protected void setupActivity() {
         Log.d(TAG, "setting up map");
         Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if(user != null) {
             SharedPreferences preferences = getPreferences(MODE_PRIVATE);
             preferences.edit()
                        .putString("email", user.email)
                        .putString("auth_token", user.authToken)
                        .commit();
             Log.d(TAG, "saved user: " + user.toString());
         }
     }
 }
