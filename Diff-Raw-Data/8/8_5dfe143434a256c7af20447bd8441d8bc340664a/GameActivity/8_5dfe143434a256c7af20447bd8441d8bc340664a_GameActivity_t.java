 package edu.mit.csail.netmap.client;
 
 import edu.mit.csail.netmap.NetMap;
 import us.costan.chrome.ChromeSettings;
 import us.costan.chrome.ChromeView;
 import android.os.Bundle;
 import android.view.Window;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 
 /**
  * The game's main and only activity.
  *
  * All the action happens inside the WebView.
  */
 public class GameActivity extends Activity {
   /** The view containing the game UI. */
   private ChromeView webView = null;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     setContentView(R.layout.activity_web);
 
     webView = (ChromeView)findViewById(R.id.gameUiView);
     setupChromeView(webView);
 
     if (savedInstanceState == null) {
       webView.loadUrl("http://netmap.pwnb.us/");
       // webView.loadUrl("http://192.168.10.71:9200");
       // webView.loadUrl("http://192.168.10.73:9200");
       // webView.loadUrl("http://128.30.6.205:9200");
     }
   }
 
   @Override
   protected void onSaveInstanceState(Bundle outState) {
     super.onSaveInstanceState(outState);
     webView.saveState(outState);
   }
 
   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState) {
     super.onRestoreInstanceState(savedInstanceState);
     webView.restoreState(savedInstanceState);
   }
 
   @Override
   protected void onPause() {
     webView.onPause();
     super.onPause();
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     webView.onResume();
   }
 
   @SuppressLint("SetJavaScriptEnabled")
   /** Configure the Chrome view for usage as the application's window. */
   private void setupChromeView(ChromeView webView) {
     ChromeSettings settings = webView.getSettings();
     settings.setJavaScriptEnabled(true);
     settings.setAllowUniversalAccessFromFileURLs(true);
     settings.setMediaPlaybackRequiresUserGesture(false);
     settings.setBuiltInZoomControls(false);
     settings.setSupportZoom(false);
     settings.setUserAgentString(settings.getUserAgentString() + " NetMap/1.0");
     
     
     // The JavaScript maps _NetMapPil to NetMap.Pil
     JsBindings jsBindings = new JsBindings(webView, this);
     jsBindings.loadCookies();
     webView.addJavascriptInterface(jsBindings, "_NetMapPil");
     NetMap.setListener(jsBindings);
   }
 
   /** Saves the server's cookies, so they can be reloaded. */
   void saveGameCookies(String gameOrigin) {
   }
 
   @Override
   /** Navigate the WebView's history when the user presses the Back key. */
   public void onBackPressed() {
     if (webView != null) {
       if (webView.canGoBack()) {
         webView.goBack();
       } else {
         super.onBackPressed();
       }
     } else {
       super.onBackPressed();
     }
   }
 }
