 package cz.emo4d.zen.gameplay;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.utils.Array;
 
 public class Mob extends Entity {
 
 	public static float MAX_VELOCITY = 8f;
 	public static float DAMPING = 0.87f;
 	public int health;
 	public boolean alive = true;
 
 	// gui sem nastavuje obrazky se srdickama, ktere si pak hrac updatuje
 	public final Array<Image> hearts = new Array<Image>();
 
 
 
 	protected enum State {
 		Standing, Walking, Shooting
 	}
 
 	public State state = State.Walking;
 	public Direction currentDir = Direction.S;
 
 	public Effect effect;
 
 	public Mob() {
 		super();
 
 		health = getMaxHealth();
 	}
 
 
 	public int getMaxHealth() {
 		return 50;
 	}
 
 
 	public void takeHit(int amount) {
 		health -= amount;
 		if (health < 0) {
 			health = 0;
 		}
 
 		if (hearts.size > 0) {
 			updateHearts();
 		}
 	}
 
 	public void updateHearts() {
 		float hpPerHeart = getMaxHealth() / 5f;
 
 		for (int i = 1; i <= 5; i++) {
 			Image im = hearts.get(i - 1);
 			Color c = im.getColor();
 
 			im.setColor(c.r, c.g, c.b, 1);
 
			if (health == 0) {
				im.setColor(c.r, c.g, c.b, 0);
			}

 			if (health < i * hpPerHeart) {
 				float part = (health - ((i - 1) * hpPerHeart)) / (float) hpPerHeart;
 
 				if (part < 0) {
 					part = 0;
 				}
 
 				im.setColor(c.r, c.g, c.b, part);
 			}
 		}
 
 
 	}
 
 
 
 
 	public void move(Vector2 dir) {
 		float dirAngle = dir.angle();
 
 		if (dirAngle > 337.5f) {
 			currentDir = Direction.E;
 		} else if (dirAngle > 292.5f) {
 			currentDir = Direction.SE;
 		} else if (dirAngle > 247.5f) {
 			currentDir = Direction.S;
 		} else if (dirAngle > 202.5f) {
 			currentDir = Direction.SW;
 		} else if (dirAngle > 157.5f) {
 			currentDir = Direction.W;
 		} else if (dirAngle > 112.5f) {
 			currentDir = Direction.NW;
 		} else if (dirAngle > 67.5f) {
 			currentDir = Direction.N;
 		} else if (dirAngle > 22.5f) {
 			currentDir = Direction.NE;
 		} else
 			currentDir = Direction.E;
 
 		velocity.set(dir.nor().mul(MAX_VELOCITY));
 		state = State.Walking;
 	}
 
 	public void update(float deltaTime) {
 
 		// clamp the velocity to the maximum
 		if (Math.abs(velocity.x) > MAX_VELOCITY) {
 			velocity.x = Math.signum(velocity.x) * MAX_VELOCITY;
 		}
 		if (Math.abs(velocity.y) > MAX_VELOCITY) {
 			velocity.y = Math.signum(velocity.y) * MAX_VELOCITY;
 		}
 
 		// clamp the velocity to 0 if it's < 1, and set the state to standing
 		if (Math.abs(velocity.x) < 1 && Math.abs(velocity.y) < 1) {
 			velocity.set(0, 0);
 			state = State.Standing;
 		}
 
 		// multiply by delta time so we know how far we go
 		// in this frame
 		velocity.mul(deltaTime);
 
 		switch (state) {
 			case Standing:
 				break;
 			case Walking: {
 				effect.update(getDirectionNumber(currentDir), true);
 				break;
 			}
 			case Shooting:
 				//frame = jump.getKeyFrame(koala.stateTime);
 				//effect.update(2, true);
 				break;
 		}
 
 		collisionWithMap();
 
 		// unscale the velocity by the inverse delta time and set
 		// the latest position
 		this.position.add(this.velocity);
 		this.velocity.mul(1 / deltaTime);
 
 		// Apply damping to the velocity so we don't
 		// walk infinitely once a key was pressed
 		this.velocity.mul(Mob.DAMPING);
 	}
 
 
 
 	public void render(SpriteBatch spriteBatch) {
 		effect.render(spriteBatch, position.x, position.y, 1 / 32f * (effect.width), 1 / 32f * (effect.height));
 	}
 
 
 	public boolean collisionWithMap() {
 		// perform collision detection & response, on each axis, separately
 		// if the koala is moving right, check the tiles to the right of it's
 		// right bounding box edge, otherwise check the ones to the left
 		boolean collided = false;
 
 		Rectangle playerRect = rectPool.obtain();
 		playerRect.set(this.position.x, this.position.y, this.WIDTH, this.HEIGHT);
 
 		int startX, startY, endX, endY;
 		if (this.velocity.x > 0) {
 			startX = endX = (int) (this.position.x + this.WIDTH + this.velocity.x);
 		} else {
 			startX = endX = (int) (this.position.x + this.velocity.x);
 		}
 		startY = (int) (this.position.y);
 		endY = (int) (this.position.y + this.HEIGHT);
 		getTiles(startX, startY, endX, endY, tiles);
 		playerRect.x += this.velocity.x;
 		for (Rectangle tile : tiles) {
 			if (playerRect.overlaps(tile)) {
 				this.velocity.x = 0;
 				collided = true;
 				break;
 			}
 		}
 		playerRect.x = this.position.x;
 
 		// if the koala is moving upwards, check the tiles to the top of it's
 		// top bounding box edge, otherwise check the ones to the bottom
 		if (this.velocity.y > 0) {
 			startY = endY = (int) (this.position.y + this.HEIGHT + this.velocity.y);
 		} else {
 			startY = endY = (int) (this.position.y + this.velocity.y);
 		}
 		startX = (int) (this.position.x);
 		endX = (int) (this.position.x + this.WIDTH);
 		getTiles(startX, startY, endX, endY, tiles);
 		playerRect.y += this.velocity.y;
 		for (Rectangle tile : tiles) {
 			if (playerRect.overlaps(tile)) {
 				this.velocity.y = 0;
 				collided = true;
 				break;
 			}
 		}
 		rectPool.free(playerRect);
 
 		return collided;
 
 	}
 
 
 }
