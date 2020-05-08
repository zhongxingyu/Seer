 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * Class for testing the Engine, using Non-GUI methods.
  * 
  * @author TeamSorryDragons
  * 
  */
 public class EngineNonGUITest {
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void test() {
 		assertNotNull(new Engine(new BoardList(), "english"));
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void testMoveOnePieceOnce() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 		try {
 			e.move(1, e.pieces[0], e.board.getStartPointers()[0]);
 		} catch (Exception exception) {
 		}
 
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsnr|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void testMoveOnePieceToEnd() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 
 		e.newGame();
 
 		Piece pawn = e.pieces[5]; // that's a blue piece
 		assertEquals(pawn.col, Piece.COLOR.blue);
 
 		movePawn(1, pawn, e);
 
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnb|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 		movePawn(5, pawn, e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsnb|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 		movePawn(8, pawn, e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysnb|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 		movePawn(25, pawn, e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsnb|gsn|nn|nn|");
 
 		movePawn(19, pawn, e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsnb|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 		movePawn(1, pawn, e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsnb|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 		movePawn(2, pawn, e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsfb|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 		movePawn(4, pawn, e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn1|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void toStartTest() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		movePawn(1, e.pieces[7], e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnb|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		movePawn(1, e.pieces[1], e);
 		movePawn(15, e.pieces[1], e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnr|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		movePawn(1, e.pieces[9], e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnr|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysny|ymn3"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		movePawn(15, e.pieces[1], e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysnr|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		movePawn(1, e.pieces[13], e);
 
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysnr|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsng|gmn3|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		movePawn(15, e.pieces[1], e);
 
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsnr|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		movePawn(1, e.pieces[12], e);
 
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsng|gmn3|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 	}
 
 	@Test
 	public void testSlideMoves() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		movePawn(1, e.pieces[0], e);
 		movePawn(12, e.pieces[0], e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnr|bmn4|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void testCoordinateToNodeCorners() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 
 		// assertEquals(e.convertCoordToNode(new SorryFrame.Coordinate(0, 0)),
 		// board.getCornerPointers()[2]);
 
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void testCoordinateToIntCorners() {
 		assertEquals(Engine.getNodePosition(new SorryFrame.Coordinate(15, 15),0),
 				0);
 		assertEquals(Engine.getNodePosition(new SorryFrame.Coordinate(0, 15),0),
 				22);
 		assertEquals(Engine.getNodePosition(new SorryFrame.Coordinate(0, 0),0),
 				44);
 		assertEquals(Engine.getNodePosition(new SorryFrame.Coordinate(15, 0),0),
 				66);
 		assertEquals(Engine.getNodePosition(new SorryFrame.Coordinate(15, 0),1),
				0);
 		assertEquals(Engine.getNodePosition(new SorryFrame.Coordinate(15, 0),2),
 				22);
 		assertEquals(Engine.getNodePosition(new SorryFrame.Coordinate(15, 0),3),
				44);
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void testCoordinateToIntStarts() {
 		for (int i = 10; i <= 12; i++) {
 			for (int j = 12; j <= 14; j++)
 				assertEquals(
 						Engine.getNodePosition(new SorryFrame.Coordinate(i, j),0),
 						11);
 		}
 
 		for (int i = 3; i <= 5; i++) {
 			for (int j = 14; j >= 12; j--)
 				assertEquals(
 						Engine.getNodePosition(new SorryFrame.Coordinate(j, i),0),
 						77);
 		}
 
 		for (int i = 3; i <= 5; i++) {
 			for (int j = 1; j <= 3; j++) {
 				assertEquals(
 						Engine.getNodePosition(new SorryFrame.Coordinate(i, j),0),
 						55);
 			}
 		}
 
 		for (int i = 1; i <= 3; i++) {
 			for (int j = 12; j >= 10; j--) {
 				assertEquals(
 						Engine.getNodePosition(new SorryFrame.Coordinate(i, j),0),
 						33);
 			}
 		}
 
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void testCoordinateToIntHomeZones() {
 		for (int i = 14; i >= 12; i--) {
 			for (int j = 7; j <= 9; j++)
 				assertEquals(
 						Engine.getNodePosition(new SorryFrame.Coordinate(i, j),0),
 						8);
 		}
 
 		for (int i = 9; i >= 7; i--) {
 			for (int j = 1; j <= 3; j++)
 				assertEquals(
 						Engine.getNodePosition(new SorryFrame.Coordinate(i, j),0),
 						74);
 		}
 
 		for (int i = 1; i <= 3; i++)
 			for (int j = 6; j <= 8; j++)
 				assertEquals(
 						Engine.getNodePosition(new SorryFrame.Coordinate(i, j),0),
 						52);
 
 		for (int i = 6; i <= 8; i++)
 			for (int j = 14; j >= 12; j--)
 				assertEquals(
 						Engine.getNodePosition(new SorryFrame.Coordinate(i, j),0),
 						30);
 
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void testCoordinateToIntSafeZones() {
 		int green = 68;
 		for (int i = 15; i >= 10; i--)
 			assertEquals(
 					Engine.getNodePosition(new SorryFrame.Coordinate(i, 2),0),
 					green++);
 
 		int red = 2;
 		for (int i = 15; i >= 10; i--)
 			assertEquals(
 					Engine.getNodePosition(new SorryFrame.Coordinate(13, i),0),
 					red++);
 
 		int blue = 24;
 		for (int i = 0; i <= 5; i++)
 			assertEquals(
 					Engine.getNodePosition(new SorryFrame.Coordinate(i, 13),0),
 					blue++);
 
 		int yellow = 46;
 		for (int i = 0; i <= 5; i++)
 			assertEquals(
 					Engine.getNodePosition(new SorryFrame.Coordinate(2, i),0),
 					yellow++);
 
 	}
 
 	@SuppressWarnings("javadoc")
 	@Test
 	public void testCoordinateToIntSideLines() {
 		checkCoordinateInt(15, 15, 0);
 		checkCoordinateInt(14, 15, 1);
 		checkCoordinateInt(13, 15, 2);
 		checkCoordinateInt(12, 15, 9);
 		checkCoordinateInt(11, 15, 10);
 		checkCoordinateInt(10, 15, 12);
 
 		int red = 13;
 		for (int i = 9; i >= 0; i--)
 			checkCoordinateInt(i, 15, red++);
 
 		checkCoordinateInt(0, 15, 22);
 		checkCoordinateInt(0, 14, 23);
 		checkCoordinateInt(0, 13, 24);
 		checkCoordinateInt(0, 12, 31);
 		checkCoordinateInt(0, 11, 32);
 		checkCoordinateInt(0, 10, 34);
 
 		int blue = 35;
 		for (int i = 9; i >= 0; i--)
 			checkCoordinateInt(0, i, blue++);
 
 		checkCoordinateInt(0, 0, 44);
 		checkCoordinateInt(1, 0, 45);
 		checkCoordinateInt(2, 0, 46);
 		checkCoordinateInt(3, 0, 53);
 		checkCoordinateInt(4, 0, 54);
 
 		int yellow = 56;
 		for (int i = 5; i <= 15; i++)
 			checkCoordinateInt(i, 0, yellow++);
 
 		checkCoordinateInt(15, 0, 66);
 		checkCoordinateInt(15, 1, 67);
 		checkCoordinateInt(15, 2, 68);
 		checkCoordinateInt(15, 3, 75);
 		checkCoordinateInt(15, 4, 76);
 
 		int green = 78;
 		for (int i = 5; i < 15; i++)
 			checkCoordinateInt(15, i, green++);
 
 	}
 
 	private void checkCoordinateInt(int x, int y, int pos) {
 		assertEquals(Engine.getNodePosition(new SorryFrame.Coordinate(x, y),0),
 				pos);
 	}
 
 	private void movePawn(int num, Piece p, Engine e) {
 		try {
 			e.move(num, p, e.findNode(p));
 		} catch (Exception exception) {
 		}
 
 	}
 
 	@Test
 	public void testFindNodeWithPiece() {
 		BoardList temp = new BoardList();
 		Engine e = new Engine(temp, "english");
 		e.newGame();
 		Piece p = temp.getStartPointers()[0].getPieces()[0];
 		assertEquals(e.findNode(p), temp.getStartPointers()[0]);
 		temp.getCornerPointers()[0].addPieceToPieces(p);
 		try {
 			temp.getCornerPointers()[0].findNodeWithPiece(new Piece());
 		} catch (Exception ex) {
 			assertTrue(true);
 		}
 	}
 
 	@Test
 	public void testFindNodeWithPosition() {
 		BoardList temp = new BoardList();
 		Engine e = new Engine(temp, "english");
 		e.newGame();
 		assertEquals(e.findNodeByPosition(87),
 				temp.getCornerPointers()[0].getPrevious());
 		assertEquals(e.findNodeByPosition(11), temp.getStartPointers()[0]);
 		assertEquals(e.findNodeByPosition(8), temp.getHomePointers()[0]);
 	}
 
 	@Test
 	public void testIsValidMoveNoMove() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		e.getNextCard();
 		assertTrue(e.isValidMove(e.pieces[0], e.currentCard.cardNum,
 				new Player(Piece.COLOR.red, "Bob Dole")));
 	}
 
 	@Test
 	public void testPanwMovement() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.activePlayer = new Player(Piece.COLOR.red, "Phil");
 		e.getNextCard();
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(0, 0),
 				new SorryFrame.Coordinate(0, 0)), 0);
 	}
 
 	@Test
 	public void testCountTo() {
 		BoardList board = new BoardList();
 		new Engine(board, "english");
 		int count = board.cornerPointers[0].countTo(board.cornerPointers[1]);
 		assertEquals(count, 15);
 	}
 
 	@Test
 	public void testIsValidPlayerCheck() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		Player p = new Player(Piece.COLOR.red, "James Bond");
 
 		e.newGame();
 		e.getNextCard();
 
 		assertTrue(e.isValidMove(e.pieces[0], e.currentCard.cardNum, p));
 
 		assertFalse(e.isValidMove(e.pieces[0], e.currentCard.cardNum,
 				new Player(Piece.COLOR.blue, "Steve Jobs")));
 
 		for (int i = 1; i < 4; i++)
 			assertTrue(e.isValidMove(e.pieces[i], e.currentCard.cardNum, p));
 
 		assertFalse(e.isValidMove(e.pieces[4], e.currentCard.cardNum, p));
 		
 		assertFalse(e.isValidMove(null, e.currentCard.cardNum, p));
 	}
 
 	@Test
 	public void testPawnMoveSamePositions() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		e.activePlayer = new Player(Piece.COLOR.red, "Phil");
 
 		assertEquals(Engine.SAME_NODE_SELECTED, e.pawnMove(
 				new SorryFrame.Coordinate(0, 0),
 				new SorryFrame.Coordinate(0, 0)));
 
 		for (int i = 0; i < 16; i++) {
 			assertEquals(Engine.SAME_NODE_SELECTED, e.pawnMove(
 					new SorryFrame.Coordinate(i, 0), new SorryFrame.Coordinate(
 							i, 0)));
 			assertEquals(0, e.pawnMove(new SorryFrame.Coordinate(0, i),
 					new SorryFrame.Coordinate(0, i)));
 		}
 	}
 
 	@Test
 	public void testPawnMoveInvalidCoordinates() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.activePlayer = new Player(Piece.COLOR.red, "Phil");
 		e.newGame();
 
 		assertEquals(Engine.NODE_NOT_FOUND, e.pawnMove(
 				new SorryFrame.Coordinate(-1, -1), new SorryFrame.Coordinate(0,
 						0)));
 
 		assertEquals(Engine.NODE_NOT_FOUND, e.pawnMove(
 				new SorryFrame.Coordinate(0, 0), new SorryFrame.Coordinate(-1,
 						-1)));
 
 		assertEquals(Engine.NODE_NOT_FOUND, e.pawnMove(
 				new SorryFrame.Coordinate(1, 1),
 				new SorryFrame.Coordinate(0, 0)));
 
 		assertEquals(Engine.NODE_NOT_FOUND, e.pawnMove(
 				new SorryFrame.Coordinate(6, 6), new SorryFrame.Coordinate(-1,
 						-1)));
 
 		assertEquals(Engine.NODE_NOT_FOUND, e.pawnMove(
 				new SorryFrame.Coordinate(7, 7),
 				new SorryFrame.Coordinate(7, 7)));
 
 	}
 
 	@Test
 	public void testInsertPlayers() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 
 		Player john = new Player(Piece.COLOR.green, "Johnny Depp");
 		Player bill = new Player(Piece.COLOR.blue, "Bill Gates");
 		Player siriam = new Player(Piece.COLOR.yellow, "Whale Rider");
 		Player buffalo = new Player(Piece.COLOR.red, "Buffalo");
 
 		e.insertPlayer(john);
 		e.insertPlayer(bill);
 
 		e.rotatePlayers();
 
 		assertEquals(e.activePlayer, john);
 		e.rotatePlayers();
 		assertEquals(e.activePlayer, bill);
 		e.rotatePlayers();
 		assertEquals(e.activePlayer, john);
 
 		e.insertPlayer(siriam);
 		assertEquals(e.activePlayer, john);
 
 		e.rotatePlayers();
 		assertEquals(e.activePlayer, bill);
 
 		e.rotatePlayers();
 		assertEquals(e.activePlayer, john);
 
 		e.rotatePlayers();
 		assertEquals(e.activePlayer, siriam);
 
 		e.insertPlayer(buffalo);
 		assertEquals(e.activePlayer, siriam);
 	}
 
 	@Test
 	public void testBackwardsMove() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		movePawn(1, e.pieces[7], e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnb|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		movePawn(-4, e.pieces[7], e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nnb|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		movePawn(-1, e.pieces[7], e);
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nnb|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		try {
 			movePawn(-1, e.pieces[1], e);
 		} catch (Exception ex) {
 		}
 	}
 
 	@Test
 	public void testHasWon() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		Player john = new Player(Piece.COLOR.green, "Johnny Depp");
 		Player bill = new Player(Piece.COLOR.blue, "Bill Gates");
 		Player siriam = new Player(Piece.COLOR.yellow, "Whale Rider");
 		Player buffalo = new Player(Piece.COLOR.red, "Buffalo");
 		e.insertPlayer(buffalo);
 		e.insertPlayer(bill);
 		e.insertPlayer(siriam);
 		e.insertPlayer(john);
 		Piece[] temp = new Piece[4];
 		for (int i = 0; i < 4; i++) {
 			e.rotatePlayers();
 			temp = board.getStartPointers()[i].getPieces();
 			board.getHomePointers()[i].setPieces(temp);
 			assertTrue(e.hasWon());
 		}
 		e.rotatePlayers();
 		temp = new Piece[4];
 		board.getHomePointers()[0].setPieces(temp);
 		assertFalse(e.hasWon());
 	}
 
 	@Test
 	public void testValidMoves() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 
 		Node start = new Node();
 		Node end = new Node();
 		Piece pawn = new Piece(Piece.COLOR.red);
 		start.addPieceToPieces(pawn);
 
 		createNodeChain(start, end, 2);
 
 		assertEquals(start.countTo(end), 1);
 
 		e.currentCard = new Card(1, "Test CARD");
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 1, 0), 1);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 2, 0),
 				Engine.INVALID_MOVE);
 
 		start = new Node();
 		end = new Node();
 		end.addPieceToPieces(pawn);
 		createNodeChain(start, end, 3);
 		assertEquals(2, start.countTo(end));
 		assertEquals(2, end.countBack(start));
 		e.board = new BoardList();
 		e.currentCard = new Card(2, "Test");
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 2, 0), 2);
 
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 1, 0),
 				Engine.INVALID_MOVE);
 
 		start = new Node();
 		end = new Node();
 		end.addPieceToPieces(pawn);
 		createNodeChain(start, end, 5);
 		assertEquals(4, start.countTo(end));
 		assertEquals(4, end.countBack(start));
 
 		e.currentCard = new Card(4, "Test CARD");
 		assertEquals(e.checkValidityOriginalRules(pawn, end, start, 0, 4), -4);
 		assertEquals(e.checkValidityOriginalRules(pawn, end, start, 0, -5),
 				Engine.INVALID_MOVE);
 
 		start = new Node();
 		end = new Node();
 		start.addPieceToPieces(pawn);
 		e.currentCard = new Card(7, "TEST");
 		createNodeChain(start, end, 8);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 7, 0), 7);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 8, 0),
 				Engine.INVALID_MOVE);
 
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 4, 0), Engine.VALID_MOVE_NO_FINALIZE);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 7, 0),
 				Engine.INVALID_MOVE);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 3, 0), 3);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 7, 0), 7);
 
 		start = new Node();
 		end = new Node();
 		start.addPieceToPieces(pawn);
 		e.currentCard = new Card(10, "TEST");
 		createNodeChain(start, end, 11);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 10, 0), 10);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 8, 0),
 				Engine.INVALID_MOVE);
 
 		start = new Node();
 		end = new Node();
 		createNodeChain(start, end, 11);
 		end.addPieceToPieces(pawn);
 		assertEquals(e.checkValidityOriginalRules(pawn, end, start, 0, 1), -1);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 0, -2),
 				Engine.INVALID_MOVE);
 
 		start = new Node();
 		end = new Node();
 		createNodeChain(start, end, 12);
 		e.currentCard = new Card(11, "TEST");
 		start.addPieceToPieces(pawn);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 11, 0), 11);
 		end.removePieceFromPieces(pawn);
 		start.removePieceFromPieces(pawn);
 		start.addPieceToPieces(pawn);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 0, -2),
 				Engine.INVALID_MOVE);
 
 		start = new Node();
 		end = new Node();
 		start.addPieceToPieces(pawn);
 		end.addPieceToPieces(new Piece(Piece.COLOR.blue));
 		createNodeChain(start, end, 4);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 3, 0), 3);
 
 		start = new Node();
 		end = new Node();
 		createNodeChain(start, end, 13);
 		e.currentCard = new Card(13, "TEST");
 		start.getPrevious().setColor(Piece.COLOR.red);
 		start.getPrevious().addPieceToPieces(pawn);
 		end.addPieceToPieces(new Piece(Piece.COLOR.blue));
 		assertEquals(e.checkValidityOriginalRules(pawn, start.getPrevious(),
 				end, 12, 0), 12);
 		end.removePieceFromPieces(pawn);
 		assertEquals(e.checkValidityOriginalRules(pawn, start, end, 4, 0),
 				Engine.INVALID_MOVE);
 
 		start = new Node();
 		end = new Node();
 		start.addPieceToPieces(pawn);
 		createNodeChain(start, end, 3);
 		Player phil = new Player(Piece.COLOR.red, "Phil");
 		Player phillis = new Player(Piece.COLOR.green, "Phillis");
 		e.currentCard = new Card(2, "Things to do");
 		e.insertPlayer(phil);
 		e.insertPlayer(phillis);
 		e.rotatePlayers();
 		assertEquals(e.activePlayer, phil);
 		e.checkValidityOriginalRules(pawn, start, end, 2, 0);
 		e.rotatePlayers();
 		assertEquals(e.activePlayer, phil);
 
 	}
 
 	@Test
 	public void testPawnMovementSwapPiecesCardEleven() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		e.insertPlayer(new Player(Piece.COLOR.red, "Dave"));
 		e.insertPlayer(new Player(Piece.COLOR.blue, "Whale Rider"));
 		e.rotatePlayers();
 
 		e.currentCard = new Card(11, "TEST");
 
 		board.homePointers[0].firstPiece();
 		int test = e.pawnMove(new SorryFrame.Coordinate(11, 14),
 				new SorryFrame.Coordinate(11, 15));
 		assertEquals(test, Engine.INVALID_MOVE);
 
 		test = e.pawnMove(new SorryFrame.Coordinate(11, 14),
 				new SorryFrame.Coordinate(0, 0));
 		assertEquals(test, Engine.INVALID_MOVE);
 
 		e.currentCard = new Card(1, "TEST");
 		test = e.pawnMove(new SorryFrame.Coordinate(11, 14),
 				new SorryFrame.Coordinate(11, 15));
 		assertEquals(test, 1);
 		e.rotatePlayers();
 		test = e.pawnMove(new SorryFrame.Coordinate(11, 14),
 				new SorryFrame.Coordinate(11,15));
 		assertEquals(test, 1);
 
 		e.currentCard = new Card(11, "Test");
 		test = e.pawnMove(new SorryFrame.Coordinate(11, 14),
 				new SorryFrame.Coordinate(0, 0));
 		assertEquals(test, Engine.INVALID_MOVE);
 
 		e.rotatePlayers();
 		test = e.pawnMove(new SorryFrame.Coordinate(11, 15),
 				new SorryFrame.Coordinate(0, 11));
 		assertEquals(test, 15);
 
 		// pieces were just swapped, test that the pieces are now in the right
 		// spots
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsnb|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnr|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 
 		// try it the other way for good measure
 		e.rotatePlayers();
 		test = e.pawnMove(new SorryFrame.Coordinate(15,4),
 				new SorryFrame.Coordinate(11, 15));
 		assertEquals(test, 15);
 
 		assertEquals(
 				board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsnr|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnb|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 	}
 
 	@Test
 	public void testSwapPiecesCardElevenIllegals() {
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.newGame();
 		e.insertPlayer(new Player(Piece.COLOR.red, "Dave"));
 		e.insertPlayer(new Player(Piece.COLOR.blue, "Whale Rider"));
 		e.rotatePlayers();
 
 		// going to fiddle with the board in a hacky sorta way...
 		Piece dave = board.startPointers[0].firstPiece();
 		board.startPointers[0].removePieceFromPieces(dave);
 		assertEquals(dave.col, Piece.COLOR.red);
 
 		Piece whale = board.startPointers[1].firstPiece();
 		board.startPointers[1].removePieceFromPieces(whale);
 		assertEquals(whale.col, Piece.COLOR.blue);
 
 		board.homePointers[0].getPrevious().addPieceToPieces(dave);
 		board.homePointers[1].getPrevious().addPieceToPieces(whale);
 
 		e.currentCard = new Card(1, "TEST");
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(11, 14),
 				new SorryFrame.Coordinate(11, 15)), 1);
 
 		e.currentCard = new Card(11, "TEST");
 		// now try an illegal move - swapping a piece in safe zone, lot of
 		// freaking work
 		String before = board.toString();
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(11, 15),
 				new SorryFrame.Coordinate(5, 13)), Engine.INVALID_MOVE);
 		assertEquals(board.toString(), before);
 
 		e.rotatePlayers();
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(5, 13),
 				new SorryFrame.Coordinate(11, 15)), Engine.INVALID_MOVE);
 		// illegal because the swap FROM node is safe... subtle...
 		// silly hobbitsies...
 	}
 
 	@Test
 	public void testSwapCardSorryIllegal(){
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.insertPlayer(new Player(Piece.COLOR.red, "Dave"));
 		e.insertPlayer(new Player(Piece.COLOR.blue, "Whale Rider"));
 		e.newGame();
 		e.rotatePlayers();
 		
 		e.currentCard = new Card(13, "TEST");
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(11, 14), new SorryFrame.Coordinate(5, 1)), Engine.INVALID_MOVE);
 		
 		e.currentCard = new Card(1, "TEST");
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(11, 14), new SorryFrame.Coordinate(11, 15)), 1);
 		
 		e.rotatePlayers();
 		e.currentCard = new Card(13, "TEST");
 		assertTrue(e.pawnMove(new SorryFrame.Coordinate(11, 14), new SorryFrame.Coordinate(15,4)) > 0);
 		
 		assertEquals(board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsnb|rmn4|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsn|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		
 		// sorry has now worked fine, try sorry on someone in safe position
 		Piece dave = board.startPointers[0].firstPiece();
 		board.startPointers[0].removePieceFromPieces(dave);
 		board.homePointers[0].getPrevious().addPieceToPieces(dave);
 		
 		String before = board.toString();
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(1, 11), new SorryFrame.Coordinate(13, 10)), Engine.INVALID_MOVE);
 		assertEquals(board.toString(), before);
 		
 		e.rotatePlayers();
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(13, 10), new SorryFrame.Coordinate(1, 11)), Engine.INVALID_MOVE);
 		
 	}
 	
 	@Test
 	public void testUpdateInfo(){
 		Engine e = new Engine(new BoardList(), "english");
 		e.getUpdatedInfo();
 		assertNotNull(e.toString());
 	}
 	
 	@Test
 	public void testSevenSplit(){
 		BoardList board = new BoardList();
 		Engine e = new Engine(board, "english");
 		e.insertPlayer(new Player(Piece.COLOR.red, "Dave"));
 		e.insertPlayer(new Player(Piece.COLOR.blue, "Whale Rider"));
 		e.newGame();
 		e.rotatePlayers();
 		
 		e.currentCard = new Card(7, "TEST");
 		Piece dave = board.startPointers[0].firstPiece();
 		board.startPointers[0].removePieceFromPieces(dave);
 		Piece harry = board.startPointers[1].firstPiece();
 		board.startPointers[1].removePieceFromPieces(harry);
 		
 		board.startPointers[0].getNext().addPieceToPieces(dave);
 		board.startPointers[1].getNext().addPieceToPieces(harry);
 		e.finalizeTurn();
 		assertEquals(board.toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsnr|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnb|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		
 		assertEquals(e.pawnMove(new SorryFrame.Coordinate(11, 15), new SorryFrame.Coordinate(9, 15)), Engine.VALID_MOVE_NO_FINALIZE);
 		
 		assertEquals(e.getActualBoard().toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsn|rmn3|nn|nnr|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnb|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		
 		e.forfeit();
 		assertEquals(e.getActualBoard().toString(),
 				"hrsn|rsn|rsf|rsf|rsf|rsf|rsf|rmn0|rsn|rsnr|rmn3|nn|nn|nn|nn|hrsn|rsn|rsn"
 						+ "|rsn|rsn|nn|nn|hbsn|bsn|bsf|bsf|bsf|bsf|bsf|bmn0|bsn|bsnb|bmn3|nn|nn|nn|nn"
 						+ "|hbsn|bsn|bsn|bsn|bsn|nn|nn|hysn|ysn|ysf|ysf|ysf|ysf|ysf|ymn0|ysn|ysn|ymn4"
 						+ "|nn|nn|nn|nn|hysn|ysn|ysn|ysn|ysn|nn|nn|hgsn|gsn|gsf|gsf|gsf|gsf|gsf|gmn0"
 						+ "|gsn|gsn|gmn4|nn|nn|nn|nn|hgsn|gsn|gsn|gsn|gsn|nn|nn|");
 		
 	}
 
 	@Test
 	public void testCreateNodeChain() {
 		Node start = new Node();
 		Node end = new Node();
 
 		createNodeChain(start, end, 2);
 		assertEquals(start.getNext(), end);
 		assertEquals(end.getPrevious(), start);
 
 		start = new Node();
 		end = new Node();
 
 		createNodeChain(start, end, 3);
 		assertEquals(start.getNext().getNext(), end);
 		assertNull(start.getNext().getNext().getNext());
 		assertEquals(end.getPrevious().getPrevious(), start);
 
 		start = new Node();
 		end = new Node();
 
 		createNodeChain(start, end, 5);
 		assertEquals(start.getNext().getNext().getNext().getNext(), end);
 		assertEquals(end.getPrevious().getPrevious().getPrevious()
 				.getPrevious(), start);
 
 	}
 
 	private static void createNodeChain(Node start, Node end, int length) {
 		Node current = start;
 		for (int i = 2; i < length; i++) {
 			Node temp = new Node();
 			temp.setPrevious(current);
 			current.setNext(temp);
 			current = temp;
 		}
 		start.setPrevious(new MultiNode(start, null, Piece.COLOR.colorless));
 		current.setNext(end);
 		end.setPrevious(current);
 	}
 	
 }
