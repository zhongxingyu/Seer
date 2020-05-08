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
 
 import thrust.entities.DynamicEntity;
 import thrust.entities.FriendEntity;
 import thrust.entities.about.Fuelable;
 import thrust.entities.behaviors.Tow;
 import thrust.physics.*;
 
 /**
  * The player's main vehicle.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 18 April 2008
  */
 public class Spaceship extends DynamicEntity
   implements FriendEntity, Fuelable, Tow {
   /*@ public invariant (* A spaceship's mass when empty of all fuel is
     @                     10000kg. *);
     @ public invariant EMPTY_MASS <= mass();
     @*/
   /** A spaceship's mass when empty of all fuel is 10000kg. */
   public static final int EMPTY_MASS = 10000;
 
   /*@ public initially (* The spaceship's initial fuel is 1000 units. *);
     @ public initially fuel() == INITIAL_FUEL;
     @*/
   /** The spaceship's initial fuel is 1000 units. */
   public static final int INITIAL_FUEL = 1000;
   
   public int my_fuel;
   public PhysicsClass my_physics = new PhysicsClass();
   
   
   
   
   //@ public initially_redundantly mass() == EMPTY_MASS + INITIAL_FUEL;
 
   /*@ public invariant (* The spaceship is destroyed by the barrier. *);
     @ public invariant (* The spaceship is destroyed by a bullet. *);
     @ public invariant (* The spaceship is destroyed by the factory. *);
     @ public invariant (* The spaceship is destroyed by the fuel pod. *);
     @ public invariant (* If the spaceship is towing the goal sphere,
     @                     and the spaceship is destroyed, the goal
     @                     sphere is also destroyed. *);
     @ public invariant (* The spaceship is destroyed by the gun turret. *);
     @ public invariant (* The spaceship is not affected by space. *);
     @ public invariant (* The spaceship is not affected by a star. *);
     @ public invariant (* The spaceship is destroyed by the terrain. *);
     @ public invariant (* A spaceship's mass is the sum of its empty mass,
     @                     plus the mass of its fuel, plus the mass of
     @                     the goal sphere, if it is being towed. *);
     @ public invariant mass() == EMPTY_MASS + fuel().mass() +
     @                  (towed() ? GoalSphere.MASS : 0);
     @ public invariant (* The spaceship's shape is always that of a ship. *);
     @ public invariant (* The spaceship's color is always white. *);
     @ public invariant color() == thrust.entities.properites.GameColor.WHITE;
     @*/
   
   /**
    * @return Are you currently towing or being towed?
    */
   public /*@ pure @*/ boolean towed(){
     //how to check it's true?
     return false;
   }
 
   /**
    * You are now towing or being towed.
    */
   //@ ensures towed();
   public void tow(){
     
   }
   
   
   public void change_fuel_content(int the_fuel_change) {
     int fuel = fuel() + the_fuel_change;
     my_fuel = fuel;
    }
 
    /**
     * @return How much fuel do you contain?
     */
    public int fuel() {
      return my_fuel;
    }
 
    /**
     * @return What is the mass of your fuel?
     */
    public int fuel_mass() {
      return fuel() * 1;
    }
 
    /**
     * @return How much fuel can you contain?
     */
    public int maximum_fuel() {
      return INITIAL_FUEL;
    }
 
    
    /**
     * @param the_fuel_content This many units is your fuel content.
     */
    //@ requires 0 <= the_fuel_content & the_fuel_content <= maximum_fuel();
    //@ ensures fuel() == the_fuel_content;
    public void set_fuel_content(int the_fuel_content) {
     my_fuel = the_fuel_content;
    }
 
    /**
     * @return What is your acceleration in meters per second squared?
     */
    //@ ensures \result.length == 2;
    public /*@ pure @*/ double[] acceleration(){
     return my_physics.acceleration();
    }
 
    /**
     * @return What is the gravitational constant?
     */
    public /*@ pure @*/ double gravitational_constant(){
      return my_physics.gravitational_constant();
    }
 
    /**
     * @return What is your mass in kilograms?
     */
    //@ ensures 0 <= \result;
   public /*@ pure @*/ double mass(){
     my_physics.mass(EMPTY_MASS + INITIAL_FUEL);
      return my_physics.mass();
    }
 
    /**
     * @return What is your momentum in kilograms*meters per second?
     */
    public /*@ pure @*/ double momentum(){
      return my_physics.momentum();
    }
 
    /**
     * @return What is your orientation in radians?
     */
    public /*@ pure @*/ double orientation(){
      return my_physics.orientation();
    }
 
    /**
     * @return What is your position in meters from the origin?
     */
    //@ ensures \result.length == 2;
   public  /*@ pure @*/ double[] position(){
     return my_physics.position();
    }
 
    /**
     * @return What is your velocity in meters per second?
     */
   public  /*@ pure @*/ double[] velocity(){
     return my_physics.velocity();
    }
 
    /**
     * Simulate yourself for this many seconds.
     * @param some_seconds the number of seconds to simulate.
     */
    public void simulate(double some_seconds){
     my_physics.simulate();
  }
 
 }
