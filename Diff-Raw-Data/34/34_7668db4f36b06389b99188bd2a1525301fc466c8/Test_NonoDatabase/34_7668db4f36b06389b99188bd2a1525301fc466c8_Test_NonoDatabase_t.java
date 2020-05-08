 /**
  * CSE 403 AA
  * Project Nonogram: Backend Test
  * @author  HyeIn Kim
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 package uw.cse403.nonogramfun.tests.nonogram;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import uw.cse403.nonogramfun.enums.Difficulty;
 import uw.cse403.nonogramfun.nonogram.NonoDatabase;
 import uw.cse403.nonogramfun.nonogram.NonoPuzzle;
 
 /**
  * This class tests NonoPuzzle object
  */
 public class Test_NonoDatabase extends TestCase {
 	
 	private final int TIMEOUT = 20000;
	private static final Integer BLACK = -16777216;
	private static final Integer WHITE = -1;
	private static final Integer[][] EXP_ARR_1 = {{BLACK, BLACK, WHITE, BLACK, WHITE},
												  {BLACK, WHITE, BLACK, WHITE, BLACK},
												  {WHITE, BLACK, WHITE, BLACK, WHITE},
												  {WHITE, BLACK, WHITE, BLACK, WHITE},
												  {WHITE, WHITE, BLACK, WHITE, WHITE}};
	private static final Integer EXP_BG_COLOR_1 = WHITE;
 	private static final String EXP_NAME_1 = "Ice Cream";
 	private static final NonoPuzzle PUZZLE_1 = NonoPuzzle.createNonoPuzzle(EXP_ARR_1, EXP_BG_COLOR_1, EXP_NAME_1);
 	
 	private int ZERO = 0;
 	
 	
 	
 	//--Test getPuzzle------------------------------------------------------------------------
 	
 	@Test(timeout=TIMEOUT)
 	public void test_getPuzzle() {
 		List<Integer> idList = null;
 		try {
 			idList = NonoDatabase.getPuzzleIDList(Difficulty.EASY);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (idList != null && idList.size() > ZERO) {
 			try {
 				int id = idList.get(ZERO);
 				NonoPuzzle puzzle = NonoDatabase.getPuzzle(id);
 				assert(puzzle != null);
 				assert(puzzle.getPuzzleID() == id);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	//--Test savePuzzle ------------------------------------------------------------------------
 	
 	@Test(timeout=TIMEOUT)
 	public void test_savePuzzle() {
 		try {
 			NonoDatabase.savePuzzle(PUZZLE_1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//--Test getPuzzleIDList ------------------------------------------------------------------------
 
 	@Test(timeout=TIMEOUT)
 	public void test_getPuzzleIDList() {
 		List<Integer> idList = null;
 		try {
 			idList = NonoDatabase.getPuzzleIDList(Difficulty.EASY);
 			assert(idList != null);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
