 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  *
  * @author "Joe Kiniry (kiniry@acm.org)"
  * @module "COMP 20050, COMP 30050"
  * @creation_date "March 2007"
  * @last_updated_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 package thrust.entities.in_game;
 
 import java.util.logging.Logger;
 
 import thrust.animation.Animatable;
 import thrust.animation.Animation;
 import thrust.entities.EnemyEntity;
 import thrust.entities.NeutralEntity;
 import thrust.entities.StaticEntity;
 import thrust.entities.behaviors.AI;
 
 /**
  * An enemy factory.
  * @author Ciaran Hale (ciaran.hale@ucdconnect.ie)
  * @author Colin Casey (colin.casey@org.com)
  * @version 28 April 2008
  */
 public class Factory extends StaticEntity
   implements EnemyEntity, Animatable {
   /*@ public invariant (* All factories have exactly one sphere and
   @                     one chimney. *);
   @ public invariant (* A bullet causes 1 unit of damage. *);
   @ public invariant (* Each second 1 unit of damage is eliminated. *);
   @ public initially (* A factory initially has zero units of damage. *);
   @ public initially damage() == 0;
   @ public invariant (* A factory can sustain 20 units of damage before
   @                     it is destroyed. *);
   @ public invariant (* A factory with more than 10 units of damage
   @                     has a chimney that does not smoke. *);
   @ public invariant 10 < damage() ==> !chimney().smoking();
   @ public invariant (* A factory with at most 10 units of damage has
   @                     a smoking chimney. *);
   @ public invariant damage() <= 10 ==> chimney().smoking();
   @*/
 
 //@ public invariant (* See constraint on color in FactoryChimney. *);
 //@ public invariant color() == chimney().color();
 
   /** Amount of damage taken by factory. */
   private transient byte my_damage_status;
   /** This factories chimney. */
   private final transient FactoryChimney my_chimney;
   /** This factories sphere. */
   private final transient FactorySphere my_sphere;
 
   /** Factory Constructor. */
   public Factory() {
     color(java.awt.Color.GREEN);
     my_chimney = new FactoryChimney();
     my_sphere = new FactorySphere();
   }
 
   /**
    * set_state method that overrides StaticEntity set_state so that
    * it calls set_state for the factory's chimney and sphere.
    */
   public void set_state(final double[] the_position,
                         final double the_orientation) {
     position(the_position);
     orientation(the_orientation);
     mass(0);
     final double[] temp = {0, 0};
     velocity(temp);
     acceleration(temp);
     my_chimney.set_state(the_position, the_orientation);
     my_sphere.set_state(the_position, the_orientation);
   }
 
   /**
    * @return How much damage have you sustained?
    */
   //@ ensures 0 <= \result & \result <= 20;
   public /*@ pure @*/ byte damage() {
     return my_damage_status;
   }
 
   /**
    * @return What is your chimney?
    */
   public /*@ pure @*/ FactoryChimney chimney() {
     return my_chimney;
   }
 
   /**
    * @return What is your sphere?
    */
   public /*@ pure @*/ FactorySphere sphere() {
     return my_sphere;
   }
 
   /**
    * @param the_damage_staus You have taken this many units of damage.
    */
   //@ requires 0 <= the_damage;
   //@ ensures damage() == \old(damage() + the_damage);
   public void damage(final byte the_damage) {
     my_damage_status += the_damage;
     if (my_damage_status <= 10) {
       my_chimney.smoking(true);
     } else {
       my_chimney.smoking(false);
     }
   }
 
   public AI attack() {
     return my_chimney.attack();
   }
 
   public AI disturb() {
     return my_chimney.disturb();
   }
 
   public void attack(final AI the_behavior) {
     my_chimney.attack(the_behavior);
   }
 
   public void disturb(final AI the_behavior) {
     my_chimney.disturb(the_behavior);
   }
 
   public Animation animation() {
     return my_chimney.animation();
   }
 
   public void animation(final Animation the_animation) {
     my_chimney.animation(the_animation);
   }
 
   public void animate() {
     //Only smoke if damage is less than or equal to 10
     if (damage() <= 10) {
       my_chimney.animate();
     }
   }
 
   /**
    * A chimney of a factory.
    * @author Ciaran Hale (ciaran.hale@ucdconnect.ie)
    * @author Colin Casey (colin.casey@org.com)
    * @version 28 April 2008
    */
   public class FactoryChimney extends StaticEntity
     implements EnemyEntity, Animatable {
 
     /*@ public invariant (* A factories chimney is the same color as
     @                     its factory. *);
     @ public invariant (* The goal sphere is destroyed by a
     @                     factory's chimney. *);
     @ public invariant (* The spaceship is destroyed by a factory's
     @                     chimney. *);
     @*/
 
     /** Whether the FactoryChimney is smoking or not. */
    private boolean my_smoking_state = true;
     /** The attack AI of a Factory. */
     private transient AI my_attack_ai;
     /** The disturb AI of a Factory. */
     private transient AI my_disturb_ai;
     /** Logger for animation. */
    private final Logger my_logger =
       Logger.getLogger(FactoryChimney.class.getName());
     /** The frames in the Star animation. */
     private transient Animation my_animation;
     /** Animation frame counter. */
     private transient int my_animation_counter;
 
     /** FactoryChimney Constructor. */
     public FactoryChimney() {
       color(java.awt.Color.GREEN);
     }
 
     /**
      * @return Are you smoking?
      */
     public /*@ pure @*/ boolean smoking() {
       return my_smoking_state;
     }
 
     /**
      * Your smoking state is dictated by this flag.
      * @param the_smoking_state A flag indicating whether the chimney
      * is smoking or not.
      */
     //@ ensures smoking() <==> the_smoking_state;
     public void smoking(final boolean the_smoking_state) {
       my_smoking_state = the_smoking_state;
     }
 
     public AI attack() {
       return my_attack_ai;
     }
 
     public AI disturb() {
       return my_disturb_ai;
     }
 
     public void attack(final AI the_behavior) {
       my_attack_ai = the_behavior;
     }
 
     public void disturb(final AI the_behavior) {
       my_disturb_ai = the_behavior;
     }
 
     public Animation animation() {
       return my_animation;
     }
 
     public void animation(final Animation the_animation) {
       my_animation = the_animation;
     }
 
     public void animate() {
       /* When animate is called a frame of animation is played
        * Resets after FactoryChimney animation completes
        */
       my_animation_counter++;
       my_logger.fine("Factory Chimney animation step " +
                        my_animation_counter + " has been rendered.");
       if (my_animation_counter % 10 == 0) {
         my_animation_counter = 0;
       }
     }
   }
 
   /**
    * A sphere of a factory.
    * @author Ciaran Hale (ciaran.hale@ucdconnect.ie)
    * @author Colin Casey (colin.casey@org.com)
    * @version 28 April 2008
    */
   public class FactorySphere extends StaticEntity
     implements NeutralEntity {
     /*@ public invariant (* A factory sphere's color is always green. *);
       @ public invariant color() == java.awt.Color.GREEN;
       @ public invariant (* The goal sphere is not destroyed by a
       @                     factory's sphere. *);
       @*/
 
     /** FactorySphere Constructor. */
     public FactorySphere() {
       color(java.awt.Color.GREEN);
     }
   }
 }
