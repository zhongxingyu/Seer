 /**
  *   Copyright 2012 Francesco Balducci
  *
  *   This file is part of FakeDawn.
  *
  *   FakeDawn is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   FakeDawn is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.balau.fakedawn;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class Dawn extends Activity implements OnClickListener {
 
 	private static int TIMER_TICK_SECONDS = 10;
 	private static final String ALARM_START_MILLIS = "ALARM_START_MILLIS";
 
 	private long m_alarmStartMillis;
 	private long m_alarmEndMillis;
 	private Timer m_timer;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.dawn);
 
 		getWindow().addFlags(
 				WindowManager.LayoutParams.FLAG_FULLSCREEN|
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
 				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
 				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
 				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
 
 		findViewById(R.id.dawn_background).setOnClickListener(this);
 
 		SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
 		String day;
		Calendar rightNow = Calendar.getInstance();
 
		switch (rightNow.get(Calendar.DAY_OF_WEEK)) {
 		case Calendar.MONDAY:
 			day = "mondays";
 			break;
 		case Calendar.TUESDAY:
 			day = "tuesdays";
 			break;
 		case Calendar.WEDNESDAY:
 			day = "wednesdays";
 			break;
 		case Calendar.THURSDAY:
 			day = "thursdays";
 			break;
 		case Calendar.FRIDAY:
 			day = "fridays";
 			break;
 		case Calendar.SATURDAY:
 			day = "saturdays";
 			break;
 		case Calendar.SUNDAY:
 			day = "sundays";
 			break;
 		default:
 			day = "NON_EXISTING_WEEKDAY";
 			break;
 		}
 		if(!pref.getBoolean(day, false))
 		{
 			this.finish();
 		}
 		else
 		{
			m_alarmStartMillis = rightNow.getTimeInMillis();
 			if(savedInstanceState != null)
 			{
 				if(savedInstanceState.containsKey(ALARM_START_MILLIS))
 				{
 					m_alarmStartMillis = savedInstanceState.getLong(ALARM_START_MILLIS);
 				}
 			}
 			m_alarmEndMillis = m_alarmStartMillis + (1000*60*pref.getInt("duration", 15));
 
 			Intent sound = new Intent(getApplicationContext(), DawnSound.class);
 			sound.putExtra(DawnSound.EXTRA_SOUND_MILLIS, m_alarmEndMillis);
 			sound.putExtra(DawnSound.EXTRA_SOUND_URI, pref.getString("sound", ""));
 			if(pref.contains("volume"))
 				sound.putExtra(DawnSound.EXTRA_SOUND_VOLUME, pref.getInt("volume", 0));			
 			startService(sound);
 
 			updateBrightness();
 
 			m_timer = new Timer();
 			m_timer.schedule(
 					new TimerTask() {
 
 						@Override
 						public void run() {
 							runOnUiThread(
 									new Runnable() {
 										public void run() {
 											updateBrightness();
 										}
 									});
 						}
 					}, TIMER_TICK_SECONDS*1000, TIMER_TICK_SECONDS*1000);
 
 		}
 	}
 
 	private void stopDawn()
 	{
 		Intent sound = new Intent(getApplicationContext(), DawnSound.class);
 		stopService(sound);
 		this.finish();
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		stopDawn();
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	public void onClick(View v) {
 		stopDawn();
 	}
 
 	private void updateBrightness()
 	{
 		float brightnessStep = 0.01F;
 		float brightness; 
 		long level_percent;
 		int grey_level;
 		int grey_rgb;
 
 		level_percent = 
 				(100 * (System.currentTimeMillis() - m_alarmStartMillis))
 				/ (m_alarmEndMillis - m_alarmStartMillis);
 		if(level_percent < 1) { level_percent = 1; }
 		else if(level_percent > 100) { level_percent = 100; }
 
 		brightness = brightnessStep * level_percent;
 		Log.d("HelloAndroid", String.format("b = %f", brightness));
 
 		grey_level = (int)(brightness * (float)0xFF);
 		if(grey_level > 0xFF) grey_level = 0xFF;
 		grey_rgb = 0xFF000000 + (grey_level * 0x010101);
 		findViewById(R.id.dawn_background).setBackgroundColor(grey_rgb);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		m_timer.cancel();
 		Log.d("FakeDawn", "Dawn Stopped.");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putLong(ALARM_START_MILLIS, m_alarmStartMillis);
 	}
 
 }
