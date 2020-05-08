 package com.example.pineapple;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{
 	private final String TAG = GamePanel.class.getSimpleName();
 	private final int INVALID_POINTER = -1;
 	
 	//Scaling coefficients, from an imagined rectangle around the protagonist
 	//Use this to fine tune the placement of the parts of the protagonist
 	private final double bodyXScale = 0.833;
 	private final double bodyYScale = 0.667;
 	private final double bodyXOffset = 0.133;
 	private final double bodyYOffset = 0.244; 
 	
 	private final double eyeMouthXScale = 0.867;
 	private final double eyeMouthYScale = 0.567;
 	private final double eyeMouthXOffset = 0.05;
 	private final double eyeMouthYOffset = 0.044; 
 	
 	private final double footXScale = 0.45;
 	private final double footYScale = 0.156;
 	private final double footXOffset = 0.35;
 	private final double footYOffset = 0.844; 
 	
 	private final double weaponXScale = 0.7;
 	private final double weaponYScale = 0.378;
 	private final double weaponXOffset = 0.8;
 	private final double weaponYOffset = 0.411; 
 	
 	private int leftStickId = INVALID_POINTER;
 	private int rightStickId = INVALID_POINTER;
 	private final int width = 155;
 	private final int height = 100;
 	private double screenX;
 	private double screenY;
 	private final int screenPadding = 50;
 	private MainThread thread;
 	private Protagonist protagonist;
 	private Ground ground;
 	private double scaleY, scaleX;
 	private Stick leftStick, rightStick;
 	private LevelLoader levelLoader;
 	private ArrayList<Platform> platforms;
 	private ArrayList<Bullet> bullets;
 	private ArrayList<Enemy> enemies;
 	private int level;
 	private HeatMeter heatMeter;
 	private boolean firing = false;
 	private Paint green = new Paint();
 	private Paint red = new Paint();
 	private Paint frame = new Paint();
 	private double time;
 	
 	//Bitmaps
 	private Bitmap bodyBitmap;
 	private Bitmap footBitmap;
 	private Bitmap eyeMouthBitmap;
 	private Bitmap weaponBitmap;
 	//private Bitmap irisBitmap;
 	
 	
 	public GamePanel(Context context, int level){
 		super(context);
 		getHolder().addCallback(this);
 		setFocusable(true);
 		
 		//screenX = 0;
 		//screenY = 0;
 		this.level = level;
 				
 		//Create game components
 		levelLoader = new LevelLoader(level);
 		heatMeter = new HeatMeter(0.01);
 		bullets = new ArrayList<Bullet>();
 		ground = new Ground(levelLoader.getLevelX(level), levelLoader.getLevelY(level));
 		protagonist = new Protagonist(width/2, ground.getYFromX(77), this);
 		leftStick = new Stick(Stick.LEFT);
 		rightStick = new Stick(Stick.RIGHT);
 		thread = new MainThread(this.getHolder(), this);
 		
 		loadPlatforms();
 		loadEnemies();
 		
 		green.setColor(Color.GREEN);
 		red.setColor(Color.RED);
 		
 		
 	}
 	
 	//Load the platforms from LevelLoader and add them to the platforms list 
 	public void loadPlatforms(){
 		platforms = new ArrayList<Platform>();
 		for(int i = 0; i < levelLoader.getNumberOfPlatforms(); i++){
 			platforms.add(new Platform(levelLoader.getPlatformUpperX(i), levelLoader.getPlatformUpperY(i), levelLoader.getPlatformLowerX(i), levelLoader.getPlatformLowerY(i)));
 		}
 	}
 	
 	public void loadEnemies(){
 		enemies = new ArrayList<Enemy>();
 		for(int i = 0; i < levelLoader.getNumberOfEnemies(); i++){
 			int[] enemyData = levelLoader.getEnemyData(i);
 			enemies.add(new Enemy(enemyData[0], enemyData[1], enemyData[2], enemyData[3], this));
 		}
 	}
 	
 	//Method that gets called every frame to update the games state
 	public void update(){
 		handleSticks();
 		moveProtagonist();
 		moveEnemies();
 		moveBullets();
 		moveScreen();
 		handleHeatMeter();
 		handleBulletEnemyCollisions();
 		this.time++;
 	}
 	
 	public void handleSticks(){
 		if(leftStick.isPointed()) {
 			protagonist.handleLeftStick(leftStick.getAngle(), 0.4);
 		} else if (Math.abs(protagonist.getXVel()) > 0){
 			protagonist.slowDown();
 		}
 		if(rightStick.isPointed()){
 			//Aim
 			double angle = rightStick.getAngle();
 			protagonist.aim(angle);
 			//Fire
 			if(!heatMeter.isCoolingDown()){
 				bullets.add(new Bullet(protagonist.getXPos()+protagonist.getWidth()/2*Math.cos(angle/180*Math.PI), protagonist.getYPos()-protagonist.getWidth()/2*Math.sin(angle/180*Math.PI), angle, 10));
 				firing = true;
 			}
 		}
 	}
 	
 	public void moveProtagonist(){
 		protagonist.gravity();
 		protagonist.checkSlope(ground, platforms);
 		protagonist.move();
 		
 		protagonist.checkGround(ground);
 		protagonist.checkPlatform(platforms);
 	}
 	
 	//Move all the enemies and check for obstacles etc
 	public void moveEnemies(){
 		for(int i = 0; i < enemies.size(); i++){
 			Enemy enemy = enemies.get(i);
 			
 			enemy.gravity();
 			enemy.accelerate(protagonist);
 			enemy.move();
 			enemy.checkGround(ground);
 			enemy.checkPlatform(platforms);
 		}
 	}
 		
 	public void moveBullets(){
 		for(int i = 0; i < bullets.size(); i++){
 			Bullet b = bullets.get(i);
 			b.gravity(1);
 			b.move();
 			int leftBound = ground.getX(0);
 			int rightBound = ground.getX(ground.getLength()-1);
 			if(b.getXPos() < leftBound || b.getXPos() > rightBound){
 				bullets.remove(i);
 				i--;
 			} else if(b.checkObstacles(ground, platforms)){
 				bullets.remove(i);
 				i--;
 			}
 			
 		}
 	}
 	
 	//Moves the screen if the protagonist is close to the edge of the screen
 	public void moveScreen(){
 		if(protagonist.getXPos() - screenX > width - screenPadding){
 			screenX = protagonist.getXPos() - width + screenPadding;
 		} else if(protagonist.getXPos() - screenX < screenPadding){
 			screenX = protagonist.getXPos() - screenPadding;
 		}
 		
 	}
 	
 	//Handles the HeatMeter 
 	public void handleHeatMeter(){
 		if(heatMeter.isCoolingDown()){//If the weapon is overheated
 			heatMeter.coolDown();     //Cool down                      (Not able to fire)
 		} else if(firing){			  //Else if the protagonist has fired
 			heatMeter.addHeat(0.01);  //Heat up the weapon
 			firing = false;
 		} else {                      //Otherwise cool down the weapon (Still able to fire)
 			heatMeter.coolDown();
 		}
 	}
 	
 	//Check collision between enemies and bullets
 	public void handleBulletEnemyCollisions(){
 		for(int i = 0; i < bullets.size(); i++){//All bullets
 			Bullet bullet = bullets.get(i);
 			for(int j = 0; j < enemies.size(); j++){//All enemies
 				Enemy enemy = enemies.get(j);
 				if(bullet.collideEnemy(enemy)){//If collision detected
 					bullets.remove(i);//Remove the bullet from the game
 					i--;
 					
 					enemy.takeDamage(0.05); //Reduce the enemies' health SET A CONSTANT OR SOMETHING HERE INSTEAD OF 0.05
 					
 					if(enemy.getHealth() <= 0)//If the enemy is dead
 						enemies.remove(j);
						Log.d(TAG, "Enemy down!!");
					
 					break;
 				}
 			}
 		}
 	}
 	
 	//Method that gets called to render the graphics
 	public void render(Canvas canvas){
 		canvas.drawColor(Color.WHITE);
 		renderSun(canvas);
		renderTree(canvas, (float)(-screenX));
		renderTree(canvas, (float)(50*scaleX - screenX));
		renderTree(canvas, (float)(100*scaleX - screenX));
 		renderGround(canvas);
 		renderPlatforms(canvas);
 		renderEnemies(canvas);
 		renderProtagonist(canvas);
 		renderBullets(canvas);
 		renderSticks(canvas);
 		renderHeatMeter(canvas);
 		renderHealthMeter(canvas);
 		
 	}
 	
 	//Pause the game
 	public void pause(){
 		thread.setRunning(false);
 	}
 	
 	//Draw the enemies
 	public void renderEnemies(Canvas canvas){
 		for(int i = 0; i < enemies.size(); i++){
 			canvas.drawRect((float)((enemies.get(i).getXPos()-enemies.get(i).getWidth()/2-screenX)*scaleX), (float)((enemies.get(i).getYPos()-enemies.get(i).getHeight()/2)*scaleY), (float)((enemies.get(i).getXPos()+enemies.get(i).getWidth()/2-screenX)*scaleX), (float)((enemies.get(i).getYPos()+enemies.get(i).getHeight()/2)*scaleY), red);
 		}
 	}
 	
 	//Draw the protagonist
 	public void renderProtagonist(Canvas canvas){
 		Paint p = new Paint();
 		p.setColor(Color.BLUE);
 		canvas.drawRect((float)((protagonist.getXPos()-protagonist.getWidth()/2-screenX)*scaleX), (float)((protagonist.getYPos()-protagonist.getHeight()/2)*scaleY), (float)((protagonist.getXPos()+protagonist.getWidth()/2-screenX)*scaleX), (float)((protagonist.getYPos()+protagonist.getHeight()/2)*scaleY), p);
 		
 		//Draw all the protagonist parts
 		Matrix m = new Matrix();
 		m.setTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-bodyXOffset) - screenX)*scaleX), (float)((protagonist.getYPos() - protagonist.getHeight()*(0.5-bodyYOffset) - screenY)*scaleY));
 		canvas.drawBitmap(bodyBitmap, m, null);
 		m.setTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-eyeMouthXOffset) - screenX)*scaleX), (float)((protagonist.getYPos() - protagonist.getHeight()*(0.5-eyeMouthYOffset) - screenY)*scaleY));
 		canvas.drawBitmap(eyeMouthBitmap, m, null);
 		m.setTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-footXOffset) - screenX)*scaleX), (float)((protagonist.getYPos() - protagonist.getHeight()*(0.5-footYOffset) - screenY)*scaleY));
 		canvas.drawBitmap(footBitmap, m, null);
 		m.setTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-weaponXOffset) - screenX)*scaleX), (float)((protagonist.getYPos() - protagonist.getHeight()*(0.5-weaponYOffset) - screenY)*scaleY));
 		canvas.drawBitmap(weaponBitmap, m, null);
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
 	
 	//Draw the platforms
 	public void renderPlatforms(Canvas canvas){
 		Paint p = new Paint();
 		p.setColor(Color.CYAN);
 		for(int i = 0; i < platforms.size(); i++){
 			Path path = platforms.get(i).getPath();
 			Path newPath = new Path(path); 
 			Matrix m = new Matrix();
 			m.postTranslate(-(float)screenX, -(float)screenY);
 			m.postScale((float)scaleX, (float)scaleY);
 			newPath.transform(m);
 			canvas.drawPath(newPath, p);
 		}
 	}
 	
 	//Draw the sticks
 	public void renderSticks(Canvas canvas){
 		Paint p = new Paint();
 		p.setColor(Color.GRAY);
 		canvas.drawCircle((float)(leftStick.getX()*scaleX), (float)(leftStick.getY()*scaleY), (float)(leftStick.getRadius()*scaleX), p);
 		canvas.drawCircle((float)(rightStick.getX()*scaleX), (float)(rightStick.getY()*scaleY), (float)(rightStick.getRadius()*scaleX), p);
 	}
 	
 	//Draw the bullets
 	public void renderBullets(Canvas canvas){
 		Paint p = new Paint();
 		p.setColor(Color.RED);
 		for(int i = 0; i < bullets.size(); i++){
 			Bullet b = bullets.get(i);
 			int radius = b.getRadius();
 			canvas.drawCircle((float)((b.getXPos()-radius/2.-screenX)*scaleX), (float)((b.getYPos()-radius/2.-screenY)*scaleY), (float)(radius*scaleX), p);
 		}
 	}
 	
 	//Draw the HeatMeter
 	public void renderHeatMeter(Canvas canvas){
 		int xPadding = 10;
 		int yPadding = 10;
 		int width = 20;
 		int height = 10;
 		int frameSize = 1;
 		double blinkInterval = 0.05; //Lower value means faster blinking (Should be at least coolingRate of HeatMeter)
 		
 		//This makes the frame blink red if overheated
 		if(heatMeter.isCoolingDown() && heatMeter.getHeat() % (2*blinkInterval) > blinkInterval){
 			frame.setColor(Color.RED);
 		} else {
 			frame.setColor(Color.BLACK);
 		}
 		
 		//Draw in top right corner
 		//Draw frame
 		canvas.drawRect((float)((this.width - width - xPadding - frameSize)*scaleX), (float)((yPadding-frameSize)*scaleY), (float)((this.width - xPadding + frameSize)*scaleX), (float)((yPadding+height+frameSize)*scaleY), frame);
 		//Draw green background
 		canvas.drawRect((float)((this.width - width - xPadding)*scaleX), (float)(yPadding*scaleY), (float)((this.width - xPadding)*scaleX), (float)((yPadding+height)*scaleY), green);
 		//Draw red indicator that moves with current heat level
 		canvas.drawRect((float)((this.width - width - xPadding)*scaleX), (float)(yPadding*scaleY), (float)((this.width - xPadding - width*(1-heatMeter.getHeat()))*scaleX), (float)((yPadding+height)*scaleY), red);
 	}
 	
 	public void renderHealthMeter(Canvas canvas){
 		int xPadding = 10;
 		int yPadding = 10;
 		int width = 20;
 		int height = 10;
 		int frameSize = 1;
 		
 		//Draw in top left corner
 		//Draw frame
 		canvas.drawRect((float)((xPadding-frameSize)*scaleX), (float)((yPadding-frameSize)*scaleY), (float)((xPadding+width+frameSize)*scaleX), (float)((yPadding+height+frameSize)*scaleY), new Paint());
 		//Draw green background
 		canvas.drawRect((float)(xPadding*scaleX), (float)(yPadding*scaleY), (float)((xPadding+width)*scaleX), (float)((yPadding+height)*scaleY), red);
 		//Draw red indicator that moves with current heat level
 		canvas.drawRect((float)(xPadding*scaleX), (float)(yPadding*scaleY), (float)((xPadding+width*protagonist.getHealth())*scaleX), (float)((yPadding+height)*scaleY), green);
 	}
 
 	//Draw the sun, moving in time
 	public void renderSun(Canvas canvas){
		float x = (float)(width*scaleX/3);//Make generalll
		float y = (float)((50 + 50*Math.sin(Math.PI + time/500))*scaleY);//Make generalll
		float radius = 60;
 		Paint p = new Paint();
 		p.setColor(Color.YELLOW);
 		
 		canvas.drawCircle(x, y, radius, p);
 	}
 	
 	//Draw some trees
 	public void renderTree(Canvas canvas, float x){
		//x is centre of tree?
 		float y = (float)(100*scaleY); //Make generalll
		float trunkHeight = (float)(height/4*scaleY);//Make generalll
		float trunkWidth = (float)(5*scaleX);//Make generalll
		float radius = 100;
		Paint trunk = new Paint();
		trunk.setColor(Color.DKGRAY);
		Paint top = new Paint();
		top.setColor(Color.GREEN);
 		
		canvas.drawCircle(x, y - trunkHeight - radius/2, radius, top);
		canvas.drawRect(x - trunkWidth/2, y - trunkHeight, x + trunkWidth/2, y, trunk);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent e){
 		double x;
 		double y;
 		
 		int index = e.getActionIndex();
 	    int id = e.getPointerId(index);
 		
 		switch(e.getActionMasked()){
 		
 		case MotionEvent.ACTION_DOWN:
 			x = e.getX()/scaleX;
 			y = e.getY()/scaleY;
 			
 			if(x > width/2){
 				rightStick.handleTouch(x, y);
 				rightStickId = id;
 			} else {
 				leftStick.handleTouch(x, y);
 				leftStickId = id;
 			}
 			
 			break;
 			
 		case MotionEvent.ACTION_POINTER_DOWN:
 			
 			x = e.getX(index)/scaleX;
 			y = e.getY(index)/scaleY;
 			
 			
 			if(x > width/2){
 				rightStick.handleTouch(x, y);
 				rightStickId = id;
 			} else {
 				leftStick.handleTouch(x, y);
 				leftStickId = id;
 			}
 			
 			break;
 			
 		case MotionEvent.ACTION_MOVE:
 
 			for(index=0; index<e.getPointerCount(); index++) {
                 id=e.getPointerId(index);
                 x = (int) e.getX(index)/scaleX;
                 y = (int) e.getY(index)/scaleY; 
                
                 if(id == rightStickId) {
                 	if(x > width/2){
         				rightStick.handleTouch(x, y);
         				rightStickId = id;
         			} 
                 }
                 else if(id == leftStickId){
                 	if(x < width/2){
                 		leftStick.handleTouch(x, y);
                 		leftStickId = id;
                 	}
                 }
             }
 			break;
 
 		case MotionEvent.ACTION_UP:
 			rightStick.release();
 			leftStick.release();
 			leftStickId = INVALID_POINTER;
 			rightStickId = INVALID_POINTER;
 			
 			break;
 
 
 		case MotionEvent.ACTION_POINTER_UP:
 			if(id == leftStickId){
                 leftStick.release();
                 leftStickId = INVALID_POINTER;
 			}
 			if(id == rightStickId){
                 rightStick.release();
                 rightStickId = INVALID_POINTER;
 			}
 	        break;
 		}
 		return true;
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		//Calculate the scale that we will use to render the game
 		scaleY = (double)getHeight()/height;
 		scaleX = (double)getWidth()/width;
 		
 		//Load Bitmaps
 		bodyBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.valentine_in_game_90_body), (int)(protagonist.getWidth()*scaleX*bodyXScale), (int)(protagonist.getHeight()*scaleY*bodyYScale), true);
 		eyeMouthBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.valentine_in_game_90_eye_mouth), (int)(protagonist.getWidth()*scaleX*eyeMouthXScale), (int)(protagonist.getHeight()*scaleY*eyeMouthYScale), true);
 		footBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.valentine_in_game_90_foot), (int)(protagonist.getWidth()*scaleX*footXScale), (int)(protagonist.getHeight()*scaleY*footYScale), true);
 		weaponBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.valentine_in_game_90_hand_gun), (int)(protagonist.getWidth()*scaleX*weaponXScale), (int)(protagonist.getHeight()*scaleY*weaponYScale), true);
 		//Start the thread
 		thread.setRunning(true);
 		thread.start();
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
 	
 	
 	public Ground getGround() {
 		return ground;
 	}
 }
