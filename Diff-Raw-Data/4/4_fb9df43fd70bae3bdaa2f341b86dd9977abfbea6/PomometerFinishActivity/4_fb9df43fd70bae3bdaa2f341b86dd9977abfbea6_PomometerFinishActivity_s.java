 package com.pomometer.PomometerApp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.NumberPicker;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PomometerFinishActivity extends Activity {
 		
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_pomometer_finish);
 		
 		Bundle extras = getIntent().getExtras();
 		
 		final String sent_goal = extras.getString("goal");
 		String notes = "";
 		final Long sent_duration = extras.getLong("elapsed_duration"); //in ms
 		double decimal_duration = sent_duration/1000; //converts to seconds
 		decimal_duration /= 60; //converts to minutes
 		final int calculated_duration = (int)Math.ceil(decimal_duration); //round up to nearest minute
 		final String started_at = extras.getString("started_at");
 		final String ended_at = extras.getString("ended_at");
 		final int task_id = extras.getInt("task_id");
 		
 		//set title to Complete: goal.  No strings.xml entry as this is dynamic
 		((TextView) findViewById(R.id.finish_title)).setText("Complete: " + sent_goal);
 		
		Button confirm_button = (Button) findViewById(R.id.confirm_button);
		confirm_button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				//json to commit to webserver
 				
 				/*
 				 * sent_goal
 				 * (EditText) findViewById(R.id.notes_edit_text)
 				 * calculated_duation
 				 * started_at
 				 * ended_at
 				 * task_id
 				 */
 				
         		Toast.makeText(getBaseContext(), ((Integer)calculated_duration).toString(), Toast.LENGTH_SHORT).show();
 
 				//Intent i = new Intent(getApplicationContext(), PomometerTimerActivity.class);        		
         		//i.putExtra("task_id", task_id);        		
         		//startActivity(i);
 			}			
 		});
 	}
 }
