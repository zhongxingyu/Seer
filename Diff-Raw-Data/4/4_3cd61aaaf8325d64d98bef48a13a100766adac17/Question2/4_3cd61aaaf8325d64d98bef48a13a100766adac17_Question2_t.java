 package algorithm.cc150.chapter18;
 
 import java.util.Random;
 
 /**
  * Implement the perfect random shuffle of the (52) cards. 
  *
  */
 public class Question2 {
 
   public void perfectShuffle(int[] cards) {
     // write implementation here
     Random rnd = new Random();
    for (int i = 1; i < cards.length; ++i) {
      int idx = rnd.nextInt(i);
       int tmp = cards[idx];
       cards[idx] = cards[i];
       cards[i] = tmp;
     }
   }
   
 }
