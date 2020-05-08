 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 import com.vorsk.crossfitr.models.WorkoutSessionModel;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.text.Editable;
 import android.text.InputType;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class WorkoutProfileActivity extends Activity implements OnClickListener 
 {
 	//initialize variables
 	private WorkoutRow workout;
 	private int ACT_TIMER = 1;
 	private Typeface font;
 	TextView screenName, tvname, tvdesc, tvbestRecord;
 	
 	//Its dynamic! android should use this by default
 	private String TAG = this.getClass().getName();
 	
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		//create model object
 		WorkoutModel model = new WorkoutModel(this);
 		//get the id passed from previous activity (workout lists)
 		long id = getIntent().getLongExtra("ID", -1);
 		//if ID is invalid, go back to home screen
 		if(id < 0)
 		{
 			finish();
 		}
 		//set view
 		setContentView(R.layout.workout_profile);
 		
 		//create a WorkoutRow, to retrieve data from database
 		model.open();
 		workout = model.getByID(id);
 		
 		//TextView objects
 
 		font = Typeface.createFromAsset(this.getAssets(),
 				"fonts/Roboto-Thin.ttf");
 		screenName = (TextView) findViewById(R.id.screenTitle);
 		screenName.setTypeface(font);
 		tvname = (TextView) findViewById(R.id.workout_profile_nameDB);
 		tvname.setTypeface(font);
 		tvbestRecord = (TextView) findViewById(R.id.workout_profile_best_recordDB);
 		tvbestRecord.setTypeface(font);
 		tvdesc = (TextView) findViewById(R.id.workout_profile_descDB);
 		tvdesc.setTypeface(font);
 
 		
 		//set the text of the TextView objects from the data retrieved from the DB
 		Resources res = getResources();
 		tvname.setText(workout.name);
		if (model.getTypeName(workout.workout_type_id).equals("WOD")){
			tvname.setText("WOD");
 			tvname.setTextColor(res.getColor(R.color.wod));
		}
 		else if (model.getTypeName(workout.workout_type_id).equals("Hero"))
 			tvname.setTextColor(res.getColor(R.color.heroes));
 		else if (model.getTypeName(workout.workout_type_id).equals("Girl"))
 			tvname.setTextColor(res.getColor(R.color.girls));
 		else if(model.getTypeName(workout.workout_type_id).equals("Custom"))
 			tvname.setTextColor(res.getColor(R.color.custom));
 		tvdesc.setText(workout.description);
 		//tvrecordType.setText(model.getTypeName(workout.workout_type_id));
         model.close();
         
 		// begin workout button
         View beginButton = findViewById(R.id.button_begin_workout);
         ((TextView) beginButton).setTypeface(font);
         if (workout.description.indexOf("Rest Day") == -1){
         	//It is not a rest day
     		tvbestRecord.setText("Personal Record: "+StopwatchActivity.formatElapsedTime(Long.parseLong(String.valueOf(workout.record))));
         	beginButton.setOnClickListener(this);
         }else{
         	//it is a rest day
         	beginButton.setVisibility(View.GONE);
         	tvbestRecord.setVisibility(View.GONE);
         }
 
 	}
 	
 	public void onClick(View v) 
 	{
 		switch (v.getId()) 
 		{
 		    // if user presses begin button, user will now go into the timer page.
 			case R.id.button_begin_workout:
 				if(workout.record_type_id == WorkoutModel.SCORE_WEIGHT){
 					weightPopup();
 				}
 				
 				else if(workout.record_type_id == WorkoutModel.SCORE_REPS){
 					repsPopup();
 				}
 				
 				else{	
 					Intent i = new Intent(this, TimeTabWidget.class);
 					i.putExtra("workout_id", workout._id);
 					//i.putExtra("workout_score",
 							//Integer.parseInt(etextra.getText().toString()));
 					startActivityForResult(i, ACT_TIMER);
 				} 
 				break;
 		}
 	}
 	
 	
 	
 	private void repsPopup() {
 		// TODO Auto-generated method stub
 		final Intent i = new Intent(this, TimeTabWidget.class);
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		
 		alert.setTitle("Enter Number of Reps:");
 
 		// Set an EditText view to get user input 
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_NUMBER);
 		alert.setView(input);
 
 		alert.setPositiveButton("Begin", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 		  Editable value = input.getText();
 			i.putExtra("workout_id", workout._id);
 			i.putExtra("workout_score",
 					Integer.parseInt(value.toString()));
 			startActivityForResult(i, ACT_TIMER);
 		  }
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled. Do nothing
 		  }
 		});
 
 		alert.show();
 	}
 
 	private void weightPopup() {
 		// TODO Auto-generated method stub
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		final Intent i = new Intent(this, TimeTabWidget.class);
 		
 		alert.setTitle("Input Weight For Workout (lbs):");
 
 		// Set an EditText view to get user input 
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_NUMBER);
 		alert.setView(input);
 
 		alert.setPositiveButton("Begin", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 		  Editable value = input.getText();
 			i.putExtra("workout_id", workout._id);
 			i.putExtra("workout_score",
 					Integer.parseInt(value.toString()));
 			startActivityForResult(i, ACT_TIMER);
 		  
 		  }
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 
 		alert.show();
 		
 	}
 
 	protected void onActivityResult(int request, int result, Intent data)
 	{
 		if (request == ACT_TIMER) {
 			if (result != RESULT_CANCELED) {
 				// Session was completed
 				long score;
 				WorkoutSessionModel model = new WorkoutSessionModel(this);
 				model.open();
 				
 				// Get the score returned
 				if (workout.record_type_id == WorkoutModel.SCORE_TIME) {
 					score = data.getLongExtra("time", WorkoutModel.NOT_SCORED);
 				} else if (workout.record_type_id == WorkoutModel.SCORE_REPS) {
 					score = data.getIntExtra("score", WorkoutModel.NOT_SCORED);
 				} else if (workout.record_type_id == WorkoutModel.SCORE_WEIGHT) {
 					score = data.getIntExtra("score", WorkoutModel.NOT_SCORED);
 				} else {
 					score = WorkoutModel.NOT_SCORED;
 				}
 				
 				//Test debugging!
 				//Log.d(TAG,"workoutID: "+workout._id+" score: "+score+" recotdTypeID: "+workout.record_type_id);
 				
 				// Save as a new session
 				long id = model.insert(workout._id, score,
 						workout.record_type_id);
 				model.close();
 				
 				// Show the results page
 				Intent res = new Intent(this, ResultsActivity.class);
 				res.putExtra("session_id", id);
 				startActivity(res);
 			}
 		}
 	}
 }
