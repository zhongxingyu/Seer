 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 package com.janrain.android.simpledemo;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import com.janrain.android.Jump;
 import com.janrain.android.capture.Capture;
 import com.janrain.android.capture.CaptureApiError;
 import com.janrain.android.capture.CaptureRecord;
 import org.json.JSONException;
 
 import static com.janrain.android.simpledemo.R.id.update_profile_display_name;
 import static com.janrain.android.simpledemo.R.id.update_profile_email;
 import static com.janrain.android.simpledemo.R.id.update_profile_first_name;
 import static com.janrain.android.simpledemo.R.id.update_profile_last_name;
 import static com.janrain.android.simpledemo.R.id.update_profile_about;
 
 public class UpdateProfileActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.update_profile_activity);
 
         setTitle("Update Profile");
 
         CaptureRecord user = Jump.getSignedInUser();
 
        setEditTextString(update_profile_email, user.optString("email"));
        setEditTextString(update_profile_display_name, user.optString("displayName"));
        setEditTextString(update_profile_last_name, user.optString("familyName"));
        setEditTextString(update_profile_first_name, user.optString("givenName"));
        setEditTextString(update_profile_about, user.optString("aboutMe"));
     }
 
     public void update(View view) {
         CaptureRecord user = Jump.getSignedInUser();
 
         String email = getEditTextString(update_profile_email);
         String firstName = getEditTextString(update_profile_first_name);
         String lastName = getEditTextString(update_profile_last_name);
         String displayName = getEditTextString(update_profile_display_name);
         String about = getEditTextString(update_profile_about);
 
         try {
             user.put("email", email);
             user.put("displayName", displayName);
             user.put("givenName", firstName);
             user.put("familyName", lastName);
             user.put("aboutMe", about);
         } catch (JSONException e) {
             throw new RuntimeException("Unexpected ", e);
         }
 
         Capture.updateUserProfile(user, new Capture.CaptureApiRequestCallback() {
 
             public void onSuccess() {
                 Toast.makeText(UpdateProfileActivity.this, "Profile Updated", Toast.LENGTH_LONG).show();
                 finish();
             }
 
             public void onFailure(CaptureApiError error) {
                 AlertDialog.Builder adb = new AlertDialog.Builder(UpdateProfileActivity.this);
                 adb.setTitle("Error");
                 adb.setMessage(error.toString());
                 adb.show();
             }
         });
     }
 
     private String getEditTextString(int layoutId) {
         return ((EditText) findViewById(layoutId)).getText().toString();
     }
 
     private void setEditTextString(int layoutId, String value) {
         ((EditText) findViewById(layoutId)).setText(value);
     }
 }
