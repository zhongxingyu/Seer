 package com.secondhand.model;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.model.PowerUp.Effect;
 
 import junit.framework.TestCase;
 
 public class PowerUpTest extends TestCase {
 	
 	public void testConstructor() {
		PowerUp pu1 = new PowerUp(new Vector2(),5,Effect.SCORE_UP,null) {};
		PowerUp pu2 = new PowerUp(new Vector2(),5,Effect.SPEED_UP,null) {};
 		
 		assertEquals(pu1.getEffect(), Effect.SCORE_UP);
 		assertEquals(pu2.getEffect(), Effect.SPEED_UP);
 	}
 	
 }
