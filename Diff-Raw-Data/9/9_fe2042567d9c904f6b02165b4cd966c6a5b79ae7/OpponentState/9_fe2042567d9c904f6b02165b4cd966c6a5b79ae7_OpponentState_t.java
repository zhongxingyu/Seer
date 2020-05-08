 import java.awt.geom.Point2D;
 
 import robocode.AdvancedRobot;
 import robocode.ScannedRobotEvent;
 
 
 @SuppressWarnings("serial")
 public class OpponentState extends Opponent {
 	
 	Opponent previousState;
 	OpponentChange change;
 	
 	public OpponentState(ScannedRobotEvent event, OpponentState previousState) {
 		super(event);
 		this.previousState = previousState;
 		if (previousState != null) {
 			this.change = new OpponentChange(this, previousState);
 		}
 		else {
 			this.change = new OpponentChange();
 		}
 	}
 	
 	public Point2D.Double getRelativePosition(AdvancedRobot other) {
 		double bearing = other.getHeadingRadians() + getBearingRadians();
 
 		double relativeX = Math.sin(bearing) * getDistance();
 		double relativeY = Math.cos(bearing) * getDistance();
 
 		return new Point2D.Double(relativeX, relativeY);
 	}
 
 	public Point2D.Double getAbsolutePosition(AdvancedRobot other) {
 		Point2D relative = getRelativePosition(other);
 
 		return new Point2D.Double(
 				other.getX() + relative.getX(),
 				other.getY() + relative.getY()
 		);
 	}
 	
 	/**
 	 * Guess if the bot has just stopped
 	 * @return true if the bot was moving, and now is not.
 	 */
 	public boolean stopped() {
		//Check if the bot's current speed is very low, and it wasn't before
		return (Math.abs(getVelocity()) < 0.01 && Math.abs(change.getVelocity()) > 0.01);
 	}
 
 }
