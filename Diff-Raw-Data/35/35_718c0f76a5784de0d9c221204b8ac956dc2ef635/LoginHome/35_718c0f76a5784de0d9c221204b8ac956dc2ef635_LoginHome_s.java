 package com.trydish.main;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginHome extends Activity {
 	
 	boolean nocheck = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 				
 		setContentView(R.layout.activity_login_home);
 		
 		ActionBar actionBar = getActionBar();
 		actionBar.hide();
 	}
 	
 	public void loginCheck(View view) {
 		if (nocheck) {
 			checkLogin("true");
 		} else {
 			EditText userText = (EditText)findViewById(R.id.login_username);
 			EditText passText = (EditText)findViewById(R.id.login_password);
 			
 			LoginTask checkLogin = new LoginTask();
 			checkLogin.execute(userText.getText().toString(), passText.getText().toString());
 		}
 	}
 	
 	public void signupButton(View view) {
 		Intent intent = new Intent(this, SignupHome.class);
 		startActivity(intent);
 	}
 	
 	
 	
 	private class LoginTask extends AsyncTask<String, Void, String> {
 
 		@Override
 		protected String doInBackground(String... params) {
 			String url = "http://trydish.pythonanywhere.com/login";
 			String responseString;
 
 			System.out.println(0);
 			
 			HttpClient httpclient = new DefaultHttpClient();
 			
 			System.out.println(1);
 			
 			HttpPost post = new HttpPost(url);
 			try {
 				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 				System.out.println("2" + params[0]);
 				postParameters.add(new BasicNameValuePair("username", params[0]));
 				System.out.println(2.5);
 				postParameters.add(new BasicNameValuePair("password", params[1]));
 				System.out.println(3);
 				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
 				System.out.println(4);
 				post.setEntity(entity);
 				System.out.println(5);
 	            HttpResponse response = httpclient.execute(post);
 	            
 	            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
 	                ByteArrayOutputStream out = new ByteArrayOutputStream();
 	                response.getEntity().writeTo(out);
 	                out.close();
 	                responseString = out.toString();
 
 	            } else {
 	                //Closes the connection.
 	                response.getEntity().getContent().close();
 	                return "false";
 	            }
 	        } catch (ClientProtocolException e) {
 	        	return "false";
 	        } catch (IOException e) {
 	        	return "false";
 	        }
 			if (responseString.indexOf("true") != -1) {
 				return "true";
 			} else {
 				return "false";
 			}
     	}
     
 		@Override
 		protected void onPostExecute(String login) {
 			checkLogin(login);
 		}
     	
     }
 	
 	private void checkLogin(String login) {
 		if (login.equalsIgnoreCase("true")) {
 			Toast toast = Toast.makeText(this, "Thank you for logging in!", Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
 			toast.show();
 			
 			Intent intent = new Intent(this, PostLoginHome.class);
 	    	startActivity(intent);
	    	//overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
 		} else {
 			Toast toast = Toast.makeText(this, "Username or password is incorrect.", Toast.LENGTH_LONG);
 			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
 			toast.show();
 			
 			EditText password = (EditText)findViewById(R.id.login_password);
 			password.setText("");
 		}
 	}
 	
 	
 	//Code from: http://www.anyexample.com/programming/java/java_simple_class_to_compute_sha_1_hash.xml
 	private static String convertToHex(byte[] data) { 
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < data.length; i++) { 
             int halfbyte = (data[i] >>> 4) & 0x0F;
             int two_halfs = 0;
             do { 
                 if ((0 <= halfbyte) && (halfbyte <= 9)) 
                     buf.append((char) ('0' + halfbyte));
                 else 
                     buf.append((char) ('a' + (halfbyte - 10)));
                 halfbyte = data[i] & 0x0F;
             } while(two_halfs++ < 1);
         } 
         return buf.toString();
     } 
  
     public static String SHA1(String text) {
     	MessageDigest md;
     	try {
     		md = MessageDigest.getInstance("SHA-1");
     		byte[] sha1hash = new byte[40];
     		md.update(text.getBytes("iso-8859-1"), 0, text.length());
     		sha1hash = md.digest();
     		return convertToHex(sha1hash);
     	} catch (Exception e) {
     		System.out.println("Something broke in SHA1!");
     		return "broken";
     	}
     }
 
 }
