 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import java.util.ArrayList;
 import javax.persistence.Entity;
 import play.db.jpa.Model;
 
 
 /**
  * The USER_CUSTOMER table associates users with customers. It is possible
  * for a single user to have more than one customer. For example, a husband
  * and wife can be associated with a single user account.
  * 
  * @author William
  */
 
 @Entity
 public class User_Customer extends Model
 {
     public static int userID;
     private static int ID = 0;
     
   public static void addCustomer(User user, Customer customer)

    {
        user.customerID.add(customer.customerID);
        customer.userID = userID;
    }
    
    public static int updateID()
    {
        return ID++;
    }
   
   public static void addNewCustomer(int customerID ,String firtName, String lastName, User user)
   {
       Customer customer = new Customer(customerID, firtName, lastName, userID).save();
       user.customerID.add((customerID));
   }
 }
