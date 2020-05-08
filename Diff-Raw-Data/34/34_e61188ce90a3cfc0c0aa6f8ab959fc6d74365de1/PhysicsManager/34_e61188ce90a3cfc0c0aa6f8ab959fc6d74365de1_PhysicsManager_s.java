 package com.realapps.engine.core.util.physics;
 

 public class PhysicsManager {
 	private CollisionChecker collision = new CollisionChecker();
 	
 	public void checkPhysics() {
 		collision.check();
 	}
 }
