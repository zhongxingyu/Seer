 package net.adbenson.robocode.bullet;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.adbenson.robocode.botstate.BotState;
 import net.adbenson.robocode.botstate.OpponentState;
 import net.adbenson.robocode.botstate.SelfState;
 import net.adbenson.utility.Utility;
 import net.adbenson.utility.Vector;
 import robocode.Rules;
 
 
 public abstract class Bullet
 	<BotType extends BotState<?, BotType>, 
 	OtherBotType extends BotState<?, OtherBotType>> {
 	
 	public static interface State {
 	}
 	
 	public static enum Origin implements State {
 		TRAVELLING
 	}
 	
 	public static enum Fate implements State {
 		HIT_TARGET,
 		HIT_BULLET,
 		MISSED
 	}
 	
 	public final BotType shooter;
 	public final OtherBotType target;
 	
 	public final Vector origin;
 	public final double power;
 	public final double heading;
 	public final long time;
 	public final double velocity;
 	
 	private State state;
 	
 	protected double distanceTravelled;
 	
 	private double escapeDistance;
 	
 	private List<Vector> escapePoints;
 	
	public Bullet(OpponentState shooter, SelfState self, double heading, long time) {
 		this.shooter = shooter;
 		this.target = self;
 		this.origin = shooter.position;
 		this.power = -shooter.change.energy;
 		this.heading = heading;
 		this.time = time;
 		this.velocity = velocity();
 		this.state = Origin.TRAVELLING;
 	}
 	
	public Bullet(SelfState self, OpponentState target, robocode.Bullet bullet, long time) {
 		this.shooter = self;
 		this.target = target;
 		this.origin = shooter.position;
 		this.power = bullet.getPower();
 		this.heading = bullet.getHeadingRadians();
 		this.time = time;
 		this.velocity = bullet.getVelocity();
 		this.state = Origin.TRAVELLING;
 	}
 
 	protected void updateDistance(long currentTime) {
 		long timeElapsed = currentTime - time;
 		//A lot of trial and error to get this fudge factor right!
 		distanceTravelled = calculateDistanceTravelled(timeElapsed);
 		
 		updateProjection();
 		updateEscapeDistance();
 	}
 	
 	public double calculateDistanceTravelled(long time) {
 		return velocity * (time+1);
 	}
 	
 	public double turnsToTravel(double distance) {
 		return distance / velocity;
 	}
 
 	private double velocity() {
         return getVelocity(power);
     }
 	
 	public static double getVelocity(double power) {
 		return (20.0 - (3.0 * power));
 	}
 	
 	public static double getRequiredPower(int turns, double distance) {
 		return (20.0 - (distance / turns)) / 3.0;
 	}
     
     public double getDistanceTravelled() {
     	return distanceTravelled;
     }
     
     protected void updateEscapeDistance() {
 		double directDistance = target.position.distance(origin);
 		
 		double turns = Math.ceil(turnsToTravel(directDistance));
 		
 		escapeDistance = Rules.MAX_VELOCITY * turns;
 
 		double angle = Utility.angleDiff(Utility.oppositeAngle(heading), Utility.HALF_PI);
 		
 		escapePoints = new LinkedList<Vector>();
 		for (int turn = (int) -turns; turn <= turns ; turn++) {
 			escapePoints.add(target.position.project(angle, turn * Rules.MAX_VELOCITY));
 		}
 	}
 	
 	public double getEscapeDistance() {
 		updateEscapeDistance();
 		return escapeDistance;
 	}
 	
 	public void drawEscapePoints(Graphics2D g, Color c) {
 		if (escapePoints != null) {
 			g.setStroke(new BasicStroke(1));
 			g.setColor(Utility.setAlpha(c, 0.4));
 			
 			for(Vector point : escapePoints) {
 				point.fill(g, 2);
 			}
 		}
 	}
 
 	public abstract void draw(Graphics2D g);
 	
 	public abstract boolean shouldDelete();
 	
     public abstract void updateProjection();
     
 	public abstract boolean matches(robocode.Bullet b);
 	
 	protected abstract void terminate(robocode.Bullet b, Fate fate);
 
 	protected void setFate(Fate fate) {
 		this.state = fate;
 	}
 	
 	public State getState() {
 		return state;
 	}
 	
 }
