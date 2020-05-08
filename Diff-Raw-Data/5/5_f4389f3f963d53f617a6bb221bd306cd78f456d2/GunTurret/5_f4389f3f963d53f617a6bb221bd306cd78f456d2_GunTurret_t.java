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
 
 import java.awt.Color;
 import java.awt.Shape;
 
 import thrust.entities.EnemyEntity;
 import thrust.entities.StaticEntity;
 import thrust.entities.behaviors.AI;
 
 
 /**
  * An enemy gun turret that shoots bullets at the spaceship.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 18 April 2008
  */
 public class GunTurret extends StaticEntity
     implements EnemyEntity {
   /**
    * @author allison fallon(allison.fallon@ucdconnect.ie)
    */
   int my_health = 1;
   /**
    *
    */
   EnemyEntity my_enemy;
   /**
    * Colour of GunTurret.
    */
   Color my_colour;
   /**
    *
    */
   Shape my_shape;
   /**
    *
    */
   double[] my_position;
   /**
    *
    */
   double my_orientation;
   /**
    *
    */
   double[] my_acceleration;
   /**
    *
    */
   double[] my_velocity;
   /**
    *
    */
   String my_shapename;
   /**
    *
    */
   StaticEntity my_entity;
   /**
    *
    */
   double my_speed;
   /**
    *
    */
   double my_anglerad;
   /**
    *
    */
   double my_mass;
   /**
    *
    */
   private AI my_attack_ai;
   /**
    *
    */
   private AI my_disturb_ai;
   /**
    *
    */
   public double[] acceleration() {
 
     return my_entity.acceleration();
   }
 
   public void acceleration(final double[] the_acceleration) {
     my_entity.acceleration(the_acceleration);
   }
   public void simulate(final double a_time_interval) {
 
     my_entity.simulate(a_time_interval);
 
   }
 
   public double mass() {
 
     return my_mass;
   }
   public void mass(final double the_mass) {
 
     my_entity.mass(the_mass);
 
   }
 
   public double momentum() {
 
     final int my_elements = 2;
     double[] my_s = new double[my_elements];
     my_s = velocity();
 
     return mass() * my_s[0];
   }
 
   public double[] velocity() {
 
     final int my_elements = 2;
     final double[] my_vel = new double[my_elements];
     my_vel[0] = my_speed;
     my_vel[1] = orientation();
     return my_vel;
 
   }
   public void velocity(final double[] the_velocity) {
 
     my_entity.velocity(the_velocity);
 
   }
   public double gravitational_constant() {
     return my_entity.gravitational_constant();
   }
   public double[] position() {
     return my_position;
   }
 
   public void position(final double[] the_position) {
 
     my_entity.position(the_position);
 
   }
   public void orientation(final double the_orientation) {
 
     my_entity.orientation(the_orientation);
 
   }
   public double orientation() {
 
     return my_orientation;
 
   }
 
 
   public void render() {
 
 
   }
   /**
    *
    */
   public class Point {
     /**
      *
      */
     int my_rx = 1;
     /**
      *
      */
     int my_ry = 1;
 
     public Point(final int the_rx, final int the_ry) {
 
       my_rx = the_rx;
       my_ry = the_ry;
 
     }
   }
/**a rectangle.*/
   public class Rectangle {
     /**
      * width of rectangle.
      */
     int my_width = 1;
     /**
      * height of rectangle.
      */
     int my_height = 1;
     /**
      *
      */
     final Point my_org;
 
     public Rectangle() {
 
       my_org = new Point(0, 0);
 
     }
 
     public Rectangle(final Point the_p) {
 
       my_org = the_p;
 
     }
     public Rectangle(final int the_w, final int the_h) {
 
       my_org = new Point(0, 0);
       my_width = the_w;
       my_height = the_h;
 
     }
     public Rectangle(final Point the_p, final int the_w, final int the_h) {
 
       my_org = the_p;
       my_width = the_w;
       my_height = the_h;
 
     }
   }
 
   public Shape shape() {
     /**
      *
      */
     final Rectangle my_a = new Rectangle(10, 50);
 
     my_shape = (Shape)my_a;
 
     return my_shape;
 
   }
 
   public void shape(final Shape the_shape) {
   }
 
   public String shape_name() {
 
     return null;
   }
 
   public byte state() {
 
     return (byte) my_health;
   }
 
   public void state(final byte the_state) {
 
     my_health = the_state;
 
   }
 
   public AI attack() {
 
     return my_attack_ai;
   }
 
   public void attack(final AI the_behavior) {
 
     my_attack_ai = the_behavior;
 
   }
 
 
   public Color color() {
     my_colour.equals(Color.GREEN);
     return my_colour;
   }
 
   public void color(final Color the_color) {
     my_colour = the_color;
 
   }
 
   /**
    * @return The turret's attack AI must shoot a bullet toward the spaceship.
    */
 
 
   /**
    * @return The turret's disturb AI must shoot a bullet in a random direction
    * away from the terrain.
    */
   public AI disturb() {
     assert false; //@ assert false;
     return my_disturb_ai;
   }
 
   /**
    * @param the_behavior The turret's disturb AI must shoot a bullet
    * in a random direction away from the terrain.
    */
   public void disturb(final AI the_behavior) {
 
     assert false; //@ assert false;
     my_disturb_ai = the_behavior;
 
   }
 
   /*@ public invariant (* A gun turret always resides on/adjacent to
     @                     the terrain. *);
     @ public invariant (* A gun turret's color is always green. *);
     @ public invariant color() == java.awt.Color.GREEN;
     @*/
 }
