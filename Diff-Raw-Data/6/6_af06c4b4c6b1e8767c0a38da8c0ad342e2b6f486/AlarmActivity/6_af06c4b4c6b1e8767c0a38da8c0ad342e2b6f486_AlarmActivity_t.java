 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.activity;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.joda.time.DateTime;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.audio.VibrationManager;
 import se.chalmers.dat255.sleepfighter.helper.NotificationHelper;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import se.chalmers.dat255.sleepfighter.preference.GlobalPreferencesReader;
 import se.chalmers.dat255.sleepfighter.service.AlarmPlannerService;
 import se.chalmers.dat255.sleepfighter.service.AlarmPlannerService.Command;
 import se.chalmers.dat255.sleepfighter.utils.android.AlarmWakeLocker;
 import se.chalmers.dat255.sleepfighter.utils.android.IntentUtils;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * The activity for when an alarm rings/occurs.
  * 
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @author Lam(m)<dannylam@gmail.com> / Danny Lam
  * @version 1.0
  * @since Sep 20, 2013
  */
 public class AlarmActivity extends Activity {
 
 	public static final String EXTRA_ALARM_ID = "alarm_id";
 
 	private static final int WINDOW_FLAGS_SCREEN_ON = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
 			| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
 			| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
 
 	private static final int WINDOW_FLAGS_LOCKSCREEN = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
 
 	public static final int CHALLENGE_REQUEST_CODE = 1;
 
 	private boolean turnScreenOn = true;
 	private boolean bypassLockscreen = true;
 	private TextView tvName;
 	private TextView tvTime;
 	private Alarm alarm;
 	public Timer timer;
 	private boolean isFlashOn = false;
 	private Camera camera;
 	private Button button;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Turn and/or Keep screen on.
 		this.setScreenFlags();
 
 		this.setContentView(R.layout.activity_alarm_prechallenge);
 
 		SFApplication app = SFApplication.get();
 
 		// Fetch alarm Id.
 		int alarmId = new IntentUtils(this.getIntent()).getAlarmId();
 		this.alarm = app.getPersister().fetchAlarmById(alarmId);
 
 		// Get the name and time of the current ringing alarm
 		tvName = (TextView) findViewById(R.id.tvAlarmName);
 		tvName.setText(alarm.getName());
 
 		tvTime = (TextView) findViewById(R.id.tvAlarmTime);
 
 		Button btnChallenge = (Button) findViewById(R.id.btnChallenge);
 		btnChallenge.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onStopClick();
 			}
 		});
 
 		Button btnSnooze = (Button) findViewById(R.id.btnSnooze);
 		if(alarm.getSnoozeConfig().isSnoozeEnabled()) {
 			btnSnooze.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					startSnooze();
 				}
 			});
 		} else {
 			btnSnooze.setVisibility(View.GONE);
 		}
 		
 		button = (Button) findViewById(R.id.btnFlash);
 		Context context = this;
 		PackageManager pm = context.getPackageManager();
		
		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Log.e("err", "No camera detected");

			return;
		}
 
 		camera = Camera.open();
 		final Parameters p = camera.getParameters();
 
 		button.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				if (isFlashOn) {
 					Log.i("info", "THE FLASH'S OFF!");
 					p.setFlashMode(Parameters.FLASH_MODE_OFF);
 					camera.setParameters(p);					
 					isFlashOn = false;
 					button.setText("FLASH ON");
 				} else {
 					Log.i("info", "THE FLASH'S ON!");
 					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
 					camera.setParameters(p);					
 					isFlashOn = true;
 					button.setText("FLASH OFF");
 				}
 			}
 		});
 		
 		
 		
 		
 		
 		
 	}
 
 	private void onStopClick() {
 		boolean challengeEnabled = this.alarm.getChallengeSet().isEnabled();
 		if (challengeEnabled) {
 			startChallenge();
 		} else {
 			stopAlarm();
 		}
 	}
 
 	/**
 	 * Launch ChallengeActivity to start alarm.
 	 */
 	private void startChallenge() {
 		// The vibration stops whenever you start the challenge
 		VibrationManager.getInstance().stopVibrate(getApplicationContext());
 
 		// Send user to ChallengeActivity.
 		Intent i = new Intent(this, ChallengeActivity.class);
 		new IntentUtils( i ).setAlarmId( this.alarm );
 		startActivityForResult(i, CHALLENGE_REQUEST_CODE);
 	}
 
 	/**
 	 * Stops alarm temporarily and sends a snooze command to the server.
 	 */
 	private void startSnooze() {
 		// Should the user complete a challenge before snoozing?
 
 		stopAudio();
 
 		VibrationManager.getInstance().stopVibrate(getApplicationContext());
 
 		// Remove notification saying alarm is ringing
 		NotificationHelper.removeNotification(this);
 
 		// Send snooze command to service
 		AlarmPlannerService.call(this, Command.SNOOZE, alarm.getId());
 		finish();
 	}
 
 	protected void onPause() {
 		super.onPause();
 
 		// Release the wake-lock acquired in AlarmReceiver!
 		AlarmWakeLocker.release();
 	}
 
 	private void performRescheduling() {
 		SFApplication app = SFApplication.get();
 
 		// Disable alarm if not repeating.
 		if (!this.alarm.isRepeating()) {
 			if (this.alarm.getMessageBus() == null) {
 				this.alarm.setMessageBus(app.getBus());
 			}
 
 			this.alarm.setActivated(false);
 		} else {
 			// Reschedule earliest alarm (if any).
 			AlarmTimestamp at = app.getAlarms().getEarliestAlarm(
 					new DateTime().getMillis());
 			if (at != AlarmTimestamp.INVALID) {
 				AlarmPlannerService.call(app, Command.CREATE, at.getAlarm()
 						.getId());
 			}
 		}
 	}
 
 	/**
 	 * Sets screen related flags, reads from preferences.
 	 */
 	private void setScreenFlags() {
 		int flags = this.computeScreenFlags();
 
 		if (flags == 0) {
 			return;
 		}
 
 		this.getWindow().addFlags(flags);
 	}
 
 	private void readPreferences() {
 		GlobalPreferencesReader prefs = SFApplication.get().getPrefs();
 
 		this.turnScreenOn = prefs.turnScreenOn();
 		this.bypassLockscreen = prefs.bypassLockscreen();
 	}
 
 	/**
 	 * Computes screen flags based on preferences.
 	 * 
 	 * @return screen flags.
 	 */
 	private int computeScreenFlags() {
 		readPreferences();
 
 		int flags = 0;
 
 		if (this.turnScreenOn) {
 			flags |= WINDOW_FLAGS_SCREEN_ON;
 		}
 
 		if (this.bypassLockscreen) {
 			flags |= WINDOW_FLAGS_LOCKSCREEN;
 		}
 
 		return flags;
 	}
 
 	/**
 	 * What will happen when you complete a challenge or press back.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Check if result is from a challenge
 		if (requestCode == CHALLENGE_REQUEST_CODE) {
 			if (resultCode == Activity.RESULT_OK) {
 				Toast.makeText(this, "Challenge completed", Toast.LENGTH_LONG)
 						.show();
 				Debug.d("done with challenge");
 
 				// If completed, stop the alarm
 				stopAlarm();
 			} else {
 				Toast.makeText(this, "Returned from uncompleted challenge",
 						Toast.LENGTH_LONG).show();
 			}
 
 		} else {
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 
 	/**
 	 * Stop the current alarm sound and vibration
 	 */
 	public void stopAlarm() {
 		this.stopAudio();
 
 		VibrationManager.getInstance().stopVibrate(getApplicationContext());
 
 		// Remove notification saying alarm is ringing
 		NotificationHelper.removeNotification(this);
 
 		this.performRescheduling();
 		finish();
 	}
 
 	private void stopAudio() {
 		SFApplication.get().setAudioDriver(null);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		timer = new Timer("SFTimer");
 		Calendar calendar = Calendar.getInstance();
 
 		// Get the current time
 		final Runnable updateTask = new Runnable() {
 			public void run() {
 				// Set the current time on the text view
 				tvTime.setText(getCurrentTime());
 			}
 		};
 
 		// Update the user interface
 		int msec = 999 - calendar.get(Calendar.MILLISECOND);
 		timer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				runOnUiThread(updateTask);
 			}
 		}, msec, 1000);
 	}
 
 	/*
 	 * Use to stop the timer and release the camera
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		
 		// Stop the timer
 		timer.cancel();
 		timer.purge();
 		timer = null;
 		
 		// Release the camera
 		if (camera != null) {
 			camera.release();
 		}
 	}
 
 	// Get the current time with the Calendar
 	@SuppressLint("DefaultLocale")
 	public String getCurrentTime() {
 		Calendar cal = Calendar.getInstance();
 		int hour = cal.get(Calendar.HOUR_OF_DAY);
 		int minute = cal.get(Calendar.MINUTE);
 		return String.format("%02d:%02d", hour, minute);
 	}
 }
