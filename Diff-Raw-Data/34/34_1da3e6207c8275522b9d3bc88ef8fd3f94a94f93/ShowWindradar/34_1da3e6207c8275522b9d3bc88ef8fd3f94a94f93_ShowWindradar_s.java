 package com.tracebuzz.windradar;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.view.Menu;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 public class ShowWindradar extends Activity {
 
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_show_windradar);
 		
 		WebView myWebView = (WebView) findViewById(R.id.webview);
 		WebSettings webSettings = myWebView.getSettings();
 		webSettings.setJavaScriptEnabled(true);
 		myWebView.loadUrl("http://www.tracebuzz.com/test/knmi/slide2.php");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.show_windradar, menu);
 		return true;
 	}
 
 }
