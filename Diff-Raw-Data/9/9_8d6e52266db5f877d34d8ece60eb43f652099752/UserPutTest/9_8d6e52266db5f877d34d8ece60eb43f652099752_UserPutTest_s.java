 package ch.hsr.bieridee.test;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.restlet.data.MediaType;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ClientResource;
 
 import ch.hsr.bieridee.config.Res;
 import ch.hsr.bieridee.utils.NodeProperty;
 
 /**
  * Test the creation of a new user via PUT on the user resource.
  * 
  */
 public class UserPutTest {
 
 	final String testUsername = "tester";
 	final JSONObject userJson = new JSONObject();
 	
 	/**
 	 * Test setup.
 	 */
 	@Before
 	public void setUp() {
 		try {
 			this.userJson.put(NodeProperty.User.USERNAME, this.testUsername);
 			this.userJson.put(NodeProperty.User.PRENAME, "Stefan");
 			this.userJson.put(NodeProperty.User.SURNAME, "Keller");
 			this.userJson.put(NodeProperty.User.EMAIL, "seff@openstreet.map");
 			this.userJson.put(NodeProperty.User.PASSWORD, "7a6sdfp87ilovecats9difcapo43we");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
	 * Tests the creaton of a new user.
 	 */
 	@Test
 	public void createAndGetCreatedUser() {
 		final ClientResource clientResource = new ClientResource(Res.API_URL + "/users/" + this.testUsername);
 
 		final Representation rep = new StringRepresentation(this.userJson.toString(), MediaType.APPLICATION_JSON);
 		clientResource.put(rep);
 		
 		final Representation newUserRep = clientResource.get(MediaType.APPLICATION_JSON);
 		JSONObject newUser = null;
 		try {
 			newUser = new JSONObject(newUserRep.getText());
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		Assert.assertNotNull(newUser);
 		try {
 			Assert.assertEquals(this.userJson.get(NodeProperty.User.USERNAME), newUser.get(NodeProperty.User.USERNAME));
 			Assert.assertEquals(this.userJson.get(NodeProperty.User.PRENAME), newUser.get(NodeProperty.User.PRENAME));
 			Assert.assertEquals(this.userJson.get(NodeProperty.User.SURNAME), newUser.get(NodeProperty.User.SURNAME));
 			Assert.assertEquals(this.userJson.get(NodeProperty.User.EMAIL), newUser.get(NodeProperty.User.EMAIL));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		clientResource.release();
 	}
 	
 	/**
 	 * Tests the update of the user.
 	 */
 	@Test
 	public void updateAndGetUser() {
 		final ClientResource clientResource = new ClientResource(Res.API_URL + "/users/" + this.testUsername);
 		
 		
 		final JSONObject partialUserJson = new JSONObject();
 		try {
 			partialUserJson.put(NodeProperty.User.USERNAME, "notpossibletoupdate");
 			partialUserJson.put(NodeProperty.User.PRENAME, "Danilo");
 			partialUserJson.put(NodeProperty.User.SURNAME, "Bargen");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		
 		final Representation rep = new StringRepresentation(partialUserJson.toString(), MediaType.APPLICATION_JSON);
 		clientResource.put(rep);
 		
 		final Representation updatedUserRep = clientResource.get(MediaType.APPLICATION_JSON);
 		JSONObject updatedUser = null;
 		try {
 			updatedUser = new JSONObject(updatedUserRep.getText());
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		Assert.assertNotNull(updatedUser);
 		try {
 			Assert.assertEquals(this.testUsername, updatedUser.get(NodeProperty.User.USERNAME));
 			Assert.assertEquals(partialUserJson.get(NodeProperty.User.PRENAME), updatedUser.get(NodeProperty.User.PRENAME));
 			Assert.assertEquals(partialUserJson.get(NodeProperty.User.SURNAME), updatedUser.get(NodeProperty.User.SURNAME));
 			Assert.assertEquals(this.userJson.get(NodeProperty.User.EMAIL), updatedUser.get(NodeProperty.User.EMAIL));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		clientResource.release();
 	}
 }
