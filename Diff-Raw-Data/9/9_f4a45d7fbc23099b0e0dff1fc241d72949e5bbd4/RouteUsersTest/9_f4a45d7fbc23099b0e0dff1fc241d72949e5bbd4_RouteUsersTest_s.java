 import models.User;
 import org.junit.Before;
 import org.junit.Test;
 import play.mvc.Http;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 import utils.FmtUtil;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Test case for user registration
  */
 public class RouteUsersTest extends FunctionalTest {
 
     /**
      * Do a simple POST with the given values
      *
      * @param url    Internal URL
      * @param params Params
      * @return Response
      */
     private static Http.Response POST(String url, Map<String, String> params) {
         return POST(url, "application/x-www-form-urlencoded",
                 FmtUtil.encode(params));
     }
 
     /**
      * Delete all fixtures and import test fixture before tests run
      */
     @Before
     public void setUp() {
         Fixtures.deleteAll();
         Fixtures.load("fixtures-test.yml");
     }
 
     /**
      * Test /users/register
      */
     @Test
     public void testRouteRegister() {
         final Map<String, String> params = new HashMap<String, String>() {{
             put("username", "test2");
             put("registrationId", "some_random_id");
         }};
         final Http.Response response = POST("/users/register", params);
         assertIsOk(response);
         assertTrue(User.count("username", "test2") == 1);
     }
 
     /**
      * Test /users/register
      */
     @Test
     public void testRouteRegisterExisting() {
         final Map<String, String> params = new HashMap<String, String>() {{
             put("username", "test");
             put("registrationId", "some_other_random_id");
         }};
         final User existingUser = User.find("username", "test").first();
         assertNotNull(existingUser);
         final String existingDeviceId = existingUser.deviceId;
         assertEquals(existingDeviceId, "some_random_id");
         final Http.Response response = POST("/users/register", params);
         assertIsOk(response);
        final User updatedUser = User.find("username", "test").first();
         assertEquals(updatedUser.deviceId, "some_other_random_id");
     }
 }
