 package test.usecase;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import controller.GameController;
 import exception.InvalidFileException;
 import exception.OutsideTheGridException;
 import grid.*;
 
 public class ChooseGridFromFileTest {
 
 	GameController sc;
 	
 	@Before
 	public void setUp() {
 		sc = new GameController();
 	}
 	
 	@Test 
 	public void testChooseValidFile() throws InvalidFileException  {
 		sc.startNewGameFromFile(new File("validfile.grid"));
 		
 		try {
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,1)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,1)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,1)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,1)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,1)) instanceof InnerSquare
 					&& sc.getGame().getGrid().isStartingPosition(new Coordinate(5,1)));
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,1)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,1))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,1)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,1)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,1)) instanceof OuterSquare);
 			
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,2)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,2)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,2)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,2)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,2)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,2)) instanceof InnerSquare
					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,2))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,2)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,2)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,2)) instanceof OuterSquare);
 			
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,3))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,3))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,3))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,3))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,3))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,3))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,3))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,3))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,3)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,3))).hasObstacle());
 			
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,4)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,4))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,4)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,4))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,4)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,4))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,4)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,4))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,4)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,4))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,4)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,4))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,4)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,4))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,4)) instanceof InnerSquare
 					&& ((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,4))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,4)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,4))).hasObstacle());
 			
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,5)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,5))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,5)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,5))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,5)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,5))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,5)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,5))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,5)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,5))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,5)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,5))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,5)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,5))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,5)) instanceof InnerSquare
 					&& ((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,5))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,5)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,5))).hasObstacle());
 			
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,6)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,6))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,6)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,6))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,6)) instanceof InnerSquare
 					&& ((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,6))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,6)) instanceof InnerSquare
 					&& ((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,6))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,6)) instanceof InnerSquare
 					&& ((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,6))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,6)) instanceof InnerSquare
 					&& ((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,6))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,6)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,6))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,6)) instanceof InnerSquare
 					&& ((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,6))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,6)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,6))).hasObstacle());
 			
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,7))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,7))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,7))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,7))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,7))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,7))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,7))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,7))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,7)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,7))).hasObstacle());
 			
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,8)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,8)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,8)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,8)) instanceof InnerSquare
 					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,8))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,8)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,8)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,8)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,8)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,8)) instanceof OuterSquare);
 			
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(1,9)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(2,9)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(3,9)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,9)) instanceof InnerSquare
					&& !((InnerSquare)sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(4,9))).hasObstacle());
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(5,9)) instanceof InnerSquare
 					&& sc.getGame().getGrid().isStartingPosition(new Coordinate(5,9)));
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(6,9)) instanceof OuterSquare); 
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(7,9)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(8,9)) instanceof OuterSquare);
 			assertTrue(sc.getGame().getGrid().getSquareAtCoordinate(new Coordinate(9,9)) instanceof OuterSquare);
 		} catch (OutsideTheGridException e) {
 				// will not occur
 		}
 	}
 	
 	@Test (expected = InvalidFileException.class)
 	public void testChooseInvalidFile() throws InvalidFileException  {
 		sc.startNewGameFromFile(new File("invalidfile.grid"));
 		assertTrue(sc.getGame() == null);
 	}
 
 }
