 package Vehicles;
 
 import Crane.Crane;
 import Crane.StorageCrane;
 import Helpers.*;
 import Network.objPublisher;
 import Parkinglot.Parkinglot;
 import Pathfinding.Pathfinder;
 import Storage.Storage_Area;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AGV extends Vehicle implements IMessageReceiver {
 
     private final float SpeedWithContainer = 20/3.6f;
     private final float SpeedWithoutContainer = 40/3.6f;    
     private List<Message> assignments;   
     
     /**
      * Reference to objPublisher
      */
     final objPublisher objpublisher; 
     
     /**
      * When the agv has a container but no assingments
      * @return True if it needs a deliver assignment
      */
     public boolean NeedDeliverAssignment()
     {
         if(needDeliverAssignment){
             needDeliverAssignment = false;
             return true;
         }
         return false;
     }
     private boolean needDeliverAssignment = false;
     
     public AGV(int id, Parkinglot startPosition, objPublisher objpublisher) throws Exception{
         if (startPosition == null){
             throw new Exception("\nThe input variable can't be null:"+
                     "\nstartPosition: " + startPosition);
         }
         this.position = startPosition.node.getPosition();
         this.destination = startPosition;
         storage = new Storage_Area(1, 1, 1, position);
         this.Id = id;
         assignments = new ArrayList();
         this.objpublisher = objpublisher;
     }
 
     /**
      * Sets the destination to the parkinglot of the given object
      * @param destinationObject The object to go
      * @throws Exception 
      */
     public void setDestination(Object destinationObject) throws Exception{
         if (parked){
             this.destination.unPark(this);
         }
         if (destinationObject.getClass() == Crane.class){
             this.destination = ((Crane)destinationObject).parkinglotAGV;
         }
         else if (destinationObject.getClass() == StorageCrane.class){
             this.destination = ((StorageCrane)destinationObject).parkinglotAGV;
         }
         else if (destinationObject.getClass() == Parkinglot.class){
             this.destination = (Parkinglot)destinationObject;
         }
         else{
             throw new Exception("The input isn't a crane or storageCrane: " + destinationObject);
         }
         // Finds the route the agv has to follow
         this.route = Pathfinding.Pathfinder.findShortest(Pathfinder.findClosestNode(position), destination.node, storage.Count() == 0);
         this.routeIndex = 1;
     }
     
     /**
      * Updates the movement of the agv
      * @param gameTime The elapsed game time
      * @throws Exception 
      */
     @Override
     public void update(float gameTime) throws Exception {
         // When the destination position is reached
         if (position == destination.node.getPosition()){
             if (!parked) {
                 // Park on the parkinglot
                 destination.park(this);
                 parked = true;
             }
         } 
         // Go to the next node on the route if this node is reached
         else if(position == route[routeIndex].getPosition()){
             parked = false;
             routeIndex++;
             if(this.objpublisher!=null)
                 this.objpublisher.syncVehicle(this);
         }
         // Move the agv to the next position on his route
         else{
             parked = false;
             float speed = (storage.Count() == 0) ? SpeedWithoutContainer : SpeedWithContainer;
             Vector3f NextNode = route[routeIndex].getPosition();
             Vector3f diff = new Vector3f(   NextNode.x - this.getPosition().x,
                                             NextNode.y - this.getPosition().y,
                                             NextNode.z - this.getPosition().z);
             diff.normalize();
             diff.x*=gameTime*speed;
             diff.y*=gameTime*speed;
             diff.z*=gameTime*speed;
             
             Vector3f temp = new Vector3f(position);
             temp.AddVector3f(diff);
 
             if (Vector3f.distance(getPosition(), temp) < Vector3f.distance(getPosition(), NextNode)){
                 this.position.AddVector3f(diff);
             }
             else{
                 this.position = NextNode;
             }
         } 
         
         // When the AGV has assignments
         if(!Available()){
             // When the AGV's current assingment is to fetch a container
             if(assignments.get(0).Fetch()){
                 // When the AGV already fetched the container
                 if(storage.Count() > 0){
                     // Remove assingment because the container is fetched
                     assignments.remove(0);    
                     if(!assignments.isEmpty()){
                         this.setDestination(assignments.get(0).DestinationObject());
                     }
                 }
             }
             // When the AGV's current assignment is to deliver a container
             else if(assignments.get(0).Deliver()){
                 // When the AGV already deliverd the container
                 if(!storage.isFilled()){
                     // Remove assingment because the contianer is deliverd
                     assignments.remove(0); 
                     if(!assignments.isEmpty()){
                         this.setDestination(assignments.get(0).DestinationObject());
                     }
                 }
             }
             else{
                 // When the assignment is not is Deliver, Fetch
                 throw new Exception("Wrong assignment AGV Can't Load or Unload");
             }
             if(!assignments.isEmpty())
                 if(destination.node != assignments.get(0).DestinationNode()){
                     this.setDestination(assignments.get(0).DestinationObject());
             }
         }
         // When the agv has no assignments left
         if(Available()){
            // When the AGV still carries a container 
            if(storage.isFilled()){
                needDeliverAssignment = true;
            }
 //            // Default parkinglot 0
 //            int index = 0;
 //            // Default set on first parkinglot
 //            float distance = Vector3f.distance(this.position, Pathfinder.parkinglots[0].node.getPosition());  
 //            
 //            // Calculate what the nearest parkinglot is
 //            float tempDist = Vector3f.distance(this.position, Pathfinder.parkinglots[11].node.getPosition());             
 //            if(distance > tempDist){
 //                distance = tempDist;
 //                index = 11;
 //            }                
 //            tempDist = Vector3f.distance(this.position, Pathfinder.parkinglots[20].node.getPosition());    
 //            if(distance > tempDist){
 //                distance = tempDist;
 //                index = 20;
 //            }                
 //            tempDist = Vector3f.distance(this.position, Pathfinder.parkinglots[41].node.getPosition());    
 //            if(distance > tempDist){
 //                distance = tempDist;
 //                index = 41;
 //            }
 //            // Sends the agv to the nearest parkinglot
 //            this.setDestination(Pathfinder.parkinglots[index]);
         }        
     }
     
     /**
      * Returns weather the agv has assignments
      * @return True if there are no assignments left 
      */
     @Override
     public boolean Available(){
         return assignments.isEmpty();
     }
     
     /**
      * Gets the current assignment message the agv is working on
      * @return The first message
      */
     public Message GetMessage(){
         if(assignments.isEmpty()){
             return null;
         }
         return assignments.get(0);
     }
     
     /**
      * Add's an assignment for the agv
      * @param mess 
      */
     @Override
     public void SendMessage(Message mess)
     {
         assignments.add(mess);
     }
     /**
      * When true clears all assignments.
      * Adds a new message
      * @param mess
      * @param destroyAssignments 
      */
     public void SendMessage(Message mess, boolean destroyAssignments)throws Exception{
         if(destroyAssignments){
             assignments = new ArrayList();
         }
         assignments.add(mess);
         
         if (Crane.class == assignments.get(0).DestinationObject().getClass()){
             this.setDestination(((Crane)assignments.get(0).DestinationObject()).parkinglotAGV);
         }
         else if (StorageCrane.class == assignments.get(0).DestinationObject().getClass()){
             this.setDestination(((StorageCrane)assignments.get(0).DestinationObject()).parkinglotAGV);
         }
         else{
             throw new Exception("Something went wrong with the next assignement: " + assignments.get(0));
         }
     }
 }
 
