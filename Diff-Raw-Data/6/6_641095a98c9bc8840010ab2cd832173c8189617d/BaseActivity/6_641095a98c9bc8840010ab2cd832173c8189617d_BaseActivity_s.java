 package com.kt3k.app.whatsmyip;
 
 import java.io.File;
 
 import org.kt3k.straw.Straw;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.LinearLayout;
 
 import com.google.ads.*;
 
 public class BaseActivity extends Activity {
 
     private WebView webView;
     private AdView adView;
     private LinearLayout layout;
 
     private Boolean adEnabled = true;
    private String url = null;
 
     /**
      * set up window
      */
     private void setUpWindow() {
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
     }
 
     /**
      * set up the main layout
      */
     private void setUpLayout() {
 
         // set up linear layout
         this.layout = new LinearLayout(this);
 
         // enable only portrait orientation
         this.layout.setOrientation(LinearLayout.VERTICAL);
 
         // set layout as content view
         this.setContentView(layout);
 
         // set up and layout the WebView
         this.setUpWebView();
         this.layout.addView(this.webView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
 
         // if ad enable then add ad
         if (this.adEnabled) {
             this.setUpAdView();
 
             this.adView.setGravity(Gravity.BOTTOM);
 
             LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
 
             p.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
 
             this.layout.addView(this.adView, p);
         }
     }
 
     /**
      * set up the WebView
      */
     private void setUpWebView() {
 
         // create WebView
         webView = new WebView(this);
 
         //
         if(url == null || "".equals(url)) {
             url = this.getAppHome();
         }
 
         // setting scroll bar styles
         webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
         webView.setScrollbarFadingEnabled(true);
 
         // set WebViewClient
         webView.setWebViewClient(new CustomWebViewClient(this.getString("wait_message", "Loading")));
 
         // set WebChromeClient
         webView.setWebChromeClient(new WebChromeClient());
 
         // enable JavaScript
         webView.getSettings().setJavaScriptEnabled(true);
 
         // enable DomStorage (i.e. localStorage and SessionStorage)
         webView.getSettings().setDomStorageEnabled(true);
 
         // set DB path
         webView.getSettings().setDatabasePath((new File(getCacheDir(), "/database")).toString());
 
         // set background color white
         webView.setBackgroundColor(Color.WHITE);
 
         // insert Straw into webView and init basic plugins
         Straw.insertInto(webView).addPlugins(org.kt3k.straw.plugin.BasicPlugins.names);
 
         // load url
         webView.loadUrl(url);
     }
 
     /**
      * set up the AdView
      */
     private void setUpAdView() {
         adView = new AdView(this, AdSize.BANNER, this.getPublisherId());
 
         // set up the request
         AdRequest adRequest = new AdRequest();
 
         // set test devices
         adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
         adRequest.addTestDevice("DA5B0069BA8827B46FD8DBDB70EB7FAE"); // Test Device 1
         adRequest.addTestDevice("7DCEAF5D75884209E5102213D1EA33C6"); // Test Device 2
 
         // request an ad
         adView.loadAd(adRequest);
     }
 
     /**
      * get string for key from string resources
      * @param key to get
      * @param defaultValue if value unavailable
      * @return string for key or default if string unavailable
      */
     private String getString(String key, String defaultValue) {
         String str;
 
         try {
             int resId = R.string.class.getField(key).getInt(null);
             str = this.getString(resId);
 
         } catch (Exception e) {
             str = defaultValue;
         }
 
         return str;
     }
 
     private String getAppHome() {
         return this.getString("app_home", "file:///android_asset/") + this.getString("app_index", "index.html");
     }
 
     private String getPublisherId() {
         return this.getString("app_publisher_id", "");
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         this.setUpWindow();
 
         this.setUpLayout();
     }
 
     class CustomWebViewClient extends WebViewClient {
         private ProgressDialog dialog;
         private String loadingMessage;
 
         public CustomWebViewClient(String loadingMessage) {
             super();
             this.dialog = null;
             this.loadingMessage = loadingMessage;
         }
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             this.dialog = new ProgressDialog(view.getContext());
             this.dialog.setMessage(this.loadingMessage);
             this.dialog.show();
         }
 
         @Override
         public void onPageFinished(WebView view, String url) {
             if (null != dialog) {
                 //ダイアログを削除
                 this.dialog.dismiss();
                 this.dialog = null;
             }
         }
     }
 }
