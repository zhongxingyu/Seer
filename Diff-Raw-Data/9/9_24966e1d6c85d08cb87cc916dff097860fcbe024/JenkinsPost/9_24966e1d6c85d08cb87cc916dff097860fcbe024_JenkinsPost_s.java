 package org.jenkins.plugins.jenkinspost;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import net.sf.json.JSONObject;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.kohsuke.stapler.StaplerRequest;
 
 import hudson.Plugin;
 import hudson.util.ListBoxModel;
 
 public class JenkinsPost extends Plugin {
 	public final static Logger LOG = Logger.getLogger(JenkinsPost.class.getName());
 	private static HttpClient httpclient = new DefaultHttpClient();
 	private static boolean sendGlobalPosts; // send posts globally 
 	private static String globalPostURL; // global URL to post to
 	private static String globalPostContentType; // content type to send the request as 
 	private static String globalPostString; // String to post to the globalPostURL
 	
 	public void start() throws IOException {
 		LOG.info("Starting JenkinsPOST plugin...");
 		load();
 	}
 
 	/*
 	 * The format parsing here is strongly tied with the config.jelly file under 
 	 * the corresponding path. The form is returned as a json object through 
 	 * formData.
 	 */
 	@Override
 	public void configure(StaplerRequest req, JSONObject formData) throws IOException {
 		globalPostURL = formData.optString("globalPostURL");
 		if(formData.has("sendGlobalPosts")) {
 			sendGlobalPosts = true;
 			globalPostURL = formData.getJSONObject("sendGlobalPosts").getString("globalPostURL");
 			globalPostContentType = formData.getJSONObject("sendGlobalPosts").getString("globalPostContentType");
			globalPostString = formData.getJSONObject("sendGlobalPosts").getString("globalPostString");
 			LOG.info(globalPostURL);
 			LOG.info(globalPostContentType);
 			LOG.info(globalPostString);
 		} else {
 			sendGlobalPosts = false;
 		}
 		save();
 	}
 	
 	public static boolean getSendGlobalPosts() { return sendGlobalPosts; }
 	public static String getGlobalPostURL() { return globalPostURL; }
 	public static String getGlobalPostContentType() { return globalPostContentType; }
 	public static String getGlobalPostString() { return globalPostString; }
 	
 	public static void sendPost(String postString) {
 		sendPost(globalPostURL, globalPostContentType, postString);
 	}
 	
 	public static ListBoxModel doFillGlobalPostContentTypeItems() {
 		ListBoxModel items = new ListBoxModel();
 		items.add("Javascript", "application/javascript");
 		items.add("Json", "application/json");
 		items.add("text", "text/plain");
 		items.add("XML", "application/xml");
 		return items;
 	}
 	
 	public static void sendPost(String postURL, String contentType, String postString) {
 		try {
 			HttpPost httppost = new HttpPost(postURL);
 			httppost.setEntity(new StringEntity(postString));
 			httppost.setHeader("Content-type", contentType);
 			httpclient.execute(httppost);
 			httppost.releaseConnection();
 		} catch (UnsupportedEncodingException e) {
 			// unsupported encoding for httppost
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// httpclient unable to execute
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
