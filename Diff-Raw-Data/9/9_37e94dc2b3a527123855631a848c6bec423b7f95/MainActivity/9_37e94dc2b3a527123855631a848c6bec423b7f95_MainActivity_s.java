 package com.lghs.stutor;
 
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Toast;
 import android.app.Activity;
 import android.content.Intent;
 
 public class MainActivity extends Activity {
 	protected void onCreate(Bundle savedInstanceState) {
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		Random rand = new Random();
 		if (rand.nextInt(1) == 0) {
 		} else {
 
 		}
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// I really need to rework the splash screen or we need an amazing login
 		// page.
 		new Timer().schedule(new TimerTask() {
 			@Override
 			public void run() {
 
 				// this code will be executed after 2 seconds
				Intent intent = new Intent(getApplicationContext(), Session.class);
 				startActivity(intent);
 				finish();
 			}
 		}, 2000);
 	}
 
 	public void onBackPressed() {
 		Toast msg = Toast.makeText(this, "Goodbye!", Toast.LENGTH_SHORT);
 		msg.show();
 		finish();
 	}
 }
