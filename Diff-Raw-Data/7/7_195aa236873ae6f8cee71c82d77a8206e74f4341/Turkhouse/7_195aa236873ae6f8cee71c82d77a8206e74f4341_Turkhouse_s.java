 package turkhouse;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.InputEvent;
 import java.util.Random;
 
 public class Turkhouse extends java.awt.Robot implements Runnable {
 
     private static final Random RNG = new Random();
     private static final int SECOND = 1000;
 
     /** The "Search" button. */
     private static final Point SEARCH = new Point(800, 188);
 
     /** The "Buyout" button. */
     private static final Point BUYOUT = new Point(760, 560);
 
     /** The "Accept" button, which pops up after clicking buyout. */
     private static final Point ACCEPT = new Point(770, 220);
 
     /** The silver coin on the top item's buyout. */
     private static final Point SILVER = new Point(810, 270);
 
     /** The gold coin on the top item's buyout. */
     private static final Point GOLD = new Point(772, 270);
 
     public static void main(String[] args) {
         try {
             new Thread(new Turkhouse()).start();
         } catch (java.awt.AWTException e) {
             System.out.println("error: failed to start robot");
         }
     }
 
     public Turkhouse() throws java.awt.AWTException {
         super();
        setAutoDelay(250);
     }
 
     @Override
     public void run() {
         delay(1 * SECOND);
         while (true) {
             click(SEARCH);
             delay((int) (1.5 * SECOND));
             while (isCheap()) {
                 System.out.println("Looks cheap, buying.");
                 click(BUYOUT);
                 click(ACCEPT);
                 delay(1 * SECOND);
             }
            int delay = RNG.nextInt(10 * SECOND) + 10 * SECOND;
             System.out.printf("Waiting %.1f seconds.\n", delay / 1000.0);
             delay(delay);
         }
     }
 
     /**
      * Click a point on the screen.
      * @param p  the point to click
      */
     private void click(Point p) {
         mouseMove(p.x, p.y);
         mousePress(InputEvent.BUTTON1_MASK);
         mouseRelease(InputEvent.BUTTON1_MASK);
     }
 
     /**
      * Sample the color at a screen point.
      * @param p  the point to be sampled
      * @return the color at the sampled point
      */
     private Color sample(Point p) {
         Rectangle pixel = new Rectangle(p.x, p.y, 1, 1);
         return new Color(createScreenCapture(pixel).getRGB(0, 0));
     }
 
     /**
      * Determines if the current top item is less than 1 silver.
      * @return true if the current top AH item is cheap
      */
     private boolean isCheap() {
         Color silver = sample(SILVER);
         Color gold = sample(GOLD);
         return (average(gold) < 127) && (average(silver) < 127);
     }
 
     /**
      * Determine the average color value.
      * @param c  the color to work with
      * @return the average color value
      */
     private int average(Color c) {
         return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
     }
 }
