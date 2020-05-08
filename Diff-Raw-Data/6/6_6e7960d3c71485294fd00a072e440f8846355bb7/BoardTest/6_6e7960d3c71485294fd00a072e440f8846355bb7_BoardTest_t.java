 package de.htwg.kalaha.model;
 
 import junit.framework.TestCase;
 
 public class BoardTest extends TestCase {
 	
 	Board board;
 	Player player1, player2;
 	
 	public void setUp() {
 		player1 = new Player("Player 1");
 		player2 = new Player("Player 2");
 		board = new Board(player1,player2);
 	}
 	
 	
 	public void testGetHollow() {
 		assertEquals(board.getHollow(player1, 2), board.getNextHollow(board.getHollow(player1, 1)));
 		assertEquals(board.getHollow(player2, 6), board.getNextHollow(board.getHollow(player2, 5)));
 		assertNull(board.getHollow(new Player("Someone else"), 1));
 	}
 	
 	public void testGetOppositeHollow() {
 		assertEquals(board.getHollow(player1, 6), board.getOppositeHollow(board.getHollow(player2, 1)));
 		assertEquals(board.getHollow(player2, 1), board.getOppositeHollow(board.getHollow(player1, 6)));
 		assertNull(board.getOppositeHollow(null));
 	}
 	
 	public void testGetNextHollow() {
 		Hollow newHollow = new Hollow(player1);
 		assertNull(board.getNextHollow(newHollow));
 		assertEquals(board.getNextHollow(board.getHollow(player1, 1)), board.getHollow(player1, 2));
 		assertEquals(board.getNextHollow(board.getHollow(player2, 1)), board.getHollow(player2, 2));
 		assertEquals(board.getNextHollow(board.getHollow(player2, 6)), board.getHollow(player1, 1));
 		assertFalse(board.getNextHollow(board.getHollow(player1, 6)).equals(board.getHollow(player2, 1)));
 		assertEquals(board.getNextHollow(board.getNextHollow(board.getHollow(player1, 6))), board.getHollow(player2, 1));
 		board.switchActivePlayer();
 		assertEquals(board.getNextHollow(board.getHollow(player1, 6)), board.getHollow(player2, 1));
 		assertFalse(board.getNextHollow(board.getHollow(player2, 6)).equals(board.getHollow(player1, 1)));
 		assertEquals(board.getNextHollow(board.getNextHollow(board.getHollow(player2, 6))), board.getHollow(player1, 1));
 	}
 	
 	public void testSwitchActivePlayer() {
 		assertEquals(player1,board.getActivePlayer());
 		board.switchActivePlayer();
 		assertEquals(player2,board.getActivePlayer());
 		board.switchActivePlayer();
 		assertEquals(player1,board.getActivePlayer());
 	}
 	
 	public void testToString(){
 		assertNotNull(board.toString());
		
		board.getNextHollow(board.getHollow(player1, 6)).setMarbles(15);
		board.switchActivePlayer();
		board.getNextHollow(board.getHollow(player2, 6)).setMarbles(15);
		
		assertNotNull(board.toString());
 	}
 
 }
