 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  * A class that implements a screen object that gives advantageous conditions to the player when acquired in the game
  */
 public abstract class Powerup extends CollidableInteractiveEntity {
 
     /**
      * Initializes powerup values
      * @param name
      * @param image
      * @param position
      * @param collisionShape
      */
    public Powerup(String name, Image image, Vector2f position, Shape collisionShape, int speed) {
        super(name, image, position, collisionShape, speed);
     }
 
     /**
      * Utilize function
      */
     public abstract void utilize();
 
 }
