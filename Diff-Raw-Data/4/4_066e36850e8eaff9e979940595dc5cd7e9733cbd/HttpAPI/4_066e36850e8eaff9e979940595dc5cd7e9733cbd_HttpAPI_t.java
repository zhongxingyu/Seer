 package fi.action.wpoint;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONObject;
 
 public class HttpAPI {
 	
 	HttpClient client;
 	
 	public HttpAPI() {
 		client = new DefaultHttpClient();
 		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
 	}
 
 	public String get(String url) throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(url);
 		String response = responseToString(client.execute(get));
 		return response;
 	}
 	
 	public String post(String url, JSONObject json) throws ClientProtocolException, IOException {
 		HttpPost post = new HttpPost(url);
 
 		HttpParams params = new BasicHttpParams();
 		params.setParameter("data", json.toString());
 		post.setHeader(HTTP.CONTENT_TYPE, "application/json");
 		post.setParams(params);
 		
 		String response = responseToString(client.execute(post));
 		return response;
 	}
 	
 	private String responseToString(HttpResponse response) {
 		if(response == null)
 			return null;
 		
 		InputStream input = null;		
 		BufferedReader reader;
 		StringBuilder builder = new StringBuilder();
 		try {
 			input = response.getEntity().getContent();
 			reader = new BufferedReader(new InputStreamReader(input));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				builder.append(line + "\n");
 			}
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if(input != null)
 					input.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} 
 		}
 		
 		return builder.toString();
 	}
 }
