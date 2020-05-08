 package com.jand.bombercommander;
 
 import com.jand.bombercommander.GameObject;
 import com.jand.bombercommander.GameObject.GameObjectType;
 import com.jand.bombercommander.screens.MainGameActivity;
 
 import java.util.ArrayList;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PixelFormat;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class MainGameView extends SurfaceView implements
 		SurfaceHolder.Callback {
 	private static final String TAG = MainGameView.class.getSimpleName();
 	ArrayList<GameObject> playerGameObjects = new ArrayList<GameObject>();
 	ArrayList<GameObject> totalGameObjects;
 
 	Player p1, p2;
 	public enum BomberState{
 		SUCCESSFUL, SHOT_DOWN_BY_AA, SHOT_DOWN_BY_FIGHTER
 	}
 	BomberState player1BomberState = BomberState.SUCCESSFUL;
 	BomberState player2BomberState = BomberState.SUCCESSFUL;
 	boolean gameResolved = false;
 
 	public MainGameView(Context context, AttributeSet attributeSet) {
 		super(context, attributeSet);
 
 		if (!isInEditMode()) {
 			this.setZOrderOnTop(true);
 			getHolder().setFormat(PixelFormat.TRANSPARENT);
 		}
 
 		getHolder().addCallback(this);
 
 		GameObject AA1 = new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_aa), GameObjectType.ANTIAIR, 110,
 				640);
 		GameObject bomber1 = new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_bomber), GameObjectType.BOMBER,
 				310, 640);
 		GameObject fighter1 = new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_fighter), GameObjectType.FIGHTER,
 				510, 640);
 		GameObject AA2 = new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_aa_upsidedown), GameObjectType.ANTIAIR, 110,
 				640);
 		GameObject bomber2 = new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_bomber_upsidedown), GameObjectType.BOMBER,
 				310, 640);
 		GameObject fighter2 = new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_fighter_upsidedown), GameObjectType.FIGHTER,
 				510, 640);
 
 		p1 = new Player(AA1, bomber1, fighter1);
 		p2 = new Player(AA2, bomber2, fighter2);
 
 		MainGameActivity.thread = new GameThread(getHolder(), this);
 		setFocusable(true);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		Matrix reverse = new Matrix();
 		reverse.postRotate(180);
 
 		MainGameActivity.thread.setRunning(true);
 		MainGameActivity.thread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry = true;
 
 		Log.d(TAG, "Surface is being destroyed");
 		while (retry) {
 			try {
 				MainGameActivity.thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				Log.d(VIEW_LOG_TAG, e.getMessage());
 			}
 		}
 		Log.d(TAG, "Thread was shut down cleanly");
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		checkPlayer();
 
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			boolean objectFound = false;
 			
 			if( MainGameActivity.state == MainGameActivity.gameState.P1_SETUP || MainGameActivity.state == MainGameActivity.gameState.P2_SETUP)
 			{
 				for (GameObject obj : playerGameObjects) {
 					// only tries to set A SINGLE object to a motion event
 					if (!objectFound) {
 						objectFound = obj.handleActionDown((int) event.getX(),
 								(int) event.getY());
 					}
 	
 				}
 				Log.d(VIEW_LOG_TAG, "Coords: x = " + event.getX() + ", y = "
 						+ event.getY());
 			}
 		}
 
 		if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			if( MainGameActivity.state == MainGameActivity.gameState.P1_SETUP || MainGameActivity.state == MainGameActivity.gameState.P2_SETUP)
 			{
 				for (GameObject obj : playerGameObjects) {
 					if (obj.getIsTouched()) {
 						obj.setX((int) event.getX() - obj.getBitmap().getWidth()
 								/ 2);
 						obj.setY((int) event.getY() - obj.getBitmap().getHeight()
 								/ 2);
 					}
 				}
 			}
 		}
 
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			// Loops through game objects and places them on the top of the
 			// screen in a box
 			// first it sets the lane for the object, and prevents the object
 			// from stacking on an occupied lane
 			// then it sets the object's x,y coordinates onto the screen
 			if( MainGameActivity.state == MainGameActivity.gameState.P1_SETUP || MainGameActivity.state == MainGameActivity.gameState.P2_SETUP)
 			{
 				for (GameObject obj : playerGameObjects) {
 					if (obj.getIsTouched())
 						obj.setIsTouched(false);
 	
 					if ((obj.getY() <= 100 && !obj.getIsPlayerOne()) || obj.getY() >= 1130 && obj.getIsPlayerOne()) {
 						if (obj.getX() >= 144 * 4) {
 							obj.setLane(4);
 	
 						} else if (obj.getX() >= 144 * 3 && obj.getX() <= 144 * 4) {
 							obj.setLane(3);
 						} else if (obj.getX() >= 144 * 2 && obj.getX() <= 144 * 3) {
 							obj.setLane(2);
 						} else if (obj.getX() >= 144 && obj.getX() <= 144 * 2) {
 							obj.setLane(1);
 						} else {
 							obj.setLane(0);
 						}
 					} else {
 						obj.setLane(-1);
 					}
 					for (GameObject otherObject : playerGameObjects) {
 						if (obj.getLane() == otherObject.getLane()
 								&& !obj.equals(otherObject)) {
 							obj.setLane(-1);
 						}
 					}
 					if (obj.getLane() == -1) {
 						int xOffset = 0;
 						switch (obj.getType()) {
 						case BOMBER:
 							xOffset = -50;
 							break;
 						case FIGHTER:
 							xOffset = 150;
 							break;
 						case ANTIAIR:
 							xOffset = -250;
 							break;
 						}
 						
 						obj.setX(360 + xOffset);
 						obj.setY(640);
 					} else {
 						if(obj.getIsPlayerOne()){
 							obj.setY(1130);
 						}else {
 							obj.setY(0);
 						}
 						obj.setX(obj.getLane() * 144);
 						obj.setX(obj.getLane() * 144);
 					}
 					
 				}
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onDraw(Canvas c) {
 		if (!isInEditMode()) {
 			c.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
 
 			checkPlayer();
 			
 			switch (MainGameActivity.state)
 			{
 			case P1_SETUP:
 			case P2_SETUP:
 				for (GameObject obj : playerGameObjects)
 					obj.draw(c);
 				break;
 			case ANIMATION:
 				if(!gameResolved){
 					resolveGame();
 				}
 				
 				for (GameObject obj : totalGameObjects)
 					obj.draw(c);
 				break;
 				
 			}
 			
 		}
 	}
 
 	private void updateAnimation() {
 
 		for (GameObject obj : totalGameObjects) {
			if(obj.getIsActive() && obj.getIsPlayerOne() && obj.getType() != GameObjectType.ANTIAIR){
 				obj.setY(obj.getY() - 5);
			}else if (obj.getIsActive() && !obj.getIsPlayerOne() && obj.getType() != GameObjectType.ANTIAIR){
 				obj.setY(obj.getY() + 5);
 			}
 			
 			if(obj.getIsPlayerOne() && obj.getType() == GameObjectType.BOMBER){
 				switch(player1BomberState){
 				case SUCCESSFUL:
 					
 					break;
 				case SHOT_DOWN_BY_FIGHTER:
 					if(obj.getY() <= 500){
 						setExplosion( obj );
 					}
 					break;
 				case SHOT_DOWN_BY_AA:
 					if(obj.getY() < 100)
 					{
 						setExplosion( obj );
 					}
 					break;
 				}
 			}
 			if(!obj.getIsPlayerOne() && obj.getType() == GameObjectType.BOMBER){
 				switch(player2BomberState){
 				case SUCCESSFUL:
 					
 					break;
 				case SHOT_DOWN_BY_FIGHTER:
 					if(obj.getY() >= 500){
 						setExplosion( obj );
 					}
 					break;
 				case SHOT_DOWN_BY_AA:
 					if(obj.getY() > 1030)
 					{
 						setExplosion( obj );
 					}
 					break;
 				}
 			}
 			
 			if( obj.getY() < 0 || obj.getY() >= 1280 ) obj.setIsActive(false);
 		}
 	}
 	public void resolveGame(){
 		player1BomberState = BomberState.SUCCESSFUL;
 		player2BomberState = BomberState.SUCCESSFUL;
 		if(Math.abs(p1.getBomber().getLane() - p2.getAntiAir().getLane()) == 1){
 		player1BomberState = BomberState.SHOT_DOWN_BY_AA;
 		}
 		if(p1.getBomber().getLane() == (p2.getFighter().getLane())){
 			player1BomberState = BomberState.SHOT_DOWN_BY_FIGHTER;
 		}
 		if(Math.abs(p2.getBomber().getLane() - p1.getAntiAir().getLane()) == 1){
 			player2BomberState = BomberState.SHOT_DOWN_BY_AA;
 		}
 		if(p2.getBomber().getLane() == (p1.getFighter().getLane())){
 			player2BomberState = BomberState.SHOT_DOWN_BY_FIGHTER;
 		}
 		gameResolved = true;
 	}
 
 	private void checkPlayer() {
 		switch (MainGameActivity.state) {
 		case P1_SETUP:
 			playerGameObjects = p1.getGameObjectList();
 			for(GameObject g : playerGameObjects){				g.setIsPlayerOne(true);			}			break;
 		case P2_SETUP:
 			playerGameObjects = p2.getGameObjectList();
 			for(GameObject g : playerGameObjects){				g.setIsPlayerOne(false);			}			break;
 		case ANIMATION:
 			if(totalGameObjects == null){
 				totalGameObjects = new ArrayList<GameObject>();
 				for (GameObject g : p1.getGameObjectList()) {
 					totalGameObjects.add(g);
 					g.setIsPlayerOne(true);				}
 				for (GameObject g : p2.getGameObjectList()) {
 					totalGameObjects.add(g);
 					g.setIsPlayerOne(false);				}
 			}	
 			updateAnimation();
 			break;
 		}
 	}
 	
 	private void setExplosion( GameObject obj )
 	{
 		obj.setIsDestroyed( true );
 		Bitmap explosion = BitmapFactory.decodeResource(getResources(), R.drawable.bc_explosion);
 		obj.setBitmap( explosion );
 	}
 }
