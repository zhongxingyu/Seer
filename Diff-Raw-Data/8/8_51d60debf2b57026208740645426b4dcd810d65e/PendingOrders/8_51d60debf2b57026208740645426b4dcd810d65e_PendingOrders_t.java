 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package logikk;
 
 import DB.Database;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /**
  *
  * @author Rino
  */
 public class PendingOrders implements Serializable {
 
     private Database database = new Database();
     private ArrayList<Order> orders = new ArrayList();
 
     public PendingOrders() {
 
     }
 
     public ArrayList<Order> getOrders() {
         return orders;
     }
 
     public ArrayList<Order> getFirstOrders() {//returns the session before chefs have changed the values
        return database.getPendingOrders("Select * from ORDERS where STATUS !=5");//5 = FINISHED
     }
 
     public void updateDb(Order s) {
         database.updateOrder(s);
     }
 }
