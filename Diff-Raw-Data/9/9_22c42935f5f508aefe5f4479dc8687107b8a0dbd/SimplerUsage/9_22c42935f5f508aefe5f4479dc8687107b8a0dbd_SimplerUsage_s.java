 package playground.jetty_httpclient;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.client.api.ContentResponse;
 
 public class SimplerUsage {
 
 	public static void main(String[] args) throws Exception {
 
 		// Create HttpClient.
 		HttpClient httpClient = new HttpClient();
 		/*
 		 * Trust Self-Signed Certificate setting.
 		 * 
 		 * boolean trustAll = true; 
 		 * HttpClient httpClient = new HttpClient(new SslContextFactory(trustAll));
 		 */
 
 		// Configure HttpClient here.
 		httpClient.setMaxConnectionsPerDestination(10);
 		httpClient.setMaxRequestsQueuedPerDestination(10);
 		httpClient.setConnectTimeout(TimeUnit.SECONDS.toMillis(3));
 
 		// Starting HttpClient. This method throws Exception.
 		httpClient.start();
 
 		try {
 			// GET and print response status, headers, body.
 			ContentResponse response = httpClient.GET("http://www.example.com");
 
 			System.out.println(response.getStatus());
 			System.out.println("----------");
 			System.out.println(response.getHeaders());
 			System.out.println("----------");
 			System.out.println(response.getContentAsString());
 			System.out.println("----------");
 
 		} catch (InterruptedException | ExecutionException | TimeoutException e) {
 			e.printStackTrace();
 		} finally {
			// destroying HttpClient. Closing resources (pooling connection,
			// threads, etc..)
			httpClient.destroy();

 			// Dumping state of HttpClient.
 			System.out.println(httpClient.dump());
 		}
 	}
 }
