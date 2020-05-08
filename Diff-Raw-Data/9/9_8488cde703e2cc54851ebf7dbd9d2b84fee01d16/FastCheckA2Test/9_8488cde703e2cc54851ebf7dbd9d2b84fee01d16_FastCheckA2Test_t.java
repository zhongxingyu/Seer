 package fastboard.checkmove.fastcheck;
 
 import base.testcase.OthelloTestCase;
 import fastboard.FastBoardFlips;
 import fastboard.lineconverter.LineConverter;
 import fastboard.checkmove.calc.FastCheckCalc;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 02:27:03 PM
 * Tests whether or not FastCheckA2 checks for valid moves properly
  */
 public class FastCheckA2Test extends OthelloTestCase {
     public void testIsMoveValidBlack() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForBlack();
         FastCheckA2 check = new FastCheckA2(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_oooooox");
         assertFalse(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("__ooooox");
         assertTrue(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_oooo_xo");
         flips.a2_g8 = LineConverter.convertStringToLine("_oo__o__");
         assertFalse(check.isValidMove(flips));
         flips.a2_g8 = LineConverter.convertStringToLine("__oxooo_");
         assertTrue(check.isValidMove(flips));
 
         flips.a2_g8 = LineConverter.convertStringToLine("_oo_xxxx");
         flips.a2_h2 = LineConverter.convertStringToLine("_xooooox");
         assertFalse(check.isValidMove(flips));
         flips.a2_h2 = LineConverter.convertStringToLine("_oox____");
         assertTrue(check.isValidMove(flips));
     }
 
     public void testIsMoveValidWhite() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForWhite();
         FastCheckA2 check = new FastCheckA2(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_xxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("__xxxxxo");
         assertTrue(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_xxxx_ox");
         flips.a2_g8 = LineConverter.convertStringToLine("_xx__x__");
         assertFalse(check.isValidMove(flips));
         flips.a2_g8 = LineConverter.convertStringToLine("__xoxxx_");
         assertTrue(check.isValidMove(flips));
 
         flips.a2_g8 = LineConverter.convertStringToLine("_xx_oooo");
         flips.a2_h2 = LineConverter.convertStringToLine("_oxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a2_h2 = LineConverter.convertStringToLine("_xxo____");
         assertTrue(check.isValidMove(flips));
     }
 }
