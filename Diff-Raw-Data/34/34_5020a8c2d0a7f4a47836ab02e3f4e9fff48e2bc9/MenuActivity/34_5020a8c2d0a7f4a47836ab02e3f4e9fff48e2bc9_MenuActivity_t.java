 package com.clusterflux.concentric;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.content.Intent;
 import android.util.Log;
 
 public class MenuActivity extends Activity {
 
 	public String message; 
 	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
 	public int reqCode;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.menu);
 		Log.d("LOGCAT", "MenuActivity Started");
 		
     }
 	
 	/**Called when the user clicks the World Selection button*/
 	public void worldSelection(View view) {
 	
 		//Start WorldSelectionActivity and request world_name
 		Log.d("LOGCAT", "Loading WorldSelectionActivity");
 		reqCode = 1;
 		Intent intent = new Intent(this, WorldSelectionActivity.class);
 		startActivityForResult(intent, reqCode);
 		
 	}
 	
 	/**Called when the user finishes WorldSelectionActivity*/
 	@Override
 	public void onActivityResult(int reqCode, int resCode, Intent data) {
 	
 		//get world_name from WorldSelectionActivity
 		Log.d("LOGCAT", "Getting world_name from WorldSelectionActivity");
 		if (reqCode == 1) {
 			if (resCode == Activity.RESULT_OK) {
 				message = data.getStringExtra(EXTRA_MESSAGE);
				//Start GameActivity and pass it world_name
				Log.d("LOGCAT", "Starting GameActivity");		
				Intent intent = new Intent(this, GameActivity.class);
				intent.putExtra(EXTRA_MESSAGE, message);
				startActivity(intent);
 			}
 		}
 		
 	}
 	
 	/**Called when the user clicks the Generate World button */
 	public void generateWorld(View view) {
 		
 		//Start WorldGenerationActivity
 		Log.d("LOGCAT", "Starting WorldGenerationActivity");
 		Intent intent = new Intent(this, WorldGenerationActivity.class);
 		startActivity(intent);
 		
 	}
 
 }
