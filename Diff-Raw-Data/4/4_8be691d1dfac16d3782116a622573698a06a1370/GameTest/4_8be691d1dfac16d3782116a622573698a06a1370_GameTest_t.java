 package game.test;
 
 import static org.junit.Assert.*;
 import item.LightGrenade;
 import exception.InvalidMoveException;
 import exception.NoItemException;
 import exception.OutsideTheGridException;
 import exception.OverCapacityException;
 import game.Game;
 import game.PlayerColour;
 import grid.Coordinate;
 import grid.Direction;
 import grid.Grid;
 
 import org.junit.Test;
 
 public class GameTest {
 
 	@Test
 	public void testGame() {
 		Game game1 = new Game(15, 20);
 		Game game2 = new Game(8,8);
 		assertTrue(game1.getPlayers().length == 2);
 		assertFalse(game2.getPlayers().length == 2);
 		assertFalse(game1.getGrid() == null);
 		assertTrue(game2.getGrid() == null);
 	}
 
 	@Test
 	public void testSetGetGrid() {
 		Grid grid = new Grid(20, 15);
 		Game game = new Game(20,15);
 		game.setGrid(grid);
 		assertTrue(game.getGrid().equals(grid));
 	}
 
 	@Test
 	public void testGetCurrentPlayer() throws OutsideTheGridException {
 		Game game = new Game(15,20);
 		assertEquals(game.getCurrentPlayer().getPlayerColour(),PlayerColour.RED);
 	}
 
 	@Test
 	public void testGetWinner() {
 		Game game = new Game(15,20);
 		game.endTurn();
 		assertEquals(game.getWinner().getPlayerColour(), PlayerColour.BLUE);
 	}
 
 	@Test
 	public void testGetNbPlayers() {
 		Game game = new Game(15,20);
 		assertEquals(game.getNbPlayers(), 2);
 	}
 
 	@Test
 	public void testIsGameEnded() {
 		Game game = new Game(15,20);
 		assertFalse(game.isGameEnded());
 		game.endTurn();
 		assertTrue(game.isGameEnded());
 	}
 
 	@Test
 	public void testSetGameEnded() {
 		Game game = new Game(15,20);
 		game.setGameEnded(false);
 		assertFalse(game.isGameEnded());
 		game.setGameEnded(true);
 		assertTrue(game.isGameEnded());
 	}
 
 	@Test (expected = InvalidMoveException.class)
 	public void testMove() throws InvalidMoveException, OutsideTheGridException {
 		Game game = new Game(15, 20);
 		game.move(Direction.EAST);
 		assertEquals(game.getCurrentPlayer().getLocation(), game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)));
 		game.move(Direction.SOUTH);
 		assertEquals(game.getCurrentPlayer().getLocation(), game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)));
 	}
 
 	@Test
 	public void testPickUpItem() throws OutsideTheGridException, NoItemException, OverCapacityException {
 		Game game = new Game(15,20);
 		game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)).addItem(new LightGrenade());
		game.getCurrentPlayer().changeLocation(game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)));
 		game.pickUpItem(game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)), game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)).getLightGrenade());
 		assertTrue(game.getCurrentPlayer().getInventory().size() == 1);
 	}
 
 	@Test (expected = NoItemException.class)
 	public void testUseItem() throws NoItemException, OverCapacityException, OutsideTheGridException {
 		Game game = new Game(15,20);
 		game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)).addItem(new LightGrenade());
 		game.pickUpItem(game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)), game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1)).getLightGrenade());
 		game.useItem(game.getCurrentPlayer().getInventory().get(0));
 		assertTrue(game.getCurrentPlayer().getInventory().size() == 0);
 	}
 
 	@Test
 	public void testCheckTurn() {
 		Game game = new Game(15,20);
 		game.checkTurn();
 		assertTrue(game.getCurrentPlayer().getPlayerColour().equals(PlayerColour.RED));
 	}
 
 }
