 package edu.pugetsound.vichar;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.EditText;
 
 /**
  * Provides interface and guts for logging into Twitter via ViChar app
  * @author Nathan Pastor & Michael Dubois
  * @version 10/15/12
  */
 public class TwitterOAuthActivity extends Activity {
 
 	Twitter twitter;
 	WebView webView;
 	String  oAuthUrl;
 	RequestToken requestToken;
 	AccessToken accessToken;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_twitter_oauth);
         this.webView = (WebView) findViewById(R.id.oauth_webview);
         //do following to ensure no address bar is visible
         this.webView.setWebViewClient(new WebViewClient()
 	        {
 	        	public boolean shouldOverrideUrlLoading(WebView view, String url)
 	        	{
 	        		view.loadUrl(url);
 	        		return false;
 	        	}        	
 	        });
         Log.d("ViChar", "Activity created, calling authorizeTwitter");
         authorizeTwitter();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_twitter_oauth, menu);
         return true;
     }
     
     /**
      * Authorize user. Launches authorization url.
      */
     private void authorizeTwitter()
 	{
 		twitter = new TwitterFactory().getInstance();
 		twitter.setOAuthConsumer(getString(R.string.oauth_consumer_key), getString(R.string.oauth_consumer_secret));
     	Log.d("ViChar", "twitter initialized");
     	//call private class to retrieve request token, true passed as a dummy variable
     	new RetrieveRequestToken().execute(true);
 	}
     
     /**
      * Parses authorization pin entered by user, and finishes Twitter
      * authorization process by retrieving OAuth access token
      * @param view The calling view
      */
     public void sendPin(View view)
     {
     	//get pin entered by user
     	EditText editText = (EditText) findViewById(R.id.twitter_auth_pin);
 		String pin = editText.getText().toString();
 		System.out.println(requestToken);
 		//call private class to retrieve access token, using request token and pin
 		new RetrieveAccessToken().execute(pin);		
 	}
     
     /**
      * Saves access token to persistent storage, so that user information
      * can be persisted across multiple game sessions
      * @param accessToken
      */
     private void storeAccessToken(AccessToken accessToken)
     {
     	PreferenceUtility prefs = new PreferenceUtility();
     	prefs.saveString(getString(R.string.access_token_key), accessToken.getToken(), this);
     	prefs.saveString(getString(R.string.access_token_secret_key), (String)accessToken.getTokenSecret(), this);
     }
     
     /**
      * Finishes login by advancing to next activity or prompting retry
      * @param loginResult Result of login attempt
      */
     public void finishLogin(boolean loginResult)    {
     	if(loginResult==true) 	{
     		//if login succeeds, move on to main menu
     		Intent mainActIntent = new Intent(this, MainMenuActivity.class);
         	startActivity(mainActIntent);
     	} else	{
     		//TODO: decide what will happen when login fails
     	}
     }
     
     /**
      * Retrieves OAuth request token in seperate thread (dummy paramters)
      * @author Nathan P
      * @version 10/18/12
      */
     private class RetrieveRequestToken extends AsyncTask<Boolean, Boolean, Boolean> 
     {    	
     	/**
     	 * Retrieve request token
     	 * @param Dummy parameter, has no purpose
     	 * @return True if successful, false if not
     	 */
     	@Override
         protected Boolean doInBackground(Boolean...booleans)
         {
             boolean result = false;
             try 
             {
             	requestToken = twitter.getOAuthRequestToken();
             	result = true;
             }
             catch (TwitterException te)
         	{
     			System.out.println(te.getStatusCode());
     			if(401 == te.getStatusCode())
     	        {
     	        	System.out.println("Unable to get the request token.");
     	        } else{
     	        	te.printStackTrace();
     	        }
     		}
     		catch (IllegalStateException ex)
     		{
     			System.out.println(ex);
     		}
             return result;
         }
     	
         /**
          * Displays results by setting URL in webview
          */
         @Override
         protected void onPostExecute(Boolean result) 
         {
         	oAuthUrl = requestToken.getAuthorizationURL();
 			Log.d("ViChar", "url retrieved");
 			Log.d("Vichar", oAuthUrl);
 			webView.loadUrl(oAuthUrl);
         }
     }
     
     /**
      * Retrieves OAuth access token in non-UI worker thread
      * @author Nathan P
      * @version 10/18/12
      */
     private class RetrieveAccessToken extends AsyncTask<String, Boolean, Boolean>
     {
     	/**
     	 * Retrieves access token
     	 * @param Array of Strings, contain OAuth pin entered by user
     	 * @return True if successful, false if not
     	 */
     	@Override
         protected Boolean doInBackground(String...pins)
         {    		
     		String pin = pins[0];
     		System.out.println(pin + "pin in worker thread");
             boolean result = false;
             try{
             	if(pin.length() > 0)
             	{
             		accessToken = twitter.getOAuthAccessToken(requestToken, pin);
             		System.out.println(accessToken + "if");
             	}
             	else
             	{
             		accessToken = twitter.getOAuthAccessToken();
             		System.out.println(accessToken + "else");
             	}
             	result = true;
             	storeAccessToken(accessToken);
             } 
             catch (TwitterException te) 
             {
             	if(401 == te.getStatusCode()){
             		System.out.println("Unable to get the access token.");
             	}else{
             		te.printStackTrace();
             	}
             }
             return result;
         }
     	
         /**
          * No UI to update here
          */
         @Override
         protected void onPostExecute(Boolean result)         {
         	finishLogin(result);
         }
     }
 }
