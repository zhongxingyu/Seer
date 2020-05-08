 package com.howfun.android.antguide;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 
 import com.howfun.android.antguide.utils.Utils;
 
 public class Splash extends Activity {
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.splash);
 
 		//TODO: do some db init word
 		new Handler() {
 		}.postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				Intent i = new Intent(Splash.this,MainPage.class);
 				startActivity(i);
 				Splash.this.finish();
 
 			}
		}, 1000L);
 	}
 
 }
