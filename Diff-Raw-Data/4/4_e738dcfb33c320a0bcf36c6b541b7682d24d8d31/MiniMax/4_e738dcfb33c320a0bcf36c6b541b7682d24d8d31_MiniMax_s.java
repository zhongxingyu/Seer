 package core.ai.search;
 
 import core.ai.Action;
 import core.ai.Enviroment;
 import core.ai.Heuristic;
 import core.ai.State;
 import java.util.List;
 
 public class MiniMax {
 
     private Heuristic heuristic;
     private Enviroment enviroment;
 
     public MiniMax(Heuristic heuristic, Enviroment enviroment) {
         this.heuristic = heuristic;
         this.enviroment = enviroment;
     }
 
     public double minimax(State state, int depth, int maxDepth) {
         double miniMaxValue = 0;
         if (depth == maxDepth)
             return heuristic.evaluate(state);
         List<Action> applicableActions = enviroment.getApplicableActions(state);
         for (int i = 0; i < applicableActions.size(); i++) {
             State childState = applicableActions.get(i).execute(state);
             double childValue = minimax(childState, ++depth, maxDepth);
             if (i == 0) miniMaxValue = childValue;
             else if (isMaxTurn(depth))
                 miniMaxValue = Math.max(miniMaxValue, childValue);
            else miniMaxValue = Math.max(miniMaxValue, childValue);
         }
         return miniMaxValue;
     }
 
     private boolean isMaxTurn(int depth) {
         return (depth % 2 == 0);
     }
 }
