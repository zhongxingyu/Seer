 import java.awt.Image;
 
 import objectdraw.*;
 
 public class Hoop extends ActiveObject {
     private static final int MOVE_DELAY = 20;
     
     private VisibleImage theRing;
     private Charlie charlie;
     private double baseSpeed;
     
     public Hoop (Image ringImage, Charlie charlie, Location loc, double speed, DrawingCanvas canvas) {
         theRing = new VisibleImage(ringImage, loc, canvas);
         this.baseSpeed = speed;
         this.charlie = charlie;
                 
         start();
     }
     
     @Override
     public void run () {
         while (theRing.getX() + theRing.getWidth() > 0) {
             double speed = baseSpeed;
             
            if (charlie.isMovingForward() && !charlie.isJumping()) {
                 // Make speed faster
                 speed += .2;
            } else if (charlie.isMovingBackward() && !charlie.isJumping()) {
                 // Make speed slower
                speed -= .1;
             }
             
             double moveDistance = speed * MOVE_DELAY;
             
             theRing.move(-moveDistance, 0);
             if (charlie.overlaps(theRing)) {
                 charlie.kill();
             }
             pause(MOVE_DELAY);
         }
     }
 }
