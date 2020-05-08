 package ultimateFrisbee.Workout;
 
 
 import java.sql.SQLException;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class RaeReplacementActivity extends Activity {
     /** Called when the activity is first created. */
 	
 	private Button addWorkout, addPairedWorkout, startWorkout;
 	private WorkoutDBOpener workoutDBOpenerHelper;
 	private static SQLiteDatabase workoutDB;
 	protected static final int ADD_WORKOUT_DIALOG = 2001;
 	protected static final int ADD_PAIRED_WORKOUT_DIALOG = 2002;
 	protected static final String SEGMENT_LENGTH = "seg len";
 	protected static final String TOTAL_LENGTH = "total len";
 	private static final String RAE_REPLACEMENT_DEBUG_TAG = "RAE_REPLACEMENT_ACTIVITY";
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         //setup db
 		workoutDBOpenerHelper = new WorkoutDBOpener(this);
 		workoutDB = workoutDBOpenerHelper.getWritableDatabase();

         
         //setup buttons
         addPairedWorkout = (Button) findViewById(R.id.AddPairedWorkout);        
         addPairedWorkout.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				RaeReplacementActivity.this.showDialog(ADD_PAIRED_WORKOUT_DIALOG);
 			}
 		});
         addWorkout = (Button) findViewById(R.id.AddWorkout);        
         addWorkout.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				RaeReplacementActivity.this.showDialog(ADD_WORKOUT_DIALOG);
 			}
 		});
         startWorkout = (Button) findViewById(R.id.StartWorkout);
         startWorkout.setOnClickListener(new OnClickListener(){
         	public void onClick(View v){
 				Intent intent = new Intent(RaeReplacementActivity.this, WorkoutActivity.class);
 				EditText segmentLength = (EditText) findViewById(R.id.SegmentLength);
 				EditText totalLength = (EditText) findViewById(R.id.TotalLength);
 				intent.putExtra(SEGMENT_LENGTH,Float.valueOf(segmentLength.getText().toString()));
 				intent.putExtra(TOTAL_LENGTH,Float.valueOf(totalLength.getText().toString()));
 				startActivity(intent);
 			}
 		});
         
     }
     
     protected Dialog onCreateDialog(int id) {
 		  final Dialog dialog = new Dialog(this);
 		  switch(id) {
 		  case ADD_WORKOUT_DIALOG:
 			  
 			  dialog.setContentView(R.layout.add_workout);
 			  dialog.setTitle(R.string.AddWorkout);
 			  dialog.setCanceledOnTouchOutside(true);
 			  dialog.show();
 			  Button cancelAddWorkoutDialog = (Button) dialog.findViewById(R.id.cancel);
 			  cancelAddWorkoutDialog.setOnClickListener(new OnClickListener(){
 				  public void onClick(View v){
 					  RaeReplacementActivity.this.removeDialog(ADD_WORKOUT_DIALOG);				  }
 			  });
 			  Button addPlayerManuallyDialogB = (Button) dialog.findViewById(R.id.AddWorkout);
 			  addPlayerManuallyDialogB.setOnClickListener(new OnClickListener(){
 				  public void onClick(View v){
 					  EditText workoutToAdd = (EditText) dialog.findViewById(R.id.WorkoutToAdd);
 					  addWorkout(workoutToAdd.getText().toString(),0);
 					  Toast.makeText(RaeReplacementActivity.this, "Added: " + workoutToAdd.getText().toString() + " to database", Toast.LENGTH_SHORT).show();
 					  RaeReplacementActivity.this.removeDialog(ADD_WORKOUT_DIALOG);
 				  }
 			  });
 			  break;
 		  case ADD_PAIRED_WORKOUT_DIALOG:
 			  
 			  dialog.setContentView(R.layout.add_workout);
 			  dialog.setTitle(R.string.AddPairedWorkout);
 			  dialog.setCanceledOnTouchOutside(true);
 			  dialog.show();
 			  Button cancelAddWorkoutDialog1 = (Button) dialog.findViewById(R.id.cancel);
 			  cancelAddWorkoutDialog1.setOnClickListener(new OnClickListener(){
 				  public void onClick(View v){
 					  RaeReplacementActivity.this.removeDialog(ADD_PAIRED_WORKOUT_DIALOG);				  }
 			  });
 			  Button addPlayerManuallyDialogB1 = (Button) dialog.findViewById(R.id.AddWorkout);
 			  addPlayerManuallyDialogB1.setOnClickListener(new OnClickListener(){
 				  public void onClick(View v){
 					  EditText workoutToAdd = (EditText) dialog.findViewById(R.id.WorkoutToAdd);
 					  addWorkout(workoutToAdd.getText().toString(),1);
 					  Toast.makeText(RaeReplacementActivity.this, "Added: " + workoutToAdd.getText().toString() + " to database , paired", Toast.LENGTH_SHORT).show();
 					  RaeReplacementActivity.this.removeDialog(ADD_PAIRED_WORKOUT_DIALOG);
 				  }
 			  });
 			  break;
 		  default:
 			  //dialog = null;
 		  }
 		  return dialog;
 	  }
     
     protected final static void addWorkout(String workoutName, int paired){
     	ContentValues values = new ContentValues();
 		  values.put("workout", workoutName);
 		  values.put("paired", paired);
		  if(workoutDB.insertOrThrow(WorkoutDBOpener.WORKOUT_TN, null, values)==-1){
 			  Log.d(RAE_REPLACEMENT_DEBUG_TAG, "Workout already in database");
 		  }
     }
 }
