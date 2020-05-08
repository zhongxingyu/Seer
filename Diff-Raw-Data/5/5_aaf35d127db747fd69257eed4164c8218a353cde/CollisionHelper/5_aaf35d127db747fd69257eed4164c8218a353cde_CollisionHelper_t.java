 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package math;
 
 public class CollisionHelper {
   /**
    * Approximate method for determining if one moving and one static rectangle
    * will intersect within the given time.
    * @param a the moving rectangle
    * @param av the velocity of the moving rectangle
    * @param b the static rectangle
    * @param dt the time delta
    * @return true or false based on the test result
    */
   public static boolean sweepCollisionTest(Rectangle a, Vector2 av, Rectangle b, float dt) {
     Vector2 vel    = av.multiply(dt);
     Vector2 target = a.getMin().add(vel);
 
     Vector2 sweepDelta = vel.divide(a.getSize().magnitude());
     int sweeps         = (int) (target.distance(a.getMin()) / sweepDelta.magnitude());
     Rectangle sweep    = new Rectangle(a.getMin(), a.getMax());
 
     for (int i = 0; i < sweeps; ++i) {
       if (sweep.isIntersecting(b)) {
         return true;
       }
 
       sweep.addPosition(sweepDelta);
     }
 
     return false;
   }
 
   /**
    * Stops an Rectangle from exiting an container Rectangle by aligning the
    * entity to the edges of the container if it's on the outside.
    *
    * @param entity
    *          The Rectangle to restrict
    * @param cont
    *          The Rectangle to use as box
    */
   public static void blockFromExiting(Rectangle entity, Rectangle cont) {
     Vector2 e = entity.getMin();
     if (e.x < cont.getX1()) {
       e.x = cont.getX1();
     } else if ((e.x + entity.getWidth()) >= cont.getX2()) {
       e.x = cont.getWidth() - entity.getWidth();
     }
 
     if (e.y < cont.getY1()) {
      e.y = cont.getY1();
     } else if ((e.y + entity.getHeight()) >= cont.getY2()) {
      e.y = cont.getY2() - entity.getHeight();
     }
 
     entity.setPosition(e);
   }
 }
