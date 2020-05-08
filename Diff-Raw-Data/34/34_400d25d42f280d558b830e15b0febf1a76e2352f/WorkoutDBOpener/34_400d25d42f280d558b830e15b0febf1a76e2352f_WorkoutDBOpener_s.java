 package ultimateFrisbee.Workout;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.widget.Toast;
 
 public class WorkoutDBOpener extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "workouts.db";
 	private static final int DATABASE_VERSION = 1;
 	public static final String WORKOUT_TN = "workouts";
 	private Context context;
 	WorkoutDBOpener(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 		db.execSQL("CREATE TABLE " + WORKOUT_TN + "(workout TEXT PRIMARY KEY, paired INTEGER)");
		RaeReplacementActivity.addWorkout("Scissors",0);
		RaeReplacementActivity.addWorkout("Bicycle ",0);
		RaeReplacementActivity.addWorkout("Vertical Leg Crunch",0);
		RaeReplacementActivity.addWorkout("Long Arm Crunch",0);
		RaeReplacementActivity.addWorkout("Reverse Crunch",0);
		RaeReplacementActivity.addWorkout("Plank on Elbows and Toes",0);
		RaeReplacementActivity.addWorkout("Side Crunch",1);
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 		Toast.makeText(context, "bad things happened", Toast.LENGTH_LONG).show();
 	}
 
 }
