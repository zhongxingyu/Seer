 /**
  * The Report that is passed through the system which holds a specific passenger
  * and a boolean representing whether or not the scan check has passed or failed.
  * 
  * @author Anthony Barone
  * @author Ryan Clough
  * @author Rebecca Dudley
  * @author Ryan Mentley
  */
 public class Report {
 
     // Instance variables
     private final Passenger passenger;
     private final boolean passing;
 
     /**
      * Constructor for a message
      * 
      * @param passenger The passenger this report concerns
      * @param passing The result of the particular scan this Report was passed from
      */
     public Report( Passenger passenger, boolean passing ) {
         this.passenger = passenger;
         this.passing = passing;
     }
 
     /**
      * Return the passenger associated with this report
      * 
      * @return passenger
      */
     public Passenger getPassenger() {
         return passenger;
     }
 
     /**
      * Tells whether this is a passing report
      * 
      * @return true if passing, false if failing
      */
     public boolean isPassing() {
         return passing;
     }
 }
