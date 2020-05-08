 package com.secondhand.model.powerup;
 
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.PowerUpType;
 
 import junit.framework.TestCase;
 
 public class MirroredMovementTest extends TestCase {
 
 	public void testEffet() {
 		Player player = new Player(new Vector2(), 10f, 3, 0, 100);
 		MirroredMovement powerup = new MirroredMovement(new Vector2());
 		
 		assertEquals(5, powerup.getDuration(), 0.001);
 		assertEquals(5, MirroredMovement.getFrequency());	
 		
 		assertEquals(1, powerup.getR(), 0.001);	
 		assertEquals(0, powerup.getG(), 0.001);	
 		assertEquals(1, powerup.getB(), 0.001);	
 		
 		assertEquals(PowerUpType.MIRRORED_MOVEMENT, powerup.getPowerUpType());	
 		
 		powerup.activateEffect(player);
 		assertTrue(player.isMirroredMovement());
 		
 		powerup.deactivateEffect(player, true);
 		assertTrue(player.isMirroredMovement());
 		
 		powerup.deactivateEffect(player, false);
 		assertFalse(player.isMirroredMovement());
 	}
 	
 }
