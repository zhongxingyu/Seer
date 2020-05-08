 package functional;
 
 import models.User;
 import models.helpers.UserConnectionHelper;
 import oauth2.functional.AccessTokenTest;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.Logger;
 import play.cache.Cache;
 import play.mvc.Http;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 public class UsersTest extends FunctionalTest {
 	
 	private Http.Request request;
 	private Http.Response response;
 
 	private static final String BASE_CONTROLLER_PATH = "/users";
 	private String userBaseQuery = "?oauth_token=";
 	private String user1Query = "";
 	private String user2Query = "";
 	private String user3Query = "";
 	
 	private User user1;
 	private User user2;
 	private User user3;
 	
 	@Before
 	public void setUp() {
		Fixtures.deleteAll();
		Fixtures.load("data.yml");
 		Cache.clear();
 		
 		request = new Http.Request();
 		request.headers.put("accept", new Http.Header("Accept", "application/json"));
 		
 		user1 = User.find("byEmail", "bob@gmail.com").first();
 		user1Query += userBaseQuery + user1.accessToken;
 		
 		user2 = User.find("byEmail", "bob2@gmail.com").first();
 		user2Query += userBaseQuery + user2.accessToken;
 		UserConnectionHelper.createUserConnection(user1, user2);
 		
 		user3 = User.find("byEmail", "alex@jarvis.com").first();
 		user3Query += userBaseQuery + user3.accessToken;
 	}
 	
 	@After
 	public void log() {
 		if (response != null) {
 			Logger.debug("Response Status: " + response.status.toString());
 			Logger.debug("Response: " + (response.out.toString().isEmpty() ? "" : "\n" + response.out.toString()) );
 		}
 	}
 
 	@Test
 	public void testIndexPage() {
 		response = GET(BASE_CONTROLLER_PATH + user1Query);
 		assertIsOk(response);
 		assertContentType("application/json", response);
 	}
 
 	@Test
 	public void testCreate() {	
 		String body = "{\"email\":\"axj7@aber.ac.uk\","
 			+ "\"firstName\":\"Alex\","
 			+ "\"lastName\":\"Jarvis\","
 			+ "\"password\":\"password\","
 			+ "\"mobileNumber\":\"123\"}";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 
 		response = POST(request, BASE_CONTROLLER_PATH, "application/json; charset=UTF-8", body);
 		
 		assertStatus(201, response);
 	}
 	
 	@Test
 	public void testCreateErrorExistingEmail() {
 		testCreate();
 		
 		String body = "{\"email\":\"axj7@aber.ac.uk\","
 			+ "\"firstName\":\"Alex\","
 			+ "\"lastName\":\"Jarvis\","
 			+ "\"password\":\"password\","
 			+ "\"mobileNumber\":\"123\"}";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 
 		response = POST(request, BASE_CONTROLLER_PATH, "application/json; charset=UTF-8", body);
 		
 		assertStatus(400, response);
 		assertContentEquals("Email already exists", response);
 	}
 	
 	@Test
 	public void testCreateWithEmailValidationError() {
 		testCreate();
 		
 		String body = "{\"email\":\"axj7\","
 			+ "\"firstName\":\"Alex\","
 			+ "\"lastName\":\"Jarvis\","
 			+ "\"password\":\"password\","
 			+ "\"mobileNumber\":\"123\"}";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 
 		response = POST(request, BASE_CONTROLLER_PATH, "application/json; charset=UTF-8", body);
 		
 		assertStatus(400, response);
 		//assertContentEquals("Email already exists", response);
 	}
 	
 	@Test
 	public void testShowAuthUser() {
 		response = GET(BASE_CONTROLLER_PATH + "/" + user1.id + user1Query);
 		assertIsOk(response);
 		assertContentType("application/json", response);
 		
 		// Contains the connections to other users
 		assertTrue(response.out.toString().contains("connections"));
 	}
 	
 	@Test
 	public void testShowUser() {
 		response = GET(BASE_CONTROLLER_PATH + "/" + user2.id + user1Query);
 		assertIsOk(response);
 		assertContentType("application/json", response);
 		
 		// Does not contain the connections to other users
 		assertFalse(response.out.toString().contains("connections"));
 	}
 	
 	@Test
 	public void testShowNonUser() {
 		response = GET(request, BASE_CONTROLLER_PATH + "/" + "9999999999" + user1Query);
 		assertIsNotFound(response);
 		assertContentType("application/json", response);
 	}
 	
 	@Test
 	public void testShowAuthUserWithEmail() {
 		response = GET(BASE_CONTROLLER_PATH + "/" + user1.email + user1Query);
 		assertIsOk(response);
 		assertContentType("application/json", response);
 		
 		// Contains the connections to other users
 		assertTrue(response.out.toString().contains("connections"));
 	}
 	
 	@Test
 	public void testShowUserWithEmail() {
 		response = GET(BASE_CONTROLLER_PATH + "/" + user2.email + user1Query);
 		assertIsOk(response);
 		assertContentType("application/json", response);
 		
 		// Does not contain the connections to other users
 		assertFalse(response.out.toString().contains("connections"));
 	}
 	
 	@Test
 	public void testUpdateAuthUser() {
 		
 		String body = "{\"email\":\"axj7@aber.ac.uk\","
 			+ "\"firstName\":\"Alex\","
 			+ "\"lastName\":\"Jarvis\","
 			+ "\"password\":\"password\","
 			+ "\"mobileNumber\":\"123\"}";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 
 		response = PUT(request, BASE_CONTROLLER_PATH + "/" + user1.id + user1Query, "application/json; charset=UTF-8", body);
 		
 		assertIsOk(response);
 	}
 	
 	@Test
 	public void testUpdateNonAuthUser() {
 		String body = "{\"email\":\"axj7@aber.ac.uk\","
 			+ "\"firstName\":\"Alex\","
 			+ "\"lastName\":\"Jarvis\","
 			+ "\"password\":\"password\","
 			+ "\"mobileNumber\":\"123\"}";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 
 		response = PUT(request, BASE_CONTROLLER_PATH + "/" + user2.id + user1Query, "application/json; charset=UTF-8", body);
 		
 		assertStatus(400, response);
 	}
 	
 	@Test
 	public void testUpdateClashes() {
 		String body = "{\"email\":\"bob2@gmail.com\","
 			+ "\"firstName\":\"Alex\","
 			+ "\"lastName\":\"Jarvis\","
 			+ "\"password\":\"password\","
 			+ "\"mobileNumber\":\"123\"}";
 		
 		Http.Request request = newRequest();
 		request.params.put("body", body);
 
 		response = PUT(request, BASE_CONTROLLER_PATH + "/" + user1.id + user1Query, "application/json; charset=UTF-8", body);
 		
 		assertStatus(400, response);
 		assertContentEquals("Email already exists", response);
 	}
 	
 	@Test
 	public void testDeleteUserWithId() {
 		response = DELETE(BASE_CONTROLLER_PATH + "/" + user2.id + user1Query);
 		assertIsOk(response);
 	}
 	
 	@Test
 	public void testDeleteUserWithEmail() {
 		response = DELETE(BASE_CONTROLLER_PATH + "/" + user2.email + user1Query);
 		assertIsOk(response);
 	}
 	
 	@Test
 	public void testDeleteAuthUser() {
 		response = DELETE(BASE_CONTROLLER_PATH + "/" + user1.id + user1Query);
 		assertStatus(404, response);
 	}
 	
 	@Test
 	public void testDeleteNonConnectedUser() {
 		response = DELETE(BASE_CONTROLLER_PATH + "/" + user3.id + user1Query);
 		assertStatus(400, response);
 	}
 	
 	@Test
 	public void testAddUserRequest() {
 		UserConnectionHelper.removeUserConnection(user1, user2);
 		response = POST(BASE_CONTROLLER_PATH + "/" + user2.email + "/add/" + user1Query);
 		assertIsOk(response);
 	}
 	
 	@Test
 	public void testAddUserRequestWithAuthUser() {
 		response = POST(BASE_CONTROLLER_PATH + "/" + user1.email + "/add/" + user1Query);
 		assertStatus(400, response);
 	}
 	
 	@Test
 	public void testAcceptUserRequest() {
 		// Remove the existing user connection and add the request
 		UserConnectionHelper.removeUserConnection(user1, user2);
 		response = POST(BASE_CONTROLLER_PATH + "/" + user2.email + "/add/" + user1Query);
 		assertIsOk(response);
 		
 		// Connect with 2nd user and accept the user connection request
 		response = POST(BASE_CONTROLLER_PATH + "/" + user1.email + "/accept/" + user2Query);
 		assertIsOk(response);
 		
 		// Try to accept the user connection request again
 		response = POST(BASE_CONTROLLER_PATH + "/" + user1.email + "/accept/" + user2Query);
 		assertStatus(400, response);
 	}
 	
 	@Test
 	public void testAcceptUserRequestNotExist() {
 		response = POST(BASE_CONTROLLER_PATH + "/" + user1.email + "/accept/" + user2Query);
 		assertStatus(400, response);
 	}
 	
 	@Test
 	public void testDeclineUserRequest() {
 		// Remove the existing user connection and add the request
 		UserConnectionHelper.removeUserConnection(user1, user2);
 		response = POST(BASE_CONTROLLER_PATH + "/" + user2.email + "/add/" + user1Query);
 		assertIsOk(response);
 		
 		// Connect with 2nd user and decline the user connection request
 		response = POST(BASE_CONTROLLER_PATH + "/" + user1.email + "/decline/" + user2Query);
 		assertIsOk(response);
 		
 		// Try to decline the user connection request again
 		response = POST(BASE_CONTROLLER_PATH + "/" + user1.email + "/decline/" + user2Query);
 		assertStatus(400, response);
 	}
 	
 	@Test
 	public void testDeclineUserRequestNotExist() {
 		response = POST(BASE_CONTROLLER_PATH + "/" + user1.email + "/decline/" + user2Query);
 		assertStatus(400, response);
 	}
 	
 	@Test
 	public void testSearchUserByFirstName() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "Bob");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserByFirstNameLowerCase() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "Bob");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserByFirstNameUpperCase() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "BOB");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserByLastName() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "Smith");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserByLastNameLowerCase() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "smith");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserByLastNameUpperCase() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "SMITH");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserByActualName() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "Bob Smith");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserByActualNameLowerCase() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "bob smith");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserByActualNameUpperCase() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "BOB SMITH");
 		assertIsOk(response);
 		assertContentMatch("(.)*firstName\":\"Bob\",\"lastName\":\"Smith\"(.)*", response);
 	}
 	
 	@Test
 	public void testSearchUserNotExist() {
 		response = GET("/search" + BASE_CONTROLLER_PATH + "/" + user1Query + "&query=" + "William");
 		assertStatus(404, response);
 	}
 }
