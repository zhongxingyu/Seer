 package bgapps.freeseatswebwrapper;
 
 import bgapps.freeseatswebwrapper.R;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class MainActivity extends Activity {
 	WebView myWebView;
 	SharedPreferences mPrefs;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         myWebView = (WebView) findViewById(R.id.webview);
         WebSettings webSettings = myWebView.getSettings();
         webSettings.setJavaScriptEnabled(true);
         myWebView.setWebViewClient(new WebViewClient() {
         	@Override
             public void onPageFinished(WebView view, String url) {
         		findViewById(R.id.textView1).setVisibility(View.GONE);
         		findViewById(R.id.webview).setVisibility(View.VISIBLE);
            }
         	public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 if (url.startsWith("mailto:") || url.startsWith("tel:")) { 
                         Intent intent = new Intent(Intent.ACTION_VIEW,
                                 Uri.parse(url)); 
                         startActivity(intent); 
                         }
                 else {
                     view.loadUrl(url);
                 }
                 return true;
                 }
         });
        myWebView.loadUrl("http://freeseatbgu.appspot.com");
         
         Context mContext = this.getApplicationContext();
         mPrefs = mContext.getSharedPreferences("myAppPrefs", 0);
 
         if (mPrefs.getBoolean("firstRun", true)) {
         	Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
         	shortcutIntent.setClassName(this, this.getClass().getName());
 
 	        Context context = this.getBaseContext();
 	        Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
 	        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
 	        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_text));
 	        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher));
 
 	        sendBroadcast(addIntent);
         }
         SharedPreferences.Editor edit = mPrefs.edit();
         edit.putBoolean("firstRun", false);
         edit.commit();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Check if the key event was the Back button and if there's history
         if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
             myWebView.goBack();
             return true;
         }
         // If it wasn't the Back key or there's no web page history, bubble up to the default
         // system behavior (probably exit the activity)
         return super.onKeyDown(keyCode, event);
     }
 }
