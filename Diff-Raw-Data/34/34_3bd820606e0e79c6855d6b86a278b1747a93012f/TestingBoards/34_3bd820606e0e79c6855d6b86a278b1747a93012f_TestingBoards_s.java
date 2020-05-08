 package boardTests;
 
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import clueGame.BadConfigFormatException;
 import clueGame.Board;
 import clueGame.BoardCell;
 import clueGame.RoomCell;
 
 public class TestingBoards {
 	private static Board board;
 	public static final int NUM_ROOMS= 11;
 	public static final int NUM_COLUMNS= 21;
 	public static final int NUM_ROWS= 22;
 	
 	@BeforeClass
 	public static void setUp(){
 		board= new Board("BoardLayout.csv", "legend.txt");
 		board.loadConfigFiles();
 	}
 
 	@Test
 	public void testRooms() {
 		Map<Character, String> rooms =board.getRooms();
 		assertEquals(NUM_ROOMS, rooms.size());
 		assertEquals("Conservatory", rooms.get('C'));
 		assertEquals("Ballroom", rooms.get('B'));
 		assertEquals("Billiard room", rooms.get('R'));
 		assertEquals("Dining room", rooms.get('D'));
 		assertEquals("Walkway", rooms.get('W'));
 		assertEquals("Kitchen", rooms.get('K'));
 		assertEquals("Library", rooms.get('L'));
 		assertEquals("Study", rooms.get('S'));
 		assertEquals("Lounge", rooms.get('O'));
 		assertEquals("Hall", rooms.get('H'));
 		assertEquals("Closet", rooms.get('X'));
 	}
 	
 	@Test
 	public void testBoardDimensions() {
 		// Ensure we have the proper number of rows and columns
 		assertEquals(NUM_ROWS, board.getNumRows());
 		assertEquals(NUM_COLUMNS, board.getNumColumns());		
 	}
 	
 	// Test a doorway in each direction, plus two cells that are not
 		// a doorway.
 		// These cells are white on the planning spreadsheet
 		@Test
 		public void FourDoorDirections() {
 			// Test one each RIGHT/LEFT/UP/DOWN
 			RoomCell room = board.getRoomCellAt(2, 6);
 			assertTrue(room.isDoorway());
 			assertEquals(RoomCell.DoorDirection.DOWN, room.getDoorDirection());
 			room = board.getRoomCellAt(0, 11);
 			assertTrue(room.isDoorway());
 			assertEquals(RoomCell.DoorDirection.RIGHT, room.getDoorDirection());
 			room = board.getRoomCellAt(20, 17);
 			assertTrue(room.isDoorway());
 			assertEquals(RoomCell.DoorDirection.LEFT, room.getDoorDirection());
 			room = board.getRoomCellAt(7, 5);
 			assertTrue(room.isDoorway());
 			assertEquals(RoomCell.DoorDirection.UP, room.getDoorDirection());
 			// Test that room pieces that aren't doors know it
 			room = board.getRoomCellAt(1, 1);
 			assertFalse(room.isDoorway());	
 			// Test that walkways are not doors
 			BoardCell cell = board.getCellAt(board.calcIndex(7, 7));
 			assertFalse(cell.isDoorway());		
 
 		}
 		
 		// Test that we have the correct number of doors
 		@Test
 		public void testNumberOfDoorways() 
 		{
 			int numDoors = 0;
 			int totalCells = board.getNumColumns() * board.getNumRows();
 			Assert.assertEquals(420, totalCells);
 			for (int i=0; i<totalCells; i++)
 			{
 				BoardCell cell = board.getCellAt(i);
 				if (cell.isDoorway())
 					numDoors++;
 			}
 			Assert.assertEquals(18, numDoors);
 		}
 		@Test
 		public void testCalcIndex() {
 			// Test each corner of the board
 			assertEquals(0, board.calcIndex(0, 0));
 			assertEquals(NUM_COLUMNS-1, board.calcIndex(0, NUM_COLUMNS-1));
			assertEquals(483, board.calcIndex(NUM_ROWS-1, 0));
			assertEquals(505, board.calcIndex(NUM_ROWS-1, NUM_COLUMNS-1));
 			// Test a couple others
 			assertEquals(22, board.calcIndex(1, 1));
 			assertEquals(62, board.calcIndex(2, 20));		
 		}
 		
 		// Test a few room cells to ensure the room initial is
 		// correct.
 		@Test
 		public void testRoomInitials() {
 			assertEquals('R', board.getRoomCellAt(0, 0).getInitial());
 			assertEquals('H', board.getRoomCellAt(4, 8).getInitial());
 			assertEquals('B', board.getRoomCellAt(21, 18).getInitial());
 			assertEquals('O', board.getRoomCellAt(9, 1).getInitial());
 			assertEquals('K', board.getRoomCellAt(21, 11).getInitial());
 		}
 		
 		// Test that an exception is thrown for a bad config file
 		@Test (expected = BadConfigFormatException.class)
 		public void testBadColumns() throws BadConfigFormatException, FileNotFoundException {
 			// overloaded Board ctor takes config file names
 			Board b = new Board("BoardLayoutBadColumns.csv", "legend.txt");
 			// You may change these calls if needed to match your function names
 			// My loadConfigFiles has a try/catch, so I can't call it directly to
 			// see test throwing the BadConfigFormatException
 			b.loadRoomConfig();
 			b.loadBoardConfig();
 		}
 		// Test that an exception is thrown for a bad config file
 		@Test (expected = BadConfigFormatException.class)
 		public void testBadRoom() throws BadConfigFormatException, FileNotFoundException {
 			// overloaded Board ctor takes config file name
 			Board b = new Board("BoardLayoutBadRoom.csv", "legend.txt");
 			b.loadRoomConfig();
 			b.loadBoardConfig();
 		}
 		// Test that an exception is thrown for a bad config file
 		@Test (expected = BadConfigFormatException.class)
 		public void testBadRoomFormat() throws BadConfigFormatException, FileNotFoundException {
 			// overloaded Board ctor takes config file name
 			Board b = new Board("BoardLayout.csv", "LegendBadFormat.txt");
 			b.loadRoomConfig();
 			b.loadBoardConfig();
 		}
 	
 
 }
