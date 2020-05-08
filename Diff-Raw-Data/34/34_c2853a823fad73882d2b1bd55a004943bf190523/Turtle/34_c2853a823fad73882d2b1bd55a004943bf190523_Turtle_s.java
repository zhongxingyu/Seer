 package model;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Iterator;
 import util.Location;
 import util.Paintable;
 import util.Pixmap;
 import util.Sprite;
 import util.Vector;
 
 
 /**
  * Represents the turtle on the canvas. Can be painted. Can be called with its custom methods by
  * commands. Also implements dataSource and can be accessed to give information about itself.
  * 
  * @author David Winegar
  * @author Zhen Gou
  */
 public class Turtle extends Sprite implements Paintable {
 
     private static final Pixmap DEFAULT_IMAGE = new Pixmap("turtle.gif");
     private static final Dimension DEFAULT_DIMENSION = new Dimension(30, 30);
     private static final int HALF_TURN_DEGREES = 180;
     private static final int FULL_TURN_DEGREES = 360;
     private static final int THREE_QUARTER_TURN_DEGREES = 270;
     private static final int ONE_QUARTER_TURN_DEGREES = 90;
     private static final int NO_TURN_DEGREES = 0;
     
 
     private boolean myPenDown = true;
     private boolean myTurtleShowing = true;
     private Line myLine = new Line();
     private Dimension myCanvasBounds;
     private int myCenterXValue;
     private int myCenterYValue;
 
     /**
      * Creates a turtle sprite.
      * 
      * @param image image to use
      * @param center center of turtle
      * @param size size of turtle
      * @param canvasBounds bounds of canvas that turtle uses
      */
     public Turtle (Pixmap image, Location center, Dimension size, Dimension canvasBounds) {
         super(image, center, size);
         myCanvasBounds = canvasBounds;
         myCenterXValue = (int) myCanvasBounds.getWidth() / 2;
         myCenterYValue = (int) myCanvasBounds.getHeight() / 2;
     }
 
     /**
      * Uses default values for constructor, except for canvasBounds.
      * 
      * @param canvasBounds bounds to use.
      */
     public Turtle (Dimension canvasBounds) {
         this(DEFAULT_IMAGE,
              new Location(canvasBounds.getWidth() / 2, canvasBounds.getHeight() / 2),
              DEFAULT_DIMENSION, canvasBounds);
     }
 
     /**
      * Move forward or backward by number of pixels.
      * 
      * @param pixels to move by
      * @return command return value
      */
     public int move (int pixels) {
         int pixelsToMove = pixels;
         // ensure that moveRecursiveHelper doesn't take a negative argument
         if (Math.abs(pixels) != pixels){
             pixelsToMove = -pixelsToMove;
             turn(HALF_TURN_DEGREES);
         }
         moveRecursiveHelper(pixelsToMove);
         if (Math.abs(pixels) != pixels){
             turn(HALF_TURN_DEGREES);
         }
         return Math.abs(pixels);
     }
 
    private void moveRecursiveHelper (int pixels) {
        if (pixels <= 0) return;
         Location currentLocation = getLocation();
         Location nextLocation = getLocation();
         Location nextCenter = nextLocation;
         nextLocation.translate(new Vector(getHeading(), pixels));
         
         // top
         if (nextLocation.getY() < 0) {
             double angle = FULL_TURN_DEGREES - getHeading();
             if(getHeading() < THREE_QUARTER_TURN_DEGREES){
                 angle = getHeading() - HALF_TURN_DEGREES;
             } 
             nextLocation = new Location(getX() + getY() / Math.tan(angle), 0);
             nextCenter = new Location(getX() + getY() / Math.tan(angle),
                                    myCanvasBounds.getHeight());
             
             if(getHeading() == THREE_QUARTER_TURN_DEGREES){
                 nextLocation = new Location(getX(), 0);
                 nextCenter = new Location(getX(), myCanvasBounds.getHeight());
             }
             // bottom
         }
         else if (nextLocation.getY() > myCanvasBounds.getHeight()) {
             double angle = getHeading();
             if(getHeading() > ONE_QUARTER_TURN_DEGREES){
                 angle = HALF_TURN_DEGREES - getHeading();
             } 
             nextLocation = new Location(getX() + getY() / Math.tan(angle), myCanvasBounds.getHeight());
             nextCenter = new Location(getX() + getY() / Math.tan(angle),
                                    0);
             if(getHeading() == ONE_QUARTER_TURN_DEGREES){
                 nextLocation = new Location(getX(), myCanvasBounds.getHeight());
                 nextCenter = new Location(getX(), 0);
             }
             // right
         }
         else if (nextLocation.getX() > myCanvasBounds.getWidth()) {
             nextLocation =
                     new Location(myCanvasBounds.getWidth(), getY() + getX() /
                                                             Math.tan(getHeading()));
             nextCenter = new Location(0, getY() + getX() / Math.tan(getHeading()));
             
             double angle = getHeading();
             if(getHeading() > HALF_TURN_DEGREES){
                 angle = HALF_TURN_DEGREES - getHeading();
             } 
             nextLocation =
                     new Location(myCanvasBounds.getWidth(), getY() + getX() /
                                                             Math.tan(angle));
             nextCenter = new Location(0, getY() + getX() / Math.tan(angle));
             if(getHeading() == ONE_QUARTER_TURN_DEGREES){
                 nextLocation = new Location(myCanvasBounds.getWidth(), getY());
                 nextCenter = new Location(0, getY());
             }
             // left
         }
         else if (nextLocation.getX() < 0) {
            nextLocation = new Location(0, getY() + getX() / Math.tan(getHeading()));
             nextCenter = new Location(myCanvasBounds.getWidth(), getY() + getX() /
                                                              Math.tan(getHeading()));
             
             if(getHeading() == THREE_QUARTER_TURN_DEGREES){
                 nextLocation = new Location(0, getY());
                 nextCenter = new Location(myCanvasBounds.getWidth(), getY());
             }
         }
         
         setCenter(nextCenter);
        int newPixels = pixels - (int) (Vector.distanceBetween(currentLocation, nextLocation));
         if (myPenDown) {
             myLine.addLineSegment(currentLocation, nextLocation);
         }
         moveRecursiveHelper(newPixels);
     }
 
     /**
      * Turns turtle by given degrees.
      * 
      * @param degrees to turn by
      * @return command return value
      */
     public double turn (double degrees) {
         setHeading(getHeading() + degrees);
         return Math.abs(degrees);
     }
 
     /**
      * sets current heading.
      * 
      * @param heading to set
      * @return current heading
      */
     public double setHeading (double heading) {
         double oldHeading = getHeading();
         setMyHeading(heading);
         return Math.abs(heading - oldHeading);
     }
 
     /**
      * Sets heading to go towards location
      * 
      * @param location location to set heading towards
      * @return distance of turn
      */
     public double towards (Location location) {
         Location convertedLocation = convertFromViewCoordinates(location);
         double turnDistance = Vector.angleBetween(new Location(getX(), getY()), convertedLocation);
         turn(turnDistance);
         return turnDistance;
     }
 
     /**
      * Moves turtle to location
      * 
      * @param location to move to
      * @return distance of move
      */
     public int setLocation (Location location) {
         Location locationToMove = convertFromViewCoordinates(location);
         double heading = getHeading();
         towards(locationToMove);
         int distance = (int) Vector.distanceBetween(location, getLocation());
         setHeading(heading);
         return distance;
     }
 
     /**
      * Sets turtle to showing.
      * 
      * @return command value
      */
     public int showTurtle () {
         myTurtleShowing = true;
         return 1;
     }
 
     /**
      * Sets turtle to hiding.
      * 
      * @return command value
      */
     public int hideTurtle () {
         myTurtleShowing = false;
         return 0;
     }
 
     /**
      * Sets lines to show up on new moves.
      * 
      * @return command value
      */
     public int showPen () {
         myPenDown = true;
         return 1;
     }
 
     /**
      * Sets lines to not show up on new moves.
      * 
      * @return command value
      */
     public int hidePen () {
         myPenDown = false;
         return 0;
     }
 
     /**
      * Moves turtle to center and original heading.
      * 
      * @return
      */
     public int home () {
         Location center = new Location(myCenterXValue, myCenterYValue);
         int distance = (int) Vector.distanceBetween(getLocation(), center);
         setLocation(center);
         resetHeading();
         return distance;
     }
 
     /**
      * Clears all lines and moves turtle home.
      * 
      * @return
      */
     public int clearScreen () {
         int distance = home();
         myLine.clear();
         return distance;
     }
 
     /**
      * Gets if turtle is showing or not.
      * 
      * @return 1 if turtle is showing, 0 if not
      */
     public int isTurtleShowing () {
         if (myTurtleShowing) return 1;
         return 0;
     }
 
     /**
      * Gets if pen is down or not.
      * 
      * @return 1 if pen is down, 0 if not
      */
     public int isPenDown () {
         if (myPenDown) return 1;
         return 0;
     }
 
     private Location convertFromViewCoordinates (Location location) {
         return new Location(location.getX() - myCenterXValue, myCenterYValue - location.getY());
     }
 
     /**
      * Gets all paintable objects currently showing and returns them in an iterator.
      * @return iterator of paintables
      */
     public Iterator<Paintable> getPaintableIterator () {
         ArrayList<Paintable> paintList = new ArrayList<Paintable>();
         if (myTurtleShowing) {
             paintList.add(this);
         }
         paintList.add(myLine);
         return paintList.iterator();
     }
 
     /**
      * 
      * @return current turtle position.
      */
     public Location getTurtlePosition () {
         return convertFromViewCoordinates(getLocation());
     }
 
 }
