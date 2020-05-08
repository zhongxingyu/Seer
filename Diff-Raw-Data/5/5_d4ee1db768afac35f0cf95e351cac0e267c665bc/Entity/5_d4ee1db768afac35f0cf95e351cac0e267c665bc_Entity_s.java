 package game.entity;
 
 import java.awt.Rectangle;
 
 import engine.WMath;
 import engine.render.Renderable;
 import game.WormsGame;
 
 public abstract class Entity extends Renderable {
 	
 	//Position and Size
 	protected float x, y, width, height;
 	//Movement
 	protected float xMotion, yMotion;
 	protected float fallDuration;
 	protected float fallDistance;
 	
 	//Game Reference
 	protected WormsGame wormsGame;
 	
 	
 	public Entity(WormsGame wormsGame, int x, int y, int width, int height) {
 		super();
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		this.wormsGame = wormsGame;
 		setFalling(false);
 	}
 	
 	public void setFalling(boolean b) {
 		fallDuration = (b) ? 1 : 0;
 	}
 	
 	public boolean isOnGround() {
 		return wormsGame.collides(this, 0, -1);
 	}
 	
 	public void doMovement() {
 		if(xMotion != 0 || yMotion != 0) {
 			setRenderUpdate(true);
 			x += xMotion;
 			y -= yMotion;
 			while(wormsGame.collides(this, 0, 1)) {
 				--y;
				--yMotion;
 			}
 			if(fallDuration > 0)
 				fallDistance += WMath.abs_f(yMotion);
 			xMotion = yMotion = 0;
 		}
 	}
 	
 	public abstract void onTick();
 	
 	public boolean isFalling() {
 		return fallDuration > 0;
 	}
 	
 	public Rectangle getCollisionBox() {
 		return new Rectangle((int)x, (int)y, (int)width, (int)height);
 	}
 
 }
