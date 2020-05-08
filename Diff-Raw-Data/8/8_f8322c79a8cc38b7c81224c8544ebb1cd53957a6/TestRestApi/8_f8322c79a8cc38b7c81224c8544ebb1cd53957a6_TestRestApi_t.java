 package earth.xor;
 
 import static com.jayway.restassured.RestAssured.given;
 import static org.hamcrest.Matchers.equalTo;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
 
 import earth.xor.rest.SparkRestApi;
 
 public class TestRestApi {
 
     private SparkRestApi restapi;
     
     private String jsonTestString = "{\"title\":\"foo\"}"; 
     
     @Before
     public void setUpRestApi() {
 	restapi = new SparkRestApi();
 	restapi.launchServer();
     }
     
     @Test
     public void testAddingAUrlThroughTheRestApi() {
 	
 	RestAssured.port = 4567;
		
	given().body(jsonTestString).expect().contentType("application/json")
		.body("title", equalTo("foo")).when().post("/urls");
     }
     
     @After
     public void stopRestApi() {
 	restapi.stopServer();
     }
 }
