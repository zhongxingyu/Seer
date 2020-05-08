 package entities;
 
 import game.debug.FrameTrace;
 
 import java.awt.Dimension;
 
 import map.Cell;
 import map.TileProperty;
 
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 
 import utils.MapLoader;
 import utils.Position;
 
 
 public abstract class Entity implements IEntity {
 	
 	private static final float HITBOX_MARGIN = 0.25f;
 	private static final int DEFAULT_MAXHEALTH = 100;
 	
 	private final Position xy, dxdy = new Position(0f,0f);
 	private final Dimension size;
 	
 	//TODO:	Implement entity image system.
 	//		No idea how at the moment.
 	
 	private int health,maxhealth;
 	
 	//for debugging purposes:
 	private final FrameTrace frameTrace = new FrameTrace();
 
 	public Entity(Position xy,Dimension size, int maxhealth) {
 		this.xy = xy;
 		this.size = size;
 		this.health = maxhealth;
 		this.maxhealth = maxhealth;
 	}
 	
 	public Entity(float x, float y, int width, int height, int maxhealth) {
 		this(new Position(x,y),new Dimension(width,height),maxhealth);
 	}
 	
 	public Entity(float x, float y, int maxhealth){
 		this(new Position(x,y),new Dimension(1,1),maxhealth);
 	}
 	
 	public Entity(int width, int height, int maxhealth){
 		this(new Position(0,0),new Dimension(width,height),maxhealth);
 	}
 	
 	public Entity(float x, float y, int width, int height){
 		this(new Position(x,y),new Dimension(width,height),DEFAULT_MAXHEALTH);
 	}
 	
 	public Entity(float x, float y){
 		this(new Position(x,y),new Dimension(1,1),DEFAULT_MAXHEALTH);
 	}
 	
 	public Entity(int width, int height){
 		this(new Position(0,0),new Dimension(width,height),DEFAULT_MAXHEALTH);
 	}
 	
 	public Entity(){
 		this(new Position(0,0),new Dimension(1,1),DEFAULT_MAXHEALTH);
 	}
 	
 	/**
 	 * Returns the current x-position of this entity.
 	 */
 	@Override
 	public float getX(){
 		return xy.getX();
 	}
 	
 	/**
 	 * Returns the current y-position of this entity.
 	 */
 	@Override
 	public float getY(){
 		return xy.getY();
 	}
 	
 	/**
 	 * Returns the current x-velocity of this entity.
 	 */
 	@Override
 	public float getdX(){
 		return dxdy.getX();
 	}
 	
 	/**
 	 * Returns the current y-velocity of this entity.
 	 */
 	@Override
 	public float getdY(){
 		return dxdy.getY();
 	}
 	
 	/**
 	 * Returns the width of the hitbox of this entity.
 	 */
 	@Override
 	public int getWidth(){
 		return size.width;
 	}
 	
 	/**
 	 * Returns the height of the hitbox of this entity;
 	 */
 	@Override
 	public int getHeight(){
 		return size.height;
 	}
 	
 	/**
 	 * Reduces this entity's health by an amount influenced by the argument provided according to some formula.
 	 * @param normalDamage The damage dealt normally ignoring special hits and armour effects etc...
 	 * @return The actual amount of damage taken by this entity.
 	 */
 	@Override
 	public int takeDamage(int normalDamage){
 		//TODO: update for armour etc...
 		int originalHealth = health;
 		health = Math.max(0, health - normalDamage);
 		return originalHealth - health;
 	}
 	
 	/**
 	 * Returns the amount of damage done by this entity when taking into account critical hits etc...
 	 */
 	@Override
 	public int getDamage(){
 		//TODO: implement
 		return -1;
 	}
 	
 	/**
 	 * Returns the normal damage (excluding critical hits etc...) done by this entity. 
 	 */
 	@Override
 	public int getNormalDamage(){
 		//TODO: implement
 		return -1;
 	}
 	
 	/**
 	 * Returns the absolute value of this entity's current health.
 	 */
 	@Override
 	public int getHealth(){
 		return health;
 	}
 	
 	/**
 	 * Returns a float value in the range [0.0 - 1.0] inclusive representing the entity's current health.
 	 */
 	@Override
 	public float getHealthPercent(){
 		return (float)health/maxhealth;
 	}
 	
 	/**
 	 * Returns the absolute value of this entity's maximum possible health.
 	 */
 	@Override
 	public int getMaxHealth(){
 		return maxhealth;
 	}
 	
 	/**
 	 * Moves this entity by it's current velocity values and applies constants such as friction and gravity.
 	 * @param delta The time in microseconds since the last frame update.
 	 */
 	@Override
 	public void frameMove() {
 //		float modFriction = getFrictionDelta(delta);
 //		float modGravity  = getGravityDelta(delta);
 		
 		//both x and y axis are affected by scalar friction
 		if (!isOnGround()) {
 			dxdy.translate(0f,GRAVITY); //fall if not on the ground
 		} else if (getdY() > 0) {
 			dxdy.setY(0);
 		}
 		dxdy.scale(FRICTION);
 		frameTrace.add(xy,dxdy);
 		xy.translate(dxdy); //move to new location
 		
 		//collision stuff
 		try{
 			int bottom = bottom();
 			int top    = top();
			int left   = left();
			int right  = right();
 			if (bottom > top) {
 				//if the new location is on the ground, set it so entity isn't clipping into the ground
 				setPosition(getX(), (int)getY());
 			}
 			//vertical collision
 			if (top > bottom) {
 			    dxdy.setY(0f);
 			    setPosition(getX(), (int)getY() + 1);
 			}
 			//horizontal collision
 			if (left > right) {
 			    dxdy.setX(0f);
 			    setPosition((int)getX() + 1, getY());
 			}
 			if (right > left) {
 			    dxdy.setX(0f);
 			    setPosition((int)getX(), getY());
 			}
 		}catch(RuntimeException e){
 			System.out.println(e.getMessage());
 			frameTrace.printTrace();
 			throw e;
 		}
 		
 	}
 	
 	@Override
 	public void setPosition(float x, float y) {
 		xy.set(x,y);
 	}
 	
 	/**
 	 * Returns true if and only if this entity has an absolute health equal to zero.
 	 */
 	@Override
 	public boolean isDead() {
 		return health <= 0;
 	}
 	
 	/**
 	 * returns whether the entity is touching the ground
 	 * @return true if touching ground
 	 */
 	@Override
 	public boolean isOnGround() {
 		return bottom() > 0;
 	}
 	
 	//collision checkers
 	private int top() {
 		Cell currentCell = MapLoader.getCurrentCell();
 		int count = 0;
 		for(float x=getX()+HITBOX_MARGIN;x<getX()+getWidth();x+=0.5f){
 			if("true".equals(currentCell.getTile((int) x, (int) getY()).lookupProperty(TileProperty.BLOCKED))){
 				++count;
 			}
 		}
 		return count;
 	}
 	
 	private int bottom() {
 		Cell currentCell = MapLoader.getCurrentCell();
 		int count = 0;
 		for(float x=getX()+HITBOX_MARGIN;x<getX()+getWidth();x+=0.5f){
 			if("true".equals(currentCell.getTile((int) x, (int) getY() + getHeight()).lookupProperty(TileProperty.BLOCKED))){
 				++count;
 			}
 		}
 		return count;          
 	}
 	
 	private int left() {
 		Cell currentCell = MapLoader.getCurrentCell();
 		int count = 0;
 		for(float y=getY()+HITBOX_MARGIN;y<getY()+getHeight();y+=0.5f){
 			if("true".equals(currentCell.getTile((int) getX(), (int) y).lookupProperty(TileProperty.BLOCKED))){
 				++count;
 			}
 		}
 		return count;
 	}
 	
 	private int right() {
 		Cell currentCell = MapLoader.getCurrentCell();
 		int count = 0;
 		for(float y=getY()+HITBOX_MARGIN;y<getY()+getHeight();y+=0.5f){
 			if("true".equals(currentCell.getTile((int) getX() + getWidth(), (int) y).lookupProperty(TileProperty.BLOCKED))){
 				++count;
 			}
 		}
 		return count;
 	}
 	
 	/**
 	 * makes the entity jump. if it is falling, sets its vertical change to zero first.
 	 */
 	@Override
 	public void jump() {
 		dxdy.translate(0f,-JUMP_AMOUNT);
 	}
 	
 	@Override
 	public void moveX(float x) {
 		dxdy.setX(x);
 	}
 	
 	public boolean isMovingX(){
 		return Math.abs(dxdy.getX()) > 0.02f;
 	}
 	
 	@Override
 	public abstract void render();
 
 	@Override
 	public abstract void update(Input input);
 
 	@Override
 	public void stop_sounds(){
 		//left blank in case sounds are moved to this class.
 		//should be overridden to add class-specific sounds with a call to the super method.
 	}
 
 	@Override
 	public boolean intersects(Rectangle hitbox) {
 		return hitbox.getX() + hitbox.getWidth() > getX() && 
 				hitbox.getX() < getX() + getWidth() &&
 				hitbox.getY() + hitbox.getHeight() > getY() &&
 				hitbox.getY() < getY() + getHeight();
 	}
 	
 }
