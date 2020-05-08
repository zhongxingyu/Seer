 package net.styleguise.converge;
 
 import net.styleguise.converge.AuthenticationResponse.Response;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class ConvergeClientTest {
 	
 	private ConvergeClient client;
 	
 	@Before
 	public void setUp() throws Exception {
		String authKey = "3d40d8a29601dc2884fd68246c473986";
		int productId = 9;
 		client = new ConvergeClient("http://www.styleguise.net/converge", authKey, productId);
 	}
 	
 	@Test
 	public void testCheckUsernameAvailable() throws Exception {
 		
 		assertFalse(client.checkUsernameAvailable("bpossolo"));
 		assertTrue(client.checkUsernameAvailable("billybobthornton"));
 	}
 	
 	@Test
 	public void testCheckEmailAvailable() throws Exception {
 		
 		assertFalse(client.checkEmailAvailable("bpossolo@gmail.com"));
 		assertTrue(client.checkEmailAvailable("tommy@gun.com"));
 	}
 	
 	@Test
 	public void testAuthenticateByUsername() throws Exception {
 		
 		AuthenticationResponse resp = client.authenticate("TheCruise", AuthType.Username, "qualityRus");
 		assertEquals(Response.Success, resp.getResponse());
 		assertEquals("bpossolo+1@gmail.com", resp.getEmail());
 	}
 	
 	@Test
 	public void testAuthenticateByEmail() throws Exception {
 		
 		AuthenticationResponse resp = client.authenticate("bpossolo+1@gmail.com", AuthType.Email, "qualityRus");
 		assertEquals(Response.Success, resp.getResponse());
 		assertEquals("TheCruise", resp.getUsername());
 	}
 	
 	@Test
 	public void testAuthenticateBadPass() throws Exception {
 		
 		AuthenticationResponse resp = client.authenticate("TheCruise", AuthType.Username, "wrongpass");
 		assertEquals(Response.WrongAuth, resp.getResponse());
 	}
 	
 	@Test
 	public void testAuthenticateNonExistentUser() throws Exception {
 		
 		AuthenticationResponse resp = client.authenticate("tommy_gun", AuthType.Username, "fakepass");
 		assertEquals(Response.NoUser, resp.getResponse());
 	}
 
 }
