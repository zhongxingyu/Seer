 package dit126.group4.group4shop.core;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
import org.junit.BeforeClass;
 
 /**
  *
  * @author Group4
  */
 public class TestDatabase {
 
         final static String PU = "group4_test_shop";
         static IGroup4Shop shop;
         
        @BeforeClass
        public static void before(){
              shop = Group4ShopFactory.getShop(PU);
         }
         
         @Test
         public void pointLess(){
             assertTrue(true);
         }
         
         @Test
         public void testAddProduct(){
             Product p = new Product(new Long(1),"Banan", 22.33, "En gul banan");
             
             shop.getProductCatalogue().add(p);
             Product found = shop.getProductCatalogue().find(new Long(1));
 
             assertTrue(found!=null);
         }
 
 
 
 }
