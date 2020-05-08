 package com.ddamiani.codejam.tictactoe;
 
 import com.ddamiani.codejam.tictactoe.TicTacToe.Result;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.*;
 import java.util.*;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 /**
  * Unit tests for the Board class
  */
 public final class TicTacToeTest {
     private TicTacToe tester;
 
     @Before
     public final void setUp() throws FileNotFoundException {
         tester = new TicTacToe(this.getClass().getResource("/test_board_input.txt").getFile(), null, true);
     }
 
     @Test
     public final void testTestMode() throws FileNotFoundException {
        TicTacToe nonTestTicTacToe = new TicTacToe(this.getClass().getResource("/test_board_input.txt").getFile(), null);
        assertFalse(nonTestTicTacToe.isTestMode());
     }
 
     @Test
     public final void testLineNum() throws IOException {
         assertEquals(0, tester.getNumCases());
         tester.operate();
         assertEquals(6, tester.getNumCases());
         assertEquals(tester.getNumCases(), tester.getNumEmittedLines());
         assertEquals(tester.getNumCases() * tester.getNumLinesPerCase(), tester.getNumConsumedLines());
     }
 
     @Test
     public final void testOutput() throws IOException {
         tester.operate();
         assertEquals(6, tester.results.size());
         List<Result> expectedResults = Arrays.asList(Result.WIN_X,
                 Result.DRAW,
                 Result.INCOMPLETE,
                 Result.WIN_O,
                 Result.WIN_O,
                 Result.WIN_O);
         for (int i = 0; i < expectedResults.size(); i++) {
             assertEquals("Test of board " + (i + 1), expectedResults.get(i), tester.results.get(i));
         }
     }
 }
