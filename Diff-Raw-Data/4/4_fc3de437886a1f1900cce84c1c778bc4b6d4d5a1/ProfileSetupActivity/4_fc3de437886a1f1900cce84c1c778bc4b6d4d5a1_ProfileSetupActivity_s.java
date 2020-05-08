 package com.example.liveabetes;
 
 import android.app.Activity;
 import android.content.Intent;
import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
import android.widget.Button;
import android.widget.TextView;
 
 public class ProfileSetupActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile_setup);
 	}
 	
 	public void skip(View view) {
 		Intent intent = new Intent(this, MainActivity.class);
 	    startActivity(intent);
 	}
 	
 	public void login(View view){
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 }
