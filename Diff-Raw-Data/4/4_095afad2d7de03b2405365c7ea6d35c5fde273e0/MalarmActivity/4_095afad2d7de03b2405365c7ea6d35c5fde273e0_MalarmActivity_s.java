 package com.mamewo.malarm;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 
 import com.mamewo.malarm.R;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebView.HitTestResult;
 import android.webkit.WebViewClient;
 import android.widget.*;
 import android.webkit.*;
 import android.net.Uri;
 import android.net.http.*;
 import android.graphics.Bitmap;
 
 public class MalarmActivity extends Activity implements OnClickListener, OnSharedPreferenceChangeListener {
 	/** Called when the activity is first created. */
 	private static final String PACKAGE_NAME = MalarmActivity.class.getPackage().getName();
 	public static final String WAKEUP_ACTION = PACKAGE_NAME + ".WAKEUP_ACTION";
 	public static final String WAKEUPAPP_ACTION = PACKAGE_NAME + ".WAKEUPAPP_ACTION";
 	public static final String SLEEP_ACTION = PACKAGE_NAME + ".SLEEP_ACTION";
 	public static final String DEFAULT_PLAYLIST_PATH = "/sdcard/music/";
 	public static String VERSION = "unknown";
 	
 	protected static final String FILE_SEPARATOR = System.getProperty("file.separator");
 
 	protected static final String WAKEUP_PLAYLIST_FILENAME = "wakeup.m3u";
 	protected static final String SLEEP_PLAYLIST_FILENAME = "sleep.m3u";
 	//copy stop.m4a file to stop native player
 	protected static final String STOP_MUSIC_FILENAME = "stop.m4a";
 
 	protected static String PLAYLIST_PATH = null;
 
 	protected static Playlist WAKEUP_PLAYLIST;
 	protected static Playlist SLEEP_PLAYLIST;
 	
 	public static class MalarmState implements Serializable {
 		private static final long serialVersionUID = 1L;
 		public Calendar _target;
 		public boolean _suspending;
 
 		public MalarmState(Calendar target) {
 			_target = target;
 			_suspending = false;
 		}
 	}
 	
 	private static final Integer DEFAULT_HOUR = new Integer(7);
 	private static final Integer DEFAULT_MIN = new Integer(0);
 	private static Vibrator _vibrator = null;
 	
 	private MalarmState _state = null;
 	private Button _next_button;
 	private TimePicker _time_picker;
 	private TextView _time_label;
 	private WebView _webview;
 //	private WebView _subwebview;
 	private ToggleButton _alarm_button;
 	private Button _set_now_button;
 	
 	@SuppressWarnings("unused")
 	private PhoneStateListener _calllistener;
 	private static boolean PREF_USE_NATIVE_PLAYER;
 	private static boolean PREF_VIBRATE;
 	
 	private static final String _NATIVE_PLAYER_KEY = "nativeplayer";
 	private static final String _PLAYLIST_PATH_KEY = "playlist_path";
 	
 	private static final int DOW_INDEX[] = {
 		Calendar.SUNDAY, 
 		Calendar.MONDAY, 
 		Calendar.TUESDAY, 
 		Calendar.WEDNESDAY, 
 		Calendar.THURSDAY, 
 		Calendar.FRIDAY, 
 		Calendar.SATURDAY, 
 	};
 	
 	public class MyCallListener extends PhoneStateListener {
 		
 		public MyCallListener(MalarmActivity context) {
 			TelephonyManager telmgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
 			telmgr.listen(this, LISTEN_CALL_STATE);
 		}
 
 		public void onCallStateChanged (int state, String incomingNumber) {
 			//TODO: save cursor and play
 			if (state == TelephonyManager.CALL_STATE_RINGING) {
 				Log.i(PACKAGE_NAME, "onCallStateChanged: RINGING");
 				//stop vibration
 				if (_vibrator != null) {
 					_vibrator.cancel();
 				}
 				//native player is OK
 				if (Player.isPlaying()) {
 					//pause
 					Player.pauseMusic();
 				}
 			}
 		}
 	}
 	
 	private static void loadPlaylist() {
 		WAKEUP_PLAYLIST = new M3UPlaylist(PLAYLIST_PATH, WAKEUP_PLAYLIST_FILENAME);
 		SLEEP_PLAYLIST = new M3UPlaylist(PLAYLIST_PATH, SLEEP_PLAYLIST_FILENAME);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.i(PACKAGE_NAME, "onCreate is called");
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		PREF_USE_NATIVE_PLAYER = pref.getBoolean("use_native_player", false);
 		PREF_VIBRATE = pref.getBoolean("vibrate", false);
 		PLAYLIST_PATH = pref.getString("playlist_path", DEFAULT_PLAYLIST_PATH);
 		if (! PLAYLIST_PATH.endsWith(FILE_SEPARATOR)) {
 			PLAYLIST_PATH = PLAYLIST_PATH + FILE_SEPARATOR;
 		}
 		loadPlaylist();
 		_vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 
 		_time_picker = (TimePicker) findViewById(R.id.timePicker1);
 		_time_picker.setIs24HourView(true);
 		if (savedInstanceState != null) {
 			_state = (MalarmState)savedInstanceState.get("state");
 		} else {
 			_state = null;
 		}
 		_next_button = (Button) findViewById(R.id.next_button);
 		_next_button.setOnClickListener(this);
 
 		_set_now_button = (Button) findViewById(R.id.set_now_button);
 		_set_now_button.setOnClickListener(this);
 		
 		_time_label = (TextView) findViewById(R.id.target_time_label);
 		_webview = (WebView)findViewById(R.id.webView1);
 		_alarm_button = (ToggleButton)findViewById(R.id.alarm_button);
 		_alarm_button.setOnClickListener(this);
 		WebSettings config = _webview.getSettings();
 		//to display twitter...
 		config.setDomStorageEnabled(true);
 		config.setJavaScriptEnabled(true);
 		config.setSupportZoom(true);
 		_webview.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				_webview.requestFocus();
 				return false;
 			}
 		});
 
 		final Activity activity = this;
 		_webview.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				Log.i(PACKAGE_NAME, "onPageStart: " + url);
 			}
 			
 			@Override
 			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 				Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
 			}
 			
 			String previous_url = "";
 			@Override
 			public void onLoadResource (WebView view, String url) {
 				//Log.i(PACKAGE_NAME, "loading: " + view.getHitTestResult().getType() + ": " + url);
 				if (url.contains("bijo-linux") && url.endsWith("/")) {
 					HitTestResult result = view.getHitTestResult();
 					//TODO: why same event delivered many times?
 					if (result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE && ! previous_url.equals(url)) {
 						_webview.stopLoading();
 						previous_url = url;
 						//TODO: should be outside?
 						loadWebPage(_webview, url);
 						return;
 					}
 				}
 				//addhoc polling...
 				int height = view.getContentHeight();
 				if ((url.contains("bijint") || url.contains("bijo-linux")) && height > 400) {
 					//TODO: get precise position....
 					if(url.contains("binan") && height > 420) {
 						view.scrollTo(0, 420);
 					} else if (url.contains("bijo-linux") && height > 100) {
 						//TODO: forbid vertical scroll?
 						//TODO: open next page in same tab
 						view.scrollTo(0, 100);
 					} else if (height > 960) {
 						view.scrollTo(0, 960);
 					}
 				}
 			}
 
 			@Override
 			public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
 				Toast.makeText(activity, "SSL error " + error, Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				Log.i(PACKAGE_NAME, "onPageFinshed: " + url);
 			}
 		});
 		
 		//load version
 		InputStream is = null;
 		try {
 			is = getResources().openRawResource(R.raw.app_version);
 			Properties prop = new Properties();
 			prop.load(is);
 			VERSION = prop.getProperty("app.version");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		if (VERSION == null) {
 			VERSION = "unknown";
 		}
 
 		//stop alarm when phone call
 		_calllistener = new MyCallListener(this);
 		if (_vibrator == null) {
 			_vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		Log.i(PACKAGE_NAME, "onPause is called, start JavaScript");
 		super.onResume();
 		//WebView.onResume is hidden, why!?!?
 		_webview.getSettings().setJavaScriptEnabled(true);
 		loadWebPage(_webview);
 
 		if (_time_picker.isEnabled()) {
 			_time_picker.setCurrentHour(DEFAULT_HOUR);
 			_time_picker.setCurrentMinute(DEFAULT_MIN);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		Log.i(PACKAGE_NAME, "onPause is called, stop JavaScript");
 		super.onPause();
 		//stop tokei
 		_webview.getSettings().setJavaScriptEnabled(false);
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		pref.unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onStart () {
 		Log.i(PACKAGE_NAME, "onStart is called");
 		super.onStart();
 
 		if (_state != null) {
 			updateAlarmUI(_state._target);
 		}
 		_alarm_button.setChecked(_state != null);
 		_alarm_button.requestFocus();
 	}
 	
 	//Avoid finishing activity not to lost _state
 	@Override
 	public void onBackPressed() {
 		if (_webview.canGoBack() && _webview.hasFocus()) {
 			_webview.goBack();
 			return;
 		}
 		moveTaskToBack(false);
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		Log.i("malarm", "onSaveInstanceState is called");
 		outState.putSerializable("state", _state);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if (key.equals("use_native_player")) {
 			PREF_USE_NATIVE_PLAYER = sharedPreferences.getBoolean(key, false);
 		} else if (key.equals("vibrate")) {
 			PREF_VIBRATE = sharedPreferences.getBoolean(key, true);
 		} else if (key.equals("playlist_path")) {
 			String newpath = sharedPreferences.getString(key, DEFAULT_PLAYLIST_PATH);
 			if (! newpath.endsWith(FILE_SEPARATOR)) {
 				newpath = newpath + FILE_SEPARATOR;
 			}
 			if (! newpath.equals(PLAYLIST_PATH)) {
 				PLAYLIST_PATH = newpath;
 				loadPlaylist();
 			}
 		}
 	}
 
 	private void loadWebPage(WebView view) {
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		String url = pref.getString("url", "http://twitter.com/");
 		loadWebPage(view, url);
 	}
 	
 	private void loadWebPage(WebView view, String url) {
 		Log.i(PACKAGE_NAME, "loadWebPage: " + url);
 		WebSettings config = _webview.getSettings();
 		if (url.contains("bijo-linux")) {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
 		} else {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
 		}
 		if (url.contains("bijo-linux")) {
 			config.setDefaultZoom(WebSettings.ZoomDensity.FAR);
 		} else {
 			config.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
 		}
 		showMessage(this, "Loading...");
 		_webview.loadUrl(url);
 	}
 	
 	private void clearAlarmUI() {
 		_time_picker.setEnabled(true);
 		_time_label.setText("");
 		_time_picker.setCurrentHour(DEFAULT_HOUR);
 		_time_picker.setCurrentMinute(DEFAULT_MIN);
 	}
 
 	private void cancelAlarm () {
 		Log.i(PACKAGE_NAME, "cancelAlarm");
 		PendingIntent p = makePlayPintent(WAKEUP_ACTION, true);
 		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		clearAlarmUI();
 		_state = null;
 		mgr.cancel(p);
     }
 
 	protected void onNewIntent (Intent intent) {
 		System.out.println ("onNewIntent is called");
 		String action = intent.getAction();
 		if (action != null && action.equals(WAKEUPAPP_ACTION)) {
 			//native player cannot start until lock screen is displayed
 			if (PREF_VIBRATE && _vibrator != null) {
 				long pattern[] = { 10, 1500, 500, 1500, 500, 1500, 500, 1500, 500 };
 				_vibrator.vibrate(pattern, 1);
 			}
 		}
 	}
 	
 	private PendingIntent makePlayPintent(String action, boolean use_native) {
 		Intent i = new Intent(this, Player.class);
 		i.setAction(action);
 		i.putExtra(_NATIVE_PLAYER_KEY, use_native);
 		i.putExtra(_PLAYLIST_PATH_KEY, PLAYLIST_PATH);
 		
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
 				PendingIntent.FLAG_CANCEL_CURRENT);
 		return pendingIntent;
 	}
 
 	//add menu to cancel alarm
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate our menu which can gather user input for switching camera
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.alarm_menu, menu);
         return true;
     }
 
     private void updateAlarmUI (Calendar target) {
     	String dow_str = "";
 		int dow_int = target.get(Calendar.DAY_OF_WEEK);
 		String[] dow_name_table = getResources().getStringArray(R.array.day_of_week);
 		for (int i = 0; i < DOW_INDEX.length; i++) {
 			if (DOW_INDEX[i] == dow_int) {
 				dow_str = dow_name_table[i];
 				break;
 			}
 		}
 		_time_label.setText(String.format("%2d/%2d %02d:%02d (%s)",
 										target.get(Calendar.MONTH)+1,
 										target.get(Calendar.DATE),
 										target.get(Calendar.HOUR_OF_DAY),
 										target.get(Calendar.MINUTE),
 										dow_str));
 		_time_picker.setCurrentHour(target.get(Calendar.HOUR_OF_DAY));
 		_time_picker.setCurrentMinute(target.get(Calendar.MINUTE));
 		_time_picker.setEnabled(false);
     }
     
 	public void setAlarm() {
 		Log.i(PACKAGE_NAME, "scheduleToPlaylist is called");
 		if (_state != null) {
 			cancelAlarm();
 			if (_vibrator != null) {
 				_vibrator.cancel();
 			}
 			if (! PREF_USE_NATIVE_PLAYER) {
 				Player.pauseMusic();
 				//TODO: what's happen if now playing alarm sound?
 				showMessage(this, getString(R.string.music_stopped));
 			}
 			return;
 		}
 		//set timer
 		Calendar now = new GregorianCalendar();
 		int target_hour = _time_picker.getCurrentHour().intValue();
 		int target_min = _time_picker.getCurrentMinute().intValue();
 		Calendar target = new GregorianCalendar(now.get(Calendar.YEAR),
 				now.get(Calendar.MONTH), now.get(Calendar.DATE), target_hour, target_min, 0);
 		long target_millis = target.getTimeInMillis();
 		String tommorow ="";
 		long now_millis = System.currentTimeMillis();
 		if (target_millis <= now_millis) {
 			//tomorrow
 			target_millis += 24 * 60 * 60 * 1000;
 			target.setTimeInMillis(target_millis);
 			tommorow = " (" + getString(R.string.tomorrow) + ")";
 		}
 		_state = new MalarmState(target);
 		updateAlarmUI(target);
 
 		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pendingIntent = makePlayPintent(WAKEUP_ACTION, false);
 		mgr.set(AlarmManager.RTC_WAKEUP, target_millis, pendingIntent);
 
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		int min = Integer.valueOf(pref.getString("sleeptime", "60"));
 		Player.playSleepMusic(this, min);
 		long sleep_time_millis = min * 60 * 1000;
 		//TODO: localize
 		String sleeptime_str = String.valueOf(min) + " min";
 		if (target_millis - now_millis >= sleep_time_millis) {
 			PendingIntent sleepIntent = makePlayPintent(SLEEP_ACTION, PREF_USE_NATIVE_PLAYER);
 			mgr.set(AlarmManager.RTC_WAKEUP, now_millis+sleep_time_millis, sleepIntent);
 		}
 		//TODO: add alarm time as Notification
 		showMessage(this, getString(R.string.alarm_set) + tommorow + " " + sleeptime_str);
 	}
 	public void setNow() {
 		if (_time_picker.isEnabled()) {
 			Calendar now = new GregorianCalendar();
 			_time_picker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
 			_time_picker.setCurrentMinute(now.get(Calendar.MINUTE));
 		}
 	}
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
 //    	case R.id.set_now:
 //    		setNow();
 //    		break;
     	case R.id.stop_vibration:
     		if (_vibrator != null) {
     			_vibrator.cancel();
     		}
     		break;
     	case R.id.play_wakeup:
     		Player.playWakeupMusic(this, PREF_USE_NATIVE_PLAYER);
     		break;
     	case R.id.pref:
     		startActivity(new Intent(this, MyPreference.class));
     		break;
     	case R.id.stop_music:
     		Player.pauseMusic();
     		break;
     	default:
     		Log.i(PACKAGE_NAME, "Unknown menu");
     		return false;
     	}
     	return true;
     }
 
 	public void onClick(View v) {
		//get focus from time picker
		v.requestFocus();
 		if (v == _next_button) {
 			Player.playNext();
 		} else if (v == _alarm_button) {
 			setAlarm();
 		} else if (v == _set_now_button) {
 			setNow();
 		} else {
 			showMessage(v.getContext(), getString(R.string.unknown_button));
 		}
 		InputMethodManager mgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 		mgr.hideSoftInputFromWindow(_time_picker.getWindowToken(), 0);
 	}
 
 	private static void showMessage(Context c, String message) {
 		Toast.makeText(c, message, Toast.LENGTH_LONG).show();
 	}
 
 	//TODO: separate BroadcastReceiver
 	//TODO: implement music player as Service to play long time
 	public static class Player extends BroadcastReceiver {
 		private static Playlist current_playlist = SLEEP_PLAYLIST;
 		private static MediaPlayer _player = null;
 
 		public static boolean isPlaying() {
 			return _player != null && _player.isPlaying();
 		}
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// AppWidgetManager mgr = AppWidgetManager.getInstance(context);
 			Log.i(PACKAGE_NAME, "onReceive: action: " + intent.getAction());
 			if (intent.getAction().equals(WAKEUP_ACTION)) {
 				if (PLAYLIST_PATH == null) {
 					PLAYLIST_PATH = intent.getStringExtra(_PLAYLIST_PATH_KEY);
 				}
 				if (WAKEUP_PLAYLIST == null) {
 					loadPlaylist();
 				}
 
 				Log.i(PACKAGE_NAME, "Wakeup action");
 				if (Player.isPlaying()) {
 					stopMusic();
 				}
 				Player.playWakeupMusic(context, false);
 
 				AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 				//TODO: add volume pref
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 
 				Intent i = new Intent(context, MalarmActivity.class);
 				//show app
 				//TODO: add playlist path
 				i.setAction(WAKEUPAPP_ACTION);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				context.startActivity(i);
 			} else if (intent.getAction().equals(SLEEP_ACTION)) {
 				if (intent.getExtras().getBoolean(_NATIVE_PLAYER_KEY)) {
 					Player.stopMusicNativePlayer(context);
 				} else {
 					Player.pauseMusic();
 				}
 				showMessage(context, context.getString(R.string.goodnight));
 			}
 		}
 
 		public static void stopMusic() {
 			if (_player == null) {
 				return;
 			}
 			_player.stop();
 		}
 
 		public static void playMusicNativePlayer(Context context, File f) {
 			Intent i = new Intent();
 			i.setAction(Intent.ACTION_VIEW);
 			i.setDataAndType(Uri.fromFile(f), "audio/*");
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(i);
 		}
 		
 		public static void stopMusicNativePlayer(Context context) {
 			File f = new File(PLAYLIST_PATH + STOP_MUSIC_FILENAME);
 			if(! f.isFile()) {
 				Log.i(PACKAGE_NAME, "No stop play list is found");
 				return;
 			}
 			playMusicNativePlayer(context, f);
 		}
 
 		public static void playWakeupMusic(Context context, boolean use_native) {
 			File f = new File(WAKEUP_PLAYLIST_FILENAME);
 			if (use_native && f.isFile()) {
 				playMusicNativePlayer(context, f);
 			} else {
 				WAKEUP_PLAYLIST.reset();
 				playMusic(WAKEUP_PLAYLIST);
 			}
 		}
 
 		public static void playSleepMusic(Context context, int min) {
 			Log.i(PACKAGE_NAME, "start sleep music and stop");
 			File f = new File(PLAYLIST_PATH + SLEEP_PLAYLIST_FILENAME);
 			if (PREF_USE_NATIVE_PLAYER && f.isFile()) {
 				Log.i(PACKAGE_NAME, "playSleepMusic: NativePlayer");
 				playMusicNativePlayer(context, f);
 			} else {
 				Log.i(PACKAGE_NAME, "playSleepMusic: MediaPlayer");
 				SLEEP_PLAYLIST.reset();
 				playMusic(SLEEP_PLAYLIST);
 			}
 		}
 
 		/**
 		 * MediaPlayer only
 		 */
 		public static void playNext() {
 			Log.i(PACKAGE_NAME, "playNext is called: ");
 			if (Player.isPlaying()) {
 				stopMusic();
 			}
 			playMusic(current_playlist);
 		}
 
 		public static class MusicCompletionListener implements
 			MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
 			public void onCompletion(MediaPlayer mp) {
 				Log.i(PACKAGE_NAME, "onCompletion listener is called");
 				Player.playNext();
 			}
 
 			// This method is not called when DRM error is occured
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 				// TODO: handle error
 				Log.i(PACKAGE_NAME, "onError is called");
 				return false;
 			}
 		}
 
 		public static void playMusic(Playlist playlist) {
 			current_playlist = playlist;
 			Log.i(PACKAGE_NAME, "playMusic");
 			if (playlist == null || playlist.isEmpty()) {
 				Log.i(PACKAGE_NAME, "playMusic: playlist is null");
 				return;
 			}
 			if (_player == null) {
 				_player = new MediaPlayer();
 				MusicCompletionListener l = new MusicCompletionListener();
 				_player.setOnCompletionListener(l);
 				_player.setOnErrorListener(l);
 			}
 			if (_player.isPlaying()) {
 				return;
 			}
 			String path = "";
 			//skip unsupported files filtering by filename ...
 			for (int i = 0; i < 10; i++) {
 				path = playlist.next();
 				File f = new File(path);
 				// ....
 				if ((!path.endsWith(".m4p")) && f.exists()) {
 					break;
 				}
 			}
 			// TODO: handle error
 			try {
 				_player.reset();
 				_player.setDataSource(path);
 				_player.prepare();
 				_player.start();
 			} catch (IOException e) {
 				//do nothing
 			}
 		}
 		
 		public static void playMusic() {
 			Log.i(PACKAGE_NAME, "playMusic (from pause) is called");
 			if (current_playlist == null) {
 				return;
 			}
 			try {
 				_player.start();
 			} catch (Exception e) {
 				
 			}
 		}
 		
 		public static void pauseMusic() {
 			Log.i(PACKAGE_NAME, "pause music is called");
 			try {
 				_player.pause();
 			} catch (Exception e) {
 				
 			}
 		}
 	}
 }
