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
 
 package ultraextreme.model.util;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public class PositionTest extends TestCase {
 
 	@Test
 	public void testEqualsObject() {
 		for (double i = -1000; i < 1000; i++) {
 			double x = i;
 			double y = i + 100;
 			Position p = new Position(x, y);
 			Position p2 = new Position(x, y);
 			assertEquals(p, p2);
 		}
 	}
 
 	/**
 	 * Test the default constructor
 	 */
 	@Test
 	public void testPosition() {
 		Position p = new Position();
 		assertEquals(0.0, p.getX());
 		assertEquals(0.0, p.getY());
 
 	}
 
 	/**
 	 * Test the constructor where an x and an y is given
 	 */
 	@Test
 	public void testPositionDoubleDouble() {
 		for (int i = -500; i < 500; i++) {
 			double x = 50 * i;
 			double y = 100 * i - 10;
 			Position p = new Position(x, y);
 			assertEquals(x, p.getX());
 			assertEquals(y, p.getY());
 		}
 	}
 
 	/**
 	 * Test the copy constructor
 	 */
 	@Test
 	public void testPositionPosition() {
 		for (int i = -500; i < 500; i++) {
 			double x = 50 * i;
 			double y = 100 * i - 10;
 			Position p = new Position(x, y);
 			Position p2 = new Position(p);
 			assertEquals(x, p2.getX());
 			assertEquals(y, p2.getY());
 		}
 	}
 
 	@Test
 	public void testSetPosition() {
 		Position p = new Position();
 		for (double i = -10000; i < 10000; i += 100) {
 			double x = i;
 			double y = i + 100;
 			p.setPosition(new Position(x, y));
 			assertEquals(x, p.getX());
 			assertEquals(y, p.getY());
 		}
 	}
 
 	@Test
 	public void testSetX() {
 		Position p = new Position();
 		for (double i = -10000; i < 10000; i += 100) {
 			p.setX(i);
 			assertEquals(i, p.getX());
 		}
 	}
 
 	@Test
 	public void testSetY() {
 		Position p = new Position();
 		for (double i = -10000; i < 10000; i += 100) {
 			p.setY(i);
 			assertEquals(i, p.getY());
 		}
 	}
 
 	/**
 	 * Create a lot of couples of positions and check if the equals methods are
 	 * the same if they have been created with the same coordinates.
 	 */
 	@Test
 	public void testEquals() {
 		for (int i = -1000; i < 1000; i += 10) {
 			// Init positions
 			Position p1 = new Position(i, i);
 			Position p2 = new Position(i, i);
 
 			// They refer to the same objects
 			assertTrue(p1.equals(p1));
 
 			// They have the same coordinates
 			assertTrue(p1.equals(p2));
 
 			// They don't have the same coordinates
 			p2 = new Position(i, i + 1);
 			assertFalse(p1.equals(p2));
 			p2 = new Position(i + 1, i);
 			assertFalse(p1.equals(p2));
 
 			// The other position is null
			assertFalse(p1.equals(null));
 
 			// The other object is of another class
 			assertFalse(p1.equals(new Object()));
 		}
 	}
 
 	/**
 	 * Create a lot of couples of positions and check if the hash codes are the
 	 * same if they have been created with the same coordinates.
 	 */
 	@Test
 	public void testHashcode() {
 		for (int i = -1000; i < 1000; i += 10) {
 			Position p1 = new Position(i, i);
 			Position p2 = new Position(i, i);
 			assertEquals(p1.hashCode(), p2.hashCode());
 		}
 	}
 }
