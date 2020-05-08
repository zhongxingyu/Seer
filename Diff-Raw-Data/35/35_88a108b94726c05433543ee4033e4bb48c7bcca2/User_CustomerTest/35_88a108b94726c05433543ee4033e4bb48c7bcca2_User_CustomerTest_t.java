 import org.junit.*;
 import play.test.*;
 import play.mvc.*;
 import play.mvc.Http.*;
 import models.*;
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author mfitzpat
  */
 public class User_CustomerTest extends UnitTest{
     
     @Test
     public void addNewCustomerToUser_succeeds(){
         User testUser = new User(5, "mfitzpat", "password", "Matt", "Fitz");
        User_Customer.addCustomer(testUser, new Customer(42, "Will", "Shep", testUser.userID));
     }
     
     @Test
     public void addNewCustomerToUser_actuallyAddsNewCustomer(){
         User testUser = new User(5, "mfitzpat", "password", "Matt", "Fitz");
        User_Customer.addCustomer(testUser, new Customer(42, "Will", "Shep", testUser.userID));
         assertEquals(new Integer(42), testUser.customerID.get(1));
     }
     
     @Test
     public void multipleCustomers_successfullyShareAUserId(){
         User savedUser = new User(5, "mfitzpat", "password", "Matt", "Fitz");
        User_Customer.addCustomer(savedUser, new Customer(42, "Will", "Shep", savedUser.userID));
        User_Customer.addCustomer(savedUser, new Customer(25, "Will", "Shep", savedUser.userID));
        User_Customer.addCustomer(savedUser, new Customer(63, "Will", "Shep", savedUser.userID));
     }
     
     
    
     
 }
