 package com.csci5115.group2.planmymeal;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class CookActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_cook);
 		
 		Intent intent = getIntent();
 		String mealName = intent.getStringExtra(HomeActivity.EXTRA_MEAL);
 		
		/*
 		TextView mealNameTextView = (TextView) findViewById(R.id.cook_meal_mealName);
 		mealNameTextView.setText(mealName);
		*/
 		
         Button settingsButton = (Button) findViewById(R.id.home_buttonSettings);
         settingsButton.setOnClickListener((OnClickListener) this);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.community_cookbook, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 	        case R.id.action_settings:
 	    		Intent intent = new Intent(this, SettingsActivity.class);
 	    		startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 
 }
