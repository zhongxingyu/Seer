 package net.andersonvom.flashlight;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
import android.content.res.Configuration;
 import android.graphics.Color;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener
 {
 	public static Camera cam;
 	public static boolean lastCameraStatus = true;
 	public static SharedPreferences settings;
 	public static int currentBackgroundColor = Color.BLACK;
 	public static boolean hasFlash;
 	public static int MARKET_NOTIFICATION_ID = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		settings = PreferenceManager.getDefaultSharedPreferences(this);
 		hasFlash = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
 
 		ImageView toggleButton = (ImageView) findViewById(R.id.toggle_button);
 		toggleButton.setOnClickListener(this);
 
 		setScreenColor(currentBackgroundColor);
 		if (!isCameraOn() && lastCameraStatus) toggleFlashlight();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.action_settings:
 				startActivity(new Intent(this, SettingsActivity.class));
 				break;
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		toggleFlashlight();
 	}
 
 	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		// This is only to prevent onPause/onResume from being called when rotating the phone
		super.onConfigurationChanged(newConfig);
	}

	@Override
 	protected void onPause()
 	{
 		boolean runBackground = settings.getBoolean(SettingsActivity.PREF_RUN_BACKGROUND, false);
 		lastCameraStatus = isCameraOn();
 		if (!runBackground)
 		{
 			if (lastCameraStatus) toggleFlashlight();
 			updateUsageStats();
 		}
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		if (lastCameraStatus && !isCameraOn())
 			toggleFlashlight();
 		super.onResume();
 	}
 
 	private boolean isCameraOn()
 	{
 		boolean cameraStatus;
 		if (hasFlash) cameraStatus = (cam != null);
 		else cameraStatus = (currentBackgroundColor == Color.WHITE);
 		return cameraStatus;
 	}
 
 	private void toggleFlashlight()
 	{
 		if (hasFlash)
 		{
 			toggleCameraFlash();
 		}
 		else
 		{
 			toggleScreen();
 		}
 	}
 
 	private boolean toggleCameraFlash()
 	{
 		if (cam == null) cam = Camera.open();
 		Parameters p = cam.getParameters();
 
 		boolean supportsTorchMode = p.getSupportedFlashModes().contains(Parameters.FLASH_MODE_TORCH);
 		if (!supportsTorchMode)
 		{
 			Toast.makeText(this, R.string.torch_not_supported, Toast.LENGTH_SHORT).show();
 			toggleScreen();
 			return false;
 		}
 
 		if (p.getFlashMode().equals(Parameters.FLASH_MODE_OFF))
 		{
 			Log.w("[Flashlight]", "Flashlight ON");
 			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
 			cam.setParameters(p);
 			cam.startPreview();
 			Toast.makeText(this, R.string.flashlight_on, Toast.LENGTH_SHORT).show();
 		}
 		else
 		{
 			Log.w("[Flashlight]", "Flashlight OFF");
 			p.setFlashMode(Parameters.FLASH_MODE_OFF);
 			cam.setParameters(p);
 			cam.stopPreview();
 			cam.release();
 			cam = null;
 			Toast.makeText(this, R.string.flashlight_off, Toast.LENGTH_SHORT).show();
 		}
 
 		return true;
 	}
 
 	private void toggleScreen()
 	{
 		currentBackgroundColor = (currentBackgroundColor == Color.BLACK) ? Color.WHITE : Color.BLACK;
 		setScreenColor(currentBackgroundColor);
 	}
 
 	private void setScreenColor(int color)
 	{
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_activity_layout);
 		layout.setBackgroundColor(color);
 	}
 
 	private void updateUsageStats()
 	{
 		SharedPreferences.Editor editor = settings.edit();
 		int usageCount = settings.getInt(SettingsActivity.PREF_USAGE_COUNT, 0) + 1;
 		editor.putInt(SettingsActivity.PREF_USAGE_COUNT, usageCount);
 		editor.commit();
 
 		if (   usageCount == SettingsActivity.PREF_SUGGEST_MARKET_COUNT_FIRST
 			|| usageCount == SettingsActivity.PREF_SUGGEST_MARKET_COUNT_LAST)
 		{
 			suggestRateApp();
 		}
 	}
 
 	private void suggestRateApp()
 	{
 		int usageCount = settings.getInt(SettingsActivity.PREF_USAGE_COUNT, 0);
 		String suggestRateTitle = getResources().getString(R.string.suggest_rate_title);
 
 		Uri marketUri = Uri.parse("market://details?id=" + this.getPackageName());
 		Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
 		PendingIntent pendingMarket = PendingIntent.getActivity(this, 0, marketIntent, 0);
 
 		NotificationCompat.Builder marketBuilder = new NotificationCompat.Builder(this)
 			.setSmallIcon(R.drawable.ic_launcher)
 			.setContentTitle(String.format(suggestRateTitle, usageCount))
 			.setContentText(getResources().getString(R.string.suggest_rate_body))
 			.setContentIntent(pendingMarket)
 			.setAutoCancel(true);
 
 		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		manager.notify(MARKET_NOTIFICATION_ID, marketBuilder.build());
 	}
 
 }
