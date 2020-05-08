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
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.engage.ui;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.PorterDuff;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.StateListDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.util.Config;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.TranslateAnimation;
 import android.widget.*;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.session.JRAuthenticatedUser;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.session.JRSessionData;
 import com.janrain.android.engage.session.JRSessionDelegate;
 import com.janrain.android.engage.types.*;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @internal
  *
  * @class JRPublishActivity
  * Publishing UI
  */
 public class JRPublishActivity extends TabActivity implements TabHost.OnTabChangeListener {
     private static final int DIALOG_FAILURE = 1;
     //private static final int DIALOG_SUCCESS = 2;
     private static final int DIALOG_CONFIRM_SIGNOUT = 3;
     private static final int DIALOG_MOBILE_CONFIG_LOADING = 4;
     //private static final int LIGHT_BLUE_BACKGROUND = 0xFF1A557C;
     private static final int JANRAIN_BLUE_20PERCENT = 0x33074764;
     private static final int JANRAIN_BLUE_100PERCENT = 0xFF074764;
     private static final String EMAIL_SMS_TAB_TAG = "email_sms";
 
     /**
      * @internal
      *
      * @class FinishReceiver
      * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
      * for iPhone-like ability to close this activity from the maestro class.
      **/
     private class FinishReceiver extends BroadcastReceiver {
 
         private final String TAG = JRPublishActivity.TAG + "-" + FinishReceiver.class.getSimpleName();
 
         @Override
         public void onReceive(Context context, Intent intent) {
             String target = intent.getStringExtra(
                     JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
             if (JRPublishActivity.class.toString().equals(target)) {
                 tryToFinishActivity();
                 Log.i(TAG, "[onReceive] handled");
             } else if (Config.LOGD) {
                 Log.i(TAG, "[onReceive] ignored");
             }
         }
     }
 
     private static final String TAG = JRPublishActivity.class.getSimpleName();
 
     //private FinishReceiver mFinishReceiver;
 
     /* Reference to the library model */
     private JRSessionData mSessionData;
     private JRSessionDelegate mSessionDelegate; /* Call backs for JRSessionData */
 
     /* JREngage objects we're operating with */
     private JRProvider mSelectedProvider; /* The provider for the selected tab */
     private JRAuthenticatedUser mAuthenticatedUser; /* The user (if logged in) for the selected tab */
     private JRActivityObject mActivityObject;
 
     /* UI properties */
     private String mShortenedActivityURL = null; /* null if it hasn't been shortened */
     private int mMaxCharacters;
     private String mDialogErrorMessage;
 
     /* UI state transitioning variables */
     //private boolean mUserHasEditedText = false;
     private boolean mWeHaveJustAuthenticated = false;
     //private boolean mWeAreCurrentlyPostingSomething = false;
     private boolean mWaitingForMobileConfig = false;
     private boolean mCurrentlyOnEmailSmsTab = false;
 
     /* UI views */
     private LinearLayout mProviderStuffContainer;
     private LinearLayout mPreviewBorder;
     private RelativeLayout mPreviewBox;
     private RelativeLayout mMediaContentView;
     private TextView mCharacterCountView;
     private TextView mPreviewLabelView;
     private ImageView mProviderIcon;
     private EditText mUserCommentView;
     private ImageView mTriangleIconView;
     //private LinearLayout mProfilePicAndButtonsHorizontalLayout;
     private LinearLayout mUserProfileInformationAndShareButtonContainer;
     private LinearLayout mUserProfileContainer;
     private ImageView mUserProfilePic;
     //private LinearLayout mNameAndSignOutContainer;
     private TextView mUserNameView;
     private Button mSignOutButton;
     private ColorButton mJustShareButton;
     private ColorButton mConnectAndShareButton;
     private LinearLayout mSharedTextAndCheckMarkContainer;
     private ColorButton mEmailButton;
     private ColorButton mSmsButton;
     private EditText mEmailSmsComment;
     private LinearLayout mEmailSmsButtonContainer;
    private RelativeLayout mTaglineAndProviderIconContainer;
 
     private HashMap<String, Boolean> mProvidersThatHaveAlreadyShared;
 
     /* Helper class used to control display of a nice loading dialog */
     private SharedLayoutHelper mLayoutHelper;
 
     /* Helper class for the JRUserInterfaceMaestro */
     private FinishReceiver mFinishReceiver;
 
     /* Activity life-cycle methods */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
         setContentView(R.layout.jr_publish_activity);
 
         mSessionData = JRSessionData.getInstance();
 
         /* For the case when this activity is relaunched after the process was killed */
         if (mSessionData == null) {
             finish();
             return;
         }
 
         mActivityObject = mSessionData.getJRActivity();
         mSessionDelegate = createSessionDelegate();
 
         /* View References */
         mProviderStuffContainer = (LinearLayout) findViewById(R.id.jr_provider_stuff_container);
         mPreviewBox = (RelativeLayout) findViewById(R.id.jr_preview_box);
         mPreviewBorder = (LinearLayout) findViewById(R.id.jr_preview_box_border);
         mMediaContentView = (RelativeLayout) findViewById(R.id.jr_media_content_view);
         mCharacterCountView = (TextView) findViewById(R.id.jr_character_count_view);
         mProviderIcon = (ImageView) findViewById(R.id.jr_provider_icon);
         mUserCommentView = (EditText) findViewById(R.id.jr_edit_comment);
         mPreviewLabelView = (TextView) findViewById(R.id.jr_preview_text_view);
         mTriangleIconView = (ImageView) findViewById(R.id.jr_triangle_icon_view);
         mUserProfileInformationAndShareButtonContainer =
                 (LinearLayout) findViewById(R.id.jr_user_profile_information_and_share_button_container);
         mUserProfileContainer = (LinearLayout) findViewById(R.id.jr_user_profile_container);
         mUserProfilePic = (ImageView) findViewById(R.id.jr_profile_pic);
         mUserNameView = (TextView) findViewById(R.id.jr_user_name);
         mSignOutButton = (Button) findViewById(R.id.jr_sign_out_button);
         mJustShareButton = (ColorButton) findViewById(R.id.jr_just_share_button);
         mConnectAndShareButton = (ColorButton) findViewById(R.id.jr_connect_and_share_button);
         mSharedTextAndCheckMarkContainer = (LinearLayout) findViewById(
                 R.id.jr_shared_text_and_check_mark_horizontal_layout);
         mEmailButton = (ColorButton) findViewById(R.id.jr_email_button);
         mSmsButton = (ColorButton) findViewById(R.id.jr_sms_button);
             // XXX: LILLI MAKING SMS CHANGES
         //mEmailSmsComment = (EditText) findViewById(R.id.jr_email_sms_edit_comment);
         mEmailSmsButtonContainer = (LinearLayout) findViewById(R.id.jr_email_sms_button_container);
         mTaglineAndProviderIconContainer =
                (RelativeLayout) findViewById(R.id.jr_tagline_and_provider_icon_container);
 
         /* Set the user comment field here before the text change listener is registered so that
          * it can be displayed while the providers are being loaded if this is a first run.
          * The text change listener will be fired when the first tab is initially selected. */
         mUserCommentView.setText(mActivityObject.getUserGeneratedContent());
 
         /* View listeners */
         mEmailButton.setOnClickListener(mEmailButtonListener);
         mSmsButton.setOnClickListener(mSmsButtonListener);
         //ButtonEventColorChangingListener colorChangingListener =
         //        new ButtonEventColorChangingListener();
         //mEmailButton.setOnFocusChangeListener(colorChangingListener);
         //mEmailButton.setOnTouchListener(colorChangingListener);
         //mSmsButton.setOnFocusChangeListener(colorChangingListener);
         //mSmsButton.setOnTouchListener(colorChangingListener);
 
         mUserCommentView.addTextChangedListener(mUserCommentTextWatcher);
         mSignOutButton.setOnClickListener(mSignoutButtonListener);
 
         mConnectAndShareButton.setOnClickListener(mShareButtonListener);
         //mConnectAndShareButton.setOnFocusChangeListener(colorChangingListener);
         //mConnectAndShareButton.setOnTouchListener(colorChangingListener);
 
         mJustShareButton.setOnClickListener(mShareButtonListener);
         //mJustShareButton.setOnFocusChangeListener(colorChangingListener);
         //mJustShareButton.setOnTouchListener(colorChangingListener);
 
         mSmsButton.setColor(JANRAIN_BLUE_100PERCENT);
         mEmailButton.setColor(JANRAIN_BLUE_100PERCENT);
 
         /* Initialize the provider shared-ness state map */
         mProvidersThatHaveAlreadyShared = new HashMap<String, Boolean>();
 
         /* SharedLayoutHelper provides a spinner dialog */
         mLayoutHelper = new SharedLayoutHelper(this);
 
         /* JRUserInterfaceMaestro's hook into calling this.finish() */
         if (mFinishReceiver == null) {
             mFinishReceiver = new FinishReceiver();
             registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
         }
 
         mSessionData.addDelegate(mSessionDelegate);
 
         loadViewElementPropertiesWithActivityObject();
 
         /* Call by hand the configuration change listener so that it sets up correctly if this
          * activity started in landscape mode. */
         onConfigurationChanged(getResources().getConfiguration());
 
         List<JRProvider>socialProviders = mSessionData.getSocialProviders();
         if ((socialProviders == null || socialProviders.size() == 0)
                 && !mSessionData.isGetMobileConfigDone()) {
             /* Hide the email/SMS tab so things look nice as we load the providers */
             // XXX: LILLI MAKING SMS CHANGES
 //            findViewById(R.id.jr_tab_email_sms_content).setVisibility(View.GONE);
             mWaitingForMobileConfig = true;
             showDialog(DIALOG_MOBILE_CONFIG_LOADING);
         } else {
             initializeWithProviderConfiguration();
         }
     }
 
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
     }
 
     private void loadViewElementPropertiesWithActivityObject() {
         /* This sets up pieces of the UI before the provider configuration information
          * has been loaded */
 
         JRMediaObject mo = null;
         if (mActivityObject.getMedia().size() > 0) mo = mActivityObject.getMedia().get(0);
 
         final ImageView mci = (ImageView) findViewById(R.id.jr_media_content_image);
         final TextView  mcd = (TextView)  findViewById(R.id.jr_media_content_description);
         final TextView  mct = (TextView)  findViewById(R.id.jr_media_content_title);
 
         /* Set the media_content_view = a thumbnail of the media */
         if (mo != null) if (mo.hasThumbnail()) {
             Log.d(TAG, "media image url: " + mo.getThumbnail());
             new AsyncTask<JRMediaObject, Void, Bitmap>(){
                 protected Bitmap doInBackground(JRMediaObject... mo_) {
                     try {
                         // TODO: get this cached
                         URL url = new URL(mo_[0].getThumbnail());
                         URLConnection urlc = url.openConnection();
                         urlc.setUseCaches(true);
                         urlc.setDefaultUseCaches(true);
                         InputStream is = urlc.getInputStream();
                         return BitmapFactory.decodeStream(is);
                     } catch (MalformedURLException e) {
                         return null;
                     } catch (IOException e) {
                         return null;
                     }
                 }
 
                 protected void onPostExecute(Bitmap bitmap) {
                     if (bitmap == null) mci.setVisibility(View.INVISIBLE);
                     else mci.setVisibility(View.VISIBLE);
                     mci.setImageBitmap(bitmap);
                 }
             }.execute(mo);
         }
 
         /* Set the media content description */
         mcd.setText(mActivityObject.getDescription());
 
         /* Set the media content title */
         mct.setText(mActivityObject.getTitle());
     }
 
     private void initializeWithProviderConfiguration() {
         /* Check for no suitable providers */
         List<JRProvider> socialProviders = mSessionData.getSocialProviders();
         if (socialProviders == null || socialProviders.size() == 0) {
             JREngageError err = new JREngageError(
                     "Cannot load the Publish Activity, no social sharing providers are configured.",
                     JREngageError.ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                     JREngageError.ErrorType.CONFIGURATION_INFORMATION_MISSING);
             mSessionData.triggerPublishingDialogDidFail(err);
             return;
         }
 
         /* Configure the properties of the UI */
         mActivityObject.shortenUrls(new JRActivityObject.ShortenedUrlCallback() {
             public void setShortenedUrl(String shortenedUrl) {
                 mShortenedActivityURL = shortenedUrl;
 
                 if (mSelectedProvider == null) return;
 
                 if (mSelectedProvider.getSocialSharingProperties().
                         getAsBoolean("content_replaces_action")) {
                     updatePreviewTextWhenContentReplacesAction();
                 } else {
                     updatePreviewTextWhenContentDoesNotReplaceAction();
                 }
                 updateCharacterCount();
             }
         });
         createTabs();
 
         /* Re-set the user comment with it's existing so the text change listener is fired
          * and the character count is updated.
          * See also onCreate */
         mUserCommentView.setText(mUserCommentView.getText());
     }
 
     private void createTabs() {
         TabHost tabHost = getTabHost();
         tabHost.setup();
 
         List<JRProvider> socialProviders = mSessionData.getSocialProviders();
 
         /* Make a tab for each social provider */
         for (JRProvider provider : socialProviders) {
             Drawable providerIconSet = provider.getTabSpecIndicatorDrawable(this);
 
             TabHost.TabSpec spec = tabHost.newTabSpec(provider.getName());
             spec.setContent(R.id.jr_tab_view_content);
 
             LinearLayout ll = createTabSpecIndicator(provider.getFriendlyName(), providerIconSet);
 
             spec.setIndicator(ll);
             tabHost.addTab(spec);
 
             mProvidersThatHaveAlreadyShared.put(provider.getName(), false);
         }
 
         /* Make a tab for email/SMS */
         TabHost.TabSpec emailSmsSpec = tabHost.newTabSpec(EMAIL_SMS_TAB_TAG);
         Drawable d = getResources().getDrawable(R.drawable.jr_email_sms_tab_indicator);
         emailSmsSpec.setIndicator(createTabSpecIndicator("Email/SMS", d));
 //        emailSmsSpec.setContent(R.id.jr_tab_email_sms_content);
         emailSmsSpec.setContent(R.id.jr_tab_view_content);
         tabHost.addTab(emailSmsSpec);
 
         tabHost.setOnTabChangedListener(this);
 
         JRProvider rp = mSessionData.getProviderByName(mSessionData.getReturningSocialProvider());
         tabHost.setCurrentTab(socialProviders.indexOf(rp));
         
         /* When TabHost is constructed it defaults to tab 0, so if indexOfLastUsedProvider is 0,
          * the tab change listener won't be invoked, so we call it manually to ensure
          * it is called.  (it's idempotent) */
         onTabChanged(tabHost.getCurrentTabTag());
 
         /* XXX TabHost is setting our FrameLayout's only child to View.GONE when loading.
          * XXX That could be a bug in the TabHost, or it could be a misuse of the TabHost system.
          * XXX This is a workaround: */
         findViewById(R.id.jr_tab_view_content).setVisibility(View.VISIBLE);
     }
 
     private LinearLayout createTabSpecIndicator(String labelText, Drawable providerIconSet) {
         LinearLayout ll = new LinearLayout(this);
         ll.setOrientation(LinearLayout.VERTICAL);
 
         /* Icon */
         ImageView icon = new ImageView(this);
         icon.setImageDrawable(providerIconSet);
         icon.setPadding(
                 scaleDipPixels(10),
                 scaleDipPixels(10),
                 scaleDipPixels(10),
                 scaleDipPixels(3)
         );
         ll.addView(icon);
 
         /* Background */
         StateListDrawable tabBackground = new StateListDrawable();
         tabBackground.addState(new int[]{android.R.attr.state_selected},
                 getResources().getDrawable(android.R.color.transparent));
         tabBackground.addState(new int[]{},
                 getResources().getDrawable(android.R.color.darker_gray));
         ll.setBackgroundDrawable(tabBackground);
 
         /* Label */
         TextView label = new TextView(this);
         label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
         label.setText(labelText);
         label.setGravity(Gravity.CENTER);
         label.setPadding(
                 scaleDipPixels(0),
                 scaleDipPixels(0),
                 scaleDipPixels(0),
                 scaleDipPixels(4)
         );
         ll.addView(label);
 
         return ll;
     }
 
     protected void onRestart() {
         super.onRestart();
         Log.d(TAG, "onRestart");
 
         if (mLayoutHelper.getProgressDialogShowing()) {
             Log.e(TAG, "onRestart: progress dialog still showing");
         }
     }
 
     protected void onResume() {
         super.onResume();
         Log.d(TAG, "onResume");
 
         // Why set the context here?
         // IIRC this was added in an earlier attempt to support resumption of JREngage dialogs
         // after a process death and restart.
         JREngage.setContext(this);
 
         //if (mSelectedProvider != null) {
         //    Object colorArray = mSelectedProvider.getSocialSharingProperties().get("color_values");
         //    int color = colorForProviderFromArray(colorArray, false);
         //
         //    //mJustShareButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
         //    //mConnectAndShareButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
         //}
 
 
         //mEmailButton.getBackground().setColorFilter(JANRAIN_BLUE_100PERCENT,
         //        PorterDuff.Mode.MULTIPLY);
         //mSmsButton.getBackground().setColorFilter(JANRAIN_BLUE_100PERCENT,
         //        PorterDuff.Mode.MULTIPLY);
     }
 
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
 
         if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
             mPreviewBox.setVisibility(View.GONE);
             // XXX: LILLI MAKING SMS CHANGES
 //            mEmailSmsComment.setLines(3);
             mEmailSmsButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
         } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
             mPreviewBox.setVisibility(View.VISIBLE);
             // XXX: LILLI MAKING SMS CHANGES
 //            mEmailSmsComment.setLines(4);
             mEmailSmsButtonContainer.setOrientation(LinearLayout.VERTICAL);
         }
     }
 
     protected void onPause() {
         super.onPause();
         Log.d(TAG, "onPause");
     }
 
     protected void onStop() {
         super.onStop();
         Log.d(TAG, "onStop");
     }
 
     protected void onDestroy() {
         super.onDestroy();
         Log.d(TAG, "onDestroy");
 
         if (mSessionData != null && mSessionDelegate != null)
             mSessionData.removeDelegate(mSessionDelegate);
 
         if (mFinishReceiver != null)
             unregisterReceiver(mFinishReceiver);
     }
 
     private void tryToFinishActivity() {
         /* Invoked by JRUserInterfaceMaestro via FinishReceiver to close this activity. */
         Log.i(TAG, "[tryToFinishActivity]");
         finish();
     }
 
     /* UI event listeners */
     public void onBackPressed() {
         Log.d(TAG, "onBackPressed");
         mSessionData.triggerPublishingDidComplete();
     }
 
     //private class ButtonEventColorChangingListener implements
     //        View.OnFocusChangeListener, View.OnTouchListener {
     //
     //    public void onFocusChange(View view, boolean hasFocus) {
     //        if (hasFocus)
     //            view.getBackground().clearColorFilter();
     //        else {
     //            int providerColor = colorForProviderFromArray(
     //                    mSelectedProvider.getSocialSharingProperties().get("color_values"), false);
     //            int color = getTabHost().getCurrentTabTag().equals(EMAIL_SMS_TAB_TAG) ?
     //                    JANRAIN_BLUE_100PERCENT
     //                    : providerColor;
     //            view.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
     //        }
     //    }
     //
     //    public boolean onTouch(View view, MotionEvent motionEvent) {
     //        boolean onButton = (motionEvent.getX() >= view.getLeft()) &&
     //                (motionEvent.getX() <= view.getLeft() + view.getWidth()) &&
     //                (motionEvent.getY() >= view.getTop()) &&
     //                (motionEvent.getY() <= view.getTop() + view.getHeight());
     //
     //        int providerColor = colorForProviderFromArray(
     //                mSelectedProvider.getSocialSharingProperties().get("color_values"), false);
     //
     //        boolean isEmailSmsTab = getTabHost().getCurrentTabTag().equals(EMAIL_SMS_TAB_TAG);
     //        int color = isEmailSmsTab ? JANRAIN_BLUE_100PERCENT : providerColor;
     //
     //        switch (motionEvent.getAction()) {
     //            case MotionEvent.ACTION_UP:
     //                if (!view.isPressed() || isEmailSmsTab) {
     //                    view.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
     //
     //                    // For some reason both email/SMS buttons' colorfilters are being cleared
     //                    // this is a hack to make sure they're both applied.
     //
     //                    mEmailButton.getBackground().setColorFilter(color,
     //                            PorterDuff.Mode.MULTIPLY);
     //                    mSmsButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
     //                }
     //                break;
     //            case MotionEvent.ACTION_CANCEL:
     //                view.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
     //                break;
     //            case MotionEvent.ACTION_DOWN:
     //                view.getBackground().clearColorFilter();
     //                break;
     //            case MotionEvent.ACTION_MOVE:
     //                if (!onButton & !view.isPressed()) {
     //                    view.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
     //                    view.invalidate();
     //                } else if (view.isPressed()) view.getBackground().clearColorFilter();
     //                break;
     //            case MotionEvent.ACTION_OUTSIDE:
     //                break;
     //        }
     //
     //        Log.d(TAG, "ColorFilter " + motionEvent.getAction() + " " + view.toString()
     //                + " " + onButton + " " + view.isPressed());
     //
     //        return false;
     //    }
     //}
 
     private View.OnClickListener mShareButtonListener = new View.OnClickListener() {
         public void onClick(View view) {
             //mWeAreCurrentlyPostingSomething = true;
 
             mActivityObject.setUserGeneratedContent(mUserCommentView.getText().toString());
 
             mLayoutHelper.showProgressDialog();
 
             if (mAuthenticatedUser == null) {
                 authenticateUserForSharing();
             } else {
                 shareActivity();
             }
         }
     };
 
     private View.OnClickListener mSignoutButtonListener = new View.OnClickListener() {
         public void onClick(View view) {
            showDialog(DIALOG_CONFIRM_SIGNOUT);
         }
     };
 
     private TextWatcher mUserCommentTextWatcher = new TextWatcher() {
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
         }
 
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
         }
 
         public void afterTextChanged(Editable editable) {
             updateUserCommentView();
             updateCharacterCount();
 
             for (String k : mProvidersThatHaveAlreadyShared.keySet())
                 mProvidersThatHaveAlreadyShared.put(k, false);
             
             showActivityAsShared(false);
         }
     };
 
     public void onTabChanged(String tabId) {
         Log.d(TAG, "[onTabChange]: " + tabId);
 
         if (tabId.equals(EMAIL_SMS_TAB_TAG)) {
 //            mEmailSmsComment.setText(mUserCommentView.getText());
 
             if (!mCurrentlyOnEmailSmsTab) {
                 animateViewDisappearing(mEmailSmsButtonContainer, false);
                 animateViewDisappearing(mProviderStuffContainer, true);
                 animateSinkingUserProfileInformationAndShareButtonContainer(true);
                 animateSinkingTaglineAndIcon(true);
             }
 
             mCurrentlyOnEmailSmsTab = true;
         } else { /* ... else a "real" provider -- Facebook, Twitter, etc. */
             mSelectedProvider = mSessionData.getProviderByName(tabId);
 
             if (mCurrentlyOnEmailSmsTab) {
                 animateViewDisappearing(mEmailSmsButtonContainer, true);
                 animateViewDisappearing(mProviderStuffContainer, false);
                 animateSinkingUserProfileInformationAndShareButtonContainer(false);
                 animateSinkingTaglineAndIcon(false);
             }
 
             configureViewElementsBasedOnProvider();
             configureLoggedInUserBasedOnProvider();
             configureSharedStatusBasedOnProvider();
 
             mProviderIcon.setImageDrawable(mSelectedProvider.getProviderListIconDrawable(
                     getApplicationContext()));
 
             mCurrentlyOnEmailSmsTab = false;
         }
     }
 
     private void animateViewDisappearing(View view, boolean disappearing) {
         AnimationSet set = new AnimationSet(true);
 
         Animation animation = new AlphaAnimation(
                 disappearing ? 1.0f : 0.0f,
                 disappearing ? 0.0f : 1.0f);
 
         animation.setDuration(750);
         animation.setFillAfter(true);
         set.addAnimation(animation);
 
         view.startAnimation(animation);
     }
 
     private void animateSinkingUserProfileInformationAndShareButtonContainer(boolean sinking) {
         AnimationSet set = new AnimationSet(true);
 
         float yOffset = sinking ?
                 (-1.0f * 1.0f) ://mUserProfileInformationAndShareButtonContainer.getHeight()) :
                 (1.0f);//(mUserProfileInformationAndShareButtonContainer.getHeight());
 
         Animation animation = new TranslateAnimation(
                 Animation.RELATIVE_TO_SELF, 0.0f,
                 Animation.RELATIVE_TO_SELF, 0.0f,
                 Animation.RELATIVE_TO_SELF, (sinking ? 0.0f : 1.0f),
                 Animation.RELATIVE_TO_SELF, (sinking ? 1.0f : 0.0f));
 
         animation.setDuration(750);
         animation.setFillAfter(true);
         set.addAnimation(animation);
 
         mUserProfileInformationAndShareButtonContainer.startAnimation(animation);
     }
 
     private void animateSinkingTaglineAndIcon(boolean sinking) {
         AnimationSet set = new AnimationSet(true);
 
         float yOrigin = sinking ?
                 0.0f : (1.0f * mUserProfileInformationAndShareButtonContainer.getHeight());
         float yOffset = sinking ?
                 (mUserProfileInformationAndShareButtonContainer.getHeight()) : 0.0f;
                 //(-1.0f * mUserProfileInformationAndShareButtonContainer.getHeight());
 
         Animation animation = new TranslateAnimation(0.0f, 0.0f, yOrigin, yOffset);
 
         animation.setDuration(750);
         animation.setFillAfter(true);
         set.addAnimation(animation);
 
         mTaglineAndProviderIconContainer.startAnimation(animation);
     }
 
     private View.OnClickListener mEmailButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
             Intent intent;
 
             JREmailObject jrEmail = mActivityObject.getEmail();
             String body, subject;
 
             if (jrEmail == null) {
                 body = mUserCommentView.getText().toString();
                 subject = getString(R.string.jr_default_email_share_subject);
             } else {
                 body = mUserCommentView.getText().toString() + "\n" + jrEmail.getBody();
                 subject = TextUtils.isEmpty(jrEmail.getSubject()) ?
                         getString(R.string.jr_default_email_share_subject)
                         : jrEmail.getSubject();
             }
 
             intent = new Intent(android.content.Intent.ACTION_SEND);
 
             /* XXX HACK XXX:
              * By setting this MIME type we cajole the right behavior out of the platform.  This
              * MIME type is not valid (normally it would be text/plain) but the email apps respond
              * to ACTION_SEND type so it works.
              * The reason that using ACTION_SENDTO with a URI with scheme mailto: does not work is
              * that the "Email" app fills the To: field with a single comma.
              * (Because it's expecting an actual email address in the URI, but we're not
              * supplying one, we're supplying only a scheme.) */
             intent.setType("plain/text");
             //intent.setData(Uri.parse("mailto:"));
             intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
             intent.putExtra(android.content.Intent.EXTRA_TEXT, body);
 
             //Intent chooser = Intent.createChooser(intent, getString(R.string.jr_choose_email_handler));
             startActivityForResult(intent, 0);
             mSessionData.notifyEmailSmsShare("email");
         }
     };
 
     private View.OnClickListener mSmsButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
             Intent intent;
 
             JRSmsObject jrSms = mActivityObject.getSms();
             String body;
 
             if (jrSms == null) {
                 body = mUserCommentView.getText().toString();
             } else {
                 body = mUserCommentView.getText().toString() + "\n" + jrSms.getBody();
             }
 
             /* Google Voice does not respect passing the body, so this Intent is constructed
              * specifically to be responded to only by Mms (the platform messaging app).
              * http://stackoverflow.com/questions/4646508/how-to-pass-text-to-google-voice-sms-programmatically */
 
             //intent = new Intent(android.content.Intent.ACTION_SEND);
             intent = new Intent(android.content.Intent.ACTION_VIEW);
             intent.setType("vnd.android-dir/mms-sms");
             //intent.setData(Uri.parse("smsto:"));
             //intent.setData(Uri.parse("sms:"));
             //intent.putExtra(android.content.Intent.EXTRA_TEXT, body.substring(0,130));
             intent.putExtra("sms_body", body.substring(0, Math.min(139, body.length())));
             intent.putExtra("exit_on_sent", true);
 
             startActivityForResult(intent, 0);
             mSessionData.notifyEmailSmsShare("sms");
         }
     };
 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         /* Callback from startActivityForResult for email sharing.
          * This code path hasn't set mSelectedProvider yet, so we use the value previously
          * set and "unselect" the email SMS tab, making it kind of a button in tab clothing. */
         
             // XXX: LILLI MAKING SMS CHANGES
 //        mUserCommentView.setText(mEmailSmsComment.getText());
 
         /* Email and SMS intents are returning 0, 0, null */
         //Log.d(TAG, "[onActivityResult]: requestCode=" + requestCode + " resultCode=" + resultCode
         //        + " data=" + data);
     }
 
     public Dialog onCreateDialog(int id) {
         DialogInterface.OnClickListener successDismiss = new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialogInterface, int i) {
                 finish();
             }
         };
         // TODO: make resources out of these strings
 
         switch (id) {
 //            case DIALOG_SUCCESS:
 //                return new AlertDialog.Builder(JRPublishActivity.this)
 //                        .setMessage("Success!")
 //                        .setCancelable(false)
 //                        .setPositiveButton("Dismiss", null)
 //                        .create();
             case DIALOG_FAILURE:
                 return new AlertDialog.Builder(JRPublishActivity.this)
                         .setMessage(mDialogErrorMessage)
                         .setPositiveButton("Dismiss", null)
                         .create();
             case DIALOG_CONFIRM_SIGNOUT:
                 return new AlertDialog.Builder(JRPublishActivity.this)
                         .setMessage("Sign out of " + mSelectedProvider.getFriendlyName() + "?")
                         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 signOutButtonHandler();
                             }
                         })
                         .setNegativeButton("Cancel", null)
                         .create();
             case DIALOG_MOBILE_CONFIG_LOADING:
                 ProgressDialog pd = new ProgressDialog(JRPublishActivity.this);
                 pd.setCancelable(false);
                 pd.setTitle("");
                 pd.setMessage("Loading first run configuration data. Please wait...");
                 pd.setIndeterminate(false);
                 return pd;
         }
         return null;
     }
 
     private void signOutButtonHandler() {
         logUserOutForProvider(mSelectedProvider.getName());
         showUserAsLoggedIn(false);
 
         mAuthenticatedUser = null;
         mProvidersThatHaveAlreadyShared.put(mSelectedProvider.getName(), false);
         onTabChanged(getTabHost().getCurrentTabTag());
     }
 
     protected void onPrepareDialog(int id, Dialog d) {
         switch (id) {
             case DIALOG_FAILURE:
                 ((AlertDialog) d).setMessage(mDialogErrorMessage);
                 break;
         }
     }
 
     /* UI property updaters */
 
     private void configureSharedStatusBasedOnProvider() {
         showActivityAsShared(mProvidersThatHaveAlreadyShared.get(mSelectedProvider.getName()));
     }
 
     public void updateCharacterCount() {
         // TODO: verify correctness of the 0 remaining characters edge case
         CharSequence characterCountText;
 
         if (mSelectedProvider.getSocialSharingProperties()
                 .getAsBoolean("content_replaces_action")) {
             /* Twitter, MySpace, LinkedIn */
             if (doesActivityUrlAffectCharacterCountForSelectedProvider()
                     && mShortenedActivityURL == null) {
                 /* Twitter, MySpace */
                 characterCountText = getText(R.string.jr_calculating_remaining_characters);
             } else {
                 int preview_length = mPreviewLabelView.getText().length();
                 int chars_remaining = mMaxCharacters - preview_length;
                 if (chars_remaining < 0)
                     characterCountText = Html.fromHtml("Remaining characters: <font color=red>"
                             + chars_remaining + "</font>");
                 else
                     characterCountText = Html.fromHtml("Remaining characters: " + chars_remaining);
             }
         } else { /* Facebook, Yahoo */
             int comment_length = mUserCommentView.getText().length();
             int chars_remaining = mMaxCharacters - comment_length;
             if (chars_remaining < 0)
                 characterCountText = Html.fromHtml(
                         "Remaining characters: <font color=red>" + chars_remaining + "</font>");
             else
                 characterCountText = Html.fromHtml("Remaining characters: " + chars_remaining);
         }
 
         mCharacterCountView.setText(characterCountText);
         Log.d(TAG, "updateCharacterCount: " + characterCountText);
     }
 
     private void updateUserCommentView() {
         //mUserHasEditedText = true;
 
         if (mSelectedProvider.getSocialSharingProperties()
                 .getAsBoolean("content_replaces_action")) {
             /* Twitter, MySpace, LinkedIn */
             updatePreviewTextWhenContentReplacesAction();
         } /* ... else Yahoo or Facebook */
     }
 
     private void updatePreviewTextWhenContentReplacesAction() {
         String newText = (!mUserCommentView.getText().toString().equals("")) ?
                   mUserCommentView.getText().toString()
                 : mActivityObject.getAction();
 
         String userNameForPreview = getUserDisplayName();
 
         String shorteningText = getString(R.string.jr_shortening_url);
 
         if (doesActivityUrlAffectCharacterCountForSelectedProvider()) { /* Twitter/MySpace -> true */
             mPreviewLabelView.setText(Html.fromHtml(
                     "<b>" + userNameForPreview + "</b> " + newText + " <font color=\"#808080\">" +
                     ((mShortenedActivityURL != null) ? mShortenedActivityURL : shorteningText) +
                     "</font>"));
 
 //            SpannableStringBuilder str = new SpannableStringBuilder(userNameForPreview + " " + newText + " " +
 //                                            ((mShortenedActivityURL != null) ? mShortenedActivityURL : R.string.jr_shortening_url));//mPreviewLabelView.getText();//.getText();
 //
 //            // Create our span sections, and assign a format to each.
 //            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, userNameForPreview.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 //            str.setSpan(new ColorSpan(android.graphics.Typeface. 0xFFFFFF00), 8, 19, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 //            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 21, str.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 //
 //            mPreviewLabelView.setText(,);
         } else {
             mPreviewLabelView.setText(Html.fromHtml("<b> " + userNameForPreview + "</b> "
                     + newText));
         }
     }
 
     private void updatePreviewTextWhenContentDoesNotReplaceAction() {
         mPreviewLabelView.setText(Html.fromHtml("<b>" + getUserDisplayName() + "</b> "
                 + mActivityObject.getAction()));
     }
 
     private String getUserDisplayName() {
         String userNameForPreview = "You";
         if (mAuthenticatedUser != null) userNameForPreview = mAuthenticatedUser.getPreferredUsername();
         return userNameForPreview;
     }
 
     private void loadUserNameAndProfilePicForUserForProvider(
             final JRAuthenticatedUser user,
             final String providerName) {
         Log.d(TAG, "loadUserNameAndProfilePicForUserForProvider");
 
         if (user == null || providerName == null) {
             mUserNameView.setText("");
             mUserProfilePic.setImageResource(R.drawable.jr_profilepic_placeholder);
             return;
         }
 
         mUserNameView.setText(user.getPreferredUsername());
 
         FileInputStream fis;
         try {
             fis = openFileInput("userpic~" + user.getCachedProfilePicKey());
         } catch (FileNotFoundException e) {
             fis = null;
         } catch (UnsupportedOperationException e) {
             fis = null;
         }
 
         Bitmap cachedProfilePic = BitmapFactory.decodeStream(fis);
 
         if (cachedProfilePic != null) {
             mUserProfilePic.setImageBitmap(cachedProfilePic);
         } else if (user.getPhoto() != null) {
             mUserProfilePic.setImageResource(R.drawable.jr_profilepic_placeholder);
             new AsyncTask<Void, Void, Bitmap>() {
                 protected Bitmap doInBackground(Void... voids) {
                     try {
                         URL url = new URL(user.getPhoto());
                         URLConnection urlc = url.openConnection();
                         InputStream is = urlc.getInputStream();
                         BufferedInputStream bis = new BufferedInputStream(is);
                         bis.mark(urlc.getContentLength());
                         FileOutputStream fos = openFileOutput("userpic~" +
                                 user.getCachedProfilePicKey(), MODE_PRIVATE);
                         int x;
                         while ((x = bis.read()) != -1) fos.write(x);
                         fos.close();
                         bis.reset();
                         return BitmapFactory.decodeStream(bis);
                     } catch (IOException e) {
                         Log.d(TAG, "profile pic image loader exception: " + e.toString());
                         // TODO: set default profile pic?
                         return null;
                     }
                 }
 
                 protected void onPostExecute(Bitmap b) {
                     if (mSelectedProvider.getName().equals(providerName))
                         mUserProfilePic.setImageBitmap(b);
                 }
             }.execute();
         } else {
             mUserProfilePic.setImageResource(R.drawable.jr_profilepic_placeholder);
         }
     }
 
     private void showActivityAsShared(boolean shared) {
         Log.d(TAG, new Exception().getStackTrace()[0].getMethodName() + ": " + shared);
 
         int visibleIfShared = shared ? View.VISIBLE : View.GONE;
         int visibleIfNotShared = !shared ? View.VISIBLE : View.GONE;
 
         mSharedTextAndCheckMarkContainer.setVisibility(visibleIfShared);
 
         if (mAuthenticatedUser != null)
             mJustShareButton.setVisibility(visibleIfNotShared);
         else
             mConnectAndShareButton.setVisibility(visibleIfNotShared);
     }
 
     private void showUserAsLoggedIn(boolean loggedIn) {
         Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
 
         int visibleIfLoggedIn = loggedIn ? View.VISIBLE : View.GONE;
         int visibleIfNotLoggedIn = !loggedIn ? View.VISIBLE : View.GONE;
 
         mJustShareButton.setVisibility(visibleIfLoggedIn);
         mUserProfileContainer.setVisibility(visibleIfLoggedIn);
 
         mConnectAndShareButton.setVisibility(visibleIfNotLoggedIn);
 
         if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action"))
             updatePreviewTextWhenContentReplacesAction();
         else
             updatePreviewTextWhenContentDoesNotReplaceAction();
 
         int scaledPadding = scaleDipPixels(240);
         if (loggedIn) mTriangleIconView.setPadding(0, 0, scaledPadding, 0);
         else mTriangleIconView.setPadding(0, 0, 0, 0);
     }
 
     private void configureViewElementsBasedOnProvider() {
         JRDictionary socialSharingProperties = mSelectedProvider.getSocialSharingProperties();
 
         if (socialSharingProperties.getAsBoolean("content_replaces_action"))
             updatePreviewTextWhenContentReplacesAction();
         else
             updatePreviewTextWhenContentDoesNotReplaceAction();
 
         if (isPublishThunk()) {
             mMaxCharacters = socialSharingProperties
                     .getAsDictionary("set_status_properties").getAsInt("max_characters");
         } else {
             mMaxCharacters = socialSharingProperties.getAsInt("max_characters");
         }
         
         if (mMaxCharacters != -1) mCharacterCountView.setVisibility(View.VISIBLE);
         else mCharacterCountView.setVisibility(View.GONE);
 
         updateCharacterCount();
 
         boolean can_share_media = socialSharingProperties.getAsBoolean("can_share_media");
 
         /* Switch on or off the media content view based on the presence of media and ability to
          * display it */
         boolean showMediaContentView = mActivityObject.getMedia().size() > 0 && can_share_media;
         mMediaContentView.setVisibility(showMediaContentView ? View.VISIBLE : View.GONE);
 
         /* Switch on or off the action label view based on the provider accepting an action */
         //boolean contentReplacesAction = socialSharingProperties.getAsBoolean("content_replaces_action");
         //mPreviewLabelView.setVisibility(contentReplacesAction ? View.GONE : View.VISIBLE);
 
         Object colorValues = socialSharingProperties.get("color_values");
         int colorWithAlpha = colorForProviderFromArray(colorValues, true);
         int colorNoAlpha = colorForProviderFromArray(colorValues, false);
 
         mUserProfileInformationAndShareButtonContainer.setBackgroundColor(colorWithAlpha);
 
         mJustShareButton.setColor(colorNoAlpha);
         mConnectAndShareButton.setColor(colorNoAlpha);
         mPreviewBorder.getBackground().setColorFilter(colorNoAlpha, PorterDuff.Mode.SRC_ATOP);
 
         mProviderIcon.setImageDrawable(mSelectedProvider.getProviderListIconDrawable(this));
     }
 
     private int colorForProviderFromArray(Object arrayOfColorStrings, boolean withAlpha) {
 
         if (!(arrayOfColorStrings instanceof ArrayList))
             if (withAlpha)
                 /* If there's ever an error, just return Janrain blue (at 20% opacity) */
                 return JANRAIN_BLUE_20PERCENT;
             else
                 return JANRAIN_BLUE_100PERCENT;
 
         @SuppressWarnings("unchecked")
         ArrayList<Double> colorArray = new ArrayList<Double>(
                 (ArrayList<Double>) arrayOfColorStrings);
 
         /* We need to reorder the array (which is RGBA, the color format returned by Engage) to the color format
          * used by Android (which is ARGB), by moving the last element (alpha) to the front */
         Double alphaValue = colorArray.remove(3);
         if (withAlpha)
             colorArray.add(0, alphaValue);
         else
             colorArray.add(0, 1.0);
         
         int finalColor = 0;
         for (Object colorValue : colorArray) {
             /* If there's ever an error, just return Janrain blue (at 20% opacity) */
             if(!(colorValue instanceof Double))
                 return JANRAIN_BLUE_20PERCENT;
 
             double colorValue_Fraction = (Double)colorValue;
 
             /* First, get an int from the double, which is the decimal percentage of 255 */
             int colorValue_Int = (int)(colorValue_Fraction * 255.0);
 
             finalColor *= 256;
             finalColor += colorValue_Int;
 
         }
 
         return finalColor;
     }
 
     private void configureLoggedInUserBasedOnProvider() {
         mAuthenticatedUser = mSessionData.getAuthenticatedUserForProvider(mSelectedProvider);
 
         loadUserNameAndProfilePicForUserForProvider(mAuthenticatedUser,
                 mSelectedProvider.getName());
 
         showUserAsLoggedIn(mAuthenticatedUser != null);
     }
 
     /* UI state updaters */
 
     private void logUserOutForProvider(String provider) {
         Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
         mSessionData.forgetAuthenticatedUserForProvider(provider);
         mAuthenticatedUser = null;
     }
 
     private void authenticateUserForSharing() {
      /* Set weHaveJustAuthenticated to true, so that when this view returns (for whatever reason...
       * successful auth user canceled, etc), the view will know that we just went through the
       * authentication process. */
         mWeHaveJustAuthenticated = true;
         mSessionData.setCurrentlyAuthenticatingProvider(mSelectedProvider);
 
      /* If the selected provider requires input from the user, go to the user landing view. Or if
       * the user started on the user landing page, went back to the list of providers, then selected
       * the same provider as their last-used provider, go back to the user landing view. */
         if (mSelectedProvider.requiresInput()) {
             JRUserInterfaceMaestro.getInstance().showUserLanding();
         } else { /* Otherwise, go straight to the web view. */
             JRUserInterfaceMaestro.getInstance().showWebView();
         }
     }
 
     private void shareActivity() {
         Log.d(TAG, "shareActivity mAuthenticatedUser: " + mAuthenticatedUser.toString());
 
         if (isPublishThunk()) {
             mSessionData.setStatusForUser(mAuthenticatedUser);
         } else {
             mSessionData.shareActivityForUser(mAuthenticatedUser);
         }
     }
 
     /* Helper functions */
 
     private boolean isPublishThunk() {
         return mActivityObject.getUrl().equals("") &&
                 mSelectedProvider.getSocialSharingProperties()
                         .getAsBoolean("uses_set_status_if_no_url");
     }
 
     public boolean doesActivityUrlAffectCharacterCountForSelectedProvider() {
         boolean url_reduces_max_chars = mSelectedProvider.getSocialSharingProperties()
                 .getAsBoolean("url_reduces_max_chars");
         boolean shows_url_as_url = mSelectedProvider.getSocialSharingProperties()
                 .getAsString("shows_url_as").equals("url");
 
         /* Twitter/MySpace -> true */
         return (url_reduces_max_chars && shows_url_as_url);
     }
 
     private int scaleDipPixels(int dip) {
         final float scale = getResources().getDisplayMetrics().density;
         return (int) (((float) dip) * scale);
     }
 
     /* JRSessionDelegate definition */
 
     private JRSessionDelegate createSessionDelegate() {
         return new JRSessionDelegate.SimpleJRSessionDelegate() {
             public void authenticationDidRestart() {
                 Log.d(TAG, "[authenticationDidRestart]");
 
                 //mWeAreCurrentlyPostingSomething = false;
                 mWeHaveJustAuthenticated = false;
                 mLayoutHelper.dismissProgressDialog();
             }
 
             /* Should never have to worry about */
 //            public void authenticationDidCancel() {
 //                Log.d(TAG, "[authenticationDidCancel]");
 //                mWeAreCurrentlyPostingSomething = false;
 //                mWeHaveJustAuthenticated = false;
 //            }
 
             public void authenticationDidFail(JREngageError error, String provider) {
                 Log.d(TAG, "[authenticationDidFail]");
                 /* This happens if the mobile endpoint URL fails to be read correctly
                  * or if Engage completes with an error (like rpxstaging via facebook) or maybe if
                  * a provider is down. */
 
                 mWeHaveJustAuthenticated = false;
                 //mWeAreCurrentlyPostingSomething = false;
                 mLayoutHelper.dismissProgressDialog();
 
                 /* We don't need to show a dialog because the WebView has already shown one. */
                 //mDialogErrorMessage = error.getMessage();
                 //showDialog(DIALOG_FAILURE);
             }
 
             public void authenticationDidComplete(JRDictionary profile, String provider) {
                 Log.d(TAG, "[authenticationDidComplete]");
                 mAuthenticatedUser = mSessionData.getAuthenticatedUserForProvider(
                         mSelectedProvider);
 
                 mLayoutHelper.showProgressDialog();
                 loadUserNameAndProfilePicForUserForProvider(mAuthenticatedUser, provider);
                 showUserAsLoggedIn(true);
 
                 shareActivity();
             }
 
             //public void publishingDidRestart() {
             //    //mWeAreCurrentlyPostingSomething = false;
             //}
             //public void publishingDidCancel() {
             //    //mWeAreCurrentlyPostingSomething = false;
             //}
             //
             ////nothing triggers this yet
             //public void publishingDidComplete() {
             //    //mWeAreCurrentlyPostingSomething = false;
             //}
 
             public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {
                 Log.d(TAG, "[publishingJRActivityDidSucceed]");
 
                 mProvidersThatHaveAlreadyShared.put(provider, true);
 
                 mLayoutHelper.dismissProgressDialog();
                 showActivityAsShared(true);
 
                 //mWeAreCurrentlyPostingSomething = false;
                 mWeHaveJustAuthenticated = false;
             }
 
             public void publishingJRActivityDidFail(JRActivityObject activity,
                                                     JREngageError error,
                                                     String provider) {
                 Log.d(TAG, "[publishingJRActivityDidFail]");
                 boolean reauthenticate = false;
                 mDialogErrorMessage = "";
 
                 mLayoutHelper.dismissProgressDialog();
 
                 switch (error.getCode())
                 {// TODO: add strings to string resource file
                     case JREngageError.SocialPublishingError.FAILED:
                         //mDialogErrorMessage = "There was an error while sharing this activity.";
                         mDialogErrorMessage = error.getMessage();
                         break;
                     case JREngageError.SocialPublishingError.DUPLICATE_TWITTER:
                         mDialogErrorMessage =
                                 "There was an error while sharing this activity: Twitter does not allow duplicate status updates.";
                         break;
                     case JREngageError.SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED:
                         mDialogErrorMessage =
                                 "There was an error while sharing this activity: Status was too long.";
                         break;
                     case JREngageError.SocialPublishingError.MISSING_API_KEY:
                         mDialogErrorMessage = "There was an error while sharing this activity.";
                         reauthenticate = true;
                         break;
                     case JREngageError.SocialPublishingError.INVALID_OAUTH_TOKEN:
                         mDialogErrorMessage = "There was an error while sharing this activity.";
                         reauthenticate = true;
                         break;
                     default:
                         //mDialogErrorMessage = "There was an error while sharing this activity.";
                         mDialogErrorMessage = error.getMessage();
                         break;
                 }
 
              /* OK, if this gets called right after authentication succeeds, then the navigation
               * controller won't be done animating back to this view.  If this view isn't loaded
               * yet, and we call shareButtonPressed, then the library will end up trying to push the
               * webview controller onto the navigation controller while the navigation controller
               * is still trying to pop the webview.  This creates craziness, hence we check for
               * [self isViewLoaded]. Also, this prevents an infinite loop of reauthing-failed
               * publishing-reauthing-failed publishing. So, only try and reauthenticate is the
               * publishing activity view is already loaded, which will only happen if we didn't
               * JUST try and authorize, or if sharing took longer than the time it takes to pop the
               * view controller. */
                 if (reauthenticate && !mWeHaveJustAuthenticated) {
                     Log.d(TAG, "reauthenticating user for sharing");
                     logUserOutForProvider(provider);
                     authenticateUserForSharing();
 
                     return;
                 }
 
                 //mWeAreCurrentlyPostingSomething = false;
                 mWeHaveJustAuthenticated = false;
 
                 showDialog(DIALOG_FAILURE);
             }
 
             public void mobileConfigDidFinish() {
                 if (mWaitingForMobileConfig) {
                     dismissDialog(DIALOG_MOBILE_CONFIG_LOADING);
                     mWaitingForMobileConfig = false;
                     initializeWithProviderConfiguration();
                 }
             }
         };
     }
 }
