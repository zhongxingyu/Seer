 package com.example.pineapple;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{
 	private final String TAG = GamePanel.class.getSimpleName();
 	private final int width = 155;
 	private final int height = 100;
 	private final int protagonistHeight = 20;
 	private final int protagonistWidth = (int)(20/1.42); //Change 1.42 to ratio of bitmap
 	private int screenX;
 	private int screenY;
 	private final int screenPadding = 50;
 	private MainThread thread;
 	private Protagonist protagonist;
 	private Ground ground;
 	private double scaleY, scaleX;
 	private Stick leftStick;
 	
 	
 	public GamePanel(Context context){
 		super(context);
 		getHolder().addCallback(this);
 		setFocusable(true);
 		protagonist = new Protagonist();
 		//Change this later
 		int[] x = {0, 100, 150};
 		int[] y = {100, 80, 100};
 		screenX = 0;
 		screenY = 0;
 		ground = new Ground(x, y);
 		leftStick = new Stick(Stick.LEFT);
 		thread = new MainThread(this.getHolder(), this);
 	}
 	
 	//Method that gets called every frame to update the games state
 	public void update(){
 		if(leftStick.isPointed()) {
 			protagonist.move(leftStick.getAngle(), -1, 0.1, 1);
 		} else if (Math.abs(protagonist.getXVel()) > 0){
 			protagonist.slowDown();
 		}
	
 		//Check if the screen has to be moved
 		moveScreen();
 	}
 	
 	//Under construction
 	//Moves the screen if the protagonist is close to the edge of the screen
 	public void moveScreen(){
 		if(protagonist.getXPos() - screenX > width - screenPadding){
 			screenX = (int)(protagonist.getXPos() - width + screenPadding);
		} else if(protagonist.getXPos() - screenX > screenPadding){
 			screenX = (int)(protagonist.getXPos() - screenPadding);
 		}
 		
 	}
 	
 	//Method that gets called to render the graphics
 	public void render(Canvas canvas){
 		canvas.drawColor(Color.WHITE);
 		renderProtagonist(canvas);
 		renderGround(canvas);
 		renderSticks(canvas);
 	}
 	
 	//Pause the game
 	public void pause(){
 		thread.setRunning(false);
 	}
 	
 	//Draw the protagonist
 	public void renderProtagonist(Canvas canvas){
 		canvas.drawRect((float)((protagonist.getXPos()-protagonistWidth/2-screenX)*scaleX), (float)((protagonist.getYPos()-protagonistHeight/2)*scaleY), (float)((protagonist.getXPos()+protagonistWidth/2-screenX)*scaleX), (float)((protagonist.getYPos()+protagonistHeight/2)*scaleY), new Paint());
 	}
 	
 	//Draws the ground using a Path
 	public void renderGround(Canvas canvas){
 		int length = ground.getLength();
 		for(int i = 0; i < length-1; i++){
 			Path p = new Path();
 			p.moveTo((int)((ground.getX(i)-screenX)*scaleX), (int)(ground.getY(i)*scaleY));
 			p.lineTo((int)((ground.getX(i+1)-screenX)*scaleX), (int)(ground.getY(i+1)*scaleY));
 			p.lineTo((int)((ground.getX(i+1)-screenX)*scaleX), (int)(200*scaleY)); //Fix line 63 and 64, (200)
 			p.lineTo((int)((ground.getX(i)-screenX)*scaleX), (int)(200*scaleY));
 			p.lineTo((int)((ground.getX(i)-screenX)*scaleX), (int)(ground.getY(i)*scaleY));
 			canvas.drawPath(p, new Paint());
 		}
 	}
 	
 	//Draw the sticks
 	public void renderSticks(Canvas canvas){
 		canvas.drawCircle((float)(leftStick.getX()*scaleX), (float)(leftStick.getY()*scaleY), (float)(leftStick.getRadius()*scaleX), new Paint());
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent e){
 		int x = (int)(e.getX()/scaleX);
 		int y = (int)(e.getY()/scaleY);
 		leftStick.handleTouch(x, y);
 		Log.d(TAG, leftStick.getAngle()+"");
 		
 		if(e.getAction() == MotionEvent.ACTION_UP){
 			leftStick.release();
 			Log.d(TAG, "release me");
 		}
 		return true;
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		//Start the thread
 		thread.setRunning(true);
 		thread.start();
 		//Calculate the scale that we will use to render the game
 		scaleY = (double)getHeight()/height;
 		scaleX = (double)getWidth()/width;
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		//End the thread
 		boolean retry = true;
 		while(retry){
 			try{
 				thread.join();
 				retry = false;
 			} catch(InterruptedException e){
 				
 			}
 		}
 	}
 }
