 package com.c1.charityworkout;
 
 import java.util.Locale;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	ImageButton running, cycling, history;
 	static int choice;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		initialize();
 		int resultCode = GooglePlayServicesUtil
 				.isGooglePlayServicesAvailable(this);
 		if (resultCode != ConnectionResult.SUCCESS) {
 			Toast.makeText(this, "INVALID PLAY SERVICES", 2000).show();
 		} else {
 			Toast.makeText(this, "VALID PLAY SERVICES", 2000).show();
 		}
 
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 	
 	private void initialize() {
 		// TODO Auto-generated method stub
 		running = (ImageButton) findViewById(R.id.imageButton);
 		cycling = (ImageButton) findViewById(R.id.imageButton2);
 
 		running.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent next = new Intent(MainActivity.this, Screen2.class);
 				startActivity(next);
 				choice = (R.drawable.runningbanner);
 			}
 		});
 		cycling.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent next = new Intent(MainActivity.this, Screen2.class);
 				startActivity(next);
 				choice = (R.drawable.cyclingbanner);
 			}
 		});
 		history.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent history = new Intent(MainActivity.this, WorkoutHistory.class);
 				startActivity(history);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			Intent p = new Intent(MainActivity.this, Prefs.class);
 			startActivity(p);
 			break;
 		case R.id.exit_app:
 			finish();
 			break;
 
 		}
 
 		return false;
 	}
 
 }
