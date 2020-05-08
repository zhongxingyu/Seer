 package starbucks.service.resteasyjackson;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpOptions;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.junit.Test;
 
 import starbucks.service.servlet.StarbucksService;
 
 public class StarbucksServiceRestEasyTest {
 
 	private static final String HTTP_CODE_100_CONTINUE = "100-Continue";
 	StarbucksService starbucksService;
 	HttpClient client;
 	HttpPost post;
 	HttpOptions options;
 	HttpPut put;
 	
 	@Test
 	public void updateOrderedCoffee() {
 		
 
 		try {
 			client = new DefaultHttpClient();
 
 			put = new HttpPut("http://localhost:8080/"
					+ "starbucks" + "/order/put" + "/1234");
 
 			StringEntity input = new StringEntity("{\"additions\":\"lactose\"}");
 			input.setContentType("application/json");			
 			put.setEntity(input);
 			
 			put.setHeader("Expect", HTTP_CODE_100_CONTINUE);
 
 			
 			HttpResponse response = client.execute(put);
 
 			// HANDLE RESPONSE
 			if (response.getStatusLine().getStatusCode() != 200) {
 				throw new RuntimeException("Failed : HTTP error code : "
 						+ response.getStatusLine().getStatusCode());
 			}
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					(response.getEntity().getContent())));
 
 			String output;
 			System.out.println("Output from Server .... \n");
 			while ((output = br.readLine()) != null) {
 				System.out.println(output);
 			}
 
 			client.getConnectionManager().shutdown();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		assert(true);
 	}
 
 	
 	@Test
 	public void checkOptionsOnOrderedCoffee() {
 		
 
 		try {
 			client = new DefaultHttpClient();
 
 			options = new HttpOptions("http://localhost:8080/"
					+ "starbucks" + "/order/options" + "/1234");
 
 			HttpResponse response = client.execute(options);
 
 			// HANDLE RESPONSE
 			if (response.getStatusLine().getStatusCode() != 200) {
 				throw new RuntimeException("Failed : HTTP error code : "
 						+ response.getStatusLine().getStatusCode());
 			}
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					(response.getEntity().getContent())));
 
 			String output;
 			System.out.println("Output from Server .... \n");
 			while ((output = br.readLine()) != null) {
 				System.out.println(output);
 			}
 
 			client.getConnectionManager().shutdown();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		assert(true);
 	}
 
 	
 	@Test
 	public void orderCupOfCoffeeFromRestEasyService() {
 
 		try {
 			client = new DefaultHttpClient();
 
 			post = new HttpPost("http://localhost:8080/"
					+ "starbucks" + "/order/post");
//			+ "Starbucks-1.0-SNAPSHOT" + "/order/post");
 
 			StringEntity input = new StringEntity("{\"drink\":\"machiato\",\"cost\":\"10\"}");
 			input.setContentType("application/json");
 			
 			post.setEntity(input);
 
 			HttpResponse response = client.execute(post);
 
 			// HANDLE RESPONSE
 			if (response.getStatusLine().getStatusCode() != 201) {
 				throw new RuntimeException("Failed : HTTP error code : "
 						+ response.getStatusLine().getStatusCode());
 			}
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					(response.getEntity().getContent())));
 
 			String output;
 			System.out.println("Output from Server .... \n");
 			while ((output = br.readLine()) != null) {
 				System.out.println(output);
 			}
 
 			client.getConnectionManager().shutdown();
 
 			assert (true);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
