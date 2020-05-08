 package vooga.fighter.model;
 
import java.awt.Rectangle;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 import vooga.fighter.model.objects.AttackObject;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.EnvironmentObject;
 import vooga.fighter.model.objects.GameObject;
 import vooga.fighter.model.objects.MapObject;
 
 /**
  * Detects collisions between all the game objects. Collision handling is achieved
  * in the game objects themselves, and 
  * 
  * @author James Wei, alanni
  * 
  */
 public class CollisionManager {
 
     /**
      * Checks for collisions between the game objects.
      */
     public void checkCollisions(List<GameObject> myObjects) {
         for (int i = 0; i < myObjects.size() - 1; i++) {
             for (int j = i + 1; j < myObjects.size(); j++) {
                 GameObject o1 = myObjects.get(i);
                 GameObject o2 = myObjects.get(j);
                 if (o1 instanceof MapObject || o2 instanceof MapObject) {
                     continue;
                 }
                 if (o1.getCurrentState().getCurrentRectangle()
                         .intersects(o2.getCurrentState().getCurrentRectangle())) {
                     handleCollisions(o1, o2);
                 }
             }
         }
     }
 
 //    /**
 //     * Applies collisions between collided game objects. Collisions are handled
 //     * through an implementation of the visitor design pattern.
 //     * 
 //     * Note: if you want to use the old visitor framework, uncomment this method
 //     * and comment out the handleCollisions method with reflection below.
 //     */
 //    public void handleCollisions(GameObject o1, GameObject o2) {
 //        o1.dispatchCollision(o2);
 //    }    
     
     /**
      * Delegates to specific collision methods based on the runtime type of our
      * colliding game objects using reflection.
      */
     public void handleCollisions(GameObject o1, GameObject o2) {
         try {            
             Class<?>[] runtimeClasses = new Class[]{o1.getClass(), o2.getClass()};
             Object[] parameters = new Object[]{o1, o2};
             Method method = this.getClass().getMethod("collide", runtimeClasses);
             method.invoke(this, parameters);
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         }
     }        
     
     /**
      * Handles collisions between two character objects.
      */
     public void collide(CharacterObject o1, CharacterObject o2) {
         System.out.println("CollisionManager: Two CharacterObjects collided!");
     }
     
     /**
      * Handles collisions between two attack objects.
      */
     public void collide(AttackObject o1, AttackObject o2) {
         System.out.println("CollisionManager: Two AttackObjects collided!");
     }
     
     /**
      * Handles collisions between two environment objects.
      */
     public void collide(EnvironmentObject o1, EnvironmentObject o2) {
         System.out.println("CollisionManager: Two EnvironmentObjects collided!");
     }
     
     /**
      * Handles collisions between a character object and an attack object.
      */
     public void collide(CharacterObject o1, AttackObject o2) {
         System.out.println("CollisionManager: CharacterObject and AttackObject collided!");
     }
     
     /**
      * Handles collisions between an attack object and a character object.
      */
     public void collide(AttackObject o1, CharacterObject o2) {
         System.out.println("CollisionManager: AttackObject and CharacterObject collided!");
     }
     
     /**
      * Handles collisions between an environment object and an attack object.
      */
     public void collide(EnvironmentObject o1, AttackObject o2) {
         System.out.println("CollisionManager: EnvironmentObject and AttackObject collided!");
     }
     
     /**
      * Handles collisions between an attack object and an environment object.
      */
     public void collide(AttackObject o1, EnvironmentObject o2) {
         System.out.println("CollisionManager: Attackobject and EnvironmentObject collided!");
     }
     
     /**
      * Handles collisions between a character object and an environment object.
      */
     public void collide(CharacterObject o1, EnvironmentObject o2) {
         System.out.println("CollisionManager: CharacterObject and EnvironmentObject collided!");
     }
     
     /**
      * Handles collisions between an environment object and a character object.
      */
     public void collide(EnvironmentObject o1, CharacterObject o2) {
         System.out.println("CollisionManager: EnvironmentObject and CharacterObject collided!");
     }
 }
