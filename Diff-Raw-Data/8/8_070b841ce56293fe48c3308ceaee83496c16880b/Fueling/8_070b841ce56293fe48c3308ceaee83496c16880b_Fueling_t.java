 /**
  * class implemented by Daire O'Doherty 06535691 14/4/08
  */
 
 package thrust.entities.about;
 /**
  * @author "Daire O'Doherty 06535691 (daireod@gmail.com)"
  * @version 14 April 2008
  */
 
 public class Fueling implements Fuelable {
   /**
    * @return How much fuel do you contain?
    */
 
   private transient int my_fuel;
   /** integer myMaxi.*/
   private final int my_maxi = 100;
  /** integer for mass of fuel.*/
  private final int my_fuel_mass = 1;
 
   //@ ensures 0 <= \result;
   //@ ensures \result <= maximum_fuel();
   /*@ pure @*/
   public int fuel() {
     return my_fuel;
   }
   /**
    * @return How much fuel can you contain?
    */
   //@ ensures 0 <= \result;
   /*@ pure @*/
   public final int maxiumumFuel() {
     if (my_maxi >= 0) {
       return my_maxi;
     }
     return 0;
   }
 
   /**
    * @param the_fuel_content This many units is your fuel content.
    */
   //@ requires 0 <= the_fuel_content & the_fuel_content <= maximum_fuel();
   //@ ensures fuel() == the_fuel_content;
   public final void setFuelContent(final int the_fuel_content) {
     if (the_fuel_content >= 0 && the_fuel_content <= my_maxi) {
       my_fuel = the_fuel_content;
     }
   }
 
 
 
   /*@ ensures (\old(fuel() + the_fuel_change < 0) ?
     @            (fuel() == 0) :
     @          (\old(maximum_fuel() < (fuel() + the_fuel_change)) ?
     @             (fuel() == maximum_fuel()) :
     @           fuel() == \old(fuel() + the_fuel_change)));
     @*/
   /**
    * @param the_fuel_change Change your fuel content by this many units.
    */
   public final void changeFuelContent(final int the_fuel_change) {
     if (fuel() + the_fuel_change < 0) {
       my_fuel = 0;
     } else if (maxiumumFuel() < (fuel() + the_fuel_change)) {
       my_fuel = maxiumumFuel();
     } else {
       my_fuel = fuel() + the_fuel_change;
     }
   }
 
   //@ invariant (* Fuel content is always non-negative and finite. *);
   //@ invariant 0 <= fuel();
 
 
 /**
  * @return What is the mass of your fuel?
  */
 //@ ensures \result == fuel * 1;
  public /*@ pure @*/ int fuel_mass()
   {
    return my_fuel * my_fuel_mass;
   }
 }
 
