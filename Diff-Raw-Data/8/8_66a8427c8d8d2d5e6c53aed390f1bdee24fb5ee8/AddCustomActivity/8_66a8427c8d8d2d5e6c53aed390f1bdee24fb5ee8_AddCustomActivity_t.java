 package com.vorsk.crossfitr;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class AddCustomActivity extends Activity implements OnClickListener 
 {
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		//button to save
		View saveButton = findViewById(R.id.button_workout_form_save);
 		saveButton.setOnClickListener(this);
 		
 		//button to save and start workout
		View saveAndStartButton = findViewById(R.id.button_workout_form_start);
 		saveAndStartButton.setOnClickListener(this);
 		setContentView(R.layout.workout_form);
 	}
 
	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 	}
 }
