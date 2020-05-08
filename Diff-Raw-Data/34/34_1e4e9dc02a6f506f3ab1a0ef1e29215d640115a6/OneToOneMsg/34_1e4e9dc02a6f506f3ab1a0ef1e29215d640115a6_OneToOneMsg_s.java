 import junit.framework.TestCase;
 import edu.berkeley.cs.cs162.*;
 import org.junit.*;
 import static org.junit.Assert.* ;
 
 public class OneToOneMsg {
 	
 	ChatServer server;
 	User a;
 	User b;
 	
 	@Before
 	public void beforeEachTest() {
 		server = new ChatServer();
 		server.login("A");
 		a = (User)server.getUser("A");
 	}
 	
 	@After
 	public void afterEachTest() {
		server = null;
 		a = null;
 		b = null;
 	}
 	
 	@Test
 	public void testReceivedMessage() {
 		System.out.println("Running send 1-1 message test");
 		server.login("B");
 		b = (User)server.getUser("B");
 		a.send("B", "testing testing 123");
 		try {
			Thread.sleep(1000);
 		}catch(Exception e) {
 			
 		}
 		String logged = b.getLog("A").getLog().get(0).getContent();
 		assertTrue(logged.equals("testing testing 123"));
 	}
 	
 }
