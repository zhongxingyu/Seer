 package edu.ufl.mobile.android;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 /**
  * Load the UF Mobile website in a WebView.
  * 
  * The splash screen code is based on:
  * 
  * http://blog.iangclifton.com/2011/01/01/android-splash-screens-done-right/
  * 
  * @author dwc
  */
 public class MainActivity extends Activity {
     /**
      *  Simple Dialog used to show the splash screen.
      */
     protected Dialog mSplashDialog;
     
     /**
      * WebView used to display the UF Mobile website.
      */
     protected WebView mWebView;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         showSplashScreen();
         setContentView(R.layout.main);
 
         mWebView = (WebView) findViewById(R.id.webview);
         mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                removeSplashScreen();
            }
        });
 
         mWebView.loadUrl("http://m.ufl.edu/");
     }
 
     /**
      * Show the splash screen over the full Activity.
      */
     protected void showSplashScreen() {
         mSplashDialog = new Dialog(this, R.style.splash);
         mSplashDialog.setContentView(R.layout.splash);
         mSplashDialog.setCancelable(false);
         mSplashDialog.show();
     }
     
     /**
      * Remove the Dialog that displays the splash screen.
      */
     protected void removeSplashScreen() {
         if (mSplashDialog != null) {
             mSplashDialog.dismiss();
             mSplashDialog = null;
         }
     }
 }
