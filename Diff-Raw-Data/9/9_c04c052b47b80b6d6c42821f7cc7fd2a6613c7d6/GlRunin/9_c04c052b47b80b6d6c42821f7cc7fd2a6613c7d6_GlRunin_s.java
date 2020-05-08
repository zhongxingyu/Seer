 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.tware.glrun;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Color;
 import android.media.MediaPlayer;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Wrapper activity demonstrating the use of {@link GLSurfaceView}, a view
  * that uses OpenGL drawing into a dedicated surface.
  */
 public class GlRunin extends Activity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Create our Preview view and set it as the content of our
         // Activity
         formatter = new SimpleDateFormat("HH:mm:ss");
         beginTime = formatter.format(new Date());
         
         mGLSurfaceView = new GLSurfaceView(this);
         mGLSurfaceView.setRenderer(new CubeRenderer(false));
         setContentView(mGLSurfaceView);
         
         this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
         
         timeView = new TextView(this);
         timeView.setText("Loading...");
         
         this.addContentView(timeView,new LayoutParams(	LayoutParams.MATCH_PARENT, 
         												LayoutParams.MATCH_PARENT));
         task = new TimerTask(){
         	@Override
         	public void run()
         	{
         		if (min >= CPUTIME)
         			cpuLoader.setStop();
         		
         		if (hou >= RUNTIME){
         			uHandler.sendEmptyMessage(1);
         		}
         		else if (isStop){
         			uHandler.sendEmptyMessage(4);
         			cpuLoader.setStop();
         		}
         		else
         			uHandler.sendEmptyMessage(0);
         		
         	}
         };
 
         timer.schedule(task, 1000, 1000);
 
 		new Thread(){
 			@Override
 			public void run()
 			{
 				while (!Environment.getExternalStorageState()
 						.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
 				{
 					try {
 						Thread.sleep(2000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				uHandler.sendEmptyMessage(3);
 				Log.e(TAG, "3D Thread Quit!");
 			}
 		}.start();
 		
         uHandler = new Handler(){
         	public void handleMessage(Message msg)
         	{
         		switch (msg.what)
         		{
         			case 0:
         				currTime = formatter.format(new Date());
        				if (min>=59){
         					sec = 0;
         					min = 0;
         					hou ++;
         				}
        				if(sec>=59){
         					sec = 0;
         					min ++;
         				}
        				sec ++;
         				timeView.setText("    Runin Time: " + RUNTIME + "Hour(s)"
         						+ "\n   Begin  Time: " + beginTime 
         						+ "\nCurrent  Time: " + currTime
         						+ "\nElapsed Time: " + String.format("%02d:%02d:%02d", hou, min, sec)
         						);
             		break;
             		
         			case 1:
         				setResults(0); /* Pass */
         				break;
         			
         			case 2:
         				setResults(1); /* Fail */			
         				break;
         			
         			case 3:
         				if (! getConfig.init()){
         					setResults(1); /* Fail */;
         					return;
         				}
         				int tempTime = getConfig.getRuninTime();
         				if (tempTime != 0){
         					RUNTIME = tempTime;
         				}
         				
         				int tempCPU = getConfig.getCPUTestTime();
         				if (tempCPU != 0){
             				cpuLoader.CpuTest();
             				CPUTIME = tempCPU;
         				}
         				
         				Toast.makeText(getApplicationContext(), 
         								"Runin Time: " + RUNTIME + "H"
         								+ "\n CPU TEST: " + CPUTIME +"m"
         								, 
         						Toast.LENGTH_LONG).show();
         				break;
         				
         			case 4:
         				setResults(2); /* Stop */
         				break;
         		}
         	}
         };
     }
 
     public void setResults(int state)
     {
 		if (mGLSurfaceView!=null)	mGLSurfaceView.onPause();
 		if (mediaplayer!=null) 		mediaplayer.stop();    
 		if (timer != null){	timer.cancel(); timer = null; Log.e(TAG, "3D Timer Quit!");}
 		if (!isStop)	isStop = true;
 
 		switch (state)
 		{
 			case 0:
 				timeView.setBackgroundColor(Color.GREEN);				
 				timeView.setText("Pass");
 				Log.i(TAG, "3D Pass!");
 				break;
 			case 1:
 				timeView.setBackgroundColor(Color.RED);				
 				timeView.setText("Fail");
 				Log.i(TAG, "3D Fail!");
 				break;
 			case 2:
 				timeView.setBackgroundColor(Color.RED);				
 				timeView.setText("Stop");
 				Log.i(TAG, "3D Stop by someone!");
 				break;
 		}
 		timeView.setTextSize(160);
 		timeView.setPadding(0, 0, 0, 0);
 		timeView.setTextColor(Color.WHITE);
 		timeView.setGravity(Gravity.CENTER);
     }
     
     
     @Override
     protected void onResume() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onResume();
         mGLSurfaceView.onResume();
         
         if (mediaplayer != null){
         	mediaplayer.start();
         	Log.e("3D Music", "music is Playing...");
         }else{
         	mediaplayer = MediaPlayer.create(this, R.raw.neocore2);
         	mediaplayer.setLooping(true);
 			mediaplayer.start();
         	Log.e("3D Music", "music Started...");	
         }
     }
 
     @Override
     protected void onPause() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onPause();
         mGLSurfaceView.onPause();
         if (mediaplayer!=null){
         	mediaplayer.pause();
         	Log.e(TAG, "music Paused!");
         }
     }
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (isStop)
 			{
 				new AlertDialog.Builder(this).setMessage("Are you sure to Quit?")
 					.setTitle("Warning").setIcon(R.drawable.ic_launcher)
 					.setCancelable(false).setPositiveButton("Ok", new OnClickListener(){
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							System.exit(0);
 						}
 					})
 					.setNegativeButton("Cancel", null).show();
 			}else{
 				Toast.makeText(getApplicationContext(), "It's Running...", Toast.LENGTH_LONG).show();
 			}
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(0, 0, 0, "Stop");
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch(item.getItemId())
         {
     		case 0:
     			Log.d(TAG, "Runin Stoped!");
     			if (! isStop ) isStop = true;
     			break;
         }
         return false;
     }    
     
     
     private GLSurfaceView mGLSurfaceView;
     private TimerTask task;
     private Timer timer = new Timer();
     private TextView timeView;
     private Handler uHandler;
     private int sec = 0;
     private int min = 0;
     private int hou = 0;
     private int RUNTIME = 2;
     private int CPUTIME = 2;
     private String beginTime;
     private String currTime;
     private SimpleDateFormat formatter;
     private MediaPlayer mediaplayer;
     private String TAG = "GLRunin";
     private Boolean isStop = false;
 }
