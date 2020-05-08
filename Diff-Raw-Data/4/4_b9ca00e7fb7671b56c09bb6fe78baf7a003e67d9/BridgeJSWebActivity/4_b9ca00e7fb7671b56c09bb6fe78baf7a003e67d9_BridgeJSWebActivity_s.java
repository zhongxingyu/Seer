 package org.bridgejs.android.phonebridge;
 
 import org.bridgejs.android.phonebridge.library.DroidBridge;
 import org.bridgejs.android.phonebridge.library.R;
 import org.bridgejs.android.phonebridge.library.browser.BridgeJSBrowser;
 
 import android.os.Bundle;
 import android.view.Window;
 
 public class BridgeJSWebActivity extends DroidBridge {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		getWindow().requestFeature(Window.FEATURE_NO_TITLE); 
 		setContentView(R.layout.main);
 		browser = (BridgeJSBrowser)findViewById(R.id.browser);
 		super.onCreate(savedInstanceState);
 		//		String url = "http://reddit.com";
 
		//String url = "http://www.bridgejs.com/";
		String url = "file:///android_asset/testButton.html";
 
 		//		String url = "http://html5.litten.com/layers/canvaslayers.html"; //simple canvas animation demo
 		//				String url = "http://impactjs.com/drop/"; //built with impactjs game library
 		//		String url = "http://www.benjoffe.com/code/games/torus/"; //fast, but needs keyboard
 		//		String url = "http://clear.youyuxi.com/"; // ui demo w/ css3 (FAST!)
 		//		String url = "http://www.nihilogic.dk/labs/wolf/"; //wolfenstein, needs keyboard
 		//		String url = "http://ptdef.com/"; //tower defence game, works well
 //				String url = "http://bridgejs.com/demos/gameNative.html"; //simple accelerometer game
 		//		String url = "http://www.webworks.dk/enginetest/?hn"; //gta clone
 		
 //		String url = "http://bridgejs.com";
 		
 //		String url = "http://kosbie.net/temp/koz-test/koz-test.html";
 
 		String defaultUrl = url;
 		url = getUrlFromIntent(getIntent(), defaultUrl);
 		loadUrlWithPlugins(url);
 //		loadUrlWithoutPlugins(url);
 	}
 }
