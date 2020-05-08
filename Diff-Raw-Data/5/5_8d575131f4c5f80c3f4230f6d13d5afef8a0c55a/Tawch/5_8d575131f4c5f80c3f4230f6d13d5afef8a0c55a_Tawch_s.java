 package com.jtws.tawch;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.google.ads.*;
 
 public class Tawch extends Activity {
 	private final String TAG = "com.jtws.Tawch";
 	
 	private boolean torchState = false;
 	
 	private Button toggleButton;
 	private Camera camera;
 	private Camera.Parameters cameraParameters;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.main);
         
         // Setup click listeners for all the buttons
         toggleButton = (Button)findViewById(R.id.turn_on_button);
         toggleButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View arg0) {
 				toggleFlash();
 			}
         	
         });
         
         // Add test device
         AdRequest adRequest = new AdRequest();
         adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
         adRequest.addTestDevice("F5DE7F445ACCC803052D88C2B5A0AFAA");
         adRequest.addTestDevice("6AE4AF6D0C4D2708108CAFA014D1C5A1");
         
         // Get an advert
         AdView adView = (AdView)findViewById(R.id.ad_view);
         adView.loadAd(adRequest);
     }
     
     @Override
     public void onAttachedToWindow() {
     	super.onAttachedToWindow();
     	Window window = getWindow();
     	window.setFormat(PixelFormat.RGBA_8888);
     }
     
     /*
      * Method used to toggle the flash on/off
      * 
      * Depending on settings it should either blank screen
      * or turn the flash on
      */
     public void toggleFlash() {
     	if(Prefs.getFlashType(this).equals("screen") || camera == null) {
     		Log.d(TAG, "Preferences state to use phone screen");
     		
     		Intent i = new Intent(this, ScreenTorch.class);
     		startActivity(i);
     		return;
     	}
     	
     	// Get the current camera parameters
 		cameraParameters = camera.getParameters();
     	
     	// Reverse torchState;
     	torchState = !torchState;
     	
     	String flashState;
     	
     	if(torchState) {
     		flashState = Camera.Parameters.FLASH_MODE_TORCH;
     	} else {
    		flashState = Camera.Parameters.FLASH_MODE_AUTO;
     	}
     	
     	// First attempt at setting the camera parameters
     	try {
     		Log.d(TAG, "Settings camera parameter to " + flashState);
     		
     		cameraParameters.setFlashMode(flashState);
     		camera.setParameters(cameraParameters);
 
     		camera.startPreview();
     	} catch(Exception e) {
     		Log.e(TAG, "Error setting camera parameter to " + flashState, e);
     		Log.d(TAG, "Trying a bit of a hack...");
     		
     		// That failed, so try a different technique
     		try {
     			if(torchState) {
     				flashState = "torch";
     			} else {
    				flashState = "auto";
     			}
     			
     			cameraParameters.set("flash-mode", flashState);
     			camera.setParameters(cameraParameters);
     			
     			camera.startPreview();
     		} catch(Exception e2) {
     			Log.e(TAG, "Failed with the hack, using the phones screen instead", e2);
     			
     			if(camera != null) {
     				camera.release();
     				camera = null;
     			}
     			
     			Intent i = new Intent(this, ScreenTorch.class);
     			startActivity(i);
     			return;
     		}
     	}
     	
     	// Decide whether or not to keep the screen on.
     	View v = findViewById(R.id.parent_ll);
     	if(torchState) {
     		v.setKeepScreenOn(true);
     		toggleButton.setText(R.string.turn_off_label);
     	} else {
     		v.setKeepScreenOn(false);
     		toggleButton.setText(R.string.turn_on_label);
     	}
     }
     
     @Override
     public void onPause() {
 		// Reset the button text and torch_state
 		Button toggleButton = (Button)findViewById(R.id.turn_on_button);
 		toggleButton.setText(R.string.turn_on_label);
 		
 		torchState = false;
     	
 		// Release the camera
 		if(camera != null) {
 			camera.release();
 			camera = null;
 		}
 		
 		super.onPause();
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	// Attempt to reopen the camera
     	try {
     		camera = Camera.open();
     	} catch(Exception e) {
     		Log.e(TAG, "Error opening camera", e);
     		
     		if(camera != null) {
     			camera.release();
     			camera = null;
     		}
     	}
     }
     
     public void showShareMenu() {
     	Intent shareIntent = new Intent(Intent.ACTION_SEND);
     	shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
     	shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
     	shareIntent.setType("text/plain");
     	startActivity(Intent.createChooser(shareIntent, "Share"));
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     	case R.id.about:
     		Intent i = new Intent(this, About.class);
     		startActivity(i);
     		break;
     	case R.id.share:
     		showShareMenu();
     		break;
     	case R.id.settings:
     		Intent j = new Intent(this, Prefs.class);
     		startActivity(j);
     		break;
     	}
     	
     	return false;
     }
 }
