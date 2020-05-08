 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.bainedog.sudoku;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author baine
  */
 public class SolutionTest {
 
     public SolutionTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     
     @Test
     public void testRowConsistency() {
         int[][] cells = new int[][] {
             {1,2,1,2},
             {3,0,3,0},
             {2,3,2,3},
             {0,1,0,1}
         };
         IllegalArgumentException e = null;
         try {
             Solution sol = new Solution(cells);
         } catch(Solution.IllegalRowException ex) {
             e = ex;
         }
         assertNotNull(e);
     }
 
     @Test
     public void testColumnConsistency() {
         int[][] cells = new int[][] {
             {1,3,2,0},
             {2,0,3,1},
             {1,3,2,0},
             {2,0,3,1}
 
         };
         IllegalArgumentException e = null;
         try {
             Solution sol = new Solution(cells);
         } catch(Solution.IllegalColumnException ex) {
             e = ex;
         }
         assertNotNull(e);
     }
 
     @Test
     public void testBoxConsistency() {
         int[][] cells = new int[][] {
             {0,1,2,3},
             {1,2,3,0},
             {2,3,0,1},
             {3,0,1,2}
 
         };
         IllegalArgumentException e = null;
         try {
             Solution sol = new Solution(cells);
         } catch(Solution.IllegalBoxException ex) {
             e = ex;
         }
         assertNotNull(e);
     }
 
     @Test
     public void testSquare() {
         int[][] cells = new int[][] {
             {0,1,2},
             {1,2,3},
             {2,3,0},
             {3,0,1}
 
         };
         IllegalArgumentException e = null;
         try {
             Solution sol = new Solution(cells);
         } catch(Solution.NotSquareException ex) {
             e = ex;
         }
         assertNotNull(e);
 
         cells = new int[][] {
             {0,1,2,3},
             {1,2,3,0},
             {2,3,0,1},
             {3,0,1,2}
 
         };
 
         try {
             Solution sol = new Solution(cells);
         } catch(Solution.IllegalBoxException ex) {
             e = ex;
         }
         assertNotNull(e);
     }
 
     @Test
     public void testOrdered() {
       int[][] cells = new int[][] {
             {0,1,2},
             {1,2,3},
             {2,3,0}
         };
         IllegalArgumentException e = null;
         try {
             Solution sol = new Solution(cells);
         } catch(Solution.NotOrderedException ex) {
             e = ex;
         }
         assertNotNull(e);
     }
 
     private int[][] test9x9() {
         return new int[][] {
             {5,3,4,6,7,8,0,1,2},
             {6,7,2,1,0,5,3,4,8},
             {1,0,8,3,4,2,5,6,7},
             {8,5,0,7,6,1,4,2,3},
             {4,2,6,8,5,3,7,0,1},
             {7,1,3,0,2,4,8,5,6},
             {0,6,1,5,3,7,2,8,4},
             {2,8,7,4,1,0,6,3,5},
             {3,4,5,2,8,6,1,7,0}
         };
     }
 
     private int[][] test4x4() {
         return new int[][] {
             {0,1,2,3},
             {2,3,0,1},
             {1,0,3,2},
             {3,2,1,0}
         };
     }
 
     private int[][] test1x1() {
         return new int[][] {
             {0}
         };
     }
 
     @Test
     public void testGetAndOrder() {
         int[][] cells = test9x9();
         Solution sol = new Solution(cells);
         assertEquals(3, sol.getOrder());
         for (int i = 0; i < 9; i++) {
             for (int j = 0; j < 9; j++) {
                assertEquals(cells[i][j], sol.get(i, j));
             }
         }
 
         cells = test4x4();
         sol = new Solution(cells);
         assertEquals(2, sol.getOrder());
         for (int i = 0; i < 4; i++) {
             for (int j = 0; j < 4; j++) {
                assertEquals(cells[i][j], sol.get(i, j));
             }
         }
 
         cells = test1x1();
         sol = new Solution(cells);
         assertEquals(1, sol.getOrder());
         for (int i = 0; i < 1; i++) {
             for (int j = 0; j < 1; j++) {
                assertEquals(cells[i][j], sol.get(i, j));
             }
         }
     }
 }
