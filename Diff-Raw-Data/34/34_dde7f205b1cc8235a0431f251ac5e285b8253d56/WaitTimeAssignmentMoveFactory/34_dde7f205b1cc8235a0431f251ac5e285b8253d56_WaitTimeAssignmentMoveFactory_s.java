 package org.drools.planner.examples.ras2012.move;
 
 import java.util.ArrayList;
import java.util.LinkedList;
 import java.util.List;
 
 import org.drools.planner.core.move.Move;
 import org.drools.planner.core.move.factory.AbstractMoveFactory;
 import org.drools.planner.core.solution.Solution;
 import org.drools.planner.examples.ras2012.RAS2012Solution;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.WaitTime;
 import org.drools.planner.examples.ras2012.model.planner.ItineraryAssignment;
 
 public class WaitTimeAssignmentMoveFactory extends AbstractMoveFactory {
 
     @Override
     public List<Move> createMoveList(@SuppressWarnings("rawtypes") final Solution solution) {
        // prepare the various delays
         // TODO estimate maximum necessary wait time from the longest arc and slowest train
        List<WaitTime> wts = new LinkedList<WaitTime>();
        wts.add(null);
        wts.add(WaitTime.getWaitTime(1));
        wts.add(WaitTime.getWaitTime(2));
        wts.add(WaitTime.getWaitTime(3));
        wts.add(WaitTime.getWaitTime(4));
        wts.add(WaitTime.getWaitTime(5));
        wts.add(WaitTime.getWaitTime(10));
        wts.add(WaitTime.getWaitTime(20));
        wts.add(WaitTime.getWaitTime(40));
         final List<Move> moves = new ArrayList<Move>();
         final RAS2012Solution sol = (RAS2012Solution) solution;
         for (final ItineraryAssignment ia : sol.getAssignments()) {
             for (final Node waitPoint : ia.getRoute().getWaitPoints()) {
                for (WaitTime waitTime : wts) {
                    moves.add(new WaitTimeAssignmentMove(ia, waitPoint, waitTime));
                 }
             }
         }
         return moves;
     }
 
 }
