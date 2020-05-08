 package test;
 
 import controller.GameController;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.awt.*;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * User: jahoefne
  * Creation Date: 03.11.13
  * Time: 11:56
  */
 public class GameControllerTest {
 
     private GameController sut;
 
     @BeforeClass
     public static void beforeClass() {
         System.out.println("Testing the GameController Class..");
     }
 
     @Before
     public void before() {
         sut = GameController.GetInstance();
         sut.resetGame();
     }
 
    /* @Test
     public void testReset() throws Exception {
 
     }*/
 
     @Test
     public void testMove() throws Exception {
         System.out.print(sut.toString());
         sut.move(new Point(1, 6), new Point(1, 5));
         System.out.print(sut.toString());
         sut.move(new Point(1, 5), new Point(1, 4));
         System.out.print(sut.toString());
         sut.move(new Point(1, 1), new Point(1, 2));
         System.out.print(sut.toString());
         sut.move(new Point(1, 5), new Point(1, 4));
         System.out.print(sut.toString());
     }
 
     @Test
     public void getUnicode() {
         for (int i = 0; i < 8; i++) {
 
             //testing pawn unicodes
             assertTrue(sut.getUnicode(new Point(i, 1)).compareTo("\u265F") == 0);
             assertTrue(sut.getUnicode(new Point(i, 6)).compareTo("\u265F") == 0);
         }
 
     }
 
     @Test
     public void whitesTurn() {
        assertTrue(sut.whitesTurn() == true);
         sut.move(new Point(1, 6), new Point(1, 5));
        assertTrue(sut.whitesTurn() == false);
         sut.move(new Point(1, 1), new Point(1, 3));
        assertTrue(sut.whitesTurn() == true);
 
     }
 
 
 }
