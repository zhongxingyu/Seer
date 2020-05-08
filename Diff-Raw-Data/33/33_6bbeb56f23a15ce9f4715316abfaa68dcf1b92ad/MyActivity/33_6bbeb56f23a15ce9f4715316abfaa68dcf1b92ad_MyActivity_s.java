 package com.example.android;
 
 import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class MyActivity extends Activity {
     private WebView mWebivew;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         setlayout();
 
 //단말 해상도에 따른 M. 페이지 크기 설정
 
 
         DisplayMetrics metrics = new DisplayMetrics();
 
 
         // ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
 
         // 혹은 액티비티에서는 다음처럼 설정이 가능
 
 
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
 
         int screenDensity = metrics.densityDpi;
 
 
         Log.d("density", "screenDensity : " + screenDensity);
 
 
        if (screenDensity == 240) {

            mWebivew.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);

        } else if (screenDensity == 160) {

            mWebivew.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

        } else if (screenDensity == 120) {

            mWebivew.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);

        }
 
         mWebivew.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
 
 
         mWebivew.loadUrl("http://stock.abyang.com");
         mWebivew.getSettings().setSupportMultipleWindows(true);
         mWebivew.getSettings().setJavaScriptEnabled(true);
 

         this.mWebivew.setWebViewClient(new WebViewClient() {
 
             @Override
             public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 view.loadUrl(url);
                 return true;
             }
         });
 
        //s
 
     }
 
 
     private void setlayout() {
         mWebivew = (WebView) findViewById(R.id.webView);
     }
 }
