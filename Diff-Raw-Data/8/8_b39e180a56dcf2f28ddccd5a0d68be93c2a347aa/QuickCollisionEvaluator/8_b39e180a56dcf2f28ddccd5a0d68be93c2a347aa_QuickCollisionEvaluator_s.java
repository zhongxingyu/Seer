 package br.ufrj.jfirn.intelligent.evaluation;
 
 import org.apache.commons.math3.util.FastMath;
 
 import br.ufrj.jfirn.common.Point;
 import br.ufrj.jfirn.intelligent.Collision;
 import br.ufrj.jfirn.intelligent.MobileObstacleStatisticsLogger;
 import br.ufrj.jfirn.intelligent.Thoughts;
 import br.ufrj.jfirn.intelligent.Trajectory;
 
 public class QuickCollisionEvaluator implements Evaluator {
 
 	@Override
 	public void evaluate(Thoughts thoughts, Instruction instruction, ChainOfEvaluations chain) {
 		//think and evaluate and change your thoughts and decide what to do next...
 		final Point myPosition = thoughts.myPosition();
 
 		for (MobileObstacleStatisticsLogger mo : thoughts.allObstacleStatistics()) { //evaluate everyone I see.
 			Collision collision = evaluateCollision(
 				myPosition,
 				thoughts.myDirection(),
 				thoughts.mySpeed(),
 				mo.lastKnownPosition(),
 				mo.directionMean(),
 				mo.speedMean(),
 				mo.getObservedObjectId()
 			);
 
 			if (collision == null) { //No collision. Verify someone else.
 				continue;
 			}
 
 			//If this collision is too far in the future, forget about it. Verify someone else.
 			if (myPosition.distanceTo(collision.position) > 200d || collision.time > 10d) {
 				continue;
 			}
 
 			//This if may be weird, but it will work because we defined a equals and hashCode
 			//to Collision class, based on the id of the object that the robot will collide with.
 			thoughts.putCollision(mo.getObservedObjectId(), collision);
 		}
 
 		chain.nextEvaluator(thoughts, instruction, chain); //keep thinking
 	}
 
 
 	private Collision evaluateCollision(Point myPosition, double myDirection, double mySpeed, Point otherPosition, double otherDirection, double otherSpeed, int id) {
 		//TODO I fear this will perform poorly for something supposed to be fast...
 
 		//here we forecast if a collision may happen
 		final Point collisionPosition =
 			intersection(myPosition, myDirection, otherPosition, otherDirection);
 		if (collisionPosition == null) { //if there is no intersection, then there is no collision
 			return null;
 		}
 
 		//now, we calculate when it's going to happen...
 
 		//but first, will it really happen?
 		if ( //if any robot has passed the evaluated collision position, then there will be no collision.
 			!isTheRightDirection(myPosition, myDirection, collisionPosition) ||
 			!isTheRightDirection(otherPosition, otherDirection, collisionPosition) ) {
 			return null;
 		}
 
 		//when each robot will reach the collision position?
		final double meTime = timeToReach(myPosition, mySpeed, collisionPosition);
 		final double otherTime = timeToReach(otherPosition, otherSpeed, collisionPosition);
 
 		//Heuristic: I'm considering there will be a collision if the time between robots to arrive at the collision position are almost the same.
 		//TODO Improve this 'if', maybe considering objects direction, speed, size, etc.
		if (FastMath.abs(meTime - otherTime) > 6d) {
 			return null;
 		}
 
 		//estimate the collision time with the average of times
		final double time = (meTime + otherTime) / 2d;
 
 		return new Collision(id, collisionPosition, time);
 	}
 
 
 	/**
 	 * The robot paths intersect at some point?
 	 */
 	private Point intersection(Point myPosition, double myDirection, Point otherPosition, double otherDirection) {
 		final Trajectory t1 = new Trajectory(myDirection, myPosition);
 		final Trajectory t2 = new Trajectory(otherDirection, otherPosition);
 		return t1.intersect(t2);
 	}
 
 
 
 	/**
 	 * How much time a robot at position with speed would take to reach destination?
 	 * Ignores the direction because I assume I'm going straight towards the destination.
 	 * @see #isTheRightDirection(Point, double, Point)
 	 */
 	private double timeToReach(Point position, double speed, Point destination) {
 		return position.distanceTo(destination) / speed;
 	}
 
 	/**
 	 * Is the robot going towards the collision or has it passed the destination already?
 	 * true if it is going in the right direction.
 	 */
 	public static boolean isTheRightDirection(Point position, double direction, Point destination) {
 		//TODO there might be a better/faster way to do this
 
 		final double y = destination.y() - position.y();
 		final double x = destination.x() - position.x();
 		final double angle = FastMath.atan2(y, x);
 
 		final double sinDirection = FastMath.sin(direction);
 		final double sinAngle = FastMath.sin(angle);
 		final double cosDirection = FastMath.cos(direction);
 		final double cosAngle = FastMath.cos(angle);
 
 		return FastMath.signum(sinDirection) == FastMath.signum(sinAngle)
 			&& FastMath.signum(cosDirection) == FastMath.signum(cosAngle);
 	}
 
 }
