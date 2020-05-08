 package org.sutemi.sudoku;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: chris
  * Date: 6/6/13
  * Time: 10:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SudokuGridTest {
     @Test
     public void testGetRowPoints() throws Exception {
         SudokuGrid grid = new SudokuGrid();
         List<CellPoint> rowPoints = grid.getRowPoints(new CellPoint(3, 4));
 
         assertNotNull(rowPoints);
         assertEquals(8,rowPoints.size());
         assertTrue(rowPoints.contains(new CellPoint(3,0)));
         assertTrue(rowPoints.contains(new CellPoint(3,1)));
         assertTrue(rowPoints.contains(new CellPoint(3,2)));
         assertTrue(rowPoints.contains(new CellPoint(3,3)));
         assertTrue(rowPoints.contains(new CellPoint(3,5)));
         assertTrue(rowPoints.contains(new CellPoint(3,6)));
         assertTrue(rowPoints.contains(new CellPoint(3,7)));
         assertTrue(rowPoints.contains(new CellPoint(3,8)));
     }
 
     @Test
     public void testGetColPoints() throws Exception {
         SudokuGrid grid = new SudokuGrid();
         List<CellPoint> colPoints = grid.getColPoints(new CellPoint(3, 4));
 
         assertNotNull(colPoints);
         assertEquals(8, colPoints.size());
         assertTrue(colPoints.contains(new CellPoint(0,4)));
         assertTrue(colPoints.contains(new CellPoint(1,4)));
         assertTrue(colPoints.contains(new CellPoint(2,4)));
         assertTrue(colPoints.contains(new CellPoint(4,4)));
         assertTrue(colPoints.contains(new CellPoint(5,4)));
         assertTrue(colPoints.contains(new CellPoint(6,4)));
         assertTrue(colPoints.contains(new CellPoint(7,4)));
         assertTrue(colPoints.contains(new CellPoint(8,4)));
     }
 
     @Test
     public void testGetPeerPoints() throws Exception {
         SudokuGrid grid = new SudokuGrid();
         List<CellPoint> peerPoints = grid.getPeerPoints(new CellPoint(3, 4));
 
         assertNotNull(peerPoints);
         assertEquals(8, peerPoints.size());
         assertTrue(peerPoints.contains(new CellPoint(3,3)));
         assertTrue(peerPoints.contains(new CellPoint(3, 5)));
         assertTrue(peerPoints.contains(new CellPoint(4,3)));
         assertTrue(peerPoints.contains(new CellPoint(4, 4)));
         assertTrue(peerPoints.contains(new CellPoint(4,5)));
         assertTrue(peerPoints.contains(new CellPoint(5, 3)));
         assertTrue(peerPoints.contains(new CellPoint(5,4)));
         assertTrue(peerPoints.contains(new CellPoint(5, 5)));
     }
 
     @Test
     public void testRemovePossibility() {
         SudokuGrid grid = new SudokuGrid();
         List<Integer> possibilities = grid.getPossibilities(new CellPoint(3, 4));
         for (int i = 1; i < 10; i++) {
             assertTrue(possibilities.contains(5));
         }
        SudokuGrid newgrid = grid.eliminatePossibility(new CellPoint(3,4),5);
        List<Integer> newpossibilities = newgrid.getPossibilities(new CellPoint(3, 4));
         assertFalse(newpossibilities.contains(5));
         for (int i = 1; i < 10; i++) {
             if (i != 5) {
                 assertTrue(newpossibilities.contains(i));
             }
         }
 
     }
 
     @Test
     public void testplaceGiven() {
         SudokuGrid grid = new SudokuGrid();
         grid.placeGiven(new CellPoint(3,4), 5);
         List<Integer> possibilities = grid.getPossibilities(new CellPoint(3,4));
         assertEquals(1,possibilities.size());
         for (int i = 0; i < 9; i++) {
             possibilities = grid.getPossibilities(new CellPoint(i,4));
             if (i != 3) {
                 assertEquals(8, possibilities.size());
             }
             possibilities = grid.getPossibilities(new CellPoint(3,i));
             if (i != 4) {
                 assertEquals(8, possibilities.size());
             }
         }
 
     }
 
     @Test
     public void testplaceConjecture() {
         SudokuGrid grid = new SudokuGrid();
         SudokuGrid newgrid = grid.placeConjecture(new CellPoint(3,4), 5);
         List<Integer> possibilities = newgrid.getPossibilities(new CellPoint(3,4));
         assertEquals(1,possibilities.size());
         possibilities = grid.getPossibilities(new CellPoint(3,4));
         assertEquals(9,possibilities.size());
         for (int i = 0; i < 9; i++) {
             possibilities = newgrid.getPossibilities(new CellPoint(i,4));
             if (i != 3) {
                 assertEquals(8, possibilities.size());
             }
             possibilities = newgrid.getPossibilities(new CellPoint(3,i));
             if (i != 4) {
                 assertEquals(8, possibilities.size());
             }
             possibilities = grid.getPossibilities(new CellPoint(i,4));
             if (i != 3) {
                 assertEquals(9, possibilities.size());
             }
             possibilities = grid.getPossibilities(new CellPoint(3,i));
             if (i != 4) {
                 assertEquals(9, possibilities.size());
             }
         }
     }
 
     // Other tests and next steps
     // Place conjecture or given return null or exception when cannot be placed
     // Eliminate possibility places when the alternatives are removed
     // Data structure still pretty ugly
     // Method to test whether position is a solution
     // What does the external identification of possibilities look like
     // Wrap head around general search implementation
     // Expose Row and Column and Peers as types (List of CellPoints?)
     // Hamcrest Matcher for tests
 }
