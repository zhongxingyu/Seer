 package com.example.pineapple;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout.LayoutParams;
 
 @SuppressLint("NewApi")
 public class MainActivity extends BaseActivity {
 	
 	private final String TAG = MainActivity.class.getSimpleName();
 	private double scaleX, scaleY;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_main);
 
		scaleBitmaps();
		positionBitmaps();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	//Go to the game
 	public void goToGame(View view){
 		Intent intent = new Intent(this, GameActivity.class);
 		startActivity(intent);
 	}
 	
 	public void scaleBitmaps(){
 		Display display = getWindowManager().getDefaultDisplay();
 		int screenHeight = 0;
 		int screenWidth = 0;
 		if(Build.VERSION.SDK_INT >= 13){
 			Point size = new Point();
 			display.getSize(size);
 			screenHeight = size.y;
 			screenWidth = size.x;
 		} else{
 			screenHeight = display.getHeight();
 			screenWidth = display.getWidth();
 		}
 		
 		Log.d(TAG, "Screen Width = " + screenWidth);
 		Log.d(TAG, "Screen Height = " + screenHeight);
 		
 		scaleX = screenWidth/155.;
 		scaleY = screenHeight/100.;
 		Bitmap playBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bird), (int)(Const.menuButtonWidth*scaleX), (int)(Const.menuButtonHeight*scaleY), true);
 		Bitmap settingsBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bird), (int)(Const.menuButtonWidth*scaleX), (int)(Const.menuButtonHeight*scaleY), true);
 		Bitmap highscoreBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bird), (int)(Const.menuButtonWidth*scaleX), (int)(Const.menuButtonHeight*scaleY), true);
 		ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
 		playButton.setImageBitmap(playBitmap);
 	}
 	
 	public void positionBitmaps(){
 		ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
 		ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);
 		ImageButton highscoreButton = (ImageButton) findViewById(R.id.highscoreButton);
 		
 		Log.d(TAG, "" + (int)(Const.menuButtonWidth*scaleX));
 		
 		LayoutParams params = new LayoutParams((int)(Const.menuButtonWidth*scaleX), (int)(Const.menuButtonHeight*scaleY));
 		
 	    params.setMargins((int)(Const.menuButtonXPadding*scaleX), (int)(Const.menuButtonYPadding*scaleY), (int)((Const.menuButtonXPadding+Const.menuButtonWidth)*scaleX), (int)((Const.menuButtonYPadding+Const.menuButtonHeight)*scaleY));
 		playButton.setLayoutParams(params);
 		
 		params.setMargins((int)(Const.menuButtonXPadding*scaleX), (int)((Const.menuButtonYPadding + Const.menuButtonHeight + Const.menuButtonSpace)*scaleY), (int)((Const.menuButtonXPadding+Const.menuButtonWidth)*scaleX), (int)((Const.menuButtonYPadding + 2*Const.menuButtonHeight + Const.menuButtonSpace)*scaleY));
 		settingsButton.setLayoutParams(params);
 		
 		params.setMargins((int)(Const.menuButtonXPadding*scaleX), (int)((Const.menuButtonYPadding + 2*Const.menuButtonHeight + 2*Const.menuButtonSpace)*scaleY), (int)((Const.menuButtonXPadding+Const.menuButtonWidth)*scaleX), (int)((Const.menuButtonYPadding + 3*Const.menuButtonHeight + 2*Const.menuButtonSpace)*scaleY));
 		highscoreButton.setLayoutParams(params);
 		
 	}
 
 }
