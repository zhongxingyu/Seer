 package vooga.fighter.util;
 
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.LinkedList;
 import java.util.List;
 
 import util.Location;
 import util.Vector;
 
 /**
  * This class is deticated to collision detection for 
  * shapes and provides useful methods to:
  * -confirm detections for shapes, either quickly or precisely
  * -check the side of a shape that another shape/point has with 
  *    (Treating all shapes as Rectangles in this case), choice 
  *    in parameters as velocity can be taken into account for 
  *    extra precision.
 * TODO: return the point on the surface of a shape that another 
  *     shape/point has collided with, Velocities required
  *     as parameters (will only work with rectanges)
  *     
  * Please read through the methods, but this class can be both
  * extended to include specific functionality for specific shapes (i.e ellipses)
  * or one can simply use the precise methods and insert their specific shape
  * (note this will not work for checking the sides, as it will always treat the
  * shape as a rectangle!)
  *
  * @author Jack Matteucci 
  * 
  */
 
 public class CollisionDetector {
 
 	public CollisionDetector() {
 	}
 	
 	 /**
 	  * Uses rectange bounds of each shape to check if any point of shape2
 	  * is within the rectange bounds of point 1
 	 */
 	public boolean quickDetectCollision(Shape shape1, Shape shape2){
		return shape1.getBounds().intersects(shape2.getBounds());
 	}
 
 	 /**
 	  * Uses rectange bounds of each shape to check if point2
 	  * is within the rectange bounds of point 1
 	 */
 	public boolean quickDetectCollision(Shape shape1, Point2D point2){
 		return shape1.contains(point2);
 	}
 	
 	 /** 
 	  * Precisely tracks the bounds of a shape, checking if for any 
 	  * point on the bounds, whether point2 is closer than the 
 	  * center than that bound point. 
 	  * @param precision is the allowed angle between the vector created
 	  * from the center to bound point and the vector created from the 
 	  * center to point2
 	  * @param rotationAngle is the angle at which shape1 is rotated- degrees
 	 */
 	public boolean preciseDetectCollision(Shape shape1, Location point2, 
 			double rotationAngle, double precision){
 		AffineTransform transform1 = AffineTransform.getRotateInstance(Math.toRadians(
 				Vector.SanitizeAngle((rotationAngle))));
 		PathIterator path1 = shape1.getPathIterator(transform1, .001);
 		Location center1 = ShapeMeasurements.getPrecisionCenter(shape1,Vector.SanitizeAngle(rotationAngle));
 		return pointWithinPath(path1,center1, point2,precision);
 	}
 	
 	 /** 
 	  * Precisely tracks the bounds of a shape, checking if for any 
 	  * point on the bounds, whether point2 is closer than the 
 	  * center than that bound point. 
 	  * @param precision is the allowed angle between the vector created
 	  * from the center to bound point and the vector created from the 
 	  * center to point2.  No rotation considered
 	 */
 	public boolean preciseDetectCollision(Shape shape1, Location point2, 
 			double precision){
 		return preciseDetectCollision(shape1, point2, 0,precision);
 	}
 	 /** 
 	  * Precisely tracks the bounds of a shape, checking if for any 
 	  * point on the bounds, whether point2 is closer than the 
 	  * center than that bound point. Precision taken to be a degree of 1;
 	  * No rotation considered.
 	 */		
 		public boolean preciseDetectCollision(Shape shape1, Location point2){
 			return preciseDetectCollision(shape1,point2, 1); // degree
 	}
 	
 		 /** 
 		  * Precisely tracks the bounds of a shape1, checking if for any 
 		  * point on the bounds, whether a point on shape2 is closer than shape1's 
 		  * center than that shape1 bound point. 
 		  * @param precision is the allowed angle between the vector created
 		  * from the center to bound point and the vector created from the 
 		  * center to the current point of shape2 being looked at 
 		  * @param rotationAngle1 is the angle at which shape1 is rotated- degrees
 		  * @param rotationAngle1 is the angle at which shape2 is rotated- degrees
 		 */
 	public boolean preciseDetectCollision(Shape shape1, Shape shape2, double rotationAngle1,
 			double rotationAngle2, double precision){
 		AffineTransform transform2 = AffineTransform.getRotateInstance(Math.toRadians(rotationAngle2));
 		PathIterator path2 = shape2.getPathIterator(transform2);
 		float[] coord2 = new float[6];
 			while(!path2.isDone()){
 				path2.currentSegment(coord2);
 				Location point2 = new Location(coord2[0],coord2[1]);
 				if(preciseDetectCollision(shape1, point2, rotationAngle1, precision)) return true;
 			}
 			
 		return false;
 	}
 	 /** 
 	  * Precisely tracks the bounds of a shape1, checking if for any 
 	  * point on the bounds, whether a point on shape2 is closer than shape1's 
 	  * center than that shape1 bound point. 
 	  * @param precision is the allowed angle between the vector created
 	  * from the center to bound point and the vector created from the 
 	  * center to the current point of shape2 being looked at.  No rotation
 	  * considered
 	  */
 	public boolean preciseDetectCollision(Shape shape1, Shape shape2, double precision){
 		return preciseDetectCollision(shape1,shape2,0,0,precision);
 	}
 	 /** 
 	  * Precisely tracks the bounds of a shape1, checking if for any 
 	  * point on the bounds, whether a point on shape2 is closer than shape1's 
 	  * center than that shape1 bound point. 
 	  * @param precision is the allowed angle between the vector created
 	  * from the center to bound point and the vector created from the 
 	  * center to the current point of shape2 being looked at.  Precision
 	  * set automatically to 1 degree. No rotation considered.
 	  */
 	public boolean preciseDetectCollsion(Shape shape1, Shape shape2){
 		double precision = 1; //degree
 		return preciseDetectCollision(shape1,shape2,1);
 	}
 	
     /**
      * Convenience method: Treating the Shape as a Rectangle
      * returns whether shape1's top has been collided with by 
      * point2
      */
 	public boolean hitTop(Shape shape1, Point2D point2){
 		double hitdirection = getQuickDirection(shape1,point2);
 		if((hitdirection-getQuickDirection(shape1, ShapeMeasurements.getTopLeftCorner(shape1) ) > 0 &&
 			(hitdirection-getQuickDirection(shape1, ShapeMeasurements.getTopRightCorner(shape1))) <= 0)){
 					return true;
 				}
 		return false;
 	}
 	
     /**
      * Convenience method: Treating the Shape as a Rectangle
      * returns whether shape1's right side has been collided with by 
      * point2
      */
 	public boolean hitRight(Shape shape1, Point2D point2){
 		double hitdirection = getQuickDirection(shape1,point2);
 		if((hitdirection-getQuickDirection(shape1, ShapeMeasurements.getTopRightCorner(shape1) ) > 0 ||
 			(hitdirection-getQuickDirection(shape1, ShapeMeasurements.getBottomRightCorner(shape1))) <= 0)){
 					return true;
 				}
 		return false;
 	}
 	
     /**
      * Convenience method: Treating the Shape as a Rectangle
      * returns whether shape1's left side has been collided with by 
      * point2
      */
 	public boolean hitLeft(Shape shape1, Point2D point2){
 		double hitdirection = getQuickDirection(shape1,point2);
 		if((hitdirection-getQuickDirection(shape1, ShapeMeasurements.getBottomLeftCorner(shape1) ) > 0 &&
 			(hitdirection-getQuickDirection(shape1, ShapeMeasurements.getTopLeftCorner(shape1))) <= 0)){
 					return true;
 				}
 		return false;
 	}
 	
     /**
      * Convenience method: Treating the Shape as a Rectangle
      * returns whether shape1's bottom side has been collided with by 
      * point2
      */
 	public boolean hitBottom(Shape shape1, Point2D point2){
 		double hitdirection = getQuickDirection(shape1,point2);
 		if((hitdirection-getQuickDirection(shape1, ShapeMeasurements.getBottomLeftCorner(shape1) ) <= 0 &&
 			(hitdirection-getQuickDirection(shape1, ShapeMeasurements.getBottomRightCorner(shape1))) > 0)){
 					return true;
 				}
 		return false;
 	}
 	
     /**
      * Convenience method: Treating the Shapes as a Rectangles
      * returns whether shape1's top side has been collided with by 
      * shape2
      */
 	public boolean hitTop(Shape shape1, Shape shape2){
 		if(quickDetectCollision(shape1, ShapeMeasurements.getBottomRightCorner(shape2)))
 			return hitTop(shape1, ShapeMeasurements.getBottomRightCorner(shape2));	
 		else if(quickDetectCollision(shape1, ShapeMeasurements.getBottomLeftCorner(shape2)))
 			return hitTop(shape1, ShapeMeasurements.getBottomLeftCorner(shape2));	
 		return false;
 	}
     /**
      * Convenience method: Treating the Shapes as a Rectangles
      * returns whether shape1's right side has been collided with by 
      * shape2
      */
 	public boolean hitRight(Shape shape1, Shape shape2){
 		if(quickDetectCollision(shape1, ShapeMeasurements.getBottomLeftCorner(shape2))){
 			return hitRight(shape1, ShapeMeasurements.getBottomLeftCorner(shape2));
 		}
 		else if(quickDetectCollision(shape1, ShapeMeasurements.getTopLeftCorner(shape2))){
 			return hitRight(shape1, ShapeMeasurements.getTopLeftCorner(shape2));	
 		}
 		return false;
 	}
 	
     /**
      * Convenience method: Treating the Shapes as a Rectangles
      * returns whether shape1's bottom side has been collided with by 
      * shape2
      */
 	public boolean hitBottom(Shape shape1, Shape shape2){
 		if(quickDetectCollision(shape1, ShapeMeasurements.getTopRightCorner(shape2)))
 			return hitBottom(shape1, ShapeMeasurements.getTopRightCorner(shape2));	
 		else if(quickDetectCollision(shape1, ShapeMeasurements.getTopLeftCorner(shape2)))
 			return hitBottom(shape1, ShapeMeasurements.getTopLeftCorner(shape2));	
 		return false;
 	}
     /**
      * Convenience method: Treating the Shapes as a Rectangles
      * returns whether shape1's left side has been collided with by 
      * shape2
      */
 	public boolean hitLeft(Shape shape1, Shape shape2){
 		if(quickDetectCollision(shape1, ShapeMeasurements.getBottomRightCorner(shape2)))
 			return hitLeft(shape1, ShapeMeasurements.getBottomRightCorner(shape2));	
 		else if(quickDetectCollision(shape1, ShapeMeasurements.getTopRightCorner(shape2)))
 			return hitLeft(shape1, ShapeMeasurements.getTopRightCorner(shape2));	
 		return false;
 	}
 	
     /**
      * Convenience method:
      * Returns direction from center of Shape to a Point.
      */
 	private double getQuickDirection (Shape shape1, Point2D point2){
 			Location center = (Location) ShapeMeasurements.getQuickCenter(shape1);
 			return Vector.SanitizeAngle(Vector.angleBetween(point2, center));
 	}
 	
 	
 	 /**
 	  * Convenience method to track rectangles
 	 */
 	public static List trackLine(Location loc1, Location loc2, int NumberOfPoints){
 		List line  = new LinkedList<Location>();
 		Vector vec = new Vector(loc1,loc2);
 		double x = loc1.getX();
 		double y = loc1.getX();
 		for(int i = 0; i<NumberOfPoints; i++){
 			line.add(new Location(x,y));
 			x += vec.getMagnitude()*Math.cos(Math.toRadians(vec.getDirection()));
 			y += vec.getMagnitude()*Math.sin(Math.toRadians(vec.getDirection()));
 		}
 		return line;
 	}
 	
 	private boolean pointWithinPath(PathIterator path1, Location center1, Location point2, double precision){
 		float[] coord1 = new float[6];
 		Vector vector2 = center1.difference(point2);
 		while(!path1.isDone()){
 			path1.currentSegment(coord1);
 			Location loc = new Location(coord1[0], coord1[1]);
 			Vector vector1 = center1.difference(loc);
 			if(Math.abs(Vector.SanitizeAngle(vector1.getAngleBetween(vector2)))<precision&&
 					vector1.getMagnitude()>= vector2.getMagnitude()){
 				return true;
 			}
 			path1.next();
 		}
 		return false;
 	}
 
 
 }
