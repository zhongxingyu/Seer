 /**
  * Represents a generic polygon shape.
  *
  * @author Ory Band
  * @version 1.0
  */
 public abstract class Polygon implements Shape {
     protected Point[] points;
 
     /**
      * @param p Coordinate list.
      *
      * @return a new initialized Polygon object.
      */
     public Polygon(Point[] ps) {
         // Validity tests.
         if (ps == null) {
             throw new RuntimeException("Point argument is null.");
         } else if (ps.length < 3) {
             throw new RuntimeException("Not enough points for Polygon (Minimum 3) - Point[] is too short.");
         }
 
         this.points = new Point[ps.length];
 
         for (int i=0; i<ps.length; i++) {
             if (ps[i] == null) {
                 throw new RuntimeException("Point[" + i + "] is null.");
             } else {
                 this.points[i] = new Point(ps[i]);
             }
         }
     }
 
     /** @return amount of Polygon's coordinates. */
     public int getNumOfPoints() {
         return this.points.length;
     }
 
     /** @return sides' list. */
     public double[] getSides() {
         int l = this.points.length;
         double[] sides = new double[l];
        int n = this.getNumOfPoints();
 
         // Iterate over points and calculate sides.
         for (int i=0; i<l; i++) {
             // This also handles case when dist(last, first).
            sides[i] = this.points[i].distance(this.points[i%n]);
         }
 
         return sides;
     }
 
     /** @return Coordinate list. */
     public Point[] getPoints() {
         return this.points;
     }
 
     public double getPerimeter() {
         double[] sides = this.getSides();
 
         // Sum sides.
         double perimeter = 0.0;
         for (int i=0; i<sides.length; i++) {
             perimeter += sides[i];
         }
 
         return perimeter;
     }
 
     public void move(Point p) {
         // Validity test.
         if (p == null) {
             throw new RuntimeException("Point argument is null.");
         }
 
         // Shift coordinates according to Point object, given as argument.
         for (int i=0; i<this.points.length; i++) {
             this.points[i].move(p);
         }
     }
 
     public abstract double getArea();
     public abstract boolean contains(Point p);
 }
