 package me.rohitmishra.groupbanker;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences ;
 import com.facebook.android.*;
 import com.facebook.android.R;
 import com.facebook.android.Facebook.*;
 
 
 import me.rohitmishra.groupbanker.GroupBankerActivity.FbAPIsAuthListener;
 import me.rohitmishra.groupbanker.GroupBankerActivity.FbAPIsLogoutListener;
 import me.rohitmishra.groupbanker.GroupBankerActivity.UserRequestListener;
 import me.rohitmishra.groupbanker.SessionEvents.AuthListener;
 import me.rohitmishra.groupbanker.SessionEvents.LogoutListener;
 import com.facebook.*;
 import me.rohitmishra.groupbanker.LoginButton ;
 import me.rohitmishra.groupbanker.Utility ;
 
 public class GroupBankerActivity extends Activity {
 	
 	private static final String TAG = "GroupBankerActivity";
 	
 	/* Hackbook wants us to initialize FB class via the Utility class 
 	
 	Facebook facebook = new Facebook("228939630494756");
 	*/
 	
 	// Copied from Hackbook for Android 
 	
 	public static final String APP_ID = "228939630494756";
 	private LoginButton mLoginButton ;
 	private TextView mText;
     private ImageView mUserPic;
 	private Handler mHandler;
     ProgressDialog dialog;
     String[] permissions = { "offline_access", "publish_stream", "user_photos", "publish_checkins",
     "photo_upload" };
     final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
    
     private SharedPreferences mPrefs;
     private Boolean mFriends ;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.v(TAG, "onCreate called") ;
     	super.onCreate(savedInstanceState);
         
     	mHandler = new Handler() ;
     	
     	// Create the Facebook Object using the app id.
         
         Utility.mFacebook = new Facebook(APP_ID);
         
         Log.v(TAG, "mFacebook = " + Utility.mFacebook) ; 
         
         // Instantiate the asynrunner object for asynchronous api calls.
         Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
 
         
         
      // restore session if one exists
         SessionStore.restore(Utility.mFacebook, this);
         SessionEvents.addAuthListener(new FbAPIsAuthListener());
         SessionEvents.addLogoutListener(new FbAPIsLogoutListener());
         
         
         mPrefs = getSharedPreferences(Constants.preferences, 0);

         if (Utility.mFacebook.isSessionValid()) {
         	Log.v(TAG, "onCreate - isSessionValid = true");
            // requestUserData();
            mFriends = mPrefs.getBoolean("friendsDownloaded", false) ;
            Log.v(TAG, "mFriends = " + mFriends);
             if (mFriends == false){
             	Log.v(TAG, "in onCreate. mFriends is false. Calling getFrieds") ;
             	getFriends();
             }
             else	{
             	Log.v(TAG, "onCreate - mFriends - true. Launching Home") ;
             	Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                 startActivity(homeIntent);
             }
         }
         else {
         
 	    	setContentView(R.layout.main);
 	    	mLoginButton = (LoginButton) findViewById(R.id.login);
 	        mText = (TextView) GroupBankerActivity.this.findViewById(R.id.txt);
 	        mUserPic = (ImageView) GroupBankerActivity.this.findViewById(R.id.user_pic);
 	        
 	        /*
 	         * Source Tag: login_tag
 	         */
 	        mLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE, Utility.mFacebook, permissions);
         }
     }
     
     @Override
     public void onResume() {
         super.onResume();
         if(Utility.mFacebook != null) {
             if (!Utility.mFacebook.isSessionValid()) {
                 Log.v(TAG, "In onResume - isSessionValid is false") ;
             	mText.setText("You are logged out! ");
                 mUserPic.setImageBitmap(null);
             } else {
             	Log.v(TAG, "in onResume -- else part - isSessionValid is true");
                 Utility.mFacebook.extendAccessTokenIfNeeded(this, null);
             }
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
         /*
          * if this is the activity result from authorization flow, do a call
          * back to authorizeCallback Source Tag: login_tag
          */
             case AUTHORIZE_ACTIVITY_RESULT_CODE: {
                 Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
                 break;
             }
         }
     }
     
     /*
      * callback after friends are fetched via me/friends or fql query.
      */
     public class FriendsRequestListener extends BaseRequestListener {
 
         @Override
         public void onComplete(final String response, final Object state) {
             Log.v(TAG, "We are in FriendsRequestListener onComplete. Response is " + response ) ;
         	dialog.dismiss();
             Intent myIntent = new Intent(getApplicationContext(), FriendsList.class);
             myIntent.putExtra("API_RESPONSE", response);
             // myIntent.putExtra("METHOD", graph_or_fql);
             startActivity(myIntent);
         }
 
         public void onFacebookError(FacebookError error) {
             dialog.dismiss();
             Toast.makeText(getApplicationContext(), "Facebook Error: " + error.getMessage(),
                     Toast.LENGTH_SHORT).show();
         }
     }
 
    
     public class FQLRequestListener extends BaseRequestListener {
 
         @Override
         public void onComplete(final String response, final Object state) {
             mHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     Toast.makeText(getApplicationContext(), "Response: " + response,
                             Toast.LENGTH_LONG).show();
                 }
             });
         }
 
         public void onFacebookError(FacebookError error) {
             Toast.makeText(getApplicationContext(), "Facebook Error: " + error.getMessage(),
                     Toast.LENGTH_LONG).show();
         }
     }
 
     /*
      * Callback for fetching current user's name, picture, uid.
      */
     public class UserRequestListener extends BaseRequestListener {
 
         @Override
         public void onComplete(final String response, final Object state) {
             JSONObject jsonObject;
             try {
                 jsonObject = new JSONObject(response);
 
                 final String picURL = jsonObject.getString("picture");
                 final String name = jsonObject.getString("name");
                 Utility.userUID = jsonObject.getString("id");
 
                 mHandler.post(new Runnable() {
                     @Override
                     public void run() {
                         mText.setText("Welcome " + name + "!");
                         mUserPic.setImageBitmap(Utility.getBitmap(picURL));
                     }
                 });
 
             } catch (JSONException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
 
     }
 
     /*
      * The Callback for notifying the application when authorization succeeds or
      * fails.
      */
 
     public class FbAPIsAuthListener implements AuthListener {
 
         @Override
         public void onAuthSucceed() {
             Log.v(TAG, "in FbAPIsAuthListener - onAuthSucced - true - calling getFriends") ;
         	//requestUserData();
             if (mFriends == false){
             	getFriends();
             }
             
         }
 
         @Override
         public void onAuthFail(String error) {
             mText.setText("Login Failed: " + error);
         }
     }
 
     /*
      * The Callback for notifying the application when log out starts and
      * finishes.
      */
     public class FbAPIsLogoutListener implements LogoutListener {
         @Override
         public void onLogoutBegin() {
             mText.setText("Logging out...");
         }
 
         @Override
         public void onLogoutFinish() {
             mText.setText("You have logged out! ");
             mUserPic.setImageBitmap(null);
         }
     }
 
     /*
      * Request user name, and picture to show on the main screen.
      */
     public void requestUserData() {
     	Log.v(TAG, "requestUserData called") ;
         mText.setText("Fetching user name, profile pic...");
         Bundle params = new Bundle();
         params.putString("fields", "name, picture");
         Utility.mAsyncRunner.request("me", params, new UserRequestListener());
     }
     
     public void getFriends() {
     	Log.v(TAG, "getFriends called") ;
     	dialog = ProgressDialog.show(GroupBankerActivity.this, "",
                 getString(R.string.please_wait), true, true);
     	String query = "select name, current_location, uid, pic_square from user where uid in (select uid2 from friend where uid1=me()) order by name";
         Bundle params = new Bundle();
         params.putString("method", "fql.query");
         params.putString("query", query);
         Utility.mAsyncRunner.request(null, params,
                 new FriendsRequestListener());
     }
 }
