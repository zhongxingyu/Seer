 package com.link;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.*;
 
 public class HelloAndroid extends Activity {
 	private static final String TAG = HelloAndroid.class.getSimpleName();
 	private static final String AUTHCODE = "cos333";
 	private final String loginurl = "http://webscript.princeton.edu/~pcao/cos333/dologin.php";
 	private final String registerurl = "http://webscript.princeton.edu/~pcao/cos333/doregister.php";
 	private final String updateactivityurl = "http://webscript.princeton.edu/~pcao/cos333/updateactivity.php";
 	private final String getlobbyurl = "http://webscript.princeton.edu/~pcao/cos333/getlobby.php";
 	
 	private String netid = "";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
     // called when login button is clicked
     public void doLogin(View v) {
     	
     	final EditText netidEdit = (EditText) findViewById(R.id.netidEntry);
         final EditText passwordEdit = (EditText) findViewById(R.id.passwordEntry);
         String netidIn = netidEdit.getText().toString();
         String pwordIn = passwordEdit.getText().toString();
         
         LoginViaPHP task = new LoginViaPHP();
 		task.execute(new String[] { loginurl, netidIn, pwordIn });
     }
 
     private class LoginViaPHP extends AsyncTask<String, String, String[]> {
     	@Override
     	// check login credentials and returns true if login successful
     	protected String[] doInBackground(String... params) {
     		String loginurl;
     		String netidIn;
     		String pwordIn;
     		
     		String[] result = new String[2];
     		// get url/login/password from params
     		try {
 	    		loginurl = params[0];
 	    		netidIn = params[1];
 	    		pwordIn = params[2];
 	    		result[1] = netidIn; 
     		} catch (Exception e) {
     			e.printStackTrace();
     			result[0] = "error";
     			return result;
     		}
     		//System.out.println("attempting to login with: " + netidIn + ", " + pwordIn);
     		
     		// set up login/password to be posted to PHP
     		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     		nameValuePairs.add(new BasicNameValuePair("netid", netidIn));
     		nameValuePairs.add(new BasicNameValuePair("pword", pwordIn));
     		nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));
     		
     		InputStream content;
     		
     		// try getting http response
     		try {
     			// TODO: check for https functionality
     	        HttpClient httpclient = new DefaultHttpClient();
     	        HttpPost httppost = new HttpPost(loginurl);	        
     	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
     	        HttpResponse response = httpclient.execute(httppost);
     	        HttpEntity entity = response.getEntity();
     	        content = entity.getContent();
     	    } catch(Exception e){
     	        Log.e("log_tag","Error in internet connection " + e.toString());
     	        result[0] = "error";
     			return result;
     	    }
     		//System.out.println("post successful");
     		
     		// try reading http response
     		String output = "";
     		try {
     			BufferedReader reader = new BufferedReader(new InputStreamReader(content,"iso-8859-1"), 8);
     	        String line;
     	        while((line = reader.readLine()) != null){
     	            output += line;
     	        }
     	        content.close();
     		} catch(Exception e){
     	        Log.e("log_tag", "Error converting result " + e.toString());
     	        result[0] = "error";
     			return result;
     	    }
     		//System.out.println(output);
     		result[0] = output;
 			return result;
     	}
     	@Override
     	// process result of login query (true or false)
     	protected void onPostExecute(String results[]) {
     		final String loginsuccess = "yes"; // output from PHP to match
     		//final String loginfailure = "error";
     		String loginresult = results[0];
     		String netid = results[1];
     		if (loginresult.equals(loginsuccess)) {
     			setNetid(netid);
     			loggedIn();
     		} else {
     			final TextView loginstatustxt = (TextView) findViewById(R.id.loginstatustxt);
     			loginstatustxt.setText("Login failed! Please try again or register.");
     			loginstatustxt.setTextColor(Color.RED);
     		}
     	}
     }
 
     private class RegisterViaPHP extends AsyncTask<String, String, String[]> {
     	protected String[] doInBackground(String... params) {
     		String registerurl;
     		String netidIn;
     		String pwordIn;
     		String emailIn;
     		
     		String[] result = new String[2];
     		
     		
     		// get url/login/password from params
     		try {
 	    		registerurl = params[0];
 	    		netidIn = params[1];
 	    		pwordIn = params[2];
 	    		emailIn = params[3];
 	    		
 	    		result[1] = netidIn;
     		} catch (Exception e) {
     			e.printStackTrace();
     			result[0] = "error";
     			return result;
     		}
     		System.out.println("attempting to register with: " + netidIn + ", " + pwordIn + ", " + emailIn);
     		
     		// set up login/password to be posted to PHP
     		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     		nameValuePairs.add(new BasicNameValuePair("netid", netidIn));
     		nameValuePairs.add(new BasicNameValuePair("pword", pwordIn));
     		nameValuePairs.add(new BasicNameValuePair("email", emailIn));
     		nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));
     		
     		InputStream content;
     		
     		// try getting http response
     		try {
     			// TODO: check for https functionality
     	        HttpClient httpclient = new DefaultHttpClient();
     	        HttpPost httppost = new HttpPost(registerurl);	        
     	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
     	        HttpResponse response = httpclient.execute(httppost);
     	        HttpEntity entity = response.getEntity();
     	        content = entity.getContent();
     	    } catch(Exception e){
     	        Log.e("log_tag","Error in internet connection " + e.toString());
     	        result[0] = "error";
     			return result;
     	    }
     		//System.out.println("post successful");
     		
     		// try reading http response
     		String output = "";
     		try {
     			BufferedReader reader = new BufferedReader(new InputStreamReader(content,"iso-8859-1"), 8);
     	        String line;
     	        while((line = reader.readLine()) != null){
     	            output += line;
     	        }
     	        content.close();
     		} catch(Exception e){
     	        Log.e("log_tag", "Error converting result " + e.toString());
     	        result[0] = "error";
     			return result;
     	    }
     		//Log.e("log_tag", "output: " + output);
     		result[0] = output;
 			return result;
     	}
     	protected void onPostExecute(String results[]) {
     		final String registersuccess = "yes";
     		//final String registerfailure = "error";
     		String registerresult = results[0];
     		String netid = results[1];
     		if (registerresult.equals(registersuccess)) {
     			setNetid(netid);
     			loggedIn();
     		} else {
     			final TextView registerstatustxt = (TextView) findViewById(R.id.registerstatustxt);
     			registerstatustxt.setText("Registration failed! Username already exists.");
     			registerstatustxt.setTextColor(Color.RED);
     		}
     	}
     }
     
     private class UpdateActivityViaPHP extends AsyncTask<String, String, String[]> {
     	protected String[] doInBackground(String... params) {
     		String updateactivityurl;
     		String netidIn;
     		String activityIn;
     		
     		String[] result = new String[2];
     		
     		
     		// get url/login/password from params
     		try {
 	    		updateactivityurl = params[0];
 	    		netidIn = params[1];
 	    		activityIn = params[2];
 	    		
 	    		result[1] = activityIn;
     		} catch (Exception e) {
     			e.printStackTrace();
     			result[0] = "error";
     			return result;
     		}
     		System.out.println("attempting to update activity with: " + netidIn + ", " + activityIn);
     		
     		// set up login/password to be posted to PHP
     		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     		nameValuePairs.add(new BasicNameValuePair("netid", netidIn));
     		nameValuePairs.add(new BasicNameValuePair("activity", activityIn));
     		nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));
     		
     		InputStream content;
     		
     		// try getting http response
     		try {
     			// TODO: check for https functionality
     	        HttpClient httpclient = new DefaultHttpClient();
     	        HttpPost httppost = new HttpPost(updateactivityurl);	        
     	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
     	        HttpResponse response = httpclient.execute(httppost);
     	        HttpEntity entity = response.getEntity();
     	        content = entity.getContent();
     	    } catch(Exception e){
     	        Log.e("log_tag","Error in internet connection " + e.toString());
     	        result[0] = "error";
     			return result;
     	    }
     		System.out.println("post successful");
     		
     		// try reading http response
     		String output = "";
     		try {
     			BufferedReader reader = new BufferedReader(new InputStreamReader(content,"iso-8859-1"), 8);
     	        String line;
     	        while((line = reader.readLine()) != null){
     	            output += line;
     	        }
     	        content.close();
     		} catch(Exception e){
     	        Log.e("log_tag", "Error converting result " + e.toString());
     	        result[0] = "error";
     			return result;
     	    }
     		Log.e("log_tag", "output: " + output);
     		result[0] = output;
 			return result;
     	}
     	protected void onPostExecute(String results[]) {
     		final String updatesuccess = "yes";
     		//final String updatefailure = "error";
     		String updateresult = results[0];
     		String activity = results[1];
     		if (updateresult.equals(updatesuccess)) {
     			// success
     			System.out.println("updated activity: " + activity);
     		} else {
     			// failure
     			System.out.println("failed to update activity: " + activity);
     		}
     	}
     }
     
     private class GetLobbyViaPHP extends AsyncTask<String, String, String[]> {
     	protected String[] doInBackground(String... params) {
     		String getlobbyurl;
     		
     		String[] result = new String[] { "error", "" }; // to be returned
     		
     		try {
     			getlobbyurl = params[0];
     		} catch (Exception e) {
     			e.printStackTrace();
     			result[0] = "error";
     			return result;
     		}
 
     		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     		nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));
     		
     		InputStream content;
     		
     		// try getting http response
     		try {
     			// TODO: check for https functionality
     	        HttpClient httpclient = new DefaultHttpClient();
     	        HttpPost httppost = new HttpPost(getlobbyurl);	        
     	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
     	        HttpResponse response = httpclient.execute(httppost);
     	        HttpEntity entity = response.getEntity();
     	        content = entity.getContent();
     	    } catch(Exception e){
     	        Log.e("log_tag","Error in internet connection " + e.toString());
     	        result[0] = "error";
     			return result;
     	    }
     		
     		// try reading http response
     		String output = "";
     		try {
    			BufferedReader reader = new BufferedReader(new InputStreamReader(content,"iso-8859-1"), 8);
     	        String line;
     	        while((line = reader.readLine()) != null){
    	            output += line;
     	        }
     	        content.close();
     		} catch(Exception e){
     	        Log.e("log_tag", "Error converting result " + e.toString());
     	        result[0] = "error";
     			return result;
     	    }
     		Log.e("log_tag", "output: " + output);
     		result[0] = "yes";
     		result[1] = output;
 			return result;
     	}
     	protected void onPostExecute(String results[]) { // print lobby results
     		//final String lobbysuccess = "yes";
     		final String lobbyfailure = "error";
     		String lobbyresult = results[0];
     		String lobbytext = results[1];
     		System.out.println("got lobby: " + lobbytext);
     		if (!lobbyresult.equals(lobbyfailure)) {
     			// success
     			final TextView tv_lobby = (TextView) findViewById(R.id.tv_lobby);
     			tv_lobby.setText(lobbytext);
     		} else {
     			// failure
     			final TextView tv_lobby = (TextView) findViewById(R.id.tv_lobby);
     			tv_lobby.setText("Unable to connect to lobby.");
     		}
     	}
     }
     
     // set up the lobby and start screen after logging in
     private void loggedIn() {
     	setContentView(R.layout.loggedin);
     	final TextView welcomeuser = (TextView) findViewById(R.id.welcomeuser);
         welcomeuser.setText("Welcome, " + netid + "!");
         
         UpdateActivityViaPHP task = new UpdateActivityViaPHP();
 		task.execute(new String[] { updateactivityurl, netid, "In Lobby" }); // set activity to in lobby
 		
 		GetLobbyViaPHP getlobby = new GetLobbyViaPHP();
 		getlobby.execute(new String[] { getlobbyurl });
 
         
         final Button startgames = (Button) findViewById(R.id.startgames);
         startgames.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 		        Intent myIntent = new Intent(HelloAndroid.this, Linker.class);
 		        myIntent.putExtra("netid", netid);
 		        HelloAndroid.this.startActivityForResult(myIntent, -1);
 		    }
 		});
     }
     // return to lobby when linker activity ends
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		if (resultCode == RESULT_OK) {
 			loggedIn();
        }
 	}
     
     // called when register button is clicked
     public void doRegister(View v) {
     	final EditText netidEdit = (EditText) findViewById(R.id.netidEntry);
         final EditText passwordEdit = (EditText) findViewById(R.id.passwordEntry);
         final EditText confirmEdit = (EditText) findViewById(R.id.confirmEntry);
         final EditText emailEdit = (EditText) findViewById(R.id.emailEntry);
         String netidIn = netidEdit.getText().toString();
         String pwordIn = passwordEdit.getText().toString();
         String confirmIn = confirmEdit.getText().toString();
         String emailIn = emailEdit.getText().toString();
         
         if (!pwordIn.equals(confirmIn)) { // check that passwords match
         	final TextView registerstatustxt = (TextView) findViewById(R.id.registerstatustxt);
         	registerstatustxt.setText("Passwords do not match!");
         	registerstatustxt.setTextColor(Color.RED);
         	return;
         }
         RegisterViaPHP task = new RegisterViaPHP();
 		task.execute(new String[] { registerurl, netidIn, pwordIn, emailIn });
     }
     
     public void gotoregister(View v) {
     	final EditText netidFromLogin = (EditText) findViewById(R.id.netidEntry);
         final EditText passwordFromLogin = (EditText) findViewById(R.id.passwordEntry);
         String netidIn = netidFromLogin.getText().toString();
         String pwordIn = passwordFromLogin.getText().toString();
     	setContentView(R.layout.register);
     	final EditText netidFromRegister = (EditText) findViewById(R.id.netidEntry);
         final EditText passwordFromRegister = (EditText) findViewById(R.id.passwordEntry);
         netidFromRegister.setText(netidIn);
         passwordFromRegister.setText(pwordIn);
     }
     
     public void setNetid(String netid) { // set this upon successful login/registration
 		this.netid = netid;
 	}
     
     public void backtologin(View v) {
     	setContentView(R.layout.main);
     }
     protected void onDestroy() {
     	Log.d(TAG, "destroying...");
     	super.onDestroy();
     }
     protected void onStop() {
     	Log.d(TAG, "stopping...");
     	super.onStop();
     }
 }
