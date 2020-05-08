 
 import org.segonds.elevators.model.*;
 
 // This class is incomplete. You need to complete its implementation according
 // to the Baseline algorithm described in "Simulator Simulator.pptx".
 public class BaselineController implements Controller {
 
     /* Declaration of instance variables */
     // The building object
     ControlledBuilding building;
     // An array of all elevator objects
     ControlledElevator E[];
     boolean startRun = false;
     // Number of floors
     int nbFloors;
     // Floor number of the top floor
     int topFloor;
     // Set up shorter aliases
     final Direction UP = Direction.UP;
     final Direction DOWN = Direction.DOWN;
     final Direction UNCOMMITTED = Direction.UNCOMMITTED;
 
     public void setup(ControlledBuilding b) {
         // Save a reference to the building object for later use
         building = b;
 
         // Obtain a reference to the array of elevators
         E = building.getElevators();
 
         // Save these constants for easier access later
         nbFloors = building.getNbFloors();
         topFloor = nbFloors - 1;
     }
 
     // Nothing to reset
     public void reset() {
     }
 
     public String getName() {
         return "Baseline Controller";
     }
 
     public void tick(Clock clock) {
 
         for (int i = 0; i < E.length; i++) {
 //            if(startRun == false) {
 //                if(E[i].getFloor() != 9)
 //                    E[i].setTarget(9);
 //                else {
 //                    startRun = true;
 //                    E[i].setDirection(DOWN);
 //                } 
 //            }
 //            else 
 //            {   //delete
             if (E[i].getDirection() == UP && E[i].getSpeed() > 0) {
                 //System.out.println("Case1; Lift: "+i);
                 handleCase1(i);
             } else if (E[i].getDirection() == UP && E[i].getSpeed() == 0) {
                 System.out.println("1this is "+i+" now "+E[i].getFloor()+" target "+E[i].getTarget());
                 //System.out.println("Case2; Lift: "+i);
                 handleCase2(i);
                 System.out.println("2this is "+i+" now "+E[i].getFloor()+" target "+E[i].getTarget());
             } else if (E[i].getDirection() == DOWN && E[i].getSpeed() < 0) {
                 //System.out.println("Case3; Lift: "+i);
                 handleCase3(i);
                 System.out.println("3this is "+i+" now "+E[i].getFloor()+" target "+E[i].getTarget());
             } else if (E[i].getDirection() == DOWN && E[i].getSpeed() == 0) {
                 //System.out.println("Case4; Lift: "+i);
                 handleCase4(i);
                 System.out.println("4this is "+i+" now "+E[i].getFloor()+" target "+E[i].getTarget());
             } else if (E[i].getDirection() == UNCOMMITTED) {
                 //System.out.println("Case5; Lift: "+i);
                 handleCase5(i);
                 System.out.println("5this is "+i+" now "+E[i].getFloor()+" target "+E[i].getTarget());
             }
 //            }  // delete
         }
     }
 
     // For these methods, "i" is the index to the elevator objects in E[].
     public void handleCase1(int i) {
         int nextFloor = E[i].nextStoppableFloor();
         int topFloor = building.getNbFloors() - 1;
 
         //set the nearest one
         int target = -1;
         for (int f = nextFloor; f <= topFloor; f = f + 1) {
             if (E[i].isFloorRequested(f) || (building.isFloorRequested(f, UP)&&!isOtherTargetTheSameThingAs(E[i],f,UP))) {
                 target = f;
                 break;
             }
         }
         if (target != -1) {
             E[i].setTarget(target);
         } else {
             handleCase5(i);
         }
     }
 
     public void handleCase2(int i) {
         int currentFloor = E[i].getFloor();
 
         if (currentFloor == topFloor) {
             handleCase5(i);
             return;
         }
 // If the elevator doors are in closing states
         if (!E[i].isClosing())return;
         
         int nextFloor = currentFloor + 1;
 
         int target = -1;
         for (int f = nextFloor; f <= topFloor; f = f + 1) {
             if(E[i].isFloorRequested(f) || (building. isFloorRequested(f, UP)&&!isOtherTargetTheSameThingAs(E[i],f,UP))){
                 if (target == -1|| target > f) {
                     target = f;
                 }
             }
         }
         if (target != -1) {
             E[i].setTarget(target);
         } else {
             handleCase5(i);
         }
     }
 
     public void handleCase3(int i) {
         int nextFloor = E[i].nextStoppableFloor();
         int topFloor = building.getNbFloors() - 1;
         
         //set the nearest one
         int target = -1;
        for (int f = E[i].getFloor(); f >= 0; f = f - 1) {
             if (E[i].isFloorRequested(f) || (building.isFloorRequested(f, DOWN)  &&!isOtherTargetTheSameThingAs(E[i],f,DOWN))) {
                 target = f;
                 break;
             }
         }
         if (target != -1) {
             E[i].setTarget(target);
         } else {
             handleCase5(i);
         }
     }
 
     public void handleCase4(int i) {
         int currentFloor = E[i].getFloor();
 
         if (currentFloor == 0) {
             handleCase5(i);
             return;
         }
 // If the elevator doors are in closing states
         if (!E[i].isClosing()) {
             return;
         }
         int nextFloor = currentFloor - 1;
 
         int target = -1;
         for (int f = nextFloor; f >= 0; f = f - 1) {
             if(E[i].isFloorRequested(f) || (building.isFloorRequested(f, DOWN)&&!isOtherTargetTheSameThingAs(E[i],f,DOWN))){
                 if(target==-1||target<f){
                     target=f;
                 }
             }
         }
         if (target != -1) {
             E[i].setTarget(target);
         } else {
             handleCase5(i);
         }
 
 
     }
 
     public void handleCase5(int i) {
         int upTarget = -1;
         int downTarget = -1;
         for (int f = 0; f < building.getNbFloors(); f++) {
            if ((building.isFloorRequested(f, UP)&&!isOtherTargetTheSameThingAs(E[i],f,UP))||E[i].isFloorRequested(f)) {
                 upTarget = f;
                 break;
             }
         }
         
         
         if(E[i].isEmpty()){
             for (int f = building.getNbFloors() - 1; f >= 0; f--) {
                 if (building.isFloorRequested(f, DOWN)&&!isOtherTargetTheSameThingAs(E[i],f,DOWN)) {
                     downTarget = f;
                     break;
                 }
             }
         }else{
             for (int f = E[i].getFloor()-1; f >= 0; f--) {
                 if (E[i].isFloorRequested(f)||(building.isFloorRequested(f, DOWN)&&!isOtherTargetTheSameThingAs(E[i],f,DOWN))) {
                     downTarget = f;
                     break;
                 }
             }
         }
         if (i == 0) {
             if (upTarget != -1) {
                 E[i].setDirection(UP);
                 E[i].setTarget(upTarget);
             } else if (downTarget != -1) {
                 E[i].setDirection(DOWN);
                 E[i].setTarget(downTarget);
             } else {
                 E[i].setDirection(UNCOMMITTED);
             }
         } else {
             if (downTarget != -1) {
                 E[i].setDirection(DOWN);
                 E[i].setTarget(downTarget);
             } else if (upTarget != -1) {
                 E[i].setDirection(UP);
                 E[i].setTarget(upTarget);
             } else {
                 E[i].setDirection(UNCOMMITTED);
             }
         }
     }
     
     public boolean isOtherTargetTheSameThingAs(ControlledElevator elevator, int target, Direction direction){
         for(int i=0;i<E.length;i++){
             if(E[i]==elevator)continue;
             if(E[i].getTarget()==target && E[i].getDirection()==direction){
                 return true;
             }
         }
         return false;
     }
 }
