 /*
  * Copyright (C) 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.partytv.server;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.InputType;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.OnHierarchyChangeListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 //Copyright 2011 Google Inc. All Rights Reserved.
 
 import org.json.JSONException;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.android.AndroidAuthSession;
 import com.dropbox.client2.android.AuthActivity;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.TokenPair;
 import com.dropbox.client2.session.Session.AccessType;
 import com.partytv.server.R;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 /**
  * The main Activity which displays the grid of photos fetched from
  * Panoramio service.
  */
 public class PartyTvServer extends Activity implements OnClickListener {
     private static final String DEFAULT_QUERY = "Los Angeles";
     
     
     
     ///////////////////////////////////////////////////////////////////////////
     //                      Your app-specific settings.                      //
     ///////////////////////////////////////////////////////////////////////////
 
     // Replace this with your app key and secret assigned by Dropbox.
     // Note that this is a really insecure way to do this, and you shouldn't
     // ship code which contains your key & secret in such an obvious way.
     // Obfuscation is good.
     final static private String APP_KEY = "ex7ktzbeatxkxfv";
     final static private String APP_SECRET = "t4mzka6bcya5xbj";
 
     // If you'd like to change the access type to the full Dropbox instead of
     // an app folder, change this value.
     final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
 
     ///////////////////////////////////////////////////////////////////////////
     //                      End app-specific settings.                       //
     ///////////////////////////////////////////////////////////////////////////
 
     // You don't need to change these, leave them alone.
     final static private String ACCOUNT_PREFS_NAME = "prefs";
     final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
     final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
 
 
     DropboxAPI<AndroidAuthSession> mApi;
     private boolean mLoggedIn;
 
     // Android widgets
     private LinearLayout mDisplay;
 
     private ImageView mImage;
 
     private final String PHOTO_DIR = "/Photos";
 
     final static private int NEW_PICTURE = 1;
     private String mCameraFileName;
 
     
     
     
     
     
     
     
     
     
     
 
     ImageManager mImageManager;
 
     private static boolean isSplashShown = false;
 
     private String query;
 
     private Context mContext;
 
     private TextView textView;
 
     private ProgressBar progressBar;
 
     /**
      * Simple Dialog used to show the splash screen.
      */
     protected Dialog mSplashDialog;
 
     @Override
     protected void onDestroy() {
         super.onStop();
         mImageManager.clear();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mContext = this;
 
         if (savedInstanceState != null) {
             mCameraFileName = savedInstanceState.getString("mCameraFileName");
         }
 
         // We create a new AuthSession so that we can use the Dropbox API.
         AndroidAuthSession session = buildSession();
         mApi = new DropboxAPI<AndroidAuthSession>(session);
         
         checkAppKeySetup();
 
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert = new AlertDialog.Builder(this);
 
         alert.setTitle("Authentication");
         alert.setMessage("Authentication");
             
 /*        
      // Set an EditText view to get user input 
         final EditText input = new EditText(this);
         input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
         input.setText("");
         input.requestFocus();
         input.setSelection(input.getText().length());
         alert.setView(input);
 */
         
     	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {        
         
             	
          setLoggedIn(mApi.getSession().isLinked());
          
         // This logs you out if you're logged in, or vice versa
         if (mLoggedIn) {
          //  logOut();
         } else {
             // Start the remote authentication
             mApi.getSession().startAuthentication(PartyTvServer.this);
         }
         
 ///////////
       mImageManager = ImageManager.getInstance(mContext);
       try {
           handleIntent(getIntent());
       } catch (final IOException e) {
           e.printStackTrace();
       } catch (final URISyntaxException e) {
           e.printStackTrace();
       } catch (final JSONException e) {
           e.printStackTrace();
       }
 
       if (!isSplashShown) {
           setContentView(R.layout.splash_screen);
           isSplashShown = true;
           CountDownTimer timer = new CountDownTimer(3000, 1000) {
               public void onTick(long millisUntilFinished) {
               }
 
               public void onFinish() {
                   initGridView();
               }
           }.start();
       } else {	
       	initGridView();  	       	
        }
       ///////
 
   
         
             }
         });
          
         
         alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 }
           });
  
         
         alert.show();
         
         
                 
         
     }
     @Override
     protected void onResume() {
         super.onResume();
         AndroidAuthSession session = mApi.getSession();
 
         // The next part must be inserted in the onResume() method of the
         // activity from which session.startAuthentication() was called, so
         // that Dropbox authentication completes properly.
         if (session.authenticationSuccessful()) {
             try {
                 // Mandatory call to complete the auth
                 session.finishAuthentication();
 
                 // Store it locally in our app for later use
                 TokenPair tokens = session.getAccessTokenPair();
                 storeKeys(tokens.key, tokens.secret);
                 setLoggedIn(true);
             } catch (IllegalStateException e) {
                 showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                 //Log.i(TAG, "Error authenticating", e);
             }
         }
     }
 
     private GridView gridView;
 
     private void initGridView() {
         setContentView(R.layout.image_grid);
         gridView = (GridView) findViewById(R.id.gridview);
         final ImageAdapter imageAdapter = new ImageAdapter(mContext);
         gridView.setAdapter(imageAdapter);
         progressBar = (ProgressBar) findViewById(R.id.a_progressbar);
         gridView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 // Create an intent to show a particular item.
                 final Intent i = new Intent(PartyTvServer.this, ViewImage.class);
                 i.putExtra(ImageManager.PANORAMIO_ITEM_EXTRA, position);
                 startActivity(i);
             }
         });
         gridView.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
             public void onChildViewAdded(View parent, View child) {
                 progressBar.setVisibility(View.INVISIBLE);
                 ((ViewGroup) parent).getChildAt(0).setSelected(true);
             }
 
             public void onChildViewRemoved(View parent, View child) {
             }
         });
 
         textView = (TextView) findViewById(R.id.place_name);
         textView.setText(query);
         PanoramioLeftNavService.getLeftNavBar(this);
         gridView.requestFocus();
     }
 
     public void onClick(View view) {
         onSearchRequested();
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         setIntent(intent);
         progressBar.setVisibility(View.VISIBLE);
         try {
             handleIntent(intent);
         } catch (final IOException e) {
             e.printStackTrace();
         } catch (final URISyntaxException e) {
             e.printStackTrace();
         } catch (final JSONException e) {
             e.printStackTrace();
         }
         initGridView();
     }
 
     private void handleIntent(Intent intent) throws IOException, URISyntaxException, JSONException {
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             query = intent.getStringExtra(SearchManager.QUERY);
         } else {
             query = intent.getStringExtra("query");
         }
 
         if (query == null || query.isEmpty()) {
             query = DEFAULT_QUERY;
         }
         // Start downloading
         mImageManager.load(query);
     }
     
 
     private void logOut() {
         // Remove credentials from the session
         mApi.getSession().unlink();
 
         // Clear our stored keys
         clearKeys();
         // Change UI state to display logged out version
         setLoggedIn(false);
     }
 
     /**
      * Convenience function to change UI state based on being logged in
      */
     
     private void setLoggedIn(boolean loggedIn) {
     	mLoggedIn = loggedIn;
     	if (loggedIn) {
     		//mSubmit.setText("Unlink from Dropbox");
//            mDisplay.setVisibility(View.VISIBLE);
     	} else {
     		//mSubmit.setText("Link with Dropbox");
//            mDisplay.setVisibility(View.GONE);
//            mImage.setImageDrawable(null);
     	}
     }
 
     private void checkAppKeySetup() {
         // Check to make sure that we have a valid app key
         if (APP_KEY.startsWith("CHANGE") ||
                 APP_SECRET.startsWith("CHANGE")) {
             showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
             finish();
             return;
         }
 
         // Check if the app has set up its manifest properly.
         Intent testIntent = new Intent(Intent.ACTION_VIEW);
         String scheme = "db-" + APP_KEY;
         String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
         testIntent.setData(Uri.parse(uri));
         PackageManager pm = getPackageManager();
         if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
             showToast("URL scheme in your app's " +
                     "manifest is not set up correctly. You should have a " +
                     "com.dropbox.client2.android.AuthActivity with the " +
                     "scheme: " + scheme);
             finish();
         }
     }
 
     private void showToast(String msg) {
         Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
         error.show();
     }
 
     /**
      * Shows keeping the access keys returned from Trusted Authenticator in a local
      * store, rather than storing user name & password, and re-authenticating each
      * time (which is not to be done, ever).
      *
      * @return Array of [access_key, access_secret], or null if none stored
      */
     private String[] getKeys() {
         SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
         String key = prefs.getString(ACCESS_KEY_NAME, null);
         String secret = prefs.getString(ACCESS_SECRET_NAME, null);
         if (key != null && secret != null) {
         	String[] ret = new String[2];
         	ret[0] = key;
         	ret[1] = secret;
         	return ret;
         } else {
         	return null;
         }
     }
 
     /**
      * Shows keeping the access keys returned from Trusted Authenticator in a local
      * store, rather than storing user name & password, and re-authenticating each
      * time (which is not to be done, ever).
      */
     private void storeKeys(String key, String secret) {
         // Save the access key for later
         SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
         Editor edit = prefs.edit();
         edit.putString(ACCESS_KEY_NAME, key);
         edit.putString(ACCESS_SECRET_NAME, secret);
         edit.commit();
     }
 
     private void clearKeys() {
         SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
         Editor edit = prefs.edit();
         edit.clear();
         edit.commit();
     }
     
     
     private AndroidAuthSession buildSession() {
         AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
         AndroidAuthSession session;
 
         String[] stored = getKeys();
         if (stored != null) {
             AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
             session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
         } else {
             session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
         }
 
         return session;
     }
 }
