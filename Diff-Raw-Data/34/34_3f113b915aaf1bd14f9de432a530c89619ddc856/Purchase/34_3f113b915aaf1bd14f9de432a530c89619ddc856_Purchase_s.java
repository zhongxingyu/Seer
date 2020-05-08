 package mediastore;
 
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
    private long date;
 
     Purchase() {
         id = 0;
         price = 0.00;
        date = 0;
     }
 
     Purchase( int id, double price, long date ) {
         this.id = id;
         this.price = price;
        this.date = date;
     }
 
     public int getID() {
         return id;
     }
 
     public double getPrice() {
         return price;
     }
 
    public long getDate() {
        return date;
     }
 
     public String toTextDB() {
        return "" + id + '\n' + price + '\n' + date + '\n';
     }
 
     public String toString() {
        return "Item ID: " + id + "\nItem Price: " + price + "\nDate: " + System.currentTimeMillis();
     }
 }
