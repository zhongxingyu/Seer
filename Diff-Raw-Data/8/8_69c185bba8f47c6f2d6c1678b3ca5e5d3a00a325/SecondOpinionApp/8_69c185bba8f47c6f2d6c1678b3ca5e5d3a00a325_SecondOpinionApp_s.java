 package com.secondopinion.android;
 
 import com.secondopinion.android.R;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.net.http.SslError;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.webkit.SslErrorHandler;
 import android.webkit.ValueCallback;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ProgressBar;
 
 public class SecondOpinionApp extends Activity {
     /** Special IP in Android to access the host machine. */
     public static final String LOCALHOST = "10.0.2.2";
 
     private final static int FILECHOOSER_REQUEST_CODE = 1;
 
     private ProgressBar progressBar;
     private ValueCallback<Uri> uploadMessage;
 
     @SuppressLint("SetJavaScriptEnabled")
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         WebView webView = (WebView) findViewById(R.id.webView);
         // We need JavaScript
         webView.getSettings().setJavaScriptEnabled(true);
 
         webView.setWebViewClient(new MyWebViewClient());
         webView.loadUrl("http://jinsudev-edwemmgfpw.elasticbeanstalk.com/"); // "file:///android_asset/www/home2.html"
         webView.setWebChromeClient(new MyWebChromeClient());
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         Log.i("info", "Inside onctivityResult");
         if (requestCode == FILECHOOSER_REQUEST_CODE) {
             if (null == uploadMessage) {
                 return;
             }
 
             Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
             uploadMessage.onReceiveValue(result);
             uploadMessage = null;
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.hello_world_app, menu);
         return true;
     }
 
     private class MyWebViewClient extends WebViewClient {
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             // Stay within this webview and load url
             view.loadUrl(url);
             return true;
         }
 
         @Override
         public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
             handler.proceed();
         }
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             super.onPageStarted(view, url, favicon);
         }
 
         @Override
         public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
         }
     }
 
     private class MyWebChromeClient extends WebChromeClient {
         public void openFileChooser(ValueCallback<Uri> uploadMessg, String acceptType,
                 String capture) {
             uploadMessage = uploadMessg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            SecondOpinionApp.this.startActivityForResult(Intent.createChooser(i, "File Chooser"),
                     SecondOpinionApp.FILECHOOSER_REQUEST_CODE);
 
         }
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }
 }
