 package net.castegaming.game.entities;
 
 import java.util.HashMap;
 import java.util.List;
 
 import Input.Button;
 import android.gameengine.icadroids.alarms.Alarm;
 import android.gameengine.icadroids.alarms.IAlarm;
 import android.gameengine.icadroids.engine.GameEngine;
 import android.gameengine.icadroids.input.OnScreenButtons;
 import android.gameengine.icadroids.input.TouchInput;
 import android.gameengine.icadroids.objects.collisions.TileCollision;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.Log;
 import net.castegaming.game.Diggerload;
 import net.castegaming.game.enums.Direction;
 import net.castegaming.game.enums.EntityType;
 import net.castegaming.game.enums.MoveWay;
 import net.castegaming.game.loadout.LoadOut;
 import net.castegaming.game.terrain.T;
 
 public class Player extends Entity implements IAlarm{
 	
 	HashMap<String, Paint> messages = new HashMap<String, Paint>();
 	HashMap<String, Integer[]> messagesPos = new HashMap<String, Integer[]>();
 	HashMap<Integer, String> messagesID = new HashMap<Integer, String>();
 	
 	private double fuelLevel;
 	LoadOut[] loadOuts;
 	private boolean canMove = true;
 	
 	private boolean movingMode = true;
 	private int playerX = 100;
	private int playerY = 40;
 	private Button movingModeButton;
 	
 	/**
 	 * @author Jasper
 	 * Function to get the absolute player position.
 	 * 
 	 * @return - the absolute player position. (x)
 	 */
 	public int getPlayerX() {
 		return playerX;
 	}
 
 	/**
 	 * @author Jasper
 	 * Function used to get the absolute player position.
 	 * 
 	 * @return - the absolute player position. (y)
 	 */
 	public int getPlayerY() {
 		return playerY;
 	}
 
 	public Player(Diggerload dl) {
		super(EntityType.PLAYER, 
				(GameEngine.getScreenWidth() / 2)- ((GameEngine.getScreenWidth() / 2) % 32), 
				(GameEngine.getScreenHeight() / 3)- ((GameEngine.getScreenHeight() / 3) % 32));
 		loadOuts = new LoadOut[4];
 		setFuelLevel(100.0);
 		setFriction(0.05);
 		
 		OnScreenButtons.use = false;
 		TouchInput.use = true;
 		
 		movingModeButton = new Button(10, 10, this);
 		dl.addGameObject(movingModeButton);
 	}
 	
 	/**
 	 * Function used to get the status of the moving variable from the player class.
 	 * @return the movingMode variable (true if the player can be moved)
 	 * 
 	 * @author Jasper
 	 */
 	public boolean getMovingMode() {
 		return movingMode;
 	}
 
 	/**
 	 * Function used to set the moving mode for the player class.
 	 * 
 	 * @param movingMode - true if the player should be able to move.
 	 */
 	public void setMovingMode(boolean movingMode) {
 		this.movingMode = movingMode;
 	}
 
 	@Override
 	public void update() {
 		/*
 		if (TouchInput.onPress && canMove){
 			if (TouchInput.xPos > GameEngine.getScreenWidth()/2){
 				move(MoveWay.RIGHT);
 			} else if (TouchInput.xPos <= GameEngine.getScreenWidth()/2){
 				move(MoveWay.LEFT);
 			} else if (TouchInput.yPos > GameEngine.getScreenHeight()/2){
 				move(MoveWay.DOWN);
 			} else if (TouchInput.yPos <= GameEngine.getScreenHeight()/2){
 				move(MoveWay.UP);
 			}
 		}
 		*/
 		
 		
 		super.update();
 		fuelLevel -= 0.01;
 		
 		double direction = (getDirection() > 180) ? getDirection() - 0.1 : getDirection() + 0.1;
 		
 		setDirectionSpeed(direction, getSpeed() - 0.1);
 		
 		checkTouchInput();
 	}
 	
 	private void checkTouchInput() {
 		if (!movingModeButton.overButton())
 			checkForBasic();
 	}
 	
 	private void checkForBasic() {
 		// check to see how many fingers there are on the screen
 		// 1 finger = move input
 		
 		if (TouchInput.onPress) {
 			Log.i("x", TouchInput.xPos + "");
 			Log.i("y", TouchInput.yPos + "");
 			Log.i("xPointer length", TouchInput.xPointer.length + "");
 		}
 		
 		if (TouchInput.xPointer.length == 10  && TouchInput.onPress) {
 			Diggerload.updateTileEnvironment = true;
 			
 			Log.i("player move", "moving");
 			
 			int pX = playerX;
 			int pY = playerY;
 			
 			if (TouchInput.xPos >= GameEngine.getScreenWidth() / 2) {
 				if (TouchInput.yPos >= GameEngine.getScreenHeight() / 2) {
 					// top right corner
 					if (xGreaterThenY()) {
 						if (movingMode) {
 							movePlayer(Direction.RIGHT);
 						} else {
 							T.breakBlock(Direction.RIGHT, pX, pY);
 						}
 					} else {
 						if (movingMode) {
 							movePlayer(Direction.DOWN);
 						} else {
 							T.breakBlock(Direction.DOWN, pX, pY);
 						}
 					}
 				} else {
 					// bottom right corner
 					if (xGreaterThenY()) {
 						if (movingMode) {
 							movePlayer(Direction.RIGHT);
 						} else {
 							T.breakBlock(Direction.RIGHT, pX, pY);
 						}
 					} else {
 						if (movingMode) {
 							movePlayer(Direction.UP);
 						} else {
 							T.breakBlock(Direction.UP, pX, pY);
 						}
 					}
 				}
 			} else {
 				if (TouchInput.yPos >= GameEngine.getScreenHeight() / 2) {
 					// top left corner
 					if (xGreaterThenY()) {
 						if (movingMode) {
 							movePlayer(Direction.LEFT);
 						} else {
 							T.breakBlock(Direction.LEFT, pX, pY);
 						}
 					} else {
 						if (movingMode) {
 							movePlayer(Direction.DOWN);
 						} else {
 							T.breakBlock(Direction.DOWN, pX, pY);
 						}
 					}
 				} else {
 					// bottom left corner
 					if (xGreaterThenY()) {
 						if (movingMode) {
 							movePlayer(Direction.LEFT);
 						} else {
 							T.breakBlock(Direction.LEFT, pX, pY);
 						}
 					} else {
 						if (movingMode) {
 							movePlayer(Direction.UP);
 						} else {
 							T.breakBlock(Direction.UP, pX, pY);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void movePlayer(Direction d) {
 		if (d.equals(Direction.UP)) {
 			if (validMove(playerX, playerY - 1))
 				playerY -= 1;
 		} else if (d.equals(Direction.RIGHT)) {
 			if (validMove(playerX + 1, playerY))
 				playerX += 1;
 		} else if (d.equals(Direction.DOWN)) {
 			if (validMove(playerX, playerY + 1))
 				playerY += 1;
 		} else if (d.equals(Direction.LEFT)) {
 			if (validMove(playerX - 1, playerY))
 				playerX -= 1;
 		} else {
 			Log.e("movePlayer", "invalid direction");
 		}
 		
 	}
 	
 	private boolean validMove(int x, int y) {
 		return (T.getTileType(x, y) == T.AIR);
 	}
 	
 	private boolean xGreaterThenY() {
 		return (Math.abs(TouchInput.xPos - (GameEngine.getScreenWidth() / 2)) >= Math.abs(TouchInput.yPos - (GameEngine.getScreenHeight() / 2)));
 	}
 
 	/**
 	 * Sets the fuel level
 	 * @param currentLevel The level to set
 	 */
 	public void setFuelLevel(double currentLevel) {
 		fuelLevel = currentLevel;
 	}
 	
 	/**
 	 * Returns the fuel level 
 	 * @return the current fuel level
 	 */
 	public double getFuelLevel(){
 		return fuelLevel;
 	}
 	
 	/**
 	 * Drains the fuel.
 	 * @param toDraing the amount to drain.
 	 */
 	public void drainFuel(double toDraing){
 		if (fuelLevel - toDraing <= 0){
 			die();
 		} else {
 			fuelLevel -= toDraing;
 		}
 	}
 	
 	public void move(MoveWay way){
 		setDirectionSpeed(way.direction, 5);
 	}
 	
 	public void drill(int collisionSide){
 		getTileOnPosition(getX(), getY()).setTileType(0);
 	}
 	
 	@Override
 	public void collisionOccurred(List<TileCollision> collidedTiles) {
 		super.collisionOccurred(collidedTiles);
 		
 		for (TileCollision tc : collidedTiles) { 
 		    if (tc.theTile.getTileType() >= 0) {
 			moveUpToTileSide(tc);
 			setSpeed(0);
 			canMove = false;
 			new Alarm(1337, 50, this);
 			text(5, 20, "made collision", 50, 50, 1);
 			return; 		// might be considered ugly by some colleagues...
 		    }
 		}
 	}
 
 	@Override
 	public void triggerAlarm(int alarmID) {
 		canMove = true;
 		
 		if (alarmID >= 100 && alarmID < 200){
 			
 			messages.remove(messagesID.get(alarmID));
 			messagesPos.remove(messagesID.get(alarmID));
 			messagesID.remove(messagesID.get(alarmID));
 		}
 	}
 	
 	/**
 	 * Creates text with the default time: 100 loops
 	 * @param size The size of the text
 	 * @param color The color of the text
 	 * @param text The text
 	 * @param x The x of the text
 	 * @param y The y of the text
 	 */
 	private void text(float size, int color, String text, int x, int y){
 		text(size, color, text, x, y, 100);
 	}
 	
 	/**
 	 * Creates text with the time defined<br/>
 	 * Limited to 100 messages!!
 	 * @param size The size of the text
 	 * @param color The color of the text
 	 * @param text The text
 	 * @param x The x of the text
 	 * @param y The y of the text
 	 * @param time The time in gameticks this message will be displayed
 	 */
 	private void text(float size, int color, String text, int x, int y, int time){
 		Paint p = new Paint();
 		p.setColor(color);
 		p.setTextSize(size);
 		messages.put(text, p);
 		messagesPos.put(text, new Integer[]{x, y});
 		messagesID.put(100 + messages.size(), text);
 		new Alarm(100 + messages.size(), time, this);
 	}
 	
 	@Override
 	public void drawGameObject(Canvas canvas) {
 		super.drawGameObject(canvas);
 		for (String m : messages.keySet()){
 			canvas.drawText(m, messagesPos.get(m)[0], messagesPos.get(m)[1], messages.get(m));
 		}
 	}
 }
