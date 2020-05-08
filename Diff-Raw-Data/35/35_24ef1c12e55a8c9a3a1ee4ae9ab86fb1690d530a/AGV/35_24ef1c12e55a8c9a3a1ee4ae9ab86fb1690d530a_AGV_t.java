 package Vehicles;
 
 import Crane.Crane;
 import Crane.StorageCrane;
 import Helpers.*;
 import Main.Container;
 import Pathfinding.Node;
 import Pathfinding.Pathfinder;
 import Storage.Storage_Area;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AGV extends Vehicle implements IMessageReceiver {
 
    private final float SpeedWithContainer = 20/3.6f;
    private final float SpeedWithoutContainer = 40/3.6f;    
     private List<Message> assignments;    
     
     public boolean NeedDeliverAssignment()
     {
         if(needDeliverAssignment){
             needDeliverAssignment = false;
             return true;
         }
         return false;
     }
     private boolean needDeliverAssignment = false;
     
     public AGV(Node startPosition) throws Exception{
         if (startPosition == null){
             throw new Exception("\nThe input variable can't be null:"+
                     "\nstartPosition: " + startPosition);
         }
         else{
             this.position = startPosition.getPosition();
             this.destination = startPosition;
             storage = new Storage_Area(1, 1, 1, position);
         }
         assignments = new ArrayList();
     }
 
     @Override
     public void update(float gameTime) throws Exception {
         if (position == destination.getPosition()){
             if(!Available()){
                 if(assignments.get(0).DestinationObject().getClass() == Crane.class){
                     Crane crane = (Crane)(assignments.get(0).DestinationObject());
                     crane.parkinglotAGV.park(this);
                 }
                 else if (assignments.get(0).DestinationObject().getClass() == StorageCrane.class){
                     StorageCrane crane = (StorageCrane)(assignments.get(0).DestinationObject());
                     crane.parkinglotAGV.park(this);
                 }
             }
        } 
        else if(position == route[routeIndex].getPosition()){
            routeIndex++;
         }
         else{
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
             // When the AGV needs to fetch a container
             if(assignments.get(0).Fetch()){
                 // When the AGV has a container on him
                 if(storage.Count() > 0){
                     //Container.TransportType transportType = storage.peakContainer(0, 0).getDepartureTransportType();
                     switch(storage.peekContainer(0,0).getDepartureTransportType()){
                         case trein:
                             //destination = trein parking node
                             break;
                         case zeeschip:
                             //destination = zeeschip parking node
                             break;
                         case binnenschip:
                             //destination = binnenship parking node
                             break;
                         case vrachtauto :
                             //destination = vrachtauto parking node
                             break;
                     }
                     // Remove assingment because the container is fetched
                     assignments.remove(0);    
                     if(!assignments.isEmpty()){
                         destination = assignments.get(0).DestinationNode();
                     }
                 }
             }
             // When the AGV need's to deliver a container
             else if(assignments.get(0).Deliver())
             {
                 // When the AGV doesn't has a contianer on him
                 if(!storage.isFilled())
                 {
                     // Remove assingment because the contianer is deliverd
                     assignments.remove(0); 
                     if(!assignments.isEmpty()){
                         destination = assignments.get(0).DestinationNode();
                     }
                 }
             }
             else
             {
                 // When the assignment is not is Deliver, Fetch
                 throw new Exception("Wrong assignment AGV Can't Load or Unload");
             }
             // When there are no assignments left
             if(Available())
             {
                 // When the AGV has no assignments but still has a container 
                 if(storage.isFilled()){
                     needDeliverAssignment = true;
                 }
                 // Default parkinglot 0
                 int index = 0;
                 // Default set on first parkinglot
                 float distance = Vector3f.distance(this.position, Pathfinder.parkinglots[0].node.getPosition());  
                 
                 float tempDist = Vector3f.distance(this.position, Pathfinder.parkinglots[11].node.getPosition());             
                 if(distance > tempDist){
                     distance = tempDist;
                     index = 11;
                 }                
                 tempDist = Vector3f.distance(this.position, Pathfinder.parkinglots[20].node.getPosition());    
                 if(distance > tempDist){
                     distance = tempDist;
                     index = 20;
                 }                
                 tempDist = Vector3f.distance(this.position, Pathfinder.parkinglots[41].node.getPosition());    
                 if(distance > tempDist){
                     distance = tempDist;
                     index = 41;
                 }
             }
         }
         
     }
     
     /**
      * When there are no assignments for the AGV
      * @return 
      */
     @Override
     public boolean Available(){
         return assignments.isEmpty();
     }
     
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
         destination = mess.DestinationNode();
     }
 }
 
