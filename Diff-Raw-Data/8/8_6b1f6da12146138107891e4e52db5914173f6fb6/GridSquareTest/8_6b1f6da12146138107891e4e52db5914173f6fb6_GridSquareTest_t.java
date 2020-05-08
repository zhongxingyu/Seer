 package com.noobathon.minesweeper.test;
 
 import com.noobathon.minesweeper.ui.BombSquare;
 import com.noobathon.minesweeper.ui.GridSquare;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
 
 import com.noobathon.minesweeper.ui.MinesweeperGridFrame;
 import org.junit.Before;
 import org.junit.Test;
 
 public class GridSquareTest
 {
     private GridSquare square0;
     private GridSquare square1;
     private GridSquare square2;
     private GridSquare square3;
     private MinesweeperGridFrame frame;
 
     @Before
     public void setUp()
     {
         square0 = new GridSquare(0,0,frame);
         square1 = new GridSquare(1,1,frame);
         square2 = new BombSquare(2,2,frame);
         square3 = new BombSquare(3,3,frame);
     }
 
     @Test
     public void shouldBeAbleToRetrieveXCoordinate()
     {
         assertEquals(square0.getXCoordinate(), 0);
         assertEquals(square1.getXCoordinate(), 1);
         assertEquals(square2.getXCoordinate(), 2);
         assertEquals(square3.getXCoordinate(), 3);
     }
 
     @Test
     public void shouldBeAbleToRetrieveYCoordinate()
     {
         assertEquals(square0.getXCoordinate(), 0);
         assertEquals(square1.getXCoordinate(), 1);
         assertEquals(square2.getXCoordinate(), 2);
         assertEquals(square3.getXCoordinate(), 3);
     }
 
     @Test
     public void gridSquareShouldReturnTypeOfEmpty()
     {
         assertEquals(square0.getSquareType(), GridSquare.EMPTY);
     }
 
     @Test
     public void bombSquareShouldReturnTypeOfBomb()
     {
         assertEquals(square3.getSquareType(), GridSquare.BOMB);
     }
 
     @Test
     public void gridSquareShouldBeInitializedToCovered()
     {
         assertTrue(square0.isCovered());
     }
 
    @Test
    public void gridSquareShouldBeInitializedToUnFlagged()
    {
        assertFalse(square0.isFlagged());
    }
 }
