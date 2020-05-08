 package com.se350.app;
 
 /**
  * This class is going to instantiate the ElevatorController
  * @author Valentin Litvak, Michael Williamson
  */
 public class App 
 {
     public static void main( String[] args )
     {
         Building building = new Building( 4, 3 );
 
        /* send request for 4th floor to elevator #3 */
        building.addFloorRequest( 4, 3 );
     }
 }
