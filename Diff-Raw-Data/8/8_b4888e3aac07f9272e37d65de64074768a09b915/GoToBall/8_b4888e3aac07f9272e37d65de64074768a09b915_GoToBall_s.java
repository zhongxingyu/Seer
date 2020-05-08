 /**
  * 
  */
 package balle.strategy.planner;
 
 import java.awt.Color;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.drawable.Dot;
 import balle.strategy.FactoryMethod;
 import balle.strategy.executor.movement.GoToObjectPFN;
 import balle.strategy.executor.movement.MovementExecutor;
 import balle.world.Coord;
 import balle.world.Snapshot;
 import balle.world.objects.FieldObject;
 import balle.world.objects.Pitch;
 import balle.world.objects.Point;
 import balle.world.objects.Robot;
 
 /**
  * @author s0909773
  * 
  */
 public class GoToBall extends AbstractPlanner {
 
     protected static final Logger LOG = Logger.getLogger(GoToBall.class);
 
     MovementExecutor executorStrategy;
 
     private final double AVOIDANCE_GAP; // Meters
     private final double OVERSHOOT_GAP; // Meters
     private static final double DEFAULT_AVOIDANCE_GAP = 0.5;
     private static final double DEFAULT_OVERSHOOT_GAP = 0.7;
 	private static final double DIST_DIFF_THRESHOLD = 0.2; // Meters
 
 	private boolean approachTargetFromCorrectSide;
 	private boolean shouldAvoidOpponent = true;
 
     public GoToBall(MovementExecutor movementExecutor) {
         this(movementExecutor, DEFAULT_AVOIDANCE_GAP, DEFAULT_OVERSHOOT_GAP,
                 false);
     }
 
     /**
      * Instantiates a new go to ball strategy.
      * 
      * @param movementExecutor
      *            the movement executor
      * @param approachTargetFromCorrectSide
      *            whether to always approach target from correct side
      */
     public GoToBall(MovementExecutor movementExecutor,
             boolean approachTargetFromCorrectSide) {
         this(movementExecutor, DEFAULT_AVOIDANCE_GAP, DEFAULT_OVERSHOOT_GAP,
                 true);
     }
 
     public GoToBall(MovementExecutor movementExecutor, double avoidanceGap,
             double overshootGap, boolean approachfromCorrectSide) {
         executorStrategy = movementExecutor;
         this.approachTargetFromCorrectSide = approachfromCorrectSide;
         AVOIDANCE_GAP = avoidanceGap;
         OVERSHOOT_GAP = overshootGap;
     }
 
     @FactoryMethod(designator = "GoToBall PFN Test", parameterNames = {
             "ApproachFromCorrectSide", "opponentPower",
             "opponentInfluenceDistance", "targetPower", "alpha" })
     public static GoToBall pfnTestFactory(boolean approachFromCorrectSide,
             double opponentPower, double opponentInfluenceDistance,
             double targetPower, double alpha) {
         return new GoToBall(new GoToObjectPFN(0, true, opponentPower,
                 opponentInfluenceDistance, targetPower, alpha),
                 DEFAULT_AVOIDANCE_GAP,
                 DEFAULT_OVERSHOOT_GAP, approachFromCorrectSide);
     }
     public void setStopDistance(double distance) {
         executorStrategy.setStopDistance(distance);
     }
     public MovementExecutor getExecutorStrategy() {
         return executorStrategy;
     }
 
     public void setExecutorStrategy(MovementExecutor executorStrategy) {
         this.executorStrategy = executorStrategy;
     }
 
 
 
     public boolean shouldApproachTargetFromCorrectSide() {
         return approachTargetFromCorrectSide;
     }
 
     public void setApproachTargetFromCorrectSide(
             boolean approachTargetFromCorrectSide) {
         this.approachTargetFromCorrectSide = approachTargetFromCorrectSide;
     }
     
 	public boolean shouldAvoidOpponent() {
     	return this.shouldAvoidOpponent;
     }
     
 	public void setShouldAvoidOpponent(boolean shouldAvoidOpponent) {
 		this.shouldAvoidOpponent = shouldAvoidOpponent;
     }
 
     protected FieldObject getTarget(Snapshot snapshot) {
 		return snapshot.getBall();
     }
 
     protected Color getTargetColor() {
         return Color.CYAN;
     }
 
 	protected Coord calculateAvoidanceCoord(double gap, boolean belowPoint,
 			Snapshot snapshot) {
         int side = 1;
         if (belowPoint) {
             side = -1;
         }
 
 		Robot robot = snapshot.getBalle();
 		Coord point = snapshot.getOpponent().getPosition();
 
         // Gets the angle and distance between the robot and the ball
         double robotObstacleAngle = point.sub(robot.getPosition())
                 .orientation().atan2styleradians();
         double robotObstacleDistance = point.dist(robot.getPosition());
         // Calculate the distance between the robot and the destination point
         double hyp = Math.sqrt((robotObstacleDistance * robotObstacleDistance)
                 + (gap * gap));
 
         // Calculate the angle between the robot and the destination point
         double robotPointAngle = Math.asin(gap / hyp);
         // Calculate the angle between the robot and the destination point.
         // Side is -1 if robot is below the ball, so will get the angle needed
         // for a point
         // below the ball, whereas side = 1 will give a point above the ball
         double angle = robotObstacleAngle + (side * robotPointAngle);
 
         // Offsets are in relation to the robot
         double xOffset = hyp * Math.cos(angle);
         double yOffset = hyp * Math.sin(angle);
 
         return new Coord(robot.getPosition().getX() + xOffset, robot
                 .getPosition().getY() + yOffset);
     }
 
     protected Coord calculateOvershootCoord(FieldObject target,
 			double gap, boolean belowPoint, Snapshot snapshot) {
         int side = 1;
         if (belowPoint) {
             side = -1;
         }
 
 		Robot robot = snapshot.getBalle();
 		Coord point = snapshot.getBall().getPosition();
 
         // Gets the angle and distance between the robot and the ball
         double robotTargetOrientation = point.sub(robot.getPosition())
                 .orientation().atan2styleradians();
         double robotTargetDistance = point.dist(robot.getPosition());
         // Calculate the distance between the robot and the destination point
         double hyp = Math.sqrt((robotTargetDistance * robotTargetDistance)
                 + (gap * gap));
 
         // Calculate the angle between the robot and the destination point
         double robotPointAngle = Math.asin(gap / hyp);
         // Calculate the angle between the robot and the destination point.
         // Side is -1 if robot is below the ball, so will get the angle needed
         // for a point
         // below the ball, whereas side = 1 will give a point above the ball
         double angle = robotTargetOrientation + (side * robotPointAngle);
 
         // Offsets are in relation to the robot
         double xOffset = hyp * Math.cos(angle);
         double yOffset = hyp * Math.sin(angle);
 
         return new Coord(robot.getPosition().getX() + xOffset, robot
                 .getPosition().getY() + yOffset);
     }
 
     /**
      * Returns the target location the robot should go to in order to overshoot
      * the target and face the opponents goal from correct angle.
      * 
      * @param target
      *            the target
      * @param overshootGap
      *            the overshoot gap
      * @return the overshoot target
      */
     protected FieldObject getOvershootTarget(FieldObject target,
 			double overshootGap, Snapshot snapshot) {
         // End case for recursive search for overshoot target
         if (overshootGap < 0.1)
             return target;
 
         boolean belowBall = true;
 		Robot robot = snapshot.getBalle();
 		Pitch pitch = snapshot.getPitch();
 
         if (robot.getPosition().getY() > target.getPosition().getY()) {
             belowBall = false;
         }
 
 		if (snapshot.getOpponentsGoal().isRightGoal())
             belowBall = !belowBall;
 
         Coord overshootCoord = calculateOvershootCoord(target, overshootGap,
 				belowBall, snapshot);
 
         // If the point is in the pitch
         if (pitch.containsCoord(overshootCoord)) {
             return new Point(overshootCoord);
 
         }
         // If its not in the pitch, pick a new one
         // DOESN't really work :(
 
         // overshootCoord = calculateOvershootCoord(target, OVERSHOOT_GAP,
         // !belowBall);
         // if (pitch.containsCoord(overshootCoord)) {
         // // Check if it is suitable
         // Coord overshootVector = overshootCoord.sub(target.getPosition());
         // if ((getSnapshot().getOpponentsGoal().isLeftGoal() && overshootVector
         // .orientation().isFacingLeft(0))
         // || (getSnapshot().getOpponentsGoal().isRightGoal() && overshootVector
         // .orientation().isFacingRight(0))) {
         // // Return it as a new target
         // return new Point(overshootCoord);
         // }
         // }
         // If the target is *still* not suitable, go to the original target at
         // least
         // Recurse with smaller gap
 		return getOvershootTarget(target, overshootGap / 2.0, snapshot);
     }
 
 	protected Point getAvoidanceTarget(Snapshot snapshot) {
 		Coord pointAbove = calculateAvoidanceCoord(AVOIDANCE_GAP, true,
 				snapshot);
 		Coord pointBelow = calculateAvoidanceCoord(AVOIDANCE_GAP, false,
 				snapshot);
 		Pitch pitch = snapshot.getPitch();
 
 		Coord currentPosition = snapshot.getBalle().getPosition();
         if (pitch.containsCoord(pointAbove) && pitch.containsCoord(pointBelow)) {
             // If both points happen to be in the pitch, return the closest one
             double distToPointAbove = currentPosition.dist(pointAbove);
             double distToPointBelow = currentPosition.dist(pointBelow);
             double distDiff = Math.abs(distToPointAbove - distToPointBelow);
 
             // If distances differ by much:
             if (distDiff > DIST_DIFF_THRESHOLD) {
                 // Pick the shorter one
                 if (distToPointAbove < distToPointBelow)
                     return new Point(pointAbove);
                 else
                     return new Point(pointBelow);
             } else {
 				double angleToTurnPointAbove = snapshot.getBalle()
                         .getAngleToTurnToTarget(pointAbove);
 				double angleToTurnPointBelow = snapshot.getBalle()
                         .getAngleToTurnToTarget(pointBelow);
 
                 if (Math.abs(angleToTurnPointAbove) < Math
                         .abs(angleToTurnPointBelow)) {
                     return new Point(pointAbove);
                 } else
                     return new Point(pointBelow);
 
             }
 
         } else if (pitch.containsCoord(pointAbove)) {
             // Else if pitch contains only pointAbove, return it
             return new Point(pointAbove);
         } else
             // if it doesn't contain pointAbove, it should contain pointBelow
             return new Point(pointBelow);
         // TODO: what happens if it does not contain both points?
         // (it will return pointBelow now, but some other behaviour might be
         // desired)
 
     }
 
     @Override
 	protected void onStep(Controller controller, Snapshot snapshot) {
         FieldObject target = getTarget(snapshot);
 
 		if ((snapshot == null) || (snapshot.getBalle().getPosition() == null)
                 || (target == null))
             return;
 
         if (shouldApproachTargetFromCorrectSide()
                 && (!snapshot.getBalle()
                         .isApproachingTargetFromCorrectSide(target,
                                 snapshot.getOpponentsGoal()))) {
             LOG.info("Approaching target from wrong side, calculating overshoot target");
 			target = getOvershootTarget(target, OVERSHOOT_GAP, snapshot);
             addDrawable(new Dot(target.getPosition(), Color.BLUE));
         }
 
         // If we see the opponent
 		if (shouldAvoidOpponent
 				&& snapshot.getOpponent().getPosition() != null) {
 			if (!snapshot.getBalle().canReachTargetInStraightLine(target,
 					snapshot.getOpponent())) {
                 // pick a new target then
                 LOG.info("Opponent is blocking the target, avoiding it");
 				target = getAvoidanceTarget(snapshot);
                 addDrawable(new Dot(target.getPosition(), Color.MAGENTA));
             }
         }
 
         // Update the target's location in executorStrategy (e.g. if target
         // moved)
         executorStrategy.updateTarget(target);
         // Draw the target
         if (target.getPosition() != null)
             addDrawable(new Dot(target.getPosition(), getTargetColor()));
 
         // If it says it is not finished, tell it to do something for a step.
         if (!executorStrategy.isFinished(snapshot)) {
 			executorStrategy.step(controller, snapshot);
         } else {
             // Tell the strategy to stop doing whatever it was doing
             executorStrategy.stop(controller);
         }
     }
 
     @Override
     public void stop(Controller controller) {
 		// TODO: do we really need to check this?
 		// if (!executorStrategy.isFinished(snapshot))
 		executorStrategy.stop(controller);
 
     }
 }
