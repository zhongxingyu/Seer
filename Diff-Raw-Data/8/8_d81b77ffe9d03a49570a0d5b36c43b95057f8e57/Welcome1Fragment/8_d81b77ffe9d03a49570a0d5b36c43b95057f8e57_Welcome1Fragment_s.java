 /**
  * <p>
  * <u><b>Copyright Notice</b></u>
  * </p><p>
  * The copyright in this document is the property of
  * Bath Institute of Medical Engineering.
  * </p><p>
  * Without the written consent of Bath Institute of Medical Engineering
  * given by Contract or otherwise the document must not be copied, reprinted or
  * reproduced in any material form, either wholly or in part, and the contents
  * of the document or any method or technique available there from, must not be
  * disclosed to any other person whomsoever.
  *  </p><p>
  *  <b><i>Copyright 2013-2014 Bath Institute of Medical Engineering.</i></b>
  * --------------------------------------------------------------------------
  *
  */
 package com.projectnocturne.views;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.projectnocturne.NocturneApplication;
 import com.projectnocturne.R;
 import com.projectnocturne.datamodel.DbMetadata;
 import com.projectnocturne.datamodel.DbMetadata.RegistrationStatus;
 import com.projectnocturne.datamodel.RESTResponseMsg;
 import com.projectnocturne.datamodel.UserDb;
 
 import java.util.List;
 
 public class Welcome1Fragment extends NocturneFragment {
 
     public static final String LOG_TAG = Welcome1Fragment.class.getSimpleName() + "::";
 
     private Handler handler = new Handler() {
         @Override
         public void handleMessage(final Message msg) {
             txtWelcomeScr1Progress.setVisibility(View.INVISIBLE);
             final RESTResponseMsg rspnsMsg = msg.getData().getParcelable("RESTResponseMsg");
             if (msg.what == DbMetadata.RegistrationStatus_ACCEPTED) {
                 NocturneApplication.logMessage(Log.INFO, LOG_TAG + "handleMessage() RegistrationStatus_ACCEPTED");
                 //FIXME : Move on to status screen
 
             } else if (msg.what == DbMetadata.RegistrationStatus_DENIED) {
                 NocturneApplication.logMessage(Log.INFO, LOG_TAG + "handleMessage() RegistrationStatus_DENIED");
                 //FIXME : show error message
                 txtWelcomeScr1ErrorMessage.setText(rspnsMsg.getMessage());
                 txtWelcomeScr1ErrorMessageDetail.setText(rspnsMsg.getContent());
                 txtWelcomeScr1ErrorMessage.setVisibility(View.VISIBLE);
             }
         }
     };
     TextWatcher textChangedWtchr = new TextWatcher() {
         @Override
         public void afterTextChanged(final Editable s) {
             Welcome1Fragment.this.enableSubscribeButton();
         }
 
         @Override
         public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
             // TODO Auto-generated method stub
         }
 
         @Override
         public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
         }
     };
     private Button btnSubscribe;
     private boolean readyFragment;
     private EditText txtWelcomeScr1EmailAddress;
     private EditText txtWelcomeScr1HomePhoneNbr;
     private EditText txtWelcomeScr1MobilePhoneNbr;
     private EditText txtWelcomeScr1PersonNameFirst;
     private EditText txtWelcomeScr1PersonNameLast;
     private UserDb userDbObj = null;
     private TextView txtWelcomeScr1ErrorMessage;
     private ProgressBar txtWelcomeScr1Progress;
     private TextView txtWelcomeScr1ErrorMessageDetail;
 
     private void enableSubscribeButton() {
         if (txtWelcomeScr1PersonNameFirst.getText().length() > 0 && txtWelcomeScr1PersonNameLast.getText().length() > 0 && txtWelcomeScr1MobilePhoneNbr.getText().length() > 0 && txtWelcomeScr1HomePhoneNbr.getText().length() > 0 && txtWelcomeScr1EmailAddress.getText().length() > 0) {
             btnSubscribe.setEnabled(true);
         } else {
             btnSubscribe.setEnabled(false);
         }
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         this.update();
     }
 
     @Override
     public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
         final View v = inflater.inflate(R.layout.activity_welcome_1, container, false);
 
         txtWelcomeScr1PersonNameFirst = (EditText) v.findViewById(R.id.welcomeScr1PersonNameFirst);
         txtWelcomeScr1PersonNameLast = (EditText) v.findViewById(R.id.welcomeScr1PersonNameLast);
         txtWelcomeScr1MobilePhoneNbr = (EditText) v.findViewById(R.id.welcomeScr1MobilePhoneNbr);
         txtWelcomeScr1HomePhoneNbr = (EditText) v.findViewById(R.id.welcomeScr1HomePhoneNbr);
         txtWelcomeScr1EmailAddress = (EditText) v.findViewById(R.id.welcomeScr1EmailAddress);
         txtWelcomeScr1Progress = (ProgressBar) v.findViewById(R.id.welcomeScr1Progress);
         txtWelcomeScr1ErrorMessage = (TextView) v.findViewById(R.id.welcomeScr1ErrorMessage);
         txtWelcomeScr1ErrorMessageDetail = (TextView) v.findViewById(R.id.welcomeScr1ErrorMessageDetail);
         btnSubscribe = (Button) v.findViewById(R.id.welcomeScr1BtnSubscribe);
 
         txtWelcomeScr1PersonNameFirst.addTextChangedListener(textChangedWtchr);
         txtWelcomeScr1PersonNameLast.addTextChangedListener(textChangedWtchr);
         txtWelcomeScr1MobilePhoneNbr.addTextChangedListener(textChangedWtchr);
         txtWelcomeScr1HomePhoneNbr.addTextChangedListener(textChangedWtchr);
         txtWelcomeScr1EmailAddress.addTextChangedListener(textChangedWtchr);
 
         btnSubscribe.setEnabled(false);
         btnSubscribe.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(final View v) {
                 txtWelcomeScr1ErrorMessage.setVisibility(View.INVISIBLE);
                 txtWelcomeScr1Progress.setVisibility(View.VISIBLE);
                 userDbObj.setName_first(txtWelcomeScr1PersonNameFirst.getText().toString());
                 userDbObj.setName_last(txtWelcomeScr1PersonNameLast.getText().toString());
                 userDbObj.setPhone_mbl(txtWelcomeScr1MobilePhoneNbr.getText().toString());
                 userDbObj.setPhone_home(txtWelcomeScr1HomePhoneNbr.getText().toString());
                 userDbObj.setEmail1(txtWelcomeScr1EmailAddress.getText().toString());
                 userDbObj.setUsername(userDbObj.getEmail1());
                 Welcome1Fragment.this.sendRegistrationMessage(userDbObj);
             }
         });
         readyFragment = true;
 
         setHasOptionsMenu(true);
         this.update();
         return v;
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see android.app.Fragment#onPrepareOptionsMenu(android.view.Menu)
      */
     @Override
     public void onPrepareOptionsMenu(final Menu menu) {
         super.onPrepareOptionsMenu(menu);
         final MenuItem someMenuItem = menu.findItem(R.id.menu_connect);
         someMenuItem.setVisible(true);
     }
 
     protected void sendRegistrationMessage(final UserDb usr) {
         if (usr.getUniqueIdentifier() == -1) {
             userDbObj = NocturneApplication.getInstance().getDataModel().addUser(usr);
         } else {
             userDbObj = NocturneApplication.getInstance().getDataModel().updateUser(usr);
         }
         NocturneApplication.getInstance().getDataModel().setRegistrationStatus(RegistrationStatus.REQUEST_SENT);
         NocturneApplication.getInstance().getServerComms().sendSubscriptionMessage(getActivity(), handler, usr);
     }
 
     public void update() {
         if (!readyFragment) {
             NocturneApplication.logMessage(Log.INFO, LOG_TAG + "update() not ready");
             return;
         }
         NocturneApplication.logMessage(Log.INFO, LOG_TAG + "update() ready");
         final List<UserDb> users = NocturneApplication.getInstance().getDataModel().getUsers();
         if (users.size() == 1) {
             userDbObj = users.get(0);
             txtWelcomeScr1PersonNameFirst.setText(userDbObj.getName_first());
             txtWelcomeScr1PersonNameLast.setText(userDbObj.getName_last());
             txtWelcomeScr1MobilePhoneNbr.setText(userDbObj.getPhone_mbl());
             txtWelcomeScr1HomePhoneNbr.setText(userDbObj.getPhone_home());
             txtWelcomeScr1EmailAddress.setText(userDbObj.getEmail1());
         } else {
             userDbObj = new UserDb();
         }
     }
 }
