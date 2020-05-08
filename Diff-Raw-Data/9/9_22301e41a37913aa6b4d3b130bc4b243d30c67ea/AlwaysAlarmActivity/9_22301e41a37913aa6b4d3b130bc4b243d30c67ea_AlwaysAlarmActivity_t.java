 package org.worldsproject.alarmclock;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class AlwaysAlarmActivity extends Activity 
 {
 	public static ArrayList<Alarm> alarms = new ArrayList<Alarm>();
 
 	public static LinearLayout root;
 	private SharedPreferences pref;
 	
 	private final long MSINWEEK = 604800000;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		pref = PreferenceManager.getDefaultSharedPreferences(this);
 		Intent intent = getIntent();
 		String mode = intent.getStringExtra("mode");
 
 		if(alarms.size() > 0)
 			root = (LinearLayout)alarms.get(0).getParent();
 		else
 			root = (LinearLayout)findViewById(R.id.alarms);
 		root.removeAllViews();
 		
 		for(Alarm v:alarms)
 		{
 			root.addView(v);
 		}
 
 		if(mode != null)
 		{
 			if(mode.equals("new_alarm"))
 			{
 				String unit = pref.getString("alarm_units", "false");
 				Alarm alarm = new Alarm(this,
 						intent.getIntExtra("hour", 0),
 						intent.getIntExtra("minute", 0),
 						intent.getIntExtra("steps", 0),
 						intent.getBooleanExtra("monday", false),
 						intent.getBooleanExtra("tuesday", false),
 						intent.getBooleanExtra("wednesday", false),
 						intent.getBooleanExtra("thursday", false),
 						intent.getBooleanExtra("friday", false),
 						intent.getBooleanExtra("saturday", false),
 						intent.getBooleanExtra("sunday", false), 
 						(unit.equalsIgnoreCase("false") ? false : true));
 
 				if(createAlarm(alarm))
 				{
 					root.addView(alarm);
 					alarms.add(alarm);
 				}
 			}
 		}
 	}
 
 	/*
 	 * @return true is a new Alarm view should be added
 	 */
 	private boolean createAlarm(Alarm alarm)
 	{
 		ArrayList<Calendar> cal = alarm.nextAlarmEvent();
 
 		if(cal == null)
 		{
 			Toast.makeText(getBaseContext(), "Alarm exists in past.\n  No alarm added", Toast.LENGTH_LONG).show();
 			return false;
 		}
 
 		Calendar cur = Calendar.getInstance();
 		long diff = cal.get(0).getTimeInMillis() - cur.getTimeInMillis();
 
 		int seconds = (int)(diff/1000)%60;
 		int minutes = (int)(diff/60000)%60;
 		int hours = (int)(diff/1440000)%24;
 		int days = (int)(diff/86400000);
 
 		StringBuffer buf = new StringBuffer("Next alarm in\n");
 
 		if(days > 0 && hours > 0 && minutes > 0)
 		{
 			buf.append(dayFormat(days));
 			buf.append(hourFormat(hours));
 			buf.append(minuteFormat(minutes));
 			buf.append(secondFormat(seconds));
 		}
 		else
 		{
 			if(hours > 0 && minutes > 0)
 			{
 				buf.append(hourFormat(hours));
 				buf.append(minuteFormat(minutes));
 				buf.append(secondFormat(seconds));
 			}
 			else
 			{
 				if(minutes > 0)
 				{
 					buf.append(minuteFormat(minutes));
 					buf.append(secondFormat(seconds));
 				}
 				else
 				{
 					buf.append(secondFormat(seconds));
 				}
 			}
 		}
 
		Toast toast = Toast.makeText(this, buf, Toast.LENGTH_LONG);
		((TextView)((LinearLayout)toast.getView()).getChildAt(0))
	    .setGravity(Gravity.CENTER_HORIZONTAL);
 		toast.show();
 
 		Intent alarmIntent = new Intent(AlwaysAlarmActivity.this, AlarmReceiver.class);
 		alarmIntent.putExtra("steps", alarm.getSteps());
 		
 		PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		alarm.setIntent(sender);
 		
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		
 		for(Calendar c:cal)
 			am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), MSINWEEK, sender);
 		
 		((ToggleButton)alarm.findViewById(R.id.toggleButton1)).setChecked(true);
 		return true;
 	}
 
 	public void removeAlarm(View v)
 	{
 		Alarm temp = (Alarm)v.getParent().getParent().getParent();//TODO This feels dirty...
 		root.removeView(temp);
 		alarms.remove(temp);
 		((AlarmManager)getSystemService(ALARM_SERVICE)).cancel(temp.getAlarmIntent());
 	}
 
 	public void toggled(View v)
 	{
 		Alarm alarm = (Alarm)v.getParent().getParent().getParent();
 		ToggleButton tb = (ToggleButton)v;
 
 		if(tb.isChecked())
 		{
 			createAlarm(alarm);
 		}
 		else
 		{
 			((AlarmManager)getSystemService(ALARM_SERVICE)).cancel(alarm.getAlarmIntent());
 		}
 	}
 
 	public void addAlarm(View v)
 	{
 		Intent myIntent = new Intent(AlwaysAlarmActivity.this, AddAlarmActivity.class);
 		AlwaysAlarmActivity.this.startActivity(myIntent);
 	}
 
 	public void showPreferences(View v)
 	{
 		Intent myIntent = new Intent(AlwaysAlarmActivity.this, AlarmPreferences.class);
 		AlwaysAlarmActivity.this.startActivity(myIntent);
 	}
 
 	private String secondFormat(int seconds)
 	{
 		if(seconds != 1)
 			return seconds + " seconds.";
 		else
 			return seconds + " second.";
 	}
 
 	private String minuteFormat(int minutes)
 	{
 		if(minutes != 1)
 			return minutes + " minutes\n";
 		else
 			return minutes + " minute\n";
 	}
 
 	private String hourFormat(int hours)
 	{
 		if(hours != 1)
 			return hours + " hours\n";
 		else
 			return hours + " hour\n";
 	}
 
 	private String dayFormat(int days)
 	{
 		if(days != 1)
 			return days + " days\n";
 		else
 			return days + " day\n";
 	}
 }
