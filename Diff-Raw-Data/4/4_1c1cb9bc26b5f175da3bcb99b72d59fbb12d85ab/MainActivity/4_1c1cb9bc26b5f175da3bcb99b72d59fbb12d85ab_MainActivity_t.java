 package com.example.myforg;
 
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 @SuppressLint("UseSparseArrays")
 public class MainActivity extends Activity {
 	private SoundPool sp;
 	private int addTarget;
 	private HashMap<Integer, Integer> spMap;
 	private int source;
 	private int tagretsource;
 	private int timelimit;
 	private int maxSource;
 	private boolean hasPlaySound;
 	private Timer timer;
 	private Button btnFrog;
 	private Button btnFrogback;
 	private boolean start;
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 //	TextView totalSouce;
 	TextView totalCount;
 	TextView remainTime;
 	TextView remainCount;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		sp = new SoundPool(2, AudioManager.STREAM_SYSTEM, 0);
 		spMap = new HashMap<Integer, Integer>();
 
 		spMap.put(1, sp.load(this, R.raw.clap, 1));
 		totalCount = (TextView) findViewById(R.id.totalSouce);
 //		totalCount = (TextView) findViewById(R.id.totalCount);
 		remainTime = (TextView) findViewById(R.id.remainTime);
 		remainCount = (TextView) findViewById(R.id.remainCount);
 		
 		source=0;
 		timelimit=500;
 		tagretsource=20;
 		addTarget=0;
 		maxSource=this.getMaxSource();
 		hasPlaySound=false;
 		start=false;
 		
 		updateText();
 		updateTime();
 //
 		btnFrog = (Button) findViewById(R.id.button1);// ȡťԴ
 		btnFrogback = (Button) findViewById(R.id.button2);//ȡťԴ
 		// Btn1Ӽ
 		btnFrog.setOnClickListener(new Button.OnClickListener() {// 
 			@Override
 			public void onClick(View v) {
 				if(start==false)
 				{
 					start=true;
 					if(timer==null)
 					{
 					timer = new Timer();
 					timer.schedule(timetask, 0, 10);
 					}
 				}
 				source++;
 				tagretsource--;
 				if (tagretsource == 0) {
 					addTarget++;
 					tagretsource = 20 + addTarget;
 					timelimit += 500;
 				}
 				if (source > maxSource) {
					maxSource=source;
 					setMaxSource(source);
 					if (hasPlaySound == false) {
 						hasPlaySound = true;
 						sp.play(spMap.get(spMap.get(1)), 50, 50, 1, 0, 1);
 					}
 				}
 				updateText();
 			}
 		});
 		
 		
 		btnFrogback.setOnClickListener(new Button.OnClickListener() {// 
 			@Override
 			public void onClick(View v) {
 				source=0;
 				timelimit=500;
 				tagretsource=20;
 				addTarget=0;
 				updateText();
 				updateTime();
 				start=false;
				hasPlaySound = false;
 				btnFrog.setClickable(true);
 			}
 		});
 //		
 //		
 //		
 //		// BtnGo Ӽҳת
 //		BtnGo.setOnClickListener(new Button.OnClickListener() {// 
 //			@Override
 //			public void onClick(View v) {
 //				String strTmp = String.valueOf(a++);
 //				Ev1.setText(strTmp);
 //				sp.play(spMap.get(spMap.get(1)), 50, 50, 1, 0, 1);
 //				Intent intentGo = new Intent();
 //				intentGo.setClass(MainActivity.this, ChronometerActivity.class);
 //				startActivity(intentGo);
 //				finish();
 //			}
 //
 //		});
 
 		
 	}
 	
 	
 	Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			addTenMMS();
 			super.handleMessage(msg);
 		}
 	};
 	
 
 	TimerTask timetask = new TimerTask() {
 		public void run() {
 			Message message = new Message();
 			message.what = 1;
 			handler.sendMessage(message);
 		}
 	};
 	
 	//ʮ
 	private void addTenMMS() {
 		if(start)
 		{
 			timelimit -= 1;
 		}
 		if (timelimit <= 0) {
 			timelimit=0;
 //			timer.cancel();
 			btnFrog.setClickable(false);
 		}
 		
 		updateTime();
 	}
 	
 
 	private void updateText() {
 		totalCount.setText("Source:" + source);
 //		totalCount.setText("Source:" + tagretsource);
 		remainCount.setText("Target:" + tagretsource);
 	}
 	
 	private void updateTime(){
 		remainTime.setText("Remain:" + Double.toString((double)timelimit/100) + "s");
 	}
 	
 	private int getMaxSource() {
 //		return 5;
 		// ȡSharedPreferences
 		Context ctx = MainActivity.this;
 		SharedPreferences sp = ctx.getSharedPreferences("SP", MODE_PRIVATE);
 		return  sp.getInt("maxSource", 0);
 	}
 	
 	
 	private void setMaxSource(int source){
 		Context ctx = MainActivity.this;
 		SharedPreferences sp = ctx.getSharedPreferences("SP", MODE_PRIVATE);
 		// 
 		Editor editor = sp.edit();
 		editor.putInt("maxSource",source );
 		editor.commit();
 		return ;
 	}
 	
 }
