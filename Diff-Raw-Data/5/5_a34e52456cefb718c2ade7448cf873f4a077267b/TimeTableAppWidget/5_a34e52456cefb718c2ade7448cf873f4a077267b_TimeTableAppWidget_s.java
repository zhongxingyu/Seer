 package my.edu.mmu.timetable;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 /**
  * Implementation of App Widget functionality.
  */
 public class TimeTableAppWidget extends AppWidgetProvider {
 
 	private static int index;
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		// There may be multiple widgets active, so update all of them
 		final int N = appWidgetIds.length;
 		for (int i = 0; i < N; i++) {
 			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
 		}
 	}
 
 	@Override
 	public void onEnabled(Context context) {
 		// Enter relevant functionality for when the first widget is created
 	}
 
 	@Override
 	public void onDisabled(Context context) {
 		// Enter relevant functionality for when the last widget is disabled
 	}
 
 	static void updateAppWidget(Context context,
 			AppWidgetManager appWidgetManager, int appWidgetId) {
 
 		String title;
 		StringBuilder output = new StringBuilder();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		String session = prefs.getString("session", "");
 		String trimester = prefs.getString("trimester", "");
 		String group = prefs.getString("group", "");
		if(!session.isEmpty()) {
 			title = "Tri " + trimester + " " + session + " " + group;
 		} else {
 			title = "MMU Simple Timetable";			
 		}
 		index = 0;
 		Calendar calendarNow = Calendar.getInstance();
 		int dayOfWeek = calendarNow.get(Calendar.DAY_OF_WEEK);
 		if(dayOfWeek>1 && dayOfWeek<7) {
 			index = dayOfWeek - 2;
 		} 
 		try {
 			InputStream is = context.getAssets().open("timetable.json");
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader reader = new BufferedReader(isr);
 			StringBuilder builder = new StringBuilder();
 			String line;
 			while ((line = reader.readLine()) != null) {
 				builder.append(line + "\n");
 			}
 			JSONObject jObject = new JSONObject(builder.toString());			
 			JSONArray jDays = jObject.getJSONArray("days");
 			JSONObject jDay = jDays.getJSONObject(index);
 			String day = jDay.getString("day");
 			JSONArray jClasses = jDay.getJSONArray("class");
 			for (int j = 0; j < jClasses.length(); j++) {
 				JSONObject jClass = jClasses.getJSONObject(j);
 				String time = jClass.getString("time");
				String startTime = time.split("-")[0].trim();	
 	            SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
 	            try {
 	            	Date timeStart = sdf.parse(startTime);
 	            	Calendar calendarStart = Calendar.getInstance();
 	            	calendarStart.setTime(timeStart);
 	            	Log.d("timetable", "start:" + calendarStart.get(Calendar.HOUR_OF_DAY));
 	            	Log.d("timetable", "now:" + calendarNow.get(Calendar.HOUR_OF_DAY));
 	            	if(calendarStart.get(Calendar.HOUR_OF_DAY) > calendarNow.get(Calendar.HOUR_OF_DAY)) {
 	    				String subject = jClass.getString("subject");
 	    				String venue = jClass.getString("venue");
 	    				Item item = new Item(day, time, subject, venue);
 	    				output.append(item.toString());
 	    				break;
 	            	}
 	            } catch (ParseException e){
 	                // Exception handling goes here
 	            }
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		// Construct the RemoteViews object
 		RemoteViews views = new RemoteViews(context.getPackageName(),
 				R.layout.time_table_app_widget);
 		views.setTextViewText(R.id.appwidget_text_title, title);
 		views.setTextViewText(R.id.appwidget_text, output.toString());
 		Intent i = new Intent(context, MainActivity.class);
 		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 		PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
 		views.setOnClickPendingIntent(R.id.widget_layout, pi);
 
 		// Instruct the widget manager to update the widget
 		appWidgetManager.updateAppWidget(appWidgetId, views);
 	}
 }
