 package com.tabbie.android.radar;
 
 /**
  *  FacebookAuthenticator.java
  * 
  *  Created on: September 15, 2012
  *      Author: Valeri Karpov
  * 
  *  Takes care of our Facebook login state
  */
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.tabbie.android.radar.core.BasicCallback;
 
 public class FacebookAuthenticator {
   private final Facebook facebook;
   private final SharedPreferences preferences;
   
   private String fbAccessToken = "";
   private long expires = 0;
   
   /** Yo dawg, I heard you like Facebook, so I put a Facebook
    *  in your Facebook so you can Facebook while you Facebook
    */
   public FacebookAuthenticator(Facebook facebook, SharedPreferences preferences) {
     this.facebook = facebook;
     this.preferences = preferences;
   }
 
   public void authenticate(final Activity parent, final BasicCallback<String> callback) {
     fbAccessToken = preferences.getString("access_token", null);
     expires = preferences.getLong("access_expires", 0);
     
     if (fbAccessToken != null) {
       facebook.setAccessToken(fbAccessToken);
     }
 
     if (expires != 0) {
       facebook.setAccessExpires(expires);
     }
     
     if (facebook.isSessionValid()) {
       callback.onDone(fbAccessToken);
     } else {
       facebook.authorize(parent, new String[] { "email" }, new DialogListener() {
         public void onComplete(final Bundle values) {
           fbAccessToken = facebook.getAccessToken();
           expires = facebook.getAccessExpires();
           
           SharedPreferences.Editor editor = preferences.edit();
           editor.putString("access_token", fbAccessToken);
           editor.putLong("access_expires", expires);
           editor.commit();
           
           callback.onDone(fbAccessToken);
         }
 
         public void onFacebookError(final FacebookError e) {
          Toast.makeText(parent, "FACEBOOK ERROR", Toast.LENGTH_LONG).show();
          e.printStackTrace();
           callback.onFail("Facebook error");
         }
 
         public void onError(final DialogError e) {
          Toast.makeText(parent, "DIALOG ERROR", Toast.LENGTH_LONG).show();
           callback.onFail("DialogError");
         }
 
         public void onCancel() {
          Toast.makeText(parent, "CANCEL ERROR", Toast.LENGTH_LONG).show();
           callback.onFail("Canceled");
         }
       });
     }
   }
 }
