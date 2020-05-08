 /**
  * 
  */
 package com.september.tableroids;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.WindowManager;
 
 import com.september.tableroids.model.Sprite;
 import com.september.tableroids.model.elements.Asteroid;
 import com.september.tableroids.model.elements.Boom;
 import com.september.tableroids.model.elements.Rocket;
 import com.september.tableroids.model.elements.Shoot;
 
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
 	private static List<Sprite> spriteToAdd;
 	private static List<Sprite> spriteToRemove;
 	private final static int MAX_ENEMIES = 3;
 	private static int astrocount = 0;
 	private int width,height;
 
 	// the fps to be displayed
 	private String avgFps;
 	public void setAvgFps(String avgFps) {
 		this.avgFps = avgFps;
 	}
 
 	public List<Sprite> getSprites() {
 			 if(spriteInScene == null) {
 					spriteInScene = Collections.synchronizedList(new LinkedList<Sprite>());
 				}
 		return spriteInScene;
 	}
 
 	public List<Sprite> getSpritesToAdd() {
 		if(spriteToAdd == null) {
 			spriteToAdd = new LinkedList<Sprite>();
 		}
 		return spriteToAdd;
 	}
 
 	public void resetLists() {
 		spriteToAdd = null;
 		spriteToRemove = null;
 	}
 
 	public List<Sprite> getSpritesToRemove() {
 		if(spriteToRemove == null) {
 			spriteToRemove = new LinkedList<Sprite>();
 		}
 		return spriteToRemove;
 	}
 	
 	
 	public Bitmap getBitmap(String name) {
 		AssetManager manager = getContext().getAssets();
 		try {
 			return BitmapFactory.decodeStream (manager.open(name));
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	public MainGamePanel(Context context) {
 		super(context);
 		// adding the callback (this) to the surface holder to intercept events
 		getHolder().addCallback(this);
 
 
 
 		AssetManager manager = getContext().getAssets();
 
 		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		Point p = new Point();
 		height = display.getHeight();
 		width = display.getWidth();
 		//int scaleSize = display.getWidth()/BASE_WIDTH;
 		//final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 		//		
 		//		Options myOptions = new Options();
 		//		myOptions .inScaled = false;
 		//		myOptions .inScreenDensity = metrics.densityDpi;
 		//		myOptions .inTargetDensity = metrics.densityDpi;
 
 		try {
 			
 			Sprite backGround = new Sprite(this,
					BitmapFactory.decodeStream (manager.open("farback.gif")),
 					0,0,
 					width,height,
 					1,1,1,1
 					);
 			
 			Rocket rocket = new Rocket(this,
 					BitmapFactory.decodeStream (manager.open("ship_116x64.png"))
 					, width/2, height-64-5	// initial position
 					, 116, 64	// width and height of sprite
 					, 10, 4,1,1);
 			
 //			Bitmap asteroidBMP = BitmapFactory.decodeStream (manager.open("asteroids.png"));
 //			
 //			int asteroidareas = display.getWidth()/3;
 //			Random random = new Random();
 			
 //			Asteroid astro1 = new Asteroid(this,asteroidBMP
 //					,10+random.nextInt(asteroidareas),-50-random.nextInt(10)
 //					,124,123
 //					,5,4,4,1
 //					);
 //			
 //			astro1.setFixedFrame(random.nextInt(15));
 //			astro1.setRuledByGarbage(false);
 //			
 //			Asteroid astro2 = new Asteroid(this,asteroidBMP
 //					,asteroidareas+10+random.nextInt(asteroidareas),-50-random.nextInt(10)
 //					,124,123
 //					,5,4,4,1
 //					);
 //			
 //			astro2.setFixedFrame(random.nextInt(15));
 //			astro2.setRuledByGarbage(false);
 //			
 //			Asteroid astro3 = new Asteroid(this,asteroidBMP
 //					,(asteroidareas*2)+random.nextInt(asteroidareas),-50-random.nextInt(10)
 //					,124,123
 //					,5,4,4,1
 //					);
 //			
 //			astro3.setFixedFrame(random.nextInt(15));
 //			astro3.setRuledByGarbage(false);
 //			
 //			
 //			
 //			rocket.addCollision(astro1);
 //			rocket.addCollision(astro2);
 //			rocket.addCollision(astro3);
 
 			getSprites().add(backGround);
 			getSprites().add(rocket);
 //			getSprites().add(astro1);
 //			getSprites().add(astro2);
 //			getSprites().add(astro3);
 			
 
 //			Boom explosion = new Boom(this,this.getBitmap("explosion.png"),
 //					100,100,
 //					320,320,
 //					100,5,5,1);
 			
 			//this.getSpritesToAdd().add(explosion);
 			
 			//getSprites().add(explosion);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	// FPS and number of frames in the animation
 
 		// create the game loop thread
 		thread = new MainThread(getHolder(), this);
 
 		// make the GamePanel focusable so it can handle events
 		setFocusable(true);
 	}
 	
 	public int addAsteroids(Sprite collider) throws IOException {
 		
 		if(astrocount < MAX_ENEMIES) {
 		
 			Bitmap asteroidBMP = BitmapFactory.decodeStream (getContext().getAssets().open("asteroids.png"));
 			Random random = new Random();
 			//for(int x = astrocount; x< MAX_ENEMIES; x++) {
 				Asteroid astro1 = new Asteroid(this,asteroidBMP
 						,random.nextInt(width),-50-random.nextInt(10)
 						,124,123
 						,5,4,4,1
 						);
 				
 				astro1.setFixedFrame(random.nextInt(15));
 				astro1.setRuledByGarbage(false);
 				collider.addCollision(astro1);
 				getSpritesToAdd().add(astro1);
 				for(Sprite s: getSprites()) {
 					if(s instanceof Rocket) {
 						s.addCollision(astro1);
 					}
 					else if(s instanceof Shoot) {
 						s.addCollision(astro1);
 					}
 				}
 				astrocount++;
 			//}
 				return addAsteroids(collider);
 		}
 		
 		return astrocount;
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
 	public boolean onTouchEvent(final MotionEvent event) {
 			if(event.getAction() == MotionEvent.ACTION_DOWN) {
 				synchronized (spriteInScene) {
 					for (Sprite sprite: getSprites()) {
 						sprite.onTouch(event);
 					}
 				}
 			
 		}
 		
 		
 
 		return true;
 
 	}
 
 	public void render(Canvas canvas) {
 		canvas.drawColor(Color.BLACK);
 		garbage(canvas);
 		for (Sprite sprite: getSprites()) {
 			sprite.draw(canvas);
 		}
 
 		//		for(Sprite sprite: getSprites()) {
 		//			sprite.draw(canvas);
 		//		}
 
 		// display fps
 		displayFps(canvas, avgFps);
 	}
 
 	/**
 	 * This is the game update method. It iterates through all the objects
 	 * and calls their update method if they have one or calls specific
 	 * engine's update method.
 	 */
 
 	private void garbage(Canvas canvas) {
 		for (Sprite sprite: getSprites()) {
 			if((new Rect(0, 0, canvas.getWidth(), canvas.getHeight())).intersect(new Rect(sprite.getX(),sprite.getY(),sprite.getX()+sprite.getSpriteWidth(),sprite.getY()+sprite.getSpriteHeight()))) {
 				sprite.setRuledByGarbage(true);
 			}
 			else {
 				if(sprite.isRuledByGarbage()) {
 					getSpritesToRemove().add(sprite);
 				}
 			}
 		}
 	}
 	
 	private Rocket getRocket() {
 		for(Sprite s:getSprites()) {
 			if(s instanceof Rocket) {
 				return (Rocket) s;
 			}
 		}
 		return null;
 	}
 
 	public void update() {
 		
 		for(Sprite toRemove: getSpritesToRemove()) {
 			if(toRemove instanceof Asteroid) {
 				astrocount--;
 			}
 			getSprites().remove(toRemove);
 		}
 		
 		for(Sprite toAdd: getSpritesToAdd()) {
 			
 			getSprites().add(toAdd);
 		}
 
 	
 
 		resetLists();
 
 		for (Sprite sprite: getSprites()) {
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
