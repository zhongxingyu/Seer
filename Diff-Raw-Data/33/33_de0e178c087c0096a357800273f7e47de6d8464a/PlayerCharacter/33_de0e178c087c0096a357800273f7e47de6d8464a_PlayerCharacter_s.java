 package edu.chl.codenameg.model.entity;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 
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
 	private boolean colliding;
 	private boolean alive;
 	private Direction direction;
 	private boolean moving;
 	private int acc;
 	private boolean onGround = false;
 
 	public PlayerCharacter() {
 		this(new Position(0, 0));
 	}
 
 	public PlayerCharacter(Position position) {
 		gameWon = false;
 		this.alive = true;
 		this.setPosition(position);
 		this.v2d = new Vector2D(0, 0);
 		this.direction = Direction.RIGHT;
 	}
 
 	public void jump() {
 		// TODO Complete this method
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
 		Rectangle thisRect = new Rectangle(
 				(int) (this.getPosition().getX() + 0.5), (int) (this
 						.getPosition().getY() + 0.5), this.getHitbox()
 						.getWidth(), this.getHitbox().getHeight());
 		Rectangle collidingRect = new Rectangle(
 				(int) (e.getPosition().getX() + 0.5), (int) (e.getPosition()
 						.getY() + 0.5), e.getHitbox().getWidth(), e.getHitbox()
 						.getHeight());
 		Rectangle intersection = thisRect.intersection(collidingRect);
 		if (intersection.y == collidingRect.y) {
 			this.onGround = true;
 		}
 	}
 
 	public void update() {
 		this.update(10);
 	}
 
 	public void update(int elapsedTime) {
 		
		this.v2d = new Vector2D(0,0);
 		
		if (this.direction == Direction.RIGHT && this.moving == true) {
 			this.v2d = new Vector2D(1, 0);
		} else if (this.direction == Direction.LEFT && this.moving == true) {
 			this.v2d = new Vector2D(-1, 0);
 		}
 
 		if (onGround) {
 			this.v2d.setY(0);
 		} else {
 			this.v2d.add(new Vector2D(0, 1));
 		}
 
 		this.onGround = false;
 		this.colliding = false;
 	}
 
 	public void die() {
 		this.alive = false;
 	}
 
 	public void winGame() {
 		this.gameWon = true;
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
 		this.v2d = v2d;
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
 }
