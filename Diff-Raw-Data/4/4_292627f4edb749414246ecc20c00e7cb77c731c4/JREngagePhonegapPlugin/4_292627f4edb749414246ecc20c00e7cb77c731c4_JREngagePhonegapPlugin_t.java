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
 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  Copyright (c) 2010, Janrain, Inc.
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation and/or
    other materials provided with the distribution.
  * Neither the name of the Janrain, Inc. nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.
 
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
  Author: lillialexis
  Date:   12/28/11
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.engage;
 
 
 import android.widget.Toast;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import org.json.JSONArray;
 import org.json.JSONException;
 import android.util.Log;
 import com.phonegap.api.Plugin;
 import com.phonegap.api.PluginResult;
 import com.phonegap.api.PluginResult.Status;
 
 import java.util.ArrayList;
 
 /**
  * Phonegap plugin for authenticating with Janrain Engage
  * <p>
  * result example - {"filename":"/sdcard","isdir":true,"children":[{"filename":"a.txt","isdir":false},{..}]}
  * </p>
  * <pre>
  * {@code
  * successCallback = function(result){
  *     //result is a json
  *
  * }
  * failureCallback = function(error){
  *     //error is error message
  * }
  *
  * </pre>
  * @author Lilli Szafranski and Nathan Ramsey
  *
  */
 public class JREngagePhonegapPlugin extends Plugin implements JREngageDelegate {
     private static String TAG = "[JREngagePhonegapPlugin]";
     private JREngage mJREngage;
     private boolean mWaitingForLibrary;
     private PluginResult mResult;
 
     private JRDictionary mFullAuthenticationResponse     = null;
     private JRDictionary mFullSharingResponse            = null;
     private ArrayList<JRDictionary> mAuthenticationBlobs = null;
     private ArrayList<JRDictionary> mShareBlobs          = null;
 
     private boolean mWeAreSharing = false;
 
     @Override
     public synchronized PluginResult execute(final String cmd, final JSONArray args, final String callback) {
         mWaitingForLibrary = true;
         ctx.runOnUiThread(new Runnable() { public void run() {
             try {
                 if (cmd.equals("print")) {
                     showToast(args.getString(0));
                 } else if (cmd.equals("initializeJREngage")) {
                     initializeJREngage(args.getString(0), args.getString(1));
                 } else if (cmd.equals("showAuthenticationDialog")) {
                     showAuthenticationDialog();
                 } else if (cmd.equals("showSharingDialog")) {
                     showSharingDialog(args.getString(0));
                 } else { // TODO: Formalize these errors into JSON objects that follow our convention
                     postResultAndCleanUp(new PluginResult(Status.INVALID_ACTION, "Unknown action: " + cmd));
                 }
             } catch (JSONException e) {
                 postResultAndCleanUp(new PluginResult(Status.JSON_EXCEPTION, "Error parsing arguments for " + cmd));
             }
         } });
 
         // TODO: Maybe need to add more thread protection (got into weird infinite loop);
         // Maybe this was fixed... haven't seen any weirdness in a while
         while (mWaitingForLibrary) {
             Log.d("[JREngagePhoneGapWrapper]", "mWaitingForLibrary = true");
             try {
                 wait();
             } catch (InterruptedException e) {
                 /* No exceptions are expected */
                 Log.e(TAG, "Interrupted exception: ", e); // TODO: Formalize these errors into JSON objects that follow our convention
                 return new PluginResult(Status.ERROR, "Unexpected InterruptedException: " + e);
             }
         }
 
         Log.d(TAG, "[JREngagePhoneGapWrapper] mWaitingForLibrary = false");
 
         return mResult;
     }
 
     private void saveTheAuthenticationBlobForLater() {
         if (mAuthenticationBlobs == null) 
             mAuthenticationBlobs = new ArrayList<JRDictionary>();
 
         mAuthenticationBlobs.add(mFullAuthenticationResponse);
         mFullAuthenticationResponse = null;
     }
 
     private synchronized void postResultAndCleanUp(PluginResult result) {
         mWeAreSharing               = false;
         mFullAuthenticationResponse = null;
         mFullSharingResponse        = null;
         mAuthenticationBlobs        = null;
         mShareBlobs                 = null;
 
         mResult            = result;
         mWaitingForLibrary = false;
 
         notifyAll();
     }
 
     private PluginResult buildSuccessResult(JRDictionary successDictionary) {
         String message = successDictionary.toJSON();
 
         JREngage.logd("[buildSuccessResult]", message);
         return new PluginResult(Status.OK, message);
     }
 
     private PluginResult buildFailureResult(JREngageError error) {
         return buildFailureResult(error.getCode(), error.getMessage());
     }
     
     private PluginResult buildFailureResult(int code, String message) {
         return new PluginResult(Status.ERROR, buildFailureString(code, message));
     }
     
     private String buildFailureString(int code, String message) {
         JRDictionary errorDictionary = new JRDictionary();
         errorDictionary.put("code", code);
         errorDictionary.put("message", message);
 
         return errorDictionary.toJSON();
     }
 
     private synchronized void showToast(final String message) {
         Toast myToast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
         myToast.show();
 
         postResultAndCleanUp(new PluginResult(PluginResult.Status.OK, message));
     }
 
     private synchronized void initializeJREngage(String appId, String tokenUrl) {
         JREngage.sLoggingEnabled = true;
         mJREngage = JREngage.initInstance(ctx, appId, tokenUrl, this);
 //        if (mJREngage == null)  // TODO: Change error messages
 //            mResult = new PluginResult(Status.ERROR, "init error");
 //        else
 //            mResult = new PluginResult(Status.OK, "Initializing JREngage...");
 //
 //        mFinishedPluginExecution = true;
 //        notifyAll();
 //
 //        return null;
 
         postResultAndCleanUp(new PluginResult(Status.OK, "Initializing JREngage..."));
     }
 
     private synchronized void showAuthenticationDialog() {
         mJREngage.showAuthenticationDialog();
     }
 
     private synchronized void showSharingDialog(String activityString) {
         try {
            mWeAreSharing = true;

             JRDictionary activityDictionary = JRDictionary.fromJSON(activityString);
             JRActivityObject activity = new JRActivityObject(activityDictionary);
             mJREngage.showSocialPublishingDialog(activity);
         } catch (JSONException e) { // TODO: Formalize these errors into JSON objects that follow our convention
             postResultAndCleanUp(new PluginResult(Status.JSON_EXCEPTION));
         }
     }
 
     public synchronized void jrEngageDialogDidFailToShowWithError(JREngageError error) {
         JREngage.logd(TAG, "[jrEngageDialogDidFailToShowWithError] " + error);
         postResultAndCleanUp(buildFailureResult(error));
     }
 
     /* Happens on user backing out of authentication, so report user cancellation */
     public synchronized void jrAuthenticationDidNotComplete() {
         JREngage.logd(TAG, "[jrAuthenticationDidNotComplete] User Canceled");
         postResultAndCleanUp(buildFailureResult(JREngageError.AuthenticationError.AUTHENTICATION_CANCELED,
                 "User canceled authentication"));
     }
 
     public synchronized void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
         JREngage.logd(TAG, "[jrAuthenticationDidFailWithError] " + error);
         // TODO: What if they fail during sharing??
         // TODO: Make sure the dialog doesn't close in this case if there's an error during sharing
         if (!mWeAreSharing)
             postResultAndCleanUp(buildFailureResult(error));
     }
 
     public synchronized void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                                    JREngageError error,
                                                                    String provider) {
         Log.e(TAG, "[jrAuthenticationCallToTokenUrlDidFail] ERROR");
         if (!mWeAreSharing)
             postResultAndCleanUp(buildFailureResult(error));
     }
 
     public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
         JREngage.logd(TAG, "[jrAuthenticationDidSucceedForUser] SUCCESS");
 
         auth_info.remove("stat");
 
         mFullAuthenticationResponse = new JRDictionary();
         mFullAuthenticationResponse.put("auth_info", auth_info);
         mFullAuthenticationResponse.put("provider", provider);
     }
 
     public synchronized void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                               HttpResponseHeaders response,
                                                               String tokenUrlPayload,
                                                               String provider) {
         JREngage.logd(TAG, "[jrAuthenticationDidReachTokenUrl] SUCCESS");
 
         mFullAuthenticationResponse.put("tokenUrl", tokenUrl);
         mFullAuthenticationResponse.put("tokenUrlPayload", tokenUrlPayload);
         mFullAuthenticationResponse.put("stat", "ok");
 
         if (mWeAreSharing)
             saveTheAuthenticationBlobForLater();
         else
             postResultAndCleanUp(buildSuccessResult(mFullAuthenticationResponse));
     }
 
 
     public void jrSocialDidNotCompletePublishing() {
         JREngage.logd(TAG, "[jrSocialDidNotCompletePublishing] User Canceled");
         // TODO: Synchronize the errors between iOS and Android!!!
         postResultAndCleanUp(buildFailureResult(JREngageError.SocialPublishingError.CANCELED_ERROR,
                 "User canceled authentication"));
     }
 
     public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                  JREngageError error,
                                                  String provider) {
         JREngage.logd(TAG, "[jrSocialPublishJRActivityDidFail] SUCCESS");
         JRDictionary shareBlob = new JRDictionary();
 
         shareBlob.put("provider", provider);
         shareBlob.put("stat", "fail");
         shareBlob.put("code", error.getCode());
         shareBlob.put("message", error.getMessage());
 
         if (mShareBlobs == null)
             mShareBlobs = new ArrayList<JRDictionary>();
 
         mShareBlobs.add(shareBlob);
     }
 
     public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
         JREngage.logd(TAG, "[jrSocialDidPublishJRActivity] SUCCESS");
         JRDictionary shareBlob = new JRDictionary();
 
         shareBlob.put("provider", provider);
         shareBlob.put("stat", "ok");
 
         if (mShareBlobs == null)
             mShareBlobs = new ArrayList<JRDictionary>();
 
         mShareBlobs.add(shareBlob);
     }
 
     public void jrSocialDidCompletePublishing() {
         JREngage.logd(TAG, "[jrSocialDidCompletePublishing] SUCCESS");
         if (mFullSharingResponse == null)
             mFullSharingResponse = new JRDictionary();
 
         if (mAuthenticationBlobs != null)
             mFullSharingResponse.put("sign-ins", mAuthenticationBlobs);
 
         if (mShareBlobs != null)
             mFullSharingResponse.put("shares", mShareBlobs);
 
         postResultAndCleanUp(buildSuccessResult(mFullSharingResponse));
     }
 }
 
 
 
 
