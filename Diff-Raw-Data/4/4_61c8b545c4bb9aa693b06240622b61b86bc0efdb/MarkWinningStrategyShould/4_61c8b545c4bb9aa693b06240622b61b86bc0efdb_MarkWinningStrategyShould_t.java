 package org.craftedsw.tictactoe.strategy;
 
 import org.craftedsw.tictactoe.Board;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import java.util.Arrays;
 
 import static java.lang.Boolean.FALSE;
 import static java.lang.Boolean.TRUE;
 import static org.craftedsw.tictactoe.Board.*;
 import static org.hamcrest.core.Is.is;
 import static org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class MarkWinningStrategyShould {
 
     @Parameters(name = "{index}: Winning mark should be {1}")
     public static Iterable<Object[]> marks() {
         return Arrays.asList(new Object[][]{
                 {new String[]{"X", "X", " ", " ", " ", " ", " ", " ", " "}, CELL_3},
                 {new String[]{"0", "0", " ", " ", " ", " ", " ", " ", " "}, CELL_3},
                 {new String[]{" ", " ", " ", "X", " ", "X", " ", " ", " "}, CELL_5},
                 {new String[]{" ", " ", " ", "0", " ", "0", " ", " ", " "}, CELL_5},
                {new String[]{" ", " ", " ", " ", " ", " ", " ", "X", "X"}, CELL_7},
                {new String[]{" ", " ", " ", " ", " ", " ", " ", "0", "0"}, CELL_7},
                 {new String[]{"X", " ", " ", " ", " ", " ", " ", " ", "X"}, CELL_5},
                 {new String[]{"0", " ", " ", " ", "0", " ", " ", " ", " "}, CELL_9},
                 {new String[]{" ", " ", " ", " ", "X", " ", "X", " ", " "}, CELL_3},
                 {new String[]{" ", " ", "0", " ", "0", " ", " ", " ", " "}, CELL_7},
                 {new String[]{" ", " ", " ", "X", " ", " ", "X", " ", " "}, CELL_1},
                 {new String[]{"0", " ", " ", " ", " ", " ", "0", " ", " "}, CELL_4}
         });
     }
 
     private final int cellToBeMarked;
     private final String[] marks;
 
     public MarkWinningStrategyShould(String[] marks, int cellToBeMarked) {
         this.marks = marks;
         this.cellToBeMarked = cellToBeMarked;
     }
 
     @Test public void
     should_return_the_winning_cell_to_be_marked() {
         MarkStrategy markStrategy = new MarkStrategy();
 
         Assert.assertThat(markStrategy.winMark(marks), is(cellToBeMarked));
     }
 
 }
