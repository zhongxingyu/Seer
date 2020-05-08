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
 package com.janrain.android;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import com.janrain.android.capture.JRCapture;
 import com.janrain.android.capture.JRCaptureRecord;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageDelegate;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.ui.JRCustomInterfaceConfiguration;
 import com.janrain.android.engage.ui.JRCustomInterfaceView;
 import org.json.JSONObject;
 
 
 import static com.janrain.android.Jump.SignInResultHandler.FailureReasons;
 
 public class Jump {
     private enum State {
         STATE;
 
         private JRCaptureRecord signedInUser;
         private JREngage jrEngage;
         private String captureDomain;
         private String captureClientId;
         private SignInResultHandler signInHandler;
     }
 
     private static final State state = State.STATE;
 
     public static void init(Context context, String engageAppId, String captureDomain,
                             String captureClientId) {
         state.jrEngage = JREngage.initInstance(context.getApplicationContext(), engageAppId, null, null);
         state.captureDomain = captureDomain;
         state.captureClientId = captureClientId;
         //j.showAuthenticationDialog();
         //j.addDelegate(); remove
         //j.cancelAuthentication();
         //j.createSocialPublishingFragment();
         //j.setAlwaysForceReauthentication();
         //j.setEnabledAuthenticationProviders();
         //j.setTokenUrl();
         //j.signoutUserForAllProviders();
     }
 
     public static String getCaptureDomain() {
         return state.captureDomain;
     }
 
     public static String getCaptureClientId() {
         return state.captureClientId;
     }
 
     public static JRCaptureRecord getSignedInUser() {
         if (state.signedInUser == null && JREngage.getApplicationContext() != null) {
             state.signedInUser = JRCaptureRecord.loadFromDisk(JREngage.getApplicationContext());
         }
         return state.signedInUser;
     }
 
     public static void showSignInDialog(Activity fromActivity, final SignInResultHandler handler) {
         if (state.jrEngage == null || state.captureDomain == null) {
             handler.onFailure(FailureReasons.jumpNotInitialized);
             return;
         }
 
         state.jrEngage.addDelegate(new JREngageDelegate.SimpleJREngageDelegate() {
             @Override
             public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
                 String authInfoToken = auth_info.getAsString("token");
 
                 //JRCapture.performSocialSignIn(authInfoToken, new JRCapture.FetchJsonCallback() {
                 JRCapture.performLegacySocialSignIn(authInfoToken, new JRCapture.FetchJsonCallback() {
                         public void run(JSONObject response) {
                         if (response == null) {
                             fireHandlerOnFailure(FailureReasons.invalidApiResponse);
                             return;
                         }
                         if ("ok".equals(response.opt("stat"))) {
                             Object user = response.opt("capture_user");
                             if (user instanceof JSONObject) {
                                 if (handler != null) {
                                     state.signedInUser = new JRCaptureRecord(((JSONObject) user));
                                     fireHandlerOnSuccess();
                                 }
                             } else {
                                 fireHandlerOnFailure(FailureReasons.invalidApiResponse);
                             }
                         } else {
                             fireHandlerOnFailure(response);
                         }
                     }
                 });
             }
 
             @Override
             public void jrAuthenticationDidNotComplete() {
                 fireHandlerOnFailure(FailureReasons.AuthenticationCanceledByUser);
             }
 
             @Override
             public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
                 fireHandlerOnFailure(error);
             }
 
             @Override
             public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
                 fireHandlerOnFailure(error);
             }
         });
 
         state.signInHandler = handler;
         state.jrEngage.showAuthenticationDialog(fromActivity, TradSignInUi.class);
     }
 
     private static void fireHandlerOnFailure(Object failureParam) {
         SignInResultHandler handler_ = state.signInHandler;
         state.signInHandler = null;
         if (handler_ != null) handler_.onFailure(failureParam);
 
     }
 
     private static void fireHandlerOnSuccess() {
         SignInResultHandler handler_ = state.signInHandler;
         state.signInHandler = null;
         if (handler_ != null) handler_.onSuccess();
 
     }
 
     public static class TradSignInUi extends JRCustomInterfaceConfiguration {
         public TradSignInUi() {
             this.mProviderListHeader = new JRCustomInterfaceView() {
                 @Override
                 public View onCreateView(Context context,
                                          LayoutInflater inflater,
                                          ViewGroup container,
                                          Bundle savedInstanceState) {
                     View v = inflater.inflate(R.layout.jr_capture_trad_signin, container, false);
                     final EditText userName = (EditText) v.findViewById(R.id.username_edit);
                     final EditText password = (EditText) v.findViewById(R.id.password_edit);
                     Button signIn = (Button) v.findViewById(R.id.custom_signin_button);
 
                     signIn.setOnClickListener(new View.OnClickListener() {
                         public void onClick(View v) {
                             final TradSignInHandler handler = 
                                     new TradSignInHandler(new SignInResultHandler() {
                                         public void onSuccess() {
                                             fireHandlerOnSuccess();
                                             dismissProgressIndicator();
                                             finishJrSignin();
                                         }
 
                                         public void onFailure(Object error) {
                                             dismissProgressIndicator();
                                             AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                                            //b.setNeutralButton(jr_dialog_dismiss, null);
                                            //b.setMessage(jr_capture_trad_signin_bad_password);
                                             b.show();
                                         }
                                     });
                             final JRConnectionManagerDelegate d =
                                     //JRCapture.performTraditionalSignIn(userName.getText().toString(),
                                     JRCapture.performLegacyTraditionalSignIn(userName.getText().toString(),
                                                     password.getText().toString(),
                                             TraditionalSignInType.EMAIL, handler);
                             showProgressIndicator(true, new DialogInterface.OnCancelListener() {
                                 public void onCancel(DialogInterface dialog) {
                                     handler.canceled = true;
                                     JRConnectionManager.stopConnectionsForDelegate(d);
                                 }
                             });
                         }
                     });
                     return v;
                 }
             };
         }
     }
 
     private static class TradSignInHandler implements JRCapture.FetchJsonCallback {
         private boolean canceled = false;
         private SignInResultHandler handler;
 
         private TradSignInHandler(SignInResultHandler handler) {
             this.handler = handler;
         }
 
         public void run(JSONObject response) {
             if (canceled) return;
             if (response == null) {
                 handler.onFailure(FailureReasons.invalidApiResponse);
                 return;
             }
             if ("ok".equals(response.opt("stat"))) {
                 Object user = response.opt("capture_user");
                 if (user instanceof JSONObject) {
                     state.signedInUser = new JRCaptureRecord(((JSONObject) user));
                     handler.onSuccess();
                 } else {
                     handler.onFailure(FailureReasons.invalidApiResponse);
                 }
             } else {
                 handler.onFailure(response);
             }
         }
     }
 
     public enum TraditionalSignInType { EMAIL, USERNAME }
 
     public static void performTraditionalSignIn(String username, String password,
                                                 TraditionalSignInType type,
                                                 SignInResultHandler handler) {
         if (state.jrEngage == null || state.captureDomain == null) {
             handler.onFailure(FailureReasons.jumpNotInitialized);
             return;
         }
 
         JRCapture.performTraditionalSignIn(username, password, type, new TradSignInHandler(handler));
     }
 
     public interface SignInResultHandler {
         public enum FailureReasons {
             invalidApiResponse, jumpNotInitialized, AuthenticationCanceledByUser
         }
 
         void onSuccess();
         void onFailure(Object error);
     }
 }
