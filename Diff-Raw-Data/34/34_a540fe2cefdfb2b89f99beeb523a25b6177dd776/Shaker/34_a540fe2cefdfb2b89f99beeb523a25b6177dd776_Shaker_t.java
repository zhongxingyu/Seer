 package org.MAG;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.PixelFormat;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.Window;
 import android.view.WindowManager;
 
 /**
  * Activity for shaking a king down for treasure.
  * @author undergear
  *
  */
 public class Shaker extends Activity implements SensorEventListener, SurfaceHolder.Callback {
 
 	private static final String TAG = "Shaker";
 	
 	//foreground view
 	private MySurfaceView foreground;
 	private SurfaceHolder holder;
	private Canvas canvas;
 	
 	//the king you caught.
 	private CatchableObject caught;
 	
 	//sprites to draw on foreground
 	private Sprite king, coinPile, fallingLoot;
 	
 	//hardware and a shake previous accelerometer reading
 	private SensorManager sensorManager;
 	private Sensor accelerometer;
 	private SoundManager soundManager;
 	private Vibrator vibrotron;
 	private float lastReading;
 	private float accelerationThreshold = 2.5f;
     private double totalAcceleration;
 	
 	private int timbersShivered; //a counter for how many times the user has shaken the king
 	
 	private AudioTask audioTask;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		setContentView(R.layout.shaker);
         
         foreground = (MySurfaceView)findViewById(R.id.shaker_foreground);
         
         caught = Reeler.getCatch();
         king = caught.getSprite();
         coinPile = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.coin_pile1), 0.5f, 1.0f, 0, Sprite.ALIGNMENT_BOTTOM);
         
         foreground.addSprite(king);
         
         holder = foreground.getHolder();
         holder.setFormat(PixelFormat.TRANSPARENT);
         
         holder.addCallback(this);
         soundManager = SoundManager.getInstance();
 		soundManager.loadSounds(SoundManager.SHAKABLE);
 		
 		vibrotron = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
 		sensorManager = (SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
         accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         
         foreground.getHolder().setFormat(PixelFormat.TRANSPARENT);
         foreground.setZOrderOnTop(true);
 	}
 	
 	@Override
 	public void onPause() {
 		sensorManager.unregisterListener(this);
 		holder.removeCallback(this);
 		if (audioTask != null) audioTask.cancel(true);
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
 		
 		audioTask = new AudioTask();
 		audioTask.execute();
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
 	
 	public void onSensorChanged(SensorEvent event) {
 		
         totalAcceleration += Math.pow(event.values[SensorManager.DATA_X]/SensorManager.GRAVITY_EARTH, 2.0);
         totalAcceleration += Math.pow(event.values[SensorManager.DATA_Y]/SensorManager.GRAVITY_EARTH, 2.0);
         totalAcceleration += Math.pow(event.values[SensorManager.DATA_Z]/SensorManager.GRAVITY_EARTH, 2.0);
         totalAcceleration = Math.sqrt(totalAcceleration);
         
         if ((totalAcceleration < accelerationThreshold) && (lastReading > accelerationThreshold)) {
         	timbersShivered++;
         	
         	vibrotron.vibrate(300);
         	soundManager.playSound(1, 1);
         	
         	//beat up the king, make him drop treasure based on how many shakes we've done.
         	//TODO: we should look up the drawables based on which level we're on.
         	switch (timbersShivered) { //TODO: set cases to add coins to the surfaceview. we need falling coins now. rotate the king a bit more.
         	case 5:
         		king.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.napoleon_sprite2));
         		
         		break;
         	case 7:
         		foreground.addSprite(coinPile);
         		break;
         	case 10:
         		king.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.napoleon_sprite3));
         		break;
         	case 12:
         		coinPile.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.coin_pile2));
         		
         	case 15:
         		king.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.napoleon_sprite4));
         		
         		break;
         	case 17:
         		coinPile.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.coin_pile3));
         		
         		break;
         	case 20:
         		sensorManager.unregisterListener(this);
         		holder.removeCallback(this);
         		audioTask.cancel(true);
         		//Launch the next activity! Throw the king back.
         		try {
                 	Intent ourIntent = new Intent(Shaker.this, Class.forName("org.MAG.Rejecterator"));
                 	ourIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         			startActivity(ourIntent);
         			finish();
         		} catch (ClassNotFoundException ex) {
         			Log.e(TAG, "Failed to jump to another activity");
         		}
         		break;
         	default:
         		lastReading = (float) totalAcceleration;
         		return;
         	}
         	drawSprites();
         }
         lastReading = (float) totalAcceleration;
         
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) { }
 	
 	/**
 	 * The surface is ready. Let's draw on it!
 	 * 
 	 * @param holder
 	 */
 	public void surfaceCreated(SurfaceHolder holder) {
 		drawSprites();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) { }
 
 	/**
 	 * Tell the foreground to draw its sprites
 	 */
 	private void drawSprites() {
 		if (holder.getSurface().isValid()) {
	        canvas = holder.lockCanvas();
 	        foreground.draw(canvas);
 	        holder.unlockCanvasAndPost(canvas);
         }
 	}
 	
 	/**
 	 * Task to handle the narrator giving instructions to the player
 	 * @author undergear
 	 *
 	 */
 	private class AudioTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e1) {
 				Log.e(TAG, e1.getMessage());
 			}
 			
 			//TODO: based on the level, play audio!
 			switch (LevelSelection.getLevel()) {
 			case 0:
 				soundManager.playSound(2, 1);
 				break;
 			default:
 				break;
 			}
 			
 			while (true) {
 				try {
 					Thread.sleep(10000);
 				} catch (InterruptedException e) {
 					Log.e(TAG, e.getMessage());
 				}
 				//TODO: play audio instructions here.
 			}
 		}
 	}
 }
