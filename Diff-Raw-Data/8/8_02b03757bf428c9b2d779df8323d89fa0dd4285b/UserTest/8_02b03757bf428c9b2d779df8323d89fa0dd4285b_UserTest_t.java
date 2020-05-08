 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import models.User;
 
 import play.modules.morphia.Blob;
 import play.modules.morphia.MorphiaPlugin;
 
 public class UserTest extends UnitTest {
 
     @Test
     public void aVeryImportantThingToTest() {
         assertEquals(2, 1 + 1);
     }
 
     @Test
     public void testDefaultStoreUser() {
       User u = new User();
       u.save();
 
       assertNotNull(u.username);
       assertEquals("derp", u.username);
       assertNotNull(u.email);
 
       u.delete();
     }
 
     @Test
     public void testStoreUser() {
      User u = new User("jazzdan", "Miller", "Dan", "jazzdan@gmail.com", "jazz", true);
       u.save();
 
       u.delete();
     }
       
     @Test
     public void testDeleteUser() {
      User u = new User("carley", "Keefe", "Carley", "carley@cloudcount.com", "password", true);
       u.save();
 
       //User.removeUser("crabideau");
       // assertNull(u);
     }
 
     @Test
     public void seleniumUser() {
       User test = User.find("username", "selenium").first();
       if(test == null) {
        test = new User("selenium", "Sel", "Enium", "sel@enium.com", "password", true);
         test.save();
       }
     }
 
 }
