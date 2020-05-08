 package se.citerus.crazysnake.brain;
 
 import se.citerus.crazysnake.*;
 
 import static se.citerus.crazysnake.Direction.*;
 import static se.citerus.crazysnake.Movement.FORWARD;
 
 /**
  * Base brain for one track-minds.
  */
 public abstract class PoliticalBrain extends BaseBrain {
 
     private static final int LOOK_AHEAD = 2;
 
     private StringBuffer memory = new StringBuffer();
 
     public abstract Movement getDodgeProblemsAheadDirection();
 
     @Override
    public Movement getNextMove(HeatState state) {
         final Movement movement = calculateNextMove(state);
         memory.append(movement.name()).append(" - ");
         return movement;
     }
 
    private Movement calculateNextMove(HeatState state) {
         try {
             Snake me = state.getSnake(getName());
 
             Position headPosition = me.getHeadPosition();
             Direction direction = me.getDirection();
 
             Square[] lookAheadArr = new Square[LOOK_AHEAD];
             int x = headPosition.getX();
             int y = headPosition.getY();
 
             if (direction == EAST) {
                 for (int i = 1; i <= LOOK_AHEAD; i++)
                     lookAheadArr[i - 1] = state.getSquare(x + i, y);
             } else if (direction == WEST) {
                 for (int i = 1; i <= LOOK_AHEAD; i++)
                     lookAheadArr[i - 1] = state.getSquare(x - i, y);
             } else if (direction == NORTH) {
                 for (int i = 1; i <= LOOK_AHEAD; i++)
                     lookAheadArr[i - 1] = state.getSquare(x, y - i);
             } else {
                 for (int i = 1; i <= LOOK_AHEAD; i++)
                     lookAheadArr[i - 1] = state.getSquare(x, y + i);
             }
 
             return anyIsOccupied(lookAheadArr) ? getDodgeProblemsAheadDirection() : FORWARD;
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private boolean anyIsOccupied(Square[] arr) {
         for (Square square : arr) {
             if (!square.isUnoccupied()) return true;
         }
         return false;
     }
 
     @Override
     public String toString() {
         return getClass().getSimpleName() + "{" + "memory=" + memory + '}';
     }
 }
