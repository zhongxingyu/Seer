 package com.example.spaceshiphunter;
 
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.WindowManager;
 import android.widget.AbsoluteLayout;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 public class Mission extends Activity implements OnTouchListener {
 
 	int nextActivity;
 	public static final String PREFS_NAME = "MyPrefsFile";
 	boolean silent;
 	MediaPlayer mp;
 	float volume = 0.2f;
 	ImageView imageView;
 	static Point dispXY;
 	int homex;
 	int homey;
 	int m1x;
 	int m1y;
 	int m2x;
 	int m2y;
 	ImageButton mission1;
 	ImageButton mission2;
 	ImageButton home;
 	RelativeLayout screenLayout;
 	RelativeLayout bg;
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		
 		
 		dispXY = new Point();
 		setContentView(R.layout.activity_mission);
 		WindowManager wm = ((WindowManager) this
 				.getSystemService(this.WINDOW_SERVICE));
 		Display display = wm.getDefaultDisplay();
 		display.getSize(dispXY);
 
 		homex = ((dispXY.x) / 4);
 		homey = ((dispXY.y) / 9) * 6;
 		
 		m1x = ((dispXY.x) / 4) * 2; 
 		m1y = ((dispXY.y) / 9) * 6;
 		
 		m2x = ((dispXY.x) / 4) * 3;
 		m2y = ((dispXY.y) / 9) * 4;
 		
 		home = new ImageButton(this);
 		home.setBackgroundResource(R.drawable.planet_home);
 		home.setId(49);
 
 		mission1 = new ImageButton(this);
 		mission1.setBackgroundResource(R.drawable.planet);
 		mission1.setId(50);
 		
 		mission2 = new ImageButton(this);
 		mission2.setBackgroundResource(R.drawable.planet_grey);
 		mission2.setId(51);
 	
 		
 		RelativeLayout.LayoutParams homepos = new LayoutParams(75, 75);
 		homepos.leftMargin = homex;
 		homepos.topMargin = homey;
 		home.setLayoutParams(homepos);
 
 		RelativeLayout.LayoutParams m1pos = new LayoutParams(75, 75);
 		m1pos.leftMargin = m1x;
 		m1pos.topMargin = m1y;
 		mission1.setLayoutParams(m1pos);
 		
 		RelativeLayout.LayoutParams m2pos = new LayoutParams(75, 75);
 		m2pos.leftMargin = m2x;
 		m2pos.topMargin = m2y;
 		mission2.setLayoutParams(m2pos);
 
 		screenLayout = (RelativeLayout) findViewById(R.id.my_frame);
 		screenLayout.addView(mission1);
 		screenLayout.addView(mission2);
 		screenLayout.addView(home);
 		mission1.setOnTouchListener(this);
 		mission2.setOnTouchListener(this);
 		home.setOnTouchListener(this);
 	
 		
 
 	}
 
 	@Override
 	protected void onResume() {
 		nextActivity = 0;
 		if (!MainActivity.silent) {
 			mp = MediaPlayer.create(getApplicationContext(), R.raw.hhavok);
 			mp.setVolume(volume, volume);
 			mp.setLooping(true);
 			mp.start();
 		} else if (MainActivity.silent == true) {
 			mp.stop();
 			mp.release();
 		}
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
		// imageView.setImageResource (R.drawable.mission_screen);
		mp.stop();
		mp.release();
		mp = null;
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.mission, menu);
 		return true;
 	}
 
 	@Override
 	// public boolean onTouch (View v, MotionEvent ev)
 	// {
 	// final int action = ev.getAction();
 	//
 	// final int evX = (int) ev.getX();
 	// final int evY = (int) ev.getY();
 	//
 	//
 	// // If we cannot find the imageView, return.
 	//
 	//
 	// int touchColor = getHotspotColor (R.id.image_areas, evX, evY);
 	// ColorTool ct = new ColorTool ();
 	// int tolerance = 25;
 	// // Now that we know the current resource being displayed we can handle
 	// the DOWN and UP events.
 	//
 	// switch (action) {
 	// case MotionEvent.ACTION_DOWN :
 	//
 	//
 	// if (ct.closeMatch (Color.BLUE, touchColor, tolerance)) {
 	// imageView.setImageResource (R.drawable.mission_screen_blue);
 	// nextActivity = 1;
 	// }
 	// else if (ct.closeMatch (Color.RED, touchColor, tolerance)) {
 	// imageView.setImageResource (R.drawable.mission_screen_red);
 	// nextActivity = 2;
 	// }
 	//
 	// break;
 	// case MotionEvent.ACTION_UP :
 	// if (nextActivity == 1) {
 	// finish();
 	// overridePendingTransition(0, 0);
 	// }
 	// else if (nextActivity == 2) {
 	// Intent i = new Intent(this, Hanger.class);
 	// i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 	// i.putExtra("FROM","mission");
 	// startActivity(i);
 	// }
 	// break;
 	// } // end switch
 	// return true;
 	// }
 	// public int getHotspotColor (int hotspotId, int x, int y) {
 	//
 	// ImageView img = (ImageView) findViewById (hotspotId);
 	// img.setDrawingCacheEnabled(true);
 	// Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
 	//
 	// img.setDrawingCacheEnabled(false);
 	// return hotspots.getPixel(x, y);
 	//
 	//
 	// }
 	protected void onStop() {
 		if (mp != null) {
 			mp.stop();
 			mp.release();
 			mp = null;
 		}
 		super.onStop();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		if (v.getId() == 49){
 			if(event.getAction()== MotionEvent.ACTION_DOWN){
 				home.setBackgroundResource(R.drawable.planet_pressed);
 				Intent j = new Intent(this, Hanger.class);
 				j.putExtra("FROM","menu");
 				startActivity(j);
 				
 				
 		
 			}
 			if(event.getAction() == MotionEvent.ACTION_UP){
 				home.setBackgroundResource(R.drawable.planet_home);
 				
 			}
 		
 			
 	}
 		if(v.getId() ==50){
 			if(event.getAction()== MotionEvent.ACTION_DOWN){
 			mission1.setBackgroundResource(R.drawable.planet_pressed);
 			Intent i = new Intent(this, Hanger.class);
 			i.putExtra("FROM","mission");
 		 	startActivity(i);
 			}
 			
 			if(event.getAction()==MotionEvent.ACTION_UP){
 				mission1.setBackgroundResource(R.drawable.planet);
 			}
 		}
 		
 		return false;
 	}
 	
 }
