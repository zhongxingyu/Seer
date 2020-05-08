 /*
  * Copyright 2012 Urban Airship and Contributors
  */
 
 package com.slalomdigital.smartalert;
 
import com.facebook.*;
import com.facebook.model.*;

 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.urbanairship.UAirship;
 import com.urbanairship.richpush.RichPushManager;
 import com.urbanairship.richpush.RichPushUser;
 import com.urbanairship.util.UAStringUtil;
 
 @SuppressWarnings("unused")
 public class MainActivity extends SherlockFragmentActivity implements
 ActionBar.OnNavigationListener {
     protected static final String TAG = "MainActivity";
 
     static final String ALIAS_KEY = "com.slalomdigital.smartalert.ALIAS";
     static final int aliasType = 1;
 
     ArrayAdapter<String> navAdapter;
     RichPushUser user;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.main);
         this.user = RichPushManager.shared().getRichPushUser();

        // start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {
                    // make request to the /me API
                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                //TODO: Add some code to the app to show the user once they log in
                                //TextView welcome = (TextView) findViewById(R.id.welcome);
                                //welcome.setText("Hello " + user.getName() + "!");
                            }
                        }
                    });
                }
            }
        });
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         UAirship.shared().getAnalytics().activityStarted(this);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         this.configureActionBar();
         this.displayMessageIfNecessary();
     }
 
     @Override
     protected void onStop() {
         super.onStart();
         UAirship.shared().getAnalytics().activityStopped(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         this.getSupportMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         case R.id.preferences:
             this.startActivity(new Intent(this, PushPreferencesActivity.class));
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onNavigationItemSelected(int itemPosition, long itemId) {
         String navName = this.navAdapter.getItem(itemPosition);
         if (SmartAlertApplication.HOME_ACTIVITY.equals(navName)) {
             // do nothing, we're here
         } else if (SmartAlertApplication.INBOX_ACTIVITY.equals(navName)) {
             Intent intent = new Intent(this, InboxActivity.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
             this.startActivity(intent);
         }
         return true;
     }
 
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

     // helpers
 
     private void displayMessageIfNecessary() {
         String messageId = this.getIntent().getStringExtra(SmartAlertApplication.MESSAGE_ID_RECEIVED_KEY);
         if (!UAStringUtil.isEmpty(messageId)) {
             MessageFragment message = MessageFragment.newInstance(messageId);
             message.show(this.getSupportFragmentManager(), R.id.floating_message_pane, "message");
             this.findViewById(R.id.floating_message_pane).setVisibility(View.VISIBLE);
         }
     }
 
     private void dismissMessageIfNecessary() {
         MessageFragment message = (MessageFragment) this.getSupportFragmentManager()
                 .findFragmentByTag("message");
         if (message != null) {
             message.dismiss();
             this.findViewById(R.id.floating_message_pane).setVisibility(View.INVISIBLE);
         }
     }
 
     private void configureActionBar() {
         ActionBar actionBar = this.getSupportActionBar();
         actionBar.setDisplayUseLogoEnabled(true);
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         this.navAdapter = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item,
                 SmartAlertApplication.navList);
         actionBar.setListNavigationCallbacks(this.navAdapter, this);
         actionBar.setSelectedNavigationItem(this.navAdapter.getPosition("Home"));
     }
 }
