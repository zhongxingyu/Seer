 package uw.cse403.nonogramfun.tests.network;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import uw.cse403.nonogramfun.enums.Difficulty;
 import uw.cse403.nonogramfun.network.NonoClient;
 import uw.cse403.nonogramfun.nonogram.NonoPuzzle;
 
 public class Test_NonoClient extends TestCase {
 
	private static final Integer BLACK = -16777216;
	private static final Integer WHITE = -1;
	private static final Integer[][] EXP_ARR_1 = {{BLACK, BLACK, WHITE, BLACK, WHITE},
												  {BLACK, WHITE, BLACK, WHITE, BLACK},
												  {WHITE, BLACK, WHITE, BLACK, WHITE},
												  {WHITE, BLACK, WHITE, BLACK, WHITE},
												  {WHITE, WHITE, BLACK, WHITE, WHITE}};
	private static final Integer EXP_BG_COLOR_1 = WHITE;
 	private static final String EXP_NAME_1 = "Ice Cream";
 	
 	public void test_createPuzle() {
 		try {
 			NonoClient.createPuzzle(EXP_ARR_1, EXP_BG_COLOR_1, EXP_NAME_1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//--Test getPuzzle------------------------------------------------------------------------
 	
 	@Test
 	public void test_getPuzzle() {
 		try {
 			NonoPuzzle puzzle = NonoClient.getPuzzle(Difficulty.EASY);
 			assert(puzzle != null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
