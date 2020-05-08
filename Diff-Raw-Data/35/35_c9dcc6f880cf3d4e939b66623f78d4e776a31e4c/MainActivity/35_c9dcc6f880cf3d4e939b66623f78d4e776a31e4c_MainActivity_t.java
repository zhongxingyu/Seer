 package com.eit.minimap;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * Activity defining the main menu. Just a few buttons and such.
  */
 public class MainActivity extends Activity implements View.OnClickListener {
     private EditText groupName;
     private EditText yourName;
     private SharedPreferences preferences;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         preferences = PreferenceManager.getDefaultSharedPreferences(this);
         setContentView(R.layout.main_menu);
         Button joinButton = (Button) findViewById(R.id.join_group);
         joinButton.setOnClickListener(this);
         Button settingsButton = (Button) findViewById(R.id.settings_button);
         settingsButton.setOnClickListener(this);
 
         groupName = (EditText) findViewById(R.id.group_name);
         yourName = (EditText) findViewById(R.id.your_name);
         // Set fields to old values, if they exist.
         groupName.setText(preferences.getString("groupName",""));
         yourName.setText(preferences.getString("yourName",""));
     }
 
     /**
      * Logic for what happens when you press the "Join group" button
      * @param v Join group button view.
      */
     @Override
     public void onClick(View v) {

         String groupNameStr = groupName.getText().toString();
         String yourNameStr = yourName.getText().toString();
         // Error if some fields are empty
         if(groupNameStr.isEmpty() || yourNameStr.isEmpty()) {
             Toast.makeText(this,R.string.error_fields_empty,Toast.LENGTH_LONG).show();
             return;
         }
         // Store these strings for later use in the app-wide properties.
         preferences.edit()
                 .putString("groupName",groupNameStr)
                 .putString("yourName",yourNameStr)
                 .commit();
        // Starts settings menu
    	if(v.getId() == R.id.settings_button){
    		Intent SettingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(SettingsIntent);
            return;
    	}
         // Go to next activity!
         Intent myIntent = new Intent(this, MapActivity.class);
         startActivity(myIntent);
     }
 }
