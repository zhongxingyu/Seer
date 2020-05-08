 package com.gradugation;
 
import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class ChooseCharacterActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_choose_character);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.choose_character, menu);
 		return true;
 	}
 
 }
