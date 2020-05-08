 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package elevator.quarter.project;
 
 /**
  *
  * @author Craig
  */
 public class ElevatorQuarterProject {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args)
     {
         //initialize the building
         RegBuilding.getInstance();
         RegElevatorController.getInstance().initializeElevators();
         
         RegElevatorController.getInstance().request(1, 2);
         
         for(int i = 0; i < RegElevatorController.getInstance().getElevatorCount(); i++)
         {
            
         }
     }
 }
