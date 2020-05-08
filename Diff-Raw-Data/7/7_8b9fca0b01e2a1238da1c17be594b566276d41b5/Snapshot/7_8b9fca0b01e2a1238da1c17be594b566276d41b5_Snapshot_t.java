 package balle.world;
 
 import balle.world.objects.Ball;
 import balle.world.objects.Goal;
 import balle.world.objects.Pitch;
 import balle.world.objects.Robot;
 
 /**
  * Immutable
  */
 public class Snapshot {
 	
 	private final AbstractWorld world;
 
 	private final Robot opponent;
 	private final Robot bot;
 	private final Ball ball;
 
 	private final Goal opponentsGoal;
 	private final Goal ownGoal;
 
 	private final Pitch pitch;
 	private final long timestamp;
 
 	public Snapshot(AbstractWorld world, Robot opponent, Robot balle,
 			Ball ball, Goal opponentsGoal, Goal ownGoal, Pitch pitch,
 			long timestamp) {
 
 		super();
 		this.world = world;
 		this.opponent = opponent;
 		this.bot = balle;
 		this.ball = ball;
 		this.timestamp = timestamp;
 		this.opponentsGoal = opponentsGoal;
 		this.ownGoal = ownGoal;
 		this.pitch = pitch;
 	}
 	
 	public Snapshot getEstimateAfter(long dTime) {
        return world.estimateAt(dTime + timestamp);
 	}
 
    public Snapshot getEstimateAtCurrentTime() {
        return world.estimateAt(System.currentTimeMillis());
    }

 	public Pitch getPitch() {
 		return pitch;
 	}
 
 	public Goal getOpponentsGoal() {
 		return opponentsGoal;
 	}
 
 	public Goal getOwnGoal() {
 		return ownGoal;
 	}
 
 	public Robot getOpponent() {
 		return opponent;
 	}
 
 	public Robot getBalle() {
 		return bot;
 	}
 
 	public Ball getBall() {
 		return ball;
 	}
 
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	public Snapshot duplicate() {
 		return new Snapshot(world, getOpponent(), getBalle(), getBall(),
 				getOpponentsGoal(), getOwnGoal(), getPitch(), getTimestamp());
 	}
 
 	public MutableSnapshot unpack() {
 		return new MutableSnapshot(world, getOpponent(), getBalle(), getBall(),
 				getOpponentsGoal(), getOwnGoal(), getPitch(), getTimestamp());
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		if (other == null)
 			return false;
 		if (other == this)
 			return true;
 		if (!(other instanceof Snapshot))
 			return false;
 		Snapshot otherSnapshot = (Snapshot) other;
 
 		if (this.getTimestamp() == otherSnapshot.getTimestamp())
 			return true;
 		else
 			return false;
 	}
 }
