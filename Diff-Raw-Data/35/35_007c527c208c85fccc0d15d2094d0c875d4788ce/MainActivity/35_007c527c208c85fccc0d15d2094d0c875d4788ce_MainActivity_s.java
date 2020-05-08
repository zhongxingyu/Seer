 package com.c1.charityworkout;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener{
 
 	private ImageButton running, cycling, history;
 	private Bundle workoutChoice;
 	private Intent next;
 
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
 		running.setOnClickListener(this);
 		cycling = (ImageButton) findViewById(R.id.imageButton2);
 		cycling.setOnClickListener(this);
 		history = (ImageButton) findViewById(R.id.imageButton3);
 		history.setOnClickListener(this);
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
 		case R.id.about_us:
 			Intent aboutUs = new Intent(MainActivity.this, AboutUs.class);
 			startActivity (aboutUs);
 			break;
 		case R.id.exit_app:
 			finish();
 			break;
 
 		}
 
 		return false;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch (arg0.getId()){
 		case R.id.imageButton:
 			workoutChoice = new Bundle();
 			workoutChoice.putString("choice", "Running");
 			next = new Intent(MainActivity.this, WorkoutPage.class);
 			next.putExtras(workoutChoice);
 			startActivity(next);		
 			break;
 		case R.id.imageButton2:
 			workoutChoice = new Bundle();
 			workoutChoice.putString("choice", "Cycling");
 			next = new Intent(MainActivity.this, WorkoutPage.class);
 			next.putExtras(workoutChoice);
 			startActivity(next);
 			break;
 		case R.id.imageButton3:
 			Intent historyIntent = new Intent(MainActivity.this, WorkoutHistory.class);
 			startActivity(historyIntent);
 			break;
 		}
 	}
 
 }
