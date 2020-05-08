 package eu.flatwhite.shiro.spatial.inifinite;
 
 import java.util.Arrays;
 
 import eu.flatwhite.shiro.spatial.AbstractSpace;
 import eu.flatwhite.shiro.spatial.Spatial;
 
 /**
  * An n-dimensional (where n > 0) Euclidean space. Common instances of this
  * class would represent 1, 2 or a 3-dimensional space, but this class supports
  * any number of dimensions.
  * <p>
  * This implementation also supports projecting points from other Euclidean
 * spaces of different dimensions. For example a {@code Point} in a
  * 3-dimensional space can be projected to a {@code Point} in a 2-dimensional
  * space (X,Y,Z -&gt; X,Y). When points are projected from lesser-dimensional
  * spaces, the additional coordinates are set to this space's origin (ie: (X,Y
 * -&gt; X,Y,O<sub>Z</sub>, where O<sub>Z</sub> is this origin's Z coordinate).
  * <p>
  * <em>Example uses</em>
  * <ul>
  * <li>1-dimensional space: grant the 'no-fee' permission when a person's
  * account balance is outside (greater than) 1000.</li>
  * <li>2 or 3-dimensional space: a game where things inside 'cities' are granted
  * the 'heal' permission.</li>
 * <li>5-dimensional space: use a student's grades (on dimension per class, eg:
  * 5) to grant the 'play' permission when at least 3 grades are within a certain
  * range.</li>
  * </ul>
  */
 public class EuclideanSpace extends AbstractSpace {
 
     private final Point origin;
 
     private final int dimensions;
 
     /**
      * Build a {@code EuclideanSpace} of {@code dimensions} dimensions and its
      * origin at 0 in all dimensions.
      * 
      * @param dimensions
      *            number of dimensions of the new space
      */
     public EuclideanSpace(int dimensions) {
 	assert dimensions > 0 : "invalid number of dimensions";
 
 	this.dimensions = dimensions;
 	double[] o = new double[dimensions];
 	Arrays.fill(o, 0, dimensions, 0);
 	this.origin = new Point(this, o);
     }
 
     /**
      * Build a {@code EuclideanSpace} centered on {@code origin}. The number of
      * points of the origin determines the number of dimensions of the new
      * space.
      * 
      * @param origin
      *            the center point of the new space
      */
     public EuclideanSpace(double... origin) {
 	assert origin != null : "invalid origin";
 	assert origin.length > 0 : "invalid number of dimensions";
 
 	this.dimensions = origin.length;
 	// Point takes care of the copy.
 	this.origin = new Point(this, origin);
     }
 
     public Point getOrigin() {
 	return origin;
     }
 
     public boolean isContaining(Spatial spatial) {
 	// FIXME: Does a Euclidean space contain any point of the same
 	// dimensions or does it contain only points where
 	// (point.getSpace() == this) is true?
 	return spatial instanceof Point;
     }
 
     @Override
     public Spatial project(Spatial spatial) {
 	if (spatial instanceof Point) {
 	    Point p = (Point) spatial;
 	    // The projected point coordinates
 	    double[] projection = new double[dimensions];
 
 	    // d holds the size of the smaller space
 	    int d = Math.min(this.dimensions, p.getDimensions());
 
 	    int i = 0;
 	    // Project all "common" dimensions
 	    for (; i < d; i++) {
 		projection[i] = p.get(i);
 	    }
 	    // Any additional dimensions are set to this space's origin
 	    for (; i < dimensions; i++) {
 		projection[i] = getOrigin().get(i);
 	    }
 	    return new Point(this, dimensions);
 	}
 	return null;
     }
 
     public int getDimensions() {
 	return this.dimensions;
     }
 
     @Override
     protected double calculateDistance(Spatial s1, Spatial s2) {
 	Point p1 = (Point) s1;
 	Point p2 = (Point) s2;
 
 	double sum = 0.0;
 	for (int i = 0; i < dimensions; i++) {
 	    double d = p1.get(i) - p2.get(i);
 	    sum += d * d;
 	}
 	return Math.sqrt(sum);
     }
 }
