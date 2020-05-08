 package com.example.ucrinstagram;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class PrefsActivity extends PreferenceActivity {
 	final int ACTIVITY_SELECT_IMAGE = 1234;
 	
 	
 @Override
 protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    addPreferencesFromResource(R.xml.prefs);
 }

 @Override
public void onDestroy(){
 	updateUserInfo();
 }
 
 public void updateUserInfo() {
 	TextView usernametv = (TextView) findViewById(R.id.username);
     TextView nicknametv = (TextView) findViewById(R.id.nickname);
     TextView gendertv = (TextView) findViewById(R.id.gender);
     TextView biotv = (TextView) findViewById(R.id.aboutme);
     SharedPreferences sharedPrefs = getSharedPreferences("tempUsername", 0);
     SharedPreferences defSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
     String username = sharedPrefs.getString("username", "username");
     String nickname = defSharedPrefs.getString("nickname", "nickname");
     String gender = defSharedPrefs.getString("listpref", "gender");
     String bio = defSharedPrefs.getString("aboutme", "About Me");
     
     usernametv.setText(username);
     nicknametv.setText(nickname);
     gendertv.setText(gender);
     biotv.setText(bio);
 }
 
 @Override
 public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
         case android.R.id.home:
             // This is called when the Home (Up) button is pressed
             // in the Action Bar.
             Intent parentActivityIntent = new Intent(this, Profile.class);
             parentActivityIntent.addFlags(
                     Intent.FLAG_ACTIVITY_CLEAR_TOP |
                     Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(parentActivityIntent);
             finish();
             return true;
     }
     return super.onOptionsItemSelected(item);
 }
 
 }
