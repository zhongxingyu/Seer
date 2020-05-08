 package test.obstacle;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
 import exception.OutsideTheGridException;
 import game.Game;
 import game.Player;
 import grid.Coordinate;
 import grid.Grid;
 import grid.Square;
 import grid.TestCaseOneGridBuilder;
 import obstacle.LightTrail;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class LightTrailTest {
 
 	LightTrail trail;
 	Square square1,square2,square3,square4;
 	Grid grid;
 	Game game;
 	
 	@Before
 	public void setUp() throws Exception
 	{
 		Game.newInstance();
 		game = Game.getInstance();
 		game.initializeGame(new TestCaseOneGridBuilder());
 		grid = game.getGrid();
 		square1 = grid.getSquareAtCoordinate(new Coordinate(2, 1));
 		trail = new LightTrail(square1);
 		square2 = grid.getSquareAtCoordinate(new Coordinate(3, 1));
 		square3 = grid.getSquareAtCoordinate(new Coordinate(4, 1));
 		square4 = grid.getSquareAtCoordinate(new Coordinate(5, 1));
 		
 		trail.addSquare(square2);
 		trail.addSquare(square3);
 		trail.addSquare(square4);
 
 	}
 	
 	@Test
 	public void testSetPlayer() {
 		Player playa = null;
 		try {
 			playa = grid.getSquareAtCoordinate(new Coordinate(1, 1)).getPlayer();
 		} catch (OutsideTheGridException e) {
			fail("Cannot occur");
 		}
 		trail.setPlayer(playa);
 		assertEquals(trail.getPlayer(), playa);
 	}
 	
 	@Test
 	public void testLowerDuration()
 	{
 		trail.lowerDuration();
 		trail.lowerDuration();
 		trail.lowerDuration();
 		trail.lowerDuration();
 		assertFalse(trail.coversSquare(square1));
 		assertFalse(trail.coversSquare(square2));
 		assertFalse(trail.coversSquare(square3));
 		assertFalse(trail.coversSquare(square4));
 	}
 	
 	@Test
 	public void testRemoveSquare()
 	{
 		assertTrue(trail.getDurationMap().size() == 4);
 		trail.removeSquare();
 		assertTrue(trail.getDurationMap().size() == 3);
 		
 	}
 	
 	@Test
 	public void testUpdateDurationMap()
 	{
 		trail.updateDurationMap(square1, 1);
 		trail.updateDurationMap(square3, 3);
 		assertTrue(trail.getDurationMap().get(square1) == 1);
 		assertTrue(trail.getDurationMap().get(square3) == 3);
 	}
 	
 	
 
 }
