 package uk.co.jarofgreen.cityoutdoors.UI;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.NameValuePair;
 
 import uk.co.jarofgreen.cityoutdoors.API.LogInCall;
 import uk.co.jarofgreen.cityoutdoors.Service.SendFeatureFavouriteService;
 import uk.co.jarofgreen.cityoutdoors.R;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * 
  * @author James Baster  <james@jarofgreen.co.uk>
  * @copyright City of Edinburgh Council & James Baster
  * @license Open Source under the 3-clause BSD License
  * @url https://github.com/City-Outdoors/City-Outdoors-Android
  */
public class LogInActivity extends Activity {
 	
 	 ProgressDialog mDialog;
 	 LogInTask logInTask;
 	 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);  
     }
     
     
     public void onClickLogIn(View v) {
     	logInTask = new LogInTask();
     	
     	mDialog = new ProgressDialog(this);
     	mDialog.setMessage("Checking, please wait ...");
     	mDialog.setOnCancelListener(new OnCancelListener() {
             public void onCancel(DialogInterface arg0) {
             	logInTask.cancel(true);
             }
         });
     	mDialog.setCancelable(true);  	
     	mDialog.show();
 
     	logInTask.setEmail(((EditText)findViewById(R.id.email)).getText().toString());
     	logInTask.setPassword(((EditText) findViewById(R.id.password)).getText().toString());
     	logInTask.execute(true);   	
     }
     
     public void onClickLogInTwitter(View v) {
     	Intent i = new Intent(this, LogInTwitterActivity.class);
     	startActivity(i);  	
     }
     
     
     public void onClickCreateAnAccount(View v) {
         Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.server_url)+"/signup.php"));
         startActivity(browserIntent);  
     }
 
     public void onClickForgotPassword(View v) {
     	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.server_url)+"/forgottenPassword.php"));
         startActivity(browserIntent);
     }
          
     private class LogInTask extends AsyncTask<Boolean, Void, Boolean> {
  
 		protected String Email;
     	protected String Password;
     	
     	protected LogInCall logInCall;
     	
         protected Boolean doInBackground(Boolean... dummy) {
         	
             try{
             	
             	logInCall = new LogInCall();
             	return logInCall.execute(LogInActivity.this, Email, Password);
                 
             } catch(Exception e) {
             	Log.d("ERRORINLOGIN",e.toString());
             	if (e.getMessage() != null) Log.d("ERRORINLOGIN",e.getMessage());
             }
         	
             return false;
         	
         }
 
         protected void onPostExecute(Boolean result) {
         	LogInActivity.this.mDialog.dismiss();
 
         	if (result) {
         		logInCall.saveResults(LogInActivity.this);
         		// send data to server in background
         		LogInActivity.this.startService(new Intent(LogInActivity.this, SendFeatureFavouriteService.class));
         		// start main screen
         		Intent i = new Intent(LogInActivity.this, MainActivity.class);
         		LogInActivity.this.startActivity(i);
         		// kill login screen
         		LogInActivity.this.finish();
         	} else {
         		Toast.makeText(LogInActivity.this, "Login failed", Toast.LENGTH_LONG).show();
         	}
 
         }
                 
     	public void setEmail(String email) {
 			Email = email;
 		}
 
 		public void setPassword(String password) {
 			Password = password;
 		}
     }
     
     
     
 }
