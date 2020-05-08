 package co.uk.jagw.andtorch;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.Parameters;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 //import com.millennialmedia.android.MMAdViewSDK;
 //import com.mopub.mobileads.MoPubView;
 
 public class Torch extends Activity {
 
 	// Create private variables
 	private Camera camera; // Main Camera used throughout the application
 	private Parameters params;
 	
 	private boolean flashOn = false;
 	private boolean stickFlash = false; // Variable used to signal whether the flash should persist onPause
 	 
 	public String tag;
 	public Notification mNotification;
 	public String flashMode;
 	private boolean hasFlash;
 
 	// ADVERTISING MoPub - create a private
 	// private MoPubView mAdView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_torch);
 
 		// TODO: See if there's a custom background selected
 		// SharedPreferences sharedPref =
 		// PreferenceManager.getDefaultSharedPreferences(this);
 		// sharedPref.
 		// layout = (LinearLayout) findViewById(R.id.mainLayout);
 		// layout.setBackground(background);
 		
 
 		// // ADVERTISING
 		// // MoPub Code
 		// mAdView = (MoPubView) findViewById(R.id.adview);
 		// mAdView.setAdUnitId("aa8476646a2a11e281c11231392559e4");
 		// mAdView.loadAd();
 		// // Set the Millennial SDK to LOG_LEVEL_VERBOSE
 		// MMAdViewSDK.logLevel = MMAdViewSDK.LOG_LEVEL_VERBOSE;
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_torch, menu);
 		return true;
 	}
 
 	@Override
 	public void onPause() {
 
 		if (stickFlash == false) {
 			// Let the camera go.
 			try{
 				camera.release();
 			} catch(Exception e){
 				// If there's no camera, that's OK. Let it go!
 			}
 		} else if (stickFlash == true) { 
 			Log.d("onPause", "onPause invoking, camera is NOT being released");
 			// Warn the user that Camera and similar apps may fail.
 			Toast toast = Toast.makeText(this, "AndTorch will remain on - other apps using the Camera may fail.", Toast.LENGTH_SHORT);
 			toast.show();
 		}
 
 		// Call the rest of the onPause
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 
 		if (stickFlash == true) {
 			// Show notification that the torch is still on.
 			Toast toast = Toast.makeText(this, "AndTorch still active", Toast.LENGTH_SHORT);
 			toast.show();
 		} 
 
 		// Call the rest of the onResume method.
 		super.onResume();
 	}
 
 	@Override
 	public void onDestroy() {
 		
 		if(camera != null){
 			camera.release();
 		}
 				
 		// If there's a notification when the app is destroyed, kill it too.
 		if(mNotification != null){
 			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 			notificationManager.cancel(1);
 		}
 		
 		// ADVERTISING - Destroy the MoPub ad unit.
 		// mAdView.destroy();
 
 		super.onDestroy();
 	}
 
 	// Controls for the widget.
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
		if(intent.getStringExtra("methodName").equals("toggleFlash")){
			try {
				camera.reconnect();
			} catch (Exception e) {
				// If we get here, either a camera doesn't exist, or it can't be reconnected to.
				camera = Camera.open();
 			}
			params = camera.getParameters();
			flashOn(params);
 		}
 
 	}
 
 
 
 	// Method to check if the camera has the flash feature,
 	// Returns: True if it has a flash, false if it doesnt.
 	// Also sets main variable with the supported flash mode.
 	public boolean checkIfFlash(Parameters params) {
 		if (this.getPackageManager().hasSystemFeature(
 				PackageManager.FEATURE_CAMERA_FLASH)) {
 			Log.d("checkIfFlash", "It has flash!");
 
 			// Checking what type it has
 			List<String> pList = params.getSupportedFlashModes();
 			if (pList.contains(Parameters.FLASH_MODE_TORCH)) {
 				Log.d("checkIfFlash", "It has FLASH_MODE_TORCH!");
 				flashMode = "FLASH_MODE_TORCH";
 			} else if(pList.contains(Parameters.FLASH_MODE_ON)){
 				Log.d("checkIfFlash", "It has FLASH_MODE_ON");
 				flashMode = "FLASH_MODE_ON";
 			} else {
 				// We shouldn't get here!
 				Log.d("checkIfFlash", "It has flash, but no flash mode!");
 				return false;
 			} 		
 
 			return true;
 		} else {
 			// It has nothing - use front LCD screen.
 			return false;
 		}
 
 	}
 
 	// Method to toggle flash. Contains a logic check for whether the flash is on or not.
 	public void toggleFlash(View view){
 		Log.d("toggleFlash", "Got to toggleFlash method");
 		
 		// Get the preferences from the application.
 		SharedPreferences sharedPref = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		Boolean screenPref = sharedPref.getBoolean("pref_screenflash", false);
 		
 		// If there's no camera object open, open one.
 		if(camera == null){
 			try {
 				camera = Camera.open();
 				
 			} catch(Exception e){
 				// Camera cannot be opened.
 				Toast toast = Toast.makeText(this, "Camera cannot be opened, screen will be used instead", Toast.LENGTH_SHORT);
 				toast.show();
 				hasFlash = false;
 			}
 		}
 		
 		
 		// If there is no Parameters set, obtain them.
 		if(params == null ){	
 			try{
 				params = camera.getParameters();
 			} catch (Exception e){
 				// It's possible we get here, if so; let's carry on!
 			}
 		}
 		// Check we've got flash available.
 		hasFlash = checkIfFlash(params);
 		
 		// Check for flash, and check for overwriting preference 'Use Screen Flash'
 		if (hasFlash == true & screenPref == false) {
 			
 			// If the flash isn't on; turn it on!
 			if (flashOn == false) {
 				try {
 					camera.reconnect();
 				} catch (Exception e) {
 					// Do nothing - camera is already connected
 				}
 				params = camera.getParameters();
 				flashOn(params);
 				
 				// If the flash is on, turn it off.
 			} else if(flashOn == true) {
 				params = camera.getParameters();
 				flashOff(params);
 			}
 		} else {
 			// The device doesn't have a flash, or the user has set Use Screen Flash.
 			flashScreen();
 		}
 	}
 
 	public void stickFlash(View view) {
 		Log.d("stickFlash", "Got to stickFlash method");
 		
 		if(flashOn == false){
 			stickFlash = true;
 		} else if (flashOn == true){
 			stickFlash = false;
 		}
 		
 		toggleFlash(view);
 
 //		if (camera == null) {
 //			try {
 //				camera = Camera.open();
 //			} catch (Exception e) {
 //				Toast toast = Toast.makeText(this, "Torch cannot be opened - screen will be used instead", Toast.LENGTH_SHORT);
 //				toast.show();
 //				
 //				flashScreen();
 //			}
 //		}
 //		
 //		if (flashOn == false) {
 //			try {
 //				camera.reconnect();
 //			} catch (Exception e) {
 //				// Do nothing - camera is already connected
 //			}
 //
 //			params = camera.getParameters();
 //			flashOn(params);
 //			stickFlash = true;
 //
 //		} else if (flashOn == true) {
 //			params = camera.getParameters();
 //			flashOff(params);
 //			stickFlash = false;
 //
 //		}
 
 	}
 
 	// Method to turn the flash on, accepts camera parameters
 	public void flashOn(Parameters params) {
 		Log.d("flashOn", "Got to flashOn method");
 		// Get supported flash modes so we can work out what is possible.
 		List<String> pList = params.getSupportedFlashModes();
 
 		// Check if the camera supports FLASH_MODE_TORCH
 		if (pList.contains(Parameters.FLASH_MODE_TORCH)) {
 			Log.d("FlashMode", "FLASH_MODE_TORCH available");
 			params.setFlashMode(Parameters.FLASH_MODE_OFF);
 			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
 			// Save the parameters back to the camera and set a flag.
 			camera.setParameters(params);
 			camera.startPreview();
 			camera.autoFocus(new AutoFocusCallback(){
 				public void onAutoFocus(boolean success, Camera camera){
 				}
 			});
 			flashOn = true;
			createNotification();
 
 			// Otherwise, does it support FLASH_MODE_ON
 		} else if (pList.contains(Parameters.FLASH_MODE_ON)){
 			Log.d("FlashMode", "FLASH_MODE_ON available");
 			params.setFlashMode(Parameters.FLASH_MODE_OFF);
 			params.setFlashMode(Parameters.FLASH_MODE_ON);
 			// Save the parameters back to the camera and set a flag.
 			camera.setParameters(params);
 			camera.startPreview();
 			camera.autoFocus(new AutoFocusCallback(){
 				public void onAutoFocus(boolean success, Camera camera){
 				}
 			});
 
 			flashOn = true;
			createNotification();
 		} else {
 			// We shouldn't get here, because of earlier checks - but in case we do,
 			// call the frontFlash activity
 			Log.d("FlashMode", "FLASH_MODE_TORCH and FLASH_MODE_ON not available");
 			flashScreen();
 		}
 	}
 
 	// Method to turn the flash off, accepts camera parameters
 	public void flashOff(Parameters params) {
 		params.setFlashMode(Parameters.FLASH_MODE_OFF);
 		camera.setParameters(params);
 		flashOn = false;
 
 		// Give the camera back to the OS.
 		camera.unlock();
 		
 
 	}
 
 	
 	// Method to make the current background white and turn brightness to maximum.
 	// Turning the front screen into a flashlight instead!
 	public void flashScreen() {
 		Intent helpIntent = new Intent(Torch.this, FrontFlash.class);
 		Torch.this.startActivity(helpIntent);
 		createNotification();
 	}
 
 	
 	// Method for creating a Push Notification
 	public void createNotification() {
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 				this).setSmallIcon(R.drawable.ic_launcher)
 				.setContentTitle("AndTorch")
 				.setContentText("Click to return to app.");
 
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		stackBuilder.addParentStack(Torch.class);
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotification = mBuilder.build();
 		// Make sure the notification only comes up once, and goes on click.
 		mNotification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
 		// When we click the notification, make sure it comes back to a new
 		// intent of Torch, although it's only a single launch activity.
 		Intent notificationIntent = new Intent(this.getApplicationContext(),
 				Torch.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(
 				this.getApplicationContext(), 0, notificationIntent, 0);
 		mNotification.contentIntent = contentIntent;
 		mNotificationManager.notify(1, mNotification);
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent settingIntent = new Intent(Torch.this,
 					TorchPreferences.class);
 			Torch.this.startActivity(settingIntent);
 			return true;
 		case R.id.menu_about:
 			Intent aboutIntent = new Intent(Torch.this, About.class);
 			Torch.this.startActivity(aboutIntent);
 			return true;
 		case R.id.menu_help:
 			Intent helpIntent = new Intent(Torch.this, Help.class);
 			Torch.this.startActivity(helpIntent);
 			return true;
 		case R.id.menu_exit:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
