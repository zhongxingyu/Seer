 package com.github.barcodescanner.activities;
 
 import com.github.barcodescanner.R;
 import com.github.barcodescanner.camera.CameraActivity;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.view.Window;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "MainActivity";
 	
 	private boolean isOwner;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_main);
 		isOwner = getIntent().getExtras().getBoolean("isOwner");
 		
 		if (isOwner) {
			((TextView) findViewById(R.id.user_message)).setText(R.string.welcome_scan_mode);
 		} else {
			((TextView) findViewById(R.id.user_message)).setText(R.string.welcome_admin_mode);
 		}
 	}
 
 	public void enterCamera(View view) {
 		Intent intent = new Intent(this, CameraActivity.class);
 		intent.putExtra("isOwner", isOwner);
 		startActivity(intent);
 	}
 	
 	public void enterHelp(View view) {
 		Intent intent = new Intent(this, HelpActivity.class);
 		startActivity(intent);
 	}
 	
 	public void enterDatabase(View view) {
 		Intent intent = new Intent(this, DatabaseActivity.class);
 		intent.putExtra("isOwner", isOwner);
 		startActivity(intent);
 	}
 }
