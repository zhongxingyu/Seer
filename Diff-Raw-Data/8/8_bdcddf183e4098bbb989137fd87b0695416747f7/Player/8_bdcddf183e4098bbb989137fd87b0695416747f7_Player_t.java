 package model.sprites;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import model.geometrical.CollisionBox;
 import model.geometrical.Position;
 import model.geometrical.Rectangle;
 import model.items.Item;
 import model.items.Supply;
 import model.items.weapons.Weapon;
 import model.items.weapons.WeaponFactory;
 
 /**
  * Models a player which can populate a world.
  * 
  * @author
  *
  */
 public class Player implements Sprite {
 	
 	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 	private State state;
 	private float faceDir;
 	private float moveDir;
 	private float speed;
 	private Weapon activeWeapon;
 	private Weapon[] weapons;
 	private int health;
 	private CollisionBox collisionBox;
 	private CollisionBox hitBox;
 	private int ammo;
 	private int food;
 	
 	/**
 	 * Creates a new player with a specific position.
 	 * @param x The x-coordinate for the player's position.
 	 * @param y The y-coordinate for the player's position.
 	 */
 	public Player(float x, float y){
 		state = State.STANDING;
 		this.speed = 0.15f;
 		this.health = 100;
 		collisionBox = new Rectangle(0, 0, 0.8f, 0.8f);
 		hitBox = new Rectangle(0, 0, 0.6f, 0.6f);
 		this.food = 100;
 		this.ammo = 20;
 		this.weapons = new Weapon[3];
 		this.setStartingWeapons();
 		this.setPosition(new Position(x, y));
 	}
 	
 	@Override
 	public void addListener(PropertyChangeListener pcl) {
 		this.pcs.addPropertyChangeListener(pcl);
 	}
 	
 	@Override
 	public void moveXAxis(){
 		if(this.state == Sprite.State.RUNNING) {
 			hitBox.setPosition(new Position(hitBox.getPosition().getX() + (float)(Math.cos(moveDir)*speed), 
 					hitBox.getPosition().getY()));
 		}
 	}
 
 	@Override
 	public void moveYAxis(){
 		if(this.state == Sprite.State.RUNNING) {
 			hitBox.setPosition(new Position(hitBox.getPosition().getX(), 
 					hitBox.getPosition().getY() - (float)(Math.sin(moveDir)*speed)));
 		}
 	}
 	
 	/**
 	 * Sets the angle of which to move at. 
 	 * @param d the new angle in radians.
 	 */
 	public void setMoveDir(float d) {
 		this.setState(Sprite.State.RUNNING);
 		this.moveDir = d;
 	}
 	
 	@Override
 	public void setState(State state) {
 		this.state = state;
 	}
 	
 	/**
 	 * Returns the direction of the player.
 	 * @return the direction of the player. 
 	 */
 	public float getDirection(){
 		return faceDir;
 	}
 	
 	/**
 	 * Sets the direction the player is facing.
 	 * @param faceDir the new direction, in radians.
 	 */
 	public void setDirection(float faceDir){
 		this.faceDir = faceDir;
 	}
 	
 	/**
 	 * Returns the x-coordinate of the player's position.
 	 * @return the x-coordinate of the player's position.
 	 */
 	public float getX(){
 		return hitBox.getPosition().getX();
 	}
 	
 	/**
 	 * Returns the y-coordinate of the player's position.
 	 * @return the y-coordinate of the player's position.
 	 */
 	public float getY(){
 		return hitBox.getPosition().getY();
 	}
 	
 	/**
 	 * Set the x-coordinate of the player's position.
 	 * @param x the new x-coordinate.
 	 */
 	public void setX(float x){
 		this.setPosition(new Position(x, this.getY()));
 	}
 	
 	/**
 	 * Set the y-coordinate of the player's position.
 	 * @param y the new y-coordinate.
 	 */
 	public void setY(float y){
 		this.setPosition(new Position(this.getX(),y));
 	}
 	
 	/**
 	 * Returns the player's weapon
 	 * @return the player's weapon
 	 */
 	public Weapon getActiveWeapon(){
 		return this.activeWeapon;
 	}
 	
 	/**
 	 * Return the player's weapons.
 	 * @return the player's weapons.
 	 */
 	public Weapon[] getWeapons(){
 		return this.weapons;
 	}
 	
 	//TODO dropped weapon still exist or destroyed?
 	/**
 	 * The player drops the active weapon and picks up another. If w == null
 	 * the player will get a melee weapon
 	 * @param w the weapon the player should use after method.
 	 */
 	public void pickUpWeapon(Weapon w){
 		for(int i = 0; i<3 ; i++){
 			if(weapons[i] == activeWeapon){
 				if(w != null){
 					weapons[i] = w;
 					activeWeapon = w;
 				}else{
 					weapons[i] = WeaponFactory.createEnemyMeleeWeapon();//TODO decide "standard" weapon
 					activeWeapon = weapons[i];
 				}
 			}
 		}
 	}
 	
 	//TODO remove this method, exist for testing purposes with starting weapon
 	/**
 	 * Try to add a weapon to the player. The player can carry a maxiumum
 	 * of three weapons.
 	 * @param w The weapon which is tried to add.
 	 * @return true if the weapon is added, false if the player carry three
 	 * weapons before.
 	 */
 	public boolean addWeapon(Weapon w){
 		for(int i = 0; i<3; i++){
 			if(weapons[i] == null){
 				weapons[i] = w;
 				if(activeWeapon == null){
 					activeWeapon = w;
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Switch to a new active weapon.
 	 * @param i the index of the new weapon.
 	 * @return <code>false</code> if the index was out of bounds or if the
 	 * specified index didn't hold any weapon.
 	 */
 	public boolean switchWeapon(int i){
 		if(i >= 0 && i < weapons.length && weapons[i] != null){
 			this.activeWeapon = weapons[i];
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Gives the index in which this weapon is placed in the inventory of the player.
 	 * @param w the weapon the player got in its inventory.
 	 * @return the index of the weapon. It will return <code>-1</code> if the specified weapon
 	 * is not part of the player's.
 	 */
 	public int getIndex(Weapon w) {
 		for(int i = 0; i < this.getWeapons().length; i++) {
 			if(this.getWeapons()[i] == w) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	@Override
 	public int getHealth() {
 		return health;
 	}
 	
 	/**
 	 * Gives the position of the player.
 	 * @return the position of the player.
 	 */
 	public Position getPosition() {
 		return hitBox.getPosition();
 	}
 	
 	@Override
 	public Position getProjectileSpawn() {
 		return new Position(getX() + getHitBox().getWidth()/2 + (float)(Math.cos(faceDir)*0.45f) + (float)(Math.cos(faceDir - Math.PI/2)*0.2f), 
 				getY() + getHitBox().getHeight()/2 - (float)(Math.sin(faceDir)*0.45f) - (float)(Math.sin(faceDir - Math.PI/2)*0.2f));
 	}
 	
 	@Override
 	public Position getCenter() {
 		return new Position(getX() + getHitBox().getWidth()/2, getY() + getHitBox().getHeight()/2);
 	}
 	
 	/**
 	 * Sets the center position.
 	 */
 	@Override
 	public void setPosition(Position p) {
 		this.hitBox.setPosition(new Position(p.getX() - hitBox.getWidth()/2, p.getY() - hitBox.getHeight()/2));
 	}
 
 	@Override
 	public void reduceHealth(int damage) {
 		health = health - damage;
 		System.out.println("Enemy health: " + health);
 	}
 	
 	/**
 	 * Increase the player's health.
 	 * @param i the amount of health of which the player's health increases with.
 	 */
 	public void increaseHealth(int i){
 		this.health = this.health + i;
 		if(this.health > 100){
 			this.health = 100;
 		}
 	}
 	
 	/**
 	 * Returns the amount of ammo the player is carrying.
 	 * @return the amount of ammo the player is carrying.
 	 */
 	public int getAmmoAmount(){
 		return ammo;
 	}
 	
 	/**
 	 * Adds the specified amount of ammo to the player.
 	 * @param ammo the ammo to add.
 	 */
 	public void increaseAmmo(int pickedUpAmmo){
 		this.ammo += pickedUpAmmo;
 		if(this.ammo > 100){
 			this.ammo = 100;
 		}
 	}
 	
 	/**
 	 * Removes the specified amount of ammo from the player.
 	 * @param ammo the amount of ammo to be reloaded
 	 * @return <code>false</code> if the ammo was reduced past 0. However,
 	 * the players ammo will be set to 0.
 	 */
 	public boolean reduceAmmo(int ammo){
 		this.ammo -= ammo;
		if(this.ammo < 0) {
			this.ammo = 0;
 			return false;
 		}else{
 			return true;
 		}
 	}
 	
 	/** 
 	 * Reload the active weapon and reduces the player's ammunition by the amount
 	 * needed to reload the weapon.
 	 */
 	public void reloadActiveWeapon(){
 		int pre = ammo;
 		ammo = activeWeapon.reload(ammo);
 		if(pre != ammo) {
 			this.pcs.firePropertyChange(EVENT_RELOADING, 0, 1);
 		}
 	}
 
 	@Override
 	public void fireEvent(String event) {
 		this.pcs.firePropertyChange(event, 0, 1);
 	}
 	
 	//TODO
 	private void setStartingWeapons(){
 		weapons = new Weapon[3];
 		for(int i = 0; i<3; i++){
 			weapons[i] = WeaponFactory.getDefaultWeapon();
 		}
 		activeWeapon = weapons[0];
 	}
 	
 	/**
 	 * Return the direction the player is currently moving
 	 * @return the direction of movement
 	 */
 	public float getMoveDir(){
 		return moveDir;
 	}
 
 	@Override
 	public State getState() {
 		return this.state;
 	}
 	/**
 	 * Adds the amount of food specified to the food level of the player
 	 * @param foodToAdd the amount of food to add
 	 */
 	public void addFood(int foodToAdd){
 		this.food += foodToAdd;
 		if(this.food > 100){
 			this.food = 100;
 		}
 	}
 	/**
 	 * returns the food level of the player
 	 * @return the food level of the player
 	 */
 	public int getFood(){
 		return this.food;
 	}
 	/**
 	 * Removes the amount of food specified from the food level of the player
 	 * @param foodToRemove the amount of food to remove
 	 */
 	public void removeFood(int foodToRemove){
 		this.food -= foodToRemove;
		if(food < 0){
			food = 0;
		}
 	}
 
 	@Override
 	public boolean pickUpItem(Item i) {
 		if(i instanceof Supply){
 			Supply s = (Supply)i;
 			if(s.getType() == Supply.Type.FOOD){
 				if(this.food >= 100){
 					return false;
 				}else{
 					this.addFood(s.getAmount());
 					return true;	
 				}
 			}else if(s.getType() == Supply.Type.AMMO){
 				if(this.ammo >= 100){
 					return false;
 				}else{
 					this.increaseAmmo(s.getAmount());
 					return true;
 				}
 
 			}else if(s.getType() == Supply.Type.HEALTH){
 				if(this.health >= 100){
 					return false;
 				}else{
 					this.increaseHealth(s.getAmount());
 					return true;
 				}
 
 			}else{
 				return false;
 			}
 		}else{
 			return false;
 		}
 	}
 
 	@Override
 	public CollisionBox getMoveBox() {
 		this.collisionBox.setPosition(new Position(this.getX() + (hitBox.getWidth() - collisionBox.getWidth()) / 2
 				, this.getY() + (hitBox.getHeight() - collisionBox.getHeight()) / 2));
 		return this.collisionBox;
 	}
 	
 	@Override
 	public CollisionBox getHitBox() {
 		return this.hitBox;
 	}
 
 	@Override
 	public void moveBack() {
 		this.hitBox.moveBack();
 	}
 
 	@Override
 	public void restore(String[] data) {
 		this.speed = Float.parseFloat(data[0]);
 		this.health = Integer.parseInt(data[1]);
 		this.ammo = Integer.parseInt(data[2]);
 		this.food = Integer.parseInt(data[3]);
 		Position center = new Position(Float.parseFloat(data[4]), Float.parseFloat(data[5]));
 		this.setPosition(center);
 		this.switchWeapon(Integer.parseInt(data[6]));
 	}
 
 	@Override
 	public String[] getData() {
 		return new String[] {
 				this.speed + "",
 				this.health + "",
 				this.ammo + "",
 				this.food + "",
 				this.getCenter().getX() + "",
 				this.getCenter().getY() + "",
 				this.getIndex(getActiveWeapon()) + "",
 		};
 	}
 }
