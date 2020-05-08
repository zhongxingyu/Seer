 package com.mymed.tests.unit.handler;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.google.gson.JsonObject;
 import com.mymed.controller.core.requesthandler.ProfileRequestHandler;
 
 /**
  * Test class for the {@link ProfileRequestHandler}.
  * 
  * @author Milo Casagrande
  * 
  */
 public class ProfileRequestHandlerTest extends GeneralHandlerTest {
 
   private static final String HANDLER_NAME = "ProfileRequestHandler";
 
   @BeforeClass
   public static void setUpOnce() {
     path = TestUtils.createPath(HANDLER_NAME);
   }
 
   /**
    * Get a non existent user from the database.
    * <p>
    * Check that the response code is '404', and that the JSON format is valid
    * 
    * @throws ClientProtocolException
    * @throws IOException
    * @throws URISyntaxException
    */
   @Test
   public void readWrongUserTest() throws IOException, URISyntaxException {
     TestUtils.addParameter(params, "code", READ);
     TestUtils.addParameter(params, "id", "wrong.email@example.org");
 
     final String query = TestUtils.createQueryParams(params);
     final URI uri = TestUtils.createUri(path, query);
 
     final HttpGet getRequest = new HttpGet(uri);
     final HttpResponse response = client.execute(getRequest);
 
     BackendAssert.assertResponseCodeIs(response, 404);
     BackendAssert.assertIsValidJson(response);
   }
 
   /**
    * Create a new user in the database.
    * <p>
    * Check that the response code is '200', and that the JSON format is a valid
    * 'user' JSON format
    * 
    * @throws URISyntaxException
    * @throws ClientProtocolException
    * @throws IOException
    */
   @Test
   public void createTest() throws URISyntaxException, ClientProtocolException, IOException {
     final JsonObject user = TestUtils.createUser();
 
     TestUtils.addParameter(params, "code", CREATE);
     TestUtils.addParameter(params, "user", user.toString());
 
     final String query = TestUtils.createQueryParams(params);
     final URI uri = TestUtils.createUri(path, query);
 
     final HttpPost postRequest = new HttpPost(uri);
     final HttpResponse response = client.execute(postRequest);
 
     BackendAssert.assertResponseCodeIs(response, 200);
     BackendAssert.assertIsValidUserJson(response);
   }
 
   /**
    * Update a user in the database.
    * <p>
    * Check that the response code is '200', and that the JSON format is a valid
    * 'user' JSON format
    * 
    * @throws URISyntaxException
    * @throws ClientProtocolException
    * @throws IOException
    */
   @Test
   public void updateTest() throws URISyntaxException, ClientProtocolException, IOException {
     TestUtils.addParameter(params, "code", UPDATE);
 
     final JsonObject user = TestUtils.createUser();
     user.addProperty("gender", "female");
 
     TestUtils.addParameter(params, "user", user.toString());
 
     final String query = TestUtils.createQueryParams(params);
     final URI uri = TestUtils.createUri(path, query);
 
     final HttpPost postRequest = new HttpPost(uri);
     final HttpResponse response = client.execute(postRequest);
 
     BackendAssert.assertResponseCodeIs(response, 200);
     BackendAssert.assertIsValidUserJson(response);
   }
 
   /**
    * Delete a user in the database.
    * <p>
    * Check that the response code is '200', and that the JSON format is valid
    * 
    * @throws URISyntaxException
    * @throws ClientProtocolException
    * @throws IOException
    */
   @Test
   public void deleteTest() throws URISyntaxException, IOException {
     TestUtils.addParameter(params, "code", DELETE);
    TestUtils.addParameter(params, "id", TestUtils.MYMED_ID);
 
     final String query = TestUtils.createQueryParams(params);
     final URI uri = TestUtils.createUri(path, query);
 
     final HttpGet getRequest = new HttpGet(uri);
     final HttpResponse response = client.execute(getRequest);
 
     BackendAssert.assertResponseCodeIs(response, 200);
     BackendAssert.assertIsValidJson(response);
   }
 
   /**
    * Read the database for the deleted user.
    * <p>
    * Check that the response code is '404', and that the JSON format is valid
    * 
    * @throws URISyntaxException
    * @throws ClientProtocolException
    * @throws IOException
    */
   @Test
   public void getDeletedUserTest() throws URISyntaxException, ClientProtocolException, IOException {
     TestUtils.addParameter(params, "code", READ);
    TestUtils.addParameter(params, "id", TestUtils.MYMED_ID);
 
     final String query = TestUtils.createQueryParams(params);
     final URI uri = TestUtils.createUri(path, query);
 
     final HttpGet getRequest = new HttpGet(uri);
     final HttpResponse response = client.execute(getRequest);
 
     BackendAssert.assertResponseCodeIs(response, 404);
     BackendAssert.assertIsValidJson(response);
   }
 }
