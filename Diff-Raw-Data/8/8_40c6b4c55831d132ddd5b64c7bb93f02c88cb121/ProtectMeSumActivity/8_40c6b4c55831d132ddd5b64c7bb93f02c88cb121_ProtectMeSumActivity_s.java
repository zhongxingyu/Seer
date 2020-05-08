 package com.rocketmbsoft.protectme;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 
 public class ProtectMeSumActivity extends Activity {
 
 	@Override
 	public void onCreate(Bundle icicle)
 	{
 		super.onCreate(icicle);
 		setContentView(R.layout.sum);
 
 		((WebView) findViewById(R.id.helloWebView)).loadUrl("file:///android_asset/docs/sum.html");
 		
 		((Button) findViewById(R.id.btn_dismiss_id)).setOnClickListener(new OnClickListener() {
 			public void onClick(View v)
 			{
 				finish();
 			}
 		});
 		
 		((Button) findViewById(R.id.btn_about_id)).setOnClickListener(new OnClickListener() {
 			public void onClick(View v)
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(ProtectMeSumActivity.this);
 		          builder.setTitle("Protect Me by\nRocket Mobile Soft");
		          builder.setMessage("Version 2.0\nSupport at support@rocketmbsoft.com");
 		          builder.setNeutralButton("OK", null);
 		          builder.show(); 
 			}
 		});
 
 	} 
 
 }
