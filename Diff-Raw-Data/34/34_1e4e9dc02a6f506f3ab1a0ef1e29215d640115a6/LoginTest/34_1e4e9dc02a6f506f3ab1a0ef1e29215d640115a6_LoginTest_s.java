 import edu.berkeley.cs.cs162.*;
 
 import org.junit.*;
 
 import static org.junit.Assert.* ;
 
 public class LoginTest{
 	
 	ChatServer server;
 	
 	@Before
 	public void beforeEachTest() {
 		server = new ChatServer();
 		
 	}
 	
 	@After
 	public void afterEachTest() {
		server = null; 
 	}
 	
 	@Test
 	public void testAcceptedUserLogin() {
 		System.out.println("Running accepted user login test");
 		assertTrue(server.login("A") == LoginError.USER_ACCEPTED);
 		
 	}
 	
 	@Test
 	public void testRejectedUserLogin() {
 		System.out.println("Running rejected user login test");
 		server.login("A");
 		assertTrue(server.login("A") == LoginError.USER_REJECTED);
 	}
 	
 	@Test
 	public void testDroppedUserLogin() {
 		System.out.println("Running dropped user login test");
 		for (int i = 0; i< 100; i++) {
 			server.login(Integer.toString(i));
 		}
 		assertTrue(server.login("A") == LoginError.USER_DROPPED);
 	}
 	
 }
