 package game.strategy;
 
 import game.Cell;
 import game.Command;
 import game.State;
 import game.ai.Strategy;
 import game.strategy.tom.Helpers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Walk to closest beard and shave
  * @author Felix Rieger
  *
  */
 public class ClosestWalkBeard extends Strategy {
 
   public ClosestWalkBeard() {
   }
 
   @Override
   public List<Command> apply(State s) {
    List<Command> ret = new ArrayList<Command>(Helpers.moveToCell(s, Cell.Beard, 1));
    ret.add(Command.Shave);
    return ret;
   }
 
   @Override
   public boolean wantsToApply(State s) {
     return s.board.razors != 0 && !s.board.bitsets[Cell.Beard.ordinal()].isEmpty();
   }
 
   @Override
   public String toString() {
     return "ClosestWalkBeard";
   }
 }
