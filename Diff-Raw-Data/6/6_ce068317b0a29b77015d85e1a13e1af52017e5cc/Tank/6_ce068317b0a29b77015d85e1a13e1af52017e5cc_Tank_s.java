 package com.tiny.tank;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.tiny.weapons.Shot;
 
 /**
  * Player class. Tanks
  */
 public class Tank {
 
 	//Constants of size of tank
 	private final int tankWidth = 20;
 	private final int tankHeight = 10;
 	
 	//animation end and the counter for it.
 	private final float animationLimit = 1;
 	private float animationCounter;
 
 	// stores the data of the player
 	private Vector2f pos;
 	// Angle of the player
 	private float barrelAng;
 	// previous angle of the player
 	private float prevAng;
 	private int direction;
 	private int health;
 	private int shotIndex;
 	// index of the player
 	private int index;
 	private ArrayList<Shot> shots;
 	private Rectangle hitbox;
 	//Keeps an original incase resiszing must happen
 	private Image originalImage;
 	private Image image;
 	// x and y ranges of numbers. Will be usefull when we get rotations
 	private float[] xRange;
 	private float[] yRange;
 
 	//Only so much movement allowed per turn. 
 	private int movementCounter;
 	private int movementLimit;
 
 	//States
 	private boolean isMoving;
 	private boolean isFalling;
 	private boolean isShooting;
 	//flag for if turn
 	private boolean isTurn;
 
 	/**
 	 * Our good old players
 	 * 
 	 * @param playerX
 	 *            X position of top right
 	 * @param playerY
 	 *            Y position of top right
 	 * @param barrelAng
 	 *            The angle of the barrel
 	 * @param health
 	 *            Player health
 	 * @param shots
 	 *            List of weapons
 	 * @param index
 	 *            Player number
 	 */
 	public void TankInfo(float playerX, float playerY, float barrelAng,
 			int health, ArrayList<Shot> shots, int index) {
 
 		this.pos = new Vector2f(playerX, playerY);
 		this.barrelAng = barrelAng;
 		this.prevAng = barrelAng;
 		this.health = health;
 		this.index = index;
 		this.shots = shots;
 		// player1 looks right, player 2 looks left
 		if (index == 1) {
 			direction = 1;
 			try {
 				originalImage = new Image("res/BlueTank.png");
 				originalImage.setFilter(Image.FILTER_NEAREST);
 				image = originalImage.getScaledCopy(tankWidth, tankHeight);
 			} catch (SlickException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			direction = -1;
 			try {
 				originalImage = new Image("res/RedTank.png");
 				originalImage.setFilter(Image.FILTER_NEAREST);
 				image = originalImage.getScaledCopy(tankWidth, tankHeight);
 
 			} catch (SlickException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		//sets states
 		isMoving = false;
 		isFalling = false;
 		isShooting = false;
 
 		//sets range and hitbox
 		hitbox = new Rectangle(playerX, playerY, tankWidth, tankHeight);
 		xRange = new float[2];
 		yRange = new float[2];
 		xRange = calculateXRange(hitbox);
 		yRange = calculateYRange(hitbox);
 
 		//initilizies player 1 to go first;
 		if(index == 1){
 			onTurnSwitch();
 			
 		}else{
 			isTurn = false;
 		}
 		//start animation over
 		animationCounter = 0;
 	}
 
 	/**
 	 * Will set the position upon map creation. Puts it right on top of map
 	 */
 	public void setFirstPos() {
 		pos.y = Main_Gameplay.map.getMaxInRange(xRange[0], xRange[1])
 				- tankHeight + 1;
 		hitbox.setBounds(pos.x, pos.y, tankWidth, tankHeight);
 	}
 
 	/**
 	 * takes care of things that happen on swith of turns. Also resets movement limit here.
 	 * This will allow for speed based attacks later (such as a movment booster).
 	 */
 	public void onTurnSwitch() {
 		movementCounter = 0;
 		movementLimit = 1000;
 		isTurn = true;
 		shotIndex = 0;
 	}
 	
 	/**
	 * Shot calls when done.
 	 */
 	public void shotDone(){
 		if(isShooting){
 			isShooting = false;
 			isFalling = true;
 			shots.remove(shotIndex);
 			turnEnd();
 		}else{
 			isFalling = true;
 		}
 	}
 	
 	/**
 	 * Clean up for when turn is over.
 	 */
 	public void turnEnd(){
 		isTurn = false;
 	}
 
 
 	/**
 	 * Will update the events relating to tanks and shots
 	 */
 	public void update(Input input) {
 
 		// state handeling if falling
 		if (isFalling) {
 			isMoving = false;
 			animationCounter += 1;
 			if (animationCounter > animationLimit) {
 
 				pos.y += 1;
 				hitbox.setBounds(pos.x, pos.y, tankWidth, tankHeight);
 				if (checkCollision(pos)) {
 					pos.y -= 1;
 					hitbox.setBounds(pos.x, pos.y, tankWidth, tankHeight);
 					isFalling = false;
 					animationCounter = 0;
 				}
 				animationCounter -= animationLimit;
 			}
 		}
 
 		// state handeling if shooting
 		if (isShooting) {
 			for (int i = 0; i < shots.size(); i++) {
 				if (shots.get(i).isShot()) {
 					shots.get(i).update();
 				}
 			}
 		}
 		
 		//will execute if sitting essentially
 		if(!isShooting && !isFalling && !isMoving && isTurn){
 			//if the shoot key is pushed, initilize shot and start shooting
 			if(input.isKeyPressed(Input.KEY_SPACE)){
 				getShots().get(shotIndex).init(new Vector2f(hitbox.getCenterX(),hitbox.getCenterY()),new Vector2f(1*direction,10));
 				setShooting(true);
 			}
 		}
 
 		// states to be added: changing barrel angles
 
 	}
 
 	/**
 	 * Takes care of movement of tank.
 	 * @param input
 	 */
 	public void move(Input input) {
 		//wont move is shooting or falling
 		if (!isShooting && !isFalling) {
 			//checks if player has used all movement alloted this turn
 			if (movementCounter < movementLimit) {
 				float tankMovement = .5f;
 				if (input.isKeyDown(Input.KEY_A)) {
 					movementCounter += 1;
 					isMoving = true;
 					pos = movePos(-tankMovement, 0);
 					isFalling = true;
 				} else if (input.isKeyDown(Input.KEY_D)) {
 					movementCounter += 1;
 					isMoving = true;
 					pos = movePos(tankMovement, 0);
 					isFalling = true;
 				} else {
 					isMoving = false;
 				}
 			}else{
 				isMoving = false;
 			}
 		}
 	}
 
 	/**
 	 * Does the actual movement and returns new position. 
 	 * Moves and then resolves collision.
 	 * If height change is too much, will return old pos.
 	 * @param x Amount to move in the x direction
 	 * @param y Amount to move in the y drection
 	 * @return the new position
 	 */
 	private Vector2f movePos(float x, float y) {
 		Vector2f tempPos = pos.copy();
 		tempPos.x += x;
 		tempPos.y += y;
 
 		int tolerance = 2;
 		for (int i = 0; i < tolerance; i++) {
 			if (checkCollision(tempPos)) {
 				tempPos.y -= 1;
 			} else {
 				return tempPos;
 			}
 		}
 
 		return pos;
 	}
 
 	/**
 	 * Anything needed to render the tanks goes here.
 	 * 
 	 * @param container
 	 * @param game
 	 * @param g
 	 */
 	public void render(GameContainer container, StateBasedGame game, Graphics g) {
 		// current graphical representation
 		image.draw(pos.x, pos.y);
 	}
 
 	/**
 	 * Checks if tanks collide with terrain
 	 * 
 	 * @return
 	 */
 	public boolean checkCollision(Vector2f pos) {
 
 		Rectangle hitbox = new Rectangle(pos.x, pos.y, tankWidth, tankHeight);
 		float[] xRange = calculateXRange(hitbox);
 		float[] yRange = calculateYRange(hitbox);
 
 		// checks if outside the map
 		if (hitbox.getMaxX() > Main_Gameplay.map.getWidth() - 1
 				|| hitbox.getMinX() < 0
 				|| hitbox.getMaxY() > Main_Gameplay.map.getHeight() - 1
 				|| hitbox.getMinY() < 0) {
 			return true;
 		}
 
 		// gets teh highest point under the tank
 		int mapMaxInRange = Main_Gameplay.map.getMaxInRange(xRange[0],
 				xRange[1]);
 		// if lowest corner is below mapmax does collision check
 		if (yRange[1] > mapMaxInRange) {
 			for (int i = (int) xRange[0]; i < xRange[1]; i++) {
 				for (int j = (int) yRange[0]; j < yRange[1]; j++) {
 					if (Main_Gameplay.map.collision(new Vector2f(i, j))) {
 						if (hitbox.contains(i, j)) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * recalculates range if needs to
 	 */
 	/*
 	 * private void checkRange() { if (!hasRangeChanged) { return; }
 	 * 
 	 * calculateXRange(); calculateYRange(); }
 	 */
 
 	private float[] calculateXRange(Rectangle hitbox) {
 
 		float left = hitbox.getMinX();
 		float right = hitbox.getMaxX();
 
 		float[] xRange = new float[2];
 		xRange[0] = left;
 		xRange[1] = right;
 		return xRange;
 	}
 
 	private float[] calculateYRange(Rectangle hitbox) {
 
 		float top = hitbox.getMinY();
 		float bottom = hitbox.getMaxY();
 
 		float[] yRange = new float[2];
 		yRange[0] = top;
 		yRange[1] = bottom;
 		return yRange;
 	}
 
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index) {
 		this.index = index;
 	}
 
 	public ArrayList<Shot> getShots() {
 		return shots;
 	}
 
 	public void setShots(ArrayList<Shot> shots) {
 		this.shots = shots;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public void setHealth(int health) {
 		this.health = health;
 	}
 
 	public float getPlayerDir() {
 		return barrelAng;
 	}
 
 	public void setPlayerDir(int playerDir) {
 		prevAng = this.barrelAng;
 		this.barrelAng = playerDir;
 
 	}
 
 	public float getPrevDir() {
 		return prevAng;
 	}
 
 	public boolean isAlive() {
 		return health > 0 ? true : false;
 	}
 
 	public boolean isShooting() {
 		return isShooting;
 	}
 
 	public void setShooting(boolean isShooting) {
 		this.isShooting = isShooting;
 	}
 
 	public Vector2f getPos() {
 		return pos;
 	}
 
 	public void setPos(Vector2f pos) {
 		this.pos = pos;
 	}
 
 	public float getBarrelAng() {
 		return barrelAng;
 	}
 
 	public void setBarrelAng(float barrelAng) {
 		this.barrelAng = barrelAng;
 	}
 
 	public int getDirection() {
 		return direction;
 	}
 
 	public void setDirection(int direction) {
 		this.direction = direction;
 	}
 
 	public Rectangle getHitbox() {
 		return hitbox;
 	}
 
 	public void setHitbox(Rectangle hitbox) {
 		this.hitbox = hitbox;
 	}
 
 	public boolean isMoving() {
 		return isMoving;
 	}
 
 	public void setMoving(boolean isMoving) {
 		this.isMoving = isMoving;
 	}
 
 	public boolean isFalling() {
 		return isFalling;
 	}
 
 	public void setFalling(boolean isFalling) {
 		this.isFalling = isFalling;
 	}
 
 	public boolean isTurn() {
 		return isTurn;
 	}
 	
	public void setTurn(boolean isturn) {
 		this.isTurn = isTurn;
 	}
 }
