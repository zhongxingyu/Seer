 package elevator.quarter.project;
 
 import java.util.ArrayList;
 import java.util.Queue;
 
 /**
  * Represents 1 floor in a building.
  * @author ZGraceffa
  */
 
 /*
  * idea: have groundFloor, RegFloor, and TopFloors!
  * Difference is that top and bottom floors wont have upwaitlist/downwaitlist
  */
 public class RegFloor implements Floor{
     //FIFO list of Movables waiting to go upwards
     private Queue<Movable> upWaitList;
     //FIFO list movables waiting to go downwards
     private Queue<Movable> downWaitList;
     //Array to keep track of everyone on the floor
     private ArrayList<Movable> movablesOnFloor;
     //Callbox object for floor
     private CallBox aRegCallBox;
     
    private static int floorIDCounter = 1;
    private int floorID;
     
     public RegFloor()
     {
         //upWaitList = new Queue<>(); //abstract queue no allowed, arraydequeue or arrayqueue?
         //downWaitList = new Queue<>();
         //movablesOnFloor = new ArrayList<Movable>();
         //aRegCallBox = new RegCallBox();
         
         floorID = floorIDCounter;
         floorIDCounter++;
     }
     
     /**
      * Checks to make sure that Movable is on floor and not already on list to 
      * go down, then adds movable to list of movables in line to go up.
      * @param up alias for person requesting to go up.
     */
     public void addUpMovable(Movable up){
         //need to add error checking
         /* If we do not implement special floors, it will also check that the mloor is not the top
          * Is movable on down waitlist? is movable on floor?(?)
          */
         upWaitList.add(up);
         if(aRegCallBox.seeUpButtonState())
         {
             aRegCallBox.upButton();
         }
     }
     
     /**
      * Checks to make sure that Movable is on floor and not already in list to 
      * go up, then adds movable to list of movables in line to go down.
      * @param down reference to movable requesting to go down.
      */
     public void addDownMovable(Movable down){
         //need to add error checking
         /*error check (assumes use of dto) for arg1 instead of 'up'
          * is movable on elevator? 
          * is elevator on floor?
          * are elevator doors open?
          * is this movables destination?
         */
         downWaitList.add(down);
         if(aRegCallBox.seeDownButtonState())
         {
             aRegCallBox.downButton();
         }
     }
     
     /**
      * This method is called by a up-bound elevator so Movables can board, checks if the
      * queue is empty and then adds on one movable; if queue is empty throws exception.
      */
     public Movable remUpMovable(){
         if(!upWaitList.isEmpty())
         {
             Movable forUpElevator = upWaitList.poll();
             return forUpElevator;
         }
         else
         {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
     
     /**
      * This method is called by a down-bound elevator so Movables can board, checks if the
      * queue is empty and then adds on one movable; if queue is empty throws exception.
      */
     public Movable remDownMovable(){
         if(!downWaitList.isEmpty())
         {
             Movable forDownElevator = downWaitList.poll();
             return forDownElevator;
         }
         else 
         {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
     
     //accessors
     @Override
     public int getFloorID()
     {
         return floorID;
     }
     
 }
