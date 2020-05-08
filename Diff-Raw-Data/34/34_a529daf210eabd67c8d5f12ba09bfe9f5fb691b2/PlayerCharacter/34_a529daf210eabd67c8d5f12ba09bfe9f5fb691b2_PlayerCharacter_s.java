 package edu.chl.codenameg.model.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.Direction;
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.Vector2D;
 
 public class PlayerCharacter implements Entity {
 	private final Hitbox hitbox = new Hitbox(10, 15);
 	private boolean gameWon;
 	private Position pt;
 	private Vector2D v2d;
 	private Vector2D addVector;
 	private Vector2D gravity;
 	private boolean colliding;
 	private boolean alive;
 	private Direction direction;
 	private boolean moving;
 	private int acc;
 	private boolean onGround = false;
 	private boolean jumping = false;
 	private boolean justJumped = false;
 	private List<Entity> collidingList;
 
 	public PlayerCharacter() {
 		this(new Position(0, 0));
 	}
 
 	public PlayerCharacter(Position position) {
 		gameWon = false;
 		this.alive = true;
 		this.setPosition(position);
 		this.v2d = new Vector2D(0, 0);
 		this.addVector = new Vector2D(0, 0);
 		this.direction = Direction.RIGHT;
 		this.collidingList = new ArrayList<Entity>();
 		this.gravity = new Vector2D(0,1);
 	}
 
 	public void jump() {
 		this.jumping = true;
 	}
 
 	public void stopJump() {
 		if(jumping)
 			this.justJumped = true;
 		this.jumping = false;
 		
 	}
 
 	public void move() {
 		this.moving = true;
 	}
 
 	public void move(Direction d) {
 		this.setDirection(d);
 		this.move();
 	}
 
 	public void setDirection(Direction d) {
 		this.direction = d;
 	}
 
 	public void stopMove() {
 		this.moving = false;
 	}
 
 	public Direction getDirection() {
 		Direction temp = this.direction;
 		return temp;
 	}
 
 	public void collide(Entity e) {
 		this.colliding = true;
 		if (this.getPosition().getY() + this.getHitbox().getHeight() == e.getPosition().getY() && this.getCollideTypes().contains(e.getType())) {
 			this.onGround = true;
 			this.justJumped = false;
 		}
 
 		this.collidingList.add(e);
 	}
 
 	public void update() {
 		this.update(10);
 	}
 
 	public void update(int elapsedTime) {
 		this.checkCollisionDeath();
 		if (!alive) {
 			
 		}
 		
 		this.v2d = new Vector2D(addVector);
 		this.addVector = new Vector2D(0, 0);
 
 		if (this.direction == Direction.RIGHT && this.moving) {
 			this.v2d.add(new Vector2D(1, 0));
 		} else if (this.direction == Direction.LEFT && this.moving) {
 			this.v2d.add(new Vector2D(-1, 0));
 		}
 
 		if (jumping && !justJumped) {
 			this.v2d.add(new Vector2D(0,-4));
 		} else if(justJumped) { //TODO Not being able to jump if just dropped from height
 			this.v2d.add(new Vector2D(0,-2));
 		}
 		
 
 		if (!onGround) {
 			this.gravity.add(new Vector2D(0,0.1f));
 		} else {
 			this.gravity = new Vector2D(0,0.98f);
 		}
 		this.v2d.add(this.gravity);
 
 		this.onGround = false;
 		this.colliding = false;
 	}
 
 	private void checkCollisionDeath() {
 		//TODO Check collision from both sides.
 		if (collidingList.size() > 0) {
 			int collideLeftCount = 0;
 			int collideRightCount = 0;
 			int collideTopCount = 0;
 			int collideBottomCount = 0;
 //			for (Entity colliding : collidingList) {
 //				if (colliding.getPosition().getX() + colliding.getHitbox().getWidth() >= this.getPosition().getX() && colliding.getPosition().getX() + colliding.getHitbox().getWidth() <= this.getPosition().getX() + this.getHitbox().getWidth()) {
 //					collideLeftCount++;
 //				}
 //				if (colliding.getPosition().getX() <= this.getPosition().getX() + this.getHitbox().getWidth() && colliding.getPosition().getX() >= this.getPosition().getX()) {
 //					collideRightCount++;
 //				}
 //				if (colliding.getPosition().getY() + colliding.getHitbox().getHeight() >= this.getPosition().getY() && colliding.getPosition().getY() + colliding.getHitbox().getHeight() <= this.getPosition().getY() + this.getHitbox().getHeight()) {
 //					collideTopCount++;
 //				}
 //				if (colliding.getPosition().getY() <= this.getPosition().getY() + this.getHitbox().getHeight() && colliding.getPosition().getY() >= this.getPosition().getY()) {
 //					collideBottomCount++;
 //				}
 //			}
 //			Rectangle thisRectangle = new Rectangle(Math.round(this.getPosition().getX()), Math.round(this.getPosition().getY()), this.getHitbox().getWidth(), this.getHitbox().getHeight());
 //			for (Entity colliding : collidingList) {
 //				Rectangle collidingRectangle = new Rectangle(Math.round(colliding.getPosition().getX()), Math.round(colliding.getPosition().getY()), colliding.getHitbox().getWidth(), colliding.getHitbox().getHeight());
 //				Rectangle intersection = thisRectangle.intersection(collidingRectangle);
 //				if(intersection.getX() == this.getPosition().getX()) {
 //					collideLeftCount++;
 //				}
 //				if(intersection.getX() + intersection.getWidth() == this.getPosition().getX() + this.getHitbox().getWidth()) {
 //					collideRightCount++;
 //				}
 //				if(intersection.getY() == this.getPosition().getY()) {
 //					collideTopCount++;
 //				}
 //				if(intersection.getY() + intersection.getHeight() == this.getPosition().getY() + this.getHitbox().getHeight()) {
 //					collideBottomCount++;
 //				}
 //			}
 			
 			if (collideLeftCount > 0 && collideRightCount > 0) {
 				this.die();
 			} else if (collideTopCount > 0 && collideBottomCount > 0) {
 				this.die();
 			}
 			this.collidingList.clear();
 		}
 	}
 
 	public void die() {
 		this.alive = false;
 		System.out.println("Auuuuuugh!");
 	}
 
 	public void winGame() {
 		this.gameWon = true;
 		System.out.println("I won, I won, I won ! :D");
 	}
 
 	// getters & setters
 	public void setPosition(Position p) {
 		this.pt = p;
 	}
 
 	public Position getPosition() {
 		return new Position(this.pt);
 	}
 
 	public Hitbox getHitbox() {
 		return new Hitbox(hitbox);
 	}
 
 	public void setVector2D(Vector2D v2d) {
 		this.v2d = new Vector2D(v2d);
 	}
 
 	public void addVector2D(Vector2D v2d) {
 		this.addVector = new Vector2D(v2d);
 	}
 
 	public Vector2D getVector2D() {
 		return new Vector2D(this.v2d);
 	}
 
 	public boolean isColliding() {
 		boolean temp = this.colliding;
 		return temp;
 	}
 
 	public boolean hasWonGame() {
 		boolean temp = this.gameWon;
 		return temp;
 	}
 
 	public boolean isAlive() {
 		boolean temp = this.alive;
 		return temp;
 	}
 
 	public void setAcceleration(int a) {
 		this.acc = a;
 	}
 
 	public int getAcceleration() {
 		int temp = this.acc;
 		return temp;
 	}
 
 	public boolean isOnGround() {
 		return this.onGround;
 	}
 
 	@Override
 	public List<String> getCollideTypes() {
 		List<String> list = new ArrayList<String>();
 		list.add("Block");
 		return list;
 	}
 
 	@Override
 	public String getType() {
 		return "PlayerCharacter";
 	}
 }
