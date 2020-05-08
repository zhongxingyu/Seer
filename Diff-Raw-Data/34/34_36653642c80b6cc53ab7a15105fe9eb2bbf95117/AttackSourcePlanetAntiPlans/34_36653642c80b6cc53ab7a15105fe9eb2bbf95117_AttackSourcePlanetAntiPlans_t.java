 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by Silvestrov Ilya
  * Date: Sep 25, 2010
  * Time: 8:24:38 AM
  */
 public class AttackSourcePlanetAntiPlans {
     public static List<List<SquareMatrix>> attackSourcePlanetAntiPlans(
             PlanetWarsState state, List<SquareMatrix> plan) {
         SquareMatrix firstTurn = plan.get(0);
 
         List<List<SquareMatrix>> plans = new ArrayList<List<SquareMatrix>>();
 
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
         for (int i = 0; i < firstTurn.size(); ++i) {
             for (int j = 0; j < firstTurn.size(); ++j) {
                 if (firstTurn.get(i, j) > 0) {
                     for (int k = 0; k < planets.length; ++k) {
                         if (owners[k] == -1 && k != i && k != j) {
                             if (Log.isEnabled()) {
                                 StringBuilder sb = new StringBuilder();
                                 sb.append("Attack source: from ").append(k).append("(").append(planets[k]).append(") to ");
                                 sb.append(i).append("(").append(planets[i]).append(")");
                                 sb.append(", distance ").append(MyBot.distances[k][i]);
                                 sb.append(", victim growth ").append(MyBot.growth[i]).append(". ");
                                 sb.append("For transition of size ").append(firstTurn.get(i, j)).
                                        append(" from ").append(i).append("(").append(planets[i]).append(")").
                                         append(" to ").append(j).append("(").append(planets[j]).append(")");
                                 Log.log(sb.toString());
                             }
                             int requiredNumShips = MyBot.requiredNumShipsForAttackAntiPlan(state, k, i);
                             if (Log.isEnabled()) {
                                 StringBuilder sb = new StringBuilder("Required num ships by growth and arrivals: ");
                                 sb.append(requiredNumShips);
                                 Log.log(sb.toString());
                             }
                             requiredNumShips -= firstTurn.get(i, j);
                             if (Log.isEnabled()) {
                                 StringBuilder sb = new StringBuilder("With transition: ");
                                 sb.append(requiredNumShips);
                                 Log.log(sb.toString());
                             }
                             requiredNumShips += MyBot.calculateAroundShips(state, k, i);
                             if (Log.isEnabled()) {
                                 StringBuilder sb = new StringBuilder("With ships around: ");
                                 sb.append(requiredNumShips);
                                 Log.log(sb.toString());
                             }
                            if (-planets[k] >= requiredNumShips && requiredNumShips > 0) {
                                 SquareMatrix antiTurn = new SquareMatrix(firstTurn.size());
                                 List<SquareMatrix> antiPlan = new LinkedList<SquareMatrix>();
                                 antiPlan.add(antiTurn);
                                 antiTurn.set(k, i, -requiredNumShips);
                                 if (Log.isEnabled()) {
                                     StringBuilder sb = new StringBuilder("Added source attack of size ").
                                             append(requiredNumShips);
                                     Log.log(sb.toString());
                                 }
                                 plans.add(antiPlan);
                             }
                         }
                     }
                 }
             }
         }
 
         return plans;
     }
 }
