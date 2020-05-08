 package balle.world;
 
 import balle.world.objects.Ball;
 import balle.world.objects.Pitch;
 import balle.world.objects.Robot;
 
 public class BasicWorld extends AbstractWorld {
 
 	protected Snapshot prev;
 
 	public BasicWorld(boolean balleIsBlue, boolean goalIsLeft, Pitch pitch) {
 		super(balleIsBlue, goalIsLeft, pitch);
 		prev = new EmptySnapshot(getOpponentsGoal(), getOwnGoal(), getPitch());
 	}
 
 	@Override
 	public synchronized Snapshot getSnapshot() {
 		return prev;
 	}
 
 	private Coord subtractOrNull(Coord a, Coord b) {
 		if ((a == null) || (b == null))
 			return null;
 		else
 			return a.sub(b);
 	}
 
 	/**
 	 * Returns true if this position seems to be reasonable enough. Returns true
 	 * if previousPosition was null and false if currentPosition is null
 	 * 
 	 * TODO: Make this function account for velocity and check whether it is
 	 * close to the expected position at this velocity!!
 	 * 
 	 * @param previousPosition
 	 * @param currentPosition
 	 * @return
 	 */
 	protected boolean positionIsCloseToExpected(Coord previousPosition,
 			Coord currentPosition) {
 
 		final double EPSILON = 1; // 1 Metre
 
 		if (previousPosition == null)
 			return true;
 		else if (currentPosition == null)
 			return false;
 		// Check whether current position is 1m further than previous position
 		// and previous position wasn't estimated
 		else if (previousPosition.dist(currentPosition) > EPSILON
 				&& !previousPosition.isEstimated())
 			return false;
 		else
 			return true;
 
 	}
 
 	/**
 	 * NOTE: DO ROBOTS ALWAYS MOVE FORWARD !? NO, treat angle of velocity
 	 * different from angle the robot is facing.
 	 * 
 	 */
 	@Override
 	public void updateScaled(Coord ourPosition, Orientation ourOrientation,
 			Coord theirsPosition, Orientation theirsOrientation,
 			Coord ballPosition, long timestamp) {
 
 		Robot ours = null;
 		Robot them = null;
 		Ball ball = null;
 
 		Snapshot prev = getSnapshot();
 
 		// Check if the new positions make sense. For instance, discard
 		// the ones that are unreasonably far away from the previous one
 		ourPosition = positionIsCloseToExpected(prev.getBalle().getPosition(),
 				ourPosition) ? ourPosition : null;
 		theirsPosition = positionIsCloseToExpected(prev.getOpponent()
 				.getPosition(), theirsPosition) ? theirsPosition : null;
 		ballPosition = positionIsCloseToExpected(prev.getBall().getPosition(),
 				ballPosition) ? ballPosition : null;
 
 		// change in time
 		long deltaT = timestamp - prev.getTimestamp(); // Hopefully that does
 														// not cause issues
 														// with EmptySnapshot
 														// and
 														// currentTimeMilis()
 
 		// Special case when we get two inputs with the same timestamp:
 		if (deltaT == 0) {
 			// This will just keep the prev world in the memory, not doing
 			// anything
 			return;
 		}
 
 		if (ourPosition == null)
 			ourPosition = estimatedPosition(prev.getBalle(), deltaT);
 		if (theirsPosition == null)
 			theirsPosition = estimatedPosition(prev.getOpponent(), deltaT);
 		if (ballPosition == null)
 			ballPosition = estimatedPosition(prev.getBall(), deltaT, true);
 
 		// Calculate how much each position has changed between frames
 		Coord oursDPos, themDPos, ballDPos;
 		oursDPos = subtractOrNull(ourPosition, prev.getBalle().getPosition());
 		themDPos = subtractOrNull(theirsPosition, prev.getOpponent()
 				.getPosition());
 		ballDPos = subtractOrNull(ballPosition, prev.getBall().getPosition());
 
 		// Recalculate the velocities from deltapositions above.
 		Velocity oursVel, themVel, ballVel;
 		oursVel = oursDPos != null ? new Velocity(oursDPos, deltaT) : null;
 		themVel = themDPos != null ? new Velocity(themDPos, deltaT) : null;
 		ballVel = ballDPos != null ? new Velocity(ballDPos, deltaT) : null;
 
 		if (ourOrientation == null) {
 			ourOrientation = prev.getBalle().getOrientation();
 		}
 
 		if (theirsOrientation == null) {
 			theirsOrientation = prev.getOpponent().getOrientation();
 		}
 
 		// Recalculate the angular velocities
 		AngularVelocity oursAngVel, themAngVel;
		oursAngVel = (ours != null) ? new AngularVelocity(
 				ourOrientation.angleToatan2Radians(prev.getBalle()
 						.getOrientation()), deltaT) : null;
		themAngVel = (them != null) ? new AngularVelocity(
 				theirsOrientation.angleToatan2Radians(prev.getOpponent()
 						.getOrientation()), deltaT) : null;
 
 		// put it all together (almost)
 		them = new Robot(theirsPosition, themVel, themAngVel, theirsOrientation);
 		ours = new Robot(ourPosition, oursVel, oursAngVel, ourOrientation);
 		ball = new Ball(ballPosition, ballVel);
 
 		// pack into a snapshot
 		Snapshot nextSnapshot = filter(new Snapshot(them, ours, ball,
 				getOpponentsGoal(), getOwnGoal(), getPitch(), timestamp));
 		synchronized (this) {
 			this.prev = nextSnapshot;
 		}
 	}
 
 	@Override
 	public void updatePitchSize(double width, double height) {
 		super.updatePitchSize(width, height);
 		synchronized (this) {
 			this.prev = new EmptySnapshot(getOpponentsGoal(), getOwnGoal(),
 					getPitch());
 		}
 	}
 
 }
