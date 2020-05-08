 package de.futjikato.mrwhiz.game;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 import de.futjikato.mrwhiz.game.events.EventRespawn;
 import de.futjikato.mrwhiz.xml.Block;
 
 public class GamePlayer extends GamePhysicalObject {
 	private static final int PLAYER_WIDTH = 80;
 	private static final int PLAYER_HEIGHT = 149;
 
 	private float spawnX;
 	private float spawnY;
 
 	private int blocksize;
 	private SpriteSheet glSprite;
 	private int sprintIndex = 0;
 
 	private boolean longJump = false;
 	private boolean jumpKeyPressed = true;
 
 	private static final float BASE_SPEED = 0.5f;
 	private float speed;
 
 	private static final int START_HEALTH = 100;
 	private int health;
 	private boolean alive = true;
 
 	public GamePlayer(float spawnx, float spawny, int blocksize) {
 		this.spawnX = spawnx;
 		this.spawnY = spawny;
 		this.setX(spawnx);
 		this.setY(spawny);
 		this.blocksize = blocksize;
 
 		this.setGrip(1.2f);
 		this.setMaxYVal(1.5f);
 
 		this.speed = BASE_SPEED;
 		this.health = START_HEALTH;
 	}
 
 	private SpriteSheet getSprite() {
 		if (this.glSprite == null) {
 			try {
 				this.glSprite = new SpriteSheet("resources/images/player_game.png", 80, 149, 1);
 			} catch (SlickException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return this.glSprite;
 	}
 
 	public float getSpeed() {
 		return speed;
 	}
 
 	public void setBlockSpeed(float speed) {
 		this.speed = BASE_SPEED + speed;
 	}
 
 	public void damage(int dmg) {
 		this.health -= dmg;
 
 		if (this.health <= 0) {
 			this.die();
 		}
 	}
 
 	private void die() {
 		if (this.alive) {
 			this.alive = false;
 			System.out.println("You´re dead to me son.");
 			EventRespawn respawn = new EventRespawn(this);
 			GameTimeTrigger.getInstance().addEvent(respawn);
 		}
 	}
 
 	public void respawn() {
 		this.setX(this.spawnX);
 		this.setY(this.spawnY);
 		this.alive = true;
 	}
 
 	public void render(float vpx, float vpy) {
 		SpriteSheet sprite = this.getSprite();
 		Image tile = sprite.getSprite(0, this.sprintIndex);
 
 		// TODO this could eventually be improved a bit ;-)
 		tile.draw(this.getX() - vpx - (GamePlayer.PLAYER_WIDTH / 2), this.getY() - vpy - GamePlayer.PLAYER_HEIGHT);
 	}
 
 	public void handleInput(long delta, Input input) {
 		// get new y position
 		this.calcNewPos(this.getX(), this.getY(), this.blocksize, delta);
 
 		if (!this.alive)
 			return;
 
 		if (input.isKeyDown(Input.KEY_D)) {
 			this.setXvel(this.speed);
 			this.sprintIndex = 1;
 		}
 
 		if (input.isKeyDown(Input.KEY_A)) {
 			this.setXvel(-this.speed);
 			this.sprintIndex = 2;
 		}
 
 		if (input.isKeyDown(Input.KEY_SPACE)) {
 			this.jump();
 		} else {
 			this.jumpKeyPressed = false;
 		}
 	}
 
 	public void jump() {
 		if (this.getYVel() == 0) {
 			this.setYVel(-1.2f);
 			this.longJump = false;
 			this.jumpKeyPressed = true;
 		} else if (this.getYVel() > -0.7 && this.getYVel() < -0.5 && this.jumpKeyPressed && !this.longJump) {
 			this.setYVel(-0.9f);
 			this.longJump = true;
 		}
 	}
 
 	@Override
 	protected int getHeight() {
 		return PLAYER_HEIGHT;
 	}
 
 	@Override
 	protected int getWidth() {
 		return PLAYER_WIDTH;
 	}
 
 	@Override
 	protected void hitBlock(Block block) {
 
 		if (!this.alive)
 			return;
 
 		int dmg = block.getDamage();
 		if (dmg > 0) {
 			this.damage(dmg);
 		}
 
 		float speed = block.getSpeed();
		if (speed > 0) {
			this.setBlockSpeed(speed);
		}
 	}
 }
