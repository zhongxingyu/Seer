 package ua.org.dector.ludumdare.ld24;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author dector
  */
 public class Player {
     public static final float RUNNING = 3.5f;
     public static final float SWIMMING = 3.5f;
    public static final float JUMPING = 3.55f;
     public static final float GRAVITY = 0.8f;
 
     public static final float FRICTION = 0.8f;
     public static final float AIR_FRICTION = 1 - 0.1f;
     public static final float WATER_FRICTION = 0.9f;
 
     public static final float MAX_SPEED_X = 2.5f;
     public static final float MAX_SPEED_Y = 4f;
 
     Direction direction = Direction.RIGHT;
     int gravityDirection = -1;
     boolean gravityAffection = true;
 
     List<Ability> abilities;
 
     boolean canJump;
     boolean isJumping;
     boolean jumpCommand;
     boolean win;
 
     State state;
 
     float x;
     float y;
     
     float vx;
     float vy;
     float ax;
     float ay;
 
     public Player(int y, int x) {
         restart(x, y);
     }
 
     public void update(float dt) {
         if (ax != 0 && (vx > 0 && ax < 0 || vx < 0 && ax > 0))
             vx = ax * dt;
         else
             vx += ax * dt;
 
         if (gravityAffection) {
             if (state == State.RUNNING)
                 ay += gravityDirection * GRAVITY;
             else
                 ay += gravityDirection * GRAVITY / 10;
         }
         vy += ay * dt;
 
         if (state == State.SWIM) {
             vx *= WATER_FRICTION;
             vy *= WATER_FRICTION;
         } else if (ax == 0) {
             if (isJumping)
                 vx *= AIR_FRICTION;
             else
                 vx *= FRICTION;
         }
 
         if (vx > MAX_SPEED_X) vx = MAX_SPEED_X;
         if (vx < -MAX_SPEED_X) vx = -MAX_SPEED_X;
         if (vy > MAX_SPEED_Y) vy = MAX_SPEED_Y;
         if (vy < -MAX_SPEED_Y) vy = -MAX_SPEED_Y;
     }
 
     public void jump() {
         if (canJump)
             jumpCommand = true;
     }
 
     public void restart(int spawnX, int spawnY) {
         x = spawnX;
         y = spawnY;
 
         win = false;
         vx = vy = ax = ay = 0;
         gravityDirection = -1;
         gravityAffection = true;
         isJumping = jumpCommand = false;
         direction = Direction.RIGHT;
 
         canJump = true;
         
         abilities = new LinkedList<Ability>();
 
         state = State.RUNNING;
     }
 
     public void clearSlick() {
         gravityAffection = true;
         abilities.remove(Ability.SLICK);
     }
 
     public void stop() {
         vx = vy = ax = ay = 0;
     }
 }
 
 enum Direction {
     RIGHT, LEFT
 }
 
 enum Ability {
     SWIM, SLICK, SOLID, LIQUID
 }
 
 enum State {
     DIE, SWIM, RUNNING, GAS
 }
