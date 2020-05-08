 package pizzawatch.datamodels;
 
 
 public class Order
 {
     private final String userID;
    private final int orderID;
     private final String address;
     private final String pizzaType;
     private final String deliveryMethod;
 
    public Order(int oid, String uid, String from, String pizzaType, String deliveryMethod)
     {
         this.address = from;
        this.orderID = oid;
         this.pizzaType = pizzaType;
         this.deliveryMethod = deliveryMethod;
         this.userID = uid;
     }
 
     public String getUserID()
     {
         return userID;
     }
 
    public int getOrderID()
     {
         return orderID;
     }
 
     public String getAddress()
     {
         return address;
     }
 
     public String getPizzaType()
     {
         return pizzaType;
     }
 
     public String getDeliveryMethod()
     {
         return deliveryMethod;
     }
 }
