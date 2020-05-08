 package mediastore;
 
import java.util.Calendar;

 /**
  * A class that encapsulates a single purchase made by a Customer.
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  * @version 1.0 Feb 7, 2013
  *
  */
 public class Purchase {
 
     private int id; // id of the product purchased.
     private double price;
    private long purchaseUnixTime;
 
     Purchase() {
         id = 0;
         price = 0.00;
        purchaseUnixTime = 0;
     }
 
     Purchase( int id, double price, long date ) {
         this.id = id;
         this.price = price;
        this.purchaseUnixTime = date;
     }
 
     public int getID() {
         return id;
     }
 
     public double getPrice() {
         return price;
     }
 
    public long getPurchaseUnixTime() {
        return purchaseUnixTime;
     }
 
     public String toTextDB() {
        return "" + id + '\n' + price + '\n' + purchaseUnixTime + '\n';
     }
 
     public String toString() {
        Calendar myDate = Calendar.getInstance();
        myDate.setTimeInMillis( purchaseUnixTime );
        return "Item ID: " + id + "\nItem Price: " + price + "\nDate: " + myDate.get( Calendar.DAY_OF_MONTH ) + "." + myDate.get( Calendar.MONTH ) + "." + myDate.get( Calendar.YEAR );
     }
 }
