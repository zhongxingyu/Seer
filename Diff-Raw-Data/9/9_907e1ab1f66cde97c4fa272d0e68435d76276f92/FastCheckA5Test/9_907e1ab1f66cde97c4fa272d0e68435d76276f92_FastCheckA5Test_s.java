 package fastboard.checkmove.fastcheck;
 
 import base.testcase.OthelloTestCase;
 import fastboard.fastflip.FastBoardFlips;
 import fastboard.lineconverter.LineConverter;
 import fastboard.checkmove.calc.FastCheckCalc;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 02:53:25 PM
 * Tests whether or not FastCheckA4 checks for valid moves properly
  */
 public class FastCheckA5Test extends OthelloTestCase {
     public void testIsMoveValidBlack() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForBlack();
         FastCheckA5 check = new FastCheckA5(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("xoooooox");
         assertFalse(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("xooo_xxx");
         assertTrue(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("xxxx_oox");
         assertTrue(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_oooo_xo");
         flips.a5_d8 = LineConverter.convertStringToLine("___oo__o");
         assertFalse(check.isValidMove(flips));
         flips.a5_d8 = LineConverter.convertStringToLine("_____oxo");
         assertTrue(check.isValidMove(flips));
 
         flips.a5_d8 = LineConverter.convertStringToLine("____o_xx");
         flips.a5_h5 = LineConverter.convertStringToLine("_xooooox");
         assertFalse(check.isValidMove(flips));
         flips.a5_h5 = LineConverter.convertStringToLine("_oox____");
         assertTrue(check.isValidMove(flips));
 
         flips.a5_h5 = LineConverter.convertStringToLine("_xooxoxo");
         flips.a5_e1 = LineConverter.convertStringToLine("___oooox");
         assertFalse(check.isValidMove(flips));
         flips.a5_e1 = LineConverter.convertStringToLine("____ooox");
         assertTrue(check.isValidMove(flips));
     }
 
     public void testIsMoveValidWhite() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForWhite();
         FastCheckA5 check = new FastCheckA5(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("oxxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("oxxx_ooo");
         assertTrue(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("oooo_xxo");
         assertTrue(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_xxxx_ox");
         flips.a5_d8 = LineConverter.convertStringToLine("___xx__x");
         assertFalse(check.isValidMove(flips));
         flips.a5_d8 = LineConverter.convertStringToLine("_____xox");
         assertTrue(check.isValidMove(flips));
 
         flips.a5_d8 = LineConverter.convertStringToLine("____x_oo");
         flips.a5_h5 = LineConverter.convertStringToLine("_oxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a5_h5 = LineConverter.convertStringToLine("_xxo____");
         assertTrue(check.isValidMove(flips));
 
         flips.a5_h5 = LineConverter.convertStringToLine("_oxxoxox");
         flips.a5_e1 = LineConverter.convertStringToLine("___xxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a5_e1 = LineConverter.convertStringToLine("____xxxo");
         assertTrue(check.isValidMove(flips));
     }
 }
