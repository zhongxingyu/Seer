 package com.eit.minimap;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 
 public class SettingsActivity extends Activity implements View.OnClickListener{
 
     private SharedPreferences preferences;
 	private EditText yourName;
 	private EditText groupName;
	private EditText iconName;
 	private EditText serverAdr;
 	private CheckBox viewStaus;
 	private CheckBox viewMessges;
 	private CheckBox viewUsers;
 
     
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings_menu);
         
         Button ok = (Button) findViewById(R.id.ok_button);
         ok.setOnClickListener(this);
         preferences = PreferenceManager.getDefaultSharedPreferences(this);
         
         // Get Fields
         groupName = (EditText) findViewById(R.id.group_name);
         yourName = (EditText) findViewById(R.id.your_name);
        iconName = (EditText) findViewById(R.id.icon_name);
         serverAdr = (EditText) findViewById(R.id.server_adr);
         viewStaus = (CheckBox) findViewById(R.id.view_status);
         viewMessges = (CheckBox) findViewById(R.id.view_messges);
         viewUsers = (CheckBox) findViewById(R.id.view_users);
         
         // Setting text from preferences to fields
         yourName.setText(preferences.getString("groupName",""));
         groupName.setText(preferences.getString("yourName",""));
        iconName.setText(preferences.getString("iconName","Medic"));
         viewStaus.setChecked(preferences.getBoolean("viewStaus",true));
         viewMessges.setChecked(preferences.getBoolean("viewMessges",true));
         viewUsers.setChecked(preferences.getBoolean("viewMessges",false));
 
     }
 
 	@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.ok_button){
 			// Update preferences from updated fields
 	        preferences.edit()
             .putString("groupName",this.yourName.getText().toString())
             .putString("yourName",this.groupName.getText().toString())
            .putString("iconName",this.iconName.getText().toString())
             .putString("serverAdr",this.serverAdr.getText().toString())
             .putBoolean("viewStaus", this.viewStaus.isChecked())
             .putBoolean("viewMessges", this.viewStaus.isChecked())
             .putBoolean("viewUsers", this.viewStaus.isChecked())
             .commit();
 	        // Go to next activity!
	        Intent myIntent = new Intent(this, MapActivity.class);
 	        startActivity(myIntent);
 		}
 	}
 }
