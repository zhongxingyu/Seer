 package com.github.zinfidel.jarvis_march.geometry;
 
 import static java.lang.Math.atan2;
 import static java.lang.Math.PI;
 
 /**
  * Represents a 2D vector (or line) as on a Cartesian plane.
  * 
  * @author Zach Friedland
  */
 public class Vector {
 
     /** The start point (origin) of this vector. */
     public final Point start;
 
     /** The end point of this vector. */
     public final Point end;
 
     /** The components of the position vector of this vector. */
     public final Point position;
 
     /** The length of this vector. */
     public final double magnitude;
 
     /** The anti-clockwise angle from the X-Axis to this vector. */
     public final double angle;
     
     
     /**
      * Construct a vector given a start and end point.
      * 
      * @param start The start point of this vector.
      * @param end The end point of this vector.
      */
     public Vector(Point start, Point end) {
 	this.start = start;
 	this.end = end;
 	this.position = end.Minus(start);
 	this.magnitude = start.DistanceTo(end);
 	this.angle = RefAngle(position);
     }
     
     
     /**
      * Calculates the anti-clockwise angle from the X-Axis to a vector
      * starting at the origin and pointing to <code>position</code>.
      * 
      * @param position Components of a position vector.
     * @return An angle in the range of [0 <= θ < 2π].
      */
     private static double RefAngle(Point position) {
 	double theta = atan2(position.y, position.x); // Implicit int -> double
 	
 	return (theta >= 0.0d) ? theta : (PI - theta);
     }
     
     /**
      * Calculates the anti-clockwise angle between this vector
      * and <code>vector</code>.
      * 
      * @param vector The vector to calculate the angle to.
     * @return An angle in the range of [0 <= θ < 2π].
      */
     public double AngleTo(Vector vector) {
 	return vector.angle - this.angle;
     }
 }
