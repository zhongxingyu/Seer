 package Resources;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Adrian & Kris
  */
 public class Order {
     ArrayList<Product> orderList = new ArrayList<>();
     int customerID;
     int dateArrival, datePickUp;
     int trucksforDelivery, assemblersNeeded;
     int orderID;
 
     public Order(int customerID, int dateArrival, int datePickUp, int trucksforDelivery, int assemblersNeeded, int orderId) {
         this.customerID = customerID;
         this.dateArrival = dateArrival;
         this.datePickUp = datePickUp;
         this.trucksforDelivery = trucksforDelivery;
         this.assemblersNeeded = assemblersNeeded;
         this.orderID = orderId;
     }
     
    /*
     * No connection to DB. 
     * Solely for manipulating 
     * the ArrayList for the 
     * current order.
     */
     public boolean addItemToList(Product prod){
        boolean success=false;
         if(!checkForDuplicate(prod.getProductID())){
            success = true;
            orderList.add(prod);
         }
         return success;
     }
     
     public void removeFromOrder(Product prod) {
         orderList.remove(prod);
     }
     
     public boolean checkForDuplicate(int ID){
         boolean same = false;
         for (int i = 0; i < orderList.size(); i++) {
             if(orderList.get(i).getProductID() == ID){
                 same = true;
             }
         }
         return same;
     }
             
     public void setCustomer (int customerID) {
         this.customerID = customerID;
     }
     
     public int getCustomer(){
         return customerID;
     }
     
     public int getDateArrival() {
         return dateArrival;
     }
 
     public void setDateArrival(int dateArrival) {
         this.dateArrival = dateArrival;
     }
 
     public int getDatePickUp() {
         return datePickUp;
     }
 
     public void setDatePickUp(int datePickUp) {
         this.datePickUp = datePickUp;
     }
 
     public int getTrucksforDelivery() {
         return trucksforDelivery;
     }
 
     public void setTrucksforDelivery(int trucksforDelivery) {
         this.trucksforDelivery = trucksforDelivery;
     }
     
     public void setOrderId (int orderId) {
         this.orderID = orderId;
     }
     
     public int getOrderID() {
         return orderID;
     }
     
     public ArrayList<Product> getListInstance(){
         return orderList;
     }
     
     @Override
     public String toString() {
         return "CustomerID: "+customerID+"OrderID: "+orderID+"Start date: "+dateArrival+"End date: "+datePickUp
                 +"Trucks: "+trucksforDelivery+"Assemblers: "+assemblersNeeded;
     }
     
 }
