 //
 //
 //  Copyright 2012 Kii Corporation
 //  http://kii.com
 //
 //  Licensed under the Apache License, Version 2.0 (the "License");
 //  you may not use this file except in compliance with the License.
 //  You may obtain a copy of the License at
 //
 //      http://www.apache.org/licenses/LICENSE-2.0
 //
 //  Unless required by applicable law or agreed to in writing, software
 //  distributed under the License is distributed on an "AS IS" BASIS,
 //  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //  See the License for the specific language governing permissions and
 //  limitations under the License.
 //  
 //
 
 package com.kii.cloud.board;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kii.cloud.board.cache.TopicCache;
 import com.kii.cloud.board.sdk.Constants;
 import com.kii.cloud.board.sdk.KiiBoardClient;
 import com.kii.cloud.board.utils.ProgressingDialog;
 import com.kii.cloud.storage.KiiUser;
 import com.kii.cloud.storage.callback.KiiUserCallBack;
 import com.kii.cloud.storage.exception.CloudExecutionException;
 
 public class SignupActivity extends Activity {
     ProgressingDialog progressing;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.signup);
         progressing = new ProgressingDialog(this);
     }
 
     public void handleCancel(View v) {
         Intent intent = new Intent(this, LoginActivity.class);
         startActivity(intent);
         finish();
     }
 
     public void handleSignUp(View v) {
         TextView usernameView = (TextView) findViewById(R.id.sync_sign_up_username_edit);
         TextView emailView = (TextView) findViewById(R.id.sync_sign_up_email_edit);
         TextView pwView = (TextView) findViewById(R.id.sync_login_password_edit);
         TextView cpwView = (TextView) findViewById(R.id.sync_confirm_password_edit);
 
         String username = usernameView.getText().toString();
         String email = emailView.getText().toString();
         String pw = pwView.getText().toString();
         String cpw = cpwView.getText().toString();
 
         if (!pw.equals(cpw)) {
             Toast.makeText(this, "Please check password input!",
                     Toast.LENGTH_SHORT).show();
             return;
         }
 
         try {
             KiiUser user = new KiiUser();
             user.setEmail(email);
             int token = asyncCreateUser(user, username, pw);
             progressing.showProcessing(token, "Creating new user");
         } catch (Exception e) {
             e.printStackTrace();
             Toast.makeText(this, "Please check email or username input!",
                     Toast.LENGTH_SHORT).show();
         }
 
     }
 
     private int asyncCreateUser(KiiUser user, final String username,
             final String pwd) {
         int token = user.register(new KiiUserCallBack() {
             @Override
             public void onRegisterCompleted(int token, boolean success,
                     KiiUser user, Exception exception) {
                 progressing.closeProgressDialog();
                 if (success) {
                     Toast.makeText(SignupActivity.this,
                             "Sign up successfully!", Toast.LENGTH_SHORT).show();
                     progressing.showProcessing(0, "User Logging...");
                     asyncUserLogin(user.getUsername(), pwd);
                 } else {
                     if (exception instanceof CloudExecutionException) {
                         CloudExecutionException cloudException = (CloudExecutionException) exception;
                         StringBuilder sb = new StringBuilder();
                         sb.append("Error:" + cloudException.getError());
                         sb.append("\n\n");
                         sb.append("Exception:" + cloudException.getException());
                         sb.append("\n\n");
                         sb.append("Error Details:"
                                 + cloudException.getErrorDetails());
                         String msg = sb.toString();
                         AlertDialog.Builder builder = new AlertDialog.Builder(
                                 SignupActivity.this);
                         builder.setTitle("Register Failed")
                                 .setMessage(msg)
                                 .setNegativeButton(
                                         getString(android.R.string.ok), null)
                                 .show();
                    } else {
                        exception.printStackTrace();
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                SignupActivity.this);
                        builder.setTitle("Register Failed")
                                .setMessage(exception.getMessage())
                                .setNegativeButton(
                                        getString(android.R.string.ok), null)
                                .show();
                     }
                 }
 
             }
 
         }, username, pwd);
 
         return token;
 
     }
 
     public void clearThreadCache() {
         getContentResolver().delete(TopicCache.CONTENT_URI, null, null);
     }
 
     private int asyncUserLogin(final String username, final String pwd) {
 
         int token = KiiUser.logIn(new KiiUserCallBack() {
             @Override
             public void onLoginCompleted(int token, boolean success,
                     KiiUser user, Exception exception) {
                 progressing.closeProgressDialog();
                 if (success) {
                     // check the old username, if username changed,
                     // clear the old cache!
                     String old_username = KiiBoardClient
                             .getLoginUserName(SignupActivity.this);
                     if (!old_username.equals(user.getUsername())) {
                         clearThreadCache();
                     }
 
                     KiiBoardClient.setLoginUserName(SignupActivity.this,
                             user.getUsername(), pwd);
                     Toast.makeText(SignupActivity.this, "Login successfully!",
                             Toast.LENGTH_SHORT).show();
 
                     Intent intent = new Intent(SignupActivity.this,
                             RemoteMessageListActivity.class);
                     intent.setAction(Constants.ACTION_REFRESH);
                     startActivity(intent);
                     finish();
                 } else {
                     Toast.makeText(SignupActivity.this,
                             "Login error, pls check & login again!",
                             Toast.LENGTH_SHORT).show();
                     Intent intent = new Intent(SignupActivity.this,
                             LoginActivity.class);
                     startActivity(intent);
                     finish();
                 }
 
             }
 
         }, username, pwd);
 
         return token;
 
     }
 }
