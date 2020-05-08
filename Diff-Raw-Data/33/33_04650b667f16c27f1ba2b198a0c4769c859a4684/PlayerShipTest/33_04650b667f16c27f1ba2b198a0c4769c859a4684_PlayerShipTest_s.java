 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.model.entity;
 
 import org.junit.Test;
 
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.Position;
 import ultraextreme.model.util.Rotation;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public class PlayerShipTest extends AbstractEntityTest {
 
 	private PlayerShip playerShip;
 
 	@Override
 	protected AbstractEntity getNewAbstractEntity(double x, double y,
 			int width, int height, Rotation direction) {
 		return new PlayerShip(x, y, width, height);
 	}
 
 	private void resetInstanceVariables(double x, double y, int width,
 			int height) {
 		playerShip = new PlayerShip(x, y, width, height);
 	}
 
 	@Override
 	public void testGetDirection() {
 		resetInstanceVariables(0, 0, 0, 0);
 		assertEquals(playerShip.getRotation(), new Rotation(0));
 	}
 
 	/**
 	 * Test if it's possible to move the ship.
 	 */
 	@Override
 	public void testMove() {
 		resetInstanceVariables(10, 20, 30, 40);
 		playerShip.move(0, 0);
 		assertEquals(playerShip.getPositionClone(), new Position(10.0, 20.0));
 
 		playerShip.move(10, 100);
 		assertEquals(playerShip.getPositionClone(), new Position(20.0, 120.0));
 
 		playerShip.move(-100, -1000);
 		assertEquals(playerShip.getPositionClone(), new Position(-80.0, -880.0));
 	}
 
 	@Override
 	public void testGetRotation() {
 		resetInstanceVariables(10, 20, 30, 40);
 		assertEquals(0.0, playerShip.getRotation().getAngle());
 	}
 
 	public void testCanMove() {
 		// Ship is on the left border of the model
		resetInstanceVariables(0, 0, 10, 10);
 		assertFalse(playerShip.canMoveX(-1));
 		assertTrue(playerShip.canMoveX(1));
 
 		// Ship is on the right border of the model
		resetInstanceVariables(Constants.getLevelDimension().getX() - 10, 0,
 				10, 10);
 		assertTrue(playerShip.canMoveX(-1));
 		assertFalse(playerShip.canMoveX(1));
 
 		// Ship is on the top border of the model
		resetInstanceVariables(0, 0, 10, 10);
 		assertFalse(playerShip.canMoveY(-1));
 		assertTrue(playerShip.canMoveY(1));
 
 		// Ship is on the bottom border of the model
		resetInstanceVariables(0, Constants.getLevelDimension().getY() - 10,
 				10, 10);
 		assertTrue(playerShip.canMoveY(-1));
 		assertFalse(playerShip.canMoveY(1));
 	}
 
 	/**
 	 * Test the methods recieveDamage and justGotHit.
 	 */
 	public void testReceiveDamageAndJustGotHit() {
 		resetInstanceVariables(0, 0, 0, 0);
 		assertFalse(playerShip.justGotHit());
 		assertFalse(playerShip.justGotHit());
 		playerShip.receiveDamage(1);
 		assertTrue(playerShip.justGotHit());
 		assertTrue(playerShip.justGotHit());
 		playerShip.move(0, 0);
 		assertFalse(playerShip.justGotHit());
 		assertFalse(playerShip.justGotHit());
 	}
 
 	public void testSetDestroyedAndIsDestroyed() {
 		resetInstanceVariables(0, 0, 0, 0);
 		assertFalse(playerShip.isDestroyed());
 		assertFalse(playerShip.isDestroyed());
 		playerShip.setDestroyed();
 		assertTrue(playerShip.isDestroyed());
 		assertTrue(playerShip.isDestroyed());
 	}
 
 	@Override
 	@Test
 	public void testGetObjectName() {
 		resetInstanceVariables(0, 0, 0, 0);
 		assertEquals(ObjectName.PLAYERSHIP, playerShip.getObjectName());
 	}
 
 	public void testReset() {
 		resetInstanceVariables(0, 0, 0, 0);
 		playerShip.setDestroyed();
 		playerShip.receiveDamage(1);
 		assertTrue(playerShip.isDestroyed());
 		assertTrue(playerShip.justGotHit());
 
 		playerShip.reset();
 		assertFalse(playerShip.isDestroyed());
 		assertFalse(playerShip.justGotHit());
 	}
 }
