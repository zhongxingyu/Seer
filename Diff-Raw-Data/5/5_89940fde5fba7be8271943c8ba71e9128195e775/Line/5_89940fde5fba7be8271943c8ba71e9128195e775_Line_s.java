 package computational_geometry.model.beans;
 
 import java.awt.Rectangle;
 
 /**
  * Class representing a straight line given it's equation :
  * Ax + By + C = 0
  * @author eloi
  *
  */
 public class Line {
 
     protected double A;
     protected double B;
     protected double C;
 
     public Line(double A, double B, double C) {
         this.A = A;
         this.B = B;
         this.C = C;
     }
 
     /**
      * Find the y coordinate of the point having the x given coordinate
      * and passing through this line
      * if B equals 0, the result will be an infinite double, arithmetics still works
      * @param x
      * @return
      */
     public double findY(double x) {
         return (-A * x - C) / B;
     }
 
     /**
      * Find the x coordinate of the point having the y given coordinate
      * and passing through this line
      * if A equals 0, the result will be an infinite double, arithmetics still works
      * @param x
      * @return
      */
     public double findX(double y) {
         return (-B * y - C) / A;
     }
 
     /**
      * Find the slope of this line
      * if B equals 0, the result will be an infinite double, arithmetics still works
      * @return
      */
     public double slope() {
         return -A / B;
     }
 
     /**
      * Find the upper point of this line bounded by a given rectangle
      * @param r
      * @return
      */
     public Point findUpperPoint(Rectangle r) {
         double x, y, a;
         a = slope();
        if (a < 0) {                // up or right
             if (Math.abs(a) > 1) {  // up
                 y = r.getY();
                 x = findX(y);
             } else {                // right
                 x = r.getWidth() + r.getX();
                 y = findY(x);
             }
         } else {                    // up or left
             if (Math.abs(a) > 1) {  // up
                 y = r.getY();
                 x = findX(y);
             } else {                // left
                 x = r.getX();
                 y = findY(x);
             }
         }
         return new Point((int) x, (int) y);
     }
 
     /**
      * Find the lower point of this line bounded by a given rectangle 
      * @param r
      * @return
      */
     public Point findLowerPoint(Rectangle r) {
         double x, y, a;
         a = slope();
        if (a < 0) {                // down or left
             if (Math.abs(a) > 1) {  // down
                 y = r.getHeight() + r.getY();
                 x = findX(y);
             } else {                // left
                 x = r.getX();
                 y = findY(x);
             }
         } else {                    // down or right
             if (Math.abs(a) > 1) {  // down
                 y = r.getHeight() + r.getY();
                 x = findX(y);
             } else {                // right
                 x = r.getWidth() + r.getX();
                 y = findY(x);
             }
         }
         return new Point((int) x, (int) y);
     }
 
     /**
      * Find the side of the line on which the point lies
      * @param p
      * @return < 0 if it lies on one side, > 0 if on the other side,
      * 		   = 0 if it lies on this Line
      */
     public int findSide(Point p) {
         return (int) Math.signum(A * p.x + B * p.y + C);
     }
 
     /**
      * Find the intersection point of this lines and a given on
      * @param l1
      * @param l2
      * @return the intersection of two lines if exists, null otherwise
      */
     public Point findIntersection(Line l) {
         double x, y, det = A * l.B - l.A * B;
         if (det == 0) {
             return null;
         }
         x = (B * l.C - l.B * C) / det;
         y = (l.A * C - A * l.C) / det;
         return new Point((int) x, (int) y);
     }
 
 }
