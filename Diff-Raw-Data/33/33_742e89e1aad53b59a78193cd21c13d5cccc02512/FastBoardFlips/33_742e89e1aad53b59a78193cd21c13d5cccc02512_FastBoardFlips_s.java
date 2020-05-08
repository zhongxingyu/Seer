 package fastboard;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: 26-Sep-2009
  * Time: 11:37:03 AM
  * This class handles all the flipping/placing of squares
  */
 public class FastBoardFlips extends FastBoardLines {
     //a1
     public void blackPlaceA1() {
         a1_h1+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_h8+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
     }
 
     public void whitePlaceA1() {
         a1_h1+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_h8+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
     }
 
     //a2
     public void blackPlaceA2() {
         a2_h2+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a2_g8+=1 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3 * 3 * 3 * 3;
     }
 
     public void blackFlipA2() {
         a2_h2-=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a2_g8-=1 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8-=1 * 3 * 3 * 3 * 3 * 3 * 3;
     }
 
     public void whitePlaceA2() {
         a2_h2+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a2_g8+=2 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=2 * 3 * 3 * 3 * 3 * 3 * 3;
     }
 
     public void whiteFlipA2() {
         a2_h2+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a2_g8+=1 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3 * 3 * 3 * 3;
     }
 
     //a3
     public void blackPlaceA3() {
         a3_h3+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a3_f8+=1 * 3 * 3 * 3 * 3 * 3;
         a3_c1+=1 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3 * 3 * 3;
     }
 
     public void blackFlipA3() {
         a3_h3-=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a3_f8-=1 * 3 * 3 * 3 * 3 * 3;
         a3_c1-=1 * 3 * 3;
         a1_a8-=1 * 3 * 3 * 3 * 3 * 3;
     }
 
     public void whitePlaceA3() {
         a3_h3+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a3_f8+=2 * 3 * 3 * 3 * 3 * 3;
         a3_c1+=2 * 3 * 3;
         a1_a8+=2 * 3 * 3 * 3 * 3 * 3;
     }
 
     public void whiteFlipA3() {
         a3_h3+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a3_f8+=1 * 3 * 3 * 3 * 3 * 3;
         a3_c1+=1 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3 * 3 * 3;
     }
 
     //a4
     public void blackPlaceA4() {
         a4_h4+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a4_e8+=1 * 3 * 3 * 3 * 3;
         a4_d1+=1 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3 * 3;
     }
 
     public void blackFlipA4() {
         a4_h4-=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a4_e8-=1 * 3 * 3 * 3 * 3;
         a4_d1-=1 * 3 * 3 * 3;
         a1_a8-=1 * 3 * 3 * 3 * 3;
     }
 
     public void whitePlaceA4() {
         a4_h4+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a4_e8+=2 * 3 * 3 * 3 * 3;
         a4_d1+=2 * 3 * 3 * 3;
         a1_a8+=2 * 3 * 3 * 3 * 3;
     }
 
     public void whiteFlipA4() {
         a4_h4+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a4_e8+=1 * 3 * 3 * 3 * 3;
         a4_d1+=1 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3 * 3;
     }
 
     //a5
     public void blackPlaceA5() {
         a5_h5+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a5_d8+=1 * 3 * 3 * 3;
         a5_e1+=1 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3;
     }
 
     public void blackFlipA5() {
         a5_h5-=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a5_d8-=1 * 3 * 3 * 3;
         a5_e1-=1 * 3 * 3 * 3 * 3;
         a1_a8-=1 * 3 * 3 * 3;
     }
 
     public void whitePlaceA5() {
         a5_h5+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a5_d8+=2 * 3 * 3 * 3;
         a5_e1+=2 * 3 * 3 * 3 * 3;
         a1_a8+=2 * 3 * 3 * 3;
     }
 
     public void whiteFlipA5() {
         a5_h5+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a5_d8+=1 * 3 * 3 * 3;
         a5_e1+=1 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3 * 3;
     }
 
     //a6
     public void blackPlaceA6() {
         a6_h6+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a6_c8+=1 * 3 * 3;
         a6_f1+=1 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3;
     }
 
     public void blackFlipA6() {
         a6_h6-=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a6_c8-=1 * 3 * 3;
         a6_f1-=1 * 3 * 3 * 3 * 3 * 3;
         a1_a8-=1 * 3 * 3;
     }
 
     public void whitePlaceA6() {
         a6_h6+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a6_c8+=2 * 3 * 3;
         a6_f1+=2 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=2 * 3 * 3;
     }
 
     public void whiteFlipA6() {
         a6_h6+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a6_c8+=1 * 3 * 3;
         a6_f1+=1 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3 * 3;
     }
 
     //a7
     public void blackPlaceA7() {
         a7_h7+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a7_g1+=1 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3;
     }
 
     public void blackFlipA7() {
         a7_h7-=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a7_g1-=1 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8-=1 * 3;
     }
 
     public void whitePlaceA7() {
         a7_h7+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a7_g1+=2 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=2 * 3;
     }
 
     public void whiteFlipA7() {
         a7_h7+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a7_g1+=1 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=1 * 3;
     }
 
     //a8
     public void blackPlaceA8() {
         a8_h8+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a8_h1+=1 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
        a1_a8+=1 * 3;
     }
 
     public void whitePlaceA8() {
         a8_h8+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a8_h1+=2 * 3 * 3 * 3 * 3 * 3 * 3 * 3;
         a1_a8+=2;
     }
 
     //h8
     public void blackPlaceH8() {
         a8_h8+=1;
         a1_h8+=1;
         h1_h8+=1;
     }
 
     public void whitePlaceH8() {
         a8_h8+=2;
         a1_h8+=2;
         h1_h8+=2;
     }
 }
