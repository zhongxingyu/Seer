 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import models.*;
 
 public class CustomerTest extends UnitTest 
 {
   
     
     @Test
     public void createandRetrieveCustomer_returnsNotNull()
     {
         Customer testCustomer = new Customer(1, "Matt", "Fitzpatrick", new Date(), 'M');
         testCustomer.save();
         assertNotNull(Customer.findById(testCustomer.id));
     }
     
     @Test
     public void newInstanceOfCustomer_defaultsToOrderCountOfZero(){
          Customer testCustomer = new Customer(1, "Matt", "Fitzpatrick", new Date(), 'M');
          assertEquals(testCustomer.orderCount, 0);
     }
     
     @Test
     public void updateOrderCount_increasesOrderCountByOne(){
         Customer testCustomer = new Customer(1, "Matt", "Fitzpatrick", new Date(), 'M');
         testCustomer.updateOrderCount();
         assertEquals(testCustomer.orderCount, (1));
     }
     
 
 }
