 package ua.in.leopard.androidCoocooAfisha;
 
 import android.app.Activity;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public class AfishaWidgetConfigure extends Activity implements OnClickListener, OnItemSelectedListener {
 	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
 	private static final String WIDGET_PREFIX_KEY = "widget_update_interval_";
 	private static final int DEF_TIMER_INTERVAL = 30; 
 	
 	public AfishaWidgetConfigure() {
         super();
     }
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setResult(RESULT_CANCELED);
       setContentView(R.layout.afisha_widget_configure);
       
       // Find the widget id from the intent. 
       Intent intent = getIntent();
       Bundle extras = intent.getExtras();
       if (extras != null) {
           mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
       }
 
       // If they gave us an intent without the widget id, just bail.
       if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
           finish();
       }
       
       Spinner spiner = (Spinner)findViewById(R.id.time_spinner);
       spiner.setOnItemSelectedListener(this);
       ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.widget_time_title, android.R.layout.simple_spinner_item);
       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       spiner.setAdapter(adapter);
       
       View okButton = findViewById(R.id.widget_ok_button);
       okButton.setOnClickListener(this);
       View cancelButton = findViewById(R.id.widget_cancel_button);
       cancelButton.setOnClickListener(this);
       
     }
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
 		int interval = (position + 1) * 30;
 		final Context context = AfishaWidgetConfigure.this;
 		saveTimerPref(context, mAppWidgetId, interval);
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		final Context context = AfishaWidgetConfigure.this;
 		saveTimerPref(context, mAppWidgetId, DEF_TIMER_INTERVAL);
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent resultValue = new Intent();
 		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
 		switch (v.getId()) {
 		  case R.id.widget_ok_button:
 			 final Context context = AfishaWidgetConfigure.this;
 			 AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 			 AfishaWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);
 			 
 	         setResult(RESULT_OK, resultValue);
 	         finish();
 	         break;
 		  case R.id.widget_cancel_button:
 			 setResult(RESULT_CANCELED, resultValue);
 			 finish();
 			 break;      
 	      }
 	}
 	
 	static void saveTimerPref(Context context, int appWidgetId, int interval) {
         SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(WIDGET_PREFIX_KEY + Integer.toString(appWidgetId), interval);
         prefs.commit();
     }
 	
 	static int loadTimerPref(Context context, int appWidgetId) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(WIDGET_PREFIX_KEY + Integer.toString(appWidgetId), DEF_TIMER_INTERVAL);
     }
 	
 	static void deleteTimerPref(Context context, int appWidgetId) {
 		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.remove(WIDGET_PREFIX_KEY + Integer.toString(appWidgetId));
         prefs.commit();
     }
 }
