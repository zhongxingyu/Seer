 import java.util.List;
 
 import models.SimpleUserAuthBinding;
 import models.User;
 
 import org.junit.Test;
 
import controllers.UserController;

 import play.test.Fixtures;
 import play.test.UnitTest;
 
 /**
  * User authentication test.
  * @author OpenARMS Service team
  */
 public class UserAuthTest extends UnitTest {
 	/**
 	 * Authentication test.
 	 */
 	@Test
     public void authenticationTest() {
 		//Fixtures.deleteAllModels();
 		//Fixtures.loadModels("data.yml");
 		
 		// Insert user to DB
 		User u = new User();
 		u.name = "test";
 		u.email = "avas@dfsdf.com";
 		u.secret = null;
 		u.userAuth = null;
 		u.save();
 		// Insert simple authentication to DB
 		SimpleUserAuthBinding s = new SimpleUserAuthBinding();
 		s.user = u;
 		s.password = "secret";
 		s.save();
 		// Bind authentication method with user in DB
 		u.userAuth = s;
 		u.save();
 		
 		// Request user from DB
 		List<User> users = User.findAll();
     	assertEquals(users.size(), 1);
     	User user = users.get(0);
     	// Try to authenticate
     	SimpleUserAuthBinding auth = (SimpleUserAuthBinding)user.userAuth;
    	String secret = auth.authenticate("secret");
     	// Check if authenticated
     	assertNotNull(secret);
     }
 }
