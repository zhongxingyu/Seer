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
 */
 package com.janrain.android.engage.session;
 
 import android.text.TextUtils;
 import android.util.Config;
 import android.util.Log;
 import android.webkit.CookieManager;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.JREngageError.AuthenticationError;
 import com.janrain.android.engage.JREngageError.ConfigurationError;
 import com.janrain.android.engage.JREngageError.ErrorType;
 import com.janrain.android.engage.JREngageError.SocialPublishingError;
 import com.janrain.android.engage.JREnvironment;
 import com.janrain.android.engage.net.CookieHelper;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.prefs.Prefs;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.utils.Archiver;
 import com.janrain.android.engage.utils.IOUtils;
 import com.janrain.android.engage.utils.ListUtils;
 import com.janrain.android.engage.utils.StringUtils;
 import org.apache.http.util.EncodingUtils;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 public class JRSessionData implements JRConnectionManagerDelegate {
 
     // ------------------------------------------------------------------------
     // TYPES
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // STATIC FIELDS
     // ------------------------------------------------------------------------
 
     private static final String TAG = JRSessionData.class.getSimpleName();
     
 	private static JRSessionData sInstance;
 
 //    private static final JREnvironment ENVIRONMENT = JREnvironment.PRODUCTION;
 //    private static final JREnvironment ENVIRONMENT = JREnvironment.STAGING;
     private static final JREnvironment ENVIRONMENT = JREnvironment.LOCAL;
 
     private static final String ARCHIVE_ALL_PROVIDERS = "allProviders";
     private static final String ARCHIVE_BASIC_PROVIDERS = "basicProviders";
     private static final String ARCHIVE_SOCIAL_PROVIDERS = "socialProviders";
     private static final String ARCHIVE_AUTH_USERS_BY_PROVIDER = "jrAuthenticatedUsersByProvider";
 
     private static final String FMT_CONFIG_URL = "%s/openid/mobile_config_and_baseurl?appId=%s&device=android";
 
     // ------------------------------------------------------------------------
     // STATIC INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // STATIC METHODS
     // ------------------------------------------------------------------------
 
 	public static JRSessionData getInstance() {
 		return sInstance;
 	}
 	
 	public static JRSessionData getInstance(String appId, String tokenUrl, JRSessionDelegate delegate) {
 		
 		if (sInstance != null) {
             if (Config.LOGD) {
                 Log.d(TAG, "[getInstance] returning existing instance.");
                 //todo this should probably be an error, as it ignores the parameters instead of
                 //reinstantiating the library
             }
 			return sInstance;
 		}
 		
 		if (TextUtils.isEmpty(appId)) {
             Log.w(TAG, "[getInstance] null instance w/ null appId specified.");
 			return null;
 		}
 
 		sInstance = new JRSessionData(appId, tokenUrl, delegate);
         if (Config.LOGD) {
             Log.d(TAG, "[getInstance] returning new instance.");
         }
 		return sInstance;
 	}
 	
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
 	private ArrayList<JRSessionDelegate> mDelegates;
 	
 	private JRProvider mCurrentlyAuthenticatingProvider;
     private JRProvider mCurrentlyPublishingProvider;
 
     private String mReturningBasicProvider;
 	private String mReturningSocialProvider;
 	
 	private JRDictionary mAllProviders;
 	private ArrayList<String> mBasicProviders;
 	private ArrayList<String> mSocialProviders;
 	private JRDictionary mAuthenticatedUsersByProvider;
 	
 	//private String mSavedConfigurationBlock;
 	private String mNewEtag;
     private String mGitCommit;
 
     //private JRProviderList mProvidersWithIcons;
     //private JRDictionary mIconsStillNeeded;
 
 	private JRActivityObject mActivity;
 	
 	private String mTokenUrl;
 	private String mBaseUrl;
 	private String mAppId;
     private String mDevice;
 
     private boolean mHidePoweredBy;
 
 	private boolean mAlwaysForceReauth;
 
     //private boolean mForceReauth;
 
 	private boolean mSocialSharing;
 	
 	private boolean mDialogIsShowing;
 	private JREngageError mError;
 	
     // ------------------------------------------------------------------------
     // INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
 	private JRSessionData(String appId, String tokenUrl, JRSessionDelegate delegate) {
         if (Config.LOGD) {
             Log.d(TAG, "[ctor] creating instance.");
         }
 
 		mDelegates = new ArrayList<JRSessionDelegate>();
 		mDelegates.add(delegate);
 		
 		mAppId = appId;
 		mTokenUrl = tokenUrl;
 
 //        mAuthenticatedUsersByProvider = new JRDictionary();
 //        mAllProviders = new JRDictionary();
 
         // Load dictionary of authenticated users.  If the dictionary is not found, the
         // archiver will return a new (empty) list.
         mAuthenticatedUsersByProvider = JRDictionary.unarchive(ARCHIVE_AUTH_USERS_BY_PROVIDER);
 
         // Load the list of all providers
         mAllProviders = JRDictionary.unarchive(ARCHIVE_ALL_PROVIDERS);
 
         for (Object provider : mAllProviders.values()) {
             ((JRProvider)provider).loadDynamicVariables();
         }
 
         // Load the list of basic providers
         mBasicProviders = (ArrayList<String>)Archiver.load(ARCHIVE_BASIC_PROVIDERS);
         if (Config.LOGD) {
             if (ListUtils.isEmpty(mBasicProviders)) {
                 Log.d(TAG, "[ctor] basic providers is empty");
             } else {
                 Log.d(TAG, "[ctor] basic providers: [" + TextUtils.join(",", mBasicProviders) + "]");
             }
         }
 
         // Load the list of social providers
         mSocialProviders = (ArrayList<String>)Archiver.load(ARCHIVE_SOCIAL_PROVIDERS);
         if (Config.LOGD) {
             if (ListUtils.isEmpty(mSocialProviders)) {
                 Log.d(TAG, "[ctor] social providers is empty");
             } else {
                 Log.d(TAG, "[ctor] social providers: [" + TextUtils.join(",", mSocialProviders) + "]");
             }
         }
 
         // Load the list of icons that the library should re-attempt to download, in case
         // previous attempts failed for whatever reason
         //mIconsStillNeeded = JRDictionary.unarchive(ARCHIVE_ICONS_STILL_NEEDED);
 
         // Load the set of providers that already have all of their icons; checking this list
         // is faster than checking for the icons themselves
         //mProvidersWithIcons = JRProviderList.unarchive(ARCHIVE_PROVIDERS_WITH_ICONS);
 
         // Load the base url
         mBaseUrl = Prefs.getAsString(Prefs.KEY_JR_BASE_URL, "");
 
         // Figure out of we're suppose to hide the powered by line
         mHidePoweredBy = Prefs.getAsBoolean(Prefs.KEY_JR_HIDE_POWERED_BY, false);
 
         // And load the last used basic and social providers
         mReturningSocialProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, "");
         mReturningBasicProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, "");
 
         mError = startGetConfiguration();
 	}
 	
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     public JREngageError getError() {
         return mError;
     }
 
     public JRActivityObject getActivity() {
         return mActivity;
     }
 
     public void setActivity(JRActivityObject activity) {
         mActivity = activity;
 
         // TODO: Add function equiv. to [self startGetShortenedUrlsForActivity:activity];
         //JRPublishActivity now shortens URLs, don't need to follow TODO?
     }
 
     public boolean getAlwaysForceReauth() {
         return mAlwaysForceReauth;
     }
 
     public void setAlwaysForceReauth(boolean force) {
         mAlwaysForceReauth = force;
     }
 
 //    public void setForceReauth(boolean mForceReauth) {
 //        this.mForceReauth = mForceReauth;
 //    }
 
     public String getTokenUrl() {
         return mTokenUrl;
     }
 
     public void setTokenUrl(String tokenUrl) {
         mTokenUrl = tokenUrl;
     }
 
     public JRProvider getCurrentlyAuthenticatingProvider() {
         return mCurrentlyAuthenticatingProvider;
     }
 
     public void setCurrentlyAuthenticatingProvider(JRProvider provider) {
         Log.d(TAG, "setCurrentlyAuthenticatingProvider: " +
                 (provider != null ? provider.getName() : null));
         
         mCurrentlyAuthenticatingProvider = provider;
     }
 
     public void setCurrentlyAuthenticatingProvider(String providerName) {
         Object provider = mAllProviders.get(providerName);
 
         setCurrentlyAuthenticatingProvider((JRProvider) provider);
     }
 
     public ArrayList<JRProvider> getBasicProviders() {
         ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
         if ((mBasicProviders != null) && (mBasicProviders.size() > 0)) {
             for (String name : mBasicProviders) {
                 JRProvider provider = mAllProviders.getAsProvider(name);
                 providerList.add(provider);
             }
         }
         return providerList;
     }
 
     public ArrayList<JRProvider> getSocialProviders() {
         ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
         if ((mSocialProviders != null) && (mSocialProviders.size() > 0)) {
             for (String name : mSocialProviders) {
                 JRProvider provider = mAllProviders.getAsProvider(name);
                 providerList.add(provider);
             }
         }
         return providerList;
     }
 
     public String getReturningBasicProvider() {
         if (mAlwaysForceReauth)
             return null;
 
         return mReturningBasicProvider;
     }
 
     public void setReturningBasicProvider(String mReturningBasicProvider) {
         this.mReturningBasicProvider = mReturningBasicProvider;
     }
 
     public String getReturningSocialProvider() {
         return mReturningSocialProvider;
     }
 
     public void setReturningSocialProvider(String mReturningSocialProvider) {
         this.mReturningSocialProvider = mReturningSocialProvider;
     }
 
 
     public String getBaseUrl() {
         return mBaseUrl;
     }
 
     public boolean getSocial() {
         return mSocialSharing;
     }
 
     public void setSocial(boolean value) {
         mSocialSharing = value;
     }
 
     public boolean getHidePoweredBy() {
         return mHidePoweredBy;
     }
 
     // ------------------------------------------------------------------------
     // DELEGATE METHODS
     // ------------------------------------------------------------------------
 
     public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
         Log.i(TAG, "[connectionDidFail]");
 
         if (userdata == null) {
             /*
             As of right now, this should not happen.  We specify the tag/userdata in all calls.
             Leave check here for future/possible use.
              */
             Log.w(TAG, "[connectionDidFail] userdata is null, this is unexpected.");
         } else if (userdata instanceof String) {
             String s = (String)userdata;
             if (s.equals("getConfiguration")) {
                 mError = new JREngageError(
                         "There was a problem communicating with the Janrain server while configuring authentication.",
                         ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                         JREngageError.ErrorType.CONFIGURATION_FAILED);
             } else if (s.equals("emailSuccess")) {
                 // TODO: Implement notifications for email/sms sharing
             } else if (s.equals("smsSuccess")) {
                 // TODO: Implement notifications for email/sms sharing
             } else {
 
             }
         } else if (userdata instanceof JRDictionary) {
             JRDictionary dictionary = (JRDictionary) userdata;
             // TODO: Should "tokenUrl" be a key, and if so, to what?  In iPhone lib, it's a value to key "action"
             //todo
             if (dictionary.containsKey("tokenUrl")) {
                 List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                 for (JRSessionDelegate delegate : delegatesCopy) {
                     JREngageError error = new JREngageError(
                         "Session error", JREngageError.CODE_UNKNOWN, "", ex
                     );
                     delegate.authenticationCallToTokenUrlDidFail(
                             dictionary.getAsString("tokenUrl"),
                             error,
                             dictionary.getAsString("providerName"));
                 }
            } else if (dictionary.containsKey("shareActivity")) {
                 List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                 JREngageError error = new JREngageError("Session error", JREngageError.CODE_UNKNOWN, "", ex);
                 for (JRSessionDelegate delegate : delegatesCopy) {
                     delegate.publishingActivityDidFail(
                             (JRActivityObject) dictionary.get("activity"),
                             error,
                             dictionary.getAsString("providerName"));
                 }
             }
         }
 	}
 
     public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
         Log.i(TAG, "[connectionDidFinishLoading]");
 
         if (Config.LOGD) {
             Log.d(TAG, "[connectionDidFinishLoading] payload: " + payload);
         }
 
         if (userdata == null) {
             /*
             As of right now, this should not happen.  We specify the tag/userdata in all calls.
             Leave check here for future/possible use.
              */
             Log.w(TAG, "[connectionDidFinishLoading] userdata is null, this is unexpected.");
         } else if (userdata instanceof String) {
             String tag = (String) userdata;
 
             if (tag.equals("emailSuccess")) {
                 //todo
             } else if (tag.equals("smsSuccess")) {
                 //todo
             } else {
                 //todo
             }
         } else if (userdata instanceof JRDictionary) {
             JRDictionary dictionary = (JRDictionary) userdata;
 
             if (dictionary.getAsString("action").equals("shareActivity"))
                 processShareActivityResponse(payload, dictionary);
 //            } else if (dicitonary.containsKey("shortenUrls")) {
 //                //todo
 //            } else {
 //                //todo
         }
 	}
 
     private void processShareActivityResponse(String payload, JRDictionary userDataTag) {
         // TODO: Move all of this code out of the connection delegate function
         String providerName = userDataTag.getAsString("providerName");
 
         JRDictionary responseDict = JRDictionary.fromJSON(payload);
 
         if (responseDict == null) {
             setCurrentlyPublishingProvider(null);
             List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
             for (JRSessionDelegate delegate : delegatesCopy) {
                 delegate.publishingActivityDidFail(
                         (JRActivityObject) userDataTag.get("activity"),
                         new JREngageError(payload,
                                 SocialPublishingError.FAILED,
                                 ErrorType.PUBLISH_FAILED),
                         providerName);
             }
         } else if (responseDict.containsKey("stat") && ("ok".equals(responseDict.get("stat")))) {
             saveLastUsedSocialProvider();
             setCurrentlyPublishingProvider(null);
             List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
             for (JRSessionDelegate delegate : delegatesCopy) {
                 delegate.publishingActivityDidSucceed(
                         mActivity,
                         providerName);
             }
         } else {
             setCurrentlyPublishingProvider(null);
             JRDictionary errorDict = responseDict.getAsDictionary("err");
             JREngageError publishError;
 
             if (errorDict == null) {
                 publishError = new JREngageError(
                         "There was a problem publishing with this activity",
                         SocialPublishingError.FAILED,
                         ErrorType.PUBLISH_FAILED);
             } else {
                 int code = (errorDict.containsKey("code"))
                         ? errorDict.getAsInt("code") : 1000;
 
                 switch (code) {
                     case 0: /* "Missing parameter: apiKey" */
                         publishError = new JREngageError(
                                 errorDict.getAsString("msg"),
                                 SocialPublishingError.MISSING_API_KEY,
                                 ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                         break;
                     case 4: /* "Facebook Error: Invalid OAuth 2.0 Access Token" */
                         if (errorDict.getAsString("msg").matches(".*nvalid ..uth.*"))
                             publishError = new JREngageError(
                                     errorDict.getAsString("msg"),
                                     SocialPublishingError.INVALID_OAUTH_TOKEN,
                                     ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                         else if (errorDict.getAsString("msg").matches(".*eed action request limit.*"))
                             publishError = new JREngageError(
                                     errorDict.getAsString("msg"),
                                     SocialPublishingError.FEED_ACTION_REQUEST_LIMIT,
                                     ErrorType.PUBLISH_FAILED);
                         else
                             publishError = new JREngageError(
                                     errorDict.getAsString("msg"),
                                     SocialPublishingError.FAILED,
                                     ErrorType.PUBLISH_FAILED);
                         break;
                     case 100: // TODO LinkedIn character limit error
                         publishError = new JREngageError(
                                 errorDict.getAsString("msg"),
                                 SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED,
                                 ErrorType.PUBLISH_INVALID_ACTIVITY);
                         break;
                     case 6: // TODO Twitter duplicate error
                         publishError = new JREngageError(
                                 errorDict.getAsString("msg"),
                                 SocialPublishingError.DUPLICATE_TWITTER,
                                 ErrorType.PUBLISH_INVALID_ACTIVITY);
                         break;
                     case 1000: /* Extracting code failed; Fall through. */
                     default: // TODO Other errors (find them)
                         publishError = new JREngageError(
                                 "There was a problem publishing this activity",
                                 SocialPublishingError.FAILED,
                                 ErrorType.PUBLISH_FAILED);
                         break;
                 }
             }
 
             List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
             for (JRSessionDelegate delegate : delegatesCopy) {
                 delegate.publishingActivityDidFail((JRActivityObject) userDataTag.get("activity"), publishError, providerName);
             }
         }
     }
 
     //todo unify http connection callbacks?
     public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload,
                                            String requestUrl, Object userdata) {
         Log.i(TAG, "[connectionDidFinishLoading-full]");
 
         if (userdata == null) {
             // TODO:  this case is not handled on iPhone, is it possible?
         } else if (userdata instanceof JRDictionary) {
             JRDictionary dictionary = (JRDictionary) userdata;
             if (dictionary.containsKey("tokenUrl")) {
                 List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                 for (JRSessionDelegate delegate : delegatesCopy) {
                     delegate.authenticationDidReachTokenUrl(
                             dictionary.getAsString("tokenUrl"),
                             headers,
                             payload,
                             dictionary.getAsString("providerName"));
                 }
             } //else if (dictionary.containsKey("downloadPicture")) {
               //  // TODO: Add icon downloading code
             //}
         } else if (userdata instanceof String) {
             String s = (String)userdata;
 //            if (TextUtils.isEmpty(s)) {
 //                // TODO:  tag string is null or empty...not handled on iPhone, is it possible?
 //            } else
             if (s.equals("getConfiguration")) {
                 String payloadString = EncodingUtils.getAsciiString(payload);
                 if (Config.LOGD) {
                     Log.d(TAG, "[connectionDidFinishLoading-full] payload string: " + payloadString);
                 }
 
                 if (payloadString.contains("\"provider_info\":{")) {
                     mError = finishGetConfiguration(payloadString, headers.getETag());
                 } else {
                     mError = new JREngageError(
                             "There was a problem communicating with the Janrain server while configuring authentication.",
                             ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                             ErrorType.CONFIGURATION_FAILED);
                 }
 
             }
         }
 	}
 
     public void connectionWasStopped(Object userdata) {
         Log.i(TAG, "[connectionWasStopped]");
         // nothing to do here...
 	}
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 
     public void addDelegate(JRSessionDelegate delegate) {
         if (Config.LOGD) {
             Log.d(TAG, "[addDelegate]");
         }
         mDelegates.add(delegate);
     }
 
     public void removeDelegate(JRSessionDelegate delegate) {
         if (Config.LOGD) {
             Log.d(TAG, "[removeDelegate]");
         }
         mDelegates.remove(delegate);
     }
 
     public void tryToReconfigureLibrary() {
         if (Config.LOGD) {
             Log.d(TAG, "[tryToReconfigureLibrary]");
         }
 
         mError = null;
         mError = startGetConfiguration();
     }
 
     private JREngageError startGetConfiguration() {
         String urlString = String.format(FMT_CONFIG_URL, ENVIRONMENT.getServerUrl(), mAppId);
         if (Config.LOGD) {
             Log.d(TAG, "[startGetConfiguration] url: " + urlString);
         }
 
         final String tag = "getConfiguration";
 
         if (!JRConnectionManager.createConnection(urlString, this, true, tag)) {
             Log.w(TAG, "[startGetConfiguration] createConnection failed.");
             return new JREngageError(
                     "There was a problem connecting to the Janrain server while configuring authentication.",
                     ConfigurationError.URL_ERROR,
                     JREngageError.ErrorType.CONFIGURATION_FAILED);
         }
 
         return null;
     }
 
     private JREngageError finishGetConfiguration(String dataStr) {
         if (Config.LOGD) {
             Log.d(TAG, "[finishGetConfiguration]");
         }
 
         // Attempt to parse the return string as JSON.
         JRDictionary jsonDict = null;
         Exception jsonEx = null;
         try {
             jsonDict = JRDictionary.fromJSON(dataStr);
         } catch (Exception e) {
             Log.w(TAG, "[finishGetConfiguration] json error: ", e);
             jsonEx = e;
         }
 
         // If the parsed dictionary object is null or an exception has occurred, return.
         if ((jsonDict == null) || (jsonEx != null)) {
             Log.w(TAG, "[finishGetConfiguration] failed.");
             return new JREngageError(
                     "There was a problem communicating with the Janrain server while configuring authentication.",
                     ConfigurationError.JSON_ERROR,
                     ErrorType.CONFIGURATION_FAILED,
                     jsonEx);
         }
 
         // Check to see if the base url has changed
         String baseUrl = jsonDict.getAsString("baseurl", "");
         if (!baseUrl.equals(mBaseUrl)) {
             // Save the new base url
             mBaseUrl = StringUtils.chomp(baseUrl, "/");
             Prefs.putString(Prefs.KEY_JR_BASE_URL, mBaseUrl);
         }
 
         // Get the providers out of the provider_info section.  These are likely to have changed.
         JRDictionary providerInfo = jsonDict.getAsDictionary("provider_info");
         mAllProviders = new JRDictionary();
 
         // For each provider
         for (String name : providerInfo.keySet()) {
             // Get its dictionary
             JRDictionary dictionary = providerInfo.getAsDictionary(name);
             // Use this to create a provider object
             JRProvider provider = new JRProvider(name, dictionary);
             // and finally add the object to our dictionary of providers
             mAllProviders.put(name, provider);
         }
 
         // Get the ordered list of basic providers
         mBasicProviders = jsonDict.getAsListOfStrings("enabled_providers");
         // Get the ordered list of social providers
         mSocialProviders = jsonDict.getAsListOfStrings("social_providers");
 
         // Done!
 
         // Save data to local store
         JRDictionary.archive(ARCHIVE_ALL_PROVIDERS, mAllProviders);
         Archiver.save(ARCHIVE_BASIC_PROVIDERS, mBasicProviders);
         Archiver.save(ARCHIVE_SOCIAL_PROVIDERS, mSocialProviders);
 
         // Figure out of we're suppose to hide the powered by line
         mHidePoweredBy = jsonDict.getAsBoolean("hide_tagline", false);
         Prefs.putBoolean(Prefs.KEY_JR_HIDE_POWERED_BY, mHidePoweredBy);
 
         // Once we know everything is parsed and saved, save the new etag
         Prefs.putString(Prefs.KEY_JR_CONFIGURATION_ETAG, mNewEtag);
 
         return null;
     }
 
     private JREngageError finishGetConfiguration(String dataStr, String etag) {
         if (Config.LOGD) {
             Log.d(TAG, "[finishGetConfiguration-etag]");
         }
 
         /* If the configuration for this rp has changed, the etag will have changed, and we need
          * to update our current configuration information. */
         String oldEtag = Prefs.getAsString(Prefs.KEY_JR_CONFIGURATION_ETAG, "");
         //todo implement git commit checking for cache reloading
         if (!oldEtag.equals(etag) || true) {
             mNewEtag = etag;
 
             /* We can only update all of our data if the UI isn't currently using that
              * information.  Otherwise, the library may crash/behave inconsistently.  If a
              * dialog isn't showing, go ahead and update that information.  Or, in the case
              * where a dialog is showing but there isn't any data that it could be using (that
              * is, the lists of basic and social providers are nil), go ahead and update it too.
              * The dialogs won't try and do anything until we're done updating the lists. */
             if (!mDialogIsShowing || (ListUtils.isEmpty(mBasicProviders)
                     && ListUtils.isEmpty(mSocialProviders))) {
                 return finishGetConfiguration(dataStr);
             }
 
             /* Otherwise, we have to save all this information for later.  The
              * UserInterfaceMaestro sends a signal to sessionData when the dialog closes (by
              * setting the boolean dialogIsShowing to "NO". In the setter function, sessionData
              * checks to see if there's anything stored in the savedConfigurationBlock, and
              * updates it then. */
             //mSavedConfigurationBlock = dataStr;
         }
 
         return null;
     }
 
     private String getWelcomeMessageFromCookieString(String cookieString) {
         if (Config.LOGD) {
             Log.d(TAG, "[getWelcomeMessageFromCookieString]");
         }
 
         String retval = "Welcome, user!";
 
         if (!TextUtils.isEmpty(cookieString)) {
             String[] parts = cookieString.split("%22");
             if (parts.length > 5) {
                 String part;
                 try {
                     part = URLDecoder.decode(parts[5], "UTF-8");
                 } catch (UnsupportedEncodingException e) {
                     part = null;
                 }
                 if (!TextUtils.isEmpty(part)) {
                     retval = "Sign in as " + part;
                 }
             }
         }
 
         return retval;
     }
 
     private void loadLastUsedSocialProvider() {
         if (Config.LOGD) {
             Log.d(TAG, "[loadLastUsedSocialProvider]");
         }
         mReturningSocialProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, "");
     }
 
     private void loadLastUsedBasicProvider() {
         if (Config.LOGD) {
             Log.d(TAG, "[loadLastUsedBasicProvider]");
         }
         mReturningBasicProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, "");
     }
 
     private void saveLastUsedSocialProvider() {
         if (Config.LOGD) {
             Log.d(TAG, "[saveLastUsedSocialProvider]");
         }
         mReturningSocialProvider = getCurrentlyPublishingProvider().getName();
         Prefs.putString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, mReturningSocialProvider);
     }
 
     private void saveLastUsedBasicProvider() {
         if (Config.LOGD) {
             Log.d(TAG, "[saveLastUsedBasicProvider]");
         }
 
         //CookieHelper doesn't interact with our WebView's cookies :(
         String cookies = CookieManager.getInstance().getCookie(getBaseUrl());
         String welcome_info = cookies.replaceAll(".*welcome_info=([^;]*).*", "$1");
         mCurrentlyAuthenticatingProvider.setWelcomeString(getWelcomeMessageFromCookieString(welcome_info));
 
         mReturningBasicProvider = mCurrentlyAuthenticatingProvider.getName();
         Prefs.putString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, mReturningBasicProvider);
     }
 
     private void deleteFacebookCookies() {
         CookieHelper.deleteCookiesByUrl("http://login.facebook.com");
     }
 
     private void deleteLiveCookies() {
         CookieHelper.deleteCookiesByUrl("http://live.com");
     }
 
     public URL startUrlForCurrentlyAuthenticatingProvider() {
         if (Config.LOGD) {
             Log.d(TAG, "[startUrlForCurrentlyAuthenticatingProvider]");
         }
 
         if (mCurrentlyAuthenticatingProvider == null) {
             return null;
         }
 
         String oid;
 
         if (!TextUtils.isEmpty(mCurrentlyAuthenticatingProvider.getOpenIdentifier())) {
             oid = String.format("openid_identifier=%s&", mCurrentlyAuthenticatingProvider.getOpenIdentifier());
             if (mCurrentlyAuthenticatingProvider.requiresInput()) {
                 oid = oid.replaceAll("%@", mCurrentlyAuthenticatingProvider.getUserInput());
             } else {
                 oid = oid.replaceAll("%@", "");
             }
         } else {
             oid = "";
         }
 
         String str;
 
         //todo why not other providers too?
         if ("facebook".equals(mCurrentlyAuthenticatingProvider.getName())) {
             if (mAlwaysForceReauth || mCurrentlyAuthenticatingProvider.getForceReauth()) {
                 deleteFacebookCookies();
             }
         } else if ("live_id".equals(mCurrentlyAuthenticatingProvider.getName())) {
             if (mAlwaysForceReauth || mCurrentlyAuthenticatingProvider.getForceReauth()) {
                 deleteLiveCookies();
             }
         }
 
         //str = String.format("%s%s?%s%sversion=android_one&device=android",
         str = String.format("%s%s?%s%sdevice=android&extended=true",
                 mBaseUrl,
                 mCurrentlyAuthenticatingProvider.getStartAuthenticationUrl(),
                 oid,
                 ((mAlwaysForceReauth || mCurrentlyAuthenticatingProvider.getForceReauth()) ? "force_reauth=true&" : "")
         );
 
         mCurrentlyAuthenticatingProvider.setForceReauth(false);
 
         if (Config.LOGD) {
             Log.d(TAG, "[startUrlForCurrentlyAuthenticatingProvider] startUrl: " + str);
         }
 
         URL url = null;
         try {
             url = new URL(str);
         } catch (MalformedURLException e) {
             Log.e(TAG, "[startUrlForCurrentlyAuthenticatingProvider] URL create failed for string: " + str);
         }
         return url;
     }
 
     /* This function is used to determine whether the library should retain first responderness for the
      * UI purposes of making sure that the on screen keyboard stays on screen
     private boolean weShouldBeFirstResponder() {
         if (Config.LOGD) {
             Log.d(TAG, "[weShouldBeFirstResponder]");
         }
 
         /* If we're authenticating with a provider for social publishing, then don't worry about the return experience
          * for basic authentication. *//*
         if (mSocialSharing)
             return mCurrentlyAuthenticatingProvider.requiresInput();
 
         /* If we're authenticating with a basic provider, then we don't need to gather infomation if we're displaying
          * return screen. *//*
         if (mCurrentlyAuthenticatingProvider.isEqualToReturningProvider(mReturningBasicProvider))
             return false;
 
         return mCurrentlyAuthenticatingProvider.requiresInput();
     }*/
 
     public JRAuthenticatedUser getAuthenticatedUserForProvider(JRProvider provider) {
         if (Config.LOGD) {
             Log.d(TAG, "[getAuthenticatedUserForProvider]");
         }
 
         return (JRAuthenticatedUser) mAuthenticatedUsersByProvider.get(provider.getName());
     }
 
     public JRAuthenticatedUser authenticatedUserForProviderNamed(String provider) {
         if (Config.LOGD) {
             Log.d(TAG, "[authenticatedUserForProviderNamed]");
         }
 
         return (JRAuthenticatedUser) mAuthenticatedUsersByProvider.get(provider);
     }
 
     public void forgetAuthenticatedUserForProvider(String providerName) {
         //todo XXX if you forget a user, then click connect and share, it shows the webview with the page for logging
         //in, as expected.  if you hit back without logging in, and then hit connect and share, you're automatically
         //logged in? need to clear the browser cookie? if not then there's no point in showing the webview to begin
         //with (since you can just hit back and then connect again, instead of, you know, typing your password.)
         if (Config.LOGD) {
             Log.d(TAG, "[forgetAuthenticatedUserForProvider]");
         }
 
         //todo delete cookies here.
 
         JRProvider provider = mAllProviders.getAsProvider(providerName);
         if (provider == null) {
             Log.w(TAG, "[forgetAuthenticatedUserForProvider] provider not found: " + providerName);
             throw new RuntimeException();
         } else {
             provider.setForceReauth(true);
             mAuthenticatedUsersByProvider.remove(provider.getName());
 
             JRDictionary.archive(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
         }
     }
 
     public void forgetAllAuthenticatedUsers() {
         if (Config.LOGD) {
             Log.d(TAG, "[forgetAllAuthenticatedUsers]");
         }
 
 
         //todo this should call the above function, "forgetAuthenticatedUserForProvider" eh? abstraction?
         for (String providerName : mAllProviders.keySet()) {
             JRProvider provider = mAllProviders.getAsProvider(providerName);
             provider.setForceReauth(true);
         }
 
         mAuthenticatedUsersByProvider.clear();
 
         JRDictionary.archive(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
     }
 
     public JRProvider getProviderAtIndex(int index, List<String> fromList) {
         String key = fromList.get(index);
         return mAllProviders.getAsProvider(key);
     }
 
     public JRProvider getBasicProviderAtIndex(int index) {
         return getProviderAtIndex(index, mBasicProviders);
     }
 
     public JRProvider getSocialProviderAtIndex(int index) {
         return getProviderAtIndex(index, mSocialProviders);
     }
 
     public JRProvider getProviderByName(String name) {
         return mAllProviders.getAsProvider(name);
     }
 
     public void shareActivityForUser(JRAuthenticatedUser user) {
         if (Config.LOGD) {
             Log.d(TAG, "[shareActivityForUser]");
         }
 
         setCurrentlyPublishingProvider(user.getProviderName());
         setSocial(true);
 
         StringBuilder body = new StringBuilder();
         String deviceToken = user.getDeviceToken();
 
         String activityContent;
         JRDictionary activityDictionary = mActivity.dictionaryForObject();
         try {
             String activityJSON = activityDictionary.toJSON();
             activityContent = URLEncoder.encode(activityJSON, "UTF-8");
             body.append("device_token=").append(deviceToken);
             body.append("&activity=").append(activityContent);
             body.append("&options={\"urlShortening\":\"true\"}"); //this is an undocumented parameter available to the mobile library?
             //TODO include truncate parameter here?
             body.append("&device=android");
         } catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
 
         String url = ENVIRONMENT.getServerUrl() + "/api/v2/activity";
 
         Log.d(TAG, "[shareActivityForUser]: " + url + " data: " + body.toString());
 
         //final String tag = "shareActivity";
         //JRConnectionManager.createConnection(url, body.toString().getBytes(), this, false, tag);
 
         JRDictionary tag = new JRDictionary();
         tag.put("action", "shareActivity");
         tag.put("activity", mActivity);
         tag.put("providerName", mCurrentlyPublishingProvider.getName());
         JRConnectionManager.createConnection(url, body.toString().getBytes(), this, false, tag);
 
         if (Config.LOGD) {
             Log.d(TAG, "[shareActivityForUser] connection started for url: " + url);
         }
     }
 
     private void makeCallToTokenUrl(String tokenUrl, String token, String providerName) {
         if (Config.LOGD) {
             Log.d(TAG, "[makeCallToTokenUrl] token: " + token);
             Log.d(TAG, "[makeCallToTokenUrl] tokenUrl: " + tokenUrl);
         }
 
         String body = "token=" + token;
         byte[] postData = body.getBytes();  // URL encode necessary?
 
         JRDictionary tag = new JRDictionary();
         tag.put("action", "callTokenUrl");
         tag.put("tokenUrl", tokenUrl);
         tag.put("providerName", providerName);
 
         if (!JRConnectionManager.createConnection(tokenUrl, postData, this, true, tag)) {
             JREngageError error = new JREngageError(
                     "Problem initializing the connection to the token url",
                     AuthenticationError.AUTHENTICATION_FAILED,
                     ErrorType.AUTHENTICATION_FAILED);
 
             for (JRSessionDelegate delegate : getDelegatesCopy()) {
                 delegate.authenticationCallToTokenUrlDidFail(tokenUrl, error, providerName);
             }
         }
     }
 
     public void triggerAuthenticationDidCompleteWithPayload(JRDictionary rpx_result) {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerAuthenticationDidCompleteWithPayload]");
         }
 
         if (mCurrentlyAuthenticatingProvider == null) {
             return;
         }
 
         JRAuthenticatedUser user = new JRAuthenticatedUser(rpx_result, mCurrentlyAuthenticatingProvider.getName());
         mAuthenticatedUsersByProvider.put(mCurrentlyAuthenticatingProvider.getName(), user);
         JRDictionary.archive(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
 
         if (mBasicProviders.contains(mCurrentlyAuthenticatingProvider.getName())) {
             saveLastUsedBasicProvider();
         }
 
 //        if (mSocialProviders.contains(mCurrentlyPublishingProvider.getName())) {
 //            saveLastUsedSocialProvider();
 //        }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidComplete(
                     rpx_result.getAsDictionary("auth_info"),
                     mCurrentlyAuthenticatingProvider.getName());
         }
 
         String auth_info_token_for_token_url = rpx_result.getAsString("auth_info_token_for_token_url");
         if (!TextUtils.isEmpty(mTokenUrl)) {
             makeCallToTokenUrl(mTokenUrl, auth_info_token_for_token_url, mCurrentlyAuthenticatingProvider.getName());
         }
 
         setCurrentlyAuthenticatingProvider((String) null);
     }
 
     public void triggerAuthenticationDidFail(JREngageError error) {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerAuthenticationDidFailWithError]");
         }
 
         //todo if we've reached this point from JRLandingActivity.prepareUserInterface mCurrentlyAuthenticatingProvider will definitely
         //be null and this will raise a null pointer exception.
         String providerName = mCurrentlyAuthenticatingProvider.getName();
 
         setCurrentlyAuthenticatingProvider((String) null);
         mReturningBasicProvider = null;
         mReturningSocialProvider = null;
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidFail(error, providerName);
         }
     }
 
     public void triggerAuthenticationDidCancel() {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerAuthenticationDidCancel]");
         }
 
         setCurrentlyAuthenticatingProvider((String) null);
         mReturningBasicProvider = null;
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidCancel();
         }
     }
 
     public void triggerAuthenticationDidRestart() {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerAuthenticationDidCancel]");
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidRestart();
         }
     }
 
     public void triggerPublishingDidComplete() {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerPublishingDidComplete]");
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.publishingDidComplete();
         }
 
         mSocialSharing = false;
     }
 
 //    public void triggerPublishingDidFail(JREngageError error) {
 //        if (Config.LOGD) {
 //            Log.d(TAG, "[triggerPublishingDidFail]");
 //        }
 //
 //        //todo null on the next line should be a JRDictionary for the activity but there's no way to
 //        //get one which makes me question the wisdom of it being passed around as a JRDictionary to begin with
 //        for (JRSessionDelegate delegate : getDelegatesCopy()) {
 //            delegate.publishingActivityDidFail(null, error, mCurrentlyAuthenticatingProvider.getName());
 //        }
 //    }
 
     public void triggerPublishingDidCancel() {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerPublishingDidCancel]");
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.publishingDidCancel();
         }
     }
 
     /*
     TODO:  This method appears to have gone away.
     TODO RESPONSE: Um... pretty sure it just moved to a spot higher in the iPhone version of the file?
 
     private String appNameAndVersion() {
         final String FMT = "appName=%s.%s&version=%d_%s";
 
         Context ctx = JREngage.getContext();
         PackageManager pkgMgr = ctx.getPackageManager();
         PackageInfo pkgInfo = null;
         try {
             pkgInfo = pkgMgr.getPackageInfo(ctx.getPackageName(), 0);
         } catch (PackageManager.NameNotFoundException e) {
             Log.w(TAG, "[appNameAndVersion] package manager issue -> ", e);
         }
 
         return (pkgInfo != null)
                 ? String.format(FMT,
                     pkgInfo.packageName,
                     pkgInfo.applicationInfo.nonLocalizedLabel,
                     pkgInfo.versionCode,
                     pkgInfo.versionName)
                 : "";
     }
      */
 
     private synchronized List<JRSessionDelegate> getDelegatesCopy() {
         return (mDelegates == null)
                 ? new ArrayList<JRSessionDelegate>()
                 : new ArrayList<JRSessionDelegate>(mDelegates);
     }
 
     public JRProvider getCurrentlyPublishingProvider() {
         return mCurrentlyPublishingProvider;
     }
 
     public void setCurrentlyPublishingProvider(String provider) {
         Log.d(TAG, "[setCurrentlyPublishingProvider]: " + provider);
         mCurrentlyPublishingProvider = getProviderByName(provider);
     }
 }
