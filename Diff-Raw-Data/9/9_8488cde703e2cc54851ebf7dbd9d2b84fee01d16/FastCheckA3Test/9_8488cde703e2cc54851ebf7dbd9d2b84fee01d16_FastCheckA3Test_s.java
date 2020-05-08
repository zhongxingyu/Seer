 package fastboard.checkmove.fastcheck;
 
 import base.testcase.OthelloTestCase;
 import fastboard.FastBoardFlips;
 import fastboard.lineconverter.LineConverter;
 import fastboard.checkmove.calc.FastCheckCalc;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 02:53:25 PM
 * Tests whether or not FastCheckA1 checks for valid moves properly
  */
 public class FastCheckA3Test extends OthelloTestCase {
     public void testIsMoveValidBlack() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForBlack();
         FastCheckA3 check = new FastCheckA3(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("xoooooox");
         assertFalse(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("xo______");
         assertTrue(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("ox_oooox");
         assertTrue(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_oooo_xo");
         flips.a3_f8 = LineConverter.convertStringToLine("_oo__o__");
         assertFalse(check.isValidMove(flips));
         flips.a3_f8 = LineConverter.convertStringToLine("___oxoo_");
         assertTrue(check.isValidMove(flips));
 
         flips.a3_f8 = LineConverter.convertStringToLine("__oo_xxx");
         flips.a3_h3 = LineConverter.convertStringToLine("_xooooox");
         assertFalse(check.isValidMove(flips));
         flips.a3_h3 = LineConverter.convertStringToLine("_oox____");
         assertTrue(check.isValidMove(flips));
         
         flips.a3_h3 = LineConverter.convertStringToLine("________");
         flips.a3_c1 = LineConverter.convertStringToLine("_____oox");
         assertFalse(check.isValidMove(flips));
         flips.a3_c1 = LineConverter.convertStringToLine("______ox");
         assertTrue(check.isValidMove(flips));
     }
 
     public void testIsMoveValidWhite() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForWhite();
         FastCheckA3 check = new FastCheckA3(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("oxxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("ox______");
         assertTrue(check.isValidMove(flips));
         flips.a1_a8 = LineConverter.convertStringToLine("xo_xxxxo");
         assertTrue(check.isValidMove(flips));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_xxxx_ox");
         flips.a3_f8 = LineConverter.convertStringToLine("_xx__x__");
         assertFalse(check.isValidMove(flips));
         flips.a3_f8 = LineConverter.convertStringToLine("___xoxx_");
         assertTrue(check.isValidMove(flips));
 
         flips.a3_f8 = LineConverter.convertStringToLine("__xx_ooo");
         flips.a3_h3 = LineConverter.convertStringToLine("_oxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a3_h3 = LineConverter.convertStringToLine("_xxo____");
         assertTrue(check.isValidMove(flips));
 
         flips.a3_h3 = LineConverter.convertStringToLine("________");
         flips.a3_c1 = LineConverter.convertStringToLine("_____xxo");
         assertFalse(check.isValidMove(flips));
         flips.a3_c1 = LineConverter.convertStringToLine("______xo");
         assertTrue(check.isValidMove(flips));
     }
 }
