 package com.eas.android.rapidjs;
 
 import com.eas.android.libraries.rapidjs.browser.RapidJSBrowser;
 import com.eas.android.libraries.rapidjs.pluginmanager.activitymodifiers.ActivityEventsModifier;
 import com.eas.android.libraries.rapidjs.pluginmanager.activitymodifiers.ActivityResultCallback;
 import com.eas.android.libraries.rapidjs.pluginmanager.activitymodifiers.SpawnActivityForResult;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.view.inputmethod.InputMethodManager;
 
 public class RapidJSWebActivity extends Activity {
 	/** Called when the activity is first created. */
 
 	private RapidJSBrowser browser;
 
 
 	private String currentUrl;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		//				String url = "http://html5.litten.com/layers/canvaslayers.html"; //simple canvas animation demo
 		//		String url = "http://impactjs.com/drop"; //built with game library, not working
 		//		String url = "http://www.benjoffe.com/code/games/torus/"; //fast, but needs keyboard
		//		String url = "http://clear.youyuxi.com/"; // ui demo w/ css3 (FAST!)
 		//		String url = "http://www.nihilogic.dk/labs/wolf/"; //wolfenstein, needs keyboard
//		String url = "http://ptdef.com/"; //tower defence game, works well
		//		String url = "http://rapidjs.com/gameNative.html"; //simple accelerometer game
 
 		this.browser = (RapidJSBrowser)this.findViewById(R.id.browser);	
 		this.browser.init(this, new Handler(), false);
 
 		loadFromIntent(getIntent(), url);
 		loadCurrentUrlWithPlugins();
 
 	}
 
 	public void loadFromIntent(Intent i, String defaultUrl){
 		String rapidURI = i.getDataString();
 		String webURI = i.getStringExtra(Intent.EXTRA_TEXT);
 		if (rapidURI != null){
 			defaultUrl = parseRapidURL(i.getDataString());
 		}
 		else if (webURI != null){
 			defaultUrl = webURI;
 		}
 
 		System.out.println("URI: " + defaultUrl);
 
 		currentUrl = defaultUrl;
 	}
 
 	public String parseRapidURL(String rapidURI) {
 
 		rapidURI = rapidURI.replaceFirst("rapid://.*?/+", "");
 		return "http://" + rapidURI;
 	}
 
 	public void loadCurrentUrlWithPlugins(){
 		browser.loadUrlWithPlugins(currentUrl);
 	}
 
 	/* Note: we do have to get the activityEventsModifier in *EVERY* event listener we override 
 	 * 			since we don't know if the pointer to the runnable will have changed at some point */
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnResumeModifier().run();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnResumeModifier().run();
 	}
 
 	@Override
 	public void onPause() {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnPauseModifier().run();
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnStopModifier().run();
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroy() {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnDestroyModifier().run();
 		super.onDestroy();
 	}
 
 	@Override
 	public void onRestart() {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnRestartModifier().run();
 		super.onRestart();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		SpawnActivityForResult spawnActivityInstance = activityEventsModifier.getSpawnActivityInstance();
 		int originalRequestCode = spawnActivityInstance.getRequestCode();
 		ActivityResultCallback callbackToPlugin = spawnActivityInstance.getActivityResultCallback();
 
 		if (originalRequestCode == requestCode) {
 			callbackToPlugin.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event){
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 
 		if (keyCode == KeyEvent.KEYCODE_BACK){
 			activityEventsModifier.getOnBackButtonModifier();
 		}
 		else if (keyCode == KeyEvent.KEYCODE_MENU) {
 			activityEventsModifier.getOnMenuButtonModifier();
 		}
 		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
 			activityEventsModifier.getOnVolumeDownButtonModifier();
 		}
 		else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
 			activityEventsModifier.getOnVolumeUpButtonModifier();
 		}
 		else if (keyCode == KeyEvent.KEYCODE_HOME) {
 			activityEventsModifier.getOnHomeButtonModifier();
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 }
