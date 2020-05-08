 
 package com.openfeint.qa.ggp;
 
 import net.gree.asdk.core.ui.GreeWebView;
import net.gree.vendor.com.google.gson.JsonArray;
 import android.app.Activity;
 import android.os.Bundle;
import android.webkit.WebSettings;
 
 public class GreeWebViewActivity extends Activity {
     private static GreeWebViewActivity activity;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.greewebview);
         GreeWebView webview = (GreeWebView) findViewById(R.id.greewebview);
         webview.setUp();
         webview.loadUrl("file:///android_asset/jslib/test.html");
 
         activity = GreeWebViewActivity.this;
     }
 
     public static GreeWebViewActivity getInstance() {
         return activity;
     }
 
 }
