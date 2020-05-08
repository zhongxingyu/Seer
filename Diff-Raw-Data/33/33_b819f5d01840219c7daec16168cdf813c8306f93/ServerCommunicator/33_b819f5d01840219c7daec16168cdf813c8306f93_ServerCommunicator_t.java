 package illinois.sweng.sctracker;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
 
 import android.util.Log;
 
 public class ServerCommunicator {
 	private final String TAG;
 	DelegateActivity mDelegate;
 	
 	/**
 	 * Creates a new ServerCommunicator with the given context
 	 * @param delegate Context of the owner of the ServerCommunicator
 	 * @param delegateTag String identifying the owner of this ServerCommunicator
 	 */
 	public ServerCommunicator(DelegateActivity delegate, String delegateTag) {
 		mDelegate = delegate;
 		TAG = "sc2TrackerServerCommunicator-" + delegateTag;
 	}
 	
 	/**
 	 * Sends a request to the server to create a new user account with the given
 	 * username and password
 	 * @param username Username for the new user account
 	 * @param password Password for the new user account
 	 */
 	public void sendAccountCreationRequest(String username, String password) {
 		String urlString = buildAccountCreationURL(username, password);
 		
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(urlString);
 		
 		try {
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 			pairs.add(new BasicNameValuePair("username", username));
 			pairs.add(new BasicNameValuePair("password", password));
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 			
 			HttpResponse response = httpClient.execute(httpPost);
 			readHttpResponse(response);
 			
 		} catch (ClientProtocolException e) {
 			String message = mDelegate.getResources().getString(R.string.serverProtocolErrorMessage);
 			mDelegate.handleServerError(message);
 			Log.e(TAG, message, e);
			e.printStackTrace();
 	    } catch (IOException e) {
 	    	String message = mDelegate.getResources().getString(R.string.serverIOExceptionMessage);
 	    	mDelegate.handleServerError(message);
 	        Log.e(TAG, message, e);
	        e.printStackTrace();
	    }  catch (JSONException e) {
			String message = mDelegate.getResources().getString(R.string.serverJSONError);
	    	mDelegate.handleServerError(message);
	        Log.e(TAG, message, e);
	        e.printStackTrace();
		}
 	}
 
 	/**
 	 * Read the httpResponse and display the appropriate success/failure notification
 	 * @param httpResponse InputStream returned from the web server
	 * @throws IOException 
	 * @throws JSONException 
 	 */
	private void readHttpResponse(HttpResponse httpResponse) throws IOException, JSONException {
 		StatusLine statusLine = httpResponse.getStatusLine();
 		int statusCode = statusLine.getStatusCode();
 		
 		if (statusCode == HttpStatus.SC_OK) {
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream in = httpEntity.getContent();	
			
			// TODO Read the JSON
			JSONObject json = new JSONObject(in.toString());
			
 			List<NameValuePair> values = new ArrayList<NameValuePair>();
			
 			mDelegate.handleServerResponse(values);
 		} else {
 			// TODO Process the error
 			String message = "An error occurred on the server";
 			mDelegate.handleServerError(message);
 		}
 	}
 
 	/**
 	 * Given a username and a password, builds the appropriate url to send a GET to
 	 * in order to create a new account
 	 * @param username User's new account email address/username
 	 * @param password User's password
 	 * @return String representing the URL to generate a new user account
 	 */
 	private String buildAccountCreationURL(String username, String password) {
 		CharSequence baseURL = mDelegate.getResources().getText(R.string.serverURL);
 		CharSequence registerURL = mDelegate.getResources().getText(R.string.serverRegisterURL);
 		
 		StringBuilder sb = new StringBuilder("http://");
 		sb.append(baseURL);
 		sb.append(registerURL);
 		String urlString = sb.toString();
 		
 		return urlString;
 	}
 }
