 package model.sprites;
 
 import java.util.List;
 
 import model.geometrical.CollisionBox;
 import model.geometrical.Position;
 import model.geometrical.Rectangle;
 import model.items.Item;
 import model.items.Supply;
 import model.items.weapons.Weapon;
 import model.pathfinding.PathfindingNode;
 
 public class Enemy implements Sprite{
 
 	private State state;
 	private float direction;
 	private float speed;
 	private Weapon weapon;
 	private int health;
 	private CollisionBox collisionBox;
 	private CollisionBox hitBox;
 	private List<Position> pathfindingList;
 	private int pathfindingListIndex;
 
	/**
	 * Creates a new enemy.
	 * @param position the position of the enemy.
	 * @param speed the speed of the enemy.
	 * @param weapon the weapon of the enemy.
	 * @param health the health of the ememy.
	 */
 	protected Enemy(Position position, float speed, Weapon weapon, int health){
 		this.setState(Sprite.State.STANDING);
 		this.speed = speed;
 		this.weapon = weapon;
 		this.health = health;
 		collisionBox = new Rectangle(0, 0, 0.7f, 0.7f);
 		hitBox = new Rectangle(position.getX(), position.getY(), 0.5f, 0.5f);
 	}
 	
 	/**
 	 * Gives the position of the enemy.
 	 * @return the position of the enemy.
 	 */
 	public Position getPosition() {
 		return hitBox.getPosition();
 	}
 	
 	@Override
 	public Position getProjectileSpawn() {
 		return new Position(getX() + getHitBox().getWidth()/2 + (float)(Math.cos(direction)*0.5f), 
 				getY() + getHitBox().getHeight()/2 - (float)(Math.sin(direction)*0.5f));
 	}
 
 	/**
 	 * Sets the position of the enemy.
 	 * @param p the position of the enemy.
 	 */
 	public void setPosition(Position p) {
 		this.hitBox.setPosition(p);
 	}
 
 	@Override
 	public void moveXAxis(){
 		if(this.state == Sprite.State.MOVING) {
 			this.setDirectionTowardsList();
 			hitBox.setPosition(new Position(hitBox.getPosition().getX() + (float)(Math.cos(direction)*speed), 
 					hitBox.getPosition().getY()));
 		}
 	}
 
 	@Override
 	public void moveYAxis(){
 		if(this.state == Sprite.State.MOVING) {
 			hitBox.setPosition(new Position(hitBox.getPosition().getX(), 
 					hitBox.getPosition().getY() - (float)(Math.sin(direction)*speed)));
 		}
 	}
 	
 
 	@Override
 	public float getDirection() {
 		return direction;
 	}
 	
 	@Override
 	public int getHealth() {
 		return health;
 	}
 
 	@Override
 	public void setDirection(float direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * Gives the x-coordinate.
 	 * @return the x-coordinate.
 	 */
 	public float getX() {
 		return this.hitBox.getPosition().getX();
 	}
 
 	/**
 	 * Gives the y-coordinate.
 	 * @return the y-coordinate.
 	 */
 	public float getY() {
 		return this.hitBox.getPosition().getY();
 	}
 
 	/**
 	 * Sets the x-coordinate.
 	 * @param x the x-coordinate.
 	 */
 	public void setX(float x) {
 		this.hitBox.setPosition(new Position(x, this.getY()));
 	}
 
 	/**
 	 * Sets the y-coordinate.
 	 * @param y the y-coordinate.
 	 */
 	public void setY(float y) {
 		this.hitBox.setPosition(new Position(this.getX(),y));
 	}
 
 	@Override
 	public Weapon getActiveWeapon() {
 		return weapon;
 	}
 	
 	@Override
 	public void reduceHealth(int damage) {
 		health = health - damage;
 	}
 	
 	/**
 	 * Set a list which the enemy will move along.
 	 * @param list the list of Positions the enemy will follow.
 	 */
 	public void setWay(List<Position> list){
 		this.state = State.MOVING;
 		pathfindingListIndex = 0;
 		this.pathfindingList = list;
 	}
 	
 	/**
 	 * Set the direction of the enemy according to current list and pathfindingListIndex.
 	 */
 	private void setDirection(){
 		float dx = (float) (this.getCenter().getX() - (pathfindingList.get(pathfindingListIndex).getX()));
 		float dy = (float) (this.getCenter().getY() - (pathfindingList.get(pathfindingListIndex).getY()));
 		float sin = (float) Math.asin((float) (dy/Math.sqrt(dx*dx+dy*dy)));
 		if(dx>0){
 			this.setDirection((float)Math.PI - sin);
 		}else{
 			this.setDirection(sin);
 		}
 	}
 	
 	/**
 	 * Set the direction towards the existing list.
 	 */
 	private void setDirectionTowardsList(){
 		//If the enemy have moved to the end of the list, stand still.
 		if(pathfindingList.size() <= pathfindingListIndex){
 			this.state = State.STANDING;
 			return;//TODO varfr behvs detta?
 		}
 		
 		//If the enemy is close to the current position in the pathfindingList set the direction,
 		//otherwise increase the pathfindingListIndex and the set the direction.
 		if(Math.abs(this.getCenter().getX() - 
 				(pathfindingList.get(pathfindingListIndex).getX())) > 0.02
 				|| Math.abs(this.getCenter().getY() - 
 				(pathfindingList.get(pathfindingListIndex).getY())) > 0.02){
 			setDirection();
 		}else{
 			pathfindingListIndex++;
 			
 			//if the pathfindingListIndex increases over size of list, stand still.
 			if(!(pathfindingList.size()<=pathfindingListIndex)){
 				setDirection();
 			}else{
 				state = State.STANDING;
 			}
 		}
 	}
 
 	@Override
 	public Position getCenter() {
 		return new Position(getX() + getHitBox().getWidth()/2, getY() + getHitBox().getHeight()/2);
 	}
 
 	@Override
 	public void setState(State state) {
 		this.state = state;
 	}
 
 	@Override
 	public State getState() {
 		return this.state;
 	}
 
 	@Override
 	public boolean pickUpItem(Item i) {
 		return false;
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
 
 }
