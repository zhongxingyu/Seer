 package fastboard;
 
 import base.testcase.OthelloTestCase;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 22, 2009
  * Time: 1:03:18 AM
  * This class tests whether or not FastBoardFlip does what it is supposed to do
  */
 public class FastBoardFlipsTest extends OthelloTestCase {
     public void testBlackPlaceA1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
 
         flips.blackPlaceA1();
 
         assertEquals("x_______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a1_h1));
     }
 
     public void testWhitePlaceA1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
 
         flips.whitePlaceA1();
 
         assertEquals("o_______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a1_h1));
     }
 
     public void testBlackPlaceA2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
 
         flips.blackPlaceA2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a2_h2));
     }
 
     public void testBlackFlipA2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_o______");
         flips.a2_g8 = LineConverter.convertStringToLine("_o______");
         flips.a2_h2 = LineConverter.convertStringToLine("o_______");
 
         flips.blackFlipA2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a2_h2));
     }
 
     public void testWhitePlaceA2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
 
         flips.whitePlaceA2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a2_h2));
     }
 
     public void testWhiteFlipA2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_x______");
         flips.a2_g8 = LineConverter.convertStringToLine("_x______");
         flips.a2_h2 = LineConverter.convertStringToLine("x_______");
 
         flips.whiteFlipA2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a2_h2));
     }
 
     public void testBlackPlaceA3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_c1));
 
         flips.blackPlaceA3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a3_c1));
     }
 
     public void testBlackFlipA3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_c1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("__o_____");
         flips.a3_f8 = LineConverter.convertStringToLine("__o_____");
         flips.a3_h3 = LineConverter.convertStringToLine("o_______");
         flips.a3_c1 = LineConverter.convertStringToLine("_____o__");
 
         flips.blackFlipA3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a3_c1));
     }
 
     public void testWhitePlaceA3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_c1));
 
         flips.whitePlaceA3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a3_c1));
     }
 
     public void testWhiteFlipA3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_c1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("__x_____");
         flips.a3_f8 = LineConverter.convertStringToLine("__x_____");
         flips.a3_h3 = LineConverter.convertStringToLine("x_______");
         flips.a3_c1 = LineConverter.convertStringToLine("_____x_x");
 
         flips.whiteFlipA3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____o_x", LineConverter.convertLineToString(flips.a3_c1));
     }
 
     public void testBlackPlaceA4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_d1));
 
         flips.blackPlaceA4();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("___x____", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("____x___", LineConverter.convertLineToString(flips.a4_d1));
     }
 
     public void testBlackFlipA4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_d1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("___o____");
         flips.a4_e8 = LineConverter.convertStringToLine("___o____");
         flips.a4_h4 = LineConverter.convertStringToLine("o_______");
         flips.a4_d1 = LineConverter.convertStringToLine("____o___");
 
         flips.blackFlipA4();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("___x____", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("____x___", LineConverter.convertLineToString(flips.a4_d1));
     }
 
     public void testWhitePlaceA4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_d1));
 
         flips.whitePlaceA4();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("___o____", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("____o___", LineConverter.convertLineToString(flips.a4_d1));
     }
 
     public void testWhiteFlipA4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_d1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("___x____");
         flips.a4_e8 = LineConverter.convertStringToLine("___x____");
         flips.a4_h4 = LineConverter.convertStringToLine("x_______");
         flips.a4_d1 = LineConverter.convertStringToLine("____x___");
 
         flips.whiteFlipA4();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("___o____", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("____o___", LineConverter.convertLineToString(flips.a4_d1));
     }
 
     public void testBlackPlaceA5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_e1));
 
         flips.blackPlaceA5();
 
         assertEquals("____x___", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("____x___", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("___x____", LineConverter.convertLineToString(flips.a5_e1));
     }
 
     public void testBlackFlipA5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_e1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("____o___");
         flips.a5_d8 = LineConverter.convertStringToLine("____o___");
         flips.a5_h5 = LineConverter.convertStringToLine("o_______");
         flips.a5_e1 = LineConverter.convertStringToLine("___o____");
 
         flips.blackFlipA5();
 
         assertEquals("____x___", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("____x___", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("___x____", LineConverter.convertLineToString(flips.a5_e1));
     }
 
     public void testWhitePlaceA5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_e1));
 
         flips.whitePlaceA5();
 
         assertEquals("____o___", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("____o___", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("___o____", LineConverter.convertLineToString(flips.a5_e1));
     }
 
     public void testWhiteFlipA5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_e1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("____x___");
         flips.a5_d8 = LineConverter.convertStringToLine("____x___");
         flips.a5_h5 = LineConverter.convertStringToLine("x_______");
         flips.a5_e1 = LineConverter.convertStringToLine("___x____");
 
         flips.whiteFlipA5();
 
         assertEquals("____o___", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("____o___", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("___o____", LineConverter.convertLineToString(flips.a5_e1));
     }
 
     public void testBlackPlaceA6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.blackPlaceA6();
 
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testBlackFlipA6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_____o__");
         flips.a6_c8 = LineConverter.convertStringToLine("_____o__");
         flips.a6_h6 = LineConverter.convertStringToLine("o_______");
         flips.a6_f1 = LineConverter.convertStringToLine("__o_____");
 
         flips.blackFlipA6();
 
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testWhitePlaceA6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.whitePlaceA6();
 
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testWhiteFlipA6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_____x__");
         flips.a6_c8 = LineConverter.convertStringToLine("_____x__");
         flips.a6_h6 = LineConverter.convertStringToLine("x_______");
         flips.a6_f1 = LineConverter.convertStringToLine("__x_____");
 
         flips.whiteFlipA6();
 
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testBlackPlaceA7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.blackPlaceA7();
 
         assertEquals("______x_", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testBlackFlipA7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("______o_");
         flips.a7_h7 = LineConverter.convertStringToLine("o_______");
         flips.a7_g1 = LineConverter.convertStringToLine("_o______");
 
         flips.blackFlipA7();
 
         assertEquals("______x_", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testWhitePlaceA7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.whitePlaceA7();
 
         assertEquals("______o_", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testWhiteFlipA7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.a1_a8 = LineConverter.convertStringToLine("______x_");
         flips.a7_h7 = LineConverter.convertStringToLine("x_______");
         flips.a7_g1 = LineConverter.convertStringToLine("_x______");
 
         flips.whiteFlipA7();
 
         assertEquals("______o_", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a7_g1));
     }
 }
