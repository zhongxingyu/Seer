 package com.lghs.stutor;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class Vital extends Activity {
 	public boolean logthekitty = false;
 	public String host = "devpump.dyndns.org";
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.am2);
 	}
 }
