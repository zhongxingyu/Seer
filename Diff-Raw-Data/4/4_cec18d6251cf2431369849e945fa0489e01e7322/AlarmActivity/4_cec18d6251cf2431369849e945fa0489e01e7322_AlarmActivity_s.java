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
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.joda.time.DateTime;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.android.utils.DialogUtils;
 import se.chalmers.dat255.sleepfighter.audio.AudioDriver;
 import se.chalmers.dat255.sleepfighter.audio.VibrationManager;
 import se.chalmers.dat255.sleepfighter.helper.NotificationHelper;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.preference.GlobalPreferencesManager;
 import se.chalmers.dat255.sleepfighter.service.AlarmPlannerService;
 import se.chalmers.dat255.sleepfighter.service.AlarmPlannerService.Command;
 import se.chalmers.dat255.sleepfighter.speech.SpeechLocalizer;
 import se.chalmers.dat255.sleepfighter.utils.MetaTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.android.AlarmWakeLocker;
 import se.chalmers.dat255.sleepfighter.utils.android.IntentUtils;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Resources;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * The activity for when an alarm rings/occurs.
  * 
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @author Lam(m)<dannylam@gmail.com> / Danny Lam
  * @version 1.0
  * @since Sep 20, 2013
  */
 @SuppressWarnings("deprecation")
 public class AlarmActivity extends Activity implements TextToSpeech.OnUtteranceCompletedListener {
 
 	public static final String EXTRA_ALARM_ID = "alarm_id";
 
 	public static final int CHALLENGE_REQUEST_CODE = 1;
 
 	private static final int WINDOW_FLAGS_SCREEN_ON = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
 			| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
 			| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
 
 	private static final int WINDOW_FLAGS_LOCKSCREEN = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
 
 	private static final int EMERGENCY_COST = 100;
 	private static final int EMERGENCY_PERCENTAGE_COST = 20;
 	private static final int SNOOZE_COST = 10;
 	private static final int SNOOZE_PERCENTAGE_COST = 5;
 	private static final int CHALLENGE_POINTS_GET = 5;
 
 	private Parameters p;
 	private TextView tvName, tvTime;
 	private Button btnStop, btnSnooze;
 	private Alarm alarm;
 	private Timer timer;
 	private Camera camera;
 	private boolean turnScreenOn = true;
 	private boolean bypassLockscreen = true;
 
 
 	private int originalVolume;
 	
 	private String IS_SPEECH_RUNNING = "is_speech_running";
 	private String ORIGINAL_VOLUME = "original_volume";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Turn and/or keep screen on.
 		this.setScreenFlags();
 		this.setContentView(R.layout.activity_alarm);
 		SFApplication app = SFApplication.get();
 
 		// Fetch alarm Id.
 		int alarmId = new IntentUtils(this.getIntent()).getAlarmId();
 		this.alarm = app.getPersister().fetchAlarmById(alarmId);
 
 		if (alarm.isSpeech() && savedInstanceState == null) {
 			// start no musis until the speech is over. 
 			lowerVolume();
 		}  else {
 			this.originalVolume = savedInstanceState.getInt(ORIGINAL_VOLUME);
 		}
 
 		// Get the name and time of the current ringing alarm
 		tvName = (TextView) findViewById(R.id.tvAlarmName);
 		tvName.setText(MetaTextUtils.printAlarmName(this, alarm));
 		tvTime = (TextView) findViewById(R.id.tvAlarmTime);
 
 		setupStopButton();
 		setupSnoozeButton();
 		setupFooter();
 		
 		
 		
 		if (alarm.isSpeech()) {
 			TextToSpeech tts = SFApplication.get().getTts();
 
 			if(savedInstanceState != null && savedInstanceState.getBoolean(IS_SPEECH_RUNNING, false)) {
 				// speech is already running, no need to start again. 
 			} else {
 				doSpeech(SFApplication.get().getWeather());
 				// TODO: is this correct?
 				SFApplication.get().setWeather(null);
 			}
 			tts.setOnUtteranceCompletedListener(this);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		outState.putBoolean(IS_SPEECH_RUNNING, true);
 	
 		outState.putInt(ORIGINAL_VOLUME,this.originalVolume);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.alarm_activity_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_emergency_stop:
 			handleEmergencyStop();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void setupStopButton() {
 		// Connect the challenge button with XML
 		btnStop = (Button) findViewById(R.id.btnStop);
 		btnStop.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onStopClick();
 			}
 		});
 	}
 
 	private void setupSnoozeButton() {
 		btnSnooze = (Button) findViewById(R.id.btnSnooze);
 
 		if (alarm.getSnoozeConfig().isSnoozeEnabled()) {
 			btnSnooze.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					startSnooze();
 				}
 			});
 		} else {
 			btnSnooze.setVisibility(View.GONE);
 		}
 	}
 
 	private void setupFooter() {
 		boolean usingChallenge = useChallenges();
 		if (usingChallenge) {
 			TextView pointText = (TextView) findViewById(R.id.challenge_points_text);
 
 			String challengePointsStr = this.getResources().getString(
 					R.string.challenge_points);
 			pointText.setText(SFApplication.get().getPrefs()
 					.getChallengePoints()
 					+ " " + challengePointsStr);
 		} else {
 			findViewById(R.id.footer).setVisibility(View.INVISIBLE);
 		}
 	}
 
 	// read out the time and weather.
 	public void doSpeech(String weather) {
 	
 		String s;
 		
 		TextToSpeech tts = SFApplication.get().getTts();
 		
 		
 		// weren't able to obtain any weather.
 		if(weather == null) {
 			s =  new SpeechLocalizer(tts, this).getSpeech();
 		} else {
 			s = new SpeechLocalizer(tts, this).getSpeech(weather);
 		}
 		HashMap<String, String> params = new HashMap<String, String>();
 		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");
 		tts.speak(s, TextToSpeech.QUEUE_FLUSH, params);
 	}
 
 
 	/**
 	 * Handle what happens when the user presses the emergency stop.
 	 */
 	private void handleEmergencyStop() {
 		boolean skippingChallenges = useChallenges();
 
 		if (skippingChallenges) {
 			skipChallengeConfirm();
 		} else {
 			stopAlarm();
 		}
 	}
 
 	/**
 	 * Handles if the user uses emergency stop so that a challenge would be
 	 * skipped by showing confirmation dialog.
 	 */
 	private void skipChallengeConfirm() {
 		final int emergencyCost = Math.max(EMERGENCY_COST, SFApplication.get()
 				.getPrefs().getChallengePoints()
 				/ (100 / EMERGENCY_PERCENTAGE_COST));
 
 		// Show confirmation dialog where the user has to confirm skipping the
 		// challenge, and in turn lose a lot of points
 		DialogInterface.OnClickListener yesAction = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				SFApplication.get().getPrefs()
 						.addChallengePoints(-emergencyCost);
 				stopAlarm();
 			}
 		};
 		Resources res = getResources();
 
 		// Get the correct string with the correct value inserted.
 		DialogUtils.showConfirmationDialog(String.format(res
 				.getString(R.string.alarm_emergency_dialog), res
 				.getQuantityString(R.plurals.alarm_emergency_cost,
 						emergencyCost, emergencyCost)), this, yesAction);
 
 	}
 
 	private void onStopClick() {
 		boolean showChallenge = useChallenges();
 
 		if (showChallenge) {
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
 		new IntentUtils(i).setAlarmId(this.alarm);
 		startActivityForResult(i, CHALLENGE_REQUEST_CODE);
 	}
 
 	/**
 	 * Stops alarm temporarily and sends a snooze command to the server.
 	 */
 	private void startSnooze() {
 		stopAudio();
 
 		VibrationManager.getInstance().stopVibrate(getApplicationContext());
 
 		// Remove notification saying alarm is ringing
 		NotificationHelper.removeNotification(this);
 
 		// Send snooze command to service
 		AlarmPlannerService.call(this, Command.SNOOZE, alarm.getId());
 
 		// Remove some challenge points if skipping challenge
 		boolean skippingChallenge = useChallenges();
 		if (skippingChallenge) {
 			GlobalPreferencesManager prefs = SFApplication.get()
 					.getPrefs();
 
 			int snoozeCost = Math.max(SNOOZE_COST,
 					prefs.getChallengePoints()
 							/ (100 / SNOOZE_PERCENTAGE_COST));
 			prefs.addChallengePoints(-snoozeCost);
 		}
 		
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
 		GlobalPreferencesManager prefs = SFApplication.get().getPrefs();
 
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
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Check if result is from a challenge
 		if (requestCode == CHALLENGE_REQUEST_CODE) {
 			if (resultCode == Activity.RESULT_OK) {
 				Debug.d("done with challenge");
 
 				// If completed, stop the alarm
 				stopAlarm();
 				
 				// Add points
 				SFApplication.get().getPrefs().addChallengePoints(CHALLENGE_POINTS_GET);
 			}
 		}  else {
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
 
 	/**
 	 * Start flash, animation and show the current time on display
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		// Start animation and flash
 		startAnimate();
 		startFlash();
 
 		// Cancel previously started timers
 		cancelTimer();
 		
 		timer = new Timer("SFTimer");
 		Calendar calendar = Calendar.getInstance();
 
 		final Runnable updateTask = new Runnable() {
 			public void run() {
 				// Set the current time on the text view
 				tvTime.setText(getCurrentTime());
 			}
 		};
 
 		// Update the user interface
 		TimerTask timerTask = new TimerTask() {
 			@Override
 			public void run() {
 				runOnUiThread(updateTask);
 			}
 		};
 		timer.scheduleAtFixedRate(timerTask,
 				calendar.get(Calendar.MILLISECOND), 1000);
 	}
 
 	private void cancelTimer() {
 		if (timer != null) {
 			timer.cancel();
 			timer = null;
 		}
 	}
 
 	/**
 	 * Checks various settings to determine if a challenge should be shown when
 	 * trying to stop.
 	 * 
 	 * @return true if a challenge should be shown
 	 */
 	private boolean useChallenges() {
 		boolean challengeEnabled = this.alarm.getChallengeSet().isEnabled();
 
 		boolean globallyEnabled = SFApplication.get().getPrefs()
 				.isChallengesActivated();
 
 		// Checks if any of the individual challenges are enabled
 		Set<ChallengeType> enabledChallenges = this.alarm.getChallengeSet()
 				.getEnabledTypes();
 		boolean anyChallengeEnabled = !enabledChallenges.isEmpty();
 
 		return challengeEnabled && globallyEnabled && anyChallengeEnabled;
 	}
 
 	/*
 	 * Use to stop the timer (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		if (camera != null) {
 			camera.release();
 		}
 
 		cancelTimer();
 	}
 
 	// Get the current time with the Calendar
 	@SuppressLint("DefaultLocale")
 	public String getCurrentTime() {
 		Calendar cal = Calendar.getInstance();
 		int hour = cal.get(Calendar.HOUR_OF_DAY);
 		int minute = cal.get(Calendar.MINUTE);
 		return String.format("%02d:%02d", hour, minute);
 	}
 
 	@Override
 	public void onUtteranceCompleted(String arg0) {
 		// now start playing the music now that the speech is over.
 		Debug.d("utterance completed.");
 		restoreVolume();
 	}
 
 
 	/**
 	 * Start the camera's flashlight if found
 	 */
 	private void startFlash() {
 		
 		// Check if there is any camera. If not found, return nothing.
 		// If found, flash!
 		Context context = this;
 		PackageManager pm = context.getPackageManager();
 
 		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
 			Log.e("err", "No flashlight detected!");
 			return;
 		}else{
 			camera = Camera.open();
 			Log.i("info", "The flashlight is on.");
 			p = camera.getParameters();
 			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
 			camera.setParameters(p);
 		}
 	}
 
 	/**
 	 * Declaring new animations and sets to components
 	 */
 
 	private void startAnimate() {
 
 		// Setting animation
 
 		Animation fadeShort = new AlphaAnimation(1, 0);
 		fadeShort.setDuration(200);
 		fadeShort.setInterpolator(new LinearInterpolator());
 		fadeShort.setRepeatCount(Animation.INFINITE);
 		fadeShort.setRepeatMode(Animation.REVERSE);
 
 		// Set the components with animation
 		tvTime.startAnimation(fadeShort);
 	}
 	
 	private void lowerVolume() {
 		AudioDriver d = SFApplication.get().getAudioDriver();
 		this.originalVolume = d.getVolume();
 		Debug.d("original volume: " + this.originalVolume);
		Debug.d("new: " + 1);
		d.setVolume(1);
 	}
 	
 	private void restoreVolume() {
 		AudioDriver d = SFApplication.get().getAudioDriver();
 		d.setVolume(this.originalVolume);
 	}
 }
