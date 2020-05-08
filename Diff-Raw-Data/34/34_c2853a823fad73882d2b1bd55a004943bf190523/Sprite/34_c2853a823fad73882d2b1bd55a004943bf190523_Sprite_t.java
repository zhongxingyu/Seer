 package util;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.Point2D;
 
 
 /**
  * This class represents a shape that moves on its own.
  * 
  * Note, Sprite is a technical term:
  * http://en.wikipedia.org/wiki/Sprite_(computer_graphics)
  * 
  * @author Robert C. Duvall
  * @author David Winegar
  * @author Zhen Gou
  */
 public abstract class Sprite implements Paintable {
     // canonical directions for a collision
     public static final int RIGHT_DIRECTION = 0;
     public static final int DOWN_DIRECTION = 90;
     public static final int LEFT_DIRECTION = 180;
     public static final int UP_DIRECTION = 270;
     private static final double DEFAULT_HEADING = UP_DIRECTION;
 
     // state
     private Location myCenter;
     private Vector myVelocity;
 
     private double myHeading;
 
     private Dimension mySize;
     private Pixmap myView;
     // keep copies of the original state so shape can be reset as needed
     private Location myOriginalCenter;
     // private Vector myOriginalVelocity;
     private Dimension myOriginalSize;
     private Pixmap myOriginalView;
     // cached for efficiency
     private Rectangle myBounds;
 
     /**
      * Create a shape at the given position, with the given size.
      */
     public Sprite (Pixmap image, Location center, Dimension size) {
         this(image, center, size, DEFAULT_HEADING);
     }
 
     /**
      * Create a shape at the given position, with the given size, velocity, and color.
      */
     public Sprite (Pixmap image, Location center, Dimension size, double heading) {
         // make copies just to be sure no one else has access
         myOriginalCenter = new Location(center);
         myOriginalSize = new Dimension(size);
         myOriginalView = new Pixmap(image);
         myHeading = heading;
         reset();
         resetBounds();
     }
 
     /**
      * Describes how to "animate" the shape by changing its state.
      * 
      * Currently, moves by the current velocity.
      */
     public void update (double elapsedTime, Dimension bounds) {
         // do not change original velocity
         Vector v = new Vector(myVelocity);
         v.scale(elapsedTime);
         translate(v);
     }
 
     /**
      * Moves shape's center by given vector.
      */
     public void translate (Vector v) {
         myCenter.translate(v);
         resetBounds();
     }
 
     /**
      * Resets shape's center.
      */
     public void setCenter (double x, double y) {
         myCenter.setLocation(x, y);
       // resetBounds();
     }
 
     /**
      * Resets shape's center.
      */
     public void setCenter (Location center) {
         setCenter(center.getX(), center.getY());
     }
 
     /**
      * Returns shape's x coordinate in pixels.
      */
     public double getX () {
         return myCenter.getX();
     }
 
     /**
      * Returns shape's y-coordinate in pixels.
      */
     public double getY () {
         return myCenter.getY();
     }
 
     /**
      * Returns shape's left-most coordinate in pixels.
      */
     public double getLeft () {
         return myCenter.getX() - mySize.width / 2;
     }
 
     /**
      * Returns shape's top-most coordinate in pixels.
      */
     public double getTop () {
         return myCenter.getY() - mySize.height / 2;
     }
 
     /**
      * Returns shape's right-most coordinate in pixels.
      */
     public double getRight () {
         return myCenter.getX() + mySize.width / 2;
     }
 
     /**
      * Returns shape's bottom-most coordinate in pixels.
      */
     public double getBottom () {
         return myCenter.getY() + mySize.height / 2;
     }
 
     /**
      * Returns shape's width in pixels.
      */
     public double getWidth () {
         return mySize.getWidth();
     }
 
     /**
      * Returns shape's height in pixels.
      */
     public double getHeight () {
         return mySize.getHeight();
     }
 
     /**
      * Scales shape's size by the given factors.
      */
     public void scale (double widthFactor, double heightFactor) {
         mySize.setSize(mySize.width * widthFactor, mySize.height * heightFactor);
         resetBounds();
     }
 
     /**
      * Resets shape's size.
      */
     public void setSize (int width, int height) {
         mySize.setSize(width, height);
         resetBounds();
     }
 
     /**
      * Resets shape's size.
      */
     public void setSize (Dimension size) {
         setSize(size.width, size.height);
     }
 
     /**
      * Returns shape's velocity.
      */
     public Vector getVelocity () {
         return myVelocity;
     }
 
     /**
      * Resets shape's velocity.
      */
     public void setVelocity (double angle, double magnitude) {
         myVelocity = new Vector(angle, magnitude);
     }
 
     /**
      * Resets shape's velocity.
      */
     public void setVelocity (Vector velocity) {
         setVelocity(velocity.getDirection(), velocity.getMagnitude());
     }
 
     /**
      * Resets shape's image.
      */
     public void setView (Pixmap image) {
         if (image != null) {
             myView = image;
         }
     }
 
     /**
      * Returns rectangle that encloses this shape.
      */
     public Rectangle getBounds () {
         return myBounds;
     }
 
     /**
      * Returns true if the given point is within a rectangle representing this shape.
      */
     public boolean intersects (Sprite other) {
         return getBounds().intersects(other.getBounds());
     }
 
     /**
      * Returns true if the given point is within a rectangle representing this shape.
      */
     public boolean intersects (Point2D pt) {
         return getBounds().contains(pt);
     }
 
     /**
      * Reset shape back to its original values.
      */
     public void reset () {
         myCenter = new Location(myOriginalCenter);
         mySize = new Dimension(myOriginalSize);
         myHeading = DEFAULT_HEADING;
         myView = new Pixmap(myOriginalView);
     }
 
     /**
      * Display this shape on the screen.
      */
     @Override
     public void paint (Graphics2D pen)
     {
         myView.paint(pen, myCenter, mySize, myHeading);
     }
 
     /**
      * Returns rectangle that encloses this shape.
      */
     protected void resetBounds () {
         myBounds = new Rectangle((int) getLeft(), (int) getTop(), mySize.width, mySize.height);
     }
 
     /**
      * Returns approximate direction from center of rectangle to side which was hit or
      * NaN if no hit took place.
      */
 
     protected double getHitDirection (Rectangle bounds) {
         // double angle = Vector.angleBetween(myCenter, new Location(bounds.getCenterX(),
         // bounds.getCenterY()));
 
         if (bounds.contains(new Location(getLeft(), getY())))
             return RIGHT_DIRECTION;
         else if (bounds.contains(new Location(getX(), getBottom())))
             return UP_DIRECTION;
         else if (bounds.contains(new Location(getRight(), getY())))
             return LEFT_DIRECTION;
         else if (bounds.contains(new Location(getX(), getTop()))) return DOWN_DIRECTION;
         return 0;
         // return Double.NaN;
     }
 
     /**
      * Gets the current location. Returns a copy so Location can't be changed using this method.
      * 
      * @return copy of current location.
      */
     protected Location getLocation () {
         return new Location(myCenter.getX(), myCenter.getY());
     }
 
     /**
      * Returns the current heading.
      * 
      * @return current heading
      */
     public double getHeading () {
         return myHeading;
     }
 
     /**
      * Sets the current heading to the passed in value.
      * @param heading to set
      */
     public void setMyHeading (double heading) {
         myHeading = heading;
         myVelocity = new Vector(heading, 0);
     }
 
     /**
      * resets the heading to the default value.
      */
     public void resetHeading () {
         myHeading = DEFAULT_HEADING;
         myVelocity = new Vector(DEFAULT_HEADING, 0);
     }
 
 }
