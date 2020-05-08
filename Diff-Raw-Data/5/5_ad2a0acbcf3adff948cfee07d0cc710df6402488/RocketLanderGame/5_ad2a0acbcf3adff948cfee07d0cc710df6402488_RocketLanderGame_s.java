 package it.chalmers.dat255_bearded_octo_lama.games;
 
 import it.chalmers.dat255_bearded_octo_lama.R;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.view.MotionEvent;
 import android.widget.LinearLayout;
 
 public class RocketLanderGame extends AbstractGameView {
 	
 	//Set all physics constants
 	private final int GRAV_ACCEL = 100;
 	private final int ENGINE_ACCEL = 200;
 	private final int ENGINE_SIDE_ACCEL = 100;
 	private final int MAX_SPD = 500;
 	private final int INIT_SPD = 25;
 	
 	//Set goal constants
 	private final int MAX_VERT_SPD = 20;
 	private final int MAX_HORI_SPD = 15;
 	
 	private long lastTime;
 	private int currentYSpd, currentXSpd;
 	private double rocketX, rocketY;
 	private boolean engineIsRunning;
 	private int groundYLevel;
 	private float pressX;
 	
 	private Bitmap rocketBitmap;
 	
 	public RocketLanderGame(Context context, LinearLayout dismissAlarmLayout) {
 		super(context, dismissAlarmLayout);
 		
 		initGame();
 	}
 
 	private void initGame() {
 		lastTime = System.currentTimeMillis() + 100;
 		rocketBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rocket);
 		
 		resetGame();
 	}
 	
 	private void resetGame() {
 		rocketX = getWidth()/2;
 		rocketY = 0;
 		
 		engineIsRunning = false;
 		currentYSpd = INIT_SPD;
 	}
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 		//When the size of the view is changed we are going to reset the position of the
 		//rocket and reset the ground level to avoid any issues with bad positioning.
 		rocketX = w/2;
 		rocketY = 0;
 		
 		groundYLevel = h/4 * 3;
 	}
 
 	@Override
 	protected void updateGame() {
 		long now = System.currentTimeMillis();
 		
 		if(lastTime > now) {
 			return;
 		}
 		
 		double timeSinceLast = (now - lastTime)/1000.0;
 		
 		//Set and calculate acceleration.
 		double xAcceleration = 0;
 		double yAcceleration = GRAV_ACCEL * timeSinceLast;
 		
 		//Calculate new speed of the aircraft.
 		if(engineIsRunning) {
 			//Add engine acceleration.
 			yAcceleration -= ENGINE_ACCEL * timeSinceLast;
 			
 			//Check if the player touches the screen on the left or right side of the rocket.
 			if(pressX > rocketX) {
 				xAcceleration -= ENGINE_SIDE_ACCEL * timeSinceLast;
 			}
 			else {
 				xAcceleration += ENGINE_SIDE_ACCEL * timeSinceLast;
 			}
 		}
 		
 		currentXSpd += xAcceleration * timeSinceLast;
 		currentYSpd += yAcceleration * timeSinceLast;
 		
 		if(currentYSpd > MAX_SPD) {
 			currentYSpd = MAX_SPD;
 		}
 		
 		rocketX += (currentXSpd * timeSinceLast);
 		rocketY += (currentYSpd * timeSinceLast);
 		
 		//Check if aircraft has landed or crashed.
 		if(rocketY >= groundYLevel) {
 			
 			//Check if it's a crash.
			if(currentYSpd > MAX_VERT_SPD) {
 				resetGame();
 			}
 			else {
 				//If it's not a crash, end the game.
 				endGame();
 			}
 			
 		}
 		
 		lastTime = now;
 	}
 
 	@Override
 	protected void updateGraphics(Canvas c) {
 		float canvasWidth = getWidth();
 		float canvasHeight = getHeight();
 		
 		// Paint heaven then ground.
 		painter.setARGB(100, 51, 204, 255);
 		c.drawRect(0, 0, canvasWidth, canvasHeight, painter);
 		painter.setARGB(100, 102, 0, 0);
 		c.drawRect(0, groundYLevel, canvasWidth, canvasHeight, painter);
 		
 		//Draw the rocket
 		c.drawBitmap(rocketBitmap, (float)(rocketX - rocketBitmap.getWidth()/2), 
 				(float)(rocketY - rocketBitmap.getHeight()), painter);
 		if(engineIsRunning) {
 			painter.setARGB(100, 255, 100, 0);
 			c.drawCircle((float)rocketX, (float)rocketY-10, 10, painter);
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {		
 		//Sleep a bit to not overload the system with unnecessary amount of data.
 		try {
 			Thread.sleep(50);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		//Check for input.
 		switch(event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			engineIsRunning = true;
 			pressX = event.getX();
 			break;
 		case MotionEvent.ACTION_MOVE:
 			pressX = event.getX();
 			break;
 		case MotionEvent.ACTION_UP:
 			engineIsRunning = false;
 			break;
 		}
 		
 		return true;
 	}
 
 }
