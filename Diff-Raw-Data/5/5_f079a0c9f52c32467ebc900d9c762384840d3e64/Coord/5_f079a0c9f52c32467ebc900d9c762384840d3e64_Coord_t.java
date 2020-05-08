 package balle.world;
 
 import java.awt.Point;
 
 import balle.world.objects.FieldObject;
 import balle.world.objects.Pitch;
 
 public class Coord {
 
 	private final double x;
 	private final double y;
 
 	private final boolean estimated;
 
 	public Coord(double x, double y) {
 		super();
 		this.x = x;
 		this.y = y;
 		this.estimated = false;
 	}
 
 	public Coord(double x, double y, boolean estimated) {
 		super();
 		this.x = x;
 		this.y = y;
 		this.estimated = estimated;
 	}
 
 	public Coord(Coord coordinate, boolean estimated) {
 		this(coordinate.getX(), coordinate.getY(), estimated);
 	}
 
 	public double getX() {
 		return x;
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public double abs() {
 		return Math.sqrt(sqrAbs());
 	}
 
 	public double sqrAbs() {
 		return x * x + y * y;
 	}
 
 	public double dot(Coord c) {
 		return x * c.getX() + y * c.getY();
 	}
 
 	public Coord sub(Coord c) {
 		return new Coord(x - c.getX(), y - c.getY(), estimated);
 	}
 
 	public Coord add(Coord c) {
 		return new Coord(x + c.getX(), y + c.getY(), estimated);
 	}
 
 	public Coord mult(double scalar) {
 		return new Coord(x * scalar, y * scalar, estimated);
 	}
 
 	public double dist(Coord c) {
 		return c.sub(this).abs();
 	}
 
 	public Orientation orientation() {
 		double orientation_atan2 = Math.atan2(this.getY(), this.getX());
 		if (orientation_atan2 < 0)
 			orientation_atan2 = 2 * Math.PI + orientation_atan2;
 		return new Orientation(orientation_atan2, true);
 	}
 
 	/**
 	 * Returns whether the coordinates have been estimated or not. By convention
 	 * the coordinates from the vision are not estimated, whereas the
 	 * coordinates that are updated from velocities e.g. when the vision returns
 	 * -1 are.
 	 * 
 	 * @return true or false depending whether the coordinates were estimated or
 	 *         not
 	 */
 	public boolean isEstimated() {
 		return estimated;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		if (other == null)
 			return false;
 		if (other == this)
 			return true;
 		if (this.getClass() != other.getClass())
 			return false;
 		Coord otherCoord = (Coord) other;
 		return (otherCoord.getX() == this.getX())
 				&& (otherCoord.getY() == this.getY())
 				&& (otherCoord.isEstimated() == this.isEstimated());
 	}
 
 	/**
 	 * Returns true if Coordinate is reachable via a straight line from another
 	 * coordinate
 	 * 
 	 * @param fromCoordinate
 	 *            the starting coordinate
 	 * @return true, if coordinate is reachable in straight line from another
 	 */
 	public boolean isReachableInStraightLineAndNotBlocked(Coord fromCoordinate,
 			Pitch pitch) {
 		return pitch.containsCoord(this) && pitch.containsCoord(fromCoordinate);
 	}
 
 	/**
 	 * Returns true if Coordinate is reachable via a straight line from another
 	 * coordinate and is not blocked
 	 * 
 	 * @param fromCoordinate
 	 *            the starting coordinate
 	 * @param potentialObstacle
 	 *            checks what could be the potential obstacle.
 	 * @return true, if coordinate is reachable in straight line from another
 	 */
 	public boolean isReachableInStraightLineAndNotBlocked(Coord fromCoordinate,
 			Pitch pitch, FieldObject potentialObstacle) {
 		if (!this.isReachableInStraightLineAndNotBlocked(fromCoordinate, pitch))
 			return false;
 
 		Line line = new Line(new Coord(this.getX(), this.getY()), new Coord(
 				fromCoordinate.getX(), fromCoordinate.getY()));
 
 		return !potentialObstacle.intersects(line);
 	}
 
 	/**
 	 * Creates a new coord rotated counter-clockwise by an angle. around the
 	 * origin
 	 * 
 	 * @param orientation
 	 */
 	public Coord rotate(Orientation orientation) {
 		double theta, nX, nY;
 		theta = orientation.radians();
 		nX = getX() * Math.cos(theta) - getY() * Math.sin(theta);
 		nY = getX() * Math.sin(theta) + getY() * Math.cos(theta);
 		return new Coord(nX, nY);
 	}
 
 	/**
 	 * Calculates the angle from one coordinate to another from this point.
 	 * 
 	 * TODO write test
 	 * 
 	 * @param from
 	 *            First coordinate.
 	 * @param to
 	 *            Second coordinate.
 	 * @return Angle between from and to from this reference point.
 	 */
 	public Orientation angleBetween(Coord from, Coord to) {
 		Orientation out;
 
 		Coord dFrom, dTo;
 		dFrom = from.sub(this);
 		dTo = to.sub(this);
 
 		double dotProduct, dX, dY, mulAbs, adbc;
 		dX = (dFrom.x * dTo.x);
 		dY = (dFrom.y * dTo.y);
 		mulAbs = dFrom.abs() * dTo.abs();
 		dotProduct = (dX + dY) / mulAbs;
 
 		adbc = from.x * to.y - from.y * to.x;
 		if (adbc >= 0) {
 			out = new Orientation(Math.acos(dotProduct), true);
		} else {
			out = new Orientation(-Math.acos(dotProduct), true);
 		}
 
 		return out;
 	}
 
 	public Point getPoint() {
 		Point out = new Point();
 		out.setLocation(x, y);
 		return out;
 	}
 
 	@Override
 	public String toString() {
 		return "(" + x + "," + y + ")";
 	}
 }
