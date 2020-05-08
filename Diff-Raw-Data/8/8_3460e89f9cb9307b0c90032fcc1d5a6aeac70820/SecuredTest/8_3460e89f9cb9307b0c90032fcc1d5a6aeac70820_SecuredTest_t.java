 package application;
 
 import java.net.HttpURLConnection;
 
 import org.junit.Test;
 
 import play.mvc.Http.Response;
 import play.test.FunctionalTest;
 
 public class SecuredTest extends FunctionalTest {
 
 	@Test
 	public void testLoginRedirect(){
 		Response response = GET("/SecuredController/noCheck");
 		assertNeedsLogin(response);
 	}
 
 	private void assertNeedsLogin(Response response) {
 		assertStatus(HttpURLConnection.HTTP_MOVED_TEMP, response);
		assertHeaderEquals("Location", "/login", response);
 	}
 
 	@Test
 	public void testLoginOK(){
 		login("user", false);
 		Response response = GET("/SecuredController/noCheck");
 		assertIsOk(response);
 	}
 
 	@Test
 	public void testLoginBadUser(){
 		login("baduser", true);
 		Response response = GET("/SecuredController/noCheck");
 		assertNeedsLogin(response);
 	}
 
 	@Test
 	public void testPermissionDenied(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionDenied");
 		assertStatus(HttpURLConnection.HTTP_FORBIDDEN, response);
 	}
 
 	@Test
 	public void testPermissionDeniedOnArg(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionDeniedOnArg?foo=bar");
 		assertStatus(HttpURLConnection.HTTP_FORBIDDEN, response);
 	}
 
 	@Test
 	public void testPermissionGrantedOnArg(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionGrantedOnArg?foo=bar");
 		assertIsOk(response);
 	}
 
 	@Test
 	public void testPermissionGrantedOnArgValue(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionOnArgValue?foo=ok");
 		assertIsOk(response);
 	}
 
 	@Test
 	public void testPermissionDeniedOnArgValue(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionOnArgValue?foo=ko");
 		assertStatus(HttpURLConnection.HTTP_FORBIDDEN, response);
 	}
 
 	@Test
 	public void testPermissionGrantedOnBeanArgValue(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionOnArgBeanValue?user.firstName=Stef&user.lastName=bar");
 		assertIsOk(response);
 	}
 
 	@Test
 	public void testPermissionDeniedOnBeanArgValue(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionOnArgBeanValue?user.firstName=Joe&user.lastName=bar");
 		assertStatus(HttpURLConnection.HTTP_FORBIDDEN, response);
 	}
 
 	@Test
 	public void testPermissionDeniedOnRole(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionDeniedOnRole");
 		assertStatus(HttpURLConnection.HTTP_FORBIDDEN, response);
 	}
 
 	@Test
 	public void testPermissionGrantedOnRole(){
 		login("user", false);
 		Response response = GET("/SecuredController/permissionGrantedOnRole");
 		assertIsOk(response);
 	}
 
 	private void login(String user, boolean expectFailure) {
 		Response response = POST("/login?username="+user+"&password=pass");
 		assertStatus(HttpURLConnection.HTTP_MOVED_TEMP, response);
 		if(expectFailure)
			assertHeaderEquals("Location", "/login", response);
 		else
			assertHeaderEquals("Location", "/", response);
 	}
 }
