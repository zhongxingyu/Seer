 import model.general.User;
 import model.service.MockUserService;
 
 import org.junit.Test;
 
 import play.mvc.Http.Response;
 import play.test.FunctionalTest;
 
 
 public class MockTest extends FunctionalTest {
 	
     @Test
     public void testMockUserLogin() {
     	String valid_username = "gudrunht";
     	String invalid_username = "siggi";
     	String password = ""; //any password is valid
     	
         MockUserService service = new MockUserService();
         User valid_user = service.login(valid_username, password);
         User invalid_user = service.login(invalid_username, password);
         
        assertNotNull(valid_user); //should return an object which is not null
         assertNull(invalid_user); //should return a null object
        assertEquals(valid_username, valid_user.getUsername());
     }
 }
