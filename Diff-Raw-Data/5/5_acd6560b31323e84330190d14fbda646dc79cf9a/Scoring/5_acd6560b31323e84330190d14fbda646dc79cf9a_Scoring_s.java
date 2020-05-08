 package game.util;
 
 import game.State;
 
 /**
  * @author seba
  */
 public class Scoring {
   public static int totalScore(int steps, int collectedLambdas, boolean abort, boolean win) {
     return -steps + collectedLambdas * (win ? 75 : (abort ? 50 : 25));
   }
 
   /**
    * An upper bound on the score we can get for an optimal strategy, starting
    * from the initial state.
    * 
    * <p>
    * The {@code state} parameter is only used to learn how many lambdas there
    * are.
    */
   public static int maximalScore(State state) {
     return Scoring.totalScore(0, state.lambdaPositions.size() + state.collectedLambdas, false, true);
   }
   
   /**
    * Just like {@item maximalScore(State)} but without reaching the lift.
    */
   public static int maximalScoreWithoutLift(State state) {
    return Scoring.totalScore(0, state.lambdaPositions.size() + state.collectedLambdas, false, false);
   }
 
   /**
    * An upper bound on the score we can get starting from the given state.
    */
   public static int maximalReachableScore(State state) {
     return Scoring.totalScore(state.steps, state.lambdaPositions.size() + state.collectedLambdas, false, true);
   }
   
   /**
    * Just like {@item maximalReachableScore(State)} but without reaching the lift.
    */
   public static int maximalReachableScoreWithoutLift(State state) {
    return Scoring.totalScore(state.steps, state.lambdaPositions.size() + state.collectedLambdas, false, false);
   }
 }
 
 
