 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package net.filiph.mothership;
 
 import java.util.Date;
 
 import net.filiph.mothership.gcm.CommonUtilities;
 import net.filiph.mothership.gcm.ServerUtilities;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnSeekCompleteListener;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View.MeasureSpec;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 import com.google.android.gcm.GCMRegistrar;
 
 /**
  * This is the main activity, meaning the UI screen.
  */
 @TargetApi(8)
 public class MainActivity extends Activity {
 
 	private static final String TAG = "motherShip MainActivity";
 
 	private UpdateReceiver updateReceiver;
 	final private Handler typeHandler = new Handler();
 	AsyncTask<Void, Void, Void> mRegisterTask;
 
 	public MainActivity() {
 	}
 
 	/** Called with the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Inflate our UI from its XML layout description.
 		setContentView(R.layout.main_activity);
 
 		// TODO: only do if there is no alarm set
 		AlarmReceiver.setAlarmForNextMessage(getBaseContext());
 
 		setupGCM();
 	}
 
 	VideoView vv = null;
 
 	/**
 	 * Called when the activity is about to start interacting with the user.
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		showCurrentMessage(TYPING_DEFAULT);
 
 		updateReceiver = new UpdateReceiver(this);
 		registerReceiver(updateReceiver, new IntentFilter("net.filiph.mothership.NEW_MOTHERSHIP_MESSAGE"));
 
 		if (vv == null) {
 			// getWindow().setFormat(PixelFormat.TRANSLUCENT);
 			vv = (VideoView) findViewById(R.id.videoView1);
 			// video from
 			// http://www.istockphoto.com/stock-video-17986614-data-servers-loopable-animation.php
 			Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.servers);
 
 			// this horrible spaghetti is responsible for showing the background
 			// video
 			// with as few flicker as possible
 			try {
 				vv.setVideoURI(video);
 				vv.setKeepScreenOn(false);
 
 				// we can only access the MediaPlaye instance after video is
 				// Prepared
 				vv.setOnPreparedListener(new OnPreparedListener() {
 					@Override
 					public void onPrepared(MediaPlayer mp) {
 						mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
 							@Override
 							public void onSeekComplete(MediaPlayer mp) {
 								mp.start();
 
 								// give the player 100 milliseconds to start
 								// playing
 								// then hide the placeholder static image
 								new Handler().postDelayed(new Runnable() {
 									@Override
 									public void run() {
 										ScrollView sv = (ScrollView) findViewById(R.id.scrollView);
 										sv.setBackgroundResource(0);
 									}
 								}, 100);
 							}
 						});
 						mp.setLooping(true);
 						mp.seekTo(0);
 					}
 				});
 
 				vv.setOnErrorListener(new OnErrorListener() {
 					@Override
 					public boolean onError(MediaPlayer mp, int what, int extra) {
 						Log.e(TAG, "Error with video playback. Reverting to static image.");
 
 						// something went wrong, fall back to static image
 						vv.suspend();
 						vv = null;
 						ScrollView sv = (ScrollView) findViewById(R.id.scrollView);
 						sv.setBackgroundResource(R.drawable.servers);
 
 						return true; // don't report
 					}
 				});
 
 				// stretch the video full view
 				Display display = getWindowManager().getDefaultDisplay();
 				int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(display.getWidth(), MeasureSpec.UNSPECIFIED);
 				int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(display.getHeight(), MeasureSpec.UNSPECIFIED);
 				vv.measure(childWidthMeasureSpec, childHeightMeasureSpec);
 			} catch (Exception e) {
 				Log.e(TAG, "Error with background video, falling back to static background.");
 				e.printStackTrace();
 
 				// something went wrong, fall back to static image
 				vv.suspend();
 				vv = null;
 				ScrollView sv = (ScrollView) findViewById(R.id.scrollView);
 				sv.setBackgroundResource(R.drawable.servers);
 			}
 		}
 
 		if (vv != null && vv.canSeekBackward() && vv.canSeekForward()) {
 			vv.seekTo(0);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		if (vv != null && vv.canPause()) {
 			vv.pause();
 			// vv.suspend();
 		}
 
 		unregisterReceiver(updateReceiver); // don't update the activity when
 											// paused
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (mRegisterTask != null) {
 			mRegisterTask.cancel(true);
 		}
 		unregisterReceiver(mHandleMessageReceiver);
 		GCMRegistrar.onDestroy(getApplicationContext());
 		super.onDestroy();
 	}
 	
 	@Override
 	public void onBackPressed()
 	{
 		// the new messages may create steps in activity stack
 		// this makes sure that pressing the back button exits the activity
         finish(); 
 	}
 
 	private int funnyRemarkIndex = 0;
 
 	/**
 	 * Clicking the menu button prints funny messages.
 	 */
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		final MainActivity mainActivity = this;
 		final TextView t = (TextView) findViewById(R.id.textView);
 		final TextView signature = (TextView) findViewById(R.id.signature);
 
 		t.setText("");
 		signature.setText("");
 
 		String[] funnyRemarks = getResources().getStringArray(R.array.menu_mothership_funny_remarks);
 		final String str = funnyRemarks[funnyRemarkIndex];
 		if (funnyRemarkIndex < funnyRemarks.length - 1) {
 			funnyRemarkIndex++;
 		}
 
 		typeHandler.removeCallbacksAndMessages(null);
 
 		typeHandler.postDelayed(new Runnable() {
 			int index = 0;
 
 			@Override
 			public void run() {
 				// skip insides of HTML tags
 				if (index < str.length() && str.charAt(index) == '<') {
 					int closingIndex = str.indexOf('>', index);
 					if (closingIndex > index)
 						index = closingIndex;
 				}
 				t.setText(Html.fromHtml((String) str.subSequence(0, index++)));
 				if (index <= str.length()) {
 					typeHandler.postDelayed(this, 10);
 				} else {
 					typeHandler.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							mainActivity.showCurrentMessage(TYPING_FORCE_SHOW);
 						}
 					}, 5000);
 				}
 			}
 		}, 10);
 		return false;
 	}
 
 	public static final String PREFS_NAME = "MothershipPrefs";
 
 	public static final int TYPING_DEFAULT = 0;
 	public static final int TYPING_FORCE_SHOW = 1;
 
 	/**
 	 * Checks which message should be shown. If that particular message has been
 	 * already shown, just make sure it's showing again (with setText). If it's
 	 * a new message, type it out ("typewriter effect").
 	 * 
 	 * @param typingOption
 	 *            When set to TYPING_FORCE_SHOW, the message will be typed out
 	 *            no matter if it's been already shown or not. Default is
 	 *            TYPING_DEFAULT.
 	 */
 	public void showCurrentMessage(int typingOption) {
 		int currentUid = getSharedPreferences(PREFS_NAME, 0).getInt("currentUid", 0);
 		Message currentMessage = Schedule.getCurrentMessage(currentUid);
 		
		if (currentMessage == null) {
			currentMessage = new Message(Schedule.date(2012, 10, 7, 16, 3), "...");
		}
		
 		// check if we ought to be showing a message sent via GCM
 		long lastGcmMessageTime = getSharedPreferences(PREFS_NAME, 0).getLong("gcmMessageTime", 0);
 		if (lastGcmMessageTime > currentMessage.time.getTime()) {
 			currentMessage = new Message(new Date(lastGcmMessageTime), 
 					getSharedPreferences(PREFS_NAME, 0).getString("gcmMessageString", ""));
 		}
 		
 		showMessage(typingOption, currentMessage);
 	}
 
 	/**
 	 * Shows new message either from saved messages or GCM.
 	 * 
 	 * @param typingOption
 	 *            When set to TYPING_FORCE_SHOW, the message will be typed out
 	 *            no matter if it's been already shown or not. Default is
 	 *            TYPING_DEFAULT.
 	 * @param message
 	 *            Message to be shown.
 	 */
 	private void showMessage(int typingOption, final Message message) {
 		if (message != null) {
 			int currentUid = getSharedPreferences(PREFS_NAME, 0).getInt("currentUid", 0);
 			boolean isNewMessage = message.uid != currentUid;
 			Log.v(TAG, "Saving new currentUid = " + message.uid);
 			getSharedPreferences(PREFS_NAME, 0).edit().putInt("currentUid", message.uid).commit();
 
 			final TextView t = (TextView) findViewById(R.id.textView);
 			final TextView signature = (TextView) findViewById(R.id.signature);
 
 			// make sure system takes care of links in messages
 			t.setMovementMethod(LinkMovementMethod.getInstance());
 
 			if (isNewMessage || typingOption == TYPING_FORCE_SHOW) {
 				t.setText("");
 				signature.setText("");
 				final Animation fadeinAniDelayed = AnimationUtils.loadAnimation(this, R.anim.fade_in_delayed);
 
 				// typewriter effect
 				typeHandler.removeCallbacksAndMessages(null);
 				typeHandler.postDelayed(new Runnable() {
 					int index = 0;
 					String str = message.text;
 
 					@Override
 					public void run() {
 						// skip insides of HTML tags
 						if (index < str.length()) {
 							while (index < str.length() && str.charAt(index) == '<') {
 								int closingIndex = str.indexOf('>', index);
 								if (closingIndex > index)
 									index = closingIndex + 1;
 							}
 							if (index < str.length()) {
 								t.append(str.substring(index,index+1));
 								index++;
 							}
 							if (index <= str.length()) {
 								typeHandler.postDelayed(this, 30);
 							}
 						} else {
 							// now add with all markup, too
 							t.setText(Html.fromHtml((String) str));
 							// the small print should just fade in
 							signature.startAnimation(fadeinAniDelayed);
 							signature.setText(message.getTimeString());
 						}
 					}
 				}, 10);
 			} else {
 				// no typewriter effect
 				if (!typeHandler.hasMessages(0)) {
 					t.setText(Html.fromHtml(message.text));
 					signature.setText(message.getTimeString());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets up Google Cloud Messaging and registers with server if necessary.
 	 */
 	private void setupGCM() {
 		// Make sure the device has the proper dependencies.
 		GCMRegistrar.checkDevice(this);
 		// Make sure the manifest was properly set - comment out this line
 		// while developing the app, then uncomment it when it's ready.
 		GCMRegistrar.checkManifest(this);
 		registerReceiver(mHandleMessageReceiver, new IntentFilter(CommonUtilities.DISPLAY_MESSAGE_ACTION));
 		final String regId = GCMRegistrar.getRegistrationId(this);
 		if (regId.equals("")) {
 			// Automatically registers application on startup.
 			GCMRegistrar.register(getApplicationContext(), CommonUtilities.SENDER_ID);
 		} else {
 			// Device is already registered on GCM, check server.
 			if (GCMRegistrar.isRegisteredOnServer(this)) {
 				// Skips registration.
 				Log.i(TAG, "Skipping registration");
 			} else {
 				// Try to register again, but not in the UI thread.
 				// It's also necessary to cancel the thread onDestroy(),
 				// hence the use of AsyncTask instead of a raw thread.
 				final Context context = getApplicationContext();
 				mRegisterTask = new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 						boolean registered = ServerUtilities.register(context, regId);
 						// At this point all attempts to register with the app
 						// server failed, so we need to unregister the device
 						// from GCM - the app will try to register again when
 						// it is restarted. Note that GCM will send an
 						// unregistered callback upon completion, but
 						// GCMIntentService.onUnregistered() will ignore it.
 						if (!registered) {
 							GCMRegistrar.unregister(context);
 						}
 						return null;
 					}
 
 					@Override
 					protected void onPostExecute(Void result) {
 						mRegisterTask = null;
 					}
 
 				};
 				mRegisterTask.execute(null, null, null);
 			}
 		}
 	}
 
 	/**
 	 * Handles messages from GCM.
 	 */
 	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String newMessage = intent.getExtras().getString(CommonUtilities.EXTRA_MESSAGE);
 			Message message = new Message(new Date(), newMessage);
 			showMessage(TYPING_DEFAULT, message);
 
 			// put the newest message into a pref so we show it even after resuming the app
 			getSharedPreferences(PREFS_NAME, 0).edit()
 				.putString("gcmMessageString", newMessage)
 				.putLong("gcmMessageTime", new Date().getTime())
 				.commit();
 		}
 	};
 }
