 package com.angrykings.cannons;
 
 import com.angrykings.GameContext;
 import com.angrykings.PhysicsManager;
 import com.angrykings.ResourceManager;
 import com.badlogic.gdx.math.Vector2;
 import org.andengine.entity.Entity;
 import org.andengine.entity.sprite.Sprite;
 
 /**
  * Cannon
  *
  * @author Shivan Taher <zn31415926535@gmail.com>
  * @date 31.05.13
  */
 public class Cannon extends Entity {
 	protected Sprite barrelSprite, wheelSprite, aimCircleSprite;
 
 	protected final boolean isLeft;
 	protected final float minAngle, maxAngle;
 
 	public Cannon(boolean isLeft) {
 		ResourceManager rm = ResourceManager.getInstance();
 
 		this.isLeft = isLeft;
 
 		GameContext gc = GameContext.getInstance();
 
 		this.wheelSprite = new Sprite(-76, 0, rm.getWheelTexture(), gc.getVboManager());
 		this.barrelSprite = new Sprite(0, 12, rm.getCannonTexture(), gc.getVboManager());
 		this.barrelSprite.setRotationCenter(40.0f, 16.0f);
 		this.aimCircleSprite = new Sprite(
 				0,
 				-rm.getAimCircleTexture().getHeight() + rm.getWheelTexture().getHeight(),
 				rm.getAimCircleTexture(),
 				gc.getVboManager()
 		);
 		
 		this.attachChild(this.barrelSprite);
 		this.attachChild(this.wheelSprite);
 		
 
 		if (!isLeft) {
 			this.setScale(-1.0f, 1.0f);
 			this.minAngle = 280;
 			this.maxAngle = 360;
 		} else {
 			this.minAngle = -80;
 			this.maxAngle = 0;
 		}
 
 		this.setPosition(25, 75);
 	}
 
 	/**
 	 *
 	 * @param x
 	 * @param y
 	 * @return	Returns true if the angle was set and false if the angle is out of bounds
 	 */
 	public boolean pointAt(int x, int y) {
 		double rotation = Math.atan2(y - this.getY() - this.barrelSprite.getHeight() / 2, x - this.getX());
 
 		rotation = Math.toDegrees(rotation);
 
 		if (!this.isLeft)
 			rotation = -rotation + 180;
 
 		if (rotation > this.minAngle && rotation < maxAngle) {
 			this.barrelSprite.setRotation((float) rotation);
 			return true;
 		}
 
 		return false;
 	}
 
 	public Vector2 getDirection() {
 		float angle = (float) Math.toRadians((double) this.barrelSprite.getRotation());
 
 		if (this.isLeft) {
 			return new Vector2((float) Math.cos(angle), (float) Math.sin(angle)).nor();
 		} else {
 			return new Vector2(-(float) Math.cos(angle), (float) Math.sin(angle))
 					.nor();
 		}
 	}
 
 	private Vector2 getBarrelEndPosition() {
 		if (this.isLeft) {
			return new Vector2(this.getX(), this.getY() + 9).add(this
					.getDirection().mul(64));
 		} else {
			return new Vector2(this.getX(), this.getY() + 9).add(this
					.getDirection().mul(64));
 		}
 	}
 
 	public Cannonball fire(float force) {
 		GameContext gc = GameContext.getInstance();
 
 		Vector2 ballPosition = this.getBarrelEndPosition();
 
 		Cannonball ball = new Cannonball(ballPosition.x, ballPosition.y);
 		ball.registerPhysicsConnector();
 
 		ball.getBody().applyLinearImpulse(this.getDirection().mul(force), ball.getBody().getPosition());
 		ball.getAreaShape().setPosition(ballPosition.x, ballPosition.y);
 
 		gc.getScene().attachChild(ball.getAreaShape());
 
 		PhysicsManager.getInstance().addPhysicalEntity(ball);
 
 		return ball;
 	}
 	
 	public void showAimCircle(){
 		this.attachChild(aimCircleSprite);
 	}
 	
 	public void hideAimCircle(){
 		this.detachChild(aimCircleSprite);
 	}
 }
