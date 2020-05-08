 package com.dimasco.rest.controller;
 
 import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
 import static org.hamcrest.Matchers.*;
 
 import org.junit.Assert;
 import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
 
 import com.dimasco.rest.domain.Foo;
 import com.jayway.restassured.http.ContentType;
 import com.jayway.restassured.response.Response;
 
 public class FooControllerTest {
 	
 	private static final String existedResourceUrl = "http://localhost:8080/RestSpringTest/foo";
 	
 	private static final String USERNAME = "dimas";
 	
 	private static final String PASSWORD = "dimas";
 	
	@Autowired
	private RestTemplate restTemplate;

 	@Test
 	public void whenInvalidPOSTIsSentToValidURIOfResource_thenAllowHeaderListsTheAllowedActions() {
 		//final String uriOfExistingResource = restTemplate
 		
 		//Response response = giv
 	}
 	
 	@Test
 	public void givenAuthenticatedByBasicAuth_whenAResourceIsCreated_then201IsReceived(){
 		Response response = given().auth().preemptive()
 				.basic(USERNAME, PASSWORD).contentType(ContentType.JSON).body(new Foo()).post(existedResourceUrl);
 		
 		Assert.assertThat(response.getStatusCode(), is(201));
 	}
 	
 	@Test
 	public void givenAuthenticatedByDigestAuth_whenAResourceIsCreated_then201IsReceived(){ 
 		Response response = given().auth()
 				.digest(USERNAME, PASSWORD).contentType(ContentType.JSON).body(new Foo()).post(existedResourceUrl);
 		
 		Assert.assertThat( response.getStatusCode(), is( 201 ) );
 	}
 	
 }
