 package fruit.g4;
 
 import java.util.*;
 
 public class Player extends fruit.sim.Player
 {
   private float[] prefs;
   private float[] platter;
   private float numFruits = 0;
   private float bowlsRemaining;
   private float totalNumBowls;
 
   public void init(int nplayers, int[] pref) {
     prefs = Vectors.castToFloatArray(pref);
     platter = new float[pref.length];
     bowlsRemaining = (float)(nplayers - getIndex());
     totalNumBowls = bowlsRemaining;
     System.out.println(getIndex());
   }
 
   public boolean pass(int[] bowl, int bowlId, int round, boolean canPick, boolean mustTake) {
     if (!canPick){
       return false;
     }
 
     System.out.println("Number of bowls that will pass: " + totalNumBowls);
     System.out.println("Number of bowls remaining: " + bowlsRemaining);
     float[] currentBowl = Vectors.castToFloatArray(bowl);
     float score = score(currentBowl);
 
     numFruits = Vectors.sum(currentBowl);
 
     float[] uniformBowl = new float[currentBowl.length];
     for (int i = 0 ; i < bowl.length; i++){
       uniformBowl[i] = numFruits / bowl.length;
     }
     float uniformScore = score(uniformBowl);
 
     System.out.println("Uniform Score: " + uniformScore);
     System.out.println("Score: " + score);
     bowlsRemaining--;
    return uniformScore < score;
   }
 
   private boolean shouldTakeBasedOnScore(float currentScore, float mle){
     // based on number of bowls remaining to pass you, decide if you should take
     if (currentScore < mle) return false;
 
     float diff = maxScore() - mle;
    return currentScore > (0.5f * diff * (totalNumBowls / bowlsRemaining)) + mle;
   }
 
   private float maxScore(){
     return numFruits * 12;
   }
 
   private float score(float[] bowl){
     return Vectors.dot(bowl, prefs);
   }
 
   private Random random = new Random();
 }
