 package net.glxn.sudoku;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class JSudokuTestCase {
 
     @Test
     public void shouldSolveforXSibling() {
         String expected = "9";
 
         JSudoku jSudoku = new JSudoku();
         ArrayList<Cell> xSiblings = jSudoku.textField9.xSiblings;
         ArrayList<String> stringList = new ArrayList<String>();
         stringList.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8"));
 
        for (Cell ySib : xSiblings) {
            ySib.setText(stringList.get(0));
             stringList.remove(0);
         }
 
         jSudoku.solvePuzzle(jSudoku.getNumberOfEmptyCellsInGrid());
 
         Assert.assertEquals(expected, jSudoku.textField9.getText());
     }
 
     @Test
     public void shouldSolveforYSibling() {
         String expected = "9";
 
         JSudoku jSudoku = new JSudoku();
         ArrayList<Cell> ysiblings = jSudoku.textField9.ySiblings;
         ArrayList<String> stringList = new ArrayList<String>();
         stringList.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8"));
 
         for (Cell ySib : ysiblings) {
             ySib.setText(stringList.get(0));
             stringList.remove(0);
         }
 
         jSudoku.solvePuzzle(jSudoku.getNumberOfEmptyCellsInGrid());
 
         Assert.assertEquals(expected, jSudoku.textField9.getText());
     }
 
     @Test
     public void shouldSolveforSubGridSibling() {
         String expected = "9";
 
         JSudoku jSudoku = new JSudoku();
         ArrayList<Cell> ysiblings = jSudoku.textField9.subGridSiblings;
         ArrayList<String> stringList = new ArrayList<String>();
         stringList.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8"));
 
         for (Cell ySib : ysiblings) {
             ySib.setText(stringList.get(0));
             stringList.remove(0);
         }
 
         jSudoku.solvePuzzle(jSudoku.getNumberOfEmptyCellsInGrid());
 
         Assert.assertEquals(expected, jSudoku.textField9.getText());
     }
 
     @Test
     public void shouldBeAbleToSolveSample() {
         JSudoku jSudoku = new JSudoku();
         jSudoku.loadSample();
 
         confirmSample(jSudoku);
 
         jSudoku.solvePuzzle(jSudoku.getNumberOfEmptyCellsInGrid());
 
         confirmWholeGrid(jSudoku);
     }
 
     @Test
     public void clearBoardShouldGiveCellsNewPossibleValues() {
         JSudoku jSudoku = new JSudoku();
         jSudoku.loadSample();
 
         confirmSample(jSudoku);
 
         jSudoku.clearAllCells();
 
         for (Cell cell : jSudoku.allCells) {
             Assert.assertEquals(9, cell.getPossibleValues().size());
         }
     }
 
     /**
      * using the following site url http://www.menneske.no/sudoku/random.html?diff=8
      * the test should be able to solve. Once the solver can do this, it is complete.
      */
     @Test
     public void shouldBeAbleToSolveSuperHardLevelSudoku() {
         JSudoku jSudoku = new JSudoku();
         jSudoku.loadSuperHardSample();
 
         jSudoku.solve();
 
         confirmSuperHardPuzzle(jSudoku);
 
     }
 
     private void confirmSuperHardPuzzle(JSudoku jSudoku) {
         confirmExpectedValueAtCoord(jSudoku,1,1,3);
         confirmExpectedValueAtCoord(jSudoku,2,1,8);
         confirmExpectedValueAtCoord(jSudoku,3,1,1);
         confirmExpectedValueAtCoord(jSudoku,4,1,9);
         confirmExpectedValueAtCoord(jSudoku,5,1,2);
         confirmExpectedValueAtCoord(jSudoku,6,1,6);
         confirmExpectedValueAtCoord(jSudoku,7,1,4);
         confirmExpectedValueAtCoord(jSudoku,8,1,5);
         confirmExpectedValueAtCoord(jSudoku,9,1,7);
 
         confirmExpectedValueAtCoord(jSudoku,1,2,6);
         confirmExpectedValueAtCoord(jSudoku,2,2,9);
         confirmExpectedValueAtCoord(jSudoku,3,2,4);
         confirmExpectedValueAtCoord(jSudoku,4,2,1);
         confirmExpectedValueAtCoord(jSudoku,5,2,7);
         confirmExpectedValueAtCoord(jSudoku,6,2,5);
         confirmExpectedValueAtCoord(jSudoku,7,2,2);
         confirmExpectedValueAtCoord(jSudoku,8,2,8);
         confirmExpectedValueAtCoord(jSudoku,9,2,3);
 
         confirmExpectedValueAtCoord(jSudoku,1,3,2);
         confirmExpectedValueAtCoord(jSudoku,2,3,5);
         confirmExpectedValueAtCoord(jSudoku,3,3,7);
         confirmExpectedValueAtCoord(jSudoku,4,3,8);
         confirmExpectedValueAtCoord(jSudoku,5,3,3);
         confirmExpectedValueAtCoord(jSudoku,6,3,4);
         confirmExpectedValueAtCoord(jSudoku,7,3,6);
         confirmExpectedValueAtCoord(jSudoku,8,3,1);
         confirmExpectedValueAtCoord(jSudoku,9,3,9);
 
 
         confirmExpectedValueAtCoord(jSudoku,1,4,1);
         confirmExpectedValueAtCoord(jSudoku,2,4,3);
         confirmExpectedValueAtCoord(jSudoku,3,4,6);
         confirmExpectedValueAtCoord(jSudoku,4,4,5);
         confirmExpectedValueAtCoord(jSudoku,5,4,9);
         confirmExpectedValueAtCoord(jSudoku,6,4,2);
         confirmExpectedValueAtCoord(jSudoku,7,4,8);
         confirmExpectedValueAtCoord(jSudoku,8,4,7);
         confirmExpectedValueAtCoord(jSudoku,9,4,4);
 
         confirmExpectedValueAtCoord(jSudoku,1,5,8);
         confirmExpectedValueAtCoord(jSudoku,2,5,7);
         confirmExpectedValueAtCoord(jSudoku,3,5,9);
         confirmExpectedValueAtCoord(jSudoku,4,5,4);
         confirmExpectedValueAtCoord(jSudoku,5,5,1);
         confirmExpectedValueAtCoord(jSudoku,6,5,3);
         confirmExpectedValueAtCoord(jSudoku,7,5,5);
         confirmExpectedValueAtCoord(jSudoku,8,5,2);
         confirmExpectedValueAtCoord(jSudoku,9,5,6);
 
         confirmExpectedValueAtCoord(jSudoku,1,6,4);
         confirmExpectedValueAtCoord(jSudoku,2,6,2);
         confirmExpectedValueAtCoord(jSudoku,3,6,5);
         confirmExpectedValueAtCoord(jSudoku,4,6,7);
         confirmExpectedValueAtCoord(jSudoku,5,6,6);
         confirmExpectedValueAtCoord(jSudoku,6,6,8);
         confirmExpectedValueAtCoord(jSudoku,7,6,3);
         confirmExpectedValueAtCoord(jSudoku,8,6,9);
         confirmExpectedValueAtCoord(jSudoku,9,6,1);
 
         confirmExpectedValueAtCoord(jSudoku,1,7,7);
         confirmExpectedValueAtCoord(jSudoku,2,7,6);
         confirmExpectedValueAtCoord(jSudoku,3,7,3);
         confirmExpectedValueAtCoord(jSudoku,4,7,2);
         confirmExpectedValueAtCoord(jSudoku,5,7,8);
         confirmExpectedValueAtCoord(jSudoku,6,7,1);
         confirmExpectedValueAtCoord(jSudoku,7,7,9);
         confirmExpectedValueAtCoord(jSudoku,8,7,4);
         confirmExpectedValueAtCoord(jSudoku,9,7,5);
 
         confirmExpectedValueAtCoord(jSudoku,1,8,9);
         confirmExpectedValueAtCoord(jSudoku,2,8,4);
         confirmExpectedValueAtCoord(jSudoku,3,8,8);
         confirmExpectedValueAtCoord(jSudoku,4,8,6);
         confirmExpectedValueAtCoord(jSudoku,5,8,5);
         confirmExpectedValueAtCoord(jSudoku,6,8,7);
         confirmExpectedValueAtCoord(jSudoku,7,8,1);
         confirmExpectedValueAtCoord(jSudoku,8,8,3);
         confirmExpectedValueAtCoord(jSudoku,9,8,2);
 
         confirmExpectedValueAtCoord(jSudoku,1,9,5);
         confirmExpectedValueAtCoord(jSudoku,2,9,1);
         confirmExpectedValueAtCoord(jSudoku,3,9,2);
         confirmExpectedValueAtCoord(jSudoku,4,9,3);
         confirmExpectedValueAtCoord(jSudoku,5,9,4);
         confirmExpectedValueAtCoord(jSudoku,6,9,9);
         confirmExpectedValueAtCoord(jSudoku,7,9,7);
         confirmExpectedValueAtCoord(jSudoku,8,9,6);
         confirmExpectedValueAtCoord(jSudoku,9,9,8);
 
     }
 
     private void confirmSample(JSudoku jSudoku) {
         confirmExpectedValueAtCoord(jSudoku, 2, 1, 1);
         confirmExpectedValueAtCoord(jSudoku, 4, 1, 9);
         confirmExpectedValueAtCoord(jSudoku, 5, 1, 5);
         confirmExpectedValueAtCoord(jSudoku, 7, 1, 7);
 
         confirmExpectedValueAtCoord(jSudoku, 2, 2, 5);
         confirmExpectedValueAtCoord(jSudoku, 5, 2, 3);
 
         confirmExpectedValueAtCoord(jSudoku, 2, 3, 9);
         confirmExpectedValueAtCoord(jSudoku, 4, 3, 6);
         confirmExpectedValueAtCoord(jSudoku, 9, 3, 5);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 4, 2);
         confirmExpectedValueAtCoord(jSudoku, 3, 4, 6);
         confirmExpectedValueAtCoord(jSudoku, 6, 4, 8);
 
         confirmExpectedValueAtCoord(jSudoku, 3, 5, 8);
         confirmExpectedValueAtCoord(jSudoku, 7, 5, 4);
 
         confirmExpectedValueAtCoord(jSudoku, 4, 6, 3);
         confirmExpectedValueAtCoord(jSudoku, 7, 6, 6);
         confirmExpectedValueAtCoord(jSudoku, 9, 6, 9);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 7, 4);
         confirmExpectedValueAtCoord(jSudoku, 6, 7, 3);
         confirmExpectedValueAtCoord(jSudoku, 8, 7, 5);
 
         confirmExpectedValueAtCoord(jSudoku, 5, 8, 8);
         confirmExpectedValueAtCoord(jSudoku, 8, 8, 6);
 
         confirmExpectedValueAtCoord(jSudoku, 3, 9, 1);
         confirmExpectedValueAtCoord(jSudoku, 5, 9, 4);
         confirmExpectedValueAtCoord(jSudoku, 6, 9, 5);
         confirmExpectedValueAtCoord(jSudoku, 8, 9, 2);
     }
 
     private void confirmWholeGrid(JSudoku jSudoku) {
         confirmExpectedValueAtCoord(jSudoku, 1, 1, 8);
         confirmExpectedValueAtCoord(jSudoku, 2, 1, 1);
         confirmExpectedValueAtCoord(jSudoku, 3, 1, 2);
         confirmExpectedValueAtCoord(jSudoku, 4, 1, 9);
         confirmExpectedValueAtCoord(jSudoku, 5, 1, 5);
         confirmExpectedValueAtCoord(jSudoku, 6, 1, 4);
         confirmExpectedValueAtCoord(jSudoku, 7, 1, 7);
         confirmExpectedValueAtCoord(jSudoku, 8, 1, 3);
         confirmExpectedValueAtCoord(jSudoku, 9, 1, 6);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 2, 6);
         confirmExpectedValueAtCoord(jSudoku, 2, 2, 5);
         confirmExpectedValueAtCoord(jSudoku, 3, 2, 4);
         confirmExpectedValueAtCoord(jSudoku, 4, 2, 8);
         confirmExpectedValueAtCoord(jSudoku, 5, 2, 3);
         confirmExpectedValueAtCoord(jSudoku, 6, 2, 7);
         confirmExpectedValueAtCoord(jSudoku, 7, 2, 2);
         confirmExpectedValueAtCoord(jSudoku, 8, 2, 9);
         confirmExpectedValueAtCoord(jSudoku, 9, 2, 1);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 3, 7);
         confirmExpectedValueAtCoord(jSudoku, 2, 3, 9);
         confirmExpectedValueAtCoord(jSudoku, 3, 3, 3);
         confirmExpectedValueAtCoord(jSudoku, 4, 3, 6);
         confirmExpectedValueAtCoord(jSudoku, 5, 3, 2);
         confirmExpectedValueAtCoord(jSudoku, 6, 3, 1);
         confirmExpectedValueAtCoord(jSudoku, 7, 3, 8);
         confirmExpectedValueAtCoord(jSudoku, 8, 3, 4);
         confirmExpectedValueAtCoord(jSudoku, 9, 3, 5);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 4, 2);
         confirmExpectedValueAtCoord(jSudoku, 2, 4, 7);
         confirmExpectedValueAtCoord(jSudoku, 3, 4, 6);
         confirmExpectedValueAtCoord(jSudoku, 4, 4, 4);
         confirmExpectedValueAtCoord(jSudoku, 5, 4, 9);
         confirmExpectedValueAtCoord(jSudoku, 6, 4, 8);
         confirmExpectedValueAtCoord(jSudoku, 7, 4, 5);
         confirmExpectedValueAtCoord(jSudoku, 8, 4, 1);
         confirmExpectedValueAtCoord(jSudoku, 9, 4, 3);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 5, 9);
         confirmExpectedValueAtCoord(jSudoku, 2, 5, 3);
         confirmExpectedValueAtCoord(jSudoku, 3, 5, 8);
         confirmExpectedValueAtCoord(jSudoku, 4, 5, 5);
         confirmExpectedValueAtCoord(jSudoku, 5, 5, 1);
         confirmExpectedValueAtCoord(jSudoku, 6, 5, 6);
         confirmExpectedValueAtCoord(jSudoku, 7, 5, 4);
         confirmExpectedValueAtCoord(jSudoku, 8, 5, 7);
         confirmExpectedValueAtCoord(jSudoku, 9, 5, 2);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 6, 1);
         confirmExpectedValueAtCoord(jSudoku, 2, 6, 4);
         confirmExpectedValueAtCoord(jSudoku, 3, 6, 5);
         confirmExpectedValueAtCoord(jSudoku, 4, 6, 3);
         confirmExpectedValueAtCoord(jSudoku, 5, 6, 7);
         confirmExpectedValueAtCoord(jSudoku, 6, 6, 2);
         confirmExpectedValueAtCoord(jSudoku, 7, 6, 6);
         confirmExpectedValueAtCoord(jSudoku, 8, 6, 8);
         confirmExpectedValueAtCoord(jSudoku, 9, 6, 9);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 7, 4);
         confirmExpectedValueAtCoord(jSudoku, 2, 7, 8);
         confirmExpectedValueAtCoord(jSudoku, 3, 7, 9);
         confirmExpectedValueAtCoord(jSudoku, 4, 7, 2);
         confirmExpectedValueAtCoord(jSudoku, 5, 7, 6);
         confirmExpectedValueAtCoord(jSudoku, 6, 7, 3);
         confirmExpectedValueAtCoord(jSudoku, 7, 7, 1);
         confirmExpectedValueAtCoord(jSudoku, 8, 7, 5);
         confirmExpectedValueAtCoord(jSudoku, 9, 7, 7);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 8, 5);
         confirmExpectedValueAtCoord(jSudoku, 2, 8, 2);
         confirmExpectedValueAtCoord(jSudoku, 3, 8, 7);
         confirmExpectedValueAtCoord(jSudoku, 4, 8, 1);
         confirmExpectedValueAtCoord(jSudoku, 5, 8, 8);
         confirmExpectedValueAtCoord(jSudoku, 6, 8, 9);
         confirmExpectedValueAtCoord(jSudoku, 7, 8, 3);
         confirmExpectedValueAtCoord(jSudoku, 8, 8, 6);
         confirmExpectedValueAtCoord(jSudoku, 9, 8, 4);
 
         confirmExpectedValueAtCoord(jSudoku, 1, 9, 3);
         confirmExpectedValueAtCoord(jSudoku, 2, 9, 6);
         confirmExpectedValueAtCoord(jSudoku, 3, 9, 1);
         confirmExpectedValueAtCoord(jSudoku, 4, 9, 7);
         confirmExpectedValueAtCoord(jSudoku, 5, 9, 4);
         confirmExpectedValueAtCoord(jSudoku, 6, 9, 5);
         confirmExpectedValueAtCoord(jSudoku, 7, 9, 9);
         confirmExpectedValueAtCoord(jSudoku, 8, 9, 2);
         confirmExpectedValueAtCoord(jSudoku, 9, 9, 8);
     }
 
     private void confirmExpectedValueAtCoord(JSudoku jSudoku, int x, int y, Integer expected) {
         Assert.assertEquals("Confirm ["+x+","+y+"] = "+ expected, expected, jSudoku.getCellAtCoord(x, y, jSudoku.allCells).getIntValue());
     }
 }
