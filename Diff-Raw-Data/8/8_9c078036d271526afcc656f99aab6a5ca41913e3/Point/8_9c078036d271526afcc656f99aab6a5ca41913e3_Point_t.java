 /*************************************************************************
  * Name:
  * Email:
  *
  * Compilation:  javac Point.java
  * Execution:
  * Dependencies: StdDraw.java
  *
  * Description: An immutable data type for points in the plane.
  *
  *************************************************************************/
 
 import java.util.Comparator;
 
 public class Point implements Comparable<Point> {
 
     // compare points by slope
     public final Comparator<Point> SLOPE_ORDER = new SlopeOrder(); 
 
     private final int x;                              // x coordinate
     private final int y;                              // y coordinate
 
     // create the point (x, y)
     public Point(int x, int y) {
         /* DO NOT MODIFY */
         this.x = x;
         this.y = y;
     }
 
     // plot this point to standard drawing
     public void draw() {
         /* DO NOT MODIFY */
         StdDraw.point(x, y);
     }
 
     // draw line between this point and that point to standard drawing
     public void drawTo(Point that) {
         /* DO NOT MODIFY */
         StdDraw.line(this.x, this.y, that.x, that.y);
     }
 
     // slope between this point and that point
     public double slopeTo(Point that) {
         /* YOUR CODE HERE */
         if (that ==  null)
             throw new java.lang.NullPointerException();
         if (this.x == that.x) {
             if (this.y == that.y)
                 return Double.NEGATIVE_INFINITY;
             else
                 return Double.POSITIVE_INFINITY;
         }
           
        if (this.y == that.y)
            return (1.0-1.0)/1;
        
         
         return ((that.y - this.y)/((double) that.x - this.x));
                     
     }
 
     // is this point lexicographically smaller than that one?
     // comparing y-coordinates and breaking ties by x-coordinates
     public int compareTo(Point that) {
         /* YOUR CODE HERE */
         if (that ==  null)
             throw new java.lang.NullPointerException();
         if (this.y < that.y)
             return -1;
         else if (this.y == that.y) {
             if (this.x < that.x)
                 return -1;
             else if (this.x == that.x)
                 return 0;
             else
                 return 1;
         }
         else
             return 1;
         
     }
     
     
     private class SlopeOrder implements Comparator<Point>
     {
         public int compare(Point p1, Point p2) {
           
             if (p1 ==  null || p2 == null)
                 throw new java.lang.NullPointerException();
             double slope1 = slopeTo(p1);
             double slope2 = slopeTo(p2);
             
             if (slope1 < slope2)
                 return -1;
             else if (slope1 == slope2)
                 return 0;
             else
                 return 1;
             
         }
     }
 
     // return string representation of this point
     public String toString() {
         /* DO NOT MODIFY */
         return "(" + x + ", " + y + ")";
     }
     
     // get the Point array from text file
     /*
     public static Point[] getFromText(String filename) {
         Point[] pointArray;
         In in = new In(filename);
         int N = in.readInt();
         pointArray = new Point[N];
         for (int i = 0; i < N; i++) {
             int x = in.readInt();
             int y = in.readInt();
             Point p = new Point(x, y);
             pointArray[i] = p;
         }
 
         return pointArray;
         
     }
    */
 
     // unit test
     public static void main(String[] args) {
         /* YOUR CODE HERE */
     }
 }
