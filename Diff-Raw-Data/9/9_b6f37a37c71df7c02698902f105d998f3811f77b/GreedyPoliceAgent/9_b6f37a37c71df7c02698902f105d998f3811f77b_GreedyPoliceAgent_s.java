 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package RSLBench.Algorithms.Greedy;
 
 import RSLBench.Assignment.DCOP.DefaultDCOPAgent;
 import RSLBench.Helpers.Utility.ProblemDefinition;
 import rescuecore2.worldmodel.EntityID;
 
 /**
  * Agent that picks whatever fire is best for him, disregarding any others.
  * <p/>
  * Keep in mind that this initial assignment can be optimized by the sequential
  * greedy deconflicting procedure.
  *
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class GreedyPoliceAgent extends DefaultDCOPAgent {
 
     @Override
     public boolean improveAssignment() {
         final ProblemDefinition problem = getProblem();
         final EntityID id = getID();
 
         double best = Double.NEGATIVE_INFINITY;
         for (EntityID target : problem.getPoliceAgentNeighbors(getID())) {
             double value = problem.getPoliceUtility(id, target);
             if (value > best) {
                 best = value;
                 setTarget(target);
             }
         }
 
        // This can happen if we have no neighbors
        if (getTarget() == null) {
            setTarget(problem.getHighestTargetForAgent(id));
        }

         return false;
     }
 
 }
