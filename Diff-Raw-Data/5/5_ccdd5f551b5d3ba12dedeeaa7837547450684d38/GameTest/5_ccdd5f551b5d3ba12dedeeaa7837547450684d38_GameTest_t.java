 /**
  * 
  */
 package charland.games.go;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 /**
  * @author Michael
  * 
  */
 @RunWith(RobolectricTestRunner.class)
 public class GameTest extends TestCase {
 
     /**
      * Test trying to capture a white stone (W4). <br>
      * <code>
      * ------0------1------2------3------4------5------6------7------8---
      * 0-----*------*------*------B1-----*------*------W2-----*------*---
      * 1-----*------*------B3-----W4-----B5-----*------W6-----*------*---
      * 2-----*------*------*------B7-----*------*------W8-----*------*---
      * </code>
      */
     @Test
     public void testCaptureSingleStone() {
 
         // Setup
         Game g = new Game();
         g.createBoard(null);
         g.playTurn(3, 0); // B1
         g.playTurn(6, 0); // W2
 
         g.playTurn(2, 1); // B3
         g.playTurn(3, 1); // W4
         g.playTurn(4, 1); // B5
         g.playTurn(6, 1); // W6
 
         // Test capturing.
         Assert.assertTrue(g.getGameBoard().toString(), g.playTurn(3, 2)); // B7 which should capture W4.
 
         Assert.assertEquals("Spot should be empty", Board.EMPTY, g.getGameBoard().getBoard()[3][1]);
     }
 
     /**
      * Test trying to capture multiple white stones (W4 & W8). <br>
      * <code>
      * ------0------1------2------3------4------5------6------7------8---
      * 0-----*------*------*------B1-----*------*------W2-----*------*---
      * 1-----*------*------B3-----W4-----B5-----*------W6-----*------*---
      * 2-----*------*------B7-----W8-----B9-----*------W10----*------*---
      * 3-----*------*------*------B11----*------*------W12----*------*---
      * </code>
      */
     @Test
     public void testCaptureMultipleStones() {
 
         // Setup
         Game g = new Game();
         g.createBoard(null);
         g.playTurn(3, 0); // B1
         g.playTurn(6, 0); // W2
 
         g.playTurn(2, 1); // B3
         g.playTurn(3, 1); // W4
         g.playTurn(4, 1); // B5
         g.playTurn(6, 1); // W6
         g.playTurn(2, 2); // B7
         g.playTurn(3, 2); // W8
         g.playTurn(4, 2); // B9
         g.playTurn(6, 2); // W10
 
         // Test capturing.
         Assert.assertTrue(g.getGameBoard().toString(), g.playTurn(3, 3)); // B11 which should capture W4 & W8.
 
         Assert.assertEquals("Spot should be empty", Board.EMPTY, g.getGameBoard().getBoard()[3][1]);
         Assert.assertEquals("Spot should be empty", Board.EMPTY, g.getGameBoard().getBoard()[3][2]);
     }
 
     /**
      * Test trying to commit suicide at B7 as black. <br>
      * <code>
      * ------0------1------2------3------4------5------6------7------8---
      * 0-----B7-----B1----W2------*------*------*------*------*------B5--
      * 1-----B3-----W4-----*------*------*------*------*------*------*---
      * 2-----W6-----*------*------*------*------*------*------*------*---
      * 3-----*------*------*------*------*------*------*------*------*---
      * 4-----*------*------*------*------*------*------*------*------*---
      * 5-----*------*------*------*------*------*------*------*------*---
      * 6-----*------*------*------*------*------*------*------*------*---
      * 7-----*------*------*------*------*------*------*------*------*---
      * 8-----*------*------*------*------*------*------*------*------*---
      * </code>
      */
     @Test
     public void testSuicide() {
 
         // Setup
         Game g = new Game();
         g.createBoard(null);
         g.playTurn(1, 0); // B1
         g.playTurn(2, 0); // W2
 
         g.playTurn(0, 1); // B3
         g.playTurn(1, 1); // W4
         g.playTurn(8, 0); // B5
         g.playTurn(0, 2); // W6
 
         boolean playTurn = g.playTurn(0, 0);
         Assert.assertFalse("black is committing suicide." + g.getGameBoard().toString(), playTurn); // B7
     }
 
     /**
      * Test trying to commit suicide at B7 as black. <br>
      * <code>
      * ------0------1------2------3------4------5------6------7------8---
      * 0-----W8-----B1----W2------*------*------*------*------*------B5--
      * 1-----B3-----W4-----*------*------*------*------*------*------B7--
      * 2-----W6-----*------*------*------*------*------*------*------*---
      * 3-----*------*------*------*------*------*------*------*------*---
      * </code>
      */
     @Test
     public void testCornerCapture() {
 
         // Setup
         Game g = new Game();
         g.createBoard(null);
         g.playTurn(1, 0); // B1
         g.playTurn(2, 0); // W2
 
         g.playTurn(0, 1); // B3
         g.playTurn(1, 1); // W4
         g.playTurn(8, 0); // B5
         g.playTurn(0, 2); // W6
         g.playTurn(8, 1); // B7
 
         boolean playTurn = g.playTurn(0, 0);
         Board gameBoard = g.getGameBoard();
         String string = "White is capturing B1 & B3." + gameBoard.toString();
         Assert.assertTrue(string, playTurn); // W8
 
         Assert.assertEquals("Spot should be White" + gameBoard.toString(), Board.WHITE, gameBoard.getBoard()[0][0]);
         Assert.assertEquals("Spot should be empty" + gameBoard.toString(), Board.EMPTY, gameBoard.getBoard()[0][1]);
         Assert.assertEquals("Spot should be empty" + gameBoard.toString(), Board.EMPTY, gameBoard.getBoard()[0][1]);
     }
 }
