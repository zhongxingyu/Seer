 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2013, Janrain, Inc.
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
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.janrain.android.capture.Capture;
 import com.janrain.android.capture.CaptureApiConnection;
 import com.janrain.android.capture.CaptureApiError;
 import com.janrain.android.capture.CaptureRecord;
 import com.janrain.android.engage.ui.JRCustomInterfaceConfiguration;
 import com.janrain.android.engage.ui.JRCustomInterfaceView;
 import com.janrain.android.utils.LogUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.util.Iterator;
 
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.AUTHENTICATION_CANCELED_BY_USER;
 import static com.janrain.android.R.string.jr_capture_trad_signin_bad_password;
 import static com.janrain.android.R.string.jr_capture_trad_signin_unrecognized_error;
 import static com.janrain.android.R.string.jr_dialog_dismiss;
 
 public class TradSignInUi extends JRCustomInterfaceConfiguration {
 
     private static AlertDialog dialog;
     private static Button signInButton;
     private static Button cancelButton;
     private static TradSignInView tradSignInView;
     private static ProgressBar progress;
 
     public TradSignInUi() {
         this.mProviderListHeader = new TradSignInView();
     }
 
     public static void showStandAloneDialog(Activity fromActivity, String mergeToken) {
         LayoutInflater inflater = LayoutInflater.from(fromActivity);
         tradSignInView = new TradSignInView();
         tradSignInView.mergeToken = mergeToken;
         final View signInView = tradSignInView.onCreateView(fromActivity, inflater, null, null);
         signInView.findViewById(R.id.custom_signin_button).setVisibility(View.GONE);
         progress = (ProgressBar) signInView.findViewById(R.id.trad_signin_progress);
 
         dialog = new AlertDialog.Builder(fromActivity)
                 .setTitle(R.string.jr_capture_trad_signin_dialog_title)
                 .setView(signInView)
                 .setCancelable(false)
                 .setPositiveButton(R.string.jr_capture_signin_view_button_title, null)
                 .setNegativeButton(android.R.string.cancel, null)
                 .create();
         dialog.setCanceledOnTouchOutside(false);
 
         // http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
         dialog.setOnShowListener(new DialogInterface.OnShowListener() {
             public void onShow(DialogInterface _) {
                 signInButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                 signInButton.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         tradSignInView.onStandAloneClick();
                     }
                 });
                 cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                 cancelButton.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         Jump.fireHandlerOnFailure(new Jump.SignInResultHandler.SignInError(
                                 AUTHENTICATION_CANCELED_BY_USER, null, null));
                         dialog.dismiss();
                     }
                 });
             }
         });
         dialog.show();
     }
 
     private static void showStandAloneProgress() {
         setInputViewsEnabled(false);
         progress.setVisibility(View.VISIBLE);
         dialog.setTitle(R.string.jr_capture_signin_view_signing_in);
     }
 
     private static void hideStandAloneProgress() {
         setInputViewsEnabled(true);
         progress.setVisibility(View.GONE);
         dialog.setTitle(R.string.jr_capture_trad_signin_dialog_title);
     }
 
     private static void setInputViewsEnabled(boolean enabled) {
         signInButton.setEnabled(enabled);
         tradSignInView.userName.setEnabled(enabled);
         tradSignInView.password.setEnabled(enabled);
     }
 
     private static class TradSignInView extends JRCustomInterfaceView implements View.OnClickListener {
         private EditText userName, password;
         private String mergeToken;
         private TextView messages;
 
         @Override
         public View onCreateView(Context context,
                                  LayoutInflater inflater,
                                  ViewGroup container,
                                  Bundle savedInstanceState) {
             View v = inflater.inflate(R.layout.jr_capture_trad_signin, container, false);
             userName = (EditText) v.findViewById(R.id.username_edit);
             password = (EditText) v.findViewById(R.id.password_edit);
             messages = (TextView) v.findViewById(R.id.message_container);
 
             v.findViewById(R.id.custom_signin_button).setOnClickListener(this);
             return v;
         }
 
         public void onClick(View v) {
             final Capture.SignInResultHandler handler = new Capture.SignInResultHandler() {
                 public void onSuccess(CaptureRecord record, JSONObject response) {
                     Jump.state.signedInUser = record;
                     Jump.fireHandlerOnSuccess(response);
                     dismissProgressIndicator();
                     finishJrSignin();
                 }
 
                 public void onFailure(CaptureApiError error) {
                     dismissProgressIndicator();
                     AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                             .setTitle(getResources().getString(
                                     R.string.jr_capture_forgotpassword_error_msg))
                             .setNegativeButton(getResources().getString(
                                     R.string.jr_capture_forgotpassword_dismiss_button),
                                     new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int id) {
                                             dialog.cancel();
                                         }
                                     })
                             .setPositiveButton(getResources().getString(
                                     R.string.jr_capture_forgotpassword_forgotpass_button),
                                     new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int id) {
                                             //Calls AlertDialog Forgot password flow
                                             AlertDialog myForgotPasswordDialog = forgotPassword();
                                             myForgotPasswordDialog.show();
                                             dialog.cancel();
                                         }
                                     });
                     adb.show();
                 }
             };
 
             final CaptureApiConnection connection = startSignIn(handler);
 
             showProgressIndicator(true, new DialogInterface.OnCancelListener() {
                 public void onCancel(DialogInterface dialog) {
                     handler.cancel();
                     connection.stopConnection();
                 }
             });
         }
 
         public void onStandAloneClick() {
             final Capture.SignInResultHandler handler = new Capture.SignInResultHandler() {
                 public void onSuccess(CaptureRecord record, JSONObject response) {
                     Jump.state.signedInUser = record;
                     Jump.fireHandlerOnSuccess(response);
                     dialog.dismiss();
                 }
 
                 public void onFailure(CaptureApiError error) {
                     if (error.raw_response.optJSONArray("messages") != null) {
                         JSONArray jsonMessages = error.raw_response.optJSONArray("messages");
                         JSONObject jmo = error.raw_response.optJSONObject("messages");
                         if (jsonMessages == null) jsonMessages = new JSONArray();
                         if (jmo != null) {
                             // remove this branch once CAP-1602 is out, and open a ticket to improve messages
                             // display
                             Iterator i = jmo.keys();
                             while (i.hasNext()) jsonMessages.put(jmo.opt((String) i.next()));
                         }
                         String html = "";
                         for (int i=0; i<jsonMessages.length(); i++) {
                             html += "&#8226; " + jsonMessages.optString(i) + "<br/>\n";
                         }
                         messages.setText(Html.fromHtml(html));
                     } else {
                         messages.setText("Error: " + error);
                     }
 
                     LogUtils.loge(error.toString());
                     hideStandAloneProgress();
                 }
             };
 
             messages.setText("");
             final CaptureApiConnection connection = startSignIn(handler);
             showStandAloneProgress();
         }
 
         private CaptureApiConnection startSignIn(Capture.SignInResultHandler handler) {
             return Capture.performTraditionalSignIn(userName.getText().toString(),
                     password.getText().toString(),
                     handler, mergeToken);
         }
 
         /**
          * Receive a callback which handles the Capture recoverPassword flow
          */
         private class ForgotPasswordResultHandler implements Jump.ForgotPasswordResultHandler {
 
             //forgot password result  success handler
             public void onSuccess() {
                 dismissProgressIndicator();
                 AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                 adb
                         .setTitle(getResources().getString(R.string.jr_capture_forgotpassword_success_msg))
                         .setPositiveButton(getResources().getString(
                                 R.string.jr_capture_forgotpassword_dismiss_button),
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int id) {
                                         dialog.cancel();
                                     }
                                 });
                 adb.show();
             }
 
             //forgot password result failure handler
             public void onFailure(ForgetPasswordError error) {
                 dismissProgressIndicator();
                 AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                 adb
                         .setTitle(getResources().getString(
                                 R.string.jr_capture_forgotpassword_reset_error_msg))
                         .setPositiveButton(getResources().getString(
                                 R.string.jr_capture_forgotpassword_dismiss_button),
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int id) {
                                         dialog.cancel();
                                     }
                                 });
                 adb.show();
             }
         }
 
         /**
          * Alert Dialog For Forgot Password  Flow setPositiveButton for send setNegativeButton for dismiss
          * emailAddress - EditText field for providing emailAddress
          */
 
         private AlertDialog forgotPassword() {
 
             LayoutInflater li = LayoutInflater.from(getActivity());
             // set alert dialog builder
             View promptsView = li.inflate(R.layout.jr_capture_forgotpassword, null);
             AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                     getActivity());
             final EditText emailAddress = (EditText) promptsView
                     .findViewById(R.id.emailAddress_edit);
             emailAddress.setText(userName.getText());
             alertDialogBuilder
                     .setView(promptsView)
                     .setTitle(getResources().getString(R.string.jr_capture_forgotpassword_dialog_header))
                     .setNegativeButton(getResources().getString(
                             android.R.string.cancel),
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int id) {
                                     dialog.cancel();
                                 }
                             })
                     .setPositiveButton(getResources().getString(
                             R.string.jr_capture_forgotpassword_send_button),
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int id) {
                                     //performForgotPassword Forgot Password
                                     Jump.performForgotPassword(emailAddress.getText().toString(),
                                             new ForgotPasswordResultHandler());
                                     showProgressIndicator(true, new DialogInterface.OnCancelListener() {
                                         public void onCancel(DialogInterface dialog) {
                                         }
                                     });
                                     dialog.cancel();
                                 }
                             });
 
             // return dialog
             AlertDialog alertDialog = alertDialogBuilder.create();
             alertDialog.setInverseBackgroundForced(true);
             return alertDialog;
         }
     }
 }
