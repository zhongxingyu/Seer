 package com.vorsk.crossfitr;
 
import org.example.sudoku.R;

 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 
 public class CrossFitrActivity extends Activity implements OnClickListener{
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.homescreen);
         
         // workout button
         View workoutButton = findViewById(R.id.main_button_workout);
         workoutButton.setOnClickListener(this);
         // calendar button
         View calendarButton = findViewById(R.id.main_button_calendar);
         calendarButton.setOnClickListener(this);
         // profile button
         View profileButton = findViewById(R.id.main_button_profile);
         profileButton.setOnClickListener(this);
     }
 
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()) {
 		case R.id.main_button_workout:
 
 			break;
 			
 		case R.id.main_button_calendar:
 			
 			break;
 			
 		case R.id.main_button_profile:
 			;
 			break;
 	}
 }
