 package Vehicles;
 
 import Helpers.Vector3f;
 import Main.Container;
 import Pathfinding.Node;
 import Pathfinding.Pathfinder;
 import Storage.Storage_Area;
 import java.util.Date;
 
 public class Train extends TransportVehicle{
 
     private Date arrivalDate;
     private Date departureDate;
     private String arrivalCompany;
     public Storage_Area storage;
     private Vector3f position;
     private Node destination;
     private Node[] route;
     private float speed/*= X*/;
    
     public Train(Date arrivalDate, Date departureDate, String arrivalCompany, int trainLenght, Node startPosition) throws Exception
     {
         if (arrivalDate == null || departureDate == null || arrivalCompany == null || trainLenght == 0 || startPosition == null){
             throw new Exception("\nThe input variable can't be null:"+
                     "\narrivalDate: " + arrivalDate +
                     "\ndepartureDate: " + departureDate +
                     "\narrivalCompany: " + arrivalCompany +
                     "\ntrainLenght: " + trainLenght +
                     "\nstartPosition: " + startPosition);
         }
         else{
             this.position = startPosition.getPosition();
             this.arrivalDate = arrivalDate;
             this.departureDate = departureDate;
             this.arrivalCompany = arrivalCompany;
             storage = new Storage_Area(trainLenght, 1, 1, position);
         }
     }
     
     @Override
     public void setDestination(Node destination) {
         try {
             this.destination = destination;
             route = Pathfinding.Pathfinder.findShortest(Pathfinder.findClosestNode(position), destination);
         } 
         catch (Exception ex) {
 
         }
     }
 
     @Override
     public Node getDestination() {
         return (destination == null) ? Pathfinder.findClosestNode(position) : destination;
     }
 
     @Override
     public Vector3f getPosition() {
        return position;
     }
 
     @Override
     public void update(float gameTime) throws Exception {
         if (position == destination.getPosition()){
             // send message
             // wait for message depart
         }
         else{
             // follow route 
             // update position
         } 
     }
     
     @Override
     public Date GetArrivalDate(){
         return arrivalDate;
     }
     
     @Override
     public Date GetDepartureDate(){
         return departureDate;
     }
     
     @Override
     public String GetCompany(){
         return arrivalCompany;
     }
     
     @Override
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
                 "\n" + "_____________________________(" + storage.Count() + ")" + 
                 "\n" + storage;
     }
 }
