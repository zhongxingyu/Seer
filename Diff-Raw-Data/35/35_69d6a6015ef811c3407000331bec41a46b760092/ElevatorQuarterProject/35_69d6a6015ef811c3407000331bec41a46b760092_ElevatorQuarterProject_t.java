 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package elevator.quarter.project;
 
import java.util.*;

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
        
        //initialize the elevators to floor 1
         RegElevatorController.getInstance().initializeElevators();
         
         RegElevatorController.getInstance().request(1, 2);
         
        /*
        //multithreading code
        ArrayList<Thread> threads = new ArrayList<Thread>();
        
         for(int i = 0; i < RegElevatorController.getInstance().getElevatorCount(); i++)
         {
            //threads.add(new Thread(RegElevatorController.getInstance().))
         }
        */
     }
 }
