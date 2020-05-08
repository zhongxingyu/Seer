 package org.clafer.compiler;
 
 import org.clafer.common.Check;
 import org.clafer.collection.Pair;
 import org.clafer.instance.InstanceModel;
 import solver.ResolutionPolicy;
 import solver.Solver;
 import solver.search.measure.IMeasures;
 import solver.variables.IntVar;
 import util.ESat;
 
 /**
  *
  * @author jimmy
  */
 public class ClaferObjective {
 
     public final Solver solver;
     private final ClaferSolutionMap solutionMap;
     private final Objective objective;
     private final IntVar score;
 
     ClaferObjective(Solver solver, ClaferSolutionMap solutionMap, Objective objective, IntVar score) {
         this.solver = Check.notNull(solver);
         this.solutionMap = Check.notNull(solutionMap);
         this.objective = Check.notNull(objective);
         this.score = Check.notNull(score);
     }
 
     public Solver getInternalSolver() {
         return solver;
     }
 
     public IMeasures getMeasures() {
         return solver.getMeasures();
     }
 
     public Objective getObjective() {
         return objective;
     }
 
     public Pair<Integer, InstanceModel> optimal() {
        int[] opt = solver.findOptimalSolution(objective.getPolicy(), score);
         return ESat.TRUE.equals(solver.isFeasible())
                ? new Pair<Integer, InstanceModel>(
                (Objective.Minimize.equals(objective) ? opt[1] : opt[0]), solutionMap.getInstance())
                 : null;
     }
 
     public static enum Objective {
 
         Maximize(ResolutionPolicy.MAXIMIZE),
         Minimize(ResolutionPolicy.MINIMIZE);
         private final ResolutionPolicy policy;
 
         Objective(ResolutionPolicy policy) {
             this.policy = policy;
         }
 
         ResolutionPolicy getPolicy() {
             return policy;
         }
     }
 }
