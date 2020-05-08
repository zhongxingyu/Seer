 package com.gozdy.HookIT;
 
 
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 
 
 
 public class Enemy extends DynamicGameObject {
 
     public static final float ENEMY_WIDTH = 0.8f;
     public static final float ENEMY_HEIGHT = 0.8f;
     public static final int HOOKED = 0;
     public static final int ALIVE = 1;
 	
     int state;
     float stateTime;  
     float speed = 20f;
     float angle;
     Vector2 gravity = new Vector2(0,-0.3f);
     int candyType;
     
 	public Enemy(float x, float y, float width, float height) {
 		super(x, y, ENEMY_WIDTH, ENEMY_HEIGHT);
 		angle = MathUtils.random(70, 85);
 		state = ALIVE;
 		stateTime=0;
 		velocity.set(MathUtils.cosDeg(angle)*speed, MathUtils.sinDeg(angle)*speed);
 		candyType = MathUtils.random(0, 3);
 	
 	}
 	
 	public boolean isOutOfBounds(float width, float height)
 	{
 		if (position.x>width || position.y > height)
 		return true;
 		else
 			return false;
 	}
 	
 	  public void update(float delta)
 	    {
 		  
 		  if (state == ALIVE){
 			  
 			  
 		 velocity.add(gravity);
 		  position.add(velocity.x * delta, velocity.y * delta);
 		  bounds.setX(position.x);
 			 bounds.setY(position.y);
 			 
 			 
 			 
 		  }
 
 		  stateTime += delta;
 	    }
 	  
 	  
 	  
 	  
 
 }
