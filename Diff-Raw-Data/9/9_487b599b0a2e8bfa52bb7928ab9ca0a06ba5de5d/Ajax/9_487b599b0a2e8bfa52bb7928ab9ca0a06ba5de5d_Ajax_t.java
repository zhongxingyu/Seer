 package cubrikproject.tud.likelines.util;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 /**
  * Simple utilities class for AJAX related work
  * 
  * @author Raynor Vliegendhart
  */
 public class Ajax {
 
 	/**
 	 * Retrieves a JSON value from the given URL.
 	 * 
 	 * @param url
 	 *            URL to JSON resource
 	 * @return JSON element
 	 * @throws MalformedURLException
 	 *             When the given URL is malformed
 	 * @throws IOException
 	 *             When the resource cannot be retrieved
 	 * @see #getJSON(URL)
 	 */
 	public static JsonElement getJSON(String url) throws MalformedURLException,
 			IOException {
 		return getJSON(new URL(url));
 	}
 
 	/**
 	 * Retrieves a JSON value from the given URL.
 	 * 
 	 * @param url
 	 *            URL to JSON resource
 	 * @return JSON element
 	 * @throws IOException
 	 *             When the resource cannot be retrieved
 	 */
 	public static JsonElement getJSON(URL url) throws IOException {
 		final JsonParser jp = new JsonParser();
 		
 		final URLConnection conn = url.openConnection();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
 		
 		return jp.parse(reader);
 	}
 	
 	
 	/**
 	 * Posts a JSON value to the given URL.
 	 * 
 	 * @param url
 	 *            URL to post JSON value to
 	 * @param data
 	 *            JSON value to be posted
 	 * @return JSON response
 	 * @throws MalformedURLException
 	 *             When the given URL is malformed
 	 * @throws IOException
 	 *             When the JSON value cannot be posted
 	 * @see #postJSON(URL)
 	 */
 	public static JsonElement postJSON(String url, Object data) throws MalformedURLException,
 			IOException {
 		return postJSON(new URL(url), data);
 	}
 
 	/**
 	 * Posts a JSON value to the given URL.
 	 * 
 	 * @param url
 	 *            URL to post JSON resource to
 	 * @param data
 	 *            JSON value to be posted
 	 * @return JSON response
 	 * @throws IOException
 	 *             When the JSON value cannot be posted
 	 */
 	public static JsonElement postJSON(URL url, Object data) throws IOException {
 		JsonParser jp = new JsonParser();
 		final Gson gson = new Gson();
 		
 		final String json = gson.toJson(data);
 		final byte[] jsonBytes = json.getBytes("utf-8");
 		
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		conn.setDoOutput(true);
 		conn.setDoInput(true);
 		conn.setRequestMethod("POST");
 		conn.setRequestProperty("Content-Type", "application/json"); 
 		conn.setRequestProperty("charset", "utf-8");
 		conn.setRequestProperty("Content-Length", Integer.toString(jsonBytes.length));
 		
 		DataOutputStream wr = new DataOutputStream(conn.getOutputStream ());
 		wr.write(jsonBytes, 0, jsonBytes.length);
 		wr.flush();
 		wr.close();
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				conn.getInputStream()));
 		
 		return jp.parse(reader);
 	}
 }
