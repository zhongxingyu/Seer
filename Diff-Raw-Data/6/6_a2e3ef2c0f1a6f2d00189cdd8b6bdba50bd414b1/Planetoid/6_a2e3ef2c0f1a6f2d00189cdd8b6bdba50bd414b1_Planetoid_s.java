 package com.calumgilchrist.ld23.tinyworld.core;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.World;
 
 public class Planetoid {
 	private Sprite sprite;
 	private Body pBody;
 	
 	private Vec2 pos;
 	
 	public Planetoid(Vec2 startPos, Sprite s, BodyDef bodyDef, World world){
 		//TODO: Need an initial mass
 		
 		this.pos = startPos;
 		
 		this.sprite = s;
 		this.pBody = world.createBody(bodyDef);
 	}
 	
 	/**
 	 * Apply force to the center of the planetoid
 	 * @param force
 	 */
 	public void applyThrust(Vec2 force) {
 
 		force = force.mul(1/Constants.PHYS_RATIO);
 		this.getBody().applyForce(force, this.getBody().getWorldCenter());
 	}
 	
 	public Sprite getSprite(){
 		return sprite;
 	}
 
 	public Vec2 getPos() {
 		return pos;
 	}
 
 	public void setPos(Vec2 pos) {
 		this.pos = pos;
 		
 		sprite.setX((int) pos.x);
 		sprite.setY((int) pos.y);
 	}
 	
 	public Body getBody() {
 		return this.pBody;
 	}
 	
 	public void update(){
		this.getSprite().setX((int) this.getBody().getWorldCenter().x);
		this.getSprite().setX((int) this.getBody().getWorldCenter().y);
 		this.getSprite().update();
 	}
 }
