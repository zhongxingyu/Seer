 package com.janrain.android.engage;
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
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import com.janrain.android.utils.LogUtils;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import static com.janrain.android.engage.JRNativeAuth.NativeProvider;
 import static com.janrain.android.engage.JRNativeAuth.NativeAuthError;
 
 public class NativeGooglePlus extends NativeProvider {
     private static Class plusClientClass;
     private static Class plusClientBuilderClass;
     private static Class connectionCallbackClass;
     private static Class connectionFailedListenerClass;
     private static Class connectionResultClass;
     private static Class playServicesUtilClass;
     private static Class googleAuthUtilClass;
     private static Class userRecoverableAuthExceptionClass;
     private static Class googleAuthExceptionClass;
     private static Class onAccessRevokedListenerClass;
     private static boolean didLoadClasses = false;
 
     private static final int REQUEST_CODE_RESOLVE_ERROR = 9000;
     private static final int RESULT_OK = -1;
     private static final int RESULT_SUCCESS = 0;
     private static final int SERVICE_MISSING_CONNECTION_RESULT = 1;
     private static final int SERVICE_VERSION_UPDATE_REQUIRED_CONNECTION_RESULT = 2;
     private static final int SERVICE_DISABLED_CONNECTION_RESULT = 3;
 
     private GooglePlusFragment googlePlusFragment;
     private String[] scopes;
     private boolean isConnecting = false;
 
     static {
         try {
             plusClientClass = Class.forName("com.google.android.gms.plus.PlusClient");
             plusClientBuilderClass = Class.forName("com.google.android.gms.plus.PlusClient$Builder");
             connectionCallbackClass = Class.forName(
                     "com.google.android.gms.common.GooglePlayServicesClient$ConnectionCallbacks");
             connectionFailedListenerClass = Class.forName(
                     "com.google.android.gms.common.GooglePlayServicesClient$OnConnectionFailedListener");
             connectionResultClass = Class.forName("com.google.android.gms.common.ConnectionResult");
             playServicesUtilClass = Class.forName(
                     "com.google.android.gms.common.GooglePlayServicesUtil");
             googleAuthUtilClass = Class.forName("com.google.android.gms.auth.GoogleAuthUtil");
             userRecoverableAuthExceptionClass = Class.forName(
                     "com.google.android.gms.auth.UserRecoverableAuthException");
             googleAuthExceptionClass = Class.forName(
                     "com.google.android.gms.auth.GoogleAuthException");
             onAccessRevokedListenerClass = Class.forName(
                     "com.google.android.gms.plus.PlusClient$OnAccessRevokedListener");
             didLoadClasses = true;
         } catch (ClassNotFoundException e) {
             LogUtils.logd("Could not load Native Google+ SDK" + e);
         }
     }
 
     /*package*/ static boolean canHandleAuthentication() {
         return didLoadClasses;
     }
 
     /*package*/ NativeGooglePlus(FragmentActivity activity, JRNativeAuth.NativeAuthCallback callback) {
         super(activity, callback);
         scopes = new String[] {"https://www.googleapis.com/auth/plus.login"};
     }
 
     @Override
     public String provider() {
         return "googleplus";
     }
 
     @Override
     public void startAuthentication() {
         int isGooglePlayAvailable = isGooglePlayAvailable();
 
         if (isGooglePlayAvailable != RESULT_SUCCESS) {
             if (shouldShowUnavailableDialog(isGooglePlayAvailable)) {
                 showGooglePlayUnavailableDialog(isGooglePlayAvailable);
             } else {
                 completion.onFailure("Google Play unavailable", NativeAuthError.GOOGLE_PLAY_UNAVAILABLE, true);
             }
         } else {
             googlePlusFragment = new GooglePlusFragment();
             googlePlusFragment.shouldSignIn = true;
             FragmentManager fragmentManager = fromActivity.getSupportFragmentManager();
             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
             fragmentTransaction.add(googlePlusFragment, "com.janrain.android.googleplusfragment");
             fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
             fragmentTransaction.commit();
         }
     }
 
     @Override
     public void signOut() {
         if (isGooglePlayAvailable() == RESULT_SUCCESS) {
             googlePlusFragment = new GooglePlusFragment();
             googlePlusFragment.shouldSignOut = true;
             FragmentManager fragmentManager = fromActivity.getSupportFragmentManager();
             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
             fragmentTransaction.add(googlePlusFragment, "com.janrain.android.googleplusfragment");
             fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
             fragmentTransaction.commit();
         }
     }
 
     @Override
     public void revoke() {
         if (isGooglePlayAvailable() == RESULT_SUCCESS) {
             googlePlusFragment = new GooglePlusFragment();
             googlePlusFragment.shouldDisconnect = true;
             FragmentManager fragmentManager = fromActivity.getSupportFragmentManager();
             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
             fragmentTransaction.add(googlePlusFragment, "com.janrain.android.googleplusfragment");
             fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
             fragmentTransaction.commit();
         }
     }
 
     @Override
     public void onActivityResult(int requestCode, int responseCode, Intent data) {
         googlePlusFragment.onActivityResult(requestCode, responseCode, data);
     }
 
     private int isGooglePlayAvailable() {
         int isGooglePlayAvailable = 0;
         try {
             Method isGooglePlayServicesAvailable =
                     playServicesUtilClass.getMethod("isGooglePlayServicesAvailable", Context.class);
             Object isAvailable = isGooglePlayServicesAvailable.invoke(playServicesUtilClass, fromActivity);
             isGooglePlayAvailable = (Integer)isAvailable;
         } catch (NoSuchMethodException e) {
             throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
 
         return isGooglePlayAvailable;
     }
 
     private boolean shouldShowUnavailableDialog(int googlePlayAvailabilityStatus) {
         return !JREngage.shouldTryWebViewAuthenticationWhenGooglePlayIsUnavailable()
             && (googlePlayAvailabilityStatus == SERVICE_MISSING_CONNECTION_RESULT
                 || googlePlayAvailabilityStatus == SERVICE_VERSION_UPDATE_REQUIRED_CONNECTION_RESULT
                 || googlePlayAvailabilityStatus == SERVICE_DISABLED_CONNECTION_RESULT);
     }
 
     private void showGooglePlayUnavailableDialog(int googlePlayAvailabilityStatus) {
         Dialog dialog = null;
         try {
             Method getErrorDialog = playServicesUtilClass.getMethod("getErrorDialog", int.class,
                     Activity.class, int.class);
             dialog = (Dialog)getErrorDialog.invoke(playServicesUtilClass, googlePlayAvailabilityStatus,
                     fromActivity, REQUEST_CODE_RESOLVE_ERROR);
         } catch (NoSuchMethodException e) {
             throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
 
         if (dialog != null) {
             dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                 @Override
                 public void onDismiss(DialogInterface dialog) {
                     completion.onFailure("Google Play unavailable", NativeAuthError.GOOGLE_PLAY_UNAVAILABLE);
                 }
             });
             dialog.show();
         } else {
             throw new RuntimeException("Unable to instantiate Google Play error dialog");
         }
     }
 
     private class GooglePlusFragment extends Fragment {
         private GooglePlusClient mPlusClient;
         private Object mConnectionResult;
         private boolean isSigningIn = false;
 
         public boolean shouldSignIn = false;
         public boolean shouldSignOut = false;
         public boolean shouldDisconnect = false;
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             LogUtils.logd("GooglePlusFragment onCreate");
             mPlusClient = new GooglePlusClient(fromActivity, getConnectionCallback(),
                     getOnConnectFailedListener(), scopes);
 
             if (shouldSignIn) {
                 signInPlusClient();
             }
         }
 
         @Override
         public void onStart() {
             super.onStart();
             LogUtils.logd("GooglePlusFragment onStart");
 
             mPlusClient.connect();
         }
 
         @Override
         public void onStop() {
             super.onStop();
             LogUtils.logd("GooglePlusFragment onStart");
 
             mPlusClient.disconnect();
         }
 
         @Override
         public void onActivityResult(int requestCode, int responseCode, Intent data) {
             if (requestCode == REQUEST_CODE_RESOLVE_ERROR) {
                 if (responseCode == RESULT_OK) {
                     mConnectionResult = null;
                     mPlusClient.connect();
                 } else {
                     completion.onFailure("Could not resolve Google+ result",
                                          NativeAuthError.COULD_NOT_RESOLVE_GOOGLE_PLUS_RESULT);
                 }
             }
         }
 
         public void signInPlusClient() {
             if (mPlusClient == null) {
                 completion.onFailure("Could not instantiate Google Plus Client",
                                      NativeAuthError.CANNOT_INSTANTIATE_GOOGLE_PLAY_CLIENT);
                 return;
             }
 
             isSigningIn = true;
 
             if (!mPlusClient.isConnected()) {
                 if (mConnectionResult == null) {
                     isConnecting = true;
                 } else {
                     startResolutionForResult();
                 }
             } else {
                 isSigningIn = false;
                 new GetAccessTokenTask().execute();
             }
         }
 
         private void signOutPlusClient() {
             if (mPlusClient.isConnected()) {
                 mPlusClient.clearDefaultAccount();
                 mPlusClient.disconnect();
                 mPlusClient.connect();
             }
             completion.onSuccess(null);
         }
 
         private void disconnectGooglePlusClient() {
             if (mPlusClient.isConnected()) {
                 mPlusClient.clearDefaultAccount();
                 mPlusClient.revokeAccessAndDisconnect(getAccessRevokedListener());
             }
         }
 
         private Object getConnectionCallback() {
             InvocationHandler handler = new InvocationHandler() {
                 @Override
                 public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                     LogUtils.logd("Method Name: " + method.getName());
                     if (method.getName().equals("onConnected")) {
                         // onConnected(Bundle connectionHint)
                         LogUtils.logd("onConnected");
                         isConnecting = false;
                         if (isSigningIn) {
                             new GetAccessTokenTask().execute();
                             isSigningIn = false;
                         }
                         if (shouldSignOut) {
                             signOutPlusClient();
                         }
                         if (shouldDisconnect) {
                             disconnectGooglePlusClient();
                         }
                     } else if (method.getName().equals("onDisconnected")) {
                         // onDisconnected()
                         LogUtils.logd("onDisconnected");
                         completion.onFailure("Google Plus Disconnected",
                                              NativeAuthError.GOOGLE_PLUS_DISCONNECTED);
                     }
                     return null;
                 }
             };
 
             return Proxy.newProxyInstance(
                     connectionCallbackClass.getClassLoader(),
                     new Class[]{connectionCallbackClass}, handler);
         }
 
         private Object getOnConnectFailedListener() {
             InvocationHandler handler = new InvocationHandler() {
                 @Override
                 public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                     if (method.getName().equals("onConnectionFailed")) {
                         // onConnectionFailed(ConnectionResult result)
                         mConnectionResult = objects[0];
 
                         if (isConnecting && connectionResultHasResolution()) {
                             startResolutionForResult();
                         } else {
                             completion.onFailure("Could not resolve Google+ result",
                                     NativeAuthError.COULD_NOT_RESOLVE_GOOGLE_PLUS_RESULT);
                         }
                     } else if (method.getName().equals("equals")) {
                         return (o == objects[0]);
                     }
                     return null;
                 }
             };
             return Proxy.newProxyInstance(
                     connectionFailedListenerClass.getClassLoader(),
                     new Class[]{connectionFailedListenerClass}, handler);
         }
 
         private Object getAccessRevokedListener() {
             InvocationHandler handler = new InvocationHandler() {
                 @Override
                 public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                     if (method.getName().equals("onAccessRevoked")) {
                         // onAccessRevoked(ConnectionResult status)
                         completion.onSuccess(null);
                     }
                     return null;
                 }
             };
 
             return Proxy.newProxyInstance(
                     onAccessRevokedListenerClass.getClassLoader(),
                     new Class[]{onAccessRevokedListenerClass}, handler);
         }
 
         private Boolean connectionResultHasResolution() {
             Object hasResolution = false;
 
             try {
                 Method resultHasResolution = connectionResultClass.getMethod("hasResolution");
                 hasResolution = resultHasResolution.invoke(mConnectionResult);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
 
             return (Boolean)hasResolution;
         }
 
         private void startResolutionForResult() {
             try {
                 Method startResolution = connectionResultClass.getMethod("startResolutionForResult",
                         Activity.class, int.class);
                 startResolution.invoke(mConnectionResult, fromActivity, REQUEST_CODE_RESOLVE_ERROR);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 if (e.getCause() instanceof IntentSender.SendIntentException) {
                     // Try connecting again
                     mConnectionResult = null;
                     mPlusClient.connect();
                 } else {
                     throw new RuntimeException(e);
                 }
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         }
 
         private void handleUserRecoverableAuthException(InvocationTargetException exception) {
             Intent intent = null;
 
             try {
                 Method getIntent = userRecoverableAuthExceptionClass.getMethod("getIntent");
                 intent = (Intent)getIntent.invoke(userRecoverableAuthExceptionClass);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
 
             fromActivity.startActivityForResult(intent, REQUEST_CODE_RESOLVE_ERROR);
         }
 
         private class GetAccessTokenTask extends AsyncTask<Void, Void, String> {
 
             @Override
             protected String doInBackground(Void... params) {
                 Object token = null;
 
                 try {
                     Method getToken = googleAuthUtilClass.getMethod("getToken", Context.class, String.class,
                             String.class);
                     token = getToken.invoke(googleAuthUtilClass, fromActivity, mPlusClient.accountName(),
                             "oauth2:" + TextUtils.join(" ", scopes));
                 } catch (NoSuchMethodException e) {
                     throw new RuntimeException(e);
                 } catch (InvocationTargetException e) {
                     if (e.getCause() instanceof  IOException) {
                         completion.onFailure("Could not get Google+ Access Token",
                                 NativeAuthError.CANNOT_GET_GOOGLE_PLUS_ACCESS_TOKEN, e);
                         return null;
                     } else if (userRecoverableAuthExceptionClass.isInstance(e.getCause())) {
                         LogUtils.logd("UserRecoverableAuthException");
                         token = null;
                         handleUserRecoverableAuthException(e);
                     } else if (googleAuthExceptionClass.isInstance(e.getCause())) {
                         throw new RuntimeException(e);
                     }
                 } catch (IllegalAccessException e) {
                     throw new RuntimeException(e);
                 }
 
                 LogUtils.logd("token: " + (String)token);
                 return (String)token;
             }
 
             @Override
             protected void onPostExecute(String token) {
                 LogUtils.logd("Got the token: " + token);
                 getAuthInfoTokenForAccessToken(token);
             }
         }
     }
 
     private class GooglePlusClient {
         Object plusClient;
 
         public GooglePlusClient(Context context, Object connectionCallback, Object onConnectFailedListener,
                 String[] scopes) {
 
             try {
                 Constructor constructor = plusClientBuilderClass.getConstructor(Context.class,
                         connectionCallbackClass,
                         connectionFailedListenerClass);
                 Object builder = constructor.newInstance(fromActivity, connectionCallback,
                         onConnectFailedListener);
 
                 Method setScopes = plusClientBuilderClass.getMethod("setScopes", String[].class);
                 setScopes.invoke(builder, new Object[] {scopes});
 
                 Method build = plusClientBuilderClass.getMethod("build");
                 plusClient = build.invoke(builder);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (SecurityException e) {
                 throw new RuntimeException(e);
             } catch (InstantiationException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             } catch (IllegalArgumentException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             }
         }
 
         public void connect() {
             LogUtils.logd("plusClient Connect");
 
             try {
                 Method connect = plusClientClass.getMethod("connect");
                 connect.invoke(plusClient);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         }
 
         public void disconnect() {
             LogUtils.logd("plusClient disconnect");
             try {
                 Method disconnect = plusClientClass.getMethod("disconnect");
                 disconnect.invoke(plusClient);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
 
         }
 
         public void revokeAccessAndDisconnect(Object listener) {
             try {
                 Method revokeAccessAndDisconnect =
                     plusClientClass.getMethod("revokeAccessAndDisconnect", onAccessRevokedListenerClass);
                 revokeAccessAndDisconnect.invoke(plusClient, listener);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         }
 
         public void clearDefaultAccount() {
             LogUtils.logd("plusClient clearDefaultAccount");
             try {
                 Method clearDefaultAccount = plusClientClass.getMethod("clearDefaultAccount");
                 clearDefaultAccount.invoke(plusClient);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
 
         }
 
         public Boolean isConnected() {
             Object isConnected = false;
             try {
                 Method isClientConnected = plusClientClass.getMethod("isConnected");
                 isConnected = isClientConnected.invoke(plusClient);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             }
 
             return (Boolean)isConnected;
         }
 
         private String accountName() {
 
             Object accountName;
 
             try {
                 Method getAccountName = plusClientClass.getMethod("getAccountName");
                 accountName = getAccountName.invoke(plusClient);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             }
 
             return (String)accountName;
         }
     }
 }
