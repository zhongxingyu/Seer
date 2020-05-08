 package com.transitwidget;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.appwidget.AppWidgetManager;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import com.transitwidget.adapters.BaseItemAdapter;
 import com.transitwidget.feed.model.Agency;
 import com.transitwidget.prefs.*;
 import com.transitwidget.service.AlarmSchedulerService;
 import com.transitwidget.utils.AdapterUtils;
 import com.transitwidget.utils.TimeUtils;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * The configuration screen for the ExampleAppWidgetProvider widget sample.
  */
 public class WidgetConfigActivity extends Activity {
     static final String TAG = "MBTAWidgetConfig";
 
     int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
     
     private NoDefaultSpinner agencySpinner;
     private NoDefaultSpinner routeSpinner;
     private NoDefaultSpinner directionSpinner;
     private NoDefaultSpinner stopSpinner;
     
     private Button saveButton;
     
     private TextView startTimeView;
     private TextView endTimeView;
     
     private BaseItemAdapter<? extends NextBusValue> routeAdapter;
     private BaseItemAdapter<? extends NextBusValue> agencyAdapter;
     private BaseItemAdapter<? extends NextBusValue> directionAdapter;
     private BaseItemAdapter<? extends NextBusValue> endPointAdapter;
     private NextBusObserverConfig config;
     
     private int startHour;
     private int startMinute;
     private int endHour;
     private int endMinute;
     
     static final int TIME_START_DIALOG_ID = 0;
     static final int TIME_END_DIALOG_ID = 1;
     
     private static void updateDisplay(TextView display, int hour, int minute) {
         StringBuilder build = new StringBuilder();
         String ampm = "";
         if (hour < 12) {
             if (hour == 0) hour = 12;  // 0 => 12am
             ampm = "am";
         } else {
             if (hour > 12) hour -= 12; // 13-23 => 1-11pm
             ampm = "pm";
         }
         build.append(hour).append(":");
         if (minute < 10) build.append("0");
         build.append(minute).append(ampm);
         display.setText(build.toString());
     }
     
     private void updateStartDisplay() {
         updateDisplay(startTimeView, startHour, startMinute);
     }
     
     private void updateEndDisplay() {
         updateDisplay(endTimeView, endHour, endMinute);
     }
     
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case TIME_START_DIALOG_ID:
                 return new TimePickerDialog(this, mStartTimeSetListener, startHour, startMinute, false);
             case TIME_END_DIALOG_ID:
                 return new TimePickerDialog(this, mEndTimeSetListener, endHour, endMinute, false);
         }
         return null;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i(TAG, "Start OnCreate");
         // Set the result to CANCELED.  This will cause the widget host to cancel
         // out of the widget placement if they press the back button.
         setResult(RESULT_CANCELED);
 
         // Set the view layout resource to use.
         setContentView(R.layout.widget_config);
         
         startTimeView = (TextView) findViewById(R.id.startTimePickerValue);
         endTimeView = (TextView) findViewById(R.id.endTimePickerValue);
         
         Calendar cal = Calendar.getInstance();
         startHour = cal.get(Calendar.HOUR_OF_DAY);
         startMinute = cal.get(Calendar.MINUTE);
         
         cal.add(Calendar.MINUTE, 60);
         endHour = cal.get(Calendar.HOUR_OF_DAY);
         endMinute = cal.get(Calendar.MINUTE);
         
         updateStartDisplay();
         updateEndDisplay();
         
         findViewById(R.id.startTimePicker).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(TIME_START_DIALOG_ID);
             }
         });
         
         findViewById(R.id.endTimePicker).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(TIME_END_DIALOG_ID);
             }
         });
 
         // Bind the action for the save button.
         saveButton = (Button) findViewById(R.id.saveBtn);
         saveButton.setEnabled(false);
         saveButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 final Context context = WidgetConfigActivity.this;
 
                 // Until there are UI selectors, we'll start in 5 seconds and run for 10 minutes.
                 Calendar start = Calendar.getInstance();
                 start.set(Calendar.HOUR_OF_DAY, startHour);
                 start.set(Calendar.MINUTE, startMinute);
                 start.set(Calendar.SECOND, 0);
                 int startTime = TimeUtils.getTimeFromBeginingOfDay(start);
 
                 Calendar end = Calendar.getInstance();
                 end.set(Calendar.HOUR_OF_DAY, endHour);
                 end.set(Calendar.MINUTE, endMinute);
                 end.set(Calendar.SECOND, 0);
                 int endTime = TimeUtils.getTimeFromBeginingOfDay(end);
 
                 config.setStartObserving(startTime);
                 config.setStopObserving(endTime);
 
                 // When the button is clicked, save the string in our prefs and return that they
                 // clicked OK.
                 savePreferences(context, mAppWidgetId);
 
                 // Push widget update to surface with newly set prefix
                 //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 
                 // Make sure we pass back the original appWidgetId
                 Intent resultValue = new Intent();
                 resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                 setResult(RESULT_OK, resultValue);
 
                 Intent serviceIntent = new Intent(getApplicationContext(), AlarmSchedulerService.class);
                 serviceIntent.putExtra(AlarmSchedulerService.EXTRA_WIDGET_ID, mAppWidgetId);
                 serviceIntent.putExtra(AlarmSchedulerService.EXTRA_DAY_START_TIME, startTime);
                 serviceIntent.putExtra(AlarmSchedulerService.EXTRA_DAY_END_TIME, endTime);
                 startService(serviceIntent);
 
                 finish();
             }
         });
 
         findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 finish();
             }
         });
         
         // Find the widget id from the intent. 
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         if (extras != null) {
             mAppWidgetId = extras.getInt(
             AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
         }
 
         // If they gave us an intent without the widget id, just bail.
         if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
             finish();
         }
         
         agencySpinner = (NoDefaultSpinner) findViewById(R.id.agencySpinner);
         routeSpinner = (NoDefaultSpinner) findViewById(R.id.routeSpinner);
         directionSpinner = (NoDefaultSpinner) findViewById(R.id.directionSpinner);
         stopSpinner = (NoDefaultSpinner) findViewById(R.id.endPointSpinner);
 
         resetRouteSpinner();
         resetDirectionSpinner();
         resetStopSpinner();
         
         agencySpinner.setEnabled(false);
         agencySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
                 Log.i(TAG, "Agency selected: " + id);
                 resetRouteSpinner();
                 resetDirectionSpinner();
                 resetStopSpinner();
                 updateRoute();
                 
                 // Save selected id
 				Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(Agency.CONTENT_URI, id), new String[] { Agency.TAG }, null, null, null);
 				String tag = null;
 				if (cursor.moveToFirst()) {
 					tag = cursor.getString(0);
 				}
 
 				Log.i(TAG, "Saving selected agency with id " + id + " and tag " + tag);
 				getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit()
 						.putLong("agency", id)
 						.putString("agencyTag", tag)
 					.commit();
             }
             public void onNothingSelected(AdapterView<?> arg0) {}
         });
         
         routeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
                 Log.i(TAG, "Route selected: " + id);
                 resetDirectionSpinner();
                 resetStopSpinner();
                 updateDirection();
             }
             public void onNothingSelected(AdapterView<?> arg0) {}
         });
      
         directionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                 resetStopSpinner();
                 updateStop();
             }
             public void onNothingSelected(AdapterView<?> arg0) {}
         });
         
         stopSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
                 saveButton.setEnabled(true);
             }
 
             public void onNothingSelected(AdapterView<?> arg0) {
                 saveButton.setEnabled(false);
             }
         });
         
         config = new NextBusObserverConfig(WidgetConfigActivity.this, mAppWidgetId);
         new UpdateAgencies().execute(config);
         
         Log.i(TAG, "End OnCreate");
     }
 
     private BaseItemAdapter<NextBusValue> createAdapter(List<? extends NextBusValue> data, Spinner spinner) {
 		ArrayList<NextBusValue> tmp = new ArrayList<NextBusValue>();
 		BaseItemAdapter<NextBusValue> adapter;
 		
 		tmp.add(new NextBusValue("Unspecified"));
 		if(data != null) {
 			tmp.addAll(data);
 		}
 		adapter =  new BaseItemAdapter<NextBusValue>(WidgetConfigActivity.this, android.R.layout.simple_spinner_item, tmp);
     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         return adapter;
 	}
     
 	private void resetRouteSpinner() {
         routeSpinner.setEnabled(false);
 		routeAdapter = createAdapter(null, routeSpinner);
 	}
 
 	private void resetStopSpinner() {
         stopSpinner.setEnabled(false);
         endPointAdapter = createAdapter(null, stopSpinner);
         saveButton.setEnabled(false);
 	}
 
 	private void resetDirectionSpinner() {
         directionSpinner.setEnabled(false);
 		directionAdapter = createAdapter(null, directionSpinner);
 	}
     
     // Write the prefix to the SharedPreferences object for this widget
     void savePreferences(Context context, int appWidgetId) {
         int pos;
         pos = stopSpinner.getSelectedItemPosition();
         config.setStop((NextBusStop) endPointAdapter.getItem(pos));
         config.save();
         Log.i(TAG, config.getStops().toString());
     }
 
     void updateRoute() {
    	if(agencySpinner.getSelectedItemPosition() >= 0) {
     		int pos = agencySpinner.getSelectedItemPosition();
             config.setAgency((NextBusAgency) agencyAdapter.getItem(pos));
     		new UpdateRoutes().execute(config);
     	}
     }
     
     void updateDirection() {
    	if(routeSpinner.getSelectedItemPosition() >= 0) {
     		int pos = routeSpinner.getSelectedItemPosition();
             config.setRoute((NextBusRoute) routeAdapter.getItem(pos));
     		new UpdateDirection().execute(config);
     	}
     }
     
     void updateStop() {
    	if(directionSpinner.getSelectedItemPosition() >= 0) {
     		int pos = directionSpinner.getSelectedItemPosition();
             config.setDirection((NextBusDirection) directionAdapter.getItem(pos));
     		new UpdateEndPoint().execute(config);
     	}
     }
     
     private class UpdateAgencies extends AsyncTask<NextBusObserverConfig, Void, List<NextBusAgency>> {
 		@Override
 		protected List<NextBusAgency> doInBackground(NextBusObserverConfig... arg) {
 			return arg[0].getAgencies();
 		}
 
         @Override
         protected void onPostExecute(List<NextBusAgency> result) {
         	agencyAdapter = createAdapter(result, agencySpinner);
         	agencySpinner.setEnabled(true);
             
             // Load previously selected agency from preferences
 	        long selectedAgency = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getLong("agency", -1);
             if (selectedAgency >= 0) {
 	        	int position = AdapterUtils.getAdapterPositionById(agencySpinner.getAdapter(), selectedAgency);
                 agencySpinner.setSelection(position);
             }
         }
     }
     
     private class UpdateRoutes extends AsyncTask<NextBusObserverConfig, Void, List<NextBusRoute>> {
 		@Override
 		protected List<NextBusRoute> doInBackground(NextBusObserverConfig... arg) {
 			return arg[0].getRoutes();
 		}
 
         @Override
         protected void onPostExecute(List<NextBusRoute> result) {
         	routeAdapter = createAdapter(result, routeSpinner);
         	routeSpinner.setEnabled(true);
         }
     }
     
     private class UpdateDirection extends AsyncTask<NextBusObserverConfig, Void, List<NextBusDirection>> {
 		@Override
 		protected List<NextBusDirection> doInBackground(NextBusObserverConfig... arg) {
 			return arg[0].getDirections();
 		}
 
         @Override
         protected void onPostExecute(List<NextBusDirection> result) {
         	directionAdapter = createAdapter(result, directionSpinner);
         	directionSpinner.setEnabled(true);
         }
     }
     
     private class UpdateEndPoint extends AsyncTask<NextBusObserverConfig, Void, List<NextBusStop>> {
 		@Override
 		protected List<NextBusStop> doInBackground(NextBusObserverConfig... arg) {
 			return arg[0].getStops();
 		}
 
         @Override
         protected void onPostExecute(List<NextBusStop> result) {
         	endPointAdapter = createAdapter(result, stopSpinner);
         	stopSpinner.setEnabled(true);
             saveButton.setEnabled(false);
         }
     }
     
     // the callback received when the user "sets" the time in the dialog
     private TimePickerDialog.OnTimeSetListener mStartTimeSetListener =
         new TimePickerDialog.OnTimeSetListener() {
             public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                 startHour = hourOfDay;
                 startMinute = minute;
                 updateStartDisplay();
             }
         };
     
     // the callback received when the user "sets" the time in the dialog
     private TimePickerDialog.OnTimeSetListener mEndTimeSetListener =
         new TimePickerDialog.OnTimeSetListener() {
             public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                 endHour = hourOfDay;
                 endMinute = minute;
                 updateEndDisplay();
             }
         };
 }
