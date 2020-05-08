 package org.p_one.deathmaze;
 
 import junit.framework.TestCase;
 
 import org.p_one.deathmaze.Room;
 
 public class RoomTest extends TestCase {
 	public void testCreate() {
 		Room room = new Room(0, 0, true, true, false, false);
 		assertEquals(room.north, true);
 		assertEquals(room.east, true);
 		assertEquals(room.south, false);
 		assertEquals(room.west, false);
 	}
 
 	public void testRotate() {
 		Room room = new Room(0, 0, true, false, false, true);
 		room.rotate();
 		assertEquals(room.north, true);
 		assertEquals(room.east, true);
 		assertEquals(room.south, false);
 		assertEquals(room.west, false);
 	}
 
 	public void testConnected() throws InvalidRoomConnection {
 		Room room = new Room(0, 0, true, false, false, false);
 		Room north_room = new Room(0, -1, false, false, true, false);
 
 		assertTrue(room.connected(north_room));
 	}
 
 	public void testNotConnected() throws InvalidRoomConnection {
 		Room room = new Room(0, 0, false, false, false, false);
 		Room south_room = new Room(0, 1, false, false, false, false);
 
 		assertFalse(room.connected(south_room));
 	}
 
 	public void testBrokenConnectionRaisesException() {
 		Room room = new Room(0, 0, false, false, false, false);
 		Room north_room = new Room(0, -1, false, false, true, false);
 
 		try {
 			room.connected(north_room);
 			fail();
 		} catch(InvalidRoomConnection e) {
 		}
 	}
 }
