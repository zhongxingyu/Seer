 package com.mmessage.dcu.sylvain.tasks;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
 
 import android.os.AsyncTask;
 
 import com.mmessage.dcu.sylvain.controler.MainActivityController;
 import com.mmessage.dcu.sylvain.interfaces.OnTaskCompleted;
 import com.mmessage.dcu.sylvain.model.Commands;
 import com.mmessage.dcu.sylvain.model.TaskMessage;
 
 public class PostRESTTask extends AsyncTask<String, Void, String> {
 
 	private OnTaskCompleted _listener = null;
 	private List<NameValuePair> _parameters;
 	private boolean _isAuthentificationNeeded;
 	private Commands _command;
 	private int _httpCode = 0;
 
 	public PostRESTTask(OnTaskCompleted parOnTaskCompleted,
 			boolean parIsAuthentificationNeeded, Commands parCommandType,
 			List<NameValuePair> parNameValuePairs) {
 		_listener = parOnTaskCompleted;
 		_parameters = parNameValuePairs;
 		_isAuthentificationNeeded = parIsAuthentificationNeeded;
 		_command = parCommandType;
 	}
 
 	@Override
 	protected String doInBackground(String... params) {
 
 		String locUrl = params[0];
 		String locResponseString = "";
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(locUrl);
 		httppost.addHeader("Accept", "application/json");
 		if (_isAuthentificationNeeded) {
 			httppost.addHeader("Authorization", "Basic "
 					+ MainActivityController.getAuthentification());
 		}
 		try {
			httppost.setEntity(new UrlEncodedFormEntity(_parameters, HTTP.UTF_8));
 			// Execute HTTP Post Request
 			HttpResponse locHttpResponse = httpclient.execute(httppost);
 			_httpCode = locHttpResponse.getStatusLine().getStatusCode();
 
 			if (_httpCode == HttpStatus.SC_OK) {
 				ByteArrayOutputStream locOut = new ByteArrayOutputStream();
 				locHttpResponse.getEntity().writeTo(locOut);
 				locOut.close();
 				locResponseString = locOut.toString();
 			} else {
 				// Closes the connection.
 				locHttpResponse.getEntity().getContent().close();
 				throw new IOException(locHttpResponse.getStatusLine()
 						.getReasonPhrase());
 			}
 
 		} catch (ClientProtocolException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		}
 		return locResponseString;
 
 	}
 
 	@Override
 	protected void onPostExecute(String parString) {
 		TaskMessage locTaskMessage = new TaskMessage(_command, _httpCode,
 				parString);
 		_listener.onTaskCompleted(locTaskMessage);
 	}
 
 }
