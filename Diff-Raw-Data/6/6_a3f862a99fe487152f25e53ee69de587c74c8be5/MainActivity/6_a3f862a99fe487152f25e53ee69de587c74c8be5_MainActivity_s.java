 package com.danmillerapps.gowithfriends;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
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
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     public void doLogin(View v){
     	String username = ((EditText)findViewById(R.id.login_username)).getText().toString();
     	String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
     	new LoginTask().execute(username,password);
     }
     
     private class LoginTask extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... loginParams) {
 			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(R.string.url_prefix + "api_post.php");
 			
 			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
				nameValuePairs.add(new BasicNameValuePair("action", "login"));
 				nameValuePairs.add(new BasicNameValuePair("username", loginParams[0]));
 				nameValuePairs.add(new BasicNameValuePair("password", loginParams[1]));
 				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 				
 				HttpResponse response = httpClient.execute(httpPost);
 				
 				InputStream responseStream = response.getEntity().getContent();
 				String line = "";
 				StringBuilder responseText = new StringBuilder();
 				BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseStream));
 				
 				while ((line = responseReader.readLine()) != null){
 					responseText.append(line);
 				}
 				
 				if (responseText.toString().equals("success")) {
 					//TODO Open new activity
 				} else {
 					Toast.makeText(MainActivity.this, "Login failed!", Toast.LENGTH_LONG).show();
 					((EditText)findViewById(R.id.login_password)).setText("");
 				}
 				
 				
 			} catch (Exception e) {
 				Toast.makeText(MainActivity.this, "Network Error!", Toast.LENGTH_LONG).show();
 			}
 			return null;
 		}
     	
     }
 }
