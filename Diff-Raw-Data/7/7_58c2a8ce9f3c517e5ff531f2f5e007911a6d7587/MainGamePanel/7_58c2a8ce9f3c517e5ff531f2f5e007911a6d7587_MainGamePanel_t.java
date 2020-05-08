 /**
  * 
  */
 package com.september.tableroids;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.WindowManager;
 
 import com.september.tableroids.model.Sprite;
 import com.september.tableroids.model.elements.Rocket;
 
 /**
  * @author impaler
  * This is the main surface that handles the ontouch events and draws
  * the image to the screen.
  */
 public class MainGamePanel extends SurfaceView implements
 		SurfaceHolder.Callback {
 
 	private static final String TAG = MainGamePanel.class.getSimpleName();
 	//private static final int BASE_WIDTH = 960;
 	
 	private MainThread thread;
 	//private Sprite elaine;
 	private static List<Sprite> spriteInScene;
 
 	// the fps to be displayed
 	private String avgFps;
 	public void setAvgFps(String avgFps) {
 		this.avgFps = avgFps;
 	}
 	
 	public List<Sprite> getSprites() {
 		return spriteInScene;
 	}
 
 	public MainGamePanel(Context context) {
 		super(context);
 		// adding the callback (this) to the surface holder to intercept events
 		getHolder().addCallback(this);
 
 		// create Elaine and load bitmap
 		if(spriteInScene == null) {
 			spriteInScene = new LinkedList<Sprite>();
 		}
 		
 		AssetManager manager = getContext().getAssets();
 		
 		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		Point p = new Point();
 		//int scaleSize = display.getWidth()/BASE_WIDTH;
 		//final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 //		
 //		Options myOptions = new Options();
 //		myOptions .inScaled = false;
 //		myOptions .inScreenDensity = metrics.densityDpi;
 //		myOptions .inTargetDensity = metrics.densityDpi;
 
 		try {
 			Rocket rocket = new Rocket(this,
 					BitmapFactory.decodeStream (manager.open("ship_116x64.png"))
 					, display.getWidth()/2, display.getHeight()-64-5	// initial position
 					, 116, 64	// width and height of sprite
 					, 15, 4,1,1);
 			
 			Sprite explosion = new Sprite(this,
 					BitmapFactory.decodeStream(manager.open("explosion.png"))
 					, 10, 150	// initial position
 					, 320, 320	// width and height of sprite
 					, 15, 5,5,1);
 			
 			spriteInScene.add(rocket);
 			spriteInScene.add(explosion);
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	// FPS and number of frames in the animation
 		
 		// create the game loop thread
 		thread = new MainThread(getHolder(), this);
 		
 		// make the GamePanel focusable so it can handle events
 		setFocusable(true);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		// at this point the surface is created and
 		// we can safely start the game loop
 		thread.setRunning(true);
 		thread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d(TAG, "Surface is being destroyed");
 		// tell the thread to shut down and wait for it to finish
 		// this is a clean shutdown
 		boolean retry = true;
 		while (retry) {
 			try {
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				// try again shutting down the thread
 			}
 		}
 		Log.d(TAG, "Thread was shut down cleanly");
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		for(Sprite sprite: spriteInScene) {
			try {
				sprite.onTouch(event);
			}
			catch (Throwable t) {
				android.util.Log.d(TAG, t.getMessage());
			}
 		}
 		return true;
 	}
 
 	public void render(Canvas canvas) {
 		canvas.drawColor(Color.BLACK);
 		for(Sprite sprite: spriteInScene) {
 			sprite.draw(canvas);
 		}
 		
 		// display fps
 		displayFps(canvas, avgFps);
 	}
 
 	/**
 	 * This is the game update method. It iterates through all the objects
 	 * and calls their update method if they have one or calls specific
 	 * engine's update method.
 	 */
 	public void update() {
 		for(Sprite sprite: spriteInScene) {
 			sprite.update(System.currentTimeMillis());
 		}
 		
 	}
 
 	private void displayFps(Canvas canvas, String fps) {
 		if (canvas != null && fps != null) {
 			Paint paint = new Paint();
 			paint.setARGB(255, 255, 255, 255);
 			canvas.drawText(fps, this.getWidth() - 50, 20, paint);
 		}
 	}
 
 }
