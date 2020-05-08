 package de.binaervarianz.sendtowebdav;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.HttpException;
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 public class SendToServer extends Activity {
 	private final String TAG = this.getClass().getName();
 	
 	private WebDAVhandler httpHandler;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// read preferences
 		SharedPreferences settings = getSharedPreferences(ConfigWebDAV.PREFS_PRIVATE, Context.MODE_PRIVATE);
 		String serverURI = settings.getString(ConfigWebDAV.KEY_SERVER_URI, "https://");
 		boolean trustAllSSLCerts = settings.getBoolean(ConfigWebDAV.KEY_TRUSTALLSSL, false);
 		String user = settings.getString(ConfigWebDAV.KEY_USERNAME, "");
 		String pass = settings.getString(ConfigWebDAV.KEY_PASSWORD, "");
 
 		try {
 			new URI(serverURI);
 		} catch (URISyntaxException e) {
 			Log.e(TAG, "URISyntaxException: "+e);
 			// TODO: user error message (maybe also do this check when testing/saving) -- is inherently done!?
 			// TODO: alert dialog notifying users of broken url and redirecting them to the config app
 			// "Please provide a valid Server URI in the Config App first"
 			return;
 		}
 		
 		httpHandler = new WebDAVhandler(serverURI, user, pass);
 		httpHandler.setTrustAllSSLCerts(trustAllSSLCerts);
 
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 
 		if (intent.getAction().equals(Intent.ACTION_SEND) && extras != null) {
 			String url = "";
 			if (extras.containsKey(Intent.EXTRA_TEXT)) {
 				url += extras.getString(Intent.EXTRA_TEXT);
 			}
 
 			try {
				SimpleDateFormat dateformater = new SimpleDateFormat("yyyyMMddHHmmss"); 
				httpHandler.putFile("URL-"+dateformater.format(new Date())+".txt", "", url);
 			} catch (ClientProtocolException e) {
 				Toast.makeText(this, this.getString(R.string.app_name) + ": ClientProtocolException: "+e, Toast.LENGTH_LONG).show();
 				Log.e(TAG, "ClientProtocolException: "+e);
 				return;
 			} catch (IOException e) {
 				Toast.makeText(this, this.getString(R.string.app_name) + ": IOException: "+e, Toast.LENGTH_LONG).show();
 				Log.e(TAG, "IOException: "+e);
 				return;
 			} catch (IllegalArgumentException e) {
 				Toast.makeText(this, this.getString(R.string.app_name) + ": IllegalArgumentException: "+e, Toast.LENGTH_LONG).show();
 				Log.e(TAG, "IllegalArgumentException: "+e);
 				return;
 			} catch (HttpException e) {
 				Toast.makeText(this, this.getString(R.string.app_name) + ": HttpException: "+e, Toast.LENGTH_LONG).show();
 				Log.e(TAG, "HttpException: "+e);
 				return;
 			}
 			
 			Toast.makeText(this, R.string.data_send, Toast.LENGTH_SHORT).show();
 		}
 	}
 }
