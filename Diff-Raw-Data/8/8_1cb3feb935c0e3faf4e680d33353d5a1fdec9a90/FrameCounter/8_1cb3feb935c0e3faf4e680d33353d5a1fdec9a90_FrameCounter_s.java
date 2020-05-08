 package vooga.rts.util;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.LinkedList;
 import java.util.Queue;
 import vooga.rts.IGameLoop;
 
 
 /**
  * This class is used to monitor the frame rate of the application.
  * It keeps track of how long it takes between each update cycle.
  * 
  * To use the class, make sure you update it with the elapsed time per
  * frame and then call paint in order to paint it on the screen.
  * 
  * @author Jonathan Schmidt
  * 
  */
 public class FrameCounter implements IGameLoop {
 
     private static final int COUNT_PER_CYCLE = 10;
 
     private Queue<Long> myTimes;
 
     private double myRate;
 
     private Location myScreenLocation;
 
     /**
      * Creates a new Frame Counter.
      * 
      * @param screenLoc The location to paint the FPS
      */
     public FrameCounter (Location screenLoc) {
         myTimes = new LinkedList<Long>();
         myScreenLocation = new Location(screenLoc);
     }
 
     @Override
     public void update (double elapsedTime) {
 
         myTimes.add(System.currentTimeMillis());
         if (myTimes.size() > COUNT_PER_CYCLE) {
             long change = System.currentTimeMillis() - myTimes.poll();
             double inS = ((double) change) / 1000;
             myRate = COUNT_PER_CYCLE / inS;
         }
     }
 
     @Override
     public void paint (Graphics2D pen) {
         Text f = new Text("FPS: " + String.format("%.2f", myRate));
         f.paint(pen, myScreenLocation, Color.RED);
     }
 }
