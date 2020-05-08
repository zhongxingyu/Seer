 package ca.skule.froshapplication;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		getActionBar().setDisplayShowTitleEnabled(false);
 		return true;
 	}
	
 	public void onMenuClick (MenuItem Item){
 		String id = Item.getTitle().toString();
 		if (id.equals("schedule")){
 			// Point to Schedule
			}
 		else if (id.equals("map")){
 			// Point to Map
 		}
		}
 	}
 
