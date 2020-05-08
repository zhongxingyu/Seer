 package com.moodmeme;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.WindowManager;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  * 
  * @see SystemUiHider
  */
 public class MoodMeme extends Activity {
 
 	private WebView webview;
 	private Timer timer;
 
 	private static final int RESULT_SETTINGS = 1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		webview = new WebView(this);
 		WebSettings webSettings = webview.getSettings();
 		webSettings.setJavaScriptEnabled(true);
 		webview.setWebViewClient(new WebViewClient() {
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				return false;
 			}
 		});
 		setContentView(webview);
 
 		webview.loadUrl("http://www.moodmeme.com:3000");
 
 		restartTimer();
 
 	}
 
 	public void restartTimer() {
 
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		int frequency = Integer.parseInt(sharedPrefs.getString("prefNotificationFrequency", "24"));
 
 		if (timer == null) {
 			timer = new Timer();
 		}
 		timer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				showNotification();
 			}
 		}, 0, frequency*1000*3600);
 	}
 
 	public void showNotification() {
 
 		Intent intent = new Intent(this, MoodMeme.class);
 		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
 
 		// Build notification
 		// Actions are just fake
		Notification noti = new NotificationCompat.Builder(this)
 				.setContentTitle("MoodMeme")
 				.setContentText("Update your mood!")
 				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
 				.build();
 
 		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		// Hide the notification after its selected
 		noti.flags |= Notification.FLAG_AUTO_CANCEL;
 		noti.defaults |= Notification.DEFAULT_VIBRATE;
 		noti.vibrate = new long[] { 100, 200 };
 
 		notificationManager.notify(0, noti);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 
 			webview.goBack();
 
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		case R.id.menu_settings:
 			Intent i = new Intent(this, UserSettingsActivity.class);
 			startActivityForResult(i, RESULT_SETTINGS);
 			break;
 
 		}
 
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case RESULT_SETTINGS:
 			restartTimer();
 			break;
 
 		}
 
 	}
 
 }
