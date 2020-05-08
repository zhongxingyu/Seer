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
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
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
public class LogInTwitterActivity extends BaseActivity {
 
 	WebView webview;
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login_twitter);
         
         webview = (WebView)findViewById(R.id.webview);
         webview.loadUrl(getString(R.string.server_url)+"/android/loginTwitter.php");   
         webview.addJavascriptInterface(new JavaScriptInterface(this), "HeresATree");
         webview.setWebViewClient(new WebViewClient());
         
         WebSettings webSettings = webview.getSettings();
         webSettings.setJavaScriptEnabled(true);
         webSettings.setSavePassword(false);
 
     }
     	
     public class JavaScriptInterface {
     	Context mContext;
 
     	JavaScriptInterface(Context c) {
     		mContext = c;
     	}
 
     	public void authDone(int userID, String token) {
 
     		SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(mContext);
     		SharedPreferences.Editor editor = settings.edit();
     		editor.putInt("userID", userID);
     		editor.putString("userToken", token);
     		editor.commit();
 
     		Toast.makeText(mContext, "Logged in!", Toast.LENGTH_SHORT).show();
 
     		LogInTwitterActivity.this.runOnUiThread(new AuthDone());
     	}
     }
     
     private class AuthDone implements Runnable {
         public void run() {
         	// send data to server in background
         	LogInTwitterActivity.this.startService(new Intent(LogInTwitterActivity.this, SendFeatureFavouriteService.class));
     		// start main screen
     		Intent i = new Intent(LogInTwitterActivity.this, MainActivity.class);
     		LogInTwitterActivity.this.startActivity(i);
     		// kill login screen
     		LogInTwitterActivity.this.finish();
         }
     }
     
     
 	
 }
