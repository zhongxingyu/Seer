 package com.example.friendzyapp;
 
 import java.util.LinkedList;
 import java.util.List;
 import com.example.friendzyapp.R;
 
 import com.facebook.*;
 import com.facebook.model.*;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.google.gson.Gson;
 
 public class LoginActivity extends Activity implements OnClickListener {
     String TAG = "login";
 
     public GraphUser user;
     public List<GraphUser> friends;
     public String regId;
 
     private int errorCount;
     private final int maxTolerableRetryCount = 2;
     private Global globals;
 
     private TextView loadingText;
 
     Session.StatusCallback callback = new Session.StatusCallback() {
         // callback when session changes state
         @Override
         public void call(Session session, SessionState state,
                 Exception exception) {
             if (state.isOpened()) {
 
                 globals.session = session;
                 Request.executeMeRequestAsync(session, new FacebookInfoCallback());
                 Request.executeMyFriendsRequestAsync(session,
                         new FacebookInfoCallback());
                 Log.d(TAG,
                         "Waiting on NameCallback and FriendListCallback to return...");
             } else {
                 Log.d(TAG, "Facebook didn't open! status=" + state.toString());
                 if (exception != null)
                     Log.d(TAG, exception.getMessage());
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         Log.d(TAG, "Starting Friendzy app");
 
         globals = (Global) getApplicationContext();
 
         Bundle extras = getIntent().getExtras();
         if (extras != null && !extras.isEmpty()
                 && extras.containsKey("EXTRA_LAUNCHED_BY_NOTIFICATION")) {
             extras.getBoolean("EXTRA_LAUNCHED_BY_NOTIFICATION");
         } else {
         }
 
         loadingText = (TextView) findViewById(R.id.loadingtxt);
         errorCount = maxTolerableRetryCount;
 
         globals.initLocationManager();
         initMessaging();
         initFacebookSession(savedInstanceState);
 
         // TODO: add error checking in case session doesn't open and if user
         // presses back or facebook glitches up and stuff
         // recommended to add an onClickListener that will reattempt this every
         // click or something.
         // } else {
         // already logged into Facebook!
         // Log.d(TAG, "open facebook Session already existed! ");
         // doFriendzyLogin();
         // }
     }
 
     private void initMessaging() {
         Log.d(TAG, "Attempting to register GCM.");
 
 
         GCMRegistrar.checkDevice(this);
         GCMRegistrar.checkManifest(this); // TODO: this line can be // for release
 
         regId = GCMRegistrar.getRegistrationId(this);
         Log.d(TAG, "regID is: '" + regId + "'");
 
         if (regId.equals("")) {
             Log.d(TAG,
                     "regId = \"\".  Trying to register with GCM with SENDER_ID: "
                             + GCMIntentService.SENDER_ID);
             GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
             regId = GCMRegistrar.getRegistrationId(this); // note:
                                                           // http://stackoverflow.com/questions/12366445/gcm-registerid-is-empty-immediatly-after-register-is-complete
 
         } else {
             Log.d(TAG, "Already registered");
             globals.gcmRegId = regId;
         }
 
         // Note: if a Log.d(GCMRegistrar, "resetting backoff for " +
         // context.getPackageName()) is called, it is because of GCM call
         // succeeding.
 
     }
 
     /*
      * This will automatically call doFriendzyLogin and redirect
      */
     private void initFacebookSession(Bundle savedInstanceState) {
         String str = "Attempting to openActiveSession for Facebook.";
         if (Session.getActiveSession() != null) {
             str += "  Session is already active.";
         } else {
             str += "  Session is not active, going to start new one";
         }
         Log.d(TAG, str);
 
         loadingText.setText("Loading Facebook...  please wait...");
 
         // Lifted from Facebook sample code SessionLogin.
 
         Session.OpenRequest openRequest = new Session.OpenRequest(this)
                 .setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK)
                 .setCallback(callback)
                 .setDefaultAudience(SessionDefaultAudience.FRIENDS);
 
         Session session = Session.getActiveSession();
         if (session == null) {
             if (savedInstanceState != null) {
                 session = Session.restoreSession(this, null, callback,
                         savedInstanceState);
             }
             if (session == null) {
                 session = new Session(this);
             }
             Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                 session.openForRead(openRequest);
             }
         }
     }
 
     private class FacebookInfoCallback implements Request.GraphUserListCallback, Request.GraphUserCallback {
         @Override
         public void onCompleted(GraphUser user, Response response) {
             if (user != null) {
                 LoginActivity.this.user = user;
 
                 Log.d(TAG, "NameCallback success: " + user.getName());
                 doFriendzyLogin();
             }
         }
         
         @Override
         public void onCompleted(List<GraphUser> users, Response response) {
             if (users != null) {
                 friends = users;
                 Log.d(TAG,
                         "FriendListCallback success: "
                                 + Integer.toString(friends.size()) + " friends");
                 doFriendzyLogin();
             }
         }
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.d(TAG, "received onActivityResult");
         super.onActivityResult(requestCode, resultCode, data);
         Session.getActiveSession().onActivityResult(this, requestCode,
                 resultCode, data);
     }
 
     private class LoginRequest {
         public LoginRequest(String _userID, List<String> _facebookFriends,
                 String ri) {
             userID = _userID;
             facebookFriends = _facebookFriends;
             this.regId = ri;
         }
 
         @SuppressWarnings("unused")
         public String userID;
         @SuppressWarnings("unused")
         public List<String> facebookFriends;
         @SuppressWarnings("unused")
         public String regId;
     }
 
     private class LoginAsyncTask extends InternetConnectionAsyncTask {
         private static final String resource = "login";
 
         public LoginAsyncTask() {
             super(resource);
             Log.d(TAG, "init LoginAsyncTask");
         }
 
         protected void onPostExecute(final String respString) {
             if (respString == null) {
 
                 Log.e(TAG, "reader is null, did download fail?");
                 loadingText
                         .setText("Oops... error with friendzy server's response.... trying again...");
 
                 if (errorCount > 0) {
                     errorCount--;
 
                     List<String> friendIds = new LinkedList<String>();
                     for (GraphUser friend : friends) {
                         friendIds.add(friend.getId());
                     }
                     String request = new Gson().toJson(new LoginRequest(user
                             .getId(), friendIds, globals.gcmRegId));
                     Log.d(TAG, "retry request=" + request);
                     LoginAsyncTask login = new LoginAsyncTask();
                     login.execute(request);   
                 }
                 return;
             }
 
             Log.d(TAG, "LoginAsyncTask: onPostExecute; " + respString);
 
             // if (redirectToAcceptDenyInstantly) {
             // switchToAcceptDenyInstantly();
             // } else {
             switchToStatusScreen(respString);
             // }
         }
     }
 
     private void doFriendzyLogin() {
         // called twice, async; one or the other might not be done
 
         if (user == null || friends == null) {
             Log.d(TAG, "doFriendzyLogin: not quite yet");
             return;
         }
 
         // Wait until GCM is done registering, only if registration was
         // required.
         // TODO: If we're waiting, anything from this point should be in a
         // worker thread instead of waiting
         // for the intent service waiter thread.
         if (globals.gcmRegId == null) // Or we were already registered when the
                                       // Activity was started, or the GCM
                                       // registration callback finished before
                                       // we arrived here
             synchronized (globals.gcmRegMonitor) {
                 try {
                     globals.gcmRegMonitor.wait();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
 
         Log.d(TAG, "Time to log in. gcmRegId=" + globals.gcmRegId);
 
         loadingText.setText("Welcome " + user.getFirstName()
                 + ". \nLogging in to Friendzy...");
         Log.d(TAG,
                 "doFriendzyLogin: all members present.  Setting up LoginAsyncTask.");
 
         Gson gson = new Gson();
         List<String> friendIds = new LinkedList<String>();
         for (GraphUser friend : friends) {
             friendIds.add(friend.getId());
         }
 
         String request = gson.toJson(new LoginRequest(user.getId(), friendIds,
                 globals.gcmRegId));
         LoginAsyncTask login = new LoginAsyncTask();
         login.execute(request);
 
     }
 
     private void switchToStatusScreen(String jsonFriendInfo) {
         Log.d(TAG, "will switch to status screen");
         Intent intent = new Intent(this, StatusScreenActivity.class);
         intent.putExtra("EXTRA_LAUNCHED_BY_NOTIFICATION", false);
         intent.putExtra("USER_ID", user.getId());
         intent.putExtra("FRIEND_INFO", jsonFriendInfo);
 
         startActivity(intent);
     }
 
     @Override
     public void onClick(View v) { // this doesn't actually work...
         Log.d(TAG, "Clicked! Retrying initFacebookSession()");
         // initFacebookSession();
     }
 
 }
