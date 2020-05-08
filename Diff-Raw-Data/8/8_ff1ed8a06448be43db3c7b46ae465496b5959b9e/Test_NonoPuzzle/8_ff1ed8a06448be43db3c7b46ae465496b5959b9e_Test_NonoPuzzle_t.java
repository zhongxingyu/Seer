 /**
  * CSE 403 AA
  * Project Nonogram: Backend Test
  * @author  HyeIn Kim
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 package uw.cse403.nonogramfun.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
import java.util.Iterator;
 import junit.framework.TestCase;
 import uw.cse403.nonogramfun.nonogram.NonoPuzzle;
import uw.cse403.nonogramfun.nonogram.NonoPuzzle.NonoNum;
import uw.cse403.nonogramfun.enums.Difficulty;
 
 /**
  * This class tests NonoPuzzle object
  */
 public class Test_NonoPuzzle extends TestCase {
 	private static final Integer[][] EXP_ARR_1 = {{0, 1, 0, 1, 0},
 												  {1, 0, 1, 0, 1},
 												  {0, 1, 0, 1, 0},
 												  {0, 1, 0, 1, 0},
 												  {0, 0, 1, 0, 0}};
 	private static final int[][] EXP_COL_1 = {{1},
 											  {1, 2},
 											  {1, 1},
 											  {1, 2},
 											  {1}};
 	private static final int[][] EXP_ROW_1 = {{1, 1},
 											  {1, 1, 1},
 											  {1, 1},
 											  {1, 1},
 											  {1}};
 	private static final int EXP_ID_1 = 0;
 	private static final String EXP_NAME_1 = "Ice Cream";
 	private static final Difficulty EXP_DIFF_1 = Difficulty.EASY;
 	private static final int EXP_PIC_ROW_1 = 5;
 	private static final int EXP_PIC_COL_1 = 5;
 	private static final int EXP_NUM_ROW_1 = 3;
 	private static final int EXP_NUM_COL_1 = 2;
 	private static final Integer EXP_BG_COLOR_1 = 0;
 	private static final NonoPuzzle PUZZLE_1 = NonoPuzzle.createNonoPuzzle(EXP_ARR_1, EXP_BG_COLOR_1, EXP_NAME_1);
 
 												//               1              1              1              1              1
 	private static final Integer[][] EXP_ARR_2 = {  {1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 													{1, 1, 1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
 													{1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1},
 													{1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 1},
 													{1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1},
 													{1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1},
 													{1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
 													{1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0},
 													{1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0},
 													{1, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0},
 													{1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0},
 													{1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0},
 													{0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0},
 													{0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0},
 													{0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 1},
 													{0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1, 1},
 													{0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1},
 													{0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1},
 													{0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1},
 													{1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1},
 													{1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1},
 													{1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1},
 													{1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1},
 													{1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1},
 													{1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1}};
 	private static final int[][] EXP_COL_2 = {{7},
 											  {1, 2},
 											  {3, 4, 1, 1},
 											  {2, 1, 2, 1, 1},
 											  {1, 1, 1, 1, 1},
 											  {23},
 											  {2, 2, 1, 1, 1},
 											  {1, 2, 1, 1, 2, 1},
 											  {5, 2, 3, 1},
 											  {2, 2},
 											  {6},
 											  {3},
 											  {5},
 											  {6},
 											  {7},
 											  {7},
 											  {24},
 											  {22},
 											  {6},
 											  {6},
 											  {6},
 											  {7},
 											  {6, 6},
 											  {11},
 											  {8}};
 	private static final int[][] EXP_ROW_2 = {{2, 4},
 											  {2, 1, 6},
 											  {1, 1, 7},
 											  {1, 1, 7, 1},
 											  {1, 1, 6, 2},
 											  {1, 2, 5, 2},
 											  {3, 4, 3},
 											  {3, 2, 3},
 											  {1, 1, 2, 3},
 											  {2, 1, 2, 2},
 											  {1, 1, 2, 2},
 											  {2, 5, 2, 3},
 											  {1, 1, 1, 2, 2, 4},
 											  {1, 1, 1, 1, 2, 2, 4},
 											  {1, 1, 1, 1, 1, 2, 4},
 											  {1, 1, 1, 1, 1, 2, 4},
 											  {1, 1, 1, 1, 1, 7},
 											  {1, 1, 1, 1, 1, 6},
 											  {2, 5, 2, 6},
 											  {2, 1, 1, 5},
 											  {6, 4},
 											  {1, 3},
 											  {1, 2},
 											  {1, 1, 2},
 											  {2, 1}};
 	private static final int EXP_ID_2 = 1;
 	private static final String EXP_NAME_2 = "Music";
 	private static final Difficulty EXP_DIFF_2 = Difficulty.HARD;
 	private static final int EXP_PIC_ROW_2 = 25;
 	private static final int EXP_PIC_COL_2 = 25;
 	private static final int EXP_NUM_ROW_2 = 7;
 	private static final int EXP_NUM_COL_2 = 6;
 	private static final Integer EXP_BG_COLOR_2 = 1;
 	private static final NonoPuzzle PUZZLE_2 = NonoPuzzle.createNonoPuzzle(EXP_ARR_2, EXP_BG_COLOR_2, EXP_NAME_2);
 
 
 
 	//--Test Puzzle 1------------------------------------------------------------------------
 
 	@Test
 	public void test_createNonoPuzzle_1() {
 		PUZZLE_1.printPuzzle();
 	}
 
 	@Test
 	public void test_test_getColNonoNumItrator_1() {
 		for(int i=0; i<EXP_COL_1.length; i++) {
 			Iterator<NonoNum> itr = PUZZLE_1.getColNonoNumItrator(i);
 			for(int j=0; j<EXP_COL_1[i].length; j++) {
 				NonoNum num = itr.next();
 				assertEquals(EXP_COL_1[i][j], num.number);
 				assertSame(1, num.color);
 			}
 		}
 	}
 
 	@Test
 	public void test_getRowNonoNumItrator_1() {
 		for(int i=0; i<EXP_ROW_1.length; i++) {
 			Iterator<NonoNum> itr = PUZZLE_1.getRowNonoNumItrator(i);
 			for(int j=0; j<EXP_ROW_1[i].length; j++) {
 				NonoNum num = itr.next();
 				assertEquals(EXP_ROW_1[i][j], num.number);
 				assertSame(1, num.color);
 			}
 		}
 	}
 
 	@Test
 	public void test_getPuzzleID_1() {
 		assertSame(EXP_ID_1, PUZZLE_1.getPuzzleID());
 	}
 
 	@Test
 	public void test_getPuzzleName_1() {
 		assertEquals(EXP_NAME_1, PUZZLE_1.getPuzzleName());
 	}
 
 	@Test
 	public void test_getDifficulty_1() {
 		assertEquals(EXP_DIFF_1, PUZZLE_1.getDifficulty());
 	}
 
 	@Test
 	public void test_getNonoPicRowSize_1() {
 		assertSame(EXP_PIC_ROW_1, PUZZLE_1.getNonoPicRowSize());
 	}
 
 	@Test
 	public void test_getNonoPicColSize_1() {
 		assertSame(EXP_PIC_COL_1, PUZZLE_1.getNonoPicColSize());
 	}
 
 	@Test
 	public void test_getNonoNumRowSize_1() {
 		assertSame(EXP_NUM_ROW_1, PUZZLE_1.getNonoNumRowSize());
 	}
 
 	@Test
 	public void test_getNonoNumColSize_1() {
 		assertSame(EXP_NUM_COL_1, PUZZLE_1.getNonoNumColSize());
 	}
 
 	@Test
 	public void test_getBackgroundColor_1() {
 		assertEquals(EXP_BG_COLOR_1, PUZZLE_1.getBackgroundColor());
 	}
 
 	@Test
 	public void test_getColor_1() {
 		assertEquals(EXP_BG_COLOR_1, PUZZLE_1.getColor(0, 0));
 		assertEquals(EXP_BG_COLOR_1, PUZZLE_1.getColor(2, 2));
 		assertSame(1, PUZZLE_1.getColor(4, 2));
 	}
 
 	@Test
 	public void test_isSameColor_1() {
 		assertTrue(PUZZLE_1.isSameColor(0, 0, EXP_BG_COLOR_1));
 		assertTrue(PUZZLE_1.isSameColor(2, 2, EXP_BG_COLOR_1));
 		assertFalse(PUZZLE_1.isSameColor(4, 2, EXP_BG_COLOR_1));
 	}
 
 	@Test
 	public void test_isBackgroundColor_1() {
 		assertTrue(PUZZLE_1.isBackgroundColor(EXP_BG_COLOR_1));
 		assertFalse(PUZZLE_1.isBackgroundColor(1));
 	}
 
 
 
 	//--Test Puzzle 2------------------------------------------------------------------------
 
 	@Test
 	public void test_createNonoPuzzle_2() {
 		PUZZLE_2.printPuzzle();
 	}
 
 	@Test
 	public void test_test_getColNonoNumItrator_2() {
 		for(int i=0; i<EXP_COL_2.length; i++) {
 			Iterator<NonoNum> itr = PUZZLE_2.getColNonoNumItrator(i);
 			for(int j=0; j<EXP_COL_2[i].length; j++) {
 				NonoNum num = itr.next();
 				assertEquals(EXP_COL_2[i][j], num.number);
 				assertSame(0, num.color);
 			}
 		}
 	}
 
 	@Test
 	public void test_getRowNonoNumItrator_2() {
 		for(int i=0; i<EXP_ROW_2.length; i++) {
 			Iterator<NonoNum> itr = PUZZLE_2.getRowNonoNumItrator(i);
 			for(int j=0; j<EXP_ROW_2[i].length; j++) {
 				NonoNum num = itr.next();
 				assertEquals(EXP_ROW_2[i][j], num.number);
 				assertSame(0, num.color);
 			}
 		}
 	}
 
 	@Test
 	public void test_getPuzzleID_2() {
 		assertSame(EXP_ID_2, PUZZLE_2.getPuzzleID());
 	}
 
 	@Test
 	public void test_getPuzzleName_2() {
 		assertEquals(EXP_NAME_2, PUZZLE_2.getPuzzleName());
 	}
 
 	@Test
 	public void test_getDifficulty_2() {
 		assertEquals(EXP_DIFF_2, PUZZLE_2.getDifficulty());
 	}
 
 	@Test
 	public void test_getNonoPicRowSize_2() {
 		assertSame(EXP_PIC_ROW_2, PUZZLE_2.getNonoPicRowSize());
 	}
 
 	@Test
 	public void test_getNonoPicColSize_2() {
 		assertSame(EXP_PIC_COL_2, PUZZLE_2.getNonoPicColSize());
 	}
 
 	@Test
 	public void test_getNonoNumRowSize_2() {
 		assertSame(EXP_NUM_ROW_2, PUZZLE_2.getNonoNumRowSize());
 	}
 
 	@Test
 	public void test_getNonoNumColSize_2() {
 		assertSame(EXP_NUM_COL_2, PUZZLE_2.getNonoNumColSize());
 	}
 
 	@Test
 	public void test_getBackgroundColor_2() {
 		assertEquals(EXP_BG_COLOR_2, PUZZLE_2.getBackgroundColor());
 	}
 
 	@Test
 	public void test_getColor_2() {
 		assertEquals(EXP_BG_COLOR_2, PUZZLE_2.getColor(0, 0));
 		assertEquals(EXP_BG_COLOR_2, PUZZLE_2.getColor(3, 4));
 		assertSame(0, PUZZLE_2.getColor(1, 15));
 	}
 
 	@Test
 	public void test_isSameColor_2() {
 		assertTrue(PUZZLE_2.isSameColor(0, 0, EXP_BG_COLOR_2));
 		assertTrue(PUZZLE_2.isSameColor(3, 4, EXP_BG_COLOR_2));
 		assertFalse(PUZZLE_2.isSameColor(1, 15, EXP_BG_COLOR_2));
 	}
 
 	@Test
 	public void test_isBackgroundColor_2() {
 		assertTrue(PUZZLE_2.isBackgroundColor(EXP_BG_COLOR_2));
 		assertFalse(PUZZLE_2.isBackgroundColor(0));
 	}
}
