 package util;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import javax.swing.ImageIcon;
 
 
 /**
  * This class represents a shape that moves on its own.
  * 
  * Note, Sprite is a technical term:
  * http://en.wikipedia.org/wiki/Sprite_(computer_graphics)
  * 
  * @author Robert C. Duvall, Richard Yang
  */
 public abstract class Animal {
     // canonical directions for a collision
     /**
      * Right
      */
     public static final int RIGHT_DIRECTION = 0;
     /**
      * Down
      */
     public static final int DOWN_DIRECTION = 90;
     /**
      * Left
      */
     public static final int LEFT_DIRECTION = 180;
     /**
      * Up
      */
     public static final int UP_DIRECTION = 270;
 
     private static final String RESOURCE_LOCATION = "/images/";
     // state
     private Location myCenter;
     private Dimension mySize;
     private Pixmap myView;
     // keep copies of the original state so shape can be reset as needed
     private Location myOriginalCenter;
     private Dimension myOriginalSize;
     private Pixmap myOriginalView;
     // cached for efficiency
     private Rectangle myBounds;
     private java.awt.Image myImage;
 
     /**
      * Create a shape at the given position, with the given size, velocity, and color.
      * 
      * @param image picture
      * @param center center
      * @param size size
      */
     public Animal (Pixmap image, Location center, Dimension size) {
         // make copies just to be sure no one else has access
         myOriginalCenter = new Location(center);
         myOriginalSize = new Dimension(size);
         myOriginalView = new Pixmap(image);
         myImage =
                 new ImageIcon(getClass().getResource(RESOURCE_LOCATION + image.getImageFileName()))
                         .getImage();
         reset();
 
         resetBounds();
     }
 
     /**
      * Resets shape's center.
      * 
      * @param x x coordinate
      * @param y y coordinate
      */
     public void setCenter (double x, double y) {
         myCenter.setLocation(x, y);
         resetBounds();
     }
 
     /**
      * Resets shape's center.
      * 
      * @param center center of shape
      */
     public void setCenter (Location center) {
         setCenter(center.getX(), center.getY());
     }
 
     /**
      * 
      * @return center of image
      */
     public Location getCenter () {
         return myCenter;
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
      * 
      * @return the shapes bottom coordinate
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
      * 
      * @param widthFactor width factor
      * @param heightFactor height factor
      */
     public void scale (double widthFactor, double heightFactor) {
         mySize.setSize(mySize.width * widthFactor, mySize.height * heightFactor);
         resetBounds();
     }
 
     /**
      * Resets shape's size.
      * 
      * @param width width to be resized to
      * @param height height to be resized to
      */
     public void setSize (int width, int height) {
         mySize.setSize(width, height);
         resetBounds();
     }
 
     /**
      * Resets shape's size.
      * 
      * @param size size to resize to
      */
     public void setSize (Dimension size) {
         setSize(size.width, size.height);
     }
 
     /**
      * Resets shape's image.
      * 
      * @param image image to reset to
      */
     public void setView (Pixmap image) {
         if (image != null) {
            myOriginalView = new Pixmap(image);
            myImage =
                    new ImageIcon(getClass().getResource(RESOURCE_LOCATION + image.getImageFileName()))
                            .getImage();
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
      * 
      * @param other other sprite
      */
     public boolean intersects (Animal other) {
         return getBounds().intersects(other.getBounds());
     }
 
     /**
      * Returns true if the given point is within a rectangle representing this shape.
      * 
      * @param pt point
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
         myView = new Pixmap(myOriginalView);
     }
 
     /**
      * Display this shape on the screen.
      * 
      * @param pen graphics pen
      */
     public void paint (Graphics2D pen) {
         myView.paint(pen, myCenter, mySize);
     }
 
     /**
      * Describes how to draw the image rotated on the screen.
      * 
      * @param pen graphics pen
      * @param center center of image
      * @param size size of image
      * @param angle angle
      */
     public void paint (Graphics2D pen, Point2D center, Dimension size, double angle) {
         System.out.println("rotated turtle");
         // save current state of the graphics area
         AffineTransform old = new AffineTransform(pen.getTransform());
         // move graphics area to center of this shape
         pen.translate(center.getX(), center.getY());
         // rotate area about this shape
         pen.rotate(angle);
         // draw as usual (i.e., rotated)
         pen.drawImage(myImage, -size.width / 2, -size.height / 2, size.width, size.height, null);
         // restore graphics area to its old state, so our changes have no lasting effects
         pen.setTransform(old);
     }
 
     /**
      * Returns rectangle that encloses this shape.
      */
     protected void resetBounds () {
         myBounds = new Rectangle((int) getLeft(), (int) getTop(), mySize.width, mySize.height);
     }
 
 }
