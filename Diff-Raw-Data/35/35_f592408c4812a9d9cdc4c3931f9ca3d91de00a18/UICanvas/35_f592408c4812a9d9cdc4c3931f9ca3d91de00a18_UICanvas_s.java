 package com.tacohen.killbots.UI;
 
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.entity.scene.Scene;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.bitmap.BitmapTexture;
 import org.andengine.util.adt.io.in.IInputStreamOpener;
 import org.andengine.util.debug.Debug;
  
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 
 import org.andengine.entity.sprite.Sprite;
 
 import android.util.Log;
 import android.util.Pair;
 import android.view.MotionEvent;
 
 
 import org.andengine.input.touch.TouchEvent;
 
 import com.tacohen.killbots.Logic.CurrentPlayerLocation;
 import com.tacohen.killbots.Logic.GridDimensions;
 import com.tacohen.killbots.Logic.MoveRobots;
 import com.tacohen.killbots.Logic.PlayerMovement;
 import com.tacohen.killbots.Logic.RobotLocations;
 
 /**
  * License notes: uses graphics (the arrows) from picol.org, and they want a linkback
  * License notes: uses graphics (characters,background) from KDE, I should be okay as long as this app is GPL-licensed
  * @author timothy
  *
  */
 
 public class UICanvas extends SimpleBaseGameActivity {
 	
 	String TAG = "UICanvas";
 	
 	private static int CAMERA_WIDTH = 800;
 	private static int CAMERA_HEIGHT = 480;
 	
 	private ITextureRegion mBackgroundTextureRegion, playerTexture, robotTexture, deadRobotTexture, leftArrow,rightArrow,upArrow,downArrow;
 	private Sprite leftArrowSprite,rightArrowSprite,downArrowSprite,upArrowSprite,player, robot, deadRobot;
 	
 	private PlayerMovement playerMovement = new PlayerMovement();
 	private MoveRobots moveRobots = new MoveRobots();
 
 	
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, 
 		    new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
 	}
 
 	@Override
 	protected void onCreateResources() {
 		try {
 		    // 1 - Set up bitmap textures
 		    ITexture backgroundTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 		        @Override
 		        public InputStream open() throws IOException {
 		        	GridDimensions.setHeight(10);
 		        	GridDimensions.setWidth(10);
 		        	GridDimensions.setGridDimensions(new Pair<Integer,Integer>(10,10));
 		            return getAssets().open("gfx/background-resized-10x10.png");   
 		        }
 		    });
 		    ITexture player = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 		        @Override
 		        public InputStream open() throws IOException {
 		            return getAssets().open("gfx/player.png");
 		        }
 		    });
 		    ITexture robot = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 		        @Override
 		        public InputStream open() throws IOException {
 		            return getAssets().open("gfx/robot.png");
 		        }
 		    });
 		    ITexture deadRobot = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 		        @Override
 		        public InputStream open() throws IOException {
 		            return getAssets().open("gfx/deadRobot.png");
 		        }
 		    });
 		    ITexture leftArrow = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 		        @Override
 		        public InputStream open() throws IOException {
 		            return getAssets().open("gfx/left_arrow.png");
 		        }
 		    });
 		    ITexture rightArrow = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 		        @Override
 		        public InputStream open() throws IOException {
 		            return getAssets().open("gfx/right_arrow.png");
 		        }
 		    });
 		    ITexture upArrow = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 		        @Override
 		        public InputStream open() throws IOException {
 		            return getAssets().open("gfx/up_arrow.png");
 		        }
 		    });
 		    ITexture downArrow = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 		        @Override
 		        public InputStream open() throws IOException {
 		            return getAssets().open("gfx/down_arrow.png");
 		        }
 		    });
 		    
 		    // 2 - Load bitmap textures into VRAM
 		    backgroundTexture.load();
 		    player.load();
 		    robot.load();
 		    deadRobot.load();
 		    leftArrow.load();
 		    rightArrow.load();
 		    upArrow.load();
 		    downArrow.load();
 		    
 		 // 3 - Set up texture regions
 		    this.mBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
 		    this.playerTexture = TextureRegionFactory.extractFromTexture(player);
 		    this.robotTexture = TextureRegionFactory.extractFromTexture(robot);
 		    this.deadRobotTexture = TextureRegionFactory.extractFromTexture(deadRobot);
 		    this.playerTexture = TextureRegionFactory.extractFromTexture(player);
 		    this.leftArrow = TextureRegionFactory.extractFromTexture(leftArrow);
 		    this.rightArrow = TextureRegionFactory.extractFromTexture(rightArrow);
 		    this.upArrow = TextureRegionFactory.extractFromTexture(upArrow);
 		    this.downArrow = TextureRegionFactory.extractFromTexture(downArrow);
 		    
 		} catch (IOException e) {
 		    Debug.e(e);
 		}
 		
 		
 		
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 		// 1 - Create new scene
 		final Scene scene = new Scene();
 		Sprite backgroundSprite = new Sprite(0, 0, this.mBackgroundTextureRegion, getVertexBufferObjectManager());
 		scene.attachChild(backgroundSprite);
 		
 		
 		//1.5, tacohen, attach the arrows directly to the background
 		leftArrowSprite = new Sprite(530,100, this.leftArrow, getVertexBufferObjectManager()){
 			@Override
 	        public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				//Only move the player sprite once the user lifts his/her finger to prevent accidental multi-touching
 				case MotionEvent.ACTION_UP:{
 					Log.i(TAG, "Touched Left Arrow");
 					if (playerMovement.canPlayerMoveLeft(new Pair<Integer, Integer>(CurrentPlayerLocation.getPlayerXLocation(), CurrentPlayerLocation.getPlayerYLocation()))){
 						player.setPosition(player.getX()-47,player.getY());
 						CurrentPlayerLocation.setPlayerLocation(CurrentPlayerLocation.getPlayerXLocation()-1, CurrentPlayerLocation.getPlayerYLocation());
 						Pair<Integer, Integer> robotLoc = RobotLocations.liveRobotLocations.get(0);
 						Log.i(TAG,"old robot location is: "+robotLoc.first+","+robotLoc.second);
 						Log.i(TAG, "new player location is: "+CurrentPlayerLocation.getPlayerXLocation()+","+CurrentPlayerLocation.getPlayerYLocation());
 						Pair<Integer,Integer> newRobotLocation = moveRobots.desiredRobotLocation(robotLoc.first, robotLoc.second,CurrentPlayerLocation.getPlayerXLocation()+1,CurrentPlayerLocation.getPlayerYLocation());
 						Log.i(TAG,"new robot location is:"+newRobotLocation.first+" , "+newRobotLocation.second);
 						robot.setPosition(newRobotLocation.first*47, newRobotLocation.second*47);
 						RobotLocations.moveRobotLocation(0,newRobotLocation.first, newRobotLocation.second);
 					}
 				}
 				}
 
 				return true;
 			}
 		};
 		scene.attachChild(leftArrowSprite);
 		
 		rightArrowSprite = new Sprite(680,100, this.rightArrow, getVertexBufferObjectManager()){
 			@Override
 	        public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				//Only move the player sprite once the user lifts his/her finger to prevent accidental multi-touching
 				case MotionEvent.ACTION_UP:{
 					Log.i(TAG, "Touched Right Arrow");
 					if (playerMovement.canPlayerMoveRight(new Pair<Integer, Integer>(CurrentPlayerLocation.getPlayerXLocation(), CurrentPlayerLocation.getPlayerYLocation()))){
 						player.setPosition(player.getX()+47,player.getY());
 						CurrentPlayerLocation.setPlayerLocation(CurrentPlayerLocation.getPlayerXLocation()+1, CurrentPlayerLocation.getPlayerYLocation());
 						
 						Pair<Integer, Integer> robotLoc = RobotLocations.liveRobotLocations.get(0);
 						Log.i(TAG,"old robot location is: "+robotLoc.first+","+robotLoc.second);
 						Log.i(TAG, "new player location is: "+CurrentPlayerLocation.getPlayerXLocation()+","+CurrentPlayerLocation.getPlayerYLocation());
 						Pair<Integer,Integer> newRobotLocation = moveRobots.desiredRobotLocation(robotLoc.first, robotLoc.second,CurrentPlayerLocation.getPlayerXLocation()-1,CurrentPlayerLocation.getPlayerYLocation());
 						Log.i(TAG,"new robot location is:"+newRobotLocation.first+" , "+newRobotLocation.second);
 						robot.setPosition(newRobotLocation.first*47, newRobotLocation.second*47);
 						RobotLocations.moveRobotLocation(0,newRobotLocation.first, newRobotLocation.second);
 					}
 				}
 				}
 
 				return true;
 			}
 		};
 		scene.attachChild(rightArrowSprite);
 		
 		upArrowSprite = new Sprite(605,0, this.upArrow, getVertexBufferObjectManager()){
 			@Override
 	        public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				//Only move the player sprite once the user lifts his/her finger to prevent accidental multi-touching
 				case MotionEvent.ACTION_UP:{
 					Log.i(TAG, "Touched Up Arrow");
 					if (playerMovement.canPlayerMoveUp(new Pair<Integer, Integer>(CurrentPlayerLocation.getPlayerXLocation(), CurrentPlayerLocation.getPlayerYLocation()))){
 						player.setPosition(player.getX(),player.getY()-47);
						CurrentPlayerLocation.setPlayerLocation(CurrentPlayerLocation.getPlayerXLocation(), CurrentPlayerLocation.getPlayerYLocation()+1);
 						
 						Pair<Integer, Integer> robotLoc = RobotLocations.liveRobotLocations.get(0);
 						Log.i(TAG,"old robot location is: "+robotLoc.first+","+robotLoc.second);
 						Log.i(TAG, "new player location is: "+CurrentPlayerLocation.getPlayerXLocation()+","+CurrentPlayerLocation.getPlayerYLocation());
 						Pair<Integer,Integer> newRobotLocation = moveRobots.desiredRobotLocation(robotLoc.first, robotLoc.second,CurrentPlayerLocation.getPlayerXLocation(),CurrentPlayerLocation.getPlayerYLocation()-1);
 						Log.i(TAG,"new robot location is:"+newRobotLocation.first+" , "+newRobotLocation.second);
 						robot.setPosition(newRobotLocation.first*47, newRobotLocation.second*47);
 						RobotLocations.moveRobotLocation(0,newRobotLocation.first, newRobotLocation.second);
 					}
 				}
 				}
 
 				return true;
 			}
 		};
 		scene.attachChild(upArrowSprite);
 		
 		downArrowSprite = new Sprite(605,200, this.downArrow, getVertexBufferObjectManager()){
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				//Only move the player sprite once the user lifts his/her finger to prevent accidental multi-touching
 				case MotionEvent.ACTION_UP:{
 					Log.i(TAG, "Touched Down Arrow");
 					if (playerMovement.canPlayerMoveDown(new Pair<Integer, Integer>(CurrentPlayerLocation.getPlayerXLocation(), CurrentPlayerLocation.getPlayerYLocation()))){
 						player.setPosition(player.getX(),player.getY()+47);
						CurrentPlayerLocation.setPlayerLocation(CurrentPlayerLocation.getPlayerXLocation(), CurrentPlayerLocation.getPlayerYLocation()-1);
 						
 						Pair<Integer, Integer> robotLoc = RobotLocations.liveRobotLocations.get(0);
 						Log.i(TAG,"old robot location is: "+robotLoc.first+","+robotLoc.second);
 						Log.i(TAG, "new player location is: "+CurrentPlayerLocation.getPlayerXLocation()+","+CurrentPlayerLocation.getPlayerYLocation());
 						Pair<Integer,Integer> newRobotLocation = moveRobots.desiredRobotLocation(robotLoc.first, robotLoc.second,CurrentPlayerLocation.getPlayerXLocation(),CurrentPlayerLocation.getPlayerYLocation()+1);
 						Log.i(TAG,"new robot location is:"+newRobotLocation.first+" , "+newRobotLocation.second);
 						robot.setPosition(newRobotLocation.first*47, newRobotLocation.second*47);
 						RobotLocations.moveRobotLocation(0,newRobotLocation.first, newRobotLocation.second);
 					}
 				}
 				}
 
 				return true;
 			}
 		};
 		scene.attachChild(downArrowSprite);
 		//tacohen note: (280,130) places the player at 6x7 on the grid
 		//tacohen note: every square in the any direction is 47 pixels (e.g. 6x7 is (280,130),
 		//7x6 is (327,177)
 		//UIObject player = new UIObject(1, 327, 177, this.playerTexture, getVertexBufferObjectManager()) {
		player = new Sprite(327,177, this.playerTexture, getVertexBufferObjectManager());// {
		CurrentPlayerLocation.setPlayerLocation(6, 5);
 		
 		//Placeholder location values, replace later!
		robot = new Sprite(280,224, this.robotTexture, getVertexBufferObjectManager());// {
		RobotLocations.setRobotLocation(5,6);
 		
 		//Placeholder values, replace later!
 		//deadRobot = new Sprite(233,87, this.deadRobotTexture, getVertexBufferObjectManager());// {
 		//RobotLocations.setDeadRobotLocation(4,7);
 
 		scene.attachChild(player);
 		scene.attachChild(robot);
 		//scene.attachChild(deadRobot);
 		
 		//Add touch handlers
 		scene.registerTouchArea(downArrowSprite);
 		scene.registerTouchArea(leftArrowSprite);
 		scene.registerTouchArea(rightArrowSprite);
 		scene.registerTouchArea(upArrowSprite);
 		scene.setTouchAreaBindingOnActionDownEnabled(true);
 		
 		
 		return scene;
 	}
 
 }
