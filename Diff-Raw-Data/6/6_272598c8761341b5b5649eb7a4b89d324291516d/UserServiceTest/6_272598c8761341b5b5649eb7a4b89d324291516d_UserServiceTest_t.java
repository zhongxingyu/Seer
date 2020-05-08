 package ro.gagarin;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Unit test for simple App.
  */
 public class UserServiceTest {
 
 	private static String session = null;
 	private static Authentication api = null;
 
 	@BeforeClass
 	public static void setUp() throws SessionNotFoundException_Exception,
 			UserNotFoundException_Exception {
 		AuthenticationService service = new AuthenticationService();
 		api = service.getAuthenticationPort();
 		session = api.createSession(null, null);
 		api.login(session, "admin", "password", null);
 	}
 
 	@AfterClass
 	public static void shutDown() {
 		api.logout(session);
 	}
 
 	@Test
 	public void createUser() throws FieldRequiredException_Exception,
			SessionNotFoundException_Exception, UserAlreadyExistsException_Exception,
			PermissionDeniedException_Exception {
 		UserServiceService service = new UserServiceService();
 		UserService userAPI = service.getUserServicePort();
		DbUser user = new DbUser();
 		user.setUsername("wsUser1");
 		user.setPassword("wspassword1");
 		userAPI.createUser(session, user);
 	}
 }
