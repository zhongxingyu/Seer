 package br.ufrj.jfirn.intelligent;
 
 import org.apache.commons.math3.util.FastMath;
 
 import br.ufrj.jfirn.common.Point;
 import br.ufrj.jfirn.intelligent.evaluation.RobotBoundaries;
 
 public class Trajectory {
 
 	//Considering this as a linear equation we have y = alpha * x + beta
 	private final double alpha;
 	private final double beta;
 

 	/**
 	 * Creates an instance of this class that is a trajectory defined by the
 	 * line segment that starts in A and ends in B.
 	 */
 	public Trajectory(Point a, Point b) {
 		this(a.directionTo(b), a);
 	}
 
 	public Trajectory(double direction, Point origin) {
 		this.alpha = FastMath.tan(direction);
		this.beta = origin.y() - this.alpha * origin.x();
 	}
 
 	public Point intersect(Trajectory other) {
 		if (this.alpha == other.alpha) {
 			return null; //no intersection or infinite intersecting points. In both cases, we return null.
 		}
 
		if (FastMath.abs(this.alpha) > FastMath.abs(other.alpha)) {
			return other.intersect(this);
		} else {
			//the intersection of those 2 linear equations is...
			final double x = (other.beta - this.beta) / (this.alpha - other.alpha);
			final double y = this.alpha * x + this.beta;
 
			return new Point(x, y);
		}
 	}
 
 	public boolean intersect(Point p) {
 		return p.y() == alpha * p.x() + beta;
 	}
 
 	/**
 	 * From the statistical data of a mobile obstacle movement, get me the trajectories
 	 * that represent the boundaries of its movement.
 	 */
 	public static Trajectory[] fromStatistics(MobileObstacleStatistics stats) {
 		final Point lkp = stats.lastKnownPosition();
 		final double mean = stats.directionMean();
 		final double stdDeviation = FastMath.sqrt(stats.directionVariance());
 
 		final Point[] points = RobotBoundaries.pointsFromVerticalAxis(lkp, mean);
 
 		return new Trajectory[] {
 			new Trajectory(mean - stdDeviation, points[0]),
 			new Trajectory(mean + stdDeviation, points[1])
 		};
 	}
 
 	@Override
 	public String toString() {
 		//sample Trajectory 2 x + 10
 		StringBuilder s = new StringBuilder()
 			.append("Trajectory ")
 			.append(alpha)
 			.append(" x ");
 
 		if (FastMath.signum(beta) == 0) {
 			return s.toString();
 		}
 
 		if (FastMath.signum(beta) > 0) {
 			return s.append("+ ")
 				.append(beta)
 				.toString();
 		} else {
 			return s.append("- ")
 				.append(-beta)
 				.toString();
 		}
 	}
 
 }
