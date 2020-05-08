 /**
  * 
  */
 package test.grid.unit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import exception.InvalidMoveException;
 import exception.OutsideTheGridException;
import game.Colour;
 import game.Game;
import game.Player;
 import game.Race;
 import grid.Coordinate;
 import grid.Direction;
 import grid.Grid;
 import grid.Square;
 import grid.TestCaseOneGridBuilder;
 
 import java.util.Iterator;
 
 import obstacle.LightTrail;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 /**
  * A test class for grids
  * 
  * @author Maxim
  *
  */
 public class GridTest {
 		
 	private static Coordinate negative;
 	
 	private static Coordinate positive;
 	
 	private static Coordinate outside;
 	
 	private static Square start;
 	
 	private Grid grid;
 	
 	private Game game;
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		negative = new Coordinate(-1,-1);
 		positive = new Coordinate(2,4);
 		outside = new Coordinate(4,16);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {		
 		game = new Game(new TestCaseOneGridBuilder(),new Race());	
 		grid = game.getGrid();		
 	}
 	
 	/**
 	 * Test method for {@link grid.Grid#getSquareAtCoordinate()}.
 	 */
 	@Test
 	public void testGetSquareAtCoordinate() {
 		//must throw exception when coordinate is negative
 		Square square = null;
 		try {
 			square = grid.getSquareAtCoordinate(negative);
 			fail("No OutsideTheGridException thrown.");
 		} catch (OutsideTheGridException e) {
 		}
 		
 		//must return the right square when the coordinate is valid
 		try {
 			square = grid.getSquareAtCoordinate(positive);
 		} catch (OutsideTheGridException e1) {
 			fail("No exception should be thrown.");
 		}
 		assertEquals(positive.getX(),square.getCoordinate().getX());
 		assertEquals(positive.getY(),square.getCoordinate().getY());
 
 		//must throw IllegalArgumentException when coordinate is outside the grid
 		try {
 			square = grid.getSquareAtCoordinate(outside);
 			fail("No OutsideTheGridException thrown.");
 		} catch (OutsideTheGridException e) {
 			
 		}
 		
 		
 		
 	}
 	
 	/**
 	 * Test method for {@link grid.Grid#getSquareAtDirection()}.
 	 */
 	@Test
 	public void testGetSquareAtDirection(){
 		//go west from coordinate (2,4)
 		Square newSquare = null;
 		try {
 			Square square1 = grid.getSquareAtCoordinate(positive);
 			newSquare = grid.getSquareAtDirection(square1,Direction.WEST);
 			assertEquals(grid.getSquareAtCoordinate(new Coordinate(1,4)),newSquare);
 		} catch (OutsideTheGridException e2) {
 			fail("No exception should be thrown.");
 
 		}
 		
 		//go south from coordinate (1,4)
 		try {
 			newSquare = grid.getSquareAtDirection(newSquare,Direction.SOUTH);
 			assertEquals(grid.getSquareAtCoordinate(new Coordinate(1,3)),newSquare);
 		} catch (OutsideTheGridException e1) {
 			fail("No exception should be thrown.");
 
 		}
 		
 		//go west from coordinate (1,4)
 		try {
 			newSquare = grid.getSquareAtDirection(newSquare,Direction.WEST);
 			fail("No OutsideTheGridException thrown");
 		} catch (OutsideTheGridException e) {
 		}
 		
 		
 		
 	}
 	
 	/**
 	 * Test method for {@link grid.Grid#hasAsSquare()}.
 	 */
 	@Test
 	public void testHasAsSquare(){
 		try {
 			Square square1 = grid.getSquareAtCoordinate(positive);
 			assertTrue(grid.hasAsSquare(square1));
 		} catch (OutsideTheGridException e) {
 			fail("No exception should be thrown.");
 		}
 		
 	}
 	
 	/**
 	 * Test method for {@link grid.Grid#hasProperSquares()}.
 	 */
 	@Test
 	public void testHasProperSquares(){
 		assertTrue(grid.hasProperSquares());
 	}
 	
 	/**
 	 * Test method for {@link grid.Grid#containsCoordinate()}.
 	 */
 	@Test
 	public void testContainsCoordinate(){
 		assertTrue(grid.containsCoordinate(positive));
 		assertFalse(grid.containsCoordinate(negative));
 		assertFalse(grid.containsCoordinate(outside));
 
 	}
 	
 	/**
 	 * Test method for {@link grid.Grid#crossingObstacle()}.
 	 */
 	@Test
 	public void testCrossingObstacle() throws InvalidMoveException {
 		
 		try {
 			/*
 			 * False if the squares are both covered with different obstacles
 			 */			
 			LightTrail l = new LightTrail();
			Player player = new Player(Colour.RED);
			l.setOwner(player);
 			l.addSquare(grid.getSquareAtCoordinate(new Coordinate(10,4)));
 			l.addSquare(grid.getSquareAtCoordinate(new Coordinate(11,4)));
 			
 			assertFalse(grid.cannotCross(grid.getSquareAtCoordinate(new Coordinate(9,4)), 
 					Direction.NORTHEAST));
 
 			
 			
 			/*
 			 * True if the squares are both covered with the same light trail and they are
 			 * next to each other
 			 */
 			LightTrail l2 = new LightTrail();
			Player player2 = new Player(Colour.BLACK);
			l2.setOwner(player2);
 			l2.addSquare(grid.getSquareAtCoordinate(new Coordinate(11,3)));
 			l2.addSquare(grid.getSquareAtCoordinate(new Coordinate(12,2)));
 						
 			assertTrue(grid.cannotCross(grid.getSquareAtCoordinate(new Coordinate(11,2))
 					, Direction.NORTHEAST));
 
 		} catch (OutsideTheGridException e) {
 			fail("Cannot happen");
 		}
 
 	}
 
 	/**
 	 * Test method for {@link grid.Grid#isValidMove()}.
 	 */
 	@Test
 	public void testIsValidMove() {
 		try {
 			start = grid.getSquareAtCoordinate(new Coordinate(1,1));
 		} catch (OutsideTheGridException e) {
 			fail("Cannot happen");
 		}
 		
 		assertTrue(grid.isValidMove(start,Direction.NORTH));
 		assertTrue(grid.isValidMove(start,Direction.NORTHEAST));
 		assertTrue(grid.isValidMove(start,Direction.EAST));
 		assertFalse(grid.isValidMove(start,Direction.SOUTHEAST));
 		assertFalse(grid.isValidMove(start,Direction.SOUTH));
 		assertFalse(grid.isValidMove(start,Direction.SOUTHWEST));
 		assertFalse(grid.isValidMove(start,Direction.WEST));
 		assertFalse(grid.isValidMove(start,Direction.NORTHWEST));
 		
 		
 	
 	}
 
 	/**
 	 * Test method for {@link grid.Grid#testGetPathDifference()}.
 	 */
 	@Test
 	public void testGetPathDistance() {
 		//first test (1,1) to (5,10)
 		int pathDistance = -1;
 		try {
 			Square fromSquare = grid.getSquareAtCoordinate(new Coordinate(1,1));
 			Square toSquare = grid.getSquareAtCoordinate(new Coordinate(5,10));
 			pathDistance = grid.getPathDistance(fromSquare,toSquare);
 		} catch (OutsideTheGridException e) {
 			fail("Cannot happen.");
 		} catch (InvalidMoveException e) {
 			fail("Cannot happen.");
 		}
 		assertEquals(9,pathDistance);
 		
 		//second test (1,1) to (10,7)
 		pathDistance = -1;
 		try {
 			Square fromSquare = grid.getSquareAtCoordinate(new Coordinate(1,1));
 			Square toSquare = grid.getSquareAtCoordinate(new Coordinate(10,7));
 			pathDistance = grid.getPathDistance(fromSquare,toSquare);
 		} catch (OutsideTheGridException e) {
 			fail("Cannot happen.");
 		} catch (InvalidMoveException e) {
 			fail("Cannot happen.");
 		}
 		assertEquals(11,pathDistance);
 	}
 	
 	
 	/**
 	 * Test method for {@link grid.Grid#iterator()}.
 	 * @throws OutsideTheGridException 
 	 */
 	@Test
 	public void testIterator() throws OutsideTheGridException {
 		Iterator<Square> it = grid.iterator();
 		int width = grid.getDimension().getX();
 		int height = grid.getDimension().getY();
 				
 		for(int row=1;row<=height;row++){
 			for(int column=1;column<=width;column++){
 				Square s = it.next();
 				assertTrue(s == grid.getSquareAtCoordinate(new Coordinate(column,row)));
 				
 
 			}
 		}
 
 	}
 	
 	
 	
 	
 	
 }
