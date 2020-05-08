 
 package com.laughingmasq.tetrayoo.game;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author schme
  * TODO: Remove magic numbers + copy-paste from tests (eg. corners).
  */
 public class GridTest {
 
     private Grid classicGrid;
     
     
     @Before
     public void setUp() {
         classicGrid = new ClassicGrid();
     }
     
 
     /* will be changed/removed. quick test */
     private void setUpForIsOccupiedTest(TType type) {
         TType[][] blockMap = classicGrid.getGridMap();
         for( int i=0; i < classicGrid.getWidth(); i++) {
             for( int j=0; j < classicGrid.getHeight(); j++) {
                 blockMap[i][j] = type; //TODO: change to ClassicBlocks
             }
         }
     }
 
 
     @Test
     public void dimensionsAreCorrect() {
         assertEquals(10, classicGrid.getWidth());
         assertEquals(22, classicGrid.getHeight());
     }
 
     @Test
     public void initiallyNoBlocksAreOccupied() {
 
         for(int i=0; i < classicGrid.getWidth(); i++) {
             for( int j=0; j < classicGrid.getHeight(); j++) {
                 assertEquals(false, classicGrid.isOccupied(i, j));
             }
         }
     }
 
     @Test
    public void isOccupiedReturnsFalseOnFalseCoordinates() {
         setUpForIsOccupiedTest(TType.O);
 
        assertEquals(false, classicGrid.isOccupied(-1, 0));
        assertEquals(false, classicGrid.isOccupied(0, 22));
        assertEquals(false, classicGrid.isOccupied(12345, -12345));
     }
 
     @Test
     public void isOccupiedReturnsTrueWhenOccupied() {
         setUpForIsOccupiedTest(TType.J);
 
         assertEquals(true, classicGrid.isOccupied(0,0));
         assertEquals(true, classicGrid.isOccupied(9,0));
         assertEquals(true, classicGrid.isOccupied(0, 21));
         assertEquals(true, classicGrid.isOccupied(9, 21));
         assertEquals(true, classicGrid.isOccupied(5, 10));
 
     }
 
     @Test
     public void isOccupiedReturnsFalseWhenUnoccupied() {
 
         assertEquals(false, classicGrid.isOccupied(0,0));
         assertEquals(false, classicGrid.isOccupied(9,0));
         assertEquals(false, classicGrid.isOccupied(0, 21));
         assertEquals(false, classicGrid.isOccupied(9, 21));
         assertEquals(false, classicGrid.isOccupied(5, 10));
     }
 
 }
