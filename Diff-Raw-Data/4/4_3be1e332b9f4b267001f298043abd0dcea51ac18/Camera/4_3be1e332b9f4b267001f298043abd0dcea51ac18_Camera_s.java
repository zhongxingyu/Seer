 package cc.game.TestGame;
 
 import org.lwjgl.util.vector.Vector2f;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Calv
  * Date: 27/02/13
  * Time: 18:08
  * To change this template use File | Settings | File Templates.
  */
 public class Camera {
     private final World game;
     private final Vector2f size;
     private final int xOffset = 0;
     private final int yOffset = 100;
 
     public Camera(World game, Vector2f size) {
         this.game = game;
         this.size = size;
     }
 
//    Todo:
//      - Allow the camera to resize with the screen
//      - Fix the getX to make sure the camera stays only on the background image.

     public float getX() {
         float x = this.game.getPlayer().getBoundingBox().getCenterX() - this.size.getX() / 2.0f;
         Vector2f bounds = this.game.getMapSize();
 
         if (x < 0f) x = 0f;
         if (x > bounds.getX() - this.size.getX()) x = (bounds.getX() - this.size.getX()) + (xOffset);
 
         return x;
     }
 
     public float getY() {
         float y = this.game.getPlayer().getBoundingBox().getCenterY() - this.size.getY() / 2.0f;
         Vector2f bounds = this.game.getMapSize();
 
         if (y < 0f) y = 0f;
         if (y > bounds.getY() - this.size.getY()) y = (bounds.getY() - this.size.getY()) + yOffset;
 
         return y;
     }
 }
