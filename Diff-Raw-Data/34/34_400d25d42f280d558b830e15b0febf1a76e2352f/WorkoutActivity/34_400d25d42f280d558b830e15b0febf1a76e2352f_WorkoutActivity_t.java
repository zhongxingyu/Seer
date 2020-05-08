 package ultimateFrisbee.Workout;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Random;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WorkoutActivity extends Activity {
 	
 	
 	
 	private static final int MIN_TO_SEC = 60;
 	private static final int SEC_TO_MILLS = 1000;
 	private static final int MIN_TO_MILLS = MIN_TO_SEC * SEC_TO_MILLS;
 	private Bundle extras;
 	private WorkoutDBOpener workoutDBOpenerHelper;
 	private SQLiteDatabase workoutDB;
 	private Collection<Workout> workouts;
 	private Button cancelB;
 	private TextView workoutField,timeField;
 	private Random randGen;
 	private WorkoutCountDownTimer workoutCountDown;
 	private boolean pairedWorkout = false;
 	private Workout tempWorkout;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.workout);
 		//get extras
 		extras = getIntent().getExtras();
 		long workoutLength = (long) (extras.getFloat(RaeReplacementActivity.TOTAL_LENGTH)*MIN_TO_MILLS);
 		long segmentLength= (long) (extras.getFloat(RaeReplacementActivity.SEGMENT_LENGTH)*MIN_TO_MILLS);
 		if(extras != null){
			Toast.makeText(this, "Segment length is: " + segmentLength/MIN_TO_MILLS +" Total length is: " + workoutLength/MIN_TO_MILLS, Toast.LENGTH_LONG).show();
 		}
 		//setup local fields and elements
 		tempWorkout = null;
 		randGen = new Random();
 		long timeToCountByInSec= 1;
 		workoutField = (TextView) findViewById(R.id.workoutName);
 		timeField = (TextView) findViewById(R.id.timeRemaning);
 		CountDownTimer timeLeftInWorkout = new CountDownTimer(workoutLength, timeToCountByInSec * SEC_TO_MILLS) {
 
 		     public void onTick(long millisUntilFinished) {
 		    	 int hours   = (int) ((millisUntilFinished / 1000) / 3600);
 		    	 int seconds = (int) ((millisUntilFinished / 1000) % 60);
 		    	 int minutes = (int) (((millisUntilFinished-(1000*3600*hours)) / 1000) / 60);
 		    	 timeField.setText(String.format("%d:%02d:%02d", hours, minutes,seconds));
 		    	 //LONGTERMTODO this can be done more efficently with someithng like the following i think
 		    	 //timer.setText(String.format("%T",millisUntilFinished));
 		    	 
 		     }
 
 		     public void onFinish() {
 		    	 timeField.setText("workout over");
 		     }
 		  };
 		workoutCountDown = new WorkoutCountDownTimer(workoutLength,segmentLength,this);
 		workouts = new ArrayList<Workout>();
 		cancelB = (Button) findViewById(R.id.cancel);
 		cancelB.setOnClickListener(new OnClickListener(){
 				  public void onClick(View v){
 					  WorkoutActivity.this.finish();
 				  }
 			  });
 
         //get db
 		workoutDBOpenerHelper = new WorkoutDBOpener(this);
 		workoutDB = workoutDBOpenerHelper.getReadableDatabase();
 		Cursor workoutCursor = workoutDB.query(WorkoutDBOpener.WORKOUT_TN, new String[] {"workout" , "paired"},null, null, null, null, null);
 		while(!workoutCursor.isLast()){
 			//XXX this should be done programaticaly not using the constant "0"
 			//the 0 should be rosterCursor.getColumnIndex("roster")
 			workoutCursor.moveToNext();
 			Workout workoutToAdd = new Workout(workoutCursor.getString(workoutCursor.getColumnIndex("workout")), workoutCursor.getInt(workoutCursor.getColumnIndex("paired")));
 			workouts.add(workoutToAdd);
 		}
 		Toast.makeText(this, workouts.toString(), Toast.LENGTH_LONG).show();
 		workoutCountDown.start();
 		timeLeftInWorkout.start();
 	}
 
 	public void setWorkoutText(String workout, long timeLeft) {
 		workoutField.setText(workout);
 	}
 
 	public Workout getNewWorkout() {
 		if(workouts.isEmpty()){
 			return new Workout("no more workouts", 0);
 		}
 		String side = "";
 		if(pairedWorkout){
 			pairedWorkout = false;
 			side = "right";
 			Workout workoutToReturn = new Workout(tempWorkout.getWorkout() + " " + side,0);
 			return workoutToReturn;
 		}
 		int randInt = randGen.nextInt(workouts.size());
 		tempWorkout = (Workout) workouts.toArray()[randInt];
 		if(tempWorkout.isPaired()){
 			pairedWorkout = true;
 			side = "left";
 		}
 		workouts.remove(tempWorkout);
 		Workout workoutToReturn = new Workout(tempWorkout.getWorkout() +" " + side,0);
 		return workoutToReturn;
 	}
 
 	public Workout getNewNonPairedWorkout() {
 		Workout returnWorkout = getNewWorkout();
 		while(returnWorkout.isPaired()){
 			returnWorkout = getNewWorkout();
 		}
 		return returnWorkout;
 	}
 
 
 }
