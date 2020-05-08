 package pl.testng.ch11;
 
 
 import org.testng.annotations.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.fail;
 
 
 public class FridgeTest {
 
     @Test
     public static void testFridge() {
         Fridge fridge = new Fridge();
         fridge.put("cheese");
         assertEquals(fridge.contains("cheese"), true);
         assertEquals(fridge.put("cheese"),false);
         assertEquals(fridge.contains("cheese"),true);
         assertEquals(fridge.contains("ham"),false);
 
 
         fridge.put("ham");
         assertEquals(fridge.contains("cheese"), true);
         assertEquals(fridge.contains("ham"),true);
 
         try {
             fridge.take("sausage");
         }catch (NoSuchItemException e) {
             //ok
         }
     }
     @Test
     public void testPutTake() {
         Fridge fridge = new Fridge();
         List<String> food = new ArrayList<>();
         food.add("yogurt");
         food.add("milk");
         food.add("eggs");
         for(String item:food) {
             fridge.put(item);
             assertEquals(fridge.contains(item),true);
            try {
                fridge.take(item);
                fail("there was no "+item+" in the fridge");
            } catch (NoSuchItemException e) {
                //wrong
            }
            assertEquals(fridge.contains(item),false);
         }
         for (String item: food) {
             try{
                 fridge.take(item);
             }catch (NoSuchItemException e) {
                 assertEquals(e.getMessage().contains(item),true);
             }
 
         }
     }
 
 }
