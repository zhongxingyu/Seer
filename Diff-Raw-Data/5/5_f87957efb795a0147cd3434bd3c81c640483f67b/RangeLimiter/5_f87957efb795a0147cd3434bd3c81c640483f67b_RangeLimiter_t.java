 package game.components.misc;
 
 import game.components.ComponentMessage;
 import game.components.ComponentType;
 import game.components.interfaces.ILogicComponent;
 import game.entities.IEntity;
 import math.Vector2;
 import math.time.GameTime;
 
 public class RangeLimiter implements ILogicComponent {
  public static class TimePos {
     public final float time;
     public final Vector2 pos;
 
     public TimePos(final float time, final Vector2 pos) {
       this.time = time;
      this.pos = new Vector2(pos);
     }
   }
 
   private IEntity owner;
 
   private TimePos start;
 
   private final float duration;
   private final float range;
 
   public RangeLimiter(final float duration, final float range) {
     this.duration = duration;
     this.range    = range;
   }
 
   @Override
   public void update(final GameTime time) {
     if ((duration != -1) && ((time.getElapsed() - start.time) > duration)) {
       owner.sendMessage(ComponentMessage.KILL, null);
     }
     if ((range != -1) && (owner.getBody().getMin().distance(start.pos) > range)) {
       owner.sendMessage(ComponentMessage.KILL, null);
     }
   }
 
   @Override
   public void reciveMessage(ComponentMessage message, Object args) {
     if (message == ComponentMessage.START_AT) {
       start = (TimePos) args;
     }
   }
 
   @Override
   public ComponentType getComponentType() {
     return ComponentType.RANGE_LIMITER;
   }
 
   @Override
   public void setOwner(IEntity owner) {
     this.owner = owner;
   }
 }
