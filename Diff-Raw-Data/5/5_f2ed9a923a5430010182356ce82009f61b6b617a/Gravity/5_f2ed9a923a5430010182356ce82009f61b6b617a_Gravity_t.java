 package components.physics;
 
 import other.GameTime;
import basics.Vector2;

 import components.interfaces.ICompUpdate;
 
 public class Gravity implements ICompUpdate {
   public static final float FACTOR = 100.0f;
   
   private final AABB body;
   private final float factor;
   
   public Gravity(final AABB body, final float factor) {
     this.body = body;
     this.factor = factor;
   }
 
   @Override
   public void update(GameTime time) {
     final float g = time.getFrameLength() * factor;
    body.addVelocity(new Vector2(0, g));
   }
 }
