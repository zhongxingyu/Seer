 package balle.strategy;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.world.Coord;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 
 public class GameFromPenaltyDefence extends Game {
 
 	private static Logger LOG = Logger.getLogger(GameFromPenaltyDefence.class);
 
 	private Snapshot firstSnapshot = null;
 	String robotState = "Center";
 	int rotateSpeed = 0;
 
 	private boolean finished = false;
 
     public GameFromPenaltyDefence() {
         super();
 	}
 	
     @FactoryMethod(designator = "Game (Penalty Defence)", parameterNames = {})
 	public static GameFromPenaltyDefence gameFromPenaltyDefenceFactory()
 	{
         return new GameFromPenaltyDefence();
 	}
 
 	public boolean isStillInPenaltyDefence(Snapshot snapshot) {
 
 		Coord ball = snapshot.getBall().getPosition();
 		if ((ball == null) && snapshot.getOwnGoal().isLeftGoal())
 			ball = new Coord(0.5, 0.6); // assume that ball is on penalty spot,
 										// if
 									// we cannot see it.
 		if ((ball == null) && !snapshot.getOwnGoal().isLeftGoal())
 			ball = new Coord(1.8, 0.6); // assume that ball is on penalty spot,
 										// if
 									// we cannot see it.
 
 		double minX = 0;
 		double maxX = 0.75;
 		if (!snapshot.getOwnGoal().isLeftGoal()) {
 			maxX = snapshot.getPitch().getMaxX();
 			minX = maxX - 0.75;
 		}
 
 		if (ball.isEstimated()
 				|| (ball.getY() < snapshot.getOwnGoal().getMaxY())
 				&& (ball.getY() > snapshot.getOwnGoal().getMinY())
 				&& (ball.getX() > minX) && (ball.getX() < maxX)) {
 
 			return true;
 		}
 
 		finished = true;
 		return false;
 	}
 
 	@Override
 	public void onStep(Controller controller, Snapshot snapshot) {
 
 		if (finished || !isStillInPenaltyDefence(snapshot)) {
 			super.onStep(controller, snapshot);
 			return;
 		}
 
 		if ((firstSnapshot == null)
 				&& (snapshot.getBall().getPosition() != null)) {
 			firstSnapshot = snapshot;
 		}
 
 		Orientation opponentAngle = snapshot.getOpponent().getOrientation();
 
 		double threshold = Math.toRadians(30);
 		
 		Boolean isLeftGoal = snapshot.getOwnGoal().isLeftGoal();
 
 		if (snapshot.getOwnGoal().getMaxY() < snapshot.getBalle()
 .getPosition()
 				.getY() + 0.3) {
 			robotState = "Up";
 		} else if (snapshot.getOwnGoal().getMinY() > snapshot.getBalle()
 				.getPosition().getY() - 0.3) {
 			robotState = "Down";
 		} else {
 			robotState = "Center";
 		}
 
 		LOG.debug("robotState: " + robotState);
 
 		if (isLeftGoal == true) {
 			if (opponentAngle.atan2styleradians() > Math.PI - threshold) {
 				moveTo("Up", controller);
 			} else if (opponentAngle.atan2styleradians() < -Math.PI + threshold) {
 				moveTo("Down", controller);
 			} else {
 				moveTo("Center", controller);
 			}
 
 		} else {
 			if (opponentAngle.atan2styleradians() > threshold) {
 				moveTo("Up", controller);
 			} else if (opponentAngle.atan2styleradians() < -threshold) {
 				moveTo("Down", controller);
 			} else {
 				moveTo("Center", controller);
 			}
 		}
 	}
 
 	private void moveTo(String moveTo, Controller controller) {
 
 		rotateSpeed = 0;
 		LOG.debug("moveTo: " + moveTo);
 
 		if (robotState.equals("Center") && moveTo.equals("Up"))
 			rotateSpeed = 400;
 		if (robotState.equals("Center") && moveTo.equals("Down"))
 			rotateSpeed = -400;
 		if (robotState.equals("Up") && moveTo.equals("Center"))
 			rotateSpeed = -400;
 		if (robotState.equals("Up") && moveTo.equals("Down"))
 			rotateSpeed = -400;
 		if (robotState.equals("Down") && moveTo.equals("Center"))
 			rotateSpeed = 400;
 		if (robotState.equals("Down") && moveTo.equals("Up"))
 			rotateSpeed = 400;
 
 		if (rotateSpeed == 0)
 			controller.stop();
 		else
 			controller.setWheelSpeeds(rotateSpeed, rotateSpeed);
 
 		LOG.debug("Speed: " + rotateSpeed);
 	}
 }
