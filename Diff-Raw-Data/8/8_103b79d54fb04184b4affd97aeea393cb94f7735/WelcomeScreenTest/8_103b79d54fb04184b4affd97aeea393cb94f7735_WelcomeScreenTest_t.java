 package testcases;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import org.junit.Test;
 
 import elfville.protocol.Response;
 import elfville.protocol.SignInRequest;
 import elfville.protocol.SignUpRequest;
 import elfville.protocol.Response.Status;
 
 public class WelcomeScreenTest extends TestBase {
 
 	@Test
 	public void test1SignUp() throws UnknownHostException, IOException {
 		int currentUser = 0;
		SignUpRequest req = new SignUpRequest("user" + currentUser, "");
 		Response resp = socketControllers.get(currentUser).send(req);
 		// System.out.println("signUpTest: " + resp.status.toString());
 		assertEquals(resp.status, Status.SUCCESS);
 	}
 
 	@Test
 	public void test2SignUpDuplicate() throws UnknownHostException, IOException {
 		// should fail because we signed up the same username once above
		SignUpRequest req = new SignUpRequest("user0", "");
 		Response resp = socketControllers.get(0).send(req);
 		// System.out.println(resp.status.toString());
 		assertEquals(resp.status, Status.FAILURE);
 	}
 
 	@Test
 	public void test3MultiSignUp() throws UnknownHostException, IOException {
 		for (int i = 1; i < clientNum; i++) {
 			// System.out.println("kkkkkkk " + i);
			SignUpRequest req = new SignUpRequest("user" + i, "");
 			Response resp = socketControllers.get(i).send(req);
 			// System.out.println("multi signup: " + i + " " + resp.status.toString());
 			assertEquals(resp.status, Status.SUCCESS);
 		}
 	}
 	
 	@Test
 	public void test4SignInWrong() throws UnknownHostException, IOException {
 		SignInRequest req = new SignInRequest("user");
 		Response resp = socketControllers.get(0).send(req);
 		// System.out.println(resp.status.toString());
 		assertEquals(resp.status, Status.FAILURE);
 	}
 
 	@Test
 	public void test5SignIn() throws UnknownHostException, IOException {
 		int currentUser = 0;
 		SignInRequest req = new SignInRequest("user" + currentUser);
 		Response resp = socketControllers.get(currentUser).send(req);
 		// System.out.println(resp.status.toString());
 		assertEquals(resp.status, Status.SUCCESS);
 	}
 }
