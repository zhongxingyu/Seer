 package se.chalmers.tda367.group15.game.controllers;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 
 import se.chalmers.tda367.group15.game.controllers.RoomsController.RelativePosition;
import se.chalmers.tda367.group15.game.models.OfficeRoomModel;
 
 public class RoomsControllerTest {
 
 	private class DummyRoom extends RoomController {
 		private String name;
 
 		protected DummyRoom(GameController gameController, String name) {
			super(gameController, new OfficeRoomModel());
 			this.name = name;
 		}
 
 		protected DummyRoom(GameController gameController) {
 			this(gameController, null);
 		}
 
 		@Override
 		public void init(GameContainer container) throws SlickException {
 		}
 
 		@Override
 		public String toString() {
 			return super.toString() + " Name: " + name;
 		}
 	}
 
 	@Test
 	public final void testAddStartingRoom() {
 		RoomsController rc = new RoomsController();
 		RoomController room = new DummyRoom(null);
 		rc.addStartingRoom(room);
 		assertEquals(rc.getCurrentRoom(), room);
 	}
 
 	@Test
 	public final void addRoom() {
 		RoomsController rc = new RoomsController();
 		RoomController start_room = new DummyRoom(null);
 		RoomController room = new DummyRoom(null);
 		rc.addStartingRoom(start_room);
 		rc.addRoom(start_room, room, RelativePosition.ABOVE);
 	}
 
 	@Test(expected = RoomDoesNotExistException.class)
 	public final void addRoomNullReference() {
 		RoomsController rc = new RoomsController();
 		RoomController room = new DummyRoom(null);
 		rc.addRoom(null, room, RelativePosition.ABOVE);
 	}
 
 	/*
 	 * Tests setting 4 rooms around the starting room, and making sure that
 	 * "moving" returns the correct room
 	 */
 	@Test
 	public final void getRoom() {
 		RoomsController rc = new RoomsController();
 		RoomController start_room = new DummyRoom(null);
 		RoomController roomL = new DummyRoom(null, "Left");
 		RoomController roomU = new DummyRoom(null, "Up");
 		RoomController roomR = new DummyRoom(null, "Right");
 		RoomController roomD = new DummyRoom(null, "Down");
 		rc.addStartingRoom(start_room);
 		rc.addRoom(start_room, roomL, RelativePosition.LEFTOF);
 		rc.addRoom(start_room, roomU, RelativePosition.ABOVE);
 		rc.addRoom(start_room, roomR, RelativePosition.RIGHTOF);
 		rc.addRoom(start_room, roomD, RelativePosition.BELOW);
 
 		rc.moveLeft();
 		assertEquals(roomL, rc.getCurrentRoom());
 		rc.moveRight();
 
 		rc.moveUp();
 		assertEquals(roomU, rc.getCurrentRoom());
 		rc.moveDown();
 
 		rc.moveRight();
 		assertEquals(roomR, rc.getCurrentRoom());
 		rc.moveLeft();
 
 		rc.moveDown();
 		assertEquals(roomD, rc.getCurrentRoom());
 	}
 }
