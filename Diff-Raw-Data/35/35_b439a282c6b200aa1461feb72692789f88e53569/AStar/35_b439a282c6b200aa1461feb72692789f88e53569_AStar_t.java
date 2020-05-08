 /* -*- c-basic-offset: 2; -*- */
 
 import java.util.*;
 
 public class AStar {
 
   public static final int INITIAL_PQ_CAPACITY = 59;
   protected CostFunction g, h;
   protected TerminationConditions.TerminationCondition termCond;
 
   private PriorityQueue<BoardState> candidates;
 
   private long timeout = 0;
 
   protected boolean halt = false;
 
   public AStar(final CostFunction g, final CostFunction h, final TerminationConditions.TerminationCondition termCond) {
     this.g = g;
     this.h = h;
     this.termCond = termCond;
     // init PriorityQueue
     candidates = new PriorityQueue<BoardState>(
       INITIAL_PQ_CAPACITY, new Comparator<BoardState>() {
         @Override
         public int compare(final BoardState s, final BoardState t) {
           if (s.score == t.score) return 0;
           else return (s.score < t.score) ? -1 : 1;
         }
       });
   }
 
   public void setTimeout(long ms) {
     timeout = ms;
   }
 
   class Timeout extends TimerTask {
     private AStar parent;
     public Timeout(AStar parent) {
       this.parent = parent;
     }
 
     public void run() {
       parent.timesUp();
     }
   }
 
   protected void timesUp() {
     halt = true;
   }
 
   /*
    * return false if terminated exceptionally, true otherwise
    */
   public boolean findPath(final Board board, final Point destination) {
     halt = false;
     Timer timer = null;
     if (timeout != 0) {
       timer = new Timer();
       timer.schedule(new Timeout(this), timeout);
     }
 
     candidates.clear();
     candidates.add(board.getBoardState());
     final Point origin = board.getRobotPosition();
 
     while (true) {
       if (Main.gotSIGINT || halt) {
         if (timer != null)
           timer.cancel();
         return false;
       }
       // A* main
       BoardState curState = candidates.poll();
       Board curBoard = curState.board;
 
       // System.err.println(curState.move);
       // System.err.println(curBoard);
       // System.err.println(curBoard.getAvailableMoves());
 
      if (curBoard.state != Board.GameState.Lose) {
        
        for (Robot.Move candMove : curBoard.getAvailableMoves()) {
          BoardState newState = curState.board.getBoardState();
          Board newBoard = newState.board;
          Point newPosition = newBoard.getRobotPosition();
          newState.parentState = curState;
          newState.copyVisits(curState);
          newState.move = candMove;
          newBoard.tick(candMove);
          newState.visits[newBoard.getRobotPosition().r][newBoard.getRobotPosition().c]++;
          newState.score =
            g.compute(newState, origin, newPosition) +
            h.compute(newState, newPosition, destination);
          candidates.add(newState);

          // System.err.println(candMove + ": " + newState.score);
        }
       }
       // System.err.println();
 
       // Termination condition
       if (termCond.isTrue(candidates, curState)) break;
     }
 
     if (timer != null)
       timer.cancel();
     return true;
   }
 
   /**
    * Interface for defining a cost function, e.g., g and h.
    */
   public interface CostFunction {
     public double compute(BoardState board, Point start, Point end);
   }
 }
