 package uw.changecapstone.tweakthetweet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.EditText;

 
 public class MainActivity extends Activity {
 
 
 	private final static int ACTIVITY_COMPOSE = 5;
 
 	final static String TWEET_STRING = "TWEET_STRING";
 	public final static String LOCATION_TEXT = "uw.changecapstone.tweakthetweet.MESSAGE";
 
 	private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
 	private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
 	private static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLoggedIn";
 
 	
 	SharedPreferences pref; 
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("COMPOSE", "activity result");
 		super.onActivityResult(requestCode, resultCode, data);
 		String tweet = data.getStringExtra(TWEET_STRING);
 		Intent intent = new Intent(this, TweetActivity.class);
 		intent.putExtra(TWEET_STRING, tweet);
 		startActivity(intent);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		pref = PreferenceManager.getDefaultSharedPreferences(this);
 		checkLogInStatus();
 	}
 	
 	private void checkLogInStatus() {
 	  if (!pref.getBoolean(PREF_KEY_TWITTER_LOGIN, false)) {
 		  Intent i = new Intent(this, OAuthTwitterActivity.class);
 		  startActivity(i);
 	  }
 	}
 	
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public void startSettings(View view) {
 		Intent intent = new Intent(this, SettingsActivity.class);
 		startActivity(intent);
 	}
 
 	public void printDebug(View view) {
 		Intent intent = new Intent(this, DebugActivity.class); 
 		startActivity(intent);
 	}
 	
 	public void composeTweet(View view) {
 		Intent intent = new Intent(this, ComposeActivity.class);
 		Log.d("COMPOSE", "startig compose");
 		startActivityForResult(intent, ACTIVITY_COMPOSE);
 	}
 	
 	public void authenticateTwitter(View view) {
 		Intent i = new Intent(this, OAuthTwitterActivity.class);
 		startActivity(i);
 	}
 		
 	public void testComposeString(View view) {
 		Intent i = new Intent(this, TestStringBuilder.class);
 		startActivity(i);
 		
 	}
 
 	public void showMap(View view){
 		Intent intent = new Intent(this, MapDisplayActivity.class);
 		startActivity(intent);
 	}
 	/*when the user clicks the "Enter" button, 
 	 * we are going to read the textfield content and 
 	 * do some validity checks before we show/zoom map*/
 	public void readLocationMessage(View view){
 		Intent intent = new Intent(this, LocationAndMapActivity.class);
 	    EditText editText = (EditText) findViewById(R.id.edit_message);
 	    String message = editText.getText().toString();
 	    intent.putExtra(LOCATION_TEXT, message);
 	    startActivity(intent);
 	}
 
 	
 	public void twitterLogout(View view) {
 		Editor e = pref.edit();
 		e.remove(PREF_KEY_OAUTH_TOKEN);
 		e.remove(PREF_KEY_OAUTH_SECRET);
 		e.remove(PREF_KEY_TWITTER_LOGIN);
 		e.remove("USERNAME");
 		e.commit();
 	
 		
 	}
 		
 
 
 }
