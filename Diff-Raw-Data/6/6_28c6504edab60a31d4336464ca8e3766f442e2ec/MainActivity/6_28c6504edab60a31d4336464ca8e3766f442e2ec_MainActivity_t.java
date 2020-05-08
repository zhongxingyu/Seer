 package com.fatsomadev.fatsomascanauthtest;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 import com.facebook.android.*;
 import com.facebook.android.Facebook.*;
 import com.facebook.android.AsyncFacebookRunner.*;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 public class MainActivity extends Activity {
 
     private final static String FB_APP_ID = "155216064540397";
 
     private SharedPreferences mPrefs;
 
     public final static String EXTRA_FB_VALUES = "com.fatsomadev.fatsomascanauthtest.FB_VALUES";
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         Utility.facebook = new Facebook(FB_APP_ID);
         Utility.asyncRunner = new AsyncFacebookRunner(Utility.facebook);
 
         // Get existing access_token if any
         mPrefs = getPreferences(MODE_PRIVATE);
         String access_token = mPrefs.getString("access_token", null);
         long expires = mPrefs.getLong("access_expires", 0);
         if (access_token != null) {
             Utility.facebook.setAccessToken(access_token);
         }
         if (expires != 0) {
             Utility.facebook.setAccessExpires(expires);
         }
     }
 
     /** Called when the user clicks the facebook login button */
     public void fbLogin(View view) {
         if (!Utility.facebook.isSessionValid()) {
             Utility.facebook.authorize(this, new String[] {}, new DialogListener() {
                 @Override
                 public void onComplete(Bundle values) {
                     SharedPreferences.Editor editor = mPrefs.edit();
                     editor.putString("access_token", Utility.facebook.getAccessToken());
                     editor.putLong("access_expires", Utility.facebook.getAccessExpires());
                     editor.commit();
                     showFacebookDetails();
                 }
 
                 @Override
                 public void onFacebookError(FacebookError error) {
                     Toast.makeText(getApplicationContext(),
                         "Facebook error: " + error.getMessage(),
                         Toast.LENGTH_LONG).show();
                 }
 
                 @Override
                 public void onError(DialogError e) {
                     Toast.makeText(getApplicationContext(),
                         "Error: " + e.getMessage(),
                         Toast.LENGTH_LONG).show();
                 }
 
                 @Override
                 public void onCancel() {
                     Toast.makeText(getApplicationContext(),
                         "FB Login cancelled",
                         Toast.LENGTH_LONG).show();
                 }
             });
         } else {
             Toast.makeText(getApplicationContext(),
                 "Already logged in to Facebook",
                 Toast.LENGTH_SHORT).show();
         }
     }
 
     /** Called when the user clicks the facebook logout button */
     public void fbLogout(View view) {
         Utility.asyncRunner.logout(this, new RequestListener() {
             @Override
             public void onComplete(String response, Object state) {
                 Toast.makeText(getApplicationContext(),
                     "Logged out from Facebook",
                     Toast.LENGTH_SHORT).show();
             }
 
             @Override
             public void onIOException(IOException e, Object state) {
                 Toast.makeText(getApplicationContext(),
                     "Error logging out: " + e.getMessage(),
                     Toast.LENGTH_LONG).show();
             }
 
             @Override
             public void onFileNotFoundException(FileNotFoundException e,
                                                 Object state) {
                 Toast.makeText(getApplicationContext(),
                     "Error logging out: " + e.getMessage(),
                     Toast.LENGTH_LONG).show();
             }
 
             @Override
             public void onMalformedURLException(MalformedURLException e,
                                                 Object state) {
                 Toast.makeText(getApplicationContext(),
                     "Malformed logout URL: " + e.getMessage(),
                     Toast.LENGTH_LONG).show();
             }
 
             @Override
            public void onFacebookError(FacebookError e, Object state) {
                Toast.makeText(getApplicationContext(),
                    "Error logging out: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
         });
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         Utility.facebook.authorizeCallback(requestCode, resultCode, data);
     }
 
     public void showFacebookDetails() {
         Bundle values = new Bundle();
         values.putString("access_token", Utility.facebook.getAccessToken());
         values.putLong("expires_in", Utility.facebook.getAccessExpires());
         showFacebookDetails(values);
     }
 
     public void showFacebookDetails(Bundle values) {
         Intent intent = new Intent(this, DisplayFacebookDetailsActivity.class);
         intent.putExtra(EXTRA_FB_VALUES, values);
         startActivity(intent);
     }
 }
