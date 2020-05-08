 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package math;
 
 public class Rectangle {
   private Vector2 min, max, center;
   private final Vector2 size, halfSize;
 
   /**
    * Construct a new rectangle from a position and a size.
    * @param x1 x coordinate for top left position
    * @param y1 y coordinate for top left position
    * @param width the width of the rectangle, greater than zero
    * @param height the height of the rectangle, greater than zero
    */
   public Rectangle(float x1, float y1, float width, float height) {
     assert width > 0;
     assert height > 0;
 
     min = new Vector2(x1, y1);
     max = new Vector2(x1 + width, y1 + height);
 
     size     = new Vector2(width, height);
     halfSize = Vector2.divide(size, 2.0f);
 
     center = Vector2.add(min, halfSize);
   }
 
   /**
    * Create a new rectangle from two vectors.
    * Note that upperLeft must be above and to the left of bottomRight.
    * @param upperLeft the upper left coordinate of the rectangle
    * @param bottomRight the bottom right coordinate of the rectangle
    */
   public Rectangle(Vector2 upperLeft, Vector2 bottomRight) {
     assert upperLeft.x < bottomRight.x && upperLeft.y < bottomRight.y;
 
     min = upperLeft;
     max = bottomRight;
 
     size = new Vector2(bottomRight.x - upperLeft.x,
                        bottomRight.y - upperLeft.x);
     halfSize = Vector2.divide(size, 2.0f);
 
     center = Vector2.add(min, halfSize);
   }
 
   /**
    * Sets the position of this rectangle.
    * @param x the new x coordinate
    * @param y the new y coordinate
    */
   public void setPosition(float x, float y) {
     setPosition(new Vector2(x, y));
   }
 
   /**
    * Sets the position of this rectangle.
    * @param v the new position
    */
   public void setPosition(Vector2 v) {
     min    = v;
     max    = Vector2.add(v, size);
     center = Vector2.add(v, halfSize);
   }
 
   /**
    * Translate the rectangle.
    * @param v the vector to use for translation
    */
   public void addPosition(Vector2 v) {
     addPosition(v.x, v.y);
   }
 
   /**
    * Translate the rectangle.
    * @param x the translation on the x-axis
    * @param y the translation on the y-axis
    */
   public void addPosition(float x, float y) {
     min    = Vector2.add(min, x, y);
     max    = Vector2.add(max, x, y);
     center = Vector2.add(min, halfSize);
   }
 
   /**
    * Top left corner of the rectangle.
    * @return the top left of the rectangle
    */
   public Vector2 getMin() {
     return min;
   }
 
   /**
    * Bottom right corner of the rectangle.
    * @return the bottom right of the rectangle
    */
   public Vector2 getMax() {
     return max;
   }
 
   /**
    * Return the position of the center of the rectangle.
    * @return the center position as a vector
    */
   public Vector2 getCenter() {
     return center;
   }
 
   /**
    * Return the size of the rectangle.
    * Note that the size can not change.
    * @return the size of the rectangle
    */
   public Vector2 getSize() {
     return size;
   }
 
   /**
    * Return the half size of the rectangle.
    * That is the offset to the center relative to the upper left corner of the
    * rectangle. Can also be referred to as the size divided by two.
    * Note that the size can not change, thus this is also constant.
    * @return the size divided by two
    */
   public Vector2 getHalfSize() {
     return halfSize;
   }
 
   /**
    * Return the width of the rectangle.
    * Note that the width is constant.
    * @return the width of the rectangle, greater than zero
    */
   public float getWidth() {
     return size.x;
   }
 
   /**
    * Return the height of the rectangle.
    * Note that the height is constant.
    * @return the height of the rectangle, greater than zero
    */
   public float getHeight() {
     return size.y;
   }
 
   /**
    * Return the upper left x coordinate.
    * @return the upper left x coordinate
    */
   public float getX1() {
     return min.x;
   }
 
   /**
    * Return the upper left y coordinate.
    * @return the upper left y coordinate
    */
   public float getY1() {
     return min.y;
   }
 
   /**
    * Return the bottom right x coordinate.
    * @return the bottom right x coordinate
    */
   public float getX2() {
     return max.x;
   }
 
   /**
    * Return the bottom right y coordinate.
    * @return the bottom right y coordinate
    */
   public float getY2() {
     return max.y;
   }
 
   /**
    * Return a string representation of the rectangle.
    * @return a string
    */
  @SuppressWarnings("boxing")
   @Override
   public String toString() {
     return String.format("(%f, %f, %f, %f) %dx%d",
         min.x, min.y, max.x, max.y, (int) size.x, (int) size.y);
   }
 
   /**
    * Check if two rectangles are intersecting.
    * @param a the first rectangle
    * @param b the second rectangle
    * @return true or false depending on if they intersecting or not
    */
   public static boolean intersects(Rectangle a, Rectangle b) {
     return !((a.min.x > b.max.x) || (a.min.y > b.max.y) ||
              (a.max.x < b.min.x) || (a.max.y < b.min.y));
   }
 
   /**
    * Check if a rectangle (a) contains another rectangle (b). That is, the
    * entire rectangle is in inside another.
    * @param a the outer rectangle
    * @param b the inner rectangle
    * @return true or false depending on if b is contained by a or not
    */
   public static boolean contains(Rectangle a, Rectangle b) {
     return ((b.min.x > a.min.x && b.max.x < a.max.x) &&
             (b.min.y > a.min.y && b.max.y < a.max.y));
   }
 }
