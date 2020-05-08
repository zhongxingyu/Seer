 package org.clafer.choco.constraint;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import solver.exception.ContradictionException;
 import solver.search.strategy.assignments.DecisionOperator;
 import solver.search.strategy.decision.Decision;
 import solver.search.strategy.decision.fast.FastDecisionSet;
 import solver.search.strategy.strategy.AbstractStrategy;
 import solver.variables.SetVar;
 import util.PoolManager;
 
 /**
  * Does random things to make propogation harder.
  */
 public class RandomSetSearchStrategy extends AbstractStrategy<SetVar> {
 
     private final Random rand = new Random();
     private final PoolManager<FastDecisionSet> pool = new PoolManager<>();
 
     public RandomSetSearchStrategy(SetVar[] variables) {
         super(variables);
     }
 
     @Override
     public void init() throws ContradictionException {
     }
 
     @Override
     public Decision<SetVar> getDecision() {
         List<SetVar> eligible = new ArrayList<>();
         for (SetVar var : vars) {
             if (!var.instantiated()) {
                 eligible.add(var);
             }
         }
         if (eligible.isEmpty()) {
             return null;
         }
         SetVar s = eligible.get(rand.nextInt(eligible.size()));
         return computeDecision(s);
     }
 
     @Override
     public Decision<SetVar> computeDecision(SetVar s) {
        int m = rand.nextInt(s.getEnvelopeSize() - s.getKernelSize());
         for (int i = s.getEnvelopeFirst(); i != SetVar.END; i = s.getEnvelopeNext()) {
             if (!s.kernelContains(i)) {
                 if (m == 0) {
                     FastDecisionSet d = pool.getE();
                     if (d == null) {
                         d = new FastDecisionSet(pool);
                     }
                     d.set(s, i, DecisionOperator.set_force);
                     return d;
                 }
                m--;
             }
         }
         throw new Error();
     }
 }
