 /* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: GenMath.java
 *
 * Copyright (c) 2003 Sun Microsystems and Static Free Software
 *
 * Electric(tm) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Electric(tm) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Electric(tm); see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, Mass 02111-1307, USA.
 */
 package com.sun.electric.database.geometry;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.AffineTransform;
 
 /**
  * General Math Functions. If you are working in Database Units, you
  * should be using DBMath instead.
  */
 public class GenMath
 {
     /**
      * Class to define an Integer-like object that can be modified.
      */
     public static class MutableInteger
     {
         private int value;
         
         /**
          * Constructor creates a MutableInteger object with an initial value.
          * @param value the initial value.
          */
         public MutableInteger(int value) { this.value = value; }
 
         /**
          * Method to change the value of this MutableInteger.
          * @param value the new value.
          */
         public void setValue(int value) { this.value = value; }
 
         /**
          * Method to increment this MutableInteger by 1.
          */
         public void increment() { value++; }
 
         /**
          * Method to return the value of this MutableInteger.
          * @return the current value of this MutableInteger.
          */
         public int intValue() { return value; }
 
         /**
          * Returns a printable version of this MutableInteger.
          * @return a printable version of this MutableInteger.
          */
         public String toString() { return Integer.toString(value); }
     }
 
     /**
      * Class to define an Double-like object that can be modified.
      */
     public static class MutableDouble
     {
         private double value;
 
         /**
          * Constructor creates a MutableDouble object with an initial value.
          * @param value the initial value.
          */
         public MutableDouble(double value) { this.value = value; }
 
         /**
          * Method to change the value of this MutableDouble.
          * @param value the new value.
          */
         public void setValue(double value) { this.value = value; }
 
         /**
          * Method to return the value of this MutableDouble.
          * @return the current value of this MutableDouble.
          */
         public double doubleValue() { return value; }
 
         /**
          * Returns a printable version of this MutableDouble.
          * @return a printable version of this MutableDouble.
          */
         public String toString() { return Double.toString(value); }
     }
 
     /**
      * Method to compare two objects for equality.
      * This does more than a simple "equal" because they may have the same value
      * but have diffent type (one Float, the other Double).
      * @param first the first object to compare.
      * @param second the second object to compare.
      * @return true if they are equal.
      */
     public static boolean objectsReallyEqual(Object first, Object second)
     {
         // a simple test
         if (first.equals(second)) return true;
 
         // better comparison of numbers (because one may be Float and the other Double)
         boolean firstNumeric = false, secondNumeric = false;
         double firstValue = 0, secondValue = 0;
         if (first instanceof Float) { firstNumeric = true; firstValue = ((Float)first).floatValue(); } else
             if (first instanceof Double) { firstNumeric = true; firstValue = ((Double)first).doubleValue(); } else
                 if (first instanceof Integer) { firstNumeric = true; firstValue = ((Integer)first).intValue(); }
         if (second instanceof Float) { secondNumeric = true; secondValue = ((Float)second).floatValue(); } else
             if (second instanceof Double) { secondNumeric = true; secondValue = ((Double)second).doubleValue(); } else
                 if (second instanceof Integer) { secondNumeric = true; secondValue = ((Integer)second).intValue(); }
         if (firstNumeric && secondNumeric)
         {
             if (firstValue == secondValue) return true;
         }
         return false;
     }
 
     /** A transformation matrix that does nothing (identity). */
     public static final AffineTransform MATID = new AffineTransform();
 
     /**
      * Method to return the angle between two points.
      * @param end1 the first point.
      * @param end2 the second point.
      * @return the angle between the points (in tenth-degrees).
      */
     public static int figureAngle(Point2D end1, Point2D end2)
     {
         double dx = end2.getX()-end1.getX();
         double dy = end2.getY()-end1.getY();
         if (dx == 0.0 && dy == 0.0)
         {
             System.out.println("Warning: domain violation while figuring angle");
             return 0;
         }
         double angle = Math.atan2(dy, dx);
         if (angle < 0) angle += Math.PI*2;
         int iAngle = (int)(angle * 1800.0 / Math.PI);
         return iAngle;
     }
 
     /**
      * Method to return the angle between two points.
      * @param end1 the first point.
      * @param end2 the second point.
      * @return the angle between the points (in radians).
      */
     public static double figureAngleRadians(Point2D end1, Point2D end2)
     {
         double dx = end2.getX() - end1.getX();
         double dy = end2.getY() - end1.getY();
         if (dx == 0.0 && dy == 0.0)
         {
             System.out.println("Warning: domain violation while figuring angle in radians");
             return 0;
         }
         double ang = Math.atan2(dy, dx);
         if (ang < 0.0) ang += Math.PI * 2.0;
         return ang;
     }
 
     /**
      * Method to return the sum of two points.
      * @param p the first point.
      * @param dx the X component of the second point
      * @param dy the T component of the second point
      * @return the sum of two points.
      */
     public static Point2D addPoints(Point2D p, double dx, double dy)
     {
         return new Point2D.Double(p.getX()+dx, p.getY()+dy);
     }
 
     /**
      * Method to tell whether a point is on a given line segment.
     * <p>NOTE: If you are comparing Electric database units, DO NOT
     * use this method. Use the corresponding method from DBMath.
      * @param end1 the first end of the line segment.
      * @param end2 the second end of the line segment.
      * @param pt the point in question.
      * @return true if the point is on the line segment.
      */
     public static boolean isOnLine(Point2D end1, Point2D end2, Point2D pt)
     {
         // trivial rejection if point not in the bounding box of the line
         if (pt.getX() < Math.min(end1.getX(), end2.getX())) return false;
         if (pt.getX() > Math.max(end1.getX(), end2.getX())) return false;
         if (pt.getY() < Math.min(end1.getY(), end2.getY())) return false;
         if (pt.getY() > Math.max(end1.getY(), end2.getY())) return false;
 
         // handle manhattan cases specially
         if (end1.getX() == end2.getX())
         {
             if (pt.getX() == end1.getX()) return true;
             return false;
         }
         if (end1.getY() == end2.getY())
         {
             if (pt.getY() == end1.getY()) return true;
             return false;
         }
 
         // handle nonmanhattan
         if ((pt.getX()-end1.getX()) * (end2.getY()-end1.getY()) == (pt.getY()-end1.getY()) * (end2.getX()-end1.getX())) return true;
         return false;
     }
 
     /**
      * Method to find the point on a line segment that is closest to a given point.
      * @param p1 one end of the line segment.
      * @param p2 the other end of the line segment.
      * @param pt the point near the line segment.
      * @return a point on the line segment that is closest to "pt".
      * The point is guaranteed to be between the two points that define the segment.
      */
     public static Point2D closestPointToSegment(Point2D p1, Point2D p2, Point2D pt)
     {
         // find closest point on line
         Point2D pi = closestPointToLine(p1, p2, pt);
 
         // see if that intersection point is actually on the segment
         if (pi.getX() >= Math.min(p1.getX(), p2.getX()) &&
                 pi.getX() <= Math.max(p1.getX(), p2.getX()) &&
                 pi.getY() >= Math.min(p1.getY(), p2.getY()) &&
                 pi.getY() <= Math.max(p1.getY(), p2.getY()))
         {
             // it is
             return pi;
         }
 
         // intersection not on segment: choose one endpoint as the closest
         double dist1 = pt.distance(p1);
         double dist2 = pt.distance(p2);
         if (dist2 < dist1) return p2;
         return p1;
     }
 
     /**
      * Method to find the point on a line that is closest to a given point.
      * @param p1 one end of the line.
      * @param p2 the other end of the line.
      * @param pt the point near the line.
      * @return a point on the line that is closest to "pt".
      * The point is not guaranteed to be between the two points that define the line.
      */
     public static Point2D closestPointToLine(Point2D p1, Point2D p2, Point2D pt)
     {
         // special case for horizontal line
         if (p1.getY() == p2.getY())
         {
             return new Point2D.Double(pt.getX(), p1.getY());
         }
 
         // special case for vertical line
         if (p1.getX() == p2.getX())
         {
             return new Point2D.Double(p1.getX(), pt.getY());
         }
 
         // compute equation of the line
         double m = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX());
         double b = -p1.getX() * m + p1.getY();
 
         // compute perpendicular to line through the point
         double mi = -1.0 / m;
         double bi = -pt.getX() * mi + pt.getY();
 
         // compute intersection of the lines
         double t = (bi-b) / (m-mi);
         return new Point2D.Double(t, m * t + b);
     }
 
     /**
      * Method to compute the distance between point (x,y) and the line that runs
      * from (x1,y1) to (x2,y2).
      */
     public static double distToLine(Point2D l1, Point2D l2, Point2D pt)
     {
         // get point on line from (x1,y1) to (x2,y2) close to (x,y)
         double x1 = l1.getX();   double y1 = l1.getY();
         double x2 = l2.getX();   double y2 = l2.getY();
         if (doublesEqual(x1, x2) && doublesEqual(y1, y2))
         {
             return pt.distance(l1);
         }
         int ang = figureAngle(l1, l2);
         Point2D iPt = intersect(l1, ang, pt, ang+900);
         double iX = iPt.getX();
         double iY = iPt.getY();
         if (doublesEqual(x1, x2)) iX = x1;
         if (doublesEqual(y1, y2)) iY = y1;
 
         // make sure (ix,iy) is on the segment from (x1,y1) to (x2,y2)
         if (iX < Math.min(x1,x2) || iX > Math.max(x1,x2) ||
                 iY < Math.min(y1,y2) || iY > Math.max(y1,y2))
         {
             if (Math.abs(iX-x1) + Math.abs(iY-y1) < Math.abs(iX-x2) + Math.abs(iY-y2))
             {
                 iX = x1;   iY = y1;
             } else
             {
                 iX = x2;   iY = y2;
             }
         }
         iPt.setLocation(iX, iY);
         return iPt.distance(pt);
     }
 
     /**
      * Method to find the two possible centers for a circle given a radius and two edge points.
      * @param r the radius of the circle.
      * @param p1 one point on the edge of the circle.
      * @param p2 the other point on the edge of the circle.
      * @param d the distance between the two points.
      * @return an array of two Point2Ds, either of which could be the center.
      * Returns null if there are no possible centers.
      * This code was written by John Mohammed of Schlumberger.
      */
     public static Point2D [] findCenters(double r, Point2D p1, Point2D p2, double d)
     {
         // quit now if the circles concentric
         if (p1.getX() == p2.getX() && p1.getY() == p2.getY()) return null;
 
         // find the intersections, if any
         double r2 = r * r;
         double delta_1 = -d / 2.0;
         double delta_12 = delta_1 * delta_1;
 
         // quit if there are no intersections
         if (r2 < delta_12) return null;
 
         // compute the intersection points
         double delta_2 = Math.sqrt(r2 - delta_12);
         double x1 = p2.getX() + ((delta_1 * (p2.getX() - p1.getX())) + (delta_2 * (p2.getY() - p1.getY()))) / d;
         double y1 = p2.getY() + ((delta_1 * (p2.getY() - p1.getY())) + (delta_2 * (p1.getX() - p2.getX()))) / d;
         double x2 = p2.getX() + ((delta_1 * (p2.getX() - p1.getX())) + (delta_2 * (p1.getY() - p2.getY()))) / d;
         double y2 = p2.getY() + ((delta_1 * (p2.getY() - p1.getY())) + (delta_2 * (p2.getX() - p1.getX()))) / d;
         Point2D [] retArray = new Point2D[2];
         retArray[0] = new Point2D.Double(x1, y1);
         retArray[1] = new Point2D.Double(x2, y2);
         return retArray;
     }
 
     /**
      * Method to tell whether a point is inside of a bounds.
     * <p>NOTE: If you are comparing Electric database units, DO NOT
     * use this method. Use the corresponding method from DBMath.
      * The reason that this is necessary is that Rectangle2D.contains requires that
      * the point be INSIDE of the bounds, whereas this method accepts a point that
      * is ON the bounds.
      * @param pt the point in question.
      * @param bounds the bounds being tested.
      * @return true if the point is in the bounds.
      */
     public static boolean pointInRect(Point2D pt, Rectangle2D bounds)
     {
         if (pt.getX() < bounds.getMinX()) return false;
         if (pt.getX() > bounds.getMaxX()) return false;
         if (pt.getY() < bounds.getMinY()) return false;
         if (pt.getY() > bounds.getMaxY()) return false;
         return true;
     }
 
     /**
      * Method to transform a Rectangle2D by a given transformation.
      * @param bounds the Rectangle to transform.
      * It is transformed "in place" (its coordinates are overwritten).
      * @param xform the transformation matrix.
      */
     public static void transformRect(Rectangle2D bounds, AffineTransform xform)
     {
         Point2D [] corners = Poly.makePoints(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
         xform.transform(corners, 0, corners, 0, 4);
         double lX = corners[0].getX();
         double lY = corners[0].getY();
         double hX = lX;
         double hY = lY;
         for(int i=1; i<4; i++)
         {
             if (corners[i].getX() < lX) lX = corners[i].getX();
             if (corners[i].getX() > hX) hX = corners[i].getX();
             if (corners[i].getY() < lY) lY = corners[i].getY();
             if (corners[i].getY() > hY) hY = corners[i].getY();
         }
         bounds.setRect(lX, lY, hX-lX, hY-lY);
     }
 
     /**
      * Method to determine the intersection of two lines and return that point.
      * @param p1 a point on the first line.
      * @param ang1 the angle of the first line (in tenth degrees).
      * @param p2 a point on the second line.
      * @param ang2 the angle of the second line (in tenth degrees).
      * @return a point that is the intersection of the lines.
      * Returns null if there is no intersection point.
      */
     public static Point2D intersect(Point2D p1, int ang1, Point2D p2, int ang2)
     {
         // cannot handle lines if they are at the same angle
         while (ang1 < 0) ang1 += 3600;
         if (ang1 >= 3600) ang1 %= 3600;
         while (ang2 < 0) ang2 += 3600;
         if (ang2 >= 3600) ang2 %= 3600;
         if (ang1 == ang2) return null;
 
         // also cannot handle lines that are off by 180 degrees
         int amin = ang2, amax = ang1;
         if (ang1 < ang2)
         {
             amin = ang1;   amax = ang2;
         }
         if (amin + 1800 == amax) return null;
 
         double fa1 = sin(ang1);
         double fb1 = -cos(ang1);
         double fc1 = -fa1 * p1.getX() - fb1 * p1.getY();
         double fa2 = sin(ang2);
         double fb2 = -cos(ang2);
         double fc2 = -fa2 * p2.getX() - fb2 * p2.getY();
         if (Math.abs(fa1) < Math.abs(fa2))
         {
             double fswap = fa1;   fa1 = fa2;   fa2 = fswap;
             fswap = fb1;   fb1 = fb2;   fb2 = fswap;
             fswap = fc1;   fc1 = fc2;   fc2 = fswap;
         }
         double fy = (fa2 * fc1 / fa1 - fc2) / (fb2 - fa2*fb1/fa1);
         return new Point2D.Double((-fb1 * fy - fc1) / fa1, fy);
     }
 
     /**
      * Method to determine the intersection of two lines and return that point.
      * @param p1 a point on the first line.
      * @param ang1 the angle of the first line (in radians).
      * @param p2 a point on the second line.
      * @param ang2 the angle of the second line (in radians).
      * @return a point that is the intersection of the lines.
      * Returns null if there is no intersection point.
      */
     public static Point2D intersectRadians(Point2D p1, double ang1, Point2D p2, double ang2)
     {
         // cannot handle lines if they are at the same angle
         if (doublesEqual(ang1, ang2)) return null;
 
         // also at the same angle if off by 180 degrees
         double fMin = ang2, fMax = ang2;
         if (ang1 < ang2) { fMin = ang1; fMax = ang2; }
         if (doublesEqual(fMin + Math.PI, fMax)) return null;
 
         double fa1 = Math.sin(ang1);
         double fb1 = -Math.cos(ang1);
         double fc1 = -fa1 * p1.getX() - fb1 * p1.getY();
         double fa2 = Math.sin(ang2);
         double fb2 = -Math.cos(ang2);
         double fc2 = -fa2 * p2.getX() - fb2 * p2.getY();
         if (Math.abs(fa1) < Math.abs(fa2))
         {
             double fswap = fa1;   fa1 = fa2;   fa2 = fswap;
             fswap = fb1;   fb1 = fb2;   fb2 = fswap;
             fswap = fc1;   fc1 = fc2;   fc2 = fswap;
         }
         double fy = (fa2 * fc1 / fa1 - fc2) / (fb2 - fa2*fb1/fa1);
         return new Point2D.Double(fy, (-fb1 * fy - fc1) / fa1);
     }
 
     /** smallest such that 1.0+DBL_EPSILON != 1.0 */	private static double DBL_EPSILON = 2.2204460492503131e-016;
 
     /**
      * Method to round a value to the nearest increment.
      * @param a the value to round.
      * @param nearest the increment to which it should be rounded.
      * @return the value, rounded to the nearest increment.
      * For example:<BR>
      * toNearest(10.3, 1.0) = 10.0<BR>
      * toNearest(10.3, 0.1) = 10.3<BR>
      * toNearest(10.3, 0.5) = 10.5
      */
     public static double toNearest(double a, double nearest)
     {
         long v = Math.round(a / nearest);
         return v * nearest;
     }
 
     /**
      * Method to compare two double-precision numbers within an acceptable epsilon.
      * @param a the first number.
      * @param b the second number.
      * @return true if the numbers are equal to 16 decimal places.
      */
     public static boolean doublesEqual(double a, double b)
     {
         if (Math.abs(a-b) <= DBL_EPSILON) return true;
         return false;
     }
 
     /**
      * Method to compare two double-precision numbers within an approximate epsilon.
     * <p>NOTE: If you are comparing Electric database units, DO NOT
     * use this method. Use the corresponding method from DBMath.
      * @param a the first number.
      * @param b the second number.
      * @return true if the numbers are approximately equal (to a few decimal places).
      */
     public static boolean doublesClose(double a, double b)
     {
         if (b != 0)
         {
             double ratio = a / b;
             if (ratio < 1.00001 && ratio > 0.99999) return true;
         }
         if (Math.abs(a-b) < 0.001) return true;
         return false;
     }
 
     /**
      * Method to compare two double-precision coordinates within an approximate epsilon.
     * <p>NOTE: If you are comparing Electric database units, DO NOT
     * use this method. Use the corresponding method from DBMath.
      * @param a the first point.
      * @param b the second point.
      * @return true if the points are approximately equal (to a few decimal places).
      */
     public static boolean pointsClose(Point2D a, Point2D b)
     {
         if (doublesClose(a.getX(), b.getX()) &&
                 doublesClose(a.getY(), b.getY())) return true;
         return false;
     }
 
     /**
      * Method to round floating-point values to sensible quantities.
      * Rounds these numbers to the nearest thousandth.
      * @param a the value to round.
      * @return the rounded value.
      */
     public static double smooth(double a)
     {
         long i = Math.round(a * 1000.0);
         return i / 1000.0;
     }
 
     private static final int LEFT    = 1;
     private static final int RIGHT   = 2;
     private static final int BOTTOM  = 4;
     private static final int TOP     = 8;
 
     /**
      * Method to clip a line against a rectangle.
      * @param from one end of the line.
      * @param to the other end of the line.
      * @param lX the low X bound of the clip.
      * @param hX the high X bound of the clip.
      * @param lY the low Y bound of the clip.
      * @param hY the high Y bound of the clip.
      * The points are modified to fit inside of the clip area.
      * @return true if the line is not visible.
      */
     public static boolean clipLine(Point2D from, Point2D to, double lX, double hX, double lY, double hY)
     {
         for(;;)
         {
             // compute code bits for "from" point
             int fc = 0;
             if (from.getX() < lX) fc |= LEFT; else
                 if (from.getX() > hX) fc |= RIGHT;
             if (from.getY() < lY) fc |= BOTTOM; else
                 if (from.getY() > hY) fc |= TOP;
 
             // compute code bits for "to" point
             int tc = 0;
             if (to.getX() < lX) tc |= LEFT; else
                 if (to.getX() > hX) tc |= RIGHT;
             if (to.getY() < lY) tc |= BOTTOM; else
                 if (to.getY() > hY) tc |= TOP;
 
             // look for trivial acceptance or rejection
             if (fc == 0 && tc == 0) return false;
             if (fc == tc || (fc & tc) != 0) return true;
 
             // make sure the "from" side needs clipping
             if (fc == 0)
             {
                 double x = from.getX();
                 double y = from.getY();
                 from.setLocation(to);
                 to.setLocation(x, y);
                 int t = fc;    fc = tc;     tc = t;
             }
 
             if ((fc&LEFT) != 0)
             {
                 if (to.getX() == from.getX()) return true;
                 double t = (to.getY() - from.getY()) * (lX - from.getX()) / (to.getX() - from.getX());
                 from.setLocation(lX, from.getY() + t);
             }
             if ((fc&RIGHT) != 0)
             {
                 if (to.getX() == from.getX()) return true;
                 double t = (to.getY() - from.getY()) * (hX - from.getX()) / (to.getX() - from.getX());
                 from.setLocation(hX, from.getY() + t);
             }
             if ((fc&BOTTOM) != 0)
             {
                 if (to.getY() == from.getY()) return true;
                 double t = (to.getX() - from.getX()) * (lY - from.getY()) / ( to.getY() - from.getY());
                 from.setLocation(from.getX() + t, lY);
             }
             if ((fc&TOP) != 0)
             {
                 if (to.getY() == from.getY()) return true;
                 double t = (to.getX() - from.getX()) * (hY - from.getY()) / (to.getY() - from.getY());
                 from.setLocation(from.getX() + t, hY);
             }
         }
     }
 
     private static final double [] sineTable = {
         0.0,0.0017453283658983088,0.003490651415223732,0.00523596383141958,0.0069812602979615525,0.008726535498373935,
         0.010471784116245792,0.012217000835247169,0.013962180339145272,0.015707317311820675,0.01745240643728351,0.019197442399689665,
         0.020942419883356957,0.022687333572781358,0.024432178152653153,0.02617694830787315,0.02792163872356888,0.029666244085110757,
         0.03141075907812829,0.033155178388526274,0.03489949670250097,0.036643708706556276,0.03838780908751994,0.04013179253255973,
         0.04187565372919962,0.043619387365336,0.04536298812925378,0.04710645070964266,0.04884976979561326,0.05059294007671331,
         0.05233595624294383,0.05407881298477529,0.0558215049931638,0.057564026959567284,0.05930637357596162,0.061048539534856866,
         0.06279051952931337,0.06453230825295798,0.06627390040000014,0.06801529066524817,0.0697564737441253,0.07149744433268591,
         0.07323819712763169,0.0749787268263277,0.07671902812681863,0.07845909572784494,0.08019892432885892,0.08193850863004093,
         0.08367784333231548,0.08541692313736746,0.08715574274765817,0.08889429686644151,0.09063258019778016,0.09237058744656158,
         0.09410831331851431,0.095845752520224,0.09758289975914947,0.099319749743639,0.10105629718294634,0.1027925367872468,
         0.10452846326765346,0.1062640713362332,0.10799935570602284,0.10973431109104527,0.11146893220632548,0.11320321376790671,
         0.11493715049286661,0.11667073709933316,0.11840396830650095,0.1201368388346471,0.12186934340514746,0.12360147674049271,
         0.12533323356430426,0.12706460860135046,0.1287955965775628,0.13052619222005157,0.13225639025712244,0.13398618541829205,
         0.13571557243430438,0.13744454603714665,0.13917310096006544,0.14090123193758267,0.14262893370551163,0.1443562010009732,
         0.14608302856241162,0.14780941112961063,0.14953534344370953,0.1512608202472192,0.15298583628403806,0.1547103862994681,
         0.15643446504023087,0.15815806725448353,0.15988118769183485,0.1616038211033611,0.16332596224162227,0.16504760586067765,
         0.16676874671610226,0.16848937956500257,0.17020949916603254,0.17192910027940955,0.17364817766693033,0.1753667260919871,
         0.1770847403195833,0.17880221511634958,0.18051914525055998,0.18223552549214747,0.18395135061272017,0.1856666153855772,
         0.1873813145857246,0.18909544298989128,0.19080899537654483,0.19252196652590742,0.19423435121997196,0.1959461442425177,
         0.19765734037912613,0.1993679344171972,0.20107792114596468,0.2027872953565125,0.20449605184179032,0.20620418539662963,
         0.20791169081775931,0.20961856290382183,0.21132479645538865,0.21303038627497656,0.2147353271670632,0.21643961393810285,
         0.21814324139654254,0.21984620435283753,0.2215484976194673,0.22325011601095135,0.22495105434386498,0.22665130743685505,
         0.22835087011065575,0.2300497371881044,0.23174790349415733,0.2334453638559054,0.23514211310259,0.23683814606561868,
         0.23853345757858088,0.24022804247726373,0.2419218955996677,0.2436150117860225,0.24530738587880258,0.2469990127227429,
         0.2486898871648548,0.2503800040544414,0.2520693582431136,0.25375794458480566,0.25544575793579055,0.25713279315469617,
         0.25881904510252074,0.26050450864264835,0.2621891786408647,0.26387304996537286,0.2655561174868088,0.26723837607825685,
         0.2689198206152657,0.27060044597586363,0.27228024704057435,0.27395921869243245,0.27563735581699916,0.2773146533023778,
         0.2789911060392293,0.28066670892078777,0.2823414568428764,0.2840153447039226,0.28568836740497355,0.287360519849712,
         0.2890317969444716,0.2907021935982525,0.29237170472273677,0.29404032523230395,0.29570805004404666,0.29737487407778596,
         0.29904079225608665,0.3007057995042731,0.30236989075044446,0.3040330609254903,0.30569530496310565,0.30735661779980705,
         0.3090169943749474,0.3106764296307318,0.31233491851223255,0.31399245596740494,0.31564903694710245,0.3173046564050921,
         0.3189593092980699,0.32061299058567627,0.3222656952305111,0.3239174181981494,0.3255681544571567,0.3272178989791039,
         0.32886664673858323,0.330514392713223,0.3321611318837033,0.3338068592337709,0.335451569750255,0.3370952584230821,
         0.3387379202452914,0.34037955021305016,0.3420201433256687,0.34365969458561607,0.34529819899853464,0.34693565157325584,
         0.3485720473218152,0.35020738125946743,0.3518416484047018,0.3534748437792571,0.35510696240813705,0.3567379993196252,
         0.35836794954530027,0.3599968081200512,0.3616245700820923,0.36325123047297836,0.3648767843376196,0.36650122672429725,
         0.3681245526846779,0.3697467572738293,0.3713678355502348,0.37298778257580895,0.37460659341591207,0.3762242631393656,
         0.3778407868184671,0.3794561595290051,0.3810703763502741,0.3826834323650898,0.3842953226598037,0.38590604232431863,
         0.38751558645210293,0.38912395014020623,0.39073112848927377,0.39233711660356146,0.3939419095909511,0.39554550256296495,
         0.3971478906347806,0.3987490689252462,0.4003490325568949,0.4019477766559601,0.40354529635239,0.4051415867798625,
         0.40673664307580015,0.40833046038138493,0.4099230338415728,0.41151435860510877,0.41310442982454176,0.414693242656239,
         0.41628079226040116,0.41786707380107674,0.4194520824461771,0.421035813367491,0.4226182617406994,0.4241994227453902,
         0.42577929156507266,0.4273578633871924,0.4289351334031459,0.43051109680829514,0.4320857488019823,0.43365908458754426,
         0.43523109937232746,0.43680178836770217,0.4383711467890774,0.4399391698559151,0.4415058527917452,0.44307119082417973,
         0.4446351791849275,0.44619781310980877,0.44775908783876966,0.4493189986158966,0.45087754068943076,0.4524347093117827,
         0.45399049973954675,0.4555449072335155,0.4570979270586942,0.45864955448431494,0.46019978478385165,0.4617486132350339,
         0.4632960351198617,0.4648420457246196,0.4663866403398912,0.46792981426057334,0.46947156278589075,0.47101188121940996,
         0.47255076486905395,0.4740882090471163,0.47562420907027525,0.4771587602596084,0.4786918579406068,0.48022349744318893,
         0.4817536741017153,0.4832823832550024,0.48480962024633695,0.48633538042349045,0.4878596591387326,0.4893824517488462,
         0.4909037536151409,0.49242356010346716,0.493941866584231,0.4954586684324075,0.49697396102755526,0.49848773975383026,
         0.49999999999999994,0.5015107371594573,0.503019946630235,0.5045276238150193,0.5060337641211637,0.5075383629607041,
         0.5090414157503713,0.5105429179116057,0.5120428648705715,0.51354125205817,0.5150380749100542,0.5165333288666418,
         0.5180270093731302,0.5195191118795094,0.5210096318405764,0.5224985647159488,0.5239859059700791,0.5254716510722678,
         0.5269557954966776,0.5284383347223471,0.5299192642332049,0.5313985795180829,0.53287627607073,0.5343523493898263,
         0.5358267949789967,0.5372996083468239,0.5387707850068629,0.540240320477655,0.5417082102827397,0.5431744499506707,
         0.544639035015027,0.5461019610144291,0.5475632234925503,0.5490228179981317,0.5504807400849956,0.5519369853120581,
         0.5533915492433441,0.5548444274479992,0.5562956155003048,0.5577451089796901,0.5591929034707469,0.5606389945632416,
         0.5620833778521306,0.5635260489375715,0.5649670034249379,0.5664062369248328,0.5678437450531012,0.5692795234308442,
         0.5707135676844316,0.5721458734455162,0.573576436351046,0.5750052520432786,0.5764323161697932,0.5778576243835053,
         0.5792811723426788,0.5807029557109398,0.5821229701572894,0.5835412113561175,0.5849576749872154,0.5863723567357892,
         0.5877852522924731,0.589196357353342,0.5906056676199254,0.5920131787992196,0.5934188866037015,0.5948227867513413,
         0.5962248749656158,0.5976251469755212,0.5990235985155858,0.600420225325884,0.6018150231520482,0.6032079877452825,
         0.6045991148623747,0.605988400265711,0.6073758397232867,0.6087614290087207,0.6101451639012676,0.6115270401858311,
         0.6129070536529765,0.6142852000989432,0.6156614753256583,0.6170358751407485,0.6184083953575542,0.61977903179514,
         0.6211477802783103,0.6225146366376195,0.6238795967093861,0.6252426563357052,0.6266038113644604,0.6279630576493379,
         0.6293203910498374,0.6306758074312863,0.6320293026648508,0.6333808726275502,0.6347305132022676,0.636078220277764,
         0.6374239897486897,0.6387678175155976,0.6401096994849556,0.6414496315691578,0.6427876096865393,0.6441236297613865,
         0.6454576877239505,0.6467897795104596,0.6481199010631309,0.6494480483301835,0.6507742172658509,0.6520984038303922,
         0.6534206039901054,0.6547408137173397,0.6560590289905073,0.6573752457940958,0.6586894601186803,0.6600016679609367,
         0.6613118653236518,0.6626200482157375,0.6639262126522416,0.6652303546543609,0.6665324702494525,0.6678325554710466,
         0.6691306063588582,0.6704266189587991,0.6717205893229902,0.6730125135097733,0.6743023875837234,0.6755902076156601,
         0.6768759696826607,0.6781596698680706,0.6794413042615165,0.6807208689589178,0.6819983600624985,0.6832737736807992,
         0.6845471059286886,0.6858183529273763,0.687087510804423,0.6883545756937539,0.6896195437356697,0.6908824110768583,
         0.6921431738704068,0.693401828275813,0.6946583704589974,0.6959127965923143,0.6971651028545645,0.6984152854310058,
         0.6996633405133654,0.7009092642998509,0.7021530529951624,0.7033947028105039,0.7046342099635946,0.705871570678681,
         0.7071067811865475,0.7083398377245288,0.7095707365365209,0.7107994738729925,0.7120260459909965,0.7132504491541816,
         0.7144726796328033,0.7156927337037359,0.7169106076504826,0.7181262977631888,0.7193398003386512,0.7205511116803304,
         0.7217602280983622,0.7229671459095681,0.7241718614374675,0.7253743710122875,0.7265746709709759,0.7277727576572104,
         0.7289686274214116,0.7301622766207523,0.7313537016191705,0.7325428987873788,0.7337298645028764,0.7349145951499599,
         0.7360970871197343,0.7372773368101241,0.7384553406258837,0.7396310949786097,0.74080459628675,0.7419758409756163,
         0.7431448254773942,0.744311546231154,0.7454759996828623,0.7466381822853914,0.7477980904985319,0.7489557207890021,
         0.7501110696304595,0.7512641335035111,0.7524149088957244,0.7535633923016378,0.754709580222772,0.7558534691676396,
         0.7569950556517564,0.7581343361976522,0.7592713073348808,0.7604059656000309,0.7615383075367367,0.7626683296956883,
         0.7637960286346421,0.7649214009184317,0.7660444431189779,0.7671651518152995,0.7682835235935234,0.7693995550468951,
         0.7705132427757893,0.77162458338772,0.7727335734973511,0.7738402097265061,0.7749444887041796,0.7760464070665459,
         0.7771459614569709,0.778243148526021,0.7793379649314741,0.7804304073383297,0.7815204724188187,0.7826081568524139,
         0.7836934573258397,0.7847763705330829,0.7858568931754019,0.7869350219613374,0.7880107536067219,0.7890840848346907,
         0.7901550123756903,0.79122353296749,0.7922896433551907,0.7933533402912352,0.7944146205354181,0.7954734808548958,
         0.7965299180241963,0.7975839288252284,0.7986355100472928,0.7996846584870905,0.8007313709487335,0.801775644243754,
         0.8028174751911145,0.8038568606172174,0.8048937973559142,0.8059282822485158,0.8069603121438019,0.8079898838980305,
         0.8090169943749475,0.810041640445796,0.8110638189893266,0.8120835268918062,0.8131007610470277,0.8141155183563192,
         0.8151277957285542,0.8161375900801602,0.8171448983351285,0.8181497174250234,0.8191520442889918,0.820151875873772,
         0.821149209133704,0.8221440410307373,0.8231363685344418,0.8241261886220157,0.8251134982782952,0.8260982944957639,
         0.8270805742745618,0.8280603346224944,0.8290375725550416,0.8300122850953675,0.8309844692743282,0.8319541221304826,
         0.8329212407100994,0.8338858220671681,0.8348478632634065,0.8358073613682702,0.8367643134589617,0.8377187166204387,
         0.838670567945424,0.8396198645344132,0.8405666034956842,0.8415107819453062,0.8424523970071476,0.8433914458128856,
         0.8443279255020151,0.8452618332218561,0.846193166127564,0.8471219213821372,0.8480480961564258,0.8489716876291414,
         0.8498926929868639,0.8508111094240512,0.8517269341430476,0.8526401643540922,0.8535507972753273,0.8544588301328074,
         0.8553642601605067,0.8562670846003282,0.8571673007021123,0.8580649057236446,0.8589598969306644,0.8598522715968734,
         0.8607420270039435,0.8616291604415257,0.8625136692072574,0.8633955506067716,0.8642748019537047,0.8651514205697045,
         0.8660254037844386,0.8668967489356028,0.8677654533689284,0.8686315144381913,0.869494929505219,0.8703556959398997,
         0.8712138111201894,0.8720692724321206,0.8729220772698096,0.8737722230354652,0.8746197071393957,0.8754645270000179,
         0.8763066800438636,0.8771461637055887,0.8779829754279805,0.8788171126619653,0.8796485728666165,0.8804773535091619,
         0.8813034520649922,0.8821268660176678,0.8829475928589269,0.8837656300886935,0.8845809752150839,0.8853936257544159,
         0.8862035792312147,0.8870108331782217,0.8878153851364013,0.8886172326549489,0.8894163732912975,0.8902128046111265,
         0.8910065241883678,0.891797529605214,0.8925858184521255,0.8933713883278375,0.8941542368393681,0.894934361602025,
         0.8957117602394129,0.8964864303834404,0.8972583696743284,0.8980275757606155,0.898794046299167,0.8995577789551804,
         0.9003187714021935,0.9010770213220917,0.9018325264051138,0.9025852843498605,0.9033352928633008,0.9040825496607783,
         0.9048270524660196,0.9055687990111395,0.9063077870366499,0.9070440142914649,0.9077774785329086,0.9085081775267219,
         0.9092361090470685,0.9099612708765432,0.910683660806177,0.9114032766354453,0.912120116172273,0.9128341772330428,
         0.9135454576426009,0.9142539552342637,0.9149596678498249,0.915662593339561,0.9163627295622396,0.917060074385124,
         0.917754625683981,0.9184463813430871,0.9191353392552345,0.9198214973217376,0.9205048534524403,0.9211854055657211,
         0.9218631515885005,0.9225380894562464,0.9232102171129808,0.9238795325112867,0.9245460336123131,0.9252097183857821,
         0.9258705848099947,0.9265286308718373,0.9271838545667874,0.92783625389892,0.9284858268809135,0.9291325715340562,
         0.9297764858882513,0.9304175679820246,0.9310558158625283,0.9316912275855489,0.9323238012155122,0.9329535348254889,
         0.9335804264972017,0.9342044743210295,0.9348256763960144,0.9354440308298673,0.9360595357389733,0.9366721892483976,
         0.9372819894918915,0.9378889346118976,0.9384930227595559,0.9390942520947091,0.9396926207859083,0.9402881270104189,
         0.9408807689542255,0.9414705448120378,0.9420574527872967,0.9426414910921784,0.943222657947601,0.9438009515832294,
         0.9443763702374811,0.9449489121575309,0.9455185755993167,0.9460853588275453,0.9466492601156964,0.9472102777460288,
         0.9477684100095857,0.9483236552061993,0.9488760116444965,0.9494254776419038,0.9499720515246525,0.9505157316277837,
         0.9510565162951535,0.9515944038794382,0.9521293927421386,0.9526614812535863,0.953190667792947,0.9537169507482268,
         0.9542403285162768,0.9547607995027975,0.9552783621223436,0.9557930147983301,0.9563047559630354,0.9568135840576074,
         0.9573194975320672,0.9578224948453149,0.9583225744651332,0.958819734868193,0.9593139745400575,0.9598052919751869,
         0.9602936856769431,0.9607791541575941,0.9612616959383188,0.9617413095492113,0.9622179935292854,0.9626917464264787,
         0.9631625667976581,0.9636304532086231,0.9640954042341101,0.9645574184577981,0.9650164944723114,0.9654726308792251,
         0.9659258262890683,0.9663760793213293,0.9668233886044594,0.9672677527758767,0.9677091704819711,0.9681476403781077,
         0.9685831611286311,0.9690157314068695,0.9694453498951389,0.9698720152847469,0.9702957262759965,0.9707164815781908,
         0.971134279909636,0.9715491199976461,0.9719610005785463,0.9723699203976766,0.9727758782093965,0.9731788727770883,
         0.9735789028731602,0.9739759672790516,0.9743700647852352,0.9747611941912218,0.9751493543055632,0.9755345439458565,
         0.9759167619387474,0.9762960071199334,0.9766722783341679,0.9770455744352635,0.9774158942860959,0.9777832367586061,
         0.9781476007338056,0.9785089851017784,0.978867388761685,0.9792228106217657,0.9795752495993441,0.9799247046208296,
         0.9802711746217219,0.980614658546613,0.9809551553491915,0.9812926639922451,0.981627183447664,0.9819587126964436,
         0.9822872507286887,0.9826127965436152,0.9829353491495543,0.9832549075639545,0.9835714708133859,0.9838850379335417,
         0.9841956079692419,0.9845031799744366,0.984807753012208,0.9851093261547739,0.9854078984834901,0.9857034690888535,
         0.9859960370705049,0.9862856015372313,0.9865721616069694,0.9868557164068072,0.9871362650729879,0.9874138067509114,
         0.9876883405951377,0.9879598657693891,0.9882283814465528,0.9884938868086836,0.9887563810470058,0.9890158633619168,
         0.9892723329629883,0.9895257890689694,0.989776230907789,0.9900236577165575,0.9902680687415704,0.9905094632383088,
         0.9907478404714436,0.9909831997148363,0.9912155402515417,0.9914448613738104,0.9916711623830904,0.9918944425900297,
         0.9921147013144778,0.9923319378854887,0.992546151641322,0.9927573419294455,0.9929655081065369,0.9931706495384861,
         0.9933727656003964,0.9935718556765875,0.9937679191605964,0.9939609554551797,0.9941509639723154,0.9943379441332046,
         0.9945218953682733,0.9947028171171742,0.9948807088287882,0.9950555699612263,0.9952273999818312,0.9953961983671789,
         0.99556196460308,0.9957246981845821,0.9958843986159703,0.9960410654107695,0.9961946980917455,0.9963452961909064,
         0.9964928592495044,0.9966373868180366,0.9967788784562471,0.996917333733128,0.9970527522269202,0.9971851335251157,
         0.9973144772244581,0.997440782930944,0.9975640502598242,0.9976842788356053,0.99780146829205,0.997915618272179,
         0.9980267284282716,0.9981347984218669,0.9982398279237653,0.9983418166140283,0.998440764181981,0.9985366703262117,
         0.9986295347545738,0.9987193571841863,0.998806137341434,0.99888987496197,0.9989705697907146,0.9990482215818578,
         0.9991228300988584,0.999194395114446,0.9992629164106211,0.9993283937786562,0.9993908270190958,0.9994502159417572,
         0.9995065603657316,0.999559860119384,0.9996101150403544,0.9996573249755573,0.9997014897811831,0.9997426093226983,
         0.9997806834748455,0.9998157121216442,0.9998476951563913,0.9998766324816606,0.9999025240093042,0.999925369660452,
         0.9999451693655121,0.9999619230641713,0.9999756307053947,0.9999862922474267,0.9999939076577904,0.9999984769132877,
         1.0};
 
     /**
      * Method to compute the sine of an integer angle (in tenth-degrees).
      * @param angle the angle in tenth-degrees.
      * @return the sine of that angle.
      */
     public static double sin(int angle)
     {
         while (angle < 0) angle += 3600;
         if (angle >= 3600) angle %= 3600;
         if (angle <= 900) return sineTable[angle];
         if (angle <= 1800) return sineTable[1800-angle];
         if (angle <= 2700) return -sineTable[angle-1800];
         return -sineTable[3600-angle];
     }
 
     /**
      * Method to compute the cosine of an integer angle (in tenth-degrees).
      * @param angle the angle in tenth-degrees.
      * @return the cosine of that angle.
      */
     public static double cos(int angle)
     {
         while (angle < 0) angle += 3600;
         if (angle >= 3600) angle %= 3600;
         if (angle <= 900) return sineTable[900-angle];
         if (angle <= 1800) return -sineTable[angle-900];
         if (angle <= 2700) return -sineTable[2700-angle];
         return sineTable[angle-2700];
     }
 
     /**
      * Method to return a long that represents the unsigned
      * value of an integer.  I.e., the passed int is a set of
      * bits, and this method returns a number as if those bits
      * were interpreted as an unsigned int.
      */
     public static long unsignedIntValue(int n)
     {
         if (n > 0) return (long)n;              // int is > 0
         long num = 0;
         num = n | num;
         return num;
     }
 
 
 }
