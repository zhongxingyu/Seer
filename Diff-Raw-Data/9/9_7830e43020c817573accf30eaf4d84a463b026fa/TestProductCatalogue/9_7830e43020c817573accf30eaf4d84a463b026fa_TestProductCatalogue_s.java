 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop.core;
 
 import java.util.List;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.BeforeClass;
 
 /**
  * Test for ProductCatalogue
  *
  * NOTE: Must have Table generation strategy as "Drop and Create"
  *
  * @author David
  */
 public class TestProductCatalogue {
     
     final static String PU = "group4_test_shop";
     static IGroup4Shop shop;
     
     @BeforeClass
     public static void before(){
         shop = Group4ShopFactory.getShop(PU);
     }
     
     //Testing methods add(Product p), getCount(), update(Long id), find(Long id) and remove(Long id)
     @Test
     public void testProductCatalgoue(){
         IProductCatalogue pc = shop.getProductCatalogue();
 
         // Test to add a product
         Long id = (long) 22000;
         Product p1 = new Product(id, "crocodile", 9000.00, "A fierce pet that only australians can have at home");
         pc.add(p1);
         
         assertTrue(pc.getCount() == 1); //and testing that getCount() works as intended
         
         // Test to find a product
         Product p2 = pc.find(id);
         
         assertTrue(p1 != p2);
         assertTrue(p1.equals(p2));
         
         // Test to update a product
         Product p = new Product(p1.getId(), "alligator", 7000.00, "FOR REDNECKS ONLY");
         pc.update(p);
         p1 = pc.find(p1.getId());
         
         assertTrue(p1.equals(p2));
         assertFalse(p1.getName().equals(p2.getName()));
         assertTrue(pc.getCount() == 1);
   
         // Test to remove a product
         pc.remove(p1.getId());
         
         assertTrue(pc.getCount() == 0);
         
     }
 
    @Test
     public void testGetRange(){
         IProductCatalogue pc = shop.getProductCatalogue();
         
         for(int i = 0; i < 5; i++){
             String desc = "Description for product " + i;
             Double d = (double) i;
             Long id = (long) i;
             pc.add(new Product(id, String.valueOf(i), d, desc));
         }
         
         List<Product> prods = pc.getRange(0, 2);
         assertTrue(prods.size() == 2);
     }
 }
