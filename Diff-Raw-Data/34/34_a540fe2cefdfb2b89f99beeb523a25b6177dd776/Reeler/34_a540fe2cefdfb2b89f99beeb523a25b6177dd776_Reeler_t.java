 package org.MAG;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.PixelFormat;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 
 /**
  * Responsible for hooking something and reeling it in.
  * @author UnderGear
  *
  */
 public class Reeler extends Activity implements OnTouchListener, SurfaceHolder.Callback {
 
 	private static final String TAG = "Reeler";
 	
 	private ImageView background;
 	private MySurfaceView foreground;
 	private SurfaceHolder holder;
 	private Canvas canvas;
 	
 	private SoundManager soundManager;
 	private Vibrator vibrotron;
 	
 	private WaitForHookTask waitTask; //AsyncTask - while waiting for a bite
 	private StruggleTask struggleTask; //AsyncTask - working against player while reeling
 	private AudioTask audioTask; //AsyncTask - monitor progress and deliver feedback to the player via audio
 	
 	private SharedPreferences settings;
 	
 	private Random random; //Random to determine what hooks on the line
 	
 	//For determining where a touch is on the screen relative to the center. Cartesian -> Polar
 	private static final DisplayMetrics metrics = new DisplayMetrics();
 	private int minusHalfWidth, minusHalfHeight;
 	private float currentTheta;
 	private float currentRadius;
 	private float previousTheta;
 	private float previousRadius;
 	private float x, y;
 	private float angularDelta; //Change in radians since last event
 	
 	private Sprite rod, spindle; //to be drawn on the foreground.
 	
 	private boolean fingerDown;
 	
 	//Passed in from Caster activity or calculated (at least partially) from that
 	private float distance; //how far you have cast the hook. also the distance you have to reel in.
 	private float lineStrength; //essentially health of your fishing line. depends on the quality of line you have.
 	
 	
 	private ArrayList<CatchableObject> level0, level1, level2, level3;
 	
 	private ArrayList<ArrayList<CatchableObject>> pool;
 	
 	private static CatchableObject hookedObject;
 	
 	
 	/**
 	 * Called on Activity creation. Set the background, touch listener, vibrator, load up sounds.
 	 * 
 	 * @param savedInstanceState
 	 */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         soundManager = SoundManager.getInstance();
         
         settings = getApplicationContext().getSharedPreferences("myPrefs", 0);
         
         lineStrength = settings.getInt("LineStrength", 100);
         
         Bundle extras = getIntent().getExtras(); 
         if (extras != null) {
         	distance = extras.getInt("CastDistance");
         }
         
         audioTask = new AudioTask();
         
         fillPool();
         
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         setContentView(R.layout.reeler);
         background = (ImageView)findViewById(R.id.reeler_background);
         foreground = (MySurfaceView)findViewById(R.id.reeler_foreground);
         
         holder = foreground.getHolder();
         holder.setFormat(PixelFormat.TRANSPARENT);
         
         holder.addCallback(this);
         
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         int height = metrics.heightPixels;
         int width = metrics.widthPixels;
         minusHalfHeight = -height/2;
         minusHalfWidth = -width/2;
         
         soundManager.loadSounds(SoundManager.REELABLE);
         
         vibrotron = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
         
         random = new Random();
         
         foreground.getHolder().setFormat(PixelFormat.TRANSPARENT);
         foreground.setZOrderOnTop(true);
         
         rod = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.reel_back_sprite), 0.5f, 0.5f, 0, Sprite.ALIGNMENT_CENTER);
         spindle = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.reel_handle_sprite), 0.5f, 0.5f, 0, Sprite.ALIGNMENT_CENTER);
         
         foreground.addSprite(rod);
         foreground.addSprite(spindle);
         
         waitTask = new WaitForHookTask();
         waitTask.execute();
     }
 	
 	public void onResume() {
 		audioTask.execute();
 		
 		super.onResume();
 	}
 	
 	public static CatchableObject getCatch() {
 		return hookedObject;
 	}
 	
	//TODO: populate these now! add more assets in when we can. modify levels as needed. maybe we can do this from an xml file or something like that later on.
 	private void fillPool() {
 		
 		Sprite napoleonSprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.napoleon_sprite1), 0.5f, 0.5f, 0, Sprite.ALIGNMENT_CENTER);
 		Sprite bootSprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.catch_boot), 0.5f, 0.5f, 0, Sprite.ALIGNMENT_CENTER);
 		Sprite eelSprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.catch_eel), 0.5f, 0.5f, 0, Sprite.ALIGNMENT_CENTER);
 		Sprite tireSprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.catch_tire), 0.5f, 0.5f, 0, Sprite.ALIGNMENT_CENTER);
 		CatchableObject boot = new CatchableObject(bootSprite, 5, 2000, 0, 500, false);
 		CatchableObject eel = new CatchableObject(eelSprite, 5, 2000, 50, 500, false);
 		CatchableObject tire = new CatchableObject(tireSprite, 5, 2000, 0, 500, false);
 		CatchableObject napoleon = new CatchableObject(napoleonSprite, 5, 2000, 50, 500, true);
 		
 		level0 = new ArrayList<CatchableObject>();
 		level0.add(boot);
 		level0.add(tire);
 		level0.add(eel);
 		level0.add(napoleon);
 		
 		level1 = new ArrayList<CatchableObject>();
 		level1.add(boot);
 		level1.add(tire);
 		level1.add(eel);
 		
 		level2 = new ArrayList<CatchableObject>();
 		level2.add(boot);
 		level2.add(tire);
 		level2.add(eel);
 		
 		level3 = new ArrayList<CatchableObject>();
 		level3.add(boot);
 		level3.add(tire);
 		level3.add(eel);
 		
 		pool = new ArrayList<ArrayList<CatchableObject>>();
 		pool.add(level0);
 		pool.add(level1);
 		pool.add(level2);
 		pool.add(level3);
 	}
 
 	/**
 	 * We've waited and hooked something! Allow the player to start reeling in and have the fish/king struggle
 	 */
 	private void hook() {		
 		vibrotron.vibrate(500);
 		soundManager.playSound(2, 1); //TODO: notify audio task
 		
 		int randomInt = random.nextInt(pool.get(LevelSelection.getLevel()).size());
 		
 		hookedObject = pool.get(LevelSelection.getLevel()).get(randomInt);
 		Log.d(TAG, "" + randomInt);
 		
 		background.setOnTouchListener(this);
 		struggleTask = new StruggleTask();
 		struggleTask.execute();
 	}
 	
 	/**
 	 * You caught something!
 	 */
 	private void success() {
 		if (struggleTask != null) struggleTask.cancel(true);
 		Log.e(TAG, "SUCCESS!");
 		background.setOnTouchListener(null);
 		holder.removeCallback(this);
 		if (audioTask != null) audioTask.cancel(true);
 		
 		if (hookedObject.isKing()) {
 			try {
 	        	Intent ourIntent = new Intent(Reeler.this, Class.forName("org.MAG.Shaker"));
 	        	ourIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(ourIntent);
 				finish();
 			} catch (ClassNotFoundException ex) {
 				Log.e(TAG, "Failed to jump to another activity");
 			}
 		}
 		else {
 			try {
 	        	Intent ourIntent = new Intent(Reeler.this, Class.forName("org.MAG.Rejecterator"));
 	        	ourIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(ourIntent);
 				finish();
 			} catch (ClassNotFoundException ex) {
 				Log.e(TAG, "Failed to jump to another activity");
 			}
 		}
 	}
 	
 	/**
 	 * The line snapped or something. Cast again.
 	 */
 	private void recast() {
 		Log.e(TAG, "RECAST!");
 		if (audioTask != null) audioTask.cancel(true);
 		background.setOnTouchListener(null);
 		holder.removeCallback(this);
 		
 		try {
         	Intent ourIntent = new Intent(Reeler.this, Class.forName("org.MAG.Caster"));
         	ourIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(ourIntent);
 			finish();
 		} catch (ClassNotFoundException ex) {
 			Log.e(TAG, "Failed to jump to another activity");
 		}
 	}
 	
 	/**
 	 * AsyncTask to wait a random amount of time for a fish bite.
 	 * @author undergear
 	 *
 	 */
 	private class WaitForHookTask extends AsyncTask<Void, Void, Boolean> {
 
 		/**
 		 * wait for a random amount of time before something gets hooked
 		 */
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			
 			try {
 				Thread.sleep(random.nextInt(10) * 1000 + 1000); // wait for a random amount of time. 1-11 seconds. TODO: make this depend upon the cast quality and bait? less magic numbery. wider range.
 			} catch (InterruptedException e) {
 				Log.e(TAG, e.getMessage());
 			}
 			
 			return true;
 		}
 
 		/**
 		 * after waiting, launch hook code.
 		 */
 		@Override
 		protected void onPostExecute(Boolean bite) {
 			hook();
 		}
 	}
 	
 	/** TODO: notify audioTask of counter-progress done here
 	 * AsyncTask for the fish's escape attempts after getting hooked on the line
 	 * @author undergear
 	 *
 	 */
 	private class StruggleTask extends AsyncTask<Void, Void, Boolean> {
 		
 		/**
 		 * Have whatever we hooked attempt to escape.
 		 */
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			while (true) {
 				try {
 					Thread.sleep(hookedObject.getStruggleDelay());
 					vibrotron.vibrate(300);
 					
 					if (fingerDown) lineStrength -= hookedObject.getStrength(); //based on what is hooked.
 					else distance += hookedObject.getStrength(); //depends upon what's hooked as well. you should hold down to prevent the fish from unreeling itself. will protect line from damage, though.
 					
 					if (lineStrength <= 0) return true;
 				} catch (InterruptedException e) {
 					Log.e(TAG, e.getMessage());
 				}
 			}
 		}
 		
 		/**
 		 * This is only called if the target has escaped. This thread is killed if we caught it first.
 		 * 
 		 * @param escape
 		 */
 		@Override
 		protected void onPostExecute(Boolean escape) {
 			if (escape) recast(); //The line was broken. Cast again.
 		}
 	}
 	
 	public void onPause() {
 		if (waitTask != null) waitTask.cancel(true);
 		if (struggleTask != null) struggleTask.cancel(true);
 		if (audioTask != null) audioTask.cancel(true);
 		super.onPause();
 	}
 	
 	private class AudioTask extends AsyncTask<Void, Void, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			// TODO: audio logic goes here
 			return null;
 		}
 	}
 	
 	/** TODO: notify audioTask of progress made here.
 	 * The user has touched the screen
 	 * 
 	 * @param v view touched
 	 * @param event touch event
 	 */
 	public boolean onTouch(View v, MotionEvent event) {
 		
 		//offset our touch location. it is now relative to the center of the screen.
 		event.offsetLocation(minusHalfWidth, minusHalfHeight);
 		x = event.getX(0);
 		y = event.getY(0);
 		
 		//switch based on the type of event
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_DOWN:
 			previousRadius = (float) Math.sqrt( x * x + y * y ); //Euclidean distance of touch from center of screen
 			previousTheta = (float) Math.acos( x / previousRadius ); //angle in radians of the touch relative to center of screen
 			
 			//NOTE: -40.0f is because our image isn't quite aligned on the x-axis
 			spindle.setRotation((float) (previousTheta * 180 / Math.PI) - 40.0f);
 			
 			
 			
 			drawSprites();
 			
 			fingerDown = true;
 			break;
 		case MotionEvent.ACTION_MOVE:
 			currentRadius = (float) Math.sqrt( x * x + y * y ); //distance of the touch from center of the screen
 			
 			//let's determine the angle we're at - will be in radians.
 			if (y > 0)
 				currentTheta = (float) Math.acos( x / currentRadius );
 			else
 				currentTheta = (float) -Math.acos( x / currentRadius );
 			
 			//find the angular change in radians from last update
 			angularDelta = Math.abs(currentTheta - previousTheta); 
 			if(angularDelta > Math.PI) //adjust for crossing PI
 				angularDelta = (float) ((2.0 * Math.PI) - angularDelta);
 			
 			//rotate the image of the spindle and then draw it
 			spindle.setRotation((float) (currentTheta * 180 / Math.PI) - 40.0f);
 			
 			drawSprites();
 			
 			//Log.d(TAG, "angular delta: " + angularDelta + ", distance: " + distance);
 			
 			//TODO: change how often this is played based on the rate of reeling. we might also want to have the line take damage if too fast. warning audio, too.
 			soundManager.playSound(4, 0.7f);
 			vibrotron.vibrate(50);
 			
 			//move the catch closer and see if we caught it yet
 			distance -= angularDelta; //TODO: scale this with testing? possibly take the rod into account.
 			if (distance <= 0) 
 				success();
 			
 			//updating these for next cycle through.
 			previousTheta = currentTheta; 
 			previousRadius = currentRadius;
 			break;
 		case MotionEvent.ACTION_UP:
 			fingerDown = false;
 			break;
 		}
 		return true;
 	}
 	
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) { }
 	
 	/**
 	 * Called once the MySurfaceView is ready. Draw our sprites ASAP
 	 * 
 	 * @param holder
 	 */
 	public void surfaceCreated(SurfaceHolder holder) {
 		drawSprites();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) { }
 
 	/**
 	 * Tell the surface to draw sprites if it is valid
 	 */
 	private void drawSprites() {
 		if (holder.getSurface().isValid()) {
 	        canvas = holder.lockCanvas();
 	        foreground.draw(canvas);
 	        holder.unlockCanvasAndPost(canvas);
         }
 	}
 }
