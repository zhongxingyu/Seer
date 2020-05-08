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
 package com.janrain.android.engage;
 
 
 // package.html type package documentation tag for Doxygen
 
 // removed from the docs until these features are implemented:
 //- Customize the sign-in experience by displaying native and social sign-in options on the same screen
 //- Match the look and feel of the iPhone app with customizable background colors, images, and navigation bar tints
 
 
 /**
  * @mainpage Janrain Engage Android
  *
  * <a href="http://rpxnow.com/docs/android">The
  * Janrain Engage for Android SDK</a> makes it easy to include third party authentication and
  * social publishing in your Android app.  This library includes the same key
  * features as Janrain Engage for the web, as well as additional features created specifically for
  * the mobile platform. With as few as three lines of code, you can authenticate your users with 
  * their accounts on Google, Yahoo!, Facebook, etc., and they can immediately publish their
  * activities to multiple social networks, including Facebook, Twitter, LinkedIn, MySpace,
  * and Yahoo, through one simple interface.
  *
  * Beyond authentication and social sharing, the latest release of the Engage for Android SDK
  * now allows mobile apps to:
  *   - Share content, activities, game scores or invitations via Email or SMS
  *   - Track popularity and click through rates on various links included in the
  *     shared email message with automatic URL shortening for up to 5 URLs
  *   - Provide an additional level of security with forced re-authentication when
  *     users are about to make a purchase or conduct a sensitive transaction
  *   - Configure and maintain separate lists of providers for mobile and web apps
  *
  * Before you begin, you need to have created a <a href="https://rpxnow.com/signup_createapp_plus">
  * Janrain Engage application</a>,
  * which you can do on <a href="http://rpxnow.com">http://rpxnow.com</a>
  *
  * For an overview of how the library works and how you can take advantage of the library's 
  * features, please see the <a href="http://rpxnow.com/docs/android#user_experience">"Overview"</a> 
  * section of our documentation.
  *
  * To begin using the SDK, please see the 
  * <a href="http://rpxnow.com/docs/android#quick">"Quick Start Guide"</a>.
  *
  * For more detailed documentation of the library's API, you can use
  * the <a href="http://rpxnow.com/docs/android_api/annotated.html">"JREngage API"</a> documentation.
  **/
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.session.JRSession;
 import com.janrain.android.engage.session.JRSessionDelegate;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.ui.JRFragmentHostActivity;
 import com.janrain.android.engage.ui.JRPublishFragment;
 import com.janrain.android.engage.ui.JRUiFragment;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @brief
  * The JREngage class provides methods for configuring the Engage for Android library and
  * initiating authentication and social sharing.
  *
  * Prior to using the Engage for Android
  * library, you must already have an application on <a href="http://rpxnow.com">http://rpxnow.com</a>.
  * This is all that is required for authentication, although some providers require
  * configuration (which can be done through your application's <a href="http://rpxnow.com/relying_parties">Dashboard</a>).
  * For social publishing, you will need to configure your Engage application with the desired providers.
  *
  * You may optionally implement server-side authentication.
  * When configured, the Engage for Android library can post the user's authentication token to a URL on your server:
  * the token URL.  Your server can complete authentication, access more of the Engage web API, log the authentication, etc.
  * and the server's response will be passed back through to your Android application.
  *
  * To use JREngage:
  *  - Call JREngage.initInstance
  *  - Save the returned JREngage object
  *  - Invoke showAuthenticationDialog() or showSocialPublishingDialog(JRActivityObject activity)
  *  - You may implement JREngageDelegate to receive responses
  *
  * @nosubgrouping
  **/
 public class JREngage {
 	private static final String TAG = JREngage.class.getSimpleName();
     public static boolean sLoggingEnabled = false;
 
     /* Singleton instance of this class */
 	private static JREngage sInstance;
 
     /* Application context */
     private Activity mActivity;
 
 	/* Holds configuration and state for the JREngage library */
 	private JRSession mSession;
 
 	/* Delegates (listeners) array */
 	private List<JREngageDelegate> mDelegates = new ArrayList<JREngageDelegate>();
     
     /* Listeners to JRSessionDelegate#configDidFinish(); */
     private ArrayList<ConfigFinishListener> mConfigFinishListeners = new ArrayList<ConfigFinishListener>();
 
     private JREngage() {}
 
 /**
  * @name Get the JREngage Instance
  * Methods that initialize and return the shared JREngage instance
  **/
 /*@{*/
     /**
      * Initializes and returns the singleton instance of JREngage.
      *
      * @param activity
      * 		The Android Activity, used for access to system resources (e.g. global
      * 		preferences).  This value cannot be null
      *
      * @param appId
      * 		Your 20-character application ID.  You can find this on your application's
      * 		Engage Dashboard at <a href="http://rpxnow.com">http://rpxnow.com</a>.  This value
      * 		cannot be null
      *
      * @param tokenUrl
      * 		The URL on your server where you wish to complete authentication, or null.  If provided,
      *   	the JREngage library will post the user's authentication token to this URL where it can
      *   	used for further authentication and processing.  When complete, the library will pass
      *   	the server's response back to the your application
 
      * @param delegate
      * 		The delegate object that implements the JREngageDelegate interface
      *
      * @return
      * 		The shared instance of the JREngage object initialized with the given
      *   	appId, tokenUrl, and delegate.  If the given appId is null, returns null
      **/
     public static JREngage initInstance(Activity activity,
                                         String appId,
                                         String tokenUrl,
                                         JREngageDelegate delegate) {
         if (activity == null) {
             Log.e(TAG, "[initialize] context parameter cannot be null.");
             throw new IllegalArgumentException("context parameter cannot be null.");
         }
 
         if (TextUtils.isEmpty(appId)) {
             Log.e(TAG, "[initialize] appId parameter cannot be null.");
             throw new IllegalArgumentException("appId parameter cannot be null.");
         }
 
         logd(TAG, "[initInstance] git resource '" + activity.getString(R.string.jr_git_describe) +
                 "' activity '" + activity + "' appId '" + appId + "' tokenUrl '" + tokenUrl + "'");
 
         if (sInstance == null) sInstance = new JREngage();
         sInstance.initialize(activity, appId, tokenUrl, delegate);
 
         return sInstance;
     }
 
     /**
      * Initializes and returns the singleton instance of JREngage.
      *
      * @deprecated Use #initInstance(Activity, String, String, JREngageDelegate) instead.
      *
      * @param context
      * 		The Android Activity, used for access to system resources (e.g. global
      * 		preferences).  This value cannot be null
      *
      * @param appId
      * 		Your 20-character application ID.  You can find this on your application's
      * 		Engage Dashboard at <a href="http://rpxnow.com">http://rpxnow.com</a>.  This value
      * 		cannot be null
      *
      * @param tokenUrl
      * 		The URL on your server where you wish to complete authentication, or null.  If provided,
      *   	the JREngage library will post the user's authentication token to this URL where it can
      *   	used for further authentication and processing.  When complete, the library will pass
      *   	the server's response back to the your application
 
      * @param delegate
      * 		The delegate object that implements the JREngageDelegate interface
      *
      * @return
      * 		The shared instance of the JREngage object initialized with the given
      *   	appId, tokenUrl, and delegate.  If the given appId is null, returns null
      **/
     public static JREngage initInstance(Context context,
                                         String appId,
                                         String tokenUrl,
                                         JREngageDelegate delegate) {
         return initInstance((Activity) context, appId, tokenUrl, delegate);
     }
 
 	/**
 	 * Returns the singleton instance, provided it has been initialized.
 	 *
 	 * @return
 	 * 		The JREngage instance if properly initialized, null otherwise
 	 **/
 	public static JREngage getInstance() {
 		return sInstance;
 	}
  /*@}*/
 
 	/**
      * @internal
      * Returns the Activity context used to initialize the library.
 	 *
 	 * @return
 	 * 		The Activity object used to initialize this library
 	 **/
     public static Activity getActivity() {
         return (sInstance == null) ? null : sInstance.mActivity;
     }
 
     /**
      * @internal
      * @deprecated use setActivityContext(Activity) instead
      * Set the Activity used to start Engage dialogs from
      *
      * @param context
      *      An Activity from which startActivity will be called
      *
      **/
     public static void setContext(Context context) {
         sInstance.mActivity = (Activity) context;
     }
 
     /**
      * @internal
      * Returns the application context used to initialize the library.
      *
      * @param activity
      *      An Activity from which startActivity will be called
      **/
     public static void setActivityContext(Activity activity) {
         sInstance.mActivity = activity;
     }
 
 	private void initialize(Activity activity,
                             String appId,
                             String tokenUrl,
                             JREngageDelegate delegate) {
         mActivity = activity;
         mDelegates = new ArrayList<JREngageDelegate>();
         if (delegate != null && !mDelegates.contains(delegate)) mDelegates.add(delegate);
         mSession = JRSession.getInstance(appId, tokenUrl, mJrsd);
 
         // Sign-in UI fragment is not yet embeddable
         //if (activity1 instanceof FragmentActivity) {
         //    FrameLayout fragmentContainer = (FrameLayout) activity1.findViewById(R.id.jr_signin_fragment);
         //    if (fragmentContainer != null) {
         //    }
         //}
 	}
 
 /**
  * @name Manage Authenticated Users
  * Methods that manage authenticated users remembered by the library
  **/
 /*@{*/
     /**
      * Remove the user's credentials for the given provider from the library.
      *
      * @param provider
      *   The name of the provider on which the user authenticated.
      *   For a list of possible strings, please see the
      *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#basicProviders">
      *   List of Providers</a>
      **/
     public void signoutUserForProvider(String provider) {
         JREngage.logd(TAG, "[signoutUserForProvider]");
         mSession.forgetAuthenticatedUserForProvider(provider);
     }
 
     /**
      * Remove the user's credentials for all providers from the library.
      **/
     public void signoutUserForAllProviders() {
         JREngage.logd(TAG, "[signoutUserForAllProviders]");
         mSession.forgetAllAuthenticatedUsers();
     }
 
     /**
      * Specify whether the Engage for Android library will require the user to reauthenticate.
      * Reauthentication will require the user to re-enter their password.
      *
      * @param force
      *   \c true if the library should force reauthentication for all providers or \c false if the
      *   library should allow cached credentials to authenticate the user
      **/
     public void setAlwaysForceReauthentication(boolean force) {
         JREngage.logd(TAG, "[setAlwaysForceReauthentication]");
         mSession.setAlwaysForceReauth(force);
     }
 /*@}*/
 
 /**
  * @name Cancel the JREngage Dialogs
  * Methods to cancel authentication and social publishing
  **/
 /*@{*/
     /**
      * Stops the authentication flow.  This finishes all Engage for Android activities and returns
      * the calling Activity to the top of the application's activity stack.
      **/
 
     public void cancelAuthentication() {
         JREngage.logd(TAG, "[cancelAuthentication]");
 
         finishJrActivities();
 
         mSession.triggerAuthenticationDidCancel();
     }
 
     /**
      * Stops the publishing flow.  This finishes all Engage for Android activities and returns
      * the calling Activity to the top of the application's activity stack.
      **/
     public void cancelPublishing() {
         JREngage.logd(TAG, "[cancelPublishing]");
 
         finishJrActivities();
 
         mSession.triggerPublishingDidCancel();
     }
 /*@}*/
 
     /**
      * @internal
      */
     private void finishJrActivities() {
         Intent intent = new Intent(JRFragmentHostActivity.ACTION_FINISH_FRAGMENT);
         intent.putExtra(JRFragmentHostActivity.EXTRA_FINISH_FRAGMENT_TARGET,
                 JRFragmentHostActivity.FINISH_TARGET_ALL);
         mActivity.sendBroadcast(intent);
     }
 
 /**
  * @name Server-side Authentication
  * Methods to configure server-side authentication
  **/
 /*@{*/
 
     /**
      * Specify a token URL (potentially a different token URL than the one the library was
      * initialized with).
      *
      * @param newTokenUrl
      *   The new token URL you wish authentications to post the Engage \e auth_info \e token to
      **/
     public void setTokenUrl(String newTokenUrl) {
         JREngage.logd(TAG, "[setTokenUrl]");
         mSession.setTokenUrl(newTokenUrl);
     }
 
 /*@}*/
 
 /**
  * @name Manage the JREngage Delegates
  * Add/remove delegates that implement the JREngageDelegate interface
  **/
 /*@{*/
 
     /**
      * Add a JREngageDelegate to the library.
      *
      * @param delegate
      *   The object that implements the JREngageDelegate interface
      **/
     public synchronized void addDelegate(JREngageDelegate delegate) {
 		JREngage.logd(TAG, "[addDelegate]");
 		mDelegates.add(delegate);
 	}
 
     /**
      * Remove a JREngageDelegate from the library.
      *
      * @param delegate
      *   The object that implements the JREngageDelegate interface
      **/
 	public synchronized void removeDelegate(JREngageDelegate delegate) {
 		JREngage.logd(TAG, "[removeDelegate]");
 		mDelegates.remove(delegate);
 	}
 /*@}*/
 
     private void engageDidFailWithError(JREngageError error) {
         for (JREngageDelegate delegate : getDelegatesCopy()) {
             delegate.jrEngageDialogDidFailToShowWithError(error);
         }
     }
 
     private boolean checkSessionDataError() {
         /* If there was error configuring the library, sessionData.error will not be null. */
         JREngageError error = mSession.getError();
         if (error != null) {
             /* If there was an error, send a message to the delegates, then
               attempt to restart the configuration.  If, for example, the error was temporary
               (network issues, etc.) reattempting to configure the library could end successfully.
               Since configuration may happen before the user attempts to use the library, if the
               user attempts to use the library at all, we only try to reconfigure when the library
               is needed. */
             if (JREngageError.ErrorType.CONFIGURATION_FAILED.equals(error.getType())) {
                 engageDidFailWithError(error);
                 mSession.tryToReconfigureLibrary();
 
                 return true;
             }
         }
         return false;
     }
 
     private void checkNullJRActivity(JRActivityObject activity) {
         if (activity == null) {
             throw new IllegalArgumentException("Illegal null activity object");
         }
     }
 
 /** @anchor showMethods **/
 /**
  * @name Show the JREngage Dialogs
  * Methods that display the Engage for Android dialogs which initiate authentication and
  * social publishing
  **/
 /*@{*/
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      **/
     public void showAuthenticationDialog() {
         JREngage.logd(TAG, "[showAuthenticationDialog]");
 
         showAuthenticationDialog(false);
     }
     
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      *
      * @param skipReturningUserLandingPage
      *  Prevents the dialog from opening to the returning-user landing page when \c true.  That is, the
      *  dialog will always open straight to the list of providers.  The dialog falls back to the default
      *  behavior when \c false
      *
      * @note
      *  If you always want to force the user to re-enter his/her credentials, pass \c true to the method
      *  setAlwaysForceReauthentication().
      **/
     public void showAuthenticationDialog(boolean skipReturningUserLandingPage) {
         JREngage.logd(TAG, "[showAuthenticationDialog]: " + skipReturningUserLandingPage);
         
         showAuthenticationDialog(skipReturningUserLandingPage, null);
     }
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      *
      * @param provider
      *  Specify a provider to start authentication with. No provider selection list will be shown, the user
      *  will be brought directly to authentication with this provider. 
      *  If null the user will be shown the provider list as usual.
      *
      * @note
      *  If you always want to force the user to re-enter his/her credentials, pass \c true to the method
      *  setAlwaysForceReauthentication().
      **/
     public void showAuthenticationDialog(String provider) {
         showAuthenticationDialog(null, provider);
     }
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      *
      * @param skipReturningUserLandingPage
      *  Prevents the dialog from opening to the returning-user landing page when \c true.  That is, the
      *  dialog will always open straight to the list of providers.  The dialog falls back to the default
      *  behavior when \c false
      *  
      * @param provider
      *  Specify a provider to start authentication with. No provider selection list will be shown, the user
      *  will be brought directly to authentication with this provider. 
      *  If null the user will be shown the provider list as usual.
      *
      * @note
      *  If you always want to force the user to re-enter his/her credentials, pass \c true to the method
      *  setAlwaysForceReauthentication().
      **/
     public void showAuthenticationDialog(Boolean skipReturningUserLandingPage, final String provider) {
         if (checkSessionDataError()) return;
 
         if (skipReturningUserLandingPage != null) mSession.setSkipLandingPage(skipReturningUserLandingPage);
 
        JRProvider p = mSession.getProviderByName(provider);

        if (p == null && !mSession.isGetMobileConfigDone()) {
             final ProgressDialog pd = new ProgressDialog(mActivity);
             pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
             pd.setIndeterminate(true);
             pd.setCancelable(false);
             pd.show();
 
             // Fix up the progress dialog's appearance
             View message = pd.findViewById(android.R.id.message);
             if (message != null) message.setVisibility(View.GONE);
             View progressBar = pd.findViewById(android.R.id.progress);
             if (progressBar != null &&
                     progressBar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                 ((ViewGroup.MarginLayoutParams) progressBar.getLayoutParams()).setMargins(0, 0, 0, 0);
             }
 
             mConfigFinishListeners.add(new ConfigFinishListener() {
                 public void configDidFinish() {
                     mConfigFinishListeners.remove(this);
                     checkSessionDataError();
                     pd.dismiss();
                     showDirectProviderFlowInternal(provider);
                 }
             });
         } else {
             showDirectProviderFlowInternal(provider);
         }
     }
 
     /**
      * @internal 
      * @hide
      */
     private interface ConfigFinishListener {
         void configDidFinish();
     }
 
     /**
      * @internal 
      * @hide
      */
     private void showDirectProviderFlowInternal(String provider) {
         Intent i;
         JRProvider p = mSession.getProviderByName(provider);
         if (p == null) {
             Log.e(TAG, "Provider " + provider + " is not in the set of configured providers.");
             i = JRFragmentHostActivity.createProviderListIntent(mActivity);
         } else {
             if (p.requiresInput()) {
                 i = JRFragmentHostActivity.createUserLandingIntent(mActivity);
             } else {
                 i = JRFragmentHostActivity.createWebViewIntent(mActivity);
             }
             i.putExtra(JRFragmentHostActivity.JR_PROVIDER, provider);
             p.setForceReauth(true);
             mSession.setCurrentlyAuthenticatingProvider(p);
         }
 
         i.putExtra(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_AUTH);
         mActivity.startActivity(i);
     }
 
     /**
      * Begin social publishing.  The library will start a new Android \e Activity enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      *
      * @param activity
      *   The activity you wish to share
      **/
     public void showSocialPublishingDialog(JRActivityObject activity) {
         JREngage.logd(TAG, "[showSocialPublishingDialog]");
         /* If there was error configuring the library, sessionData.error will not be null. */
         if (checkSessionDataError()) return;
         checkNullJRActivity(activity);
         mSession.setJRActivity(activity);
 
         Intent i = JRFragmentHostActivity.createIntentForCurrentScreen(mActivity, false);
         i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, JRFragmentHostActivity.JR_PUBLISH);
         i.putExtra(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_SHARING);
         mActivity.startActivity(i);
     }
 
     /**
      * Begin social publishing.  The library will display a new Android \e Fragment enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      *
      * @param activity
      *   The activity you wish to share
      * @param hostActivity
      *   The android.support.v4.app.FragmentActivity which will host the publishing fragment
      * @param containerId
      *   The resource ID of a FrameLayout to embed the publishing fragment in
      * @param addToBackStack
      *   True if the publishing fragment should be added to the back stack, false otherwise
      * @param transit
      *   Select a standard transition animation for this transaction. See FragmentTransaction#setTransition.
      *   Null for not set
      * @param transitRes
      *   Set a custom style resource that will be used for resolving transit animations. Null for not set
      * @param customEnterAnimation
      *   Set a custom enter animation. May be null if-and-only-if customExitAnimation is also null
      * @param customExitAnimation
      *   Set a custom exit animation.  May be null if-and-only-if customEnterAnimation is also null
      **/
     public void showSocialPublishingFragment(JRActivityObject activity,
                                              FragmentActivity hostActivity,
                                              int containerId,
                                              boolean addToBackStack,
                                              Integer transit,
                                              Integer transitRes,
                                              Integer customEnterAnimation,
                                              Integer customExitAnimation) {
         JREngage.logd(TAG, "[showSocialPublishingFragment]");
         checkNullJRActivity(activity);
 
         JRUiFragment f = createSocialPublishingFragment(activity);
         Bundle arguments = new Bundle();
         arguments.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_SHARING);
         f.setArguments(arguments);
         showFragment(f,
                 hostActivity,
                 containerId,
                 addToBackStack,
                 transit,
                 transitRes,
                 customEnterAnimation,
                 customExitAnimation);
     }
 
     /**
      * Begin social publishing.  The library will display a new Android \e Fragment enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      * This simple variant displays the Fragment, does not add it to the Fragment back stack, and uses default
      * animations.
      *
      * @param activity
      *   The activity you wish to share
      * @param hostActivity
      *   The android.support.v4.app.FragmentActivity which will host the publishing fragment
      * @param containerId
      *   The resource ID of a FrameLayout to embed the publishing fragment in
      **/
     public void showSocialPublishingFragment(JRActivityObject activity,
                                              FragmentActivity hostActivity,
                                              int containerId) {
         showSocialPublishingFragment(activity, hostActivity, containerId, false, null, null, null, null);
     }
 
     /**
      * Create a new android.support.v4.Fragment for social publishing.  Use this if you wish to manage the
      * FragmentTransaction yourself.
      *
      * @param activity
      *  The JRActivityObject to share, may not be null
      *
      * @return
      *  The created Fragment, or null upon error (caused by library configuration failure)
      */
     public JRPublishFragment createSocialPublishingFragment(JRActivityObject activity) {
         if (checkSessionDataError()) return null;
 
         checkNullJRActivity(activity);
 
         mSession.setJRActivity(activity);
         
         Bundle arguments = new Bundle();
         arguments.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_SHARING);
 
         JRPublishFragment f = new JRPublishFragment();
         f.setArguments(arguments);
         return f;
     }
 
 //    /**
 //     * Begin social sign-in.  The library will display a new Android \e Fragment enabling the user to
 //     * sign in.
 //     *
 //     * @param hostActivity
 //     *   The android.support.v4.app.FragmentActivity which will host the publishing fragment
 //     * @param containerId
 //     *   The resource ID of a FrameLayout to embed the publishing fragment in
 //     * @param addToBackStack
 //     *   True if the publishing fragment should be added to the back stack, false otherwise
 //     * @param transit
 //     *   Select a standard transition animation for this transaction. See FragmentTransaction#setTransition.
 //     *   Null for not set
 //     * @param transitRes
 //     *   Set a custom style resource that will be used for resolving transit animations. Null for not set
 //     * @param customEnterAnimation
 //     *   Set a custom enter animation. May be null if-and-only-if customExitAnimation is also null
 //     * @param customExitAnimation
 //     *   Set a custom exit animation.  May be null if-and-only-if customEnterAnimation is also null
 //     **/
 //    public void showSocialSignInFragment(FragmentActivity hostActivity,
 //                                             int containerId,
 //                                             boolean addToBackStack,
 //                                             Integer transit,
 //                                             Integer transitRes,
 //                                             Integer customEnterAnimation,
 //                                             Integer customExitAnimation) {
 //        JREngage.logd(TAG, "[showSocialSignInFragment]");
 //
 //        JRUiFragment f = createSocialSignInFragment();
 //Bundle arguments = new Bundle();
 //    arguments.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_AUTH);
 //    f.setArguments(arguments);
 //        showFragment(f,
 //                hostActivity,
 //                containerId,
 //                addToBackStack,
 //                transit,
 //                transitRes,
 //                customEnterAnimation,
 //                customExitAnimation);
 //    }
 
 //    /**
 //     * Begin social sign-in.  The library will display a new Android \e Fragment enabling the user to
 //     * sign in.
 //     * This simple variant displays the Fragment, does not add it to the Fragment back stack, and uses default
 //     * animations.
 //     *
 //     * @param hostActivity
 //     *   The android.support.v4.app.FragmentActivity which will host the publishing fragment
 //     * @param containerId
 //     *   The resource ID of a FrameLayout to embed the publishing fragment in
 //     **/
 //    public void showSocialSignInFragment(FragmentActivity hostActivity,
 //                                             int containerId) {
 //        showSocialSignInFragment(hostActivity, containerId, false, null, null, null, null);
 //    }
 
 //    /**
 //     * Create a new android.support.v4.Fragment for social sign-in.  Use this if you wish to manage the
 //     * FragmentTransaction yourself.
 //     *
 //     * @return
 //     *  The created Fragment, or null upon error (caused by library configuration failure)
 //     */
 //    public JRProviderListFragment createSocialSignInFragment() {
 //        if (checkSessionDataError()) return null;
 //
 //        JRProviderListFragment jplf = new JRProviderListFragment();
 
 //        Bundle arguments = new Bundle();
 //    arguments.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_AUTH);
 //    jplf.setArguments(arguments);
 //        jplf.setArguments(arguments);
 //        return jplf;
 //    }
 /*@}*/
 
     private void showFragment(Fragment fragment,
                               FragmentActivity hostActivity,
                               int containerId,
                               boolean addToBackStack,
                               Integer transit,
                               Integer transitRes,
                               Integer customEnterAnimation,
                               Integer customExitAnimation) {
         if (checkSessionDataError()) return;
 
         View fragmentContainer = hostActivity.findViewById(containerId);
         if (!(fragmentContainer instanceof FrameLayout)) {
             throw new IllegalStateException("No FrameLayout with ID: " + containerId + ". Found: " +
                     fragmentContainer);
         }
 
         FragmentManager fm = hostActivity.getSupportFragmentManager();
 
         FragmentTransaction ft = fm.beginTransaction();
         if (transit != null) ft.setTransition(transit);
         if (transitRes != null) ft.setTransitionStyle(transitRes);
         if (customEnterAnimation != null || customExitAnimation != null) {
             //noinspection ConstantConditions
             ft.setCustomAnimations(customEnterAnimation, customExitAnimation);
         }
         ft.replace(fragmentContainer.getId(), fragment, fragment.getClass().getSimpleName());
         if (addToBackStack) ft.addToBackStack(fragment.getClass().getSimpleName());
         ft.commit();
     }
 
 
 /** @anchor enableProviders **/
 /**
  * @name Enable a Subset of Providers
  * Methods that configure at runtime a subset of providers to use with the JREngage dialogs.  These methods
  * can only configure a subset of the configured and enabled providers found on your Engage application's
  * dashboard.
  **/
 /*@{*/
     
     /**
      * Sets the list of providers that are enabled for authentication.  This does not supersede your
      * RP's deplyoment settings for Android sign-in, as configured on rpxnow.com, it is a supplemental
      * filter to that configuration.
      *
      * @param enabledProviders
      *  A list of providers which will be enabled. This set will be intersected with the set of
      *  providers configured on the Engage Dashboard, that intersection will be the providers that are
      *  actually available to the end-user.
      */
     public void setEnabledAuthenticationProviders(List<String> enabledProviders) {
         mSession.setEnabledAuthenticationProviders(enabledProviders);
     }
 
     /**
      * Convenience variant of setEnabledAuthenticationProviders(List&lt;String>)
      * @param enabledProviders
      *  An array of providers which will be enabled. This set will be intersected with the set of
      *  providers configured on the Engage Dashboard, that intersection will be the providers that are
      *  actually available to the end-user.
      */
     public void setEnabledAuthenticationProviders(String[] enabledProviders) {
         mSession.setEnabledAuthenticationProviders(Arrays.asList(enabledProviders));
     }
 
     /**
      * Sets the list of providers that are enabled for social sharing.  This does not supersede your
      * RP's deplyoment settings for Android social sharing, as configured on rpxnow.com, it is a
      * supplemental filter to that configuration.
      *
      * @param enabledSharingProviders
      *  Which providers to enable for authentication, null for all providers.
      *  A list of social sharing providers which will be enabled. This set will be intersected with the
      *  set of providers configured on the Engage Dashboard, that intersection will be the providers that are
      *  actually available to the end-user.
      */
     public void setEnabledSharingProviders(List<String> enabledSharingProviders) {
         mSession.setEnabledSharingProviders(enabledSharingProviders);
     }
 
     /**
      * Convenience variant of setEnabledSharingProviders(List&lt;String>)
      * @param enabledSharingProviders
      *  An array of social sharing providers which will be enabled. This set will be intersected with the
      *  set of providers configured on the Engage Dashboard, that intersection will be the providers that are
      *  actually available to the end-user.
      */
     public void setEnabledSharingProviders(String[] enabledSharingProviders) {
         mSession.setEnabledSharingProviders(Arrays.asList(enabledSharingProviders));
     }
 /*@}*/
 
     private JRSessionDelegate mJrsd = new JRSessionDelegate.SimpleJRSessionDelegate() {
         public void authenticationDidRestart() {
             JREngage.logd(TAG, "[authenticationDidRestart]");
         }
 
         public void authenticationDidCancel() {
             JREngage.logd(TAG, "[authenticationDidCancel]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) delegate.jrAuthenticationDidNotComplete();
         }
 
         public void authenticationDidComplete(JRDictionary profile, String provider) {
             JREngage.logd(TAG, "[authenticationDidComplete]");
 
             for (JREngageDelegate d : getDelegatesCopy()) {
                 d.jrAuthenticationDidSucceedForUser(profile, provider);
             }
         }
 
         public void authenticationDidFail(JREngageError error, String provider) {
             JREngage.logd(TAG, "[authenticationDidFail]");
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationDidFailWithError(error, provider);
             }
         }
 
         public void authenticationDidReachTokenUrl(String tokenUrl,
                                                    HttpResponseHeaders responseHeaders,
                                                    String payload,
                                                    String provider) {
             JREngage.logd(TAG, "[authenticationDidReachTokenUrl]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationDidReachTokenUrl(tokenUrl, responseHeaders, payload, provider);
             }
         }
 
         public void authenticationCallToTokenUrlDidFail(String tokenUrl,
                                                         JREngageError error,
                                                         String provider) {
             JREngage.logd(TAG, "[authenticationCallToTokenUrlDidFail]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationCallToTokenUrlDidFail(tokenUrl, error, provider);
             }
         }
 
         public void publishingDidCancel() {
             JREngage.logd(TAG, "[publishingDidCancel]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) delegate.jrSocialDidNotCompletePublishing();
         }
 
         public void publishingDidComplete() {
             JREngage.logd(TAG, "[publishingDidComplete]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) delegate.jrSocialDidCompletePublishing();
         }
 
         public void publishingDialogDidFail(JREngageError error) {
             engageDidFailWithError(error);
 
             if (JREngageError.ErrorType.CONFIGURATION_FAILED.equals(error.getType())) {
                 mSession.tryToReconfigureLibrary();
             }
         }
 
         public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {
             JREngage.logd(TAG, "[publishingJRActivityDidSucceed]");
 
             for (JREngageDelegate d : getDelegatesCopy()) d.jrSocialDidPublishJRActivity(activity, provider);
         }
 
         public void publishingJRActivityDidFail(JRActivityObject activity,
                                                 JREngageError error,
                                                 String provider) {
             JREngage.logd(TAG, "[publishingJRActivityDidFail]");
 
             for (JREngageDelegate d : getDelegatesCopy()) {
                 d.jrSocialPublishJRActivityDidFail(activity, error, provider);
             }
         }
 
         @Override
         public void configDidFinish() {
             for (ConfigFinishListener cfl : new ArrayList<ConfigFinishListener>(mConfigFinishListeners)) {
                 cfl.configDidFinish();
             }
         }
     };
 
     private synchronized List<JREngageDelegate> getDelegatesCopy() {
         return new ArrayList<JREngageDelegate>(mDelegates);
     }
 
     public static void logd(String tag, String msg, Throwable tr) {
         if (sLoggingEnabled) Log.d(tag, msg, tr);
     }
 
     public static void logd(String tag, String msg) {
         if (sLoggingEnabled) Log.d(tag, msg);
     }
 }
 
 /**
  * @page Providers
  *
 @htmlonly
 <!-- Script to resize the iFrames; Only works because iFrames origin is on same domain and iFrame
       code contains script that calls this script -->
 <script type="text/javascript">
     function resize(width, height, id) {
         var iframe = document.getElementById(id);
         iframe.width = width;
         iframe.height = height + 50;
         iframe.scrolling = false;
         console.log(width);
         console.log(height);
     }
 </script>
 
 <!-- Redundant attributes to force scrolling to work across multiple browsers -->
 <iframe id="intro" src="../mobile_providers?list=intro&device=android" width="100%" height="100%"
     style="border:none; overflow:hidden;" frameborder="0" scrolling="no">
   Your browser does not support iFrames.
 </iframe>
 @endhtmlonly
 
 @anchor basicProviders
 @htmlonly
 <iframe id="basic" src="../mobile_providers?list=basic&device=android" width="100%" height="100%"
     style="border:none; overflow:hidden;" frameborder="0" scrolling="no">
   Your browser does not support iFrames.
   <a href="../mobile_providers?list=basic&device=android">List of Providers</a>
 </iframe></p>
 @endhtmlonly
 
 @anchor socialProviders
 @htmlonly
 <iframe id="social" src="../mobile_providers?list=social&device=android" width="100%" height="100%"
     style="border:none; overflow:hidden;" frameborder="0" scrolling="no">
   Your browser does not support iFrames.
   <a href="../mobile_providers?list=social&device=android">List of Social Providers</a>
 </iframe></p>
 @endhtmlonly
  *
  **/
