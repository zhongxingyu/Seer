 package cs437.som;
 
 /**
  * Grouping of x- and y-coordinates or height and width (with area).
  */
 public class Dimension {
     /**
      * The dimension value in the x direction, generally the width.
      */
     public final int x;
 
     /**
      * The dimension value in the y direction, generally the height.
      */
     public final int y;
 
     /**
      * The area, taking x and y to be width and height.
      */
     public final int area;
 
     /**
      * Create a new dimension.
     * 
      * @param x The x value to assume.
      * @param y The y value to assume.
      * @throws SOMError if x or y is less than 0.
      */
     public Dimension(int x, int y) {
         if (x <= 0 || y <= 0) {
             throw new SOMError("Cannot have a negative or zero dimension.");
         }
         this.x = x;
         this.y = y;
         area = x * y;
     }
 
     @Override
     public String toString() {
         return "Dimension{x=" + x + ", y=" + y + '}';
     }
 }
