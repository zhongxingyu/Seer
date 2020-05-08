 import java.util.*;
 
 public class TerminationConditions {
 
   public static abstract class TerminationCondition {
     private PriorityQueue<BoardState> finalCandidates;
     private Board finalBoard;
     private BoardState finalLastMove;
 
     public boolean isTrue(PriorityQueue<BoardState> candidates, Board board,
                           BoardState lastMove) {
      if (concreteIsTrue(candidates, board, lastMove)) {
         finalCandidates = candidates;
         finalBoard = board;
         finalLastMove = lastMove;
         return true;
       }
 
       return false;
     }
 
     protected abstract boolean concreteIsTrue(PriorityQueue<BoardState> candidates,
                                               Board board, BoardState lastMove);
 
     public Path getPath() {
       Path path = new Path();
       for (BoardState cur = finalLastMove; cur != null; cur = cur.parentState) {
         path.addPosition(cur.position);
       }
       path.reverse();
       return path;
     }
 
     public Board getBoard() {
       return finalBoard;
     }
   }
 
   public static class PointTermination extends TerminationCondition {
     Point destination;
 
     public PointTermination(Point initDestination) {
       destination = initDestination;
     }
 
     protected boolean concreteIsTrue(PriorityQueue<BoardState> candidates, Board board, BoardState lastMove) {
       return board.robot.getPosition().equals(destination);
     }
   }
 }
