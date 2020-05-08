 package ca.couchware.wezzle2d.animation;
 
 import ca.couchware.wezzle2d.util.IBuilder;
 import ca.couchware.wezzle2d.util.CouchLogger;
 import ca.couchware.wezzle2d.graphics.IEntity;
 import ca.couchware.wezzle2d.manager.Settings;
 import ca.couchware.wezzle2d.util.ImmutablePosition;
 import ca.couchware.wezzle2d.util.NumUtil;
 
 /**
  * An animation that starts an explosion in the middle of the entity.
  * 
  * @author cdmckay
  */
 public class MoveAnimation extends AbstractAnimation
 {    
     
     /**
      * The finish rules.
      */
     public enum FinishRule
     {
         /**
          * Finish the animation when the first coordinate is met (default).
          */
         FIRST,        
         
         /**
          * Finish the animation only when both coordinates are met.
          */
         BOTH
     }
     
     /**
      * Is skip on?  This is engaged when both x and y boundaries are met to
      * tell the animation to skip calculations until done.
      */
     private boolean skip = false;
     
     /**
      * Counts the number of ticks.
      */
     private int ticks;
     
     /**
      * The entity that is being moved.
      */
     final private IEntity entity;
     
     /**
      * The initial position.
      */
     private ImmutablePosition initialPosition;
     
     /**
      * The x-component of the launch speed, in pixels / tick.
      */
     private int speedX;
     
     /**
      * The y-component of the launch speed, in pixels / tick.
      */
     private int speedY;              
     
     /**
      * The minimum x that the animation may move to.
      */
     private int minX;
     
     /**
      * The minimum y that the animation may move to.
      */    
     private int minY;
     
     /**
      * The maximum x that the animation may move to.
      */
     private int maxX;
     
     /**
      * The maximum y that the animation may move to.
      */
     private int maxY;   
 
     /**
      * The launch angle, in degrees.
      */
     private int theta;
 
     /** The theta offset. */
     private double thetaOffset;
 
     /**
      * The anglar velocity, in degrees / sec.
      */
     private double omega;
     
     /**
      * The gravity, in pixels / sec^2.
      */
     private int gravity;
     
     /**
      * The amount of time to wait before starting.
      */
     private int wait;
     
     /**
      * Whether or not wait period is finished.
      */
     private boolean waitFinished = false;
     
     /**
      * The max time for the animation to run for, in ticks.
      */
     private int duration;
     
     /**
      * The finish rule for the animation.
      */
     private final FinishRule finishRule;
 
    /**
     * Fire an entity with the given launch speed, angle and gravity and 
     * rotation speed.        
     */
     private MoveAnimation(Builder builder)
     {                                
         // Set a reference to the entity.
         this.entity     = builder.entity;             
         this.finishRule = builder.finishRule;
         
         // If the duration is 0, warn and end now.
         if (builder.duration == 0)        
             throw new RuntimeException("Animation had a zero duration!");
         
         // Record other values.
         this.theta   = builder.theta;
         this.gravity = builder.gravity;        
         this.omega   = builder.omega;                
         
         // Convert to ticks.
         this.wait = builder.wait;         
         this.duration = builder.duration;
                 
         this.minX = builder.minX;
         this.minY = builder.minY;
         this.maxX = builder.maxX;
         this.maxY = builder.maxY;        
         
         // Determine the components of the launch velocity.
         this.speedX = (int) ((double) builder.speed * Math.cos(Math.toRadians(theta)));
         this.speedY = (int) ((double) builder.speed * Math.sin(Math.toRadians(theta)));          
     }    
     
     public static class Builder implements IBuilder<MoveAnimation>
     {      
         private final IEntity entity;
         
         private int wait = 0;
         private int duration = -1;
         private int theta = 0;
         private int gravity = 0;   
         private int speed = 0;
         private double omega = 0;
         private int minX = Integer.MIN_VALUE;
         private int minY = Integer.MIN_VALUE;
         private int maxX = Integer.MAX_VALUE;
         private int maxY = Integer.MAX_VALUE;
         private FinishRule finishRule = FinishRule.FIRST;
         
         public Builder(IEntity entity)
         {            
             this.entity = entity;
         }
         
         public Builder wait(int val) { wait = val; return this; }
         public Builder duration(int val) { duration = val; return this; }
         public Builder theta(int val) { theta = val; return this; }
         public Builder speed(int val) { speed = val; return this; }         
         public Builder gravity(int val) { gravity = val; return this; }     
         public Builder omega(double val) { omega = val; return this; }
         public Builder minX(int val) { minX = val; return this; }
         public Builder minY(int val) { minY = val; return this; }
         public Builder maxX(int val) { maxX = val; return this; }
         public Builder maxY(int val) { maxY = val; return this; }
         public Builder finishRule(FinishRule val) { finishRule = val; return this; }
                 
         public MoveAnimation build()
         {
             return new MoveAnimation(this);
         }                
     }
 
     public void nextFrame()
     {                   
         // Make sure we've set the started flag.
         if (!this.started)
         {
             // Record the initial position.
             this.thetaOffset = this.entity.getRotation();
             setStarted();
         }
         
         // Check if we're done, if we are, return.
         if (this.finished)
         {
             //LogManager.recordMessage("Move finished!");
             return;
         }
               
         // Increment counter.  This serves as the time variable.
         ticks++;
         
         // Convert to ms.
         int ms = ticks * Settings.getMillisecondsPerTick();
         
         // Skip if necessary.
         if (skip == true)
         {
             if (ms > wait + duration)
                 setFinished();
             
             return;
         }
         
         if (!waitFinished && ms > wait)
         {
             // Record initial position now.
             initialPosition = entity.getPosition();   
             
             // And start!
             waitFinished = true;
             ticks = 1;
             ms = ticks * Settings.getMillisecondsPerTick();
         }                
        
         if (waitFinished)
         {
             // Determine the current x and y.   
             // d = speed * ticks
             //   = pixels/sec * ticks * ms/tick * sec/ms
             //   = pixels
             int g = (gravity * NumUtil.sqInt(ms)) / (2 * NumUtil.sqInt(1000));
             int x = ((speedX * ms)) / 1000;
             int y = ((speedY * ms)) / 1000 - g;
 
             // Move the entity.
             int newX = initialPosition.getX() + x;
             int newY = initialPosition.getY() - y;
             
             boolean doneX = false;
             boolean doneY = false;
             
             if (newX <= minX) 
             { 
                 newX = minX; doneX = true;
             }
             else if (newX >= maxX) 
             {
                 newX = maxX; doneX = true;
             }            
             
             if (newY <= minY) 
             {
                 newY = minY; doneY = true;
             }
             else if (newY >= maxY)
             {
                 newY = maxY; doneY = true;
             }           
             
             entity.setX(newX);                        
            entity.setY(newY);              
            entity.setRotation( thetaOffset + (Math.min( ms, duration ) * omega) / 1000 );
                     
             if ((finishRule == FinishRule.FIRST && (doneX || doneY ))
                 || (finishRule == FinishRule.BOTH && (doneX && doneY)))                
             {
                 if (duration < 0) setFinished();
                 else skip = true;
             }
             
             if (duration > 0 && ms > wait + duration)   
                 setFinished();    
             
             // Update the mouse shit.
             //entity.
         }        
     }
     
     @Override
     public void setVisible(final boolean visible)
     {
         super.setVisible(visible);
         
         if (entity != null)
             entity.setVisible(visible);
     }
     
 }
