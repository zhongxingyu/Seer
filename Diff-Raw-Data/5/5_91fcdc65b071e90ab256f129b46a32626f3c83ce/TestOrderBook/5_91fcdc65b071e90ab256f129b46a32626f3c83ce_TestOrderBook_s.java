 package dit126.group4.group4shop.core;
 
 import java.util.List;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author David
  */
 public class TestOrderBook {
    
     final static String PU = "group4_test_shop";
     static IGroup4Shop shop;
     
     @BeforeClass
     public static void before(){
         shop = Group4ShopFactory.getShop(PU);
     }
     
     //Will test OrderItem, PurchaseOrder and OrderBook
     @Test
     public void testOrderBook(){
         String email = "do@test.se";
         Users user = new Users(email, "David", "O", "asdfgh");
         shop.getUserRegister().add(user);
         
         Users user1 = shop.getUserRegister().find(email);
         assertTrue(user != user1);
         assertTrue(user.equals(user1));
         
         Long id = (long) 10001;
         Product p1 = new Product(id, "Bat", 20.20, "Djur","A flying creature that navigates with its supersonic hearing");
         Product p2 = new Product((id+1), "Ferret", 100.80,"Djur", "Have become popular lately as a pet");
         shop.getProductCatalogue().add(p1);
         shop.getProductCatalogue().add(p2);
         
         user.getCart().add(p1);
         user.getCart().add(p1);
         user.getCart().add(p2);
         
         //test if PurchaseOrder persists
         List<OrderItem> items = user.getCart().getAsOrderItems();
         PurchaseOrder po = new PurchaseOrder(id, user, items);
         shop.getOrderBook().add(po);
         
         PurchaseOrder po1 = shop.getOrderBook().find(id);
         assertTrue(po != po1);
         assertTrue(po.equals(po1));
    }
 }
