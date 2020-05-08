 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package elevator.quarter.project;
 
 import java.util.ArrayList;
 
 /**
  * 
  * @author Craig
  */
 public class RegElevator implements Elevator, Lift, Runnable
 {
     //current floor & queue information
     private Floor currentFloor;
     private ArrayList<Floor> downDestinations;
     private ArrayList<Floor> upDestinations;
     private static int elevatorIDCounter = 1;
     private int elevatorID;
     
     //people currently on the elevator
     private ArrayList<Movable> movablesOnElevator;
     
     //other elevator components
     
     private FloorPanel elevatorFloorPanel;
     
     //state variables
     private Door doorState;
     private ElevatorState elevatorState;
     
     //time values
     private final int timeBetweenFloors = 1000;
     private final int doorOpenCloseTime = 250;
     private final int doorStaysOpenTime = 1000;
     
     public RegElevator()
     {
         doorState = Door.CLOSED;
         elevatorState = ElevatorState.IDLE;
         movablesOnElevator = new ArrayList<Movable>();
         upDestinations = new ArrayList<Floor>();
         downDestinations = new ArrayList<Floor>();
         elevatorID = elevatorIDCounter;
         elevatorIDCounter++;
         
         //start all elevators at the first floor
         //ALERT: this can't be in the constructor because it causes an infinite loop of calling the getInstance on a building that is trying to create itself but isn't quite done yet.
         //currentFloor = RegBuilding.getInstance().getFloorWithIndex(0);
     }
     
     //accessors
     /**
      * accessor for elevatorID
      * @return 
      */
     @Override
     public int getElevatorID()
     {
         return elevatorID;
     }
     
     /**
      * accessor for currentFloor
      * @return 
      */
     @Override
     public Floor getCurrentFloor()
     {
         return currentFloor;
     }
     
     /**
      * Part of workaround that says the elevator's initial floor can be set only once with this method.
      * @param floorIn 
      */
     @Override
     public void initiallySetCurrentFloor()
     {
         if(currentFloor == null)
         {
             currentFloor = RegBuilding.getInstance().getFloorWithIndex(0);
         }
     }
     
     /**
      * This method adds a new floor request to either the upward-bound or downward-bound destination list.
     * @param floorIn
      */
     @Override
     public void addFloorToDestList(Floor floorIn)
     {
         //if the elevator is on the same floor as the floor in request, open the doors and remain idle. otherwise, add it to the proper up/down destination list.
         if(currentFloor.getFloorID() == floorIn.getFloorID())
         {
             System.out.println("The requested elevator is already on the requested floor. Doors opening.");
             doorOpen();
         }
         else if(currentFloor.getFloorID() < floorIn.getFloorID())
         {
             upDestinations.add(floorIn);
             System.out.println("Floor " + floorIn.getFloorID() + " added to the upDestinations list of Elevator " + elevatorID);
         }
         else if(currentFloor.getFloorID() > floorIn.getFloorID())
         {
             downDestinations.add(floorIn);
             System.out.println("Floor " + floorIn.getFloorID() + " added to the downDestinations list of Elevator" + elevatorID);
         }
     }
     
     /**
      * This method opens the elevator doors and prints a message.
      */
     @Override
     public void doorOpen()
     {
         if(doorState != Door.OPEN)
         {
             doorState = Door.OPEN;
             System.out.println("Doors open.");
         }
     }
 
     /**
      * This method closes the elevator doors and prints a message.
      */
     @Override
     public void doorClose()
     {
         if(doorState != Door.CLOSED)
         {
             doorState = Door.CLOSED;
             System.out.println("Doors closed.");
         }
     }
     
     ///////////////////////////////////////////////////////////////////////////
     //everything below this line is not part of submission 2
     
     
     /**
      * Checks that an entry request for the elevator is valid. First checks that the elevator is not at capacity, then checks that the Movable that made the request is currently on the same floor as the elevator.
      * @param movableIn
      * @throws OverCapacityException 
      */
     @Override
     public void entryRequest(Movable movableIn) throws OverCapacityException
     {
         /*
         if(movablesOnElevator.size() < ELEVATOR_CAPACITY)
         {
             if(((RegPerson)movableIn).getCurrentFloor().getFloorID() == this.currentFloor)
             {
                 
             }
             else
             {
                 //throw new MovableNotAvailableException("The requested movable is not on the same floor as the elevator.");
             }
         }
         else
         {
             throw new OverCapacityException("This elevator cannot accept any more movables.");
         }
         * 
         *         if(doorState == Door.OPEN)
         {
             
         }
         else
         {
             throw new ElevatorNotReadyException("This elevator's doors are closed and cannot accept movables.");
         }
         * */
         
     }
 
     @Override
     public void removalRequest(Movable movableIn)
     {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     /**
      * After confirming that the entry request is valid, this method will delete the movable that requested entry from its current floor and add it to the elevator. This method can potentially be delegated using the strategy pattern.
      * @param movableIn 
      */
     private void addMovable(Movable movableIn)
     {
         //error involves converting between int/ floor types. ALSO: why do i have to cast everything???
         
         //movableIn.getCurrentFloor().getMovablesOnFloor().delete(movableIn);
         movablesOnElevator.add(movableIn);
     }
     
     /**
      * After confirming that the exit request is valid, this method will delete the movable that requested to exit from the elevator and add it to the elevator's current floor. This method can potentially be delegated using the strategy pattern.
      * @param movableIn 
      */
     private void removeMovable(Movable movableIn)
     {
         movablesOnElevator.remove(movableIn);
         //currentFloor.getMovablesOnFloor().add(movableIn);
     }
 
     @Override
     public void run()
     {
         System.out.println("Elevator " + elevatorID + " running");
     }
 
 }
