 package com.lghs.stutor;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.RotateAnimation;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 
 public class Vital extends Activity {
 	public boolean logthekitty = false;
 	public String host = "devpump.dyndns.org";
 	ImageView imageView;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.am1);
 		imageView = (ImageView) findViewById(R.id.imageView1);
 		final int[] imageArray = { R.drawable.stutordance,
 				R.drawable.stutordance2 };
 
 		final Handler handler = new Handler();
 		Runnable runnable = new Runnable() {
 			int i = 0;
 
 			public void run() {
 				
 				imageView.setImageResource(imageArray[i]);
 				i++;
 				if (i > imageArray.length - 1) {
 					i = 0;
 				}
				handler.postDelayed(this, 1000); // for interval...
 			}
 		};
 		handler.postDelayed(runnable, 2000); // for initial delay..
 
 	}
 }
