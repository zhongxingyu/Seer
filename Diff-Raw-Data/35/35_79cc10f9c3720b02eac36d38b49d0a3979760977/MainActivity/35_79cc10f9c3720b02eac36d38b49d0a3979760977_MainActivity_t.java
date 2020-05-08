 package com.rhythm.app;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity 
 {
 
	private static float bpm = 120;
 	private static HashMap<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();
 	
 	SoundPool pool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
 	Integer clickID = null;
 	Timer metronome;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		View.OnTouchListener tapTouched = new View.OnTouchListener()
 		{
 
 			@Override
 			public boolean onTouch(View view, MotionEvent event) 
 			{
 				onTapTouched(event);
 				return true;
 			}
 			
 		};
 		
 		((Button)this.findViewById(R.id.button1)).setOnTouchListener(tapTouched);
 		
 		loadAssets();
 	}
 	
 	@Override 
 	protected void onResume()
 	{
 		super.onResume();
 		
 		if(metronome != null) metronome.cancel();
 		metronome = new Timer();
 		
 		TimerTask metronomeTick = new TimerTask()
 		{
 
 			@Override
 			public void run() 
 			{
 				//Play the given sound with leftVolume 100%, right volume 100%, lowest priority, no looping, normal speed
 				pool.play(clickID, 1, 1, 10, 0, 1);
 			}
 			
 		};
 		
		metronome.schedule(metronomeTick, 1000, (long)(60000f/bpm));
 	}
 	
 	@Override 
 	protected void onPause()
 	{
 		super.onPause();
 		
 		metronome.cancel();
 	}
 	
 	private void loadAssets()
 	{
 		loadFile("one_single");
 		loadFile("one_rest");
 		loadFile("two_single");
 		loadFile("two_rest");
 		loadFile("four_single");
 		loadFile("four_rest");
 		loadFile("eight_injoin");
 		loadFile("eight_endjoin");
 		loadFile("eight_single");
 		loadFile("eight_rest");
 		loadFile("sixteen_injoin");
 		loadFile("sixteen_endjoin");
 		loadFile("sixteen_single");
 		loadFile("sixteen_rest");
 		loadFile("sixteen_cutjoin");
 		
 		try 
 		{
 			clickID = pool.load(this.getAssets().openFd("click.wav"), 1);
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void loadFile(String location)
 	{
 		//Asset loading here
 		try
 		{
 			AssetManager assets = getAssets();
 			
 			InputStream stream = assets.open(location + ".png");
 			Bitmap aBitmap = BitmapFactory.decodeStream(stream);
 			
 			bitmaps.put(location, aBitmap);
 		}
 		catch(IOException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 
 	public void onTapTouched(MotionEvent event)
 	{
 		
 	}
 	
 	public static Bitmap getBitmap(String name)
 	{
 		return bitmaps.get(name);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
