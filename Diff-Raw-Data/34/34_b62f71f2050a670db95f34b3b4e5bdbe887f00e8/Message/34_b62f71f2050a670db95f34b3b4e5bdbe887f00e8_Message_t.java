 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Helpers;
 
 import Crane.Crane;
 import Crane.StorageCrane;
 import Main.Container;
 import Pathfinding.Node;
 import Vehicles.*;
 
 /**
  * 
  * @author Martin
  */
 public class Message {    
     // The actiontypes for what the requestedObject needs to do   
     public enum ACTION { Load, Unload, Fetch, Deliver }        
     // The action for the requested object
     ACTION action;
     
     // The object that's requested
     Object destinationObject;
     // The object that's requested
     Object requestedObject;
     
     // The container that needs to be fetched or deliverd
     Container container;
     // The position of the continaer
     Vector3f containerPos;
     
     Node destNode;
     
     /**
      * Constructs a new message
      * @param sourceSender The object that requests an object
      * @param requestedType The object that's requested
      * @param action The action for the requested object
      * @param container The container that's on transport
      */
     public Message(
             Object sourceSender, 
             Object requestedType,
             ACTION action,
             Container container) throws Exception
     {        
         if(sourceSender == null){
             throw new Exception("Source Sender can't be null");
         }        
         if(!(sourceSender instanceof Vehicle ||
              sourceSender instanceof Crane)){            
             throw new Exception("Source Sender must be a crane or transportVehicle"); 
         }
         if(sourceSender == requestedType){
             throw new Exception("Can't request the same type of object");
         }        
         
         if(requestedType == null){
             throw new Exception("Requested type can't be null");
         }        
         if(!(requestedType.equals(AGV.class) ||
              requestedType.equals(Crane.class) ||
              requestedType.equals(StorageCrane.class))){
             throw new Exception("Requested type must be a crane or an agv");
         }        
         if(action == null){
             throw new Exception("action can't be null");
         }        
         if(!(container == null ||
              requestedType.equals(AGV.class))){
             throw new Exception("Only agvs can have a container in there message");
         }
         
         this.destinationObject = sourceSender;
         this.requestedObject = requestedType;
         this.action = action;
         this.container = container;
     }
     
     /**
      * Constructs a new message
      * @param sourceSender The object that requests an object
      * @param requestedType The object that's requested
      * @param action The action for the requested object
      * @param container The container that's on transport
      * @param destNode A temp destination node to drive to
      */
     public Message(
             Object sourceSender, 
             Object requestedType,
             ACTION action,
             Container container, 
             Node destNode) throws Exception{           
         this(sourceSender,requestedType,action, container);
         
         if(destNode != null){
             this.destNode = destNode;
         }
     }
     
     public Message(
             Object sourceSender,
             Object requestedType,
             ACTION action,
             Vector3f position) throws Exception
     {
         this(sourceSender, requestedType, action, (Container)null);
         
     }
     
     /**
      * The current action of the message
      * @return The current action
      */
 //    public ACTION GetAction(){
 //        return action;
 //    }
     
     /**
      * When the current action is to deliver a container
      * @return True if Action is deliver, False otherwise
      */
     public boolean Deliver(){
         return action.equals(ACTION.Deliver);
     }
     
     /**
      * When the current action is to fetch a container
      * @return True if Action is fetch, False otherwise
      */
     public boolean Fetch(){
         return action.equals(ACTION.Fetch);
     }
     
     /**
      * When the current action is to load a vehicle
      * @return True if Action is load, False otherwise
      */
     public boolean Load(){
         return action.equals(ACTION.Load);
     }
     
     /**
      * When the current action is to unload a vehicle
      * @return True if Action is unload, False otherwise
      */
     public boolean UnLoad(){
         return action.equals(ACTION.Unload);
     }
     
     /**
      * The destination node where the sender is stationd
      * @return The destination node
      */
     public Node DestinationNode() throws Exception
     {
        if(destinationObject.getClass() == TransportVehicle.class){            
             if(((TransportVehicle)destinationObject).getDestination() == null){
                 throw new Exception("destination node is null");
             }
             return ((TransportVehicle)destinationObject).getDestination();    
         }        
        else if (destinationObject.getClass() == Crane.class){
             if(((Crane)destinationObject).parkinglotAGV.node == null){
                 throw new Exception("destination node is null");
             }
             return ((Crane)destinationObject).parkinglotAGV.node;
         }
        else if (destinationObject.getClass() == StorageCrane.class){
             if(((StorageCrane)destinationObject).parkinglotAGV.node == null){
                 throw new Exception("destination node is null");
             }
             return ((StorageCrane)destinationObject).parkinglotAGV.node;
         }
         throw new Exception("Message send from unknown source");
     }
     
     /**
      * Gets the container that needs to be fetched or deliverd
      * @return The container
      */
     public Container GetContainer()
     {
         return container;
     }
     
     /**
      * Gets the object that send the request
      * @return The destination object
      */
     public Object DestinationObject()
     {
         return destinationObject;
     }
     
     /**
      * Gets the object that's requested
      * @return The requested object
      */
     public Object RequestedObject()
     {
         return requestedObject;
     }
     
     public Vector3f ContainerPosition()
     {
         return containerPos;
     }
 }
