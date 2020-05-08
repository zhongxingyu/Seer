 package ultraextreme.model.entity;
 
 import junit.framework.TestCase;
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.model.util.Position;
 import ultraextreme.model.util.Rotation;
 
 /**
  * 
  * @author Viktor Anderling
  * @author Daniel Jonsson
  * @author Bjorn Persson Mattsson
  * 
  */
 public abstract class AbstractEntityTest extends TestCase {
 
 	protected abstract AbstractEntity getNewAbstractEntity(double x, double y,
 			int width, int height, Rotation direction);
 
 	public void testMove() {
 		AbstractEntity entity = newEntity();
 		entity.move(0, 0);
 		assertEquals(entity.getPosition(), new Position(10.0, 20.0));
 
 		entity.move(10, 100);
 		assertEquals(entity.getPosition(), new Position(20.0, 120.0));
 
 		entity.move(-100, -1000);
 		assertEquals(entity.getPosition(), new Position(-80.0, -880.0));
 	}
 
 	public void testSetPosition() {
 		AbstractEntity entity = newEntity();
 		entity.setPosition(new Position(40, 50));
 		assertEquals(entity.getPosition().getX(), 40.0);
 		assertEquals(entity.getPosition().getY(), 50.0);
 		entity.setPosition(new Position(-40, -50));
 		assertEquals(entity.getPosition().getX(), -40.0);
 		assertEquals(entity.getPosition().getY(), -50.0);
 	}
 
 	public void testGetPosition() {
 		AbstractEntity entity = newEntity();
 		assertEquals(entity.getPosition().getX(), 10.0);
 		assertEquals(entity.getPosition().getY(), 20.0);
 	}
 
 	public void tetGetCenteredPosition() {
		fail("Not implemented");
		// TODO Write getCenteredPosition test
 	}
 
 	public void testGetWidth() {
 		AbstractEntity entity = newEntity();
 		assertEquals(entity.getWidth(), 30);
 	}
 
 	public void testGetHeight() {
 		AbstractEntity entity = newEntity();
 		assertEquals(entity.getHeight(), 40);
 	}
 
 	public void testGetDirection() {
 		AbstractEntity entity = newEntity();
 		assertEquals(entity.getRotation(), new Rotation(0));
 	}
 
 	public void testCollidesWith() {
 		AbstractEntity e1 = getNewAbstractEntity(10, 10, 10, 10,
 				new Rotation(0));
 		AbstractEntity e2 = getNewAbstractEntity(30, 10, 10, 10,
 				new Rotation(0));
 
 		assertFalse(e1.collidesWith(e2));
 		assertFalse(e2.collidesWith(e1));
 
 		e1 = getNewAbstractEntity(10, 10, 10, 10, new Rotation(0));
 		e2 = getNewAbstractEntity(15, 15, 10, 10, new Rotation(0));
 
 		assertTrue(e1.collidesWith(e2));
 		assertTrue(e2.collidesWith(e1));
 
 		e1 = getNewAbstractEntity(20, 20, 10, 10, new Rotation(0));
 		e2 = getNewAbstractEntity(30, 20, 10, 10, new Rotation(0));
 
 		assertTrue(e1.collidesWith(e2));
 		assertTrue(e2.collidesWith(e1));
 
 		e1 = getNewAbstractEntity(20, 20, 10, 10, new Rotation(0));
 		e2 = getNewAbstractEntity(31, 20, 10, 10, new Rotation(0));
 
 		assertFalse(e1.collidesWith(e2));
 		assertFalse(e2.collidesWith(e1));
 	}
 
 	public void testIsOutOfScreen() {
 		final Dimension screen = Constants.getLevelDimension();
 		// Run through a lot of margins
 		for (int marginal = 1000; marginal > -300; --marginal) {
 			/*
 			 * Test outside top
 			 */
 			int width = 20;
 			int height = 10;
 			int x = (int) (screen.getX() / 2);
 			int y = -marginal + height - 1;
 			AbstractEntity e = getNewAbstractEntity(x, y, width, height,
 					new Rotation(0));
 			// Check it's outside
 			assertTrue(e.isOutOfScreen(marginal));
 			e = getNewAbstractEntity(x, y + 1, width, height, new Rotation(0));
 			// Check so it's now inside
 			assertFalse(e.isOutOfScreen(marginal));
 
 			/*
 			 * Test outside bottom
 			 */
 			y = (int) screen.getY() + marginal + 1;
 			e = getNewAbstractEntity(x, y, width, height, new Rotation(0));
 			// Check it's outside
 			assertTrue(e.isOutOfScreen(marginal));
 			e = getNewAbstractEntity(x, y - 1, width, height, new Rotation(0));
 			// Check so it's now inside
 			assertFalse(e.isOutOfScreen(marginal));
 
 			/*
 			 * Test outside left
 			 */
 			x = -marginal + width - 1;
 			y = (int) (screen.getX() / 2);
 			e = getNewAbstractEntity(x, y, width, height, new Rotation(0));
 			// Check it's outside
 			assertTrue(e.isOutOfScreen(marginal));
 			e = getNewAbstractEntity(x + 1, y, width, height, new Rotation(0));
 			// Check so it's now inside
 			assertFalse(e.isOutOfScreen(marginal));
 
 			/*
 			 * Test outside right
 			 */
 			x = (int) screen.getX() + marginal + 1;
 			e = getNewAbstractEntity(x, y, width, height, new Rotation(0));
 			// Check it's outside
 			assertTrue(e.isOutOfScreen(marginal));
 			e = getNewAbstractEntity(x - 1, y, width, height, new Rotation(0));
 			// Check so it's now inside
 			assertFalse(e.isOutOfScreen(marginal));
 		}
 	}
 
 	public abstract void testGetSpeedMod();
 
 	private AbstractEntity newEntity() {
 		return getNewAbstractEntity(10, 20, 30, 40, new Rotation(0));
 	}
 }
