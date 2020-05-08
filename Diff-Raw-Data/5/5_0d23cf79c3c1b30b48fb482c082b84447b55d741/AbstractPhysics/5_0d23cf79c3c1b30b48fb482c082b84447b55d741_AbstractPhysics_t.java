 package thrust.physics;
 
 /**
  * Implementation of Physics methods.
  * @author Colin Casey (colin.casey@org.com)
  * @version 7 April 2008
  */
public abstract class AbstractPhysics implements PhysicsInterface {
   /** The force that attracts the spaceship and
    * goal sphere toward the terrain. */
   static final double GRAVITY_CONSTANT = -9.8;
   /** The quantity of matter that an entity contains. */
   double my_mass;
   /** The rate of motion. */
   double my_speed;
   /** Change in the speed of something. */
   double my_rate_of_speed;
   /** The relative physical direction of entities. */
   double my_orientation;
   /** The x co-ordinate where an entity is located. */
   double my_position_x;
   /** The y co-ordinate where an entity is located. */
   double my_position_y;
 
   /**
    * @return What is your acceleration in meters per second squared?
    */
   public double[] acceleration() {
     final double[] acceleration = {my_speed, my_rate_of_speed};
     return acceleration;
   }
 
   /**
    * @return What is the gravitational constant?
    */
   public double gravitational_constant() {
    return GRAVITY_CONSTANT;
   }
 
   /**
    * @return What is your mass in kilogrammes?
    */
   public double mass() {
     return my_mass;
   }
 
   /**
    * @return What is your momentum in kilogrammes*meters per second?
    */
   public double momentum() {
     return my_mass * my_speed;
   }
 
   /**
    * @return What is your orientation in radians?
    */
   public double orientation() {
     return my_orientation;
   }
 
   /**
    * @return What is your position in meters from the origin?
    */
   public double[] position() {
     final double[] position = {my_position_x, my_position_y};
     return position;
   }
 
   /**
    * @return What is your velocity in meters per second?
    */
   public double[] velocity() {
     final double[] velocity = {my_speed, my_orientation};
     return velocity;
   }
 }
