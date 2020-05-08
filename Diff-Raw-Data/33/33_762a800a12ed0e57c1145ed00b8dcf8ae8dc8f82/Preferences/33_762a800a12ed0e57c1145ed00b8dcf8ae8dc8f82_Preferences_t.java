 package com.example.projectv1;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
import android.view.WindowManager;
 
 // removed this line from AndroidManifest.xml and preferences work     android:permission="PrefsPermission"
 
 
 public class Preferences extends PreferenceActivity {
     
     @Override
     public void onCreate(Bundle savedInstanceState) {       
         super.onCreate(savedInstanceState);       
         
     // must call this method to check password before  displaying activity
         checkPassword();
        
        addPreferencesFromResource(R.xml.preferences);       
       
       // PreferenceActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        //Go back a screen
         Preference button = (Preference)getPreferenceManager().findPreference("backButtons");      
         
         button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                         @Override
                         public boolean onPreferenceClick(Preference arg0) { 
                             //Intent intent = new Intent();
                             //intent.setClass(this,Menu.class);
                             //startActivity(intent);
                         	Log.v("back","button");
                 			Intent openMenu = new Intent("com.project.MENU");
                 			startActivity(openMenu);
 
                             finish();
          	                            return true;
                         }
                     });
 
 //Force refresh iCal
         Preference buttoniCal = (Preference)getPreferenceManager().findPreference("refresh");      
         
         buttoniCal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                         @Override
                         public boolean onPreferenceClick(Preference arg0) {
                         	//Code goes here to force iCal refresh
                 			//Intent openMenu = new Intent("com.project.MENU");
                 			//startActivity(openMenu);
 
                             //finish();
          	                            return true;
                         }
                     });
     
     }
      
     @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		checkPassword();
	}

	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.NONE, 0, 0, "Show current settings");
         return super.onCreateOptionsMenu(menu);
     }
  
     @Override
 public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case 0:
 //                startActivity(new Intent(this, ShowSettingsActivity.class));
                 return true;
         }
         return false;
     }
     
     public void checkPassword()
     { 	
 		Bundle submitted = getIntent().getExtras();
 		long savedPass=0, submittedPass=-1;
 		
 		if (submitted !=null){			
 			submittedPass = submitted.getLong("submitted_pass");								
 		}
 		
 		SharedPreferences prefs = this.getSharedPreferences("com.example.projectv1", Context.MODE_PRIVATE);
 		
 	//	prefs.edit().putLong("savedPass", "default".hashCode()).commit();						
 		savedPass = prefs.getLong("savedPass", "default".hashCode());
 		
 		if (savedPass != submittedPass){			
 			Intent promptPass = new Intent("android.intent.action.PASS");
 			startActivity(promptPass);				
 		} 
     } 
 }
 /*
 public class Preferences extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_preferences);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_preferences, menu);
 		return true;
 	}
 	public void buttonClick(View v)
 	{
 		switch(v.getId())
 		{
 		case R.id.back_from_prefs:
 						
 			finish();
 			
 			break;
 			
 		case R.id.update:
 			
 			// need to implement this
 			//
 			//
 			finish();
 			
 			break;
 			
 		}
 	}
 }
 */
