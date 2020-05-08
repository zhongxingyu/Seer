 package balle.strategy.planner;
 
 import java.awt.Color;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.drawable.DrawableLine;
 import balle.misc.Globals;
 import balle.strategy.executor.movement.GoToObject;
 import balle.strategy.executor.movement.GoToObjectPFN;
 import balle.strategy.executor.turning.FaceAngle;
 import balle.strategy.executor.turning.RotateToOrientationExecutor;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Snapshot;
 import balle.world.objects.Ball;
 import balle.world.objects.Goal;
 import balle.world.objects.Pitch;
 import balle.world.objects.Point;
 import balle.world.objects.Robot;
 import balle.world.objects.StaticFieldObject;
 
 public class GoToBallM3 extends GoToBall {
     private int stage;
     private static final double BALL_SAFE_GAP = 0.25;
     private RotateToOrientationExecutor turnExecutor = new FaceAngle();
 
     private static Logger LOG = Logger.getLogger(GoToBallM3.class);
 
     public GoToBallM3() {
         super(new GoToObjectPFN(0), true);
         stage = 0;
     }
 
     @Override
 	protected StaticFieldObject getTarget(Snapshot snapshot) {
 		Ball ball = snapshot.getBall();
        if (ball.getPosition() == null) {
            LOG.warn("Cannot see the ball");
            return null;
        }
 		Goal targetGoal = snapshot.getOpponentsGoal();
 
         Line targetLine = new Line(targetGoal.getPosition(), ball.getPosition());
 
         if (stage < 2)
         {
 
 			Pitch pitch = snapshot.getPitch();
             double ballSafeGap = BALL_SAFE_GAP;
             Line newTargetLine = targetLine.extend(ballSafeGap);
 			while (ballSafeGap > 0.01
 					&& !pitch.containsCoord(newTargetLine.extend(
                     Globals.ROBOT_LENGTH).getB())) {
                 ballSafeGap *= 0.95;
                 newTargetLine = targetLine.extend(ballSafeGap);
             }
             targetLine = newTargetLine;
         }
 
         if (stage == 1) {
             Coord targetCoord = targetLine.getB();
 			Line ballTargetLine = new Line(snapshot.getBall()
                     .getPosition(), targetCoord);
 			Coord ourPos = snapshot.getBalle().getPosition();
 
             if (ballTargetLine.contains(ourPos)) {
                 LOG.warn("Were fucked, TODO get out!");
             }
         }
         addDrawable(new DrawableLine(targetLine, Color.ORANGE));
         return new Point(targetLine.getB());
     }
 
     public void setAppropriateMovementStrategy()
     {
         if (stage == 1)
         {
             LOG.info("Going to BALL_SAFE target");
             setExecutorStrategy(new GoToObjectPFN(0));
             setApproachTargetFromCorrectSide(true);
         }
         else
         {
             LOG.info("Going to the ball");
             GoToObject strategy = new GoToObject(turnExecutor);
 			strategy.setStopDistance(0.00);
 
 			if (stage == 2) {
 				strategy.setMovementSpeed(200);
 			} else {
 				strategy.setMovementSpeed(GoToObject.DEFAULT_MOVEMENT_SPEED);
 			}
 
             setExecutorStrategy(strategy);
             setApproachTargetFromCorrectSide(false);
         }
     }
 
 	private void changeStage(int newStage) {
 		stage = newStage;
 		setAppropriateMovementStrategy();
 	}
 
 	@Override
 	protected void onStep(Controller controller, Snapshot snapshot) {
 		Robot ourRobot = snapshot.getBalle();
 		Ball ball = snapshot.getBall();
 
         if (stage == 0)
         {
             if ((ourRobot.getPosition() == null)
                     || (ourRobot.getOrientation() == null))
                 return;
 
 			Coord targetCoord = getTarget(snapshot).getPosition();
 
             double angleToFaceTarget = ourRobot
                     .getAngleToTurnToTarget(targetCoord);
             
             if (Math.abs(angleToFaceTarget) > Math.PI / 8) {
 				if (!turnExecutor.isTurning()) {
 					turnExecutor.setTargetOrientation(targetCoord.sub(
 							ourRobot.getPosition()).orientation());
 					LOG.info("Turning to target");
 				}
 
 				turnExecutor.step(controller, snapshot);
             } else {
                 LOG.info("Facing the target correctly");
                 turnExecutor.stop(controller);
 				changeStage(1);
             }
         } else {
         	
     		if (stage == 1
                     && ourRobot.getPosition().dist(
                             getTarget(snapshot).getPosition()) < Globals.ROBOT_LENGTH / 4) {
     			
 				changeStage(2);
     		
             } else if (stage == 2) {
 				if (ourRobot.getPosition().dist(ball.getPosition()) > BALL_SAFE_GAP * 2) {
 
 					changeStage(1);
 					
 				} else if (ourRobot.possessesBall(snapshot.getBall())
 						&& ourRobot.getFacingLine().intersects(
 								snapshot.getOpponentsGoal().getGoalLine())) {
 					LOG.info("Kicking");
 					controller.kick();
 					controller.setWheelSpeeds(200, 200);
                 } else if (ourRobot.getFrontSide().midpoint()
                         .dist(snapshot.getBall().getPosition()) < 0.11
 						&& !ourRobot.getFacingLine().intersects(
                                 snapshot.getOpponentsGoal().getGoalLine())
                         && (!turnExecutor.isTurning())) {
                     LOG.trace("dist "
                             + ourRobot.getFrontSide().midpoint()
                                     .dist(snapshot.getBall().getPosition()));
                     if (ourRobot.getFrontSide().midpoint()
                             .dist(snapshot.getBall().getPosition()) > 0.07)
 				    {
     					LOG.info("Trying to go to ball-safe-gap again");
     					changeStage(0);
 				    }
 				    else
 				    {
                         LOG.info("Backing away from the ball");
 				        controller.setWheelSpeeds(-200, -200);
 				    }
                 } else {
                     LOG.trace("elsedist "
                             + ourRobot.getFrontSide().midpoint()
                                     .dist(snapshot.getBall().getPosition()));
                 }
 			}
 			super.onStep(controller, snapshot);
         }
     }
 
 }
