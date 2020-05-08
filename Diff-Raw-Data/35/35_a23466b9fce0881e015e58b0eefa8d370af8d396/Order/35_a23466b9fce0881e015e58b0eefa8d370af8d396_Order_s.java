 package pizzawatch.datamodels;
 
import java.util.Random;

 
 public class Order
 {
     private final String userID;
    private final String orderID;
     private final String address;
     private final String pizzaType;
     private final String deliveryMethod;
 
    public Order(String uid, String from, String pizzaType, String deliveryMethod)
     {
        Random rng = new Random();
         this.address = from;
        this.orderID = Integer.toString(rng.nextInt(10000000));
         this.pizzaType = pizzaType;
         this.deliveryMethod = deliveryMethod;
         this.userID = uid;
     }
 
     public String getUserID()
     {
         return userID;
     }
 
    public String getOrderID()
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
