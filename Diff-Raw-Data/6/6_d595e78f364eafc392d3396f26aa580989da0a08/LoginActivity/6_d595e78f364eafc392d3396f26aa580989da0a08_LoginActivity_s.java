 package com.jcheed06.myhealthapp;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 
 import android.annotation.TargetApi;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginActivity extends BaseActivity {
 
 	String username = "";
 	String password = "";
 	private EditText editPassword;
 	int incorrectLogins = 0;
 	private ProgressDialog dialogWait;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		findViewById(R.id.btn_login).setOnClickListener(
 			new View.OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					attemptLogin();
 				}
 			}
 		);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.action_settings:
 	        	startActivity(new Intent(this, SettingsActivity.class));
 	            return true;
 	        case R.id.action_about:
 	            startActivity(new Intent(this, AboutActivity.class));
 	            return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void attemptLogin() {
 		EditText editUsername = (EditText)findViewById(R.id.username);
     	username = editUsername.getText().toString();
     	editPassword = (EditText)findViewById(R.id.password);
     	password = editPassword.getText().toString();
     	
 		Log.e("H1:", username + ":" + password);
 		
 		UserLogin userLogin = new UserLogin();
 		userLogin.execute((Void) null);
 		
 		dialogWait = ProgressDialog.show(this, "", "Please wait...", true);
 	}
 	
 	public class UserLogin extends AsyncTask<Void, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			// Create a new HttpClient and Post Header
 		    HttpClient httpclient = new DefaultHttpClient();
 		    
 		    HttpPost httppost = new HttpPost(Registry.BASE_API_URL + Registry.LOGIN_COMMAND);
 
 		    try {
 		        // Add your data
 		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 		        nameValuePairs.add(new BasicNameValuePair("username", username));
 		        nameValuePairs.add(new BasicNameValuePair("password", password));
 		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 		        
 		        HttpResponse response = httpclient.execute(httppost);
 		        
 		        BufferedReader buffer = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8")); 
 			    StringBuilder sb = new StringBuilder();
 			    String string = "";
 			    while ((string = buffer.readLine()) != null) {
 			    	sb.append(string);
 			    }
 
 			    JSONObject content = new JSONObject(sb.toString());
 		        buffer.close();
 		        Log.e("H2:", "content: " + content.toString());
		        
 		        if (content.get("status").toString().equals("1")) {
 		        	spEdit.putBoolean("loggedIn", true);
 		        	spEdit.putString("id", (String) content.get("id"));
 		        	spEdit.putString("username", username);
 		        	spEdit.commit();
 		        	incorrectLogins = 0;
 		        	Log.e("L1:", "Login ok");
 		        	return true;
 		        } else if (content.get("status").toString().equals("9")) {
 		        	Log.e("L2:", "Login NOT ok");
 		        	return false;
 		        }
 		        
 		        return false;
 		    } catch (Exception e) {
 		    	Log.e("H3:", e.toString());
 		    	return false;
 		    }
 		}
 		
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			dialogWait.dismiss();
 			
 			if (success) {
 				Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_LONG).show();
 				finish();
 			} else {
 				Toast.makeText(getApplicationContext(), "Could not login", Toast.LENGTH_LONG).show();
 			}
 		}
 		
 	}
 	
 	@Override
 	protected void onResume() {
 		spEdit.putBoolean("inLoginActivity", true);
 		spEdit.commit();
 		
 		super.onResume();
 	}
 	
 	@Override
 	protected void onPause() {
 		spEdit.putBoolean("inLoginActivity", false);
 		spEdit.commit();
 		
 		super.onPause();
 	}
 	
 	public int getIncorrectLogins() {
 		return incorrectLogins;
 	}
 }
