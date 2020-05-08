 package ua.org.dector.ludumdare.ld24;
 
 /**
  * @author dector
  */
 public class Player {
    public static final float ACCELERATION = 0.9f;
     public static final float FRICTION = 0.9f;
     public static final float MAX_SPEED_X = 5f;
    public static final float MAX_SPEED_Y = 5f;
 
     Direction direction = Direction.RIGHT;
     
     float x;
     float y;
     
     float vx;
     float vy;
     float ax;
     float ay;
 
     public Player(float y, float x) {
         this.y = y;
         this.x = x;
     }
 
     public void update(float dt) {
         vx += ax * dt;
        vy += ay * dt;
 
         if (vx > MAX_SPEED_X) vx = MAX_SPEED_X;
         if (vx < -MAX_SPEED_X) vx = -MAX_SPEED_X;
         if (vy > MAX_SPEED_Y) vy = MAX_SPEED_Y;
         if (vy < -MAX_SPEED_Y) vy = -MAX_SPEED_Y;
 
         if (ax == 0) vx *= FRICTION;
        if (ay == 0) vy *= FRICTION;
     }
 }
 
 enum Direction {
     RIGHT, LEFT
 }
