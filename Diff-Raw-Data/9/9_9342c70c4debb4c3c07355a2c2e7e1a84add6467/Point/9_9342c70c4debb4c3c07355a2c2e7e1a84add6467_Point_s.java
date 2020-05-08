 package br.ufrj.jfirn.common;
 
 import java.text.DecimalFormat;
 import java.util.Comparator;
 
 import org.apache.commons.math3.util.FastMath;
 
 
 /**
  * A point in a bidimensional space. This implementation is not mutable.
  * 
  * @author <a href="mailto:ramiro.p.magalhaes@gmail.com">Ramiro Pereira de Magalhães</a>
  *
  */
 public class Point {
 
	protected double x, y;

	public Point() {
		x = y = 0;
	}
 
 	public Point(double x, double y) {
 		this. x = x;
 		this.y = y;
 	}
 
 	public final double x() {
 		return x;
 	}
 
 	public final double y() {
 		return y;
 	}
 
 	/**
 	 * Returns the euclidean distance between this and p.
 	 */
 	public double distanceTo(final Point p) {
 		return FastMath.hypot(
 			this.x - p.x,
 			this.y - p.y
 		);
 	}
 
 	/**
 	 * Returns the angle a line would make to a horizontal
 	 * line should it pass through this and p points. Zero
 	 * points to the {@link Robot#RIGHT}.
 	 */
 	public double directionTo(final Point p) {
 		return FastMath.atan2(
 			p.y - this.y,
 			p.x - this.x
 		);
 	}
 
	private static final DecimalFormat format = new DecimalFormat("#.00");
 
 	@Override
 	public String toString() {
 		return new StringBuilder()
 			.append("Point[")
 			.append(format.format(x))
 			.append(", ")
 			.append(format.format(y))
 			.append("]")
 			.toString();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Point)) {
 			return false;
 		}
 
 		final Point other = (Point)obj;
 		return this.x == other.x && this.y == other.y;
 	}
 
 	@Override
 	public int hashCode() {
 		return (int)(13 * this.x + 7 * this.y);
 	}
 
 	/**
 	 * Sorts points in X order, smaller to bigger.
 	 */
 	public static class XComparator implements Comparator<Point> {
 		public static final XComparator instance = new XComparator();
 
 		private XComparator() {}
 
 		@Override
 		public int compare(Point p1, Point p2) {
 			return (int)(p1.x - p2.x);
 		}
 	}
 
 	/**
 	 * Sorts points in Y order, smaller to bigger.
 	 */
 	public static class YComparator implements Comparator<Point> {
 		public static final YComparator instance = new YComparator();
 
 		private YComparator() {}
 
 		@Override
 		public int compare(Point p1, Point p2) {
 			return (int)(p1.y - p2.y);
 		}
 	}
 
 	/**
 	 * Sorts points in Y order,) smaller to bigger.
 	 */
 	public static class YThenXComparator implements Comparator<Point> {
 		public static final YThenXComparator instance = new YThenXComparator();
 
 		private YThenXComparator() {}
 
 		@Override
 		public int compare(Point p1, Point p2) {
 			if (p1.y != p2.y) {
 				return p1.y < p2.y ? -1 : 1;
 			}
 
 			if  (p1.x != p2.x){
 				return p1.x < p2.x ? -1 : 1;
 			}
 
 			return 0;
 		}
 	}
 
 }
