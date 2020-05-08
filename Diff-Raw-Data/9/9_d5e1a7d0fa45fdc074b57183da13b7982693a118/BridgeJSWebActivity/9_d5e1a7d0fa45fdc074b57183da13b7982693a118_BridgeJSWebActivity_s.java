 package org.bridgejs.android.phonebridge;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.bridgejs.android.phonebridge.library.DroidBridge;
 import org.bridgejs.android.phonebridge.library.browser.BridgeJSBrowser;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.view.inputmethod.InputMethodManager;
 
 public class BridgeJSWebActivity extends DroidBridge {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		this.browser = (BridgeJSBrowser)this.findViewById(R.id.browser);
 		this.browser.init(this, new Handler(), false);
 
		String url = "http://html5.litten.com/layers/canvaslayers.html"; //simple canvas animation demo
		//		String url = "http://impactjs.com/drop"; //built with game library, not working
 		//		String url = "http://www.benjoffe.com/code/games/torus/"; //fast, but needs keyboard
 		//		String url = "http://clear.youyuxi.com/"; // ui demo w/ css3 (FAST!)
 		//		String url = "http://www.nihilogic.dk/labs/wolf/"; //wolfenstein, needs keyboard
		//				String url = "http://ptdef.com/"; //tower defence game, works well
		//				String url = "http://rapidjs.com/demos/gameNative.html"; //simple accelerometer game
 		//		String url = "http://www.webworks.dk/enginetest/?hn"; //gta clone
 
 		url = getUrlFromIntent(getIntent(), url);
 		loadUrlWithPlugins(url);
 
 	}
 }
