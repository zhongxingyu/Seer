 package edu.ucla.cens.whatsnoisy;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.google.android.googlelogin.GoogleLoginServiceBlockingHelper;
 import com.google.android.googlelogin.GoogleLoginServiceConstants;
 import com.google.android.googlelogin.GoogleLoginServiceHelper;
 import com.google.android.googlelogin.GoogleLoginServiceNotFoundException;
 
 import edu.ucla.cens.whatsnoisy.data.LocationDatabase;
 import edu.ucla.cens.whatsnoisy.data.SampleDatabase;
 import edu.ucla.cens.whatsnoisy.services.LocationService;
 import edu.ucla.cens.whatsnoisy.services.LocationTrace;
 import edu.ucla.cens.whatsnoisy.services.LocationUpload;
 import edu.ucla.cens.whatsnoisy.services.SampleUpload;
 import edu.ucla.cens.whatsnoisy.Record;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences.Editor;
 
 public class whatsnoisy extends Activity {
 
 	// An arbitrary constant to pass to the GoogleLoginHelperService
 	private static final int GET_ACCOUNT_REQUEST = 1;
 	private static final int RECORD_FINISHED = 0;
 	private static final String TAG = "whatsnoisy";
 	SharedPreferences preferences;
 	private SampleDatabase sdb;
 	private LocationDatabase ldb;
 	private String authToken = "";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		preferences = this.getSharedPreferences(Settings.NAME, Activity.MODE_PRIVATE);
 
 		new AuthorizeTask().execute();
 	}
 
 	private void startServices() {
 		Intent service = new Intent();
 
 		service.setClass(this, LocationService.class);
 		startService(service);
 
 		service.setClass(this, LocationTrace.class);
 		startService(service);
 
 		service.setClass(this, LocationUpload.class);
 		startService(service);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		stopServices();
 	}
 
 	private void stopServices() {
 		Intent service = new Intent();
 
 		Log.d(TAG,"Stop location service");
 
 		service.setClass(this, LocationService.class);
 		stopService(service);
 
 		//		service.setClass(this, LocationTrace.class);
 		//		stopService(service);
 		//		
 		//		service.setClass(this, LocationUpload.class);
 		//		stopService(service);
 	}
 
 
 	private class AuthorizeTask extends AsyncTask<Void, Void, Boolean> {
 
 		protected Boolean doInBackground(Void... progress) {
 			authToken = getAuthToken();
 			Boolean result = authorize(authToken);
 			if(!result) {
 				try {
 					//try reseting the authtoken and trying again
 					GoogleLoginServiceBlockingHelper.invalidateAuthToken(whatsnoisy.this, authToken);
 				} catch (GoogleLoginServiceNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				result=authorize(authToken);
 			}
 
 			return result;
 		}
 
 		protected void onPostExecute(Boolean result) {
 			//save if we have been authenticated or not
 			preferences.edit().putBoolean("authenticated", result).commit();
 
 			if(result) {
 				Log.d(TAG, "authorized");
 
 				startServices();
 
 
 				sdb = new SampleDatabase(whatsnoisy.this);
 				sdb.openRead();
 				if(sdb.hasSamples()) {
 					Log.d(TAG,"sample database has samples");
 					Intent uploadService = new Intent(whatsnoisy.this, SampleUpload.class);
 					whatsnoisy.this.startService(uploadService);
 				}
 				sdb.close();
 
 				ldb = new LocationDatabase(whatsnoisy.this);
 				ldb.open();
 				if(ldb.hasSamples()) {
 					Log.d(TAG,"location database has locations");
 					Intent uploadService = new Intent(whatsnoisy.this, LocationUpload.class);
 					whatsnoisy.this.startService(uploadService);
 				}
 				ldb.close();
 
 				//start recording intent
 				Intent act = new Intent(whatsnoisy.this, Record.class);
 				whatsnoisy.this.startActivityForResult(act, RECORD_FINISHED);
 
 
 			} else {
 				AlertDialog alert = new AlertDialog.Builder(whatsnoisy.this)
 				.setTitle("Could Not Authenticate")
 				.setMessage("Whats Noisy Could Not authenticate with Google. Please make sure this phone is linked to a google account and try again. You can continue but samples will not be uploaded until you are authenticated.")					
 				.setPositiveButton("Continue", new OnClickListener(){
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//start recording intent
 						Intent act = new Intent(whatsnoisy.this, Record.class);
 						whatsnoisy.this.startActivityForResult(act, RECORD_FINISHED);
 					}})
 					.setNegativeButton(android.R.string.cancel, new OnClickListener(){
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							finish();
 						}})
 						.create();
 
 				alert.show();
 			}
 
 
 
 
 
 		}
 
 	}
 
 
 	protected String getAuthToken() {
 		GoogleLoginServiceBlockingHelper loginHelper = null;
 		String username = null;
 		String authToken = null;
 
 		try {
 			loginHelper = new GoogleLoginServiceBlockingHelper(this);
 
 			// TODO: allow caller to specify which account's feeds should be updated
 			username = loginHelper.getAccount(false);
 			if (TextUtils.isEmpty(username)) {
 				Log.w(TAG, "no users configured.");
 				return null;
 			}
 
 			try {
 				authToken = loginHelper.getAuthToken(username,
 				"ah");
 			} catch (GoogleLoginServiceBlockingHelper.AuthenticationException e) {
 				Log.w(TAG, "could not "
 						+ "authenticate user " + username, e);
 				return null;
 			}
 		} catch (GoogleLoginServiceNotFoundException e) {
 			Log.e(TAG, "Could not find Google login service", e);
 			return null;
 		} finally {
 			if (loginHelper != null) {
 				loginHelper.close();
 			}
 		}
 		return authToken;
 	}
 
 	protected boolean authorize(String authToken) {
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 
 
 		String base_url = getString(R.string.base_url);
 		HttpGet auth_request = new HttpGet(base_url + "/_ah/login?continue=" + base_url + "&auth="+authToken);
 		HttpResponse response;
 
 
 		try {
 			response = httpClient.execute(auth_request);
 			int status = response.getStatusLine().getStatusCode();
 
 			Log.d(TAG,"auth status: "+status);
 			if(status == HttpStatus.SC_OK) {
 
 				CookieStore cs = httpClient.getCookieStore();
 				Editor edit = preferences.edit();
 				edit.putString("AUTHCOOKIE", cs.getCookies().get(0).getValue());
 				edit.commit();
 
 				return true;
 			}
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 }
