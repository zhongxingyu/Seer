 import edu.berkeley.cs.cs162.*;
 
 import org.junit.*;
 
 import static org.junit.Assert.* ;
 
 public class LogoutTest {
 	
 	ChatServer server;
 	
 	@Before
 	public void beforeEachTest() {
 		server = new ChatServer();
 	}
 	
 	@After
 	public void afterEachTest() {
		server.shutdown();
 	}
 	
 	@Test
 	public void testAcceptedUserLogoff() {
 		server.login("A");
 		System.out.println("Running accepted user logoff test");
 		assertTrue(server.logoff("A") == true);
 	}
 	
 	@Test
 	public void testRejectedUserLogoff() {
 		System.out.println("Running rejected user logoff test");
 		assertTrue(server.logoff("A") == false);
 	}
 }
