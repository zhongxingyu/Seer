 package com.realapps.engine.core.util.physics;
 
 public class PhysicsManager {
 	private CollisionChecker collision = new CollisionChecker();
 	
	public CollisionChecker getCollisionChecker() {
		return collision;
	}
	
 	public void checkPhysics() {
 		collision.check();
 	}
 }
