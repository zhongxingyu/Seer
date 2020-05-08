 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  *
  * @author "Patrick Nevin: 06754155"
  * @creation_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 package thrust.entities.about;
 /**
  * @author Patrick Nevin (patrick.nevin@ucdconnect.ie)
  * @version 20 April 2008
  */
 public class FuelClass implements Fuelable {
 //@ invariant (* Fuel content is always non-negative and finite. *);
   //@ invariant 0 <= fuel();
   /**
    * the entities fuel and maximum fuel.
    */
   private int my_fuel, my_maxFuel;
   /**
    * @return How much fuel do you contain?
    */
   //@ ensures 0 <= \result;
   //@ ensures \result <= maximum_fuel();
   public /*@ pure @*/ int fuel() {
     return this.my_fuel;
   }
   /**
    * @return How much fuel can you contain?
    */
   //@ ensures 0 <= \result;
   public /*@ pure @*/ int maximum_fuel() {
     return this.my_maxFuel;
   }
   /**
    * @param the_fuel_content This many units is your fuel content.
    */
   //@ requires 0 <= the_fuel_content & the_fuel_content <= maximum_fuel();
   //@ ensures fuel() == the_fuel_content;
   public void set_fuel_content(final int the_fuel_content) {
     this.my_fuel = the_fuel_content;
   }
   /**
    * @param the_fuel_change Change your fuel content by this many units.
    */
   /*@ ensures (\old(fuel() + the_fuel_change < 0) ?
     @            (fuel() == 0) :
     @          (\old(maximum_fuel() < (fuel() + the_fuel_change)) ?
     @             (fuel() == maximum_fuel()) :
     @           fuel() == \old(fuel() + the_fuel_change)));
     @*/
   public void change_fuel_content(final int the_fuel_change) {
     this.my_fuel += the_fuel_change;
   }
//@ public invariant (* One unit of fuel weights 1kg. *);
  /**
   * @return What is the mass of your fuel?
   */
  //@ ensures \result == fuel() * 1;
  public /*@ pure @*/ int fuel_mass() {
    return this.my_fuel * 1;
  }
 }
