 package com.amazon.dinorama;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 	private SurfaceHolder sh;
 	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	
 	private Resources res;
 	private GameThread thread;
 	
 	private Canvas canvas;
 	
 	private ArrayList<DisplayableObject> objects = new ArrayList<DisplayableObject>(); // drawn in order
 	private TouchButton[] buttons = new TouchButton[6];
 	private Player player = null;
 	
 	public GameView(Context context) {
 		super(context);
 		sh = getHolder();
 		sh.addCallback(this);
 		paint.setColor(Color.BLUE);
 		paint.setStyle(Style.FILL);
 		
 		res = getResources();
 		
 		// adding the callback (this) to the surface holder to intercept events
 		getHolder().addCallback(this);
 		// make the GamePanel focusable so it can handle events
 		setFocusable(true);
 		
 		// create the game loop thread
 		thread = new GameThread(getHolder(), this);
 	}
 	
 	public void createButtons() {
 		buttons[0] = new TouchButton(res, 0, 0, TouchButton.TouchButtonDirection.HI_ATK);
 		buttons[1] = new TouchButton(res, 128, 0, TouchButton.TouchButtonDirection.HI_BLK);
 		buttons[2] = new TouchButton(res, 256, 0, TouchButton.TouchButtonDirection.LEFT);
 		buttons[3] = new TouchButton(res, 0, 128, TouchButton.TouchButtonDirection.LO_ATK);
 		buttons[4] = new TouchButton(res, 128, 128, TouchButton.TouchButtonDirection.LO_BLK);
 		buttons[5] = new TouchButton(res, 256, 128, TouchButton.TouchButtonDirection.RIGHT);
 	}
 	
 	public void init() {
 		player = new Player(1, res, 0, 0);
 		objects.add(player);
 		synchronized(buttons) {
 			createButtons();
 		}
		synchronized(objects) {
 //			for (int i=0; i<100; i++)
 //				objects.add(new TestItem(res, (int)(Math.random()*1280), (int)(Math.random()*800)));
			for (TouchButton o : buttons)
				objects.add(o);
		}
 		
 	}
 	
 	public void activate(boolean on) {
 		thread.setRunning(on);
 	}
 	
 	public void render(Canvas canvas) {	
 		// clear canvas
 		canvas.drawColor(Color.BLACK);
 		
 		// draw everything
 		synchronized(objects) {
 			for (DisplayableObject o : objects)
 				canvas.drawBitmap(o.getImageDisplayed(), o.getX(), o.getY(), null);
 		}
 //		synchronized(buttons) {
 //			for (TouchButton o : buttons) {
 //				System.err.println("!!! "+o.getX()+" , "+o.getY());
 //				canvas.drawBitmap(o.getImageDisplayed(), o.getX(), o.getY(), null);
 //			}
 //		}
 	}
 
 	public void update() {
 		// update everything
 		synchronized(objects) {
 		for (DisplayableObject o : objects)
 			o.update();
 		}
 	}
 	
 	public void surfaceCreated(SurfaceHolder holder) {
 		thread.setRunning(true);
 		thread.start();
 		init();
 	}
 	
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	}
 	
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry = true;
 		while (retry) {
 			try {
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				// try again shutting down the thread
 			}
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		double x = event.getX();
 		double y = event.getY();
 		for (TouchButton b : buttons)
 			if (b.hit(x, y)) {
 				if (b.type == TouchButton.TouchButtonDirection.RIGHT)
 					player.moveRight();
 				if (b.type == TouchButton.TouchButtonDirection.LEFT)
 					player.moveLeft();
 				System.out.println(b);
 			}
 		return true;
 	}
 }
