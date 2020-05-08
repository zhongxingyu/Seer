 package snozama.runtime.tests.mechanics;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import snozama.amazons.mechanics.Board;
 import snozama.amazons.mechanics.MoveManager;
 
 /**
  * Unit tests for snozama.amazons.mechanics.Board.java
  * @author gdouglas
  *
  */
 public class BoardTest {
 
 	/**
 	 * Test that board setup correctly.
 	 */
 	@Test
 	public void testCreateNewBoard()
 	{
 		Board board = new Board();
 		
 		assertTrue(board.isWhite(6, 0));
 		assertTrue(board.isWhite(9, 3));
 		assertTrue(board.isWhite(9, 6));
 		assertTrue(board.isWhite(6, 9));
 		
 		assertTrue(board.isBlack(3, 0));
 		assertTrue(board.isBlack(0, 3));
 		assertTrue(board.isBlack(0, 6));
 		assertTrue(board.isBlack(3, 9));
 		
 		// TODO: Maybe test everything else is empty?
 	}
 	
 	/**
 	 * Test valid move checking function.
 	 */
 	@Test
 	public void testValidMoves()
 	{
 		Board board = new Board();
 		
 		assertTrue(board.isValidMove(1, 1, 2, 2));
 		
 		assertTrue(board.isValidMove(2, 2, 1, 1));
 		
 		assertTrue(board.isValidMove(4, 4, 1, 1));
 		
 		assertTrue(board.isValidMove(2, 5, 5, 5));
 		
 		assertTrue(board.isValidMove(6, 1, 6, 2));
 		
 		assertTrue(board.isValidMove(0, 0, 9, 9));
 		
 		assertTrue(board.isValidMove(9, 0, 0, 9));
 		
 		assertTrue(board.isValidMove(0, 9, 9, 0));
 		
 		assertTrue(board.isValidMove(2, 1, 2, 3));
 		
 		// TODO: Add some tests
 	}
 	
 	/**
 	 * Test invalid moves are invalid.
 	 */
 	@Test
 	public void testInvalidMoves()
 	{
 		Board board = new Board();
 		
 		assertFalse(board.isValidMove(1, 1, 2, 3));
 		
 		assertFalse(board.isValidMove(2, 1, 1, 3));
 		
 		// Add an arrow at (5, 5)
 		assertTrue(board.placeArrow(1, 1, 5, 5));
 		
 		assertFalse(board.isValidMove(1, 1, 5, 5));
 		
 		assertFalse(board.isValidMove(1, 1, 6, 6));
 		
 		assertFalse(board.isValidMove(1, 1, 7, 6));
 		
 		assertFalse(board.isValidMove(2, 2, 10, 2));
 		
 		assertFalse(board.isValidMove(0, 0, 1, 2));
 		
 		// TODO: Test for amazons.
 		assertTrue(board.moveAmazon(9, 3, 1, 3, Board.WHITE));
 		
 		assertFalse(board.moveAmazon(1, 3, 0, 3, Board.WHITE));
 	}
 	
 	/**
 	 * Make sure we get the right number of boards for first move.
 	 */
 	@Test
 	public void testSuccessorsFirstBoard()
 	{
 		Board board = new Board();
 		
 		MoveManager successors = board.getSuccessors(Board.BLACK);
 		
 		assertEquals(successors.size(), 2176);
 	}
 	
 	/**
 	 * Test isTerminal
 	 */
 	@Test
 	public void testIsTerminal()
 	{
 		Board board = new Board();
 		
 		assertFalse(board.isTerminal());
 		
 		//test bottom left corner
 		assertTrue(board.moveAmazon(6, 0, 9, 0, Board.WHITE));
 		assertTrue(board.placeArrow(9, 0, 8, 0));
 		
 		assertFalse(board.isTerminal());
 		
 		//first white piece will be trapped
 		assertTrue(board.moveAmazon(9, 3, 9, 1, Board.WHITE));
 		assertTrue(board.placeArrow(9, 1, 8, 1));
 		
 		assertFalse(board.isTerminal());
 		
 		//second white piece will be trapped
 		assertTrue(board.moveAmazon(9, 6, 9, 2, Board.WHITE));
 		assertTrue(board.placeArrow(9, 2, 8, 2));
 		
 		assertFalse(board.isTerminal());
 		
 		//third white piece will be trapped
 		assertTrue(board.moveAmazon(0, 3, 8, 3, Board.BLACK));
 		assertTrue(board.placeArrow(8, 3, 9, 3));
 		
 		assertFalse(board.isTerminal());
 		
 		//test bottom right corner
 		assertTrue(board.moveAmazon(6, 9, 9, 9, Board.WHITE));
 		assertTrue(board.placeArrow(9, 9, 8, 8));
 		
 		assertFalse(board.isTerminal());
 		
 		//test top left corner
 		assertTrue(board.moveAmazon(3, 0, 0, 0, Board.BLACK));
 		assertTrue(board.placeArrow(0, 0, 1, 1));
 		
 		assertFalse(board.isTerminal());
 		
 		//test top right corner
 		assertTrue(board.moveAmazon(0, 6, 0, 9, Board.BLACK));
 		assertTrue(board.placeArrow(0, 9, 1, 8));
 		
 		assertFalse(board.isTerminal());
 		
 		//white will have no more moves after this
 		assertTrue(board.moveAmazon(3, 9, 8, 9, Board.BLACK));
 		assertTrue(board.placeArrow(8, 9, 9, 8));
 		
 		assertTrue(board.isTerminal());
 	}
 	
 	@Test
 	public void testGenerateSuccessors()
 	{
 		Board board = new Board();
 		MoveManager moves = board.getSuccessors(Board.WHITE);
 		
 		int i = 0;
 		while (i < moves.size())
 		{
 			int colour = Board.WHITE;
 			int amazon = moves.getAmazonIndex(i);
 			byte position = board.amazons[colour][amazon];
 			int row_s = Board.decodeAmazonRow(position);
 			int col_s = Board.decodeAmazonColumn(position);
 			int row_f = moves.getFinishRow(i);
 			int col_f = moves.getFinishColumn(i);
 			int arow = moves.getArrowRow(i);
 			int acol = moves.getArrowColumn(i);
 			
 			System.out.println("-- Iteration: " + i);
 			System.out.println("Starting position: (" + row_s + ", " + col_s + ")");
 			System.out.println("Finish position:   (" + row_f + ", " + col_f + ")");
 			System.out.println("Arrow position:    (" + arow + ", " + acol + ")");
 			
 			assertTrue(board.isValidMove(row_s, col_s, row_f, col_f));
			
			// TODO: Figure out a better way to test this.  Because this doesn't work.
//			assertTrue(board.isValidMove(row_f, col_f, arow, acol));
 			i++;
 		}
 	}
 }
