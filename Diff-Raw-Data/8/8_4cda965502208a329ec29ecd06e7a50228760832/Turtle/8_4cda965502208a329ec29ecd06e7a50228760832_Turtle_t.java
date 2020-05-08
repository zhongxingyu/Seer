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
     private final int myCenterXValue;
     private final int myCenterYValue;
 
     private boolean myPenDown = true;
     private boolean myTurtleShowing = true;
     private Line myLine = new Line();
     private Dimension myCanvasBounds;
 
     public Turtle (Pixmap image, Location center, Dimension size, Dimension canvasBounds) {
         super(image, center, size);
         myCanvasBounds = canvasBounds;
         myCenterXValue = (int) myCanvasBounds.getWidth() / 2;
         myCenterYValue = (int) myCanvasBounds.getHeight() / 2;
     }
     
     public Turtle(Dimension canvasBounds){
         this(DEFAULT_IMAGE, new Location(canvasBounds.getWidth() / 2, canvasBounds.getHeight() / 2), DEFAULT_DIMENSION, canvasBounds);
     }
     
 
     public int move (int pixels) {
         moveRecursiveHelper(pixels);
         return pixels;
     }
 
     private void moveRecursiveHelper (int pixels) {
         if (pixels == 0) return;
         Location currentLocation = getLocation();
         Location nextLocation = getLocation();
         nextLocation.translate(new Vector(getHeading(), pixels));
         // top
         if (nextLocation.getY() < 0) {
             nextLocation = new Location(getX() + getY() / Math.tan(getHeading()), 0);
             setCenter(new Location(getX() + getY() / Math.tan(getHeading()),
                                    myCanvasBounds.getHeight()));
             // bottom
         }
         else if (nextLocation.getY() > myCanvasBounds.getHeight()) {
             nextLocation =
                     new Location(getX() + getY() / Math.tan(getHeading()), myCanvasBounds.getHeight());
             setCenter(new Location(getX() + getY() / Math.tan(getHeading()), 0));
             // right
         }
         else if (nextLocation.getX() > myCanvasBounds.getWidth()) {
             nextLocation =
                     new Location(myCanvasBounds.getWidth(), getY() + getX() / Math.tan(getHeading()));
             setCenter(new Location(0, getY() + getX() / Math.tan(getHeading())));
             // left
         }
         else if (nextLocation.getX() < 0) {
             nextLocation = new Location(0, getY() + getX() / Math.tan(getHeading()));
             setCenter(new Location(myCanvasBounds.getWidth(), getY() + getX() / Math.tan(getHeading())));
         }
         
         setCenter(nextLocation);  //fixed why turtle not moving bug
         pixels -= (int) Vector.distanceBetween(currentLocation, nextLocation) * Math.signum(pixels);
         myLine.addLineSegment(currentLocation, nextLocation);
         moveRecursiveHelper(pixels);
     }
 
     public double turn (double degrees) {
        setHeading(getHeading()+degrees);
         return degrees;
     }
     
     public double setHeading (double heading) {
     	setMyHeading(heading);
         return heading;
     }
 
     public double towards (Location location) {
         location = convertFromViewCoordinates(location);
         double turnDistance = Vector.angleBetween(new Location(getX(), getY()), location);
         turn(turnDistance);
         return turnDistance;
     }
 
     public int setLocation (Location location) {
         location = convertFromViewCoordinates(location);
         double heading = getHeading();
         towards(location);
         int distance = (int) Vector.distanceBetween(location, getLocation());
         setHeading(heading);
         return distance;
     }
 
     public int showTurtle () {
         myTurtleShowing = true;
         return 1;
     }
 
     public int hideTurtle () {
         myTurtleShowing = false;
         return 0;
     }
 
     public int showPen () {
         myPenDown = true;
         return 1;
     }
 
     public int hidePen () {
         myPenDown = false;
         return 0;
     }
 
     public int home () {
         Location center = new Location(myCenterXValue, myCenterYValue);
         int distance = (int) Vector.distanceBetween(getLocation(), center);
         setLocation(center);
         resetHeading();
         return distance;
     }
 
     public int clearScreen () {
         int distance = home();
         myLine.clear();
         return distance;
     }
 
     public int isTurtleShowing () {
         if (myTurtleShowing) return 1;
         return 0;
     }
 
     public int isPenDown () {
         if (myPenDown) return 1;
         return 0;
     }
 
 
     private Location convertFromViewCoordinates (Location location) {
         return new Location(location.getX() - myCenterXValue, location.getY() - myCenterYValue);
     }
 
     public Iterator<Paintable> getPaintableIterator () {
         ArrayList<Paintable> paintList = new ArrayList<Paintable>();
         paintList.add(this);
         paintList.add(myLine);
         return paintList.iterator();
     }
 
     public Location getTurtlePosition () {
         return convertFromViewCoordinates(getLocation());
     }
 
     public int getTurtleHeading () {
         return (int) getHeading();
     }
 }
