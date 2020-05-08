 package balle.strategy.planner;
 
 import java.awt.Color;
 
 import org.apache.log4j.Logger;
 
 import balle.strategy.executor.movement.MovementExecutor;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Snapshot;
 import balle.world.objects.Goal;
 import balle.world.objects.Point;
 import balle.world.objects.Robot;
 import balle.world.objects.StaticFieldObject;
 
 /**
  * The Class DefensiveStrategy. Controls the robot so it gets in between
  * opponent and our own goal and stays there.
  */
 public class DefensiveStrategy extends GoToBall {
     private final static Logger LOG = Logger.getLogger(DefensiveStrategy.class);
 
     public DefensiveStrategy(MovementExecutor movementExecutor) {
         super(movementExecutor);
     }
 
     protected Coord calculateDefenceCoord() {
         Snapshot snapshot = getSnapshot();
         Goal ownGoal = snapshot.getOwnGoal();
 
         Robot opponent = snapshot.getOpponent();
         Robot our = snapshot.getBalle();
         if (opponent.getPosition() == null)
             return null;
 
         Line defenceLine = new Line(opponent.getPosition(), ownGoal.getGoalLine().midpoint());
 
         // TODO: change this to point of intersection.
         return defenceLine.midpoint();
     }
 
     @Override
     protected StaticFieldObject getTarget() {
         return new Point(calculateDefenceCoord());
     }
 
     @Override
     protected Color getTargetColor() {
         return Color.PINK;
     }
 
 }
