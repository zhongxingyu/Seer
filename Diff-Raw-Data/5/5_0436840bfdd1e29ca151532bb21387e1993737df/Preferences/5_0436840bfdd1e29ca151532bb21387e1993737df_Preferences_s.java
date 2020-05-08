 package edu.gatech.cs4261.LAWN;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import edu.gatech.cs4261.LAWN.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 
 public class Preferences extends CustomActivity {
 	/* constants*/
 	private static final String TAG = "Preferences";
 	
 	/* buttons*/
 	CheckBox WifiCheck;
 	CheckBox BTCheck;
 	
 	/*called when the class is first made*/
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d(TAG, "before set prefs layout");
         setContentView(R.layout.prefs);
         
         /* enable options*/
 		WifiCheck = (CheckBox)findViewById(R.id.WifiCheck);
 		BTCheck = (CheckBox)findViewById(R.id.BTCheck);
 		
 		//check booleans in preferences to see what state should be on startup
 		boolean wifi, bt;
 		wifi = getPreferences().getBoolean("WifiState", false);
 		bt = getPreferences().getBoolean("BTState", false);
 		
 		Log.d(TAG, "after preferences check");
 		
 		//change the checkboxes to match the saved state
 		WifiCheck.setChecked(wifi);
 		BTCheck.setChecked(bt);
 		
 		//set up save button listener
 		Button btnSave = (Button)findViewById(R.id.SavePrefs);
 		btnSave.setOnClickListener(SaveListener);
 		
 		Log.d(TAG, "end of onCreate");
 	}
 	
 	/* button listeners/actions */
 	/** what to do when an option is clicked */
     private OnClickListener SaveListener = new OnClickListener() {
 		public void onClick(View v) {
 			Log.d(TAG, "save preferences clicked");
 			
 			//set up the preferences editor
 			SharedPreferences.Editor editor = getPreferences().edit();
 			
 			//check if wifi was checked
 	        if (WifiCheck.isChecked()) {
 	            editor.putBoolean("WifiState", true);
 	        } else {
 	            editor.putBoolean("WifiState", false);
 	        }
 	        
 	        //check if bluetooth was checked
	        if (WifiCheck.isChecked()) {
 	            editor.putBoolean("BTState", true);
 	        } else {
 	            editor.putBoolean("BTState", false);
 	        }
 	        
 	        //commit the changes to preferences
 	        editor.commit();
 	        
 	        Log.d(TAG, "BTState: " + getPreferences().getBoolean("BTState", false));
 	        Log.d(TAG, "WifiState: " + getPreferences().getBoolean("WifiState", false));
 		}
     };
 }
