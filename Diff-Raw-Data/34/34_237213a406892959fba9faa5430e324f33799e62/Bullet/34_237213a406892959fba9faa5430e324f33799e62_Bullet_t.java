 package actor;
 
 import math.*;
 
 public class Bullet extends Actor {
     private static final long serialVersionUID = -3860927022451699968L;
     private static final int MAX_AGE = 60 * 5; /* 60 fps * 5 seconds = 300 frames */
     private static final float BULLET_SPEED = 1.0f;
     private static final String MODEL_NAME = "bullet";
     
     
     public Bullet(Actor actor){
         super();
         angularVelocity = new Quaternion(Vector3.UNIT_Y, 1);
         this.velocity = actor.velocity.plus(actor.getDirection().times(BULLET_SPEED));
         position = new Vector3(actor.getPosition());
         rotation = new Quaternion(actor.getRotation());
         modelName = MODEL_NAME;
         setSize(.1f);
     }
     
     /**
      * 
      * @param actor
      * @param positionOffset the offset relative to the actor
      * @param direction
      */
     public Bullet(Actor actor, Vector3 positionOffset){
         this(actor);
         position.plusEquals(positionOffset);
     }
     
     @Override
     public void handleCollision(Actor other) {
         System.err.println("Collision Detected Between " + other + " and " + this);
     }
 
     public void update() {
         super.update();
        
        if (age > MAX_AGE)
            delete();   
     }
 }
