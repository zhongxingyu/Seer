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
 
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.InputType;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.Toast;
 import com.janrain.android.Jump;
 import com.janrain.android.capture.CaptureApiError;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.utils.LogUtils;
 import org.json.JSONObject;
 
 import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
 import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
 import static com.janrain.android.capture.Capture.CaptureApiRequestCallback;
 
 public class MainActivity extends FragmentActivity {
 
     private boolean flowDownloaded = false;
 
     private class MySignInResultHandler implements Jump.SignInResultHandler, Jump.SignInCodeHandler {
         public void onSuccess() {
             AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
             b.setMessage("Sign-in complete.");
             b.setNeutralButton("Dismiss", null);
             b.show();
         }
 
         public void onCode(String code) {
             Toast.makeText(MainActivity.this, "Authorization Code: " + code, Toast.LENGTH_LONG).show();
         }
 
         public void onFailure(SignInError error) {
             if (error.reason == SignInError.FailureReason.CAPTURE_API_ERROR &&
                     error.captureApiError.isMergeFlowError()) {
                 // Called below is the default merge-flow handler. Merge behavior may also be implemented by
                 // headless-native-API for more control over the user experience.
                 //
                 // To do so, call Jump.showSignInDialog or Jump.performTraditionalSignIn directly, and
                 // pass in the merge-token and existing-provider-name retrieved from `error`.
                 //
                 // String mergeToken = error.captureApiError.getMergeToken();
                 // String existingProvider = error.captureApiError.getExistingAccountIdentityProvider()
                 //
                 // (An existing-provider-name of "capture" indicates a conflict with a traditional-sign-in
                 // account. You can handle this case yourself, by displaying a dialog and calling
                 // Jump.performTraditionalSignIn, or you can call Jump.showSignInDialog(..., "capture") and
                 // a library-provided dialog will be provided.)
 
                 Jump.startDefaultMergeFlowUi(MainActivity.this, error, signInResultHandler);
             } else if (error.reason == SignInError.FailureReason.CAPTURE_API_ERROR &&
                     error.captureApiError.isTwoStepRegFlowError()) {
                 // Called when a user cannot sign in because they have no record, but a two-step social
                 // registration is possible. (Which means that the error contains pre-filled form fields
                 // for the registration form.
                 Intent i = new Intent(MainActivity.this, RegistrationActivity.class);
                 JSONObject prefilledRecord = error.captureApiError.getPreregistrationRecord();
                 i.putExtra("preregistrationRecord", prefilledRecord.toString());
                 i.putExtra("socialRegistrationToken", error.captureApiError.getSocialRegistrationToken());
                 MainActivity.this.startActivity(i);
             } else {
                 AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                 b.setMessage("Sign-in failure:" + error);
                 b.setNeutralButton("Dismiss", null);
                 b.show();
             }
         }
     };
 
     private final MySignInResultHandler signInResultHandler = new MySignInResultHandler();
 
     private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
             b.setMessage("Could not download flow.");
             b.setNeutralButton("Dismiss", null);
             b.show();
         }
     };
 
     private final BroadcastReceiver flowMessageReceiver = new BroadcastReceiver() {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             flowDownloaded = true;
             Bundle extras = intent.getExtras();
             if (extras != null) {
                 String state = extras.getString("message");
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setMessage(state);
                b.setNeutralButton("Dismiss", null);
                b.show();
             }
 
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         //enableStrictMode();
 
         IntentFilter filter = new IntentFilter(Jump.JR_FAILED_TO_DOWNLOAD_FLOW);
         LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);
 
         IntentFilter flowFilter = new IntentFilter(Jump.JR_DOWNLOAD_FLOW_SUCCESS);
         LocalBroadcastManager.getInstance(this).registerReceiver(flowMessageReceiver, flowFilter);
 
         ScrollView sv = new ScrollView(this);
         sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 
         LinearLayout linearLayout = new LinearLayout(this);
         linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
         linearLayout.setOrientation(LinearLayout.VERTICAL);
 
         Button testAuth = addButton(linearLayout, "Capture Sign-In");
         addButton(linearLayout, "Test Direct Auth").setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if(flowDownloaded)
                 Jump.showSignInDialog(MainActivity.this, "linkedin", signInResultHandler, null);
             }
         });
         Button dumpRecord = addButton(linearLayout, "Dump Record to Log");
         Button editProfile = addButton(linearLayout, "Edit Profile");
         Button refreshToken = addButton(linearLayout, "Refresh Access Token");
         Button resendVerificationButton = addButton(linearLayout, "Resend Email Verification");
         Button link_unlinkAccount = addButton(linearLayout, "Link & Unlink Account");
         addButton(linearLayout, "Share").setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if(flowDownloaded)
                 JREngage.getInstance().showSocialPublishingDialog(MainActivity.this,
                         new JRActivityObject("aslkdfj", "http://google.com"));
             }
         });
 
         addButton(linearLayout, "Traditional Registration").setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if(flowDownloaded)
                 MainActivity.this.startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
             }
         });
 
         Button signOut = addButton(linearLayout, "Sign Out");
 
         sv.addView(linearLayout);
         setContentView(sv);
 
         testAuth.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if(flowDownloaded)
                 Jump.showSignInDialog(MainActivity.this, null, signInResultHandler, null);
             }
         });
 
         dumpRecord.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if(flowDownloaded)
                 LogUtils.logd(String.valueOf(Jump.getSignedInUser()));
             }
         });
 
         editProfile.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (Jump.getSignedInUser() == null) {
                     Toast.makeText(MainActivity.this, "Can't edit without record instance.",
                             Toast.LENGTH_LONG).show();
                     return;
                 }
                 Intent i = new Intent(MainActivity.this, UpdateProfileActivity.class);
                 MainActivity.this.startActivity(i);
             }
         });
 
         refreshToken.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (Jump.getSignedInUser() == null) {
                     Toast.makeText(MainActivity.this, "Cannot refresh token without signed in user",
                                    Toast.LENGTH_LONG).show();
                     return;
                 }
 
                 Jump.getSignedInUser().refreshAccessToken(new CaptureApiRequestCallback() {
                     public void onSuccess() {
                         Toast.makeText(MainActivity.this, "Access Token Refreshed",
                                 Toast.LENGTH_LONG).show();
                     }
 
                     public void onFailure(CaptureApiError e) {
                         Toast.makeText(MainActivity.this, "Failed to refresh access token",
                                 Toast.LENGTH_LONG).show();
                         LogUtils.loge(e.toString());
                     }
                 });
             }
         });
 
         resendVerificationButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 promptForResendVerificationEmailAddress();
             }
         });
 
         link_unlinkAccount.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (Jump.getSignedInUser() != null && Jump.getAccessToken() != null) {
                     MainActivity.this.startActivity(new Intent(MainActivity.this, LinkListActivity.class));
                 } else {
                     Toast.makeText(MainActivity.this, "Please Login to Link Account",
                             Toast.LENGTH_LONG).show();
                 }
             }
         });
 
         signOut.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Jump.signOutCaptureUser(MainActivity.this);
             }
         });
     }
 
     private void promptForResendVerificationEmailAddress() {
         final EditText input = new EditText(this);
         input.setInputType(InputType.TYPE_CLASS_TEXT);
 
         AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
         b.setView(input);
         b.setTitle("Please confirm your email address");
         b.setMessage("We'll resend your verification email.");
         b.setNegativeButton("Cancel", null);
         b.setPositiveButton("Send", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 String emailAddress = input.getText().toString();
                 sendVerificationEmail(emailAddress);
             }
         });
         b.show();
     }
 
     private void sendVerificationEmail(String emailAddress) {
         Jump.resendEmailVerification(emailAddress, new CaptureApiRequestCallback() {
 
             public void onSuccess() {
                 Toast.makeText(MainActivity.this, "Verification email sent.", Toast.LENGTH_LONG).show();
             }
 
             public void onFailure(CaptureApiError e) {
                 Toast.makeText(MainActivity.this, "Failed to send verification email: " + e,
                                Toast.LENGTH_LONG).show();
             }
         });
     }
 
     private Button addButton(LinearLayout linearLayout, String label) {
         Button button = new Button(this);
         button.setText(label);
         button.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
         linearLayout.addView(button);
         return button;
     }
 
     @Override
     protected void onPause() {
         Jump.saveToDisk(this);
         super.onPause();
     }
 
     private static void enableStrictMode() {
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                 .detectAll()
         //        .detectDiskReads()
         //        .detectDiskWrites()
         //        .detectNetwork()   // or .detectAll() for all detectable problems
                 .penaltyLog()
         //        .penaltyDeath()
                 .build());
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                 //.detectAll()
                 //.detectActivityLeaks()
                 //.detectLeakedSqlLiteObjects()
                 //.detectLeakedClosableObjects()
                 .penaltyLog()
                 //.penaltyDeath()
                 .build());
     }
 
     @Override
     protected void onDestroy() {
         LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
         LocalBroadcastManager.getInstance(this).unregisterReceiver(flowMessageReceiver);
         super.onDestroy();
     }
 }
