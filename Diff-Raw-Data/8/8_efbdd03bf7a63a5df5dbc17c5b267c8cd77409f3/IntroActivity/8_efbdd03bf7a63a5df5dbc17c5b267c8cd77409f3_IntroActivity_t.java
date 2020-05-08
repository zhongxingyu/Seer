 package com.kaist.crescendo.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Window;
 import android.widget.ImageView;
 
 import com.kaist.crescendo.R;
 import com.kaist.crescendo.utils.MyPref;
 
 public class IntroActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);	
 		setContentView(R.layout.activity_intro);
 		
 		Handler handler = new Handler();
 		handler.postDelayed(run, 3000);
 	}
 	
 	Runnable run = new Runnable() {
 		
 		@Override
 		public void run() {
 			/* Call Register Activity when session is not established */
 			SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
 			boolean saved = prefs.getBoolean(MyPref.KEY_SESSION, false);
 			if(saved == false)
 			{
 				Intent intent = new Intent();
 				startActivity(intent.setClass(getApplicationContext(), EntranceActivity.class));
 			}
 			else {
 				Intent intent = new Intent();
 				startActivity(intent.setClass(getApplicationContext(), MainActivity.class));
 			}
			finish();
			//overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_in_left);
 		}
 	};
 	
 	@Override
 	public void onBackPressed() {
 		//super.onBackPressed(); // it's intended
 		// consume back key
 	}
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		// TODO Auto-generated method stub
 		super.onWindowFocusChanged(hasFocus);
 		ImageView img = (ImageView)findViewById(R.id.imageanimation);
 		AnimationDrawable frameAnimation =    (AnimationDrawable)img.getDrawable();
 		frameAnimation.setCallback(img);
 		frameAnimation.setVisible(true, true);
 		frameAnimation.start();
 	}
 }
