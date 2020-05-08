 package com.mebigfatguy.polycasso;
 
 /**
  * a default implementation of a score for the error in a image against a target image
  * this score maintains a grid of scores representing scores in sections of the image, and
  * then rolls up these scores to an overall score.
  */
 public class DefaultScore implements Score {
 
     private static final long serialVersionUID = 2603006530810631094L;
     static final int NUM_DIVISIONS = 8;
 
     /**
      * a worst case score
      */
     public static final DefaultScore MAX_SCORE = new DefaultScore(Long.MAX_VALUE);
 
     long gridScores[][] = new long[NUM_DIVISIONS][NUM_DIVISIONS];
     long overallScore;
 
     /**
      * constructs an empty score
      */
     public DefaultScore() {
         overallScore = 0;
     }
 
     /**
      * constructs a score with the specified delta, spreads the score across all grids
      * 
      * @param delta the delta score
      */
     public DefaultScore(long delta) {
         overallScore = delta;
 
         long divider = NUM_DIVISIONS * NUM_DIVISIONS;
         long gridScore = overallScore / divider;
 
         for (int y = 0; y < NUM_DIVISIONS; y++) {
             for (int x = 0; x < NUM_DIVISIONS; x++) {
                 gridScores[x][y] = gridScore;
             }
         }
     }
 
     /**
      * returns the sum of the square of pixel errors
      * @return the delta between a generate image and the target
      */
     @Override
     public long getDelta() {
         return overallScore;
     }
 
     /**
     * compares this score to another
      * 
      * @param o the score to compare to
      * @return whether the two scores have the same delta
      */
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof DefaultScore)) {
             return false;
         }
 
         return getDelta() == ((DefaultScore) o).getDelta();
     }
 
     /**
      * clones this object
      * 
      * @return a clone
      */
     @Override
     public Object clone() {
         DefaultScore clonedScore;
 
         try {
             clonedScore = (DefaultScore) super.clone();
         } catch (CloneNotSupportedException cnse) {
             clonedScore = new DefaultScore();
             clonedScore.overallScore = overallScore;
         }
 
         clonedScore.gridScores = new long[NUM_DIVISIONS][NUM_DIVISIONS];
         for (int i = 0; i < NUM_DIVISIONS; i++) {
             System.arraycopy(gridScores[i], 0, clonedScore.gridScores[i], 0, NUM_DIVISIONS);
         }
 
         return clonedScore;
     }
 
     /**
      * generates a hash code for this score
      * 
      * @return the hash code of this score
      */
     @Override
     public int hashCode() {
         return (int) overallScore;
     }
 
     /**
      * returns a string representation of the score
      * 
      * @return the score as a string
      */
     @Override
     public String toString() {
         return String.valueOf(overallScore);
     }
 }
