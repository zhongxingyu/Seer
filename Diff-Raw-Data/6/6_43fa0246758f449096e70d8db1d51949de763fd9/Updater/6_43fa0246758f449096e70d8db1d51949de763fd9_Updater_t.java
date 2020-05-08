 package com.tuit.ar.services;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.IBinder;
 import android.widget.Toast;
 
 import com.tuit.ar.R;
import com.tuit.ar.api.Twitter;
 import com.tuit.ar.models.Settings;
 import com.tuit.ar.models.SettingsObserver;
 import com.tuit.ar.models.timeline.Friends;
 import com.tuit.ar.models.timeline.Replies;
 
 public class Updater extends Service implements SettingsObserver {
 	private Timer timer;
 	private Handler handler = new Handler();
 	private Runnable runnable = new Runnable() {
 		@Override
 		public void run() {
 			try {
				Friends.getInstance(Twitter.getInstance().getDefaultAccount()).refresh();
				Replies.getInstance(Twitter.getInstance().getDefaultAccount()).refresh();
 			} catch (Exception e) {
 				Toast.makeText(getApplicationContext(), getString(R.string.unableToFetchTimeline), Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
 
 	public Updater() {
 		super();
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	public void onCreate() {
 		super.onCreate();
 		createTimer();
 		Settings.getInstance().addObserver(this);
 	}
 
 	private void createTimer() {
 		if (timer != null) timer.cancel();
 		SharedPreferences preferences = Settings.getInstance().getSharedPreferences(getApplicationContext());
 		if (!preferences.getBoolean(Settings.AUTOMATIC_UPDATE, Settings.AUTOMATIC_UPDATE_DEFAULT)) {
 			return;
 		}
 		String interval = preferences.getString(Settings.UPDATE_INTERVAL, Settings.UPDATE_INTERVAL_DEFAULT);
 		Integer freq = new Integer(interval);
 		int miliFreq = freq * 1000 * 60;
 		timer = new Timer();
 		timer.scheduleAtFixedRate(
 				new TimerTask() {
 					public void run() {
 						handler.post(runnable);
 					}
 				},
 				miliFreq, miliFreq);
 	}
 
 	public void settingsHasChanged(Settings settings) {
 		this.createTimer();
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		timer.cancel();
 	}
 }
