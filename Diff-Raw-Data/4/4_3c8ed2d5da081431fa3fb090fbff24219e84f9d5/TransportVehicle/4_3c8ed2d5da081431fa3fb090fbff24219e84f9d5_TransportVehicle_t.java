 package Vehicles;
 
 import Crane.Crane;
 import Crane.StorageCrane;
 import Helpers.Vector3f;
 import Main.Container;
 import Network.objPublisher;
 import Parkinglot.Parkinglot;
 import Pathfinding.Node;
 import Pathfinding.Pathfinder;
 import Storage.Storage_Area;
 import java.util.Date;
 
 /**
  * An extension of the vehicle class.
  * @author Tonnie Boersma
  */
 public class TransportVehicle extends Vehicle {
     
     /**
      * The date the vehicle arrives
      */
     Date arrivalDate;
     /**
      * The date the vehicle departures 
      */
     Date departureDate;
     /**
      * The company that send this vehicle
      */
     String arrivalCompany;
     /**
      * The speed of this vehicle
      */
     public float speed = 5f;
     
     /**
      * If the vehicle is going to departure
      */
     boolean departure;
     /**
      * If the vehicle doesn't need to be updated anymore
      */
     boolean destroy;
     /**
      * Reference to objPublisher
      */
     final objPublisher objpublisher;
     
     /**
      * When called the transport vehicle will leave the harbor
      */
     public void Departure(Node destination) throws Exception
     {
         departure = true;
         setDestination(destination);
     }
     /**
      * When the vehicle needs to be destroyed
      * @return If true, destroy the vehcile
      */
     public boolean Destroy()
     {
         return destroy;
     }
     
     /**
      * Constructs a new TransportVehicle
      * @param arrivalDate The date the vehicle arrives
      * @param departureDate The date the vehicle departures
      * @param arrivalCompany The company that send this vehcile
      * @param vehicleType The type of this transport vehcile
      * @param containerArraySize The size of the storage 
      * @param startPosition The node the vehicle arrives
      * @throws Exception 
      */
     public TransportVehicle(Date arrivalDate, Date departureDate, String arrivalCompany, VehicleType vehicleType, Vector3f containerArraySize, Node startPosition, objPublisher objpublisher) throws Exception
     {
         if (arrivalDate == null || departureDate == null || arrivalCompany == null || containerArraySize == null || startPosition == null || vehicleType == null){
             throw new Exception("\nThe input variable can't be null:"+
                     "\narrivalDate: " + arrivalDate +
                     "\ndepartureDate: " + departureDate +
                     "\narrivalCompany: " + arrivalCompany +
                     "\nvehicleType: " + vehicleType +
                     "\ncontainerArraySize: " + containerArraySize +
                     "\nstartPosition: " + startPosition);
         }        
         this.position = startPosition.getPosition();
         this.destination = new Parkinglot(1, startPosition);
         this.arrivalDate = arrivalDate;
         this.departureDate = departureDate;
         this.arrivalCompany = arrivalCompany;
         this.vehicleType = vehicleType;
         this.storage = new Storage_Area((int)containerArraySize.x, (int)containerArraySize.z, (int)containerArraySize.y, position);        
         this.objpublisher = objpublisher;
         this.destroy = false;
     }
         
     /**
      * Set the destination for the vehicle.
      * @param destination The destination node.
      * @throws Exception If something goes wrong while calculating the route.
      */
     public void setDestination(Node destination) throws Exception{
        this.destination.unPark(this);
         this.destination = new Parkinglot(1, destination);
         this.route = Pathfinding.Pathfinder.findShortest(Pathfinder.findClosestNode(position), destination, storage.Count() == 0);
         this.routeIndex = 1;
     }
     
     /**
      * Gets the destination from the destinationObject
      * Sets the found destination for the vehicle.
      * @param destinationObject The destination Object.
      * @throws Exception If something goes wrong while calculating the route.
      */
     public void setDestination(Object destinationObject) throws Exception{
         if (Crane.class == destinationObject.getClass()){
             this.destination = ((Crane)destinationObject).parkinglotTransport;
         }
         else if (StorageCrane.class == destinationObject.getClass()){
             this.destination = ((StorageCrane)destinationObject).parkinglotTransport;
         }
         else if (destinationObject.getClass() == Parkinglot.class){
             this.destination = (Parkinglot)destinationObject;
         }
         else{
             throw new Exception("The input isn't a crane or storageCrane: " + destinationObject);
         }
         
         this.route = Pathfinding.Pathfinder.findShortest(Pathfinder.findClosestNode(position), destination.node, storage.Count() == 0);
         this.routeIndex = 1;
     }
     
     public void setPostion(Vector3f position){
         this.position = position;        
     
     }
     public void setPostion(Node position){
         this.position = position.getPosition();        
     
     }
     /**
      * Updates the movement
      * @param gameTime The elapsed gametime between 
      * @throws Exception 
      */
     @Override
     public void update(float gameTime) throws Exception {
         if (position == destination.node.getPosition()){
             if(!parked){
                destination.park(this);
             }
             if(departure){
                 destroy = true;
             }                
         }
         else if(position == route[routeIndex].getPosition()){
             routeIndex++;
             if(objpublisher!=null)
                 objpublisher.syncVehicle(this);
         }
         else{
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
     }
     
     /**
      * Get the arrival date.
      * @return arrivalDate.
      */
     public Date GetArrivalDate(){
         return arrivalDate;
     }
 
     /**
      * Get the departure date.
      * @return departureDate.
      */
     public Date GetDepartureDate(){
         return departureDate;
     }
     
     /**
      * Get the Company.
      * @return Company.
      */
     public String GetCompany(){
         return arrivalCompany;
     }
     
     /**
      * Matches the container properties with the vehicle properties,
      * @param container The container to match.
      * @return True if the container matches the vehicle, false otherwise.
      */
     public boolean MatchesContainer(Container container){        
         return this.GetArrivalDate().equals(container.getArrivalDateStart()) && 
                 this.GetDepartureDate().equals(container.getArrivalDateEnd()) &&
                 this.GetCompany().equals(container.getArrivalCompany());
     }
     
     @Override	
     public String toString(){
         return  "\n" + Container.df.format(arrivalDate) + " <-> " + Container.df.format(departureDate) +
                 "\n" + "ArrivalCompany: " + arrivalCompany +
                 "\n" + "ContainerfieldLenght: " + storage.getLength() + 
                 "\n" + "ContainerfieldWidth: " + storage.getWidth() + 
                 "\n" + "ContainerfieldHeight: " + storage.getHeight() + 
                 "\n" + "_____________________________(" + storage.Count() + ")"+ 
                 "\n" + storage;
     }
 }
