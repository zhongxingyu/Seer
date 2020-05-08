 package com.gravity.player;
 
 import java.util.List;
 import java.util.Set;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 import org.newdawn.slick.geom.Vector2f;
 
 import com.gravity.gameplay.GravityGameController;
 import com.gravity.map.GameWorld;
 import com.gravity.physics.Collision;
 import com.gravity.physics.Entity;
 
 public class Player implements Entity {
 
     public static enum Movement {
         LEFT, RIGHT, STOP
     }
 
     public static int TOP_LEFT = 0, TOP_RIGHT = 1, BOT_RIGHT = 2, BOT_LEFT = 3;
 
     private GravityGameController game;
 
     // PLAYER STARTING CONSTANTS (Units = pixels, milliseconds)
 
    private final float JUMP_POWER = 2f / 3f;
    private final float MOVEMENT_INCREMENT = 1f / 4f;
     private final float MAX_HEALTH = 10;
     private final float MAX_VEL = 100f;
     private final float VEL_DAMP = 0.5f;
     private final float GRAVITY = 1.0f / 500f;
 
     private final Shape BASE_SHAPE = new Rectangle(1f, 1f, 15f, 32f);
 
     // PLAYER CURRENT VALUES
     private GameWorld map;
 
     // position and magnitude
 
     // TODO: bring these back into tile widths instead of pixel widths
     private Vector2f acceleration = new Vector2f(0, 0);
     private Vector2f position = new Vector2f(50, 700);
     private Vector2f velocity = new Vector2f(0, 0);
     private Vector2f facing = new Vector2f(0, 1);
     private float health;
     private Shape myShape;
 
     // GAME STATE STUFF
     private boolean onGround = true;
 
     private final String name;
 
     public Player(GameWorld map, GravityGameController game, String name) {
         health = MAX_HEALTH;
         velocity = new Vector2f(0, 0);
         this.map = map;
         this.game = game;
         this.myShape = BASE_SHAPE;
         this.name = name;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("Player [name=");
         builder.append(name);
         builder.append("]");
         return builder.toString();
     }
 
     // //////////////////////////////////////////////////////////////////////////
     // //////////////////////////GET & SET METHODS///////////////////////////////
     // //////////////////////////////////////////////////////////////////////////
     public Vector2f getPosition() {
         return getPosition(0);
     }
 
     @Override
     public Vector2f getPosition(int ticks) {
         return new Vector2f(position.x + (velocity.x * ticks), position.y + (velocity.y * ticks));
     }
 
     @Override
     public Shape getShape(int ticks) {
         return BASE_SHAPE.transform(Transform.createTranslateTransform(position.x + (velocity.x * ticks), position.y + (velocity.y * ticks)));
     }
 
     @Override
     public Vector2f getVelocity(int ticks) {
         return velocity.copy();
     }
 
     // //////////////////////////////////////////////////////////////////////////
     // //////////////////////////KEY-PRESS METHODS///////////////////////////////
     // //////////////////////////////////////////////////////////////////////////
 
     /**
      * @param jumping
      *            true if keydown, false if keyup
      */
     public void jump(boolean jumping) {
         if (jumping && onGround) {
             velocity.y -= JUMP_POWER;
             onGround = false;
         }
     }
 
     /**
      * 
      * @param direction
      */
     public void move(Movement direction) {
         switch (direction) {
         case LEFT: {
             velocity.x = -MOVEMENT_INCREMENT;
             break;
         }
         case RIGHT: {
             velocity.x = MOVEMENT_INCREMENT;
             break;
         }
         case STOP: {
             velocity.x = 0;
             break;
         }
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////
     // //////////////////////////COLLISION METHODS///////////////////////////////
     // //////////////////////////////////////////////////////////////////////////
 
     @Override
     public Shape handleCollisions(int millis, List<Collision> collisions) {
         for (Collision c : collisions) {
             Entity them = c.getOtherEntity(this);
 
             if ((them.getShape(millis) instanceof Rectangle)) {
                 terrainCollision(c, millis);
             } else {
                 throw new RuntimeException("Cannot resolve non-Rectangle collision.");
             }
         }
         return myShape;
     }
 
     @Override
     public Shape rehandleCollisions(int ticks, List<Collision> collisions) {
         throw new RuntimeException("Cannot resolve re-collision.");
     }
 
     /**
      * Handles collision with terrain
      */
     private void terrainCollision(Collision collidee, int millis) {
         // position.add(velocity.scale((float) (millis - (10000.0 / 1000))));
         // updateShape();
         // If I'm overlapping their xcoord
         Shape sh = getShape(millis);
         Shape oth = collidee.getOtherEntity(this).getShape(millis);
         Set<Integer> points = collidee.getMyCollisions(this);
         if (points.contains(TOP_RIGHT) && points.contains(BOT_RIGHT)) {
             // collision right
             velocity.x = 0;
         } else if (points.contains(TOP_LEFT) && points.contains(BOT_LEFT)) {
             // collision left
             velocity.x = 0;
         }
         // If I'm overlapping their ycoord
         else if (points.contains(TOP_LEFT) && points.contains(TOP_RIGHT)) {
             // collision top
             velocity.y = 0;
             onGround = false;
         } else if (points.contains(BOT_LEFT) && points.contains(BOT_RIGHT)) {
             // collision bottom
             velocity.y = 0;
             onGround = true;
         } else if (points.contains(TOP_LEFT) || points.contains(BOT_LEFT)) {
             velocity.y = 0;
             velocity.x = Math.abs(velocity.x);
         } else if (points.contains(TOP_RIGHT) || points.contains(BOT_RIGHT)) {
             velocity.y = 0;
             velocity.x = -Math.abs(velocity.x);
         } else {
             throw new RuntimeException("No overlap detected: " + sh.toString() + " with " + oth.toString());
         }
         updateShape();
     }
 
     public void takeDamage(float damage) {
         health -= damage;
     }
 
     public void heal(float heal) {
         health += heal;
     }
 
     // //////////////////////////////////////////////////////////////////////////
     // //////////////////////////ON-TICK METHODS/////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////
     @Override
     public void tick(int millis) {
         updatePosition(millis);
         updateAcceleration(millis);
         updateVelocity(millis);
     }
 
     public void updateAcceleration(float millis) {
         if (onGround) {
             acceleration.y = 0;
         } else {
             acceleration.y = GRAVITY;
         }
     }
 
     public void updateVelocity(float millis) {
         // dv = a
         velocity.add(acceleration.copy().scale(millis));
 
         // velocity < maxVel
         if (velocity.length() > MAX_VEL * millis) {
             velocity.scale(MAX_VEL * millis / velocity.length());
         }
     }
 
     public void updatePosition(float millis) {
         position.add(velocity.copy().scale(millis));
         updateShape();
     }
 
     /*
      * Sets onGround depending on if the player is on the ground or not
      * 
      * 
      * /** CALL THIS EVERY TIME YOU DO ANYTHING TO POSITION OR SHAPE >>>>>>> 578f54515a017ccc7211c613d175bbac8740860c
      */
     public void updateShape() {
         myShape = BASE_SHAPE.transform(Transform.createTranslateTransform(position.x, position.y));
     }
 }
