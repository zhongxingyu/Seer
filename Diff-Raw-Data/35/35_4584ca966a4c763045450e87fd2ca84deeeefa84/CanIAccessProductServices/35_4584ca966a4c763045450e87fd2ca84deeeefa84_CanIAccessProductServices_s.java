 package no.miles;
 
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 /**
  * FitNesse fixture.
  */
 public class CanIAccessProductServices {
 
 	public CanIAccessProductServices() {
 
 	}
 
 	public int listProducts() throws IOException {
 		HttpClient client = new HttpClient();
 
 		return client.executeMethod(new GetMethod(
 				"http://localhost:8080/restest/product"));
 	}
 
 }
