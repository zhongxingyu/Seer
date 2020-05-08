 import objectdraw.*;
 import java.awt.Image;
 
 public class Charlie extends ActiveObject {
     private static final double FORWARD_SPEED = .4;
     private static final double BACKWARD_SPEED = .2;
     private static final double JUMP_SPEED = .38;
 
     private static final int MOVE_DISTANCE = 2;
 
     private static final int FORWARD_MOVE_DELAY = (int) (MOVE_DISTANCE / FORWARD_SPEED);
     private static final int BACKWARD_MOVE_DELAY = (int) (MOVE_DISTANCE / BACKWARD_SPEED);
 
     private static final int JUMP_HEIGHT = 150;
 
 
     private VisibleImage charlie;
     private Background background;
 
     private boolean isMovingBackward = false;
     private boolean isMovingForward = false;
     private boolean onScreen = false;
     private boolean isAlive = false;
     private boolean isJumping = false;
 
     public Charlie(Image charlieImage, Background background, Location origin,
             DrawingCanvas canvas) {
         this.charlie = new VisibleImage(charlieImage, origin, canvas);
         this.background = background;
         onScreen = true;
         isAlive = true;
 
         start();
     }
 
     @Override
     public void run() {
         while (isAlive) {
             if (isMovingBackward) {
                 background.move(MOVE_DISTANCE);
                 pause(BACKWARD_MOVE_DELAY);
             } else if (isMovingForward) {
                 background.move(-MOVE_DISTANCE);
                 pause(FORWARD_MOVE_DELAY);
             }
 
             // Move charlie
             if (isJumping) {
                 // Charlie keeps horizontal speed when jumping
                 double initialDistance;
                 double moveDelay = isMovingForward ? FORWARD_MOVE_DELAY : BACKWARD_MOVE_DELAY;
 
                 if (isMovingBackward) {
                     initialDistance = BACKWARD_SPEED * BACKWARD_MOVE_DELAY;
                 } else if (isMovingForward) {
                     initialDistance = -FORWARD_SPEED * FORWARD_MOVE_DELAY;
                 } else {
                     initialDistance = 0;
                 }
                 
                 double jumpDistance = JUMP_SPEED * moveDelay;
                 
 
                 for (int i = 0; i < JUMP_HEIGHT; i += jumpDistance) {
                     charlie.move(0, -jumpDistance);
                     background.move(initialDistance);
                     pause(moveDelay);
                 }
 
                 for (int i = 0; i < JUMP_HEIGHT; i += jumpDistance) {
                     charlie.move(0, jumpDistance);
                     background.move(initialDistance);
                     pause(moveDelay);
                 }
             }
         }
     }
 
     public void jump() {
         this.isJumping = true;
     }
 
     public void moveBackward() {
         this.isMovingBackward = true;
     }
 
     public void moveForward() {
         this.isMovingForward = true;
     }
 
     public void stopMoving() {
         this.isMovingBackward = false;
         this.isMovingForward = false;
     }
 
     public boolean overlaps(VisibleImage image) {
         return charlie.overlaps(image);
     }
 
     public void kill() {
         //isAlive = false;
         //removeFromCanvas();
     }
 
     private void removeFromCanvas() {
         if (onScreen)
             charlie.removeFromCanvas();
 
         onScreen = false;
     }
 
     public void stopJumping() {
         this.isJumping = false;
     }
     
     public boolean isMovingForward() {
         return isMovingForward;
     }
     
     public boolean isMovingBackward() {
        return isMovingBackward();
     }
 }
