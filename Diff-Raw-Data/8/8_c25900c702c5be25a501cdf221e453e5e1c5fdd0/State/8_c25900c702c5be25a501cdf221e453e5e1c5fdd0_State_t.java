 package vooga.fighter.model.utils;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.GameObject;
 import util.Location;
 import util.Pixmap;
 
 
 /**
  * Holds a series of rectangles and images representing one state of an object.
  * Additionally holds a series of dimensions representing the size of each frame,
  * as well as a series of integers representing frame delay for each frame.
  * Used for collision detection and handling, as well as animation.
  * 
  * @author James Wei, David Le
  * 
  */
 public class State {
 
     public int myNumFrames;
     public int myCurrentFrame;
 
     /**
      * The priority of the hitbox when interacting with other hitboxes.
      * Lower numbers indicate higher priority.
      */
     private int myPriority;
 
     /**
      * The depth of the animations when overlapping with other animations.
      * Lower numbers indicate "deeper" images, aka images with lower depth
      * are drawn first (and thus other animations are drawn on top of them).
      */
     private int myDepth;
 
     /**
      * True if the animation for this state loops, false otherwise.
      */
     private boolean myLooping;
 
     /**
      * Maintains current frame delay count. Used to remain in the same frame of
      * an animation for more than one game loop.
      */
     private int myDelay;
 
     private Pixmap[] myImages;
     private Rectangle[] myRectangles;
     private Dimension[] mySizes;
     private Integer[] myFrameDelays;
     
     private GameObject myOwner;
 
     /**
      * Creates a state with the given owner, number of frames, and default priority
      * and depth of zero. State has default frame delays of zero.
      */
     public State(GameObject owner, int numFrames) {
         this(owner, numFrames, 0, 0);
     }
 
     /**
      * Creates a state with the given owner, number of frames, priority, and depth.
      * State has default frame delays of zero.
      */
     public State(GameObject owner, int numFrames, int priority, int depth) {
         myOwner = owner;
         myNumFrames = numFrames;
         myPriority = priority;
         myDepth = depth;
         mySizes = new Dimension[myNumFrames];
         myRectangles = new Rectangle[myNumFrames];
         myImages = new Pixmap[myNumFrames];
         myFrameDelays = new Integer[myNumFrames];
         myCurrentFrame = 0;
         myDelay = 0;
         myLooping = false;
         populateAllDelays(0);
     }
 
     /**
      * Sets looping boolean. If this method is not called, looping defaults to false.
      */
     public void setLooping(boolean looping) {
         myLooping = looping;
     }
 
     /**
      * Adds a rectangle this state's rectangle array.
      */
     public void populateRectangle(Rectangle rect, int index) {
         myRectangles[index] = rect;
     }
 
     /**
      * Adds a Pixmap into this state's Pixmap array.
      */
     public void populateImage(Pixmap image, int index) {
         myImages[index] = image;
         mySizes[index] = image.getSize();
     }
 
     /**
      * Adds a Dimension into this state's Dimension array.
      */
     public void populateSize(Dimension size, int index) {
         mySizes[index] = size;
     }
     
     /**
      * Adds a frame delay represented by an Integer into this state's Integer
      * array. By default, all frame delays are 0. If a frame has a delay greater
      * than 0, then that frame will persist for an extra number of frames equal
      * to the frame delay. Negative delays are not allowed.
      */
     public void populateFrameDelay(Integer delay, int index) {
         if (delay < 0) {
             return;
         }
         myFrameDelays[index] = delay;
     }
     
     /**
      * Shortcut method for populating the entire size array. Useful if a state
      * never changes sizes throughout its animation.
      */
     public void populateAllSizes(Dimension size) {
         for (int i=0; i<mySizes.length; i++) {
             mySizes[i] = size;
         }
     }
 
     /**
      * Shortcut method for populating the entire delay array. Useful if a state
      * never changes frame delay throughout its animation.
      */
     public void populateAllDelays(Integer delay) {
         for (int i=0; i<myFrameDelays.length; i++) {
             myFrameDelays[i] = delay;
         }
     }
     
     /**
      * Returns the current active rectangle for this state.
      */
     public Rectangle getCurrentRectangle() {
         Rectangle result = myRectangles[myCurrentFrame];
         Location location = myOwner.getLocation().getLocation();
        result.setLocation((int) location.getX() - getCurrentSize().width/2, (int) location.getY()  - getCurrentSize().height/2);
         return result;
     }
 
     /**
      * Returns the current active image for this state.
      */
     public Pixmap getCurrentImage() {
         return myImages[myCurrentFrame];
     }
 
     /**
      * Returns the current active size for this state.
      */
     public Dimension getCurrentSize() {
         return mySizes[myCurrentFrame];
     }
 
     /**
      * Returns the priority of this state. Lower numbers are considered higher
      * priority.
      */
     public int getPriority() {
         return myPriority;
     }
 
     /**
      * Returns the depth of this state. Lower numbers are considered lower depth,
      * i.e. an image with a lower depth will be drawn first (and thus other
      * images will be drawn on top of it).
      */
     public int getDepth() {
         return myDepth;
     }
 
     /**
      * Returns true if this state's hitbox has priority over another, false otherwise.
      * Note that returning false DOES NOT necessarily indicate that the other
      * hitbox has priority, as the two could have equal priorities.
      */
     public boolean hasPriority(State other) {
         int difference = myPriority - other.getPriority();
         return (difference < 0);
     }
 
     /**
      * Returns true if this state's image is deeper than another, false otherwise.
      * Note that returning false DOES NOT necessarily indicate that the other
      * image is deeper, as the two could have equal depths.
      */
     public boolean hasDepth(State other) {
         int difference = myDepth - other.getDepth();
         return (difference < 0);
     }
 
     /**
      * Returns true if the state has been properly initialized, i.e. if all arrays
      * have been properly populated. Since frame delay array is populated with zeroes
      * by default, it is not checked. Useful for debugging.
      */
     public boolean checkInitialization() {
         for (int i=0; i<myNumFrames; i++) {
             if (mySizes[i] == null || myImages[i] == null || myRectangles[i] == null) {
                 return false;
             }
         }
         return true;
     }
     
     /**
      * Progresses this state to the next frame in its animation and hitbox. If the
      * state progresses past its final frame and is looping, it will reset. Note
      * that it is possible for a state to progress past its final frame--this is
      * the moment when the state is considered "complete". It is incumbent on the
      * developer to properly handle state switching when this occurs if the state
      * is not looping, or exceptions will be encountered.
      */
     public void update() {
         if (myDelay == myFrameDelays[myCurrentFrame]) {
             myCurrentFrame++;
             myDelay = 0;
         } else {
             myDelay++;
         }
         if (hasCompleted() && myLooping) {
             resetState();
         }
     }
 
     /**
      * Resets the current frame to the beginning of the state.
      */
     public void resetState() {
         myCurrentFrame = 0;
     }
 
     /**
      * Returns true if the state's animation has concluded, false otherwise.
      * Concluded in this sense means after the final animation has updated, i.e.
      * a concluded state has progressed to a frame beyond the number of frames
      * it actually has.
      */
     public boolean hasCompleted() {
         return (myCurrentFrame >= myNumFrames);
     }
 }
