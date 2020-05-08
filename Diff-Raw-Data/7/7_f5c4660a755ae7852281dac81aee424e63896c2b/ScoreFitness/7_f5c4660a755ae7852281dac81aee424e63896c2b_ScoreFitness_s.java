 package game.fitness;
 
 import game.Scoring;
 import game.State;
 import game.ai.Fitness;
 
 /**
  * Evaluates how many points we already secured in a state.
  * 
  * @author Tillmann Rendel
  */
 
 public class ScoreFitness implements Fitness {
   @Override
   public int evaluate(State state) {
     int maximalScore = Scoring.totalScore(0, state.lambdasLeft + state.collectedLambdas, false, true);
     // TODO Overflow!
     return state.score * 1000000 / maximalScore;
   }
 }
