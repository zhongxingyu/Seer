 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 
 import org.junit.Test;
 
 /**
  * @author Steve Pennington and Julie Sparrow
  *
  */
 public class ScoreTest {
 	
 	static final int NUM_COLS = 10;
 	static final int NUM_ROWS = 8;
 	
 	/**
 	 * Test method for {@link WinChecker#score(byte, byte, byte, byte)}.
 	 */
 	@Test
 	public void scoreTest() {
 		assertEquals(5, WinChecker.score(Board.RED, Board.RED, Board.GREEN, Board.GREEN));
 		assertEquals(5, WinChecker.score(Board.GREEN, Board.GREEN, Board.RED, Board.RED));
 		
 		assertEquals(-5, WinChecker.score(Board.BLUE, Board.BLUE, Board.GREEN, Board.GREEN));
 		assertEquals(-5, WinChecker.score(Board.GREEN, Board.GREEN, Board.BLUE, Board.BLUE));
 		
 		assertEquals(4, WinChecker.score(Board.RED, Board.GREEN, Board.GREEN, Board.RED));
 		assertEquals(4, WinChecker.score(Board.GREEN, Board.RED, Board.RED, Board.GREEN));
 		
 		assertEquals(-4, WinChecker.score(Board.BLUE, Board.GREEN, Board.GREEN, Board.BLUE));
 		assertEquals(-4, WinChecker.score(Board.GREEN, Board.BLUE, Board.BLUE, Board.GREEN));
 		
 		assertEquals(3, WinChecker.score(Board.RED, Board.GREEN, Board.RED, Board.GREEN));
 		assertEquals(3, WinChecker.score(Board.GREEN, Board.RED, Board.GREEN, Board.RED));
 		
 		assertEquals(-3, WinChecker.score(Board.BLUE, Board.GREEN, Board.BLUE, Board.GREEN));
 		assertEquals(-3, WinChecker.score(Board.GREEN, Board.BLUE, Board.GREEN, Board.BLUE));
 	}
 	
 	/**
 	 * Test method for {@link WinChecker#isWin(Board, int)}.
 	 */
 	@Test
 	public void testVerticalIsWin() {
 		Board board = generateBoard("b,g,g,b,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s");
 		assertEquals(-4, WinChecker.isWin(board, 0));
		board = generateBoard("s,s,s,s,b,b,g,g,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s");
 		assertEquals(-5, WinChecker.isWin(board, 0));
 	}
 	
 	/**
 	 * Test method for {@link WinChecker#isWin(Board, int)}.
 	 */
 	@Test
 	public void testHorizontalIsWin() {
 		Board board = generateBoard("b,s,s,s,s,s,s,s,b,s,s,s,s,s,s,s,g,s,s,s,s,s,s,s,g,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s");
 		assertEquals(-5, WinChecker.isWin(board, 0));
 		assertEquals(-5, WinChecker.isWin(board, 1));
 		assertEquals(-5, WinChecker.isWin(board, 2));
 		assertEquals(-5, WinChecker.isWin(board, 3));
 		
 		board = generateBoard("s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,g,s,s,s,s,s,s,s,g,s,s,s,s,s,s,s,b,s,s,s,s,s,s,s,b,s,s,s,s,s,s,s");
 		assertEquals(-5, WinChecker.isWin(board, 6));
 		assertEquals(-5, WinChecker.isWin(board, 7));
 		assertEquals(-5, WinChecker.isWin(board, 8));
 		assertEquals(-5, WinChecker.isWin(board, 9));
 	}
 	
 	/**
 	 * Test method for {@link WinChecker#isWin(Board, int)}.
 	 */
 	@Test
 	public void testDiagonalIsWin() {
 		Board board = generateBoard("b,s,s,s,s,s,s,s,s,b,s,s,s,s,s,s,s,s,g,s,s,s,s,s,s,s,s,g,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s");
 		assertEquals(-5, WinChecker.isWin(board, 0));
		board = generateBoard("s,s,s,b,s,s,s,s,s,s,b,s,s,s,s,s,s,g,s,s,s,s,s,s,g,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s");
		assertEquals(-5, WinChecker.isWin(board, 0));
 		board = generateBoard("s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,r,r,r,r,g,s,s,s,r,r,r,r,r,g,s,s,r,r,r,r,r,r,b,s,r,r,r,r,r,r,r,b");
 		assertEquals(-5, WinChecker.isWin(board, 6));
 		assertEquals(-5, WinChecker.isWin(board, 7));
 		assertEquals(-5, WinChecker.isWin(board, 8));
 		assertEquals(-5, WinChecker.isWin(board, 9));
 	}
 	
 	/**
 	 * Test method for {@link WinChecker#isWin(Board, int)}.
 	 */
 	@Test
 	public void testMultipleWinnerIsWin() {
 		Board board = generateBoard("b,b,g,g,s,s,s,s,r,r,r,r,s,s,s,s,r,r,r,g,s,s,s,s,r,r,r,r,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s");
 		assertEquals(-5, WinChecker.isWin(board, 0));
 		board = generateBoard("b,b,g,g,s,s,s,s,r,r,r,r,s,s,s,s,r,g,g,g,s,s,s,s,r,r,r,r,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s,s");
 		assertEquals(3, WinChecker.isWin(board, 0));
 	}
 	
 	/**
 	 * Test method for {@link WinChecker#isWin(Board, int)}.
 	 */
 	@Test
 	public void testNoWinners() {
 		Board board = generateBoard("b,b,b,b,b,b,b,b,r,r,r,r,b,b,b,b,r,r,r,b,b,b,b,b,r,r,r,r,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b");
 		assertEquals(0, WinChecker.isWin(board, 0));
 	}
 	
 	private Board generateBoard(String config) {
 		IO.init(new ByteArrayInputStream(config.getBytes()));
 		Board board = IO.parseBoard(NUM_COLS, NUM_ROWS);
 		IO.close();
 		return board;
 	}
 }
