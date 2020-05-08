 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.modules.activity;
 
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.WindowManager;
 
 /**
  * Base activity for fired alarms.
  * 
  * @author Adrian Bjugrd
  * 
  */
 public abstract class BaseActivationActivity extends Activity {
 
 	private final static String WAKE_LOCK_TAG = "edu.chalmers.dat255.group09.Alarmed.activity.BaseActivationActivity";
 	private WakeLock wakeLock;
 	private AudioManager audioManager;
 	private MediaPlayer mediaPlayer;
 	private Vibrator vibrator;
 	private Timer inputTimer;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		enterFullScreen();
 		initWakeLock();
 		initTouchListener();
 		initServices();
 		startAlarm();
 	}
 
 	private void enterFullScreen() {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		Window window = getWindow();
 		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 	}
 
 	/**
 	 * Sets up wake lock
 	 */
 	private void initWakeLock() {
 		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 
 		wakeLock = powerManager.newWakeLock(
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
 						| PowerManager.FULL_WAKE_LOCK
 						| PowerManager.ACQUIRE_CAUSES_WAKEUP, WAKE_LOCK_TAG);
 
 		wakeLock.acquire();
 	}
 
 	/**
 	 * Sets up touch listener to detect input
 	 */
 	private void initTouchListener() {
 		View v = (View) this.findViewById(android.R.id.content);
 		v.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				inputDetected();
 				return false;
 			}
 		});
 	}
 
 	/**
 	 * Initialises services
 	 */
 	private void initServices() {
 		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		mediaPlayer = new MediaPlayer();
 		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		inputTimer = new Timer();
 	}
 
 	/**
 	 * Start the alarm
 	 */
 	private void startAlarm() {
 		if (getIntent().getBooleanExtra("vibration", false)) {
 			startVibration();
 		}
 		startAudio();
 	}
 
 	/**
 	 * Starts the vibration
 	 */
 	private void startVibration() {
 		long[] vibPattern = { 0, 200, 500 };
 		vibrator.vibrate(vibPattern, 0);
 	}
 
 	/**
 	 * Starts the audio
 	 */
 	private void startAudio() {
 		try {
 			mediaPlayer.setDataSource(this, getAlarmTone());
 			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, getIntent()
 					.getIntExtra("volume", 0), 0);
 			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mediaPlayer.setLooping(true);
 			mediaPlayer.prepare();
 			mediaPlayer.start();
 		} catch (IOException e) {
 			Log.d("Sound", "Sound I/O error");
 		}
 
 	}
 
 	/**
 	 * Method for getting an alarm tone
 	 * 
 	 * @return Returns alarm tone
 	 */
 	private Uri getAlarmTone() {
 		Uri alertTone = Uri.parse(getIntent().getStringExtra("toneuri"));
 		if (alertTone == null) {
 			alertTone = RingtoneManager
 					.getDefaultUri(RingtoneManager.TYPE_ALARM);
 			if (alertTone == null) {
 				alertTone = RingtoneManager
 						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
 			}
 		}
 		return alertTone;
 	}
 
 	/**
 	 * Stops the alarm
 	 */
 	public void stopAlarm() {
 		vibrator.cancel();
 		mediaPlayer.stop();
 		wakeLock.release();
 		finish();
 		overrideTransition();
 	}
 
 	/**
 	 * Makes the transition between views smoother by animating them.
 	 */
 	private void overrideTransition() {
 		int fadeIn = android.R.anim.fade_in;
 		int fadeOut = android.R.anim.fade_out;
 		overridePendingTransition(fadeIn, fadeOut);
 	}
 
 	@Override
 	public void onBackPressed() {
 		// The user is not allowed to go back
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
 			// The user is not allowed to lower the volume
 		}
 		inputDetected();
 		return true;
 	}
 
 	/**
 	 * Increase the alarm audio streams volume one step
 	 */
 	public void increaseVolume() {
 		audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
 				AudioManager.ADJUST_RAISE, 0);
 	}
 
 	/**
 	 * Decrease the alarm audio streams volume one step (if volume greater than
 	 * one)
 	 */
 	public void decreaseVolume() {
 		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 1) {
 			audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
 					AudioManager.ADJUST_LOWER, 0);
 		}
 	}
 
 	/**
 	 * Sets the volume for the alarm audio stream to the desired level.
 	 * 
 	 * @param desiredVol
 	 *            An integer between 0 and 7
 	 */
 	public void setVolume(int desiredVol) {
 		if (desiredVol > 7) {
 			desiredVol = 7;
 		} else if (desiredVol < 1) {
 			desiredVol = 1;
 		}
 		audioManager.setStreamVolume(AudioManager.STREAM_ALARM, desiredVol, 0);
 	}
 
 	/**
 	 * Methd which is run when input is detected
 	 */
 	public void inputDetected() {
 		setVolume(1);
 		refreshTimer(5000);
 	}
 
 	/**
 	 * Refreshes the input timer
 	 * 
 	 * @param millis
 	 *            Amount of seconds until timer runs task
 	 */
 	private void refreshTimer(int millis) {
 		inputTimer.cancel();
 		inputTimer = new Timer();
 		inputTimer.schedule(new InputTimerTask(), millis);
 	}
 
 	/**
 	 * Timer-task that is used for scheduling what happens no input is detected
 	 * for a while
 	 */
 	private class InputTimerTask extends TimerTask {
 		@Override
 		public void run() {
 			increaseVolume();
 
 			refreshTimer(5000);
 		}
 	}
 }
