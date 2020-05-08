 package com.msynet.ld22;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 
 public abstract class Entity {
 	
 	public Vector2f pos =  new Vector2f();
 	
 	public Vector2f dem = new Vector2f(64.0f, 64.0f);
 	public Vector2f cdem = new Vector2f(64.0f, 64.0f); // collision dem
 	
 	public float heading;
 	public boolean alive = true;
 	
 	public float speed = 1.0f;
 	
 	public abstract String getTextureName();
 	
 	public abstract Vector3f getColor();
 	
 	public static Vector2f getRandomPoint(int xmax, int ymax) {
 		return new Vector2f(LD22.random.nextInt(xmax), LD22.random.nextInt(ymax));		
 	}
 	
 	public Entity() {
 		
 	}
 	
 	private boolean willCollide(Vector2f newPos) {
 		return newPos.x < 0.0f || newPos.y < 0.0f || newPos.x >= 800.0f || newPos.y >= 600.0f;
 	}
 	
 	public void update(long stepMs) {		
 		float fracSec = (float)((float)stepMs / 1000.0);
 		
 		Vector2f newPos = new Vector2f();
 		newPos.x = pos.x + (float)(Math.cos(Math.toRadians(heading)) * fracSec) * speed;
 		newPos.y = pos.y + (float)(Math.sin(Math.toRadians(heading)) * fracSec) * speed;
 		
 		if(!willCollide(newPos)) {
 			pos = newPos;
 		} else {
			do {
				this.heading = LD22.random.nextFloat() * 360.0f;	
				newPos.x = pos.x + (float)(Math.cos(Math.toRadians(heading)) * fracSec) * speed;
				newPos.y = pos.y + (float)(Math.sin(Math.toRadians(heading)) * fracSec) * speed;
			}while(willCollide(newPos));
			
			pos = newPos;
 		}
 		
 	}
 	
 	public void gotoPoint(Vector2f epos) {
 		Vector2f vec = new Vector2f(epos.x - pos.x, epos.y - pos.y);		
 		vec.normalise();
 		 
 		double rad = Math.atan2(vec.y, vec.x);
 		
 		this.heading = (float) Math.toDegrees(rad);
 	}
 	
 	public boolean intersects( Entity other ) {		
 	  float ulx = Math.max (pos.x, other.pos.x );
 	  float uly = Math.max (pos.y, other.pos.y );
 	  float lrx = Math.min (pos.x + cdem.x, other.pos.x + other.cdem.x );
 	  float lry = Math.min (pos.y + cdem.y, other.pos.y + other.cdem.y );
 
 	  return ulx <= lrx && uly <= lry;
 	}
 	
 }
