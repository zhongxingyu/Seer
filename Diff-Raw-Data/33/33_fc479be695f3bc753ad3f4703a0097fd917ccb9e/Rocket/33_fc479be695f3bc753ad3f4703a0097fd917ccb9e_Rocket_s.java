 package ua.org.dector.moon_lander;
 
 import com.badlogic.gdx.Gdx;
 
 import static ua.org.dector.moon_lander.AppConfig.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class Rocket {
     private float x;
     private float y;
 
     private float vx;
     private float vy;
     private float ax;
     private float ay;
 
     private float directionAngle;
 
     private boolean moveUp;
     private boolean rotateLeft;
     private boolean rotateRight;
 
     public Rocket() {
         int x = (Gdx.graphics.getWidth() - ROCKET_WIDTH) / 2;
         int y = Gdx.graphics.getHeight();
 
         reset(x, y, 90);
     }
 
     public void reset(int x, int y, float angle) {
         this.x = x;
         this.y = y;
 
         vx = 0;
         vy = 0;
         ax = 0;
         ay = 0;
 
         directionAngle = angle;
 
         moveUp = false;
         rotateLeft = false;
         rotateRight = false;
     }
 
     public float getVy() {
         return vy;
     }
 
     public float getVx() {
         return vx;
     }
 
     public float getY() {
         return y;
     }
 
     public float getX() {
         return x;
     }
 
     public float getDirectionAngle() {
         return directionAngle;
     }
 
     public void updateRocket(float delta) {
         if (rotateLeft) {
             directionAngle += ROCKET_ROTATING * delta;
         }
         if (rotateRight) {
             directionAngle -= ROCKET_ROTATING * delta;
         }
 
         if (directionAngle >= 360) {
             directionAngle -= 360;
         } else if (directionAngle < 0) {
             directionAngle += 360;
         }
 
         if (moveUp) {
             ay = (float)(Math.sin(Math.toRadians(directionAngle)) * ROCKET_AY);
             ax = (float)(Math.cos(Math.toRadians(directionAngle)) * ROCKET_AY);
         } else {
             ay *= FRICTION;
             ax *= FRICTION;
         }
 
         vy += (ay - GRAVITY) * delta;
         vx += ax * delta;
 
         y += vy;
         x += vx;
     }
 
     public void moveUp(boolean moveUp) {
         this.moveUp = moveUp;
     }
 
     public void rotateLeft(boolean rotateLeft) {
         this.rotateLeft = rotateLeft;
     }
 
     public void rotateRight(boolean rotateRight) {
         this.rotateRight = rotateRight;
     }
 
     public boolean isMoveUp() {
         return moveUp;
     }
 }
