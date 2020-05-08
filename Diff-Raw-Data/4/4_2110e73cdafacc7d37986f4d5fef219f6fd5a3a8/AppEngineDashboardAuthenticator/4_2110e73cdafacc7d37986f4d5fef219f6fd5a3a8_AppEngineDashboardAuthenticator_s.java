 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.friedran.appengine.dashboard.client;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 import com.friedran.appengine.dashboard.utils.LogUtils;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.IOException;
 
 public class AppEngineDashboardAuthenticator {
     public static final String AUTH_TOKEN_TYPE = "ah";
     protected Account mAccount;
     protected DefaultHttpClient mHttpClient;
     protected Context mApplicationContext;
     protected OnUserInputRequiredCallback mOnUserInputRequiredCallback;
     protected PostAuthenticateCallback mPostAuthenticateCallback;
 
     public interface OnUserInputRequiredCallback {
         public void onUserInputRequired(Intent accountManagerIntent);
     }
 
     public interface PostAuthenticateCallback {
         public void run(boolean result);
     }
 
     public AppEngineDashboardAuthenticator(Account account, DefaultHttpClient httpClient, Context context,
                                            OnUserInputRequiredCallback userInputRequiredCallback,
                                            PostAuthenticateCallback postAuthenticateCallback) {
         mAccount = account;
         mHttpClient = httpClient;
         mApplicationContext = context.getApplicationContext();
         mOnUserInputRequiredCallback = userInputRequiredCallback;
         mPostAuthenticateCallback = postAuthenticateCallback;
     }
 
     public void executeAuthentication() {
        // Gets the auth token asynchronously, calling the callback with its result (uses the deprecated API which is the only
        // one supported from API level 5).
         AccountManager.get(mApplicationContext).getAuthToken(mAccount, AUTH_TOKEN_TYPE, false, new AccountManagerCallback<Bundle>() {
             public void run(AccountManagerFuture result) {
                 Bundle bundle;
                 try {
                     LogUtils.i("AppEngineDashboardAuthenticator", "GetAuthTokenCallback.onPostExecute started...");
                     bundle = (Bundle) result.getResult();
                     Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
                     if(intent != null) {
                         // User input required
                         LogUtils.i("AppEngineDashboardAuthenticator", "User input is required...");
                         mOnUserInputRequiredCallback.onUserInputRequired(intent);
                     } else {
                         LogUtils.i("AppEngineDashboardAuthenticator", "Authenticated, getting auth token...");
                         onGetAuthToken(bundle);
                     }
                 } catch (Exception e) {
                     // Can happen because of various like connectivity issues, google server errors, etc.
                     LogUtils.e("AppEngineDashboardAuthenticator", "Exception caught from GetAuthTokenCallback", e);
                     mPostAuthenticateCallback.run(false);
                 }
             }
         }, null);
     }
 
     protected void onGetAuthToken(Bundle bundle) {
         String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
         LogUtils.i("AppEngineDashboardAuthenticator", "onGetAuthToken: Got the auth token " + authToken);
 
         if (authToken == null) {
             // Failure, looks like an illegal account
             mPostAuthenticateCallback.run(false);
         } else {
             new LoginToAppEngineTask().execute(authToken);
         }
     }
 
     private class LoginToAppEngineTask extends AsyncTask<String, Void, Boolean> {
         @Override
         protected Boolean doInBackground(String... params) {
             try {
                 LogUtils.i("AppEngineDashboardAuthenticator", "LoginToAppEngine starting...");
                 String authToken = params[0];
 
                 // Don't follow redirects
                 mHttpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
 
                 String url = "https://appengine.google.com/_ah/login?continue=http://localhost/&auth=" + authToken;
                 LogUtils.i("LoginToAppEngineTask", "Executing GET request: " + url);
                 HttpGet httpGet = new HttpGet(url);
                 HttpResponse response;
                 response = mHttpClient.execute(httpGet);
                 response.getEntity().consumeContent();
                 if(response.getStatusLine().getStatusCode() != 302)
                     // Response should be a redirect
                     return false;
 
                 for(Cookie cookie : mHttpClient.getCookieStore().getCookies()) {
                     LogUtils.i("LoginToAppEngineTask", "Cookie name: " + cookie.getName());
                     if(cookie.getName().equals("SACSID"))
                         return true;
                 }
 
                 throw new IOException("LoginToAppEngineTask failed: SACSID Cookie not found");
 
             } catch (IOException e) {
                 LogUtils.e("LoginToAppEngineTask", "IOException caught from authenticator logic", e);
             } finally {
                 mHttpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
             }
             LogUtils.e("AppEngineDashboardAuthenticator", "LoginToAppEngine failed...");
 
             return false;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             LogUtils.i("AppEngineDashboardAuthenticator", "LoginToAppEngine onPostExecute");
             mPostAuthenticateCallback.run(result);
         }
     }
 }
