 package com.mamewo.malarm24;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  * http://www002.upp.so-net.ne.jp/mamewo/
  */
 
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.speech.RecognizerIntent;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.GestureDetector;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 import android.webkit.*;
 import android.net.Uri;
 import android.graphics.Bitmap;
 
 final
 public class MalarmActivity
 	extends Activity
 	implements OnClickListener,
 		OnSharedPreferenceChangeListener,
 		OnLongClickListener,
 		OnKeyListener,
 		ServiceConnection,
 		MalarmPlayerService.PlayerStateListener
 {
 	final static
 	public String PACKAGE_NAME = MalarmActivity.class.getPackage().getName();
 	final static
 	public String LOADWEB_ACTION = PACKAGE_NAME + ".LOADWEB_ACTION";
 	final static
 	public String PLAY_ACTION = PACKAGE_NAME + ".PLAY_ACTION";
 	final static
 	private String TAG = "malarm";
 	final static
 	private String MYURL = "http://www002.upp.so-net.ne.jp/mamewo/mobile_shop.html";
 	final static
 	private int SPEECH_RECOGNITION_REQUEST_CODE = 2121;
 	final static
 	private String NATIVE_PLAYER_KEY = "nativeplayer";
 	final static
 	private Pattern TIME_PATTERN = Pattern.compile("(\\d+)時((\\d+)分|半)?");
 	final static
 	private Pattern AFTER_TIME_PATTERN = Pattern.compile("((\\d+)時間)?((\\d+)分|半)?.*");
 
 	protected static String prefPlaylistPath;
 	private static String[] WEB_PAGE_LIST = new String []{ MYURL };
 	private static boolean prefUseNativePlayer;
 	private static Integer prefDefaultHour;
 	private static Integer prefDefaultMin;
 	private static MalarmState state_;
 
 	private ImageButton speechButton_;
 	private ImageButton nextButton_;
 	private TimePicker timePicker_;
 	private TextView timeLabel_;
 	private WebView webview_;
 	private ToggleButton alarmButton_;
 	private Button setNowButton_;
 	private GestureDetector gd_;
 	private boolean setDefaultTime_;
 	private Intent speechIntent_;
 	private ProgressBar loadingIcon_;
 	private boolean startingSpeechActivity_;
 //	private TextView playlistLabel_;
 	private TextView sleepTimeLabel_;
 	private MalarmPlayerService player_ = null;
 	private SharedPreferences pref_;
 	private PhoneStateListener callListener_;
 
 	private static final int DOW_INDEX[] = {
 		Calendar.SUNDAY, 
 		Calendar.MONDAY, 
 		Calendar.TUESDAY, 
 		Calendar.WEDNESDAY, 
 		Calendar.THURSDAY, 
 		Calendar.FRIDAY, 
 		Calendar.SATURDAY, 
 	};
 
 	public final static class MalarmState
 		implements Serializable
 	{
 		private static final long serialVersionUID = 1L;
 		public Calendar targetTime_;
 		public int webIndex_;
 		public int sleepMin_;
 		
 		public MalarmState() {
 			webIndex_ = 0;
 			targetTime_ = null;
 			sleepMin_ = 0;
 		}
 	}
 	
 	public final class MyCallListener
 		extends PhoneStateListener
 	{
 		private boolean playing_ = false;
 
 		public MyCallListener(MalarmActivity context) {
 			super();
 			TelephonyManager telmgr =
 					(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
 			telmgr.listen(this, LISTEN_CALL_STATE);
 		}
 
 		public void onCallStateChanged (int state, String incomingNumber) {
 			switch (state) {
 			case TelephonyManager.CALL_STATE_RINGING:
 				//fall-through
 			case TelephonyManager.CALL_STATE_OFFHOOK:
 				Log.d(TAG, "onCallStateChanged: RINGING");
 				player_.stopVibrator();
 				//native player stops automatically
 				playing_ = player_.isPlaying();
 				if (playing_) {
 					player_.pauseMusic();
 				}
 				break;
 			case TelephonyManager.CALL_STATE_IDLE:
 				//TODO: play music
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	private class WebViewDblTapListener 
 		extends GestureDetector.SimpleOnGestureListener
 	{
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			int x = (int)e.getX();
 			int y = (int)e.getY();
 			Log.d(TAG, "onDoubleTap: " + x + ", " + y);
 			int width = webview_.getWidth();
 			boolean start_browser = false;
 			int side_width = width/3;
 			if (x <= side_width) {
 				state_.webIndex_--;
 			}
 			else if (x > width - side_width) {
 				state_.webIndex_++;
 			}
 			else {
 				start_browser = true;
 			}
 			if (start_browser) {
 				String url = webview_.getUrl();
 				if(null == url) {
 					return false;
 				}
 				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 				startActivity(i);
 			}
 			else {
 				loadWebPage();
 			}
 			return true;
 		}
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		PreferenceManager.setDefaultValues(this, R.xml.preference, false);
 		pref_ =
 			PreferenceManager.getDefaultSharedPreferences(this);
 		pref_.registerOnSharedPreferenceChangeListener(this);
 		syncPreferences(pref_, "ALL");
 		setContentView(R.layout.main);
 		setDefaultTime_ = true;
 		Intent intent = new Intent(this, MalarmPlayerService.class);
 		startService(intent);
 		//TODO: handle failure of bindService
 		boolean result = bindService(intent, this, Context.BIND_AUTO_CREATE);
 		Log.d(TAG, "bindService: " + result);
 		
 		timePicker_ = (TimePicker) findViewById(R.id.timePicker1);
 		timePicker_.setIs24HourView(true);
 
 		if (null == savedInstanceState) {
 			state_ = new MalarmState();
 		}
 		else {
 			state_ = (MalarmState)savedInstanceState.get("state");
 		}
 		loadingIcon_ = (ProgressBar) findViewById(R.id.loading_icon);
 		loadingIcon_.setOnLongClickListener(this);
 		
 //		playlistLabel_ = (TextView) findViewById(R.id.playlist_name_view);
 //		playlistLabel_.setOnLongClickListener(this);
 		
 		speechButton_ = (ImageButton) findViewById(R.id.set_by_voice);
 		speechButton_.setOnClickListener(this);
 		startingSpeechActivity_ = false;
 		
 		nextButton_ = (ImageButton) findViewById(R.id.next_button);
 		nextButton_.setOnClickListener(this);
 		nextButton_.setOnLongClickListener(this);
 		
 		setNowButton_ = (Button) findViewById(R.id.set_now_button);
 		setNowButton_.setOnClickListener(this);
 		
 		timeLabel_ = (TextView) findViewById(R.id.target_time_label);
 		sleepTimeLabel_ = (TextView) findViewById(R.id.sleep_time_label);
 		
 		webview_ = (WebView)findViewById(R.id.webView1);
 		alarmButton_ = (ToggleButton)findViewById(R.id.alarm_button);
 		alarmButton_.setOnClickListener(this);
 		alarmButton_.setLongClickable(true);
 		alarmButton_.setOnLongClickListener(this);
 		//umm...
 		alarmButton_.setOnKeyListener(this);
 
 		CookieSyncManager.createInstance(this);
 		
 		//umm...
 		webview_.setOnKeyListener(this);
 		WebSettings webSettings = webview_.getSettings();
 		//to display twitter...
 		webSettings.setDomStorageEnabled(true);
 		webSettings.setJavaScriptEnabled(true);
 		webSettings.setSupportZoom(true);
 		webview_.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				webview_.requestFocus();
 				gd_.onTouchEvent(event);
 				return false;
 			}
 		});
 		
 		final Activity activity = this;
 		webview_.setWebChromeClient(new WebChromeClient());
 		webview_.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				loadingIcon_.setVisibility(View.VISIBLE);
 			}
 
 			@Override
 			public void onReceivedError(WebView view, int errorCode,
 										String description, String failingUrl) {
 				Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			public void onLoadResource (WebView view, String url) {
 				//addhoc polling...
 				//TODO: move to resource
 				//TODO: automate scrolling
 				int height = view.getContentHeight();
 				if ((url.contains("bijint") ||
 						url.contains("bijo-linux")) && height > 400) {
 					if(url.contains("binan") && height > 420) {
 						view.scrollTo(0, 420);
 					}
 					else if (url.contains("bijo-linux") && height > 100) {
 						view.scrollTo(310, 740);
 					}
 					else if (height > 400) {
 						//bijin tokei
 						view.scrollTo(0, 400);
 					}
 				}
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				loadingIcon_.setVisibility(View.INVISIBLE);
 				if(url.contains("weather.yahoo")) {
 					view.scrollTo(0, 180);
 				}
 			}
 		});
 
 		//stop alarm when phone call
 		callListener_ = new MyCallListener(this);
 		gd_ = new GestureDetector(this, new WebViewDblTapListener());
 	}
 	
 	@Override
 	public void onDestroy() {
 		Log.d(TAG, "onDestroy");
 		if (! player_.isPlaying()) {
 			Intent intent = new Intent(this, MalarmPlayerService.class);
 			stopService(intent);
 		}
 		unbindService(this);
 		SharedPreferences pref = 
 			PreferenceManager.getDefaultSharedPreferences(this);
 		pref.unregisterOnSharedPreferenceChangeListener(this);
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		startingSpeechActivity_ = false;
 		
 		alarmButton_.requestFocus();
 		//WebView.onResume is hidden, why!?!?
 		webview_.getSettings().setJavaScriptEnabled(true);
 		CookieSyncManager.getInstance().startSync();
 		loadWebPage();
 		updateUI();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		//stop tokei
 		webview_.getSettings().setJavaScriptEnabled(false);
 		webview_.stopLoading();
 		CookieSyncManager.getInstance().stopSync();
 	}
 
 	@Override
	protected void onStart () {
 		super.onStart();
 	}
 	
 	@Override
 	protected void onStop(){
 		super.onStop();
 	}
 
 	//Avoid finishing activity not to lost _state
 	@Override
 	public void onBackPressed() {
 		if (webview_.canGoBack() && webview_.hasFocus()) {
 			webview_.goBack();
 			return;
 		}
 		//this activity is not quit by back button
 		//to quit, state of timer should be saved and restored
 		moveTaskToBack(false);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		Log.d("malarm", "onSaveInstanceState is called");
 		outState.putSerializable("state", state_);
 	}
 
 	//escape preference value into static value
 	//TODO: improve design
 	public void syncPreferences(SharedPreferences pref, String key) {
 		boolean updateAll = "ALL".equals(key);
 		if (updateAll || "default_time".equals(key)) {
 			String timestr =
 				pref.getString(MalarmPreference.PREFKEY_WAKEUP_TIME,
 						MalarmPreference.DEFAULT_WAKEUP_TIME);
 			String[] split_timestr = timestr.split(":");
 			if (split_timestr.length == 2) {
 				prefDefaultHour = Integer.valueOf(split_timestr[0]);
 				prefDefaultMin = Integer.valueOf(split_timestr[1]);
 			}
 		}
 		if (updateAll || "url_list".equals(key)) {
 			String liststr = pref.getString(MalarmPreference.PREFKEY_URL_LIST,
 					MalarmPreference.DEFAULT_WEB_LIST);
 			if(0 < liststr.length()){
 				liststr += MultiListPreference.SEPARATOR;
 			}
 			liststr += MYURL;
 			WEB_PAGE_LIST = liststr.split(MultiListPreference.SEPARATOR);
 		}
 		if (updateAll || "use_native_player".equals(key)) {
 			prefUseNativePlayer =
 					pref.getBoolean(MalarmPreference.PREFKEY_USE_NATIVE_PLAYER, false);
 		}
 		if (updateAll || "playlist_path".equals(key)) {
 			String newpath = 
 				pref.getString(key, MalarmPreference.DEFAULT_PLAYLIST_PATH.getAbsolutePath());
 			if (! newpath.equals(prefPlaylistPath)) {
 				prefPlaylistPath = newpath;
 				if (null != player_) {
 					player_.loadPlaylist();
 				}
 			}
 		}
 		Log.d(TAG, "syncPref: key " + key);
 		if("clear_webview_cache".equals(key)){
 			webview_.clearCache(true);
 			webview_.clearHistory();
 			webview_.clearFormData();
 			CookieManager mgr = CookieManager.getInstance();
 			mgr.removeAllCookie();
 			showMessage(this, getString(R.string.webview_cache_cleared));
 		}
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
 		syncPreferences(pref, key);
 	}
 
 	private void loadWebPage() {
 		if (state_.webIndex_ < 0) {
 			state_.webIndex_ = WEB_PAGE_LIST.length - 1;
 		}
 		if (state_.webIndex_ >= WEB_PAGE_LIST.length) {
 			state_.webIndex_ = 0;
 		}
 		String url = WEB_PAGE_LIST[state_.webIndex_];
 		loadWebPage(url);
 	}
 
 	//TODO: move to resource
 	private void adjustWebviewSetting(String url) {
 		WebSettings config = webview_.getSettings();
 		if (url.contains("bijo-linux") || 
 			url.contains("google") ||
 			url.contains("yahoo") ||
 			url.contains("so-net")) {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
 		}
 		else {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
 		}
 		if (url.contains("bijo-linux") || url.contains("bijin-tokei")) {
 			config.setDefaultZoom(WebSettings.ZoomDensity.FAR);
 		}
 		else {
 			config.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
 		}
 	}
 	
 	private void loadWebPage(String url) {
 		showMessage(this, "Loading... \n" + url);
 		adjustWebviewSetting(url);
 		webview_.loadUrl(url);
 	}
 	
 	/**
 	 * call updateUI from caller
 	 */
 	private void cancelAlarmTimer() {
 		if (null == state_.targetTime_) {
 			return;
 		}
 		PendingIntent p =
 				makePlayPintent(MalarmPlayerService.WAKEUP_ACTION, false);
 		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		mgr.cancel(p);
 		state_.targetTime_ = null;
 	}
 	
 	/**
 	 * call updateUI from caller
 	 */
 	private void cancelSleepTimer() {
 		if (state_.sleepMin_ == 0) {
 			return;
 		}
 		PendingIntent sleep =
 			makePlayPintent(MalarmPlayerService.SLEEP_ACTION, false);
 		AlarmManager mgr =
 			(AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		mgr.cancel(sleep);
 		state_.sleepMin_ = 0;
 	}
 
 	//onResume is called after this method is called
 	//TODO: call setNewIntent and handle in onResume?
 	//TODO: this method is not called until home button is pressed
 	protected void onNewIntent (Intent intent) {
 		String action = intent.getAction();
 		Log.d(TAG, "onNewIntent is called: " + action);
 		if (null == action) {
 			return;
 		}
 		//TODO: modify action name
 		if (MalarmPlayerService.WAKEUP_ACTION.equals(action)) {
 			//native player cannot start until lock screen is displayed
 			if (state_.sleepMin_ > 0) {
 				state_.sleepMin_ = 0;
 			}
 		}
 		else if (LOADWEB_ACTION.equals(action)) {
 			String url = intent.getStringExtra("url");
 			loadWebPage(url);
 		}
 		else if (PLAY_ACTION.equals(action)) {
 			//sleep | wakeup
 			String playlist = intent.getStringExtra("playlist");
 			int pos = intent.getIntExtra("position", 0);
 			//TODO: 
 			Playlist list;
 			if(null == playlist || "wakeup".equals(playlist)){
 				list = MalarmPlayerService.wakeupPlaylist_;
 			}
 			else {
 				list = MalarmPlayerService.sleepPlaylist_;
 			}
 			player_.playMusic(list, pos, null == state_.targetTime_);
 		}
 	}
 
 	//TODO: design
 	private PendingIntent makePlayPintent(String action, boolean useNative) {
 		Intent i = new Intent(this, MalarmPlayerService.Receiver.class);
 		i.setAction(action);
 		i.putExtra(NATIVE_PLAYER_KEY, useNative);
 		
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
 				PendingIntent.FLAG_CANCEL_CURRENT);
 		return pendingIntent;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.alarm_menu, menu);
 		return true;
 	}
 
 	private String dateStr(Calendar target) {
 		String dow_str = "";
 		int dow_int = target.get(Calendar.DAY_OF_WEEK);
 		String[] dow_name_table = getResources().getStringArray(R.array.day_of_week);
 		for (int i = 0; i < DOW_INDEX.length; i++) {
 			if (DOW_INDEX[i] == dow_int) {
 				dow_str = dow_name_table[i];
 				break;
 			}
 		}
 		return String.format("%2d/%2d %02d:%02d (%s)",
 				target.get(Calendar.MONTH)+1,
 				target.get(Calendar.DATE),
 				target.get(Calendar.HOUR_OF_DAY),
 				target.get(Calendar.MINUTE),
 				dow_str);
 	}
 
 	private void updateUI () {
 		Calendar target = state_.targetTime_;
 		if(null != target) {
 			//alarm is not set
 			timeLabel_.setText(dateStr(target));
 			timePicker_.setCurrentHour(target.get(Calendar.HOUR_OF_DAY));
 			timePicker_.setCurrentMinute(target.get(Calendar.MINUTE));
 		}
 		else {
 			timeLabel_.setText("");
 			if (setDefaultTime_) {
 				timePicker_.setCurrentHour(prefDefaultHour);
 				timePicker_.setCurrentMinute(prefDefaultMin);
 			}
 			else {
 				setDefaultTime_ = true;
 			}
 		}
 		int sleepMin = state_.sleepMin_;
 		if(sleepMin > 0) {
 			sleepTimeLabel_.setText(MessageFormat.format(getString(R.string.unit_min), 
 								Integer.valueOf(sleepMin)));
 		}
 		else {
 			sleepTimeLabel_.setText("");
 		}
 		alarmButton_.setChecked(null != state_.targetTime_);
 		//following two buttons can be hidden
 		speechButton_.setEnabled(null == state_.targetTime_);
 		setNowButton_.setEnabled(null == state_.targetTime_);
 
 		timePicker_.setEnabled(null == state_.targetTime_);
 		//TODO: add function to get player state
 		//playlistLabel_.setText(Player.getCurrentPlaylistName());
 	}
 
 	private void stopAlarm() {
 //		NotificationManager mgr = 
 //			(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 //		mgr.cancel(PACKAGE_NAME, 0);
 		player_.clearNotification();
 		cancelSleepTimer();
 		cancelAlarmTimer();
 		updateUI();
 		player_.stopVibrator();
 		if (! prefUseNativePlayer) {
 			player_.stopMusic();
 			showMessage(this, getString(R.string.music_stopped));
 		}
 	}
 	
 //	private void setNotification(String title, String text) {
 //		Notification note =
 //			new Notification(R.drawable.img, title, System.currentTimeMillis());
 //		
 //		Intent ni = new Intent(this, MalarmActivity.class);
 //		PendingIntent npi = PendingIntent.getActivity(this, 0, ni, 0);
 //		note.setLatestEventInfo(this, title, text, npi);
 //		NotificationManager notify_mgr =
 //			(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 //		notify_mgr.notify(PACKAGE_NAME, 0, note);
 //	}
 	
 	private void setSleepTimer() {
 		SharedPreferences pref =
 			PreferenceManager.getDefaultSharedPreferences(this);
 		long nowMillis = System.currentTimeMillis();
 		long target = 0;
 		if(state_.targetTime_ != null) {
 			target = state_.targetTime_.getTimeInMillis();
 		}
 		String minStr =
 			pref.getString(MalarmPreference.PREFKEY_SLEEP_TIME,
 					MalarmPreference.DEFAULT_SLEEPTIME);
 		int min = Integer.valueOf(minStr);
 		long sleepTimeMillis = min * 60 * 1000;
 		state_.sleepMin_ = min;
 		if (target == 0 || target - nowMillis >= sleepTimeMillis) {
 			PendingIntent sleepIntent =
 				makePlayPintent(MalarmPlayerService.SLEEP_ACTION, prefUseNativePlayer);
 			AlarmManager mgr =
 				(AlarmManager) getSystemService(Context.ALARM_SERVICE);
 			mgr.set(AlarmManager.RTC_WAKEUP, nowMillis + sleepTimeMillis, sleepIntent);
 			updateUI();
 		}
 	}
 	
 	private void playSleepMusic(long targetMillis) {
 		if (player_.isPlaying()) {
 			player_.stopMusic();
 		}
 		Playlist list = MalarmPlayerService.sleepPlaylist_;
 		Log.d(TAG, "playSleepMusic sleepPlaylist:" + list);
 		if(null == list){
 			showMessage(this, getString(R.string.sleep_playlist_not_exist));
 			return;
 		}
 		int sleepVolume =
 			Integer.valueOf(pref_.getString(MalarmPreference.PREFKEY_SLEEP_VOLUME,
 					MalarmPreference.DEFAULT_SLEEP_VOLUME));
 		AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 		mgr.setStreamVolume(AudioManager.STREAM_MUSIC, sleepVolume, AudioManager.FLAG_SHOW_UI);
 		player_.playMusic(list, null == state_.targetTime_);
 		setSleepTimer();
 	}
 	
 	/**
 	 * 
 	 * @return target time in epoch time (miliseconds)
 	 */
 	private void setAlarm() {
 		//set timer
 		Calendar now = new GregorianCalendar();
 
 		//remove focus from timeticker to save time which is entered by software keyboard
 		timePicker_.clearFocus();
 		int target_hour = timePicker_.getCurrentHour().intValue();
 		int target_min = timePicker_.getCurrentMinute().intValue();
 		Calendar target = new GregorianCalendar(now.get(Calendar.YEAR),
 				now.get(Calendar.MONTH), now.get(Calendar.DATE), target_hour, target_min, 0);
 		long targetMillis = target.getTimeInMillis();
 		long nowMillis = System.currentTimeMillis();
 		if (targetMillis <= nowMillis) {
 			//tomorrow
 			targetMillis += 24 * 60 * 60 * 1000;
 			target.setTimeInMillis(targetMillis);
 		}
 		state_.targetTime_ = target;
 
 		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pendingIntent =
 				makePlayPintent(MalarmPlayerService.WAKEUP_ACTION, false);
 		mgr.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent);
 
 		String title = getString(R.string.notify_waiting_title);
 		title += " " + dateStr(target);
 		player_.showNotification(title, "");
 	}
 
 	public void setNow() {
 		if (timePicker_.isEnabled()) {
 			Calendar now = new GregorianCalendar();
 			timePicker_.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
 			timePicker_.setCurrentMinute(now.get(Calendar.MINUTE));
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.stop_vibration:
 			player_.stopVibrator();
 			showMessage(this, getString(R.string.notify_wakeup_text));
 			break;
 		case R.id.play_wakeup:
 			if(player_.isPlaying()) {
 				return true;
 			}
 			Log.d(TAG, "play_wakeup: playMusic");
 			player_.playMusic(null == state_.targetTime_);
 			break;
 		case R.id.pref:
 			//TODO: use startActivityForResult
 			startActivity(new Intent(this, MalarmPreference.class));
 			break;
 		case R.id.stop_music:
 			if(! player_.isPlaying()){
 				return true;
 			}
 			player_.pauseMusic();
 			if(null == state_.targetTime_){
 				player_.clearNotification();
 			}
 			cancelSleepTimer();
 			updateUI();
 			break;
 		default:
 			Log.d(TAG, "Unknown menu");
 			return false;
 		}
 		return true;
 	}
 
 	public void onClick(View v) {
 		if (v == nextButton_) {
 			if(player_.isPlaying()) {
 				player_.playNext();
 			}
 			// otherwise confirm and play music?
 		}
 		else if (v == speechButton_) {
 			setTimeBySpeech();
 		}
 		else if (v == alarmButton_) {
 			InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 			mgr.hideSoftInputFromWindow(timePicker_.getWindowToken(), 0);
 			if (state_.targetTime_ != null) {
 				Log.d(TAG, "Stop Alarm");
 				stopAlarm();
 			}
 			else {
 				Log.d(TAG, "Set Alarm");
 				setAlarm();
 				playSleepMusic(state_.targetTime_.getTimeInMillis());
 				updateUI();
 			}
 		}
 		else if (v == setNowButton_) {
 			setNow();
 		}
 	}
 
 	@Override
 	public boolean onKey(View view, int keyCode, KeyEvent event) {
 		if(event.getAction() == KeyEvent.ACTION_UP) {
 			boolean handled = false;
 			if((KeyEvent.KEYCODE_0 <= keyCode) &&
 				(keyCode <= KeyEvent.KEYCODE_9)) {
 				state_.webIndex_ = (keyCode - KeyEvent.KEYCODE_0) % WEB_PAGE_LIST.length;
 				loadWebPage();
 				handled = true;
 			}
 			return handled;
 		}
 		return false;
 	}
 
 	private void setTimeBySpeech() {
 		if (! timePicker_.isEnabled() || startingSpeechActivity_) {
 			return;
 		}
 		if (null == speechIntent_) {
 			//to reduce task of onCreate method
 			showMessage(this, getString(R.string.init_voice));
 			PackageManager pm = getPackageManager();
 			List<ResolveInfo> activities =
 					pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 			if (activities.isEmpty()) {
 				return;
 			}
 			speechIntent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 			speechIntent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 			speechIntent_.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_dialog));
 		}
 		startingSpeechActivity_ = true;
 		webview_.stopLoading();
 		startActivityForResult(speechIntent_, SPEECH_RECOGNITION_REQUEST_CODE);
 	}
 	
 	public static void showMessage(Context c, String message) {
 		Toast.makeText(c, message, Toast.LENGTH_LONG).show();
 	}
 
 	private void shortVibrate() {
 		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (vibrator != null) {
 			vibrator.vibrate(150);
 		}
 	}
 	
 	@Override
 	public boolean onLongClick(View view) {
 		if (view == loadingIcon_) {
 			shortVibrate();
 			webview_.stopLoading();
 			showMessage(this, getString(R.string.stop_loading));
 			return true;
 		}
 		if (view == alarmButton_) {
 			if (alarmButton_.isChecked()) {
 				return false;
 			}
 			shortVibrate();
 			setAlarm();
 			new AlertDialog.Builder(this)
 			.setTitle(R.string.ask_play_sleep_tune)
 			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					playSleepMusic(state_.targetTime_.getTimeInMillis());
 					updateUI();
 				}
 			})
 			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 					updateUI();
 				}
 			})
 			.create()
 			.show();
 			return true;
 		}
 		//TODO: implement
 //		if (view == playlistLabel_) {
 //			if (player_.isPlaying()) {
 //				return false;
 //			}
 //			shortVibrate();
 //			Player.switchPlaylist();
 //			updateUI();
 //			return true;
 //		}
 		if (view == nextButton_) {
 			shortVibrate();
 			if(! player_.isPlaying()) {
 				Log.d(TAG, "onLongClick: playMusic");
 				player_.playMusic(null == state_.targetTime_);
 			}
 			cancelSleepTimer();
 			setSleepTimer();
 			updateUI();
 			showMessage(this, getString(R.string.play_with_sleep_timer));
 			return true;
 		}
 		return false;
 	}
 
 	private class TimePickerTime
 	{
 		public final int hour_;
 		public final int min_;
 		public final String speach_;
 		
 		public TimePickerTime(int hour, int min, String speach) {
 			hour_ = hour;
 			min_ = min;
 			speach_ = speach;
 		}
 	}
 
 	private class ClickListener
 		implements DialogInterface.OnClickListener
 	{
 		private TimePickerTime[] mTimeList;
 
 		public ClickListener(TimePickerTime[] time) {
 			mTimeList = time;
 		}
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			setTimePickerTime(mTimeList[which]);
 		}
 	}
 	
 	private void setTimePickerTime(TimePickerTime time) {
 		setDefaultTime_ = false;
 		timePicker_.setCurrentHour(time.hour_);
 		timePicker_.setCurrentMinute(time.min_);
 		String msg = 
 				MessageFormat.format(getString(R.string.voice_success_format), time.speach_);
 		showMessage(this, msg);
 	}
 	
 	//TODO: support english???
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == SPEECH_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
 			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			//ArrayList<TimePickerTime> result = new ArrayList<TimePickerTime>();
 			Map<String, TimePickerTime> result = new HashMap<String, TimePickerTime>();
 			for (String speech : matches) {
 				Matcher m = TIME_PATTERN.matcher(speech);
 				if (m.matches()) {
 					int hour = Integer.valueOf(m.group(1)) % 24;
 					int minute;
 					String minStr = m.group(2);
 					if (null == minStr) {
 						minute = 0;
 					}
 					else if ("半".equals(minStr)) {
 						minute = 30;
 					}
 					else {
 						minute = Integer.valueOf(m.group(3)) % 60;
 					}
 					String key = hour + ":" + minute;
 					if(! result.containsKey(key)){
 						result.put(key, new TimePickerTime(hour, minute, speech));
 					}
 				}
 				else {
 					Matcher m2 = AFTER_TIME_PATTERN.matcher(speech);
 					if (m2.matches()) {
 						String hourStr = m2.group(2);
 						String minStr = m2.group(3);
 						if (null == hourStr && null == minStr) {
 							continue;
 						}
 						long after_millis = 0;
 						if (null != hourStr) {
 							after_millis += 60 * 60 * 1000 * Integer.valueOf(hourStr);
 						}
 						if (null != minStr){
 							if ("半".equals(minStr)) {
 								after_millis += 60 * 1000 * 30;
 							}
 							else {
 								long int_data = Integer.valueOf(m2.group(4));
 								after_millis += 60 * 1000 * int_data;
 							}
 						}
 						Calendar cal = new GregorianCalendar();
 						cal.setTimeInMillis(System.currentTimeMillis() + after_millis);
 						int hour = cal.get(Calendar.HOUR_OF_DAY);
 						int min = cal.get(Calendar.MINUTE);
 						String key = hour + ":" + min;
 						if(!result.containsKey(key)){
 							result.put(key, new TimePickerTime(hour, min, speech));
 						}
 					}
 				}
 			}
 			if (result.isEmpty()) {
 				showMessage(this, getString(R.string.voice_fail));
 			}
 			else if (result.size() == 1) {
 				setTimePickerTime(result.values().iterator().next());
 			}
 			else {
 				String [] speechArray = new String[result.size()];
 				Iterator<TimePickerTime> iter = result.values().iterator();
 				for(int i = 0; i < result.size(); i++){
 					TimePickerTime time = iter.next();
 					speechArray[i] = time.speach_ +
 							String.format(" (%02d:%02d)", time.hour_, time.min_);
 				}
 				//select from list dialog
 				new AlertDialog.Builder(this)
 				.setTitle(R.string.select_time_from_list)
 				.setItems(speechArray,
 						new ClickListener(result.values().toArray(new TimePickerTime[0])))
 				.create()
 				.show();
 			}
 		}
 	}
 
 	@Override
 	public void onLowMemory () {
 		//TODO: close this activity
 		webview_.clearCache(false);
 	}
 
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder binder) {
 		Log.d(TAG, "onServiceConnected");
 		player_ = ((MalarmPlayerService.LocalBinder)binder).getService();
 		updateUI();
 		player_.setPlayerStateListener(this);
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		Log.d(TAG, "onServiceDisconnected");
 		player_.clearPlayerStateListener();
 		player_ = null;
 	}
 
 	@Override
 	public void onStartMusic(String title) {
 		updateUI();
 	}
 
 	@Override
 	public void onStopMusic() {
 		updateUI();
 	}
 }
