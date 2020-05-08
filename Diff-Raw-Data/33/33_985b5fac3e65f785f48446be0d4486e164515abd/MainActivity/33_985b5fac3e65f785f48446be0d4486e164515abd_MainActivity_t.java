 package edu.kaist.kse631.bmaingret_achin.mycoach;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class MainActivity extends BaseActivity {
 	private View startActivitySpecifics = null;
 	private View activityStartedSpecifics = null;
 	private long activityId = -1;
 	private boolean ongoing = false;
 	private static final String TAG = "MainActivity";
 	private Chronometer chrono = null;
 	private ListView historyView = null;
 	long datetime  = 0;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         /* Getting back the data in savedinstance */
         if (null != savedInstanceState){
         	activityId = savedInstanceState.getLong("activityId", -1);
         	ongoing = savedInstanceState.getBoolean("ongoing", false);
         	Log.d(TAG, "Instance state restored");
         }
 
         setContentView(R.layout.activity_main);
 
         /* Alarm manager */
 		ReminderAlarm alarm = new ReminderAlarm(this);
 		Log.d(TAG, "Reminder set");
         
         /* Storing views and update the UI */
         startActivitySpecifics = (View) findViewById(R.id.start_activity_layout);
         activityStartedSpecifics = (View) findViewById(R.id.activity_started_layout);
         updateUI(null);
         
         /* Start activity button */
         Button startActivity = (Button) findViewById(R.id.main_startButton);
         startActivity.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this, NewActivityActivity.class);
 				startActivityForResult(intent, C.REQUEST_START_ACTIVITY);			
 			}
 		});
         
         /* History button */
         Button historyButton = (Button) findViewById(R.id.main_historyButton);
         historyButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
 				startActivity(intent);	
 			}
 		});
         
         /* Stop activity button */
         ImageButton stopActivity = (ImageButton) findViewById(R.id.main_stop_button);
         stopActivity.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				chrono.stop();
 				updateUI(false);
 				
 				Toast toast = Toast.makeText(MainActivity.this, "TODO: Record activity", Toast.LENGTH_SHORT);
 				toast.show();
				long id = saveActivity();
 				Intent intent = new Intent(MainActivity.this, ActivityDetailsActivity.class);
 				intent.putExtra(C.DETAILS_FROM, C.DETAILS_FROM_MAIN);
				intent.putExtra("activityId", id);
 				startActivity(intent);
 			}
 		});
         
         /* Record a new activity */
         Button recordActivity = (Button) findViewById(R.id.main_recordButton);
         recordActivity.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this, ActivityDetailsActivity.class);
 				startActivity(intent);	
 			}
 		});
         
         /* History listview */
         historyView = (ListView) findViewById(R.id.main_historyListView);
         historyView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
 				Log.d(TAG, "Clicked activity's id" + id);
 				Intent intent = new Intent(MainActivity.this, ActivityDetailsActivity.class);
 				intent.putExtra("activityId", id);
 				startActivity(intent);	
 			}
 		});
         updateHistory();
     }
     
     protected void onSaveInstanceState (Bundle outState){
     	outState.putLong("activityId", activityId);
     	outState.putBoolean("ongoing", ongoing);
     	Log.d(TAG, "Instance state saved");
     }
 
     protected void updateUI(Boolean ongoing){
     	if (null != ongoing){
     		this.ongoing = ongoing;
     	}
     		
     	if (this.ongoing){
 			startActivitySpecifics.setVisibility(View.GONE);
 			activityStartedSpecifics.setVisibility(View.VISIBLE);
     	}
     	else{
 			startActivitySpecifics.setVisibility(View.VISIBLE);
 			activityStartedSpecifics.setVisibility(View.GONE);
     	}
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if (requestCode == 1) {
     		if(resultCode == RESULT_OK){       
     			updateUI(true);
     			/* Setting activityId */
     			activityId = data.getLongExtra("activityId", -1);
     			
     			/*display activity name*/
     			TextView activityText = (TextView) findViewById(R.id.main_ongoing_activity);
     			String activity = data.getStringExtra("activity");
     			activityText.setText(getString(R.string.main_ongoing_activity) + " " + activity);
 
     			/* Starting chronometer*/
     			chrono = (Chronometer) findViewById(R.id.main_chronometer);
     			chrono.setBase(SystemClock.elapsedRealtime());
     			chrono.start();
     			
     			/* Recording start datetime */
     			datetime =  System.currentTimeMillis();
     		}
     		if (resultCode == RESULT_CANCELED) {
     		}
     	}
     }
     
    protected long saveActivity(){
     	long duration = SystemClock.elapsedRealtime()-chrono.getBase();
     	UserActivitiesTableHelper helper = new UserActivitiesTableHelper(this);
    	return helper.saveActivity(activityId, datetime, duration);
     }
     
     protected void updateHistory(){
         UserActivitiesTableHelper helper = new UserActivitiesTableHelper(this);
         Cursor history = helper.getHistory();
         String[] from = new String[]{ActivitiesTableHelper.COLUMN_ACTIVITY, UserActivitiesTableHelper.COLUMN_DATETIME};
         int[] to = {android.R.id.text1, android.R.id.text2};
         SimpleCursorAdapter historyAdapter = new SimpleCursorAdapter(this,
         		android.R.layout.simple_list_item_2,
         		history,
         		from,
         		to);
         historyAdapter.setViewBinder(new ViewBinder() {
             public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
                 if (aColumnIndex == aCursor.getColumnIndex(UserActivitiesTableHelper.COLUMN_DATETIME)) {
                         long datetime= aCursor.getLong(aColumnIndex);
                         TextView textView = (TextView) aView;
             			Date date = new Date(datetime);
             			DateFormat formatter = new SimpleDateFormat(" MM/dd/yyyy");
             			String dateFormatted = formatter.format(date);
                         textView.setText(dateFormatted);
                         return true;
                  }
                  return false;
             }
         });
         historyView.setAdapter(historyAdapter);
     }
 }
