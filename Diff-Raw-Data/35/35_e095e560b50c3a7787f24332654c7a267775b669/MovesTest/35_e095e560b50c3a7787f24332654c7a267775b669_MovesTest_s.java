 package com.github.bfallstrom.ominoslide.junit;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.github.bfallstrom.ominoslide.areastructure.Board;
 import com.github.bfallstrom.ominoslide.areastructure.Direction;
 import com.github.bfallstrom.ominoslide.areastructure.Omino;
 import com.github.bfallstrom.ominoslide.areastructure.Tile;
 import com.github.bfallstrom.ominoslide.areastructure.WinningPosition;
 import com.github.bfallstrom.ominoslide.solver.Move;
 import com.github.bfallstrom.ominoslide.solver.MoveStatus;
 import com.github.bfallstrom.ominoslide.solver.Moves;
 
 public class MovesTest {
 	public static List<Tile> boardLayout = new ArrayList<Tile>();
 	public static List<Tile> ominoStyle1 = new ArrayList<Tile>();
 	public static List<Tile> ominoStyle2 = new ArrayList<Tile>();
 	public static Board masterBoard;
 	public static Omino omino1;
 	public static Omino omino2;
 	public static Omino omino3;
 	public static WinningPosition solved;
 	
 	public Board boardUnderTest;
 	public Moves movesUnderTest;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		boardLayout.add(new Tile(0,0));
 		boardLayout.add(new Tile(1,0));
 		boardLayout.add(new Tile(2,0));
 		boardLayout.add(new Tile(0,1));
 		boardLayout.add(new Tile(1,1));
 		boardLayout.add(new Tile(2,1));
 		boardLayout.add(new Tile(3,1));
 		ominoStyle1.add(new Tile(0,0));
 		ominoStyle1.add(new Tile(1,0));
 		ominoStyle2.add(new Tile(0,0));
 		masterBoard = new Board(boardLayout);
 		omino1 = new Omino(ominoStyle1);
 		omino2 = new Omino(ominoStyle1);
 		omino3 = new Omino(ominoStyle2);
 		assertTrue("ERROR: Master board initialization failed!!", masterBoard.placeOmino(omino1, new Tile(1,0)));
 		assertTrue("ERROR: Master board initialization failed!!", masterBoard.placeOmino(omino2, new Tile(1,1)));
 		assertTrue("ERROR: Master board initialization failed!!", masterBoard.placeOmino(omino3, new Tile(3,1)));
 		solved = new WinningPosition(boardLayout);
 		assertTrue("ERROR: Solved position initialization failed!!", solved.placeOmino(omino3, new Tile(2,0)));
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		boardUnderTest = new Board(masterBoard);
 		movesUnderTest = new Moves(boardUnderTest);
 	}
 
 	@Test
 	public void testGetNumberOfMoves() {
 		assertTrue("Incorrect number of moves generated: " + movesUnderTest.getNumberOfMoves(),
 				movesUnderTest.getNumberOfMoves() == 12);
 	}
 
 	@Test
 	public void testGetMove() {
 		Move testMove = movesUnderTest.getMove(0);
 		assertNotNull("Error: Move failed to generate properly!", testMove);
 		assertTrue("Error: Omino index unexpectedly " + testMove.getOminoIndex(), testMove.getOminoIndex() == 0);
 		assertTrue("Error: Move direction unexpectedly " + testMove.getMoveDirection(), testMove.getMoveDirection() == Direction.UP);
 	}
 
 	@Test
 	public void testGetStatusFromMove() {
 		MoveStatus testStatus = movesUnderTest.getStatusFromMove(0);
 		assertTrue("Error: Move status is unexpectedly " + testStatus, testStatus == MoveStatus.UNKNOWN);
 	}
 
 	@Test
 	public void testHasUnblocked() {
 		assertTrue("Error: Erroneously reporting no unblocked moves!", movesUnderTest.hasUnblocked());
 	}
 
 	@Test
 	public void testResolveMoves() {
 		movesUnderTest.resolveMoves(solved);
 		MoveStatus testStatus = movesUnderTest.getStatusFromMove(0);
 		assertTrue("Error: Move status is unexpectedly " + testStatus, testStatus == MoveStatus.BLOCKED);
 		assertNull("Error: Board from a blocked move did not get dropped!", movesUnderTest.getBoardFromMove(0));
		for(int i = 0; i < movesUnderTest.getNumberOfMoves(); i++)
		{
			System.out.println("Status of move " + i + ": " + movesUnderTest.getStatusFromMove(i)
					+ " Omino index = " + movesUnderTest.getMove(i).getOminoIndex()
					+ " Direction = " + movesUnderTest.getMove(i).getMoveDirection());
		}
 	}
 
 	@Test
 	public void testTrimBlocked() {
		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testHasWinner() {
 		fail("Not yet implemented");
 	}
 }
