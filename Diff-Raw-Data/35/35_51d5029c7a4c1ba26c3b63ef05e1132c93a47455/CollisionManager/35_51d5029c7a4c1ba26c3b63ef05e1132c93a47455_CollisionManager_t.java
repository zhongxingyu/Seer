 package vooga.scroller.collision_manager;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import util.Vector;
 import vooga.scroller.level_editor.Level;
 import vooga.scroller.level_management.LevelPortal;
 import vooga.scroller.sprites.test_sprites.MarioLib;
 import vooga.scroller.sprites.test_sprites.mario.Mario;
 import vooga.scroller.util.Direction;
 import vooga.scroller.util.Sprite;
 
 /**
  * Currently, we are handling all collisions through CollisionManager. 
  * CollisionManager uses reflection to figure out which visit() method needs to 
  * be called for the Sprites that have just intersected. This is a much more elegant 
  * way of handling collisions than the Visitor method we were previously using. 
  * 
  * 
  * @author Jay Wang
  *
  */
 
 public class CollisionManager {
 
     Level myLevel;
     private static final int COLLISION_GRANULARITY = 15;
     private static final double FRICTION = .5;
        
     public CollisionManager (Level level) {
         myLevel = level;
     }
         
     public void handleCollision (Sprite sprite1, Sprite sprite2) {
 
         Class[] classArray = {sprite1.getClass(), sprite2.getClass()};
         Object[] sprites = {sprite1, sprite2};
 
         try {
             Method method = this.getClass().getMethod("visit", classArray);
             method.invoke(this, sprites);
         }
         
         catch (SecurityException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         /**
          * If there is No Suck Method, that means the game designer does not want 
          * anything to happen when this collision occurs. 
          */
         catch (NoSuchMethodException e) {
             return;
         }
         
         catch (IllegalArgumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         catch (IllegalAccessException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (InvocationTargetException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
     
     }
     
     
     /**
      * Helper Methods for getting Collision Direction
      * @param Sprite sprite1
      * @param Sprite sprite2
      * @return one of four Direction enums
      */
     private Direction collisionDirection (Sprite sprite1, Sprite sprite2) {  
         if (checkTop(sprite1, sprite2)) return Direction.TOP;
         else if (checkBottom(sprite1, sprite2)) return Direction.BOTTOM;
         else if (checkLeft(sprite1, sprite2)) return Direction.LEFT;
         else if (checkRight(sprite1, sprite2)) return Direction.RIGHT;        
         return null;
     }
     
     private boolean checkTop(Sprite sprite1, Sprite sprite2) {
         return (sprite1.getBottom() >= sprite2.getTop() && sprite1.getBottom() <= sprite2.getTop() + COLLISION_GRANULARITY);
     }
     
     private boolean checkBottom(Sprite sprite1, Sprite sprite2) {
         return (sprite1.getTop() <= sprite2.getBottom() && sprite1.getTop() >= sprite2.getBottom() - COLLISION_GRANULARITY);
     }
     
     private boolean checkLeft(Sprite sprite1, Sprite sprite2) {
         return (sprite1.getRight() >= sprite2.getLeft() && sprite1.getRight() <= sprite2.getLeft() + COLLISION_GRANULARITY);
     }
     
     private boolean checkRight(Sprite sprite1, Sprite sprite2) {
         return (sprite1.getLeft() <= sprite2.getRight() && sprite1.getLeft() >= sprite2.getRight() - COLLISION_GRANULARITY);
     }
     
     private void marioAndNonStaticEntityCollision (Mario mario, Sprite sprite) {
   
         
         Direction collisionType = collisionDirection(mario, sprite);
 
         if (collisionType == null) return;
         
         switch (collisionType) {
             case TOP:
                 mario.setCenter(mario.getX(), sprite.getTop() - (mario.getHeight() / 2));
                 Vector v = mario.getVelocity().getComponentVector((double)Sprite.DOWN_DIRECTION);
                 v.negate();
                 mario.addVector(v);
                 
                 
                 Vector right = mario.getVelocity().getComponentVector(Sprite.RIGHT_DIRECTION);
                 Vector left = mario.getVelocity().getComponentVector(Sprite.LEFT_DIRECTION);
                 
                 right.negate();
                 right.scale(FRICTION);
                 left.negate();
                 left.scale(FRICTION);
                 mario.addVector(right);
                 mario.addVector(left);
                 
                Vector sLeft = sprite.getVelocity().getComponentVector(Sprite.LEFT_DIRECTION);
                sLeft.scale(.5);
                Vector sRight = sprite.getVelocity().getComponentVector(Sprite.RIGHT_DIRECTION);
                sRight.scale(.5);
                
                
                mario.addVector(sRight);
                mario.addVector(sLeft);

                 
                 break;
             case BOTTOM:
                 mario.setCenter(mario.getX(), sprite.getBottom() + (mario.getHeight() / 2));
                 //mario.addVector(force);
 
                 break;
             case LEFT:
                 mario.setCenter(sprite.getLeft() - (mario.getWidth() / 2), mario.getY());
                 //mario.addVector(force);
 
                 
                 break;
             case RIGHT:
                 mario.setCenter(sprite.getRight() + (mario.getWidth() / 2), mario.getY());
                 
                 //mario.addVector(force);
 
                 
                 break;
             default: 
                 break;
         }
     }
     
     
     
     /**
      * Begin the long slew of visit() methods... 
      * Thinking about ways to refactor/repackage this.
      * 
      */
     
     public void visit (Mario mario, Mario mario2) {
         System.out.println("Mario has just collided with Mario!");
         
     }
     
     public void visit (Mario mario, MarioLib.Coin coin) {
         System.out.println("Mario just collected a coin");
         
     }
 
     public void visit (Mario mario, MarioLib.Koopa koopa) {
         System.out.println("Mario has just collided with Koopa!");
         
     }
 
     public void visit (Mario mario, MarioLib.Platform platform) {
         marioAndNonStaticEntityCollision(mario, platform);
         System.out.println("Mario has just collided with Platform!");
         
     }
     
    public void visit (Mario mario, MarioLib.MovingPlatformTwo movingPlatform) {
         marioAndNonStaticEntityCollision(mario, movingPlatform);
         System.out.println("Mario has just collided with Platform!");
         
     }
     
     public void visit (Mario mario, MarioLib.Turtle turtle) {
         //endGame();
         mario.hit(turtle);
         System.out.println("Mario has just collided with Turtle!");
         
     }
     
     public void visit (Mario mario, LevelPortal sp) {
         System.out.println("Hit portal");
         sp.goToNextStartPoint(mario);
     }
     
     public void visit (MarioLib.Coin coin, Mario mario) {
         myLevel.removeSprite(coin);
     }
 
     public void visit (MarioLib.Coin coin, MarioLib.Coin coin2) {
         System.out.println("Coin has just collided with Coin!");
         
     }
 
     public void visit (MarioLib.Coin coin, MarioLib.Koopa koopa) {
         System.out.println("Coin has just collided with Koopa!");
         
     }
 
     public void visit (MarioLib.Coin coin, MarioLib.Platform platform) {
         System.out.println("Coin has just collided with Platform!");
         
     }
 
     public void visit (MarioLib.Coin coin, MarioLib.Turtle turtle) {
         System.out.println("Coin has just collided with Turtle!");
     }
 
     public void visit (MarioLib.Koopa koopa, Mario mario) {
         mario.changeState(1);
         System.out.println("Koopa has just collided with Mario!");
     }
 
     public void visit (MarioLib.Koopa koopa, MarioLib.Coin coin) {
         System.out.println("Koopa has just collided with Coin!");
         
     }
 
     public void visit (MarioLib.Koopa koopa, MarioLib.Koopa koopa2) {
         System.out.println("Koopa has just collided with Koopa!");
         
     }
 
     public void visit (MarioLib.Koopa koopa, MarioLib.Platform platform) {
         System.out.println("Koopa has just collided with Platform!");
         
     }
 
     public void visit (MarioLib.Koopa koopa, MarioLib.Turtle turtle) {
         System.out.println("Koopa has just collided with Turtle!");
         
     }
     
     public void visit (MarioLib.Platform platform, Mario mario) {
         //System.out.println("Platform has just collided with Mario!");
         
     }
 
     public void visit (MarioLib.Platform platform, MarioLib.Coin coin) {
         System.out.println("Platform has just collided with Coin!");
         
     }
 
     public void visit (MarioLib.Platform platform, MarioLib.Koopa koopa) {
         System.out.println("Platform has just collided with Koopa!");
         
     }
 
     public void visit (MarioLib.Platform platform, MarioLib.Platform platform2) {
         System.out.println("Platform has just collided with Platform!");
         
     }
 
     public void visit (MarioLib.Platform platform, MarioLib.Turtle turtle) {
         System.out.println("Platform has just collided with Turtle!");
         
     }
     
     public void visit (MarioLib.Turtle turtle, Mario mario) {
         System.out.println("Turtle has just collided with Mario!");
         
     }
 
     public void visit (MarioLib.Turtle turtle, MarioLib.Coin coin) {
         System.out.println("Turtle has just collided with Coin!");
         
     }
 
     public void visit (MarioLib.Turtle turtle, MarioLib.Koopa koopa) {
         System.out.println("Turtle has just collided with Koopa!");
         
     }
 
     public void visit (MarioLib.Turtle turtle, MarioLib.Platform platform) {
         System.out.println("Turtle has just collided with Platform!");
         
     }
 
     public void visit (MarioLib.Turtle turtle, MarioLib.Turtle turtle2) {
         System.out.println("Turtle has just collided with Turtle!");
         
     }
      
     private void endGame () {
         myLevel.getView().win();        
     }
     
     
 }
